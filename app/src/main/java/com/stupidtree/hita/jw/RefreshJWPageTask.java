package com.stupidtree.hita.jw;

import android.os.AsyncTask;

public class RefreshJWPageTask<T,T1,T2> extends AsyncTask<T,T1,T2> {
    JWFragment.OnRefreshStartListener refreshStartListener;
    JWFragment.OnRefreshFinishListener refreshFinishListener;

    public RefreshJWPageTask(JWFragment.OnRefreshStartListener refreshStartListener, JWFragment.OnRefreshFinishListener refreshFinishListener) {
        this.refreshStartListener = refreshStartListener;
        this.refreshFinishListener = refreshFinishListener;
    }

    @Override
    protected T2 doInBackground(T... ts) {
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(refreshStartListener!=null) refreshStartListener.OnStart();
    }

    @Override
    protected void onPostExecute(T2 o) {
        super.onPostExecute(o);
        if(refreshFinishListener!=null)refreshFinishListener.OnFinish();
    }

}
