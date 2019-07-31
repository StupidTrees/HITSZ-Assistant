package com.stupidtree.hita.adapter;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

public class SubjectsManagerPagerAdapter extends FragmentPagerAdapter {
    List<Fragment> mBeans;
    List<String> titles;

    public SubjectsManagerPagerAdapter(FragmentManager fm, List<Fragment> pagers, List<String> titles) {
        super(fm);
        mBeans = pagers;
        this.titles = titles;
    }

    @Override
    public Fragment getItem(int i) {
        return mBeans.get(i);
    }

    @Override
    public int getCount() {
        return mBeans.size();
    }


    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }
}
