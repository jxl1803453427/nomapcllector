package com.example.signalcollection.activity;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.example.signalcollection.R;
import com.example.signalcollection.bean.PersonInfoResult;
import com.example.signalcollection.fragment.BankCardFragment;
import com.example.signalcollection.fragment.IdentityCardPictureFragment;
import com.example.signalcollection.fragment.RegisterBaseInfoFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * 注册v2版本
 * Created by Konmin on 2017/3/3.
 */

public class RegisterActivityV2 extends BaseActivity {


    private FragmentManager mFragmentManager;

    private RegisterBaseInfoFragment mRegisterBaseInfoFragment;
    private IdentityCardPictureFragment mIdentityCardPictureFragment;
    private BankCardFragment mBankCardFragment;

    private int mStepIndex = 1;

    private PersonInfoResult.PersonInfo mPersonInfo;

    @Override
    public void init() {
        mFragmentManager = getSupportFragmentManager();
        showFragment(1);
        EventBus.getDefault().register(this);
    }


    /**
     * 根据步骤显示Activity
     *
     * @param step 步骤编号
     */
    private void showFragment(int step) {
        hideAllFragment();
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        switch (step) {
            case 1:
                if (mRegisterBaseInfoFragment == null) {
                    mRegisterBaseInfoFragment = new RegisterBaseInfoFragment();
                    transaction.add(R.id.fl_content, mRegisterBaseInfoFragment);
                } else {
                    transaction.show(mRegisterBaseInfoFragment);
                }
                break;
            case 2:
                if (mIdentityCardPictureFragment == null) {
                    mIdentityCardPictureFragment = new IdentityCardPictureFragment();
                    transaction.add(R.id.fl_content, mIdentityCardPictureFragment);
                } else {
                    transaction.show(mIdentityCardPictureFragment);
                }
                break;
            case 3:
                if (mBankCardFragment == null) {
                    mBankCardFragment = new BankCardFragment();
                    transaction.add(R.id.fl_content, mBankCardFragment);
                } else {
                    transaction.show(mBankCardFragment);
                }
                break;
        }
        transaction.commit();
    }


    @Subscribe
    public void obtianPersionInfo(PersonInfoResult.PersonInfo info) {
        mPersonInfo = info;
    }


    public PersonInfoResult.PersonInfo getPersonInfo() {
        return mPersonInfo;
    }

    /**
     * 显示下一个Fragment
     */
    public void showNextFragment() {
        mStepIndex++;
        if (mStepIndex <= 3) {
            showFragment(mStepIndex);
        }
    }


    /**
     * 隐藏所有显示的fragment
     */
    private void hideAllFragment() {

        if (mRegisterBaseInfoFragment != null && mRegisterBaseInfoFragment.isVisible()) {
            mFragmentManager.beginTransaction().hide(mRegisterBaseInfoFragment).commit();
        }

        if (mIdentityCardPictureFragment != null && mIdentityCardPictureFragment.isVisible()) {
            mFragmentManager.beginTransaction().hide(mIdentityCardPictureFragment).commit();
        }
        if (mBankCardFragment != null && mBankCardFragment.isVisible()) {
            mFragmentManager.beginTransaction().hide(mBankCardFragment).commit();
        }

    }


    @Override
    public int createSuccessView() {
        return R.layout.activity_register_v2;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
