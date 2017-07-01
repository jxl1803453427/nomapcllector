package com.example.signalcollection.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;
import com.example.signalcollection.Constans;
import com.example.signalcollection.R;
import com.example.signalcollection.bean.AddBusinessBodyV2;
import com.example.signalcollection.bean.AddBusinessResult;
import com.example.signalcollection.bean.AreaExprop;
import com.example.signalcollection.bean.BusinessTypeList;
import com.example.signalcollection.bean.Exprop;
import com.example.signalcollection.bean.NmpReportPoint;
import com.example.signalcollection.bean.Predefine;
import com.example.signalcollection.bean.RegionList;
import com.example.signalcollection.bean.SearchResult;
import com.example.signalcollection.bean.SimilarResult;
import com.example.signalcollection.bean.WorkListResult;
import com.example.signalcollection.network.NetWorkService;
import com.example.signalcollection.network.WorkService;
import com.example.signalcollection.recyclerview.SpinnerAdapter;
import com.example.signalcollection.recyclerview.ViewHolder;
import com.example.signalcollection.util.DialogUtils;
import com.example.signalcollection.util.SPUtils;
import com.example.signalcollection.util.SimilarDialog;
import com.example.signalcollection.util.UIUtils;
import com.example.signalcollection.view.LoadingDialog;
import com.example.signalcollection.view.SearchBandDialog;
import com.example.signalcollection.view.SearchMultiSelectDialog;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.example.signalcollection.R.id.tvIn;

/**
 * Created by Konmin on 2017/3/22.
 */

public class ConfirmAddAreaTaskActivity extends BaseActivity {


    @BindView(R.id.tv_city)
    TextView tvCity;
    @BindView(R.id.tv_region)
    TextView tvRegion;
    @BindView(R.id.tv_street)
    TextView tvStreet;
    @BindView(R.id.tv_area_name)
    TextView tvAreaName;
    @BindView(R.id.sp_area_type)
    Spinner spAreaType;
    @BindView(R.id.llytAdd)
    LinearLayout layoutExprop;
    @BindView(R.id.et_remark)
    EditText etRemark;


    private SpinnerAdapter<BusinessTypeList.DataBean> mBusinessAdapter;
    private String mBusinessType;
    // private NetWorkService.Work mBusineessTypeList;
    private LoadingDialog mLoadingDialog;
    private List<BusinessTypeList.DataBean> mLtBusineessTypeList;

    private SpinnerAdapter<Predefine.Data> mPredefineSpinnerAdapter;
    private Map<String, List<Predefine.Data>> mPredefineDataMap = new HashMap<>();
    private Map<String, List<Exprop.DataBean>> mExpropDataMap = new HashMap<>();
    private Map<String, AreaExprop> mExprop2SubmitMap = new HashMap<>();
    private DialogUtils mDialogUtils;
    private SearchBandDialog mSearchBandDialog;

    private AddBusinessBodyV2 mBusinessBodyV2;

    private String mRemark;
    private List<RegionList.DataBean> mLtRegion;
    private String mRegionName;
    private int mRegionId;
    private String mRefCityCode;

    private SearchMultiSelectDialog mSearchMultiSelectDialog;
    private SimilarDialog<WorkListResult.DataBean> mSimilarDialog;
    private static final String SELECT = "select";

    /**
     * "areaName": "用户自行添加的美容院测试",
     * "refCityCode": "SZ755",
     * "remark": "在这个地方找到了一个美容院",
     * "longitude": 22.33333,
     * "latitude": 109.34234,
     * "cityRegion": "南山区",
     * "cityStress": "石厦北三街",
     */


    @Override
    public void init() {
        setMyTitle("确认添加商圈");
        showBack();
        Intent intent = getIntent();
        mBusinessBodyV2 = new AddBusinessBodyV2();

        String areaName = intent.getStringExtra(Constans.AREA_NAME);
        tvAreaName.setText("商圈名称：" + areaName);
        mBusinessBodyV2.setAreaName(areaName);


        String city = intent.getStringExtra(Constans.CITY_NAME);
        mRefCityCode = intent.getStringExtra(Constans.CITY_CODE);
        mBusinessBodyV2.setRefCityCode(mRefCityCode);
        tvCity.setText("城市：" + city);


        mRegionName = intent.getStringExtra(Constans.CITY_REGION);
        tvRegion.setText("区域：" + mRegionName);

        String cityStress = intent.getStringExtra(Constans.CITY_STRESS);
        tvStreet.setText("街道：" + cityStress);
        mBusinessBodyV2.setCityStress(cityStress);
        mBusinessBodyV2.setUid(intent.getStringExtra(Constans.UID));
        LatLng latLng = intent.getParcelableExtra(Constans.LAT_LNG);
        loadRegion();
        mBusinessBodyV2.setLatitude(latLng.latitude);
        mBusinessBodyV2.setLongitude(latLng.longitude);


        mLoadingDialog = new LoadingDialog(this);
        mDialogUtils = new DialogUtils(this);
        mBusinessAdapter = new SpinnerAdapter<BusinessTypeList.DataBean>(this) {
            @Override
            public void setText(TextView textView, BusinessTypeList.DataBean dataBean) {
                textView.setText(dataBean.getName());
            }
        };
        spAreaType.setAdapter(mBusinessAdapter);
        spAreaType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                BusinessTypeList.DataBean dataBean = (BusinessTypeList.DataBean) parent.getAdapter().getItem(position);
                layoutExprop.removeAllViews();
                mExprop2SubmitMap.clear();
                mBusinessType = dataBean.getCode();
                if (!mBusinessType.equals(SELECT)) {
                    List<Exprop.DataBean> exprops = mExpropDataMap.get(dataBean.getCode());
                    if (exprops == null) {
                        Logger.e("load exprop data from net");
                        loadExpropFromNet(mBusinessType);
                    } else {
                        Logger.e("load exprop data from View");
                        loadExprop2View(exprops);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        loadBusinessTypeList();
        mPredefineSpinnerAdapter = new SpinnerAdapter<Predefine.Data>(this) {
            @Override
            public void setText(TextView textView, Predefine.Data data) {
                textView.setText(data.getTagName());
            }
        };

        mSimilarDialog = new SimilarDialog<WorkListResult.DataBean>(this) {
            @Override
            public void onPosition(ViewHolder holder, WorkListResult.DataBean dataBean) {
                holder.setText(R.id.tv_area_name, Html.fromHtml(dataBean.getAreaName()));
                holder.setText(R.id.tv_address, dataBean.getAddress());
            }
        };
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
                showTest(mNetWorkError);
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
                    mBusinessAdapter.setListData(mLtBusineessTypeList);
                } else {
                    showTest(businessTypeList.getMsg());
                }
            }
        });


    }


    /**
     * 把扩展属性放到 view 里
     *
     * @param exrops 扩展属性列表
     */
    private void loadExprop2View(final List<Exprop.DataBean> exrops) {
        Logger.e("loadExprop2Views");
        for (final Exprop.DataBean dataBean : exrops) {
            View view = LayoutInflater.from(this).inflate(R.layout.item_exprop, layoutExprop, false);
            TextView tvTitle = (TextView) view.findViewById(R.id.tv);
            tvTitle.setTag(dataBean.getPropCode());
            final TextView tvValue = (TextView) view.findViewById(tvIn);
            tvTitle.setText(dataBean.getPropName() + ":");
            LinearLayout.LayoutParams layoutParames = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParames.setMargins(0, UIUtils.dip2px(8), 0, 0);
            Logger.e("add view ");
            layoutExprop.addView(view, layoutParames);

            switch (dataBean.getControlType()) {
                case Constans.SPINNER_CONTROL_TYPE:
                    List<Predefine.Data> datas = mPredefineDataMap.get(dataBean.getPropCode());
                    if (datas == null || datas.isEmpty()) {
                        loadPredefineDataFromNet(Constans.SPINNER_CONTROL_TYPE, tvValue, dataBean.getPropCode());
                    }
                    tvValue.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            List<Predefine.Data> datas = mPredefineDataMap.get(dataBean.getPropCode());
                            if (datas == null || datas.isEmpty()) {
                                loadPredefineDataFromNet(Constans.SPINNER_CONTROL_TYPE, tvValue, dataBean.getPropCode());
                                return;
                            }
                            mDialogUtils.showSelectExpropDialog("请选择" + dataBean.getPropName(), datas, new DialogUtils.SpinnerItemSelectedListener() {
                                @Override
                                public void onItemSelect(NmpReportPoint.Exprop exprop) {
                                    exprop.setRefExPropCode(dataBean.getPropCode());
                                    AreaExprop areaExprop = new AreaExprop();
                                    areaExprop.setRefExpropCode(exprop.getRefExPropCode());
                                    areaExprop.setPropValue(exprop.getPropValue());
                                    areaExprop.setIsOtherInput(exprop.getIsOtherInput());
                                    areaExprop.setTagCode(exprop.getTagCode());
                                    mExprop2SubmitMap.put(exprop.getRefExPropCode(), areaExprop);
                                    tvValue.setText(exprop.getPropValue());
                                }
                            });
                        }
                    });
                    break;
                case Constans.MULTI_INPUT_CONTROL_TYPE:
                    tvValue.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDialogUtils.showEditDialog("请输入" + dataBean.getPropName(), tvValue.getText().toString(), false, new DialogUtils.EditResultListener() {
                                @Override
                                public void resultText(String text) {
                                    AreaExprop exprop = new AreaExprop();
                                    exprop.setPropValue(text);
                                    exprop.setRefExpropCode(dataBean.getPropCode());
                                    tvValue.setText(exprop.getPropValue());
                                    mExprop2SubmitMap.put(dataBean.getPropCode(), exprop);
                                }
                            });
                        }
                    });
                    break;
                case Constans.MULTI_SELECT_CONTROL_TYPE:
                    List<Predefine.Data> list = mPredefineDataMap.get(dataBean.getPropCode());
                    if (list == null || list.isEmpty()) {
                        loadPredefineDataFromNet(Constans.MULTI_SELECT_CONTROL_TYPE, tvValue, dataBean.getPropCode());
                    }
                    tvValue.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            List<Predefine.Data> datas = mPredefineDataMap.get(dataBean.getPropCode());
                            if (datas == null || datas.isEmpty()) {
                                loadPredefineDataFromNet(Constans.MULTI_SELECT_CONTROL_TYPE, tvValue, dataBean.getPropCode());
                                return;
                            }
                            showCreateDialog(dataBean, datas, tvValue);
                        }
                    });
                    break;
                case Constans.SEARCH_CONTROL_TYPE:
                    tvValue.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //启动一个搜索的dialog
                            if (mSearchBandDialog == null) {
                                mSearchBandDialog = new SearchBandDialog(ConfirmAddAreaTaskActivity.this);
                            }

                            String api = dataBean.getSearchApi();
                            if (TextUtils.isEmpty(api)) {

                                showTest("后台数据异常，请退出后重试");
                                return;
                            }


                            String url = api.substring(0, api.lastIndexOf("."));
                            url = url + "/" + mBusinessType + ".j5";

                            mSearchBandDialog.show(dataBean.getPropName(), url, new SearchBandDialog.OnItemSelectListener() {
                                @Override
                                public void onItemSelected(SearchResult.Data data) {
                                    tvValue.setText(data.getShowName());
                                    //现在是什么也不做
                                    AreaExprop exprop = new AreaExprop();
                                    exprop.setPropValue(data.getShowName());
                                    exprop.setRefExpropCode(dataBean.getPropCode());
                                    exprop.setTagCode(data.getBrandCode());
                                    mExprop2SubmitMap.put(dataBean.getPropCode(), exprop);
                                    //品牌的这些扩展属性不需要了
                                }

                                @Override
                                public void onOtherClick() {
                                    //添加品牌，请求获得添加品牌的扩展属性
                                    mDialogUtils.showEditDialog("请输入" + dataBean.getPropName(), null, true, new DialogUtils.EditResultListener() {
                                        @Override
                                        public void resultText(String text) {
                                            tvValue.setText(text);
                                            AreaExprop exprop = new AreaExprop();
                                            exprop.setPropValue(text);
                                            exprop.setRefExpropCode(dataBean.getPropCode());
                                            exprop.setIsOtherInput(1);
                                            mExprop2SubmitMap.put(dataBean.getPropCode(), exprop);
                                        }
                                    });
                                }

                                @Override
                                public void onErrMsg(String msg) {
                                    showTest(msg);
                                }
                            });
                        }
                    });

                    break;
                case Constans.SEARCH_MULTI_SELECT_CONTROL_TYPE:

                    tvValue.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String api = dataBean.getSearchApi();
                            String url = api.substring(0, api.lastIndexOf("."));
                            url = url + "/" + mBusinessType + ".j5";
                            if (mSearchMultiSelectDialog == null) {
                                mSearchMultiSelectDialog = new SearchMultiSelectDialog(ConfirmAddAreaTaskActivity.this);
                            }

                            mSearchMultiSelectDialog.show(url, new SearchMultiSelectDialog.OnConfirmListener() {
                                @Override
                                public void onConfirm(String results) {
                                    tvValue.setText(results);
                                    AreaExprop exprop = new AreaExprop();
                                    exprop.setPropValue(results);
                                    exprop.setRefExpropCode(dataBean.getPropCode());
                                    mExprop2SubmitMap.put(dataBean.getPropCode(), exprop);
                                }
                            });
                        }
                    });
                    break;
            }

        }

    }


    private void loadRegion() {
        //mLoadingDialog.show();
        Map<String, String> map = new HashMap<>();
        map.put("cityCode", mRefCityCode);
        WorkService.getWorkService().getRegionList(map).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<RegionList>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                // mLoadingDialog.dismiss();
                e.printStackTrace();
                showTest(mNetWorkError);
            }

            @Override
            public void onNext(RegionList regionList) {
                // mLoadingDialog.dismiss();
                if (regionList.getRetCode() == 0) {
                    mLtRegion = regionList.getData();
                    for (RegionList.DataBean dataBean : mLtRegion) {
                        if (dataBean.getCityRegionName().trim().equals(mRegionName.trim())) {
                            mRegionId = dataBean.getId();
                        }
                    }

                } else {
                    showTest(regionList.getMsg());
                }
            }
        });
    }


    /**
     * 多选框类型的扩展属性对话框
     *
     * @param dataBean dataBean
     * @param datas    datas
     * @param mTv      mTv
     */
    private void showCreateDialog(final Exprop.DataBean dataBean, final List<Predefine.Data> datas, final TextView mTv) {

        Dialog dialog;
        AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("请选择" + dataBean.getPropName());
        final String[] strs = new String[datas.size()];
        if (datas instanceof ArrayList) {
            for (int i = 0; i < datas.size(); i++) {
                strs[i] = datas.get(i).getTagName();
            }
        }

        final boolean[] flags = new boolean[strs.length];//初始复选情况
        final StringBuffer result = new StringBuffer();
        final StringBuffer resultCode = new StringBuffer();
        builder.setMultiChoiceItems(strs, flags, new DialogInterface.OnMultiChoiceClickListener() {
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                flags[which] = isChecked;
            }
        });

        //添加一个确定按钮
        builder.setPositiveButton(" 确 定 ", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                for (int i = 0; i < flags.length; i++) {
                    if (flags[i]) {
                        if (TextUtils.isEmpty(result)) {
                            result.append(strs[i]);
                            resultCode.append(datas.get(i).getTagCode());
                        } else {
                            result.append(";").append(strs[i]);
                            resultCode.append(";").append(datas.get(i).getTagCode());
                        }
                    }
                }
                mTv.setText(result);
                AreaExprop exprop = new AreaExprop();
                exprop.setPropValue(result.toString());
                exprop.setRefExpropCode(dataBean.getPropCode());
                exprop.setTagCode(resultCode.toString());
                mExprop2SubmitMap.put(dataBean.getPropCode(), exprop);
            }
        });
        //创建一个复选框对话框
        dialog = builder.create();
        dialog.show();
        Log.e("TAG", "sss");
    }


    /**
     * 从网络中获取扩展属性
     *
     * @param areaTypeCode 商圈的类型code
     */
    private void loadExpropFromNet(final String areaTypeCode) {

        mLoadingDialog.show();
        Map<String, String> map = new HashMap<>();
        map.put("areaTypeCode", areaTypeCode);
        Subscription subscription = wrapObserverWithHttp(WorkService.getWorkService().getExpropByAreaTypeCode(map)).subscribe(new Subscriber<Exprop>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                mLoadingDialog.dismiss();
                e.printStackTrace();
                showTest(mNetWorkError);
            }

            @Override
            public void onNext(Exprop exprop) {
                mLoadingDialog.dismiss();

                Logger.e(new Gson().toJson(exprop));
                if (exprop.getRetCode() == 0) {
                    if (exprop.getData() != null && !exprop.getData().isEmpty()) {
                        mExpropDataMap.put(areaTypeCode, exprop.getData());
                        loadExprop2View(exprop.getData());
                    }
                } else {
                    showTest(exprop.getMsg());
                }
            }
        });

        addSubscription(subscription);
    }


    /**
     * 从网络中加载预设值
     *
     * @param textView   textView
     * @param expropCode expropCode
     */
    private void loadPredefineDataFromNet(final int controlType, final TextView textView, final String expropCode) {

        Map<String, String> map = new HashMap<>();
        map.put("refAreaTypeCode", mBusinessType);
        map.put("refExPropCode", expropCode);
        mLoadingDialog.show();
        Subscription subscription = wrapObserverWithHttp(WorkService.getWorkService().getPredefineByAreaCodeAndExpropCode(map)).subscribe(new Subscriber<Predefine>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                mLoadingDialog.dismiss();
                showTest(mNetWorkError);
            }

            @Override
            public void onNext(Predefine predefine) {
                mLoadingDialog.dismiss();
                if (predefine.getRetCode() == 0 && !predefine.getData().isEmpty()) {
                    mPredefineDataMap.put(expropCode, predefine.getData());
                    if (controlType == Constans.SPINNER_CONTROL_TYPE) {
                        textView.setText(predefine.getData().get(0).getTagName());
                        Predefine.Data data = predefine.getData().get(0);
                        AreaExprop areaExprop = new AreaExprop();
                        areaExprop.setRefExpropCode(expropCode);
                        areaExprop.setPropValue(data.getTagName());
                        //areaExprop.setIsOtherInput(data.getNeedOtherInput());
                        areaExprop.setTagCode(data.getTagCode());
                        mExprop2SubmitMap.put(expropCode, areaExprop);
                    }
                } else {
                    showTest(predefine.getMsg());
                }
            }
        });
        addSubscription(subscription);

    }


    @OnClick(R.id.tv_confirm)
    public void onClick() {


        if (mBusinessType.equals(SELECT)) {

            showTest("请选择商圈类型");
            return;

        }

        String bulidingName = null;

        for (int i = 0; i < layoutExprop.getChildCount(); i++) {
            View view = layoutExprop.getChildAt(i);
            TextView tvTitle = (TextView) view.findViewById(R.id.tv);
            TextView tvValue = (TextView) view.findViewById(R.id.tvIn);
            if (((String) tvTitle.getTag()).trim().equals("EXP-BUILDING")) {
                bulidingName = tvValue.getText().toString();
            }

            /*if (TextUtils.isEmpty(tvValue.getText())) {ss
                String tagName = tvTitle.getText().toString();
                showTest("请选择或填写" + tagName.substring(0, tagName.length() - 1));
                return;
            }*/
        }

        mRemark = etRemark.getText().toString();
        if (TextUtils.isEmpty(mRemark)) {
            showTest("请填写说明");
            return;
        }


        if (mRegionId == 0) {
            loadRegion();
            showTest("区域没找到，请重试…");
            return;
        }

        String areaName = mBusinessBodyV2.getAreaName();
        if (!TextUtils.isEmpty(bulidingName)) {
            areaName = mBusinessBodyV2.getAreaName() + "(" + bulidingName + ")";
        }
        //先去查找类似商圈
        getSimilarAreaTask(areaName, mRefCityCode);
    }


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
                if (o.getRetCode() == 0) {
                    if (o.getData() != null && o.getData().getSimilarAreas() != null && !o.getData().getSimilarAreas().isEmpty()) {
                        markRed(o.getData().getSimilarAreas(), o.getData().getSegmentAreaNames());
                    } else {
                        userAddAreaTask();
                    }
                } else {
                    showTest(o.getMsg());
                }
            }
        });

        addSubscription(subscription);
    }


    /**
     * 加载标红
     * @param dataBeans
     * @param keyword
     */
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
                        //新增
                        userAddAreaTask();
                        //mAddAreaDialog.show(mPoiInfo.uid, mPoiInfo.name, mPoiInfo.city, mCityCode, mAddressDetail.district, mAddressDetail.street + mAddressDetail.streetNumber, mPoiInfo.location);
                    }

                    @Override
                    public void onNoClick() {
                        finish();
                    }
                });
            }
        });
        addSubscription(subscription);
    }


    /**
     * 将扩展属性从map转回list
     *
     * @param map map
     * @return map
     */
    private List<AreaExprop> exchangeMap2List(Map<String, AreaExprop> map) {

        List<AreaExprop> list = new ArrayList<>();
        Iterator<String> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            list.add(map.get(iterator.next()));
        }
        return list;
    }


    /**
     * 做新增商圈动作
     */
    private void userAddAreaTask() {

        mBusinessBodyV2.setRefAddUsername(SPUtils.getUserName());
        mBusinessBodyV2.setRefAreaTypeCode(mBusinessType);
        mBusinessBodyV2.setExProps(exchangeMap2List(mExprop2SubmitMap));
        mBusinessBodyV2.setRefRegionId(mRegionId);
        mBusinessBodyV2.setRemark(mRemark);
        Logger.e(new Gson().toJson(mBusinessBodyV2));
        mLoadingDialog.show("正在新增商圈…");
        Subscription subscription = wrapObserverWithHttp(WorkService.getWorkService().userAddAreaTask(mBusinessBodyV2)).subscribe(new Subscriber<AddBusinessResult>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                mLoadingDialog.dismiss();
                e.printStackTrace();
                showTest(mNetWorkError);
            }

            @Override
            public void onNext(AddBusinessResult addBusinessResult) {

                mLoadingDialog.dismiss();
                if (addBusinessResult.getRetCode() == 0) {
                    Intent intent = new Intent(ConfirmAddAreaTaskActivity.this, CollectionActivity.class);
                    WorkListResult.DataBean dataBean = new WorkListResult.DataBean();
                    dataBean.setAreaName(addBusinessResult.getData().getAreaName());
                    dataBean.setAreaCode(addBusinessResult.getData().getAreaCode());
                    dataBean.setAreaTypeName(addBusinessResult.getData().getAreaTypeName());
                    dataBean.setRefAreaTypeCode(addBusinessResult.getData().getRefAreaTypeCode());
                    intent.putExtra(Constans.TASK_BEAN, dataBean);
                    ConfirmAddAreaTaskActivity.this.startActivity(intent);
                    etRemark.setText("");
                    //这个页面要关掉
                    ConfirmAddAreaTaskActivity.this.finish();

                } else if (addBusinessResult.getRetCode() == 810) {
                    UIUtils.getAlertDialog(ConfirmAddAreaTaskActivity.this, "提示", addBusinessResult.getMsg(), "确定", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ConfirmAddAreaTaskActivity.this.finish();
                        }
                    }).show();
                } else {
                    showTest(addBusinessResult.getMsg());
                }
            }
        });

        addSubscription(subscription);

    }


    @Override
    public int createSuccessView() {
        return R.layout.activity_add_area_task_confirm;
    }


}
