package com.example.signalcollection.activity;

import android.app.Dialog;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.example.signalcollection.Constans;
import com.example.signalcollection.R;
import com.example.signalcollection.bean.PhotoUrl;
import com.example.signalcollection.qinniu.ImageUploadTool;
import com.example.signalcollection.recyclerview.CommonAdapter;
import com.example.signalcollection.recyclerview.ViewHolder;
import com.example.signalcollection.util.ImageUtil;
import com.example.signalcollection.util.UIUtils;
import com.example.signalcollection.view.ImageUpLoadDialog;
import com.example.signalcollection.view.LoadingDialog;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;

/**
 * 上传失败的照片
 * Created by Konmin on 2016/9/23.
 */

public class UploadFailurePhotoActivity extends BaseActivity {


    @BindView(R.id.list)
    RecyclerView mRecyclerView;

    private CommonAdapter<PhotoUrl> mCommonAdapter;

    private List<PhotoUrl> mPhotoUrls = new ArrayList<>();
    private LoadingDialog mLoadingDialog;
    private Dialog mAlertDialog;
    private ImageUpLoadDialog mImageUpLoadDialog;

    @Override
    public void init() {
        showBack();
        setMyTitle("重传已上传失败的照片");
        mLoadingDialog = new LoadingDialog(this);
        mImageUpLoadDialog = new ImageUpLoadDialog(this);
        mCommonAdapter = new CommonAdapter<PhotoUrl>(this, R.layout.item_failure_photo, mPhotoUrls) {
            @Override
            public void convert(ViewHolder holder, final PhotoUrl photoUrl) {
                final Uri uri = Uri.parse("file:/" + photoUrl.getImgLocalUrl());
                ImageLoader.getInstance().displayImage(uri.toString(), (ImageView) holder.getView(R.id.iv_photo), ImageUtil.getInstance().getBaseDisplayOption());
                holder.setOnClickListener(R.id.tv_upload, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        List<PhotoUrl> photoUrls = new ArrayList<PhotoUrl>();
                        photoUrls.add(photoUrl);
                        optItemImg(photoUrls);
                    }
                });
            }
        };
        mRecyclerView.setAdapter(mCommonAdapter);
        loadData();
    }

    private void loadData() {

        mLoadingDialog.show();
        Observable<List<PhotoUrl>> observable = Observable.create(new Observable.OnSubscribe<List<PhotoUrl>>() {
            @Override
            public void call(Subscriber<? super List<PhotoUrl>> subscriber) {
                List<PhotoUrl> photoUrls = DataSupport.where("isupload = ?", Constans.PHOTO_LOAD_FAILURE + "").find(PhotoUrl.class);
                List<PhotoUrl> hasPhotos = new ArrayList<PhotoUrl>();
                //检查有文件存在的
                if (photoUrls != null && !photoUrls.isEmpty()) {
                    for (PhotoUrl photoUrl : photoUrls) {
                        String path = photoUrl.getImgLocalUrl();
                        if (!TextUtils.isEmpty(path)) {
                            File file = new File(path);
                            if (file.exists() && file.length() > 0) {
                                hasPhotos.add(photoUrl);
                            }
                        }
                    }
                }
                subscriber.onNext(hasPhotos);
                subscriber.onCompleted();
            }
        });

        wrapObserverWithHttp(observable).subscribe(new Action1<List<PhotoUrl>>() {
            @Override
            public void call(List<PhotoUrl> o) {
                mLoadingDialog.dismiss();
                mPhotoUrls = o;
                mCommonAdapter.setData(mPhotoUrls);

            }
        });
    }


    @OnClick(R.id.tv_submit_all)
    public void submitAll() {
        if (mPhotoUrls != null && !mPhotoUrls.isEmpty()) {
            optItemImg(mPhotoUrls);
        } else {
            showTest("没有照片你点个毛啊……");
        }
    }


    private void optItemImg(final List<PhotoUrl> list) {
        mImageUpLoadDialog.show(list.size());
        //应该是在这里检查照片再不在吧？
        ImageUploadTool.getInstance().upLoadImages(list, new ImageUploadTool.UploadFinishListener() {
            @Override
            public void onUploadFinish(final List<PhotoUrl> failureImages) {
                mImageUpLoadDialog.dismiss();
                if (!failureImages.isEmpty()) {
                    String str = "您当前上传的照片出现" + failureImages.size() + "张失败,点击确定提交失败的照片";
                    mAlertDialog = UIUtils.getAlertDialog(UploadFailurePhotoActivity.this, null, str, null, "确定", null, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            optItemImg(failureImages);
                            mAlertDialog.dismiss();
                        }
                    });
                    mAlertDialog.setCancelable(false);
                    mAlertDialog.show();
                } else {
                    showTest("上传图片成功！");
                }
            }

            @Override
            public void onAllProgress(int count) {
                mImageUpLoadDialog.updateAllProgress(count);
            }

            @Override
            public void onImageNotFound() {
                mImageUpLoadDialog.dismiss();
                //这个应该是不会存在的了
                showTest("照片文件找不到，请检查照片是否存在！");
            }
        });
    }


    @Override
    public int createSuccessView() {
        return R.layout.activity_upload_failure_picture;
    }
}
