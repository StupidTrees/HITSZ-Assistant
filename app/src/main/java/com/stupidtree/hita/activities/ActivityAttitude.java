package com.stupidtree.hita.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.AttitudeListAdapter;
import com.stupidtree.hita.diy.WrapContentLinearLayoutManager;
import com.stupidtree.hita.fragments.attitude.FragmentAttitude;
import com.stupidtree.hita.fragments.popup.FragmentAddAttitude;
import com.stupidtree.hita.online.Attitude;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class ActivityAttitude extends BaseActivity implements FragmentAddAttitude.AttachedActivity{


    FloatingActionButton fab;
    Toolbar toolbar;
    ViewPager pager;
    TabLayout tabs;
    List<FragmentAttitude> fragments;
    @Override
    protected void stopTasks() {

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attitude);
        setWindowParams(true,true,false);
        initToolbar();
        initFragments();
        initViews();

    }


    @Override
    protected void onResume() {
        super.onResume();
//        if(refreshFlags[pager.getCurrentItem()]) fragments.get(pager.getCurrentItem()).Refresh();
    }

    void initToolbar(){

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.label_activity_attitude));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    void initFragments(){
        fragments = new ArrayList<>();
        fragments.add(new FragmentAttitude(getString(R.string.attitude_tabs_latest_created), new FragmentAttitude.DataFetcher() {
                    @Override
                    public List<Attitude> fetch() throws Exception {
                        BmobQuery<Attitude> bq = new BmobQuery<>();
//                        Calendar c = Calendar.getInstance();
//                        c.add(Calendar.DATE,-14);
//                        bq.addWhereGreaterThanOrEqualTo("createdAt",new BmobDate(c.getTime()));
                        List<Attitude> res = bq.setLimit(50).order("-createdAt").findObjectsSync(Attitude.class);
                        if(res==null) throw new Exception();
                        return res;
                    }
                }));
        fragments.add(new FragmentAttitude(getString(R.string.attitude_tabs_latest_updated), new FragmentAttitude.DataFetcher() {
            @Override
            public List<Attitude> fetch() throws Exception {
                BmobQuery<Attitude> bq = new BmobQuery<>();
//                        Calendar c = Calendar.getInstance();
//                        c.add(Calendar.DATE,-14);
//                        bq.addWhereGreaterThanOrEqualTo("createdAt",new BmobDate(c.getTime()));
                List<Attitude> res = bq.setLimit(50).order("-updatedAt").findObjectsSync(Attitude.class);
                if(res==null) throw new Exception();
                return res;
            }
        }));
        fragments.add(new FragmentAttitude(getString(R.string.attitude_tabs_weekly_top), new FragmentAttitude.DataFetcher() {
            @Override
            public List<Attitude> fetch() throws Exception {
                BmobQuery<Attitude> bq = new BmobQuery<>();
                Calendar c = Calendar.getInstance();
                int dow = c.get(Calendar.DAY_OF_WEEK)==1?6:c.get(Calendar.DAY_OF_WEEK)-2
                        ;
                //Log.e("dow", String.valueOf(dow));
                c.add(Calendar.DATE,-dow);
                c.set(Calendar.HOUR_OF_DAY,0);
                c.set(Calendar.MINUTE,0);
                bq.addWhereGreaterThanOrEqualTo("createdAt",new BmobDate(c.getTime()));
                List<Attitude> res = bq.findObjectsSync(Attitude.class);
                Collections.sort(res, new Comparator<Attitude>() {
                    @Override
                    public int compare(Attitude attitude, Attitude t1) {
                        int x = attitude.getUp()+attitude.getDown();
                        int y = t1.getUp() + t1.getDown();
                        return y-x;
                    }
                });
                if(res==null) throw new Exception();
                return res;
            }
        }));
        fragments.add(new FragmentAttitude(getString(R.string.attitude_tabs_history_top), new FragmentAttitude.DataFetcher() {
            @Override
            public List<Attitude> fetch() throws Exception {
                BmobQuery<Attitude> bq = new BmobQuery<>();
               // bq.addWhereGreaterThanOrEqualTo("up",10);
//                bq.addWhereGreaterThanOrEqualTo("down",30);
                List<Attitude> res = bq.setLimit(50).findObjectsSync(Attitude.class);
                Collections.sort(res, new Comparator<Attitude>() {
                    @Override
                    public int compare(Attitude attitude, Attitude t1) {
                        int x = attitude.getUp()+attitude.getDown();
                        int y = t1.getUp() + t1.getDown();
                        return y-x;
                    }
                });
                if(res==null) throw new Exception();
                return res;
            }
        }));
        for(FragmentAttitude fa:fragments) fa.setShouldRefresh(true);
    }
    void initViews(){

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentAddAttitude.newInstance().show(getSupportFragmentManager(),"add_attitude");
            }
        });
        pager = findViewById(R.id.pager);
        tabs = findViewById(R.id.tabs);
        pager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager(),FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }

            @Override
            public int getCount() {
                return fragments.size();
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                //super.destroyItem(container, position, object);
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return fragments.get(position).getTitle();
            }
        });
        tabs.setupWithViewPager(pager);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        pager.setCurrentItem(0);
    }


    @Override
    public void refreshAll() {
        for(FragmentAttitude fa:fragments) fa.setShouldRefresh(true);
        fragments.get(pager.getCurrentItem()).Refresh();
    }

    @Override
    public void refreshOthers() {
        for(FragmentAttitude fa:fragments) fa.setShouldRefresh(true);
    }
}
