package com.stupidtree.hita.util;

import com.stupidtree.hita.activities.BaseActivity;
import com.tencent.bugly.beta.Beta;

public class UpdateManager {
    static public void checkUpdate(BaseActivity activity){
        ((mUpgradeListener)Beta.upgradeListener).setContext(activity);
        Beta.checkUpgrade();
    }
}
