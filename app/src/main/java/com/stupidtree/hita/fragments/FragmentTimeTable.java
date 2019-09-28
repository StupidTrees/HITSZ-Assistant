package com.stupidtree.hita.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBarDrawerToggle;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivityCurriculumManager;
import com.stupidtree.hita.activities.ActivitySetting;
import com.stupidtree.hita.adapter.TimeTablePagerAdapter;

import java.util.Calendar;



import static com.stupidtree.hita.HITAApplication.DATA_STATE_HEALTHY;
import static com.stupidtree.hita.HITAApplication.DATA_STATE_NONE_CURRICULUM;
import static com.stupidtree.hita.HITAApplication.DATA_STATE_NULL;
import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.allCurriculum;
import static com.stupidtree.hita.HITAApplication.correctData;
import static com.stupidtree.hita.HITAApplication.getDataState;
import static com.stupidtree.hita.HITAApplication.isThisTerm;
import static com.stupidtree.hita.HITAApplication.now;
import static com.stupidtree.hita.HITAApplication.thisCurriculumIndex;
import static com.stupidtree.hita.HITAApplication.thisWeekOfTerm;
import static com.stupidtree.hita.HITAApplication.timeWatcher;

public class FragmentTimeTable extends BaseFragment implements FragmentTimeTablePage.OnFragmentInteractionListener {
    /*标志类常量*/
    final int FROM_SEEKBAR = 0;
    final int FROM_SPINNER_Curriculum = 1;
    final int FROM_SPINNER_TIMETABLE = 2;
    final int FROM_DIS_HIDE = 3;
    final int FROM_INIT = 4;


    /*重要数据变量*/

    int pageWeekOfTerm; //代表当前页面显示的是哪一周
    boolean hasInit = false;

    public AppBarLayout mAppBarLayout;
    Toolbar mToolbar;
    ActionBarDrawerToggle mActionBarDrawerToggle;
    LinearLayout invalidLayout;
    Button invalidJump;
    TextView toolbar_title;
    TabLayout tabs;
    ViewPager viewPager;
    TimeTablePagerAdapter pagerAdapter;
    FloatingActionButton fab;
    RefreshTask pageTask;




    /*初始化_获取所有控件对象*/
    void initAllViews(View v) {
        fab = v.findViewById(R.id.fab);
        toolbar_title = v.findViewById(R.id.toolbar_title);
        mAppBarLayout = v.findViewById(R.id.app_bar);
        invalidLayout = v.findViewById(R.id.tt_invalidview);
        invalidJump = v.findViewById(R.id.tt_invalid_jump);

        invalidJump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(FragmentTimeTable.this.getActivity(), ActivityCurriculumManager.class);
                FragmentTimeTable.this.getActivity().startActivity(i);
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pageWeekOfTerm != thisWeekOfTerm && isThisTerm) {
                    pageWeekOfTerm = thisWeekOfTerm;
                    viewPager.setCurrentItem(thisWeekOfTerm-1);
                }
            }
        });
        initViewPager(v);
    }

    void initViewPager(View v) {
        tabs = v.findViewById(R.id.timetable_tabs);
        viewPager = v.findViewById(R.id.timetable_viewpager);
        //ContextCompat.getColor(getContext(),R.color.material_primary_text)
        tabs.setTabTextColors(Color.parseColor("#55000000"),((BaseActivity)getActivity()).getColorPrimary());
        if (getDataState() == DATA_STATE_HEALTHY)
            pagerAdapter = new TimeTablePagerAdapter(this.getActivity().getSupportFragmentManager(), allCurriculum.get(thisCurriculumIndex).totalWeeks);
        else pagerAdapter = new TimeTablePagerAdapter(this.getActivity().getSupportFragmentManager(), 0);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                pageWeekOfTerm = i + 1;
                toolbar_title.setText("第" + pageWeekOfTerm + "周");
                if (pageWeekOfTerm == thisWeekOfTerm) {
                    fab.hide();
                    //toolbar_title.setTextColor(ContextCompat.getColor(FragmentTimeTable.this.getActivity(), R.color.theme1_colorPrimaryDark));
                } else if(isThisTerm){
                    fab.show();
                    //toolbar_title.setTextColor(ContextCompat.getColor(FragmentTimeTable.this.getActivity(), R.color.material_primary_text));
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        tabs.setupWithViewPager(viewPager);

    }


    private void initToolBarAndDrawer(View v) {
        mToolbar = v.findViewById(R.id.main_tool_bar);
        mToolbar.inflateMenu(R.menu.toolbar_time_table);
        mToolbar.setTitle("");
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                boolean isChecked = !menuItem.isChecked();
                switch (id) {

                    case R.id.action_settings:
                        Intent x = new Intent(FragmentTimeTable.this.getActivity(), ActivitySetting.class);
                        startActivity(x);
                        break;
//                    case R.id.action_switch_timetable:
//                        menuItem.setChecked(isChecked);
//                        PreferenceManager.getDefaultSharedPreferences(FragmentTimeTable.this.getActivity()).edit().putBoolean("timetable_curriculumonly",isChecked).apply();
//                        //Toast.makeText(FragmentTimeTable.this.getActivity(),"重新进入本页面生效",Toast.LENGTH_SHORT).show();
//                        //reFreshViewPager();
//                        pagerAdapter.notifyAllFragments();
//                        break;

                    case R.id.action_whole_day:
                        menuItem.setChecked(isChecked);
                        PreferenceManager.getDefaultSharedPreferences(FragmentTimeTable.this.getActivity()).edit().putBoolean("timetable_wholeday",isChecked).apply();
                        //Toast.makeText(FragmentTimeTable.this.getActivity(),"重新进入本页面生效",Toast.LENGTH_SHORT).show();
                        pagerAdapter.notifyAllFragments();
                        // Refresh(FROM_INIT);
                        break;

                }
                return true;
            }
        });
//        setDrawerLeftEdgeSize(FragmentTimeTable.this.getActivity(),mDrawerLayout,0,false);


    }

    /*刷新课表视图函数*/
    public void Refresh(int from) {
        if(pageTask!=null&&!pageTask.isCancelled()) pageTask.cancel(true);
        pageTask = new RefreshTask(from);
        pageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    void reFreshViewPager() {
        //pagerAdapter.notifyAllFragments();
        viewPager.setCurrentItem(pageWeekOfTerm - 1);
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    protected void stopTasks() {
        if(pageTask!=null&&!pageTask.isCancelled()) pageTask.cancel(true);
    }

    @Override
    public void Refresh() {

    }


    @SuppressLint("StaticFieldLeak")
    class RefreshTask extends AsyncTask<String, Integer, Integer> {

        int from;

        RefreshTask(int from) {
            this.from = from;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(String... strings) {
            if (!hasInit || getDataState() == DATA_STATE_NULL) return -1;
            if (getDataState() == DATA_STATE_NONE_CURRICULUM) {
                thisWeekOfTerm = -1;
                return DATA_STATE_NONE_CURRICULUM;
            }
            /*刷新必备代码--刷新数据*/
            now.setTimeInMillis(System.currentTimeMillis());
            correctData();
            isThisTerm = allCurriculum.get(thisCurriculumIndex).Within(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH));
            if (from == FROM_INIT || from == FROM_SPINNER_Curriculum) {
                try {
                    thisWeekOfTerm = allCurriculum.get(thisCurriculumIndex).getWeekOfTerm(now);
                    pageWeekOfTerm = isThisTerm ? thisWeekOfTerm : 1;
                } catch (Exception e) {
                    thisWeekOfTerm = -1;
                    pageWeekOfTerm = 1;
                }
            }
            if (from == FROM_SPINNER_Curriculum || from == FROM_SPINNER_TIMETABLE || from == FROM_INIT) {
                timeWatcher.refreshProgress(true,true);
            }
            return 99;
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(Integer i) {
            if (i == -1) return;
            if (i == DATA_STATE_NONE_CURRICULUM) {
                invalidLayout.setVisibility(View.VISIBLE);
                viewPager.setVisibility(View.GONE);
                thisWeekOfTerm = -1;
                return;
            } else {
                invalidLayout.setVisibility(View.GONE);
                viewPager.setVisibility(View.VISIBLE);
            }

            reFreshViewPager();
            super.onPostExecute(i);
        }

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_timetable,container,false);
        initAllViews(v);
        if (allCurriculum.size() <= 0) {
            pageWeekOfTerm = 1;
        } else {
            pageWeekOfTerm = allCurriculum.get(thisCurriculumIndex).getWeekOfTerm(now);
            if (pageWeekOfTerm < 0) {
                Toast.makeText(HContext, "这个学期还没有开始哟", Toast.LENGTH_SHORT).show();
                isThisTerm = false;
                pageWeekOfTerm = 1;
            }
        }
        initAllViews(v);
        if (thisWeekOfTerm > 0) pageWeekOfTerm = thisWeekOfTerm;
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        initToolBarAndDrawer(v);
        hasInit = true;
        if(!isThisTerm) fab.hide();
        Refresh(FROM_INIT);
        return v;
    }



    @Override
    public void onHiddenChanged(boolean hidden) {
        if(!hidden) Refresh(FROM_INIT);
    }

}
