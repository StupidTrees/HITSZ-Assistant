package com.stupidtree.hita;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;

public abstract class BaseFragment extends Fragment {
    abstract protected void stopTasks();
    abstract protected void Refresh();
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTasks();
        Log.e("onDestroy","停止任务");
    }
}
