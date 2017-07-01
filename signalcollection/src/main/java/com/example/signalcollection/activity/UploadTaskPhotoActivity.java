package com.example.signalcollection.activity;

import android.app.Dialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.example.signalcollection.Constans;
import com.example.signalcollection.R;
import com.example.signalcollection.bean.NmpReportData;
import com.example.signalcollection.bean.NmpReportPoint;
import com.example.signalcollection.bean.PhotoUrl;
import com.example.signalcollection.bean.UserInfoRequest;
import com.example.signalcollection.bean.WorkListResult;
import com.example.signalcollection.network.NetWorkService;
import com.example.signalcollection.network.WorkService;
import com.example.signalcollection.qinniu.ImageUploadTool;
import com.example.signalcollection.recyclerview.CommonAdapter;
import com.example.signalcollection.recyclerview.ViewHolder;
import com.example.signalcollection.util.DialogUtils;
import com.example.signalcollection.util.SPUtils;
import com.example.signalcollection.util.UIUtils;
import com.example.signalcollection.view.ImageUpLoadDialog;
import com.example.signalcollection.view.LoadingDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;

/**
 * 提交待审核后任务的照片
 * Created by Konmin on 2017/4/12.
 */

public class UploadTaskPhotoActivity extends BaseActivity {


    @BindView(R.id.rv_content)
    RecyclerView rvContent;
    private CommonAdapter<NmpReportData> mAdapter;
    private LoadingDialog mLoadingDialog;
    private Dialog mDialog;
    private ImageUpLoadDialog mImageUpLoadDialog;


    @Override

    public void init() {
        showBack();
        setMyTitle("历史任务照片上传");
        mLoadingDialog = new LoadingDialog(this);
        mImageUpLoadDialog = new ImageUpLoadDialog(this);
        mAdapter = new CommonAdapter<NmpReportData>(this, R.layout.item_task_photo) {
            @Override
            public void convert(ViewHolder holder, final NmpReportData nmpReportData) {
                holder.setText(R.id.tv_area_name, nmpReportData.getAreaName());
                holder.setOnClickListener(R.id.tv_submit, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        findPhotos(nmpReportData.getId());
                    }
                });
            }
        };
        rvContent.setAdapter(mAdapter);

        loadMyTaskFromNet();
    }


    /**
     * 通过任务的id去查找采集点，采集点查找照片
     *
     * @param taskId taskId
     */
    private void findPhotos(final int taskId) {

        mLoadingDialog.show("正在查找照片…");
        Observable<List<PhotoUrl>> observable = Observable.create(new Observable.OnSubscribe<List<PhotoUrl>>() {
            @Override
            public void call(Subscriber<? super List<PhotoUrl>> subscriber) {
                List<NmpReportPoint> points = DataSupport.where("nmpreportdata_id = ? AND remark <> ?", String.valueOf(taskId), "1").find(NmpReportPoint.class);
                List<PhotoUrl> allPhotoUrls = new ArrayList<PhotoUrl>();
                if (points != null && !points.isEmpty()) {
                    for (NmpReportPoint point : points) {
                        List<PhotoUrl> photoUrls = DataSupport.where("nmpreportpoint_id = " + point.getId()).find(PhotoUrl.class);
                        if (photoUrls != null && !photoUrls.isEmpty()) {
                            for (PhotoUrl photoUrl : photoUrls) {
                                File file = new File(photoUrl.getImgLocalUrl());
                                if (file.exists() && file.length() > 0) {
                                    allPhotoUrls.add(photoUrl);
                                }
                            }
                        }
                    }

                }
                List<PhotoUrl> areaPhotoUrls = DataSupport.where("nid = ? AND phototype =?", String.valueOf(taskId), String.valueOf(Constans.PHOTO_TYPE_AREA)).find(PhotoUrl.class);
                List<PhotoUrl> realAreaPhotoList = new ArrayList<PhotoUrl>();
                for (PhotoUrl photoUrl : areaPhotoUrls) {
                    File file;
                    if (!TextUtils.isEmpty(photoUrl.getImgLocalUrl()) && (file = new File(photoUrl.getImgLocalUrl())).exists() && file.length() > 0) {
                        realAreaPhotoList.add(photoUrl);
                    }
                }

                if (!realAreaPhotoList.isEmpty()) {
                    allPhotoUrls.addAll(realAreaPhotoList);
                }
                subscriber.onNext(allPhotoUrls);
                subscriber.onCompleted();
            }
        });


        Subscription subscription = wrapObserverWithHttp(observable).subscribe(new Subscriber<List<PhotoUrl>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                mLoadingDialog.dismiss();
            }

            @Override
            public void onNext(final List<PhotoUrl> photoUrls) {
                mLoadingDialog.dismiss();

                if (photoUrls.isEmpty()) {
                    showTest("在本地没有查找到照片，请检查是否拍了照片或者照片是否被删除");
                } else {
                    mDialog = UIUtils.getAlertDialog(UploadTaskPhotoActivity.this, "提示", "本商圈找到了" + photoUrls.size() + "张照片，是否要提交？", "取消提交", "确认提交", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDialog.dismiss();
                        }
                    }, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //提交照片
                            mDialog.dismiss();
                            optItemImg(photoUrls);
                        }
                    });
                    mDialog.show();
                }
            }
        });
        addSubscription(subscription);
    }


    private void optItemImg(final List<PhotoUrl> list) {
        mImageUpLoadDialog.show(list.size());
        ImageUploadTool.getInstance().upLoadImages(list, new ImageUploadTool.UploadFinishListener() {
            @Override
            public void onUploadFinish(final List<PhotoUrl> failureImages) {
                mImageUpLoadDialog.dismiss();
                if (!failureImages.isEmpty()) {
                    String str = "您当前上传的照片出现" + failureImages.size() + "张失败,点击确定提交失败的照片";
                    mDialog = UIUtils.getAlertDialog(UploadTaskPhotoActivity.this, null, str, null, "确定", null, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDialog.dismiss();
                            optItemImg(failureImages);
                        }
                    });
                    mDialog.setCancelable(false);
                    mDialog.show();
                } else {
                    showTest("提交照片成功！");
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


    private void loadMyTaskFromNet() {
        mLoadingDialog.show();
        Subscription subscription = wrapObserverWithHttp(WorkService.getWorkService().getOnlineChecking(new UserInfoRequest(SPUtils.getUserName()))).subscribe(new Subscriber<WorkListResult>() {
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
            public void onNext(WorkListResult result) {

                if (result.getRetCode() == 0) {
                    loadTaskDataFromDataBase(result.getData());
                } else {
                    mLoadingDialog.dismiss();
                    showTest(result.getMsg());
                }
            }
        });
        addSubscription(subscription);
    }


    /**
     * 从数据库加载任务数据,归并显示
     */
    private void loadTaskDataFromDataBase(final List<WorkListResult.DataBean> list) {


        Observable<List<NmpReportData>> observable = Observable.create(new Observable.OnSubscribe<List<NmpReportData>>() {
            @Override
            public void call(Subscriber<? super List<NmpReportData>> subscriber) {

                List<NmpReportData> dataList = DataSupport.order("id DESC").find(NmpReportData.class);

                if (dataList != null && !dataList.isEmpty()) {
                    Map<String, NmpReportData> map = new HashMap<String, NmpReportData>();
                    for (NmpReportData data : dataList) {
                        map.put(data.getAreaCode(), data);
                    }
                    List<NmpReportData> commitedDataList = new ArrayList<NmpReportData>();
                    for (WorkListResult.DataBean dataBean : list) {
                        NmpReportData data = map.get(dataBean.getAreaCode());
                        if (data != null) {
                            commitedDataList.add(data);
                        }
                    }
                    subscriber.onNext(commitedDataList);
                } else {
                    subscriber.onNext(null);
                }
                subscriber.onCompleted();
            }
        });


        Subscription subscription = wrapObserverWithHttp(observable).subscribe(new Subscriber<List<NmpReportData>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                mLoadingDialog.dismiss();
                e.printStackTrace();
                showTest("获取本地数据错误！");
            }

            @Override
            public void onNext(List<NmpReportData> nmpReportDataList) {
                mLoadingDialog.dismiss();
                if (nmpReportDataList != null) {
                    mAdapter.setData(nmpReportDataList);
                }
            }
        });


        addSubscription(subscription);

    }


    @Override
    public int createSuccessView() {
        return R.layout.activity_upload_task_photo;
    }


}
