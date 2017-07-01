package com.example.signalcollection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.example.signalcollection.bean.WifiSignalItem;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hehe on 2016/7/5.
 */
public class WifiInfoManager {
    //private static WifiInfoManager mInstance;
    private Context mContext;
    public WifiManager mWifiManager;
    private BroadcastReceiver mBroadCastReceiver;
    public List<ScanResult> mLtScanResults;
    //private ConnectionInterface mConnectionInterface;
    private int collectionNum = 6;


    public WifiInfoManager(Context context, ConnectionInterface connectionInterface) {
        mContext = context;
        init(connectionInterface);
    }


    public WifiInfoManager init(final ConnectionInterface connectionInterface) {
        mWifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mBroadCastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                    collectionNum++;
                    //Logger.i("collectionNum = " + collectionNum);
                    if (collectionNum == 2) {
                        mLtScanResults = mWifiManager.getScanResults();
                        connectionInterface.wifiResult(getWifiList());
                    } else {
                        mWifiManager.startScan();
                    }
                }
            }
        };
        mContext.registerReceiver(mBroadCastReceiver, intentFilter); // 注册监听
        return this;
    }

    List<ScanResult> mLtSort = new ArrayList<>();
    List<WifiSignalItem> mLtWifiSignalItem = new ArrayList<>();

    public List<WifiSignalItem> getWifiList() {
        ScanResult temp; // 记录临时中间值
        mLtSort.clear();
        mLtSort.addAll(mLtScanResults);

        if (mLtSort != null) {
            int size = mLtSort.size();
            for (int q = 0; q < size - 1; q++) {
                for (int j = q + 1; j < size; j++) {

                    if (mLtSort.get(q).level < mLtSort.get(j).level) {
                        temp = mLtSort.get(q);
                        mLtSort.set(q, mLtSort.get(j));
                        mLtSort.set(j, temp);
                    }
                }
            }

            if (mLtSort != null) {
                mLtWifiSignalItem.clear();
                ScanResult scanResult = null;
                int size1 = mLtSort.size();
                for (int i = 0; i < size1; i++) {
                    scanResult = mLtSort.get(i);
                    if (i < 15) {
                        if (scanResult != null) {
                            int nSigLevel = scanResult.level;
                            WifiSignalItem rwifiInfo = new WifiSignalItem();
                            rwifiInfo.setMac(scanResult.BSSID);
                            rwifiInfo.setSsid(scanResult.SSID);
                            rwifiInfo.setRssi(nSigLevel);
                            mLtWifiSignalItem.add(rwifiInfo);
                        }
                    } else {
                        break;
                    }
                }
            } else {
                // UIUtils.showToastSafe("当前位置无法进行室内定位");
            }
        }
        return mLtWifiSignalItem;
    }


    public void start() {
        collectionNum = 0;
        mWifiManager.startScan();
    }

    public void stop() {
        mLtScanResults = null;
        mContext.unregisterReceiver(mBroadCastReceiver);
    }
}
