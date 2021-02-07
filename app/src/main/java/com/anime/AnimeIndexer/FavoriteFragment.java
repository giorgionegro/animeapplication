package com.anime.AnimeIndexer;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;

import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class FavoriteFragment extends Fragment {

    LinearLayout ll;
    List<Preferiti> prefs;
    String filedir ;
    final int Width = 250;
    private bitmaplist listabitm;
    DatabaseHelper dbh;
    private Context mContext;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (mContext == null)
            mContext = context.getApplicationContext();
        filedir=mContext.getFilesDir().getAbsolutePath()     ;
    }
    public FavoriteFragment() {
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_favorite, container, false);
    }


    @SuppressWarnings("ConstantConditions")
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
        //noinspection ConstantConditions
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


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
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
        ll = view.findViewById(R.id.favorite);
        prefs=readfile(filedir);
        ll.removeAllViews();
        System.out.println(prefs.toString());
        for(Preferiti p:prefs){
            button myButton = new button(requireContext(),p.getImg(),p.getUrl(),p.getSource(),p.getTitle());
            ll.addView(myButton);
        }






    }












    public class button extends MaterialButton {
        public final String imgurl;


        public button(@NonNull Context context, String imgurl, String tag,String source,String title) {
            super(context);
            super.setTag(tag);
            super.setText(title);
            this.imgurl = imgurl;
            super.setOnClickListener(new buttonlisener(source));
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
                if(is_in_favorite((String)this.getTag())){
                    draw_overlay(this);
                }
            }
        }

        public class downsetbitmap extends Thread {
            final button b;

            public downsetbitmap(button b) {
                this.b = b;

            }

            public void run() {
                final Bitmap x;

                HttpURLConnection connection = null;
                try {
                    connection = (HttpURLConnection) new URL(imgurl).openConnection();

                } catch (IOException e) {
                    Looper.prepare();
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
                }
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
                            if(is_in_favorite((String)b.getTag())){
                                draw_overlay(b);
                            }
                        }
                    });


                }

            }


        }
    }



    private boolean is_in_favorite(String url){
        for(Preferiti p: prefs){
            if(url!=null)
                try{
                    if(  url.equals(p.getUrl())){
                        return true;

                    }}catch (Exception e){return false;}


        }

        return false;

    }

    private Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Canvas canvas = new Canvas(bmp1);
        canvas.drawBitmap(bmp2, 0,0, null);
        return bmp1;
    }
    private void draw_overlay(View v){
        button b = (button) v;
        Bitmap icon = BitmapFactory.decodeResource(requireContext().getResources(),
                R.drawable.star);
        b.setIcon(new BitmapDrawable(getResources(),overlay(drawableToBitmap(b.getIcon()),icon)));

    }




    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;}



    public class buttonlisener implements View.OnClickListener {
        final String source;
        public buttonlisener(String source){

            this.source=source;
        }
        @Override
        public void onClick(View view) {
            Button bu = (Button) view;

            System.out.println(source);
            MainActivity activity = (MainActivity) getActivity();
            Objects.requireNonNull(activity).setterfor2fragment((String) bu.getTag(),source);


            NavHostFragment.findNavController(FavoriteFragment.this)
                    .navigate(R.id.action_global_SecondFragment);


// Commit the transaction


            //  onClickBtn(view);
        }
    }

}