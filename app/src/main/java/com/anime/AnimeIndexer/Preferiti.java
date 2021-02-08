package com.anime.AnimeIndexer;

import java.io.Serializable;

public class Preferiti implements Serializable {
    private final String url;
    private final String source;
    private final String img;
    private final String title;
    private int numperepisode = 0;

    public Preferiti(String url, String source, String img, String title) {
        this.url = url;
        this.source = source;
        this.img = img;
        this.title = title;
    }

    public int getNumperepisode() {
        return numperepisode;
    }

    public void setNumperepisode(int numperepisode) {
        this.numperepisode = numperepisode;
    }

    public String getUrl() {
        return url;
    }

    public String getSource() {
        return source;
    }

    public String getImg() {
        return img;
    }

    public String getTitle() {
        return title;
    }

}
