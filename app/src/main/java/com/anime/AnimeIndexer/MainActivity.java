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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    final List sresult = new ArrayList();
    private String source;

    public GlobalVariable getGb() {
        return gb;
    }

    public Context context;
    FileInputStream fi;
    ObjectInputStream oi;
    GlobalVariable gb;
    String source2="/aw/";
    // --Commented out by Inspection (03/02/2021 18:04):bitmaplist listabitm;
    // --Commented out by Inspection (03/02/2021 18:03):DatabaseHelper dbh;
    private String currentnanimeforfragment;
    private String urlforfragment;
    private List listforfab;
    final String port = "16834";
    //final String server = "http://serverparan.ddns.net:";
    final String server = "http://192.168.0.211:";
    public void setterfor2fragment(String url,String source2) {
        this.urlforfragment = url;this.source2 = source2;
    }

    public void fablistsetter(List l) {
        listforfab = l;
    }

    public List getter() {


        List result = new ArrayList();
        result.add(urlforfragment);

        result.add(currentnanimeforfragment);
        result.add(source2);

        return result;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);

      source = sharedPreferences.getString("list_preference_1", "/aw/");
        setTheme(R.style.Theme_MaterialComponents_DayNight); //imposta tema scuro

        System.err.println("setting");


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FloatingActionButton fab = findViewById(R.id.fab); // cerco il floating button
        fab.setOnClickListener(new View.OnClickListener() { // aggiungo lissener
            @Override
            public void onClick(View view) {


                if (listforfab == null) return;
                System.out.println(listforfab.toString());
                List<String> l = listforfab;
                StringBuilder link = new StringBuilder();
                for (String s : l
                ) {
                    currentnanimeforfragment=s.split("/")[s.split("/").length - 2];
                    link.append(s).append("<line>");


                }
                currentnanimeforfragment=currentnanimeforfragment.replaceAll(" ","" );
                System.out.println(link);
                try {

                    ActivityManager manager = (ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE);
                    manager.killBackgroundProcesses("com.dv.adm.A Editor");
                    Intent intent = new Intent("android.intent.action.MAIN");
                    intent.setClassName("com.dv.adm", "com.dv.adm.AEditor");
                    intent.putExtra("Referer", gb.get_references_by_entry(source));
                    System.out.println(gb.get_references_by_entry(source));
                    intent.putExtra("com.dv.get.ACTION_LIST_PATH", Environment.getExternalStorageDirectory() + File.separator + "anime" + File.separator + currentnanimeforfragment); // destination directory (default "Settings - Downloading - Folder for files")
                    intent.putExtra("com.dv.get.ACTION_LIST_OPEN", false);
                    intent.putExtra("com.dv.get.ACTION_LIST_ADD", link.toString());

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
        new Info().start();
        File file = new File(this.getApplicationContext().getFilesDir() + File.pathSeparator + "serievisteoscaricate.txt");//file episodi gia' scaricati
        if (!file.exists()) {
            try {

                file.createNewFile();

                System.out.println("file created");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        List episodelist;
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
        //noinspection ConstantConditions
        if (episodelist == null) {
            episodelist = new ArrayList<>();


        }

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

        FragmentManager fm = getSupportFragmentManager();

        if (id == R.id.latest) {
            try {
                NavHostFragment.findNavController(fm.getFragments().get(0))
                        .navigate(R.id.action_global_FirstFragment);
                System.out.println("good");
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();                }



        } else if (id == R.id.action_settings) {
            System.out.println("asd");
            Intent intent = new Intent(this,
                    SettingsActivity.class);
            startActivity(intent);


        }else if (id == R.id.favorite) {

            try {
                NavHostFragment.findNavController(fm.getFragments().get(0))
                        .navigate(R.id.action_global_favoriteFragment);
                System.out.println("good");
                Thread.sleep(1000);
            } catch (Exception e) {


                System.out.println(e.getMessage());
            }





        }else if (id == R.id.downloaded) {
            try {
                NavHostFragment.findNavController(fm.getFragments().get(0))
                        .navigate(R.id.action_global_downloadedFragment);
                System.out.println("good");
                Thread.sleep(1000);
            } catch (Exception e) {


                System.out.println(e.getMessage());
            }



        }

        return super.onOptionsItemSelected(item);
    }

















    private class Info extends Thread {


        public Info() {


        }

        @Override
        public void run() {

            try {
                requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, 1);

                RequestQueue queue = Volley.newRequestQueue(context);
                String url = server + port + "/info";

// Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                System.out.println("sadsad");
                                System.out.println(response);
                                try {
                                    String[] items = response.split("\\s*], \\s*");
                                    for (String i : items
                                    ) {
                                        i = i.replace('[', ' ').replace(']', ' ').replace('"', ' ');

                                        List<String> items2 = Arrays.asList(i.split("\\s*, \\s*"));
                                        sresult.add(items2);


                                    }


                                } catch (Exception e) {

                                    System.out.println(e.toString());
                                }
                                System.out.println("asd");
                                gb=new GlobalVariable(sresult);


                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, "Error on reaching server please retry later", Toast.LENGTH_SHORT).show();

                        System.out.println(error.getMessage());
                    }
                });
                System.err.println("info");


// Add the request to the RequestQueue.
                queue.add(stringRequest);
                queue.start();
            } catch (Exception e) {
                e.printStackTrace();
            }


        }


    }




}