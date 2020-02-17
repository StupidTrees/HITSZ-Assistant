package com.stupidtree.hita.fragments.attitude;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivityAttitude;
import com.stupidtree.hita.adapter.AttitudeListAdapter;
import com.stupidtree.hita.diy.WrapContentLinearLayoutManager;
import com.stupidtree.hita.online.Attitude;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

import static com.stupidtree.hita.HITAApplication.TPE;

public class FragmentAttitude extends BaseFragment {
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView list;
    private AttitudeListAdapter listAdapter;
    private List<Attitude> listRes;
    private String title;
    private DataFetcher dataFetcher;
    private FetchDataTask pageTask;
    private boolean shouldRefresh = false;

    public String getTitle() {
        return title;
    }

    public boolean ShouldRefresh() {
        return shouldRefresh;
    }

    public void setShouldRefresh(boolean shouldRefresh) {
        this.shouldRefresh = shouldRefresh;
    }

    public interface DataFetcher{
        @WorkerThread
        List<Attitude> fetch() throws Exception;
    }

    public FragmentAttitude(String title, DataFetcher dataFetcher) {
        this.title = title;
        this.dataFetcher = dataFetcher;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_attitude,container,false);
        initList(v);
        return v;
    }

    @Override
    protected void stopTasks() {
        if(pageTask!=null&&!pageTask.isCancelled()) pageTask.cancel(true);
    }
    void initList(View v){
        refreshLayout = v.findViewById(R.id.refresh);
        refreshLayout.setColorSchemeColors(getColorPrimary());
        list = v.findViewById(R.id.list);
        list.setItemViewCacheSize(30);
        listRes = new ArrayList<>();
        listAdapter = new AttitudeListAdapter(getActivity(),listRes);
        list.setAdapter(listAdapter);
        list.setLayoutManager(new WrapContentLinearLayoutManager(getContext()));
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Refresh();
            }
        });
    }

    @Override
    public void Refresh(){
        if(pageTask!=null&&!pageTask.isCancelled()) pageTask.cancel(true);
        pageTask = new FetchDataTask();
        pageTask.executeOnExecutor(TPE);
        shouldRefresh = false;
    }


    @Override
    public void onResume() {
        super.onResume();
        if(shouldRefresh) Refresh();

    }

    class FetchDataTask extends AsyncTask{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            refreshLayout.setRefreshing(true);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                listRes.clear();
                listRes.addAll(dataFetcher.fetch());
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

        }


        @Override
        protected void onPostExecute(Object o) {
            refreshLayout.setRefreshing(false);
            super.onPostExecute(o);
            if((boolean)o){
                listAdapter.notifyDataSetChanged();
                list.scheduleLayoutAnimation();
            }else Toast.makeText(getContext(),"刷新失败",Toast.LENGTH_SHORT).show();

        }
    }

}
