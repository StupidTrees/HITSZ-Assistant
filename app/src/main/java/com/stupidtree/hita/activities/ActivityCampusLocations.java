package com.stupidtree.hita.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.CampusServicePagerAdapter;
import com.stupidtree.hita.fragments.FragmentCanteenList;
import com.stupidtree.hita.fragments.FragmentClassroomList;
import com.stupidtree.hita.fragments.FragmentDormitoryList;
import com.stupidtree.hita.fragments.FragmentFacilityList;
import com.stupidtree.hita.fragments.FragmentSceneryList;

import java.util.ArrayList;
import java.util.List;

public class ActivityCampusLocations extends BaseActivity {


    CampusServicePagerAdapter pagerAdapter;
    ViewPager viewPager;
    TabLayout tabLayout;
    List<Fragment> pagerRes;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campus_locations);
        setWindowParams(true,true,false);
        toolbar = findViewById(R.id.toolbar);
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
        initPager();
    }

    void initPager() {
        tabLayout = findViewById(R.id.tablayout);
        viewPager = findViewById(R.id.viewpager);
        tabLayout.setTabIndicatorFullWidth(false);
        tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#00000000"));
        pagerRes = new ArrayList<>();
        //pagerRes.add(new FragmentCanteenList());
        pagerRes.add(new FragmentSceneryList());
        pagerRes.add(new FragmentClassroomList());
        pagerRes.add(new FragmentFacilityList());
        pagerRes.add(new FragmentDormitoryList());
        pagerAdapter = new CampusServicePagerAdapter(this.getSupportFragmentManager(), pagerRes);
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }


}
