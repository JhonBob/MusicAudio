package com.bob.musicaudio.Interface;

/**
 * Created by Administrator on 2015/7/13.
 */
public interface IConstants {
     String BROADCAST_NAME = "com.bob.musicaudio.broadcast";
     String SERVICE_NAME = "com.bob.musicaudio.service.MediaService";
     String BROADCAST_QUERY_COMPLETE_NAME = "com.bob.musicaudio.querycomplete.broadcast";
     String BROADCAST_CHANGEBG = "com.bob.musicaudio.changebg";
     String BROADCAST_SHAKE = "com.bob.musicaudio.shake";

    //是否开启了振动模式
    String SHAKE_ON_OFF = "SHAKE_ON_OFF";

    String SP_NAME = "com.bob.musicaudio.music_preference";
    String SP_BG_PATH = "bg_path";
    String SP_SHAKE_CHANGE_SONG = "shake_change_song";
    String SP_AUTO_DOWNLOAD_LYRIC = "auto_download_lyric";
    String SP_FILTER_SIZE = "filter_size";
    String SP_FILTER_TIME = "filter_time";

    int REFRESH_PROGRESS_EVENT = 0x100;

    // 播放状态
    int MPS_NOFILE = -1; // 无音乐文件
    int MPS_INVALID = 0; // 当前音乐文件无效
    int MPS_PREPARE = 1; // 准备就绪
    int MPS_PLAYING = 2; // 播放中
    int MPS_PAUSE = 3; // 暂停

    // 播放模式
    int MPM_LIST_LOOP_PLAY = 0; // 列表循环
    int MPM_ORDER_PLAY = 1; // 顺序播放
    int MPM_RANDOM_PLAY = 2; // 随机播放
    int MPM_SINGLE_LOOP_PLAY = 3; // 单曲循环

    String PLAY_STATE_NAME = "PLAY_STATE_NAME";
    String PLAY_MUSIC_INDEX = "PLAY_MUSIC_INDEX";

    //歌手和专辑列表点击都会进入MyMusic 此时要传递参数表明是从哪里进入的
    String FROM = "from";
    int START_FROM_ARTIST = 1;
    int START_FROM_ALBUM = 2;
    int START_FROM_LOCAL = 3;
    int START_FROM_FOLDER = 4;
    int START_FROM_FAVORITE = 5;

    int FOLDER_TO_MYMUSIC = 6;
    int ALBUM_TO_MYMUSIC = 7;
    int ARTIST_TO_MYMUSIC = 8;

    int MENU_BACKGROUND = 9;
}
