package com.example.signalcollection.recyclerview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.signalcollection.R;

import java.util.List;

/**
 * SpinnerAdapter
 * Created by Konmin on 2016/8/25.
 */
public abstract class SpinnerAdapter<T> extends BaseAdapter {

    private List<T> mList;

    private Context mContext;

    public SpinnerAdapter(Context context) {

        mContext = context;
    }

    public SpinnerAdapter(Context context, List<T> list) {
        mList = list;
        mContext = context;
    }

    @Override
    public int getCount() {

        if (mList == null) {
            return 0;
        }
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_spinner, viewGroup, false);
            //view = LayoutInflater.from(mContext).inflate(android.R.layout.simple_spinner_dropdown_item, viewGroup, false);
        }
        T t = mList.get(i);
        TextView tvContent = (TextView) view.findViewById(R.id.text1);
        setText(tvContent, t);
        return view;
    }

    public abstract void setText(TextView textView, T t);


    public void setListData(List<T> data) {
        mList = data;
        notifyDataSetChanged();
    }

}
