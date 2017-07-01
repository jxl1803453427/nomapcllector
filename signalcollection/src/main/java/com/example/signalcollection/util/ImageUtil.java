package com.example.signalcollection.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;

import com.example.signalcollection.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer;
import com.orhanobut.logger.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * ImageUtil
 * Created by gk on 2016/1/29.
 */
public class ImageUtil {


    private static ImageUtil mInstance;

    public static ImageUtil getInstance() {
        if (mInstance == null) {
            synchronized (ImageUtil.class) {
                if (mInstance == null) {
                    mInstance = new ImageUtil();
                }
            }
        }
        return mInstance;
    }

    /**
     * 根据计算的inSampleSize，得到压缩后图片
     *
     * @param reqWidth reqWidth
     * @return reqWidth
     *//*
    public Bitmap decodeSampledBitmapFromResource(String pathName, int reqWidth, int reqHeight, FileOutputStream fileOutputStream) {
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        // 调用上面定义的方法计算inSampleSize值
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        Logger.i("options.inSampleSize = " + options.inSampleSize);
        Bitmap bitmap = BitmapFactory.decodeFile(pathName, options);
        return decodeBitmapFromBitmap();
    }*/
    public void decodeBitmapFromBitmap(Bitmap bitmapSrc, int reqWidth, FileOutputStream fileOutputStream) {

        int inSampleSize = calculateRadio(bitmapSrc.getWidth(), bitmapSrc.getHeight(), reqWidth);
        bitmapSrc = bitmapSrc.createScaledBitmap(bitmapSrc, bitmapSrc.getWidth() / inSampleSize, bitmapSrc.getHeight() / inSampleSize, false);
        cQuality(bitmapSrc, fileOutputStream, 1024);

    }


    /**
     * 图片压缩保存
     *
     * @param outPath 压缩后的照片路径
     * @param inPath  压缩前的照片路径
     */
    public void saveBitmap(String outPath, String inPath, int maxSize) throws FileNotFoundException {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmapSrc = BitmapFactory.decodeFile(inPath);
        int bitmapHeight = bitmapSrc.getHeight();
        int bitmapWidth = bitmapSrc.getWidth();
        options.inJustDecodeBounds = false;
        int rating = calculateRadio(bitmapWidth, bitmapHeight, 1280);//固定最长边最大为1280


        int height = bitmapHeight / rating;
        int width = bitmapWidth / rating;
        Logger.i("height :" + height + " ; width:" + width);
        bitmapSrc = Bitmap.createScaledBitmap(bitmapSrc, width, height, false);
        cQuality(bitmapSrc, new FileOutputStream(outPath), maxSize);
        bitmapSrc.recycle();
    }


   /* public void decodeBitmapFromBitmap(String path, int reqWidth, int reqHeight, FileOutputStream fileOutputStream) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmapSrc = BitmapFactory.decodeFile(path);
        int inSampleSize = calculateRadio(bitmapSrc.getWidth(), bitmapSrc.getHeight(), reqWidth, reqHeight);
        options.inJustDecodeBounds = false;

        bitmapSrc = bitmapSrc.createScaledBitmap(bitmapSrc, bitmapSrc.getWidth() / inSampleSize, bitmapSrc.getHeight() / inSampleSize, false);
        cQuality(bitmapSrc, fileOutputStream);
        bitmapSrc.recycle();
    }*/



   /* private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // 源图片的宽度
        int width = options.outWidth;
        int height = options.outHeight;
        return calculateRadio(width, height, reqWidth, reqHeight);
    }*/

    private int calculateRadio(int width, int height, int reqWidth) {
        int inSampleSize = 1;
        if (width >= height && width > reqWidth) {//如果宽度大的话根据宽度固定大小缩放
            inSampleSize = (int) (width / reqWidth);
        } else if (width < height && height > reqWidth) {//如果高度高的话根据宽度固定大小缩放
            inSampleSize = (int) (height / reqWidth);
        }
        return inSampleSize;
    }

/*
    public boolean saveBitmap2file(Bitmap bmp, String filename) {
        int quality = 100;
        OutputStream stream = null;
        Bitmap.CompressFormat format = Bitmap.CompressFormat.PNG;
        try {
            stream = new FileOutputStream(UIUtils.getExternalCacheDir() + File.separator + filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (!(filename.endsWith(".png") || filename.endsWith(".PNG"))) {
            format = Bitmap.CompressFormat.JPEG;
            bmp = cQuality(bmp);
        }
        return bmp.compress(format, quality, stream);
    }*/

    /**
     * 根据bitmap压缩图片质量
     *
     * @param bitmap 未压缩的bitmap
     */
    private void cQuality(Bitmap bitmap, FileOutputStream fileOutputStream, int maxSize) {

        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        int beginRate = 100;
        int i = 0;
        //第一个参数 ：图片格式 ，第二个参数： 图片质量，100为最高，0为最差  ，第三个参数：保存压缩后的数据的流
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bOut);
        while (bOut.size() / 1024 > maxSize) {  //如果压缩后大于1024Kb，则提高压缩率，重新压缩   如果是png会进入死循环吗
            if (beginRate > 8) {
                beginRate -= 8;
                bOut.reset();
                Logger.i("压缩了" + (++i) + "次");
                bitmap.compress(Bitmap.CompressFormat.JPEG, beginRate, bOut);
            } else {
                break;
            }
        }

        Logger.i("maxSize: " + maxSize + "K,bout size:" + bOut.size() / 1024 + "K");

        try {
            fileOutputStream.write(bOut.toByteArray());
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


      /*  Logger.i("==================" + beginRate);
        ByteArrayInputStream bInt = new ByteArrayInputStream(bOut.toByteArray());
        Bitmap newBitmap = BitmapFactory.decodeStream(bInt);
        if (newBitmap != null) {
            return newBitmap;
        } else {
            return bitmap;
        }*/
    }


    final int REQUEST_CODE_GALLERY = 1001;

//    public void chooseImage(GalleryFinal.OnHanlderResultCallback onHanlderResultCallback, int size) {
//        FunctionConfig functionConfig = new FunctionConfig.Builder()
//                .setEnableCamera(false)
//                .setEnableEdit(false)
//                .setEnableCrop(false)
//                .setEnableRotate(false)
//                .setCropSquare(false)
//                .setEnablePreview(true).setMutiSelectMaxSize(size)
//                .build();
//        GalleryFinal.openGalleryMuti(REQUEST_CODE_GALLERY, functionConfig, onHanlderResultCallback);
//    }
//
//    public void openCamera(GalleryFinal.OnHanlderResultCallback onHanlderResultCallback) {
//        FunctionConfig functionConfig = new FunctionConfig.Builder()
//                .setEnableCamera(true)
//                .setEnableEdit(false)
//                .setEnableCrop(true)
//                .setEnableRotate(false)
//                .setCropSquare(true)
//                .setEnablePreview(false)
//                .build();
//        GalleryFinal.openCamera(REQUEST_CODE_GALLERY, functionConfig, onHanlderResultCallback);
//    }

    public Map<String, RequestBody> wrapUploadImgRequest(File imgFile) {
        Map<String, RequestBody> map = new HashMap<>();
        if (imgFile != null) {
            RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpg"), imgFile);
            map.put("image\"; filename=\"" + imgFile.getName() + "", fileBody);
        }

        return map;
    }

    public DisplayImageOptions getCircleDisplayOption() {
        DisplayImageOptions options = new DisplayImageOptions.Builder().showImageOnLoading(R.mipmap.ic_launcher) //设置图片在下载期间显示的图片
                .showImageForEmptyUri(R.mipmap.ic_launcher)//设置图片Uri为空或是错误的时候显示的图片
                .showImageOnFail(R.mipmap.ic_launcher)  //设置图片加载/解码过程中错误时候显示的图片
                .cacheInMemory(true)//设置下载的图片是否缓存在内存中
                .cacheOnDisk(true).displayer(new CircleBitmapDisplayer()).build();//设置下载的图片是否缓存在SD卡中
        return options;
    }


    public DisplayImageOptions getBaseDisplayOption() {
        DisplayImageOptions options = new DisplayImageOptions.Builder().showImageOnFail(R.mipmap.ic_gf_default_photo).showImageForEmptyUri(R.mipmap.ic_gf_default_photo).showImageOnLoading(R.mipmap.ic_gf_default_photo).cacheInMemory(true).cacheOnDisk(true).build();
        return options;
    }
}
