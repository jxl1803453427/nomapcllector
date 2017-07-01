package com.example.signalcollection.bean;

import com.google.gson.annotations.Expose;

import org.litepal.crud.DataSupport;

import java.util.List;

/**
 * Created by hehe on 2016/7/22.
 */
public class ReportWrongBody extends DataSupport {


    /**
     * refAreaCode : A-SZ755-WDTCSNBCSJCD3
     * wrongType : 1
     * refReportUsername : nonmap001
     * photos : ["http://jmtool3.jjfinder.com/wrongarea/1.png","http://jmtool3.jjfinder.com/wrongarea/2.png"]
     */
    @Expose
    private String refAreaCode;
    @Expose
    private int wrongType;
    @Expose
    private String refReportUsername;
    @Expose
    private List<String> photos;

    private String photosUrls;


    @Expose
    private String remark;


    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getRefAreaCode() {
        return refAreaCode;
    }

    public void setRefAreaCode(String refAreaCode) {
        this.refAreaCode = refAreaCode;
    }

    public int getWrongType() {
        return wrongType;
    }

    public void setWrongType(int wrongType) {
        this.wrongType = wrongType;
    }

    public String getRefReportUsername() {
        return refReportUsername;
    }

    public void setRefReportUsername(String refReportUsername) {
        this.refReportUsername = refReportUsername;
    }

    public List<String> getPhotos() {
        return photos;
    }

    public void setPhotos(List<String> photos) {
        this.photos = photos;
    }

    public String getPhotosUrls() {
        return photosUrls;
    }

    public void setPhotosUrls(String photosUrls) {
        this.photosUrls = photosUrls;
    }
}
