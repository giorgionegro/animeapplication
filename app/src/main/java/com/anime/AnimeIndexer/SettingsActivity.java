package com.anime.AnimeIndexer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    static final List sresult = new ArrayList();
    //final String server = "http://serverparan.ddns.net:";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.Theme_MaterialComponents_DayNight);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
      /*  ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);

        }*/
    }
    public static String decrypt(String en){
        String key="xJGHsUXg|NIRd#WL&<Yy>h@kQ`19n0a)! .4-3pjCbB[7:O=;'*o^c8e{vqZ+\\li$?w}T~P\"MKV(_f5r/mtF]ASE,D2%uz6";
        String dec="";
        for(int i=0;i<en.length();i++){
            dec+=key.charAt(((int)en.charAt(i))-32);
        }
        return dec;
    }


    public static class SettingsFragment extends PreferenceFragmentCompat {
        GlobalVariable gb;
         String server = "http://192.168.0.211:16834";

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            SettingsActivity m = (SettingsActivity) getActivity();
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(m);
            server=sharedPreferences.getString("server","192.168.0.211:16834/");

            ListPreference sources = findPreference("sources");
            System.out.println();
            Thread i= new Info(server,sources);
            i.start();
            try {
                i.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
           // sources.setEntries(gb.Entry);
            //sources.setEntryValues(gb.Entryvalues);
        }


        private class Info extends Thread {

            final String server;
            final ListPreference sf;
            public Info(String server,ListPreference sf){
                this.server=server;
                this.sf=sf;
            }


            @Override
            public void run() {
                try {
                    sresult.clear();
                    requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, 1);
                    RequestQueue queue = Volley.newRequestQueue(requireContext());
                    String url = server  + "/info";
                    StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            System.out.println("sadsad");
                            System.out.println(response);
                            response = decrypt(response);
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
                            System.err.println("info");

                            sf.setEntries(gb.Entry);
                            sf.setEntryValues(gb.Entryvalues);


                        }



                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(requireContext(), "Error on reaching server please retry later", Toast.LENGTH_SHORT).show();
                            System.out.println(error.getMessage());
                        }
                    });
                    System.err.println("info");
                    queue.add(stringRequest);
                    queue.start();

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }


        }







    }







}