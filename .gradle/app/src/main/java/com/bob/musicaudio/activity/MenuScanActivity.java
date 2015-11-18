package com.bob.musicaudio.activity;


import android.support.v4.app.Fragment;

import com.bob.musicaudio.fragment.MenuScanFragment;


public class MenuScanActivity extends SingleFragmentActivity {
    @Override
    protected Fragment creatFragment() {
        return new MenuScanFragment();
    }
}
