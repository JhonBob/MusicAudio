package com.bob.musicaudio.activity;

import android.support.v4.app.Fragment;

import com.bob.musicaudio.fragment.MenuSettingFragment;


public class MenuSettingActivity extends SingleFragmentActivity {

    @Override
    protected Fragment creatFragment() {
        return new MenuSettingFragment();
    }
}
