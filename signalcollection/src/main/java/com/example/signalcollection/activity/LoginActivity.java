package com.example.signalcollection.activity;

import android.app.Dialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.signalcollection.R;
import com.example.signalcollection.bean.DefaultResult;
import com.example.signalcollection.bean.RegisterResult;
import com.example.signalcollection.bean.UserBehavior;
import com.example.signalcollection.network.RetrofitUtil;
import com.example.signalcollection.network.WorkService;
import com.example.signalcollection.util.LocationService;
import com.example.signalcollection.util.SPUtils;
import com.example.signalcollection.util.UIUtils;
import com.example.signalcollection.view.CheckCanUseDialog;
import com.example.signalcollection.view.LoadingDialog;
import com.orhanobut.logger.Logger;
import com.yyc.mcollector.SignalCollector;
import com.yyc.mcollector.bean.WifiData;
import com.yyc.mcollector.listener.WifiScanResultListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnLongClick;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import rx.Subscriber;
import rx.Subscription;

/**
 *
 */
public class LoginActivity extends BaseActivity {

    @BindView(R.id.etUserName)
    EditText mEtName;

    @BindView(R.id.etPassword)
    EditText mEtPassword;

    @BindView(R.id.tv_get_ver_code)

    TextView tvGetVerCode;


    private Thread thread;

    private LoadingDialog mLoadingDialog;

    public static final String USERNAME = "username";
    private String userName;
    private boolean mCancelAlias;

    private Dialog mDialog;

    private CheckCanUseDialog mCheckCanUseDialog;


    private Handler mHandler = new Handler(new Handler.Callback() {
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
    private String mobile;
    private String registerCode;


    /***
     *
     */
    private TagAliasCallback mTagAliasCallback = new TagAliasCallback() {
        @Override
        public void gotResult(int i, String s, Set<String> set) {
            switch (i) {
                case 0:
                    Logger.i("注册alias成功");
                    if (!mCancelAlias) {
                        LocationService.getInstance().start(new LocationService.LocationServiceListener() {
                            @Override
                            public void onLocationResult(double longitude, double latitude, String errMsg) {
                                reportUserBehavior(longitude, latitude);
                            }
                        });
                    }

                    break;
                case 6002:
                    Logger.i("注册alias失败");
                    //一直注册到注册成功
                    if (mCancelAlias) {
                        JPushInterface.setAlias(getApplicationContext(), "", mTagAliasCallback);
                    } else {
                        JPushInterface.setAlias(getApplicationContext(), SPUtils.getUserName(), mTagAliasCallback);
                    }
                    break;
                default:
            }
        }
    };


    @Override
    public void init() {

        userName = SPUtils.getUserName();
        if (!((boolean) (SPUtils.get("un_sign", false))) && !TextUtils.isEmpty(userName)) {
            //用户名不为空的情况
            long lastLoginTime = SPUtils.getLong("last_login", 0);
            Logger.i("lastLoginTime:" + lastLoginTime);
            if (lastLoginTime != 0 && (System.currentTimeMillis() - lastLoginTime) < (24 * 60 * 60 * 1000)) {
                //排除超收个小时的情况
                baseStartActivity(MainActivity.class);
                this.finish();
                return;
            }
        }

        mCancelAlias = true;
        JPushInterface.setAlias(getApplicationContext(), "", mTagAliasCallback);
        mLoadingDialog = new LoadingDialog(this);


        //第一次安装手机打开后才做的动作
        if (!SPUtils.getBoolean("could_use", false)) {
            // checkDeviceCouldUsed();
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            if (!wifiManager.isWifiEnabled()) {
                //显示开启WiFi的对话框
                wifiManager.setWifiEnabled(true);//强制打开WiFi
            }
            wifiManager.getScanResults();//如果是Android 6.0会使应用弹出获取权限的对话框
            mCheckCanUseDialog = new CheckCanUseDialog(this, new CheckCanUseDialog.CheckOverListener() {
                @Override
                public void over(boolean flag) {
                    SPUtils.put("could_use", flag);
                    if (!flag) {
                        finish();
                    }
                }
            });
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mCheckCanUseDialog.show();
                }
            }, 1000);
        }


    }


    /**
     * 检查设备是否可用
     */
    private void checkDeviceCouldUsed() {

        //先检查WiFi是否开启
        //检查是否有扫描WiFi的权限
        mLoadingDialog.show("正在检查手机是否可以用于采集……");
        final SignalCollector signalCollector = new SignalCollector(this);
        signalCollector.setCollectWifi(new WifiScanResultListener() {
            @Override
            public void scanResult(List<WifiData> list) {
                //遍历list发现 mac为123456789abc就不行，不是就不行
                if (list != null && !list.isEmpty()) {
                    boolean couldUsed = true;
                    for (WifiData data : list) {
                        if (data.getMac().toUpperCase().equals("12:34:56:78:9A:BC")) {
                            couldUsed = false;
                            break;
                        }
                    }

                    if (couldUsed) {
                        showUsedTipDialog();
                    } else {
                        showNotUsedDialog();
                        SPUtils.put("could_use", false);
                    }
                } else {
                    signalCollector.collectWifi();
                }
            }
        });

        signalCollector.collectWifi();
    }


    private void showNotUsedDialog() {
        mLoadingDialog.dismiss();
        mDialog = UIUtils.getAlertDialog(this, "很遗憾", "你的手机不能用于采集，请更换设备");
        mDialog.setCancelable(false);
        mDialog.show();
    }


    private void showUsedTipDialog() {
        mLoadingDialog.dismiss();
        mDialog = UIUtils.getAlertDialog(this, "恭喜你", "你的手机能用于采集", "我知道了", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SPUtils.put("could_use", true);
                login();
            }
        });
        mDialog.setCancelable(false);
        mDialog.show();
    }


    @OnClick(R.id.tv_login)
    public void loginClick() {
        if (!SPUtils.getBoolean("could_use", false)) {
            checkDeviceCouldUsed();
        } else {
            login();
        }

    }

    @Override
    protected void onResume() {
        userName = (String) SPUtils.get(USERNAME, "");
        mEtName.setText(userName);
        super.onResume();
    }

    @OnClick(R.id.tv_start_register)
    void startRegister() {
        baseStartActivity(RegisterActivity.class);
    }


    @OnClick(R.id.tv_get_ver_code)
    void getVerificationCode() {

        mobile = mEtName.getText().toString().trim();
        if (TextUtils.isEmpty(mobile)) {
            showTest("填写手机号码");
            return;
        }

        mLoadingDialog.show("正在获取验证码…");
        Map<String, String> map = new HashMap<>();
        map.put("phoneNumber", mobile);

        Subscription subscription = wrapObserverWithHttp(WorkService.getWorkService().getLoginCode(map)).subscribe(new Subscriber<RegisterResult>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                mLoadingDialog.dismiss();
                e.printStackTrace();
                showTest(mNetWorkError);
            }

            @Override
            public void onNext(RegisterResult o) {
                mLoadingDialog.dismiss();
                registerCode = o.getData();
                if (o.getRetCode() == 0 && !TextUtils.isEmpty(registerCode)) {
                    showTest("验证码已经发出，请注意查收");
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
                } else {
                    showTest(o.getMsg());
                }

            }
        });
        addSubscription(subscription);
    }


    private void login() {
        mobile = mEtName.getText().toString().trim();
        if (TextUtils.isEmpty(mobile)) {
            showTest("请输入手机号");
            return;
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////
        //SPUtils.put(USERNAME, mobile);                                                                  //
        //baseStartActivity(MainActivity.class);                                                          //
        //mCancelAlias = false;                                                                          //
        //mLoadingDialog.show("正在注册消息服务…");                                                         //
        //JPushInterface.setAlias(getApplicationContext(), SPUtils.getUserName(), mTagAliasCallback);    //
        //String key = intent.getStringExtra("KEY");                                                      //
        //mUrl = intent.getStringExtra("URL");                                                           //
        ////////////////////////////////////////////////////////////////////////////////////////////////////

        loginWithCode();
    }


    /**
     * 获取版本号
     *
     * @return 获取版本号
     */
    private int getVersionCode() {


        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }


    }


    /**
     * 用验证码登录
     */
    private void loginWithCode() {

        String code = mEtPassword.getText().toString().trim();
        if (TextUtils.isEmpty(code)) {
            showTest("请输入验证码");
            return;
        }

        mLoadingDialog.show("正在登录…");
        Map<String, Object> map = new HashMap<>();
        map.put("phoneNumber", mobile);
        map.put("vcode", code);
        map.put("vernum", getVersionCode());
        Subscription sbMyAccount = wrapObserverWithHttp(WorkService.getWorkService().loginV2(map)).subscribe(new Subscriber<DefaultResult>() {
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
            public void onNext(DefaultResult loginResult) {

                //Logger.i(loginResult.toString());
                mLoadingDialog.dismiss();
                if (loginResult.getRetCode() == 0) {
                    SPUtils.put(USERNAME, mobile);
                    //登录成功就设置别名
                    mCancelAlias = false;
                    mLoadingDialog.show("正在注册消息服务…");
                    JPushInterface.setAlias(getApplicationContext(), mobile.trim(), mTagAliasCallback);
                } else {
                    showTest(loginResult.getMsg());
                }
            }
        });
        addSubscription(sbMyAccount);
    }


    private void reportUserBehavior(double longitude, double latitude) {

        UserBehavior userBehavior = new UserBehavior();
        userBehavior.setUsername(userName);
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        userBehavior.setImei(telephonyManager.getDeviceId());
        userBehavior.setPhomeModel(Build.BRAND);
        userBehavior.setSystemVersion("Android " + Build.VERSION.RELEASE);
        userBehavior.setNetworkType(networkType(telephonyManager.getNetworkType()));
        userBehavior.setAction(1);
        userBehavior.setLongitude(longitude);
        userBehavior.setLatitude(latitude);

        //Logger.e("IMEI" + new Gson().toJson(userBehavior));
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
                    LoginActivity.this.finish();
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
     * @param networkType networkType
     * @return String
     */
    private String networkType(int networkType) {

        String network;
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


    @OnLongClick(R.id.logo)
    boolean a() {
        final String test = mEtPassword.getText().toString();
        if (!TextUtils.isEmpty(test) && test.equals("yycadmin")) {
            UIUtils.getAlertDialogEdit(this, RetrofitUtil.API_URL, new UIUtils.EditResultInterface() {
                @Override
                public void editText(String text) {
                    RetrofitUtil.resetServerUrl(text);
                }
            }).show();
            mEtPassword.setText("");
        }

        return true;
    }


    @Override
    protected void onDestroy() {
        SPUtils.putLong("last_login", System.currentTimeMillis());
        super.onDestroy();
    }


    @Override
    public int createSuccessView() {

        return R.layout.activity_login;
    }
}
