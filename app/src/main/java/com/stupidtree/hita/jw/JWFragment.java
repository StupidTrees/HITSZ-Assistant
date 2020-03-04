package com.stupidtree.hita.jw;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class JWFragment extends BaseFragment {
    SwipeRefreshLayout refresh;
    protected JWRoot jwRoot;
    protected boolean willRefreshOnResume = false;

    public boolean isWillRefreshOnResume() {
        return willRefreshOnResume;
    }

    public void setWillRefreshOnResume(boolean willRefreshOnResume) {
        this.willRefreshOnResume = willRefreshOnResume;
    }

    void initRefresh(View v){
        refresh = v.findViewById(R.id.refresh);
        if(refresh!=null) {
            refresh.setColorSchemeColors(getColorPrimary());
            refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    Refresh();
                }
            });
        }

    }
    public interface JWRoot{

        List<Map<String,String>> getXNXQItems();
        Map<String,String> getKeyToTitleMap();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof JWRoot){
            jwRoot = (JWRoot)context;
        }  else{
            throw new RuntimeException(context.toString()
                    + " must implement JWRoot");
        }
    }



    @Override
    public void onResume() {
        super.onResume();
        if(willRefreshOnResume) Refresh();
        willRefreshOnResume = false;
    }


    abstract public String getTitle();
    abstract protected void stopTasks();
    public abstract void Refresh();
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTasks();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        jwRoot = null;
    }

    class RefreshJWPageTask<T,T1,T2> extends AsyncTask<T,T1,T2> {

        @Override
        protected T2 doInBackground(T... ts) {
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(refresh!=null)refresh.setRefreshing(true);
        }

        @Override
        protected void onPostExecute(T2 o) {
            super.onPostExecute(o);
            if(refresh!=null)refresh.setRefreshing(false);
        }

    }

}
