package com.example.signalcollection.bean;

import com.google.gson.annotations.Expose;

/**
 * Created by Konmin on 2016/9/11.
 */
public class UnAssignedRequest {

    public UnAssignedRequest(String cityCode, String cityRegionName, String searchText, String refAreaTypeCode, int statusCode, int pageNumber, int pageSize) {
        this.pageSize = pageSize;
        this.cityCode = cityCode;
        this.cityRegionName = cityRegionName;
        this.pageNumber = pageNumber;
        this.searchText = searchText;
        this.refAreaTypeCode = refAreaTypeCode;
        this.statusCode = statusCode;
    }

    @Expose
    private String cityCode;

    @Expose
    private String cityRegionName;

    @Expose
    private int pageNumber;

    @Expose
    private int pageSize;


    @Expose
    private int statusCode; //1待领取，2已领取 3，全部

    @Expose
    private String searchText;

    @Expose
    private String refAreaTypeCode;
}
