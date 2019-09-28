package com.stupidtree.hita.activities;

import android.net.Uri;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;
import android.view.View;

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



    @Override
    protected void stopTasks() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_manager);
        setWindowParams(true,true,false);
        initToolbar();


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

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
