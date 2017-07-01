package com.example.signalcollection.view;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.signalcollection.R;
import com.example.signalcollection.bean.SearchResult;
import com.example.signalcollection.network.WorkService;
import com.example.signalcollection.recyclerview.CommonAdapter;
import com.example.signalcollection.recyclerview.ViewHolder;
import com.example.signalcollection.util.UIUtils;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * 搜索多选对话框
 * Created by Konmin on 2017/4/22.
 */

public class SearchMultiSelectDialog extends Dialog implements View.OnClickListener {


    private TextView tvCancel;
    private TextView tvConfirm;
    private TextView tvTitle;
    private RecyclerView rvHadSelected;
    private RecyclerView rvSelectList;
    private EditText etKeyword;

    private CommonAdapter<SearchResult.Data> mSelectListAdapter;
    private CommonAdapter<SearchResult.Data> mHadSelectedListAdapter;
    private String mUrl;
    private CompositeSubscription mCompositeSubscription;
    private OnConfirmListener mOnConfirmListener;

    private List<SearchResult.Data> mHadSelectedDataList;
    private Dialog mDialog;
    //private List<SearchResult.Data> mSelectDataList = new ArrayList<>();


    public SearchMultiSelectDialog(@NonNull Context context) {
        super(context, R.style.Style_Dialog);
        setContentView(R.layout.dialog_search_multiselect);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        tvCancel = (TextView) findViewById(R.id.tv_cancel);
        tvConfirm = (TextView) findViewById(R.id.tv_confirm);
        rvHadSelected = (RecyclerView) findViewById(R.id.rv_selected);
        rvSelectList = (RecyclerView) findViewById(R.id.rv_select);
        etKeyword = (EditText) findViewById(R.id.et_keyword);
        setCancelable(false);
        mSelectListAdapter = new CommonAdapter<SearchResult.Data>(context, R.layout.item_select_singal) {
            @Override
            public void convert(final ViewHolder holder, final SearchResult.Data data) {
                holder.setText(R.id.tv_name, data.getShowName());
                holder.setOnClickListener(R.id.iv_action, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //添加一个到已选择，选择前记得先对比
                        mSelectListAdapter.removeDate(data);
                        addSelectedItem(data);
                    }
                });
            }
        };
        rvSelectList.setAdapter(mSelectListAdapter);
        mHadSelectedListAdapter = new CommonAdapter<SearchResult.Data>(context, R.layout.item_select_singal) {
            @Override
            public void convert(final ViewHolder holder, SearchResult.Data data) {
                holder.setText(R.id.tv_name, data.getShowName());
                holder.setImageResource(R.id.iv_action, R.mipmap.ic_del);
                holder.setOnClickListener(R.id.iv_action, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //删除自己
                        mHadSelectedDataList.remove(holder.getMyPosition());
                        mHadSelectedListAdapter.notifyDataSetChanged();
                    }
                });
            }
        };
        rvHadSelected.setAdapter(mHadSelectedListAdapter);
        etKeyword.addTextChangedListener(new TextWatcher() {
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

        tvCancel.setOnClickListener(this);
        tvConfirm.setOnClickListener(this);
    }

    private void addSelectedItem(SearchResult.Data selectedData) {

        if (mHadSelectedDataList.isEmpty()) {
            mHadSelectedDataList.add(selectedData);
            mHadSelectedListAdapter.notifyDataSetChanged();
        } else {

            boolean isAdd = true;
            for (SearchResult.Data data : mHadSelectedDataList) {
                if (data.getShowName().equals(selectedData.getShowName())) {
                    isAdd = false;
                    break;
                }
            }
            if (isAdd) {
                mHadSelectedDataList.add(selectedData);
                mHadSelectedListAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), "您已经选择了" + selectedData.getShowName(), Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * 显示这个对话框
     *
     * @param title    标题
     * @param url      url
     * @param listener 监听器
     */
    public void show(String title, String url, OnConfirmListener listener) {
        mUrl = url;
        etKeyword.setText("");
        mHadSelectedDataList = new ArrayList<>();
        mHadSelectedListAdapter.setData(mHadSelectedDataList);
        mOnConfirmListener = listener;
        if (!TextUtils.isEmpty(title)) {
            tvTitle.setText(title);
        } else {
            tvTitle.setText("品牌选择");
        }
        show();
    }


    /**
     * 显示对话框
     *
     * @param url      url
     * @param listener listener 回调
     */
    public void show(String url, OnConfirmListener listener) {
        show(null, url, listener);
    }


    /**
     * 搜索品牌
     *
     * @param key 关键字
     */
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
                            mSelectListAdapter.setData(data);
                        }
                    }
                }
            });


            if (mCompositeSubscription == null) {
                mCompositeSubscription = new CompositeSubscription();
            }

            mCompositeSubscription.add(s);

        } else {
            Logger.e("no search");
            mSelectListAdapter.setData(null);
        }


    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.tv_cancel:
                dismiss();
                break;
            case R.id.tv_confirm:
                confirm();
                break;
        }

    }


    private void confirm() {

        //todo 最后的结果是个什么样的？
        if (mHadSelectedDataList.isEmpty()) {
            mDialog = UIUtils.getAlertDialog(getContext(), "温馨提示", "你没有选择到任何品牌", "放弃选择", "重新选择", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDialog.dismiss();
                    dismiss();
                }
            }, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDialog.dismiss();
                }
            });
            mDialog.show();
        } else {
            if (mOnConfirmListener != null) {

                StringBuilder resultStringBuilder = new StringBuilder();
                for (SearchResult.Data data : mHadSelectedDataList) {
                    resultStringBuilder.append(data.getShowName()).append(";");
                }
                mOnConfirmListener.onConfirm(resultStringBuilder.toString());
            }
            dismiss();
        }


    }


    public interface OnConfirmListener {

        void onConfirm(String results);
    }

}
