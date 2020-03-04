package com.stupidtree.hita;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.stupidtree.hita.timetable.HITADBHelper;
import com.stupidtree.hita.timetable.TimeWatcherService;
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
import java.util.List;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobUser;

import static com.stupidtree.hita.timetable.TimeWatcherService.TIMETABLE_CHANGED;

/**
 * 全局Application类，生命周期和整个应用相同
 */
public class HITAApplication extends Application {

    //一个全局的Context变量
    public static Context HContext;
    public static Calendar now;
    public static HITADBHelper mDBHelper;
    public static AppThemeCore themeCore;
    public static JWCore jwCore;
    public static TimetableCore timeTableCore;
    public static SharedPreferences defaultSP;
    public static List<ChatBotMessageItem> ChatBotListRes;//聊天机器人的聊天记录
    public static List<BmobObject> SearchResultList;
    public static String searchText ="";
    public static HITAUser CurrentUser = null;

    public static Handler ToastHander;
    public static ThreadPoolExecutor TPE;

    public static TimeWatcherService.TimeServiceBinder timeServiceBinder;


    @SuppressLint("HandlerLeak")
    @Override
    public void onCreate() {
        super.onCreate();
        //初始化顺序不可乱
        now = Calendar.getInstance();
        TPE = new ThreadPoolExecutor(0,Integer.MAX_VALUE,60L, TimeUnit.SECONDS,new SynchronousQueue<Runnable>());
        HContext = getBaseContext();
        mDBHelper = new HITADBHelper(HContext);
        defaultSP = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this);
        themeCore = new AppThemeCore();
        timeTableCore = new TimetableCore();
        ChatBotListRes = new ArrayList<>();
        SearchResultList = new ArrayList<>();
        jwCore = new JWCore();
       // timeWatcher = new TimeWatcher(this);
        initUpgradeDialog();
        initServices();
        //initLanguage();
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
        themeCore.initAppTheme();
    }




    private void initServices(){
        ServiceConnection conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                timeServiceBinder = (TimeWatcherService.TimeServiceBinder) service;
                new InitTask(HITAApplication.this).executeOnExecutor(HITAApplication.TPE);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                timeServiceBinder = null;
            }
        };
        Intent i = new Intent(this, TimeWatcherService.class);
        bindService(i,conn, Service.BIND_AUTO_CREATE);
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




    @Override
    public void onTerminate() {
        super.onTerminate();
        timeTableCore.onTerminate();
    }


    //    private void initLanguage(){
//        String lan = defaultSP.getString("app_language","ch");
//        changeAppLanguage(getResources(),lan);
//    }


    class InitTask extends AsyncTask {

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

           // timeServiceBinder.refreshProgress();
            //Log.e("time_test","initialize:end");
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
//            Intent i = new Intent(WATCHER_REFRESH);
//            i.putExtra("call_everyone",true);
//            HITAApplication.this.sendBroadcast(i);
            Intent i2 = new Intent(TIMETABLE_CHANGED);
            LocalBroadcastManager.getInstance(HITAApplication.this).sendBroadcast(i2);
        }
    }
}
