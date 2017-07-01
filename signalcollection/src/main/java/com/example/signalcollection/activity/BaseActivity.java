package com.example.signalcollection.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.signalcollection.R;
import com.example.signalcollection.util.UIUtils;
import com.orhanobut.logger.Logger;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;


public abstract class BaseActivity extends AppCompatActivity {


    Toolbar mToolbar;
    FrameLayout mFlytContent;
    public View mSuccessView;
    FrameLayout.LayoutParams mBaseLayoutParams;
    TextView mTvTitle;
    TextView mTvRight;
    private CompositeSubscription mCompositeSubscription;
    public String mNetWorkError = "请检查网络";
    private Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Logger.i(getClass().getName() + " onCreate");
        setContentView(R.layout.activity_base);
        mTvRight = (TextView) findViewById(R.id.tvRight);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTvTitle = (TextView) findViewById(R.id.tvTitle);
        mTvRight = (TextView) findViewById(R.id.tvRight);
        mFlytContent = (FrameLayout) findViewById(R.id.flytContent);
        setSupportActionBar(mToolbar);
        mBaseLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        int successLayoutId = createSuccessView();

        mSuccessView = UIUtils.inflate(this, successLayoutId); //可以试一下inflate加入父布局
        unbinder = ButterKnife.bind(this, mSuccessView);
        setTitle("");
        init();
        mSuccessView.setLayoutParams(mBaseLayoutParams);
        if (null != mFlytContent) {
            mFlytContent.addView(mSuccessView);
        }

    }

    public View getSuccessView() {
        return mSuccessView;
    }

    public void setMyTitle(String title) {
        mTvTitle.setText(title);
    }

    public void setBackColor(String color) {
        mToolbar.getNavigationIcon().setColorFilter(Color.parseColor(color), PorterDuff.Mode.SRC_ATOP);
    }

    public void setTitleColor(String color) {
        mTvTitle.setTextColor(Color.parseColor(color));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return super.dispatchTouchEvent(event);
    }

    public TextView showRight(String text, View.OnClickListener onClickListener) {
        mTvRight.setVisibility(View.VISIBLE);
        mTvRight.setText(text);
        mTvRight.setOnClickListener(onClickListener);
        return mTvRight;
    }


    public void hideRightText() {
        mTvRight.setVisibility(View.GONE);
    }

    public void setTitleBackGround(int drawableId) {
        mToolbar.setBackgroundResource(drawableId);
    }

    public TextView showRight() {
        mTvRight.setVisibility(View.VISIBLE);
        return mTvRight;
    }

    public void showBack() {
        mToolbar.setNavigationIcon(R.mipmap.ic_back);
        setBackColor("#ffffff");
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    public void showBack(int resId) {
        mToolbar.setNavigationIcon(resId);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    public void addSubscription(Subscription s) {
        if (this.mCompositeSubscription == null) {
            this.mCompositeSubscription = new CompositeSubscription();
        }

        this.mCompositeSubscription.add(s);
    }

    public abstract void init();

    public void showSoftInput(EditText editText) {
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
    }

    public void hideSoftInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean isOpen = imm.isActive();
        if (isOpen) {
            if (this.getCurrentFocus() != null)
                imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0); // InputMethodManager.HIDE_NOT_ALWAYS
        }
    }

    public void hideActionBar() {
        if (getSupportActionBar() != null && getSupportActionBar().isShowing()) {
            getSupportActionBar().hide();
        }
    }

    public void showActionBar() {
        if (getSupportActionBar() != null && !getSupportActionBar().isShowing()) {
            getSupportActionBar().show();
        }
    }


    public abstract int createSuccessView();

    /**
     * 为fragment设置functions，具体实现子类来做
     *
     * @param fragmentTag
     */
    public void setFunctionsForFragment(String fragmentTag) {
    }

    private Snackbar mSnackbar;

    public void showTest(String text) {
        mSnackbar = Snackbar.make(mFlytContent, text, Snackbar.LENGTH_SHORT);
        setSnackbarMessageTextColor(mSnackbar, Color.WHITE);
        mSnackbar.show();
    }

    public void setSnackbarMessageTextColor(Snackbar snackbar, int color) {
        View view = snackbar.getView();
        ((TextView) view.findViewById(R.id.snackbar_text)).setTextColor(color);
    }


    public void showTest(View view, String text, int time, String action, View.OnClickListener onClickListener) {
        mSnackbar = Snackbar.make(view, text, time).setAction(action, onClickListener);
        setSnackbarMessageTextColor(mSnackbar, Color.WHITE);
        mSnackbar.show();
    }


    private boolean mIsOpenLog = true;

    public void l(String log) {
        if (mIsOpenLog) {
            Logger.t(this.getClass().getSimpleName() + "1").i(log);
        }
    }

    public void baseStartActivity(Class intentClass) {
        Intent intent = new Intent(this, intentClass);
        startActivity(intent);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        Logger.i(getClass().getName() + "on Destroy");
        if (this.mCompositeSubscription != null) {
            this.mCompositeSubscription.unsubscribe();
        }
    }

    public Observable wrapObserverWithHttp(Observable observable) {
        return observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }
}
