package com.example.signalcollection.bean;

import com.google.gson.annotations.Expose;
import com.yyc.mcollector.bean.IBeaconData;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * 蓝牙信号采集
 * Created by Konmin on 2017/3/14.
 */

public class BleSignal extends DataSupport implements Serializable {

    @Expose
    private String mac;
    @Expose
    private String name;
    @Expose
    private int rssi;
    @Expose
    private String ibeaconUUID;
    @Expose
    private String ibeaconMajorId;
    @Expose
    private int ibeaconMinorId;


    private NmpReportPoint nmpReportPoint;


    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public String getIbeaconUUID() {
        return ibeaconUUID;
    }

    public void setIbeaconUUID(String ibeaconUUID) {
        this.ibeaconUUID = ibeaconUUID;
    }

    public String getIbeaconMajorId() {
        return ibeaconMajorId;
    }

    public void setIbeaconMajorId(String ibeaconMajorId) {
        this.ibeaconMajorId = ibeaconMajorId;
    }

    public int getIbeaconMinorId() {
        return ibeaconMinorId;
    }

    public void setIbeaconMinorId(int ibeaconMinorId) {
        this.ibeaconMinorId = ibeaconMinorId;
    }


    public NmpReportPoint getNmpReportPoint() {
        return nmpReportPoint;
    }

    public void setNmpReportPoint(NmpReportPoint nmpReportPoint) {
        this.nmpReportPoint = nmpReportPoint;
    }

   /* public static BleSignal createFromBluetoothData(NmpReportPoint nmpReportPoint, IBeaconData data) {
        BleSignal signal = new BleSignal();
        signal.setMac(data.bluetoothAddress);
        signal.setRssi(data.rssi);
        signal.setName(data.name);
        signal.setIbeaconMajorId(String.valueOf(data.major));
        signal.setIbeaconUUID(data.proximityUuid);
        signal.setIbeaconMinorId(data.minor);
        signal.setNmpReportPoint(nmpReportPoint);
        return signal;
    }*/
}
