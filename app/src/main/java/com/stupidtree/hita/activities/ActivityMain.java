package com.stupidtree.hita.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.MainPagerAdapter;
import com.stupidtree.hita.fragments.main.FragmentTimeLine;
import com.stupidtree.hita.fragments.popup.FragmentTheme;
import com.stupidtree.hita.eas.JWException;
import com.stupidtree.hita.online.errorTableText;
import com.stupidtree.hita.timetable.packable.Curriculum;
import com.stupidtree.hita.util.ActivityUtils;
import com.stupidtree.hita.util.FileOperator;
import com.stupidtree.hita.views.mBottomHideBehavior;
import com.tencent.bugly.beta.Beta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.defaultSP;
import static com.stupidtree.hita.HITAApplication.jwCore;
import static com.stupidtree.hita.HITAApplication.themeCore;
import static com.stupidtree.hita.HITAApplication.timeTableCore;
import static com.stupidtree.hita.util.UpdateManager.checkUpdate;

public class ActivityMain extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener
        , FragmentTimeLine.MainFABController {

    public static final String MAIN_RECREATE = "COM.STUPIDTREE.HITA.MAIN_ACTIVITY_RECREATE";
    ExtendedFloatingActionButton fabMain;
    mBottomHideBehavior<FrameLayout> fabBehavior;
    Toolbar mToolbar;
    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;
    ImageView drawerUserAvatar;
    TextView drawerUserName, drawerSignature;
    ActionBarDrawerToggle mActionBarDrawerToggle;
    ViewPager mainPager;
    TabLayout mainTabs;
    MainPagerAdapter pagerAdapter;
    CardView drawer_card_profile, drawer_card_curriculummanager, drawer_card_theme, drawer_card_dynamic;
    CardView avatar_card;
    LinearLayout drawer_header;
    Switch dark_mode_switch;
    LinearLayout settingsMenu, darkModeMenu;
    boolean isFirst;
    boolean willRecreateOnResume = false;

    View.OnClickListener click_timetable, click_search;


    public static void showUpdateDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.update_dialog_title))
                .setMessage(context.getString(R.string.update_dialog_mes1) + Beta.getUpgradeInfo().versionName + "\n" + context.getString(R.string.update_dialog_mes2) + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Beta.getUpgradeInfo().publishTime))
                .setCancelable(false)
                .setPositiveButton(context.getString(R.string.button_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent it = new Intent(context, ActivityAboutHITA.class);
                        context.startActivity(it);
                    }
                })
                .setNegativeButton(context.getString(R.string.button_cancel), null);
        builder.create();
        builder.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true, true, false);
        isFirst = defaultSP.getBoolean("firstOpen", true);
        willRecreateOnResume = false;
        checkAPPPermission();
        initBroadcast();
        setContentView(R.layout.activity_main);
        fabMain = findViewById(R.id.fab_main);
        try {
            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) ((FrameLayout) fabMain.getParent()).getLayoutParams();
            fabBehavior = (mBottomHideBehavior<FrameLayout>) lp.getBehavior();
        } catch (Exception e) {
            fabBehavior = null;
        }
        click_timetable = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timeTableCore.isDataAvailable()) {
                    Intent i = new Intent(ActivityMain.this, ActivityTimeTable.class);
                    startActivity(i);
                } else {
                    Snackbar.make(fabMain, getString(R.string.notif_importdatafirst), Snackbar.LENGTH_SHORT).show();
                }
            }
        };
        click_search = new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Intent i = new Intent(getThis(), ActivitySearch.class);
                startActivity(i);
            }
        };
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
        } else if (!defaultSP.getBoolean("update20200420_guide", false)) {
            UpdateGuide20200420();
        }
    }

    public static void autoLogin() {

        if (!defaultSP.getBoolean("jwts_autologin", true)) return;
        if (CurrentUser != null) {
            String stun = CurrentUser.getStudentnumber();
            String password = null;
            String cookie = defaultSP.getString("jw_cookie", null);
            if (cookie != null) {
                //Log.e("JW_LOGIN","有本地cookie，进行载入");
                Gson gson = new Gson();
                jwCore.loadCookies(gson.fromJson(cookie, jwCore.getCookies().getClass()));
            }
            if (!TextUtils.isEmpty(stun)) password = defaultSP.getString(stun + ".password", null);
            if (password != null) {
                new loginJWTSInBackgroundTask(stun, password).executeOnExecutor(HITAApplication.TPE);
            }
        }


    }

    void initBroadcast() {
        BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() != null && intent.getAction().equals(MAIN_RECREATE)) {
                    try {
                        recreate();
                        willRecreateOnResume = false;
                    } catch (Exception e) {
                        willRecreateOnResume = true;
                    }


                }
            }
        };
        IntentFilter iF = new IntentFilter();
        iF.addAction(MAIN_RECREATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(br, iF);
    }

    void initToolBar() {
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true); //1.决定显示.
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

    @SuppressLint("ResourceType")
    void initPager() {
        mainPager = findViewById(R.id.mainPager);
        mainTabs = findViewById(R.id.mainTabs);
        pagerAdapter = new MainPagerAdapter(getSupportFragmentManager(), new String[]{getString(R.string.maintab_navi), getString(R.string.maintab_today), getString(R.string.maintab_events)});
        mainTabs.setupWithViewPager(mainPager);
        fabMain.setBackgroundTintList(ColorStateList.valueOf(getColorAccent()));
        fabMain.setHideMotionSpecResource(R.anim.fab_scale_hide);
        fabMain.setShowMotionSpecResource(R.anim.fab_scale_show);
        mainPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                switch (i) {
                    case 0:
                        fabMain.setIcon(getDrawable(R.drawable.ic_search_solid));
                        fabMain.extend();
                        fabShow();
                        if (fabBehavior != null) fabBehavior.setFabSlideEnable(true);
                        // fabBehavior.setSlideEnable(true);
                        fabMain.setOnClickListener(click_search);
                        break;
                    case 1:
                        if (fabBehavior != null) {
                            fabBehavior.setFabSlideEnable(false);
                            fabRise();
                        }
                        if (timelineIsHeaderExpanded()) {
                            fabHide();
                        } else {
                            fabShow();
                        }
                        fabMain.setIcon(getDrawable(R.drawable.ic_menu_timetable));
                        fabMain.shrink();
                        fabMain.setOnClickListener(click_timetable);
                        break;

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
        intent.putExtra("anim", true);
        ActivityCompat.startActivity(activity, intent, null);
        activity.overridePendingTransition(0, 0);
    }

    private void Guide() {
        new TapTargetSequence(this)
                .targets(
                        TapTarget.forView(mainPager, getString(R.string.guide_1p), getString(R.string.guide_1s))
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(true)
                                .transparentTarget(false)
                                .outerCircleColorInt(getColorAccent())
                                .titleTextSize(24)
                                .cancelable(true)
                                .id(1)
                                .icon(getDrawable(R.drawable.ic_navigation))
                        ,
                        TapTarget.forToolbarNavigationIcon(mToolbar, getString(R.string.guide_3p), getString(R.string.guide_3s))
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(true)
                                .titleTextSize(18)
                                .id(13)
                                .transparentTarget(true)
                                .targetCircleColor(R.color.white)
                                .outerCircleColorInt(getColorAccent())
                        ,
//                        TapTarget.forView(drawerUserAvatar, getString(R.string.guide_4p), getString(R.string.guide_4s))
//                                .drawShadow(true)
//                                .cancelable(false)
//                                .tintTarget(true)
//                                .id(14)
//                                .titleTextSize(18)
//                                .transparentTarget(true)
//                                .targetCircleColor(R.color.white)
//                                .outerCircleColor(R.color.cruelsummer_accent),
                        TapTarget.forView(mainPager, getString(R.string.guid_7p), getString(R.string.guid_7s))
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(true)
                                .icon(getDrawable(R.drawable.ic_arrow_downward))
                                .transparentTarget(false)
                                .targetCircleColor(R.color.white)
                                .outerCircleColorInt(getColorAccent()),
                        TapTarget.forView(fabMain, getString(R.string.guide_5p), getString(R.string.guide_5s))
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(false)
                                .transparentTarget(false)
                                .targetCircleColor(R.color.white)
                                .outerCircleColorInt(getColorAccent()),
                        TapTarget.forToolbarMenuItem(mToolbar, R.id.action_search, getString(R.string.guide_6p), getString(R.string.guide_6s))
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(false)
                                .transparentTarget(false)
                                .targetCircleColor(R.color.white)
                                .outerCircleColorInt(getColorAccent()),
                        TapTarget.forView(mainPager, getString(R.string.guide_7p), getString(R.string.guide_7s))
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(true)
                                .titleTextSize(24)
                                .descriptionTextSize(18)
                                .transparentTarget(false)
                                .targetCircleColor(R.color.white)
                                .outerCircleColorInt(getColorAccent())
                                .icon(getDrawable(R.drawable.bt_guide_done))
                ).listener(new TapTargetSequence.Listener() {
            @Override
            public void onSequenceFinish() {
                Intent i = new Intent(ActivityMain.this, ActivityLogin.class);
                startActivity(i);
                defaultSP.edit().putBoolean("firstOpen", false).apply();
                defaultSP.edit().putBoolean("update20200420_guide", true).apply();
                isFirst = false;
            }

            @Override
            public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
//                if (lastTarget.id() == 13) mDrawerLayout.openDrawer(Gravity.LEFT);
//                if (lastTarget.id() == 14) mDrawerLayout.closeDrawer(Gravity.LEFT);
            }

            @Override
            public void onSequenceCanceled(TapTarget lastTarget) {
                defaultSP.edit().putBoolean("firstOpen", false).apply();
                defaultSP.edit().putBoolean("update20200420_guide", true).apply();
            }
        }).start();


    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }


//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if(defaultSP.getBoolean("auto_upload_user_data",true)) saveData();
//    }


    public static void saveData() {
        new SaveDataTask().executeOnExecutor(HITAApplication.TPE);
    }

    private void UpdateGuide20200420() {

        TapTarget tapTarget = TapTarget.forView(mainPager, getString(R.string.guid_7p), getString(R.string.guid_7s))
                .drawShadow(true)
                .cancelable(false)
                .tintTarget(true)
                .icon(getDrawable(R.drawable.ic_arrow_downward))
                .transparentTarget(false)
                .targetCircleColor(R.color.white)
                .outerCircleColorInt(getColorAccent());
        TapTargetView.showFor(this, tapTarget, new TapTargetView.Listener() {
            @Override
            public void onTargetClick(TapTargetView view) {
                super.onTargetClick(view);
                defaultSP.edit().putBoolean("update20200420_guide", true).apply();
            }
        });
        //new TapTargetView(this,getWindowManager(),null,tapTarget,null);
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
            String[] permissions = permissionList.toArray(new String[0]);
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
        drawer_header = headview.findViewById(R.id.drawer_header);
        settingsMenu = findViewById(R.id.setting);
        darkModeMenu = findViewById(R.id.dark_mode);
        dark_mode_switch = findViewById(R.id.switch_darkmode);
        settingsMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.startSettingFor(getThis(), "basic");
            }
        });
//        drawerheader = headview.findViewById(R.id.drawer_header);
//        drawerheader.setOnClickListener(new onUserAvatarClickListener());
//        Menu menu = mNavigationView.getMenu();
//        dark_mode_menu = menu.findItem(R.id.drawer_nav_darkmode);
//        dark_mode_menu.setActionView(R.layout.action_switch_darkmode);
//        Switch switchA = dark_mode_menu.getActionView().findViewById(R.id.switch_darkmode);
        dark_mode_switch.setChecked(themeCore.isDarkModeOn());
        dark_mode_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                themeCore.switchDarkMode(getThis(), isChecked);
            }
        });
        darkModeMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dark_mode_switch.toggle();
            }
        });
        drawer_card_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                avatar_card.callOnClick();
//                Intent pp = new Intent(ActivityMain.this, ActivityChatbot.class);
//                ActivityMain.this.startActivity(pp);
            }
        });
        drawer_header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer_card_profile.callOnClick();
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
                    case R.id.drawer_nav_hita:
                        Intent h = new Intent(ActivityMain.this, ActivityChatbot.class);
                        ActivityMain.this.startActivity(h);
                        break;
                    case R.id.drawer_nav_attitude:
                        ActivityUtils.startAttitudeActivity(getThis());
                        break;
                    case R.id.drawer_nav_community:
                        ActivityUtils.startCommunityActivity(getThis());
                        break;
                    case R.id.drawer_nav_info:
                        Intent ppp = new Intent(ActivityMain.this, ActivityNews.class);
                        startActivity(ppp);
                        break;
                    case R.id.drawer_nav_search:
                        mDrawerLayout.closeDrawer(GravityCompat.START);
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
                            Toast.makeText(HContext, getString(R.string.notif_installQQ), Toast.LENGTH_SHORT).show();
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
            drawerUserName.setText(getString(R.string.drawer_username_null));
            drawerSignature.setText(getString(R.string.drawer_signature_null));
        } else {
            Glide.with(ActivityMain.this)
                    .load(CurrentUser.getAvatarUri())
                    .placeholder(R.drawable.ic_account_activated)
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

            drawerUserName.setText(CurrentUser.getNick());
            drawerSignature.setText(TextUtils.isEmpty(CurrentUser.getSignature()) ? getString(R.string.drawer_signature_none) : CurrentUser.getSignature());
        }

        if (!themeCore.isCurrentDarkTheme()
                && defaultSP.getString("dark_mode_mode", "dark_mode_normal").equals("dark_mode_normal")) {
            darkModeMenu.setVisibility(View.VISIBLE);
            darkModeMenu.setClickable(true);
        } else {
            darkModeMenu.setVisibility(View.GONE);
            darkModeMenu.setClickable(false);
            //gdark_mode_menu.setVisible(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (willRecreateOnResume) {
            willRecreateOnResume = false;
            recreate();
        }
        refreshDrawerHeader();
        try {
            if (!jwCore.hasLogin()) autoLogin();
        } catch (Exception e) {
            e.printStackTrace();
            new errorTableText("自动登录错误", e).save(new SaveListener<String>() {
                @Override
                public void done(String s, BmobException e) {

                }
            });
        }
        //Glide.with(ActivityMain.this).load(userInfos.get("头像")).into(drawerUserAvatar);
        //if(tlf.hasInit) tlf.Refresh(FragmentTimeLine.TL_REFRESH_FROM_UNHIDE);

    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //4.同步状态
        mActionBarDrawerToggle.syncState();
    }

    @Override
    public void fabRise() {
        if (fabBehavior != null && fabMain != null) {
            fabBehavior.slideUp((FrameLayout) fabMain.getParent());
        }

    }

    @Override
    public void fabHide() {
        if (fabMain != null) fabMain.hide();
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

    @Override
    public void fabShow() {
        if (fabMain != null) fabMain.show();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) mDrawerLayout.closeDrawers();
        else if (!timelineCloseHeader()) { //返回桌面而不是退出
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
        }
    }

    boolean timelineIsHeaderExpanded() {
        for (Fragment f : getSupportFragmentManager().getFragments()) {
            if (f instanceof FragmentTimeLine) {
                FragmentTimeLine tlf = (FragmentTimeLine) f;
                return tlf.isHeaderExpanded();
            }
        }
        return false;
    }

    boolean timelineCloseHeader() {
        for (Fragment f : getSupportFragmentManager().getFragments()) {
            if (f instanceof FragmentTimeLine) {
                FragmentTimeLine tlf = (FragmentTimeLine) f;
                if (tlf.isResumed() && tlf.isHeaderExpanded()) {
                    tlf.closeHeader();
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    static class SaveDataTask extends AsyncTask<Object, Integer, Boolean> {


        @Override
        protected Boolean doInBackground(Object... objects) {
            try {
                for (Curriculum c : timeTableCore.getAllCurriculum()) {
                    timeTableCore.saveCurriculum(c);
                }
                return timeTableCore.saveDataToCloud();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    public static class loginJWTSInBackgroundTask extends AsyncTask<String, Integer, Object> {

        String username, password;

        loginJWTSInBackgroundTask(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected Object doInBackground(String... strings) {
            try {
                if (!jwCore.loginCheck()) {
                    Log.e("JW_LOGIN", "无登录状态保持，开始重新请求");
                    return jwCore.login(username, password);
                }
                Log.e("JW_LOGIN", "登录状态保持，无需重新请求");
                return true;
            } catch (JWException e) {
                return e;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected void onPostExecute(Object o) {
            if (o instanceof JWException) {
                Intent i = new Intent();
                i.setAction("COM.STUPIDTREE.HITA.JWTS_LOGIN_FAIL");
                LocalBroadcastManager.getInstance(HContext).sendBroadcast(i);
            } else if (o instanceof Boolean) {
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
}