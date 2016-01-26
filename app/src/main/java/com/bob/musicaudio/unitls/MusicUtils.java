package com.bob.musicaudio.unitls;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
import android.provider.MediaStore.Audio.Albums;
import android.provider.MediaStore.Files.FileColumns;
import android.text.TextUtils;

import com.bob.musicaudio.model.AlbumInfo;
import com.bob.musicaudio.model.ArtistInfo;
import com.bob.musicaudio.model.FolderInfo;
import com.bob.musicaudio.Interface.IConstants;
import com.bob.musicaudio.SQLdatabase.AlbumInfoDao;
import com.bob.musicaudio.SQLdatabase.ArtistInfoDao;
import com.bob.musicaudio.SQLdatabase.FavoriteInfoDao;
import com.bob.musicaudio.SQLdatabase.FolderInfoDao;
import com.bob.musicaudio.SQLdatabase.MusicInfoDao;
import com.bob.musicaudio.model.MusicInfo;

/**
 * Created by Administrator on 2015/7/13.
 */

//功能：音乐数据处理
public class MusicUtils implements IConstants {
    //所有音乐查询条件数组
    private static String[] proj_music=new String[]{
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.DURATION
    };
    //专辑音乐查询数组
    private static String[] proj_album=new String[]{
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.NUMBER_OF_SONGS,
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM_ART
    };
    //艺术家音乐查询数组
    private static String[] proj_artist=new String[]{
            MediaStore.Audio.Artists.ARTIST,
            MediaStore.Audio.Artists.NUMBER_OF_TRACKS
    };
    //文件夹音乐查询数组
    private static String[] proj_folder=new String[]{
            MediaStore.Files.FileColumns.DATA
    };

    //过滤条件
    public static final int FILTER_SIZE=1*1024*1024;//1MB
    public static final int FILTER_DURATION = 1 * 60 * 1000;// 1分钟
    //艺术加Bitmap优化处理
    private static final BitmapFactory.Options sBitmapOptionsCache = new BitmapFactory.Options();
    private static final BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();
    private static final HashMap<Long, Bitmap> sArtCache = new HashMap<Long, Bitmap>();
    private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");

    static {
        //565快速编码和显示
        sBitmapOptionsCache.inPreferredConfig = Bitmap.Config.RGB_565;
        //禁止图片缩放
        sBitmapOptionsCache.inDither = false;

        sBitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        sBitmapOptions.inDither = false;
    }

    // 歌曲信息数据库
    private static MusicInfoDao mMusicInfoDao;
    // 专辑信息数据库
    private static AlbumInfoDao mAlbumInfoDao;
    // 歌手信息数据库
    private static ArtistInfoDao mArtistInfoDao;
    // 文件夹信息数据库
    private static FolderInfoDao mFolderInfoDao;
    //我的收藏信息数据库
    private static FavoriteInfoDao mFavoriteDao;



    public static List<MusicInfo> queryFavorite(Context context) {
        //复用对象优化
        if(mFavoriteDao == null) {
            mFavoriteDao = new FavoriteInfoDao(context);
        }
        //从数据库返回音乐数据
        return mFavoriteDao.getMusicInfo();
    }


    //获取包含音频文件的文件夹信息
    public static List<FolderInfo> queryFolder(Context context) {
        if(mFolderInfoDao == null) {
            mFolderInfoDao = new FolderInfoDao(context);
        }

        SPStorage sp = new SPStorage(context);
        Uri uri = MediaStore.Files.getContentUri("external");
        //内容提供者
        ContentResolver cr = context.getContentResolver();
        //构建查询条件
        StringBuilder mSelection = new StringBuilder(FileColumns.MEDIA_TYPE
                + " = " + FileColumns.MEDIA_TYPE_AUDIO + " and " + "("
                + FileColumns.DATA + " like'%.mp3' or " + Media.DATA
                + " like'%.wma')");
        // 查询语句：检索出.mp3为后缀名，时长大于1分钟，文件大小大于1MB的媒体文件
        if(sp.getFilterSize()) {
            mSelection.append(" and " + Media.SIZE + " > " + FILTER_SIZE);
        }
        if(sp.getFilterTime()) {
            mSelection.append(" and " + Media.DURATION + " > " + FILTER_DURATION);
        }

        mSelection.append(") group by ( " + FileColumns.PARENT);
        //数据库存在文件时返回数据，否则查询内容提供者并添加到数据库
        if (mFolderInfoDao.hasData()) {
            return mFolderInfoDao.getFolderInfo();
        } else {
            List<FolderInfo> list = getFolderList(cr.query(uri, proj_folder, mSelection.toString(), null, null));
            mFolderInfoDao.saveFolderInfo(list);
            return list;
        }
    }


    //获取歌手信息
    public static List<ArtistInfo> queryArtist(Context context) {
        if(mArtistInfoDao == null) {
            mArtistInfoDao = new ArtistInfoDao(context);
        }
        //内容提供者路径
        Uri uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
        ContentResolver cr = context.getContentResolver();
        //数据库存在艺术家时返回数据，否则查询内容提供者并添加到数据库
        if (mArtistInfoDao.hasData()) {
            return mArtistInfoDao.getArtistInfo();
        } else {
            //降序查询
            List<ArtistInfo> list = getArtistList(cr.query(uri, proj_artist,
                    null, null, MediaStore.Audio.Artists.NUMBER_OF_TRACKS
                            + " desc"));
            mArtistInfoDao.saveArtistInfo(list);
            return list;
        }
    }


    //获取专辑信息
    public static List<AlbumInfo> queryAlbums(Context context) {
        if(mAlbumInfoDao == null) {
            mAlbumInfoDao = new AlbumInfoDao(context);
        }

        SPStorage sp = new SPStorage(context);
        //查询路径
        Uri uri = Albums.EXTERNAL_CONTENT_URI;
        ContentResolver cr = context.getContentResolver();
        //连表查询
        StringBuilder where = new StringBuilder(Albums._ID
                + " in (select distinct " + Media.ALBUM_ID
                + " from audio_meta where (1=1 ");

        if(sp.getFilterSize()) {
            where.append(" and " + Media.SIZE + " > " + FILTER_SIZE);
        }
        if(sp.getFilterTime()) {
            where.append(" and " + Media.DURATION + " > " + FILTER_DURATION);
        }
        where.append("))");

        if (mAlbumInfoDao.hasData()) {
            return mAlbumInfoDao.getAlbumInfo();
        } else {
            // Media.ALBUM_KEY 按专辑名称排序
            List<AlbumInfo> list = getAlbumList(cr.query(uri, proj_album,
                    where.toString(), null, Media.ALBUM_KEY));
            mAlbumInfoDao.saveAlbumInfo(list);
            return list;
        }
    }



    //对数据库的操作，不同的界面进来要做不同的查询
    public static List<MusicInfo> queryMusic(Context context, int from) {
        return queryMusic(context, null, null, from);
    }

    public static List<MusicInfo> queryMusic(Context context,
                                             String selections, String selection, int from) {
        if(mMusicInfoDao == null) {
            mMusicInfoDao = new MusicInfoDao(context);
        }
        SPStorage sp = new SPStorage(context);
        //构建路径
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        ContentResolver cr = context.getContentResolver();
        //构建查询条件
        StringBuffer select = new StringBuffer(" 1=1 ");
        // 查询语句：检索出.mp3为后缀名，时长大于1分钟，文件大小大于1MB的媒体文件
        if(sp.getFilterSize()) {
            select.append(" and " + Media.SIZE + " > " + FILTER_SIZE);
        }
        if(sp.getFilterTime()) {
            select.append(" and " + Media.DURATION + " > " + FILTER_DURATION);
        }

        if (!TextUtils.isEmpty(selections)) {
            select.append(selections);
        }

        switch(from) {
            case START_FROM_LOCAL:
                if (mMusicInfoDao.hasData()) {
                    return mMusicInfoDao.getMusicInfo();
                } else {
                    List<MusicInfo> list = getMusicList(cr.query(uri, proj_music,
                            select.toString(), null,
                            MediaStore.Audio.Media.ARTIST_KEY));
                    mMusicInfoDao.saveMusicInfo(list);
                    return list;
                }
            case START_FROM_ARTIST:
                if (mMusicInfoDao.hasData()) {
                    return mMusicInfoDao.getMusicInfoByType(selection, START_FROM_ARTIST);
                }
            case START_FROM_ALBUM:
                if (mMusicInfoDao.hasData()) {
                    return mMusicInfoDao.getMusicInfoByType(selection, START_FROM_ALBUM);
                }
            case START_FROM_FOLDER:
                if(mMusicInfoDao.hasData()) {
                    return mMusicInfoDao.getMusicInfoByType(selection, START_FROM_FOLDER);
                }
            default:
                return null;
        }

    }


    //以下为实际查询的方法
    //按文件夹查询
    public static List<FolderInfo> getFolderList(Cursor cursor) {
        List<FolderInfo> list = new ArrayList<FolderInfo>();
        while (cursor.moveToNext()) {
            //初始化封装类
            FolderInfo info = new FolderInfo();
            String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
            info.folder_path = filePath.substring(0, filePath.lastIndexOf(File.separator));
            info.folder_name = info.folder_path.substring(info.folder_path.lastIndexOf(File.separator) + 1);
            list.add(info);
        }
        cursor.close();
        return list;
    }

    //按艺术家查询
    public static List<ArtistInfo> getArtistList(Cursor cursor) {
        List<ArtistInfo> list = new ArrayList<ArtistInfo>();
        while (cursor.moveToNext()) {
            ArtistInfo info = new ArtistInfo();
            info.artist_name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST));
            //歌曲数目
            info.number_of_tracks = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS));
            list.add(info);
        }
        cursor.close();
        return list;
    }

    //按专辑查询
    public static List<AlbumInfo> getAlbumList(Cursor cursor) {
        List<AlbumInfo> list = new ArrayList<AlbumInfo>();
        while (cursor.moveToNext()) {
            AlbumInfo info = new AlbumInfo();
            info.album_name = cursor.getString(cursor.getColumnIndex(Albums.ALBUM));
            info.album_id = cursor.getInt(cursor.getColumnIndex(Albums._ID));
            //歌曲数目
            info.number_of_songs = cursor.getInt(cursor.getColumnIndex(Albums.NUMBER_OF_SONGS));
            info.album_art = cursor.getString(cursor.getColumnIndex(Albums.ALBUM_ART));
            list.add(info);
        }
        cursor.close();
        return list;
    }
    //查询所有音乐数据
    public static ArrayList<MusicInfo> getMusicList(Cursor cursor) {
        if (cursor == null) {
            return null;
        }
        ArrayList<MusicInfo> musicList = new ArrayList<MusicInfo>();
        while (cursor.moveToNext()) {
            //音乐数据封装类
            MusicInfo music = new MusicInfo();
            music.songId = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Audio.Media._ID));
            music.albumId = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            music.duration = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Audio.Media.DURATION));
            music.musicName = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.TITLE));
            music.artist = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.ARTIST));

            String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            music.data = filePath;

            String folderPath = filePath.substring(0, filePath.lastIndexOf(File.separator));
            music.folder = folderPath;
            musicList.add(music);
        }
        cursor.close();
        return musicList;
    }

   //根据歌曲的ID，寻找出歌曲在当前播放列表中的位置，遍历集合
    public static int seekPosInListById(List<MusicInfo> list, int id) {
        if(id == -1) {
            return -1;
        }
        int result = -1;
        if (list != null) {

            for (int i = 0; i < list.size(); i++) {
                if (id == list.get(i).songId) {
                    result = i;
                    break;
                }
            }
        }
        return result;
    }

    //格式化时间
    public static String makeTimeString(long milliSecs) {
        StringBuffer sb = new StringBuffer();
        long m = milliSecs / (60 * 1000);
        sb.append(m < 10 ? "0" + m : m);
        sb.append(":");
        long s = (milliSecs % (60 * 1000)) / 1000;
        sb.append(s < 10 ? "0" + s : s);
        return sb.toString();
    }

    //艺术家图片处理
    public static Bitmap getCachedArtwork(Context context, long artIndex,
                                          Bitmap defaultArtwork) {
        Bitmap bitmap = null;
        synchronized (sArtCache) {
            bitmap = sArtCache.get(artIndex);
        }
        if(context == null) {
            return null;
        }
        if (bitmap == null) {
            bitmap = defaultArtwork;
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            Bitmap b = MusicUtils.getArtworkQuick(context, artIndex, w, h);
            if (b != null) {
                bitmap = b;
                //异步加锁
                synchronized (sArtCache) {
                    // the cache may have changed since we checked
                    Bitmap value = sArtCache.get(artIndex);
                    if (value == null) {
                        sArtCache.put(artIndex, bitmap);
                    } else {
                        bitmap = value;
                    }
                }
            }
        }
        return bitmap;
    }

    //查询艺术家图片
    public static Bitmap getArtworkQuick(Context context, long album_id, int w,
                                         int h) {
        // NOTE: There is in fact a 1 pixel border on the right side in the
        // ImageView
        // used to display this drawable. Take it into account now, so we don't
        // have to
        // scale later.
        w -= 1;
        ContentResolver res = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
        if (uri != null) {
            ParcelFileDescriptor fd = null;
            try {
                fd = res.openFileDescriptor(uri, "r");
                int sampleSize = 1;

                // Compute the closest power-of-two scale factor
                // and pass that to sBitmapOptionsCache.inSampleSize, which will
                // result in faster decoding and better quality
                sBitmapOptionsCache.inJustDecodeBounds = true;
                //Bitmap优化
                BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null, sBitmapOptionsCache);
                int nextWidth = sBitmapOptionsCache.outWidth >> 1;
                int nextHeight = sBitmapOptionsCache.outHeight >> 1;
                while (nextWidth > w && nextHeight > h) {
                    sampleSize <<= 1;
                    nextWidth >>= 1;
                    nextHeight >>= 1;
                }

                sBitmapOptionsCache.inSampleSize = sampleSize;
                sBitmapOptionsCache.inJustDecodeBounds = false;
                Bitmap b = BitmapFactory.decodeFileDescriptor(
                        fd.getFileDescriptor(), null, sBitmapOptionsCache);

                if (b != null) {
                    // finally rescale to exactly the size we need
                    if (sBitmapOptionsCache.outWidth != w
                            || sBitmapOptionsCache.outHeight != h) {
                        Bitmap tmp = Bitmap.createScaledBitmap(b, w, h, true);
                        // Bitmap.createScaledBitmap() can return the same
                        // bitmap
                        if (tmp != b)
                            b.recycle();
                        b = tmp;
                    }
                }

                return b;
            } catch (FileNotFoundException e) {
            } finally {
                try {
                    if (fd != null)
                        fd.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }
}
