package com.stupidtree.hita.util;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

public class killSelfService extends Service {
  /**关闭应用后多久重新启动*/
          private static long stopDelayed=1;
  private Handler handler;
  private String PackageName;
 
  /**
   * Instantiates a new Kill self service.
   */
          public killSelfService() {
    handler=new Handler();
  }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
  public int onStartCommand(final Intent intent, int flags, int startId) {
    stopDelayed=intent.getLongExtra("Delayed",1);
    PackageName=intent.getStringExtra("PackageName");
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(PackageName);
        startActivity(LaunchIntent);
        killSelfService.this.stopSelf();
      }
    },10);
    return super.onStartCommand(intent, flags, startId);
  }

}
