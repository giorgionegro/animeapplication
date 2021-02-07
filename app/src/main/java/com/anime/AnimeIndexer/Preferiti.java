package com.anime.AnimeIndexer;

import java.io.Serializable;

public class Preferiti implements Serializable {
    private final String url;
    private final String source;
    private final String img;
    private final String title;
    public Preferiti(String url, String source, String img, String title) {
        this.url = url;
        this.source = source;
        this.img = img;
        this.title = title;
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
