package com.example.signalcollection.activity;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.signalcollection.R;
import com.example.signalcollection.bean.BusinessTypeList;
import com.example.signalcollection.bean.CityList;
import com.example.signalcollection.bean.DefaultResult;
import com.example.signalcollection.bean.NmpReportData;
import com.example.signalcollection.bean.NmpReportPoint;
import com.example.signalcollection.bean.PhotoUrl;
import com.example.signalcollection.bean.RegionList;
import com.example.signalcollection.bean.UnAssignedRequest;
import com.example.signalcollection.bean.WorkListResult;
import com.example.signalcollection.network.WorkService;
import com.example.signalcollection.recyclerview.CommonAdapter;
import com.example.signalcollection.recyclerview.SpinnerAdapter;
import com.example.signalcollection.recyclerview.ViewHolder;
import com.example.signalcollection.util.LocationService;
import com.example.signalcollection.util.SPUtils;
import com.example.signalcollection.util.UIUtils;
import com.example.signalcollection.view.LoadingDialog;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;

public class TasklistActivity extends BaseActivity {


    @BindView(R.id.list)
    RecyclerView mList;

    @BindView(R.id.spRegion)
    Spinner mSpCityRegion;

    @BindView(R.id.tvCity)
    TextView mTvCity;
    @BindView(R.id.tv_more_task)
    View tvMoreTask;
    @BindView(R.id.spType)
    Spinner spType;
    @BindView(R.id.et_keyword)
    EditText etKeyword;


    private LoadingDialog mLoadingDialog;
    private String mCurrentRegionName;
    private String mCurrentCityCode;

    private CommonAdapter<WorkListResult.DataBean> mCommonAdapter;
    private List<WorkListResult.DataBean> mLt = new ArrayList<>();
    private SpinnerAdapter mAdapterRegion;
    private SpinnerAdapter mAdapterBussinessType;
    private List<RegionList.DataBean> mLtRegionList;
    private Map<String, List<RegionList.DataBean>> mMapRegions = new HashMap<>();

    private List<BusinessTypeList.DataBean> mBusinessType;
    private String mCurrentBussinessTypeCode;
    private int pageIndex = 1;

    private boolean needClean;
    private boolean nonFirst;

    private boolean canScroll;
    private Dialog mGetTaskDialog;

    private List<CityList.DataBean> mCityList;
    //private int mStatus = 3;


    @Override
    public void init() {
        Logger.i("init Fragment");
        showBack();
        setMyTitle("领取采集任务");
        //EventBus.getDefault().register(this);
        mLoadingDialog = new LoadingDialog(this);

        mCommonAdapter = new CommonAdapter<WorkListResult.DataBean>(this, R.layout.rvitem_waitcollect, mLt) {
            @Override
            public void convert(ViewHolder holder, final WorkListResult.DataBean o) {
                holder.setText(R.id.tvName, "[" + o.getId() + "] " + o.getAreaName());
                holder.setText(R.id.tv_type, o.getCityName() + "-" + o.getCityRegion() + "-" + o.getAreaTypeName());
                String addr = (TextUtils.isEmpty(o.getCityStress()) ? "" : o.getCityStress()) + (TextUtils.isEmpty(o.getAddress()) ? "" : o.getAddress());
                holder.setText(R.id.tvInfo, o.getCityStress() + addr);
                holder.setOnClickListener(R.id.tv_get_task, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showGetTaskDialog(o);
                    }
                });
            }
        };
        mList.setAdapter(mCommonAdapter);
        mList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (isSlideToBottom(recyclerView) && canScroll) {

                    //没有数据的时候这里会被执行
                    Logger.i("is slideTo Bottom:");
                    needClean = false;
                    canScroll = false;
                    pageIndex++;
                    loadTaskList();
                    tvMoreTask.setVisibility(View.VISIBLE);
                }
            }
        });


        mAdapterRegion = new SpinnerAdapter<RegionList.DataBean>(this) {
            @Override
            public void setText(TextView textView, RegionList.DataBean o) {
                textView.setText(o.getCityRegionName());
                textView.setTextColor(getResources().getColor(R.color.text_color_black));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
            }
        };

        mAdapterBussinessType = new SpinnerAdapter<BusinessTypeList.DataBean>(this) {
            @Override
            public void setText(TextView textView, BusinessTypeList.DataBean o) {
                textView.setText(o.getName());
                textView.setTextColor(getResources().getColor(R.color.text_color_black));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
            }
        };

        mSpCityRegion.setAdapter(mAdapterRegion);
        spType.setAdapter(mAdapterBussinessType);

        //城市区域的选择
        mSpCityRegion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Logger.i("cityName:" + mCurrentCityCode + " mCurrentRegionName " + mLtRegionList.get(position).getCityRegionName());
                mCurrentRegionName = mLtRegionList.get(position).getCityRegionName();
                pageIndex = 1;
                needClean = true;
                loadTaskList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCurrentBussinessTypeCode = mBusinessType.get(position).getCode();
                if (nonFirst) {
                    needClean = true;
                    pageIndex = 1;
                    Logger.i("nonFrist" + mCurrentBussinessTypeCode);
                    loadTaskList();
                }
                nonFirst = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        /*SpinnerAdapter statusAdapter = new SpinnerAdapter<String>(this) {
            @Override
            public void setText(TextView textView, String status) {
                textView.setText(status);
            }
        };

        statusAdapter.setListData(statusList);
        spStatus.setAdapter(statusAdapter);
        spStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mStatus = 3 - position;
                if (nonFirst) {
                    pageIndex = 1;
                    needClean = true;
                    loadTaskList();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });*/
        locationCity();
        loadBusinessTypeList();
    }


    /**
     * 网络加载商圈类型的列表
     */
    private void loadBusinessTypeList() {
        //mLoadingDialog.show("正在加载商圈类型…");
        Subscription sbMyAccount = wrapObserverWithHttp(WorkService.getWorkService().getBusinessTypeList()).subscribe(new Subscriber<BusinessTypeList>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                //mLoadingDialog.dismiss();
                showTest(mNetWorkError);
            }

            @Override
            public void onNext(BusinessTypeList businessTypeList) {
                //mLoadingDialog.dismiss();
                if (businessTypeList.getRetCode() == 0) {
                    mBusinessType = businessTypeList.getData();
                    if (mBusinessType != null) {
                        BusinessTypeList.DataBean data = new BusinessTypeList.DataBean();
                        data.setName("全部");
                        mBusinessType.add(0, data);
                    }
                    mAdapterBussinessType.setListData(mBusinessType);
                } else {
                    showTest(businessTypeList.getMsg());
                }
            }
        });
        addSubscription(sbMyAccount);
    }


    /**
     * 显示获取任务列表的对话框
     *
     * @param o
     */
    private void showGetTaskDialog(final WorkListResult.DataBean o) {

        mGetTaskDialog = UIUtils.getAlertDialog(this, "提示", "是否领取\n" + o.getAreaName() + "\n采集任务？领取后需尽快完成。若两天未提交，任务自动作废", "确定领取", "暂不领取", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTaskForMySelf(o);
                mGetTaskDialog.dismiss();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGetTaskDialog.dismiss();
            }
        });

        mGetTaskDialog.show();
    }


    /**
     * 检查是否划到底了
     *
     * @param recyclerView
     * @return
     */
    private boolean isSlideToBottom(RecyclerView recyclerView) {
        if (recyclerView == null || recyclerView.getChildCount() == 0) return false;
        if (recyclerView.computeVerticalScrollExtent() + recyclerView.computeVerticalScrollOffset() >= recyclerView.computeVerticalScrollRange())
            return true;
        return false;
    }


    /**
     * 领取任务列表
     *
     * @param dataBean
     */
    private void getTaskForMySelf(final WorkListResult.DataBean dataBean) {

        mLoadingDialog.show("正在领取任务…");

        //在领取任务之前先看看数据库有没有这个商圈
        Observable<Boolean> observable = Observable.create(new Observable.OnSubscribe<Boolean>() {

            @Override
            public void call(Subscriber<? super Boolean> subscriber) {

                NmpReportData data = DataSupport.where("areacode = ?", dataBean.getAreaCode()).findFirst(NmpReportData.class, true);
                if (data != null) {
                    //只会找到一个
                    if (data.getPoints() != null && !data.getPoints().isEmpty()) {
                        for (NmpReportPoint point : data.getPoints()) {
                            point.setRemark(1);
                            point.update(point.getId());
                        }
                    }
                    //审核照片全部删掉
                    DataSupport.deleteAll(PhotoUrl.class, "nid = " + data.getId() + " AND phototype = 2");
                }
                subscriber.onNext(true);
                subscriber.onCompleted();
            }
        });

        Subscription su = wrapObserverWithHttp(observable).subscribe(new Subscriber<Boolean>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                mLoadingDialog.dismiss();
                e.printStackTrace();
                Logger.e(e.getMessage());
                showTest("查询本地错误！");
            }

            @Override
            public void onNext(Boolean o) {
                Map<String, String> map = new HashMap<>();
                map.put("areaCode", dataBean.getAreaCode());
                map.put("userName", SPUtils.getUserName());
                Subscription subscription = wrapObserverWithHttp(WorkService.getWorkService().userDraw(map)).subscribe(new Subscriber<DefaultResult>() {
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
                    public void onNext(DefaultResult o) {
                        mLoadingDialog.dismiss();
                        if (o.getRetCode() == 0) {
                            NmpReportData mNmpReportData = new NmpReportData();
                            mNmpReportData.setAreaCode(dataBean.getAreaCode());
                            mNmpReportData.setAreaName(dataBean.getAreaName());
                            mNmpReportData.setAreaTypeName(dataBean.getAreaTypeName());
                            mNmpReportData.setRefAreaTypeCode(dataBean.getRefAreaTypeCode());
                            mNmpReportData.save();
                            mLt.remove(dataBean);
                            mCommonAdapter.notifyDataSetChanged();
                            showTest("领取任务成功！");
                        } else {
                            showTest(o.getMsg());
                        }

                    }
                });
                addSubscription(subscription);
            }
        });
        addSubscription(su);
    }


    /**
     * 加载任务列表
     */
    private void loadTaskList() {

        if (needClean) {
            mLoadingDialog.show("正在加载任务列表…");
        }

        int pageSize = 20;
        Logger.i("need clean:" + needClean);
        String text = etKeyword.getText().toString();
        if (TextUtils.isEmpty(text)) {
            text = null;
        }
        UnAssignedRequest request = new UnAssignedRequest(mCurrentCityCode, mCurrentRegionName, text, mCurrentBussinessTypeCode, 1, pageIndex, pageSize);
        Logger.i(new Gson().toJson(request));
        Subscription subscription = wrapObserverWithHttp(WorkService.getWorkService().getByCondition(request)).subscribe(new Subscriber<WorkListResult>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                if (tvMoreTask.getVisibility() == View.VISIBLE) {
                    tvMoreTask.setVisibility(View.GONE);
                }
                if (needClean) {
                    mLoadingDialog.dismiss();
                }

                e.printStackTrace();
                showTest(mNetWorkError);
            }

            @Override
            public void onNext(WorkListResult workListResult) {

                if (tvMoreTask.getVisibility() == View.VISIBLE) {
                    tvMoreTask.setVisibility(View.GONE);
                }

                if (needClean) {
                    mLoadingDialog.dismiss();
                    mLt.clear();
                    mCommonAdapter.setData(mLt);
                }
                canScroll = true;

                if (workListResult.getRetCode() == 0) {
                    //可能的数据为空
                    if (workListResult.getData() != null) {

                        Logger.e(new Gson().toJson(workListResult.getData()));
                        mLt.addAll(workListResult.getData());
                        mCommonAdapter.setData(mLt);
                        if (needClean) {
                            mList.scrollToPosition(0);
                        }
                    } else {
                        pageIndex--;
                    }
                } else {
                    pageIndex--;
                    showTest(workListResult.getMsg());
                }
            }

        });

        addSubscription(subscription);

    }


    @OnClick(R.id.tvCity)
    public void tvCityClick() {


        locationCity();
    }


    /**
     * 获取定位城市
     */
    private void locationCity() {
        mLoadingDialog.show("正在定位城市…");
        LocationService.getInstance().start(new LocationService.LocationResultListener() {
            @Override
            public void onCityResult(String cityName, String region, String cityCode) {
                mLoadingDialog.dismiss();
                mTvCity.setText(cityName);
                if (mCityList == null || mCityList.isEmpty()) {
                    loadCityList(cityName, region);
                } else {

                    CityList.DataBean cityBean = findCity(cityName);
                    if (cityBean == null) {
                        Logger.e("cityBean is null");
                        showTest("您所在的城市还没有采集任务，请联系我们的工作人员");
                    } else {
                        Logger.e("cityBean is not null");
                        if (TextUtils.isEmpty(mCurrentCityCode) || !mCurrentCityCode.equals(cityBean.getCityCode())) {
                            mCurrentCityCode = cityBean.getCityCode();
                            needClean = true;
                            loadRegion(mCurrentCityCode, region);
                        }
                    }
                }
            }

            @Override
            public void onErr(String errMsg) {
                mLoadingDialog.dismiss();
                showTest("定位超时，请点城市重新定位！");
            }
        });
    }


    /**
     *  mLoadingDialog.dismiss();
     mTvCity.setText(cityName);
     CityList.DataBean cityBean = findCity(cityName);
     if (cityBean == null) {
     showTest("您所在的城市还没有采集任务，请联系我们的工作人员");
     } else {

     if (TextUtils.isEmpty(mCurrentCityCode)) {
     mCurrentCityCode = cityBean.getCityCode();
     needClean = true;
     loadRegion(mCurrentCityCode);
     } else if (!mCurrentCityCode.equals(cityBean.getCityCode())) {
     needClean = true;
     loadRegion(mCurrentCityCode);
     }


     }
     */
    /**
     * 查找就是为了能找到对应的cityCode,获取城市任务是通过cityCode来的
     *
     * @param cityName
     * @return
     */
    private CityList.DataBean findCity(String cityName) {


        Logger.e("find city");
        for (CityList.DataBean dataBean : mCityList) {
            if (dataBean.getCityName().equals(cityName)) {
                return dataBean;
            }
        }
        return null;

    }

/*
    @Subscribe
    public void onCitySelected(CityList.DataBean dataBean) {

        mTvCity.setText(dataBean.getCityName());
        loadRegion(dataBean.getCityCode());
        mCurrentCityCode = dataBean.getCityCode();
        mLtRegionList = mMapRegions.get(mCurrentCityCode);
        needClean = true;
        if (mLtRegionList == null) {
            mLt.clear();
            mCommonAdapter.notifyDataSetChanged();
            loadRegion(mCurrentCityCode);
        } else {
            mAdapterRegion.setListData(mLtRegionList);
            mSpCityRegion.setSelection(0);
            mCurrentRegionName = mLtRegionList.get(0).getCityRegionName();
            Logger.i("city code :" + mCurrentCityCode + "mCurrentRegionName:" + mCurrentRegionName);
            pageIndex = 1;
            loadTaskList();
        }
    }*/

    /**
     * 搜索按钮的点击事件
     */
    @OnClick(R.id.tv_search)
    public void onClick() {
        //
        pageIndex = 1;
        needClean = true;
        loadTaskList();
    }


    /**
     * 加载区域列表
     *
     * @param cityCode cityCode
     */
    private void loadRegion(final String cityCode, final String locationRegion) {
        mLoadingDialog.show("正在加载区域…");
        Map<String, String> map = new HashMap<>();
        map.put("cityCode", cityCode);
        Subscription sbMyAccount = wrapObserverWithHttp(WorkService.getWorkService().getRegionList(map)).subscribe(new Subscriber<RegionList>() {
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
            public void onNext(RegionList regionList) {
                mLoadingDialog.dismiss();
                if (regionList.getRetCode() == 0) {
                    mLtRegionList = regionList.getData();
                    mAdapterRegion.setListData(mLtRegionList);
                    if (mLtRegionList != null && !mLtRegionList.isEmpty()) {
                        mMapRegions.put(cityCode, mLtRegionList);
                        int position = findRegionPosition(locationRegion);
                        if (mSpCityRegion.getSelectedItemPosition() != position) {
                            mSpCityRegion.setSelection(position);
                        } else {
                            mCurrentRegionName = mLtRegionList.get(0).getCityRegionName();
                            Logger.i("cityCode:" + mCurrentCityCode + "mCurrentRegionName:" + mCurrentRegionName);
                            pageIndex = 1;
                            loadTaskList();
                        }

                    }
                } else {
                    showTest(regionList.getMsg());
                }
            }
        });
        addSubscription(sbMyAccount);
    }


    private int findRegionPosition(String locationRegion) {


        for (int i = 0; i < mLtRegionList.size(); i++) {


            if (mLtRegionList.get(i).getCityRegionName().equals(locationRegion)) {
                return i;

            }

        }

        return 0;
    }

    /**
     * 网络加载城市列表
     */
    private void loadCityList(final String cityName, final String region) {
        mLoadingDialog.show("正在加载城市…");
        Subscription sbMyAccount = wrapObserverWithHttp(WorkService.getWorkService().getCityList()).subscribe(new Subscriber<CityList>() {
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
            public void onNext(CityList cityList) {
                mLoadingDialog.dismiss();
                if (cityList.getRetCode() == 0 && cityList.getData() != null) {
                    mCityList = cityList.getData();
                    //加载完城市，就定位城市

                    CityList.DataBean cityBean = findCity(cityName);
                    if (cityBean == null) {
                        Logger.e("cityBean is null");
                        showTest("您所在的城市还没有采集任务，请联系我们的工作人员");
                    } else {
                        Logger.e("cityBean is not null");
                        if (TextUtils.isEmpty(mCurrentCityCode) || !mCurrentCityCode.equals(cityBean.getCityCode())) {
                            mCurrentCityCode = cityBean.getCityCode();
                            needClean = true;
                            loadRegion(mCurrentCityCode, region);
                        }
                    }
                    //locationCity();
                } else {
                    showTest(cityList.getMsg());
                }

            }
        });
        addSubscription(sbMyAccount);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionAdd:

                Intent intent = new Intent(this, AddAreaTaskActivity.class);
                intent.putExtra("cityCode", mCurrentCityCode);
                intent.putExtra("cityName", mTvCity.getText().toString());
                startActivity(intent);
                //baseStartActivity(AddAreaTaskActivity.class);
                break;

        }
        return super.onOptionsItemSelected(item);

    }


    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    public int createSuccessView() {
        return R.layout.activity_wait_collect;
    }

}
