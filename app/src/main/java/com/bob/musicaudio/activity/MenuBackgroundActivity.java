package com.bob.musicaudio.activity;

import android.support.v4.app.Fragment;
import com.bob.musicaudio.fragment.MenuBackgroundFragment;

public class MenuBackgroundActivity extends SingleFragmentActivity {

    @Override
    protected Fragment creatFragment() {
        return new MenuBackgroundFragment();
    }
}
