package com.stupidtree.hita.util;

import androidx.appcompat.app.AppCompatActivity;

import com.stupidtree.hita.BaseActivity;
import com.tencent.bugly.beta.Beta;

public class UpdateManager {
    static public void checkUpdate(BaseActivity activity){
        ((mUpgradeListener)Beta.upgradeListener).setContext(activity);
        Beta.checkUpgrade();
    }
}
