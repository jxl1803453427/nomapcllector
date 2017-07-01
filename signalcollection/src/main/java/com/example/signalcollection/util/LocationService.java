package com.example.signalcollection.util;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.orhanobut.logger.Logger;

import com.example.signalcollection.BaseApplication;

/**
 * 定位服务
 * Created by Konmin on 2016/8/1.
 */
public class LocationService {


    private static LocationService ourInstance;

    public LocationClient mLocationClient;
    public BDLocationListener mBdLocationListener;

    private LocationServiceListener mListener;
    private LocationCityListener mCityListener;

    private LocationResultListener mLocationResultListener;

    private boolean isFinished = false;

    private Handler mHandler;
    private Runnable mRunnable;

    private LocationService() {
        init();
    }


    private void init() {
        //用应用的context，应该不会出什么问题吧？
        mLocationClient = new LocationClient(BaseApplication.getContext());
        mBdLocationListener = new YYCBDLocationListener();
        mLocationClient.registerLocationListener(mBdLocationListener);
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 1100;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }


    public static LocationService getInstance() {

        if (ourInstance == null) {
            synchronized (LocationService.class) {
                if (ourInstance == null) {
                    ourInstance = new LocationService();
                }
            }
        }
        return ourInstance;
    }


    /**
     * 一次定位结束
     *
     * @param listener listener
     */
    public void start(LocationServiceListener listener) {
        mListener = listener;
        mLocationClient.start();
    }


    public void start(LocationCityListener listener) {
        mCityListener = listener;
        mLocationClient.start();
    }


    public void start(LocationResultListener listener) {
        mLocationResultListener = listener;
        isFinished = false;
        calculateTime();
        mLocationClient.start();
    }


    private class YYCBDLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            double latitude = bdLocation.getLatitude();
            double longitude = bdLocation.getLongitude();
            String errMsg = "";

            switch (bdLocation.getLocType()) {
                case BDLocation.TypeServerError:
                    errMsg = "服务端网络定位失败";
                    break;
                case BDLocation.TypeNetWorkException:
                    errMsg = "网络不同导致定位失败,请检查网络是否通畅";
                    break;
                case BDLocation.TypeCriteriaException:
                    errMsg = "无法获取有效定位依据导致定位失败，请重启手机再试";
                    break;
            }

            if (TextUtils.isEmpty(errMsg)) {
                Logger.i("定位成功！");
                mLocationClient.stop();
                if (mListener != null) {
                    mListener.onLocationResult(longitude, latitude, errMsg);
                }

                if (mLocationResultListener != null) {
                    isFinished = true;
                    mHandler.removeCallbacks(mRunnable);
                    mLocationResultListener.onCityResult(bdLocation.getCity(), bdLocation.getDistrict(), bdLocation.getCityCode());
                }

                if (mCityListener != null) {
                    isFinished = true;
                    mCityListener.onCityResult(bdLocation.getCity(), bdLocation.getCityCode());
                }
            } else {
                Logger.e("定位失败！" + errMsg);
                //定位失败是不会停止的，继续下一次定位
            }
        }
    }


    private void calculateTime() {
        if (mHandler == null) {
            mHandler = new Handler();
        }

        if (mRunnable == null) {
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    if (mLocationResultListener != null && !isFinished) {
                        mLocationResultListener.onErr("定位超时！");
                        mLocationClient.stop();
                    }
                }
            };
        }

        mHandler.postDelayed(mRunnable, 10 * 1000);
    }


    /**
     * 先这样，不定死了返回方法
     */
    public interface LocationServiceListener {

        void onLocationResult(double longitude, double latitude, String errMsg);

    }


    public interface LocationCityListener {
        void onCityResult(String cityName, String cityCode);
    }


    public interface LocationResultListener {

        void onCityResult(String cityName, String region, String cityCode);

        void onErr(String errMsg);
    }


}
