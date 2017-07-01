package com.example.signalcollection.bean;

import com.yyc.mcollector.bean.BaseStationData;
import com.yyc.mcollector.bean.IBeaconData;
import com.yyc.mcollector.bean.MagneticData;
import com.yyc.mcollector.bean.WifiData;

import java.io.Serializable;
import java.util.List;

/**
 * 再封装一次信号集合
 * Created by Konmin on 2017/2/15.
 */

public class SignalBean implements Serializable {


    private List<WifiData> wifiDataList;
    private List<BaseStationData> baseStationDataList;
    private MagneticData magneticData;

    private long createTime;

   // private List<IBeaconData> bluetoothData;

    /*public SignalBean(List<WifiData> wifiDataList, List<BaseStationData> baseStationDataList, List<IBeaconData> bluetoothData, MagneticData magneticData) {
        this.wifiDataList = wifiDataList;
        this.baseStationDataList = baseStationDataList;
        this.magneticData = magneticData;
        this.bluetoothData = bluetoothData;
    }*/


    public SignalBean(List<WifiData> wifiDataList, List<BaseStationData> baseStationDataList, MagneticData magneticData, long createTime/*, List<IBeaconData> bluetoothData*/) {
        this.wifiDataList = wifiDataList;
        this.baseStationDataList = baseStationDataList;
        this.magneticData = magneticData;
        this.createTime = createTime;
        //this.bluetoothData = bluetoothData;
    }

    public long getCreateTime() {
        return createTime;
    }

    public List<WifiData> getWifiDataList() {
        return wifiDataList;
    }

    public List<BaseStationData> getBaseStationDataList() {
        return baseStationDataList;
    }

    public MagneticData getMagneticData() {
        return magneticData;
    }

   /* public List<IBeaconData> getBluetoothData() {
        return bluetoothData;
    }*/


}
