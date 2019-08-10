package com.stupidtree.hita.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;
import com.stupidtree.hita.fragments.FragmentFDY;
import com.stupidtree.hita.online.Infos;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;

public class ActivityYX_FDY extends BaseActivity {

    TabLayout tabs;
    ViewPager pager;
    List<FragmentFDY> fragments;
    List<JsonObject> pagerRes;
    pagerAdapter pagerAdapter;
    Toolbar toolbar;
    ImageView appbarBG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true,false,false);
        setContentView(R.layout.activity_yx_fdy);
        initPager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Refresh();
    }

    void initPager(){
        appbarBG = findViewById(R.id.appbarBG);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        fragments = new ArrayList<>();
        pagerRes = new ArrayList<>();
        pager = findViewById(R.id.pager);
        tabs = findViewById(R.id.tabs);
        pagerAdapter = new pagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        tabs.setupWithViewPager(pager);
    }

    void Refresh(){
        BmobQuery<Infos> bq = new BmobQuery<>();
        bq.getObject("EnPB777A", new QueryListener<Infos>() {
            @Override
            public void done(Infos infos, BmobException e) {
                pagerRes.clear();
                fragments.clear();
                for(JsonElement je:infos.getJsonArray()){
                    pagerRes.add(je.getAsJsonObject());
                    fragments.add(FragmentFDY.newInstance(je.getAsJsonObject()));
                }
                pagerAdapter.notifyDataSetChanged();
            }
        });
        Glide.with(this).load("https://bmob-cdn-26359.bmobpay.com/2019/08/08/39c4b4f240c73aa6808fdbf6d0789148.jpg")
                .into(appbarBG);
    }
    @Override
    protected void stopTasks() {

    }

    private class pagerAdapter extends FragmentPagerAdapter{

        public pagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return pagerRes.get(position).get("school").getAsString();
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }
}
