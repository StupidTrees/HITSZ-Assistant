package com.stupidtree.hita;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;

import com.stupidtree.hita.timetable.HITADBHelper;
import com.stupidtree.hita.timetable.TimetableCore;
import com.stupidtree.hita.jw.JWCore;
import com.stupidtree.hita.online.HITAUser;
import com.stupidtree.hita.hita.ChatBotMessageItem;
import com.stupidtree.hita.activities.ActivityMain;
import com.stupidtree.hita.util.mUpgradeListener;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.beta.upgrade.UpgradeStateListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobUser;

/**
 * 全局Application类，生命周期和整个应用相同
 */
public class HITAApplication extends Application {

    //一个全局的Context变量
    public static Context HContext;
    public static TimeWatcher timeWatcher;
    public static int themeID;
    public static Calendar now;
    public static HITADBHelper mDBHelper;
    public static JWCore jwCore;
    public static TimetableCore timeTableCore;
    public static HashMap<String, String> cookies_ut = new HashMap<>();
    public static HashMap<String,String> cookies_ut_card = new HashMap<>();
    public static String ut_username;
    public static boolean login_ut = false;
    public static SharedPreferences defaultSP;
    public static List<ChatBotMessageItem> ChatBotListRes;//聊天机器人的聊天记录
    public static List<BmobObject> SearchResultList;
    public static String searchText ="";
    public static HITAUser CurrentUser = null;

    public static Handler ToastHander;
    public static ThreadPoolExecutor TPE;


    @SuppressLint("HandlerLeak")
    @Override
    public void onCreate() {
        super.onCreate();
        now = Calendar.getInstance();
        TPE = new ThreadPoolExecutor(0,Integer.MAX_VALUE,60L, TimeUnit.SECONDS,new SynchronousQueue<Runnable>());
        HContext = getApplicationContext();
        mDBHelper = new HITADBHelper(HContext);
        defaultSP = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this);
        ChatBotListRes = new ArrayList<>();
        SearchResultList = new ArrayList<>();
        jwCore = new JWCore();
        timeTableCore = new TimetableCore();
        timeWatcher = new TimeWatcher(this);

        initUpgradeDialog();
        new InitTask(this).executeOnExecutor(HITAApplication.TPE);
        ToastHander = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Toast.makeText(HContext,msg.getData().getString("msg"),Toast.LENGTH_LONG).show();
            }
        };

        Bugly.init(this, "7c0e87536a", false);//务必最后再init
        Bmob.initialize(this, "9c9c53cd53b3c7f02c37b7a3e6fd9145");
        CurrentUser = BmobUser.getCurrentUser(HITAUser.class);
        getThemeID();
    }




    private void initUpgradeDialog() {
        Beta.autoInit = true;
        /**
         * 自定义初始化开关
         */
        /**
         * true表示初始化时自动检查升级; false表示不会自动检查升级,需要手动调用Beta.checkUpgrade()方法;
         */
        Beta.autoCheckUpgrade = false;

        /**
         * 设置升级检查周期为60s(默认检查周期为0s)，60s内SDK不重复向后台请求策略);
         */
//        Beta.upgradeCheckPeriod = 60 * 1000;
        /**
         * 设置启动延时为1s（默认延时3s），APP启动1s后初始化SDK，避免影响APP启动速度;
         */
        Beta.initDelay = 3 * 1000;
        /**
         * 设置通知栏大图标，largeIconId为项目中的图片资源;
         */
        Beta.largeIconId = R.mipmap.ic_launcher_round;
        /**
         * 设置状态栏小图标，smallIconId为项目中的图片资源Id;
         */
        Beta.smallIconId = R.mipmap.ic_launcher_round;
        //Beta.defaultBannerId = R.mipmap.ic_launcher_round;
        Beta.storageDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        Beta.showInterruptedStrategy = true;
        Beta.canShowUpgradeActs.add(ActivityMain.class);

        /**
         * 设置Wifi下自动下载
         */
        Beta.autoDownloadOnWifi = defaultSP.getBoolean("autoDownloadInWifi", true);


        /*在application中初始化时设置监听，监听策略的收取*/
        Beta.upgradeListener = new mUpgradeListener();

        /* 设置更新状态回调接口 */
        Beta.upgradeStateListener = new UpgradeStateListener() {
            @Override
            public void onUpgradeFailed(boolean isManual) {
                Intent intent = new Intent();
                intent.setAction("com.stupidtree.hita.upgrade_failed");
                sendBroadcast(intent);
                    }

            @Override
            public void onUpgradeSuccess(boolean b) {
                Intent intent = new Intent();
                intent.setAction("com.stupidtree.hita.upgrade_success");
                sendBroadcast(intent);
                  }

            @Override
            public void onUpgrading(boolean isManual) {
                //Log.e("!!!","!");
//                Intent intent = new Intent();
//                intent.setAction("android.intent.updatebroadcast");
//                localBroadcastManager.sendBroadcast(intent);
                // Toast.makeText(getApplicationContext(),"更新成功!",Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onDownloadCompleted(boolean b) {
//                Intent intent = new Intent();
//                intent.setAction("android.intent.updatebroadcast");
//                localBroadcastManager.sendBroadcast(intent);
                Toast.makeText(getApplicationContext(), "下载完成", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onUpgradeNoVersion(boolean isManual) {
                Intent intent = new Intent();
                intent.setAction("com.stupidtree.hita.upgrade_no_version");
                sendBroadcast(intent);
               }
        };


    }

    public static void getThemeID() {
        String mode = defaultSP.getString("dark_mode_mode","dark_mode_normal");
        if(mode.equals("dark_mode_normal")){
            if(defaultSP.getBoolean("is_dark_mode",false)) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            else  AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }else if(mode.equals("dark_mode_follow")){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }

        switch (defaultSP.getInt("theme_id", 3)) {
            case 0:
                themeID = R.style.RedTheme;
                break;
            case 1:
                themeID = R.style.PinkTheme;
                break;
            case 2:
                themeID = R.style.BrownTheme;
                break;
            case 3:
                themeID = R.style.BlueTheme;
                break;
            case 4:
                themeID = R.style.BlueGreyTheme;
                break;
            case 5:
                themeID = R.style.TealTheme;
                break;
            case 6:
                themeID = R.style.DeepPurpleTheme;
                break;
            case 7:
                themeID = R.style.GreenTheme;
                break;
            case 8:
                themeID = R.style.DeepOrangeTheme;
                break;
            case 9:
                themeID = R.style.IndigoTheme;
                break;
            case 10:
                themeID = R.style.CyanTheme;
                break;
            case 11:
                themeID = R.style.AmberTheme;
                break;
        }
    }




    @Override
    public void onTerminate() {
        super.onTerminate();
        timeTableCore.onTerminate();
    }



    static class InitTask extends AsyncTask {

        @SuppressLint("StaticFieldLeak")
        Context context;

        InitTask(Context f) {
            context = f;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Log.e("time_test","initialize:begin");
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            timeTableCore.initCoreData();
            timeWatcher.refreshProgress(true, true);
            //Log.e("time_test","initialize:end");
            Intent i = new Intent();
            i.setAction("COM.STUPIDTREE.HITA.TIMELINE_REFRESH");
            context.sendBroadcast(i);
            return null;
        }

    }
}
