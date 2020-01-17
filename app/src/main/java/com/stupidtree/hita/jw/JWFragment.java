package com.stupidtree.hita.jw;

import androidx.fragment.app.Fragment;

public abstract class JWFragment extends Fragment {
    public interface OnRefreshFinishListener{
        void OnFinish();
    }
    public interface OnRefreshStartListener{
        void OnStart();
    }
    abstract protected void stopTasks();
    public abstract void Refresh(OnRefreshStartListener start,OnRefreshFinishListener finish);
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTasks();
    }


}
