package com.example.signalcollection.activity;

import android.app.Dialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.signalcollection.R;
import com.example.signalcollection.bean.DefaultResult;
import com.example.signalcollection.bean.Exprop;
import com.example.signalcollection.bean.PersonInfoResult;
import com.example.signalcollection.network.WorkService;
import com.example.signalcollection.recyclerview.SpinnerAdapter;
import com.example.signalcollection.util.IDCard;
import com.example.signalcollection.util.SPUtils;
import com.example.signalcollection.util.UIUtils;
import com.example.signalcollection.view.LoadingDialog;
import com.example.signalcollection.view.SelectDialog;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Subscriber;
import rx.Subscription;

/**
 * 个人的设置界面
 * Created by Konmin on 2017/2/13.
 */

public class SettingActivity extends BaseActivity {


    @BindView(R.id.et_real_name)
    EditText etRealName;//真实姓名

    @BindView(R.id.et_id_card_number)
    EditText etIDCardNumber;//身份证号

    @BindView(R.id.tv_bank_name)
    TextView tvBankName;//开户行

    @BindView(R.id.et_card_number)
    EditText etBankCardNumber;//银行卡账号

    @BindView(R.id.et_other_bank)
    EditText etOtherBank;//其他的银行

    @BindView(R.id.et_city)
    EditText etCity;//银行城市


    @BindView(R.id.tv_action)
    TextView mTvAction;

    private Dialog mAlertDialog;
    private LoadingDialog mLoadingDialog;

    private int personId;

    private TextView mTvRight;

    private PersonInfoResult.PersonInfo mPersonInfo;

    private SpinnerAdapter<String> mBankSpinnerAdapter;

    private List<String> mBankList;


    private SelectDialog<String> mStringSelectDialog;
    private String realName;
    private String idCardNumber;
    private String cardNumber;
    private String mBankName;
    private String cityName;

    @Override
    public void init() {
        mLoadingDialog = new LoadingDialog(this);
        //EventBus.getDefault().register(this);
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
                    //恢复原样
                    setInfo(mPersonInfo);
                    setEditable(false);
                }
            }
        });
        initBankName();
        createDialog();
    }


    private void initBankName() {
        String[] banks = getResources().getStringArray(R.array.bank);
        mBankList = Arrays.asList(banks);
    }


    @OnClick(R.id.tv_action)
    public void onActionClick(View view) {
        saveInfo();
    }


    @OnClick(R.id.tv_bank_name)
    public void onBankNameClick() {
        mStringSelectDialog.setDataAndShow(mBankList);
    }


    private void createDialog() {

        mStringSelectDialog = new SelectDialog<String>(this, "请选择开户银行") {
            @Override
            public void showData(TextView textView, String s) {
                textView.setText(s);
            }
        };

        mStringSelectDialog.setOnItemSelectedListener(new SelectDialog.OnItemSelectedListener() {
            @Override
            public void onItemSelected(Object t) {
                String bankName = (String) t;
                tvBankName.setText(bankName);
                mBankName = bankName;
                if (bankName.equals("其他")) {
                    etOtherBank.setVisibility(View.VISIBLE);
                } else {
                    etOtherBank.setVisibility(View.GONE);
                }
            }
        });

    }


    /**
     * 保存信息
     */
    private void saveInfo() {


        realName = etRealName.getText().toString();
        if (TextUtils.isEmpty(realName)) {
            showTest("请输入姓名");
            return;
        }


        idCardNumber = etIDCardNumber.getText().toString();
        if (TextUtils.isEmpty(idCardNumber)) {

            showTest("请输入身份证号");
            return;
        }


        if (!new IDCard(idCardNumber).validate()) {
            showTest("身份证号码有误，请重新输入");
            return;
        }


        cardNumber = etBankCardNumber.getText().toString();
        if (TextUtils.isEmpty(cardNumber)) {

            showTest("请填写银行卡号");
            return;
        }

        if (cardNumber.length() < 14) {
            showTest("银行卡的格式不对，请重新输入");
            return;
        }


        mBankName = tvBankName.getText().toString();

        if (TextUtils.isEmpty(mBankName)) {
            showTest("请选择你的开户银行");
            return;
        }

        if (mBankName.equals("其他")) {
            mBankName = etOtherBank.getText().toString();
            if (TextUtils.isEmpty(mBankName)) {
                showTest("请填写您的银行卡所属银行");
                return;
            }
        }


        cityName = etCity.getText().toString();
        if (TextUtils.isEmpty(cityName)) {

            showTest("请填写银行卡开户城市");
            return;
        }


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
            showTest("你还没提交过个人信息，请点击右上角的修改，添加您个人身份信息");
        } else {

            etBankCardNumber.setText(personInfo.getBankCardNumber());
            tvBankName.setText(personInfo.getBankName());
            etIDCardNumber.setText(personInfo.getIdentityId());
            etRealName.setText(personInfo.getTrueName());
            etCity.setText(personInfo.getBankCity());
        }
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
        map.put("trueName", realName);
        map.put("identityId", idCardNumber);
        map.put("bankCardNumber", cardNumber);
        map.put("bankName", mBankName);
        map.put("bankCity", cityName);

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


    /**
     * 设置信息的可点击状态
     *
     * @param flag
     */
    private void setEditable(boolean flag) {

        if (flag) {
            etRealName.setEnabled(true);
            etIDCardNumber.setEnabled(true);
            tvBankName.setEnabled(true);
            etBankCardNumber.setEnabled(true);
            etCity.setEnabled(true);
            mTvRight.setText("退出修改");
            mTvAction.setVisibility(View.VISIBLE);
        } else {
            etRealName.setEnabled(false);
            etIDCardNumber.setEnabled(false);
            tvBankName.setEnabled(false);
            etBankCardNumber.setEnabled(false);
            etCity.setEnabled(false);
            mTvRight.setText("修改");
            mTvAction.setVisibility(View.GONE);
        }
    }

    @Override
    public int createSuccessView() {
        return R.layout.activity_setting;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
