package com.stupidtree.hita.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.MainPagerAdapter;
import com.stupidtree.hita.core.Curriculum;
import com.stupidtree.hita.fragments.FragmentAddEvent;
import com.stupidtree.hita.fragments.FragmentNavi;
import com.stupidtree.hita.fragments.FragmentTasks;
import com.stupidtree.hita.fragments.FragmentTimeLine;
import com.stupidtree.hita.fragments.FragmentTimeTablePage;
import com.stupidtree.hita.util.ActivityUtils;
import com.stupidtree.hita.util.FileOperator;
import com.tencent.bugly.beta.Beta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.*;
import static com.stupidtree.hita.util.UpdateManager.checkUpdate;

public class ActivityMain extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener
        , FragmentTimeLine.OnFragmentInteractionListener
        , FragmentAddEvent.OnFragmentInteractionListener
    , FragmentTimeTablePage.OnFragmentInteractionListener
    , FragmentNavi.OnFragmentInteractionListener
{


    FragmentTimeLine tlf;
    FragmentNavi nvf;
    FragmentTasks tskf;
    public FloatingActionButton fabmain;
    Toolbar mToolbar;
    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;
    ImageView drawerUserAvatar;
    TextView drawerUserName;
    FrameLayout drawerheader;
    ActionBarDrawerToggle mActionBarDrawerToggle;
    ViewPager mainPager;
    TabLayout mainTabs;
    MainPagerAdapter pagerAdapter;
    CardView drawer_card_profile,drawer_card_curriculummanager,drawer_card_theme,drawer_card_dynamic;
    ImageView drawer_bg;
    boolean isFirst;

    @Override
    protected void stopTasks() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true,true,false);
        isFirst =  defaultSP.getBoolean("firstOpen",true);
        tlf = FragmentTimeLine.newInstance(isFirst);
        nvf = new FragmentNavi();
        tskf = new FragmentTasks();
        checkAPPPermission();
        setContentView(R.layout.activity_main_drawer);
        fabmain = findViewById(R.id.fab_main);
        fabmain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // presentActivity(ActivityMain.this,v);
                Intent i = new Intent(ActivityMain.this,ActivityTimeTable.class);
                startActivity(i);
            }
        });

        if(defaultSP.getBoolean("autoCheckUpdate",true)) checkUpdate(this);
        initDrawer();
        initToolBar();
        initPager();
        if(isFirst) {
            try {
                Guide();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }


    void initToolBar(){
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //1.决定显示.
        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close); //2.传入Toolbar可以点击.
        mActionBarDrawerToggle.setDrawerSlideAnimationEnabled(true);
        mActionBarDrawerToggle.syncState();
        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle); //3.监听变化.
        getSupportActionBar().setTitle("");
        mToolbar.inflateMenu(R.menu.toolbar_main);
        mToolbar.setBackgroundColor(ContextCompat.getColor(this,R.color.material_backgroung_grey_50));
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
               if(menuItem.getItemId()==R.id.action_chatbot){
                   WindowManager manager =  getWindowManager();
                   DisplayMetrics outMetrics = new DisplayMetrics();
                   manager.getDefaultDisplay().getMetrics(outMetrics);
                   int width = outMetrics.widthPixels;
                   presentActivity(ActivityMain.this,width,0);
               }
               return true;
            }
        });
    }
    void initPager(){
        mainPager = findViewById(R.id.mainPager);
        mainTabs = findViewById(R.id.mainTabs);
        ArrayList fragments = new ArrayList();
        fragments.add(nvf);
        fragments.add(tlf);
        fragments.add(tskf);
        mainTabs.setSelectedTabIndicatorColor(Color.parseColor("#00000000"));
        pagerAdapter = new MainPagerAdapter(getSupportFragmentManager(),fragments,new String[]{"校园","今日","任务"});
        mainTabs.setupWithViewPager(mainPager);

        mainPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                if(i!=1) fabmain.hide();
                else fabmain.show();
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        mainPager.setAdapter(pagerAdapter);
        mainPager.setCurrentItem(1);
    }

    public void presentActivity(Activity activity, View view) {
        int revealX,revealY;
        if(view==null){
            revealX= 0;
            revealY = 0;
        }else{
            revealX = (int) (view.getX() + view.getWidth() / 2);
            revealY = (int) (view.getY() + view.getHeight() / 2);
        }
        Intent intent = new Intent(this, ActivityChatbot.class);
        intent.putExtra(ActivityJWTS.EXTRA_CIRCULAR_REVEAL_X, revealX);
        intent.putExtra(ActivityJWTS.EXTRA_CIRCULAR_REVEAL_Y, revealY);

        if(view!=null)view.setVisibility(View.VISIBLE);
        ActivityCompat.startActivity(activity, intent,null);
        overridePendingTransition(0, 0);
    }
    public void presentActivity(Activity activity, int revealX,int revealY) {
        Intent intent = new Intent(this, ActivityChatbot.class);
        intent.putExtra(ActivityJWTS.EXTRA_CIRCULAR_REVEAL_X, revealX);
        intent.putExtra(ActivityJWTS.EXTRA_CIRCULAR_REVEAL_Y, revealY);
        ActivityCompat.startActivity(activity, intent,null);
        overridePendingTransition(0, 0);
    }


    public void Guide() throws Exception{
        new TapTargetSequence(this)
                .targets(
                 TapTarget.forView(mainPager,"欢迎使用HITSZ助手","以下是使用导航")
                        .drawShadow(true)
                        .cancelable(false)
                        .tintTarget(true)
                        .transparentTarget(false)
                        .outerCircleColor(R.color.blue_accent)
                        .titleTextSize(24)
                         .id(1)
                        .icon(getDrawable(R.drawable.ic_navigation))
                      ,
                 TapTarget.forView(mainTabs,"左右滑动切换主视图","默认显示今日日程")
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(true)
                                .transparentTarget(false)
                                .outerCircleColor(R.color.blue_accent)
                                .titleTextSize(24),

                 TapTarget.forToolbarNavigationIcon(mToolbar,"点击展开抽屉\n所有功能都藏在这","教务系统、更换主题、课表管理")
                                        .drawShadow(true)
                                        .cancelable(false)
                                        .tintTarget(true)
                                         .titleTextSize(18)
                                        .id(13)
                                        .transparentTarget(true)
                                        .targetCircleColor(R.color.white)
                                        .outerCircleColor(R.color.blue_accent)
                                ,
                TapTarget.forView(drawerUserAvatar,"点击进入个人中心","管理科目与个人资料")
                        .drawShadow(true)
                        .cancelable(false)
                        .tintTarget(true)
                        .id(14)
                        .titleTextSize(18)
                        .transparentTarget(true)
                        .targetCircleColor(R.color.white)
                        .outerCircleColor(R.color.blue_accent),

                        TapTarget.forView(fabmain,"点击进入时间表","课程表Plus")
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(false)
                                .transparentTarget(false)
                                .targetCircleColor(R.color.white)
                                .outerCircleColor(R.color.blue_accent),
                        TapTarget.forToolbarMenuItem(mToolbar,R.id.action_chatbot,"点击唤起希塔","哈工深专属问答系统")
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(false)
                                .transparentTarget(false)
                                .targetCircleColor(R.color.white)
                                .outerCircleColor(R.color.amber_primary)
                ).listener(new TapTargetSequence.Listener() {
            @Override
            public void onSequenceFinish() {
                tlf.continueToGuide();

            }

            @Override
            public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {;
            if(lastTarget.id()==14) tlf.showHeadCard();
            if(lastTarget.id()==13) mDrawerLayout.openDrawer(Gravity.LEFT);
            if(lastTarget.id()==14) mDrawerLayout.closeDrawer(Gravity.LEFT);
            }

            @Override
            public void onSequenceCanceled(TapTarget lastTarget) {

            }
        }).start();


    }

    public void continueToGuide(){
        TapTargetView.showFor(this,TapTarget.forView(mainPager,"登录HITSZ助手","开启精彩体验吧")
                .drawShadow(true)
                .cancelable(false)
                .tintTarget(true)
                .titleTextSize(24)
                .descriptionTextSize(18)
                .transparentTarget(false)
                .targetCircleColor(R.color.white)
                .outerCircleColor(R.color.amber_primary)
                .icon(getDrawable(R.drawable.bt_guide_done)),new TapTargetView.Listener(){
            @Override
            public void onTargetClick(TapTargetView view) {
                super.onTargetClick(view);
                Intent i = new Intent(ActivityMain.this, ActivityLogin.class);
                startActivity(i);
                defaultSP.edit().putBoolean("firstOpen",false).commit();
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
        saveData(this);
    }



    public static void saveData(Activity context) {
        new SaveDataTask(context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }






    public void checkAPPPermission(){
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
        drawerUserName = headview.findViewById(R.id.main_drawer_user_name);
        drawer_card_curriculummanager = headview.findViewById(R.id.drawer_card_curriculummanager);
        drawer_card_profile = headview.findViewById(R.id.drawer_card_profile);
        drawer_card_theme = headview.findViewById(R.id.drawer_card_theme);
        drawer_card_dynamic = headview.findViewById(R.id.drawer_card_dynamic);
        drawerUserAvatar.setOnClickListener(new onUserAvatarClickListener());
        drawer_bg = headview.findViewById(R.id.drawer_bg);
        drawerheader = headview.findViewById(R.id.drawer_header);
        drawerheader.setOnClickListener(new onUserAvatarClickListener());
        drawer_card_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pp = new Intent(ActivityMain.this,ActivitySubjectManager.class);
                ActivityMain.this.startActivity(pp);
            }
        });
        drawer_card_dynamic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pp = new Intent(ActivityMain.this,ActivityDynamicTable.class);
                ActivityMain.this.startActivity(pp);
            }
        });

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
                Intent p = new Intent(ActivityMain.this,ActivityTheme.class);
                ActivityMain.this.startActivity(p);
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
                        Intent d = new Intent(ActivityMain.this,ActivityAboutHITA.class);
                        ActivityMain.this.startActivity(d);
                        break;
//                    case R.id.drawer_nav_user:
//                        drawerUserAvatar.callOnClick();
//                        break;
//                    case R.id.drawer_nav_theme:
//                        Intent p = new Intent(ActivityMain.this,ActivityTheme.class);
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
                    case R.id.drawer_nav_campusservice:
                        Intent g = new Intent(ActivityMain.this, ActivityRankBoard.class);
                        startActivity(g);
                        break;
                    case R.id.drawer_nav_laf:
                        Intent gg = new Intent(HContext, ActivityLostAndFound.class);
                        startActivity(gg);
                        break;
                    case R.id.drawer_nav_jwts:
                        ActivityUtils.startJWTSActivity(ActivityMain.this);
                       break;


                }
                return false;
            }
        });
    }

    protected  void refreshDrawerHeader(){
        if(CurrentUser==null){
            drawerUserAvatar.setImageResource(R.drawable.ic_account);
            //drawer_bg.setImageResource(R.drawable.timeline_head_bg);
            drawerUserName.setText("登录");
        }else{
            if(TextUtils.isEmpty(CurrentUser.getAvatarUri())){
                drawerUserAvatar.setImageResource(R.drawable.ic_account_activated);
               // drawer_bg.setImageResource(R.drawable.timeline_head_bg);
            }else{
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
        getMenuInflater().inflate(R.menu.toolbar_main,menu);
            return super.onCreateOptionsMenu(menu);
    }


    @Override
    protected void onResume() {
        super.onResume();
        //Glide.with(ActivityMain.this).load(userInfos.get("头像")).into(drawerUserAvatar);
        //if(tlf.hasInit) tlf.Refresh(FragmentTimeLine.TL_REFRESH_FROM_UNHIDE);
        refreshDrawerHeader();
    }




    class onUserAvatarClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if(CurrentUser==null){
                Intent i = new Intent(ActivityMain.this,ActivityLogin.class);
                startActivity(i);
            }else{
                ActivityOptions option = ActivityOptions.makeSceneTransitionAnimation(ActivityMain.this,drawerUserAvatar,"useravatar");
                Intent i = new Intent(ActivityMain.this,ActivityUserCenter.class);
                ActivityMain.this.startActivity(i,option.toBundle());
            }
        }
    }


    static class SaveDataTask extends AsyncTask{

        Activity context;
        SaveDataTask(Activity context){
            this.context = context;
        }
        @Override
        protected Object doInBackground(Object[] objects) {
            FileOperator.verifyStoragePermissions(context);
            SQLiteDatabase sd = mDBHelper.getWritableDatabase();
            for(Curriculum c:allCurriculum){
                if(sd.update("curriculum",c.getContentValues(),"curriculum_code=?",new String[]{c.curriculumCode})==0){
                    sd.insert("curriculum",null,c.getContentValues());
                }
            }

            return saveDataToCloud(false);
        }
    }



    public static void showUpdateDialog(final Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("有可用更新")
                .setMessage("新版本："+Beta.getUpgradeInfo().versionName+"\n"+"发布时间："+new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss").format(Beta.getUpgradeInfo().publishTime))
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
        if(mDrawerLayout.isDrawerOpen(Gravity.LEFT)) mDrawerLayout.closeDrawers();
        else { //返回桌面而不是退出
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
        }
    }
}