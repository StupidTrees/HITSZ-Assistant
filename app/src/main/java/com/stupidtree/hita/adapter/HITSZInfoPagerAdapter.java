package com.stupidtree.hita.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.stupidtree.hita.fragments.FragmentNewsBulletin;
import com.stupidtree.hita.fragments.FragmentNewsIPNews;
import com.stupidtree.hita.fragments.FragmentNewsLecture;

import java.util.List;

public class HITSZInfoPagerAdapter extends FragmentPagerAdapter {
    List<Fragment> mBeans;
    public HITSZInfoPagerAdapter(FragmentManager fm,List<Fragment> res) {
        super(fm);
        mBeans = res;
    }

    @Override
    public Fragment getItem(int i) {
        return mBeans.get(i);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        Fragment f = mBeans.get(position);
        if(f instanceof FragmentNewsBulletin) return "通知公告";
        else if(f instanceof FragmentNewsLecture) return "讲座信息";
        else if(f instanceof FragmentNewsIPNews) return "校区要闻";
        else return "社区";
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
