package com.stupidtree.hita.activities;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;

import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;
import com.stupidtree.hita.fragments.FragmentSettings;

public class ActivitySetting extends BaseActivity {

    Toolbar mToolbar;

    @Override
    protected void stopTasks() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true,true,false);
        setContentView(R.layout.activity_setting);
        initToolbar();
        FragmentSettings fragmentSettings = new FragmentSettings();
        getSupportFragmentManager().beginTransaction().replace(R.id.settingsLayout,fragmentSettings).commit();
    }

    void initToolbar(){
        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle("设置");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }
}
