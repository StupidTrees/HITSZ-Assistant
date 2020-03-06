package com.stupidtree.hita.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.TimeTablePagerAdapter;
import com.stupidtree.hita.diy.TimeTableBlockView;
import com.stupidtree.hita.timetable.Subject;
import com.stupidtree.hita.fragments.popup.FragmentAddEvent;
import com.stupidtree.hita.fragments.FragmentTimeTablePage;
import com.stupidtree.hita.timetable.timetable.HTime;
import com.stupidtree.hita.util.ActivityUtils;
import com.stupidtree.hita.util.ColorBox;

import java.lang.ref.WeakReference;
import java.util.Calendar;



import static com.stupidtree.hita.HITAApplication.TPE;
import static com.stupidtree.hita.HITAApplication.defaultSP;
import static com.stupidtree.hita.HITAApplication.now;
import static com.stupidtree.hita.HITAApplication.timeServiceBinder;
import static com.stupidtree.hita.HITAApplication.timeTableCore;

public class ActivityTimeTable extends BaseActivity implements TimeTableBlockView.TimeTablePreferenceRoot, FragmentAddEvent.OnFragmentInteractionListener {

    public static final String TIMETABLE_REFRESH = "COM.STUPIDTREE.HITA.TIMETABLE_ACTIVITY_REFRESH";
    int pageWeekOfTerm;
    boolean hasInit = false;
    boolean refreshOnResume;
    public static final int TIMETABLE_REQUEST_SETTING = 858;
    public static final int SETTING_RESULT_TIMETABLE = 865;

    public AppBarLayout mAppBarLayout;
    Toolbar mToolbar;
    LinearLayout invalidLayout;
    ViewPager viewPager;
    TimeTablePagerAdapter pagerAdapter;
    FloatingActionButton fab;//,fab_add;
    TabLayout tabs;
    RefreshTask pageTask;
    Menu popUpMenu;

    /*个性化参数*/
    private boolean wholeday;
    private boolean drawNowLine;
    private String titleGravity;
    private String cardBackground;
    private String titleColor;
    private String subTitleColor;
    private String iconColor;
    private boolean enableIcon;
    private boolean colorfulMode;
    private boolean enableAnim;
    private int bgOpacity;
    private int titleAlpha;
    private int subtitleAlpha;
    private boolean boldText;
    private int card_height;


    /*初始化_获取所有控件对象*/
    void initAllViews() {
        fab = findViewById(R.id.fab_thisweek);
        //fab_add = findViewById(R.id.fab_add);
        //toolbar_title = findViewById(R.id.toolbar_title);
        mAppBarLayout = findViewById(R.id.app_bar);
        invalidLayout = findViewById(R.id.tt_invalidview);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if (pageWeekOfTerm != timeTableCore.getThisWeekOfTerm() && timeTableCore.isThisTerm()) {
                        pageWeekOfTerm = timeTableCore.getThisWeekOfTerm();
                        viewPager.setCurrentItem(timeTableCore.getThisWeekOfTerm()-1);
                    }
            }
        });
//        fab_add.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                new FragmentAddEvent().show(getSupportFragmentManager(),"fae");
//            }
//        });
        initViewPager();
    }

    void initViewPager() {
        viewPager = findViewById(R.id.timetable_viewpager);
      //  viewPager.setOffscreenPageLimit(2);
        tabs = findViewById(R.id.timetable_tabs);
       // tabs.setTabTextColors(Color.parseColor("#55000000"),getColorPrimary());
        tabs.setTabIndicatorFullWidth(false);
        if (timeTableCore.isDataAvailable())
            pagerAdapter = new TimeTablePagerAdapter(this,getSupportFragmentManager(), timeTableCore.getCurrentCurriculum().getTotalWeeks(),this);
        else pagerAdapter = new TimeTablePagerAdapter(this,getSupportFragmentManager(), 0,this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                pageWeekOfTerm = i + 1;
                //toolbar_title.setText("第" + pageWeekOfTerm + "周");
                if (pageWeekOfTerm == timeTableCore.getThisWeekOfTerm()) {
                    fab.hide();
                    //toolbar_title.setTextColor(ContextCompat.getColor(ActivityTimeTable.this, R.color.theme1_colorPrimaryDark));
                } else if(timeTableCore.isThisTerm()){
                    if(defaultSP.getBoolean("timetable_back_enable",true))fab.show();
                    //toolbar_title.setTextColor(ContextCompat.getColor(ActivityTimeTable.this, R.color.material_primary_text));
            }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        tabs.setupWithViewPager(viewPager);
        //if(thisWeekOfTerm-1>=0) viewPager.setCurrentItem(thisWeekOfTerm-1);
    }

    void initReceiver(){
        BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Log.e("timetable的Activity接收到广播","进行刷新");
                try{
                    onCalledRefresh();
                    refreshOnResume = false;
                } catch (Exception e) {
                    refreshOnResume = true;
                }

            }
        };
        IntentFilter iF = new IntentFilter(TIMETABLE_REFRESH);
        LocalBroadcastManager.getInstance(this).registerReceiver(br,iF);
    }
    private void initToolBarAndDrawer() {
        mToolbar = findViewById(R.id.main_tool_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mToolbar.inflateMenu(R.menu.toolbar_time_table);
        mToolbar.setTitle("");
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                boolean isChecked = !menuItem.isChecked();
                switch (id) {
//                    case R.id.action_add_course:
//                       fragmentAddCourse.show(getSupportFragmentManager(),"fac");
//                        break;
//                    case R.id.action_switch_timetable:
//                        menuItem.setChecked(isChecked);
//                         PreferenceManager.getDefaultSharedPreferences(ActivityTimeTable.this).edit().putBoolean("timetable_curriculumonly",isChecked).apply();
//                        //Toast.makeText(ActivityTimeTable.this,"重新进入本页面生效",Toast.LENGTH_SHORT).show();
//                        //reFreshViewPager();
//                        pagerAdapter.notifyAllFragments();
//                        break;
                    case R.id.action_timetable_style:
                        ActivityUtils.startSettingFor(getThis(),"appearance");
                        break;
                    case R.id.action_curriculum_manager:
                        Intent i = new Intent(ActivityTimeTable.this,ActivityCurriculumManager.class);
                        startActivity(i);
                        break;
                    case R.id.action_whole_day:
                        menuItem.setChecked(isChecked);
                        defaultSP.edit().putBoolean("timetable_wholeday",isChecked).apply();
                        //Toast.makeText(ActivityTimeTable.this,"重新进入本页面生效",Toast.LENGTH_SHORT).show();
                        onCalledRefresh();
                        // Refresh(FROM_INIT);
                        break;

                    case R.id.action_colorful_mode:
                        menuItem.setChecked(isChecked);
                        defaultSP.edit().putBoolean("subjects_color_enable",isChecked).apply();
                        popUpMenu.findItem(R.id.action_reset_color).setVisible(isChecked);
                        //Toast.makeText(ActivityTimeTable.this,"重新进入本页面生效",Toast.LENGTH_SHORT).show();
                        onCalledRefresh();
                        // Refresh(FROM_INIT);
                        break;
                    case R.id.action_draw_now_line:
                        menuItem.setChecked(isChecked);
                        defaultSP.edit().putBoolean("timetable_draw_now_line",isChecked).apply();
                        //Toast.makeText(ActivityTimeTable.this,"重新进入本页面生效",Toast.LENGTH_SHORT).show();
                        onCalledRefresh();
                        // Refresh(FROM_INIT);
                        break;
                    case R.id.action_reset_color:
                        AlertDialog ad = new AlertDialog.Builder(ActivityTimeTable.this).setTitle(getString(R.string.dialog_title_random_allocate))
                                .setNegativeButton(getString(R.string.button_cancel),null).setPositiveButton(getString(R.string.button_confirm), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        new resetColorTask().executeOnExecutor(TPE);
                                    }
                                }).create();
                        ad.show();
                        break;
//                    case R.id.action_reset_color_to_theme:
//                        AlertDialog ad2 = new AlertDialog.Builder(ActivityTimeTable.this).setTitle(getString(R.string.dialog_title_set_to_theme))
//                                .setNegativeButton(getString(R.string.button_cancel),null).setPositiveButton(getString(R.string.button_confirm), new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        new resetColorToThemeTask().executeOnExecutor(HITAApplication.TPE);
//                                    }
//                                }).create();
//                        ad2.show();
//                        break;
                }
                return true;
            }
        });
//        setDrawerLeftEdgeSize(FragmentTimeTable.this.getActivity(),mDrawerLayout,0,false);


    }

    /*刷新课表视图函数*/
    public void Refresh(boolean backToThisWeek) {
        if(pageTask!=null&&pageTask.getStatus()!=AsyncTask.Status.FINISHED) pageTask.cancel(true);
        pageTask = new RefreshTask(backToThisWeek);
        pageTask.executeOnExecutor(HITAApplication.TPE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==TIMETABLE_REQUEST_SETTING&&requestCode == SETTING_RESULT_TIMETABLE){
            onCalledRefresh();
        }
    }

    void reFreshViewPager() {
        //curriculuManagerPagerAdapter.notifyAllFragments();
        viewPager.setCurrentItem(pageWeekOfTerm - 1);

    }
    private void syncAllPreferences(){
        cardBackground = defaultSP.getString("timetable_card_background","gradient");
        card_height = defaultSP.getInt("timetable_card_height", 160);//课程表卡片高度
        titleGravity = defaultSP.getString("timetable_card_title_gravity","top");
        bgOpacity = defaultSP.getInt("timetable_card_opacity",100);
        enableIcon = defaultSP.getBoolean("timetable_card_icon_enable",true);
        boldText = defaultSP.getBoolean("timetable_card_text_bold",false);
        titleColor = defaultSP.getString("timetable_card_title_color","white");
        subTitleColor = defaultSP.getString("timetable_card_subtitle_color","white");
        iconColor = defaultSP.getString("timetable_card_icon_color","white");
        colorfulMode = defaultSP.getBoolean("subjects_color_enable", false);
        wholeday = defaultSP.getBoolean("timetable_wholeday", false);
        enableAnim = defaultSP.getBoolean("timetable_animation_enable",true);
        drawNowLine = defaultSP.getBoolean("timetable_draw_now_line", true);
        titleAlpha = defaultSP.getInt("timetable_card_title_alpha",100);
        subtitleAlpha = defaultSP.getInt("timetable_card_subtitle_alpha",100);
    }



    @Override
    public void onCalledRefresh() {
        syncAllPreferences();
        pagerAdapter.notifyAllFragments();
    }


    class InitTask extends AsyncTask<String, Integer, String> {

        WeakReference<ActivityTimeTable> weakReference;
        InitTask(ActivityTimeTable at){
            weakReference = new WeakReference<>(at);
        }
        @Override
        protected String doInBackground(String... strings) {
            syncAllPreferences();
            if (timeTableCore.getThisWeekOfTerm() > 0) pageWeekOfTerm = timeTableCore.getThisWeekOfTerm();
            return null;
        }


        @Override
        protected void onPostExecute(String s) {
            ActivityTimeTable at = weakReference.get();
            if(at==null||at.isDestroyed()||at.isFinishing()) return;
            super.onPostExecute(s);
            initToolBarAndDrawer();
            hasInit = true;
            if(!timeTableCore.isThisTerm()||!defaultSP.getBoolean("timetable_back_enable",true)) fab.hide();
            Refresh(true);
        }
    }

    @SuppressLint("StaticFieldLeak")
    class RefreshTask extends AsyncTask<String, Integer, Integer> {

        boolean backToThisWeek;

        public RefreshTask(boolean backToThisWeek) {
            this.backToThisWeek = backToThisWeek;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(String... strings) {
            if (!hasInit) return -1;
            if (!timeTableCore.isDataAvailable()) {
                timeTableCore.setThisWeekOfTerm(-1);
                return -2;
            }
            /*刷新必备代码--刷新数据*/
            now.setTimeInMillis(System.currentTimeMillis());
            timeTableCore.setThisTerm(timeTableCore.getCurrentCurriculum().Within(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH)));
                try {
                   timeTableCore.setThisWeekOfTerm(timeTableCore.getCurrentCurriculum().getWeekOfTerm(now));
                    pageWeekOfTerm = timeTableCore.isThisTerm()? timeTableCore.getThisWeekOfTerm() : 1;
                } catch (Exception e) {
                   timeTableCore.setThisWeekOfTerm( -1);
                    pageWeekOfTerm = 1;
                }
            return 99;
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(Integer i) {
            if (i == -1) return;
            if (i == -2) {
                invalidLayout.setVisibility(View.VISIBLE);
                viewPager.setVisibility(View.GONE);
                timeTableCore.setThisWeekOfTerm(-1);
                return;
            } else {
                invalidLayout.setVisibility(View.GONE);
                viewPager.setVisibility(View.VISIBLE);
            }
            if (pageWeekOfTerm == timeTableCore.getThisWeekOfTerm()) {
                fab.hide();
                //toolbar_title.setTextColor(ContextCompat.getColor(ActivityTimeTable.this, R.color.theme1_colorPrimaryDark));
            } else if(timeTableCore.isThisTerm()){
                if(defaultSP.getBoolean("timetable_back_enable",true))fab.show();
                //toolbar_title.setTextColor(ContextCompat.getColor(ActivityTimeTable.this, R.color.material_primary_text));
            }
            if(backToThisWeek)reFreshViewPager();
            super.onPostExecute(i);
        }

    }


    @Override
    protected void stopTasks() {
        if(pageTask!=null&&pageTask.getStatus()!=AsyncTask.Status.FINISHED) pageTask.cancel(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true,true,false);
        setContentView(R.layout.activity_time_table);
        initReceiver();
        initAllViews();
        if (!timeTableCore.isDataAvailable()) {
            pageWeekOfTerm = 1;
        } else {
            pageWeekOfTerm = timeTableCore.getCurrentCurriculum().getWeekOfTerm(now);
            if (pageWeekOfTerm < 0) {
                Snackbar.make(fab.getRootView(),getString(R.string.snack_semester_notstarted),Snackbar.LENGTH_SHORT).show();
                //Toast.makeText(this, , Toast.LENGTH_SHORT).show();
                timeTableCore.setThisTerm(false);
                pageWeekOfTerm = 1;

            }
        }

        InitTask it = new InitTask(this);
        it.executeOnExecutor(HITAApplication.TPE);
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

    }
    @Override
    public boolean isColorEnabled() {
        return colorfulMode;
    }

    @Override
    public String getCardTitleColor() {
        return titleColor;
    }

    @Override
    public String getSubTitleColor() {
        return subTitleColor;
    }

    @Override
    public String getIconColor() {
        return iconColor;
    }

    @Override
    public boolean willBoldText() {
        return boldText;
    }

    @Override
    public boolean cardIconEnabled() {
        return enableIcon;
    }

    @Override
    public int getCardOpacity() {
        return bgOpacity;
    }

    @Override
    public int getCardHeight() {
        return card_height;
    }

    @Override
    public HTime getStartTime(){
        return new HTime(wholeday?0:8, 0);
    }

    @Override
    public int getTodayBGColor() {
        return getBGIconColor();
    }

    @Override
    public int getTitleGravity() {
        if(titleGravity.equals("top")) return Gravity.TOP|Gravity.CENTER_HORIZONTAL;
        else if(titleGravity.equals("center")) return Gravity.CENTER;
        else return Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL;
    }

    @Override
    public int getTitleAlpha() {
        return titleAlpha;
    }

    @Override
    public int getSubtitleAlpha() {
        return subtitleAlpha;
    }

    @Override
    public boolean isWholeDay() {
        return wholeday;
    }

    @Override
    public boolean animEnabled() {
        return enableAnim;
    }


    @Override
    public String getCardBackground() {
        return cardBackground;
    }

    @Override
    public boolean drawNowLine() {
        return drawNowLine;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_time_table,menu);
//        MenuItem item = (MenuItem) menu.findItem(R.id.action_switch_timetable);
//        item.setActionView(R.layout.util_dynamictimetable_toolbar_actionlayout);
//        final Switch switchA = item
//                .getActionView().findViewById(R.id.action_layout_switch);
//        switchA.setChecked(!defaultSP.getBoolean("curriculumsonly",true));
//        switchA.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
//                PreferenceManager.getDefaultSharedPreferences(ActivityTimeTable.this).edit().putBoolean("curriculumsonly",!isChecked).apply();
//                Toast.makeText(ActivityTimeTable.this,"重新进入本页面生效",Toast.LENGTH_SHORT).show();
//                //Refresh(FROM_INIT);
//            }
//        });
       // menu.findItem(R.id.action_switch_timetable).setChecked(defaultSP.getBoolean("timetable_curriculumonly",false));
        boolean colorEnabled = defaultSP.getBoolean("subjects_color_enable",false);
        menu.findItem(R.id.action_whole_day).setChecked(defaultSP.getBoolean("timetable_wholeday",false));
        menu.findItem(R.id.action_colorful_mode).setChecked(colorEnabled);
        menu.findItem(R.id.action_draw_now_line).setChecked(defaultSP.getBoolean("timetable_draw_now_line",true));
        menu.findItem(R.id.action_reset_color).setVisible(colorEnabled);
        popUpMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    class resetColorTask extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] objects) {
            for(Subject s:timeTableCore.getCurrentCurriculum().getSubjects()){
                defaultSP.edit().putInt("color:"+s.getName(), ColorBox.getRandomColor_Material()).commit();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            pagerAdapter.notifyAllFragments();
            super.onPostExecute(o);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(refreshOnResume){
            onCalledRefresh();
            refreshOnResume = false;
        }
        Refresh(false);
    }
}
