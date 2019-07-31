package com.stupidtree.hita.util;

import android.content.Context;
import android.widget.Toast;

import com.stupidtree.hita.activities.ActivityMain;
import com.tencent.bugly.beta.UpgradeInfo;
import com.tencent.bugly.beta.upgrade.UpgradeListener;

//import static com.stupidtree.hita.HITAApplication.localBroadcastManager;

public class  mUpgradeListener implements UpgradeListener {
    Context context;


    public void setContext(Context context){
        this.context = context;
    }



    @Override
    public void onUpgrade(int i, UpgradeInfo upgradeInfo, boolean b, boolean b1) {
        //Intent intent = new Intent();
        //intent.setAction("android.intent.updatebroadcast");
        //localBroadcastManager.sendBroadcast(intent);

        if (upgradeInfo != null) {
            Toast.makeText(context,"检测到版本更新",Toast.LENGTH_SHORT).show();
            ActivityMain.showUpdateDialog(context);
        } else {
            if(! (context instanceof ActivityMain)) Toast.makeText(context,"已是最新版本！",Toast.LENGTH_SHORT).show();
        }
    }




}
