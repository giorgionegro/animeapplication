package com.anime.AnimeIndexer;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.androidnetworking.AndroidNetworking;
import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.google.android.material.button.MaterialButton;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class SearchandlatestFragment extends Fragment implements SearchView.OnQueryTextListener {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    //final String server = "http://serverparan.ddns.net:";
    final List<List<String>> sresult = new ArrayList<>();
    final int Width = 250;
    private final buttonlisener buttonl = new buttonlisener();
    public GlobalVariable gb;
    public List<String> episodelist;
    String currentanime;
    String json;
    LinearLayout ll;
    FileInputStream fi;
    ObjectInputStream oi;
    bitmaplist listabitm;
    DatabaseHelper dbh;
    boolean streaming;
    List<Preferiti> prefs;
    private String source;
    private List<downloadedserieselement> serieslist;
    private String server;


    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState

    ) {


        listabitm = new bitmaplist();
        dbh = new DatabaseHelper(getContext());
        new Thread() {
            @Override
            public void run() {


                try {
                    listabitm = dbh.getAllbitmaps();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }


        }.start();
        MainActivity m = (MainActivity) getActivity();
        Objects.requireNonNull(m).fablistsetter(null);
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(m);
        source = sharedPreferences.getString("sources", "/aw/");
        streaming = sharedPreferences.getBoolean("Streaming", true);


        verifyStoragePermissions(getActivity());
        AndroidNetworking.initialize(requireContext());
        new Latest().start();
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(requireActivity());
        prefs = FavoriteFragment.readfile(requireContext().getFilesDir().getAbsolutePath());
        server=sharedPreferences.getString("server","192.168.0.211:16834/");
        source = sharedPreferences.getString("sources", "/aw/");
        streaming = sharedPreferences.getBoolean("Streaming", true);
        super.onViewCreated(view, savedInstanceState);
        SearchView search = view.findViewWithTag("ricerca");
        search.setOnQueryTextListener(this);
        ll = view.findViewWithTag(getString(R.string.lista_firstfragment));
        search.setOnQueryTextListener(this);

        PRDownloader.initialize(getContext());
        PRDownloader.cleanUp(1);
        PRDownloaderConfig config = PRDownloaderConfig.newBuilder()
                .setDatabaseEnabled(true)
                .setReadTimeout(30_000)
                .setConnectTimeout(30_000)
                .build();
        PRDownloader.initialize(getContext(), config);
        File file = new File(requireContext().getFilesDir() + File.pathSeparator + "serievisteoscaricate.txt");
        System.out.println(requireContext().getFilesDir());
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
        file = new File(requireContext().getFilesDir() + File.pathSeparator + "seriedownloaded.txt");
        System.out.println(requireContext().getFilesDir());
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


            serieslist = (List<downloadedserieselement>) oi.readObject();

            System.out.println(serieslist.toString());
            oi.close();
            fi.close();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("File not found");
            serieslist = new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error initializing stream");
            serieslist = new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            serieslist = new ArrayList<>();
        }
        if (serieslist == null) {
            serieslist = new ArrayList<>();


        }


        new Latest().start();
        System.out.println(this.getId());
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        sresult.clear();
        System.out.println(ll + "as");
        s = s.replaceAll(" ", "+");
        ll.removeAllViews();

        try {
            MainActivity m = (MainActivity) getActivity();
            Objects.requireNonNull(m).fablistsetter(null);
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(m);
            source = sharedPreferences.getString("sources", "/aw/");
            System.out.println("asd");
            new Search(s).start();
            prefs = FavoriteFragment.readfile(requireContext().getFilesDir().getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        return false;
    }

    public void startdownload(View view)//some preparation before starting the download
    {

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(requireActivity());

        source = sharedPreferences.getString("sources", "/aw/");
        streaming = sharedPreferences.getBoolean("Streaming", true);
        button bu = (button) view;

        currentanime = (((String) view.getTag()).split("/"))[((String) view.getTag()).split("/").length - 2];
        bu.setTextColor(Color.BLUE);
        currentanime = currentanime.replaceAll("", "");
        if (!isinlistbyname(serieslist, currentanime)) {
            serieslist.add(new downloadedserieselement(currentanime, bu.imgurl));
            new savefileseries().start();
        }
        if (!episodelist.contains(view.getTag())) {
            episodelist.add((String) view.getTag());
        }
        new savefile().start();
        new downloadTask(getActivity(), currentanime, bu.getText().toString()).execute((String) view.getTag());


        System.out.println((String) view.getTag());


    }

    private boolean is_in_favorite(String imgurl) {
        for (Preferiti p : prefs) {
            if (imgurl != null)
                try {
                    if (imgurl.equals(p.getImg())) {
                        return true;

                    }
                } catch (Exception e) {
                    return false;
                }


        }

        return false;

    }

    private boolean removeduplicate(String url) {
        boolean b = false;
        Preferiti pr = null;
        for (Preferiti p : prefs)
            if (url.equals(p.getUrl())) {
                b = true;
                pr = p;
            }
        prefs.remove(pr);
        return b;


    }

    private Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Canvas canvas = new Canvas(bmp1);
        canvas.drawBitmap(bmp2, 0, 0, null);
        return bmp1;
    }

    private void drawoverlay(View v) {
        button b = (button) v;
        Bitmap icon = BitmapFactory.decodeResource(requireContext().getResources(),
                R.drawable.star);
        b.setIcon(new BitmapDrawable(getResources(), overlay(drawableToBitmap(b.getIcon()), icon)));

    }

    private boolean isinlistbyname(List<downloadedserieselement> list, String name) {
        for (downloadedserieselement element : list) {
            if (name.equals(element.getName())) return true;
        }
        return false;


    }

    public class savefile extends Thread {

        public void run() {
            File file = new File(requireContext().getFilesDir() + File.pathSeparator + "serievisteoscaricate.txt");
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


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class Search extends Thread {

        final String Searchterm;

        public Search(String Searchterm) {

            this.Searchterm = Searchterm;

        }

        @Override
        public void run() {
            try {
                requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, 1);

                RequestQueue queue = Volley.newRequestQueue(requireContext());
                String url = server  + source + "/q?q=" + Searchterm;

// Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                response = decrypt(response);
                                response=unescape(response);
                                json = response;
                                System.out.println(json);
                                try {
                                    String[] items = json.split("\\s*], \\s*");
                                    for (String i : items
                                    ) {
                                        i = i.replace('[', ' ').replace(']', ' ').replace('"', ' ');

                                        List<String> items2 = Arrays.asList(i.split("\\s*, \\s*"));
                                        sresult.add(items2);


                                    }
                                    for (int i = 0; i < sresult.size(); i++) {
                                        button myButton = new button(requireContext(), sresult.get(i).get(2));
                                        myButton.setTag(sresult.get(i).get(0));
                                        byte[] string = sresult.get(i).get(1).getBytes();
                                        myButton.setText(new String(string, StandardCharsets.UTF_8));
                                        myButton.setOnClickListener(buttonl);
                                        myButton.setOnLongClickListener(new longclocklistener());
                                        ll.addView(myButton);
                                    }


                                } catch (Exception e) {

                                    System.out.println(e.toString());
                                }
                                System.out.println("asd");
                                System.out.println(sresult);

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Toast.makeText(getContext(), "Error on reaching server please retry later", Toast.LENGTH_SHORT).show();

                        System.out.println(error.getMessage());
                    }
                });
                System.out.println("sfdad");

                System.out.println(url);
// Add the request to the RequestQueue.
                queue.add(stringRequest);

            } catch (Exception e) {
                e.printStackTrace();
            }


        }


    }

    private class downloadTask extends AsyncTask<String, Integer, String> {
        private final String currentanime;
        private final String numero;
        // --Commented out by Inspection (03/02/2021 18:04):int prev = 0;
        // --Commented out by Inspection (03/02/2021// --Commented out by Inspection (03/02/2021 18:04): 18:04):List channelList = new ArrayList();
        // --Commented out by Inspection (03/02/2021 18:04):private NotificationManager mNotifyManager;
        // --Commented out by Inspection (03/02/2021 18:04):private NotificationCompat.Builder mBuilder;
        // --Commented out by Inspection (03/02/2021 18:04):private String chanel_id;

        public downloadTask(Context context, String currentanime, String numero) {
            this.currentanime = (currentanime + " ").replaceAll("[^a-zA-Z0-9]", " ");
            this.numero = numero;


        }


        @Override
        protected String doInBackground(String... sUrl) {
            MainActivity mn = (MainActivity) requireActivity();
            gb = mn.getGb();


            URL url = null;
            try {
                url = new URL(sUrl[0]);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            File file = new File(Environment.getExternalStorageDirectory() + File.separator + "anime" + File.separator + currentanime + File.separator + FilenameUtils.getName(Objects.requireNonNull(url).getPath()));
            if (streaming) {
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(sUrl[0]), "video/*");
                startActivityForResult(intent, 10);


            } else if (!file.exists()) {

                Intent intent1 = new Intent("android.intent.action.MAIN");
                intent1.setClassName("com.dv.adm", "com.dv.adm.AEditor");
                intent1.putExtra("com.dv.get.ACTION_LIST_ADD", sUrl[0]);
                intent1.putExtra("User-Agent", "Mozilla compatible/1.0");
                intent1.putExtra("Referer", gb.get_references_by_entry(source));
                System.out.println(gb.get_references_by_entry(source));
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
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(file.getPath()), "video/*");
                startActivityForResult(intent, 10);


            }


            return null;
        }


    }

    private class savefileseries extends Thread {

        public void run() {
            File file = new File(requireContext().getFilesDir() + File.pathSeparator + "seriedownloaded.txt");
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
                List su = new ArrayList(serieslist);

                o.writeObject(su);

                o.close();
                f.close();


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public class buttonlisener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            button bu = (button) view;
            currentanime = (((String) view.getTag()).split("/"))[((String) view.getTag()).split("/").length - 1];
            currentanime = currentanime.replaceAll("", "");
            if (!isinlistbyname(serieslist, currentanime)) {

                serieslist.add(new downloadedserieselement(currentanime, bu.imgurl));
                new savefileseries().start();
            }
            // Cr


            MainActivity activity = (MainActivity) getActivity();
            Objects.requireNonNull(activity).setterfor2fragment((String) bu.getTag(), source);

            // Replace whatever is in the fragment_container view with this fragment,
// and add the transaction to the back stack
            //   Intent i = new Intent(R.id.action_FirstFragment_to_SecondFragment);
            NavHostFragment.findNavController(SearchandlatestFragment.this)
                    .navigate(R.id.action_FirstFragment_to_SecondFragment);


// Commit the transaction


            //  onClickBtn(view);
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

    public class button extends MaterialButton {
        public final String imgurl;
        public Drawable original = null;

        public button(@NonNull Context context, String imgurl) {
            super(context);
            this.imgurl = imgurl;
            if (listabitm.getbitbylink(this.imgurl) == null) {
                downsetbitmap dsb = new downsetbitmap(this);
                dsb.start();

            } else {

                Bitmap b = listabitm.getbitbylink(this.imgurl);
                float aspectRatio = b.getWidth() /
                        (float) b.getHeight();

                int height = Math.round(Width / aspectRatio);

                b = Bitmap.createScaledBitmap(
                        b, Width, height, false);


                this.setIconTint(null);
                this.setIcon(new BitmapDrawable(Resources.getSystem(), b));
                original = new BitmapDrawable(getResources(), drawableToBitmap(this.getIcon()).copy(b.getConfig(), true));
                if (is_in_favorite(imgurl)) {
                    drawoverlay(this);
                }
            }
        }

        public class downsetbitmap extends Thread {
            final button b;

            public downsetbitmap(button b) {
                this.b = b;

            }

            public void run() {
                Looper.prepare();

                final Bitmap x;

                HttpURLConnection connection = null;
                try {
                    connection = (HttpURLConnection) new URL(imgurl).openConnection();

                } catch (IOException e) {

                    Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    System.err.println(e.getMessage());
                }
                try {
                    Objects.requireNonNull(connection).connect();
                } catch (Exception e) {
                    Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                InputStream input = null;
                try {
                    input = Objects.requireNonNull(connection).getInputStream();
                } catch (Exception e) {
                    Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

                x = BitmapFactory.decodeStream(input);
                if (x != null) {
                    listabitm.add(imgurl, x);
                    new savebitlist(imgurl, x).run();
                }
                if (x != null) {
                    float aspectRatio = x.getWidth() /
                            (float) x.getHeight();

                    int height = Math.round(Width / aspectRatio);

                    final Bitmap fbit = Bitmap.createScaledBitmap(
                            x, Width, height, false);
                    try{
                    requireActivity().runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            b.setIconTint(null);
                            b.setIcon(new BitmapDrawable(Resources.getSystem(), fbit));
                            original = new BitmapDrawable(getResources(), drawableToBitmap(b.getIcon()).copy(fbit.getConfig(), true));
                            if (is_in_favorite(imgurl)) {
                                drawoverlay(b);
                            }
                        }
                    });}catch (Exception e)
                    {
                        System.out.println((e.getMessage()));
                    }


                }

            }


        }
    }

    public class savebitlist extends Thread {
        final String imgurl;
        final Bitmap x;

        public savebitlist(String imgurl, Bitmap x) {
            this.imgurl = imgurl;
            this.x = x;
        }

        public void run() {
            dbh.addEntry(imgurl, x);
        }
    }

    public class Latest extends Thread {

        public Latest() {

        }


        @Override

        public void run() {
            Looper.prepare();

            try {
                requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, 1);
                try {
                    Thread.sleep(0);
                } catch (Exception e) {
                    Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
                }
                RequestQueue queue = Volley.newRequestQueue(requireContext());
                String url = server  + source + "/latest";
                System.out.println(url);
// Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        ll.removeAllViews();
                        response = decrypt(response);
                        //System.out.println("sadsad");
                        sresult.clear();
                        response=unescape(response);
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
                                button myButton = new button(requireContext(), sresult.get(i).get(2));
                                myButton.setTag(sresult.get(i).get(0));

                                myButton.setText( sresult.get(i).get(1));
                                myButton.setOnClickListener(new buttonlisener2());
                                ll.addView(myButton);
                            }


                            System.out.println(ll);
                            /*for (Button myButton : listabottoni
                            ) {

                            }*/
                            listabottoni.clear();

                        } catch (Exception e) {

                            Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
                            System.out.println(e.toString());
                        }
                        System.out.println("asd");
                        System.out.println(sresult);

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println(Arrays.toString(error.getStackTrace()));

                        Toast.makeText(requireContext(), "Error on reaching server, maybe there are no new episode D:, if not  please retry later", Toast.LENGTH_SHORT).show();

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
                e.printStackTrace();
                System.err.println(e.getMessage());
                Looper.prepare();

                Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
            }


        }


    }

    class longclocklistener implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(View v) {
            button b = (button) v;
            if (!removeduplicate((String) b.getTag())) {
                Preferiti p = new Preferiti((String) b.getTag(), source, b.imgurl, b.getText().toString());


                prefs.add(p);
                new add_number_of_episode(p).start();
                Bitmap icon = BitmapFactory.decodeResource(requireContext().getResources(),
                        R.drawable.star);

                b.setIcon(new BitmapDrawable(getResources(), overlay(drawableToBitmap(b.getIcon()), icon)));
            } else {
                b.setIcon(new BitmapDrawable(getResources(), drawableToBitmap(b.original).copy(drawableToBitmap(b.original).getConfig(), true)));
            }
            FavoriteFragment.writefile(prefs, requireContext().getFilesDir().getAbsolutePath());
            System.out.println(prefs.toString());
            return true;
        }
    }


    class add_number_of_episode extends Thread {
        private final Preferiti p;

        public add_number_of_episode(Preferiti p) {
            this.p = p;
        }

        public void run() {
            try {
                requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String url = p.getUrl();
            final RequestQueue queue = Volley.newRequestQueue(requireContext());
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
                                System.out.println(json);
                                try {


                                    p.setNumperepisode(Integer.parseInt(response));


                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                System.out.println("asd");
                                System.out.println();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Toast.makeText(requireContext(), "Error on reaching server please retry later", Toast.LENGTH_SHORT).show();
                        System.out.println(error.getMessage()); }});

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
    public static String decrypt(String en){
        String key="xJGHsUXg|NIRd#WL&<Yy>h@kQ`19n0a)! .4-3pjCbB[7:O=;'*o^c8e{vqZ+\\li$?w}T~P\"MKV(_f5r/mtF]ASE,D2%uz6";
        String dec="";
        for(int i=0;i<en.length();i++){
            dec+=key.charAt(((int)en.charAt(i))-32);
        }
        return dec;
    }
    private String unescape(String s) {
        int i=0, len=s.length();
        char c;
        StringBuffer sb = new StringBuffer(len);
        while (i < len) {
            c = s.charAt(i++);
            if (c == '\\') {
                if (i < len) {
                    c = s.charAt(i++);
                    if (c == 'u') {
                        c = (char) Integer.parseInt(s.substring(i, i+4), 16);
                        i += 4;
                    }
                    if(c== '"'){
                        c= '\'';
                    }
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }

}






