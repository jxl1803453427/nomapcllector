package com.example.signalcollection.view;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.signalcollection.R;
import com.example.signalcollection.bean.MessageBean;
import com.example.signalcollection.util.UIUtils;

/**
 * 消息阅读的对话框
 * Created by Konmin on 2016/11/2.
 */

public class MessageDetailDialog extends Dialog {


    private TextView tvTitle;
    private TextView tvContent;
    private TextView tvDateTime;
    private TextView tvConfirm;

    public MessageDetailDialog(Context context) {
        super(context, R.style.Style_Dialog);
        setContentView(R.layout.dialog_msg_detial);
        tvTitle = (TextView) findViewById(R.id.tv_msg_title);
        tvContent = (TextView) findViewById(R.id.tv_msg_content);
        tvDateTime = (TextView) findViewById(R.id.tv_datetime);
        tvConfirm = (TextView) findViewById(R.id.tv_confirm);

        Window window = getWindow();
        WindowManager manager = window.getWindowManager();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = manager.getDefaultDisplay().getWidth() - 30;
        window.setAttributes(params);

    }


    public void show(MessageBean bean, View.OnClickListener listener) {
        tvTitle.setText("["+bean.getAreaName()+"]"+bean.getTitle());
        tvContent.setText(bean.getContent());
        tvDateTime.setText(UIUtils.convertDateTime(bean.getRececiveTime()));
        if (listener != null) {
            tvConfirm.setOnClickListener(listener);
        }
        show();
    }


}
