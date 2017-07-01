package com.example.signalcollection.util;

import com.example.signalcollection.Constans;
import com.example.signalcollection.bean.PhotoUrl;
import com.example.signalcollection.bean.TakePhoto;
import com.orhanobut.logger.Logger;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.example.signalcollection.qinniu.GenearateToke;
import com.example.signalcollection.qinniu.ReturnBody;
import com.qiniu.android.storage.UploadOptions;

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

    public static ImageUploadTool getmInstance() {

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
        if (file.exists()) {
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
                    if (!info.isOK()) {
                        Logger.i("logger the statusCode:" + info.statusCode + info.toString());
                        failure.add(photoUrl);
                        photoUrl.setIsUpLoad(Constans.PHOTO_LOAD_FAILURE);
                        photoUrl.update(photoUrl.getId());
                    } else {
                        photoUrl.setIsUpLoad(Constans.PHOTO_LOADED);
                        photoUrl.update(photoUrl.getId());
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
            }, new UploadOptions(null, null, false, new UpProgressHandler() {
                @Override
                public void progress(String key, double percent) {
                    //进度
                }
            }, null));
        } else {
            if (listener != null) {
                listener.onImageNotFound();
            }
        }
    }


    public void uploadImage(final TakePhoto takePhoto, final UploadListener listener) {

        if (listener == null) {
            return;
        }
        String path = takePhoto.getPath();
        String key = takePhoto.getUrl();
        key = key.replace(KEY_BASEURL, "");
        File file = new File(path);
        if (file.exists()) {
            ReturnBody returnBody = new ReturnBody();
            //returnBody.setH(1024);
            //returnBody.setW(768);
            returnBody.setHash(file.hashCode() + "");
            returnBody.setName(file.getName());
            returnBody.setSize(file.getTotalSpace());
            String token = GenearateToke.getToken(returnBody);
            mUploadManager.put(file, key, token, new UpCompletionHandler() {
                @Override
                public void complete(String key, ResponseInfo info, JSONObject response) {
                    if (info.isOK()) {
                        listener.onSuccess();
                    } else {
                        listener.onFailure(info.error, takePhoto);
                    }
                }
            }, null);
        } else {
            listener.onFailure("file not find", takePhoto);
        }

    }


    public interface UploadListener {

        void onSuccess();

        void onFailure(String err, TakePhoto takePhoto);

    }


    public interface UploadFinishListener {

        void onUploadFinish(List<PhotoUrl> failureImages);

        void onImageNotFound();

    }

}
