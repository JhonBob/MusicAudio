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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bob.musicaudio.R;
import com.bob.musicaudio.model.FolderInfo;
import com.bob.musicaudio.Interface.IConstants;
import com.bob.musicaudio.unitls.MusicUtils;
import com.bob.musicaudio.unitls.SPStorage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/7/23.
 */
public class FolderBrowserManager extends MainUIManager implements IConstants,View.OnClickListener,
        AdapterView.OnItemClickListener{
    private Activity mActivity;
    private LayoutInflater mInflater;

    private ListView mListView;
    private MyAdapter mAdapter;
    private List<FolderInfo> list = new ArrayList<FolderInfo>();
    private ImageButton mBackBtn;

    private UIManager mUIManager;

    private RelativeLayout mFolderLayout;

    public FolderBrowserManager(Activity activity, UIManager manager) {
        this.mActivity = activity;
        this.mInflater = LayoutInflater.from(activity);
        this.mUIManager = manager;
    }

    public View getView() {
        View folderView = mInflater.inflate(R.layout.folderbrower, null);
        initView(folderView);
        return folderView;
    }

    private void initView(View view) {

        mBackBtn = (ImageButton) view.findViewById(R.id.backBtn);
        mBackBtn.setOnClickListener(this);

        mListView = (ListView) view.findViewById(R.id.folder_listview);
        mListView.setOnItemClickListener(this);
        mAdapter = new MyAdapter();
        list = MusicUtils.queryFolder(mActivity);
        mListView.setAdapter(mAdapter);
    }


    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public FolderInfo getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FolderInfo folder = getItem(position);
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = mActivity.getLayoutInflater().inflate(
                        R.layout.folderbrower_listitem, null);
                viewHolder.folderNameTv = (TextView) convertView
                        .findViewById(R.id.folder_name_tv);
                viewHolder.folderPathTv = (TextView) convertView
                        .findViewById(R.id.folder_path_tv);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.folderNameTv.setText(folder.folder_name);
            viewHolder.folderPathTv.setText(folder.folder_path);

            return convertView;
        }

        private class ViewHolder {
            TextView folderNameTv, folderPathTv;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        mUIManager
                .setContentType(FOLDER_TO_MYMUSIC, mAdapter.getItem(position));
    }

    @Override
    public void onClick(View v) {
        mUIManager.setCurrentItem();
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
