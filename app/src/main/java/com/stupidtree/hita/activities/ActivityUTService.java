package com.stupidtree.hita.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.fragments.FragmentUT_Card;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.cookies_ut;
import static com.stupidtree.hita.HITAApplication.cookies_ut_card;
import static com.stupidtree.hita.HITAApplication.defaultSP;
import static com.stupidtree.hita.HITAApplication.login_ut;
import static com.stupidtree.hita.HITAApplication.ut_username;

public class ActivityUTService extends BaseActivity {
    String username;
    ViewPager pager;
    UTPagerAdapter pagerAdapter;
    List<BaseFragment> fragments;

    TabLayout tabs;
    @Override
    protected void stopTasks() {

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true,true,false);
        username = getIntent().getStringExtra("username");
        setContentView(R.layout.activity_utservice);
        initToolbar();
        initPager();
    }
    @Override
    protected void onResume() {
        super.onResume();
        new checkLoginTask().executeOnExecutor(HITAApplication.TPE);
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
        toolbar.inflateMenu(R.menu.toolbar_ut);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_logout) {
                    AlertDialog ad = new AlertDialog.Builder(ActivityUTService.this).create();
                    ad.setMessage("下次进入需要重新登录，是否退出？");
                    ad.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Document d = Jsoup.connect("https://idp.utsz.edu.cn/cas/logout")
                                                .cookies(cookies_ut).header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36").header("Connection","keep-alive")
                                                .get();
                                        cookies_ut.clear();
                                        System.out.println(d);
                                        login_ut = false;
                                        ut_username = null;
                                        defaultSP.edit().putString("ut_cookies",null).apply();
                                        Intent i = new Intent(ActivityUTService.this, ActivityLoginUT.class);
                                        ActivityUTService.this.startActivity(i);
                                        finish();
                                    } catch (IOException e) {
                                        Toast.makeText(HContext,"登出失败！"+e.toString(),Toast.LENGTH_SHORT).show();
                                        e.printStackTrace();
                                    }
                                }
                            }).start();

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
        fragments.add(FragmentUT_Card.newInstance(username));
        String[] titles = new String[]{"校园卡管理"};
        pagerAdapter = new UTPagerAdapter(getSupportFragmentManager(), fragments, titles);
        pager.setAdapter(pagerAdapter);
        tabs.setupWithViewPager(pager);
        if(!TextUtils.isEmpty(getIntent().getStringExtra("terminal"))){
            pager.setCurrentItem(Integer.parseInt(getIntent().getStringExtra("terminal")));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_ut, menu);
        return super.onCreateOptionsMenu(menu);
    }
    class checkLoginTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
              //  HashMap tempC = new HashMap()
                Connection.Response r = Jsoup.connect("http://10.64.1.15/sfrzwhlgportalHome.action")
                        .data("errorcode","1")
                        .header("Connection","keep-alive")
                        .data("continueurl","http://ecard.utsz.edu.cn/accountcardUser.action")
                        .data("ssoticketid",username)
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36").header("Connection","keep-alive")
                        .execute();
                cookies_ut_card.clear();
                cookies_ut_card.putAll(r.cookies());
                Document d2 = Jsoup.connect("http://10.64.1.15/accountcardUser.action")
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                        .header("Connection","keep-alive")
                        .header("Host","10.64.1.15")
                        .cookies(cookies_ut_card)
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .get();
                return  d2.toString().contains("余额");
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean o) {
            super.onPostExecute(o);
            if (!o) {
                Toast.makeText(HContext, "页面过期，请返回重新登录！", Toast.LENGTH_SHORT).show();
                cookies_ut.clear();
                login_ut = false;
                ut_username = null;
                Intent i = new Intent(ActivityUTService.this, ActivityLoginUT.class);
                startActivity(i);
                finish();
            }
        }
    }
    public class UTPagerAdapter extends FragmentPagerAdapter {
        List<BaseFragment> mBeans;
        String[] titles;
        public UTPagerAdapter(FragmentManager fm, List<BaseFragment> res, String[] titles) {
            super(fm,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            mBeans = res;
            this.titles = titles;
        }

        @Override
        public Fragment getItem(int i) {
            return mBeans.get(i);
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            //super.destroyItem(container, position, object);
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        @Override
        public int getCount() {
            return mBeans.size();
        }
    }
}
