package com.stupidtree.hita.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;


import com.google.android.material.tabs.TabLayout;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.CurriculuManagerPagerAdapter;
import com.stupidtree.hita.timetable.Curriculum;
import com.stupidtree.hita.fragments.FragmentCurriculum;
import com.stupidtree.hita.util.ActivityUtils;

import java.util.ArrayList;
import java.util.List;


import static com.stupidtree.hita.activities.ActivityMain.saveData;
import static com.stupidtree.hita.HITAApplication.*;

public class ActivityCurriculumManager extends BaseActivity implements FragmentCurriculum.OnFragmentInteractionListener {

    private static final int CHOOSE_FILE_CODE = 0;
    FloatingActionButton fab;
    Toolbar mToolbar;
    LinearLayout noneLayout;
    ViewPager pager;
    CurriculuManagerPagerAdapter curriculuManagerPagerAdapter;
    List<BaseFragment> pagerRes;
    List<Curriculum> pagerData;
    TabLayout tabs;

    @Override
    protected void stopTasks() {

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true, true, false);
        setContentView(R.layout.activity_curriculum_manager);
        initToolbar();
        initViews();
        initPager();
        //Refresh();
    }

    void initToolbar() {

        Toolbar toolbar = findViewById(R.id.main_tool_bar);
        toolbar.setTitle("选择当前课表");
        toolbar.inflateMenu(R.menu.toolbar_curriculum_manager);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_import_from_jwts) {
                    ActivityUtils.startJWTSActivity(ActivityCurriculumManager.this);
                }
                return true;
            }
        });
    }

    void initPager() {
        tabs = findViewById(R.id.tabs);
        pager = findViewById(R.id.pager);
        pagerRes = new ArrayList<>();
        pagerData = new ArrayList<>();
        curriculuManagerPagerAdapter = new CurriculuManagerPagerAdapter(getSupportFragmentManager(), pagerRes);
        pager.setAdapter(curriculuManagerPagerAdapter);
        tabs.setupWithViewPager(pager);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(!timeTableCore.isDataAvailable()){
                    fab.hide();
                    return;
                }
                if (!pagerData.get(position).getCurriculumCode().equals(timeTableCore.getCurrentCurriculum().getCurriculumCode())) fab.show();
                else fab.hide();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    void initViews() {
        noneLayout = findViewById(R.id.none_layout);
        mToolbar = findViewById(R.id.main_tool_bar);
        fab = findViewById(R.id.fab_done);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Curriculum chosenItem =  pagerData.get(pager.getCurrentItem());
                AlertDialog ad = new AlertDialog.Builder(ActivityCurriculumManager.this).
                        setTitle("提示")
                        .setMessage("确定更换当前课表为\n" +chosenItem.getName() + "\n吗？").
                                setNegativeButton("取消", null).
                                setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                timeTableCore.changeCurrentCurriculum(chosenItem.getCurriculumCode());
                                                finish();
                                            }
                                        }).start();
                                    }
                                }).create();
                ad.show();


            }
        });


    }


    public void Refresh() {
        new RefreshPageTask().execute();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_curriculum_manager, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public void onFragmentInteraction() {
        Refresh();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    protected void onResume() {
        super.onResume();
        Refresh();
    }


    @SuppressLint("StaticFieldLeak")
    class RefreshPageTask extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] objects) {
            pagerData.clear();
            for(Curriculum c:timeTableCore.getAllCurriculum()){
                if(c!=null)pagerData.add(c);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            pagerRes.clear();
            // curriculuManagerPagerAdapter.notifyDataSetChanged();
            for (Curriculum c : pagerData) {
                pagerRes.add(FragmentCurriculum.newInstance(c));
            }

            curriculuManagerPagerAdapter.notifyDataSetChanged();
            if (pagerRes.size() > 0) {
                pager.setVisibility(View.VISIBLE);
                noneLayout.setVisibility(View.GONE);
            } else {
                pager.setVisibility(View.GONE);
                noneLayout.setVisibility(View.VISIBLE);
                fab.hide();
            }
            int curIndex = 0;
            for(curIndex=0;curIndex<pagerData.size();curIndex++){
                if(pagerData.get(curIndex).getCurriculumCode().equals(timeTableCore.getCurrentCurriculum().getCurriculumCode())){
                    break;
                }
            }
            pager.setCurrentItem(curIndex);
            fab.hide();
        }
    }


}




