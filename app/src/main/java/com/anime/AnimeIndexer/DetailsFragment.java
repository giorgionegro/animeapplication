package com.anime.AnimeIndexer;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DetailsFragment extends Fragment implements ViewTreeObserver.OnScrollChangedListener {


    //  final String server = "http://serverparan.ddns.net:";
     String server = "http://192.168.0.211:16834/";
    final List<List<String>> sresult = new ArrayList<>();
    public List<String> episodelist;
    String currentanime;
    String json;
    boolean streaming;
    // --Commented out by Inspection (03/02/2021 18:03):View view;
    LinearLayout ll;
    FileInputStream fi;
    ObjectInputStream oi;
    GlobalVariable gb;


    String url;
    // --Commented out by Inspection (03/02/2021 18:03):DatabaseHelper dbh;
    ScrollView scrollView;
    int chunk;
    int chunckrequested;
    private String source;
    private Context context;
    public String fileDir;


    public DetailsFragment() {
    }
    @SuppressWarnings("ConstantConditions")
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        //noinspection ConstantConditions
        if (context == null)
            context = context.getApplicationContext();
        fileDir = context.getFilesDir().getAbsolutePath();
    }
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_second, container, false);
    }




    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

        MainActivity mn = (MainActivity) requireActivity();
        gb = mn.getGb();
        chunk = 0;
        chunckrequested = 0;
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(requireActivity());


                streaming = sharedPreferences.getBoolean("Streaming",true);
        MainActivity ma = (MainActivity) requireActivity();

        List<String> l = ma.getter();
        if(l == null){            Navigation.findNavController(view).navigate(R.id.action_global_FirstFragment); }
        else{
        url = l.get(0);
        currentanime = l.get(1);
        source = l.get(2);
        if(url == null|source==null){            Navigation.findNavController(view).navigate(R.id.action_global_FirstFragment); }

        server=sharedPreferences.getString("server","192.168.0.211:16834/");

        ll = view.findViewById(R.id.elencoepisodi);
        ll.removeAllViews();
        scrollView = (ScrollView) ll.getParent();
        scrollView.getViewTreeObserver().addOnScrollChangedListener(this);
        File file = new File(requireContext().getFilesDir() + File.pathSeparator + "serievisteoscaricate.txt");
        System.out.println(requireContext().getFilesDir());






            try {

                if (file.createNewFile()){
                    System.out.println("file created");
                }
            } catch (Exception e) {
                e.printStackTrace();
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


        super.onViewCreated(view, savedInstanceState);}

    }

    public void startdownload(View view) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(requireActivity());

        source = sharedPreferences.getString("sources", "/aw/");
        streaming = sharedPreferences.getBoolean("Streaming",true);
        Button bu = (Button) view;
        bu.setTextColor(Color.BLUE);
        currentanime = (((String) view.getTag()).split("/"))[((String) view.getTag()).split("/").length - 2];
        currentanime = currentanime.replaceAll(" ", "");

        if (!episodelist.contains(view.getTag())) {
            episodelist.add((String) view.getTag());
        }
        new savefile().start();
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
            MainActivity ma = (MainActivity) getActivity();
            List<String> l = Objects.requireNonNull(ma).getter();
            url = l.get(0);
            currentanime = l.get(1);
            source = l.get(2);
            try {
                requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }

            final RequestQueue queue = Volley.newRequestQueue(requireContext());
            url = url.replace('"', ' ');
            try {


                String url2 = server  + source + "//dettagli?url=" + url + "&chunk=" + chunk;
                url2 = url2.replaceAll("\\s+", "");
                final List<String> listforfab = new ArrayList<>();
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
                                    ArrayList<List<String>> item2 = new ArrayList<List<String>>();

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


                                    new savefile().start();

                                    System.out.println(items);

                                    System.out.println(items);
                                    List<Button> listabottoni = new ArrayList<>();
                                    for (int i = 0; i < items.size(); i++) {
                                        Button myButton = new Button(getContext());
                                        myButton.setTag((item2.get(i).get(0)).replace('"', ' ').replace('[', ' ').replace(']', ' ').replaceAll("\\s+", ""));
                                        listforfab.add((item2.get(i).get(0)).replace('"', ' ').replace('[', ' ').replace(']', ' ').replaceAll("\\s+", ""));
                                        myButton.setText((item2.get(i).get(1)).replace('"', ' ').replace('[', ' ').replace(']', ' ').replaceAll("\\s+", ""));
                                        if (episodelist.contains((item2.get(i).get(0)).replace('"', ' ').replace('[', ' ').replace(']', ' ').replaceAll("\\s+", "")))
                                            myButton.setTextColor(Color.BLUE);

                                        myButton.setOnClickListener(new buttonlisener2());
                                        listabottoni.add(myButton);
                                    }
                                    MainActivity m = (MainActivity) getActivity();
                                    Objects.requireNonNull(m).fablistsetter(listforfab);
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
                        Toast.makeText(requireContext(), "Error on reaching server please retry later", Toast.LENGTH_SHORT).show();
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
            File file = new File(fileDir + File.pathSeparator + "serievisteoscaricate.txt");
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
                List<String> su = new ArrayList<>(episodelist);

                o.writeObject(su);

                o.close();
                f.close();


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private class downloadTask extends AsyncTask<String, Integer, String> {
        private final String currentanime;
        // --Commented out by Inspection START (03/02/2021 18:03):
// --Commented out by Inspection START (03/02/2021 18:04):
        private final String numero;
        private final Context context;
//// --Commented out by Inspection STOP (03/02/2021 18:03)
//        int prev = 0;
// --Commented out by Inspection STOP (03/02/2021 18:04)
// --Commented out by Inspection (03/02/2021 18:03):        // --Commented out by Inspection (03/02/2021// --Commented out by Inspection (03/02/2021 18:03): 18:03):List channelList = new ArrayList();
        // --Commented out by Inspection (03/02/2021 18:04):private NotificationManager mNotifyManager;
        // --Commented out by Inspection (03/02/2021 18:04):private NotificationCompat.Builder mBuilder;
        // --Commented out by Inspection (03/02/2021 18:03):private String chanel_id;

        public downloadTask(Context context, String currentanime, String numero) {
            this.currentanime = (currentanime).replaceAll("[^a-zA-Z0-9]", " ").replaceAll(" ", "");
            this.numero = numero;
            this.context = context;


        }


        @Override
        protected String doInBackground(String... sUrl) {



            URL url = null;
            try {
                url = new URL(sUrl[0]);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            File file = new File(Environment.getExternalStorageDirectory() + File.separator + "anime" + File.separator + currentanime + File.separator + FilenameUtils.getName(Objects.requireNonNull(url).getPath()));

            if (sUrl[0].contains("m3u8")|streaming) {


                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(sUrl[0]), "video/*");
                startActivityForResult(intent, 10);


            }
            else if (!file.exists()) {
                Intent intent1 = new Intent("android.intent.action.MAIN");
                intent1.setClassName("com.dv.adm", "com.dv.adm.AEditor");
                intent1.putExtra("Referer", gb.get_references_by_entry(source));

                intent1.putExtra("com.dv.get.ACTION_LIST_ADD", sUrl[0]);
                intent1.putExtra("com.dv.get.ACTION_LIST_PATH", Environment.getExternalStorageDirectory() + File.separator + "anime" + File.separator + currentanime); // destination directory (default "Settings - Downloading - Folder for files")
                intent1.putExtra("com.dv.get.ACTION_LIST_OPEN", false);
                intent1.putExtra("com.android.extra.filename", currentanime + numero + ".mp4");
                try {
                    System.out.println("starting service");

                    requireActivity().startActivity(intent1);

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

                Uri uri = Uri.parse(file.getPath());


                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(file.getPath()), "video/*");
                startActivityForResult(intent, 10);




            }


            return null;
        }


    }

    private class fileoject{
        private final File directory;


        public fileoject(File directory) {
            this.directory = directory;
        }
    }








}