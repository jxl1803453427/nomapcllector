package com.example.signalcollection.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.signalcollection.R;
import com.example.signalcollection.activity.ModifyInfoActivity;
import com.example.signalcollection.activity.RegisterActivityV2;
import com.example.signalcollection.activity.TakePhotoActivity;
import com.example.signalcollection.bean.PersonInfoResult;
import com.example.signalcollection.bean.TakePhoto;
import com.example.signalcollection.util.IDCard;
import com.example.signalcollection.util.ImageUploadTool;
import com.example.signalcollection.util.ImageUtil;
import com.example.signalcollection.util.UIUtils;
import com.example.signalcollection.view.LoadingDialog;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 输入身份证和拍照信息fragment
 * Created by Konmin on 2017/3/2.
 */

public class IdentityCardPictureFragment extends BaseFragment {


    @BindView(R.id.et_id_card_num)
    EditText etIdCardNumber;//身份证号码

    @BindView(R.id.et_real_name)
    EditText etRealName;//真实姓名


    @BindView(R.id.iv_id_card_front)
    ImageView ivCardFront;//身份证前面照片

    @BindView(R.id.iv_id_card_back)
    ImageView ivCardBackground;//身份证后面照片


    private TakePhoto mFrontTakePhoto;
    private TakePhoto mBackTakePhoto;


    private LoadingDialog mLoadingDialog;
    private String realName;
    private String idCardNumber;
    private Dialog mAlertDialog;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EventBus.getDefault().register(this);
        mLoadingDialog = new LoadingDialog(getActivity());
    }

    @Override
    public int createSuccessView() {
        return R.layout.fragment_id_card_picture;
    }


    @OnClick(R.id.tv_commit)
    public void commitClick() {
        //通过参数控制这里的逻辑
        realName = etRealName.getText().toString();
        if (TextUtils.isEmpty(realName)) {
            showTest("请输入姓名");
            return;
        }


        idCardNumber = etIdCardNumber.getText().toString();
        if (TextUtils.isEmpty(idCardNumber)) {

            showTest("请输入身份证号");
            return;
        }


        if (!new IDCard(idCardNumber).validate()) {
            showTest("身份证号码有误，请重新输入");
            return;
        }


        if (mFrontTakePhoto == null) {
            showTest("请拍身份证正面的照片");
            return;
        }

        if (mBackTakePhoto == null) {
            showTest("请拍身份证背面的照片");
            return;
        }


        mAlertDialog = UIUtils.getAlertDialog(getActivity(), "郑重提示", "请确保你填的信息已经正确填写,否则将会影响你的佣金发放！", "正确提交", "我要检查", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlertDialog.dismiss();
                mLoadingDialog.show("正在上传照片…");
                uploadPhoto(mFrontTakePhoto);

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


    @OnClick({R.id.iv_id_card_front, R.id.iv_id_card_back})
    public void onIvClick(View view) {

        switch (view.getId()) {
            case R.id.iv_id_card_front:
                openCamera(1);
                break;
            case R.id.iv_id_card_back:
                openCamera(2);
                break;
        }
    }


    @Subscribe
    public void getPhoto(TakePhoto takePhoto) {
        final Uri uri = Uri.parse("file:/" + takePhoto.getPath());
        if (takePhoto.getTag() == 1) {
            mFrontTakePhoto = takePhoto;
            ImageLoader.getInstance().displayImage(uri.toString(), ivCardFront, ImageUtil.getInstance().getBaseDisplayOption());
        } else if (takePhoto.getTag() == 2) {
            mBackTakePhoto = takePhoto;
            ImageLoader.getInstance().displayImage(uri.toString(), ivCardBackground, ImageUtil.getInstance().getBaseDisplayOption());
        }

    }


    /**
     * 打开相机
     *
     * @param tag 用于标识是哪个照片
     */
    private void openCamera(int tag) {
        Intent intent = new Intent(getActivity(), TakePhotoActivity.class);
        intent.putExtra("TAG", tag);
        startActivity(intent);
    }


    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }


    private boolean frontTakePhotoSuccess;


    /**
     * 上传照片
     *
     * @param photo
     */
    private void uploadPhoto(TakePhoto photo) {
        ImageUploadTool.getmInstance().uploadImage(photo, new ImageUploadTool.UploadListener() {
            @Override
            public void onSuccess() {
                frontTakePhotoSuccess = (!frontTakePhotoSuccess);
                if (frontTakePhotoSuccess) {
                    uploadPhoto(mBackTakePhoto);
                } else {
                    mLoadingDialog.dismiss();
                    FragmentActivity activity = getActivity();
                    EventBus.getDefault().post(generatePersonInfo());
                    if (activity instanceof RegisterActivityV2) {
                        ((RegisterActivityV2) activity).showNextFragment();
                    } else if (activity instanceof ModifyInfoActivity) {
                        activity.finish();
                    }
                }
            }

            @Override
            public void onFailure(String err, TakePhoto takePhoto) {
                uploadPhoto(takePhoto);
            }
        });
    }


    private PersonInfoResult.PersonInfo generatePersonInfo() {

        PersonInfoResult.PersonInfo personInfo = new PersonInfoResult.PersonInfo();
        personInfo.setIdCardFrontUrl(mFrontTakePhoto.getUrl());
        personInfo.setIdCardBackgroundUrl(mBackTakePhoto.getUrl());
        personInfo.setTrueName(realName);
        personInfo.setIdentityId(idCardNumber);
        return personInfo;
    }


}
