package com.example.signalcollection.activity;

import android.app.Dialog;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.example.signalcollection.R;
import com.example.signalcollection.bean.DefaultResult;
import com.example.signalcollection.bean.PersonInfoResult;
import com.example.signalcollection.network.WorkService;
import com.example.signalcollection.util.SPUtils;
import com.example.signalcollection.util.UIUtils;
import com.example.signalcollection.view.LoadingDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Subscriber;
import rx.Subscription;

/**
 * 个人的设置界面
 * Created by Konmin on 2017/2/13.
 */

public class SettingActivityV2 extends BaseActivity {


    @BindView(R.id.tv_real_name)
    TextView tvRealName;//真实姓名

    @BindView(R.id.tv_id_card_number)
    TextView tvIDCardNumber;//身份证号

    @BindView(R.id.tv_bank_name)
    TextView tvBankName;//开户行

    @BindView(R.id.tv_card_number)
    TextView tvBankCardNumber;//银行卡账号

    @BindView(R.id.tv_city)
    TextView tvCity;//银行城市


    @BindView(R.id.tv_action)
    TextView mTvAction;

    private Dialog mAlertDialog;
    private LoadingDialog mLoadingDialog;

    private int personId;

    private TextView mTvRight;

    private PersonInfoResult.PersonInfo mPersonInfo;

    @Override
    public void init() {
        mLoadingDialog = new LoadingDialog(this);
        EventBus.getDefault().register(this);
        showBack();
        mTvRight = showRight();
        boolean modify = getIntent().getBooleanExtra("modify", false);
        personId = getIntent().getIntExtra("personId", 0);
        if (modify) {
            setEditable(true);
        } else {
            setEditable(false);
            loadPersonalInfo();
        }

        mTvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textView = (TextView) v;
                String text = textView.getText().toString();
                if (text.equals("修改")) {
                    textView.setText("退出修改");
                    setEditable(true);
                } else {
                    textView.setText("修改");
                    setEditable(false);
                }
            }
        });


    }


    @OnClick(R.id.tv_action)
    public void onActionClick(View view) {
        saveInfo();
    }


    /**
     * 保存信息
     */
    private void saveInfo() {

        //先检查字段是否为空
        mAlertDialog = UIUtils.getAlertDialog(this, "郑重提示", "请确保你填的信息已经正确", "正确提交", "我要检查", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlertDialog.dismiss();
                modifyPerson();

            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlertDialog.dismiss();
            }
        });
        mAlertDialog.show();
    }


    private void loadPersonalInfo() {

        mLoadingDialog.show();
        Map<String, String> map = new HashMap<>();
        map.put("phoneNumber", SPUtils.getUserName());
        wrapObserverWithHttp(WorkService.getWorkService().getPersonMsg(map)).subscribe(new Subscriber<PersonInfoResult>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                mLoadingDialog.dismiss();
                e.printStackTrace();
                showTest("网络错误");
            }

            @Override
            public void onNext(PersonInfoResult personInfoResult) {
                mLoadingDialog.dismiss();
                if (personInfoResult.getRetCode() == 0) {
                    mPersonInfo = personInfoResult.getData();
                    setInfo(mPersonInfo);
                } else {
                    showTest(personInfoResult.getMsg());
                }
            }
        });

    }


    private void setInfo(PersonInfoResult.PersonInfo personInfo) {

        personId = personInfo.getId();
        if (TextUtils.isEmpty(personInfo.getBankCardNumber())) {
            showTest("你还提交过个人信息，请点击右上角的修改，添加您个人身份信息");
        } else {
            tvBankCardNumber.setText(personInfo.getBankCardNumber());
            tvBankName.setText(personInfo.getBankName());
            tvIDCardNumber.setText(personInfo.getIdentityId());
            tvRealName.setText(personInfo.getTrueName());
            tvCity.setText(personInfo.getBankCity());
        }
    }


    @OnClick({R.id.tv_real_name, R.id.tv_id_card_number})
    public void onPersonalClick() {
        startModifyActivity(1);
    }


    @OnClick({R.id.tv_bank_name, R.id.tv_card_number, R.id.tv_city})
    public void onCardClick() {
        startModifyActivity(2);
    }


    private void startModifyActivity(int flag) {
        Intent intent = new Intent(this, ModifyInfoActivity.class);
        intent.putExtra("flag", flag);
        startActivity(intent);
    }

    /**
     * 提交修改的信息
     */
    private void modifyPerson() {

        if (personId == 0) {
            showTest("内部出错，没获取到你的个人信息，请退出本界面再试");
            return;
        }
        mLoadingDialog.show("正在提交数据…");
        Map<String, Object> map = new HashMap<>();
        map.put("id", personId);
        map.put("trueName", tvRealName.getText().toString());
        map.put("identityId", tvIDCardNumber.getText().toString());
        map.put("bankCardNumber", tvBankCardNumber.getText().toString());
        map.put("bankName", tvBankName.getText().toString());
        map.put("bankCity", tvCity.getText().toString());


        //
        Subscription s = wrapObserverWithHttp(WorkService.getWorkService().modifiyPersonMsg(map)).subscribe(new Subscriber<DefaultResult>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                mLoadingDialog.dismiss();
                e.printStackTrace();
                showTest("请检查网络");

            }

            @Override
            public void onNext(DefaultResult loginResult) {
                mLoadingDialog.dismiss();
                if (loginResult.getRetCode() == 0) {
                    showTest("更新个人信息成功!");
                    setEditable(false);
                } else {
                    showTest(loginResult.getMsg());
                }
            }
        });
        addSubscription(s);

    }


    @Subscribe
    public void recevied(PersonInfoResult.PersonInfo info) {


        if (mPersonInfo == null) {
            mPersonInfo = new PersonInfoResult.PersonInfo();
            mPersonInfo.setId(personId);
        }
        if (info.isUpdataBankCard()) {
            //修改设置银行卡的信息

            //update person bank info
            mPersonInfo.setBankCardNumber(info.getBankCardNumber());
            mPersonInfo.setBankCity(info.getBankCity());
            mPersonInfo.setBankName(info.getBankName());
            mPersonInfo.setBankCardUrl(info.getBankCardUrl());

            //update widget
            tvBankCardNumber.setText(info.getBankCardNumber());
            tvBankName.setText(info.getBankName());
            tvCity.setText(info.getBankCity());
        } else {

            //update person id card info
            mPersonInfo.setIdCardBackgroundUrl(info.getIdCardBackgroundUrl());
            mPersonInfo.setIdCardFrontUrl(info.getIdCardFrontUrl());
            mPersonInfo.setIdentityId(info.getIdentityId());
            mPersonInfo.setTrueName(info.getTrueName());

            //update widget
            tvIDCardNumber.setText(info.getIdentityId());
            tvRealName.setText(info.getTrueName());
        }


    }


    /**
     * 设置信息的可点击状态
     *
     * @param flag
     */
    private void setEditable(boolean flag) {

        if (flag) {
            tvRealName.setEnabled(true);
            tvIDCardNumber.setEnabled(true);
            tvBankName.setEnabled(true);
            tvBankCardNumber.setEnabled(true);
            tvCity.setEnabled(true);
            mTvRight.setText("退出修改");
            mTvAction.setVisibility(View.VISIBLE);
        } else {
            tvRealName.setEnabled(false);
            tvIDCardNumber.setEnabled(false);
            tvBankName.setEnabled(false);
            tvBankCardNumber.setEnabled(false);
            tvCity.setEnabled(false);
            mTvRight.setText("修改");
            mTvAction.setVisibility(View.GONE);
        }
    }


    @Override
    public int createSuccessView() {
        return R.layout.activity_setting_v2;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
