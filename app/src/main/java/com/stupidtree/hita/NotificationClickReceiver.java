package com.stupidtree.hita;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.stupidtree.hita.activities.ActivityLogin;
import com.stupidtree.hita.activities.ActivityLoginJWTS;
import com.stupidtree.hita.activities.ActivityMain;
import com.stupidtree.hita.activities.ActivityUserCenter;

public class NotificationClickReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent newIntent;
        newIntent = new Intent(context, ActivityMain.class);
        switch (intent.getIntExtra("terminal",0)){
            case 0:newIntent = new Intent(context, ActivityMain.class);break;
            case 1:newIntent = new Intent(context, ActivityLogin.class);break;
            case 2:newIntent = new Intent(context, ActivityUserCenter.class);break;
            case 3:newIntent = new Intent(context,ActivityLoginJWTS.class);break;
        }
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(newIntent);
    }
}
