package com.example.signalcollection.network;


import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.example.signalcollection.util.SPUtils;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by hehe on 2016/4/18.
 */
public class RetrofitUtil {
    private static Retrofit mInstance;
    public static final String API_URL = "http://jmtool.papakaka.com:81/JMTool/";
    //public static final String API_URL = "http://116.62.95.29:81/JMTool/";
    //public static final String API_URL = "http://jtest.papakaka.com:81/JMTool/";
    //public static final String API_URL = "http://192.168.0.59:8080/JMTool/";
    //public static final String API_URL = "http://192.168.0.201:8080/";

    //public static final String API_URL = "http://192.168.0.200:8080/";
    //public static final String API_URL = "http://jmtool.papakaka.com:58080/JMTool/";
    //public static final String API_URL = "http://jmtool.papakaka.com:48080/JMTool/";
    public final static String PHOTO_BASE_URL = "http://jmtool3.jjfinder.com/";



    public static Retrofit getInstance() {
        if (mInstance == null) {
            synchronized (RetrofitUtil.class) {
                if (mInstance == null) {
                    String baseurl = (String) SPUtils.get("baseUrl", "");
                    if (TextUtils.isEmpty(baseurl)) {
                        baseurl = API_URL;
                    }
                    OkHttpClient client = new OkHttpClient.Builder().addNetworkInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request request = chain.request();
                            Response response = chain.proceed(request);
                            return response;
                        }
                    }).connectTimeout(60, TimeUnit.SECONDS).build();
                    Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                    mInstance = new Retrofit.Builder().baseUrl(baseurl).addConverterFactory(GsonConverterFactory.create(gson)).addConverterFactory(GsonConverterFactory.create()).addCallAdapterFactory(RxJavaCallAdapterFactory.create()).client(client).build();
                }
            }
        }
        return mInstance;
    }


    private static String bodyToString(final RequestBody request) {

        try {
            final Buffer buffer = new Buffer();
            request.writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "did not work";
        }
    }


    public static void resetServerUrl(String url) {
        SPUtils.put("baseUrl", url);
        if (mInstance != null) {
            mInstance = null;
            getInstance();
        }
    }

}
