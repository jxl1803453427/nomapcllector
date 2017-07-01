package com.example.signalcollection.bean;

import com.google.gson.annotations.Expose;

import org.litepal.annotation.Column;
import org.litepal.crud.DataSupport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 无地图搜集客户端,上报的数据
 * Created by bukp on 16/7/15.
 * 2017-5-8 添加了审核照片
 */
public class NmpReportData extends DataSupport implements Serializable {


    private int id;
    // 商圈编码
    @Expose
    @Column(unique = true, defaultValue = "unknown")
    private String areaCode;

    private String areaName;
    // 采集的点信息
    @Expose
    private List<NmpReportPoint> points = new ArrayList<>();
    @Expose
    @Column(ignore = true)
    private String userName;

    @Column(ignore = true)
    private int pointSize;

    @Expose
    @Column(ignore = true)
    private List<PhotoUrl> areaPhotos;//审核照片

    @Column(ignore = true)
    private String city;
    @Column(ignore = true)
    private String address;
    @Column(ignore = true)
    private String street;
    @Column(ignore = true)
    private String region;


    private int status; //0，为可以提交数据，1为不可以提交数据

    private String areaTypeName;

    private String refAreaTypeCode;

    private boolean isError;

    public int getStatus() {
        return status;
    }


    public List<PhotoUrl> getAreaPhotos() {
        return areaPhotos;
    }

    public void setAreaPhotos(List<PhotoUrl> areaPhotos) {
        this.areaPhotos = areaPhotos;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRefAreaTypeCode() {
        return refAreaTypeCode;
    }

    public void setRefAreaTypeCode(String refAreaTypeCode) {
        this.refAreaTypeCode = refAreaTypeCode;
    }

    public String getAreaTypeName() {
        return areaTypeName;
    }

    public void setAreaTypeName(String areaTypeName) {
        this.areaTypeName = areaTypeName;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public List<NmpReportPoint> getPoints() {
        return points;
    }

    public void setPoints(List<NmpReportPoint> points) {
        this.points = points;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isError() {
        return isError;
    }

    public void setError(boolean error) {
        isError = error;
    }

    public int getPointSize() {
        return pointSize;
    }

    public void setPointSize(int pointSize) {
        this.pointSize = pointSize;
    }
}
