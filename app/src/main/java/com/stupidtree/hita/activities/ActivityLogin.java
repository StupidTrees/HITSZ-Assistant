package com.stupidtree.hita.activities;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.BaseTabAdapter;
import com.stupidtree.hita.fragments.user.FragmentLogin;
import com.stupidtree.hita.fragments.user.FragmentSignup;
import com.stupidtree.hita.views.LongStringDialog;

public class ActivityLogin extends BaseActivity {

    ViewPager pager;
    BaseTabAdapter pagerAdapter;
    TabLayout tabs;
    TextView userPro, privacyPro;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true, false, false);
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
        userPro = findViewById(R.id.user_protocol);
        privacyPro = findViewById(R.id.privacy_protocol);
        userPro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LongStringDialog(getThis(), R.string.name_user_agreement, R.string.user_agreement, R.string.i_have_read).show();
            }
        });
        privacyPro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LongStringDialog(getThis(), R.string.name_privacy_agreement, R.string.privacy_policy, R.string.i_have_read).show();
            }
        });
        final int[] tabs = new int[]{R.string.sign_up, R.string.log_in};
        pagerAdapter = new BaseTabAdapter(getSupportFragmentManager(), 2) {
            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return getString(tabs[position]);
            }

            @Override
            protected Fragment initItem(int position) {
                switch (position) {
                    case 0:
                        return new FragmentSignup();
                    case 1:
                        return new FragmentLogin();
                }
                return null;
            }
        };
        pager.setAdapter(pagerAdapter);

    }
}

