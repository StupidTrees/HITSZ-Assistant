package com.stupidtree.hita.activities;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;

import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.SubjectsManagerPagerAdapter;
import com.stupidtree.hita.fragments.FragmentTeachers;
import com.stupidtree.hita.jwts.FragmentJWTS_info;
import com.stupidtree.hita.fragments.FragmentSubjects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ActivitySubjectManager extends BaseActivity implements FragmentSubjects.OnFragmentInteractionListener,
        FragmentJWTS_info.OnListFragmentInteractionListener,FragmentTeachers.OnFragmentInteractionListener
{

    ViewPager viewpager;
    SubjectsManagerPagerAdapter pagerAdapter;
    List<Fragment> fragments;
    TabLayout tabLayout;

    @Override
    protected void stopTasks() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_manager);
        setWindowParams(true,true,false);
        initToolbar();
        initPager();

    }
    void initToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
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

    void initPager(){
        tabLayout = findViewById(R.id.subjects_tablayout);
        viewpager = findViewById(R.id.subjects_viewpager);
        String[] titles= {"科目","教师"};
        fragments = new ArrayList<>();
        fragments.add(new FragmentSubjects());
        fragments.add(FragmentTeachers.newInstance());
        pagerAdapter = new SubjectsManagerPagerAdapter(getSupportFragmentManager(),fragments,Arrays.asList(titles));
        viewpager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewpager);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setTabIndicatorFullWidth(false);
        //tabLayout.setTabTextColors(ColorStateList.valueOf(getColorPrimary()));
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
