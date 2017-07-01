package com.example.signalcollection.activity;

import android.app.Dialog;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.signalcollection.Constans;
import com.example.signalcollection.R;
import com.example.signalcollection.bean.EventBusMessage;
import com.example.signalcollection.bean.NmpReportData;
import com.example.signalcollection.bean.PhotoUrl;
import com.example.signalcollection.util.ImageUtil;
import com.example.signalcollection.util.UIUtils;
import com.example.signalcollection.view.LoadingDialog;
import com.example.signalcollection.view.TouchImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;

import static com.example.signalcollection.network.RetrofitUtil.PHOTO_BASE_URL;

/**
 * 拍摄审核照片
 * Created by Konmin on 2017/5/6.
 */

public class TakeAuditPhotoActivity extends BaseActivity {


    @BindView(R.id.gv_photo)
    GridView gvPhoto;
    private List<PhotoUrl> mPhotoUrls;

    private PhotoAdapter mPhotoAdapter;
    private Dialog mImageViewDialog;

    private String mAreaCode;
    private int mNmpReportDataId;

    private boolean isRetake;//是不是重拍？
    private int mRetakePosition;//重拍的位置是哪个？
    private LoadingDialog mLoadingDialog;

    @Subscribe
    public void receivedEvenbusMessage(EventBusMessage message) {

        if (message.what == Constans.EVENBUS_MESSAGE_CODE_RECEIVED_TASK_IMG) {
            String path = message.tag;
            File file;
            if (!TextUtils.isEmpty(path) && (file = new File(path)).exists() && file.length() > 0) {
                if (isRetake) {
                    //要找到那张照片把信息给改了，显示上面修改照片
                    PhotoUrl photoUrl = mPhotoUrls.get(mRetakePosition);
                    photoUrl.setImgLocalUrl(path);
                    photoUrl.setUpdateTime(System.currentTimeMillis());
                    if (photoUrl.getId() != 0) {
                        photoUrl.update(photoUrl.getId());
                    } else {
                        photoUrl = DataSupport.where("photourl = ?", photoUrl.getPhotoUrl()).findFirst(PhotoUrl.class);
                        photoUrl.setImgLocalUrl(path);
                        photoUrl.setUpdateTime(System.currentTimeMillis());
                        photoUrl.update(photoUrl.getId());
                    }
                    mPhotoAdapter.notifyDataSetChanged();
                } else {
                    String key = PHOTO_BASE_URL + "cpphoto" + File.separator + mAreaCode + "-" + System.currentTimeMillis() + ".jpg";
                    PhotoUrl photoUrl = new PhotoUrl();
                    photoUrl.setPhotoUrl(key);
                    photoUrl.setImgLocalUrl(path);
                    photoUrl.setPhotoType(Constans.PHOTO_TYPE_AREA);
                    photoUrl.setNid(mNmpReportDataId);
                    photoUrl.setUpdateTime(System.currentTimeMillis());
                    photoUrl.save();
                    if (mPhotoUrls == null) {
                        mPhotoUrls = new ArrayList<>();
                    }
                    mPhotoUrls.add(photoUrl);
                    mPhotoAdapter.notifyDataSetChanged();
                }
            }
        }
    }


    @Override
    public void init() {
        showBack();
        setMyTitle("拍摄审核照片");
        mLoadingDialog = new LoadingDialog(this);
        Intent intent = getIntent();
        mAreaCode = intent.getStringExtra(Constans.AREA_CODE);
        mNmpReportDataId = intent.getIntExtra(Constans.AREA_DATA_ID, 0);
        if (mNmpReportDataId == 0) {
            NmpReportData data = DataSupport.where("areacode = " + mAreaCode).findFirst(NmpReportData.class);
            mNmpReportDataId = data.getId();
        }
        EventBus.getDefault().register(this);
        mPhotoAdapter = new PhotoAdapter();
        gvPhoto.setAdapter(mPhotoAdapter);
        gvPhoto.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (mPhotoUrls == null || mPhotoUrls.isEmpty() || position == mPhotoUrls.size()) {
                    //拍照
                    isRetake = false;
                    Intent intent1 = new Intent(TakeAuditPhotoActivity.this, CropImageActivity.class);
                    intent1.putExtra("origin", Constans.EVENBUS_MESSAGE_CODE_RECEIVED_TASK_IMG);
                    startActivity(intent1);
                } else {
                    //预览照片
                    mRetakePosition = position;
                    showImgDialog("file:/" + mPhotoUrls.get(position).getImgLocalUrl());
                }

            }
        });
        initData();
    }

    private void initData() {
        mLoadingDialog.show("正在加载照片数据…");
        Observable<List<PhotoUrl>> observable = Observable.create(new Observable.OnSubscribe<List<PhotoUrl>>() {
            @Override
            public void call(Subscriber<? super List<PhotoUrl>> subscriber) {
                List<PhotoUrl> photoUrls = DataSupport.where("nid = ? AND phototype =?", String.valueOf(mNmpReportDataId), String.valueOf(Constans.PHOTO_TYPE_AREA)).find(PhotoUrl.class);
                subscriber.onNext(photoUrls);
                subscriber.onCompleted();
            }
        });
        Subscription subscription = wrapObserverWithHttp(observable).subscribe(new Subscriber<List<PhotoUrl>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                mLoadingDialog.dismiss();
                e.printStackTrace();
                showTest("加载数据错误…");
            }

            @Override
            public void onNext(List<PhotoUrl> photoUrls) {
                mLoadingDialog.dismiss();
                mPhotoUrls = photoUrls;
                mPhotoAdapter.notifyDataSetChanged();
            }
        });

        addSubscription(subscription);
    }

    private class PhotoAdapter extends BaseAdapter {

        @Override
        public int getCount() {

            if (mPhotoUrls == null) {
                return 1;
            }
            return mPhotoUrls.size() + 1;
        }

        @Override
        public Object getItem(int position) {

            if (mPhotoUrls == null || mPhotoUrls.isEmpty()) {
                return null;
            }
            return mPhotoUrls.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView = null;
            if (convertView == null) {
                imageView = new ImageView(TakeAuditPhotoActivity.this);
                GridView.LayoutParams layoutParams = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UIUtils.dip2px(120));
                imageView.setScaleType(ImageView.ScaleType.CENTER);
                imageView.setLayoutParams(layoutParams);
                imageView.setBackgroundResource(R.color.graybg);
                convertView = imageView;
            } else {
                imageView = (ImageView) convertView;
            }

            if (mPhotoUrls == null || mPhotoUrls.isEmpty() || position == mPhotoUrls.size()) {

                if (position == 1) {
                    String uri = "drawable://" + R.mipmap.ic_camera;

                    ImageLoader.getInstance().displayImage(uri, imageView, ImageUtil.getInstance().getBaseDisplayOption());
                } else {
                    imageView.setImageResource(R.mipmap.ic_camera);
                }
            } else {
                PhotoUrl photoUrl = mPhotoUrls.get(position);
                Logger.i(photoUrl.getImgLocalUrl());
                ImageLoader.getInstance().displayImage("file:/" + photoUrl.getImgLocalUrl(), imageView, ImageUtil.getInstance().getBaseDisplayOption());
            }
            return convertView;
        }
    }


    /**
     * 显示照片预览的对话框
     */
    private void showImgDialog(String uri) {

        if (mImageViewDialog == null) {
            mImageViewDialog = new Dialog(this, R.style.Style_Dialog_FullScreen);
            mImageViewDialog.setContentView(R.layout.dialog_img_viewer_edit);
            WindowManager manager = mImageViewDialog.getWindow().getWindowManager();
            int height = manager.getDefaultDisplay().getHeight();
            int width = manager.getDefaultDisplay().getWidth();
            WindowManager.LayoutParams params = mImageViewDialog.getWindow().getAttributes();
            params.width = width;
            params.height = height;
            mImageViewDialog.getWindow().setAttributes(params);
        }

        TouchImageView imageView = (TouchImageView) mImageViewDialog.findViewById(R.id.tiv);
        imageView.setImageResource(android.R.color.transparent);
        TextView tvRetake = (TextView) mImageViewDialog.findViewById(R.id.tv_retake);
        tvRetake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageViewDialog.dismiss();
                isRetake = true;
                Intent intent1 = new Intent(TakeAuditPhotoActivity.this, CropImageActivity.class);
                intent1.putExtra("origin", Constans.EVENBUS_MESSAGE_CODE_RECEIVED_TASK_IMG);
                startActivity(intent1);
               /* baseStartActivity(CropImageActivity.class);*/
            }
        });
        ImageLoader.getInstance().displayImage(uri, imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageViewDialog.dismiss();
            }
        });
        mImageViewDialog.show();
    }


    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public int createSuccessView() {
        return R.layout.activity_take_audit_photo;
    }
}
