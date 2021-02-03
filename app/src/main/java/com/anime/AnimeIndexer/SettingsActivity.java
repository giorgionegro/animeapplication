package com.anime.AnimeIndexer;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {
    GlobalVariable gb;
    Context context=this;

    static List sresult = new ArrayList();
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


    public static class SettingsFragment extends PreferenceFragmentCompat {
        GlobalVariable gb;
        final String port = "16834/";
        final String server = "http://192.168.0.211:";

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            ListPreference sources = findPreference("list_preference_1");
            System.out.println();
            SettingsActivity sa=(SettingsActivity)requireActivity();
            Thread i= new Info(server,port,sources);
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

            String server;
            String port;
            ListPreference sf;
            public Info(String server, String port,ListPreference sf){
                this.server=server;
                this.port=port;
                this.sf=sf;
            }


            @Override
            public void run() {
                try {
                    requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, 1);
                    RequestQueue queue = Volley.newRequestQueue(requireContext());
                    String url = server + port + "/info";
                    StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
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