package com.example.signalcollection.view;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.signalcollection.R;
import com.example.signalcollection.util.SPUtils;
import com.orhanobut.logger.Logger;
import com.yyc.mcollector.SignalCollector;
import com.yyc.mcollector.bean.WifiData;
import com.yyc.mcollector.listener.WifiScanResultListener;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;

/**
 * 检查手机是否可用的dialog
 * Created by Konmin on 2017/6/13.
 */

public class CheckCanUseDialog extends Dialog implements View.OnClickListener {


    private final Animation mOperatingAnim;

    private ImageView ivProgress;
    private TextView tvTip;
    private TextView tvMemory;
    private TextView tvSignal;
    private TextView tvSignalNormal;
    private TextView tvSure;
    private LinearLayout layoutDoubleBtn;
    private TextView tvCheckAgain;
    private TextView tvGiveUpCheck;

    private Handler mHandler;
    private Runnable mTimeOutRunnable;
    private List<WifiData> mWifiDataList;
    private Drawable yesDrawable;
    private Drawable noDrawable;
    private CheckOverListener mCheckOverListener;

    private void assignViews() {
        ivProgress = (ImageView) findViewById(R.id.iv_progress);
        tvTip = (TextView) findViewById(R.id.tv_tip);
        tvMemory = (TextView) findViewById(R.id.tv_memory);
        tvSignal = (TextView) findViewById(R.id.tv_signal);
        tvSignalNormal = (TextView) findViewById(R.id.tv_signal_normal);
        tvSure = (TextView) findViewById(R.id.tv_sure);
        layoutDoubleBtn = (LinearLayout) findViewById(R.id.layout_double_btn);
        tvCheckAgain = (TextView) findViewById(R.id.tv_check_again);
        tvGiveUpCheck = (TextView) findViewById(R.id.tv_give_up_check);
        tvGiveUpCheck.setOnClickListener(this);
        tvCheckAgain.setOnClickListener(this);
        tvSure.setOnClickListener(this);
    }


    public CheckCanUseDialog(@NonNull Context context, CheckOverListener listener) {
        super(context, R.style.Style_Dialog);
        setContentView(R.layout.dialog_check_can_use);
        assignViews();
        mCheckOverListener = listener;
        mOperatingAnim = AnimationUtils.loadAnimation(context, R.anim.anim_rotate);
        AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
        mOperatingAnim.setInterpolator(interpolator);
        mHandler = new Handler();
        yesDrawable = getContext().getResources().getDrawable(R.mipmap.ic_selected);
        yesDrawable.setBounds(0, 0, yesDrawable.getMinimumWidth(), yesDrawable.getMinimumHeight());
        noDrawable = getContext().getResources().getDrawable(R.mipmap.ic_warm);
        noDrawable.setBounds(0, 0, noDrawable.getMinimumWidth(), noDrawable.getMinimumHeight());
        setCancelable(false);
    }

    boolean canUse = true;

    @Override
    public void show() {
        super.show();
        startCheck();
    }


    private void startCheck() {
        canUse = true;
        ivProgress.setImageResource(R.mipmap.ic_progress);
        ivProgress.startAnimation(mOperatingAnim);
        tvTip.setTextColor(getContext().getResources().getColor(R.color.black));
        layoutDoubleBtn.setVisibility(View.INVISIBLE);
        tvMemory.setCompoundDrawables(null, null, null, null);
        tvSignal.setCompoundDrawables(null, null, null, null);
        tvSignalNormal.setCompoundDrawables(null, null, null, null);
        tvTip.setText("正在检测手机是否可用于采集…");
        tvSignalNormal.setText("手机采集的信号是否正常");
        tvMemory.setText("手机的RAM是否满足条件");
        tvSignal.setText("手机是否能扫描到信号");
        tvSignal.setTextColor(getContext().getResources().getColor(R.color.gray));
        tvMemory.setTextColor(getContext().getResources().getColor(R.color.gray));
        tvSignalNormal.setTextColor(getContext().getResources().getColor(R.color.gray));
        tvSure.setVisibility(View.GONE);
        checkMemory(new CheckOverListener() {
            @Override
            public void over(boolean flag) {
                if (flag) {
                    tvMemory.setText("手机的内存满足采集条件");
                    tvMemory.setTextColor(getContext().getResources().getColor(R.color.gray));
                    tvMemory.setCompoundDrawables(null, null, yesDrawable, null);
                } else {
                    tvMemory.setCompoundDrawables(null, null, noDrawable, null);
                    tvMemory.setTextColor(getContext().getResources().getColor(R.color.yellow));
                    tvMemory.setText("手机的RAM低于2GB,会对采集操作影响，比如卡顿,采集信号不成功等");
                    //canUse = false;
                }

                tvSignal.setText("正在检查手机是否可以采集信号…");
                tvSignal.setTextColor(getContext().getResources().getColor(R.color.black));
                checkWifi(new CheckOverListener() {
                    @Override
                    public void over(boolean flag) {
                        if (!flag) {
                            canUse = false;
                        } else {
                            tvSignal.setText("手机可以扫描到信号列表");
                            tvSignal.setTextColor(getContext().getResources().getColor(R.color.gray));
                            tvSignal.setCompoundDrawables(null, null, yesDrawable, null);
                        }
                        checkWifiCouldUsed(new CheckOverListener() {
                            @Override
                            public void over(boolean flag) {
                                if (flag) {
                                    tvSignalNormal.setText("手机扫描到的信号可用");
                                    tvSignalNormal.setTextColor(getContext().getResources().getColor(R.color.gray));
                                    tvSignalNormal.setCompoundDrawables(null, null, yesDrawable, null);
                                } else {
                                    canUse = false;
                                    tvSignalNormal.setText("手机扫描到的信号不可用");
                                    tvSignalNormal.setTextColor(getContext().getResources().getColor(R.color.red));
                                    tvSignalNormal.setCompoundDrawables(null, null, noDrawable, null);
                                }

                                ivProgress.clearAnimation();
                                if (canUse) {
                                    tvSure.setVisibility(View.VISIBLE);
                                    layoutDoubleBtn.setVisibility(View.GONE);

                                    ivProgress.setImageResource(R.mipmap.ic_select);
                                    tvTip.setText("恭喜你！你的手机可以用于采集");

                                } else {
                                    tvSure.setVisibility(View.GONE);
                                    ivProgress.setImageResource(R.mipmap.ic_warm);
                                    tvTip.setText("很遗憾！你的手机不能用于采集！");
                                    tvTip.setTextColor(getContext().getResources().getColor(R.color.red));
                                    layoutDoubleBtn.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                });
            }
        });


    }


    /**
     * 检查手机内存
     *
     * @param listener
     */
    private void checkMemory(final CheckOverListener listener) {
        tvMemory.setTextColor(getContext().getResources().getColor(R.color.black));
        tvMemory.setText("正在检查内存是否满足要求…");
        new Thread(new Runnable() {
            @Override
            public void run() {
                int ramSize = getTotalRam();
                if (ramSize >= 2) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            listener.over(true);
                        }
                    }, 1000);

                } else {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            listener.over(false);
                        }
                    }, 1000);
                }
            }
        }).start();
    }


    private void checkWifi(final CheckOverListener listener) {

        tvSignal.setTextColor(getContext().getResources().getColor(R.color.black));
        tvSignal.setText("正在检查是否可以扫描到信号……");
        WifiManager wifiManager = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);//强制打开WiFi
        }

        final SignalCollector signalCollector = new SignalCollector(getContext());
        signalCollector.setCollectWifi(new WifiScanResultListener() {
            @Override
            public void scanResult(List<WifiData> list) {
                if (list == null || list.isEmpty()) {
                    signalCollector.collectWifi();
                } else {
                    mHandler.removeCallbacks(mTimeOutRunnable);
                    mWifiDataList = list;
                    signalCollector.onDestroy();
                    listener.over(true);
                }
            }
        });
        signalCollector.collectWifi();
        mTimeOutRunnable = new TimeOutRunnable(signalCollector, listener);
        mHandler.postDelayed(mTimeOutRunnable, 15 * 1000);
    }


    private void checkWifiCouldUsed(final CheckOverListener listener) {

        tvSignalNormal.setText("正在检查信号是否可用…");
        tvSignalNormal.setTextColor(getContext().getResources().getColor(R.color.black));
        if (mWifiDataList == null || mWifiDataList.isEmpty()) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    listener.over(false);
                }
            }, 1000);
        } else {
            boolean couldUsed = true;
            for (WifiData data : mWifiDataList) {
                if (data.getMac().toUpperCase().equals("12:34:56:78:9A:BC")) {
                    couldUsed = false;
                    break;
                }
            }
            final boolean cu = couldUsed;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    listener.over(cu);
                }
            }, 1000);
        }

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.tv_sure:
                setResult(true);
                dismiss();
                break;
            case R.id.tv_give_up_check:
                setResult(false);
                dismiss();
                break;
            case R.id.tv_check_again:
                startCheck();
                break;
        }
    }


    private void setResult(boolean flag) {


        if (null != mCheckOverListener) {
            mCheckOverListener.over(flag);
        }
    }


    private class TimeOutRunnable implements Runnable {

        SignalCollector collector;
        CheckOverListener listener;

        TimeOutRunnable(SignalCollector collector, CheckOverListener listener) {
            this.collector = collector;
            this.listener = listener;
        }

        @Override
        public void run() {
            tvSignal.setTextColor(getContext().getResources().getColor(R.color.red));
            tvSignal.setCompoundDrawables(null, null, noDrawable, null);
            tvSignal.setText("信号扫描超时！请检查系统是否授予应用的定位权限");
            collector.onDestroy();
            listener.over(false);
        }
    }


    /**
     * 获取手机内存
     *
     * @return 获取手机ram信息
     */
    private static int getTotalRam() {
        String path = "/proc/meminfo";
        String firstLine = null;
        int totalRam = 0;
        try {
            FileReader fileReader = new FileReader(path);
            BufferedReader br = new BufferedReader(fileReader, 8192);
            firstLine = br.readLine().split("\\s+")[1];
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (firstLine != null) {
            totalRam = (int) Math.ceil((new Float(Float.valueOf(firstLine) / (1024 * 1024)).doubleValue()));
        }

        return totalRam;//返回1GB/2GB/3GB/4GB
    }


    public interface CheckOverListener {

        void over(boolean flag);
    }


}
