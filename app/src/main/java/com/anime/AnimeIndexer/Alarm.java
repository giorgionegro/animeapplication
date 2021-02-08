package com.anime.AnimeIndexer;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.os.PowerManager;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Alarm extends BroadcastReceiver
{
    //final String server = "http://serverparan.ddns.net:";
     String server = "http://192.168.0.211:16834";
    List<Preferiti> prefs;
    private int MID=0;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "test:");
        wl.acquire();
        Toast.makeText(context, "Alarm !!!!!!!!!!", Toast.LENGTH_LONG).show(); // For example


        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        server=sharedPreferences.getString("server","192.168.0.211:16834/");

        prefs = readfile(context.getFilesDir().getAbsolutePath());
        for(Preferiti p : prefs){
            new add_number_of_episode(p,context).start();








        }
        writefile(prefs,context.getFilesDir().getAbsolutePath());


        wl.release();
    }

    public void setAlarm(Context context)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 18);
        calendar.set(Calendar.MINUTE, 30);
        calendar.set(Calendar.SECOND, 0);
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, Alarm.class);
        context.startService(i);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_HALF_DAY, pi);
    }

    public void cancelAlarm(Context context)
    {
        Intent intent = new Intent(context, Alarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }



    public static List<Preferiti> readfile(String filedir){
        List<Preferiti> prefs;
        File file = new File( filedir+ File.pathSeparator + "prefs");
        if (!file.exists()) {
            try {
                file.createNewFile();
                System.out.println("file created");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            FileInputStream fi = new FileInputStream(file);
            ObjectInputStream oi = new ObjectInputStream(fi);
            prefs = (List<Preferiti>) oi.readObject();
            System.out.println(prefs.toString());
            oi.close();
            fi.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("File not found");
            prefs = new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error initializing stream");
            prefs = new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            prefs = new ArrayList<>();
        }
        if (prefs == null) {
            prefs = new ArrayList<>();
        }
        return prefs;
    }

    public static void writefile(final List<Preferiti> prefs, final String filedir){
        class savefilep extends Thread {

            public void run() {
                File file = new File(filedir + File.pathSeparator + "prefs");
                if (!file.exists()) {
                    try {

                        file.createNewFile();

                        System.out.println("file created");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    FileOutputStream f = new FileOutputStream(file);
                    ObjectOutputStream o = new ObjectOutputStream(f);
                    System.out.println("File opened");
                    // Write objects to file
                    List su = new ArrayList(prefs);

                    o.writeObject(su);

                    o.close();
                    f.close();


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        new savefilep().start();
    }


    class add_number_of_episode extends Thread {
        private final Preferiti p;
        private final Context context;
        public add_number_of_episode(Preferiti p, Context context) {
            this.p = p;
            this.context = context;
        }

        public void run() {
            try {
            } catch (Exception e) {
                e.printStackTrace();
            }
            String url = p.getUrl();
            final RequestQueue queue = Volley.newRequestQueue(context);
            url = url.replace('"', ' ');
            final int[] result = new int[1];
            try {


                String url2 = server + p.getSource() + "//nepi?url=" + url;
                url2 = url2.replaceAll("\\s+", "");
                final List<String> listforfab = new ArrayList<>();
// Request a string response from the provided URL.
                System.out.println(url2);
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url2,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                System.out.println("sadsad");
                                try {

                                    if (!(Integer.parseInt(response)>p.getNumperepisode())){
                                        NotificationManager notificationManager = (NotificationManager) context
                                                .getSystemService(Context.NOTIFICATION_SERVICE);

                                        p.setNumperepisode(Integer.parseInt(response));
                                        NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(
                                                context).setSmallIcon(R.mipmap.ic_launcher_round)
                                                .setContentTitle("Alarm Fired")
                                                .setContentText("new episode"+p.getTitle())
                                                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});
                                        notificationManager.notify(MID, mNotifyBuilder.build());
                                        MID++;



                                    }


                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                System.out.println("asd");
                                System.out.println();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, "Error on reaching server please retry later", Toast.LENGTH_SHORT).show();
                        System.out.println(error.getMessage());
                    }
                });

                stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                        10000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


// Add the request to the RequestQueue.
                queue.add(stringRequest);

            } catch (Exception e) {
                e.printStackTrace();
            }


        }


    }
}
