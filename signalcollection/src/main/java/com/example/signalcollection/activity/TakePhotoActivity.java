package com.example.signalcollection.activity;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import com.example.signalcollection.R;
import com.example.signalcollection.bean.TakePhoto;
import com.example.signalcollection.util.FileUtil;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 自己实现的拍照界面
 * Created by Konmin on 2017/3/6.
 */

public class TakePhotoActivity extends BaseActivity implements SurfaceHolder.Callback {


    private String photoPath;

    private int mTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
    }

    @BindView(R.id.surface_view)
    SurfaceView mSurfaceView;


    @BindView(R.id.iv_save)
    View ivSave;

    @BindView(R.id.iv_cancel)
    View ivCancel;

    @BindView(R.id.iv_take_photo)
    View ivTakePicture;

    private SurfaceHolder mSurfaceHolder;
    private Camera camera;


    private PictureCallback mPictureCallback;


    @Override
    public void init() {
        hideActionBar();
        mPictureCallback = new PictureCallback();
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.addCallback(this);
        mTag = getIntent().getIntExtra("TAG", 1);

    }

    @Override
    public int createSuccessView() {
        return R.layout.activity_take_photo;
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Logger.i("surfaceCreated");
        camera = Camera.open();
        try {
            //设置预览监听
            camera.setPreviewDisplay(holder);
            Camera.Parameters parameters = camera.getParameters();
            parameters.setPictureFormat(PixelFormat.JPEG);
            //parameters.setPictureSize();
            if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                parameters.set("orientation", "portrait");
                camera.setDisplayOrientation(90);
                parameters.setRotation(90);
            }

            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//1连续对焦
            camera.setParameters(parameters);
            //启动摄像头预览
            camera.startPreview();
            camera.cancelAutoFocus();//如果要实现连续的自动对焦，这一句必须加上
        } catch (IOException e) {
            e.printStackTrace();
            camera.release();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Logger.i("surfaceChanged");

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Logger.i("surfaceDestroyed");
        if (camera != null) {
            camera.stopPreview();
            camera.release();
        }
    }


    @OnClick(R.id.iv_cancel)
    public void cancel() {
        camera.startPreview();
        showSaveCancel(false);
    }

    @OnClick(R.id.iv_save)
    public void save() {

        if (!TextUtils.isEmpty(photoPath)) {
            EventBus.getDefault().post(new TakePhoto(photoPath, mTag));
            photoPath = null;
            this.finish();
        } else {
            showTest("保存出错，请重新拍照…");
            camera.startPreview();
        }

    }


    @OnClick(R.id.iv_take_photo)
    public void takePhoto() {
        showSaveCancel(true);
        Camera.Parameters parameters = camera.getParameters();// 获取相机参数集
        List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();// 获取支持保存图片的尺寸
        Camera.Size pictureSize = supportedPictureSizes.get(getPictureSize(supportedPictureSizes));// 从List取出Size
        parameters.setPictureSize(pictureSize.width, pictureSize.height);
        // 设置照片的大小
        camera.setParameters(parameters);
        camera.takePicture(null, null, mPictureCallback);
    }


    private int getPictureSize(List<Camera.Size> sizes) {


        // 屏幕的宽度
        int screenWidth = getScreenWidth();
        int index = -1;

        for (int i = 0; i < sizes.size(); i++) {
            if (Math.abs(screenWidth - sizes.get(i).width) == 0) {
                index = i;
                break;
            }
        }
        // 当未找到与手机分辨率相等的数值,取列表中间的分辨率
        if (index == -1) {
            index = sizes.size() / 2;
        }
        return index;
    }


    private int getScreenWidth() {
        return getWindowManager().getDefaultDisplay().getWidth();
    }


    private void showSaveCancel(boolean flag) {
        if (flag) {
            ivTakePicture.setVisibility(View.GONE);
            ivCancel.setVisibility(View.VISIBLE);
            ivSave.setVisibility(View.VISIBLE);
        } else {
            ivTakePicture.setVisibility(View.VISIBLE);
            ivCancel.setVisibility(View.GONE);
            ivSave.setVisibility(View.GONE);
        }
    }


    private class PictureCallback implements Camera.PictureCallback {


        @Override
        public void onPictureTaken(final byte[] data, Camera camera) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    photoPath = FileUtil.getPhotoPath(TakePhotoActivity.this);
                    try {

                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

                        if (bitmap != null) {
                            bitmap = rotateBitmapByDegree(bitmap, -90);
                            FileOutputStream outputStream = new FileOutputStream(new File(photoPath));
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                            outputStream.flush();
                            outputStream.close();
                        }

                    } catch (Exception e) {
                        photoPath = null;
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }


    public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        Bitmap returnBm = null;
        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bm;
        }

        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }


    private int getBitmapDegree(String path) {
        int degree = 0;
        try {
            // 从指定路径下读取图片，并获取其EXIF信息
            ExifInterface exifInterface = new ExifInterface(path);
            // 获取图片的旋转信息
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Logger.e("orientation :" + orientation);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }


}
