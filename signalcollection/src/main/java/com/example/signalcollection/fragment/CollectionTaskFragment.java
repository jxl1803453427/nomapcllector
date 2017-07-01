package com.example.signalcollection.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.example.signalcollection.activity.MyTaskActivity;
import com.example.signalcollection.R;
import com.example.signalcollection.activity.TasklistActivity;

import com.example.signalcollection.activity.BaseActivity;

import butterknife.OnClick;

/**
 * 采集任务的Fragment
 * Created by Konmin on 2016/7/27.
 */
public class CollectionTaskFragment extends BaseFragment {


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @OnClick(R.id.item_collection_task)
    void collectionTaskOnClick() {
        ((BaseActivity) getActivity()).baseStartActivity(TasklistActivity.class);
    }


    @OnClick(R.id.item_my_task)
    void myTaskClick() {
        ((BaseActivity) getActivity()).baseStartActivity(MyTaskActivity.class);
    }

    @Override
    public int createSuccessView() {
        return R.layout.fragment_collection_task;
    }
}
