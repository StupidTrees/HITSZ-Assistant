package com.stupidtree.hita.adapter;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.stupidtree.hita.fragments.main.FragmentNavigation;
import com.stupidtree.hita.fragments.main.FragmentTimeLine;

public class MainPagerAdapter extends BaseTabAdapter {
    private String[] name;

    public MainPagerAdapter(FragmentManager fm, String[] name) {
        super(fm, 2);
        this.name = name;
    }

    @Override
    protected Fragment initItem(int position) {
        switch (position) {
            case 0:
                return new FragmentNavigation();
            case 1:
                return new FragmentTimeLine();
        }
        return null;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return name[position];
    }

//    @Override
//    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
//       // super.destroyItem(container, position, object);
//        //防止销毁
//    }

}
