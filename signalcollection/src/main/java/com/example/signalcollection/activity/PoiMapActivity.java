package com.example.signalcollection.activity;

import android.content.Intent;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.example.signalcollection.Constans;
import com.example.signalcollection.R;
import com.example.signalcollection.bean.SimilarResult;
import com.example.signalcollection.bean.WorkListResult;
import com.example.signalcollection.network.WorkService;
import com.example.signalcollection.recyclerview.ViewHolder;
import com.example.signalcollection.util.SimilarDialog;
import com.example.signalcollection.util.UIUtils;
import com.example.signalcollection.view.AddAreaDialog;
import com.example.signalcollection.view.LoadingDialog;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;

/**
 * 新增商圈地图显示
 * Created by Konmin on 2016/10/9.
 */

public class PoiMapActivity extends BaseActivity {


    @BindView(R.id.map)
    MapView mMapView;

    @BindView(R.id.tv_info)
    TextView tvInfo;


    private BaiduMap mBaiduMap;
    private PoiInfo mPoiInfo;
    BitmapDescriptor aim = BitmapDescriptorFactory.fromResource(R.drawable.ic_aim);
    private LoadingDialog mLoadingDialog;
    private GeoCoder mGeoCoder;
    private AddAreaDialog mAddAreaDialog;
    private String mCityCode;
    ReverseGeoCodeResult.AddressComponent mAddressDetail;
    private SimilarDialog<WorkListResult.DataBean> mSimilarDialog;

    @Override
    public void init() {
        showBack();
        setMyTitle("商圈的位置");
        mLoadingDialog = new LoadingDialog(this);
        mBaiduMap = mMapView.getMap();
        mPoiInfo = getIntent().getParcelableExtra("poi");
        mCityCode = getIntent().getStringExtra("cityCode");
        tvInfo.setText(mPoiInfo.name + "\r\n" + mPoiInfo.address);
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(mPoiInfo.location).zoom(18.0f);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        MarkerOptions markerOptions = new MarkerOptions().position(mPoiInfo.location).icon(aim).zIndex(9);
        mBaiduMap.addOverlay(markerOptions);
        mGeoCoder = GeoCoder.newInstance();
        mGeoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
                mLoadingDialog.dismiss();
            }

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                mLoadingDialog.dismiss();
                mAddressDetail = reverseGeoCodeResult.getAddressDetail();
            }
        });
        mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(mPoiInfo.location));
        mLoadingDialog.show("现在获取地理信息…");
        mSimilarDialog = new SimilarDialog<WorkListResult.DataBean>(this) {
            @Override
            public void onPosition(ViewHolder holder, WorkListResult.DataBean dataBean) {
                // holder.setText(R.id.text1, dataBean.getAreaName());
                holder.setText(R.id.tv_area_name, Html.fromHtml(dataBean.getAreaName()));
                holder.setText(R.id.tv_address, dataBean.getAddress());
            }
        };
        mAddAreaDialog = new AddAreaDialog(this, mLoadingDialog);
    }

    @Override
    public int createSuccessView() {
        return R.layout.activity_poi_map;
    }


    @OnClick(R.id.tv_add)
    void addAreaClick() {
        if (mAddressDetail == null) {
            showTest("地理信息为空");
            mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(mPoiInfo.location));
            mLoadingDialog.show("现在获取地理信息…");
            return;
        }

        Intent intent = new Intent(this, ConfirmAddAreaTaskActivity.class);
        intent.putExtra(Constans.AREA_NAME, mPoiInfo.name);
        intent.putExtra(Constans.CITY_NAME, mPoiInfo.city);
        intent.putExtra(Constans.CITY_CODE, mCityCode);
        intent.putExtra(Constans.CITY_REGION, mAddressDetail.district);
        intent.putExtra(Constans.CITY_STRESS, mAddressDetail.street + mAddressDetail.streetNumber);
        intent.putExtra(Constans.LAT_LNG, mPoiInfo.location);
        intent.putExtra(Constans.UID, mPoiInfo.uid);
        startActivity(intent);
        this.finish();


        //getSimilarAreaTask(mPoiInfo.name, mCityCode);
        /*if (mTaskList != null && !mTaskList.isEmpty()) {

            mSimilarDialog.show(mTaskList, new SimilarDialog.OnDialogBtnClickListener() {
                @Override
                public void onYesClick() {
                    mAddAreaDialog.show(mPoiInfo.uid, mPoiInfo.name, mPoiInfo.city, mCityCode, mAddressDetail.district, mAddressDetail.street + mAddressDetail.streetNumber, mPoiInfo.location);
                }

                @Override
                public void onNoClick() {
                    finish();
                }
            });
        } else {
        }*/

    }


    /**
     * 查找类似的商圈
     *
     * @param keyword 关键字
     */
    private void getSimilarAreaTask(String keyword, String cityCode) {

        mLoadingDialog.show();
        Map<String, String> map = new HashMap<>();
        map.put("cityCode", cityCode);
        map.put("areaName", keyword);
        Subscription subscription = wrapObserverWithHttp(WorkService.getWorkService().searchSimilarAreaTask(map)).subscribe(new Subscriber<SimilarResult>() {

            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                mLoadingDialog.dismiss();
                //mAddAreaDialog.show(mPoiInfo.uid, mPoiInfo.name, mPoiInfo.city, mCityCode, mAddressDetail.district, mAddressDetail.street + mAddressDetail.streetNumber, mPoiInfo.location);
                e.printStackTrace();
                showTest(mNetWorkError);
            }

            @Override
            public void onNext(SimilarResult o) {
                mLoadingDialog.dismiss();
                Logger.e("getSimilarAreaTask result");
                if (o.getRetCode() == 0 && o.getData() != null && o.getData().getSimilarAreas() != null && !o.getData().getSimilarAreas().isEmpty()) {
                    markRed(o.getData().getSimilarAreas(), o.getData().getSegmentAreaNames());
                } else {

                    mAddAreaDialog.show(mPoiInfo.uid, mPoiInfo.name, mPoiInfo.city, mCityCode, mAddressDetail.district, mAddressDetail.street + mAddressDetail.streetNumber, mPoiInfo.location);
                }

            }
        });

        addSubscription(subscription);
    }


    private void markRed(final List<WorkListResult.DataBean> dataBeans, final List<String> keyword) {
        mLoadingDialog.show();
        final Observable<List<WorkListResult.DataBean>> observable = Observable.create(new Observable.OnSubscribe<List<WorkListResult.DataBean>>() {
            @Override
            public void call(Subscriber<? super List<WorkListResult.DataBean>> subscriber) {
                for (WorkListResult.DataBean dataBean : dataBeans) {
                    StringBuffer stringBuffer = new StringBuffer();
                    List<String> keywords = new ArrayList<String>(keyword);
                    UIUtils.addChild(dataBean.getAreaName(), keywords, stringBuffer);
                    dataBean.setAreaName(stringBuffer.toString());
                    StringBuffer strAddress = new StringBuffer();
                    if (!TextUtils.isEmpty(dataBean.getCityName())) {
                        strAddress.append(dataBean.getCityName());
                    }
                    if (!TextUtils.isEmpty(dataBean.getCityRegion())) {
                        strAddress.append(dataBean.getCityRegion());
                    }

                    if (!TextUtils.isEmpty(dataBean.getCityStress())) {
                        strAddress.append(dataBean.getCityStress());
                    }
                    if (!TextUtils.isEmpty(dataBean.getAddress())) {
                        strAddress.append(dataBean.getAddress());
                    }
                    dataBean.setAddress(strAddress.toString());
                }
                subscriber.onNext(dataBeans);
                subscriber.onCompleted();
            }
        });


        Subscription subscription = wrapObserverWithHttp(observable).subscribe(new Subscriber<List<WorkListResult.DataBean>>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                mLoadingDialog.dismiss();
                e.printStackTrace();
            }

            @Override
            public void onNext(List<WorkListResult.DataBean> dataBeans) {
                mLoadingDialog.dismiss();
                mSimilarDialog.show(dataBeans, new SimilarDialog.OnDialogBtnClickListener() {
                    @Override
                    public void onYesClick() {
                        mAddAreaDialog.show(mPoiInfo.uid, mPoiInfo.name, mPoiInfo.city, mCityCode, mAddressDetail.district, mAddressDetail.street + mAddressDetail.streetNumber, mPoiInfo.location);
                    }

                    @Override
                    public void onNoClick() {
                        //finish();
                    }
                });
            }
        });
        addSubscription(subscription);
    }


    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }


    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        mGeoCoder.destroy();
        aim.recycle();
        mAddAreaDialog = null;
        super.onDestroy();
    }
}
