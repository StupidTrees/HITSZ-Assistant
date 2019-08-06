package com.stupidtree.hita.fragments;

import android.net.Uri;
import android.os.Bundle;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.DormitoryListAdapter;
import com.stupidtree.hita.online.Dormitory;
import com.stupidtree.hita.online.Location;
import com.stupidtree.hita.util.ActivityUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class FragmentDormitoryList extends BaseFragment {
    DormitoryListAdapter listAdapter;
    RecyclerView list;
    List<Dormitory> listRes;
    SwipeRefreshLayout refreshLayout;

    boolean firstOpen = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_dormitory_list, container, false);
        initList(v);
        //refresh();
        return v;
    }

    @Override
    protected void stopTasks() {

    }

    @Override
    protected void Refresh() {

    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    void initList(View v) {
        refreshLayout = v.findViewById(R.id.refresh);
        list = v.findViewById(R.id.dormitory_list);
        listRes = new ArrayList<>();
        listAdapter = new DormitoryListAdapter(this.getContext(), listRes);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false);
        list.setAdapter(listAdapter);
        list.setLayoutManager(layoutManager);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh(true,true);
            }
        });

        listAdapter.setmOnNaviClickListener(new DormitoryListAdapter.OnNaviClickListener() {
            @Override
            public void OnClick(Dormitory c) {
                ActivityUtils.startExploreActivity_forNavi(getActivity(), c.getName(), c.getLongitude(), c.getLatitude());
            }
        });

        listAdapter.setOnItemClickListener(new DormitoryListAdapter.OnItemClickListener() {
            @Override
            public void OnClick(View v, int position) {
                ActivityUtils.startLocationActivity(getActivity(), listRes.get(position));
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        refresh(firstOpen,firstOpen);
    }

    void refresh(final boolean animate, boolean swipeRefresh) {
        firstOpen = false;
        if(swipeRefresh)refreshLayout.setRefreshing(true);
        BmobQuery<Location> bq = new BmobQuery();
        bq.addWhereEqualTo("type", "dormitory");
        bq.findObjects(new FindListener<Location>() {
            @Override
            public void done(List<Location> listA, BmobException e) {
                refreshLayout.setRefreshing(false);
                if (e == null) {
                    listRes.clear();
                    for (Location l : listA) {
                        Dormitory s = new Dormitory(l);
                        listRes.add(s);
                    }
                    System.out.println(listA);
                    Collections.sort(listRes);
                    listAdapter.notifyDataSetChanged();
                    if(animate) list.scheduleLayoutAnimation();
                } else Log.e("!!", e.toString());
            }
        });

    }


}
