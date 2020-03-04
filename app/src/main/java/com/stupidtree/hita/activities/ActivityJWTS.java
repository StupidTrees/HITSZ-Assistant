package com.stupidtree.hita.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import androidx.annotation.WorkerThread;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.JWTSPagerAdapter;
import com.stupidtree.hita.diy.MaterialCircleAnimator;
import com.stupidtree.hita.jw.FragmentJWTS_cjgl;
import com.stupidtree.hita.jw.FragmentJWTS_cjgl_grcj;
import com.stupidtree.hita.jw.FragmentJWTS_grkb;
import com.stupidtree.hita.jw.FragmentJWTS_xsxk;
import com.stupidtree.hita.jw.JWException;
import com.stupidtree.hita.jw.JWFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.TPE;
import static com.stupidtree.hita.HITAApplication.defaultSP;
import static com.stupidtree.hita.HITAApplication.jwCore;

public class ActivityJWTS extends BaseActivity implements JWFragment.JWRoot {
    public static final String EXTRA_CIRCULAR_REVEAL_X = "EXTRA_CIRCULAR_REVEAL_X";
    public static final String EXTRA_CIRCULAR_REVEAL_Y = "EXTRA_CIRCULAR_REVEAL_Y";
    ViewPager pager;
    JWTSPagerAdapter pagerAdapter;
    List<JWFragment> fragments;
    TabLayout tabs;
    FloatingActionButton fab;
    CoordinatorLayout rootLayout;
    List<Map<String, String>> xnxqItems;
    Map<String, String> keyToTitle;
    View loading;


    @Override
    protected void stopTasks() {

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true, false, false);
        setContentView(R.layout.activity_jwts);

        initViews();
        initToolbar();
        initPager();
        new loadBasicInfosTask().executeOnExecutor(TPE);
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        new checkLoginTask().executeOnExecutor(
//                TPE);
//    }


    void initViews() {
        loading = findViewById(R.id.loading);
        rootLayout = findViewById(R.id.jwts_root);
        fab = findViewById(R.id.fab);
        xnxqItems = new ArrayList<>();
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
        keyToTitle = new HashMap<>();

        // fragments.add(FragmentJWTS_ksxx.newInstance());
        //  fragments.add(FragmentJWTS_info.newInstance());
        pagerAdapter = new JWTSPagerAdapter(getSupportFragmentManager(), fragments);
        pager.setAdapter(pagerAdapter);
        tabs.setupWithViewPager(pager);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_jwts, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public List<Map<String, String>> getXNXQItems() {
        return xnxqItems;
    }

    @Override
    public Map<String, String> getKeyToTitleMap() {
        return keyToTitle;
    }

    class loadBasicInfosTask extends AsyncTask {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading.setVisibility(View.VISIBLE);
            pager.setVisibility(View.GONE);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                xnxqItems.clear();
                xnxqItems.addAll(jwCore.getXNXQ());
                keyToTitle.clear();
                keyToTitle.putAll(jwCore.getXKColumnTitles());
                return true;
            } catch (JWException e) {
                try {
                    xnxqItems.clear();
                    if (tryToReLogin()) {
                        xnxqItems.addAll(jwCore.getXNXQ());
                        keyToTitle.clear();
                        keyToTitle.putAll(jwCore.getXKColumnTitles());
                        return true;
                    } else return e;
                } catch (JWException e2) {
                    return e2;
                }
            }

        }


        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            MaterialCircleAnimator.animHide(loading);
            //  loading.setVisibility(View.GONE);
            pager.setVisibility(View.VISIBLE);
            if (o instanceof JWException) {
                jwCore.logOut();
                Toast.makeText(HContext, "页面过期，请返回重新登录！", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(ActivityJWTS.this, ActivityLoginJWTS.class);
                startActivity(i);
                finish();
            } else if ((boolean) o) {
                fragments.clear();
                fragments.add(FragmentJWTS_grkb.newInstance());
                fragments.add(FragmentJWTS_xsxk.newInstance());
                fragments.add(FragmentJWTS_cjgl.newInstance());
                //  fragments.add(FragmentJWTS_pyfa.newInstance());


                pagerAdapter.notifyDataSetChanged();
                if (!TextUtils.isEmpty(getIntent().getStringExtra("terminal"))) {
                    pager.setCurrentItem(Integer.parseInt(getIntent().getStringExtra("terminal")));
                }
                for (int i = 0; i < fragments.size(); i++) {
                    if (i == pager.getCurrentItem()) {
                        JWFragment current = fragments.get(i);
                        if (current.isResumed()) current.Refresh();
                        else current.setWillRefreshOnResume(true);
                    } else {
                        fragments.get(i).setWillRefreshOnResume(true);
                    }
                }
            }
        }
    }


    @WorkerThread
    public static boolean tryToReLogin() throws JWException {
        if (CurrentUser != null) {
            String stun = CurrentUser.getStudentnumber();
            String password = null;
            if (!TextUtils.isEmpty(stun)) password = defaultSP.getString(stun + ".password", null);
            if (password != null) {
                return jwCore.login(stun, password);
            }
        }
        return false;
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
