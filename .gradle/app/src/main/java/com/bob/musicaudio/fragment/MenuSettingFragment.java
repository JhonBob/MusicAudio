package com.bob.musicaudio.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.bob.musicaudio.Interface.IConstants;
import com.bob.musicaudio.R;
import com.bob.musicaudio.unitls.SPStorage;

/**
 * Created by Administrator on 2015/8/1.
 */
public class MenuSettingFragment extends Fragment implements OnClickListener, IConstants {

    private LinearLayout mAdviceLayout, mAboutLayout;
    private CheckedTextView mChangeSongTv, mFilterSizeTv, mFilterTimeTv;
    private SPStorage mSp;

    private ImageButton mBackBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.menu_setting_fragment, container, false);

        mSp = new SPStorage(getActivity());

        initView(view);

        return view;
    }

    private void initView(View view) {
        mAboutLayout = (LinearLayout) view.findViewById(R.id.setting_about_layout);
        mAdviceLayout = (LinearLayout) view.findViewById(R.id.setting_advice_layout);
        mAboutLayout.setOnClickListener(this);
        mAdviceLayout.setOnClickListener(this);

        mBackBtn = (ImageButton) view.findViewById(R.id.backBtn);
        mBackBtn.setOnClickListener(this);

        mChangeSongTv = (CheckedTextView) view.findViewById(R.id.shake_change_song);
        mFilterSizeTv = (CheckedTextView) view.findViewById(R.id.filter_size);
        mFilterTimeTv = (CheckedTextView) view.findViewById(R.id.filter_time);

        mChangeSongTv.setChecked(mSp.getShake());
        mFilterSizeTv.setChecked(mSp.getFilterSize());
        mFilterTimeTv.setChecked(mSp.getFilterTime());

        mChangeSongTv.setOnClickListener(this);
        mFilterSizeTv.setOnClickListener(this);
        mFilterTimeTv.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.setting_about_layout:
                break;
            case R.id.setting_advice_layout:
                break;
            case R.id.shake_change_song:
                mChangeSongTv.toggle();
                mSp.saveShake(mChangeSongTv.isChecked());
                Intent intent = new Intent(BROADCAST_SHAKE);
                intent.putExtra(SHAKE_ON_OFF, mChangeSongTv.isChecked());
                getActivity().sendBroadcast(intent);
                break;
            case R.id.filter_size:
                mFilterSizeTv.toggle();
                mSp.saveFilterSize(mFilterSizeTv.isChecked());
                break;
            case R.id.filter_time:
                mFilterTimeTv.toggle();
                mSp.saveFilterTime(mFilterTimeTv.isChecked());
                break;
            case R.id.backBtn:
                getActivity().finish();
                break;
        }
    }
}

