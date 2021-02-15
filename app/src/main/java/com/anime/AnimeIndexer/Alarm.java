package com.anime.AnimeIndexer;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.os.Build;
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
import java.util.Random;

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


        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        server=sharedPreferences.getString("server","192.168.0.211:16834/");

        prefs = readfile(context.getFilesDir().getAbsolutePath());
        for(Preferiti p : prefs){
           add_number_of_episode ad= new add_number_of_episode(prefs,context,prefs.lastIndexOf(p));
           ad.start();
            try {
                ad.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }


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
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_HOUR, pi);
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
                    List<Preferiti> su = new ArrayList<>(prefs);

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
        private final List<Preferiti> p;
        private final Context context;
        private final int i;


        public add_number_of_episode(List<Preferiti> p, Context context, int i) {
            this.p = prefs;
            this.context = context;
            this.i = i;
        }

        public void run() {
            try {
            } catch (Exception e) {
                e.printStackTrace();
            }
            String url = p.get(i).getUrl();
            final RequestQueue queue = Volley.newRequestQueue(context);
            url = url.replace('"', ' ');
            final int[] result = new int[1];
            try {


                String url2 = server + p.get(i).getSource() + "//nepi?url=" + url;
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

                                    if ((Integer.parseInt(response)>p.get(i).getNumperepisode())){
                                        Random randint = new Random();
                                        int a = randint.nextInt(200)+135772;
                                        createNotificationChannel(a,context);
                                        p.get(i).setNumperepisode(Integer.parseInt(response));
                                        NotificationManager notificationManager = (NotificationManager) context
                                                .getSystemService(Context.NOTIFICATION_SERVICE);


                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, String.valueOf(a))
                                                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                                                .setChannelId( String.valueOf(a))

                                                .setContentTitle("Maybe there are some new episode of"+ p.get(i).getTitle())
                                                .setContentText("Maybe there are some new episode of"+ p.get(i).getTitle())
                                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                                        notificationManager.notify(MID, builder.build());
                                        Toast.makeText(context, "finded new episode", Toast.LENGTH_LONG).show(); // For example




                                        MID++;


                                            writefile(prefs,context.getFilesDir().getAbsolutePath());


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


    private void createNotificationChannel(int i,Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "notification_indexer";
            String description = "new episode?";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(String.valueOf(i), name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
