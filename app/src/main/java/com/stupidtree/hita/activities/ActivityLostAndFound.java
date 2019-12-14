package com.stupidtree.hita.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;
import com.stupidtree.hita.diy.PickInfoDialog;
import com.stupidtree.hita.fragments.FragmentAddLAF;
import com.stupidtree.hita.fragments.FragmentLostAndFound;
import com.stupidtree.hita.online.Location;
import com.stupidtree.hita.online.LostAndFound;
import com.stupidtree.hita.online.HITAUser;
import com.yuyh.library.imgsel.ISNav;
import com.yuyh.library.imgsel.common.ImageLoader;
import com.yuyh.library.imgsel.config.ISListConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UploadFileListener;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.fragments.FragmentAddLAF.FOUND;


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
        mToolbar.setTitle("失物招领");
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
        titles = new String[]{"寻物启事","失物招领"};
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
        Log.e("刷新：", String.valueOf(which));
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
