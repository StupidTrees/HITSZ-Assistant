package com.stupidtree.hita.fragments.popup;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.stupidtree.hita.R;
import com.stupidtree.hita.fragments.events.FragmentEventItem;
import com.stupidtree.hita.timetable.packable.EventItem;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("ValidFragment")
public class FragmentEvent extends FragmentRadiusPopup implements FragmentEventItem.PopupFragment {

    private int currentPosition;
    private List<EventItem> events;
    private List<FragmentEventItem> fragments;

    public FragmentEvent() {

    }

    public static FragmentEvent newInstance(ArrayList<EventItem> events) {
        Bundle d = new Bundle();
        d.putSerializable("events", events);
        FragmentEvent fe = new FragmentEvent();
        fe.setArguments(d);
        return fe;
    }


    @Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        super.onAttachFragment(childFragment);
        if (childFragment instanceof FragmentEventItem) {
            FragmentEventItem fe = (FragmentEventItem) childFragment;
            fe.setRoot(this);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            events = (List<EventItem>) getArguments().getSerializable("events");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = View.inflate(requireContext(), R.layout.fragment_event_popup, null);
        initViews(view);
        return view;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }


    void initViews(View v) {
        fragments = new ArrayList<>();
        FrameLayout layout = v.findViewById(R.id.layout);
        ViewPager pager = v.findViewById(R.id.pager);
        TabLayout tabs = v.findViewById(R.id.tabs);
        if (events.size() <= 1) {
            pager.setVisibility(View.GONE);
            tabs.setVisibility(View.GONE);
            layout.setVisibility(View.VISIBLE);
            EventItem ei = events.get(0);
            getChildFragmentManager().beginTransaction()
                    .add(R.id.layout, FragmentEventItem.newInstance(ei), "f").commit();

        } else {
            layout.setVisibility(View.VISIBLE);
            pager.setVisibility(View.GONE);
            tabs.setVisibility(View.VISIBLE);
            // layout.setVisibility(View.GONE);
            tabs.removeAllTabs();
            FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
            for (EventItem ei : events) {
                fragments.add(FragmentEventItem.newInstance(ei));
                tabs.addTab(tabs.newTab().setText(ei.getMainName()));
            }
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.layout, fragments.get(0), "f").commit();
            currentPosition = 0;
            FragmentEventItem currentFragment = fragments.get(0);
            // fragmentTransaction.show(fragments.get(0)).commit();
            tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    if (currentPosition < tab.getPosition()) {
                        getChildFragmentManager().beginTransaction()
                                .setCustomAnimations(R.anim.fragment_slide_from_right, R.anim.fragment_slide_to_left)
                                .replace(R.id.layout, fragments.get(tab.getPosition())).commitAllowingStateLoss();

                    } else {
                        getChildFragmentManager().beginTransaction()
                                .setCustomAnimations(R.anim.fragment_slide_from_left, R.anim.fragment_slide_to_right)
                                .replace(R.id.layout, fragments.get(tab.getPosition())).commitAllowingStateLoss();

                    }
                    currentPosition = tab.getPosition();
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });
//            pager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager(),FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
//                @Override
//                public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
//                    //super.destroyItem(container, position, object);
//                }
//
//                @Nullable
//                @Override
//                public CharSequence getPageTitle(int position) {
//                    return fragments.get(position).getEventItem().getMainName();
//                }
//
//                @NonNull
//                @Override
//                public Fragment getItem(int position) {
//                    return fragments.get(position);
//                }
//
//                @Override
//                public int getCount() {
//                    return fragments.size();
//                }
//            });
//            tabs.setupWithViewPager(pager);
//            if(events.size()<=1){
//                tabs.setVisibility(View.GONE);
//            }
        }


    }


    @Override
    public void callDismiss() {
        try {
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
