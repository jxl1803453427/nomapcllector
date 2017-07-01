package com.example.signalcollection.fragment;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.example.signalcollection.Constans;
import com.example.signalcollection.R;
import com.example.signalcollection.activity.BaseActivity;
import com.example.signalcollection.activity.CollectionActivity;
import com.example.signalcollection.activity.MapActivity;
import com.example.signalcollection.activity.MyTaskActivity;
import com.example.signalcollection.bean.BaseStationBean;
import com.example.signalcollection.bean.DefaultResult;
import com.example.signalcollection.bean.MagneticBean;
import com.example.signalcollection.bean.MessageBean;
import com.example.signalcollection.bean.NmpReportData;
import com.example.signalcollection.bean.NmpReportPoint;
import com.example.signalcollection.bean.PhotoBean;
import com.example.signalcollection.bean.PhotoUrl;
import com.example.signalcollection.bean.StatusResult;
import com.example.signalcollection.bean.UserInfoRequest;
import com.example.signalcollection.bean.WifiSignalItem;
import com.example.signalcollection.bean.WorkListResult;
import com.example.signalcollection.network.WorkService;
import com.example.signalcollection.qinniu.ImageUploadTool;
import com.example.signalcollection.recyclerview.CommonAdapter;
import com.example.signalcollection.recyclerview.OnItemClickListener;
import com.example.signalcollection.recyclerview.ViewHolder;
import com.example.signalcollection.util.SPUtils;
import com.example.signalcollection.util.UIUtils;
import com.example.signalcollection.view.ImageUpLoadDialog;
import com.example.signalcollection.view.LoadingDialog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
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


public class OverCollectFragment extends BaseFragment {

    @BindView(R.id.list)
    RecyclerView mList;

    private CommonAdapter<NmpReportData> mCommonAdapter;
    private List<NmpReportData> mLt;
    private LoadingDialog mLoadingDialog;
    private Gson mGson;
    private List<PhotoUrl> mLtPhotoUrl;
    private Dialog mAlertDialog;
    private boolean mCanUploadData = true;//能不能提交数据的标志
    private MsgReceiver mMsgReciver;
    private NmpReportData mCurrentNmpReportData;
    private ImageUpLoadDialog mImageUpLoadDialog;

    /**
     * 显示有照片没找到
     *
     * @param areaPhotoCount      审核照片应有计数
     * @param realAreaPhotoCount  审核照片实际存在数
     * @param pointPhotoCount     采集点照片应有数
     * @param realPointPhotoCount 采集点照片实际存在数
     */
    private void showPhotoNotFound(int areaPhotoCount, int realAreaPhotoCount, int pointPhotoCount, int realPointPhotoCount) {

        mAlertDialog = UIUtils.getAlertDialog(getActivity(), "提示", "该商圈共有" + areaPhotoCount + "张审核照片，实际检查有" + realAreaPhotoCount + "张照片" + "采集点出现了" + pointPhotoCount + "张照片，实际检查有" + realPointPhotoCount + "张照片，有的照片找不到，请确定是否要提交任务？", "取消", "确定", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlertDialog.dismiss();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitTask(mCurrentNmpReportData);
            }
        });
        mAlertDialog.show();
    }


    private class MsgReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(Constans.MASSAGE_IN) && intent.getIntExtra("type", 0) == 1) {
                MessageBean bean = (MessageBean) intent.getSerializableExtra("message");
                for (NmpReportData data : mLt) {
                    if (data.getAreaCode().equals(bean.getAreaCode())) {
                        data.setStatus(0);
                        mCommonAdapter.notifyDataSetChanged();
                    }
                }

            }
        }
    }


    @Override
    public int createSuccessView() {
        return R.layout.fragment_over_collect;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLoadingDialog = new LoadingDialog(getActivity());
        mImageUpLoadDialog = new ImageUpLoadDialog(getActivity());
        EventBus.getDefault().register(this);
    }


    @Subscribe
    public void update(NmpReportData nmpReportData) {
        //数据太多这里查询好慢是个问题，这里只是为了可以显示数字
        //在列表里找到该数据
        for (NmpReportData data : mLt) {
            if (data.getAreaCode().equals(nmpReportData.getAreaCode())) {
                data.setPointSize(nmpReportData.getPointSize());
                mCommonAdapter.notifyDataSetChanged();
                break;
            }
        }
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMsgReciver = new MsgReceiver();
        mGson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        IntentFilter intentFilter = new IntentFilter(Constans.MASSAGE_IN);
        getActivity().registerReceiver(mMsgReciver, intentFilter);

        mCommonAdapter = new CommonAdapter<NmpReportData>(getActivity(), R.layout.rvitem_collecting, mLt) {
            @Override
            public void convert(final ViewHolder holder, final NmpReportData o) {
                holder.setText(R.id.tvName, o.getAreaName());
                holder.setText(R.id.tvInfo, String.format("已采集点数%d个", o.getPointSize()));
                holder.setText(R.id.tvAddress, o.getAddress());
                //旁边添加的提交按钮，可以直接new OnClickListener ？待看代码
                holder.setOnClickListener(R.id.tvSubmitSingle, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //任务提交
                        mAlertDialog = UIUtils.getAlertDialog(getActivity(), "重要提示", "请确认你最新采集的数据是否已经完全提交?\n任务提交后，你将不能进行数据提交", "取消任务提交", "确定任务提交", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mAlertDialog.dismiss();
                            }
                        }, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mAlertDialog.dismiss();
                                //任务提交的响应按钮！
                                //先查找照片
                                mCurrentNmpReportData = o;
                                findImage4Upload(o);
                            }
                        });
                        mAlertDialog.show();
                    }
                });

                if (o.getStatus() == 0) {
                    holder.getView(R.id.tvDataSubmit).setEnabled(true);
                    holder.getView(R.id.tvSubmitSingle).setEnabled(true);
                    holder.setBackgroundRes(R.id.tvSubmitSingle, R.drawable.selector_blue_round_rect);
                    holder.setBackgroundRes(R.id.tvDataSubmit, R.drawable.selector_blue_round_rect);
                    holder.setOnClickListener(R.id.tvDataSubmit, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //数据提交
                            if (mCanUploadData) {
                                mCanUploadData = false;
                                findDataAndSubmit(o);
                            }
                        }
                    });
                } else {
                    holder.getView(R.id.tvDataSubmit).setEnabled(false);
                    holder.getView(R.id.tvSubmitSingle).setEnabled(false);
                    holder.setBackgroundRes(R.id.tvDataSubmit, R.drawable.shape_gray_round_rect);
                    holder.setBackgroundRes(R.id.tvSubmitSingle, R.drawable.shape_gray_round_rect);
                }

                holder.setOnClickListener(R.id.tvShowMap, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), MapActivity.class);
                        intent.putExtra(Constans.CITY, o.getCity());
                        intent.putExtra(Constans.KEYWORD, o.getAreaName());
                        intent.putExtra(Constans.REGION, o.getRegion());
                        intent.putExtra(Constans.STREET, o.getStreet());
                        startActivity(intent);
                    }
                });
            }
        };
        mList.setAdapter(mCommonAdapter);
        mCommonAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(ViewGroup parent, View view, Object o, int position, ViewHolder viewHolder) {
                Intent intent = new Intent(getActivity(), CollectionActivity.class);
                intent.putExtra(Constans.POINT_COUNT, ((NmpReportData) o).getPointSize());
                intent.putExtra(Constans.TASK_ID, ((NmpReportData) o).getId());
                getActivity().startActivity(intent);
            }

            @Override
            public boolean onItemLongClick(ViewGroup parent, View view, Object o, int position, final ViewHolder viewHolder) {


                /*final View dataSubmitView = view.findViewById(R.id.tvDataSubmit);
                final View submitView = view.findViewById(R.id.tvSubmitSingle);
                if (!dataSubmitView.isEnabled() && !submitView.isEnabled()) {
                    //弹出自助激活对话框
                    final NmpReportData data = (NmpReportData) o;
                    mAlertDialog = UIUtils.getAlertDialog(getActivity(), "自助激活提示", "你将要激活任务【" + data.getAreaName() + "】的[数据提交]和[任务提交]的按钮，请一定要预先联系并告知我们的工作人员，在工作人员查看后台数据无误并允许后才能自助激活，否则因激活造成的数据错误或者异常的，后果自负", "先不激活", "我要激活", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mAlertDialog.dismiss();
                        }
                    }, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dataSubmitView.setEnabled(true);
                            submitView.setEnabled(true);
                            viewHolder.setBackgroundRes(R.id.tvSubmitSingle, R.drawable.selector_blue_round_rect);
                            viewHolder.setBackgroundRes(R.id.tvDataSubmit, R.drawable.selector_blue_round_rect);
                            data.setStatus(0);
                            if (data.getId() != 0) {
                                data.update(data.getId());
                            }
                            mAlertDialog.dismiss();
                        }
                    });
                    mAlertDialog.setCancelable(false);
                    mAlertDialog.show();
                    return true;
                }*/

                return false;
            }
        });
    }


    /**
     * 数据提交
     *
     * @param nmpReportData nmpReportData
     */
    private void uploadData(final NmpReportData nmpReportData) {


        Subscription sbMyAccount = wrapObserverWithHttp(WorkService.getWorkService().uploadData(nmpReportData)).subscribe(new Subscriber<DefaultResult>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                mCanUploadData = true;
                mLoadingDialog.dismiss();
                showTest(mNetWorkError);
            }

            @Override
            public void onNext(DefaultResult loginResult) {
                mCanUploadData = true;
                mLoadingDialog.dismiss();
                if (loginResult.getRetCode() == 0) {
                    ContentValues values = new ContentValues(1);
                    values.put("status", 1);
                    int i = DataSupport.updateAll(NmpReportData.class, values, "areacode = ?", nmpReportData.getAreaCode());
                    Logger.i("更新了" + i + "条数据");
                    updateData(nmpReportData.getAreaCode());
                    mAlertDialog = UIUtils.getAlertDialog((Context) getActivity(), "提示", "您已经成功提交数据给服务器，请等待后台处理，后台处理完成，会返回数据处理结果在消息界面，请留意！", "确定", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mAlertDialog.dismiss();
                        }
                    });
                    mAlertDialog.setCancelable(false);
                    mAlertDialog.show();
                } else {
                    Logger.i("出错的信息：" + loginResult.getMsg());
                    showTest(loginResult.getMsg());
                }
            }
        });
        getMyTaskActivity().addSubscription(sbMyAccount);
    }


    private void updateData(String areaCode) {
        for (NmpReportData data : mLt) {
            if (data.getAreaCode().equals(areaCode)) {
                data.setStatus(1);
                mCommonAdapter.notifyDataSetChanged();
                break;
            }
        }
    }


    /**
     * 查找数据并提交
     *
     * @param nmpReportData 本地数据
     */
    private void findDataAndSubmit(final NmpReportData nmpReportData) {

        mLoadingDialog.show("正在提交数据…");

        Observable<NmpReportData> observable = Observable.create(new Observable.OnSubscribe<NmpReportData>() {
            @Override
            public void call(Subscriber<? super NmpReportData> subscriber) {

                NmpReportData data;
                if (nmpReportData.getId() != 0) {
                    data = DataSupport.find(NmpReportData.class, nmpReportData.getId(), true);
                } else {
                    data = DataSupport.where("areacode = ?", nmpReportData.getAreaCode()).findFirst(NmpReportData.class, true);
                }
                //照片的url
                List<PhotoUrl> photoUrls = DataSupport.where("nid = ? AND phototype =?", String.valueOf(data.getId()), String.valueOf(Constans.PHOTO_TYPE_AREA)).find(PhotoUrl.class);
                data.setAreaPhotos(photoUrls);
                List<NmpReportPoint> list = new ArrayList<>();
                if (data.getPoints() != null && !data.getPoints().isEmpty()) {
                    Type expropType = new TypeToken<ArrayList<NmpReportPoint.Exprop>>() {
                    }.getType();
                    for (NmpReportPoint nmpReportPoint : data.getPoints()) {
                        if (nmpReportPoint.getRemark() != 1) {
                            //nmpReportPoint.setPhotoUrls(mGson.<List<String>>fromJson(nmpReportPoint.getStrPhotoUrls(), stringType));
                            List<PhotoUrl> pointPhotoUrlList = DataSupport.where("nmpreportpoint_id =" + nmpReportPoint.getId()).find(PhotoUrl.class);
                            nmpReportPoint.setPhotoList(pointPhotoUrlList);
                            nmpReportPoint.setExPropList(mGson.<List<NmpReportPoint.Exprop>>fromJson(nmpReportPoint.getStrExprop(), expropType));
                            nmpReportPoint.setSignals(DataSupport.where("nmpreportpoint_id = ?", String.valueOf(nmpReportPoint.getId())).find(WifiSignalItem.class));
                            nmpReportPoint.setBaseStations(DataSupport.where("nmpreportpoint_id = ?", String.valueOf(nmpReportPoint.getId())).find(BaseStationBean.class));
                            nmpReportPoint.setMagnetic(DataSupport.where("nmpreportpoint_id = ?", String.valueOf(nmpReportPoint.getId())).findFirst(MagneticBean.class));
                            //nmpReportPoint.setBleSignals(DataSupport.where("nmpreportpoint_id = ?", String.valueOf(nmpReportPoint.getId())).find(BleSignal.class));
                            list.add(nmpReportPoint);
                        }
                    }
                }
                data.setPoints(list);
                data.setUserName(SPUtils.getUserName());
                subscriber.onNext(data);
                subscriber.onCompleted();
            }
        });


        Subscription subscription = wrapObserverWithHttp(observable).subscribe(new Subscriber<NmpReportData>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                mLoadingDialog.dismiss();
                showTest("查找数据出错");
            }

            @Override
            public void onNext(NmpReportData nmpReportData) {
                if (nmpReportData.getPoints() != null && !nmpReportData.getPoints().isEmpty()) {
                    //提交数据
                    //mLoadingDialog.dismiss();
                    Logger.e(mGson.toJson(nmpReportData));

                    //mCanUploadData = true;
                    uploadData(nmpReportData);
                } else {
                    mLoadingDialog.dismiss();
                    mCanUploadData = true;
                    showTest("采集点为空，请先采集数据后才能提交");
                }
            }
        });
        getMyTaskActivity().addSubscription(subscription);

    }


    /**
     * 向数据库查找照片用于提交
     *
     * @param nmpReportData nmpReportData
     */
    private void findImage4Upload(final NmpReportData nmpReportData) {

        mLoadingDialog.show("正在查找照片……");
        Observable<PhotoBean> observable = Observable.create(new Observable.OnSubscribe<PhotoBean>() {
            @Override
            public void call(Subscriber<? super PhotoBean> subscriber) {
                List<PhotoUrl> pointPhotolist = new ArrayList<PhotoUrl>();
                Type type = new TypeToken<ArrayList<String>>() {
                }.getType();
                NmpReportData data = DataSupport.where("areacode = ?", nmpReportData.getAreaCode()).findFirst(NmpReportData.class);

                //找出商圈的审核照片
                List<PhotoUrl> areaPhotoUrls = DataSupport.where("nid = ? AND phototype =?", String.valueOf(data.getId()), String.valueOf(Constans.PHOTO_TYPE_AREA)).find(PhotoUrl.class);
                List<PhotoUrl> realAreaPhotoList = new ArrayList<PhotoUrl>();
                for (PhotoUrl photoUrl : areaPhotoUrls) {
                    File file;
                    if (!TextUtils.isEmpty(photoUrl.getImgLocalUrl()) && (file = new File(photoUrl.getImgLocalUrl())).exists() && file.length() > 0) {
                        realAreaPhotoList.add(photoUrl);
                    }
                }

                List<NmpReportPoint> points = DataSupport.where("nmpreportdata_id = ? AND remark <> ?", data.getId() + "", "1").find(NmpReportPoint.class);
                //根据点找出所有的图片
                if (points != null && !points.isEmpty()) {
                    for (NmpReportPoint point : points) {
                        //通过ID去找
                        List<PhotoUrl> photoUrls = DataSupport.where("nmpreportpoint_id = " + point.getId()).find(PhotoUrl.class);
                        if (photoUrls != null && !photoUrls.isEmpty()) {
                            pointPhotolist.addAll(photoUrls);
                        }
                    }
                }
                List<PhotoUrl> realPointPhotoUrls = new ArrayList<PhotoUrl>();
                //判断这些照片在不在
                for (PhotoUrl url : pointPhotolist) {
                    String path = url.getImgLocalUrl();
                    if (!TextUtils.isEmpty(path)) {
                        File file = new File(path);
                        if (file.exists() && file.length() > 0) {
                            realPointPhotoUrls.add(url);
                        }
                    }
                }
                PhotoBean bean = new PhotoBean(pointPhotolist.size(), realPointPhotoUrls, areaPhotoUrls.size(), realAreaPhotoList);
                subscriber.onNext(bean);
                subscriber.onCompleted();
            }
        });


        Subscription subscription = wrapObserverWithHttp(observable).subscribe(new Subscriber<PhotoBean>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                mLoadingDialog.dismiss();
            }

            @Override
            public void onNext(PhotoBean photoBean) {
                mLoadingDialog.dismiss();

                mLtPhotoUrl = new ArrayList<PhotoUrl>();
                mLtPhotoUrl.addAll(photoBean.getAreaPhotos());
                mLtPhotoUrl.addAll(photoBean.getPointPhotos());
                if (photoBean.getPointPhotoCount() != photoBean.getPointPhotos().size() || photoBean.getAreaPhotoCount() != photoBean.getAreaPhotos().size()) {
                    showPhotoNotFound(photoBean.getAreaPhotoCount(), photoBean.getAreaPhotos().size(), photoBean.getPointPhotoCount(), photoBean.getPointPhotos().size());
                } else {
                    submitTask(mCurrentNmpReportData);
                }
            }
        });

        getMyTaskActivity().addSubscription(subscription);

    }


    /**
     * 任务完成后提交照片数据
     */
    private void optItemImg(final List<PhotoUrl> list) {
        mImageUpLoadDialog.show(list.size());
        ImageUploadTool.getInstance().upLoadImages(list, new ImageUploadTool.UploadFinishListener() {
            @Override
            public void onUploadFinish(final List<PhotoUrl> failureImages) {
                mImageUpLoadDialog.dismiss();
                if (!failureImages.isEmpty()) {
                    String str = "您当前上传的照片出现" + failureImages.size() + "张失败,点击确定提交失败的照片";
                    mAlertDialog = UIUtils.getAlertDialog(getActivity(), null, str, null, "确定", null, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mAlertDialog.dismiss();
                            optItemImg(failureImages);
                        }
                    });
                    mAlertDialog.setCancelable(false);
                    mAlertDialog.show();
                } else {
                    showTest("提交任务成功！");
                    loadFromNetWork();
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


    /**
     * 任务提交，只提交商编
     *
     * @param nmpReportData nmpReportData
     */
    private void submitTask(final NmpReportData nmpReportData) {
        Map<String, String> map = new HashMap<>();
        map.put("areaCode", nmpReportData.getAreaCode());
        map.put("userName", SPUtils.getUserName());
        mLoadingDialog.show("正在提交任务…");
        Subscription sbMyAccount = wrapObserverWithHttp(WorkService.getWorkService().submit(map)).subscribe(new Subscriber<DefaultResult>() {
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
            public void onNext(DefaultResult loginResult) {
                mLoadingDialog.dismiss();
                if (loginResult.getRetCode() == 0) {
                    //提交照片
                    if (mLtPhotoUrl == null || mLtPhotoUrl.isEmpty()) {
                        showTest("提交任务成功");
                        loadFromNetWork();
                    } else {
                        //提交照片
                        optItemImg(mLtPhotoUrl);
                    }

                } else if (loginResult.getRetCode() == 952) {
                    mAlertDialog = UIUtils.getAlertDialog(getActivity(), null, loginResult.getMsg(), null, "确定", null, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mAlertDialog.dismiss();
                        }
                    });
                    mAlertDialog.show();
                } else {
                    Logger.i("出错的信息：" + loginResult.getMsg());
                    showTest(loginResult.getMsg());
                }
            }
        });
        ((MyTaskActivity) getActivity()).addSubscription(sbMyAccount);
    }

    /**
     * 获取自己领取的任务
     */
    public void loadFromNetWork() {

        mLoadingDialog.show();
        UserInfoRequest userInfoRequest = new UserInfoRequest();
        String userName = SPUtils.getUserName();
        Logger.i("user name " + userName);
        userInfoRequest.setUserName(SPUtils.getUserName());
        Subscription sbMyAccount = wrapObserverWithHttp(WorkService.getWorkService().get(userInfoRequest)).subscribe(new Subscriber<WorkListResult>() {
            @Override
            public void onCompleted() {
                mLoadingDialog.dismiss();
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                mLoadingDialog.dismiss();
                showTest(mNetWorkError);
            }

            @Override
            public void onNext(WorkListResult workListResult) {

                if (workListResult.getRetCode() == 0) {
                    getDataForShow(workListResult.getData());
                } else {
                    showTest(workListResult.getMsg());
                }

            }
        });
        getMyTaskActivity().addSubscription(sbMyAccount);
    }


    /**
     * 从数据库中查找对应的任务归并显示
     *
     * @param dataBeanList 在线的列表
     */
    private void getDataForShow(final List<WorkListResult.DataBean> dataBeanList) {

        Observable<List<NmpReportData>> observable = Observable.create(new Observable.OnSubscribe<List<NmpReportData>>() {
            @Override
            public void call(Subscriber<? super List<NmpReportData>> subscriber) {

                List<NmpReportData> datas = new ArrayList<NmpReportData>();
                for (WorkListResult.DataBean dataBean : dataBeanList) {
                    NmpReportData nmpReportData = DataSupport.where("areacode = ?", dataBean.getAreaCode()).findFirst(NmpReportData.class);
                    if (nmpReportData != null) {
                        int size = DataSupport.where("nmpreportdata_id = ? AND remark <> ?", nmpReportData.getId() + "", "1").count(NmpReportPoint.class);
                        nmpReportData.setPointSize(size);
                    } else {
                        nmpReportData = new NmpReportData();
                        nmpReportData.setPointSize(0);
                        nmpReportData.setRefAreaTypeCode(dataBean.getRefAreaTypeCode());
                        nmpReportData.setAreaCode(dataBean.getAreaCode());
                        nmpReportData.setAreaName(dataBean.getAreaName());
                        nmpReportData.setAreaTypeName(dataBean.getAreaTypeName());
                        nmpReportData.save();
                    }
                    nmpReportData.setRegion(dataBean.getCityRegion());
                    nmpReportData.setStreet(dataBean.getCityStress());
                    nmpReportData.setCity(dataBean.getCityName());
                    String addr = (TextUtils.isEmpty(dataBean.getCityStress()) ? "" : dataBean.getCityStress()) + (TextUtils.isEmpty(dataBean.getAddress()) ? "" : dataBean.getAddress());
                    nmpReportData.setAddress(dataBean.getCityName() + dataBean.getCityRegion() + addr);
                    datas.add(nmpReportData);
                }
                subscriber.onNext(datas);
                subscriber.onCompleted();
            }
        });

        Subscription subscription = wrapObserverWithHttp(observable).subscribe(new Subscriber<List<NmpReportData>>() {
            @Override
            public void onCompleted() {
                mLoadingDialog.dismiss();
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                mLoadingDialog.dismiss();
            }

            @Override
            public void onNext(List<NmpReportData> nmpReportDataList) {
                mLt = nmpReportDataList;
                mCommonAdapter.setData(mLt);
            }
        });
        getMyTaskActivity().addSubscription(subscription);
    }


    public void getTaskStatus() {

        mLoadingDialog.show("正在查询任务状态……");
        List<String> areaCodes = new ArrayList<>();
        for (NmpReportData data : mLt) {

            if (data.getStatus() == 1) {
                areaCodes.add(data.getAreaCode());
            }

        }

        if (areaCodes.isEmpty()) {
            showTest("没有正在提交的任务…");
            mLoadingDialog.dismiss();
            return;
        }

        Map<String, List<String>> map = new HashMap<>();
        map.put("areaCodes", areaCodes);

        Subscription s = wrapObserverWithHttp(WorkService.getWorkService().getDataSubmitStatus(map)).subscribe(new Subscriber<StatusResult>() {
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
            public void onNext(StatusResult result) {
                mLoadingDialog.dismiss();
                if (result.getRetCode() == 0) {

                    List<StatusResult.Status> status = result.getData();
                    if (status != null && !status.isEmpty()) {
                        updateStatus(status);
                    }

                } else {
                    showTest(result.getMsg());
                }
            }
        });

        ((BaseActivity) getActivity()).addSubscription(s);
    }


    private void updateStatus(List<StatusResult.Status> statuses) {


        Map<String, StatusResult.Status> map = new HashMap<>(statuses.size());

        for (StatusResult.Status status : statuses) {
            map.put(status.getAreaCode(), status);
        }
        for (NmpReportData data : mLt) {
            StatusResult.Status status = map.get(data.getAreaCode());

            if (status != null && data.getStatus() == 1 && status.getStatus() == 1) {
                ContentValues contentValues = new ContentValues();
                contentValues.put("status", 0);
                DataSupport.updateAll(NmpReportData.class, contentValues, "areacode = ?", data.getAreaCode());
                data.setStatus(0);
            }
        }


        mCommonAdapter.notifyDataSetChanged();
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getUserVisibleHint()) {
            //有时候可能dialog还没初始化，这里延迟一点点启动
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadFromNetWork();
                }
            }, 100);
        }
    }


    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(mMsgReciver);
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }


    private MyTaskActivity getMyTaskActivity() {
        return (MyTaskActivity) getActivity();
    }

}
