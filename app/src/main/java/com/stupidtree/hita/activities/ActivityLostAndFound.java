package com.stupidtree.hita.activities;

import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Toast;

import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;
import com.stupidtree.hita.fragments.popup.FragmentAddLAF;
import com.stupidtree.hita.fragments.FragmentLostAndFound;
import com.stupidtree.hita.online.HITAUser;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobUser;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.fragments.popup.FragmentAddLAF.FOUND;


public class ActivityLostAndFound extends BaseActivity implements FragmentLostAndFound.OnFragmentInteractionListener
,FragmentAddLAF.AttachedActivity{


    FloatingActionButton fab;
    Toolbar mToolbar;
    List<FragmentLostAndFound> fragments;
    ViewPager pager;
    TabLayout tabs;
    String[] titles;

    @Override
    protected void stopTasks() {

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true,true,false);
        setContentView(R.layout.activity_lostandfound);
        initPager();
        fab = findViewById(R.id.fab_post);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(BmobUser.getCurrentUser(HITAUser.class)==null){
                    Toast.makeText(HContext,"请先登录！",Toast.LENGTH_SHORT).show();
               }else FragmentAddLAF.newInstance(pager.getCurrentItem()==0?FragmentAddLAF.LOST:FOUND).show(getSupportFragmentManager(),"add_laf");

            }
        });
    }

    void initPager(){
        fragments = new ArrayList<>();
        fragments.add(FragmentLostAndFound.newInstance("lost"));
        fragments.add(FragmentLostAndFound.newInstance("found"));
        titles = getResources().getStringArray(R.array.laf_tabs);
        pager = findViewById(R.id.laf_pager);
        tabs = findViewById(R.id.laf_tabs);
        pager.setAdapter(new lafPagerAdapter(getSupportFragmentManager()));
        tabs.setupWithViewPager(pager);
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onFragmentCalledRefresh(int which) {
        fragments.get(which).Refresh();
    }


    class lafPagerAdapter extends FragmentPagerAdapter{

        public lafPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            return fragments.get(i);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }
}
