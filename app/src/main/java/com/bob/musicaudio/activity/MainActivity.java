package com.bob.musicaudio.activity;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.os.Handler;
import android.widget.Toast;

import com.bob.musicaudio.R;
import com.bob.musicaudio.fragment.MenuFragment;
import com.bob.musicaudio.slidingmenu.SlidingMenu;
import com.bob.musicaudio.Interface.IConstants;
import com.bob.musicaudio.fragment.MainFragment;
import com.bob.musicaudio.app.MusicApp;
import com.bob.musicaudio.SQLdatabase.MusicInfoDao;
import com.bob.musicaudio.unitls.MusicUtils;
import com.bob.musicaudio.unitls.SplashScreen;

import java.util.ArrayList;
import java.util.List;


//功能：主界面
public class MainActivity extends SingleFragmentActivity implements IConstants {
    //音乐数据API
    public MusicInfoDao mMusicDao;
    //启动动画
    private SplashScreen mSplashScreen;
    //滑动菜单
    public SlidingMenu mSlidingMenu;
    //任务分发线程
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mSplashScreen.removeSplashScreen();
        }
    };


    //泛型，后台监听器
    private List<OnBackListener> mBackListeners = new ArrayList<OnBackListener>();
    //回掉接口，由实现着自己处理自己所触发的事件
    public interface OnBackListener {
         void onBack();
    }
    //注册后台监听
    public void registerBackListener(OnBackListener listener) {
        if (!mBackListeners.contains(listener)) {
            mBackListeners.add(listener);
        }
    }
    //解注册
    public void unRegisterBackListener(OnBackListener listener) {
        mBackListeners.remove(listener);
    }

    //返回按键的回掉事件
    @Override
    public void onBackPressed() {
        if (mSlidingMenu.isMenuShowing()) {
            mSlidingMenu.showContent();
        } else {
            if (mBackListeners.size() == 0) {
                // super.onBackPressed();
                // 在activity中调用 moveTaskToBack (boolean nonRoot)方法即可将activity
                // 退到后台，注意不是finish()退出。
                // 参数为false代表只有当前activity是task根，指应用启动的第一个activity时，才有效;
                moveTaskToBack(true);
            }
        }
        for (OnBackListener listener : mBackListeners) {
            listener.onBack();
        }
    }

    //创建碎片
    @Override
    protected Fragment creatFragment() {
        return new MainFragment();
    }


    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        //初始化SD卡
        initSDCard();
        //启动动画
        mSplashScreen = new SplashScreen(this);
        mSplashScreen.show(R.drawable.image_splash_background, SplashScreen.SLIDE_LEFT);
        // 配置滑动菜单
        initSlidingMenu();
        //初始化API
        mMusicDao = new MusicInfoDao(this);
        //获取音乐数据
        getData();
    }


    // 配置滑动菜单
    public void initSlidingMenu(){
        mSlidingMenu = new SlidingMenu(this);
        mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        mSlidingMenu.setMode(SlidingMenu.RIGHT);
        mSlidingMenu.setShadowWidthRes(R.dimen.shadow_width);
        mSlidingMenu.setShadowDrawable(R.drawable.shadow);
        mSlidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        mSlidingMenu.setFadeDegree(0.35f);
        mSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        mSlidingMenu.setMenu(R.layout.frame_menu);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_menu, new MenuFragment()).commit();
    }


    //加载音乐数据
    private void getData() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                if (mMusicDao.hasData()) {
                    // 如果有数据就等三秒跳转
                    mHandler.sendMessageDelayed(mHandler.obtainMessage(), 3000);
                } else {
                    MusicUtils.queryMusic(MainActivity.this,
                            START_FROM_LOCAL);
                    MusicUtils.queryAlbums(MainActivity.this);
                    MusicUtils.queryArtist(MainActivity.this);
                    MusicUtils.queryFolder(MainActivity.this);
                    //Handler发送消息
                    mHandler.sendEmptyMessage(1);
                }
            }
        }).start();//启动线程
    }

    //初始化SD卡
    private void initSDCard() {
        IntentFilter intentFilter = new IntentFilter();//新建意图过滤器对象
        intentFilter.setPriority(1000);// 设置最高优先级
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);// sd卡被插入，且已经挂载
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);// sd卡存在，但还没有挂载
        intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);// sd卡被移除
        intentFilter.addAction(Intent.ACTION_MEDIA_SHARED);// sd卡作为
        // USB大容量存储被共享，挂载被解除
        intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);// sd卡已经从sd卡插槽拔出，但是挂载点还没解除
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);// 开始扫描
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);// 扫描完成
        intentFilter.addDataScheme("file");
        registerReceiver(sdCardReceiver, intentFilter);// 注册广播接收监听函数
    }


    //SD卡监听广播接收器
    private final BroadcastReceiver sdCardReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.intent.action.MEDIA_REMOVED")// 各种未挂载状态
                    || action.equals("android.intent.action.MEDIA_UNMOUNTED")
                    || action.equals("android.intent.action.MEDIA_BAD_REMOVAL")
                    || action.equals("android.intent.action.MEDIA_SHARED")) {
                finish();
                Toast.makeText(MainActivity.this, "SD卡以外拔出，本地数据没法初始化!",
                        Toast.LENGTH_SHORT).show();
            }
        }
    };


        @Override
        public void onDestroy() {
            super.onDestroy();
            //解注册sd卡，时钟广播接收器
            unregisterReceiver(sdCardReceiver);
            MusicApp.mServiceManager.exit();
            MusicApp.mServiceManager = null;
            //清楚寄存器
            System.exit(0);
        }
    }

