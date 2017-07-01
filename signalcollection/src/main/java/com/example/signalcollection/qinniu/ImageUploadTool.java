package com.example.signalcollection.qinniu;

import android.content.ContentValues;

import com.example.signalcollection.Constans;
import com.example.signalcollection.bean.PhotoUrl;
import com.orhanobut.logger.Logger;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;

import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 上传图片的工具类
 * Created by Konmin on 2016/8/5.
 */
public class ImageUploadTool {


    private UploadManager mUploadManager;
    public static final String KEY_BASEURL = "http://jmtool3.jjfinder.com/";

    private static ImageUploadTool mInstance = new ImageUploadTool();

    private ImageUploadTool() {
        mUploadManager = new UploadManager();
    }

    public static ImageUploadTool getInstance() {

        return mInstance;
    }

    private int size;
    private int currentIndex = 0;
    private List<PhotoUrl> mList;

    public void upLoadImages(List<PhotoUrl> list, UploadFinishListener listener) {
        List<PhotoUrl> failure = new ArrayList<>();
        mList = list;
        size = mList.size();
        currentIndex = 0;
        uploadImage(listener, failure, mList.get(0));
    }

    private void uploadImage(final UploadFinishListener listener, final List<PhotoUrl> failure, final PhotoUrl photoUrl) {
        String path = photoUrl.getImgLocalUrl();
        String key = photoUrl.getPhotoUrl();
        key = key.replace(KEY_BASEURL, "");
        File file = new File(path);
        if (file.exists() && file.length() > 0) {
            ReturnBody returnBody = new ReturnBody();
            returnBody.setH(1024);
            returnBody.setW(768);
            returnBody.setHash(file.hashCode() + "");
            returnBody.setName(file.getName());
            returnBody.setSize(file.getTotalSpace());
            String token = GenearateToke.getToken(returnBody);
            mUploadManager.put(file, key, token, new UpCompletionHandler() {

                @Override
                public void complete(String key, ResponseInfo info, JSONObject response) {
                    currentIndex++;
                    listener.onAllProgress(currentIndex);
                    if (!info.isOK()) {
                        Logger.i("logger the statusCode:" + info.statusCode + info.toString());
                        //这样的更新方式肯能会挂掉
                        failure.add(photoUrl);
                        ContentValues values = new ContentValues();
                        values.put("isupload", Constans.PHOTO_LOAD_FAILURE);
                        DataSupport.update(PhotoUrl.class, values, photoUrl.getId());
                    } else {
                        ContentValues values = new ContentValues();
                        values.put("isupload", Constans.PHOTO_LOADED);
                        DataSupport.update(PhotoUrl.class, values, photoUrl.getId());
                    }
                    if (currentIndex == size) {
                        if (listener != null) {
                            listener.onUploadFinish(failure);
                        }
                        mList = null;
                        size = 0;
                        currentIndex = 0;
                    } else {
                        uploadImage(listener, failure, mList.get(currentIndex));
                    }
                }
            }, null);
        } else {
            if (listener != null) {
                listener.onImageNotFound();
            }
        }
    }


    public interface UploadFinishListener {

        void onUploadFinish(List<PhotoUrl> failureImages);


        void onAllProgress(int count);

        void onImageNotFound();

    }

}
