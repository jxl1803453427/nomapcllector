package com.example.signalcollection.activity;

import android.app.Dialog;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.signalcollection.Constans;
import com.example.signalcollection.R;
import com.example.signalcollection.bean.Exprop;
import com.example.signalcollection.bean.NmpReportData;
import com.example.signalcollection.bean.NmpReportPoint;
import com.example.signalcollection.bean.PhotoUrl;
import com.example.signalcollection.network.WorkService;
import com.example.signalcollection.util.UIUtils;
import com.example.signalcollection.view.LoadingDialog;
import com.example.signalcollection.view.TouchImageView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.orhanobut.logger.Logger;

import org.litepal.crud.DataSupport;
import org.litepal.util.Const;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Subscriber;
import rx.Subscription;

/**
 * 显示历史点的详情
 * Created by Konmin on 2017/4/6.
 */

public class HistoryDetailActivity extends BaseActivity {


    @BindView(R.id.tvName)
    TextView tvName;
    @BindView(R.id.tvType)
    TextView tvType;
    @BindView(R.id.tvFloor)
    TextView tvFloor;
    @BindView(R.id.tv_point_type)
    TextView tvPointType;
    @BindView(R.id.layout_exprop)
    LinearLayout layoutExprop;
    @BindView(R.id.iv2)
    ImageView iv2;
    @BindView(R.id.iv1)
    ImageView iv1;
    @BindView(R.id.tv_point_index)
    TextView tvPointIndex;


    private LoadingDialog mLoadingDialog;
    private Map<String, NmpReportPoint.Exprop> mMapPointExprop;

    private Gson mGson;
    private int mTaskId;
    private int mPointId;
    private int mPointSize;
    private int mPointIndex;

    private Dialog mImageViewDialog;

    @Override
    public void init() {
        mGson = new Gson();
        setMyTitle("点详细信息");
        showBack();
        mLoadingDialog = new LoadingDialog(this);
        Intent intent = getIntent();
        mTaskId = intent.getIntExtra(Constans.AREA_DATA_ID, 0);
        mPointId = intent.getIntExtra(Constans.POINT_DATA_ID, 0);
        mPointSize = intent.getIntExtra(Constans.POINT_COUNT, 0);
        Logger.e(mTaskId + "," + mPointId + "," + mPointSize);
        NmpReportPoint nmpReportPoint = DataSupport.find(NmpReportPoint.class, mPointId);
        mPointIndex = nmpReportPoint.getPointIndex();
        NmpReportData nmpReportData = DataSupport.find(NmpReportData.class, mTaskId);
        tvPointIndex.setText("当前点编号为：" + nmpReportPoint.getPointIndex());
        tvName.setText(nmpReportData.getAreaName());
        tvType.setText(nmpReportData.getAreaTypeName());
        tvFloor.setText(String.valueOf(nmpReportPoint.getFloorNumber()));
        tvPointType.setText(nmpReportPoint.getPointType());
        Type type = new TypeToken<ArrayList<NmpReportPoint.Exprop>>() {
        }.getType();
        List<NmpReportPoint.Exprop> list = mGson.fromJson(nmpReportPoint.getStrExprop(), type);
        exchangeExpropList2Map(list);
        loadExpropFromNet(nmpReportPoint.getPointTypeCode());
        loadImages(nmpReportPoint);
    }


    private void exchangeExpropList2Map(List<NmpReportPoint.Exprop> list) {
        mMapPointExprop = new HashMap<>();
        if (list != null && !list.isEmpty()) {
            for (NmpReportPoint.Exprop exprop : list) {
                mMapPointExprop.put(exprop.getRefExPropCode(), exprop);
            }
        }
    }

    private void loadExpropFromNet(String cpCode) {

        mLoadingDialog.show();

        Map<String, String> map = new HashMap<>();
        map.put("cpTypeCode", cpCode);

        Subscription sbMyAccount = wrapObserverWithHttp(WorkService.getWorkService().getExpropByCpCode(map)).subscribe(new Subscriber<Exprop>() {
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
            public void onNext(Exprop exprop) {
                mLoadingDialog.dismiss();
                if (exprop.getRetCode() == 0) {
                    if (!exprop.getData().isEmpty()) {
                        loadExprop2View(exprop);
                    }
                } else {
                    showTest(exprop.getMsg());
                }

            }
        });
        addSubscription(sbMyAccount);

    }


    @OnClick(R.id.iv1)
    public void iv1Click(View view) {
        String uri = (String) view.getTag();

        if (!TextUtils.isEmpty(uri)) {
            showImgDialog(uri);
        }


    }

    @OnClick(R.id.iv2)
    public void iv2Click(View view) {
        String uri = (String) view.getTag();
        if (!TextUtils.isEmpty(uri)) {
            showImgDialog(uri);
        }
    }


    /**
     * 加载扩展属性
     *
     * @param exprop 扩展属性
     */
    private void loadExprop2View(Exprop exprop) {

        for (Exprop.DataBean dataBean : exprop.getData()) {
            View view = LayoutInflater.from(this).inflate(R.layout.item_exprop, layoutExprop, false);
            LinearLayout.LayoutParams layoutParames = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParames.setMargins(0, UIUtils.dip2px(8), 0, 0);
            layoutExprop.addView(view, layoutParames);
            TextView tvTitle = (TextView) view.findViewById(R.id.tv);
            TextView tvValue = (TextView) view.findViewById(R.id.tvIn);
            tvTitle.setText(dataBean.getPropName() + "：");
            NmpReportPoint.Exprop exprop1 = mMapPointExprop.get(dataBean.getPropCode());
            if (exprop1 != null && !TextUtils.isEmpty(exprop1.getPropValue())) {
                tvValue.setText(exprop1.getPropValue());
            }
        }
    }


    /**
     * 加载图片
     */
    private void loadImages(NmpReportPoint point) {

       /* Type type = new TypeToken<ArrayList<String>>() {
        }.getType();

        List<String> photos = mGson.fromJson(point.getStrPhotoUrls(), type);*/
        List<PhotoUrl> photoUrls = DataSupport.where("nmpreportpoint_id = " + point.getId()).find(PhotoUrl.class);

        //店内点店外点要注意

        if (TextUtils.isEmpty(point.getSamePageCode())) {
            //普通点，就显示一张照片
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) iv1.getLayoutParams();
            layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT;
            layoutParams.height = UIUtils.dip2px(160);
            iv2.setVisibility(View.GONE);
            if (photoUrls != null && !photoUrls.isEmpty()) {
                int size = photoUrls.size();
                //PhotoUrl photoUrl1 = DataSupport.where("imgKey = ?", photos.get(size - 1)).findFirst(PhotoUrl.class);

                String uri = "file:/" + photoUrls.get(size - 1).getImgLocalUrl();
                iv1.setTag(uri);
                iv1.setScaleType(ImageView.ScaleType.CENTER_CROP);
                ImageLoader.getInstance().displayImage(uri, iv1);
            }
        } else {
            //店内显示在右边店外显示在左边
            NmpReportPoint.Exprop exprop = mMapPointExprop.get("EXP-SOTRE_INOROUT");
            boolean isIndoor = false;
            if (photoUrls != null && !photoUrls.isEmpty()) {
                int size = photoUrls.size();
                //PhotoUrl photoUrl1 = DataSupport.where("imgKey = ?", photos.get(size - 1)).findFirst(PhotoUrl.class);
                ImageView iv = null;
                if (exprop.getPropValue().equals("店内")) {
                    iv = iv2;
                    isIndoor = true;
                } else {
                    iv = iv1;
                    isIndoor = false;
                }
                String uri = "file:/" + photoUrls.get(size - 1).getImgLocalUrl();
                iv.setTag(uri);
                ImageLoader.getInstance().displayImage(uri, iv);
            }


            NmpReportPoint point1 = DataSupport.where("samepagecode = ? AND id <> ?", point.getSamePageCode(), point.getId() + "").findFirst(NmpReportPoint.class);
            //找出另外一个点，显示一张照片
            //List<String> photo2 = mGson.fromJson(point1.getStrPhotoUrls(), type);
            List<PhotoUrl> otherPhotos = DataSupport.where("nmpreportpoint_id = " + point1.getId()).find(PhotoUrl.class);
            if (otherPhotos != null && !otherPhotos.isEmpty()) {
                // PhotoUrl photoUrl1 = DataSupport.where("imgKey = ?", photo2.get(photo2.size() - 1)).findFirst(PhotoUrl.class);
                ImageView iv = null;
                if (isIndoor) {
                    iv = iv1;
                } else {
                    iv = iv2;
                }
                String uri = "file:/" + otherPhotos.get(otherPhotos.size() - 1).getImgLocalUrl();
                iv.setTag(uri);
                ImageLoader.getInstance().displayImage(uri, iv);
            }

        }
    }


    @OnClick(R.id.tv_modify)
    public void toModify() {

        Intent intent = new Intent(HistoryDetailActivity.this, ModifyHistoryActivity.class);
        intent.putExtra(Constans.AREA_DATA_ID, mTaskId);
        intent.putExtra(Constans.POINT_DATA_ID, mPointId);
        intent.putExtra(Constans.POINT_COUNT, mPointSize);
        intent.putExtra(Constans.POINT_INDEX, mPointIndex);
        startActivity(intent);
        finish();
    }


    /**
     * 显示照片预览的对话框
     */
    private void showImgDialog(String uri) {

        if (mImageViewDialog == null) {
            mImageViewDialog = new Dialog(this, R.style.Style_Dialog_FullScreen);
            mImageViewDialog.setContentView(R.layout.dialog_img_viewer);
            WindowManager manager = mImageViewDialog.getWindow().getWindowManager();
            int height = manager.getDefaultDisplay().getHeight();
            int width = manager.getDefaultDisplay().getWidth();
            WindowManager.LayoutParams params = mImageViewDialog.getWindow().getAttributes();
            params.width = width;
            params.height = height;
            mImageViewDialog.getWindow().setAttributes(params);
        }

        final TouchImageView imageView = (TouchImageView) mImageViewDialog.findViewById(R.id.tiv);
        ImageLoader.getInstance().displayImage(uri, imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setImageResource(android.R.color.transparent);
                mImageViewDialog.dismiss();
            }
        });
        mImageViewDialog.show();
    }


    @Override
    public int createSuccessView() {
        return R.layout.activity_history_detail;
    }

}
