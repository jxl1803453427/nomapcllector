package com.example.signalcollection;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.example.signalcollection.activity.MainActivity;
import com.example.signalcollection.bean.MessageBean;
import com.example.signalcollection.bean.NmpReportData;
import com.example.signalcollection.bean.NmpReportPoint;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import org.litepal.crud.DataSupport;

import java.util.List;

import cn.jpush.android.api.JPushInterface;

/**
 * 接收推送消息的广播接收器
 * Created by Konmin on 2016/11/1.
 */

public class MessageReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();

        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
            Logger.d("[MyReceiver] 接收Registration Id : " + regId);
            //send the Registration Id to your server...

        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
            Logger.d("[MyReceiver] 接收到推送下来的自定义消息: " + bundle.getString(JPushInterface.EXTRA_MESSAGE));
            processCustomMessage(context, bundle);

        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            Logger.d("[MyReceiver] 接收到推送下来的通知");
            int notifactionId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
            Logger.d("[MyReceiver] 接收到推送下来的通知的ID: " + notifactionId);

        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            Logger.d("[MyReceiver] 用户点击打开了通知");

            //打开自定义的Activity
            //Intent i = new Intent(context, TestActivity.class);
            //i.putExtras(bundle);
            //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            //context.startActivity(i);

        } else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {
            Logger.d("[MyReceiver] 用户收到到RICH PUSH CALLBACK: " + bundle.getString(JPushInterface.EXTRA_EXTRA));
            //在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity， 打开一个网页等..

        } else if (JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {
            boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);
            Logger.w("[MyReceiver]" + intent.getAction() + " connected state change to " + connected);
        } else {
            Logger.d("[MyReceiver] Unhandled intent - " + intent.getAction());
        }


    }

    private void processCustomMessage(Context context, Bundle bundle) {
        //发送广播到主界面显示那个点
        String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
        Logger.i("收到的推送消息：" + message);
        Gson gson = new Gson();
        MessageBean messageBean = gson.fromJson(message, MessageBean.class);
        if (messageBean != null) {
            messageBean.setRececiveTime(System.currentTimeMillis());
            messageBean.save();
            //修改那条上传数据的状态
            ContentValues values = new ContentValues();
            values.put("status", 0);
            int i = DataSupport.updateAll(NmpReportData.class, values, "areacode = ?", messageBean.getAreaCode());
            Logger.i("我更新了" + i + "条数据");
            Intent intent = new Intent(Constans.MASSAGE_IN);
            intent.putExtra("message", messageBean);
            intent.putExtra("type", messageBean.getPushType());
            context.sendBroadcast(intent);
        } else {
            Logger.i("message is null");
        }


    }


}
