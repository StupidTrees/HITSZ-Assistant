package com.stupidtree.hita.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.JWTSPagerAdapter;
import com.stupidtree.hita.diy.RevealAnimation;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.cookies;
import static com.stupidtree.hita.HITAApplication.login;

public class ActivityJWTS extends BaseActivity implements  FragmentJWTS_grkb.OnFragmentInteractionListener, FragmentJWTS_xsxk.OnFragmentInteractionListener, FragmentJWTS_ksxx.OnFragmentInteractionListener,
        FragmentJWTS_cjgl_grcj.OnFragmentInteractionListener, FragmentJWTS_pyfa.OnFragmentInteractionListener,FragmentJWTS_pyfa_pyjhcx.OnFragmentInteractionListener,FragmentJWTS_pyfa_zxjxjh.OnFragmentInteractionListener,
        FragmentJWTS_cjgl_xxjd.OnFragmentInteractionListener,FragmentJWTS_cjgl.OnFragmentInteractionListener , FragmentJWTS_cjgl_xfj.OnFragmentInteractionListener {
    public static final String EXTRA_CIRCULAR_REVEAL_X = "EXTRA_CIRCULAR_REVEAL_X";
    public static final String EXTRA_CIRCULAR_REVEAL_Y = "EXTRA_CIRCULAR_REVEAL_Y";
    CollapsingToolbarLayout collaspinglayout;
    WebView webview;
    ViewPager pager;
    JWTSPagerAdapter pagerAdapter;
    List<Fragment> fragments;
    TabLayout tabs;
    HashMap<String,String> userInfos;
    //FloatingActionButton fab_sync;
    //头像
    private byte[] avatar;
    CoordinatorLayout rootLayout;
    private RevealAnimation mRevealAnimation;
    private int revealX;
    private int revealY;
    ImageView avatarView;
    TextView nameText,studentnumberText;
    refreshInfoTask pageTask;

    @Override
    protected void stopTasks() {
        if(pageTask!=null&&!pageTask.isCancelled()) pageTask.cancel(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true,true,false);
        setContentView(R.layout.activity_jwts);
        webview = findViewById(R.id.webview);
        rootLayout = findViewById(R.id.jwts_root);
        userInfos = new HashMap<>();
        avatarView = findViewById(R.id.jwts_avatar);
        nameText = findViewById(R.id.jwts_name);

        studentnumberText = findViewById(R.id.jwts_studentnumber);
        initToolbar();
        initPager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(pageTask!=null&&!pageTask.isCancelled()) pageTask.cancel(true);
        pageTask = new refreshInfoTask();
        pageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    void initToolbar(){

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("哈工深教务");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        collaspinglayout = findViewById(R.id.collapsingtoolbar);
        collaspinglayout.setExpandedTitleColor(Color.parseColor("#00000000"));
        collaspinglayout.setCollapsedTitleTextColor(ContextCompat.getColor(this,R.color.material_primary_text));
        toolbar.inflateMenu(R.menu.toolbar_jwts);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(menuItem.getItemId()==R.id.action_logout){
                    AlertDialog ad = new AlertDialog.Builder(ActivityJWTS.this).create();
                    ad.setMessage("下次进入需要重新登录，是否退出？");
                    ad.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            login = false;
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
        fragments.add(new FragmentJWTS_info(userInfos));
        String[] titles = new String[]{ "学生课表导入","培养方案","学生选课","成绩管理","考试详细查询","个人信息"};
        pagerAdapter = new JWTSPagerAdapter(getSupportFragmentManager(),fragments,titles);
        pager.setAdapter(pagerAdapter);
        tabs.setupWithViewPager(pager);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_jwts,menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void onAnimateLayout(Bundle savedInstanceState, Intent intent) {
        if (savedInstanceState == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                intent.hasExtra(EXTRA_CIRCULAR_REVEAL_X) &&
                intent.hasExtra(EXTRA_CIRCULAR_REVEAL_Y)) {
            rootLayout.setVisibility(View.INVISIBLE);

            revealX = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_X, 0);
            revealY = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_Y, 0);

            ViewTreeObserver viewTreeObserver = rootLayout.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mRevealAnimation.revealActivity(revealX, revealY);
                        rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
            }
        } else {
            rootLayout.setVisibility(View.VISIBLE);
        }
    }

    public void getUserInfo(Document doc){
        Element table = doc.getElementsByTag("table").first();
        try {
            Elements ths = table.getElementsByTag("th");
            Elements tds = table.getElementsByTag("td");
            for(int i = 0;i<tds.size();i++){
                if(tds.get(i).toString().contains("<img")){
                    String image;
                    image = "http://jwts.hitsz.edu.cn/"+tds.get(i).select("img").attr("src");
                   // userInfos.put("头像",image);
                    avatar = Jsoup.connect(image).cookies(cookies).ignoreContentType(true).execute().bodyAsBytes();
                    new saveAvatarTask(image).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//                    if(avatar==null) Toast.makeText(HContext,"加载教务头像失败！",Toast.LENGTH_SHORT).show();
//                    else Glide.with(HContext).load(avatar).signature(new ObjectKey(System.currentTimeMillis())).placeholder(R.drawable.ic_account).apply(RequestOptions.bitmapTransform(new CircleCrop())).into(avatarView);

                    tds.remove(i);
                }
            }
            for(int i = 0;i<ths.size();i++){
                userInfos.put(ths.get(i).text().replaceAll("：",""),tds.get(i).text());
            }
            if(CurrentUser!=null&&userInfos.get("学号").equals(CurrentUser.getStudentnumber())){
                CurrentUser.setSchool(userInfos.get("系"));
                CurrentUser.setRealname(userInfos.get("姓名"));
                CurrentUser.update(new UpdateListener() {
                    @Override
                    public void done(BmobException e) {
                        Toast.makeText(HContext,"已更新用户信息",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println(userInfos);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    class refreshInfoTask extends AsyncTask<String,Integer,Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                Document userinfo = Jsoup.connect("http://jwts.hitsz.edu.cn/xswhxx/queryXswhxx").cookies(cookies).timeout(20000)
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .ignoreContentType(true)
                        .get();
                //userinfo.toString().contains("alert('")||
                if(userinfo.getElementsByTag("table").size()<=0){
                    System.out.println(userinfo.toString());
                    return false;
                    //dd.toString().substring(dd.toString().indexOf("alert('")+7,dd.toString().indexOf("\')",dd.toString().indexOf("alert(\'"))).contains("过期")
                }
                getUserInfo(userinfo);
            } catch (IOException e) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean o) {
            super.onPostExecute(o);
            try {
                if(!o){
                    Toast.makeText(HContext,"页面过期，请返回重新登录！",Toast.LENGTH_SHORT).show();
                    login = false;
                    Intent i = new Intent(ActivityJWTS.this, ActivityLoginJWTS.class);
                    ActivityJWTS.this.startActivity(i);
                    finish();
                }
                //Glide.with(HContext).load(avatar).signature(new ObjectKey(System.currentTimeMillis())).placeholder(R.drawable.ic_account).apply(RequestOptions.bitmapTransform(new CircleCrop())).into(avatarView);
                if(CurrentUser!=null&&userInfos.get("学号").equals(CurrentUser.getStudentnumber()))  studentnumberText.setText(userInfos.get("学号")+"（已与本账号绑定）");
                else  studentnumberText.setText(userInfos.get("学号"));
                nameText.setText(userInfos.get("姓名"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class saveAvatarTask extends AsyncTask{
        String link;
        saveAvatarTask(String link){
            this.link = link;
        }
        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                avatar = Jsoup.connect(link).cookies(cookies).ignoreContentType(true).execute().bodyAsBytes();
            } catch (IOException e) {
               return null;
            }
            //FileOperator.saveAvatarToFile(ActivityJWTS.this.getFilesDir(), "avatar_ugly", BitmapFactory.decodeByteArray(avatar,0,avatar.length));
            //defaultSP.edit().putString("avatarGlideSignature", String.valueOf(System.currentTimeMillis())).commit();
            return avatar;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if(o==null) Toast.makeText(HContext,"加载教务头像失败！",Toast.LENGTH_SHORT).show();
            else Glide.with(HContext).load(avatar).signature(new ObjectKey(System.currentTimeMillis())).placeholder(R.drawable.ic_account).apply(RequestOptions.bitmapTransform(new CircleCrop())).into(avatarView);

            //Toast.makeText(ActivityJWTS.this,"已同步个人信息到APP",Toast.LENGTH_SHORT).show();
          }
    }




}
