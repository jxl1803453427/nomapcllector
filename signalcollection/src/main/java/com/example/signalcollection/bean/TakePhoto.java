package com.example.signalcollection.bean;

import java.io.Serializable;

/**
 * Created by Konmin on 2017/3/8.
 */

public class TakePhoto implements Serializable {


    private String path;

    private int tag;
    private String url;

    public TakePhoto(String path, int tag) {
        this.path = path;
        this.url = setUrl();
        this.tag = tag;
    }


    private String setUrl() {
        return "http://jmtool3.jjfinder.com/cpphoto/" + System.currentTimeMillis() + tag + ".jpg";
    }


    public String getUrl() {
        return url;
    }

    public String getPath() {
        return path;
    }

    public int getTag() {
        return tag;
    }
}
