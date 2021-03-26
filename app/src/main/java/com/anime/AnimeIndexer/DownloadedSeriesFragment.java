package com.anime.AnimeIndexer;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.anime.AnimeIndexer.R.style.Theme_AppCompat;


public class DownloadedSeriesFragment extends Fragment {

    final int Width = 250;

    private Context mContext;
    private List<downloadedserieselement> serieslist;
    private List<String> episodelist;
    private DatabaseHelper dbh;
    private bitmaplist listabitm;
    private LinearLayout ll;

    public DownloadedSeriesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (mContext == null)
            mContext = context.getApplicationContext();
        String filedir = mContext.getFilesDir().getAbsolutePath();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        return inflater.inflate(R.layout.fragment_downloaded, container, false);
    }
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        ll = view.findViewById(R.id.downladedseries);
        File file = new File(requireContext().getFilesDir() + File.pathSeparator + "seriedownloaded.txt");
        dbh = new DatabaseHelper(mContext);

       Thread t = new Thread() {
            @Override
            public void run() {


                try {
                    listabitm = dbh.getAllbitmaps();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }


        };
       t.start();
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

            FileInputStream fi = new FileInputStream(file);
            ObjectInputStream oi = new ObjectInputStream(fi);


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
        file= new File(requireContext().getFilesDir() + File.pathSeparator + "serievisteoscaricate.txt");
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



        File directory = new File(Environment.getExternalStorageDirectory() + File.separator + "anime");
        ll.removeAllViews();
        if (directory.exists() && directory.isDirectory()) {
            List<File> list_of_series = Arrays.asList(Objects.requireNonNull(directory.listFiles()));
            for(File f: list_of_series){
                button myButton = new button(requireContext(),urlbyname(serieslist,f.getName()));
                myButton.setOnClickListener(new buttonlistener(f));
                myButton.setText(unescape(f.getName()));
                ll.addView(myButton);



            }


        } else {

            Looper.prepare();

            Toast.makeText(getContext(), "nessuna cartella", Toast.LENGTH_SHORT).show();
        }
        System.out.println();





    }




    private boolean isinlistbyname(List<downloadedserieselement> list, String name) {
        for (downloadedserieselement element : list) {
            if (name.equals(element.getName())) return true;
        }
        return false;


    }

    private String urlbyname(List<downloadedserieselement> list, String name) {
        for (downloadedserieselement element : list) {
            if (name.replaceAll("[^a-zA-Z0-9]", "").equals(element.getName().replaceAll("[^a-zA-Z0-9]", ""))) return element.getImgUrl();
        }
        return null;


    }

    public class button extends MaterialButton {
        public final String imgurl;
        public Drawable original = null;

        public button(@NonNull Context context, String imgurl) {
            super(context);
            this.imgurl = imgurl;
            try{
            if (listabitm.getbitbylink(this.imgurl) == null) {
                button.downsetbitmap dsb = new button.downsetbitmap(this);
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

            }}catch(Exception e){ button.downsetbitmap dsb = new button.downsetbitmap(this);
                dsb.start();}
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
                    float aspectRatio = x.getWidth() /
                            (float) x.getHeight();

                    int height = Math.round(Width / aspectRatio);

                    final Bitmap fbit = Bitmap.createScaledBitmap(
                            x, Width, height, false);
                    requireActivity().runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            b.setIconTint(null);
                            b.setIcon(new BitmapDrawable(Resources.getSystem(), fbit));

                        }
                    });


                }

            }


        }
    }




    private class buttonlistener implements View.OnClickListener {
        public buttonlistener(File directory) {
            this.directory = directory;
        }

        private File directory;


        @Override
        public void onClick(View v) {
            Bundle var = new Bundle();
            var.putString("Filearg",directory.getAbsolutePath());

            Navigation.findNavController(v).navigate(R.id.action_downloadedFragment_to_downloadedepisodeFragment,var);


        }
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