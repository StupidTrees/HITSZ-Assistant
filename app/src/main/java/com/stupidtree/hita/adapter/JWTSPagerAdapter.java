package com.stupidtree.hita.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.List;

public class JWTSPagerAdapter extends FragmentPagerAdapter {
    List<Fragment> mBeans;
    String[] titles;
    public JWTSPagerAdapter(FragmentManager fm,List<Fragment> res,String[] titles) {
        super(fm);
        mBeans = res;
        this.titles = titles;
    }

    @Override
    public Fragment getItem(int i) {
        return mBeans.get(i);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        //super.destroyItem(container, position, object);
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

    @Override
    public int getCount() {
        return mBeans.size();
    }
}
