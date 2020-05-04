package com.stupidtree.hita.jw;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.stupidtree.hita.R;

import java.util.ArrayList;
import java.util.List;

public class FragmentJWTS_cjgl extends JWFragment {


    ViewPager pager;
    TabLayout tabs;
    List<JWFragment> fragments;

    public FragmentJWTS_cjgl() {
        // Required empty public constructor
    }


    public static FragmentJWTS_cjgl newInstance() {
        FragmentJWTS_cjgl fragment = new FragmentJWTS_cjgl();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_jwts_cjgl;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
    }



    void initViews(View v){
        pager = v.findViewById(R.id.cjgl_pager);
        tabs = v.findViewById(R.id.cjgl_tabs);
       fragments = new ArrayList<>();
        fragments.add(new FragmentJWTS_cjgl_grcj());
        pager.setAdapter(new pagerAdapter(getFragmentManager(),fragments,new String[]{"学习进度","个人成绩","学分绩"}));
        tabs.setupWithViewPager(pager);
    }





    @Override
    protected void stopTasks() {

    }

    @Override
    public void onResume() {
        Log.e("cjgl_refresh","will="+willRefreshOnResume);
        super.onResume();
    }

    @Override
    public int getTitle() {
        return R.string.jw_tabs_cj;
    }

    @Override
    public void Refresh() {
        for(int i=0;i<fragments.size();i++){
            if(i==pager.getCurrentItem()){
                JWFragment current = fragments.get(i);
                if(current.isResumed()) {
                    current.Refresh();
                    Log.e("refresh","method1");
                }
                else {
                    current.setWillRefreshOnResume(true);
                    Log.e("refresh","method1");
                }
            }else{
                fragments.get(i).setWillRefreshOnResume(true);
            }
        }
        //fragments.get(pager.getCurrentItem()).setWillRefreshOnResume(true);
    }


    private class pagerAdapter extends FragmentPagerAdapter{

        List<JWFragment> mBeans;
        String[] title;
        public pagerAdapter(FragmentManager fm,List<JWFragment> res,String[] title) {
            super(fm,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            this.title = title;
            mBeans = res;
        }

        @Override
        public Fragment getItem(int i) {
            return mBeans.get(i);
        }

        @Override
        public int getCount() {
            return mBeans.size();
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            //super.destroyItem(container, position, object);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return title[position];
        }
    }
}
