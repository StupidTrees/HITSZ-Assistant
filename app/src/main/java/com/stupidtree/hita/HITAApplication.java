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
import android.os.IBinder;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.stupidtree.hita.activities.ActivityMain;
import com.stupidtree.hita.community.BmobCacheHelper;
import com.stupidtree.hita.eas.JWCore;
import com.stupidtree.hita.online.HITAUser;
import com.stupidtree.hita.timetable.TimeWatcherService;
import com.stupidtree.hita.timetable.TimetableCore;
import com.stupidtree.hita.util.mUpgradeListener;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.beta.upgrade.UpgradeStateListener;

import java.lang.ref.WeakReference;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;

import static com.stupidtree.hita.timetable.TimeWatcherService.TIMETABLE_CHANGED;

/**
 * 全局Application类，生命周期和整个应用相同
 */
public class HITAApplication extends Application {

    //一个全局的Context变量
    public static Application HContext;
    public static AppThemeCore themeCore;
    public static JWCore jwCore;
    public static SharedPreferences defaultSP;
    public static HITAUser CurrentUser = null;
    public static BmobCacheHelper bmobCacheHelper;
    public static ThreadPoolExecutor TPE;
    public static TimeWatcherService.TimeServiceBinder timeServiceBinder;


    @SuppressLint("HandlerLeak")
    @Override
    public void onCreate() {
        super.onCreate();
        //初始化顺序不可乱
        TPE = new ThreadPoolExecutor(0,Integer.MAX_VALUE,60L, TimeUnit.SECONDS,new SynchronousQueue<Runnable>());
        HContext = this;
        defaultSP = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this);
        themeCore = new AppThemeCore();
        //   TimetableCore.getInstance(HContext) = new TimetableCore(getContentResolver());
        bmobCacheHelper = new BmobCacheHelper();
        jwCore = new JWCore();
        initUpgradeDialog();
        initServices();
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
                new InitTask(getApplicationContext()).executeOnExecutor(HITAApplication.TPE);
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
        Beta.autoCheckUpgrade = false;

        //        Beta.upgradeCheckPeriod = 60 * 1000;
        Beta.initDelay = 3 * 1000;
        Beta.largeIconId = R.drawable.logo;
        Beta.smallIconId = R.drawable.notification_logo_small;
        //Beta.defaultBannerId = R.mipmap.ic_launcher_round;
        Beta.storageDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        Beta.showInterruptedStrategy = true;
        Beta.canShowUpgradeActs.add(ActivityMain.class);

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
        TimetableCore.getInstance(this).onTerminate();
    }


    //    private void initLanguage(){
//        String lan = defaultSP.getString("app_language","ch");
//        changeAppLanguage(getResources(),lan);
//    }


    static class InitTask extends AsyncTask<Object,Object,Object> {

        WeakReference<Context> context;

        InitTask(Context f) {
            context = new WeakReference<>(f);
        }


        @Override
        protected Object doInBackground(Object[] objects) {
            TimetableCore.getInstance(HContext).initCoreData();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            if(context.get()!=null){
                Intent i2 = new Intent(TIMETABLE_CHANGED);
                LocalBroadcastManager.getInstance(context.get()).sendBroadcast(i2);
            }

        }
    }
}
