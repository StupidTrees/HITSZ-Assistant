package com.stupidtree.hita.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.stupidtree.hita.fragments.timetable_manager.FragmentTimeTableSettings;
import com.stupidtree.hita.fragments.timetable_manager.FragmentSubjects;
import com.stupidtree.hita.fragments.timetable_manager.FragmentTeachers;

import java.util.List;

public class SubjectsManagerPagerAdapter extends BaseTabAdapter {
    private List<String> titles;

    public SubjectsManagerPagerAdapter(FragmentManager fm, List<String> titles) {
        super(fm, 3);
        this.titles = titles;
    }



    @Override
    protected Fragment initItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new FragmentSubjects();
                break;
            case 1:
                fragment = new FragmentTeachers();
                break;
            case 2:
                fragment = new FragmentTimeTableSettings();
        }
        return fragment;
    }



    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
//        super.destroyItem(container, position, object);
//        container.removeView(((Fragment)object).getView());
    }
//    @Override
//    public int getItemPosition(@NonNull Object object) {
//        return POSITION_NONE;
//    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }
}
