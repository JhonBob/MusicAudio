package com.bob.musicaudio.uimanager;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bob.musicaudio.R;
import com.bob.musicaudio.model.AlbumInfo;
import com.bob.musicaudio.Interface.IConstants;
import com.bob.musicaudio.unitls.MusicUtils;
import com.bob.musicaudio.unitls.SPStorage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/7/23.
 */
//专辑歌曲显示
public class AlbumBrowserManager extends MainUIManager implements IConstants,View.OnClickListener,
        AdapterView.OnItemClickListener {
    private Activity mActivity;
    private UIManager mUIManager;
    private LayoutInflater mInflater;

    private ListView mListView;
    private ImageButton mBackBtn;
    private List<AlbumInfo> mAlbumList = new ArrayList<AlbumInfo>();
    private MyAdapter mAdapter;

    public AlbumBrowserManager(Activity activity, UIManager manager) {
        this.mActivity = activity;
        this.mUIManager = manager;
        this.mInflater = LayoutInflater.from(activity);
    }

    public View getView() {
        View view = mInflater.inflate(R.layout.albumbrower, null);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mListView = (ListView) view.findViewById(R.id.album_listview);
        mListView.setOnItemClickListener(this);
        mBackBtn = (ImageButton) view.findViewById(R.id.backBtn);
        mBackBtn.setOnClickListener(this);

        mAlbumList = MusicUtils.queryAlbums(mActivity);
        mAdapter = new MyAdapter();
        mListView.setAdapter(mAdapter);

    }


    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mAlbumList.size();
        }

        @Override
        public AlbumInfo getItem(int position) {
            return mAlbumList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            AlbumInfo album = getItem(position);

            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = mActivity.getLayoutInflater().inflate(
                        R.layout.albumbrower_listitem, null);
                viewHolder.albumNameTv = (TextView) convertView
                        .findViewById(R.id.album_name_tv);
                viewHolder.numberTv = (TextView) convertView
                        .findViewById(R.id.number_of_songs_tv);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.albumNameTv.setText(album.album_name);
            viewHolder.numberTv.setText(album.number_of_songs + "首歌");

            return convertView;
        }

        private class ViewHolder {
            TextView albumNameTv, numberTv;
        }

    }

    @Override
    public void onClick(View v) {
        if (v == mBackBtn) {
            mUIManager.setCurrentItem();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {

        //进入MyMusic界面
        mUIManager.setContentType(ALBUM_TO_MYMUSIC, mAdapter.getItem(position));
    }

    @Override
    public View getView(int from) {
        return null;
    }

    @Override
    public View getView(int from, Object obj) {
        return null;
    }
}
