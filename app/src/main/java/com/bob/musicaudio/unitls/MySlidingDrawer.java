package com.bob.musicaudio.unitls;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;

import com.bob.musicaudio.Interface.IConstants;
import com.bob.musicaudio.Interface.IOnSlidingHandleViewClickListener;
import com.bob.musicaudio.R;
import com.bob.musicaudio.activity.MainContentActivity;
import com.bob.musicaudio.uimanager.MainUIManager;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Administrator on 2015/7/30.
 */
public class MySlidingDrawer extends SlidingDrawer implements IConstants{
    private int mHandleId = 0;              	 //抽屉行为控件ID
    private int[] mTouchableIds = null;    		//Handle 部分其他控件ID
    public Context mContext;
    public SlidingDrawer mSlidingDrawer;
    public ChangeBgReceiver mReceiver;




    private IOnSlidingHandleViewClickListener mTouchViewClickListener;

    public int[] getTouchableIds() {
        return mTouchableIds;
    }

    public void setTouchableIds(int[] mTouchableIds) {
        this.mTouchableIds = mTouchableIds;
    }

    public int getHandleId() {
        return mHandleId;
    }

    public void setHandleId(int mHandleId) {
        this.mHandleId = mHandleId;
    }



    public void setOnSliderHandleViewClickListener(IOnSlidingHandleViewClickListener listener)
    {
        mTouchViewClickListener = listener;
    }

    public MySlidingDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext=context;
        initBroadCast();
        initBg();
    }

    public MySlidingDrawer(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
    }



    private void initBg() {
        SPStorage mSp = new SPStorage(mContext);
        String mDefaultBgPath = mSp.getPath();
       mSlidingDrawer = (SlidingDrawer) findViewById(R.id.slidingDrawer);
        Bitmap bitmap = getBitmapByPath(mDefaultBgPath);
        if(bitmap != null) {
            mSlidingDrawer.setBackgroundDrawable(new BitmapDrawable(mContext.getResources(), bitmap));
        }

        //如果第一次进来 SharedPreference中没有数据
        if(TextUtils.isEmpty(mDefaultBgPath)) {
            mSp.savePath("004.jpg");
        }
    }


    public Bitmap getBitmapByPath(String path) {
        AssetManager am = mContext.getAssets();
        Bitmap bitmap = null;
        try {
            InputStream is = am.open("bkgs/" + path);
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private void initBroadCast() {
        mReceiver = new ChangeBgReceiver();
        IntentFilter filter = new IntentFilter(BROADCAST_CHANGEBG);
        mContext.registerReceiver(mReceiver, filter);
    }

    private class ChangeBgReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String path = intent.getStringExtra("path");
            Bitmap bitmap = getBitmapByPath(path);
            if(bitmap != null) {
                mSlidingDrawer.setBackgroundDrawable(new BitmapDrawable(mContext.getResources(), bitmap));
            }
        }
    }

    /*
     * 获取控件的屏幕区域
     */
    public Rect getRectOnScreen(View view){
        Rect rect = new Rect();
        int[] location = new int[2];
        View parent = view;
        if(view.getParent() instanceof View){
            parent = (View)view.getParent();
        }
        parent.getLocationOnScreen(location);
        view.getHitRect(rect);
        rect.offset(location[0], location[1]);

        return rect;
    }


    public boolean onInterceptTouchEvent(MotionEvent event) {
        // 触摸位置转换为屏幕坐标
        int[] location = new int[2];
        int x = (int)event.getX();
        int y = (int)event.getY();
        this.getLocationOnScreen(location);
        x += location[0];
        y += location[1];



        if(mTouchableIds != null){
            for(int id : mTouchableIds){
                View view = findViewById(id);
                if (view.isShown())
                {
                    Rect rect = getRectOnScreen(view);
                    if(rect.contains(x,y)){

                        if (event.getAction() == MotionEvent.ACTION_DOWN)
                        {
                            if (mTouchViewClickListener != null)
                            {
                                mTouchViewClickListener.onViewClick(view);
                            }
                        }
                        return true;
                    }
                }
            }
        }


        //抽屉行为控件
        if(event.getAction() == MotionEvent.ACTION_DOWN && mHandleId != 0){
            View view = findViewById(mHandleId);
            Rect rect = getRectOnScreen(view);
            if(rect.contains(x, y)){//点击抽屉控件时交由系统处理
                {
                    return super.onInterceptTouchEvent(event);
                }
            }else{
                return false;
            }
        }

        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

}
