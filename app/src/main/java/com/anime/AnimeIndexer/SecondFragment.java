package com.anime.AnimeIndexer;

import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SecondFragment extends Fragment implements ViewTreeObserver.OnScrollChangedListener {


    final String port = "16384";
    //  final String server = "http://serverparan.ddns.net:";
    final String server = "http://192.168.0.250:";
    final List<List<String>> sresult = new ArrayList<>();
    public List<String> episodelist;
    String currentanime;
    String json;
    View view;
    LinearLayout ll;
    FileInputStream fi;
    ObjectInputStream oi;
    String url;
    DatabaseHelper dbh;
    ScrollView scrollView;
    int chunk;
    int chunckrequested;
    private String source;


    public SecondFragment() {
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_second, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

        chunk = 0;
        chunckrequested = 0;
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getActivity());

        source = sharedPreferences.getString("list_preference_1", "/aw/");

        MainActivity ma = (MainActivity) getActivity();
        List l = ma.getter();
        url = (String) l.get(0);
        currentanime = (String) l.get(1);
        ll = view.findViewById(R.id.elencoepisodi);
        ll.removeAllViews();
        scrollView = (ScrollView) ll.getParent();
        scrollView.getViewTreeObserver().addOnScrollChangedListener(this);
        File file = new File(getContext().getFilesDir() + File.pathSeparator + "myObjects.txt");
        System.out.println(getContext().getFilesDir());
        if (!file.exists()) {
            try {

                file.createNewFile();

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
        new Details().execute();


        super.onViewCreated(view, savedInstanceState);

    }

    public void startdownload(View view) {
        this.view = view;
        Button bu = (Button) view;
        bu.setTextColor(Color.BLUE);
        currentanime = (((String) view.getTag()).split("/"))[((String) view.getTag()).split("/").length - 2];
        if (!episodelist.contains(view.getTag())) {
            episodelist.add((String) view.getTag());
        }
        new savefile().run();
        new downloadTask(getActivity(), currentanime, bu.getText().toString()).execute((String) view.getTag());


        System.out.println((String) view.getTag());


    }

    @Override
    public void onScrollChanged() {
        System.out.println("bottom");
        View view2 = scrollView.getChildAt(scrollView.getChildCount() - 1);
        int bottomDetector = view2.getBottom() - (scrollView.getHeight() + scrollView.getScrollY());
        if (bottomDetector == 0 && chunckrequested == chunk) {
            chunk++;
            new Details().execute();
            Toast.makeText(getContext(), "reaching server for new episode", Toast.LENGTH_SHORT).show();
            System.out.println("bottom");

        }

    }

    public class Details extends AsyncTask {


        public Details() {


            sresult.clear();

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
            url = url.replace('"', ' ');
            try {


                final List listaepisode = new ArrayList();
                String url2 = server + port + source + "//dettagli?url=" + url + "&chunk=" + chunk;
                url2 = url2.replaceAll("\\s+", "");
                final List listforfab = new ArrayList();
// Request a string response from the provided URL.
                System.out.println(url2);
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url2,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                System.out.println("sadsad");
                                json = response;
                                System.out.println(json);
                                try {
                                    List<String> items = Arrays.asList(json.split("\\s*], \\s*"));
                                    ArrayList<List> item2 = new ArrayList<>();

                                    for (String s : items) {
                                        item2.add(Arrays.asList(s.split("\\s*, \\s*")));


                                    }
                             /*       for (int j =0; j<(item2.size());j++){
                                        String i = (String) (item2.get(j)).get(0);
                                       i =  i.replace('"', ' ');
                                        i = i.replace('[', ' ');
                                        i = i.replace(']', ' ');
                                        i = i.replaceAll("\\s+", "");

                                        String episode = (String)(item2.get(j)).get(1);
                                        List l= new ArrayList();
                                        l.add(i);
                                        l.add(episode);
                                        item2.set(j,l);
                                        System.out.println(i);

                                    }*/


                                    new savefile().run();

                                    System.out.println(items);

                                    System.out.println(items);
                                    List<Button> listabottoni = new ArrayList<>();
                                    for (int i = 0; i < items.size(); i++) {
                                        Button myButton = new Button(getContext());
                                        myButton.setTag(((String) item2.get(i).get(0)).replace('"', ' ').replace('[', ' ').replace(']', ' ').replaceAll("\\s+", ""));
                                        listforfab.add(((String) item2.get(i).get(0)).replace('"', ' ').replace('[', ' ').replace(']', ' ').replaceAll("\\s+", ""));
                                        myButton.setText(((String) item2.get(i).get(1)).replace('"', ' ').replace('[', ' ').replace(']', ' ').replaceAll("\\s+", ""));
                                        if (episodelist.contains(((String) item2.get(i).get(0)).replace('"', ' ').replace('[', ' ').replace(']', ' ').replaceAll("\\s+", "")))
                                            myButton.setTextColor(Color.BLUE);

                                        myButton.setOnClickListener(new buttonlisener2());
                                        listabottoni.add(myButton);
                                    }
                                    MainActivity m = (MainActivity) getActivity();
                                    m.fablistsetter(listforfab);
                                    System.out.println(ll);
                                    for (Button myButton : listabottoni
                                    ) {
                                        ll.addView(myButton);
                                    }
                                    listabottoni.clear();
                                    chunckrequested = chunk;


                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                System.out.println("asd");
                                System.out.println();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        chunk--;
                        Toast.makeText(getContext(), "Error on reaching server, maybe there are no new episode D:, if not  please retry later", Toast.LENGTH_SHORT).show();
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
//            new savefile().run();

            return null;
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


    private class downloadTask extends AsyncTask<String, Integer, String> {
        private final String currentanime;
        private final String numero;
        private final Context context;
        int prev = 0;
        List channelList = new ArrayList();
        private NotificationManager mNotifyManager;
        private NotificationCompat.Builder mBuilder;
        private String chanel_id;

        public downloadTask(Context context, String currentanime, String numero) {
            this.context = context;
            this.currentanime = (currentanime + " ").replaceAll("[^a-zA-Z0-9]", " ");
            this.numero = numero;


        }


        @Override
        protected String doInBackground(String... sUrl) {


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
                Intent intent = new Intent(getActivity(), BackgroundService.class);
                Intent intent1 = intent.setClassName("com.dv.adm", "com.dv.adm.AEditor");


                intent1.putExtra("com.dv.get.ACTION_LIST_ADD", sUrl[0]);

                intent1.putExtra("com.dv.get.ACTION_LIST_PATH", Environment.getExternalStorageDirectory() + File.separator + "anime" + File.separator + currentanime); // destination directory (default "Settings - Downloading - Folder for files")
                intent1.putExtra("com.dv.get.ACTION_LIST_OPEN", false);
                intent1.putExtra("com.android.extra.filename", currentanime + numero + ".mp4");
                try {
                    System.out.println("starting service");

                    getActivity().startActivity(intent1);

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


                    Toast.makeText(getContext(), "An Error Occurred! missing video player", Toast.LENGTH_SHORT).show();


                    System.out.println("error");


                }


            }


            return null;
        }


    }

}