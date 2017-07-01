package com.example.signalcollection.activity;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.signalcollection.Constans;
import com.example.signalcollection.R;
import com.example.signalcollection.bean.SearchResult;
import com.example.signalcollection.network.WorkService;
import com.example.signalcollection.recyclerview.CommonAdapter;
import com.example.signalcollection.recyclerview.OnItemClickListener;
import com.example.signalcollection.recyclerview.ViewHolder;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import rx.Observer;
import rx.Subscription;

/**
 * 搜索品牌的Activity
 * Created by Konmin on 2017/3/9.
 */

public class SearchBandActivity extends BaseActivity {


    @BindView(R.id.tv_key)
    TextView tvKey;//名字
    @BindView(R.id.et_value)
    EditText etValue;//值

    @BindView(R.id.rv_result)
    RecyclerView rvSearch;//结果展示列表

    private CommonAdapter<SearchResult.Data> mAdapter;


    private String mUrl;


    @Override
    public void init() {

        Intent intent = getIntent();
        String key = intent.getStringExtra(Constans.KEY);
        mUrl = intent.getStringExtra(Constans.URL);
        Logger.e(mUrl);
        tvKey.setText(key);
        etValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                search(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mAdapter = new CommonAdapter<SearchResult.Data>(this, R.layout.item_spinner) {
            @Override
            public void convert(ViewHolder holder, SearchResult.Data data) {
                holder.setText(R.id.text1, data.getShowName());
            }
        };

        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(ViewGroup parent, View view, Object o, int position, ViewHolder viewHolder) {
                EventBus.getDefault().post(o);
                SearchBandActivity.this.finish();
            }

            @Override
            public boolean onItemLongClick(ViewGroup parent, View view, Object o, int position, ViewHolder viewHolder) {
                return false;
            }
        });
        rvSearch.setAdapter(mAdapter);

    }


    private void search(String key) {

        if (!TextUtils.isEmpty(mUrl) && !TextUtils.isEmpty(key)) {
            Map<String, String> map = new HashMap<>();
            map.put("keyword", key);
            Logger.e(mUrl);
            Subscription s = wrapObserverWithHttp(WorkService.getWorkService().getSearch(mUrl, map)).subscribe(new Observer<SearchResult>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                    showTest(mNetWorkError);
                }

                @Override
                public void onNext(SearchResult searchResult) {

                    Logger.i("SearchResult searchResult");
                    if (searchResult.getRetCode() == 0) {
                        List<SearchResult.Data> data = searchResult.getData();
                        if (data != null) {
                            mAdapter.setData(data);
                        }
                    } else {
                        showTest(searchResult.getMsg());
                    }
                }
            });
            addSubscription(s);
        } else {
            mAdapter.setData(null);
        }


    }


    @Override
    public int createSuccessView() {
        return R.layout.dialog_search_band;
    }
}
