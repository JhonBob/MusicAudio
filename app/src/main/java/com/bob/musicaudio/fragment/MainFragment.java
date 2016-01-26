package com.bob.musicaudio.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bob.musicaudio.R;
import com.bob.musicaudio.SQLdatabase.AlbumInfoDao;
import com.bob.musicaudio.SQLdatabase.ArtistInfoDao;
import com.bob.musicaudio.SQLdatabase.FavoriteInfoDao;
import com.bob.musicaudio.SQLdatabase.FolderInfoDao;
import com.bob.musicaudio.Interface.IConstants;
import com.bob.musicaudio.model.MusicInfo;
import com.bob.musicaudio.service.IMediaService;
import com.bob.musicaudio.Interface.IOnServiceConnectComplete;
import com.bob.musicaudio.service.ServiceManager;
import com.bob.musicaudio.uimanager.MainBottomUIManager;
import com.bob.musicaudio.app.MusicApp;
import com.bob.musicaudio.SQLdatabase.MusicInfoDao;
import com.bob.musicaudio.unitls.MusicTimer;
import com.bob.musicaudio.uimanager.UIManager;


/**
 * Created by Administrator on 2015/7/12.
 */


//应用程序主界面
public class MainFragment extends Fragment implements IConstants,UIManager.OnRefreshListener,
        IOnServiceConnectComplete{
    public GridView mGridView;
    private MyListViewAdapter mAdapter;
    public UIManager mUIManager;
    public MusicTimer mMusicTimer;

    //数据库API
    private MusicInfoDao mMusicDao;
    private FolderInfoDao mFolderDao;
    private ArtistInfoDao mArtistDao;
    private AlbumInfoDao mAlbumDao;
    private FavoriteInfoDao mFavoriteDao;

    public RelativeLayout mBottomLayout,mMainLayout;
    private ServiceManager mServiceManager;
    private MainBottomUIManager mBottomUIManager;
    private MusicPlayBroadcast mPlayBroadcast;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMusicDao = new MusicInfoDao(getActivity());
        mFolderDao = new FolderInfoDao(getActivity());
        mArtistDao = new ArtistInfoDao(getActivity());
        mAlbumDao = new AlbumInfoDao(getActivity());
        mFavoriteDao = new FavoriteInfoDao(getActivity());
        mServiceManager = MusicApp.mServiceManager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.frame_main,container,false);
        mGridView=(GridView)v.findViewById(R.id.gridview);
        mAdapter=new MyListViewAdapter();

        //界面布局
        mMainLayout=(RelativeLayout)v.findViewById(R.id.mainLayout);
        mBottomLayout=(RelativeLayout)v.findViewById(R.id.bottomLayout);

        //服务管理
        MusicApp.mServiceManager.connectService();
        MusicApp.mServiceManager.setOnServiceConnectComplete(this);

        //设置适配
        mGridView.setAdapter(mAdapter);

        mUIManager=new UIManager(getActivity(),v);
        mUIManager.setOnRefreshListener(this);


        mBottomUIManager = new MainBottomUIManager(getActivity(), v);


        //广播处理动态注册
        mPlayBroadcast = new MusicPlayBroadcast();
        IntentFilter filter = new IntentFilter(BROADCAST_NAME);
        filter.addAction(BROADCAST_NAME);
        getActivity().registerReceiver(mPlayBroadcast, filter);
        return v;
    }

    //布局适配器
    private class MyListViewAdapter extends BaseAdapter{
        private int musicNum=0,artistNum=0,albumNum=0,favoriteNum=0,folderNum=0;
        private int[] drawable=new int[]{R.drawable.icon_local_music,
                                         R.drawable.icon_favorites,
                                         R.drawable.icon_folder_plus,
                                         R.drawable.icon_artist_plus,
                                         R.drawable.icon_album_plus};
        private String[] name=new String[]{"我的音乐","我的最爱","文件夹","艺术家","专辑"};

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView==null){
                holder=new ViewHolder();
                convertView=getActivity().getLayoutInflater().inflate(R.layout.gridview_item,null);
                holder.iv=(ImageView)convertView.findViewById(R.id.gridview_item_iv);
                holder.nameTv=(TextView)convertView.findViewById(R.id.gridview_item_name);
                holder.numTv=(TextView)convertView.findViewById(R.id.gridview_item_num);
                convertView.setTag(holder);
            }else{
                holder=(ViewHolder)convertView.getTag();
            }
            switch (position) {
                case 0:
                    holder.numTv.setText(musicNum + "");
                    break;
                case 1:
                    holder.numTv.setText(favoriteNum + "");
                    break;
                case 2:
                    holder.numTv.setText(folderNum + "");
                    break;
                case 3:
                    holder.numTv.setText(artistNum + "");
                    break;
                case 4:
                    holder.numTv.setText(albumNum + "");
            }

            convertView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int from = -1;
                    switch (position) {
                        case 0:// 我的音乐
                            from = START_FROM_LOCAL;
                            break;
                        case 1:// 我的最爱
                            from = START_FROM_FAVORITE;
                            break;
                        case 2:// 文件夹
                            from = START_FROM_FOLDER;
                            break;
                        case 3:// 歌手
                            from = START_FROM_ARTIST;
                            break;
                        case 4:// 专辑
                            from = START_FROM_ALBUM;
                            break;
                    }
                    mUIManager.setContentType(from);
                }
            });

            holder.iv.setImageResource(drawable[position]);
            holder.nameTv.setText(name[position]);
            return convertView;
        }

        //设置数据
        public void setNum(int music_num,int artist_num,int album_num,
                           int folder_num,int favorite_num){
            musicNum=music_num;
            artistNum=artist_num;
            albumNum=album_num;
            folderNum=folder_num;
            favoriteNum=favorite_num;
           notifyDataSetChanged();
        }

        //缓存复用
        private class ViewHolder {
            ImageView iv;
            TextView nameTv, numTv;
        }
    }

    //刷新界面数据
    public void refreshNum() {
        int musicCount = mMusicDao.getDataCount();
        int artistCount = mArtistDao.getDataCount();
        int albumCount = mAlbumDao.getDataCount();
        int folderCount = mFolderDao.getDataCount();
        int favoriteCount = mFavoriteDao.getDataCount();
        //设置更新数据
       mAdapter.setNum(musicCount, artistCount, albumCount, folderCount, favoriteCount);
    }

    //刷新数据
    @Override
    public void onRefresh() {
        refreshNum();
    }



    //服务回掉刷新主界面数据
    @Override
    public void onServiceConnectComplete(IMediaService service) {
        // service绑定成功会执行到这里
        //刷新数据
        refreshNum();
    }

    private class MusicPlayBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BROADCAST_NAME)) {
                MusicInfo music = new MusicInfo();
                int playState = intent.getIntExtra(PLAY_STATE_NAME, MPS_NOFILE);
                Bundle bundle = intent.getBundleExtra(MusicInfo.KEY_MUSIC);
                if (bundle != null) {
                    music = bundle.getParcelable(MusicInfo.KEY_MUSIC);
                }

                switch (playState) {
                    case MPS_INVALID:// 考虑后面加上如果文件不可播放直接跳到下一首
                        //mMusicTimer.stopTimer();

                        mBottomUIManager.refreshUI(0, music.duration, music);
                        mBottomUIManager.showPlay(true);
                        break;
                    case MPS_PAUSE:
                        //mMusicTimer.stopTimer();

                        mBottomUIManager.refreshUI(mServiceManager.position(), music.duration,
                                music);
                        mBottomUIManager.showPlay(true);

                        mServiceManager.cancelNotification();
                        break;
                    case MPS_PLAYING:
                        //mMusicTimer.startTimer();

                        mBottomUIManager.refreshUI(mServiceManager.position(), music.duration,
                                music);
                        mBottomUIManager.showPlay(false);

                        break;
                    case MPS_PREPARE:
                       // mMusicTimer.stopTimer();
                        mBottomUIManager.refreshUI(0, music.duration, music);
                        mBottomUIManager.showPlay(true);
                        break;
                }
            }
        }
    }

}
