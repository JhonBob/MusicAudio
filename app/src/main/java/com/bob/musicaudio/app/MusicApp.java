package com.bob.musicaudio.app;

import android.app.Application;
import android.os.Environment;

import com.bob.musicaudio.service.ServiceManager;

import java.io.File;

/**
 * Created by Administrator on 2015/7/23.
 */
public class MusicApp extends Application {
    //时钟休眠
    public static boolean mIsSleepClockSetting = false;
    //服务管理
    public static ServiceManager mServiceManager = null;
    //歌词音乐存储路径
    private static String rootPath = "/music";
    public static String lrcPath = "/lrc";

    @Override
    public void onCreate() {
        super.onCreate();
        //获取服务管理对象
        mServiceManager = new ServiceManager(this);
        //初始化存储路径
        initPath();
    }
    //初始化存储路径
    private void initPath() {
        String ROOT = "";
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            ROOT = Environment.getExternalStorageDirectory().getPath();
        }
        rootPath = ROOT + rootPath;
        lrcPath = rootPath + lrcPath;
        File lrcFile = new File(lrcPath);
        if(lrcFile.exists()) {
            lrcFile.mkdirs();
        }
    }
}
