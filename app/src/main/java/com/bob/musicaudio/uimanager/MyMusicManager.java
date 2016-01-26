package com.bob.musicaudio.uimanager;

import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.bob.musicaudio.R;
import com.bob.musicaudio.model.AlbumInfo;
import com.bob.musicaudio.model.ArtistInfo;
import com.bob.musicaudio.model.FolderInfo;
import com.bob.musicaudio.Interface.IConstants;
import com.bob.musicaudio.app.MusicApp;
import com.bob.musicaudio.model.MusicInfo;
import com.bob.musicaudio.service.ServiceManager;
import com.bob.musicaudio.unitls.MusicTimer;
import com.bob.musicaudio.unitls.MusicUtils;
import com.bob.musicaudio.adapter.MyAdapter;
import com.bob.musicaudio.unitls.SPStorage;

/**
 * Created by Administrator on 2015/7/23.
 */

//我的音乐
public class MyMusicManager extends MainUIManager implements IConstants{

    private LayoutInflater mInflater;
    private Activity mActivity;

    private String TAG = MyMusicManager.class.getSimpleName();
    private MyAdapter mAdapter;
    private ListView mListView;
    private ServiceManager mServiceManager = null;
    private MyMusicUIManager mUIm;
    private MusicPlayBroadcast mPlayBroadcast;


    private int mFrom;
    private Object mObj;

    public RelativeLayout mBottomLayout, mMainLayout;
    private Bitmap defaultArtwork;

    private UIManager mUIManager;

    public MyMusicManager(Activity activity, UIManager manager) {
        this.mActivity = activity;
        mInflater = LayoutInflater.from(activity);
        this.mUIManager = manager;
    }

    public View getView(int from) {
        return getView(from, null);
    }

    public View getView(int from, Object object) {
        View contentView = mInflater.inflate(R.layout.mymusic, null);
        mFrom = from;
        mObj = object;
        initView(contentView);
        return contentView;
    }

    private void initView(View view) {
        defaultArtwork = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.img_album_background);
        //获得服务管理对象
        mServiceManager = MusicApp.mServiceManager;

        mMainLayout=(RelativeLayout)view.findViewById(R.id.main_mymusic_layout);
        mBottomLayout = (RelativeLayout) view.findViewById(R.id.bottomLayout);
        //音乐列表
        mListView = (ListView) view.findViewById(R.id.music);

        mActivity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        //动态注册广播
        mPlayBroadcast = new MusicPlayBroadcast();
        IntentFilter filter = new IntentFilter(BROADCAST_NAME);
        filter.addAction(BROADCAST_NAME);
        filter.addAction(BROADCAST_QUERY_COMPLETE_NAME);
        mActivity.registerReceiver(mPlayBroadcast, filter);
        //获得UI管理对象
        mUIm = new MyMusicUIManager(mActivity, mServiceManager, view, mUIManager);
        //初始化列表控件
        initListView();
        //初始化列表控件状态
        initListViewStatus();

    }

    //初始化列表控件
    private void initListView() {
        //列表适配
        mAdapter = new MyAdapter(mActivity, mServiceManager);
        mListView.setAdapter(mAdapter);
        //列表点击事件处理
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                //刷新列表
                mAdapter.refreshPlayingList();
                //播放点中的歌曲
                mServiceManager.playById(mAdapter.getData().get(position).songId);
            }
        });

        //构建查询条件
        StringBuffer select = new StringBuffer();
        //按条件查询
        switch (mFrom) {
            case START_FROM_ARTIST:
                //对象强转
                ArtistInfo artistInfo = (ArtistInfo) mObj;
                //调用查询引擎，向适配器集合添加数据
                mAdapter.setData(MusicUtils.queryMusic(mActivity, select.toString(), artistInfo.artist_name, START_FROM_ARTIST));
                break;
            case START_FROM_ALBUM:
                AlbumInfo albumInfo = (AlbumInfo) mObj;
                //调用查询引擎，向适配器集合添加数据
                mAdapter.setData(MusicUtils.queryMusic(mActivity, select.toString(), albumInfo.album_id + "", START_FROM_ALBUM));
                break;
            case START_FROM_FOLDER:
                FolderInfo folderInfo = (FolderInfo) mObj;
                //调用查询引擎，向适配器集合添加数据
                mAdapter.setData(MusicUtils.queryMusic(mActivity, select.toString(), folderInfo.folder_path, START_FROM_FOLDER));
                break;
            case START_FROM_FAVORITE:
                mAdapter.setData(MusicUtils.queryFavorite(mActivity), START_FROM_FAVORITE);
                break;
            default:
                //默认查询
                mAdapter.setData(MusicUtils.queryMusic(mActivity, START_FROM_LOCAL));
                break;
        }
    }


    //初始化列表控件状态
    private void initListViewStatus() {
        try {
            //获得服务的播放状态
            int playState = mServiceManager.getPlayState();
            if (playState == MPS_NOFILE || playState == MPS_INVALID) {
                return;
            }
            if (playState == MPS_PLAYING) {
                //mMusicTimer.startTimer();
            }

            List<MusicInfo> musicList = mAdapter.getData();
            //获得当前播放位置
            int playingSongPosition = MusicUtils.seekPosInListById(musicList, mServiceManager.getCurMusicId());
            mAdapter.setPlayState(playState, playingSongPosition);
            MusicInfo music = mServiceManager.getCurMusic();
            mUIm.refreshUI(mServiceManager.position(), music.duration, music);
            mUIm.showPlay(false);

        } catch (Exception e) {
            Log.d(TAG, "", e);
        }
    }


    private class MusicPlayBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BROADCAST_NAME)) {
                MusicInfo music = new MusicInfo();
                int playState = intent.getIntExtra(PLAY_STATE_NAME, MPS_NOFILE);
                int curPlayIndex = intent.getIntExtra(PLAY_MUSIC_INDEX, -1);
                Bundle bundle = intent.getBundleExtra(MusicInfo.KEY_MUSIC);
                if (bundle != null) {
                    music = bundle.getParcelable(MusicInfo.KEY_MUSIC);
                }
                mAdapter.setPlayState(playState, curPlayIndex);
                switch (playState) {
                    case MPS_INVALID:// 考虑后面加上如果文件不可播放直接跳到下一首
                        //mMusicTimer.stopTimer();

                        mUIm.refreshUI(0, music.duration, music);
                        mUIm.showPlay(true);
                        mServiceManager.next();
                        break;
                    case MPS_PAUSE:
                        //mMusicTimer.stopTimer();

                        mUIm.refreshUI(mServiceManager.position(), music.duration,
                                music);
                        mUIm.showPlay(true);

                        mServiceManager.cancelNotification();
                        break;
                    case MPS_PLAYING:
                        //mMusicTimer.startTimer();

                        mUIm.refreshUI(mServiceManager.position(), music.duration,
                                music);
                        mUIm.showPlay(false);

                        Bitmap bitmap = MusicUtils.getCachedArtwork(mActivity,
                                music.albumId, defaultArtwork);
                        mServiceManager.updateNotification(bitmap, music.musicName,
                                music.artist);

                        break;
                    case MPS_PREPARE:
                        //mMusicTimer.stopTimer();

                        mUIm.refreshUI(0, music.duration, music);
                        mUIm.showPlay(true);
                        break;
                }
            }
        }
    }

    @Override
    public View getView() {
        return null;
    }

}

