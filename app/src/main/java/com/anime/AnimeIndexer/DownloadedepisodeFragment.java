package com.anime.AnimeIndexer;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class DownloadedepisodeFragment extends Fragment {
    private Context mContext;
    private LinearLayout ll;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    public DownloadedepisodeFragment() {
        // Required empty public constructor
    }
    private File directory;
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (mContext == null)
            mContext = context.getApplicationContext();
        String filedir = mContext.getFilesDir().getAbsolutePath();
    }

    // TODO: Rename and change types and number of parameters


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       directory= new File(getArguments().getString("Filearg"));



        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_downloadedepisode, container, false);
    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

        ll = view.findViewById(R.id.episodedownloaded);
        ll.removeAllViews();
        if (directory.exists() && directory.isDirectory()) {
            List<File> list_of_series = Arrays.asList(Objects.requireNonNull(directory.listFiles()));
            Collections.sort(list_of_series);
            for(File f: list_of_series){
                button myButton = new button(requireContext(),f);
                myButton.setText(f.getName());
                ll.addView(myButton);



            }


        } else {

            Looper.prepare();

            Toast.makeText(getContext(), "nessuna cartella", Toast.LENGTH_SHORT).show();
        }







    }







    public class button extends MaterialButton {
        private final File video;
        public button(@NonNull Context context,File video) {
            super(context);
            this.video = video;
            super.setOnClickListener(new buttonlister(video.getAbsolutePath()));
        }
   }
    class buttonlister implements View.OnClickListener{
        private String file_path;

        public buttonlister(String file_path) {
            this.file_path = file_path;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(file_path), "video/*");
            startActivityForResult(intent, 10);
        }
    }








}