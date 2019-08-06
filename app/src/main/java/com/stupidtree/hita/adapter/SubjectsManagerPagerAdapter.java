package com.stupidtree.hita.adapter;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

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
