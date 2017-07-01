package com.example.signalcollection.view;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.example.signalcollection.Constans;
import com.example.signalcollection.R;
import com.example.signalcollection.activity.CollectionActivity;
import com.example.signalcollection.activity.PoiMapActivity;
import com.example.signalcollection.bean.AddBusinessBody;
import com.example.signalcollection.bean.AddBusinessResult;
import com.example.signalcollection.bean.BusinessTypeList;
import com.example.signalcollection.bean.RegionList;
import com.example.signalcollection.bean.WorkListResult;
import com.example.signalcollection.network.WorkService;
import com.example.signalcollection.recyclerview.SpinnerAdapter;
import com.example.signalcollection.util.SPUtils;
import com.example.signalcollection.util.UIUtils;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 添加商圈的dialog
 * Created by Konmin on 2016/10/9.
 */

public class AddAreaDialog extends Dialog {


    private TextView tvCity;
    private TextView tvRegion;
    private TextView tvStreet;
    private TextView tvAreaName;
    private EditText etRemark;
    private Spinner spAreaType;
    private View vCancel;
    private View vConfirm;
    private LoadingDialog mLoadingDialog;
    private List<BusinessTypeList.DataBean> mLtBusineessTypeList;
    private SpinnerAdapter<BusinessTypeList.DataBean> mBusinessAdapter;
    private Context mContext;
    private long regionId;
    private String mCityCode;
    private String mBusinessType;
    private String businessName;
    private String street;
    private List<RegionList.DataBean> mLtRegion;
    private String mRegionName;
    private String mUid;
    private LatLng mLocation;

    private static final String SELECT = "select";

    public AddAreaDialog(Context context, LoadingDialog loadingDialog) {
        super(context, R.style.Style_Dialog);
        setContentView(R.layout.dialog_add_area_confirm);
        mLoadingDialog = loadingDialog;
        findViewByIds();
        mContext = context;
        mBusinessAdapter = new SpinnerAdapter<BusinessTypeList.DataBean>(context) {
            @Override
            public void setText(TextView textView, BusinessTypeList.DataBean dataBean) {
                textView.setText(dataBean.getName());
            }
        };
        spAreaType.setAdapter(mBusinessAdapter);
        spAreaType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                BusinessTypeList.DataBean dataBean = mLtBusineessTypeList.get(position);
                mBusinessType = dataBean.getCode();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        loadBusinessTypeList();
        setCancelable(false);
    }


    public void show(String uid, String name, String city, String cityCode, String region, String street, LatLng location) {

        tvCity.setText("城市：" + city);
        tvRegion.setText("区域：" + region);
        tvStreet.setText("街道：" + street);
        tvAreaName.setText("商圈名称：" + name);


        mCityCode = cityCode;
        this.street = street;
        businessName = name;
        mRegionName = region;
        mUid = uid;
        mLocation = location;
        loadRegion(cityCode);

        show();
    }


    private void findViewByIds() {
        tvCity = (TextView) findViewById(R.id.tv_city);
        tvRegion = (TextView) findViewById(R.id.tv_region);
        tvStreet = (TextView) findViewById(R.id.tv_street);
        tvAreaName = (TextView) findViewById(R.id.tv_area_name);
        etRemark = (EditText) findViewById(R.id.et_remark);
        spAreaType = (Spinner) findViewById(R.id.sp_area_type);
        vCancel = findViewById(R.id.tv_cancel);
        vConfirm = findViewById(R.id.tv_confirm);
        vCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        vConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //要校验一下一些值;

                if (TextUtils.isEmpty(mBusinessType)) {
                    showToast("商圈类型没有加载出来，请等加载后正确选择再添加！");
                    loadBusinessTypeList();
                    return;
                }


                if (mBusinessType.equals(SELECT)) {
                    showToast("请正确选择商圈类型再添加");
                    return;
                }

                if (regionId == 0) {
                    showToast("区域没加载正确，请等区域加载完成再添加");
                    loadRegion(mCityCode);
                    return;
                }

                mLoadingDialog.show("正在提交新增商圈数据…");


              /*  LocationService.getInstance().start(new LocationService.LocationServiceListener() {
                    @Override
                    public void onLocationResult(double longitude, double latitude, String errMsg) {*/
                String mRemark = etRemark.getText().toString();
                AddBusinessBody addBusinessBody = new AddBusinessBody();
                addBusinessBody.setAreaName(businessName);
                addBusinessBody.setRefCityCode(mCityCode);
                addBusinessBody.setRemark(mRemark);
                addBusinessBody.setRefRegionId(regionId);
                addBusinessBody.setUid(mUid);
                addBusinessBody.setCityStress(street);
                addBusinessBody.setRefAreaTypeCode(mBusinessType);
                addBusinessBody.setLatitude(mLocation.latitude);
                addBusinessBody.setLongitude(mLocation.longitude);
                addBusinessBody.setRefAddUsername(SPUtils.getUserName());
                Logger.i("data :" + new Gson().toJson(addBusinessBody));
                addBusiness(addBusinessBody);
               /*     }
                });*/
            }
        });
    }


    private void addBusiness(AddBusinessBody businessBody) {


        WorkService.getWorkService().addBusiness(businessBody).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<AddBusinessResult>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                mLoadingDialog.dismiss();
                showToast("网络错误");
            }

            @Override
            public void onNext(AddBusinessResult addBusinessResult) {
                mLoadingDialog.dismiss();
                if (addBusinessResult.getRetCode() == 0) {
                    Intent intent = new Intent(mContext, CollectionActivity.class);
                    WorkListResult.DataBean dataBean = new WorkListResult.DataBean();
                    dataBean.setAreaName(addBusinessResult.getData().getAreaName());
                    dataBean.setAreaCode(addBusinessResult.getData().getAreaCode());
                    dataBean.setAreaTypeName(addBusinessResult.getData().getAreaTypeName());
                    dataBean.setRefAreaTypeCode(addBusinessResult.getData().getRefAreaTypeCode());
                    intent.putExtra(Constans.TASK_BEAN, dataBean);
                    mContext.startActivity(intent);
                    etRemark.setText("");
                    dismiss();
                    ((PoiMapActivity) mContext).finish();
                } else if (addBusinessResult.getRetCode() == 810) {
                    UIUtils.getAlertDialog(mContext, "提示", addBusinessResult.getMsg(), "确定", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((PoiMapActivity) mContext).finish();
                        }
                    }).show();
                } else {
                    showToast(addBusinessResult.getMsg());
                }

            }
        });
    }


    private void loadBusinessTypeList() {
        mLoadingDialog.show();
        WorkService.getWorkService().getBusinessTypeList().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<BusinessTypeList>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                mLoadingDialog.dismiss();
                showToast("网络出错！");
            }

            @Override
            public void onNext(BusinessTypeList businessTypeList) {
                mLoadingDialog.dismiss();
                if (businessTypeList.getRetCode() == 0) {


                    mLtBusineessTypeList = businessTypeList.getData();
                    BusinessTypeList.DataBean dataBean = new BusinessTypeList.DataBean();
                    dataBean.setName("请选择");
                    dataBean.setCode(SELECT);
                    mLtBusineessTypeList.add(0, dataBean);
                    //添加一个请选择的项
                    mBusinessAdapter.setListData(mLtBusineessTypeList);
                } else {
                    showToast(businessTypeList.getMsg());
                }
            }
        });
    }


    private void loadRegion(String cityCode) {
        mLoadingDialog.show();
        Map<String, String> map = new HashMap<>();
        map.put("cityCode", cityCode);
        WorkService.getWorkService().getRegionList(map).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<RegionList>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                mLoadingDialog.dismiss();
                e.printStackTrace();
                showToast("网络错误！");
            }

            @Override
            public void onNext(RegionList regionList) {
                mLoadingDialog.dismiss();
                if (regionList.getRetCode() == 0) {
                    mLtRegion = regionList.getData();
                    for (RegionList.DataBean dataBean : mLtRegion) {
                        //做匹配吗?
                        if (dataBean.getCityRegionName().trim().equals(mRegionName.trim())) {
                            regionId = dataBean.getId();
                        }
                    }

                } else {
                    showToast(regionList.getMsg());
                }
            }
        });
    }


    private void showToast(String text) {
        Toast.makeText(mContext, text, Toast.LENGTH_LONG).show();
    }

}
