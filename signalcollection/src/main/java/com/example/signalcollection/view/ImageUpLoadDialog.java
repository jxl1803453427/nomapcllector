package com.example.signalcollection.view;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.signalcollection.R;
import com.orhanobut.logger.Logger;

/**
 * 照片上传的dialog
 * Created by Konmin on 2016/11/7.
 */

public class ImageUpLoadDialog extends Dialog {


    private ProgressBar pbAll;
    private TextView tvMsg;
    private TextView tvCount;
    private int mTotalCount;
    private ImageView ivLoading;

    private Animation mOperatingAnim;

    public ImageUpLoadDialog(Context context) {
        super(context, R.style.Style_Dialog);
        setContentView(R.layout.dialog_upload_images);
        pbAll = (ProgressBar) findViewById(R.id.pb_all);
        tvMsg = (TextView) findViewById(R.id.tv_msg);
        tvCount = (TextView) findViewById(R.id.tv_count);
        ivLoading = (ImageView) findViewById(R.id.iv_pb);
        mOperatingAnim = AnimationUtils.loadAnimation(context, R.anim.anim_rotate);
        AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
        mOperatingAnim.setInterpolator(interpolator);
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = window.getWindowManager().getDefaultDisplay().getWidth() - 30;
        window.setAttributes(params);
        setCancelable(false);
    }


    public void show(int totalCount) {
        if (mOperatingAnim != null) {
            ivLoading.startAnimation(mOperatingAnim);
        }
        mTotalCount = totalCount;
        pbAll.setMax(totalCount);
        tvMsg.setText("该任务有" + totalCount + "张图片\n请耐心等待图片的上传");
        pbAll.setProgress(0);
        tvCount.setText(0 + "/" + mTotalCount);
        show();
    }


    public void updateAllProgress(int count) {
        if (isShowing()) {
            tvCount.setText(count + "/" + mTotalCount);
            pbAll.setProgress(count);
        }
    }

}
