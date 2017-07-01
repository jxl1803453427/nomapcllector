package com.example.signalcollection.util;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.signalcollection.R;
import com.example.signalcollection.bean.NmpReportPoint;
import com.example.signalcollection.bean.Predefine;
import com.example.signalcollection.recyclerview.CommonAdapter;
import com.example.signalcollection.recyclerview.SpinnerAdapter;
import com.example.signalcollection.recyclerview.ViewHolder;

import java.util.List;

/**
 * 各种对话框
 * Created by Konmin on 2016/9/28.
 */

public class DialogUtils {


    private Dialog mSelectDialog; //选择扩展属性类型的对话框
    private Dialog mEditDialog; //编辑类型的对话框

    private Dialog mMultSelectDialog;//多选框扩展属性的对话框

    private Context mContext;

    private SpinnerAdapter<Predefine.Data> mPredefineSpinnerAdapter;

    //private CommonAdapter<Predefine.Data> mCommonAdapter;


    public DialogUtils(Context context) {
        mContext = context;
        mPredefineSpinnerAdapter = new SpinnerAdapter<Predefine.Data>(mContext) {
            @Override
            public void setText(TextView textView, Predefine.Data data) {
                textView.setText(data.getTagName());
            }
        };

        /*mCommonAdapter = new CommonAdapter<Predefine.Data>(mContext, R.layout.item_multiselect) {
            @Override
            public void convert(ViewHolder holder, Predefine.Data data) {
                holder.setText(R.id.tv_content, data.getTagName());
            }
        };*/
    }


    /**
     * 选择类型的扩展属性对话框
     *
     * @param title    title
     * @param listener listener
     */
    public void showSelectExpropDialog(final String title, List<Predefine.Data> dataList, @NonNull final SpinnerItemSelectedListener listener) {
        if (mSelectDialog == null) {
            mSelectDialog = new Dialog(mContext, R.style.Style_Dialog);
            mSelectDialog.setContentView(R.layout.dialog_collection_type);
            mSelectDialog.setCancelable(false);
            TextView tvConfirm = (TextView) mSelectDialog.findViewById(R.id.tvConfirm);
            tvConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSelectDialog.dismiss();
                }
            });
        }

        Spinner spinner = (Spinner) mSelectDialog.findViewById(R.id.sp_point_type);
        spinner.setAdapter(mPredefineSpinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Predefine.Data data = (Predefine.Data) parent.getAdapter().getItem(position);
                final NmpReportPoint.Exprop exprop = new NmpReportPoint.Exprop();
                if (data.getNeedOtherInput() == 1) {
                    mSelectDialog.dismiss();
                    showEditDialog("请输入" + title.substring(3), null, true, new EditResultListener() {
                        @Override
                        public void resultText(String text) {
                            exprop.setPropValue(text);
                            exprop.setIsOtherInput(1);
                            listener.onItemSelect(exprop);
                        }
                    });
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


        TextView tvTitle = (TextView) mSelectDialog.findViewById(R.id.tvTitle);
        tvTitle.setText(title);
        mPredefineSpinnerAdapter.setListData(dataList);
        mSelectDialog.show();
    }
















    /**
     * 编辑类型的对话框
     *
     * @param title              title
     * @param content            content
     * @param shouldNotNull      shouldNotNull
     * @param editResultListener editResultListener
     */
    public void showEditDialog(String title, String content, final boolean shouldNotNull, @NonNull final EditResultListener editResultListener) {
        if (mEditDialog == null) {
            mEditDialog = new Dialog(mContext, R.style.Style_Dialog);
            mEditDialog.setContentView(R.layout.dialog_edit_not_null);
            TextView cancelView = (TextView) mEditDialog.findViewById(R.id.tvCancle);
            cancelView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mEditDialog.dismiss();
                }
            });

        }
        TextView tvTitle = (TextView) mEditDialog.findViewById(R.id.tvTitle);
        final TextView tvErr = (TextView) mEditDialog.findViewById(R.id.tv_err);
        final EditText et = (EditText) mEditDialog.findViewById(R.id.et);
        TextView confirmView = (TextView) mEditDialog.findViewById(R.id.tvConfirm);

        if (TextUtils.isEmpty(content)) {
            et.setText(content);
        }
        tvErr.setVisibility(View.INVISIBLE);
        tvTitle.setText(title);
        if (shouldNotNull) {
            et.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    if (!TextUtils.isEmpty(s)) {
                        tvErr.setVisibility(View.INVISIBLE);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }
        confirmView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String result = et.getText().toString();
                if (shouldNotNull && TextUtils.isEmpty(result)) {
                    tvErr.setVisibility(View.VISIBLE);
                } else {
                    mEditDialog.dismiss();
                    editResultListener.resultText(result);
                }


            }
        });
        mEditDialog.show();
    }


    /**
     * 多选型扩展属性对话框
     */
    public void showMultSelectExpropDialog() {

        if (mMultSelectDialog == null) {
            mMultSelectDialog = new Dialog(mContext, R.style.Style_Dialog);
            mMultSelectDialog.setContentView(R.layout.dialog_multiselect);
        }

    }


    /**
     * 编辑类型的对话框的返回监听
     */
    public interface EditResultListener {
        void resultText(String text);
    }

    /**
     * 选择类型的对话框的回调
     */
    public interface SpinnerItemSelectedListener {
        void onItemSelect(NmpReportPoint.Exprop exprop);
    }
}
