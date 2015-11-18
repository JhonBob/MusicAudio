package com.bob.musicaudio.uimanager;

import android.view.View;

/**
 * Created by Administrator on 2015/7/22.
 */
public abstract class MainUIManager {

    protected abstract void setBgByPath(String path);
    public abstract View getView();
    public abstract View getView(int from);
    public abstract View getView(int from, Object obj);
}
