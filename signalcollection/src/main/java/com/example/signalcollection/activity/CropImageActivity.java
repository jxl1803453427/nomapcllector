package com.example.signalcollection.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.example.signalcollection.Constans;
import com.example.signalcollection.R;
import com.example.signalcollection.bean.EventBusMessage;
import com.example.signalcollection.util.FileUtil;
import com.example.signalcollection.util.ImageUtil;
import com.example.signalcollection.view.LoadingDialog;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;


/**
 * 裁剪照片的Activtiy
 * Created by Konmin on 2016/8/15.
 */
public class CropImageActivity extends BaseActivity {


    private Uri mTakePhotoUri;

    private String mOutputPath;

    private LoadingDialog mLoadingDialog;

    private static final String KEY_PHOTO_URI = "photo_uri";
    private static final String KEY_IS_TAKE_PHOTO = "is_take_photo";
    private boolean isTakePhoto = false;

    private int what = 0;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Logger.i("CropImageActivity onSaveInstanceState");
        outState.putParcelable(KEY_PHOTO_URI, mTakePhotoUri);
        outState.putBoolean(KEY_IS_TAKE_PHOTO, isTakePhoto);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        if (savedInstanceState != null) {
            Logger.i("savedInstanceState != null");
            Uri uri = savedInstanceState.getParcelable(KEY_PHOTO_URI);
            if (uri != null) {
                mTakePhotoUri = uri;
            }
            boolean a = savedInstanceState.getBoolean(KEY_IS_TAKE_PHOTO);
            if (a) {
                isTakePhoto = a;
            }
        }

        super.onCreate(savedInstanceState);
    }


    @Override
    public void init() {
        hideActionBar();
        what = getIntent().getIntExtra("origin", 0);
        mLoadingDialog = new LoadingDialog(this);
        if (!isTakePhoto) {
            takePhotoAction();
        }
    }


    /**
     * 拍照
     */
    protected void takePhotoAction() {

        if (!FileUtil.isSDCardEnable()) {
            showTest("没有SD卡");
            return;
        }

        File takePhotoFolder = new File(FileUtil.getTakePhotoDir());

        boolean suc = FileUtil.mkdirs(takePhotoFolder);
        String format = "yyyyMMddHHmmss";
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        File toFile = new File(takePhotoFolder, "IMG" + dateFormat.format(new Date()) + ".jpg");
        if (suc) {
            isTakePhoto = true;
            mTakePhotoUri = Uri.fromFile(toFile);
            Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mTakePhotoUri);
            startActivityForResult(captureIntent, 1001);
        } else {
            showTest("拍照片失败");
        }
    }


    private void confirm(final String path, final EventBusMessage eventBusMessage) {
        mLoadingDialog.show("正在保存照片……");
        Observable observable = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                //FileOutputStream fos = null;
                mOutputPath = FileUtil.getImagePath(getApplicationContext());
                try {
                    //这里要优化，会出现内存溢出
                    //fos = new FileOutputStream(mOutputPath);
                    //fos 在decodeBitmapFromBitmap里面被关掉了，所以下面不用关掉
                    //ImageUtil.getInstance().decodeBitmapFromBitmap(path, 1080, 1080, fos);
                    //NativeUtil.saveBitmap(path,mOutputPath);
                    //ImageUtil.getInstance().saveBitmap(mOutputPath, path);
                    int maxSize = 200;
                    switch (what) {
                        case Constans.EVENBUS_MESSAGE_CODE_RECEIVED_TASK_IMG:
                            maxSize = 400;
                            break;
                        case Constans.EVENBUS_MESSAGE_CODE_RECEIVED_ERR_IMG:
                            maxSize = 100;
                            break;
                    }
                    ImageUtil.getInstance().saveBitmap(mOutputPath, path, maxSize);

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    subscriber.onNext("s");
                    subscriber.onCompleted();
                }

                if (mTakePhotoUri != null) {
                    String path = mTakePhotoUri.getPath();
                    if (!TextUtils.isEmpty(path)) {
                        File file = new File(path);
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                }
            }
        });

        Subscription subscription = wrapObserverWithHttp(observable).subscribe(new Subscriber() {
            @Override
            public void onCompleted() {
                mLoadingDialog.dismiss();
                eventBusMessage.tag = mOutputPath;
                EventBus.getDefault().post(eventBusMessage);
                finish();
            }

            @Override
            public void onError(Throwable e) {
                mLoadingDialog.dismiss();
                e.printStackTrace();
                showTest("压缩图片失败");
            }

            @Override
            public void onNext(Object o) {

            }
        });

        addSubscription(subscription);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Logger.i("onActivity Result");
        EventBusMessage eventBusMessage = new EventBusMessage();
        if (what != 0) {
            eventBusMessage.what = what;
        } else {
            eventBusMessage.what = Constans.EVENBUS_MESSAGE_CODE_RECEIVED_IMG;
        }


        if (requestCode == 1001) {
            if (resultCode == RESULT_OK && mTakePhotoUri != null) {
                final String path = mTakePhotoUri.getPath();
                if (new File(path).exists()) {
                    confirm(path, eventBusMessage);
                } else {
                    showTest("拍照片失败");
                    Logger.i("photo file is not exists");
                    EventBus.getDefault().post(eventBusMessage);
                    finish();
                }
            } else {
                showTest("拍照片失败");
                EventBus.getDefault().post(eventBusMessage);
                finish();

                Logger.i("RESULT_OK is not oK");
            }
        } else {
            Logger.i("requestCode != 1001");
            EventBus.getDefault().post(eventBusMessage);
            finish();
        }
    }


    public void hideActionBar() {
        if (getSupportActionBar() != null && getSupportActionBar().isShowing()) {
            getSupportActionBar().hide();
        }
    }


    @Override
    public int createSuccessView() {
        return R.layout.activity_clip;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
