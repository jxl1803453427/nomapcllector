package com.example.signalcollection.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.signalcollection.BaseApplication;
import com.example.signalcollection.R;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class UIUtils {

    /**
     * 获取应用的上下文对象
     *
     * @return
     */
    public static Context getContext() {
        return BaseApplication.getApplication();
    }


    /**
     * 时间戳转换时间
     *
     * @param time
     * @return
     */
    public static String convertDateTime(long time) {
        Date date = new Date(time);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return format.format(date);
    }


    /**
     * dip转换px
     */
    public static int dip2px(int dip) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f);
    }

    /**
     * px转换dip
     */
    public static int px2dip(int px) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }


    /**
     * 获取布局
     *
     * @param resId
     * @return
     */
    public static View inflate(Context context, int resId) {
        return LayoutInflater.from(context).inflate(resId, null);
    }


    public static String encryptToSHA(String info) {
        byte[] digesta = null;
        try {
            // 得到一个SHA-1的消息摘要
            MessageDigest alga = MessageDigest.getInstance("SHA-1");
            // 添加要进行计算摘要的信息
            alga.update(info.getBytes());
            // 得到该摘要
            digesta = alga.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        // 将摘要转为字符串
        String rs = byte2hex(digesta);
        return rs;
    }

    public static String byte2hex(byte[] b) {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs;
    }


    /**
     * 取消确定对话框
     *
     * @param activity
     * @param title
     * @param content
     * @param left
     * @param right
     * @param leftOnClick
     * @param rightOnClick
     * @return
     */
    public static Dialog getAlertDialog(Context activity, String title, CharSequence content, String left, String right, View.OnClickListener leftOnClick, View.OnClickListener rightOnClick) {
        Dialog dialog = new Dialog(activity, R.style.Style_Dialog);
        dialog.setContentView(R.layout.dialog_cancel_confirm);
        TextView titleView = (TextView) dialog.findViewById(R.id.tvTitle);
        TextView contentView = (TextView) dialog.findViewById(R.id.tvContent);
        TextView cancelView = (TextView) dialog.findViewById(R.id.tvCancle);
        TextView confirmView = (TextView) dialog.findViewById(R.id.tvConfirm);
        if (TextUtils.isEmpty(title)) {
            titleView.setVisibility(View.GONE);
        } else {
            titleView.setText(title);
        }
        contentView.setText(content);
        if (TextUtils.isEmpty(left)) {
            cancelView.setVisibility(View.GONE);
        } else {
            cancelView.setText(left);
        }
        confirmView.setText(right);
        cancelView.setOnClickListener(leftOnClick);
        confirmView.setOnClickListener(rightOnClick);
        return dialog;
    }


    public static Dialog getAlertDialog(Context activity, String title, CharSequence content) {
        Dialog dialog = new Dialog(activity, R.style.Style_Dialog);
        dialog.setContentView(R.layout.dialog_not_cancel);
        TextView titleView = (TextView) dialog.findViewById(R.id.tvTitle);
        TextView contentView = (TextView) dialog.findViewById(R.id.tvContent);
        if (TextUtils.isEmpty(title)) {
            titleView.setVisibility(View.GONE);
        } else {
            titleView.setText(title);
        }
        contentView.setText(content);
        return dialog;
    }


    /**
     * 确定对话框
     *
     * @param activity
     * @param title
     * @param content
     * @param right
     * @param leftOnClick
     * @return
     */
    public static Dialog getAlertDialog(Context activity, String title, CharSequence content, String right, final View.OnClickListener leftOnClick) {
        final Dialog dialog = new Dialog(activity, R.style.Style_Dialog);
        dialog.setContentView(R.layout.dialog_confirm);
        TextView titleView = (TextView) dialog.findViewById(R.id.tvTitle);
        TextView contentView = (TextView) dialog.findViewById(R.id.tvContent);
        TextView confirmView = (TextView) dialog.findViewById(R.id.tvConfirm);
        if (TextUtils.isEmpty(title)) {
            titleView.setVisibility(View.GONE);
        } else {
            titleView.setText(title);
        }
        contentView.setText(content);
        confirmView.setText(right);
        confirmView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (leftOnClick != null) {
                    leftOnClick.onClick(v);
                }
                dialog.dismiss();
            }
        });
        return dialog;
    }


    public interface EditResultInterface {
        void editText(String text);
    }


    /**
     * 编辑框的对话框
     *
     * @param activity
     * @param reset               带重置文本框的
     * @param editResultInterface
     * @return
     */
    public static Dialog getAlertDialogEdit(Activity activity, String reset, final EditResultInterface editResultInterface) {
        final Dialog dialog = new Dialog(activity, R.style.Style_Dialog);  //内存泄露的危险
        dialog.setContentView(R.layout.dialog_edit);
        final EditText et = (EditText) dialog.findViewById(R.id.et);
        et.setText(reset);
        TextView cancelView = (TextView) dialog.findViewById(R.id.tvCancle);
        TextView confirmView = (TextView) dialog.findViewById(R.id.tvConfirm);

        cancelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        confirmView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                editResultInterface.editText(et.getText().toString());
            }
        });
        return dialog;
    }


    /**
     * 不带重置文本框的
     *
     * @param activity
     * @param editResultInterface
     * @return
     */
    public static Dialog getAlertDialogEdit(String title, Activity activity, String reset, final EditResultInterface editResultInterface) {
        final Dialog dialog = new Dialog(activity, R.style.Style_Dialog);  //内存泄露的危险
        dialog.setContentView(R.layout.dialog_edit);
        TextView tvTitle = (TextView) dialog.findViewById(R.id.tvTitle);
        tvTitle.setText(title);
        final EditText et = (EditText) dialog.findViewById(R.id.et);
        et.setText(reset);
        TextView cancelView = (TextView) dialog.findViewById(R.id.tvCancle);
        TextView confirmView = (TextView) dialog.findViewById(R.id.tvConfirm);
        cancelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        confirmView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                editResultInterface.editText(et.getText().toString());
            }
        });
        return dialog;
    }


    /**
     * 不带重置文本框的
     *
     * @param notNull             返回的文字是否可以为null
     * @param activity            activity
     * @param editResultInterface activity
     * @return
     */
    public static Dialog getDialogEdit(final boolean notNull, String title, Activity activity, final EditResultInterface editResultInterface) {
        final Dialog dialog = new Dialog(activity, R.style.Style_Dialog);
        dialog.setContentView(R.layout.dialog_edit_not_null);
        final EditText et = (EditText) dialog.findViewById(R.id.et);
        TextView cancelView = (TextView) dialog.findViewById(R.id.tvCancle);
        TextView confirmView = (TextView) dialog.findViewById(R.id.tvConfirm);
        TextView tvTitle = (TextView) dialog.findViewById(R.id.tvTitle);
        final TextView tvErr = (TextView) dialog.findViewById(R.id.tv_err);
        tvErr.setVisibility(View.INVISIBLE);
        tvTitle.setText(title);

        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s)) {
                    tvErr.setVisibility(View.VISIBLE);
                } else {
                    tvErr.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        cancelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        confirmView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String result = et.getText().toString();
                if (notNull && TextUtils.isEmpty(result)) {
                    tvErr.setVisibility(View.VISIBLE);
                } else {
                    dialog.dismiss();
                    editResultInterface.editText(result);
                }


            }
        });
        return dialog;
    }


    /**
     * 多关键字查询表红,避免后面的关键字成为特殊的HTML语言代码
     *
     * @param str    检索结果
     * @param inputs 关键字集合
     * @param resStr 表红后的结果
     */
    public static void addChild(String str, List<String> inputs, StringBuffer resStr) {

        int index = str.length();//用来做为标识,判断关键字的下标
        String next = "";//保存str中最先找到的关键字
        for (int i = inputs.size() - 1; i >= 0; i--) {
            String theNext = inputs.get(i);
            int theIndex = str.indexOf(theNext);
            if (theIndex == -1) {//过滤掉无效关键字
                inputs.remove(i);
            } else if (theIndex <= index) {
                index = theIndex;//替换下标
                next = theNext;
            }
        }

        //如果条件成立,表示串中已经没有可以被替换的关键字,否则递归处理
        if (index == str.length()) {
            resStr.append(str);
        } else {
            resStr.append(str.substring(0, index));
            resStr.append("<font color='#FF0000'>");
            resStr.append(str.substring(index, index + next.length()));
            resStr.append("</font>");
            String str1 = str.substring(index + next.length(), str.length());
            addChild(str1, inputs, resStr);//剩余的字符串继续替换
        }
    }
}
