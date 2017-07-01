package com.example.signalcollection.view;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.signalcollection.R;

import java.util.List;

/**
 * Created by Konmin on 2017/2/16.
 */

public abstract class SelectDialog<T> extends Dialog {


    private TextView tvTitle;
    private ListView lvData;
    private List<T> data;
    private BaseAdapter mAdapter;
    private Context mContext;

    private OnItemSelectedListener mOnItemSelectedListener;

    public SelectDialog(Context context, String title) {
        super(context, R.style.Style_Dialog);
        setContentView(R.layout.dialog_list_select);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        mContext = context;
        if (!TextUtils.isEmpty(title)) {
            tvTitle.setText(title);
        }
        lvData = (ListView) findViewById(R.id.list);
        init();
    }


    private void init() {

        mAdapter = new BaseAdapter() {
            @Override
            public int getCount() {

                if (data == null) return 0;
                return data.size();
            }

            @Override
            public Object getItem(int position) {
                return data.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.item_text, parent, false);
                }
                TextView textView = (TextView) convertView;
                showData(textView, data.get(position));
                return convertView;
            }
        };

        lvData.setAdapter(mAdapter);
        lvData.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mOnItemSelectedListener != null) {
                    mOnItemSelectedListener.onItemSelected(parent.getAdapter().getItem(position));
                }
                dismiss();
            }
        });
    }


    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.mOnItemSelectedListener = listener;
    }


    public void setData(List<T> list) {
        this.data = list;
        mAdapter.notifyDataSetChanged();

    }

    public void setDataAndShow(List<T> list) {
        this.data = list;
        mAdapter.notifyDataSetChanged();
        show();
    }


    public void setDataAndShow(List<T> list, @NonNull String title, @NonNull OnItemSelectedListener listener) {
        this.mOnItemSelectedListener = listener;
        this.data = list;
        setTitle(title);
        mAdapter.notifyDataSetChanged();
        show();
    }

    public void setTitle(@NonNull String title) {
        tvTitle.setText(title);
    }


    public abstract void showData(TextView textView, T t);


    public interface OnItemSelectedListener {

        void onItemSelected(Object t);

    }

}
