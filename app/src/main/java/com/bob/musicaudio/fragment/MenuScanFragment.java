package com.bob.musicaudio.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import com.bob.musicaudio.R;
import com.bob.musicaudio.SQLdatabase.DatabaseHelper;
import com.bob.musicaudio.Interface.IConstants;
import com.bob.musicaudio.model.FolderInfo;
import com.bob.musicaudio.unitls.MusicUtils;

import java.io.File;

/**
 * Created by Administrator on 2015/7/31.
 */
public class MenuScanFragment extends Fragment implements IConstants, OnClickListener {

    private Button mScanBtn;
    private ImageButton mBackBtn;
    private Handler mHandler;
    private DatabaseHelper mHelper;
    private ProgressDialog mProgress;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHelper = new DatabaseHelper(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.menu_scan_frament, container,
                false);
        mScanBtn = (Button) view.findViewById(R.id.scanbtn);
        mBackBtn=(ImageButton)view.findViewById(R.id.sbackbtn);
        mBackBtn.setOnClickListener(this);
        mScanBtn.setOnClickListener(this);


        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                mProgress.dismiss();
            }
        };

        return view;
    }

    private void getData() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                mHelper.deleteTables(getActivity());
                MusicUtils.queryMusic(getActivity(), START_FROM_LOCAL);
                MusicUtils.queryAlbums(getActivity());
                MusicUtils.queryArtist(getActivity());
                MusicUtils.queryFolder(getActivity());
                mHandler.sendEmptyMessage(1);
            }
        }).start();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.scanbtn:
            mProgress = new ProgressDialog(getActivity());
            mProgress.setMessage("正在扫描歌曲，请勿退出软件！");
            mProgress.setCancelable(false);
            mProgress.setCanceledOnTouchOutside(false);
            mProgress.show();
            getData();
                break;
            case R.id.sbackbtn:
               getActivity().finish();
                break;
        }
    }
}

