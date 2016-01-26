package com.bob.musicaudio.service;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.util.Log;

import com.bob.musicaudio.Interface.IConstants;
import com.bob.musicaudio.model.MusicInfo;
import com.bob.musicaudio.unitls.MusicUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2015/7/22.
 */

//功能:音乐播放控制类
public class MusicControl implements IConstants,OnCompletionListener {
    //反射获取类名
    private String TAG = MusicControl.class.getSimpleName();
    //播放对象
    private MediaPlayer mMediaPlayer;
    //播放模式
    private int mPlayMode;
    //音乐对象集合
    private List<MusicInfo> mMusicList = new ArrayList<MusicInfo>();
    //播放状态
    private int mPlayState;
    //当前播放位置
    private int mCurPlayIndex;
    //上下文
    private Context mContext;
    //随机函数
    private Random mRandom;
    //当前播放ID
    private int mCurMusicId;
    //当前播放的音乐对象
    private MusicInfo mCurMusic;


    //构造函数，初始化相关播放状态机配置
    public MusicControl(Context context) {
        mMediaPlayer = new MediaPlayer();
        //音频类型
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //设置当前播放完成监听
        mMediaPlayer.setOnCompletionListener(this);
        //初始化当前播放模式为列表循环播放
        mPlayMode = MPM_LIST_LOOP_PLAY;
        //初始化播放状态值
        mPlayState = MPS_NOFILE;
        //初始化当前播放索引值
        mCurPlayIndex = -1;
        //初始化当前播放音乐的ID
        mCurMusicId = -1;
        //初始化上下文
        this.mContext = context;
        //初始化随机函数
        mRandom = new Random();
        mRandom.setSeed(System.currentTimeMillis());
    }


    //按位置播放
    public boolean play(int pos) {
        if(mCurPlayIndex == pos) {
            if(!mMediaPlayer.isPlaying()) {
                mMediaPlayer.start();
                mPlayState = MPS_PLAYING;
                sendBroadCast();
            } else {
                pause();
            }
            return true;
        }
        if(!prepare(pos)) {
            return false;
        }
        return replay();
    }

    //根据歌曲的id来播放
    public boolean playById(int id) {
        //根据ID号返回所在列表的位置
        int position = MusicUtils.seekPosInListById(mMusicList, id);
        //作为当前播放的索引位置
        mCurPlayIndex = position;
        if(mCurMusicId == id) {
            if(!mMediaPlayer.isPlaying()) {
                mMediaPlayer.start();
                mPlayState = MPS_PLAYING;
                sendBroadCast();
            } else {
                pause();
            }
            return true;
        }
        if(!prepare(position)) {
            return false;
        }
        return replay();
    }

    //重新播放
    public boolean replay() {
        if(mPlayState == MPS_INVALID || mPlayState == MPS_NOFILE) {
            return false;
        }

        mMediaPlayer.start();
        mPlayState = MPS_PLAYING;
        sendBroadCast();
        return true;
    }

    //暂停播放
    public boolean pause() {
        if(mPlayState != MPS_PLAYING) {
            return false;
        }
        mMediaPlayer.pause();
        mPlayState = MPS_PAUSE;
        sendBroadCast();
        return true;
    }

    //播放上一首
    public boolean prev() {
        if(mPlayState == MPS_NOFILE) {
            return false;
        }
        mCurPlayIndex--;
        mCurPlayIndex = reviseIndex(mCurPlayIndex);
        if(!prepare(mCurPlayIndex)) {
            return false;
        }
        return replay();
    }

    //播放下一首
    public boolean next() {
        if(mPlayState == MPS_NOFILE) {
            return false;
        }
        mCurPlayIndex++;
        mCurPlayIndex = reviseIndex(mCurPlayIndex);
        if(!prepare(mCurPlayIndex)) {
            return false;
        }
        return replay();
    }

    //处理列表索引出界问题
    private int reviseIndex(int index) {
        if(index < 0) {
            index = mMusicList.size() - 1;
        }
        if(index >= mMusicList.size()) {
            index = 0;
        }
        return index;
    }

    //当前正在播放的歌曲在列表的位置
    public int position() {
        if(mPlayState == MPS_PLAYING || mPlayState == MPS_PAUSE) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

   //毫秒，返回当前音乐的时长
    public int duration() {
        if(mPlayState == MPS_INVALID || mPlayState == MPS_NOFILE) {
            return 0;
        }
        return mMediaPlayer.getDuration();
    }

    //定位进度条当前的时间值
    public boolean seekTo(int progress) {
        if(mPlayState == MPS_INVALID || mPlayState == MPS_NOFILE) {
            return false;
        }
        int pro = reviseSeekValue(progress);
        int time = mMediaPlayer.getDuration();
        int curTime = (int)((float)pro / 100 * time);
        mMediaPlayer.seekTo(curTime);
        return true;
    }

    //进度条位置越界处理
    private int reviseSeekValue(int progress) {
        if(progress < 0) {
            progress = 0;
        } else if(progress > 100) {
            progress = 100;
        }
        return progress;
    }

    //刷新播放列表
    public void refreshMusicList(List<MusicInfo> musicList) {
        mMusicList.clear();
        mMusicList.addAll(musicList);
        if(mMusicList.size() == 0) {
            mPlayState = MPS_NOFILE;
            mCurPlayIndex = -1;
            return;
        }
    }

    //歌曲播放前的初始化
    private boolean prepare(int pos) {
        mCurPlayIndex = pos;
        mMediaPlayer.reset();
        String path = mMusicList.get(pos).data;
        try {
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.prepare();
            mPlayState = MPS_PREPARE;
        } catch (Exception e) {
            Log.e(TAG, "", e);
            mPlayState = MPS_INVALID;
            if(pos < mMusicList.size()) {
                pos++;
                playById(mMusicList.get(pos).songId);
            }
            return false;
        }
        sendBroadCast();
        return true;
    }


    //发送广播
    public void sendBroadCast() {
        Intent intent = new Intent(BROADCAST_NAME);
        intent.putExtra(PLAY_STATE_NAME, mPlayState);
        intent.putExtra(PLAY_MUSIC_INDEX, mCurPlayIndex);
        intent.putExtra("music_num", mMusicList.size());
        if(mPlayState != MPS_NOFILE && mMusicList.size() > 0) {
            Bundle bundle = new Bundle();
            mCurMusic = mMusicList.get(mCurPlayIndex);
            mCurMusicId = mCurMusic.songId;
            bundle.putParcelable(MusicInfo.KEY_MUSIC, mCurMusic);
            intent.putExtra(MusicInfo.KEY_MUSIC, bundle);
        }
        mContext.sendBroadcast(intent);
    }


    //返回当前歌曲ID
    public int getCurMusicId() {
        return mCurMusicId;
    }
    //返回当前歌曲对象
    public MusicInfo getCurMusic() {
        return mCurMusic;
    }
    //返回当前播放状态
    public int getPlayState() {
        return mPlayState;
    }
    //返回当前播放模式
    public int getPlayMode() {
        return mPlayMode;
    }


    //设置播放模式
    public void setPlayMode(int mode) {
        switch(mode) {
            case MPM_LIST_LOOP_PLAY:
            case MPM_ORDER_PLAY:
            case MPM_RANDOM_PLAY:
            case MPM_SINGLE_LOOP_PLAY:
                mPlayMode = mode;
                break;
        }
    }

    //返回音乐列表集合对象
    public List<MusicInfo> getMusicList() {
        return mMusicList;
    }



    //实现播放完成监听
    @Override
    public void onCompletion(MediaPlayer mp) {
        switch(mPlayMode) {
            case MPM_LIST_LOOP_PLAY:
                next();
                break;
            case MPM_ORDER_PLAY:
                if(mCurPlayIndex != mMusicList.size() - 1) {
                    next();
                } else {
                    prepare(mCurPlayIndex);
                }
                break;
            case MPM_RANDOM_PLAY:
                int index = getRandomIndex();
                if(index != -1) {
                    mCurPlayIndex = index;
                } else {
                    mCurPlayIndex = 0;
                }
                if(prepare(mCurPlayIndex)) {
                    replay();
                }
                break;
            case MPM_SINGLE_LOOP_PLAY:
                play(mCurPlayIndex);
                break;
        }
    }

    //获取随机播放的歌曲值
    private int getRandomIndex() {
        int size = mMusicList.size();
        if(size == 0) {
            return -1;
        }
        return Math.abs(mRandom.nextInt() % size);
    }


    //歌曲播放完成处理
    public void exit() {
        mMediaPlayer.stop();
        mMediaPlayer.release();
        mCurPlayIndex = -1;
        mMusicList.clear();
    }
}
