package com.stupidtree.hita.fragments.attitude;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.AttitudeListAdapter;
import com.stupidtree.hita.fragments.BaseFragment;
import com.stupidtree.hita.fragments.BasicRefreshTask;
import com.stupidtree.hita.online.Attitude;
import com.stupidtree.hita.views.WrapContentLinearLayoutManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.stupidtree.hita.HITAApplication.TPE;

public class FragmentAttitude extends BaseFragment implements BasicRefreshTask.ListRefreshedListener<List<Attitude>> {
    private SwipeRefreshLayout refreshLayout;
    private AttitudeListAdapter listAdapter;
    private DataFetcher dataFetcher;
    private List<Attitude> listRes;
    private FetchDataTask pageTask;
    private RecyclerView list;
    private boolean shouldRefresh;


    public void setShouldRefresh(boolean shouldRefresh) {
        this.shouldRefresh = shouldRefresh;
    }


    public FragmentAttitude() {

    }

    public FragmentAttitude(DataFetcher dataFetcher) {
        this.dataFetcher = dataFetcher;
    }

    public interface DataFetcher{
        @WorkerThread
        List<Attitude> fetch() throws Exception;
    }

    @Override
    public void onRefreshStart(String id, Boolean[] params) {
        refreshLayout.setRefreshing(true);
    }

    @Override
    public void onListRefreshed(String id, Boolean[] params, List<Attitude> result) {
        refreshLayout.setRefreshing(false);
        listAdapter.notifyItemChangedSmooth(result);
        list.smoothScrollToPosition(0);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initList(view);
        shouldRefresh = true;
    }

    @Override
    protected void stopTasks() {
        if(pageTask!=null&&!pageTask.isCancelled()) pageTask.cancel(true);
    }

    private void initList(View v) {
        refreshLayout = v.findViewById(R.id.refresh);
        refreshLayout.setColorSchemeColors(getColorAccent());
        list = v.findViewById(R.id.list);
        list.setItemViewCacheSize(100);
        listRes = new ArrayList<>();
        listAdapter = new AttitudeListAdapter(getActivity(), listRes);
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
        pageTask = new FetchDataTask(this, dataFetcher);
        pageTask.executeOnExecutor(TPE);
        shouldRefresh = false;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_attitude;
    }


    public void notifySpecificAttitudeChanged(String objectId) {
        for (int i = 0; i < listRes.size(); i++) {
            if (Objects.equals(listRes.get(i).getObjectId(), objectId)) {
                listAdapter.notifyItemChanged(i);
            }
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        if(shouldRefresh) Refresh();

    }

    static class FetchDataTask extends BasicRefreshTask<List<Attitude>> {
        DataFetcher dataFetcher;

        FetchDataTask(ListRefreshedListener listRefreshedListener, DataFetcher fetcher) {
            super(listRefreshedListener);
            this.dataFetcher = fetcher;
        }

        @Override
        protected List<Attitude> doInBackground(ListRefreshedListener listRefreshedListener, Boolean... booleans) {
            List<Attitude> res = new ArrayList<>();
            try {
                res.addAll(dataFetcher.fetch());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return res;

        }
    }

}
