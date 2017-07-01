package com.example.signalcollection.fragment;

import android.content.Intent;

import com.example.signalcollection.R;
import com.example.signalcollection.activity.HelpInfoActivity;

import butterknife.OnClick;

/**
 * 我的Fragment
 * Created by Konmin on 2016/7/28.
 */
public class MoreFragment extends BaseFragment {


    @Override
    public int createSuccessView() {
        return R.layout.activity_strategy;
    }


    @OnClick(R.id.item_basic_opt)
    void basicOptClick() {

        Intent intent = new Intent(getActivity(), HelpInfoActivity.class);
        intent.putExtra("info", "基本操作");
        startActivity(intent);
    }


    @OnClick(R.id.item_collection_standard)
    void collectionStandardClick() {

        Intent intent = new Intent(getActivity(), HelpInfoActivity.class);
        intent.putExtra("info", "采集任务规范");
        startActivity(intent);
    }

    @OnClick(R.id.item_special_case)
    void specialCaseClick() {

        Intent intent = new Intent(getActivity(), HelpInfoActivity.class);
        intent.putExtra("info", "特殊案例");
        startActivity(intent);

    }


}
