package com.example.signalcollection.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.signalcollection.Constans;
import com.example.signalcollection.R;
import com.example.signalcollection.bean.AreaType;
import com.example.signalcollection.bean.BaseStationBean;
import com.example.signalcollection.bean.BleSignal;
import com.example.signalcollection.bean.CPResult;
import com.example.signalcollection.bean.EventBusMessage;
import com.example.signalcollection.bean.Exprop;
import com.example.signalcollection.bean.MagneticBean;
import com.example.signalcollection.bean.NmpReportData;
import com.example.signalcollection.bean.NmpReportPoint;
import com.example.signalcollection.bean.PhotoUrl;
import com.example.signalcollection.bean.Predefine;
import com.example.signalcollection.bean.PredefineRequest;
import com.example.signalcollection.bean.SearchResult;
import com.example.signalcollection.bean.SignalBean;
import com.example.signalcollection.bean.WifiSignalItem;
import com.example.signalcollection.network.RetrofitUtil;
import com.example.signalcollection.network.WorkService;
import com.example.signalcollection.recyclerview.SpinnerAdapter;
import com.example.signalcollection.util.ImageUtil;
import com.example.signalcollection.util.UIUtils;
import com.example.signalcollection.view.LoadingDialog;
import com.example.signalcollection.view.SearchBandDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.orhanobut.logger.Logger;
import com.yyc.mcollector.SignalCollector;
import com.yyc.mcollector.bean.BaseStationData;
import com.yyc.mcollector.bean.IBeaconData;
import com.yyc.mcollector.bean.MagneticData;
import com.yyc.mcollector.bean.WifiData;
import com.yyc.mcollector.listener.ScanResultListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.litepal.crud.DataSupport;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;

/**
 *
 */
public class ModifyHistoryActivity extends BaseActivity {

    @BindView(R.id.tvInfo)
    TextView mTvInfo;//显示采集点数字信息
    @BindView(R.id.tvName)
    TextView mTvName;//显示商圈名字
    @BindView(R.id.tvType)
    TextView mTvType;//显示商圈类型


    Spinner mSpCollectType;//采集点类型选择,对话框没出，这里为null
    @BindView(R.id.spFloor)
    Spinner mSpFloor; //楼层选择
    @BindView(R.id.iv1)
    ImageView mIv1; //照片1
    @BindView(R.id.iv2)
    ImageView mIv2; //照片2
    @BindView(R.id.layout_exprop)
    LinearLayout mLayoutExprop; //扩展属性
    @BindView(R.id.tv_point_type)
    TextView tvPointType; //采集点类型

    @BindView(R.id.tv_in)
    TextView tvIn;//店内采集
    @BindView(R.id.tv_out)
    TextView tvOut;//店外采集

    @BindView(R.id.btCollect)
    TextView tvCollect;//开始采集按钮

    @BindView(R.id.btNext)
    TextView tvSave;//开始采集按钮

    @BindView(R.id.fl_in)
    View flIn;//店内店外采集按钮的父View

    private HashMap<String, Exprop> mMapExprop = new HashMap<>();//采集点类型对应的扩展属性
    private Map<String, NmpReportPoint.Exprop> mMapPointExprop = new HashMap<>(); //采集点扩展属性键值对
    private Map<String, NmpReportPoint.Exprop> mPrimaryMapPointExprop; //采集点扩展属性键值对
    private HashMap<String, List<Predefine.Data>> mMapExpropPredefine = new HashMap<>();//扩展属性的预设值
    private HashMap<String, SpinnerAdapter> mMapExpropAdapter = new HashMap<>();//扩展属性预设值列表的适配器结合
    private SpinnerAdapter<CPResult.DataBean> mColltionTypeAdapter; //采集点类型适配器
    private SignalCollector mSignalCollector; //信号采集器
    private LoadingDialog mLoadingDialog;
    //private Integer mCurrentIndex;//当前点的索引从0开始
    private int mFloorNumber = 1;
    private List<Integer> mLtFloor = new ArrayList<>();//楼层的编号
    //private List<String> mLtPhotos = new ArrayList<>();//存放照片的,怎么区分店内拍照、店外拍照？
    //private List<String> mLtPhotosIn = new ArrayList<>();//存放店内照片的
    //private List<String> mLtPhotosOut = new ArrayList<>();//存放店外照片的
    private List<PhotoUrl> mPhotos = new ArrayList<>();
    private List<PhotoUrl> mPhotosIn = new ArrayList<>();
    private List<PhotoUrl> mPhotosOut = new ArrayList<>();

    private Dialog mSelectDialog;


    private String samePageCode;//标识同一页面内两个采集点相同的code
    /**
     * 2017-2-15更改要拍照的时候使用了拍照前采集的信号，不用手动采集的信号
     */
    //private SignalBean mClickSignalBean;//点击采集信号按钮时采集的信号
    private SignalBean mTakePicSignalBean;//点击拍照前采集的信号
    private SignalBean mShopInSignalBean;//点击拍照前采集的信号
    private SignalBean mShopOutSignalBean;//点击拍照前采集的信号


    //private boolean mIsTakePhotoCollectWifi;//是否是在拍照的时候采集了WiFi

    private boolean isShopInOut;//是否是店内外采集
    private boolean isShopIn;//是否是店内采集

    private String mCollectTypeName;//采集点类型
    private String mCollectTypeCode;//采集点类型的code
    private List<CPResult.DataBean> mCPs;//采集点类型的存储
    private NmpReportPoint mNmpReportPoint;//采集点数据
    private NmpReportPoint mNmpReportPointOut;//店铺的外点
    private NmpReportPoint mNmpReportPointIn;//店铺的内点
    private Dialog mAlertDialog;
    private int pointsize;//采集点数
    private Dialog mTypeDialog;
    private NmpReportData mNmpReportData;
    private ImageView mCurrentImageView;

    private SearchBandDialog mSearchBandDialog;
    private Gson mGson = new Gson();

    private boolean isReCollect;//重新采集，只要点可重新采集
    private boolean isReCollectIn;//店内重新采集
    private boolean isReCollectOut;//店外重新采集
    //private boolean isCollect;//点了采集按钮
    private String mPrimaryCollectTypeName;//初始的采集点类型


    //private int mPointChangeType;

    //private static final int POINT_NO_CHANGE = 0;//点没改
    //private static final int POINT_SHOP_CHANGE_OTHER = 1;
    //private static final int POINT_OTHER_CHANGE_SHOP = 2;


    private static final int SIGNAL_CHANGE_IN_SHOP_POINT = 3;
    private static final int SIGNAL_CHANGE_OUT_SHOP_POINT = 4;
    private static final int SIGNAL_CHANGE_OUT_IN_SHOP_POINT = 5;
    private static final int SIGNAL_CHANGE_GENERAL_POINT = 6;
    private static final int SIGNAL_NO_CHANGE_GENERAL_POINT = 7;
    private static final int SIGNAL_NO_CHANGE_OUT_IN_POINT = 8;
    private int mCurrentIndex;
    private boolean mNetworkErr = false;
    private CPResult.DataBean mCurrnetCp;


    @Subscribe
    public void recivedMessage(EventBusMessage message) {

        if (message.what == Constans.EVENBUS_MESSAGE_CODE_RECEIVED_IMG) {
            String path = message.tag;
            if (!TextUtils.isEmpty(path)) {
                //检查一下照片成不成功，
                File file = new File(path);
                if (!file.exists() || file.length() <= 0) {
                    showPhotoLostDialog();
                } else {
                    ImageLoader.getInstance().displayImage("file:/" + path, mCurrentImageView, ImageUtil.getInstance().getBaseDisplayOption());
                    String key = RetrofitUtil.PHOTO_BASE_URL + "cpphoto" + File.separator + mNmpReportData.getAreaCode() + "-" + pointsize++ + "-" + System.currentTimeMillis() + ".jpg";
                    PhotoUrl photoUrl = new PhotoUrl();
                    photoUrl.setPhotoUrl(key);
                    photoUrl.setImgLocalUrl(path);
                    photoUrl.setUpdateTime(System.currentTimeMillis());
                    photoUrl.setPhotoType(Constans.PHOTO_TYPE_POINT);
                    if (isShopInOut) {
                        if (isShopIn) {
                            //mLtPhotosIn.add(key);
                            mPhotosIn.add(photoUrl);
                        } else {
                            //mLtPhotosOut.add(key);
                            mPhotosOut.add(photoUrl);
                        }
                    } else {

                        mCurrentImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        //mLtPhotos.add(key);
                        mPhotos.add(photoUrl);
                    }
                }
            } else {

                //采了信号但是没拍照,
                Logger.i("I had not take photo");
                if (isShopInOut) {
                    if (isShopIn) {
                        mCurrentImageView.setImageResource(R.mipmap.ic_in_photo);
                        //mLtPhotosIn.clear();
                        mPhotosIn.clear();
                        mShopInSignalBean = null;
                    } else {
                        mCurrentImageView.setImageResource(R.mipmap.ic_out_photo);
                        //mLtPhotosOut.clear();
                        mPhotosOut.clear();
                        mShopOutSignalBean = null;
                    }
                } else {
                    mIv1.setImageResource(R.mipmap.ic_camera);
                    mIv2.setImageResource(R.mipmap.ic_camera);
                    mIv1.setScaleType(ImageView.ScaleType.CENTER);
                    //mIv2.setScaleType(ImageView.ScaleType.CENTER);
                    //mLtPhotos.clear();
                    mPhotos.clear();
                    mTakePicSignalBean = null;
                }

            }
        }
    }


    private int findLastIndex(int id) {

        //mPointSize = DataSupport.where("remark <> 1 AND nmpreportdata_id = " + mNmpReportData.getId()).count(NmpReportPoint.class)

        NmpReportPoint point = DataSupport.where("remark <> 1 AND nmpreportdata_id = " + id).findLast(NmpReportPoint.class);
        if (point != null) {
            int pointIndex = point.getPointIndex();
            return Math.abs(pointIndex % 10000);
        }

        return 0;
    }


    @Override
    public void init() {
        mLoadingDialog = new LoadingDialog(this);
        showBack();
        setMyTitle("修改采集历史");
        EventBus.getDefault().register(this);
        Intent intent = getIntent();
        int data_id = intent.getIntExtra(Constans.AREA_DATA_ID, 0);
        int point_id = intent.getIntExtra(Constans.POINT_DATA_ID, 0);
        pointsize = intent.getIntExtra(Constans.POINT_COUNT, 0);
        //mCurrentIndex = intent.getIntExtra(Constans.POINT_INDEX, 0);
        mCurrentIndex = findLastIndex(data_id);
        mNmpReportData = DataSupport.find(NmpReportData.class, data_id);
        NmpReportPoint nmpReportPoint = DataSupport.find(NmpReportPoint.class, point_id);
        mCollectTypeName = nmpReportPoint.getPointType();
        mCollectTypeCode = nmpReportPoint.getPointTypeCode();
        mPrimaryCollectTypeName = mCollectTypeName;
        //转换扩展属性list为map
        Type type = new TypeToken<ArrayList<NmpReportPoint.Exprop>>() {
        }.getType();
        List<NmpReportPoint.Exprop> list = mGson.fromJson(nmpReportPoint.getStrExprop(), type);
        exchangeExpropList2Map(list);
        mPrimaryMapPointExprop = mMapPointExprop;

        //如果是店铺类型就找出另外一个点
        String sameTypeCode = nmpReportPoint.getSamePageCode();
        if (!TextUtils.isEmpty(sameTypeCode)) {
            isShopInOut = true;
            flIn.setVisibility(View.VISIBLE);
            tvCollect.setVisibility(View.GONE);
            NmpReportPoint.Exprop exprop = mMapPointExprop.get("EXP-SOTRE_INOROUT");
            //先判断这个是店内点还是店外点
            if (exprop != null) {
                //当前点是店内点
                //一般采集的都是店外点
                isShopIn = exprop.getPropValue().equals("店内");
            }
            //找出另外一个点,就只有两个点
            NmpReportPoint point = DataSupport.where("samepagecode = ? AND id <> ?", sameTypeCode, point_id + "").findFirst(NmpReportPoint.class);//一般是两个，也只有两个
            if (isShopIn) {
                mNmpReportPointIn = nmpReportPoint;
                mNmpReportPointOut = point;
            } else {
                mNmpReportPointIn = point;
                mNmpReportPointOut = nmpReportPoint;
            }
            mNmpReportPoint = mNmpReportPointIn;
        } else {
            isShopInOut = false;
            mNmpReportPoint = nmpReportPoint;
        }


        if (isShopInOut && mNmpReportPointOut != null) {
            mPhotosIn = DataSupport.where("nmpreportpoint_id = " + mNmpReportPointIn.getId()).find(PhotoUrl.class);
            mPhotosOut = DataSupport.where("nmpreportpoint_id = " + mNmpReportPointOut.getId()).find(PhotoUrl.class);
        } else {
            mPhotos = DataSupport.where("nmpreportpoint_id = " + mNmpReportPoint.getId()).find(PhotoUrl.class);
        }

        //恢复照片的显示
        //添加商圈的时候会显示这里
        mTvInfo.setText(String.format(mTvInfo.getText().toString(), nmpReportPoint.getPointIndex()));
        //初始化信号采集器只保证WiFi信号
        mSignalCollector = new SignalCollector(this, new ScanResultListener() {
            @Override
            public void onResult(List<WifiData> wifiResult, List<BaseStationData> baseStationDataList, /*List<IBeaconData> bluetoothData,*/ MagneticData magneticData) {
                mLoadingDialog.dismiss();
                SignalBean bean = new SignalBean(wifiResult, baseStationDataList, magneticData, System.currentTimeMillis()/*, bluetoothData*/);
                //isReCollect = true;//跑过这里都是重新采集的
                samePageCode = String.valueOf(System.currentTimeMillis());
                //点拍照按钮进来的
                if (isShopInOut) {
                    if (isShopIn) {
                        mShopInSignalBean = bean;
                    } else {
                        mShopOutSignalBean = bean;
                    }
                } else {
                    mTakePicSignalBean = bean;
                }
                //启动相机去拍照
                baseStartActivity(CropImageActivity.class);
            }

            @Override
            public void onError(int i) {

            }
        });

        //初始化点属性的adapter
        mColltionTypeAdapter = new SpinnerAdapter<CPResult.DataBean>(this) {
            @Override
            public void setText(TextView textView, CPResult.DataBean dataBean) {
                textView.setText(dataBean.getCpTypeName());
            }
        };

        //初始化楼层的adapter
        for (int i = -5; i <= 100; i++) {
            if (i == 0) {
                continue;
            }
            mLtFloor.add(i);
        }

        SpinnerAdapter<Integer> adapterFloor = new SpinnerAdapter<Integer>(this) {
            @Override
            public void setText(TextView textView, Integer floorNumber) {
                textView.setText(String.valueOf(floorNumber));
            }
        };
        adapterFloor.setListData(mLtFloor);
        mSpFloor.setAdapter(adapterFloor);
        mFloorNumber = nmpReportPoint.getFloorNumber();
        mSpFloor.setSelection(mLtFloor.indexOf(mFloorNumber));
        mSpFloor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mFloorNumber = mLtFloor.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (mNmpReportData != null) {
            //这个东西为空的原因是拍照的时候有些手机内存不足这个Activity被系统强制回收了，恢复的时候没了，这里要检查一下是不是为null
            mTvName.setText(mNmpReportData.getAreaName());
            mTvType.setText(mNmpReportData.getAreaTypeName());
            loadCpTypeFromNet();
        } else {
            showTest("信息丢失，请退出本界面再进入采集！");
        }
        restorePhoto();
    }


    /**
     * 转换扩展属性List为map类型
     *
     * @param list 扩展属性列表
     */
    private void exchangeExpropList2Map(List<NmpReportPoint.Exprop> list) {

        if (list != null && !list.isEmpty()) {
            for (NmpReportPoint.Exprop exprop : list) {
                mMapPointExprop.put(exprop.getRefExPropCode(), exprop);
            }
        }
    }


    /**
     * 回显照片
     */
    private void restorePhoto() {
        int size;

        Logger.e("restore Phtoto ");
        if (isShopInOut) {

            //室内放在第二张
            if (mPhotosIn != null && !mPhotosIn.isEmpty()) {
                size = mPhotosIn.size();
                //PhotoUrl photoUrl1 = DataSupport.where("imgKey = ?", mLtPhotosIn.get(size - 1)).findFirst(PhotoUrl.class);
                ImageLoader.getInstance().displayImage("file:/" + mPhotosIn.get(size - 1).getImgLocalUrl(), mIv2);
            }

            if (mPhotosOut != null && !mPhotosOut.isEmpty()) {
                size = mPhotosOut.size();
                ImageLoader.getInstance().displayImage("file:/" + mPhotosOut.get(size - 1).getImgLocalUrl(), mIv1);
            }

        } else {
            ViewGroup.LayoutParams layoutParams = mIv1.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = UIUtils.dip2px(160);
            mIv1.setLayoutParams(layoutParams);
            mIv2.setVisibility(View.GONE);

            if (mPhotos != null && !mPhotos.isEmpty()) {
                size = mPhotos.size();
                mIv1.setScaleType(ImageView.ScaleType.CENTER_CROP);
                ImageLoader.getInstance().displayImage("file:/" + mPhotos.get(size - 1).getImgLocalUrl(), mIv1);
            }

        }
    }


    /**
     * 添加店内店外点的扩展属性
     *
     * @param isShopIn isShopIn
     */
    private void addShopInOutExprop(boolean isShopIn) {
        NmpReportPoint.Exprop exprop = new NmpReportPoint.Exprop();
        exprop.setRefExPropCode("EXP-SOTRE_INOROUT");
        if (isShopIn) {
            //exprop.setTagCode("EXP-INSTORE");
            exprop.setPropValue("店内");
        } else {
            //exprop.setTagCode("EXP-OUTSTORE");
            exprop.setPropValue("店外");
        }
        mMapPointExprop.put(exprop.getRefExPropCode(), exprop);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Logger.i("CollectionActivity  onSaveInstanceState");
        outState.putInt("pointSize", pointsize);
        //outState.putInt("mPointSize", mPointSize);
        //outState.putInt("pointIndex", mCurrentIndex);
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        //有很多数据需要从这里保存的
        if (null != savedInstanceState) {
            pointsize = savedInstanceState.getInt("pointSize");
            //mCurrentIndex = savedInstanceState.getInt("pointIndex");
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    /**
     * 多选框的对话框
     *
     * @param dataBean dataBean
     * @param mTv      dataBean
     * @return dataBean
     */
    private Dialog getCreateDialog(final Exprop.DataBean dataBean, final List<Predefine.Data> list, final TextView mTv) {

        Dialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请选择" + dataBean.getPropName());
        final String[] strs = new String[list.size()];
        //list.toArray(strs);
        if (list instanceof ArrayList) {
            for (int i = 0; i < list.size(); i++) {
                strs[i] = list.get(i).getTagName();
            }
        }

        final boolean[] flags = new boolean[strs.length];//初始复选情况
        final StringBuffer result = new StringBuffer();
        final StringBuffer resultCode = new StringBuffer();
        builder.setMultiChoiceItems(strs, flags, new DialogInterface.OnMultiChoiceClickListener() {
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                flags[which] = isChecked;
            }
        });

        //添加一个确定按钮
        builder.setPositiveButton(" 确 定 ", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                for (int i = 0; i < flags.length; i++) {
                    if (flags[i]) {
                        if (TextUtils.isEmpty(result)) {
                            result.append(strs[i]);
                            resultCode.append(list.get(i).getTagCode());
                        } else {
                            result.append(";").append(strs[i]);
                            resultCode.append(";").append(list.get(i).getTagCode());
                        }
                    }
                }
                mTv.setText(result);
                //mMapPointExprop.remove(dataBean.getPropCode());
                NmpReportPoint.Exprop exprop = new NmpReportPoint.Exprop();
                exprop.setPropValue(result.toString());
                exprop.setRefExPropCode(dataBean.getPropCode());
                exprop.setTagCode(resultCode.toString());
                mMapPointExprop.put(dataBean.getPropCode(), exprop);
            }
        });
        //创建一个复选框对话框
        dialog = builder.create();

        return dialog;
    }

    //加载采集点类型
    private void loadCpTypeFromNet() {
        mLoadingDialog.show();
        AreaType areaType = new AreaType();
        areaType.setAreaTypeCode(mNmpReportData.getRefAreaTypeCode());
        Subscription sbMyAccount = wrapObserverWithHttp(WorkService.getWorkService().getCp(areaType)).subscribe(new Subscriber<CPResult>() {
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
            public void onNext(CPResult cpResult) {
                mLoadingDialog.dismiss();
                if (cpResult.getRetCode() == 0) {
                    mCPs = cpResult.getData();
                    mColltionTypeAdapter.setListData(mCPs);
                    for (CPResult.DataBean dataBeen : mCPs) {
                        if (dataBeen.getCpTypeName().equals(mNmpReportPoint.getPointType())) {
                            mCurrnetCp = dataBeen;
                            loadExpropFromNet(dataBeen);
                            //mSpCollectType.setSelection(i);
                            //mTypeDialog.dismiss();
                            break;
                        }
                    }
                    //把采集点的数据恢复回去
                    tvPointType.setText(mNmpReportPoint.getPointType());
                } else {
                    showTest(cpResult.getMsg());
                }
            }
        });
        addSubscription(sbMyAccount);
    }


    /**
     * 从网络获取扩展属性
     *
     * @param dataBean dataBean
     */
    private void loadExpropFromNet(final CPResult.DataBean dataBean) {

        mLoadingDialog.show();
        Subscription sbMyAccount = wrapObserverWithHttp(WorkService.getWorkService().getExprop(dataBean)).subscribe(new Subscriber<Exprop>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                mNetworkErr = true;
                mLoadingDialog.dismiss();
                showTest(mNetWorkError);
            }

            @Override
            public void onNext(Exprop exprop) {
                mLoadingDialog.dismiss();
                if (exprop.getRetCode() == 0) {
                    mNetworkErr = false;
                    Logger.e("mNetworkErr ==false");
                    if (exprop.getData() != null && !exprop.getData().isEmpty()) {
                        mMapExprop.put(dataBean.getCpTypeCode(), exprop);
                        loadExprop2View(dataBean.getCpTypeCode(), exprop);
                    }

                } else {
                    mNetworkErr = true;
                    showTest(exprop.getMsg());
                }

            }
        });
        addSubscription(sbMyAccount);

    }

    /**
     * 加载扩展属性预设值
     *
     * @param textView         //textView textView
     * @param preDefineRequest //preDefineRequest
     */
    private void loadPredefineFromNet(final TextView textView, final PredefineRequest preDefineRequest) {
        mLoadingDialog.show();
        Subscription sbMyAccount = wrapObserverWithHttp(WorkService.getWorkService().getPredefine(preDefineRequest)).subscribe(new Subscriber<Predefine>() {
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
            public void onNext(Predefine predefine) {
                mLoadingDialog.dismiss();
                if (predefine.getRetCode() == 0) {
                    List<Predefine.Data> list = predefine.getData();
                    mMapExpropPredefine.put(preDefineRequest.getRefExPropCode(), list);
                    SpinnerAdapter<Predefine.Data> adapter = mMapExpropAdapter.get(preDefineRequest.getRefExPropCode());
                    if (adapter != null) {
                        adapter.setListData(list);
                    }
                } else {
                    showTest(predefine.getMsg());
                }
            }
        });
        addSubscription(sbMyAccount);
    }


    /**
     * 扩展属性的布局 exprop
     *
     * @param cpTypeCode cpTypeCode
     * @param exprop     exprop
     */
    public void loadExprop2View(final String cpTypeCode, final Exprop exprop) {
        for (final Exprop.DataBean dataBean : exprop.getData()) {
            View view = UIUtils.inflate(this, R.layout.item_exprop);
            final TextView mTv = (TextView) view.findViewById(R.id.tv);
            final TextView tvIn = (TextView) view.findViewById(R.id.tvIn);
            mTv.setTag(dataBean.getPropCode());
            mTv.setText(dataBean.getPropName() + "： ");
            LinearLayout.LayoutParams layoutParames = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParames.setMargins(0, UIUtils.dip2px(8), 0, 0);
            mLayoutExprop.addView(view, layoutParames);
            restoreHistoryValue(dataBean.getPropCode(), tvIn);
            if (dataBean.getControlType() == 2) {
                //下拉框
                final List<Predefine.Data> list = mMapExpropPredefine.get(dataBean.getPropCode());
                SpinnerAdapter<Predefine.Data> adapter = mMapExpropAdapter.get(dataBean.getPropCode());
                if (adapter == null) {
                    adapter = new SpinnerAdapter<Predefine.Data>(this) {
                        @Override
                        public void setText(TextView textView, Predefine.Data s) {
                            textView.setText(s.getTagName());
                        }
                    };
                    mMapExpropAdapter.put(dataBean.getPropCode(), adapter);
                }

                if (list != null && !list.isEmpty()) {
                    adapter.setListData(list);
                } else {
                    PredefineRequest predefineRequest = new PredefineRequest();
                    predefineRequest.setRefCpTypeCode(cpTypeCode);
                    predefineRequest.setRefExPropCode(dataBean.getPropCode());
                    loadPredefineFromNet(tvIn, predefineRequest);
                }
                final SpinnerAdapter adapter1 = adapter;
                tvIn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showSelectType("请选择" + dataBean.getPropName(), adapter1, new SelectTypeItemSelectedListener<NmpReportPoint.Exprop>() {
                            @Override
                            public void onItemSelect(NmpReportPoint.Exprop data) {

                                tvIn.setText(data.getPropValue());//这里是不是显示英文了？
                                data.setRefExPropCode(dataBean.getPropCode());
                                mMapPointExprop.put(dataBean.getPropCode(), data);
                            }
                        });
                    }
                });
            } else if (dataBean.getControlType() == 4) {
                //可多选的选择框
                //sp.setVisibility(View.GONE);
                List<Predefine.Data> list = mMapExpropPredefine.get(dataBean.getPropCode());
                if (list == null || list.isEmpty()) {
                    PredefineRequest predefineRequest = new PredefineRequest();
                    predefineRequest.setRefCpTypeCode(cpTypeCode);
                    predefineRequest.setRefExPropCode(dataBean.getPropCode());
                    loadPredefineFromNet(tvIn, predefineRequest);
                }

                tvIn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        List<Predefine.Data> list2 = mMapExpropPredefine.get(dataBean.getPropCode());
                        if (list2 != null && !list2.isEmpty()) {
                            getCreateDialog(dataBean, list2, tvIn).show();
                        } else {
                            PredefineRequest predefineRequest = new PredefineRequest();
                            predefineRequest.setRefCpTypeCode(cpTypeCode);
                            predefineRequest.setRefExPropCode(dataBean.getPropCode());
                            loadPredefineFromNet(tvIn, predefineRequest);
                        }
                    }
                });
            } else if (dataBean.getControlType() == 3) {
                //标签形式的选择框
                //sp.setVisibility(View.GONE);
                tvIn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UIUtils.getAlertDialogEdit("请输入" + dataBean.getPropName(), ModifyHistoryActivity.this, tvIn.getText().toString(), new UIUtils.EditResultInterface() {
                            @Override
                            public void editText(String text) {

                                if (TextUtils.isEmpty(text)) {
                                    tvIn.setText("");
                                    mMapPointExprop.remove(dataBean.getPropCode());
                                } else {
                                    tvIn.setText(text);
                                    NmpReportPoint.Exprop exprop1 = new NmpReportPoint.Exprop();
                                    exprop1.setRefExPropCode(dataBean.getPropCode());
                                    exprop1.setPropValue(text);
                                    exprop1.setIsOtherInput(1);
                                    mMapPointExprop.put(dataBean.getPropCode(), exprop1);
                                }
                            }
                        }).show();
                    }
                });
            } else if (dataBean.getControlType() == 5) {
                //搜索类型的选择框
                //sp.setVisibility(View.GONE);
                tvIn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //启动一个搜索的dialog
                        if (mSearchBandDialog == null) {
                            mSearchBandDialog = new SearchBandDialog(ModifyHistoryActivity.this);
                        }
                        mSearchBandDialog.show(dataBean.getPropName(), dataBean.getSearchApi(), new SearchBandDialog.OnItemSelectListener() {
                            @Override
                            public void onItemSelected(SearchResult.Data data) {
                                tvIn.setText(data.getShowName());
                                //现在是什么也不做
                                NmpReportPoint.Exprop exprop = new NmpReportPoint.Exprop();
                                exprop.setPropValue(data.getShowName());
                                exprop.setRefExPropCode(dataBean.getPropCode());
                                exprop.setTagCode(data.getBrandCode());
                                mMapPointExprop.put(dataBean.getPropCode(), exprop);
                                //品牌的这些扩展属性不需要了
                            }

                            @Override
                            public void onOtherClick() {
                                //添加品牌，请求获得添加品牌的扩展属性
                                UIUtils.getDialogEdit(true, "请输入品牌名", ModifyHistoryActivity.this, new UIUtils.EditResultInterface() {
                                    @Override
                                    public void editText(String text) {
                                        tvIn.setText(text);
                                        NmpReportPoint.Exprop exprop = new NmpReportPoint.Exprop();
                                        exprop.setPropValue(text);
                                        exprop.setRefExPropCode(dataBean.getPropCode());
                                        exprop.setIsOtherInput(1);
                                        mMapPointExprop.put(dataBean.getPropCode(), exprop);
                                    }
                                }).show();
                            }

                            @Override
                            public void onErrMsg(String msg) {
                                showTest(msg);
                            }
                        });
                    }
                });
            }

        }

    }


    /**
     * 恢复历史的值
     *
     * @param propCode propCode
     * @param tvValue  tvValue
     */
    private void restoreHistoryValue(String propCode, TextView tvValue) {
        NmpReportPoint.Exprop exprop = mMapPointExprop.get(propCode);
        if (exprop != null) {
            tvValue.setText(exprop.getPropValue());
        }
    }


    /**
     * 选择的回调，待优化
     */
    interface SelectTypeItemSelectedListener<T> {

        void onItemSelect(T data);
    }

    /**
     * 选择类型的对话框
     *
     * @param title    标题
     * @param adapter  单项选择适配器
     * @param listener 选择回调
     */
    private void showSelectType(final String title, @NonNull SpinnerAdapter<Predefine.Data> adapter, @NonNull final SelectTypeItemSelectedListener listener) {

        if (mSelectDialog == null) {
            mSelectDialog = new Dialog(this, R.style.Style_Dialog);
            mSelectDialog.setContentView(R.layout.dialog_collection_type);
            mSelectDialog.setCancelable(false);
        }

        TextView tvTitle = (TextView) mSelectDialog.findViewById(R.id.tvTitle);
        tvTitle.setText(title);
        TextView tvConfirm = (TextView) mSelectDialog.findViewById(R.id.tvConfirm);

        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectDialog.dismiss();
            }
        });
        Spinner spinner = (Spinner) mSelectDialog.findViewById(R.id.sp_point_type);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Predefine.Data data = (Predefine.Data) parent.getAdapter().getItem(position);
                final NmpReportPoint.Exprop exprop = new NmpReportPoint.Exprop();
                if (data.getNeedOtherInput() == 1) {
                    mSelectDialog.dismiss();
                    UIUtils.getDialogEdit(true, "请输入" + title.substring(3), ModifyHistoryActivity.this, new UIUtils.EditResultInterface() {
                        @Override
                        public void editText(String text) {
                            exprop.setPropValue(text);
                            exprop.setIsOtherInput(1);
                            exprop.setPropValue(text);
                            listener.onItemSelect(exprop);
                        }
                    }).show();
                    //选到了其他的要弄输入框
                } else {
                    exprop.setPropValue(data.getTagName());
                    exprop.setTagCode(data.getTagCode());
                    listener.onItemSelect(exprop);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mSelectDialog.show();
    }


    @OnClick({R.id.iv1, R.id.iv2, R.id.btCollect, R.id.btNext, R.id.tv_in, R.id.tv_out})
    public void click(View view) {
        switch (view.getId()) {
            case R.id.iv1:
                mCurrentImageView = (ImageView) view;


                if (isShopInOut) {
                    isShopIn = false;

//                    if (!isReCollectOut) {
//                        //第一次点这个按钮是要清掉以前的照片
//                        mPhotosOut.clear();
//                    }
                    isReCollectOut = true;
                } else {
//                    if (!isReCollect) {
//                        //第一次点这个按钮是要清掉以前的照片
//                        mPhotos.clear();
//                    }
                    isReCollect = true;
                }

                collectSignal("正在启动相机…");
                break;
            case R.id.iv2:
                if (isShopInOut) {
                    isShopIn = true;
//                    if (!isReCollectIn) {
//                        //第一次点这个按钮是要清掉以前的照片
//                        mPhotosIn.clear();
//                    }
                    isReCollectIn = true;
                } else {
                    isReCollect = true;
                }
                mCurrentImageView = (ImageView) view;
                collectSignal("正在启动相机…");
                break;
            case R.id.btNext:
                //总共两种保存，一种不需要采集，一种需要采集信号
                //1、点了采集按钮或者是拍照的都要重新采集
                //2、店铺转其他，其他转店铺的，需要重新采集
                if (mNetworkErr) {
                    showTest("由于网络问题，有扩展属性没加载出来，请等待扩展属性加载出来，填写扩展属性，再保存");

                    //这个要不要延迟一点点再加载？
                    tvCollect.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loadExpropFromNet(mCurrnetCp);
                        }
                    }, 1000);

                    return;
                }


                if (!checkExpropValue()) {
                    return;
                }


                int type = getResultType();
                if (mPrimaryCollectTypeName.equals(mCollectTypeName)) {
                    //采集点类型没有改变
                    Logger.e("collect Type notChange");
                    switch (type) {
                        case SIGNAL_CHANGE_IN_SHOP_POINT://只是店内信号的改变，店外没有，店外更新扩展属性即可
                            if (mPhotosIn.isEmpty()) {
                                showTest("请补充店内照片，再点击保存");
                            } else {

                                if (mShopInSignalBean == null || mShopInSignalBean.getWifiDataList() == null) {
                                    mPhotosIn.clear();
                                    mIv2.setImageResource(R.mipmap.ic_in_photo);
                                    showTest("请重新拍店内的照片，再保存…");
                                    return;
                                }
                                checkAndUpdatePoint(mNmpReportPointIn, mShopInSignalBean, mPhotosIn, new SaveResultListener() {
                                    @Override
                                    public void onSaveFinish() {
                                        isShopIn = false;
                                        updatePointData2DB(mNmpReportPointOut, new SaveResultListener() {
                                            @Override
                                            public void onSaveFinish() {
                                                doFinish();
                                            }
                                        });
                                    }
                                });
                            }
                            break;
                        case SIGNAL_CHANGE_OUT_SHOP_POINT://只是店外信号的改变，店内没有，店内更新扩展属性即可
                            if (mPhotosOut.isEmpty()) {
                                showTest("请补充店外照片，再点击保存");
                            } else {
                                if (mShopOutSignalBean == null || mShopOutSignalBean.getWifiDataList() == null) {
                                    mPhotosOut.clear();
                                    mIv2.setImageResource(R.mipmap.ic_out_photo);
                                    showTest("请重新拍店外的照片，再保存…");
                                    return;
                                }
                                checkAndUpdatePoint(mNmpReportPointOut, mShopOutSignalBean, mPhotosOut, new SaveResultListener() {
                                    @Override
                                    public void onSaveFinish() {
                                        isShopIn = true;
                                        updatePointData2DB(mNmpReportPointIn, new SaveResultListener() {
                                            @Override
                                            public void onSaveFinish() {
                                                doFinish();
                                            }
                                        });
                                    }
                                });
                            }
                            break;
                        case SIGNAL_CHANGE_GENERAL_POINT://普通点的更改，更新即可
                            if (mPhotos.isEmpty()) {
                                showTest("请补充照片，再点击保存");
                            } else {
                                if (mTakePicSignalBean == null || mTakePicSignalBean.getWifiDataList() == null) {
                                    mPhotos.clear();
                                    mIv1.setImageResource(R.mipmap.ic_camera);
                                    showTest("请重新拍照，再保存…");
                                    return;
                                }
                                checkAndUpdatePoint(mNmpReportPoint, mTakePicSignalBean, mPhotos, new SaveResultListener() {
                                    @Override
                                    public void onSaveFinish() {
                                        doFinish();
                                    }
                                });
                            }
                            break;
                        case SIGNAL_NO_CHANGE_GENERAL_POINT://普通点的信号没有改变，更新扩展属性即可
                            updatePointData2DB(mNmpReportPoint, new SaveResultListener() {
                                @Override
                                public void onSaveFinish() {
                                    doFinish();
                                }
                            });
                            break;
                        case SIGNAL_NO_CHANGE_OUT_IN_POINT://店内店外点没有信号改变，只需要更新两个点的扩展属性

                            isShopIn = true;
                            updatePointData2DB(mNmpReportPointIn, new SaveResultListener() {
                                @Override
                                public void onSaveFinish() {
                                    isShopIn = false;
                                    updatePointData2DB(mNmpReportPointOut, new SaveResultListener() {
                                        @Override
                                        public void onSaveFinish() {
                                            doFinish();
                                        }
                                    });
                                }
                            });
                            break;

                        case SIGNAL_CHANGE_OUT_IN_SHOP_POINT://店内店外信号都改变，那么两个都要更新

                            if (mPhotosIn.isEmpty() || mPhotosOut.isEmpty()) {
                                showTest("请补充店外，店内的照片，再点击保存");
                            } else {
                                if (mShopOutSignalBean == null || mShopOutSignalBean.getWifiDataList() == null) {
                                    mPhotosOut.clear();
                                    mIv1.setImageResource(R.mipmap.ic_out_photo);
                                    showTest("请重新拍店外的照片，再保存…");
                                    return;
                                }
                                if (mShopInSignalBean == null || mShopInSignalBean.getWifiDataList() == null) {

                                    mPhotosIn.clear();
                                    mIv2.setImageResource(R.mipmap.ic_in_photo);
                                    showTest("请重新拍店内的照片，再保存…");
                                    return;
                                }

                                isShopIn = true;
                                checkAndUpdatePoint(mNmpReportPointIn, mShopInSignalBean, mPhotosIn, new SaveResultListener() {
                                    @Override
                                    public void onSaveFinish() {
                                        isShopIn = false;
                                        checkAndUpdatePoint(mNmpReportPointOut, mShopOutSignalBean, mPhotosOut, new SaveResultListener() {
                                            @Override
                                            public void onSaveFinish() {
                                                doFinish();
                                            }
                                        });
                                    }
                                });
                            }
                            break;
                    }
                } else {
                    //采集点类型已经改变
                    //检查是不是店外转店内
                    Logger.e("collectType change");
                    if (mPrimaryCollectTypeName.trim().equals("店铺") && !mCollectTypeName.trim().equals("店铺")) {
                        //店铺转其他
                        if (mPhotos.isEmpty()) {
                            showTest("请补充照片，再点击保存");
                            return;
                        }

                        if (mTakePicSignalBean == null || mTakePicSignalBean.getWifiDataList() == null) {
                            mPhotos.clear();
                            mIv1.setImageResource(R.mipmap.ic_camera);
                            showTest("请重新拍照片，再保存…");
                            return;
                        }
                        isShopInOut = false;
                        //没有检查扩展属性
                        saveNewPoint(mTakePicSignalBean, mPhotos, new SaveResultListener() {
                            @Override
                            public void onSaveFinish() {
                                //保存了一个新点，要删除旧的两个点
                                deletePointAndSignal(mNmpReportPointIn.getId());
                                deletePointAndSignal(mNmpReportPointOut.getId());
                                doFinish();
                            }
                        });
                    } else if (!mPrimaryCollectTypeName.trim().equals("店铺") && mCollectTypeName.trim().equals("店铺")) {
                        //其他转店铺
                        isShopInOut = true;

                        if (mShopOutSignalBean == null || mShopOutSignalBean.getWifiDataList() == null) {
                            mPhotosOut.clear();
                            mIv1.setImageResource(R.mipmap.ic_out_photo);
                            showTest("请重新拍店外的照片，再保存…");
                            return;
                        }
                        if (mShopInSignalBean == null || mShopInSignalBean.getWifiDataList() == null) {

                            mPhotosIn.clear();
                            mIv2.setImageResource(R.mipmap.ic_in_photo);
                            showTest("请重新拍店内的照片，再保存…");
                            return;
                        }


                        isShopIn = true;
                        saveNewPoint(mShopInSignalBean, mPhotosIn, new SaveResultListener() {
                            @Override
                            public void onSaveFinish() {
                                isShopIn = false;
                                saveNewPoint(mShopOutSignalBean, mPhotosOut, new SaveResultListener() {
                                    @Override
                                    public void onSaveFinish() {
                                        deletePointAndSignal(mNmpReportPoint.getId());
                                        doFinish();
                                    }
                                });
                            }
                        });
                    } else {
                        //非这两类给的情况
                        if (isReCollect) {
                            checkAndUpdatePoint(mNmpReportPoint, mTakePicSignalBean, mPhotos, new SaveResultListener() {
                                @Override
                                public void onSaveFinish() {
                                    doFinish();
                                }
                            });
                        } else {
                            updatePointData2DB(mNmpReportPoint, new SaveResultListener() {
                                @Override
                                public void onSaveFinish() {
                                    doFinish();
                                }
                            });
                        }
                    }
                }
                break;
            case R.id.tv_in:
                isShopIn = true;
                collectBtnClick();
                break;
            case R.id.tv_out:
                isShopIn = false;
                collectBtnClick();
                break;
            case R.id.btCollect:
                collectBtnClick();
                break;
        }
    }


    private void doFinish() {
        EventBus.getDefault().post(new EventBusMessage(Constans.EVENBUS_MESSAGE_CODE_RECEIVED_POINT_INDEX, mCurrentIndex));
        ModifyHistoryActivity.this.finish();
    }


    private int getResultType() {
        if (isShopInOut) {

            if (isReCollectIn && isReCollectOut) {
                //店内店外都重新采集了
                return SIGNAL_CHANGE_OUT_IN_SHOP_POINT;
            } else if (isReCollectIn) {
                //重新采集店内
                return SIGNAL_CHANGE_IN_SHOP_POINT;
            } else if (isReCollectOut) {
                //重新采集了店外的

                return SIGNAL_CHANGE_OUT_SHOP_POINT;
            } else {
                //店内店外都没重新采集
                return SIGNAL_NO_CHANGE_OUT_IN_POINT;
            }
        } else {
            if (isReCollect) {
                return SIGNAL_CHANGE_GENERAL_POINT;
            } else {
                return SIGNAL_NO_CHANGE_GENERAL_POINT;
            }
        }
    }


    private void collectBtnClick() {

        //isCollect = true;
        if (isShopInOut) {
            if (isShopIn) {
                mIv2.setImageResource(R.mipmap.ic_in_photo);
                isReCollectIn = true;
                mPhotosIn.clear();
            } else {
                isReCollectOut = true;
                mIv1.setImageResource(R.mipmap.ic_out_photo);
                mPhotosOut.clear();
            }

        } else {
            mIv1.setScaleType(ImageView.ScaleType.CENTER);
            mIv1.setImageResource(R.mipmap.ic_camera);
            isReCollect = true;
            mPhotos.clear();

        }
        treatCollect();
    }


    /**
     * 欺骗采集
     */
    private void treatCollect() {
        mLoadingDialog.show("正在采集信号…");
        tvSave.postDelayed(new Runnable() {
            @Override
            public void run() {
                mLoadingDialog.dismiss();
                mAlertDialog = UIUtils.getAlertDialog(ModifyHistoryActivity.this, null, "已采集完成，请拍照", null, "确定", null, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAlertDialog.dismiss();
                    }
                });
                mAlertDialog.show();
            }
        }, 500);

    }


    /**
     * 采集WiFi信号
     *
     * @param tip tip
     */
    private void collectSignal(String tip) {
        mLoadingDialog.show(tip);
        try {
            mSignalCollector.collectAll();
        } catch (Exception e) {
            e.printStackTrace();
            mLoadingDialog.dismiss();
            showTest("未知错误，请重试…");
        }
    }


    /**
     * 直接更新点的属性就行了
     *
     * @param nmpReportPoint nmpReportPoint
     * @param listener       listener
     */
    private void updatePointData2DB(final NmpReportPoint nmpReportPoint, SaveResultListener listener) {

        if (checkExpropValue()) {
            nmpReportPoint.setCreateTime(System.currentTimeMillis());
            if (isShopInOut) {
                addShopInOutExprop(isShopIn);
            }
            nmpReportPoint.setExPropList(exchangeMap2List(mMapPointExprop));
            nmpReportPoint.setPointType(mCollectTypeName);
            nmpReportPoint.setPointTypeCode(mCollectTypeCode);
            nmpReportPoint.setFloorNumber(mFloorNumber);
            nmpReportPoint.setNmpReportData(mNmpReportData);
            nmpReportPoint.update(nmpReportPoint.getId());
            listener.onSaveFinish();
        }
    }


    /**
     * 更新一个重新采集信号的点
     *
     * @param nmpReportPoint 要更新的点
     * @param signalBean     信号
     * @param photoUrls      photoUrls
     */
    private void checkAndUpdatePoint(final NmpReportPoint nmpReportPoint, final SignalBean signalBean, final List<PhotoUrl> photoUrls, @NonNull final SaveResultListener listener) {

        if (TextUtils.isEmpty(mCollectTypeName) || TextUtils.isEmpty(mCollectTypeCode)) {
            //点类型可能因为网络的原因为null
            loadCpTypeFromNet();
            showTest("采集点类型没有加载，不能保存，请等采集点类型加载完成后选择正确的采集点类型才能保存");
            return;
        }


        if (photoUrls == null || photoUrls.isEmpty()) {
            showTest("请拍照片");
            return;
        }


        if (mNmpReportData == null) {
            showTest("信息丢失，请退出本界面再进入采集");
            return;
        }


        if (checkExpropValue()) {
            List<WifiData> wifiDataIn = signalBean.getWifiDataList();
            if (wifiDataIn.isEmpty()) {
                mAlertDialog = UIUtils.getAlertDialog(this, "提示", "店内点的信号个数为0\n请确认你的Wifi开关是否已经打开？", "我已经打开Wifi", "我还没打开WiFi", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAlertDialog.dismiss();
                        updatePoint(nmpReportPoint, signalBean, photoUrls, listener);
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAlertDialog.dismiss();
                    }
                });
                mAlertDialog.show();
            } else {
                updatePoint(nmpReportPoint, signalBean, photoUrls, listener);
            }
        }

    }


    /**
     * 更新一个点的数据
     *
     * @param nmpReportPoint nmpReportPoint
     * @param signalBean     signalBean
     * @param photoUrls      photoUrls
     * @param listener       listener
     */
    private void updatePoint(final NmpReportPoint nmpReportPoint, final SignalBean signalBean, final List<PhotoUrl> photoUrls, @NonNull final SaveResultListener listener) {


        Observable<String> observable = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                //把删旧数据
                if (isShopInOut) {
                    addShopInOutExprop(isShopIn);
                }
                //还是按原样，先更新店内点，再更新店外点
                nmpReportPoint.setCreateTime(signalBean.getCreateTime());//修改后时间还是得改
                nmpReportPoint.setExPropList(exchangeMap2List(mMapPointExprop));
                nmpReportPoint.setPointType(mCollectTypeName);
                nmpReportPoint.setPointTypeCode(mCollectTypeCode);
                nmpReportPoint.setFloorNumber(mFloorNumber);
                nmpReportPoint.setNmpReportData(mNmpReportData);
                List<WifiSignalItem> wifiSignalItems = new ArrayList<>();
                List<BaseStationBean> baseStationBeans = new ArrayList<>();
                //List<BleSignal> bleSignals = new ArrayList<>();
                MagneticBean magneticBean = null;
                //有拍照时的信号不为null就用拍照时的信号，不拍照的时候才用采集按钮采集的信号
                Logger.e("signalBean !=null");
                for (WifiData wifiData : signalBean.getWifiDataList()) {
                    WifiSignalItem wifiSignalItem = WifiSignalItem.createFromWifiData(nmpReportPoint, wifiData);
                    if (nmpReportPoint.getId() != 0) {
                        wifiSignalItem.setNmpreportpoint_id(nmpReportPoint.getId());
                    }
                    wifiSignalItems.add(wifiSignalItem);
                }

                if (signalBean.getBaseStationDataList() != null && !signalBean.getBaseStationDataList().isEmpty()) {
                    for (BaseStationData data : signalBean.getBaseStationDataList()) {
                        BaseStationBean baseStationBean = BaseStationBean.creatFromBaseStationData(nmpReportPoint, data);
                        baseStationBeans.add(baseStationBean);
                    }
                }

                /*if (signalBean.getBluetoothData() != null) {
                    for (IBeaconData data : signalBean.getBluetoothData()) {
                        BleSignal bleSignal = BleSignal.createFromBluetoothData(nmpReportPoint, data);
                        bleSignals.add(bleSignal);
                    }

                }*/

                if (signalBean.getMagneticData() != null) {
                    magneticBean = MagneticBean.createFromMagneticData(nmpReportPoint, signalBean.getMagneticData());
                }


                //这里不一定需要保存新的采集点
                nmpReportPoint.setPhotoList(photoUrls);
                nmpReportPoint.setBaseStations(baseStationBeans);
                nmpReportPoint.setSignals(wifiSignalItems);
                nmpReportPoint.setMagnetic(magneticBean);
                //nmpReportPoint.setBleSignals(bleSignals);

                MagneticBean magneticBeanHistory = null;
                if (nmpReportPoint.getId() != 0) {
                    DataSupport.deleteAll(WifiSignalItem.class, "nmpreportpoint_id = " + nmpReportPoint.getId());
                    DataSupport.deleteAll(BaseStationBean.class, "nmpreportpoint_id = " + nmpReportPoint.getId());
                    //DataSupport.deleteAll(BleSignal.class, "nmpreportpoint_id = " + nmpReportPoint.getId());
                    DataSupport.deleteAll(PhotoUrl.class, "nmpreportpoint_id = " + nmpReportPoint.getId());
                    magneticBeanHistory = DataSupport.where("nmpreportpoint_id = " + nmpReportPoint.getId()).findFirst(MagneticBean.class);
                }

                //保存和更新的是需要搞吗？
                nmpReportPoint.update(nmpReportPoint.getId());
                for (PhotoUrl photoUrl : photoUrls) {
                    photoUrl.setNmpreportpoint_id(nmpReportPoint.getId());
                }
                DataSupport.saveAll(photoUrls);
                DataSupport.saveAll(wifiSignalItems);
                DataSupport.saveAll(baseStationBeans);
                //DataSupport.saveAll(bleSignals);
                if (magneticBean != null) {
                    magneticBean.save();
                }
                if (magneticBeanHistory != null) {
                    magneticBeanHistory.delete();
                }

                subscriber.onNext("");
                subscriber.onCompleted();
            }
        });
        Subscription subscription = wrapObserverWithHttp(observable).subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                mLoadingDialog.dismiss();
                e.printStackTrace();
                showTest("保存数据出错");
            }

            @Override
            public void onNext(String result) {
                mLoadingDialog.dismiss();
                listener.onSaveFinish();
            }
        });
        addSubscription(subscription);


    }

    interface SaveResultListener {
        void onSaveFinish();
    }


    /**
     * 检查扩展属性有没有值
     */
    private boolean checkExpropValue() {
        for (int i = 0; i < mLayoutExprop.getChildCount(); i++) {
            View view = mLayoutExprop.getChildAt(i);
            TextView tvTitle = (TextView) view.findViewById(R.id.tv);
            TextView tvIn = (TextView) view.findViewById(R.id.tvIn);
            String key = tvTitle.getText().toString();
            String value = tvIn.getText().toString();
            String tagCode = (String) tvTitle.getTag();
            if (mCollectTypeCode.equals("CP-CBDWRITE-COMPANY-OUTDOOR") && tagCode.equals("EXP-COMPANY-NAME")) {
                continue;
            }

            if (TextUtils.isEmpty(value)) {
                showTest("请选择" + key.substring(0, key.length() - 2));
                return false;
            }
        }
        return true;
    }


    /**
     * 保存采集点
     */
   /* private void save(final NmpReportPoint point) {
        if (TextUtils.isEmpty(mCollectTypeName) || TextUtils.isEmpty(mCollectTypeCode)) {
            //点类型可能因为网络的原因为null
            loadCpTypeFromNet();
            showTest("采集点类型没有加载，不能保存，请等采集点类型加载完成后选择正确的采集点类型才能保存");
            return;
        }

        if (mNmpReportData == null) {
            showTest("信息丢失，请退出本界面再进入采集");
            return;
        }

        //检查扩展属性是否填满了，现在检查属性吗？//现在先不检查
        if (checkExpropValue()) {
            mLoadingDialog.show("正在保存数据…");
            //商圈的第一个点需要经纬度 于2016-12-14 经黄、林总的方案改的，如果改了信号，是要重新定位的
            if ((pointsize == 1) && (mShopInSignalBean != null || mShopOutSignalBean != null || mTakePicSignalBean != null)) {
                LocationService.getInstance().start(new LocationService.LocationServiceListener() {
                    @Override
                    public void onLocationResult(double longitude, double latitude, String errMsg) {
                        updateLatLong(point, latitude, longitude);
                    }
                });
            } else {
                savePointAsync(point);
            }
        }

    }*/
    @OnClick(R.id.layout_point_type)
    public void pointTypeClick() {

        //显示更改采集点类型的对话框
        showSelectPointType("请选择正确的采集点类型");

    }


    /**
     * 删除采集点的信息
     *
     * @param pointId pointId
     */
    private void deletePointAndSignal(final int pointId) {

        final Observable<String> observable = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                if (pointId != 0) {
                    DataSupport.delete(NmpReportPoint.class, pointId);
                    DataSupport.deleteAll(WifiSignalItem.class, "nmpReportpoint_id = " + pointId);
                    //DataSupport.deleteAll(BleSignal.class, "nmpReportpoint_id = " + pointId);
                    DataSupport.deleteAll(BaseStationBean.class, "nmpReportpoint_id = " + pointId);
                    DataSupport.deleteAll(MagneticBean.class, "nmpReportpoint_id = " + pointId);
                }

                subscriber.onNext("finshed");
                subscriber.onCompleted();
            }
        });
        Subscription subscription = wrapObserverWithHttp(observable).subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String o) {
                Logger.i(o);
            }
        });

        addSubscription(subscription);

    }


    /**
     * 显示采集点类型的对话框
     *
     * @param title title
     */
    private void showSelectPointType(String title) {
        if (mTypeDialog == null) {
            mTypeDialog = new Dialog(this, R.style.Style_Dialog);
            mTypeDialog.setContentView(R.layout.dialog_collection_type);
            View vConfirm = mTypeDialog.findViewById(R.id.tvConfirm);
            vConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mTypeDialog.dismiss();
                }
            });
            mSpCollectType = (Spinner) mTypeDialog.findViewById(R.id.sp_point_type);
            mSpCollectType.setAdapter(mColltionTypeAdapter);
            mSpCollectType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Logger.i("mSpCollectType onItemSelected--------");
                    String preCollctTypeName = mCollectTypeName;
                    mLayoutExprop.removeAllViews();
                    mCollectTypeName = mCPs.get(position).getCpTypeName();
                    mCollectTypeCode = mCPs.get(position).getCpTypeCode();
                    tvPointType.setText(mCollectTypeName);

                    if (!preCollctTypeName.equals(mCollectTypeName)) {

                        if (preCollctTypeName.trim().equals("店铺") && !mCollectTypeName.trim().equals("店铺")) {
                            //店铺转其他
                            mIv1.setImageResource(R.mipmap.ic_camera);
                            ViewGroup.LayoutParams layoutParams = mIv1.getLayoutParams();
                            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                            layoutParams.height = UIUtils.dip2px(160);
                            mIv1.setLayoutParams(layoutParams);
                            mIv2.setVisibility(View.GONE);
                        } else if (!preCollctTypeName.trim().equals("店铺") && mCollectTypeName.trim().equals("店铺")) {

                            mIv1.setScaleType(ImageView.ScaleType.CENTER);
                            mIv1.setImageResource(R.mipmap.ic_out_photo);
                            mIv2.setImageResource(R.mipmap.ic_in_photo);
                            ViewGroup.LayoutParams layoutParams = mIv1.getLayoutParams();
                            layoutParams.height = UIUtils.dip2px(120);
                            layoutParams.width = UIUtils.dip2px(160);
                            mIv1.setLayoutParams(layoutParams);
                            mIv2.setVisibility(View.VISIBLE);
                            //其他转店铺
                        }
                    }

                    //如果采集点的类型为店铺，出现点内店外采集按钮,判断是不是其他转店铺，店铺转其他了
                    if (mCollectTypeName.trim().equals("店铺")) {
                        flIn.setVisibility(View.VISIBLE);
                        tvCollect.setVisibility(View.GONE);
                        isShopInOut = true;
                    } else {
                        flIn.setVisibility(View.GONE);
                        tvCollect.setVisibility(View.VISIBLE);
                        isShopInOut = false;
                    }
                    //每选一次扩展属性要清掉
                    if (mPrimaryCollectTypeName.equals(mCollectTypeName)) {
                        restorePhoto();
                        mMapPointExprop = mPrimaryMapPointExprop;
                    } else {
                        mMapPointExprop = new HashMap<String, NmpReportPoint.Exprop>();
                    }

                    Exprop exprop = mMapExprop.get(mCollectTypeCode);
                    if (exprop == null) {
                        mCurrnetCp = mCPs.get(position);
                        loadExpropFromNet(mCurrnetCp);
                    } else {
                        loadExprop2View(mCollectTypeCode, exprop);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            mTypeDialog.setCancelable(false);
        }

        TextView tvTitle = (TextView) mTypeDialog.findViewById(R.id.tvTitle);
        tvTitle.setText(title);
        mTypeDialog.show();
    }

    /**
     * 将扩展属性从map转回list
     *
     * @param map map
     * @return list
     */
    private List<NmpReportPoint.Exprop> exchangeMap2List(Map<String, NmpReportPoint.Exprop> map) {

        Logger.i("map size" + map.size());
        List<NmpReportPoint.Exprop> list = new ArrayList<>();
        Iterator<String> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            list.add(map.get(iterator.next()));
        }
        Logger.i("list size" + list.size());
        return list;
    }


    /**
     * 新保存一个点
     *
     * @param signalBean 信号
     * @param photoUrls  照片
     */
    private void saveNewPoint(final SignalBean signalBean, final List<PhotoUrl> photoUrls, final SaveResultListener listener) {


        //检查信号
        if (TextUtils.isEmpty(mCollectTypeName) || TextUtils.isEmpty(mCollectTypeCode)) {
            //点类型可能因为网络的原因为null
            loadCpTypeFromNet();
            showTest("采集点类型没有加载，不能保存，请等采集点类型加载完成后选择正确的采集点类型才能保存");
            return;
        }

        if (mNmpReportData == null) {
            showTest("信息丢失，请退出本界面再进入采集");
            return;
        }


        Observable<String> observable = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                List<WifiSignalItem> wifiSignalItems = new ArrayList<>();
                //List<BleSignal> bleSignals = new ArrayList<>();
                List<BaseStationBean> baseStationBeans = new ArrayList<>();
                MagneticBean magneticBean = null;
                NmpReportPoint point = new NmpReportPoint();
                point.setNmpReportData(mNmpReportData);
                for (WifiData wifiData : signalBean.getWifiDataList()) {
                    WifiSignalItem wifiSignalItem = WifiSignalItem.createFromWifiData(point, wifiData);
                    wifiSignalItems.add(wifiSignalItem);
                }

                if (signalBean.getBaseStationDataList() != null && !signalBean.getBaseStationDataList().isEmpty()) {
                    for (BaseStationData data : signalBean.getBaseStationDataList()) {
                        BaseStationBean baseStationBean = BaseStationBean.creatFromBaseStationData(point, data);
                        baseStationBeans.add(baseStationBean);
                    }
                }

              /*  if (signalBean.getBluetoothData() != null) {
                    for (IBeaconData data : signalBean.getBluetoothData()) {
                        BleSignal bleSignal = BleSignal.createFromBluetoothData(point, data);
                        bleSignals.add(bleSignal);
                    }

                }*/

                if (signalBean.getMagneticData() != null) {
                    magneticBean = MagneticBean.createFromMagneticData(point, signalBean.getMagneticData());
                }

                if (isShopInOut) {
                    point.setSamePageCode(samePageCode);
                }
                //int pointSize = DataSupport.where("mnmpreportdata_Id = " + mNmpReportData.getId()).count(NmpReportPoint.class);
                mCurrentIndex++;
                if (mFloorNumber < 0) {
                    point.setPointIndex(mFloorNumber * 10000 - (mCurrentIndex));
                } else {
                    point.setPointIndex(mFloorNumber * 10000 + (mCurrentIndex));
                }

                point.setCreateTime(signalBean.getCreateTime());//取采集的时间

                if (isShopInOut) {
                    addShopInOutExprop(isShopIn);
                }
                point.setPointType(mCollectTypeName);
                point.setPointTypeCode(mCollectTypeCode);

                point.setExPropList(exchangeMap2List(mMapPointExprop));
                point.setFloorNumber(mFloorNumber);
                //point.setSamePageCode(sameTypeCode);
                point.setPhotoList(photoUrls);
                point.setBaseStations(baseStationBeans);
                point.setSignals(wifiSignalItems);
                point.setMagnetic(magneticBean);
                //point.setBleSignals(bleSignals);
                point.save();
                DataSupport.saveAll(baseStationBeans);
                DataSupport.saveAll(wifiSignalItems);
                DataSupport.saveAll(photoUrls);
                //DataSupport.saveAll(bleSignals);
                if (magneticBean != null) {
                    magneticBean.save();
                }

                subscriber.onNext("save Over");
                subscriber.onCompleted();
            }
        });
        Subscription subscription = wrapObserverWithHttp(observable).subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                mLoadingDialog.dismiss();
            }

            @Override
            public void onNext(String o) {
                mLoadingDialog.dismiss();
                listener.onSaveFinish();
            }
        });
        addSubscription(subscription);
    }


    /**
     * 显示照片找不到对话框
     */
    private void showPhotoLostDialog() {

        mAlertDialog = UIUtils.getAlertDialog(this, "提示", "照片文件找不到，请重新拍照", "确定", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlertDialog.dismiss();
            }
        });
        mAlertDialog.show();
    }


    @Override
    public int createSuccessView() {
        return R.layout.activity_modify_history;
    }

    @Override
    protected void onDestroy() {
        mSignalCollector.onDestroy();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
