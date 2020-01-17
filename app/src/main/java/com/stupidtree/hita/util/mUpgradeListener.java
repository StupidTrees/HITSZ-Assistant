package com.stupidtree.hita.util;

import android.content.Context;
import android.widget.Toast;

import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivityAboutHITA;
import com.stupidtree.hita.activities.ActivityMain;
import com.tencent.bugly.beta.UpgradeInfo;
import com.tencent.bugly.beta.upgrade.UpgradeListener;

//import static com.stupidtree.hita.HITAApplication.localBroadcastManager;

public class  mUpgradeListener implements UpgradeListener {
    BaseActivity context;


    public void setContext(BaseActivity context){
        this.context = context;
    }



    @Override
    public void onUpgrade(int i, UpgradeInfo upgradeInfo, boolean b, boolean b1) {
        if (upgradeInfo != null) {
            if(context instanceof ActivityMain){
                Toast.makeText(context, R.string.new_version_available,Toast.LENGTH_SHORT).show();
                ActivityMain.showUpdateDialog(context);
            }

        }
    }




}
