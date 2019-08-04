package com.stupidtree.hita.activities;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.MainPagerAdapter;
import com.stupidtree.hita.fragments.FragmentLogin;
import com.stupidtree.hita.fragments.FragmentSignup;

import java.util.ArrayList;
import java.util.List;

public class ActivityLogin extends BaseActivity {

    ViewPager pager;
    MainPagerAdapter pagerAdapter;
    List<Fragment> fragments;
    TabLayout tabs;

    @Override
    protected void stopTasks() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true,false,false);
        setContentView(R.layout.activity_login);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        initToolbar();
        initPager();
    }

    void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    void initPager() {
        pager = findViewById(R.id.login_pager);
        tabs = findViewById(R.id.login_tabs);
        tabs.setupWithViewPager(pager);
        fragments = new ArrayList<>();
        fragments.add(new FragmentSignup());
        fragments.add(new FragmentLogin());
        pagerAdapter = new MainPagerAdapter(getSupportFragmentManager(), fragments, new String[]{"注册", "登录"});
        pager.setAdapter(pagerAdapter);

    }
}

