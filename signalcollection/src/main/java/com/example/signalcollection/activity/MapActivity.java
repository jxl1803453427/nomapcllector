package com.example.signalcollection.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.example.signalcollection.Constans;
import com.example.signalcollection.R;
import com.example.signalcollection.util.PoiOverlay;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Konmin on 2016/9/29.
 */

public class MapActivity extends BaseActivity implements OnGetPoiSearchResultListener {


    @BindView(R.id.map)
    MapView mMapView;


    @BindView(R.id.et_keyword)
    EditText etKeyword;
    private BaiduMap mBaiduMap;
    private PoiSearch mPointSearch;
    private String city;

    @BindView(R.id.tv_msg)
    TextView tvMsg;
    private String region;
    private String keyword;
    private String street;

    private GeoCoder mGeoCoder;

    private int times = 0;
    private BitmapDescriptor aim = BitmapDescriptorFactory.fromResource(R.drawable.ic_aim);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showBack();
        setMyTitle("地理位置");
        city = getIntent().getStringExtra(Constans.CITY);
        keyword = getIntent().getStringExtra(Constans.KEYWORD);
        region = getIntent().getStringExtra(Constans.REGION);
        street = getIntent().getStringExtra(Constans.STREET);
        etKeyword.setText(keyword);
        mPointSearch = PoiSearch.newInstance();
        mBaiduMap = mMapView.getMap();
        mPointSearch.setOnGetPoiSearchResultListener(this);
        times = 1;
        //search(keyword);
        mGeoCoder = GeoCoder.newInstance();

        mGeoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
                if (geoCodeResult != null&& geoCodeResult.getLocation()!=null) {
                    MapStatus.Builder builder = new MapStatus.Builder();
                    builder.target(geoCodeResult.getLocation()).zoom(18.0f);
                    mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                    MarkerOptions markerOptions = new MarkerOptions().position(geoCodeResult.getLocation()).icon(aim).zIndex(6);
                    mBaiduMap.addOverlay(markerOptions);
                } else {
                    showTest("位置没找到……");
                }
            }

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {

            }
        });
        mGeoCoder.geocode(new GeoCodeOption().city(city).address(region + street));
    }


    @OnClick(R.id.tv_search)
    public void searchClick() {
        hideSoftInput();
        if (!TextUtils.isEmpty(etKeyword.getText())) {
            times = 2;
            search(etKeyword.getText().toString());
            tvMsg.setText("");
        } else {
            showTest("输入的关键字不能为空");
        }

    }


    private void search(String keyword) {

        mPointSearch.searchInCity(new PoiCitySearchOption().city(city).keyword(keyword).pageNum(1));

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
        mBaiduMap.setMyLocationEnabled(false);
        mPointSearch.destroy();
        mMapView.onDestroy();
        mGeoCoder.destroy();
        mMapView = null;
        super.onDestroy();
    }

    @Override
    public void init() {
    }


    @Override
    public int createSuccessView() {
        return R.layout.activity_map;
    }


    @Override
    public void onGetPoiResult(PoiResult poiResult) {

        if (poiResult == null || poiResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
            //showTest("结果未找到");
            if (times < 2) {
                search(region + street);
                times = 2;
            } else {
                tvMsg.setText("结果未搜到，请手动输入搜索");
            }
            return;
        }
        if (poiResult.error == SearchResult.ERRORNO.NO_ERROR) {
            mBaiduMap.clear();
            PoiOverlay overlay = new MyPoiOverlay(mBaiduMap);
            mBaiduMap.setOnMarkerClickListener(overlay);
            overlay.setData(poiResult);
            overlay.addToMap();
            overlay.zoomToSpan();
            return;
        }

        if (poiResult.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {

            // 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
            String strInfo = "在";
            for (CityInfo cityInfo : poiResult.getSuggestCityList()) {
                strInfo += cityInfo.city;
                strInfo += ",";
            }
            strInfo += "找到结果";
            showTest(strInfo);
        }
    }


    private class MyPoiOverlay extends PoiOverlay {

        public MyPoiOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public boolean onPoiClick(int index) {
            super.onPoiClick(index);
            PoiInfo poi = getPoiResult().getAllPoi().get(index);
            mPointSearch.searchPoiDetail((new PoiDetailSearchOption()).poiUid(poi.uid));
            return true;
        }
    }


    @Override
    public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
        if (poiDetailResult.error != SearchResult.ERRORNO.NO_ERROR) {
            tvMsg.setText("抱歉，未找到结果");
        } else {
            tvMsg.setText(poiDetailResult.getName() + "\n" + poiDetailResult.getAddress());
        }

    }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

    }

}
