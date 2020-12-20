package com.example.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class downloadedanime extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {


        File directory = new File(Environment.getExternalStorageDirectory() + File.separator + "anime");
        if (!directory.exists()) {

            try {
                directory.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        LinearLayout ll = getView().findViewById(R.id.elencoanime);
        ll.removeAllViewsInLayout();
        List<directorybutton> b = new ArrayList<>();
        for (File anime : directory.listFiles()) {
            directorybutton bu = new directorybutton(getContext());
            bu.setDirectory(anime);
            bu.setText(anime.getName());
            ll.addView(bu);


        }

        List l = Arrays.asList(directory.listFiles());


        return inflater.inflate(R.layout.downloaded_anime, container, false);


    }


    public class directorybutton extends MaterialButton {
        private File directory;

        public directorybutton(@NonNull Context context) {
            super(context);
        }


        public File getDirectory() {
            return directory;
        }

        public void setDirectory(File directory) {
            this.directory = directory;
        }
    }
}