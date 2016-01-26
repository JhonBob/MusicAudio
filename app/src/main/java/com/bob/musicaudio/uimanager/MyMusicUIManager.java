package com.bob.musicaudio.uimanager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bob.musicaudio.Interface.IConstants;
import com.bob.musicaudio.R;
import com.bob.musicaudio.activity.MainActivity;
import com.bob.musicaudio.model.MusicInfo;
import com.bob.musicaudio.service.ServiceManager;
import com.bob.musicaudio.unitls.AlwaysMarqueeTextView;
import com.bob.musicaudio.unitls.BitmapUtil;
import com.bob.musicaudio.unitls.ImageUtil;
import com.bob.musicaudio.unitls.MusicUtils;

/**
 * Created by Administrator on 2015/7/30.
 */
@SuppressLint("HandlerLeak")
public class MyMusicUIManager implements OnClickListener, IConstants {

    private Activity mActivity;
    private View mView;
    private ServiceManager mServiceManager;
    private AlwaysMarqueeTextView mMusicNameTv, mArtistTv;
    private TextView mPositionTv, mDurationTv;
    private ImageButton mPlayBtn, mPauseBtn, mNextBtn, mMenuBtn;
    private ProgressBar mPlaybackProgress;
    private Bitmap mDefaultAlbumIcon;
    private ImageView mHeadIcon;
    public ImageButton  mBackBtn;
    private UIManager mUIManager;
    public Handler mHandler;


    public MyMusicUIManager(Activity a, ServiceManager sm, View view, UIManager manager) {
        this.mActivity = a;
        this.mView = view;
        this.mUIManager = manager;
        this.mServiceManager = sm;
        initView();

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                refreshSeekProgress(mServiceManager.position(),
                        mServiceManager.duration());
            }
        };
    }

    //初始化UI界面控件
    private void initView() {
        mBackBtn = (ImageButton) findViewById(R.id.backBtn);
        mMusicNameTv = (AlwaysMarqueeTextView) findViewById(R.id.musicname_tv2);
        mArtistTv = (AlwaysMarqueeTextView) findViewById(R.id.artist_tv2);
        mPositionTv = (TextView) findViewById(R.id.position_tv2);
        mDurationTv = (TextView) findViewById(R.id.duration_tv2);

        mPlayBtn = (ImageButton) findViewById(R.id.btn_play2);
        mPauseBtn = (ImageButton) findViewById(R.id.btn_pause2);
        mNextBtn = (ImageButton) findViewById(R.id.btn_playNext2);
        mMenuBtn = (ImageButton) findViewById(R.id.btn_menu2);


        mBackBtn.setOnClickListener(this);
        mPlayBtn.setOnClickListener(this);
        mPauseBtn.setOnClickListener(this);
        mNextBtn.setOnClickListener(this);
        mMenuBtn.setOnClickListener(this);

        mPlaybackProgress = (ProgressBar) findViewById(R.id.playback_seekbar2);

        mDefaultAlbumIcon = BitmapFactory.decodeResource(
                mActivity.getResources(), R.drawable.img_album_background);
        mHeadIcon = (ImageView) findViewById(R.id.headicon_iv);

    }

    //刷新进度条
    public void refreshSeekProgress(int curTime, int totalTime) {

        curTime /= 1000;
        totalTime /= 1000;
        int curminute = curTime / 60;
        int cursecond = curTime % 60;

        String curTimeString = String.format("%02d:%02d", curminute, cursecond);
        mPositionTv.setText(curTimeString);

        int rate = 0;
        if (totalTime != 0) {
            rate = (int) ((float) curTime / totalTime * 100);
        }

        mPlaybackProgress.setProgress(rate);
    }


    //刷新UI
    public void refreshUI(int curTime, int totalTime, MusicInfo music) {

        int tempCurTime = curTime;
        int tempTotalTime = totalTime;

        totalTime /= 1000;
        int totalminute = totalTime / 60;
        int totalsecond = totalTime % 60;
        String totalTimeString = String.format("%02d:%02d", totalminute,
                totalsecond);

        mDurationTv.setText(totalTimeString);

        mMusicNameTv.setText(music.musicName);
        mArtistTv.setText(music.artist);

        Bitmap bitmap = MusicUtils.getCachedArtwork(mActivity, music.albumId,
                mDefaultAlbumIcon);
        mHeadIcon.setBackgroundDrawable(new BitmapDrawable(mActivity.getResources(), bitmap));

        refreshSeekProgress(tempCurTime, tempTotalTime);
    }


    //图标交替处理
    public void showPlay(boolean flag) {
        if (flag) {
            mPlayBtn.setVisibility(View.VISIBLE);
            mPauseBtn.setVisibility(View.GONE);
        } else {
            mPlayBtn.setVisibility(View.GONE);
            mPauseBtn.setVisibility(View.VISIBLE);
        }
    }

    //封装简化
    public View findViewById(int id) {
        return mView.findViewById(id);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_play2:
                mServiceManager.rePlay();
                break;
            case R.id.btn_pause2:
                mServiceManager.pause();
                break;
            case R.id.btn_playNext2:
                mServiceManager.next();
                break;
            case R.id.backBtn:
                mActivity.onBackPressed();
                mUIManager.setCurrentItem();
                break;
           case R.id.btn_menu2:
              ((MainActivity)mActivity).mSlidingMenu.showMenu(true);
               break;
        }
    }
}

