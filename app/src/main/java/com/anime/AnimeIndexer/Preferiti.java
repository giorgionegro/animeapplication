package com.anime.AnimeIndexer;

import java.io.Serializable;

public class Preferiti implements Serializable {
    public Preferiti(String url, String source, String img) {
        this.url = url;
        this.source = source;
        this.img = img;
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

    private String url;
    private String source;
    private String img;
}
