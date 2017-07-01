package com.example.signalcollection.bean;

import com.google.gson.annotations.Expose;

/**
 * 用户行为参数
 * Created by Konmin on 2016/7/29.
 */
public class UserBehavior {
    @Expose
    private String username;
    @Expose
    private String imei;
    @Expose
    private String phoneModel;
    @Expose
    private String systemVersion;
    @Expose
    private String networkType;
    @Expose
    private double longitude;
    @Expose
    private double latitude;
    @Expose
    private int action;


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getPhoneModel() {
        return phoneModel;
    }

    public void setPhomeModel(String phomeModel) {
        this.phoneModel = phomeModel;
    }

    public String getSystemVersion() {
        return systemVersion;
    }

    public void setSystemVersion(String systemVersion) {
        this.systemVersion = systemVersion;
    }

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }
}
