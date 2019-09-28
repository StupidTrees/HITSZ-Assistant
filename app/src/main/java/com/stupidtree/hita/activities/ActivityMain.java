package com.stupidtree.hita.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.Toolbar;

import android.os.Message;
import android.text.TextUtils;
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
import com.getkeepsafe.taptargetview.TapTargetView;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.MainPagerAdapter;
import com.stupidtree.hita.core.Curriculum;
import com.stupidtree.hita.fragments.FragmentAddEvent;
import com.stupidtree.hita.fragments.FragmentNavi;
import com.stupidtree.hita.fragments.FragmentTasks;
import com.stupidtree.hita.fragments.FragmentTheme;
import com.stupidtree.hita.fragments.FragmentTimeLine;
import com.stupidtree.hita.fragments.FragmentTimeTablePage;
import com.stupidtree.hita.util.ActivityUtils;
import com.stupidtree.hita.util.FileOperator;
import com.tencent.bugly.beta.Beta;

import org.ansj.splitWord.analysis.ToAnalysis;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

import static com.stupidtree.hita.HITAApplication.*;
import static com.stupidtree.hita.activities.ActivityLoginJWTS.loginCheck;
import static com.stupidtree.hita.util.SafecodeUtil.getProcessedBitmap;
import static com.stupidtree.hita.util.SafecodeUtil.splitBitmapInto;
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
    //FrameLayout drawerheader;
    ActionBarDrawerToggle mActionBarDrawerToggle;
    ViewPager mainPager;
    TabLayout mainTabs;
    MainPagerAdapter pagerAdapter;
    CardView drawer_card_profile, drawer_card_curriculummanager, drawer_card_theme, drawer_card_dynamic;
    CardView avatar_card;
    ImageView drawer_bg, drawer_bt_settings;
    MenuItem dark_mode_menu;
    boolean isFirst;

    @Override
    protected void stopTasks() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true, true, false);
        isFirst = defaultSP.getBoolean("firstOpen", true);
        app_task_enabled = defaultSP.getBoolean("app_task_enabled", false);
        tlf = FragmentTimeLine.newInstance(isFirst);
        nvf = new FragmentNavi();
        if (app_task_enabled) tskf = new FragmentTasks();
        checkAPPPermission();
        setContentView(R.layout.activity_main);
        fabmain = findViewById(R.id.fab_main);

        fabmain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // QACrawler.gogogo(String.valueOf(getExternalFilesDir("")));
//
                //List a = new ArrayList();
                if(isDataAvailable()){
                    Intent i = new Intent(ActivityMain.this, ActivityTimeTable.class);
                    startActivity(i);
                }else{
                    Snackbar.make(v,"请先导入课表",Snackbar.LENGTH_SHORT).show();
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
        new InitTask(this).execute();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ToAnalysis.parse("abc");
            }
        }).start();
        // autoLogin();
    }

    public static void autoLogin() {
        if (!defaultSP.getBoolean("jwts_autologin", true)) return;
        if (CurrentUser != null) {
            String stun = CurrentUser.getStudentnumber();
            String password = null;
            if (!TextUtils.isEmpty(stun)) password = defaultSP.getString(stun + ".password", null);
            if (password != null) {
                new loginJWTSInBackgroundTask(stun, password).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }
    public static void autoLogin(SwipeRefreshLayout refreshLayout) {
        if (!defaultSP.getBoolean("jwts_autologin", true)) {
            refreshLayout.setRefreshing(false);
            return;
        }
        if (CurrentUser != null) {
            String stun = CurrentUser.getStudentnumber();
            String password = null;
            if (!TextUtils.isEmpty(stun)) password = defaultSP.getString(stun + ".password", null);
            if (password != null) {
                new loginJWTSInBackgroundTask(stun, password,refreshLayout).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }else  refreshLayout.setRefreshing(false);
        }else  refreshLayout.setRefreshing(false);
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
        if (tskf != null & app_task_enabled) fragments.add(tskf);
        mainTabs.setSelectedTabIndicatorColor(Color.parseColor("#00000000"));
        pagerAdapter = new MainPagerAdapter(getSupportFragmentManager(), fragments, new String[]{"校园", "今日", "任务"});
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

    public void presentActivity(Activity activity, View view) {
        int revealX, revealY;
        if (view == null) {
            revealX = 0;
            revealY = 0;
        } else {
            revealX = (int) (view.getX() + view.getWidth() / 2);
            revealY = (int) (view.getY() + view.getHeight() / 2);
        }
        Intent intent = new Intent(this, ActivityChatbot.class);
        intent.putExtra(ActivityJWTS.EXTRA_CIRCULAR_REVEAL_X, revealX);
        intent.putExtra(ActivityJWTS.EXTRA_CIRCULAR_REVEAL_Y, revealY);

        if (view != null) view.setVisibility(View.VISIBLE);
        ActivityCompat.startActivity(activity, intent, null);
        overridePendingTransition(0, 0);
    }

    public void presentActivity(Activity activity, int revealX, int revealY) {
        Intent intent = new Intent(this, ActivitySearch.class);
        intent.putExtra(ActivityJWTS.EXTRA_CIRCULAR_REVEAL_X, revealX);
        intent.putExtra(ActivityJWTS.EXTRA_CIRCULAR_REVEAL_Y, revealY);
        ActivityCompat.startActivity(activity, intent, null);
        overridePendingTransition(0, 0);
    }


    public void Guide() throws Exception {
        new TapTargetSequence(this)
                .targets(
                        TapTarget.forView(mainPager, "欢迎使用HITSZ助手", "以下是使用导航")
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(true)
                                .transparentTarget(false)
                                .outerCircleColor(R.color.blue_accent)
                                .titleTextSize(24)
                                .id(1)
                                .icon(getDrawable(R.drawable.ic_navigation))
                        ,
                        TapTarget.forView(mainTabs, "左右滑动切换主视图", "默认显示今日日程")
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(true)
                                .transparentTarget(false)
                                .outerCircleColor(R.color.blue_accent)
                                .titleTextSize(24),

                        TapTarget.forToolbarNavigationIcon(mToolbar, "点击展开抽屉\n所有功能都藏在这", "教务系统、更换主题、课表管理")
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(true)
                                .titleTextSize(18)
                                .id(13)
                                .transparentTarget(true)
                                .targetCircleColor(R.color.white)
                                .outerCircleColor(R.color.blue_accent)
                        ,
                        TapTarget.forView(drawerUserAvatar, "点击进入个人中心", "管理科目与个人资料")
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(true)
                                .id(14)
                                .titleTextSize(18)
                                .transparentTarget(true)
                                .targetCircleColor(R.color.white)
                                .outerCircleColor(R.color.blue_accent),

                        TapTarget.forView(fabmain, "点击进入时间表", "课程表Plus")
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(false)
                                .transparentTarget(false)
                                .targetCircleColor(R.color.white)
                                .outerCircleColor(R.color.blue_accent),
                        TapTarget.forToolbarMenuItem(mToolbar, R.id.action_search, "点击这里搜索哦", "教室、老师都可以找")
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(false)
                                .transparentTarget(false)
                                .targetCircleColor(R.color.white)
                                .outerCircleColor(R.color.blue_accent)
                ).listener(new TapTargetSequence.Listener() {
            @Override
            public void onSequenceFinish() {
                tlf.continueToGuide();

            }

            @Override
            public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                ;
                if (lastTarget.id() == 14) tlf.showHeadCard();
                if (lastTarget.id() == 13) mDrawerLayout.openDrawer(Gravity.LEFT);
                if (lastTarget.id() == 14) mDrawerLayout.closeDrawer(Gravity.LEFT);
            }

            @Override
            public void onSequenceCanceled(TapTarget lastTarget) {

            }
        }).start();


    }

    public void continueToGuide() {
        TapTargetView.showFor(this, TapTarget.forView(mainPager, "登录HITSZ助手", "开启精彩体验吧")
                .drawShadow(true)
                .cancelable(false)
                .tintTarget(true)
                .titleTextSize(24)
                .descriptionTextSize(18)
                .transparentTarget(false)
                .targetCircleColor(R.color.white)
                .outerCircleColor(R.color.amber_primary)
                .icon(getDrawable(R.drawable.bt_guide_done)), new TapTargetView.Listener() {
            @Override
            public void onTargetClick(TapTargetView view) {
                super.onTargetClick(view);
                Intent i = new Intent(ActivityMain.this, ActivityLogin.class);
                startActivity(i);
                defaultSP.edit().putBoolean("firstOpen", false).commit();
                isFirst = false;
            }
        });
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
        new SaveDataTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
                if (!app_task_enabled) {
                    Toast.makeText(HContext, "开启任务模块方可使用！", Toast.LENGTH_SHORT).show();
                } else {
                    Intent pp = new Intent(ActivityMain.this, ActivityDynamicTable.class);
                    ActivityMain.this.startActivity(pp);
                }
//

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
//        final CoordinatorLayout mainPage = findViewById(R.id.mainpage);
//        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
//
//            @Override
//            public void onDrawerSlide(@NonNull View view, float v) {
//                mainPage.setX(v * view.getWidth());
//            }
//
//            @Override
//            public void onDrawerOpened(@NonNull View view) {
//
//            }
//
//            @Override
//            public void onDrawerClosed(@NonNull View view) {
//
//            }
//
//            @Override
//            public void onDrawerStateChanged(int i) {
//
//            }
//        });
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
//                    case R.id.drawer_nav_user:
//                        drawerUserAvatar.callOnClick();
//                        break;
//                    case R.id.drawer_nav_theme:
//                        Intent p = new Intent(ActivityMain.this,FragmentTheme.class);
//                        ActivityMain.this.startActivity(p);
//                        break;
//                    case R.id.drawer_nav_dynamictimetable:
//                        Intent pp = new Intent(ActivityMain.this,ActivityDynamicTable.class);
//                        ActivityMain.this.startActivity(pp);
//                        break;
//                    case R.id.drawer_nav_datamanage:
//                        //ActivityOptionsCompat op = ActivityOptionsCompat.makeSceneTransitionAnimation(ActivityMain.this);
//                        Intent i = new Intent(ActivityMain.this, ActivityCurriculumManager.class);
//                        startActivity(i);
//                        break;

                    case R.id.drawer_nav_info:
                        Intent ppp = new Intent(ActivityMain.this, ActivityHITSZInfo.class);
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
                    case R.id.drawer_nav_report:
                        try {
                         // Toast.makeText(HContext,"发")
                            //第二种方式：可以跳转到添加好友，如果qq号是好友了，直接聊天
                            String url = "mqqwpa://im/chat?chat_type=wpa&uin=1012124511";
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(HContext,"请安装QQ后使用反馈",Toast.LENGTH_SHORT).show();
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
            drawerUserName.setText("登录");
            drawerSignature.setText("HITSZ账号");
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
            drawerSignature.setText(TextUtils.isEmpty(CurrentUser.getSignature()) ? "无签名" : CurrentUser.getSignature());
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
            autoLogin();
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
                //FileOperator.verifyStoragePermissions(context);
                SQLiteDatabase sd = mDBHelper.getWritableDatabase();
                for (Curriculum c : allCurriculum) {
                    if (sd.update("curriculum", c.getContentValues(), "curriculum_code=?", new String[]{c.curriculumCode}) == 0) {
                        sd.insert("curriculum", null, c.getContentValues());
                    }
                }

                return saveDataToCloud(false);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }


    public static void showUpdateDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("有可用更新")
                .setMessage("新版本：" + Beta.getUpgradeInfo().versionName + "\n" + "发布时间：" + new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss").format(Beta.getUpgradeInfo().publishTime))
                .setCancelable(false)
                .setPositiveButton("查看", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent it = new Intent(context, ActivityAboutHITA.class);
                        context.startActivity(it);
                    }
                })
                .setNegativeButton("取消", null);
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


    static class InitTask extends AsyncTask {

        Activity activity;

        InitTask(Activity f) {
            activity = f;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Log.e("time_test","initialize:begin");
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            initCoreData();

            timeWatcher.refreshProgress(true, true);
            //Log.e("time_test","initialize:end");
            Intent i = new Intent();
            i.setAction("COM.STUPIDTREE.HITA.TIMELINE_REFRESH");
            activity.sendBroadcast(i);

            return null;
        }

    }

    public static class loginJWTSInBackgroundTask extends AsyncTask {

        String username, password;
        SwipeRefreshLayout refreshLayout = null;

        loginJWTSInBackgroundTask(String username, String password) {
            this.username = username;
            this.password = password;
        }
        loginJWTSInBackgroundTask(String username, String password,SwipeRefreshLayout refreshLayout) {
            this.username = username;
            this.password = password;
            this.refreshLayout = refreshLayout;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(refreshLayout!=null) refreshLayout.setRefreshing(true);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            if (checkLogin()) return true;
            File dict = new File(HContext.getFilesDir() + "/tessdata/eng.traineddata");
            String path = HContext.getFilesDir() + "/tessdata/";
            File f = new File(path);
            f.mkdirs();
            if (!dict.exists()) {
                copyAssetsSingleFile(f, "eng.traineddata");
            }
            TessBaseAPI baseApi = new TessBaseAPI();
            //记得要在你的sd卡的tessdata文件夹下放对应的字典文件,例如我这里就放的是custom.traineddata
            // baseApi.init(ActivityLoginJWTS.this.getAssets().)
            baseApi.init(f.getParent(), "eng");
            baseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "0123456789ABCDEF");
            baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_CHAR);
            int tryTime = 0;
            while (true) {
                tryTime++;
                Log.e("后台登录教务，尝试第", tryTime + "次");
                if (tryTime > 4) break;
                byte[] checkPic;
                try {
                    //第一次访问登录界面
                    Connection.Response response = Jsoup.connect("http://jwts.hitsz.edu.cn/").timeout(30000).execute();
                    //得到系统返回的Cookies
                    cookies.clear();
                    cookies.putAll(response.cookies());
                    //Log.e("cookie:",cookies.toString()+" ");
                    //请求获得验证码的内容
                    checkPic = Jsoup.connect("http://jwts.hitsz.edu.cn/captchaImage").cookies(cookies).ignoreContentType(true).execute().bodyAsBytes();
                    if (checkPic.length == 0 || cookies.size() == 0) continue;
                    Bitmap bm = BitmapFactory.decodeByteArray(checkPic, 0, checkPic.length);
                    Bitmap res = getProcessedBitmap(bm);
                    StringBuilder result = new StringBuilder();
                    for (Bitmap r : splitBitmapInto(res, 4, -6)) {
                        baseApi.setImage(r);
                        final String x = baseApi.getUTF8Text();
                        result.append(x);
                    }
                    String loginResult = loginCheck(username, password, result.toString());
                    if (loginResult.contains("成功")) {
                        HITAApplication.login = true;
                        break;
                    } else if (loginResult.startsWith("ALT:") && loginResult.contains("密码")) {
                        cookies.clear();
                        Message msg = ToastHander.obtainMessage();
                        Bundle b = new Bundle();
                        b.putString("msg", "密码错误，后台登录失败");
                        msg.setData(b);
                        ToastHander.sendMessage(msg);
                        HITAApplication.login = false;
                        break;
                    } else {
                        cookies.clear();
                        login = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
            baseApi.end();
            return !cookies.isEmpty();
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if(refreshLayout!=null) refreshLayout.setRefreshing(false);
            if ((Boolean) o) {
                Log.e("!", "登录成功");
                Intent i = new Intent();
                i.setAction("COM.STUPIDTREE.HITA.JWTS_AUTO_LOGIN_DONE");
                LocalBroadcastManager.getInstance(HContext).sendBroadcast(i);
            } else {
                cookies.clear();
                login = false;
                Intent i = new Intent();
                i.setAction("COM.STUPIDTREE.HITA.JWTS_LOGIN_FAIL");
                LocalBroadcastManager.getInstance(HContext).sendBroadcast(i);
            }
        }
    }

    public static boolean checkLogin() {
        try {
            Document userinfo = Jsoup.connect("http://jwts.hitsz.edu.cn/xswhxx/queryXswhxx").cookies(cookies).timeout(20000)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .ignoreContentType(true)
                    .get();
            if (userinfo.getElementsByTag("table").size() <= 0) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

}