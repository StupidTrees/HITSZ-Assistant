package com.stupidtree.hita.activities;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.view.View;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.tabs.TabLayout;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;
import com.stupidtree.hita.fragments.pref.FragmentSettings;

import java.util.ArrayList;
import java.util.List;

public class ActivitySetting extends BaseActivity {

    Toolbar mToolbar;
    ViewPager pager;
    TabLayout tabLayout;
    List<FragmentSettings> fragments;

    @Override
    protected void stopTasks() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        setWindowParams(true,false,false);
        setContentView(R.layout.activity_setting);
        initToolbar();
        initPager();

//        FragmentSettings fragmentSettings = new FragmentSettings();
//        getSupportFragmentManager().beginTransaction().replace(R.id.settingsLayout,fragmentSettings).commit();
    }



    void initToolbar(){
        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapse);
        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
        collapsingToolbarLayout.setExpandedTitleColor(Color.WHITE);
        mToolbar = findViewById(R.id.toolbar);
       // mToolbar.setTitle(getString(R.string.label_activity_settings));
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

    void initPager(){
        fragments = new ArrayList<>();
        pager = findViewById(R.id.pager);
        tabLayout = findViewById(R.id.tabs);
        fragments.add(new FragmentSettings(R.xml.pref_basic,getString(R.string.preference_tabs_basic)));
        fragments.add(new FragmentSettings(R.xml.pref_appearence,getString(R.string.preference_tabs_appearance)));
        fragments.add(new FragmentSettings(R.xml.pref_others,getString(R.string.preference_tabs_others)));
        pager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager(),FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }

            @Override
            public int getCount() {
                return fragments.size();
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return fragments.get(position).getTitle();
            }
        });
        tabLayout.setupWithViewPager(pager);
        if(getIntent()!=null&&getIntent().hasExtra("target")){
            switch(getIntent().getStringExtra("target")){
                case "basic":
                    pager.setCurrentItem(0);
                    break;
                case "appearance":
                    pager.setCurrentItem(1);
                    break;
                case"other":
                    pager.setCurrentItem(2);
                    break;
            }
        }

    }

}
