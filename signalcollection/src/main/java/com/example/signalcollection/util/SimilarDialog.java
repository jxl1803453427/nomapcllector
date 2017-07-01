package com.example.signalcollection.util;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.signalcollection.R;
import com.example.signalcollection.recyclerview.CommonAdapter;
import com.example.signalcollection.recyclerview.ViewHolder;

import java.util.List;

/**
 * 显示类似的dialog
 * Created by Konmin on 2017/5/25.
 */

public abstract class SimilarDialog<T> extends Dialog implements View.OnClickListener {


    private RecyclerView rvContent;
    private TextView tvNo;
    private TextView tvYes;

    private CommonAdapter<T> mListAdapter;
    private OnDialogBtnClickListener mOnDialogBtnClickListener;

    private void assignViews() {
        rvContent = (RecyclerView) findViewById(R.id.rv_content);
        tvNo = (TextView) findViewById(R.id.tv_no);
        tvYes = (TextView) findViewById(R.id.tv_yes);
    }


    public SimilarDialog(@NonNull Context context) {
        super(context, R.style.Style_Dialog);
        setContentView(R.layout.dialog_show_similar);
        assignViews();
        mListAdapter = new CommonAdapter<T>(context, R.layout.item_similar_area_task) {
            @Override
            public void convert(ViewHolder holder, T t) {
                onPosition(holder, t);
            }
        };

        rvContent.setAdapter(mListAdapter);

        setCancelable(false);
        tvNo.setOnClickListener(this);
        tvYes.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.tv_no:
                dismiss();
                if (checkListener()) {
                    mOnDialogBtnClickListener.onNoClick();
                }
                break;
            case R.id.tv_yes:
                if (checkListener()) {
                    mOnDialogBtnClickListener.onYesClick();
                }
                dismiss();
                break;
        }
    }


    private boolean checkListener() {
        return mOnDialogBtnClickListener != null;
    }


    public abstract void onPosition(ViewHolder holder, T t);

    public void show(List<T> data, OnDialogBtnClickListener listener) {

        mOnDialogBtnClickListener = listener;
        mListAdapter.setData(data);
        show();
    }



    public interface OnDialogBtnClickListener {
        void onYesClick();

        void onNoClick();
    }


}
