package com.example.signalcollection.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.example.signalcollection.Constans;
import com.example.signalcollection.R;
import com.example.signalcollection.bean.EventBusMessage;
import com.example.signalcollection.recyclerview.ViewHolder;
import com.example.signalcollection.recyclerview.CommonAdapter;
import com.example.signalcollection.recyclerview.OnItemClickListener;
import com.example.signalcollection.bean.NmpReportData;
import com.example.signalcollection.bean.NmpReportPoint;
import com.example.signalcollection.view.LoadingDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.litepal.crud.DataSupport;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class HistoryCollectedActivity extends BaseActivity {

    @BindView(R.id.list)
    RecyclerView mList;
    private List<NmpReportPoint> mLt = new ArrayList<>();
    private CommonAdapter<NmpReportPoint> mCommonAdapter;
    private NmpReportData mNmpReportData;
    private Gson mGson = new Gson();

    private LoadingDialog mLoadingDialog;

    private int mCurrentIndex;


    private Handler mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            if (msg.what == 1004) {
                mLt = mNmpReportData.getPoints();
                mCommonAdapter.setData(mLt);
            }
            return false;
        }
    });

    @Override
    public void init() {

        Intent intent = getIntent();
        loadData(intent.getIntExtra(Constans.TASK_ID, 0));
        mCurrentIndex = intent.getIntExtra(Constans.POINT_INDEX, 0);
        showBack();
        setMyTitle("采集历史");
        mLoadingDialog = new LoadingDialog(this);
        EventBus.getDefault().register(this);
        mCommonAdapter = new CommonAdapter<NmpReportPoint>(this, R.layout.rvitem_historycollected, mLt) {
            @Override
            public void convert(ViewHolder holder, NmpReportPoint o) {
                holder.setText(R.id.tvSerial, o.getPointIndex() + "");
                holder.setText(R.id.tvType, o.getPointType());
                holder.setImageResource(R.id.iv_has_take_img, R.mipmap.ic_selected);
                //holder.setText(R.id.tvName, o.getPointName());
               /* if ((o.getPhotoUrls() == null || o.getPhotoUrls().isEmpty()) && !TextUtils.isEmpty(o.getStrPhotoUrls())) {
                    o.setPhotoUrls(mGson.fromJson(o.getStrPhotoUrls(), List.class));
                }

                if (o.getPhotoUrls() == null || o.getPhotoUrls().isEmpty()) {

                    holder.setImageResource(R.id.iv_has_take_img, R.mipmap.ic_null);
                } else {
                    holder.setImageResource(R.id.iv_has_take_img, R.mipmap.ic_selected);
                }*/
            }
        };
        mList.setAdapter(mCommonAdapter);
        mCommonAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(ViewGroup parent, View view, Object o, int position, ViewHolder viewHolder) {
                Intent intent = new Intent(HistoryCollectedActivity.this, HistoryDetailActivity.class);
                intent.putExtra(Constans.AREA_DATA_ID, mNmpReportData.getId());
                intent.putExtra(Constans.POINT_DATA_ID, ((NmpReportPoint) o).getId());
                intent.putExtra(Constans.POINT_COUNT, mLt.size());
                intent.putExtra(Constans.POINT_INDEX, mCurrentIndex);
                startActivity(intent);
            }

            @Override
            public boolean onItemLongClick(ViewGroup parent, View view, Object o, int position, ViewHolder viewHolder) {
                return false;
            }
        });
    }


    private void loadData(final int id) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mNmpReportData = DataSupport.find(NmpReportData.class, id);
                //要进行点归并，把店铺（店外点、店内点归并成一个点,只显示店内点）
                List<NmpReportPoint> mNmpReportPoints = DataSupport.where("remark<>1 AND nmpreportdata_id =" + id).find(NmpReportPoint.class);
                List<NmpReportPoint> mNmpReportPoints2 = new ArrayList<NmpReportPoint>(mNmpReportPoints);
                int i = 0;
                int[] indexs = new int[mNmpReportPoints.size()];
                Type type = new TypeToken<List<NmpReportPoint.Exprop>>() {
                }.getType();
                for (NmpReportPoint point : mNmpReportPoints2) {
                    i++;
                    String samePointCode = point.getSamePageCode();
                    if (!TextUtils.isEmpty(samePointCode)) {
                        //找到另外一个点
                        for (int j = i; j < mNmpReportPoints2.size(); j++) {
                            NmpReportPoint point2 = mNmpReportPoints2.get(j);
                            String samePointCode2 = point2.getSamePageCode();
                            if (!TextUtils.isEmpty(samePointCode2) && samePointCode2.equals(samePointCode)) {
                                List<NmpReportPoint.Exprop> exprops = mGson.fromJson(point2.getStrExprop(), type);
                                for (NmpReportPoint.Exprop exprop : exprops) {
                                    if (exprop.getPropValue().equals("店外")) {
                                        indexs[j] = 1;
                                    } else {
                                        indexs[i] = 1;
                                    }
                                }
                            }
                        }
                    }
                }
                List<NmpReportPoint> points = new ArrayList<NmpReportPoint>();
                for (int k = 0; k < indexs.length; k++) {
                    if (indexs[k] != 1) {
                        points.add(mNmpReportPoints.get(k));
                    }
                }
                mNmpReportData.setPoints(points);
                mHandler.sendEmptyMessage(1004);
            }
        }).start();
    }


    @Subscribe
    public void showData(EventBusMessage data) {

        if (data.what == Constans.EVENBUS_MESSAGE_CODE_RECEIVED_POINT_INDEX) {
            mCurrentIndex = (Integer) data.data;
            loadData(mNmpReportData.getId());
        }
    }


    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public int createSuccessView() {
        return R.layout.activity_history_collected;
    }
}
