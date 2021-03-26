package com.anime.AnimeIndexer;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class bitmaplist implements Serializable {
    private final List<bitmaplink> Listabitmap;


    public bitmaplist() {
        Listabitmap = new ArrayList<>();
    }

    public void add(String link, Bitmap bitm) {
        try{
        Listabitmap.add(new bitmaplink(bitm, link));}
        catch(Exception e){
            System.out.println("");
        }
    }

    public Bitmap getbitbylink(String link) {


        for (bitmaplink bit : Listabitmap) {
            if (bit.link.equals(link)) {
                return bit.bitmap;


            }
        }


        return null;
    }

// --Commented out by Inspection START (03/02/2021 18:03):
//    public List<bitmaplink> getListabitmap() {
//        return Listabitmap;
//    }
// --Commented out by Inspection STOP (03/02/2021 18:03)

// --Commented out by Inspection START (03/02/2021 18:03):
//    public void setListabitmap(List<bitmaplink> listabitmap) {
//        Listabitmap = listabitmap;
//    }
// --Commented out by Inspection STOP (03/02/2021 18:03)

    private static class bitmaplink implements Serializable {


        public final String link;
        final Bitmap bitmap;


        public bitmaplink(Bitmap b, String sourceUrl) {
            bitmap = b;

            link = sourceUrl;
        }


    }


}

