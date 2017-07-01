package com.example.signalcollection.view;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.signalcollection.R;
import com.example.signalcollection.bean.SearchResult;
import com.example.signalcollection.network.WorkService;
import com.example.signalcollection.recyclerview.CommonAdapter;
import com.example.signalcollection.recyclerview.OnItemClickListener;
import com.example.signalcollection.recyclerview.ViewHolder;
import com.orhanobut.logger.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * 搜索品牌的dialog
 * Created by Konmin on 2017/3/14.
 */

public class SearchBandDialog extends Dialog {


    private final CommonAdapter<SearchResult.Data> mAdapter;
    private String mUrl;

    private TextView tvKey;
    private TextView tvOther;
    private EditText etValue;//值

    private RecyclerView rvResult;

    private OnItemSelectListener listener;

    private CompositeSubscription mCompositeSubscription;

    public SearchBandDialog(@NonNull Context context) {
        super(context, R.style.Style_Dialog);
        setContentView(R.layout.dialog_search_band);
        tvKey = (TextView) findViewById(R.id.tv_key);
        etValue = (EditText) findViewById(R.id.et_value);
        rvResult = (RecyclerView) findViewById(R.id.rv_result);
        tvOther = (TextView) findViewById(R.id.tv_other);
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


        mAdapter = new CommonAdapter<SearchResult.Data>(getContext(), R.layout.item_spinner) {
            @Override
            public void convert(ViewHolder holder, SearchResult.Data data) {
                holder.setText(R.id.text1, data.getShowName());
            }
        };

        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(ViewGroup parent, View view, Object o, int position, ViewHolder viewHolder) {

                listener.onItemSelected((SearchResult.Data) o);
                etValue.setText("");
                mAdapter.setData(null);
                dismiss();
            }

            @Override
            public boolean onItemLongClick(ViewGroup parent, View view, Object o, int position, ViewHolder viewHolder) {
                return false;
            }
        });
        rvResult.setAdapter(mAdapter);
        tvOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onOtherClick();
                etValue.setText("");
                mAdapter.setData(null);
                dismiss();
            }
        });
    }


    private void search(String key) {

        if (!TextUtils.isEmpty(mUrl) && !TextUtils.isEmpty(key)) {

            Map<String, String> map = new HashMap<>();
            map.put("keyword", key);
            Subscription s = WorkService.getWorkService().getSearch(mUrl, map).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<SearchResult>() {
                @Override
                public void onCompleted() {
                    Logger.i("SearchResult onCompleted");
                }

                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                    Logger.e(e.getMessage());
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
                        listener.onErrMsg(searchResult.getMsg());
                    }
                }
            });


            if (mCompositeSubscription == null) {
                mCompositeSubscription = new CompositeSubscription();
            }

            mCompositeSubscription.add(s);

        } else {
            Logger.e("no search");
            mAdapter.setData(null);
        }


    }


    public interface OnItemSelectListener {
        void onItemSelected(SearchResult.Data data);

        void onOtherClick();

        void onErrMsg(String msg);
    }


    public void show(String key, String url, OnItemSelectListener listener) {
        tvKey.setText(key);
        mUrl = url;
        this.listener = listener;
        show();
    }


    @Override
    public void dismiss() {

        listener = null;
        super.dismiss();

    }


}
