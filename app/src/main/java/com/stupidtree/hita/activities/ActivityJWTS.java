package com.stupidtree.hita.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

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
import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.JWTSPagerAdapter;
import com.stupidtree.hita.jwts.FragmentJWTS_cjgl_xfj;
import com.stupidtree.hita.jwts.FragmentJWTS_cjgl_xxjd;
import com.stupidtree.hita.jwts.FragmentJWTS_cjgl;
import com.stupidtree.hita.jwts.FragmentJWTS_cjgl_grcj;
import com.stupidtree.hita.jwts.FragmentJWTS_grkb;
import com.stupidtree.hita.jwts.FragmentJWTS_info;
import com.stupidtree.hita.jwts.FragmentJWTS_ksxx;
import com.stupidtree.hita.jwts.FragmentJWTS_pyfa;
import com.stupidtree.hita.jwts.FragmentJWTS_pyfa_pyjhcx;
import com.stupidtree.hita.jwts.FragmentJWTS_xsxk;
import com.stupidtree.hita.jwts.FragmentJWTS_pyfa_zxjxjh;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;


import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.cookies_jwts;
import static com.stupidtree.hita.HITAApplication.login_jwts;

public class ActivityJWTS extends BaseActivity implements FragmentJWTS_grkb.OnFragmentInteractionListener, FragmentJWTS_xsxk.OnFragmentInteractionListener, FragmentJWTS_ksxx.OnFragmentInteractionListener,
        FragmentJWTS_cjgl_grcj.OnFragmentInteractionListener, FragmentJWTS_pyfa.OnFragmentInteractionListener, FragmentJWTS_pyfa_pyjhcx.OnFragmentInteractionListener, FragmentJWTS_pyfa_zxjxjh.OnFragmentInteractionListener,
        FragmentJWTS_cjgl_xxjd.OnFragmentInteractionListener, FragmentJWTS_cjgl.OnFragmentInteractionListener, FragmentJWTS_cjgl_xfj.OnFragmentInteractionListener {
    public static final String EXTRA_CIRCULAR_REVEAL_X = "EXTRA_CIRCULAR_REVEAL_X";
    public static final String EXTRA_CIRCULAR_REVEAL_Y = "EXTRA_CIRCULAR_REVEAL_Y";
    ViewPager pager;
    JWTSPagerAdapter pagerAdapter;
    List<BaseFragment> fragments;
    TabLayout tabs;
    FloatingActionButton fab;
    CoordinatorLayout rootLayout;


    @Override
    protected void stopTasks() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true, true, false);
        setContentView(R.layout.activity_jwts);
        rootLayout = findViewById(R.id.jwts_root);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragments.get(pager.getCurrentItem()).Refresh();
            }
        });

        initToolbar();
        initPager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        new checkLoginTask().executeOnExecutor(
                HITAApplication.TPE);
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
                            login_jwts = false;
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
        fragments.add(FragmentJWTS_pyfa.newInstance());
        fragments.add(FragmentJWTS_xsxk.newInstance());
        fragments.add(FragmentJWTS_cjgl.newInstance());
        fragments.add(FragmentJWTS_ksxx.newInstance());
        fragments.add(FragmentJWTS_info.newInstance());
        String[] titles = new String[]{"学生课表导入", "培养方案", "学生选课", "成绩管理", "考试详细查询", "个人信息"};
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
    public void getUserInfo(Document doc) {
        Element table = doc.getElementsByTag("table").first();
        try {
            Elements ths = table.getElementsByTag("th");
            Elements tds = table.getElementsByTag("td");

            String stuNum = new String();
            String school = new String();
            String realname = new String();
            for (int i = 0; i < tds.size(); i++) {
                if (tds.get(i).toString().contains("<img")) tds.remove(i);
            }
            for (int i = 0; i < ths.size(); i++) {
                String key = ths.get(i).text().replaceAll("：", "");
                if (key.equals("学号")) stuNum = tds.get(i).text();
                if (key.equals("系")) school = tds.get(i).text();
                if (key.equals("姓名")) realname = tds.get(i).text();
            }
           // Log.e("获取用户数据：", stuNum + "," + school + "," + realname);
            if (CurrentUser != null&&!TextUtils.isEmpty(CurrentUser.getStudentnumber())&&stuNum.equals(CurrentUser.getStudentnumber())) {
                CurrentUser.setSchool(school);
                CurrentUser.setRealname(realname);
                CurrentUser.update(new UpdateListener() {
                    @Override
                    public void done(BmobException e) {
                        //Toast.makeText(HContext,"已更新用户信息",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println(userInfos);
    }
    class checkLoginTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                Document userinfo = Jsoup.connect("http://jwts.hitsz.edu.cn:8080/xswhxx/queryXswhxx").cookies(cookies_jwts).timeout(5000)
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .ignoreContentType(true)
                        .get();
                getUserInfo(userinfo);
                if (userinfo.getElementsByTag("table").size() <= 0) {
                    return false;
                   }
            } catch (Exception e) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean o) {
            super.onPostExecute(o);
            if (!o) {
                Toast.makeText(HContext, "页面过期，请返回重新登录！", Toast.LENGTH_SHORT).show();
                cookies_jwts.clear();
                login_jwts = false;
                Intent i = new Intent(ActivityJWTS.this, ActivityLoginJWTS.class);
                startActivity(i);
                finish();
            }
        }
    }
}
