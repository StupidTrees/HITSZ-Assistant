package com.stupidtree.hita.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.List;

public class MainPagerAdapter extends FragmentPagerAdapter {
    List<Fragment> mBeans;
    String[] name;
    public MainPagerAdapter(FragmentManager fm,List mBeans,String[] name) {
        super(fm);
        this.mBeans = mBeans;
        this.name = name;
    }

    @Override
    public Fragment getItem(int i) {
        return mBeans.get(i);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return name[position];
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
       // super.destroyItem(container, position, object);
        //防止销毁
    }

    @Override
    public int getCount() {
        return mBeans.size();
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }
}
