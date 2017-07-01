package com.example.signalcollection.activity;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.example.signalcollection.R;
import com.example.signalcollection.bean.CityList;
import com.example.signalcollection.bean.SimilarResult;
import com.example.signalcollection.bean.WorkListResult;
import com.example.signalcollection.network.WorkService;
import com.example.signalcollection.recyclerview.CommonAdapter;
import com.example.signalcollection.recyclerview.OnItemClickListener;
import com.example.signalcollection.recyclerview.ViewHolder;
import com.example.signalcollection.util.LocationService;
import com.example.signalcollection.util.UIUtils;
import com.example.signalcollection.view.LoadingDialog;
import com.example.signalcollection.view.SelectDialog;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

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
 * 新增商圈
 * Created by Konmin on 2016/10/8.
 */

public class AddAreaTaskActivity extends BaseActivity implements OnGetPoiSearchResultListener {


    @BindView(R.id.tv_city)
    TextView tvCity;
    @BindView(R.id.et_keyword)
    AutoCompleteTextView etKeyword;

    @BindView(R.id.rv_content)
    RecyclerView mRecyclerView;

    @BindView(R.id.tv_load_more)
    TextView tvLoadding;

    @BindView(R.id.layout_similar)
    View vSimilar;

    @BindView(R.id.tv_similar_count)
    TextView tvSimilarCount;
    @BindView(R.id.tv_similar_item1)
    TextView tvSimilarItem1;
    @BindView(R.id.tv_similar_item2)
    TextView tvSimilarItem2;

    @BindView(R.id.tv_similar_more)
    TextView tvSimilarItemMore;


    private LoadingDialog mLoadingDialog;
    //private SpinnerAdapter<DataBean> mCityAdapter;
    //private List<DataBean> mCityList;
    private String mCurrentCity;
    private PoiSearch mPoiSearch;

    private SuggestionSearch mSuggestionSearch;

    private CommonAdapter<PoiInfo> mCommonAdapter;
    private boolean isLoaddingMore;
    private List<PoiInfo> mPoiInfos;
    private boolean canScroll;
    private int currentPageIndex;
    private String mCurrentCityCode;

    private ArrayAdapter<String> mSuggestAdapter;
    private ArrayList<String> mSuggestList;
    private List<CityList.DataBean> mCityList;

    private List<WorkListResult.DataBean> mSimilarList;


    @Override

    public void init() {
        setMyTitle("新增商圈");
        showBack();
        mLoadingDialog = new LoadingDialog(this);
        EventBus.getDefault().register(this);

        mCurrentCityCode = getIntent().getStringExtra("cityCode");
        mCurrentCity = getIntent().getStringExtra("cityName");
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(this);
        mCommonAdapter = new CommonAdapter<PoiInfo>(this, R.layout.item_poi) {
            @Override
            public void convert(ViewHolder holder, final PoiInfo poiInfo) {
                holder.setText(R.id.tv_title, poiInfo.name);
                holder.setText(R.id.tv_address, poiInfo.address);
            }
        };

        mCommonAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(ViewGroup parent, View view, Object o, int position, ViewHolder viewHolder) {
                Intent intent = new Intent(AddAreaTaskActivity.this, PoiMapActivity.class);
                intent.putExtra("poi", mPoiInfos.get(position));
                //intent.putExtra("task", (ArrayList) mSimilarList);
                intent.putExtra("cityCode", mCurrentCityCode);
                startActivity(intent);
            }

            @Override
            public boolean onItemLongClick(ViewGroup parent, View view, Object o, int position, ViewHolder viewHolder) {
                return false;
            }
        });
        mRecyclerView.setAdapter(mCommonAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (isSlideToBottom(recyclerView) && canScroll) {
                    isLoaddingMore = true;
                    canScroll = false;
                    tvLoadding.setVisibility(View.VISIBLE);
                    currentPageIndex++;
                    mPoiSearch.searchInCity(new PoiCitySearchOption().pageNum(currentPageIndex).city(mCurrentCity).keyword(etKeyword.getText().toString()));
                }
            }
        });
        mSuggestionSearch = SuggestionSearch.newInstance();

        mSuggestionSearch.setOnGetSuggestionResultListener(new OnGetSuggestionResultListener() {
            @Override
            public void onGetSuggestionResult(SuggestionResult suggestionResult) {

                if (suggestionResult == null || suggestionResult.getAllSuggestions() == null) {
                    return;
                }


                mSuggestList = new ArrayList<String>();

                for (SuggestionResult.SuggestionInfo info : suggestionResult.getAllSuggestions()) {
                    if (info.key != null) {
                        mSuggestList.add(info.key);
                    }
                }
                mSuggestAdapter = new ArrayAdapter<String>(AddAreaTaskActivity.this, R.layout.item_text, mSuggestList);
                etKeyword.setAdapter(mSuggestAdapter);
                mSuggestAdapter.notifyDataSetChanged();
            }
        });

        etKeyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (!TextUtils.isEmpty(mCurrentCity) && !TextUtils.isEmpty(s)) {
                    mSuggestionSearch.requestSuggestion(new SuggestionSearchOption().city(mCurrentCity).keyword(s.toString()));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        loadCityList();
        tvCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baseStartActivity(CitySelectActivity.class);
            }
        });
    }


    private CityList.DataBean findOutCity() {

        if (TextUtils.isEmpty(mCurrentCityCode)) {
            return null;
        }

        for (CityList.DataBean dataBean : mCityList) {

            if (dataBean.getCityCode().equals(mCurrentCityCode)) {
                return dataBean;
            }
        }

        return null;
    }


    @Subscribe
    public void getCityBean(CityList.DataBean dataBean) {

        mCurrentCityCode = dataBean.getCityCode();
        mCurrentCity = dataBean.getCityName();
        tvCity.setText(mCurrentCity);

    }


    private void loadCityList() {
        mLoadingDialog.show();
        Subscription sbMyAccount = wrapObserverWithHttp(WorkService.getWorkService().getCityList()).subscribe(new Subscriber<CityList>() {
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
            public void onNext(CityList cityList) {
                mLoadingDialog.dismiss();
                if (cityList.getRetCode() == 0) {
                    mCityList = cityList.getData();
                    CityList.DataBean dataBean = findOutCity();
                    if (dataBean != null) {
                        tvCity.setText(dataBean.getCityName());
                        mCurrentCity = dataBean.getCityName();
                        mCurrentCityCode = dataBean.getCityCode();
                    } else {
                        tvCity.setText(mCurrentCity);
                        showTest("你所在的城市我们还没开始采集计划，请稍后再试…");
                    }

                } else {
                    showTest(cityList.getMsg());
                }
            }
        });
        addSubscription(sbMyAccount);
    }


    @Override
    public int createSuccessView() {
        return R.layout.activity_add_area;
    }


    @OnClick(R.id.tv_search)
    public void onClick() {

        if (TextUtils.isEmpty(etKeyword.getText())) {
            showTest("请输入搜索关键字");
            return;
        }

        isLoaddingMore = false;
        currentPageIndex = 0;
        if (!TextUtils.isEmpty(mCurrentCity)) {
            mLoadingDialog.show("正在搜索…");
            getSimilarAreaTask(etKeyword.getText().toString(), mCurrentCityCode);
        } else {
            showTest("请选择城市");
        }

    }


    /**
     * 查找类似的商圈
     *
     * @param keyword 关键字
     */
    private void getSimilarAreaTask(String keyword, String cityCode) {

        Map<String, String> map = new HashMap<>();
        map.put("cityCode", cityCode);
        map.put("areaName", keyword);
        Subscription subscription = wrapObserverWithHttp(WorkService.getWorkService().searchSimilarAreaTask(map)).subscribe(new Subscriber<SimilarResult>() {

            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                //mPoiSearch.searchInCity(new PoiCitySearchOption().city(mCurrentCity).keyword(etKeyword.getText().toString()).pageNum(currentPageIndex));
                mLoadingDialog.dismiss();

            }

            @Override
            public void onNext(SimilarResult o) {

                Logger.e("getSimilarAreaTask result");
                mPoiSearch.searchInCity(new PoiCitySearchOption().city(mCurrentCity).keyword(etKeyword.getText().toString()).pageNum(currentPageIndex));
                if (o.getRetCode() == 0 && o.getData() != null && o.getData().getSimilarAreas() != null && !o.getData().getSimilarAreas().isEmpty()) {

                    markRed(o.getData().getSimilarAreas(), o.getData().getSegmentAreaNames());
                } else {
                    vSimilar.setVisibility(View.GONE);
                }

            }
        });

        addSubscription(subscription);
    }


    private void markRed(final List<WorkListResult.DataBean> dataBeans, final List<String> keyword) {

        final Observable<List<WorkListResult.DataBean>> observable = Observable.create(new Observable.OnSubscribe<List<WorkListResult.DataBean>>() {
            @Override
            public void call(Subscriber<? super List<WorkListResult.DataBean>> subscriber) {

                //if(dataBeans.size())
                for (WorkListResult.DataBean dataBean : dataBeans) {
                    StringBuffer stringBuffer = new StringBuffer();
                    List<String> keywords = new ArrayList<String>(keyword);
                    UIUtils.addChild(dataBean.getAreaName(), keywords, stringBuffer);
                    dataBean.setAreaName(stringBuffer.toString());
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
                e.printStackTrace();
                mLoadingDialog.dismiss();

            }

            @Override
            public void onNext(List<WorkListResult.DataBean> dataBeans) {
                vSimilar.setVisibility(View.VISIBLE);
                mLoadingDialog.dismiss();
                mSimilarList = dataBeans;
                tvSimilarCount.setText("已有类似商圈（共" + dataBeans.size() + "个）");
                switch (dataBeans.size()) {

                    case 1:
                        tvSimilarItem1.setText(Html.fromHtml(dataBeans.get(0).getAreaName()));
                        tvSimilarItem2.setVisibility(View.GONE);
                        tvSimilarItemMore.setVisibility(View.GONE);
                        break;
                    case 2:
                        tvSimilarItem1.setText(Html.fromHtml(dataBeans.get(0).getAreaName()));
                        tvSimilarItem2.setText(Html.fromHtml(dataBeans.get(1).getAreaName()));
                        tvSimilarItem2.setVisibility(View.VISIBLE);
                        tvSimilarItemMore.setVisibility(View.GONE);
                    default:
                        tvSimilarItem1.setText(Html.fromHtml(dataBeans.get(0).getAreaName()));
                        tvSimilarItem2.setText(Html.fromHtml(dataBeans.get(1).getAreaName()));
                        tvSimilarItem2.setVisibility(View.VISIBLE);
                        tvSimilarItemMore.setVisibility(View.VISIBLE);
                }
            }
        });
        addSubscription(subscription);
    }


    private SelectDialog<WorkListResult.DataBean> mSelectDialog;

    @OnClick(R.id.tv_similar_more)
    public void onClickMore() {

        if (mSimilarList != null && !mSimilarList.isEmpty()) {
            if (mSelectDialog == null) {
                mSelectDialog = new SelectDialog<WorkListResult.DataBean>(this, "类似的商圈任务") {
                    @Override
                    public void showData(TextView textView, WorkListResult.DataBean dataBean) {
                        textView.setText(Html.fromHtml(dataBean.getAreaName()));
                    }
                };
            }
            mSelectDialog.setDataAndShow(mSimilarList);
        }
    }


    @Override
    public void onGetPoiResult(PoiResult poiResult) {
        Logger.i("poi result");
        mLoadingDialog.dismiss();
        canScroll = true;
        if (!isLoaddingMore && mPoiInfos != null && !mPoiInfos.isEmpty()) {
            mPoiInfos.clear();
            mCommonAdapter.notifyDataSetChanged();
        }
        if (tvLoadding.getVisibility() != View.GONE) {
            tvLoadding.setVisibility(View.GONE);
        }
        if (poiResult == null || poiResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {

            if (isLoaddingMore) {
                showTest("没有更多了");
            } else {
                showTest("结果未找到");
            }

            return;
        }
        if (poiResult.error == SearchResult.ERRORNO.NO_ERROR) {
            if (poiResult == null) {
                showTest("结果未找到");
                return;
            }

            if (isLoaddingMore) {
                mPoiInfos.addAll(poiResult.getAllPoi());
            } else {
                mPoiInfos = poiResult.getAllPoi();
            }
            mCommonAdapter.setData(mPoiInfos);

        }

        if (poiResult.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {
            String strInfo = "在";
            for (CityInfo cityInfo : poiResult.getSuggestCityList()) {
                strInfo += cityInfo.city;
                strInfo += ",";
            }
            strInfo += "找到结果";
            showTest(strInfo);
        }

    }


    protected boolean isSlideToBottom(RecyclerView recyclerView) {

        return !(recyclerView == null || recyclerView.getChildCount() == 0 || recyclerView.getChildCount() < 10) && recyclerView.computeVerticalScrollExtent() + recyclerView.computeVerticalScrollOffset() >= recyclerView.computeVerticalScrollRange();
    }

    @Override
    public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

    }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

    }


    @Override
    protected void onDestroy() {
        mPoiSearch.destroy();
        EventBus.getDefault().unregister(this);
        mSuggestionSearch.destroy();
        super.onDestroy();
    }
}
