package com.stupidtree.hita.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;
import com.stupidtree.hita.fragments.FragmentCanteenList;
import com.stupidtree.hita.fragments.FragmentSceneryList;
import java.util.ArrayList;
import java.util.List;

public class ActivityRankBoard extends BaseActivity {


    RankBoardPagerAdapter pagerAdapter;
    ViewPager viewPager;
    TabLayout tabLayout;
    List<Fragment> pagerRes;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rank_board);
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
        pagerRes.add(new FragmentCanteenList());
        //pagerRes.add(new FragmentSceneryList());
        pagerAdapter = new RankBoardPagerAdapter(this.getSupportFragmentManager(), pagerRes);
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }
    

    class RankBoardPagerAdapter extends FragmentPagerAdapter {
        List<Fragment> mBeans;

        public RankBoardPagerAdapter(FragmentManager fm, List<Fragment> fx) {
            super(fm);
            mBeans = fx;
        }

        @Override
        public Fragment getItem(int i) {
            return mBeans.get(i);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            if(mBeans.get(position) instanceof FragmentCanteenList) return "食堂排行";
            return "";
        }

        @Override
        public int getCount() {
            return mBeans.size();
        }
    }



}
