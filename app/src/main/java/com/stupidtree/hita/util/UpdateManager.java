package com.stupidtree.hita.util;

import android.support.v7.app.AppCompatActivity;

import com.tencent.bugly.beta.Beta;

public class UpdateManager {
    static public void checkUpdate(AppCompatActivity activity){
        ((mUpgradeListener)Beta.upgradeListener).setContext(activity);
        Beta.checkUpgrade();
    }
}
