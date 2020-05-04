package com.stupidtree.hita.community;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.stupidtree.hita.R;
import com.stupidtree.hita.fragments.BaseFragment;

import java.util.HashMap;


public class FragmentTabsPostPage extends BaseFragment {

    CommunityRoot communityRoot;
    HashMap<String, FragmentPostsList.DataFetcher> fetcherMap;
    ViewPager pager;
    FragmentStatePagerAdapter pagerAdapter;
    TabLayout tabs;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof CommunityRoot) {
            communityRoot = (CommunityRoot) context;
        }
    }

    @Override
    protected void stopTasks() {

    }

    @Override
    public void Refresh() {
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_community_topics;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Refresh();
    }

    public boolean canShowFabNow() {
        return false;
    }

    void callEveryoneToRefresh(Intent intent) {
        for (Fragment fp : getChildFragmentManager().getFragments()) {
            if (fp instanceof FragmentPostsList) {
                ((FragmentPostsList) fp).respondRefreshRequest(intent);
            }
        }
    }


    interface CommunityRoot {
        void hideFab();

        void showFab();
    }


}
