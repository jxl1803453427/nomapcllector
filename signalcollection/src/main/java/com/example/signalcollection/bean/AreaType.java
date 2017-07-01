package com.example.signalcollection.bean;

import com.google.gson.annotations.Expose;

/**
 * Created by hehe on 2016/7/15.
 */
public class AreaType {


    @Expose
    private String areaTypeCode;

    public String getAreaTypeCode() {
        return areaTypeCode;
    }


    public void setAreaTypeCode(String areaTypeCode) {
        this.areaTypeCode = areaTypeCode;
    }
}
