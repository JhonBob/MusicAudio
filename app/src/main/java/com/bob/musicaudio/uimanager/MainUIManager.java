package com.bob.musicaudio.uimanager;

import android.view.View;

/**
 * Created by Administrator on 2015/7/22.
 */

//功能：抽取公用方法
public abstract class MainUIManager {

    public abstract View getView();
    public abstract View getView(int from);
    public abstract View getView(int from, Object obj);
}
