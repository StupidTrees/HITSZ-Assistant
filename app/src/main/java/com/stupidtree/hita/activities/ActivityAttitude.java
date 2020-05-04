package com.stupidtree.hita.activities;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.BaseTabAdapter;
import com.stupidtree.hita.fragments.attitude.FragmentAttitude;
import com.stupidtree.hita.fragments.popup.FragmentAddAttitude;
import com.stupidtree.hita.online.Attitude;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobDate;

public class ActivityAttitude extends BaseActivity implements FragmentAddAttitude.AttachedActivity {


    FloatingActionButton fab;
    Toolbar toolbar;
    ViewPager pager;
    TabLayout tabs;

    @Override
    protected void stopTasks() {

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attitude);
        setWindowParams(true, true, false);
        initToolbar();
        initViews();

    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    void initToolbar() {

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

    void initViews() {

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentAddAttitude.newInstance().show(getSupportFragmentManager(), "add_attitude");
            }
        });
        pager = findViewById(R.id.pager);
        tabs = findViewById(R.id.tabs);
        pager.setAdapter(new BaseTabAdapter(getSupportFragmentManager(), 4) {
            int[] titles = new int[]{
                    R.string.attitude_tabs_latest_created,
                    R.string.attitude_tabs_latest_updated,
                    R.string.attitude_tabs_weekly_top,
                    R.string.attitude_tabs_history_top
            };

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                //super.destroyItem(container, position, object);
                mFragments[position] = null;
            }

            @Override
            protected Fragment initItem(int position) {
                switch (position) {
                    case 0:
                        return new FragmentAttitude(new FragmentAttitude.DataFetcher() {
                            @Override
                            public List<Attitude> fetch() throws Exception {
                                BmobQuery<Attitude> bq = new BmobQuery<>();
                                List<Attitude> res = bq.setLimit(50).order("-createdAt").findObjectsSync(Attitude.class);
                                if (res == null) throw new Exception();
                                return res;
                            }
                        });
                    case 1:
                        return new FragmentAttitude(new FragmentAttitude.DataFetcher() {
                            @Override
                            public List<Attitude> fetch() throws Exception {
                                BmobQuery<Attitude> bq = new BmobQuery<>();
                                List<Attitude> res = bq.setLimit(50).order("-updatedAt").findObjectsSync(Attitude.class);
                                if (res == null) throw new Exception();
                                return res;
                            }
                        });
                    case 2:
                        return new FragmentAttitude(new FragmentAttitude.DataFetcher() {
                            @Override
                            public List<Attitude> fetch() {
                                BmobQuery<Attitude> bq = new BmobQuery<>();
                                Calendar c = Calendar.getInstance();
                                int dow = c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ? 6 : c.get(Calendar.DAY_OF_WEEK) - 2;
                                c.add(Calendar.DATE, -dow);
                                c.set(Calendar.HOUR_OF_DAY, 0);
                                c.set(Calendar.MINUTE, 0);
                                bq.addWhereGreaterThanOrEqualTo("createdAt", new BmobDate(c.getTime()));
                                List<Attitude> res = bq.findObjectsSync(Attitude.class);
                                Collections.sort(res, new Comparator<Attitude>() {
                                    @Override
                                    public int compare(Attitude attitude, Attitude t1) {
                                        int x = attitude.getUp() + attitude.getDown();
                                        int y = t1.getUp() + t1.getDown();
                                        return y - x;
                                    }
                                });
                                return res;
                            }
                        });
                    default:
                        return new FragmentAttitude(new FragmentAttitude.DataFetcher() {
                            @Override
                            public List<Attitude> fetch() {
                                BmobQuery<Attitude> bq = new BmobQuery<>();
                                List<Attitude> res = bq.setLimit(50).findObjectsSync(Attitude.class);
                                Collections.sort(res, new Comparator<Attitude>() {
                                    @Override
                                    public int compare(Attitude attitude, Attitude t1) {
                                        int x = attitude.getUp() + attitude.getDown();
                                        int y = t1.getUp() + t1.getDown();
                                        return y - x;
                                    }
                                });
                                return res;
                            }
                        });
                }
            }

            @NonNull
            @Override
            public CharSequence getPageTitle(int position) {
                return getString(titles[position]);
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
        for (Fragment f : getSupportFragmentManager().getFragments()) {
            if (f instanceof FragmentAttitude) {
                if (f.isResumed()) {
                    ((FragmentAttitude) f).Refresh();
                } else {
                    ((FragmentAttitude) f).setShouldRefresh(true);
                }
            }
        }
    }

    @Override
    public void refreshOthers() {
        for (Fragment f : getSupportFragmentManager().getFragments()) {
            if (f instanceof FragmentAttitude) {
                if (!f.isResumed()) ((FragmentAttitude) f).setShouldRefresh(true);
            }
        }
    }


    @Override
    public void notifyItem(String objectId) {
        for (Fragment f : getSupportFragmentManager().getFragments()) {
            if (f instanceof FragmentAttitude) {
                ((FragmentAttitude) f).notifySpecificAttitudeChanged(objectId);
            }
        }
    }
}
