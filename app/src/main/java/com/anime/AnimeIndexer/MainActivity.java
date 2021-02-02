package com.anime.AnimeIndexer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import androidx.fragment.app.Fragment;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    final List<List<String>> sresult = new ArrayList<>();
    final String port = "16384";
   // final String server = "http://serverparan.ddns.net:";
    final String server = "http://192.168.0.250:";
    final int Width = 250;
    private final int downloadnumber = 0;
    private final Context context2 = this;
    private final buttonlistener buttonl = new buttonlistener();
    public Context context;
    String source;
    View view2;
    FileInputStream fi;
    ObjectInputStream oi;
    bitmaplist listabitm;
    DatabaseHelper dbh;
    Bitmap img;
    private String json;
    private LinearLayout ll;
    private String currentanime;
    private List episodelist;
    private String currentnanimeforfragment;
    private String urlforfragment;
    private List listforfab;
    static Fragment f;
    public void setterfor2fragment(String url) {
        this.urlforfragment = url;


    }

    public void fablistsetter(List l) {
        listforfab = l;
    }

    public List getter() {


        List result = new ArrayList();
        result.add(urlforfragment);
        result.add(currentnanimeforfragment);


        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_MaterialComponents_DayNight); //imposta tema scuro
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        source = sharedPreferences.getString("list_preference_1", "/aw/");//prendo la sorgente dalle preferenze
        System.err.println("settings");


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Toolbar toolbar = findViewById(R.id.toolbar);
        // setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab); // cerco il floating button
        fab.setOnClickListener(new View.OnClickListener() { // aggiungo lissener
            @Override
            public void onClick(View view) {

                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(view.getContext());
                source = sharedPreferences.getString("list_preference_1", "/aw/");

                if (listforfab == null) return;
                System.out.println(listforfab.toString());
                List<String> l = listforfab;
                String link = "";
                for (String s : l
                ) {
                    link = link + s + "<line>";


                }
                System.out.println(link);
                try {

                    ActivityManager manager = (ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE);
                    manager.killBackgroundProcesses("com.dv.adm.A Editor");


                    Intent intent = new Intent("android.intent.action.MAIN");
                    intent.setClassName("com.dv.adm", "com.dv.adm.AEditor");
                    intent.putExtra("com.dv.get.ACTION_LIST_ADD", link); // or "url1<line>url2...", or "url1<info>name_ext1<line>..."
// optional
                    intent.putExtra("com.dv.get.ACTION_LIST_PATH", Environment.getExternalStorageDirectory() + File.separator + "anime" + File.separator + currentnanimeforfragment); // destination directory (default "Settings - Downloading - Folder for files")

                    intent.putExtra("com.dv.get.ACTION_LIST_OPEN", false);
                    try {

                        startActivity(intent);


                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                        Log.w("my_app", "not found");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    // new Allepisode().start();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                // fragment.onClicklatest(fragment.view);
            }

        });

        FragmentManager fm = getSupportFragmentManager();
        context = fm.getFragments().get(0).getContext();
        dbh = new DatabaseHelper(this);
        //new downloadallimage().start();
        File file = new File(this.getApplicationContext().getFilesDir() + File.pathSeparator + "myObjects.txt");//file episodi gia' scaricati
        if (!file.exists()) {
            try {

                file.createNewFile();

                System.out.println("file created");
            } catch (IOException e) {
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
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            episodelist = new ArrayList<>();
        }
        if (episodelist == null) {
            episodelist = new ArrayList<>();


        }
        listabitm = new bitmaplist();


       /* new Thread() {
            @Override
            public void run() {

*//*
                try {
                   // listabitm = dbh.getAllbitmaps();
                } catch (InterruptedException e) {
                //    e.printStackTrace();
                }
*//*
            }


        }.start();*/


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public  boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        source = sharedPreferences.getString("list_preference_1", "/aw/");
        //noinspection SimplifiableIfStatement

            FragmentManager fm = getSupportFragmentManager();
         
        if (id == R.id.latest) {
            try {
                NavHostFragment.findNavController(fm.getFragments().get(0))
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
                System.out.println("good");
                Thread.sleep(1000);
            }catch(Exception e){
                NavHostFragment.findNavController(fm.getFragments().get(0))
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
                NavHostFragment.findNavController(fm.getFragments().get(0))
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);


                System.out.println(e.getMessage());
            }
           view2 = fm.getFragments().get(0).getView();
            context = fm.getFragments().get(0).getContext();// prendo il context
            ll =(LinearLayout) ((ScrollView)view2.findViewById(R.id.elenco_anime)).getChildAt(0);

            try {
                androidx.fragment.app.Fragment f = (androidx.fragment.app.Fragment) fm.getFragments().get(0);

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } else if (id == R.id.action_settings) {
            System.out.println("asd");
            Intent intent = new Intent(this,
                    SettingsActivity.class);
            startActivity(intent);


        }

        return super.onOptionsItemSelected(item);
    }
public void latest(Context c){





}
    public void onClickBtn(View v) {
        try {
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(this);
            source = sharedPreferences.getString("list_preference_1", "/aw/");
            String testurl = (String) v.getTag();
            new Details((String) v.getTag()).execute();
            Button bu = (Button) v;
            currentanime = bu.getText().toString().replace("\"", "").replaceAll("[^a-zA-Z0-9]", " ");

        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }
    }

    public void newButtons() {

        List<Button> listabottoni = new ArrayList<>();
        for (int i = 0; i < sresult.size(); i++) {
            Button myButton = new Button(context);
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

    private class DownloadTask extends Thread {


        private final String currentanime;
        private final String[] sUrl;
        private final int maxdownload = 3;
        protected String numero;
        int prev = 0;
        private NotificationManager mNotifyManager;
        private NotificationCompat.Builder mBuilder;
        private String chanel_id;

        public DownloadTask(Context context, String currentanime, String... sUrl) {
            numero = currentanime.split(" ")[currentanime.split(" ").length - 1].trim();
            this.currentanime = currentanime.split("Episodio")[0].replaceAll("[^a-zA-Z0-9]", " ");

            this.sUrl = sUrl;

        }


        public void run() {


            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection;

            URL url = null;
            try {
                url = new URL(sUrl[0]);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            File file = new File(Environment.getExternalStorageDirectory() + File.separator + "anime" + File.separator + currentanime + File.separator + FilenameUtils.getName(url.getPath()));
            if (!file.exists()) {
                Intent intent = new Intent(MainActivity.this, BackgroundService.class);
                Intent intent1 = intent.setClassName("com.dv.adm", "com.dv.adm.AEditor");


// ------ 2 --- single and batch addition without Editor opening
                intent1.putExtra("com.dv.get.ACTION_LIST_ADD", sUrl[0]); // or "url1<line>url2...", or "url1<info>name_ext1<line>..."
// optional
                intent1.putExtra("com.dv.get.ACTION_LIST_PATH", Environment.getExternalStorageDirectory() + File.separator + "anime" + File.separator + currentanime); // destination directory (default "Settings - Downloading - Folder for files")
                intent1.putExtra("com.dv.get.ACTION_LIST_OPEN", false);
                intent1.putExtra("com.android.extra.filename", currentanime + numero + ".mp4");
                try {
                    System.out.println("starting service");

                    startActivity(intent1);

                } catch (ActivityNotFoundException e) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + "com.dv.adm")));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + "com.dv.adm")));
                    }
                    e.printStackTrace();
                    Log.w("my_app", "not found");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                int vlcRequestCode = 42;
                Uri uri = Uri.parse(file.getPath());
                Intent vlcIntent = new Intent(Intent.ACTION_VIEW);
                vlcIntent.setDataAndTypeAndNormalize(uri, "video/*");
                vlcIntent.putExtra("title", currentanime);
                PackageManager packageManager = context.getPackageManager();
                List<ResolveInfo> activities = packageManager.queryIntentActivities(vlcIntent,
                        PackageManager.MATCH_DEFAULT_ONLY);

                String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(".mp4");

                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(file.getPath()), "video/*");
                startActivityForResult(intent, 10);

                if (activities.size() > 0) {
                    //startActivityForResult(vlcIntent, vlcRequestCode);
                } else {

                    Toast.makeText(context, "An Error Occurred! missing video player", Toast.LENGTH_SHORT).show();

                    System.out.println("error");


                }


            }

        }


    }

    public class buttonlisener2 implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            try {
                new startdownload(view).run();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private class startdownload extends Thread {
        final View view;


        public startdownload(View view) {
            this.view = view;

        }


        public void run() {

            Button bu = (Button) view;
            final DownloadTask downloadTask = new DownloadTask(context, bu.getText().toString(), (String) view.getTag());

            downloadTask.run();
            System.out.println((String) view.getTag());


        }

    }

    private class Allepisode extends Thread {


        private List<link> participantJsonList;

        public Allepisode() {


        }

        @Override
        public void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            try {
                if (Build.VERSION.SDK_INT > 22) {
                    requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, 1);
                }

                RequestQueue queue = Volley.newRequestQueue(context);
                String url = server + port + source + "//allseries";

// Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                System.out.println("sadsad");
                                json = response;
                                System.out.println(json);
                                ObjectMapper mapper = new ObjectMapper();
                                Gson gson = new Gson();

                                try {
                                    Type userListType = new TypeToken<ArrayList<link>>() {
                                    }.getType();
                                    participantJsonList = gson.fromJson(response, userListType);


                                } catch (Exception e) {
                                    Toast.makeText(context,e.toString(),Toast.LENGTH_SHORT).show();

                                    System.out.println(e.toString());
                                }
                                System.out.println("asd");
                                System.out.println(sresult);

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context,"Error on reaching server please retry later",Toast.LENGTH_SHORT).show();

                        System.out.println(error.getMessage());
                    }
                });
                System.out.println("sfdad");
                stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                        1000000000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

// Add the request to the RequestQueue.
                queue.add(stringRequest);

            } catch (Exception e) {
                Toast.makeText(context,e.toString(),Toast.LENGTH_SHORT).show();
            }


        }

        protected void onPostExecute(int result) {
            // NO NEED to use activity.runOnUiThread(), code execute here under UI thread.

            // Updating parsed JSON data into ListView

            // updating listview

        }

    }

    public class buttonlistener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            onClickBtn(view);
        }
    }



    @SuppressWarnings("rawtypes")
    @SuppressLint("StaticFieldLeak")
    private class Latest extends Thread {
        String url2;

        public Latest(View view) {

            this.url2 = url2;

        }


        @Override

        public void run() {
            try {
                if (Build.VERSION.SDK_INT > 22) {
                    requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, 1);
                }
                try {
                    Thread.sleep(0);
                } catch (Exception e) {
                    Toast.makeText(context,e.toString(),Toast.LENGTH_SHORT).show();
                }
                RequestQueue queue = Volley.newRequestQueue(context);
                String url = server + port + source + "/latest";
                System.out.println(url);
// Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        ll.removeAllViews();
                        //System.out.println("sadsad");
                        sresult.clear();
                        json = response;
                        System.out.println(json);
                       // ObjectMapper mapper = new ObjectMapper();

                        try {

                            String[] items = json.split("\\s*], \\s*");

                            for (String i : items
                            ) {
                                i = i.replace("\"", "");
                                i = i.replace("[", "");
                                i = i.replace("]", "");
                                i = i.replace("Streaming & Download SUB ITA - AnimeWorld", "");


                                List<String> items2 = Arrays.asList(i.split("\\s*,\\s*"));
                                System.out.println(i);
                                sresult.add(items2);


                            }
                            List<button> listabottoni = new ArrayList<>();
                            for (int i = 0; i < sresult.size(); i++) {
                                button myButton = new button(context, sresult.get(i).get(2));
                                myButton.setTag(sresult.get(i).get(0));
                                myButton.setText(sresult.get(i).get(1));
                                myButton.setOnClickListener(new buttonlisener2());
                                ll.addView(myButton);
                            }


                            System.out.println(ll);
                            listabottoni.clear();

                        } catch (Exception e) {
                            Toast.makeText(context,e.toString(),Toast.LENGTH_SHORT).show();
                            System.out.println(e.toString());
                        }
                        System.out.println("asd");
                        System.out.println(sresult);

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println(error.getStackTrace());
                        Toast.makeText(context,"Error on reaching server, maybe there are no new episode D:, if not  please retry later",Toast.LENGTH_SHORT).show();

                    }
                });


                stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                        1000000000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                System.out.println("sfdad");


// Add the request to the RequestQueue.
                queue.add(stringRequest);

            } catch (Exception e) {
                System.err.println(e);
                Toast.makeText(context,e.toString(),Toast.LENGTH_SHORT).show();
            }


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
                Toast.makeText(context,e.toString(),Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            final RequestQueue queue = Volley.newRequestQueue(context);
            url2 = url2.replace('"', ' ');
            try {


                final List listaepisode = new ArrayList();
                String url = server + port + source + "//dettagli?url=" + url2;
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


                                    new saveepisodelist().run();

                                    System.out.println(items);
                                    items = item2;
                                    System.out.println(items);
                                    List<Button> listabottoni = new ArrayList<>();
                                    for (int i = 0; i < items.size(); i++) {
                                        Button myButton = new Button(context);
                                        myButton.setTag(items.get(i));
                                        myButton.setText(String.valueOf(i + 1));
                                        if (episodelist.contains(items.get(i)))
                                            myButton.setTextColor(Color.BLUE);
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
                                    Toast.makeText(context,e.toString(),Toast.LENGTH_SHORT).show();
                                    System.out.println(e.toString());
                                }
                                System.out.println("asd");
                                System.out.println();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println(error.getMessage());
                        Toast.makeText(context,"Error on reaching server, maybe there are no new episode D:, if not  please retry later",Toast.LENGTH_SHORT).show();

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
                Toast.makeText(context,e.toString(),Toast.LENGTH_SHORT).show();
            }
            new saveepisodelist().run();

            return null;
        }


    }


    public class saveepisodelist extends Thread {


        public void run() {
            File file = new File(context.getFilesDir() + File.pathSeparator + "myObjects.txt");
            if (!file.exists()) {
                try {

                    file.createNewFile();

                    System.out.println("file created");
                } catch (IOException e) {
                    Toast.makeText(context,e.toString(),Toast.LENGTH_SHORT).show();
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
                Toast.makeText(context,e.toString(),Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context,e.toString(),Toast.LENGTH_SHORT).show();
            }
            if (episodelist == null) {
                episodelist = new ArrayList<>();


            }
        }
    }

    public class savebitlist extends Thread {
        String imgurl;
        Bitmap x;

        public savebitlist(String imgurl, Bitmap x) {
            this.imgurl = imgurl;
            this.x = x;
        }

        public void run() {
            dbh.addEntry(imgurl, x);
        }
    }

    public class respose implements Response.Listener<Bitmap> {
        MaterialButton b;

        public respose(MaterialButton b) {
            System.out.println("asd");
            this.b = b;


        }

        @Override
        public void onResponse(Bitmap response) {
            b.setIcon(new BitmapDrawable(getResources(), response));

        }
    }

    public class button extends MaterialButton {
        public String imgurl;

        public button(@NonNull Context context, String imgurl) {
            super(context);
            this.imgurl = imgurl;
            if (listabitm.getbitbylink(this.imgurl) == null) {
                downsetbitmap dsb = new downsetbitmap(this);
                dsb.start();
                try {
                    dsb.join();
                } catch (InterruptedException e) {
                    Toast.makeText(context,e.toString(),Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            } else {

                Bitmap b = listabitm.getbitbylink(this.imgurl);
                float aspectRatio = b.getWidth() /
                        (float) b.getHeight();

                int height = Math.round(Width / aspectRatio);

                b = Bitmap.createScaledBitmap(
                        b, Width, height, false);


                this.setIconTint(null);
                this.setIcon(new BitmapDrawable(Resources.getSystem(), b));
            }
        }

        public class downsetbitmap extends Thread {
            button b;

            public downsetbitmap(button b) {
                this.b = b;

            }

            public void run() {
                final Bitmap x;

                HttpURLConnection connection = null;
                try {
                    connection = (HttpURLConnection) new URL(imgurl).openConnection();

                } catch (IOException e) {
                    Toast.makeText(context,e.toString(),Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    System.err.println(e.getMessage());
                }
                try {
                    connection.connect();
                } catch (Exception e) {
                    Toast.makeText(context,e.toString(),Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                InputStream input = null;
                try {
                    input = connection.getInputStream();
                } catch (Exception e) {
                    Toast.makeText(context,e.toString(),Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

                x = BitmapFactory.decodeStream(input);
                if (x != null) {
                    listabitm.add(imgurl, x);
                    new savebitlist(imgurl, x).run();
                }
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Bitmap bit = x;
                        if (bit != null) {
                            float aspectRatio = bit.getWidth() /
                                    (float) bit.getHeight();

                            int height = Math.round(Width / aspectRatio);

                            bit = Bitmap.createScaledBitmap(
                                    bit, Width, height, false);
                            b.setIconTint(null);
                            b.setIcon(new BitmapDrawable(Resources.getSystem(), bit));



                        }
                    }
                });


            }

        }


    }

    public class downloadallimage extends Thread {


        private List<String> participantJsonList;

        public void run() {
            System.out.println("sfdad");
            try {
                RequestQueue queue = Volley.newRequestQueue(context2);
                String url = server + port + source + "//allimg";

// Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                System.out.println("sadsad");
                                json = response;
                                System.out.println(json);
                                ObjectMapper mapper = new ObjectMapper();
                                Gson gson = new Gson();

                                try {
                                    Type userListType = new TypeToken<ArrayList<linkimg>>() {
                                    }.getType();
                                    participantJsonList = Arrays.asList(json.replace("{", "").replace("img", "").replace('"', ' ').replace(":", "").replace("}", "").split("\\s*, \\s*"));


                                } catch (Exception e) {
                                    Toast.makeText(context,e.toString(),Toast.LENGTH_SHORT).show();

                                    System.out.println(e.toString());
                                }
                                System.out.println("asd");
                                System.out.println(participantJsonList);


                                new Thread() {

                                    @Override
                                    public void run() {
                                        if (participantJsonList != null)
                                            for (String imgurl : participantJsonList
                                            ) {


                                                imgurl = imgurl.replace("https//", "https://img");

                                                final Bitmap x;

                                                HttpURLConnection connection = null;
                                                InputStream input = null;
                                                try {
                                                    connection = (HttpURLConnection) new URL(imgurl).openConnection();


                                                    connection.connect();

                                                    input = connection.getInputStream();
                                                } catch (Exception e) {
                                                    Toast.makeText(context,e.toString(),Toast.LENGTH_SHORT).show();
                                                    e.printStackTrace();
                                                }

                                                x = BitmapFactory.decodeStream(input);
                                                if (x != null) {
                                                    listabitm.add(imgurl, x);
                                                    new savebitlist(imgurl, x).run();
                                                    try {
                                                        sleep(10);
                                                    } catch (InterruptedException e) {
                                                        Toast.makeText(context,e.toString(),Toast.LENGTH_SHORT).show();
                                                        e.printStackTrace();
                                                    }
                                                }


                                            }

                                    }
                                }.start();


                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context,"Error on reaching server, maybe there are no new episode D:, if not  please retry later",Toast.LENGTH_SHORT).show();

                        error.printStackTrace();
                        System.err.println("errore volley");
                    }
                });
                System.out.println("sfdad");
                stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                        1000000000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

// Add the request to the RequestQueue.
                queue.add(stringRequest);

            } catch (Exception e) {
                Toast.makeText(context,e.toString(),Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            try {
                sleep(500);
            } catch (InterruptedException e) {
                Toast.makeText(context,e.toString(),Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }


        }


    }


    public class t1 extends Thread {
        List<link> participantJsonList;

        public t1(List<link> participantJsonList) {
            this.participantJsonList = participantJsonList;

        }

        public void run() {
            List<button> listabottoni = new ArrayList<>();
            for (int i = 0; i < participantJsonList.size(); i++) {
                button myButton = new button(context, participantJsonList.get(i).img);
                myButton.setTag(participantJsonList.get(i).getLink());
                myButton.setText(participantJsonList.get(i).getTitle());
                myButton.setOnClickListener(buttonl);
                listabottoni.add(myButton);
            }


            System.out.println(ll);
            ll.removeAllViews();
            for (Button myButton : listabottoni
            ) {
                ll.addView(myButton);
            }
            listabottoni.clear();


        }

    }


}