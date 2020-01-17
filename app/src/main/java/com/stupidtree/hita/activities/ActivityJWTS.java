package com.stupidtree.hita.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.JWTSPagerAdapter;
import com.stupidtree.hita.jw.FragmentJWTS_cjgl;
import com.stupidtree.hita.jw.FragmentJWTS_cjgl_grcj;
import com.stupidtree.hita.jw.FragmentJWTS_grkb;
import com.stupidtree.hita.jw.JWException;
import com.stupidtree.hita.jw.JWFragment;

import java.util.ArrayList;
import java.util.List;


import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.jwCore;

public class ActivityJWTS extends BaseActivity implements FragmentJWTS_grkb.OnFragmentInteractionListener
//        , FragmentJWTS_xsxk.OnFragmentInteractionListener, FragmentJWTS_ksxx.OnFragmentInteractionListener,
        , FragmentJWTS_cjgl_grcj.OnFragmentInteractionListener//, FragmentJWTS_pyfa.OnFragmentInteractionListener, FragmentJWTS_pyfa_pyjhcx.OnFragmentInteractionListener, FragmentJWTS_pyfa_zxjxjh.OnFragmentInteractionListener,
//        FragmentJWTS_cjgl_xxjd.OnFragmentInteractionListener,
        ,FragmentJWTS_cjgl.OnFragmentInteractionListener//, FragmentJWTS_cjgl_xfj.OnFragmentInteractionListener
{
    public static final String EXTRA_CIRCULAR_REVEAL_X = "EXTRA_CIRCULAR_REVEAL_X";
    public static final String EXTRA_CIRCULAR_REVEAL_Y = "EXTRA_CIRCULAR_REVEAL_Y";
    ViewPager pager;
    JWTSPagerAdapter pagerAdapter;
    List<JWFragment> fragments;
    TabLayout tabs;
    FloatingActionButton fab;
    CoordinatorLayout rootLayout;
    SwipeRefreshLayout refresh;


    @Override
    protected void stopTasks() {

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true, true, false);
        setContentView(R.layout.activity_jwts);

        initViews();
        initToolbar();
        initPager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        new checkLoginTask().executeOnExecutor(
                HITAApplication.TPE);
        fragments.get(pager.getCurrentItem()).Refresh(
                new JWFragment.OnRefreshStartListener() {
                    @Override
                    public void OnStart() {
                        refresh.setRefreshing(true);
                    }
                },
                new JWFragment.OnRefreshFinishListener() {
                    @Override
                    public void OnFinish() {
                        refresh.setRefreshing(false);
                    }
                }
        );
    }


    void initViews(){
        rootLayout = findViewById(R.id.jwts_root);
        fab = findViewById(R.id.fab);
        refresh = findViewById(R.id.refresh);
        refresh.setColorSchemeColors(getColorPrimary(),getColorAccent(),getColorFade());
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fragments.get(pager.getCurrentItem()).Refresh(
                        new JWFragment.OnRefreshStartListener() {
                            @Override
                            public void OnStart() {
                                refresh.setRefreshing(true);
                            }
                        },
                        new JWFragment.OnRefreshFinishListener() {
                            @Override
                            public void OnFinish() {
                                refresh.setRefreshing(false);
                            }
                        }
                );
            }
        });
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
    }
    void initToolbar() {

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.inflateMenu(R.menu.toolbar_jwts);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_logout) {
                    AlertDialog ad = new AlertDialog.Builder(ActivityJWTS.this).create();
                    ad.setMessage("下次进入需要重新登录，是否退出？");
                    ad.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            jwCore.logOut();
                            Intent i = new Intent(ActivityJWTS.this, ActivityLoginJWTS.class);
                            ActivityJWTS.this.startActivity(i);
                            finish();
                        }
                    });
                    ad.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    ad.show();

                }
                return true;
            }
        });

    }

    void initPager() {
        tabs = findViewById(R.id.jwts_tab);
        pager = findViewById(R.id.jwts_pager);
        fragments = new ArrayList<>();
        fragments.add(FragmentJWTS_grkb.newInstance());
      //  fragments.add(FragmentJWTS_pyfa.newInstance());
       // fragments.add(FragmentJWTS_xsxk.newInstance());
        fragments.add(FragmentJWTS_cjgl.newInstance());
       // fragments.add(FragmentJWTS_ksxx.newInstance());
      //  fragments.add(FragmentJWTS_info.newInstance());
        String[] titles = new String[]{"个人课表导入", "成绩查询"};
        pagerAdapter = new JWTSPagerAdapter(getSupportFragmentManager(), fragments, titles);
        pager.setAdapter(pagerAdapter);
        tabs.setupWithViewPager(pager);
        if(!TextUtils.isEmpty(getIntent().getStringExtra("terminal"))){
            pager.setCurrentItem(Integer.parseInt(getIntent().getStringExtra("terminal")));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_jwts, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    class checkLoginTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                return jwCore.loginCheck();
            } catch (JWException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean o) {
            super.onPostExecute(o);
            if (!o) {
                jwCore.logOut();
                Toast.makeText(HContext, "页面过期，请返回重新登录！", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(ActivityJWTS.this, ActivityLoginJWTS.class);
                startActivity(i);
                finish();
            }
        }
    }
}
