package com.example.signalcollection.activity;

import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.example.signalcollection.R;
import com.orhanobut.logger.Logger;

/**
 * 测试功能的Activity
 * Created by Konmin on 2016/10/10.
 */

public class TestActivity extends BaseActivity {


    private GeoCoder mGeoCoder;


    @Override
    public void init() {
        mGeoCoder = GeoCoder.newInstance();
        mGeoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
                Logger.i(geoCodeResult.getAddress());
                Logger.i(geoCodeResult.getLocation().longitude + "," + geoCodeResult.getLocation().latitude);

            }

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {

            }
        });

        mGeoCoder.geocode(new GeoCodeOption().city("深圳").address("天虹"));
    }

    @Override
    public int createSuccessView() {
        return R.layout.activity_test;
    }
}
