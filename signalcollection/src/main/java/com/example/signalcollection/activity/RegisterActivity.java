package com.example.signalcollection.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.AppCompatSpinner;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.example.signalcollection.Constans;
import com.example.signalcollection.R;
import com.example.signalcollection.bean.CompanyResult;
import com.example.signalcollection.bean.DefaultResult;
import com.example.signalcollection.bean.RegisterResult;
import com.example.signalcollection.bean.UserBehavior;
import com.example.signalcollection.network.RetrofitUtil;
import com.example.signalcollection.network.WorkService;
import com.example.signalcollection.recyclerview.SpinnerAdapter;
import com.example.signalcollection.util.IDCard;
import com.example.signalcollection.util.LocationService;
import com.example.signalcollection.util.SPUtils;
import com.example.signalcollection.util.UIUtils;
import com.example.signalcollection.view.LoadingDialog;
import com.orhanobut.logger.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Subscriber;
import rx.Subscription;

/**
 * 手机注册页面
 * Created by Konmin on 2016/9/11.
 */
public class RegisterActivity extends BaseActivity {


    @BindView(R.id.sp_company)
    AppCompatSpinner spCompany;//公司下拉

    @BindView(R.id.et_mobile)
    EditText etMobile;//手机号

    @BindView(R.id.tv_get_ver_code)
    TextView tvGetVerCode;//获取验证码按钮

    @BindView(R.id.et_ver_code)
    EditText etVerCode;//验证码填写

    @BindView(R.id.cb_agree)
    CheckBox cbAgreement;//协议

    @BindView(R.id.tv_register)
    TextView tvRegister;//注册按钮

    @BindView(R.id.et_real_name)
    EditText etRealName;//真实姓名填写框

    @BindView(R.id.et_card_number)
    EditText etBankCardNumber;//银行卡号填写框

    @BindView(R.id.et_other_bank)
    EditText etOtherBank;//其他银行的银行名字

    @BindView(R.id.et_id)
    EditText etId;//身份证号

    @BindView(R.id.et_city)
    EditText etCityName;//城市

    @BindView(R.id.sp_bank)
    AppCompatSpinner spBank;//银行名字下拉


    private Thread thread;

    private SpinnerAdapter mAdapterCompany;

    private SpinnerAdapter mAdapterBank;

    private String mSelectedCompanyCode;

    private String registerCode;

    private LoadingDialog mLoadingDialog;

    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            //用来计时获取验证码的
            if (tvGetVerCode != null) {
                tvGetVerCode.setText((60 - msg.what) + " s");
                if (msg.what == 60) {
                    tvGetVerCode.setText("获取验证码");
                    tvGetVerCode.setClickable(true);
                }
            }
            return false;
        }
    });

    private List<CompanyResult.Data> mListCompanyData;
    public static final String USERNAME = "username";
    private String strMobile;

    private String mRealName;
    private String mId;
    private String mBankCardNumber;
    private String mBankName;
    private Dialog mAlertDialog;
    private String mBankCity;


    @Override
    public void init() {
        showBack();
        setMyTitle("注册账号");
        mLoadingDialog = new LoadingDialog(this);
        mAdapterCompany = new SpinnerAdapter<CompanyResult.Data>(this) {
            @Override
            public void setText(TextView textView, CompanyResult.Data o) {
                textView.setText(o.getCompanyName());
            }
        };
        spCompany.setAdapter(mAdapterCompany);
        spCompany.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSelectedCompanyCode = mListCompanyData.get(position).getCompanyCode();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        mAdapterBank = new SpinnerAdapter<String>(this) {
            @Override
            public void setText(TextView textView, String bankName) {
                textView.setText(bankName);
            }
        };

        spBank.setAdapter(mAdapterBank);

        spBank.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mBankName = (String) parent.getAdapter().getItem(position);
                if (mBankName.equals("其他")) {
                    etOtherBank.setVisibility(View.VISIBLE);
                } else {
                    etOtherBank.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        cbAgreement.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tvRegister.setEnabled(true);
                } else {
                    tvRegister.setEnabled(false);
                }
            }

        });
        getCompanyCode();
        getBankNames();
    }

    private void getBankNames() {
        String[] banks = getResources().getStringArray(R.array.bank);
        List<String> banklist = Arrays.asList(banks);
        mAdapterBank.setListData(banklist);
    }

    @Override
    public int createSuccessView() {
        return R.layout.activity_register;
    }


    @OnClick({R.id.tv_get_ver_code, R.id.tv_register, R.id.tv_agreement})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_get_ver_code:
                getVerificationCode();
                break;
            case R.id.tv_register:
                register();
                break;
            case R.id.tv_agreement:
                Intent intent = new Intent(this, WebActivity.class);
                intent.putExtra(Constans.TITLE, "注册协议及声明");
                intent.putExtra(Constans.URL, RetrofitUtil.API_URL + "html/app/user_agreement.html");
                startActivity(intent);
                break;
        }
    }


    /**
     * 注册
     */
    private void register() {

        strMobile = etMobile.getText().toString().trim();

        if (TextUtils.isEmpty(strMobile)) {
            showTest("请填写手机号码");
            return;
        }


        mRealName = etRealName.getText().toString();
        if (TextUtils.isEmpty(mRealName)) {
            showTest("请填写你的真实姓名");
            return;
        }

        mId = etId.getText().toString();
        if (TextUtils.isEmpty(mId)) {
            showTest("请填写你的身份证号");
            return;
        }

        mBankCardNumber = etBankCardNumber.getText().toString();
        if (TextUtils.isEmpty(mBankCardNumber)) {
            showTest("请填写你的银行卡号");
            return;
        }


        if (mBankName.equals("其他")) {
            mBankName = etOtherBank.getText().toString();
            if (TextUtils.isEmpty(mBankName)) {
                showTest("请填写您的银行卡所属银行");
                return;
            }
        }


        mBankCity = etCityName.getText().toString();
        if (TextUtils.isEmpty(mBankCity)) {
            showTest("请填写银行卡所属城市");
            return;
        }


        if (!new IDCard(mId).validate()) {
            showTest("身份证号出错，请重新输入");
            return;
        }


        if (mBankCardNumber.length() < 14) {
            showTest("银行卡号出错，请重新输入");
            return;
        }


        final String strCode = etVerCode.getText().toString().trim();
        if (TextUtils.isEmpty(strCode)) {
            showTest("请填写验证码");
            return;
        }


        if (TextUtils.isEmpty(mSelectedCompanyCode)) {
            showTest("请重新选择公司");
            return;
        }

        mAlertDialog = UIUtils.getAlertDialog(this, "郑重提示", "请确保你填的信息已经正确填写", "正确提交", "我要检查", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerFromNet(strCode);
                mAlertDialog.dismiss();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlertDialog.dismiss();
            }
        });
        mAlertDialog.show();

    }


    private void registerFromNet(String strCode) {


        /**
         * trueName
         identityId
         bankCardNumber
         bankName
         bankCity
         */
        mLoadingDialog.show("正在注册…");
        Map<String, String> map = new HashMap<>();
        map.put("phoneNumber", strMobile);
        map.put("companyCode", mSelectedCompanyCode);
        map.put("vcode", strCode);

        /**
         * 2017-2-15新增了身份证和银行信息
         */
        map.put("trueName", mRealName);
        map.put("identityId", mId);
        map.put("bankCardNumber", mBankCardNumber);
        map.put("bankName", mBankName);
        map.put("bankCity", mBankCity);

        wrapObserverWithHttp(WorkService.getWorkService().register(map)).subscribe(new Subscriber<DefaultResult>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                mLoadingDialog.dismiss();
                showTest(mNetWorkError);
            }

            @Override
            public void onNext(DefaultResult o) {
                mLoadingDialog.dismiss();
                if (o.getRetCode() == 0) {
                    SPUtils.put(USERNAME, strMobile);
                    LocationService.getInstance().start(new LocationService.LocationServiceListener() {
                        @Override
                        public void onLocationResult(double logitude, double latitude, String errMsg) {
                            reportUserBehavior(logitude, latitude);
                        }
                    });

                } else {
                    Logger.i("register result  msg:" + o.getMsg());
                    showTest(o.getMsg());
                }
            }
        });

    }

    /**
     * 上报用户行为
     *
     * @param longitude
     * @param latitude
     */
    private void reportUserBehavior(double longitude, double latitude) {

        UserBehavior userBehavior = new UserBehavior();
        userBehavior.setUsername(strMobile);
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        userBehavior.setImei(telephonyManager.getDeviceId());
        userBehavior.setPhomeModel(Build.BRAND);
        userBehavior.setSystemVersion("Android " + Build.VERSION.RELEASE);
        userBehavior.setNetworkType(networkType(telephonyManager.getNetworkType()));
        userBehavior.setAction(1);
        userBehavior.setLongitude(longitude);
        userBehavior.setLatitude(latitude);
        Subscription subscription = wrapObserverWithHttp(WorkService.getWorkService().reportUserBehavior(userBehavior)).subscribe(new Subscriber<DefaultResult>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                mLoadingDialog.dismiss();
                showTest(mNetWorkError);
            }

            @Override
            public void onNext(DefaultResult o) {
                mLoadingDialog.dismiss();
                if (o.getRetCode() == 0) {
                    baseStartActivity(MainActivity.class);
                    finish();
                } else {
                    showTest(o.getMsg());
                }
            }
        });

        addSubscription(subscription);

    }


    /**
     * 根据网络类型选择上传的WCDMA等等
     *
     * @param networkType
     * @return
     */
    private String networkType(int networkType) {

/**
 * @see #NETWORK_TYPE_UNKNOWN
 * @see #NETWORK_TYPE_GPRS
 * @see #NETWORK_TYPE_EDGE
 * @see #NETWORK_TYPE_UMTS
 * @see #NETWORK_TYPE_HSDPA
 * @see #NETWORK_TYPE_HSUPA
 * @see #NETWORK_TYPE_HSPA
 * @see #NETWORK_TYPE_CDMA
 * @see #NETWORK_TYPE_EVDO_0
 * @see #NETWORK_TYPE_EVDO_A
 * @see #NETWORK_TYPE_EVDO_B
 * @see #NETWORK_TYPE_1xRTT
 * @see #NETWORK_TYPE_IDEN
 * @see #NETWORK_TYPE_LTE
 * @see #NETWORK_TYPE_EHRPD
 * @see #NETWORK_TYPE_HSPAP
 */String network;

        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                network = "GPRS";
                break;
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                network = "WCDMA";
                break;

            case TelephonyManager.NETWORK_TYPE_CDMA:
                network = "CDMA";
                break;
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                network = "CDMA2000";
                break;
            case TelephonyManager.NETWORK_TYPE_LTE:
                network = "LTE";
                break;
            default:
                network = "UNKNOWN";
        }
        return network;

    }


    //获取所属公司列表
    private void getCompanyCode() {

        mLoadingDialog.show();
        Subscription s = wrapObserverWithHttp(WorkService.getWorkService().getCompany()).subscribe(new Subscriber<CompanyResult>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                mLoadingDialog.dismiss();
                showTest(mNetWorkError);
            }

            @Override
            public void onNext(CompanyResult o) {
                mLoadingDialog.dismiss();
                if (o.getRetCode() == 0) {
                    Logger.i(o.getMsg());
                    mListCompanyData = o.getData();
                    mAdapterCompany.setListData(mListCompanyData);
                } else {
                    showTest(o.getMsg());
                }
            }
        });

        addSubscription(s);
    }


    private void getVerificationCode() {

        if (TextUtils.isEmpty(etMobile.getText())) {
            showTest("请填写手机号码");
            return;
        }


        Map<String, String> map = new HashMap<>();
        map.put("phoneNumber", etMobile.getText().toString());

        wrapObserverWithHttp(WorkService.getWorkService().getRegisterCode(map)).subscribe(new Subscriber<RegisterResult>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                showTest(mNetWorkError);
            }

            @Override
            public void onNext(RegisterResult o) {

                if (o.getRetCode() == 0) {
                    registerCode = o.getData();
                    if (o.getRetCode() == 0 && !TextUtils.isEmpty(registerCode)) {
                        showTest("验证码已经发出，请注意查收");
                    }
                } else {

                    Logger.i("register code msg :" + o.getMsg() + "recode :" + o.getRetCode());
                    showTest(o.getMsg());
                }


            }
        });

        tvGetVerCode.setClickable(false);
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //多次发送消息
                    for (int i = 0; i <= 60; i++) {
                        mHandler.sendEmptyMessage(i);
                        thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

}
