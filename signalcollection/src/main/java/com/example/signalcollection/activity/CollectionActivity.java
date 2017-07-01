package com.example.signalcollection.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
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
import com.example.signalcollection.bean.DefaultResult;
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
import com.example.signalcollection.bean.WorkListResult;
import com.example.signalcollection.network.WorkService;
import com.example.signalcollection.recyclerview.SpinnerAdapter;
import com.example.signalcollection.util.ImageUtil;
import com.example.signalcollection.util.LocationService;
import com.example.signalcollection.util.UIUtils;
import com.example.signalcollection.view.LoadingDialog;
import com.example.signalcollection.view.SearchBandDialog;
import com.google.gson.Gson;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;

import static com.example.signalcollection.network.RetrofitUtil.PHOTO_BASE_URL;

/**
 * 信号采集界面
 */
public class CollectionActivity extends BaseActivity {

    @BindView(R.id.tvInfo)
    TextView mTvInfo;//显示采集点数字信息
    @BindView(R.id.tvName)
    TextView mTvName;//显示商圈名字
    @BindView(R.id.tvType)
    TextView mTvType;//显示商圈类型
    Spinner mSpCollectType;//采集点类型选择
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


    //public static final String KEY_BASEURL = "http://jmtool3.jjfinder.com/";
    private HashMap<String, Exprop> mMapExprop = new HashMap<>();//采集点类型对应的扩展属性
    private Map<String, NmpReportPoint.Exprop> mMapPointExprop = new HashMap<>(); //采集点扩展属性键值对
    private HashMap<String, List<Predefine.Data>> mMapExpropPredefine = new HashMap<>();//扩展属性的预设值
    private HashMap<String, SpinnerAdapter> mMapExpropAdapter = new HashMap<>();//扩展属性预设值列表的适配器结合
    private SpinnerAdapter<CPResult.DataBean> mColltionTypeAdapter; //采集点类型适配器
    private SignalCollector mSignalCollector; //信号采集器
    private WorkListResult.DataBean mTaskBean;
    private LoadingDialog mLoadingDialog;
    private Integer mCurrentIndex;//当前点的索引从0开始
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

    private String mCollectType;//采集点类型
    private String mCollectTypeCode;//采集点类型的code
    private List<CPResult.DataBean> mCPs;//采集点类型的存储
    private NmpReportPoint mNmpReortPoint;//采集点数据
    private Dialog mAlertDialog;
    private int mPointSize;//采集点数
    private Dialog mTypeDialog;
    private NmpReportData mNmpReportData;
    private ImageView mCurrentImageView;
    private SearchBandDialog mSearchBandDialog;

    private CPResult.DataBean mCurrnetCp;

    private boolean mNetworkErr = false;

    @Subscribe
    public void receivedEvenbusMessage(EventBusMessage message) {

        //审核照片会来这里，怎么办？
        switch (message.what) {
            case Constans.EVENBUS_MESSAGE_CODE_RECEIVED_IMG:
                String path = message.tag;
                if (!TextUtils.isEmpty(path)) {
                    //检查一下照片存不存在
                    File file = new File(path);
                    if (!file.exists() || file.length() <= 0) {
                        showPhotoLostDialog();
                    } else {

                        ImageLoader.getInstance().displayImage("file:/" + path, mCurrentImageView, ImageUtil.getInstance().getBaseDisplayOption());
                        String key = PHOTO_BASE_URL + "cpphoto" + File.separator + mNmpReportData.getAreaCode() + "-" + mCurrentIndex + "-" + System.currentTimeMillis() + ".jpg";
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
                            mIv1.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            //mLtPhotos.add(key);
                            mPhotos.add(photoUrl);
                        }
                    }
                } else {
                    //采了信号但是没拍照
                    if (isShopInOut) {
                        if (isShopIn) {
                            mShopInSignalBean = null;
                            //mLtPhotosIn.clear();
                            mPhotosIn.clear();
                            mIv2.setImageResource(R.mipmap.ic_in_photo);
                        } else {
                            mShopOutSignalBean = null;
                            //mLtPhotosOut.clear();
                            mPhotosOut.clear();
                            mIv1.setImageResource(R.mipmap.ic_out_photo);
                        }
                    } else {
                        mTakePicSignalBean = null;
                        //mLtPhotos.clear();
                        mPhotos.clear();
                        mIv1.setScaleType(ImageView.ScaleType.CENTER);
                        mIv1.setImageResource(R.mipmap.ic_camera);
                        mIv2.setImageResource(R.mipmap.ic_camera);
                    }
                }
                break;
            case Constans.EVENBUS_MESSAGE_CODE_RECEIVED_POINT_INDEX:
                mCurrentIndex = (Integer) message.data;
                mPointSize = DataSupport.where("remark <> 1 AND nmpreportdata_id = " + mNmpReportData.getId()).count(NmpReportPoint.class);
                mTvInfo.setText(String.format("您当前已采集%d个点，正在对第%d个点进行采集", mPointSize, mPointSize + 1));
                mNmpReportData.setPointSize(mPointSize);
                EventBus.getDefault().post(mNmpReportData);
                break;
        }
    }


    /**
     * 查找最后一个点的pointIndex
     *
     * @param id 商圈的ID
     * @return 点序
     */
    private int findLastIndex(int id) {

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
        EventBus.getDefault().register(this);

        //添加商圈的时候这个会有值
        mTaskBean = (WorkListResult.DataBean) getIntent().getSerializableExtra(Constans.TASK_BEAN);

        if (mTaskBean != null) {
            mNmpReportData = new NmpReportData();
            mNmpReportData.setAreaCode(mTaskBean.getAreaCode());
            mNmpReportData.setAreaName(mTaskBean.getAreaName());
            mNmpReportData.setAreaTypeName(mTaskBean.getAreaTypeName());
            mNmpReportData.setRefAreaTypeCode(mTaskBean.getRefAreaTypeCode());
            mNmpReportData.save();
            mCurrentIndex = 0;
        } else {
            int id = getIntent().getIntExtra(Constans.TASK_ID, 0);
            mPointSize = getIntent().getIntExtra(Constans.POINT_COUNT, 0);//相机挂了，这里好像就是从零开始了，奔溃的时候保存这个属性
            mCurrentIndex = findLastIndex(id);//通过直接从数据库查找点序的方式，避免传值出错
            mNmpReportData = DataSupport.find(NmpReportData.class, id);
        }

        mTvInfo.setText(String.format(mTvInfo.getText().toString(), mPointSize, mPointSize + 1));
        //初始化信号采集器只保证WiFi信号
        mSignalCollector = new SignalCollector(this, new ScanResultListener() {
            @Override
            public void onResult(List<WifiData> wifiResult, List<BaseStationData> baseStationDataList,/* List<IBeaconData> bluetoothData, */MagneticData magneticData) {
                mLoadingDialog.dismiss();

                SignalBean bean = new SignalBean(wifiResult, baseStationDataList, magneticData, System.currentTimeMillis()/*, bluetoothData*/);
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
        //初始化采集点属性的adapter
        mColltionTypeAdapter = new SpinnerAdapter<CPResult.DataBean>(this) {
            @Override
            public void setText(TextView textView, CPResult.DataBean dataBean) {
                textView.setText(dataBean.getCpTypeName());
            }
        };

        //初始化楼层的adapter
        for (int i = -5; i <= 100; i++) {
            if (i == 0) {
                //排除0楼，没有0楼的概念
                continue;
            }
            mLtFloor.add(i);
        }

        SpinnerAdapter<Integer> adapterFloor = new SpinnerAdapter<Integer>(this) {
            @Override
            public void setText(TextView textView, Integer s) {
                textView.setText(String.valueOf(s));
            }
        };
        adapterFloor.setListData(mLtFloor);
        mSpFloor.setAdapter(adapterFloor);
        mSpFloor.setSelection(5);//选到1楼
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
            //这个东西为空的原因是拍照的时候Activity被系统强制回收了，恢复的时候没了
            mTvName.setText(mNmpReportData.getAreaName());
            mTvType.setText(mNmpReportData.getAreaTypeName());
            loadCpTypeFromNet();
        } else {
            showTest("信息丢失，请退出本界面再进入采集！");
        }

    }


    /**
     * 添加店内店外点的扩展属性
     *
     * @param isShopIn 是否是店内
     */
    private void addShopInOutExprop(boolean isShopIn) {
        NmpReportPoint.Exprop exprop = new NmpReportPoint.Exprop();
        exprop.setRefExPropCode("EXP-SOTRE_INOROUT");
        if (isShopIn) {
            exprop.setPropValue("店内");
        } else {
            exprop.setPropValue("店外");
        }
        mMapPointExprop.put(exprop.getRefExPropCode(), exprop);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Logger.i("CollectionActivity  onSaveInstanceState");
        outState.putInt("mPointSize", mPointSize);
        //outState.putInt("mPointIndex", mCurrentIndex);
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        //有很多数据需要从这里保存的
        Logger.i("CollectionActivity  onSaveInstanceState");
        if (savedInstanceState != null) {
            mPointSize = savedInstanceState.getInt("mPointSize");
            mTvInfo.setText(String.format(mTvInfo.getText().toString(), mPointSize, mPointSize + 1));
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    /**
     * 多选框的对话框
     *
     * @param dataBean dataBean
     * @param mTv      mTv
     * @return 对话框
     */
    private Dialog getCreateDialog(final Exprop.DataBean dataBean, final List<Predefine.Data> list, final TextView mTv) {

        Dialog dialog;
        AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("请选择" + dataBean.getPropName());
        final String[] strs = new String[list.size()];
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_collect, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionHistory:
                Intent intent = new Intent(CollectionActivity.this, HistoryCollectedActivity.class);
                intent.putExtra(Constans.TASK_ID, mNmpReportData.getId());
                intent.putExtra(Constans.POINT_INDEX, mCurrentIndex);
                startActivity(intent);
                break;
            case R.id.actionError:
                Intent intent1 = new Intent(CollectionActivity.this, TaskErrorActivity.class);
                intent1.putExtra(TaskErrorActivity.KEY_AREACODE, mNmpReportData);
                startActivity(intent1);
                break;

            case R.id.action_take_audit_photo:
                Intent intent2 = new Intent(CollectionActivity.this, TakeAuditPhotoActivity.class);
                intent2.putExtra(Constans.AREA_CODE, mNmpReportData.getAreaCode());
                intent2.putExtra(Constans.AREA_DATA_ID, mNmpReportData.getId());
                startActivity(intent2);
                break;
        }
        return super.onOptionsItemSelected(item);
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
                    showSelectPointType("请选择当前采集点的点类型");
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
                mLoadingDialog.dismiss();
                mNetworkErr = true;
                showTest(mNetWorkError);
            }

            @Override
            public void onNext(Exprop exprop) {
                mLoadingDialog.dismiss();
                mNetworkErr = false;
                if (exprop.getRetCode() == 0) {
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
     * @param textView         textView
     * @param preDefineRequest textView
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
                    if (adapter != null && !list.isEmpty()) {
                        adapter.setListData(list);
                        Predefine.Data data = list.get(0);
                        textView.setText(data.getTagName());
                        NmpReportPoint.Exprop exprop = new NmpReportPoint.Exprop();
                        exprop.setPropValue(data.getTagName());
                        exprop.setTagCode(data.getTagCode());
                        exprop.setRefExPropCode(preDefineRequest.getRefExPropCode());
                        mMapPointExprop.put(preDefineRequest.getRefExPropCode(), exprop);
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

            //为了防止错误，先提前显示出来
            LinearLayout.LayoutParams layoutParames = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParames.setMargins(0, UIUtils.dip2px(8), 0, 0);
            mLayoutExprop.addView(view, layoutParames);

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
                //标签形式的输入框
                //sp.setVisibility(View.GONE);
                tvIn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        UIUtils.getAlertDialogEdit("请输入" + dataBean.getPropName(), CollectionActivity.this, tvIn.getText().toString(), new UIUtils.EditResultInterface() {
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
                            mSearchBandDialog = new SearchBandDialog(CollectionActivity.this);
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
                                UIUtils.getDialogEdit(true, "请输入品牌名", CollectionActivity.this, new UIUtils.EditResultInterface() {
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
     * 品牌的扩展属性添加到扩展属性中
     *
     * @param data
     */
    /*private void addBandExprop2PointExprop(SearchResult.Data data) {

        if (data != null && data.getExProps() != null && data.getExProps().isEmpty()) {
            for (SearchResult.Exprop exprop : data.getExProps()) {
                NmpReportPoint.Exprop pointExprop = new NmpReportPoint.Exprop();
                pointExprop.setRefExPropCode(exprop.getPropCode());
                pointExprop.setPropValue(exprop.getValueName());
                pointExprop.setTagCode(exprop.getValueCode());
                mMapPointExprop.put(exprop.getPropCode(), pointExprop);
                tvIn.setText(exprop.getValueName());
                tvIn.setEnabled(false);

            }
        }
    }*/


    /**
     * 添加选择品牌的值放回到空间的View中去
     *
     * @param data
     */
    /*private void loadBandValueToView(SearchResult.Data data) {

        Logger.e(new Gson().toJson(data));
        if (data == null || data.getExProps() == null || data.getExProps().isEmpty()) {

            showTest("品牌的扩展属性异常");
            return;
        }
        for (int i = 0; i < mLayoutExprop.getChildCount(); i++) {
            View view = mLayoutExprop.getChildAt(i);
            TextView tvTitle = (TextView) view.findViewById(R.id.tv);
            TextView tvIn = (TextView) view.findViewById(R.id.tvIn);
            String propCode = (String) tvTitle.getTag();
            for (SearchResult.Exprop exprop : data.getExProps()) {

                if (exprop.getPropCode().equals(propCode)) {
                    NmpReportPoint.Exprop exprop1 = new NmpReportPoint.Exprop();
                    exprop1.setRefExPropCode(propCode);
                    exprop1.setPropValue(exprop.getValueCode());
                    mMapPointExprop.put(propCode, exprop1);
                    tvIn.setText(exprop.getValueName());
                    tvIn.setEnabled(false);
                }
            }
        }
    }*/


    /**
     * 选择的回调，待优化
     */
    interface SelectTypeItemSelectedListener<T> {

        void onItemSelect(T data);
    }

    /**
     * 选择类型的对话框
     *
     * @param title    title
     * @param adapter  title
     * @param listener title
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
                    UIUtils.getDialogEdit(true, "请输入" + title.substring(3), CollectionActivity.this, new UIUtils.EditResultInterface() {
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


    @Override
    public int createSuccessView() {
        return R.layout.activity_collection;
    }

    @Override
    protected void onDestroy() {
        mSignalCollector.onDestroy();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }


    /**
     * @param view view
     */
    @OnClick({R.id.iv1, R.id.iv2, R.id.btCollect, R.id.btNext, R.id.tv_in, R.id.tv_out})
    public void click(View view) {
        switch (view.getId()) {

            case R.id.iv1:
                mCurrentImageView = (ImageView) view;
                if (isShopInOut) {
                    isShopIn = false;
                }
                collectSignal();
                break;
            case R.id.iv2:
                if (isShopInOut) {
                    isShopIn = true;
                }
                mCurrentImageView = (ImageView) view;
                collectSignal();
                break;
            case R.id.btNext:
                //保存按钮,如果是店铺类型要保存两个点

                if (mNetworkErr) {
                    showTest("由于网络问题，有扩展属性没加载出来，请等待扩展属性加载出来，填写扩展属性，再保存");
                    tvCollect.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loadExpropFromNet(mCurrnetCp);
                        }
                    }, 1000);
                    return;
                }


                if (isShopInOut) {
                    checkBothWifiSignal();
                } else {
                    checkWifiSignal(mTakePicSignalBean);
                }
                break;
            case R.id.tv_in:
            case R.id.tv_out:
            case R.id.btCollect:
                treatCollect();
                break;
        }
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
                mAlertDialog = UIUtils.getAlertDialog(CollectionActivity.this, null, "已采集完成，请拍照", null, "确定", null, new View.OnClickListener() {
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
     */
    private void collectSignal() {
        mLoadingDialog.show("正在启动相机…");
        try {
            mSignalCollector.collectAll();
        } catch (Exception e) {
            e.printStackTrace();
            mLoadingDialog.dismiss();
            showTest("未知错误，请重试…");
        }
    }


    /**
     * 检查和保存店外店内点的数据
     */
    private void checkBothWifiSignal() {

        if (mShopInSignalBean == null || mShopInSignalBean.getWifiDataList() == null) {

            showTest("你还没对店内进行拍照，请拍照后保存");
            return;
        }
        if (mShopOutSignalBean == null || mShopOutSignalBean.getWifiDataList() == null) {
            showTest("你还没对店外进行拍照，请拍照后保存");
            return;
        }

        //检查室内点的
        List<WifiData> wifiDataIn = mShopInSignalBean.getWifiDataList();
        Logger.e("室内 :" + new Gson().toJson(wifiDataIn));
        if (wifiDataIn.isEmpty()) {
            mAlertDialog = UIUtils.getAlertDialog(this, "提示", "店内点的信号个数为0\n请确认你的Wifi开关是否已经打开？", "我已经打开Wifi", "我还没打开WiFi", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAlertDialog.dismiss();
                    isShopIn = true;
                    save();
                }
            }, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAlertDialog.dismiss();
                }
            });
            mAlertDialog.show();
        } else {
            isShopIn = true;
            save();
        }

    }


    /**
     * 检查和保存室外点的数据
     */
    private void checkOutWifiSignal() {

        List<WifiData> wifiDataList = mShopOutSignalBean.getWifiDataList();
        if (wifiDataList.isEmpty()) {
            //提示采集的信号数为0
            mAlertDialog = UIUtils.getAlertDialog(this, "提示", "店外采集点的信号个数为0\n请确认你的Wifi开关是否已经打开？", "我已经打开Wifi", "我还没打开WiFi", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAlertDialog.dismiss();
                    isShopIn = false;
                    save();
                }
            }, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAlertDialog.dismiss();
                }
            });
            mAlertDialog.show();
        } else {
            isShopIn = false;
            save();
        }
    }


    /**
     * 检查WiFi信号是否可用
     *
     * @param signalBean 信号
     */
    private void checkWifiSignal(SignalBean signalBean) {

        if (signalBean == null || signalBean.getWifiDataList() == null) {
            showTest("你还没有拍照，请拍照后保存…");
            return;
        }
        List<WifiData> wifiDataList = signalBean.getWifiDataList();
        if (wifiDataList.isEmpty()) {
            //提示采集的信号数为0
            mAlertDialog = UIUtils.getAlertDialog(this, "提示", "当前采集点的信号个数为0\n请确认你的Wifi开关是否已经打开？", "我已经打开Wifi", "我还没打开WiFi", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAlertDialog.dismiss();
                    save();
                }
            }, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAlertDialog.dismiss();
                }
            });
            mAlertDialog.show();
        } else {
            save();
        }
    }


    /**
     * 检查扩展属性有没有值
     */
    private boolean checkExpropValue() {

        //2017-06-27 排除对写字楼扩展属性的检测

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
    private void save() {
        if (TextUtils.isEmpty(mCollectType) || TextUtils.isEmpty(mCollectTypeCode)) {
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
            //商圈的第一个点需要经纬度 于2016-12-14 经黄、林总的方案改的,商圈的第一个点要提交经纬度
            if (mPointSize == 0) {
                LocationService.getInstance().start(new LocationService.LocationServiceListener() {
                    @Override
                    public void onLocationResult(double longitude, double latitude, String errMsg) {
                        updateLatLong(latitude, longitude);
                    }
                });
            } else {
                savePointAsync();
            }
        }

    }


    /**
     * 提交经纬度
     *
     * @param latitude  纬度
     * @param longitude 经度
     */
    private void updateLatLong(Double latitude, Double longitude) {

        mLoadingDialog.show();
        Map<String, Object> map = new HashMap<>();
        map.put("areaCode", mNmpReportData.getAreaCode());
        map.put("latitude", latitude);
        map.put("longitude", longitude);
        Subscription subscription = wrapObserverWithHttp(WorkService.getWorkService().updateLatAndLong(map)).subscribe(new Subscriber<DefaultResult>() {
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
            public void onNext(DefaultResult o) {
                mLoadingDialog.dismiss();
                Logger.e(new Gson().toJson(o));
                if (o.getRetCode() == 0) {
                    savePointAsync();
                } else {
                    showTest(o.getMsg());
                }
            }
        });

        addSubscription(subscription);

    }

    @OnClick(R.id.layout_point_type)
    public void pointTypeClick() {

        //显示更改采集点类型的对话框
        showSelectPointType("请选择正确的采集点类型");

    }


    /**
     * 显示采集点类型的对话框
     *
     * @param title 标题
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
                    mLayoutExprop.removeAllViews();
                    mCollectType = mCPs.get(position).getCpTypeName();
                    mCollectTypeCode = mCPs.get(position).getCpTypeCode();
                    tvPointType.setText(mCollectType);
                    //如果采集点的类型为店铺，出现点内店外采集按钮
                    mIv1.setScaleType(ImageView.ScaleType.CENTER);
                    if (mCollectType.trim().equals("店铺")) {
                        flIn.setVisibility(View.VISIBLE);
                        tvCollect.setVisibility(View.GONE);
                        mIv1.setImageResource(R.mipmap.ic_out_photo);
                        mIv2.setImageResource(R.mipmap.ic_in_photo);
                        mIv2.setVisibility(View.VISIBLE);
                        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mIv1.getLayoutParams();
                        layoutParams.gravity = Gravity.LEFT;
                        layoutParams.width = UIUtils.dip2px(160);
                        layoutParams.height = UIUtils.dip2px(120);
                        mIv1.setLayoutParams(layoutParams);
                        isShopInOut = true;
                    } else {
                        flIn.setVisibility(View.GONE);
                        tvCollect.setVisibility(View.VISIBLE);
                        mIv1.setImageResource(R.mipmap.ic_camera);
                        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mIv1.getLayoutParams();
                        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                        layoutParams.height = UIUtils.dip2px(160);
                        layoutParams.gravity = Gravity.LEFT;
                        mIv1.setLayoutParams(layoutParams);
                        mIv2.setVisibility(View.GONE);
                        mIv2.setImageResource(R.mipmap.ic_camera);
                        isShopInOut = false;
                    }

                    Exprop exprop = mMapExprop.get(mCollectTypeCode);
                    //每选一次扩展属性要清掉
                    mMapPointExprop.clear();
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
     * 因为原来是map现在改成list了
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
     * 异步保存点数据
     */
    private void savePointAsync() {

        Observable<String> observable = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                mNmpReortPoint = new NmpReportPoint();
                //mNmpReortPoint.setCreateTime(System.currentTimeMillis());
                mCurrentIndex++;
                if (isShopInOut) {
                    addShopInOutExprop(isShopIn);
                }
                mNmpReortPoint.setExPropList(exchangeMap2List(mMapPointExprop));
                if (mFloorNumber < 0) {
                    mNmpReortPoint.setPointIndex(mFloorNumber * 10000 - mCurrentIndex);
                } else {
                    mNmpReortPoint.setPointIndex(mFloorNumber * 10000 + mCurrentIndex);
                }
                mNmpReortPoint.setPointTypeCode(mCollectTypeCode);
                mNmpReortPoint.setFloorNumber(mFloorNumber);
                mNmpReortPoint.setPointType(mCollectType);
                mNmpReortPoint.setNmpReportData(mNmpReportData);
                List<WifiSignalItem> wifiSignalItems = new ArrayList<>();
                List<BaseStationBean> baseStationBeans = new ArrayList<>();
                //List<BleSignal> bleSignals = new ArrayList<>();
                MagneticBean magneticBean = null;
                SignalBean signalBean = null;
                List<PhotoUrl> photoUrls = null;
                //有拍照时的信号不为null就用拍照时的信号，不拍照的时候才用采集按钮采集的信号
                if (isShopInOut) {
                    mNmpReortPoint.setSamePageCode(samePageCode);
                    if (isShopIn) {
                        Logger.e("save shopIn");
                        //mNmpReortPoint.setPhotoUrls(mLtPhotosIn);
                        //mNmpReortPoint.setPhotoUrl(mPhotosIn);
                        photoUrls = mPhotosIn;
                        signalBean = mShopInSignalBean;

                    } else {
                        Logger.e("save shopOut");
                        //mNmpReortPoint.setPhotoUrls(mLtPhotosOut);
                        //mNmpReortPoint.setPhotoUrl(mPhotosOut);
                        photoUrls = mPhotosOut;
                        signalBean = mShopOutSignalBean;
                    }
                } else {
                    Logger.e("save mClickSignalBean");
                    signalBean = mTakePicSignalBean;
                    //mNmpReortPoint.setPhotoUrls(mLtPhotos);
                    //mNmpReortPoint.setPhotoUrl(mPhotos);
                    photoUrls = mPhotos;
                }

                for (WifiData wifiData : signalBean.getWifiDataList()) {
                    WifiSignalItem wifiSignalItem = WifiSignalItem.createFromWifiData(mNmpReortPoint, wifiData);
                    wifiSignalItems.add(wifiSignalItem);
                }

                if (signalBean.getBaseStationDataList() != null && !signalBean.getBaseStationDataList().isEmpty()) {
                    for (BaseStationData data : signalBean.getBaseStationDataList()) {
                        BaseStationBean baseStationBean = BaseStationBean.creatFromBaseStationData(mNmpReortPoint, data);
                        baseStationBeans.add(baseStationBean);
                    }
                }

               /* if (signalBean.getBluetoothData() != null && !signalBean.getBluetoothData().isEmpty()) {

                    for (IBeaconData data : signalBean.getBluetoothData()) {
                        BleSignal bleSignal = BleSignal.createFromBluetoothData(mNmpReortPoint, data);
                        bleSignals.add(bleSignal);
                    }
                }*/
                if (signalBean.getMagneticData() != null) {
                    magneticBean = MagneticBean.createFromMagneticData(mNmpReortPoint, signalBean.getMagneticData());
                }

                mNmpReortPoint.setCreateTime(signalBean.getCreateTime());
                mNmpReortPoint.setBaseStations(baseStationBeans);
                mNmpReortPoint.setSignals(wifiSignalItems);
                //mNmpReortPoint.setBleSignals(bleSignals);
                mNmpReortPoint.setPhotoList(photoUrls);
                mNmpReortPoint.save();
                DataSupport.saveAll(wifiSignalItems);
                DataSupport.saveAll(baseStationBeans);
                //DataSupport.saveAll(bleSignals);
                DataSupport.saveAll(photoUrls);
                if (magneticBean != null) {
                    magneticBean.save();
                }

                mPointSize++;
                mNmpReportData.setPointSize(mPointSize);
                mNmpReortPoint = null;
                //mLtPhotos.clear();
                //mLtPhotosIn.clear();
                mPhotos.clear();
                mPhotosIn.clear();

                if (!(isShopInOut && isShopIn)) {
                    mShopOutSignalBean = null;
                    //mLtPhotosOut.clear();
                    mPhotosOut.clear();
                }
                mShopInSignalBean = null;
                mTakePicSignalBean = null;
                subscriber.onNext(String.format("您当前已采集%d个点，正在对第%d个点进行采集", mPointSize, mPointSize + 1));
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

                if (isShopInOut && isShopIn) {

                    checkOutWifiSignal();
                    return;
                }

                mLoadingDialog.dismiss();
                mTvInfo.setText(result);
                mIv1.setScaleType(ImageView.ScaleType.CENTER);
                if (isShopInOut) {
                    mIv1.setImageResource(R.mipmap.ic_out_photo);
                    mIv2.setImageResource(R.mipmap.ic_in_photo);

                } else {
                    mIv1.setImageResource(R.mipmap.ic_camera);
                    mIv2.setImageResource(R.mipmap.ic_camera);
                }

                EventBus.getDefault().post(mNmpReportData);
                showTest("保存成功");
                cleanExprop();
                showSelectPointType("请确认并选择下一个采集点的点类型");
            }
        });

        addSubscription(subscription);

    }

    /**
     * 显示照片找不到对话框
     */
    private void showPhotoLostDialog() {

        mAlertDialog = UIUtils.getAlertDialog((Context) this, "提示", "照片文件找不到，请重新拍照", "确定", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlertDialog.dismiss();
            }
        });
        mAlertDialog.show();
    }


    private void cleanExprop() {
        mMapPointExprop.clear();
        for (int i = 0; i < mLayoutExprop.getChildCount(); i++) {
            View view = mLayoutExprop.getChildAt(i);
            TextView textView = (TextView) view.findViewById(R.id.tvIn);
            textView.setText("");
        }
    }


}
