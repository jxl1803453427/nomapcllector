package com.example.signalcollection;

import com.example.signalcollection.bean.WifiSignalItem;

import java.util.List;

/**
 * Created by hehe on 2016/7/5.
 */
public interface ConnectionInterface {
    public void wifiResult(List<WifiSignalItem> ltWifiSigalItem);
}
