package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.DownloadListener;
import com.androidnetworking.interfaces.DownloadProgressListener;
import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;

import com.google.android.material.snackbar.Snackbar;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;


public class FirstFragment extends Fragment implements SearchView.OnQueryTextListener {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    final String port = "5000";
    final String server = "http://192.168.0.111:";
    final List<List<String>> sresult = new ArrayList<>();
    final int maxdownload = 3;
    private final buttonlisener buttonl = new buttonlisener();
    public List<String> episodelist;
    String currentanime;
    String json;
    View view;
    LinearLayout ll;
    FileInputStream fi;
    ObjectInputStream oi;
    int prev = 0;
    int downloadnumber = 0;

    {


    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState

    ) {

        verifyStoragePermissions(getActivity());
        AndroidNetworking.initialize(getContext());
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public void onClickBtn(View v) {
        try {

            String testurl = sresult.get(Integer.parseInt((String) v.getTag())).get(0);
            new Details(sresult.get(Integer.parseInt((String) v.getTag())).get(0)).execute();
            Button bu = (Button) v;
            currentanime = bu.getText().toString().replace("\"", "");

        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }
    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SearchView search = view.findViewWithTag("ricerca");
        this.view = view;
        ll = view.findViewWithTag("wedr");
        search.setOnQueryTextListener(this);

        PRDownloader.initialize(getContext());
        PRDownloader.cleanUp(1);
        PRDownloaderConfig config = PRDownloaderConfig.newBuilder()
                .setDatabaseEnabled(true)
                .setReadTimeout(30_000)
                .setConnectTimeout(30_000)
                .build();
        PRDownloader.initialize(getContext(), config);
        File file = new File(getContext().getFilesDir() + File.pathSeparator + "myObjects.txt");
        System.out.println(getContext().getFilesDir());
        if (!file.exists()) {
            try {

                //file.createNewFile();

                System.out.println("file created");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {

            fi = new FileInputStream(file);
            oi = new ObjectInputStream(fi);


            episodelist = (List<String>) oi.readObject();

            System.out.println(episodelist.toString());
            oi.close();
            fi.close();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("File not found");
            episodelist = new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error initializing stream");
            episodelist = new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            episodelist = new ArrayList<>();
        }
        if (episodelist == null) {
            episodelist = new ArrayList<>();


        }

        System.out.println(this.getId());
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        sresult.clear();
        System.out.println(ll);

        ll.removeAllViews();

        try {


            new Search(s).execute();
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        return false;
    }

    public void newButtons() {

        List<Button> listabottoni = new ArrayList<>();
        for (int i = 0; i < sresult.size(); i++) {
            Button myButton = new Button(getContext());
            myButton.setTag(String.valueOf(i));
            myButton.setText(sresult.get(i).get(1));
            myButton.setOnClickListener(buttonl);
            listabottoni.add(myButton);
        }


        System.out.println(ll);
        for (Button myButton : listabottoni
        ) {
            ll.addView(myButton);
        }
        listabottoni.clear();


    }

    public void startdownload(View view) {
        this.view = view;
        Button bu = (Button) view;
        bu.setTextColor(Color.BLUE);
        if (!episodelist.contains((String) view.getTag())) {
            episodelist.add((String) view.getTag());
        }
        new savefile().run();
        new downloadTask(getActivity(), currentanime, bu.getText().toString()).execute((String) view.getTag());


        System.out.println((String) view.getTag());


    }

    public class savefile extends Thread {


        public void run() {
            File file = new File(getContext().getFilesDir() + File.pathSeparator + "myObjects.txt");
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
                List su = new ArrayList(episodelist);

                o.writeObject(su);

                o.close();
                f.close();


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class Search extends AsyncTask {

        final String Searchterm;

        public Search(String Searchterm) {

            this.Searchterm = Searchterm;

        }

        @Override
        protected Object doInBackground(Object... arg0) {
            try {
                if (Build.VERSION.SDK_INT > 22) {
                    requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, 1);
                }

                RequestQueue queue = Volley.newRequestQueue(getContext());
                String url = server + port + "//q?q=" + Searchterm;

// Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                System.out.println("sadsad");
                                json = response;
                                System.out.println(json);
                                try {
                                    String[] items = json.split("\\s*], \\s*");
                                    for (String i : items
                                    ) {
                                        i = i.replace('[', ' ');
                                        i = i.replace(']', ' ');
                                        List<String> items2 = Arrays.asList(i.split("\\s*, \\s*"));
                                        sresult.add(items2);


                                    }
                                    newButtons();

                                } catch (Exception e) {

                                    System.out.println(e.toString());
                                }
                                System.out.println("asd");
                                System.out.println(sresult);

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println(error.getMessage());
                    }
                });
                System.out.println("sfdad");


// Add the request to the RequestQueue.
                queue.add(stringRequest);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }


    }

    public class Details extends AsyncTask {

        String url2;


        public Details(String url2) {

            this.url2 = url2;
            sresult.clear();
            ll.removeAllViews();
        }

        @Override
        protected Object doInBackground(Object... arg0) {

            try {
                if (Build.VERSION.SDK_INT > 22) {
                    requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, 1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            final RequestQueue queue = Volley.newRequestQueue(getContext());
            url2 = url2.replace('"', ' ');
            try {


                final List listaepisode = new ArrayList();
                String url = server + port + "//dettagli?url=" + url2;
                url = url.replaceAll("\\s+", "");

// Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                System.out.println("sadsad");
                                json = response;
                                System.out.println(json);
                                try {

                                    List<String> items = Arrays.asList(json.split("\\s*, \\s*"));
                                    List<String> item2 = new ArrayList<>();
                                    for (String i : items
                                    ) {
                                        i = i.replace('"', ' ');
                                        i = i.replace('[', ' ');
                                        i = i.replace(']', ' ');
                                        i = i.replaceAll("\\s+", "");
                                        
                                        System.out.println(i);
                                        item2.add(i);
                                    }


                                    new savefile().run();

                                    System.out.println(items);
                                    items = item2;
                                    System.out.println(items);
                                    List<Button> listabottoni = new ArrayList<>();
                                    for (int i = 0; i < items.size(); i++) {
                                        Button myButton = new Button(getContext());
                                        myButton.setTag(items.get(i));
                                        myButton.setText(String.valueOf(i + 1));
                                        if (episodelist.contains(items.get(i)))
                                            myButton.setTextColor(Color.BLUE);
                                        //myButton.setTextColor(Color.parseColor("99FFFFFFF"));
                                        myButton.setOnClickListener(new buttonlisener2());
                                        listabottoni.add(myButton);
                                    }


                                    System.out.println(ll);
                                    for (Button myButton : listabottoni
                                    ) {
                                        ll.addView(myButton);
                                    }
                                    listabottoni.clear();


                                } catch (Exception e) {

                                    System.out.println(e.toString());
                                }
                                System.out.println("asd");
                                System.out.println();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println(error.getMessage());
                    }
                });
                System.out.println("sfdad");
                stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                        100000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                System.out.println("sfdad");

// Add the request to the RequestQueue.
                queue.add(stringRequest);

            } catch (Exception e) {
                e.printStackTrace();
            }
            new savefile().run();

            return null;
        }


    }

    private class downloadTask extends AsyncTask<String, Integer, String> {
        private final String currentanime;
        private final String numero;
        int prev = 0;
        List channelList = new ArrayList();
        private Context context;
        private NotificationManager mNotifyManager;
        private NotificationCompat.Builder mBuilder;
        private String chanel_id;

        public downloadTask(Context context, String currentanime, String numero) {
            this.context = context;
            this.currentanime = currentanime+" ";
            this.numero = numero;



        }

        @Override
        protected String doInBackground(String... sUrl) {

            File directory = new File(Environment.getExternalStorageDirectory() + File.separator + "anime" + File.separator + currentanime);


            File file = new File(directory.getPath() + File.separator + currentanime + numero + ".mp4");
            if (file.exists()) {


                int vlcRequestCode = 42;
                Uri uri = Uri.parse(file.getPath());
                Intent vlcIntent = new Intent(Intent.ACTION_VIEW);
                vlcIntent.setDataAndTypeAndNormalize(uri, "video/*");
                vlcIntent.putExtra("title", currentanime);
                PackageManager packageManager = getContext().getPackageManager();
                List<ResolveInfo> activities = packageManager.queryIntentActivities(vlcIntent,
                        PackageManager.MATCH_DEFAULT_ONLY);

                if (activities.size() > 0) {
                    startActivityForResult(vlcIntent, vlcRequestCode);
                } else {
                    Snackbar snackBar = Snackbar.make(view, "An Error Occurred! missing video player", Snackbar.LENGTH_LONG).setAction("RETRY", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        }
                    });
                    snackBar.setActionTextColor(Color.BLUE);
                    View snackBarView = snackBar.getView();
                    TextView textView = snackBarView.findViewById(R.id.snackbar_text);
                    textView.setTextColor(Color.RED);
                    snackBar.show();
                    System.out.println("error");


                }

            } else {
                while (downloadnumber >= maxdownload) {

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

                downloadnumber++;
                Random rand = new Random();
                chanel_id = String.valueOf(rand.nextInt(5000));

                mNotifyManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                mBuilder = new NotificationCompat.Builder(context, chanel_id);
                mBuilder.setContentTitle("Picture Download")
                        .setContentText("Download in progress")
                        .setSmallIcon(R.drawable.ic_launcher_foreground);

                CharSequence name = "Channel Name";
                String description = "Chanel Description";
                int importance = NotificationManager.IMPORTANCE_LOW;
                NotificationChannel mChannel = new NotificationChannel(chanel_id, name, importance);
                mChannel.setDescription(description);
                mChannel.enableLights(true);
                mChannel.setLightColor(Color.BLUE);
                mNotifyManager.createNotificationChannel(mChannel);
                mBuilder = new NotificationCompat.Builder(context, chanel_id);
                mBuilder.setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("Downloading" + currentanime + "Ep " + numero)
                        .setContentText("Notification Body")
                        .setAutoCancel(true);

                AndroidNetworking.download(sUrl[0], file.getParentFile().getAbsolutePath(), file.getName())
                        .setTag("downloadTest")
                        .setPriority(Priority.MEDIUM)
                        .build()
                        .setDownloadProgressListener(new DownloadProgressListener() {
                            int prev = 0;

                            @Override
                            public void onProgress(long bytesDownloaded, long totalBytes) {
                                // do anything with progress
                                System.out.println(prev);
                                System.out.println((int) ((double) bytesDownloaded / (int) totalBytes * 100));
                                System.out.println(prev != (int) ((double) bytesDownloaded / (int) totalBytes * 100));
                                if (prev != (int) ((double) bytesDownloaded / (int) totalBytes * 100)) {
                                    System.out.println((prev != (int) ((double) bytesDownloaded / (int) totalBytes * 100)) + "ad");

                                    DecimalFormat df = new DecimalFormat();
                                    df.setMaximumFractionDigits(2);
                                    prev = (int) ((double) bytesDownloaded / (int) totalBytes * 100);
                                    mBuilder.setProgress(100, (int) ((double) bytesDownloaded / (int) totalBytes * 100), false);
                                    mBuilder.setContentText(String.valueOf(df.format(((double) bytesDownloaded / (int) totalBytes * 100)) + "%"));
                                    mNotifyManager.notify(Integer.parseInt(chanel_id), mBuilder.build());
                                }
                            }
                        })
                        .startDownload(new DownloadListener() {
                            @Override
                            public void onDownloadComplete() {
                                downloadnumber--;
                                // do anything after completion
                                mBuilder.setContentText("download complete")
                                        // Removes the progress bar
                                        .setProgress(0,0,false);
                                mNotifyManager.notify(Integer.parseInt(chanel_id), mBuilder.build());
                            }

                            @Override
                            public void onError(ANError error) {
                                downloadnumber--;
                                mBuilder.setContentText("error")
                                        // Removes the progress bar
                                        .setProgress(0,0,false);
                                mNotifyManager.notify(Integer.parseInt(chanel_id), mBuilder.build());
                                // handle error
                            }
                        });


            }


            return null;
        }


    }

    public class buttonlisener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            onClickBtn(view);
        }
    }

    public class buttonlisener2 implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            try {
                startdownload(view);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }


}






