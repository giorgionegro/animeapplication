package com.anime.AnimeIndexer;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class bitmaplist implements Serializable {
    private List<bitmaplink> Listabitmap;


    public bitmaplist() {
        Listabitmap = new ArrayList<>();
    }

    public void add(String link, Bitmap bitm) {
        Listabitmap.add(new bitmaplink(bitm, link));
    }

    public Bitmap getbitbylink(String link) {


        for (bitmaplink bit : Listabitmap) {
            if (bit.link.equals(link)) {
                return bit.bitmap;


            }
        }


        return null;
    }

    public List<bitmaplink> getListabitmap() {
        return Listabitmap;
    }

    public void setListabitmap(List<bitmaplink> listabitmap) {
        Listabitmap = listabitmap;
    }

    private static class bitmaplink implements Serializable {


        public String link;
        Bitmap bitmap;


        public bitmaplink(Bitmap b, String sourceUrl) {
            bitmap = b;

            link = sourceUrl;
        }


    }


}

