package com.stupidtree.hita.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.gson.Gson;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.NormalPagerAdapter;
import com.stupidtree.hita.timetable.Curriculum;
import com.stupidtree.hita.fragments.FragmentAddEvent;
import com.stupidtree.hita.fragments.FragmentNavi;
import com.stupidtree.hita.fragments.FragmentTasks;
import com.stupidtree.hita.fragments.FragmentTheme;
import com.stupidtree.hita.fragments.FragmentTimeLine;
import com.stupidtree.hita.fragments.FragmentTimeTablePage;
import com.stupidtree.hita.jw.JWException;
import com.stupidtree.hita.util.ActivityUtils;
import com.stupidtree.hita.util.FileOperator;
import com.tencent.bugly.beta.Beta;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

import static com.stupidtree.hita.HITAApplication.*;
import static com.stupidtree.hita.activities.ActivityLoginUT.UT_login_url;
import static com.stupidtree.hita.util.UpdateManager.checkUpdate;

public class ActivityMain extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener
        , FragmentTimeLine.OnFragmentInteractionListener
        , FragmentAddEvent.OnFragmentInteractionListener
        , FragmentTimeTablePage.OnFragmentInteractionListener
        , FragmentNavi.OnFragmentInteractionListener {


    public static boolean app_task_enabled;
    FragmentTimeLine tlf;
    FragmentNavi nvf;
    FragmentTasks tskf;
    public FloatingActionButton fabmain;
    Toolbar mToolbar;
    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;
    ImageView drawerUserAvatar;
    TextView drawerUserName, drawerSignature;
    ActionBarDrawerToggle mActionBarDrawerToggle;
    ViewPager mainPager;
    TabLayout mainTabs;
    NormalPagerAdapter pagerAdapter;
    CardView drawer_card_profile, drawer_card_curriculummanager, drawer_card_theme, drawer_card_dynamic;
    CardView avatar_card;
    ImageView drawer_bg, drawer_bt_settings;
    MenuItem dark_mode_menu;
    boolean isFirst;

    //boolean upDateNoti1202;
    @Override
    protected void stopTasks() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true, true, false);
        isFirst = defaultSP.getBoolean("firstOpen", true);
        app_task_enabled = defaultSP.getBoolean("app_events_enabled", true);
       // upDateNoti1202 = defaultSP.getBoolean("update_noti_1202",true);
        tlf = FragmentTimeLine.newInstance(isFirst);
        nvf = new FragmentNavi();
        if (app_task_enabled) tskf = new FragmentTasks();
        checkAPPPermission();
        setContentView(R.layout.activity_main);
        fabmain = findViewById(R.id.fab_main);
        fabmain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //buildHita();
                if(timeTableCore.isDataAvailable()){
                    Intent i = new Intent(ActivityMain.this, ActivityTimeTable.class);
                    startActivity(i);
                }else{
                    Snackbar.make(v,HContext.getString(R.string.notif_importdatafirst),Snackbar.LENGTH_SHORT).show();
                }


            }
        });
        if (defaultSP.getBoolean("autoCheckUpdate", true)) checkUpdate(this);
        initDrawer();
        initToolBar();
        initPager();
        if (isFirst) {
            try {
                Guide();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void autoLogin() {
        String ut_un = defaultSP.getString("ut_username",null);
        if(ut_un!=null){
            String ut_pw = defaultSP.getString(ut_un+".password",null);
            if(ut_pw!=null) new loginUTInBackgroundTask(ut_un,ut_pw).executeOnExecutor(HITAApplication.TPE);
        }

        if (!defaultSP.getBoolean("jwts_autologin", true)) return;
        if (CurrentUser != null) {
            String stun = CurrentUser.getStudentnumber();
            String password = null;
            String cookie = defaultSP.getString("jw_cookie",null);
            if(cookie!=null){
                Log.e("JW_LOGIN","有本地cookie，进行载入");
                Gson gson = new Gson();
                jwCore.loadCookies(gson.fromJson(cookie,jwCore.getCookies().getClass()));
            }
            if (!TextUtils.isEmpty(stun)) password = defaultSP.getString(stun + ".password", null);
            if (password != null) {
                new loginJWTSInBackgroundTask(stun, password).executeOnExecutor(HITAApplication.TPE);
            }
        }


     }


    void initToolBar() {
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //1.决定显示.
        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close); //2.传入Toolbar可以点击.
        mActionBarDrawerToggle.setDrawerSlideAnimationEnabled(true);
        mActionBarDrawerToggle.syncState();
        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle); //3.监听变化.
        getSupportActionBar().setTitle("");
        mToolbar.inflateMenu(R.menu.toolbar_main);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_search) {
                    WindowManager manager = getWindowManager();
                    DisplayMetrics outMetrics = new DisplayMetrics();
                    manager.getDefaultDisplay().getMetrics(outMetrics);
                    int width = outMetrics.widthPixels;
                    presentActivity(ActivityMain.this, width, 10);
                }
                return true;
            }
        });
    }

    void initPager() {
        mainPager = findViewById(R.id.mainPager);
        mainTabs = findViewById(R.id.mainTabs);
        ArrayList fragments = new ArrayList();
        fragments.add(nvf);
        fragments.add(tlf);
       // fragments.add(new FragmentTasksBackUp());
        if (tskf != null & app_task_enabled) fragments.add(tskf);
        mainTabs.setSelectedTabIndicatorColor(Color.parseColor("#00000000"));
        pagerAdapter = new NormalPagerAdapter(getSupportFragmentManager(), fragments,
                new String[]{HContext.getString(R.string.maintab_navi), HContext.getString(R.string.maintab_today),HContext.getString(R.string.maintab_events)});
        mainTabs.setupWithViewPager(mainPager);

        mainPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                if (i != 1){
                    fabmain.hide();

                }
                else {
                    fabmain.show();

                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        mainPager.setAdapter(pagerAdapter);
        mainPager.setCurrentItem(1);

    }

    public void presentActivity(Activity activity, int revealX, int revealY) {
        Intent intent = new Intent(this, ActivitySearch.class);
        intent.putExtra(ActivityJWTS.EXTRA_CIRCULAR_REVEAL_X, revealX);
        intent.putExtra(ActivityJWTS.EXTRA_CIRCULAR_REVEAL_Y, revealY);
        ActivityCompat.startActivity(activity, intent, null);
        overridePendingTransition(0, 0);
    }


    public void Guide(){
        new TapTargetSequence(this)
                .targets(
                        TapTarget.forView(mainPager, HContext.getString(R.string.guide_1p), HContext.getString(R.string.guide_1s))
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(true)
                                .transparentTarget(false)
                                .outerCircleColor(R.color.blue_accent)
                                .titleTextSize(24)
                                .id(1)
                                .icon(getDrawable(R.drawable.ic_navigation))
                        ,
                        TapTarget.forView(mainTabs, HContext.getString(R.string.guide_2p), HContext.getString(R.string.guide_2s))
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(true)
                                .transparentTarget(false)
                                .outerCircleColor(R.color.blue_accent)
                                .titleTextSize(24),

                        TapTarget.forToolbarNavigationIcon(mToolbar, HContext.getString(R.string.guide_3p), HContext.getString(R.string.guide_3s))
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(true)
                                .titleTextSize(18)
                                .id(13)
                                .transparentTarget(true)
                                .targetCircleColor(R.color.white)
                                .outerCircleColor(R.color.blue_accent)
                        ,
                        TapTarget.forView(drawerUserAvatar, HContext.getString(R.string.guide_4p), HContext.getString(R.string.guide_4s))
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(true)
                                .id(14)
                                .titleTextSize(18)
                                .transparentTarget(true)
                                .targetCircleColor(R.color.white)
                                .outerCircleColor(R.color.blue_accent),

                        TapTarget.forView(fabmain, HContext.getString(R.string.guide_5p), HContext.getString(R.string.guide_5s))
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(false)
                                .transparentTarget(false)
                                .targetCircleColor(R.color.white)
                                .outerCircleColor(R.color.blue_accent),
                        TapTarget.forToolbarMenuItem(mToolbar, R.id.action_search, HContext.getString(R.string.guide_6p), HContext.getString(R.string.guide_6s))
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(false)
                                .transparentTarget(false)
                                .targetCircleColor(R.color.white)
                                .outerCircleColor(R.color.blue_accent),
                        TapTarget.forView(mainPager, HContext.getString(R.string.guide_7p), HContext.getString(R.string.guide_7s))
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(true)
                                .titleTextSize(24)
                                .descriptionTextSize(18)
                                .transparentTarget(false)
                                .targetCircleColor(R.color.white)
                                .outerCircleColor(R.color.amber_primary)
                                .icon(getDrawable(R.drawable.bt_guide_done))
                ).listener(new TapTargetSequence.Listener() {
            @Override
            public void onSequenceFinish() {
                Intent i = new Intent(ActivityMain.this, ActivityLogin.class);
                startActivity(i);
                defaultSP.edit().putBoolean("firstOpen", false).apply();
                isFirst = false;
            }

            @Override
            public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                if (lastTarget.id() == 14) tlf.showHeadCard();
                if (lastTarget.id() == 13) mDrawerLayout.openDrawer(Gravity.LEFT);
                if (lastTarget.id() == 14) mDrawerLayout.closeDrawer(Gravity.LEFT);
            }

            @Override
            public void onSequenceCanceled(TapTarget lastTarget) {

            }
        }).start();


    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(defaultSP.getBoolean("auto_upload_user_data",true)) saveData();
    }


    public static void saveData() {
        new SaveDataTask().executeOnExecutor(HITAApplication.TPE);
    }


    public void checkAPPPermission() {
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.RECORD_AUDIO);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_CONTACTS);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(this, permissions, 1);
        }
        FileOperator.verifyStoragePermissions(this);
    }

    private void initDrawer() {
        mDrawerLayout = findViewById(R.id.main_drawer_layout);
        mNavigationView = findViewById(R.id.drawer_navigationview);
        View headview = mNavigationView.inflateHeaderView(R.layout.activity_main_nav_header);
        drawerUserAvatar = headview.findViewById(R.id.main_drawer_user_avatar);
        avatar_card = headview.findViewById(R.id.main_drawer_user_avatar_card);
        drawerUserName = headview.findViewById(R.id.main_drawer_user_name);
        drawerSignature = headview.findViewById(R.id.main_drawer_user_signature);
        drawer_card_curriculummanager = headview.findViewById(R.id.drawer_card_curriculummanager);
        drawer_card_profile = headview.findViewById(R.id.drawer_card_hita);
        drawer_card_theme = headview.findViewById(R.id.drawer_card_theme);
        drawer_card_dynamic = headview.findViewById(R.id.drawer_card_dynamic);
        //  drawerUserAvatar.setOnClickListener(new onUserAvatarClickListener());
        drawer_bg = headview.findViewById(R.id.drawer_bg);
        drawer_bg.setOnClickListener(new onUserAvatarClickListener());
        drawer_bt_settings = headview.findViewById(R.id.drawer_bt_setting);
//        drawerheader = headview.findViewById(R.id.drawer_header);
//        drawerheader.setOnClickListener(new onUserAvatarClickListener());
        Menu menu = mNavigationView.getMenu();
        dark_mode_menu = menu.findItem(R.id.drawer_nav_darkmode);
        dark_mode_menu.setActionView(R.layout.action_switch_darkmode);
        Switch switchA = dark_mode_menu.getActionView().findViewById(R.id.switch_darkmode);
        switchA.setChecked(defaultSP.getBoolean("is_dark_mode", false));
        switchA.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                defaultSP.edit().putBoolean("is_dark_mode", isChecked).apply();
                if (isChecked)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                recreate();
            }
        });


        drawer_bt_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ActivityMain.this, ActivitySetting.class);
                startActivity(i);
            }
        });
        drawer_card_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pp = new Intent(ActivityMain.this, ActivityChatbot.class);
                ActivityMain.this.startActivity(pp);
            }
        });
        drawer_card_dynamic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent pp = new Intent(ActivityMain.this, ActivityDynamicTable.class);
                    ActivityMain.this.startActivity(pp);
            }
        });
        avatar_card.setOnClickListener(new onUserAvatarClickListener());
        drawer_card_curriculummanager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ActivityMain.this, ActivityCurriculumManager.class);
                startActivity(i);
            }
        });
        drawer_card_theme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FragmentTheme().show(getSupportFragmentManager(), "fragment_theme");
            }
        });
        mDrawerLayout.setStatusBarBackgroundColor(Color.parseColor("#00000000"));
        mDrawerLayout.setScrimColor(Color.parseColor("#00000000"));
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {

                    case R.id.drawer_nav_settings:
                        Intent x = new Intent(ActivityMain.this, ActivitySetting.class);
                        ActivityMain.this.startActivity(x);
                        break;
                    case R.id.drawer_nav_about:
                        Intent d = new Intent(ActivityMain.this, ActivityAboutHITA.class);
                        ActivityMain.this.startActivity(d);
                        break;
                    case R.id.drawer_nav_info:
                        Intent ppp = new Intent(ActivityMain.this, ActivityNews.class);
                        startActivity(ppp);
                        break;
                    case R.id.drawer_nav_search:
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                        WindowManager manager = getWindowManager();
                        DisplayMetrics outMetrics = new DisplayMetrics();
                        manager.getDefaultDisplay().getMetrics(outMetrics);
                        int width = outMetrics.widthPixels;
                        presentActivity(ActivityMain.this, width, 10);
                        break;
                    case R.id.drawer_nav_jwts:
                        ActivityUtils.startJWTSActivity(ActivityMain.this);
                        break;
                    case R.id.drawer_nav_ut:
                        ActivityUtils.startUTActivity(ActivityMain.this);
                        break;
                    case R.id.drawer_nav_report:
                        try {
                         // Toast.makeText(HContext,"发")
                            //第二种方式：可以跳转到添加好友，如果qq号是好友了，直接聊天
                            String url = "mqqwpa://im/chat?chat_type=wpa&uin=1012124511";
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(HContext,HContext.getString(R.string.notif_installQQ),Toast.LENGTH_SHORT).show();
                        }


                }
                return false;
            }
        });
    }

    protected void refreshDrawerHeader() {
        if (CurrentUser == null) {
            drawerUserAvatar.
                    setImageResource(R.drawable.ic_account);
            //drawer_bg.setImageResource(R.drawable.gradient_bg);
            drawerUserName.setText(HContext.getString(R.string.drawer_username_null));
            drawerSignature.setText(HContext.getString(R.string.drawer_signature_null));
        } else {
            if (TextUtils.isEmpty(CurrentUser.getAvatarUri())) {
                drawerUserAvatar.setImageResource(R.drawable.ic_account_activated);
                // drawer_bg.setImageResource(R.drawable.gradient_bg);
            } else {
                Glide.with(ActivityMain.this)
                        .load(CurrentUser.getAvatarUri())
                        //.placeholder(R.drawable.ic_account_activated)
                        //.skipMemoryCache(false)
                        //.dontAnimate()
                        //.signature(new ObjectKey(Objects.requireNonNull(defaultSP.getString("avatarGlideSignature", String.valueOf(System.currentTimeMillis())))))
                        .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                        .into(drawerUserAvatar);
//                Glide.with(ActivityMain.this).load(CurrentUser.getAvatarUri())
//                        //.signature(new ObjectKey(Objects.requireNonNull(defaultSP.getString("avatarGlideSignature", String.valueOf(System.currentTimeMillis())))))
//                        //.placeholder(R.drawable.ic_account_activated)
//                        .apply(RequestOptions.bitmapTransform(new mBlurTransformation(this, 24, 6)))
//                        .into(drawer_bg);
            }
            drawerUserName.setText(CurrentUser.getNick());
            drawerSignature.setText(TextUtils.isEmpty(CurrentUser.getSignature()) ?HContext.getString(R.string.drawer_signature_none) : CurrentUser.getSignature());
        }

        if (defaultSP.getString("dark_mode_mode", "dark_mode_normal").equals("dark_mode_normal")) {
            dark_mode_menu.setVisible(true);
        } else {
            dark_mode_menu.setVisible(false);
        }
    }


    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //4.同步状态
        //mActionBarDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    protected void onResume() {
        super.onResume();
        try {
            if(!jwCore.hasLogin()) autoLogin();
        } catch (Exception e) {
            e.printStackTrace();
            new FileOperator.errorTableText("自动登录错误", e).save(new SaveListener<String>() {
                @Override
                public void done(String s, BmobException e) {

                }
            });
        }
        //Glide.with(ActivityMain.this).load(userInfos.get("头像")).into(drawerUserAvatar);
        //if(tlf.hasInit) tlf.Refresh(FragmentTimeLine.TL_REFRESH_FROM_UNHIDE);
        refreshDrawerHeader();
    }

    @Override
    public void onCalledRefresh() {

    }


    class onUserAvatarClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (CurrentUser == null) {
                Intent i = new Intent(ActivityMain.this, ActivityLogin.class);
                startActivity(i);
            } else {
                ActivityOptions option = ActivityOptions.makeSceneTransitionAnimation(ActivityMain.this, drawerUserAvatar, "useravatar");
                Intent i = new Intent(ActivityMain.this, ActivityUserCenter.class);
                ActivityMain.this.startActivity(i, option.toBundle());
            }
        }
    }


    static class SaveDataTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                for (Curriculum c : timeTableCore.getAllCurriculum()) {
                  c.saveToDB();
                }
                return timeTableCore.saveDataToCloud(false);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }


    public static void showUpdateDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(HContext.getString(R.string.update_dialog_title))
                .setMessage(HContext.getString(R.string.update_dialog_mes1) + Beta.getUpgradeInfo().versionName + "\n" + HContext.getString(R.string.update_dialog_mes2) + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Beta.getUpgradeInfo().publishTime))
                .setCancelable(false)
                .setPositiveButton(HContext.getString(R.string.button_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent it = new Intent(context, ActivityAboutHITA.class);
                        context.startActivity(it);
                    }
                })
                .setNegativeButton(HContext.getString(R.string.button_cancel), null);
        builder.create();
        builder.show();
    }

    @Override
    public void onBackPressed() {
       if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) mDrawerLayout.closeDrawers();
        else { //返回桌面而不是退出
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
        }
    }



    public static class loginJWTSInBackgroundTask extends AsyncTask {

        String username, password;

        loginJWTSInBackgroundTask(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                if(!jwCore.loginCheck()) {
                    Log.e("JW_LOGIN","无登录状态保持，开始重新请求");
                    return jwCore.login(username,password);
                }
                Log.e("JW_LOGIN","登录状态保持，无需重新请求");
                return true;
            } catch (JWException e) {
               return e;
            }
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if(o instanceof JWException){
                Intent i = new Intent();
                i.setAction("COM.STUPIDTREE.HITA.JWTS_LOGIN_FAIL");
                LocalBroadcastManager.getInstance(HContext).sendBroadcast(i);
            }else if(o instanceof Boolean){
                if ((Boolean) o) {
                    Log.e("!", "登录成功");
                    Intent i = new Intent();
                    i.setAction("COM.STUPIDTREE.HITA.JWTS_AUTO_LOGIN_DONE");
                    LocalBroadcastManager.getInstance(HContext).sendBroadcast(i);
                } else {
                    Intent i = new Intent();
                    i.setAction("COM.STUPIDTREE.HITA.JWTS_LOGIN_FAIL");
                    LocalBroadcastManager.getInstance(HContext).sendBroadcast(i);
                }
            }
        }
    }
    public static class loginUTInBackgroundTask extends AsyncTask {

        String username, password;
       // SwipeRefreshLayout refreshLayout = null;

        loginUTInBackgroundTask(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
          //  if(refreshLayout!=null) refreshLayout.setRefreshing(true);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            if(cookies_ut.isEmpty()){
                String temped = defaultSP.getString("ut_cookies",null);
                if(temped!=null){
                    HashMap cookies = new Gson().fromJson(temped,HashMap.class);
                    cookies_ut.putAll(cookies);
                }
            }
            if (checkLogin_UT()) return true;
            try {
                Connection.Response response = Jsoup.connect(UT_login_url)
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .timeout(5000).execute();
                cookies_ut.clear();
                cookies_ut.putAll(response.cookies());
                Document d = Jsoup.connect(UT_login_url)
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .cookies(cookies_ut).get();
                //System.out.println(d);
                String lt = d.getElementsByAttributeValue("name","lt").first().attr("value");

                Document after =  Jsoup.connect(UT_login_url)
                        .cookies(cookies_ut).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36")
                        .data("username",username)
                        .data("password", Base64.encodeToString(password.getBytes(),Base64.DEFAULT))
                        .data("lt",lt)
                        .data("Connection","keep-alive")
                        .data("_eventId","submit").post();
                //System.out.println(after);
                //Log.e("cookies", String.valueOf(cookies_ut));
                if (after.toString().contains("姓 名：")) return true;
                else if(after.toString().contains("您已在别的终端登录"))
                {
                    Log.e("UT","多终端登录，改变链接");
                    Document after2 =  Jsoup.connect(UT_login_url)
                            .cookies(cookies_ut).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36")
                            .data("username",username)
                            .data("password", Base64.encodeToString(password.getBytes(),Base64.DEFAULT))
                            .data("Connection","keep-alive")
                            .data("lt",lt)
                            .data("continueLogin","1")
                            .data("_eventId","submit").post();
                   if(after2.toString().contains("姓 名：")){
                       ut_username = username;
                       return true;
                   }else return false;
                }
                else return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
          //  return !cookies_ut.isEmpty();
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
           // if(refreshLayout!=null) refreshLayout.setRefreshing(false);
            if ((Boolean) o) {
                login_ut = true;
                ut_username = username;
                Log.e("!", "UT登录成功");
                Intent i = new Intent();
                i.setAction("COM.STUPIDTREE.HITA.UT_AUTO_LOGIN_DONE");
                LocalBroadcastManager.getInstance(HContext).sendBroadcast(i);
            } else {
                cookies_ut.clear();
                login_ut = false;
                Intent i = new Intent();
                i.setAction("COM.STUPIDTREE.HITA.UT_LOGIN_FAIL");
                LocalBroadcastManager.getInstance(HContext).sendBroadcast(i);
            }
        }
    }



    public static boolean checkLogin_UT(){
        try {
            //  HashMap tempC = new HashMap()
            Connection.Response r = Jsoup.connect("http://10.64.1.15/sfrzwhlgportalHome.action")
                    .data("errorcode","1")
                    .header("Connection","keep-alive")
                    .data("continueurl","http://ecard.utsz.edu.cn/accountcardUser.action")
                    .data("ssoticketid",ut_username)
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
}