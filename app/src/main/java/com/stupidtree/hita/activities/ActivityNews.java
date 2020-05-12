package com.stupidtree.hita.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.NewsPagerAdapter;
import com.stupidtree.hita.fragments.news.FragmentNewsBulletin;
import com.stupidtree.hita.fragments.news.FragmentNewsIPNews;
import com.stupidtree.hita.fragments.news.FragmentNewsLecture;

import java.util.ArrayList;
import java.util.List;

public class ActivityNews extends BaseActivity {

    ViewPager pager;
    NewsPagerAdapter pagerAdapter;
    List<Fragment> fragments;
    TabLayout tab;
    Toolbar toolbar;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true,true,false);
        setContentView(R.layout.activity_news);
        initPager();

    }

    void initPager(){
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        tab = findViewById(R.id.hitszinfo_tab);
        pager = findViewById(R.id.hitszinfo_pager);
        fragments = new ArrayList<>();
        fragments.add(FragmentNewsIPNews.getInstance("75"));
        fragments.add(FragmentNewsIPNews.getInstance("77"));
        fragments.add(new FragmentNewsLecture());
        fragments.add(new FragmentNewsBulletin());
        fragments.add(FragmentNewsIPNews.getInstance("116"));

        pagerAdapter = new NewsPagerAdapter(getSupportFragmentManager(),fragments,getResources().getStringArray(R.array.news_tabs));
        pager.setAdapter(pagerAdapter);
        tab.setTabIndicatorFullWidth(false);
        tab.setupWithViewPager(pager);
        if(!TextUtils.isEmpty(getIntent().getStringExtra("terminal"))){
            pager.setCurrentItem(Integer.parseInt(getIntent().getStringExtra("terminal")));
        }

    }


}
