package com.example.signalcollection.activity;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.example.signalcollection.R;
import com.example.signalcollection.view.MyTabLayout;
import com.example.signalcollection.fragment.AlreadyAuditFragment;
import com.example.signalcollection.fragment.OverCollectFragment;
import com.example.signalcollection.fragment.WaitAuditFragment;

import butterknife.BindView;

/**
 * Created by Konmin on 2016/7/27.
 */
public class MyTaskActivity extends BaseActivity {


    private String[] tabStrs = new String[]{"提交状态", "审核状态", "结算状态"};

    private WaitAuditFragment mWaitAuditFragment;
    private AlreadyAuditFragment mAlreadyAuditFragment;

    private OverCollectFragment mOverCollectFragment;


    @BindView(R.id.myTabLayout)
    MyTabLayout mMyTabLayout;

    @BindView(R.id.taskViewPager)
    ViewPager mViewPager;


    @Override
    public void init() {

        setMyTitle("我的任务");
        showBack();
        mMyTabLayout.setTabMode(TabLayout.MODE_FIXED);
        initFragments();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {

                switch (position) {
                    case 0:
                        return mOverCollectFragment;
                    case 1:
                        return mWaitAuditFragment;
                    case 2:
                        return mAlreadyAuditFragment;
                }
                return null;
            }

            @Override
            public int getCount() {
                return tabStrs.length;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return tabStrs[position];
            }
        });
        mMyTabLayout.setupWithViewPager(mViewPager);
    }

    private void initFragments() {
        mWaitAuditFragment = new WaitAuditFragment();//审核状态
        mAlreadyAuditFragment = new AlreadyAuditFragment();//结算状态
        mOverCollectFragment = new OverCollectFragment();//提交状态
    }

    @Override
    public int createSuccessView() {
        return R.layout.activity_my_task;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_my_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.upload:
                baseStartActivity(UploadFailurePhotoActivity.class);
                break;
            case R.id.submit_photo:
                baseStartActivity(UploadTaskPhotoActivity.class);
                break;
            case R.id.update_status:
                if (mOverCollectFragment != null && mOverCollectFragment.isVisible()) {
                    mOverCollectFragment.getTaskStatus();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }


}
