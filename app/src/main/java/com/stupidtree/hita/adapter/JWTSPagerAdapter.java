package com.stupidtree.hita.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.stupidtree.hita.BaseFragment;

import java.util.List;

public class JWTSPagerAdapter extends FragmentPagerAdapter {
    List<BaseFragment> mBeans;
    String[] titles;
    public JWTSPagerAdapter(FragmentManager fm, List<BaseFragment> res, String[] titles) {
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
