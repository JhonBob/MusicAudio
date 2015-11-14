package com.bob.musicaudio.uimanager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.TextView;

import com.bob.musicaudio.Interface.IConstants;
import com.bob.musicaudio.R;
import com.bob.musicaudio.SQLdatabase.FavoriteInfoDao;
import com.bob.musicaudio.SQLdatabase.MusicInfoDao;
import com.bob.musicaudio.activity.MainContentActivity;
import com.bob.musicaudio.model.MusicInfo;
import com.bob.musicaudio.service.ServiceManager;
import com.bob.musicaudio.unitls.MusicTimer;
import com.bob.musicaudio.adapter.MyAdapter;
import com.bob.musicaudio.unitls.MySlidingDrawer;
import com.bob.musicaudio.unitls.SPStorage;

/**
 * Created by Administrator on 2015/7/30.
 */
@SuppressLint("HandlerLeak")
public class SlidingDrawerManager implements OnClickListener,
        OnSeekBarChangeListener, IConstants, OnDrawerOpenListener,
        OnDrawerCloseListener {

    private MySlidingDrawer mSliding;
    private TextView mMusicNameTv, mArtistTv, mCurTimeTv, mTotalTimeTv;
    private ImageButton mPrevBtn, mNextBtn, mPlayBtn, mPauseBtn, mVolumeBtn,
            mFavoriteBtn;
    private ListView mLrcListView;
    private LinearLayout mVolumeLayout;
    private Activity mActivity;
    private View mView;
    private ServiceManager mServiceManager;
    private SeekBar mPlaybackSeekBar, mVolumeSeekBar;
    public Handler mHandler;
    private boolean mPlayAuto = true;

    private AudioManager mAudioManager;
    private int mMaxVolume;
    private int mCurVolume;

    private Animation view_in, view_out;
    // private LrcUtil mLrcUtil;
    // private LrcView mLrcView;
    private ListView mListView;
    private GridView mGridView;

    private ImageButton mShowMoreBtn;

    private ImageView mMoveIv;
    private boolean mIsFavorite = false;
    private FavoriteInfoDao mFavoriteDao;
    private MusicInfoDao mMusicDao;
    private MusicInfo mCurrentMusicInfo;
    private boolean mListNeedRefresh = false;
    private MyAdapter mAdapter;
    private MusicTimer mMusicTimer;
    private int mProgress;
    //private LyricDownloadManager mLyricDownloadManager;
    //private LyricLoadHelper mLyricLoadHelper;
    //private LyricAdapter mLyricAdapter;
    private int mScreenWidth;

    private SPStorage mSp;


    public SlidingDrawerManager(Activity a, ServiceManager sm, View view) {
        this.mServiceManager = sm;
        this.mActivity = a;
        this.mView = view;
        mFavoriteDao = new FavoriteInfoDao(a);
        mMusicDao = new MusicInfoDao(a);
        mSp = new SPStorage(a);
        // mLyricDownloadManager = new LyricDownloadManager(a);
        //mLyricLoadHelper = new LyricLoadHelper();
        // mLyricLoadHelper.setLyricListener(mLyricListener);

        DisplayMetrics metric = new DisplayMetrics();
        a.getWindowManager().getDefaultDisplay().getMetrics(metric);
        mScreenWidth = metric.widthPixels;

        // 歌词秀设置---------------------------------------------------------------
        //mLyricAdapter = new LyricAdapter(a);

        // mLrcUtil = new LrcUtil();

        mAudioManager = (AudioManager) a
                .getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mCurVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        view_in = AnimationUtils.loadAnimation(a, R.anim.fade_in);
        view_out = AnimationUtils.loadAnimation(a, R.anim.fade_out);

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

    private void initView() {
        mListView = (ListView) findViewById(R.id.music);
        mGridView = (GridView) findViewById(R.id.gridview);
        mSliding = (MySlidingDrawer) findViewById(R.id.slidingDrawer);
        mMusicNameTv = (TextView) findViewById(R.id.musicname_tv);
        mArtistTv = (TextView) findViewById(R.id.artist_tv);
        mPrevBtn = (ImageButton) findViewById(R.id.btn_playPre);
        mNextBtn = (ImageButton) findViewById(R.id.btn_playNext);
        mPlayBtn = (ImageButton) findViewById(R.id.btn_play);
        mPauseBtn = (ImageButton) findViewById(R.id.btn_pause);
        mVolumeBtn = (ImageButton) findViewById(R.id.btn_volume);
        mShowMoreBtn = (ImageButton) findViewById(R.id.btn_more);
        mFavoriteBtn = (ImageButton) findViewById(R.id.btn_favorite);
        mMoveIv = (ImageView) findViewById(R.id.move_iv);




        mSliding.setOnDrawerCloseListener(this);
        mSliding.setOnDrawerOpenListener(this);

        mPrevBtn.setOnClickListener(this);
        mNextBtn.setOnClickListener(this);
        mPlayBtn.setOnClickListener(this);
        mPauseBtn.setOnClickListener(this);
        mVolumeBtn.setOnClickListener(this);
        mShowMoreBtn.setOnClickListener(this);
        mFavoriteBtn.setOnClickListener(this);


        mPlaybackSeekBar = (SeekBar) findViewById(R.id.playback_seekbar);
        mVolumeSeekBar = (SeekBar) findViewById(R.id.volume_seekbar);
        mVolumeSeekBar.setMax(mMaxVolume);
        mVolumeSeekBar.setProgress(mCurVolume);

        mPlaybackSeekBar.setOnSeekBarChangeListener(this);
        mVolumeSeekBar.setOnSeekBarChangeListener(this);

        mCurTimeTv = (TextView) findViewById(R.id.currentTime_tv);
        mTotalTimeTv = (TextView) findViewById(R.id.totalTime_tv);

        mVolumeLayout = (LinearLayout) findViewById(R.id.volumeLayout);


    }

    public void refreshSeekProgress(int curTime, int totalTime) {

        int tempCurTime = curTime;

        curTime /= 1000;
        totalTime /= 1000;
        int curminute = curTime / 60;
        int cursecond = curTime % 60;

        String curTimeString = String.format("%02d:%02d", curminute, cursecond);
        mCurTimeTv.setText(curTimeString);

        int rate = 0;
        if (totalTime != 0) {
            rate = (int) ((float) curTime / totalTime * 100);
        }
        mPlaybackSeekBar.setProgress(rate);


    }

    public void refreshUI(int curTime, int totalTime, MusicInfo music) {

        mCurrentMusicInfo = music;
        if (music.favorite == 1) {
            mIsFavorite = true;
            mFavoriteBtn.setImageResource(R.drawable.icon_favourite_checked);
        } else {
            mIsFavorite = false;
            mFavoriteBtn.setImageResource(R.drawable.icon_favourite_normal);
        }

        int tempCurTime = curTime;
        int tempTotalTime = totalTime;

        totalTime /= 1000;
        int totalminute = totalTime / 60;
        int totalsecond = totalTime % 60;
        String totalTimeString = String.format("%02d:%02d", totalminute,
                totalsecond);

        mTotalTimeTv.setText(totalTimeString);

        mMusicNameTv.setText(music.musicName);
        mArtistTv.setText(music.artist);

        refreshSeekProgress(tempCurTime, tempTotalTime);
    }

    public void showPlay(boolean flag) {
        if (flag) {
            mPlayBtn.setVisibility(View.VISIBLE);
            mPauseBtn.setVisibility(View.GONE);
        } else {
            mPlayBtn.setVisibility(View.GONE);
            mPauseBtn.setVisibility(View.VISIBLE);
        }
    }

    private View findViewById(int id) {
        return mView.findViewById(id);
    }

    public void refreshFavorite(int favorite) {
        if (favorite == 1) {
            mIsFavorite = true;
            mFavoriteBtn.setImageResource(R.drawable.icon_favourite_checked);
        } else {
            mIsFavorite = false;
            mFavoriteBtn.setImageResource(R.drawable.icon_favourite_normal);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_playPre:
                if (mCurrentMusicInfo == null) {
                    return;
                }
                mServiceManager.prev();
                break;
            case R.id.btn_play:
                if (mCurrentMusicInfo == null) {
                    return;
                }
                mServiceManager.rePlay();
                break;
            case R.id.btn_playNext:
                if (mCurrentMusicInfo == null) {
                    return;
                }
                mServiceManager.next();
                break;
            case R.id.btn_pause:
                mServiceManager.pause();
                break;
            case R.id.btn_volume:
                if (mVolumeLayout.isShown()) {
                    mVolumeLayout.setVisibility(View.INVISIBLE);
                    mVolumeLayout.startAnimation(view_out);
                } else {
                    mVolumeLayout.setVisibility(View.VISIBLE);
                    mVolumeLayout.startAnimation(view_in);
                }
                break;

            case R.id.btn_more:
                ((MainContentActivity)mActivity).mSlidingMenu.showMenu(true);
                break;

            case R.id.btn_favorite:
                if (mCurrentMusicInfo == null) {
                    return;
                }
                mListNeedRefresh = true;
                if (!mIsFavorite) {
                    startAnimation(mMoveIv);
                    mFavoriteDao.saveMusicInfo(mCurrentMusicInfo);
                    mMusicDao.setFavoriteStateById(mCurrentMusicInfo._id, 1);
                    mFavoriteBtn.setImageResource(R.drawable.icon_favorite_on);
                } else {
                    mFavoriteDao.deleteById(mCurrentMusicInfo._id);
                    mMusicDao.setFavoriteStateById(mCurrentMusicInfo._id, 0);
                    mFavoriteBtn.setImageResource(R.drawable.icon_favorite);
                }
                mIsFavorite = !mIsFavorite;
                break;

        }
    }



    public void setMusicTimer(MusicTimer musicTimer) {
        this.mMusicTimer = musicTimer;
    }

    public void open() {
        mSliding.setVisibility(View.VISIBLE);
        mSliding.animateOpen();
    }

    public void close() {
        mSliding.animateClose();
    }

    public boolean isOpened() {
        return mSliding.isOpened();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        System.out.println("---------------------------------------");
        if (seekBar == mPlaybackSeekBar) {
            if (!mPlayAuto) {
                mProgress = progress;
                //mServiceManager.seekTo(progress);
                // refreshSeekProgress(mServiceManager.position(),
                // mServiceManager.duration());
            }
        } else if (seekBar == mVolumeSeekBar) {
            System.out.println("++++++++++++++++++++++++++++++++++++++");
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress,
                    0);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (seekBar == mPlaybackSeekBar) {
            mPlayAuto = false;
            mMusicTimer.stopTimer();
            mServiceManager.pause();
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (seekBar == mPlaybackSeekBar) {
            mPlayAuto = true;
            mServiceManager.seekTo(mProgress);
            refreshSeekProgress(mServiceManager.position(),
                    mServiceManager.duration());
            mServiceManager.rePlay();
            mMusicTimer.startTimer();
        }
    }


    private void startAnimation(View view) {
        view.setVisibility(View.VISIBLE);
        int fromX = view.getLeft();
        int fromY = view.getTop();

        AnimationSet animSet = new AnimationSet(true);
        // 注：ABSOLUTE表示离当前自己的View绝对的像素单位
        // 使用RELATIVE_TO_SELF和RELATIVE_TO_PARENT时一般用倍数关系 一般用1f 0f
        // 表示相对于自身或父控件几倍的移动
        TranslateAnimation transAnim = new TranslateAnimation(
                Animation.ABSOLUTE, 0f, Animation.ABSOLUTE, -fromX,
                Animation.ABSOLUTE, 0f, Animation.ABSOLUTE, -fromY);

        AlphaAnimation alphaAnim1 = new AlphaAnimation(0f, 1f);
        ScaleAnimation scaleAnim1 = new ScaleAnimation(0, 1, 0, 1,
                Animation.RELATIVE_TO_PARENT, Animation.RELATIVE_TO_PARENT);

        AlphaAnimation alphaAnim2 = new AlphaAnimation(1f, 0f);
        ScaleAnimation scaleAnim2 = new ScaleAnimation(1, 0, 1, 0,
                Animation.RELATIVE_TO_PARENT, Animation.RELATIVE_TO_PARENT);

        transAnim.setDuration(600);

        scaleAnim1.setDuration(600);
        alphaAnim1.setDuration(600);

        scaleAnim2.setDuration(800);
        alphaAnim2.setDuration(800);
        scaleAnim2.setStartOffset(600);
        alphaAnim2.setStartOffset(600);
        transAnim.setStartOffset(600);

        animSet.addAnimation(scaleAnim1);
        animSet.addAnimation(alphaAnim1);

        animSet.addAnimation(scaleAnim2);
        animSet.addAnimation(alphaAnim2);
        animSet.addAnimation(transAnim);
        view.startAnimation(animSet);
        view.setVisibility(View.GONE);
    }


    public void setListViewAdapter(MyAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public void onDrawerClosed() {
        if (mListView != null) {
            mListView.setVisibility(View.VISIBLE);
        }
        if (mGridView != null) {
            mGridView.setVisibility(View.VISIBLE);
        }
        mSliding.setVisibility(View.GONE);
        if (mListNeedRefresh) {
            if (mIsFavorite) {
                mAdapter.refreshFavoriteById(mCurrentMusicInfo.songId, 1);
            } else {
                mAdapter.refreshFavoriteById(mCurrentMusicInfo.songId, 0);
            }
        }
    }

    @Override
    public void onDrawerOpened() {
        if (mListView != null) {
            mListView.setVisibility(View.INVISIBLE);
        }
        if (mGridView != null) {
            mGridView.setVisibility(View.INVISIBLE);
        }
    }

}

