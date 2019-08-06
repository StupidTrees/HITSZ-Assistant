package com.stupidtree.hita.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import android.view.ViewGroup;


import com.stupidtree.hita.fragments.FragmentClassroomList;
import com.stupidtree.hita.fragments.FragmentDormitoryList;
import com.stupidtree.hita.fragments.FragmentFacilityList;
import com.stupidtree.hita.fragments.FragmentSceneryList;

import java.util.List;

public class CampusServicePagerAdapter extends FragmentPagerAdapter {
    List<Fragment> mBeans;

    public CampusServicePagerAdapter(FragmentManager fm, List<Fragment> fx) {
        super(fm);
        mBeans = fx;
    }

    @Override
    public Fragment getItem(int i) {
        return mBeans.get(i);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if(mBeans.get(position) instanceof FragmentSceneryList) return "景点";
        if(mBeans.get(position) instanceof FragmentClassroomList) return "教室";
        if(mBeans.get(position) instanceof FragmentFacilityList) return "设施";
        if(mBeans.get(position) instanceof FragmentDormitoryList) return "宿舍";
        return "地点";
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
