package com.anime.AnimeIndexer;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SearchView;

import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;


public class FavoriteFragment extends Fragment {

    LinearLayout ll;
    List<Preferiti> prefs;
    String filedir =requireContext().getFilesDir().getAbsolutePath();


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


                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        new savefilep().start();
    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireActivity());
        ll = view.findViewById(R.id.favorite);
        prefs=readfile(filedir);
    }












}