package com.stupidtree.hita.adapter;

import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.stupidtree.hita.BaseFragment;

import java.util.List;

public class SubjectsManagerPagerAdapter extends FragmentPagerAdapter {
    List<BaseFragment> mBeans;
    List<String> titles;

    public SubjectsManagerPagerAdapter(FragmentManager fm, List<BaseFragment> pagers, List<String> titles) {
        super(fm);
        mBeans = pagers;
        this.titles = titles;
    }

    @Override
    public BaseFragment getItem(int i) {
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
    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }
}
