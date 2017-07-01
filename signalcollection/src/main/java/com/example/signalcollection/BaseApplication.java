package com.example.signalcollection;

import android.content.Context;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.baidu.mapapi.SDKInitializer;
import com.example.signalcollection.activity.MainActivity;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.orhanobut.logger.AndroidLogTool;
import com.orhanobut.logger.Logger;
import com.shuwei.trigger.TriggerSdkClient;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;

import org.litepal.LitePalApplication;
import org.litepal.tablemanager.Connector;

import cn.jpush.android.api.JPushInterface;


/**
 * Created by gk on 2016/1/10.
 */
public class BaseApplication extends LitePalApplication {

    //public static FunctionConfig mFunctionConfig;
    private static BaseApplication baseApplication;
    private static Handler mHandler = new Handler();
    public static final String YOUR_TAG = "baseLog";
    //public static FunctionConfig mFunctionConfig;

    @Override
    public void onCreate() {
        super.onCreate();

        TriggerSdkClient.initialization(this);
        SDKInitializer.initialize(getApplicationContext());
        Beta.autoInit = true;
        Beta.autoCheckUpgrade = true;
        Beta.initDelay = 1 * 1000;
        Beta.canShowUpgradeActs.add(MainActivity.class);//在主界面提示更新界面
        Bugly.init(getApplicationContext(), "900053432", true);
        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);
        Connector.getDatabase();
        baseApplication = this;
        initImageLoader(this);

        Logger.init(YOUR_TAG).methodCount(3).methodOffset(2).logTool(new AndroidLogTool());
    }


    public static Context getApplication() {
        return baseApplication;
    }

    public static Handler getMainThreadHandler() {

        return mHandler;
    }

    private void initImageLoader(Context context) {

        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
        config.tasksProcessingOrder(QueueProcessingType.LIFO);
        config.writeDebugLogs();
        ImageLoader.getInstance().init(config.build());
    }


}
