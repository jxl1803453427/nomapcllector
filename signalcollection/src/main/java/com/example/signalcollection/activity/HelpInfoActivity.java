package com.example.signalcollection.activity;

import android.text.TextUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.signalcollection.R;
import com.example.signalcollection.util.SPUtils;
import com.example.signalcollection.network.RetrofitUtil;
import com.orhanobut.logger.Logger;

import butterknife.BindView;

/**
 *
 * Created by Konmin on 2016/9/10.
 */
public class HelpInfoActivity extends BaseActivity {


    @BindView(R.id.webView)
    WebView mWebView;

    String baseUrl;

    @Override
    public void init() {
        showBack();
        baseUrl = (String) SPUtils.get("baseUrl", "");
        if (TextUtils.isEmpty(baseUrl)) {
            baseUrl = RetrofitUtil.API_URL;
        }

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
        // settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);


        String title = getIntent().getStringExtra("info");
        setMyTitle(title);
        switch (title) {
            case "基本操作":
                Logger.i("基本操作");
                mWebView.loadUrl(baseUrl + "html/h1.html");
                break;
            case "采集任务规范":
                mWebView.loadUrl(baseUrl + "html/h2.html");
                break;
            case "特殊案例":
                mWebView.loadUrl(baseUrl + "html/h3.html");
                break;
        }


    }

    @Override
    public int createSuccessView() {
        return R.layout.activity_help_info;
    }
}
