package com.example.signalcollection.activity;

import android.text.TextUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.signalcollection.Constans;
import com.example.signalcollection.R;
import com.orhanobut.logger.Logger;

import butterknife.BindView;

/**
 * Created by Konmin on 2016/11/8.
 */

public class WebActivity extends BaseActivity {

    @BindView(R.id.webView)
    WebView mWebView;


    @Override
    public void init() {
        showBack();
        String title =getIntent().getStringExtra(Constans.TITLE);
        setMyTitle(title);
        String url = getIntent().getStringExtra(Constans.URL);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Logger.i(errorCode + ":" + description);
                super.onReceivedError(view, errorCode, description, failingUrl);

            }
        });

        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        //settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);// webview缓存的用法
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setDomStorageEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setDefaultTextEncodingName("UTF-8");
        //settings.setAllowUniversalAccessFromFileURLs(true);
        //settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        if (!TextUtils.isEmpty(url)) {
            mWebView.loadUrl(url);
        }
    }

    @Override
    public int createSuccessView() {
        return R.layout.activity_web;
    }
}
