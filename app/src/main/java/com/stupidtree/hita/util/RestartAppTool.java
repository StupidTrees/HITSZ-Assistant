package com.stupidtree.hita.util;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.stupidtree.hita.activities.ActivitySplash;

public class RestartAppTool {


 public static void restartAPP(Context context, long Delayed){

 /**开启一个新的服务，用来重启本APP*/
Intent intent1=new Intent(context,killSelfService.class);
intent1.putExtra("PackageName",context.getPackageName());
intent1.putExtra("Delayed",Delayed);
intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
context.startService(intent1);

/**杀死整个进程**/
android.os.Process.killProcess(android.os.Process.myPid());
System.exit(0);
  }

  /***重启整个APP @param context the context*/
          public static void restartAPP(Context context){
   /* restartAPP(context,1);*/
Intent intent = new Intent(context, ActivitySplash.class);
@SuppressLint("WrongConstant") PendingIntent restartIntent = PendingIntent.getActivity(context, 0, intent,
                Intent.FLAG_ACTIVITY_NEW_TASK);
//退出程序
AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
assert mgr != null;
mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000,
                restartIntent); // 1秒钟后重启应用
// 退出程序
android.os.Process.killProcess(android.os.Process.myPid());
System.exit(1);
  }
}

