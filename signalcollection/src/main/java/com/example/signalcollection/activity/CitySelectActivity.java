package com.example.signalcollection.activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.signalcollection.R;
import com.example.signalcollection.bean.CityList;
import com.example.signalcollection.network.WorkService;
import com.example.signalcollection.util.CharacterParser;
import com.example.signalcollection.util.LocationService;
import com.example.signalcollection.util.PinyinComparator;
import com.example.signalcollection.view.LoadingDialog;
import com.example.signalcollection.view.SideBarView;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;

/**
 * 选择城市的列表
 * Created by Konmin on 2017/1/17.
 */

public class CitySelectActivity extends BaseActivity {


    @BindView(R.id.lv_city)
    ListView mLvCity;

    @BindView(R.id.sb)
    SideBarView mSideBarView;

    @BindView(R.id.tv_letter)
    TextView mTvLetter;

    private List<CityList.DataBean> mCities;

    private TextView mTvCity;
    private LoadingDialog mLoadingDialog;

    private CityAdapter mCityAdapter;


    @Override
    public void init() {
        showBack();
        setMyTitle("选择城市");
        mLoadingDialog = new LoadingDialog(this);
        mCityAdapter = new CityAdapter();
        initHeaderView();
        locationCity();
        mLvCity.setAdapter(mCityAdapter);
        loadCityList();
        mLvCity.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                int index = position - 1;
                if (index >= 0) {
                    EventBus.getDefault().post(mCities.get(position - 1));
                    CitySelectActivity.this.finish();
                }

            }
        });


        mSideBarView.setTextView(mTvLetter);
        mSideBarView.setOnTouchingLetterChangedListener(new SideBarView.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                int position = getPositionForSection(s.charAt(0));

                if (position != -1) {
                    mLvCity.setSelection(position + 1);
                }

            }
        });
    }

    /**
     * 找到该字母出现的第一个位置
     *
     * @param section section
     * @return section
     */
    public int getPositionForSection(int section) {

        for (int i = 0; i < mCityAdapter.getCount(); i++) {
            String sortStr = mCities.get(i).getSortLetter();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public int createSuccessView() {
        return R.layout.activity_city_select;
    }


    /**
     * 获取定位城市
     */
    private void locationCity() {

        LocationService.getInstance().start(new LocationService.LocationCityListener() {
            @Override
            public void onCityResult(String cityName, String cityCode) {
                mTvCity.setText(cityName);
                Logger.e(cityCode);
            }
        });

        mTvCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CityList.DataBean dataBean = findCity(mTvCity.getText().toString());
                if (dataBean == null) {
                    showTest("没有找到与定位城市的匹配项，请选择一个城市！");
                } else {
                    EventBus.getDefault().post(dataBean);
                    CitySelectActivity.this.finish();
                }
            }
        });
    }


    /**
     * 查找就是为了能找到对应的cityCode,获取城市任务是通过cityCode来的
     *
     * @param cityName cityName
     * @return cityName
     */
    private CityList.DataBean findCity(String cityName) {

        for (CityList.DataBean dataBean : mCities) {

            if (dataBean.getCityName().equals(cityName)) {
                return dataBean;
            }
        }
        return null;

    }

    /**
     * 初始化头部的定位城市的View
     */
    private void initHeaderView() {
        View headerView = LayoutInflater.from(this).inflate(R.layout.item_city_header, mLvCity, false);
        mTvCity = (TextView) headerView.findViewById(R.id.tv_city_name);
        mLvCity.addHeaderView(headerView);
    }


    /**
     * 城市列表的适配器
     */
    private class CityAdapter extends BaseAdapter {


        @Override
        public int getCount() {

            if (mCities == null) {
                return 0;
            }
            return mCities.size();
        }

        @Override
        public Object getItem(int position) {
            return mCities.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(CitySelectActivity.this).inflate(R.layout.item_city, parent, false);
                holder = new ViewHolder();
                holder.tvCityName = (TextView) convertView.findViewById(R.id.tv_city_name);
                holder.tvLetter = (TextView) convertView.findViewById(R.id.tv_litter);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            //如果上一个字母与当前字母不同，就认为是第一个字母
            CityList.DataBean dataBean = mCities.get(position);
            if (position < 1) {
                holder.tvLetter.setVisibility(View.VISIBLE);
                holder.tvLetter.setText(dataBean.getSortLetter());
            } else {
                char preItemFirstLetter = mCities.get(position - 1).getSortLetter().charAt(0);
                char curItemFirstLetter = dataBean.getSortLetter().charAt(0);
                if (preItemFirstLetter == curItemFirstLetter) {
                    holder.tvLetter.setVisibility(View.GONE);
                } else {
                    holder.tvLetter.setVisibility(View.VISIBLE);
                    holder.tvLetter.setText(dataBean.getSortLetter());
                }
            }
            holder.tvCityName.setText(dataBean.getCityName());
            return convertView;
        }


        private class ViewHolder {
            TextView tvLetter;
            TextView tvCityName;
        }

    }


    /**
     * 网络加载城市列表
     */
    private void loadCityList() {
        mLoadingDialog.show();
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

                if (cityList.getRetCode() == 0 && cityList.getData() != null) {
                    sortCities(cityList.getData());
                } else {
                    showTest(cityList.getMsg());
                }

            }
        });
        addSubscription(sbMyAccount);
    }


    /**
     * 索引并排列城市
     *
     * @param cityLists
     */
    private void sortCities(final List<CityList.DataBean> cityLists) {

        Observable<List<CityList.DataBean>> observable = Observable.create(new Observable.OnSubscribe<List<CityList.DataBean>>() {
            @Override
            public void call(Subscriber<? super List<CityList.DataBean>> subscriber) {
                if (cityLists == null || cityLists.isEmpty()) {
                    subscriber.onError(new IllegalArgumentException("cityLists should not null or empty"));
                    return;
                }
                for (CityList.DataBean dataBean : cityLists) {
                    String pinyin = CharacterParser.getInstance().getSelling(dataBean.getCityName());
                    String sortString = pinyin.substring(0, 1).toUpperCase();
                    // 正则表达式，判断首字母是否是英文字母
                    if (sortString.matches("[A-Z]")) {
                        dataBean.setSortLetter(sortString.toUpperCase());
                    } else {
                        dataBean.setSortLetter("#");
                    }
                }
                Collections.sort(cityLists, new PinyinComparator());
                subscriber.onNext(cityLists);
                subscriber.onCompleted();
            }
        });

        Subscription subscription = wrapObserverWithHttp(observable).subscribe(new Subscriber<List<CityList.DataBean>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                mLoadingDialog.dismiss();
            }

            @Override
            public void onNext(List<CityList.DataBean> o) {
                mLoadingDialog.dismiss();
                mCities = o;
                mCityAdapter.notifyDataSetChanged();
            }
        });

        addSubscription(subscription);

    }


}
