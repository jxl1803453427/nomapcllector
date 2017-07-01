package com.example.signalcollection.activity;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.example.signalcollection.R;
import com.example.signalcollection.fragment.BankCardFragment;
import com.example.signalcollection.fragment.IdentityCardPictureFragment;

import butterknife.BindView;

/**
 * 修改信息的fragment
 * Created by Konmin on 2017/3/2.
 */

public class ModifyInfoActivity extends BaseActivity {


    private FragmentManager mFragmentManager;


    @Override
    public void init() {
        showBack();
        int flag = getIntent().getIntExtra("flag", 1);
        mFragmentManager = getSupportFragmentManager();
        if (flag == 1) {
            mFragmentManager.beginTransaction().replace(R.id.fl_content, new IdentityCardPictureFragment()).commit();
        } else if (flag == 2) {
            mFragmentManager.beginTransaction().replace(R.id.fl_content, new BankCardFragment()).commit();
        }
    }

    @Override
    public int createSuccessView() {
        return R.layout.activity_credentials_picture;
    }


}
