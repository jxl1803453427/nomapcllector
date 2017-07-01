package com.example.signalcollection.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.signalcollection.Constans;
import com.example.signalcollection.R;
import com.example.signalcollection.bean.DefaultResult;
import com.example.signalcollection.bean.MagneticBean;
import com.example.signalcollection.bean.MessageBean;
import com.example.signalcollection.bean.PersonInfoResult;
import com.example.signalcollection.bean.UserBehavior;
import com.example.signalcollection.fragment.CollectionTaskFragment;
import com.example.signalcollection.fragment.MessageFragment;
import com.example.signalcollection.fragment.MoreFragment;
import com.example.signalcollection.network.WorkService;
import com.example.signalcollection.util.SPUtils;
import com.example.signalcollection.util.UIUtils;
import com.orhanobut.logger.Logger;

import org.litepal.crud.DataSupport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import rx.Subscriber;
import rx.Subscription;

public class MainActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {


    private CollectionTaskFragment mCollectionTaskFragment;
    private MoreFragment mMoreFragment;
    private FragmentManager mFragmentManager;


    @BindView(R.id.rg)
    RadioGroup mRadioGroup;

    @BindView(R.id.rb1)
    RadioButton radioButton1;

    @BindView(R.id.rb2)
    RadioButton radioButton2;

    @BindView(R.id.rb3)
    RadioButton radioButton3;
    private long mExitTime;

    @BindView(R.id.tv_msg_count)
    TextView mTvMsgCount;

    private MsgReceiver mMsgReceiver;

    private MessageFragment mMessageFragment;


    @Override
    public void init() {
        //进来初始化的名字叫采集任务
        SPUtils.put("un_sign", false);
        setMyTitle("采集任务");
        findUnReadMessage();
        mFragmentManager = getSupportFragmentManager();
        initFragment();
        mRadioGroup.setOnCheckedChangeListener(this);
        loadPersonalInfo();
        mMsgReceiver = new MsgReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constans.MASSAGE_IN);
        registerReceiver(mMsgReceiver, filter);
    }


    private void initFragment() {
        mCollectionTaskFragment = new CollectionTaskFragment();
        mFragmentManager.beginTransaction().replace(R.id.fmContent, mCollectionTaskFragment).commit();
        radioButton1.setTextColor(getResources().getColor(R.color.themeBlue));
    }


    @Override
    public int createSuccessView() {
        return R.layout.activity_main;
    }


    @Override
    public void onBackPressed() {
        if ((System.currentTimeMillis() - mExitTime) > 1000) {
            showTest("再按一次退出程序");
            mExitTime = System.currentTimeMillis();
        } else {
            super.onBackPressed();
        }
        //super.onBackPressed();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        if (itemId == R.id.un_sign) {
            //退出登录
            SPUtils.put("un_sign", true);
            baseStartActivity(LoginActivity.class);
            finish();
        } else if (itemId == R.id.setting) {
            baseStartActivity(SettingActivity.class);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        hideAllFragment();
        Fragment fragment;

        switch (checkedId) {
            case R.id.rb1:
                setMyTitle("采集任务");
                if (mCollectionTaskFragment == null) {
                    mCollectionTaskFragment = new CollectionTaskFragment();
                }
                fragment = mCollectionTaskFragment;
                radioButton1.setTextColor(getResources().getColor(R.color.themeBlue));
                break;
            case R.id.rb2:
                if (mMessageFragment == null) {
                    mMessageFragment = new MessageFragment();
                }
                setMyTitle("消息");
                fragment = mMessageFragment;
                radioButton2.setTextColor(getResources().getColor(R.color.themeBlue));
                break;
            case R.id.rb3:
                if (mMoreFragment == null) {
                    mMoreFragment = new MoreFragment();
                }
                fragment = mMoreFragment;
                setMyTitle("攻略");
                radioButton3.setTextColor(getResources().getColor(R.color.themeBlue));
                break;
            default:
                fragment = mCollectionTaskFragment;
                break;
        }

        mFragmentManager.beginTransaction().replace(R.id.fmContent, fragment).commit();
    }


    private void hideAllFragment() {

        radioButton1.setTextColor(getResources().getColor(R.color.grayText));
        radioButton2.setTextColor(getResources().getColor(R.color.grayText));
        radioButton3.setTextColor(getResources().getColor(R.color.grayText));
    }


    @Override
    protected void onDestroy() {
        shutDown();
        unregisterReceiver(mMsgReceiver);
        super.onDestroy();
    }


    private void shutDown() {
        UserBehavior userBehavior = new UserBehavior();
        userBehavior.setUsername(SPUtils.getUserName());
        TelephonyManager manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        userBehavior.setImei(manager.getDeviceId());
        userBehavior.setAction(2);
        Subscription subscription = wrapObserverWithHttp(WorkService.getWorkService().reportUserBehavior(userBehavior)).subscribe(new Subscriber<DefaultResult>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(DefaultResult o) {


            }
        });
        addSubscription(subscription);
    }


    private class MsgReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constans.MASSAGE_IN)) {

                if (mTvMsgCount.getVisibility() == View.GONE) {
                    mTvMsgCount.setVisibility(View.VISIBLE);
                }
                int count = Integer.valueOf(mTvMsgCount.getText().toString()) + 1;
                mTvMsgCount.setText(String.valueOf(count));
                if (mMessageFragment != null && mMessageFragment.getUserVisibleHint()) {
                    mMessageFragment.addMessage((MessageBean) intent.getSerializableExtra("message"));
                }
            }
        }
    }


    public void findUnReadMessage() {

        int unReadMsgCount = DataSupport.where("status = 0").count(MessageBean.class);
        if (unReadMsgCount > 0) {
            mTvMsgCount.setVisibility(View.VISIBLE);
            mTvMsgCount.setText(String.valueOf(unReadMsgCount));
        }
    }


    private void loadPersonalInfo() {


        Map<String, String> map = new HashMap<>();
        map.put("phoneNumber", SPUtils.getUserName());
        wrapObserverWithHttp(WorkService.getWorkService().getPersonMsg(map)).subscribe(new Subscriber<PersonInfoResult>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();

            }

            @Override
            public void onNext(final PersonInfoResult personInfoResult) {

                if (personInfoResult.getRetCode() == 0) {
                    String bankCardNumber = personInfoResult.getData().getBankCardNumber();
                    if (TextUtils.isEmpty(bankCardNumber)) {
                        //getAlertDialog(Context activity, String title, CharSequence content, String right, final View.OnClickListener leftOnClick)
                        UIUtils.getAlertDialog((Context) MainActivity.this, "温馨提示", "为了提高劳务费付款效率，APP需要你填写身份银行信息，用于快速劳务费用结算，是否要添加劳务信息？", "现在添加", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                //Intent intent = new Intent(MainActivity.this, SettingActivityV2.class);
                                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                                intent.putExtra("modify", true);
                                intent.putExtra("personId", personInfoResult.getData().getId());
                                startActivity(intent);
                            }
                        }).show();
                    }
                }
            }
        });

    }


    /**
     * 消息减一执行
     */
    public void setMsgMinusOne() {

        int count = Integer.valueOf(mTvMsgCount.getText().toString()) - 1;
        if (count > 0) {
            mTvMsgCount.setText(String.valueOf(count));
        } else {
            mTvMsgCount.setText("0");
            mTvMsgCount.setVisibility(View.GONE);
        }

    }

}
