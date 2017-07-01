package com.example.signalcollection.bean;

import com.google.gson.annotations.Expose;
import com.yyc.mcollector.bean.WifiData;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * WIFI信号项
 * Created by bukp on 2016/4/7.
 */
public class WifiSignalItem extends DataSupport implements Serializable {


    @Expose
    String mac;
    @Expose
    String ssid;
    @Expose
    Integer rssi;

    NmpReportPoint nmpReportPoint;

    Integer nmpreportpoint_id;


    public NmpReportPoint getNmpReportPoint() {
        return nmpReportPoint;
    }

    public void setNmpReportPoint(NmpReportPoint nmpReportPoint) {
        this.nmpReportPoint = nmpReportPoint;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public Integer getRssi() {
        return rssi;
    }

    public void setRssi(Integer rssi) {
        this.rssi = rssi;
    }

    public Integer getNmpreportpoint_id() {
        return nmpreportpoint_id;
    }

    public void setNmpreportpoint_id(Integer nmpreportpoint_id) {
        this.nmpreportpoint_id = nmpreportpoint_id;
    }


    public static WifiSignalItem createFromWifiData(NmpReportPoint point, WifiData wifiData) {

        WifiSignalItem item = new WifiSignalItem();
        item.setSsid(wifiData.getSsid());
        item.setMac(wifiData.getMac());
        item.setRssi(wifiData.getRssi());
        item.setNmpReportPoint(point);
        return item;
    }

}
