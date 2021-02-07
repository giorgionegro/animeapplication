package com.anime.AnimeIndexer;

import java.io.Serializable;

public class downloadedserieselement implements Serializable {
    private String name ;
    private String imgUrl;

    public String getName() {
        return name;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public downloadedserieselement(String name, String imgUrl) {
        this.name = name;
        this.imgUrl = imgUrl;
    }
}
