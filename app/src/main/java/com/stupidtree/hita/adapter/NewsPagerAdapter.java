package com.stupidtree.hita.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.List;

public class NewsPagerAdapter extends FragmentPagerAdapter {
    List<Fragment> mBeans;
    String[] titles;
    public NewsPagerAdapter(FragmentManager fm, List<Fragment> res,String[] titles) {
        super(fm,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.titles = titles;
        mBeans = res;
    }

    @Override
    public Fragment getItem(int i) {
        return mBeans.get(i);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
       // super.destroyItem(container, position, object);
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return mBeans.size();
    }
}
