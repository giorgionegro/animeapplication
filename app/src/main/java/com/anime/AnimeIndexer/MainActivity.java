package com.anime.AnimeIndexer;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public Context context;
    String source;
    View view2;
    FileInputStream fi;
    ObjectInputStream oi;
    bitmaplist listabitm;
    DatabaseHelper dbh;
    private LinearLayout ll;
    private List episodelist;
    private String currentnanimeforfragment;
    private String urlforfragment;
    private List listforfab;

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
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }

        });

        FragmentManager fm = getSupportFragmentManager();
        context = fm.getFragments().get(0).getContext();
        dbh = new DatabaseHelper(this);
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

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        source = sharedPreferences.getString("list_preference_1", "/aw/");
        FragmentManager fm = getSupportFragmentManager();

        if (id == R.id.latest) {
            try {
                NavHostFragment.findNavController(fm.getFragments().get(0))
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
                System.out.println("good");
                Thread.sleep(1000);
            } catch (Exception e) {
                NavHostFragment.findNavController(fm.getFragments().get(0))
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
                NavHostFragment.findNavController(fm.getFragments().get(0))
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);


                System.out.println(e.getMessage());
            }
            view2 = fm.getFragments().get(0).getView();
            context = fm.getFragments().get(0).getContext();
            ll = (LinearLayout) ((ScrollView) view2.findViewById(R.id.elenco_anime)).getChildAt(0);

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
}