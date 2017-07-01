package com.example.signalcollection.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.signalcollection.R;


/**
 * Created by konmin on 2016/5/27.
 */
public class LoadingDialog extends Dialog {

    ImageView mIvLoading;

    TextView mTvWarm;

    private final Animation mOperatingAnim;

    public LoadingDialog(Context context) {
        super(context, R.style.Style_Dialog);
        setContentView(R.layout.dialog_loading);
        mIvLoading = (ImageView) findViewById(R.id.ivLoading);
        mTvWarm = (TextView) findViewById(R.id.tv_warm);
        mOperatingAnim = AnimationUtils.loadAnimation(context, R.anim.anim_rotate);
        AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
        mOperatingAnim.setInterpolator(interpolator);
        setCancelable(false);
    }


    @Override
    public void show() {
        mTvWarm.setText("正在加载数据…");
        if (mOperatingAnim != null) {
            mIvLoading.startAnimation(mOperatingAnim);
        }
        super.show();
    }


    public void showUpload() {
        mTvWarm.setText("正在上传数据…\n请不要做任何操作");
        if (mOperatingAnim != null) {
            mIvLoading.startAnimation(mOperatingAnim);
        }
        super.show();
    }


    public void show(String s) {
        if (!TextUtils.isEmpty(s)) {
            mTvWarm.setText(s);
        }
        if (mOperatingAnim != null) {
            mIvLoading.startAnimation(mOperatingAnim);
        }
        super.show();
    }

    public void dismiss() {
        mIvLoading.clearAnimation();
        super.dismiss();
    }
}
