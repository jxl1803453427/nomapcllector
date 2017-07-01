package com.example.signalcollection.bean;

import java.io.Serializable;
import java.util.List;

/**
 * 用于照片查找的bean
 * Created by Konmin on 2017/5/3.
 */

public class PhotoBean implements Serializable {


    private int pointPhotoCount;
    private List<PhotoUrl> pointPhotos;

    private int areaPhotoCount;

    private List<PhotoUrl> areaPhotos;


    public PhotoBean(int pointPhotoCount, List<PhotoUrl> pointPhotos, int areaPhotoCount, List<PhotoUrl> areaPhotos) {
        this.pointPhotoCount = pointPhotoCount;
        this.pointPhotos = pointPhotos;
        this.areaPhotoCount = areaPhotoCount;
        this.areaPhotos = areaPhotos;
    }

    public int getPointPhotoCount() {
        return pointPhotoCount;
    }

    public List<PhotoUrl> getPointPhotos() {
        return pointPhotos;
    }

    public int getAreaPhotoCount() {
        return areaPhotoCount;
    }

    public List<PhotoUrl> getAreaPhotos() {
        return areaPhotos;
    }
}
