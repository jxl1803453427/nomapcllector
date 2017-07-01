package com.example.signalcollection.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.signalcollection.R;
import com.example.signalcollection.activity.RegisterActivityV2;
import com.example.signalcollection.activity.TakePhotoActivity;
import com.example.signalcollection.bean.PersonInfoResult;
import com.example.signalcollection.bean.TakePhoto;
import com.example.signalcollection.recyclerview.SpinnerAdapter;
import com.example.signalcollection.util.ImageUploadTool;
import com.example.signalcollection.util.ImageUtil;
import com.example.signalcollection.util.UIUtils;
import com.example.signalcollection.view.LoadingDialog;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 银行卡信息提交的Activity
 * Created by Konmin on 2017/3/3.
 */

public class BankCardFragment extends BaseFragment {


    private SpinnerAdapter<String> mAdapterBank;

    @BindView(R.id.et_bank_card_number)
    EditText etBankCardNumber;//银行卡号填写框

    @BindView(R.id.et_other_bank)
    EditText etOtherBank;//其他银行的银行名字

    @BindView(R.id.sp_bank)
    AppCompatSpinner spBankName;//银行名字选择

    @BindView(R.id.et_bank_city)
    EditText etCity;//银行卡户城市


    @BindView(R.id.iv_bank_card)
    ImageView ivBankCard;//银行卡照片


    private String mBankName;//银行卡名字


    private LoadingDialog mLoadingDialog;
    private TakePhoto mTakePhoto;
    private String cardNumber;
    private String cityName;
    private Dialog mAlertDialog;


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EventBus.getDefault().register(this);
        mLoadingDialog = new LoadingDialog(getActivity());
        mAdapterBank = new SpinnerAdapter<String>(getActivity()) {
            @Override
            public void setText(TextView textView, String s) {
                textView.setText(s);
            }
        };
        spBankName.setAdapter(mAdapterBank);
        spBankName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
        getBankNames();
    }


    private void getBankNames() {
        String[] banks = getResources().getStringArray(R.array.bank);
        List<String> banklist = Arrays.asList(banks);
        mAdapterBank.setListData(banklist);
    }


    @OnClick(R.id.iv_bank_card)
    public void takePhoto() {

        Intent intent = new Intent(getActivity(), TakePhotoActivity.class);
        intent.putExtra("TAG", 3);
        startActivity(intent);

    }


    @Subscribe
    public void getPhoto(TakePhoto takePhoto) {

        final Uri uri = Uri.parse("file:/" + takePhoto.getPath());
        if (takePhoto.getTag() == 3) {
            mTakePhoto = takePhoto;
            ImageLoader.getInstance().displayImage(uri.toString(), ivBankCard, ImageUtil.getInstance().getBaseDisplayOption());
        }

    }


    @OnClick(R.id.tv_submit)
    public void submit() {

        cardNumber = etBankCardNumber.getText().toString();
        if (TextUtils.isEmpty(cardNumber)) {

            showTest("请填写银行卡号");
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


        if (mTakePhoto == null) {
            showTest("登记的银行卡要拍照拍照");
            return;
        }

        if (cardNumber.length() < 14) {
            showTest("银行卡的格式不对，请重新输入");
            return;
        }

        mAlertDialog = UIUtils.getAlertDialog(getActivity(), "郑重提示", "请确保你填的信息已经正确填写,否则将会影响你的佣金发放！", "正确提交", "我要检查", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlertDialog.dismiss();
                mLoadingDialog.show("正在提交照片…");
                uploadImage(mTakePhoto);
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlertDialog.dismiss();
            }
        });
        mAlertDialog.setCancelable(false);
        mAlertDialog.show();

    }


    /**
     * @param photo
     */
    private void uploadImage(TakePhoto photo) {

        ImageUploadTool.getmInstance().uploadImage(photo, new ImageUploadTool.UploadListener() {
            @Override
            public void onSuccess() {
                mLoadingDialog.dismiss();
                FragmentActivity activity = getActivity();
                if (activity instanceof RegisterActivityV2) {
                    //直接数据提交，然后关闭
                    PersonInfoResult.PersonInfo info = ((RegisterActivityV2) activity).getPersonInfo();
                    info.setBankCardNumber(cardNumber);
                    info.setBankCardUrl(mTakePhoto.getUrl());
                    info.setBankCity(cityName);
                    info.setBankName(mBankName);
                    info.setUpdataBankCard(true);
                    submit(info);
                } else {
                    //回调给SettingActivity然后关闭
                    EventBus.getDefault().post(generatePersonInfo());
                    activity.finish();
                }
            }

            @Override
            public void onFailure(String err, TakePhoto takePhoto) {
                uploadImage(takePhoto);
            }
        });


    }


    /**
     * 提交银行卡信息
     *
     * @param info
     */
    private void submit(PersonInfoResult.PersonInfo info) {
        mLoadingDialog.show();

        //wrapObserverWithHttp()

    }


    private PersonInfoResult.PersonInfo generatePersonInfo() {
        PersonInfoResult.PersonInfo personInfo = new PersonInfoResult.PersonInfo();
        personInfo.setBankCity(cityName);
        personInfo.setBankName(mBankName);
        personInfo.setBankCardUrl(mTakePhoto.getUrl());
        personInfo.setUpdataBankCard(true);
        personInfo.setBankCardNumber(cardNumber);
        return personInfo;
    }


    @Override
    public int createSuccessView() {
        return R.layout.fragment_bank_card;
    }


    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }
}
