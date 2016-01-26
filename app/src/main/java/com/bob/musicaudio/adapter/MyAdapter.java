package com.bob.musicaudio.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bob.musicaudio.Interface.IConstants;
import com.bob.musicaudio.R;
import com.bob.musicaudio.SQLdatabase.FavoriteInfoDao;
import com.bob.musicaudio.SQLdatabase.MusicInfoDao;
import com.bob.musicaudio.model.MusicInfo;
import com.bob.musicaudio.service.ServiceManager;
import com.bob.musicaudio.unitls.MusicUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/7/23.
 */

//我的音乐适配器
public class MyAdapter extends BaseAdapter implements IConstants {
    private LayoutInflater mLayoutInflater;
    private ArrayList<MusicInfo> mMusicList;
    private ServiceManager mServiceManager;



    private int mPlayState, mCurPlayMusicIndex = -1;

    private FavoriteInfoDao mFavoriteDao;
    private MusicInfoDao mMusicDao;
    private int mFrom;
    //复用缓存对象
    class ViewHolder {
        TextView musicNameTv, artistTv, durationTv;
        ImageView playStateIconIv, favoriteIv;
    }

    public MyAdapter(Context context, ServiceManager sm) {
        mLayoutInflater = LayoutInflater.from(context);
        mMusicList = new ArrayList<MusicInfo>();
        this.mServiceManager = sm;

        mFavoriteDao = new FavoriteInfoDao(context);
        mMusicDao = new MusicInfoDao(context);
    }

    public void setData(List<MusicInfo> list, int from) {
        setData(list);
        this.mFrom = from;
    }

   // 当数据库中有数据的时候会调用该方法来更新列表
    public void setData(List<MusicInfo> list) {
        mMusicList.clear();
        if (list != null && list.size() > 0) {
            mMusicList.addAll(list);
            notifyDataSetChanged();
        }
    }

    public void refreshPlayingList() {
        if(mMusicList.size() > 0) {
            mServiceManager.refreshMusicList(mMusicList);
        }
    }

    public void refreshFavoriteById(int id, int favorite) {
        int position = MusicUtils.seekPosInListById(mMusicList, id);
        mMusicList.get(position).favorite = favorite;
        notifyDataSetChanged();
    }

    public List<MusicInfo> getData() {
        return mMusicList;
    }


    public void setPlayState(int playState, int playIndex) {
        mPlayState = playState;
        mCurPlayMusicIndex = playIndex;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mMusicList.size();
    }

    @Override
    public MusicInfo getItem(int position) {
        return mMusicList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        final MusicInfo music = getItem(position);
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = mLayoutInflater
                    .inflate(R.layout.musiclist_item, null);
            viewHolder.musicNameTv = (TextView) convertView
                    .findViewById(R.id.musicname_tv);
            viewHolder.artistTv = (TextView) convertView
                    .findViewById(R.id.artist_tv);
            viewHolder.durationTv = (TextView) convertView
                    .findViewById(R.id.duration_tv);
            viewHolder.playStateIconIv = (ImageView) convertView
                    .findViewById(R.id.playstate_iv);
            viewHolder.favoriteIv = (ImageView) convertView
                    .findViewById(R.id.favorite_iv);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (position != mCurPlayMusicIndex) {
            viewHolder.playStateIconIv.setVisibility(View.GONE);
        } else {
            viewHolder.playStateIconIv.setVisibility(View.VISIBLE);
            if (mPlayState == MPS_PAUSE) {
                viewHolder.playStateIconIv
                        .setBackgroundResource(R.drawable.list_pause_state);
            } else {
                viewHolder.playStateIconIv
                        .setBackgroundResource(R.drawable.list_play_state);
            }
        }

        viewHolder.favoriteIv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(music.favorite == 1) {
                    if(mFrom == START_FROM_FAVORITE) {
                        mMusicList.remove(position);
                        notifyDataSetChanged();
                    }
//					music.favorite = 0;
                    mFavoriteDao.deleteById(music._id);
                    mMusicDao.setFavoriteStateById(music._id, 0);
                    viewHolder.favoriteIv.setImageResource(R.drawable.icon_favourite_normal);
                    mMusicList.get(position).favorite = 0;

                } else {
//					music.favorite = 1;
                    mFavoriteDao.saveMusicInfo(music);
                    mMusicDao.setFavoriteStateById(music._id, 1);
                    viewHolder.favoriteIv.setImageResource(R.drawable.icon_favourite_checked);
                    mMusicList.get(position).favorite = 1;

                }
            }
        });

        if(music.favorite == 1) {
            viewHolder.favoriteIv.setImageResource(R.drawable.icon_favourite_checked);
        } else {
            viewHolder.favoriteIv.setImageResource(R.drawable.icon_favourite_normal);
        }

        viewHolder.musicNameTv.setText((position + 1) + "." + music.musicName);
        viewHolder.artistTv.setText(music.artist);
        viewHolder.durationTv
                .setText(MusicUtils.makeTimeString(music.duration));

        return convertView;
    }
}

