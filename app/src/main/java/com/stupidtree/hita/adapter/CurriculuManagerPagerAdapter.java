package com.stupidtree.hita.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.stupidtree.hita.BaseFragment;

import java.util.List;

import static com.stupidtree.hita.HITAApplication.timeTableCore;


public class CurriculuManagerPagerAdapter extends FragmentPagerAdapter {
    List<BaseFragment> mBeans;
    public CurriculuManagerPagerAdapter(FragmentManager fm, List<BaseFragment> pagers) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mBeans = pagers;
    }

    @Override
    public BaseFragment getItem(int i) {
        return mBeans.get(i);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        String n = timeTableCore.getAllCurriculum().get(position).getName();
        if(n.indexOf("(")>0){
            return n.substring(0,n.indexOf("("));
        }
        else  return n;
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

}
