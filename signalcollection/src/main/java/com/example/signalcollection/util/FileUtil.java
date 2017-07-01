package com.example.signalcollection.util;

import android.content.Context;
import android.os.Environment;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;

/**
 * 文件管理类
 * Created by Konmin on 2016/8/13.
 */
public class FileUtil {


    /**
     * 获取目录，使用之前记得申请权限
     *
     * @return
     */
    public static String getRootDir(Context context) {
        if (isSDCardEnable()) {
            return Environment.getExternalStorageDirectory().getPath() + File.separator + "YYCCollection";
        }

        return context.getExternalFilesDir("YYCCollection").getPath();
    }


    /**
     * 获取map存放的目录
     *
     * @param cxt cxt
     * @return String
     */

    public static String getMapDir(Context cxt) {

        String mapDir = getRootDir(cxt) + File.separator + "MapData";

        File file = new File(mapDir);
        if (!file.exists()) {
            file.mkdirs();
        }

        return mapDir;

    }


    /**
     * 获取拍照的路径
     *
     * @param context
     * @return
     */
    public static String getImagePath(Context context) {
        return getImageDir(context) + File.separator + System.currentTimeMillis() + ".png";
    }


    public static String getPhotoPath(Context context) {
        return getImageDir(context) + File.separator + System.currentTimeMillis() + ".jpg";

    }


    /**
     * 获取图片保存的路径
     *
     * @param cxt
     * @return
     */
    public static String getImageDir(Context cxt) {

        String imageDir = getRootDir(cxt) + File.separator + "Images";

        File file = new File(imageDir);
        if (!file.exists()) {
            file.mkdirs();
        }

        return imageDir;

    }


    /**
     * 获取拍照目录的路径
     *
     * @return
     */
    public static String getTakePhotoDir() {

        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator + "YYCSC";

        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }

        return dir;


    }


    /**
     * 判断sd卡是否可用
     *
     * @return
     */

    public static boolean isSDCardEnable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }


    public static boolean mkdirs(File directory) {
        try {
            forceMkdir(directory);
            return true;
        } catch (IOException e) {

        }
        return false;
    }

    public static void forceMkdir(File directory) throws IOException {
        if (directory.exists()) {
            if (!directory.isDirectory()) {
                String message = "File " + directory + " exists and is " + "not a directory. Unable to create directory.";
                throw new IOException(message);
            }
        } else {
            if (!directory.mkdirs()) {
                // Double-check that some other thread or process hasn't made the directory in the background
                if (!directory.isDirectory()) {
                    String message = "Unable to create directory " + directory;
                    throw new IOException(message);
                }
            }
        }
    }


    //遍历所有的map文件，并且写进数据库
    public static List<String> getMapFiles(Context context) {

        List<String> list = new ArrayList<>();
        File[] files = new File(getMapDir(context)).listFiles();
        if (files != null && files.length != 0) {
            for (File file : files) {
                if (file.isDirectory()) {
                    list.add(file.getPath());
                }
            }
        }
        return list;
    }


    /**
     * 获取地图文件夹的路径
     *
     * @param context
     * @param areaCode
     * @return
     */
    public static String getMapPath(Context context, String areaCode) {
        return getMapDir(context) + File.separator + areaCode;
    }

    /**
     * 获取地图文件夹的路径
     *
     * @param context
     * @param areaCode
     * @return
     */
    public static String getMapPathFloor(Context context, String areaCode, String floorCode) {
        return getMapDir(context) + File.separator + areaCode + File.separator + floorCode;
    }


    /**
     * 获取地图文件
     *
     * @param context
     * @param areaCode
     * @return
     */
    public static File getMapFlies(Context context, String areaCode) {
        return new File(getMapPath(context, areaCode));
    }


}
