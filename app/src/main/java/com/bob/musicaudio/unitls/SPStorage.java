package com.bob.musicaudio.unitls;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.widget.Toast;

import com.bob.musicaudio.Interface.IConstants;

/**
 * Created by Administrator on 2015/7/13.
 */

//功能：配置管理类
public class SPStorage implements IConstants {
    //配置属性
    private SharedPreferences mSp;
    //配置写
    private Editor mEditor;


    //构造函数初始化
    public SPStorage(Context context) {
        mSp = context.getSharedPreferences(SP_NAME, Context.MODE_WORLD_WRITEABLE);
        mEditor = mSp.edit();
    }


    //保存背景图片的地址
    public void savePath(String path) {
        mEditor.putString(SP_BG_PATH, path);
        mEditor.commit();
    }
    //获取背景图片的地址
    public String getPath() {
        return mSp.getString(SP_BG_PATH, null);
    }



    //切歌属性设置
    public void saveShake(boolean shake) {
        mEditor.putBoolean(SP_SHAKE_CHANGE_SONG, shake);
        mEditor.commit();
    }
    //切歌属性获取
    public boolean getShake() {
        return mSp.getBoolean(SP_SHAKE_CHANGE_SONG, false);
    }



    //是否自动下载歌词
    public void saveAutoLyric(boolean auto) {
        mEditor.putBoolean(SP_AUTO_DOWNLOAD_LYRIC, auto);
        mEditor.commit();
    }
    //获得歌词下载配置
    public boolean getAutoLyric() {
        return mSp.getBoolean(SP_AUTO_DOWNLOAD_LYRIC, false);
    }


    //文件大小过滤属性存储
    public void saveFilterSize(boolean size) {
        mEditor.putBoolean(SP_FILTER_SIZE, size);
        mEditor.commit();
    }
    //文件大小过滤属性获取
    public boolean getFilterSize() {
        return mSp.getBoolean(SP_FILTER_SIZE, false);
    }



    //文件时长过滤属性存储
    public void saveFilterTime(boolean time) {
        mEditor.putBoolean(SP_FILTER_TIME, time);
        mEditor.commit();
    }
    //文件时长过滤属性存储
    public boolean getFilterTime() {
        return mSp.getBoolean(SP_FILTER_TIME, false);
    }



    //提示消息吐司
    public static Toast showMessage(Toast toastMsg, Context context, String msg) {
        if (toastMsg == null) {
            toastMsg = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        } else {
            toastMsg.setText(msg);
        }
        toastMsg.show();
        return toastMsg;
    }
}
