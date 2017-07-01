package com.example.signalcollection.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.signalcollection.Constans;
import com.example.signalcollection.R;
import com.example.signalcollection.bean.DefaultResult;
import com.example.signalcollection.bean.EventBusMessage;
import com.example.signalcollection.bean.NmpReportData;
import com.example.signalcollection.bean.PhotoUrl;
import com.example.signalcollection.bean.ReportWrongBody;
import com.example.signalcollection.network.RetrofitUtil;
import com.example.signalcollection.network.WorkService;
import com.example.signalcollection.util.ImageUploadTool;
import com.example.signalcollection.util.UIUtils;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.example.signalcollection.util.ImageUtil;
import com.example.signalcollection.util.SPUtils;
import com.example.signalcollection.view.LoadingDialog;
import com.orhanobut.logger.Logger;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Subscriber;
import rx.Subscription;

public class TaskErrorActivity extends BaseActivity {

    @BindView(R.id.iv1)
    ImageView mIv1;

    @BindView(R.id.iv2)
    ImageView mIv2;


    @BindView(R.id.et_reason)
    EditText etReason;

    private String mPath;
    private LoadingDialog mLoadingDialog;
    private List<String> mLtPhotos = new ArrayList<>();
    private static final String IMGBASEURL = "http://jmtool3.jjfinder.com/";
    private NmpReportData mNmReportData;
    private Gson mGson = new Gson();
    public static final String KEY_AREACODE = "AREACODE";
    private ImageView mCurrentImageView;
    private List<PhotoUrl> photoUrls = new ArrayList<>();
    private Dialog mAlertDialog;


    @Override
    public void init() {
        setMyTitle("任务不存在");
        EventBus.getDefault().register(this);
        mNmReportData = (NmpReportData) getIntent().getSerializableExtra(KEY_AREACODE);
        if (mNmReportData == null) {
            finish();
        }
        mLoadingDialog = new LoadingDialog(this);
        showBack();
    }


    @Subscribe
    public void selected(EventBusMessage message) {

        String path = message.tag;
        if (!TextUtils.isEmpty(path) && message.what == Constans.EVENBUS_MESSAGE_CODE_RECEIVED_ERR_IMG) {
            if (path != null) {
                final Uri uri = Uri.parse("file:/" + path);
                mPath = path;
                ImageLoader.getInstance().displayImage(uri.toString(), mCurrentImageView, ImageUtil.getInstance().getBaseDisplayOption());
                PhotoUrl photoUrl = new PhotoUrl();
                String key = RetrofitUtil.PHOTO_BASE_URL + "wrongareaphoto" + File.separator + System.currentTimeMillis() + ".jpg";
                photoUrl.setPhotoUrl(key);
                photoUrl.setImgLocalUrl(mPath);
                photoUrls.add(photoUrl);
                mLtPhotos.add(key);
            }
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public int createSuccessView() {
        return R.layout.activity_task_error;
    }

    @OnClick({R.id.btSubmit, R.id.iv1, R.id.iv2})
    public void submit(View view) {


        switch (view.getId()) {
            case R.id.iv1:
                mCurrentImageView = (ImageView) view;
                Intent intent = new Intent(this, CropImageActivity.class);
                intent.putExtra("origin", Constans.EVENBUS_MESSAGE_CODE_RECEIVED_ERR_IMG);
                startActivity(intent);
                break;
            case R.id.iv2:
                mCurrentImageView = (ImageView) view;
                Intent intent1 = new Intent(this, CropImageActivity.class);
                intent1.putExtra("origin", Constans.EVENBUS_MESSAGE_CODE_RECEIVED_ERR_IMG);
                startActivity(intent1);
                break;
            case R.id.btSubmit:

                if (mLtPhotos.isEmpty() || mLtPhotos.size() < 2) {
                    showTest("请拍摄2张照片");
                    return;
                }

                if (TextUtils.isEmpty(etReason.getText())) {

                    showTest("请填写上报原因");
                    return;
                }

                optItemImg(photoUrls);
                break;
        }
    }


    private void optItemImg(List<PhotoUrl> photoUrls) {

        mLoadingDialog.show("正在提交照片…");
        ImageUploadTool.getmInstance().upLoadImages(photoUrls, new ImageUploadTool.UploadFinishListener() {
            @Override
            public void onUploadFinish(final List<PhotoUrl> failureImages) {
                mLoadingDialog.dismiss();
                if (!failureImages.isEmpty()) {
                    String str = "您当前上传的照片出现" + failureImages.size() + "张失败,点击确定提交失败的照片";
                    mAlertDialog = UIUtils.getAlertDialog(TaskErrorActivity.this, null, str, null, "确定", null, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mAlertDialog.dismiss();
                            optItemImg(failureImages);

                        }
                    });
                    mAlertDialog.setCancelable(false);
                    mAlertDialog.show();
                } else {
                    report();
                }
            }


            @Override
            public void onImageNotFound() {
                mLoadingDialog.dismiss();
                showTest("照片未找到");
            }
        });


    }


    private void report() {
        mLoadingDialog.show("正在提交数据……");
        ReportWrongBody reportWrongBody = new ReportWrongBody();
        reportWrongBody.setRefAreaCode(mNmReportData.getAreaCode());
        reportWrongBody.setRefReportUsername(SPUtils.getUserName());
        reportWrongBody.setRemark(etReason.getText().toString());
        reportWrongBody.setWrongType(1);
        reportWrongBody.setPhotos(mLtPhotos);
        Subscription subscription = wrapObserverWithHttp(WorkService.getWorkService().reportWrong(reportWrongBody)).subscribe(new Subscriber<DefaultResult>() {
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
            public void onNext(DefaultResult result) {
                mLoadingDialog.dismiss();
                if (result.getRetCode() == 0) {

                    Logger.i("report err success");
                    showTest("上报成功！");
                    finish();
                } else {
                    showTest(result.getMsg());
                }

            }
        });
        addSubscription(subscription);

    }


    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
