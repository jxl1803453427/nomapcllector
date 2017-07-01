package com.example.signalcollection.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.example.signalcollection.Constans;
import com.example.signalcollection.R;
import com.example.signalcollection.activity.RegisterActivityV2;
import com.example.signalcollection.activity.WebActivity;
import com.example.signalcollection.bean.CompanyResult;
import com.example.signalcollection.bean.DefaultResult;
import com.example.signalcollection.bean.RegisterResult;
import com.example.signalcollection.network.RetrofitUtil;
import com.example.signalcollection.network.WorkService;
import com.example.signalcollection.recyclerview.SpinnerAdapter;
import com.example.signalcollection.view.LoadingDialog;
import com.orhanobut.logger.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Subscriber;
import rx.Subscription;

/**
 * 填写基本信息的Fragment
 * Created by Konmin on 2017/3/4.
 */

public class RegisterBaseInfoFragment extends BaseFragment {


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


    private LoadingDialog mLoadingDialog;//加载的对话框
    private SpinnerAdapter<CompanyResult.Data> mAdapterCompany;//公司列表的适配器
    private String mSelectedCompanyCode;//被选择的公司code;
    private List<CompanyResult.Data> mListCompanyData;//公司列表数据
    private String mRegisterCode;//注册码
    private Thread mThread;//算秒数的线程
    private String mStrMobile;
    public static final String USERNAME = "username";

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


    /**
     * 谷歌提建议要有一个空参的构造函数
     */
    public RegisterBaseInfoFragment() {

    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mLoadingDialog = new LoadingDialog(getActivity());
        mAdapterCompany = new SpinnerAdapter<CompanyResult.Data>(getActivity()) {
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
    }


    /**
     * 网络获取团队列表
     */
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

        ((RegisterActivityV2) getActivity()).addSubscription(s);
    }


    @OnClick({R.id.tv_get_ver_code, R.id.tv_register, R.id.tv_agreement})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_get_ver_code:
                getVerificationCode();
                break;
            case R.id.tv_register:
                ((RegisterActivityV2) getActivity()).showNextFragment();
                //register();
                break;
            case R.id.tv_agreement:
                Intent intent = new Intent(getActivity(), WebActivity.class);
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

        mStrMobile = etMobile.getText().toString().trim();

        if (TextUtils.isEmpty(mStrMobile)) {
            showTest("请填写手机号码");
            return;
        }


        final String strCode = etVerCode.getText().toString().trim();
        if (TextUtils.isEmpty(strCode)) {
            showTest("请填写验证码");
            return;
        } else if (!mRegisterCode.equals(strCode)) {
            showTest("验证码不正确，请重新输入");
            return;
        }


        mLoadingDialog.show("正在注册…");
        Map<String, String> map = new HashMap<>();
        map.put("phoneNumber", mStrMobile);
        map.put("companyCode", mSelectedCompanyCode);
        map.put("vcode", strCode);

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

                } else {
                    Logger.i("register result  msg:" + o.getMsg());
                    showTest(o.getMsg());
                }
            }
        });

    }


    /**
     * 获取手机验证码
     */
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
                    mRegisterCode = o.getData();
                    if (o.getRetCode() == 0 && !TextUtils.isEmpty(mRegisterCode)) {
                        showTest("验证码已经发出，请注意查收");
                    }
                } else {

                    Logger.i("register code msg :" + o.getMsg() + "recode :" + o.getRetCode());
                    showTest(o.getMsg());
                }


            }
        });

        tvGetVerCode.setClickable(false);
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //多次发送消息
                    for (int i = 0; i <= 60; i++) {
                        mHandler.sendEmptyMessage(i);
                        mThread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        mThread.start();
    }


    @Override
    public int createSuccessView() {
        return R.layout.fragment_register_base_info;
    }
}
