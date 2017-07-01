package com.example.signalcollection.activity;

import com.example.signalcollection.R;

import butterknife.OnClick;

/**
 * 攻略的Activity
 * Created by Konmin on 2016/7/28.
 */
public class StrategyActivity extends BaseActivity {


    @Override
    public void init() {
        setMyTitle("攻略");
        showBack();
    }

    @Override
    public int createSuccessView() {
        return R.layout.activity_strategy;
    }


    @OnClick(R.id.item_basic_opt)
    void basicOptClick() {


        showTest("基本操作功能还没上线");
    }


    @OnClick(R.id.item_collection_standard)
    void collectionStandardClick() {

        showTest("采集任务规范还没上线");
    }

    @OnClick(R.id.item_special_case)
    void specialCaseClick() {

        showTest("特殊案例功能还没上线");

    }

}
