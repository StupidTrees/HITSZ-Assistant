package com.stupidtree.hita;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class WidgetService extends Service {
    // 更新 widget 的广播对应的 action
    private final String ACTION_UPDATE_ALL = "com.stupidtree.hita.UPDATE_ALL";



    private BroadcastReceiver timeReceiver = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent)
        {
           // Log.i(TAG, "onReceive 11111 intent.getAction()"+intent.getAction());
            if ("android.intent.action.TIMEZONE_CHANGED".equals(intent.getAction())
                    ||"android.intent.action.TIME_TICK".equals(intent.getAction())
                    ||"android.intent.action.TIME_CHANGED".equals(intent.getAction())
                    ||"android.intent.action.TIME_SET".equals(intent.getAction()))
            {
                Intent updateIntent = new Intent();
                updateIntent.setAction(ACTION_UPDATE_ALL);
                sendBroadcast(updateIntent);
                Log.e("send_broadcast","!");
            }

        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter localIntentFilter = new IntentFilter();
        localIntentFilter.addAction("android.intent.action.TIME_TICK");
        localIntentFilter.addAction("android.intent.action.TIME_CHANGED");
        localIntentFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
        registerReceiver(timeReceiver,localIntentFilter);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(timeReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }
}
