package com.example.signalcollection.bean;

import com.google.gson.annotations.Expose;

import org.litepal.annotation.Column;
import org.litepal.crud.DataSupport;

/**
 * 照片数据表
 * Created by hehe on 2016/7/21.
 */
public class PhotoUrl extends DataSupport {


    private int id;


    private int nmpreportpoint_id;

    private String imgLocalUrl;//本地的路径


    @Expose
    @Column(unique = true, defaultValue = "unknown")
    private String photoUrl;//url


    @Expose
    private Long updateTime;//拍照时候的时间戳


    private Integer photoType;//照片类型，1是采集点的，2是审核的


    private int nid;//关联到采集点id或者是商圈的id


    private int isUpLoad;//是否已经上传了


    public int getIsUpLoad() {
        return isUpLoad;
    }

    public void setIsUpLoad(int isUpLoad) {
        this.isUpLoad = isUpLoad;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImgLocalUrl() {
        return imgLocalUrl;
    }

    public void setImgLocalUrl(String imgLocalUrl) {
        this.imgLocalUrl = imgLocalUrl;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getPhotoType() {
        return photoType;
    }

    public void setPhotoType(Integer photoType) {
        this.photoType = photoType;
    }

    public int getNid() {
        return nid;
    }

    public void setNid(int nid) {
        this.nid = nid;
    }

    public int getNmpreportpoint_id() {
        return nmpreportpoint_id;
    }

    public void setNmpreportpoint_id(int nmpreportpoint_id) {
        this.nmpreportpoint_id = nmpreportpoint_id;
    }
}
