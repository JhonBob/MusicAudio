package com.bob.musicaudio.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.bob.musicaudio.R;

public class AboutActivity extends Activity {

    private ImageButton mbackButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_about_layout);
        mbackButton=(ImageButton)findViewById(R.id.ibtn_player_back_return);
        mbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}