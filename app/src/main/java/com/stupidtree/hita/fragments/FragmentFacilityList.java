package com.stupidtree.hita.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.FacilityListAdapter;
import com.stupidtree.hita.online.Location;
import com.stupidtree.hita.online.Facility;
import com.stupidtree.hita.util.ActivityUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class FragmentFacilityList extends Fragment {
    FacilityListAdapter listAdapter;
    RecyclerView list;
    List<Facility> listRes;
    SwipeRefreshLayout refreshLayout;

    boolean firstOpen = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_facility_list, container, false);
        initList(v);
        //refresh();
        return v;
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    void initList(View v) {
        refreshLayout = v.findViewById(R.id.refresh);
        list = v.findViewById(R.id.facility_list);
        listRes = new ArrayList<>();
        listAdapter = new FacilityListAdapter(this.getContext(), listRes);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false);
        list.setAdapter(listAdapter);
        list.setLayoutManager(layoutManager);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh(true,true);
            }
        });

        listAdapter.setmOnNaviClickListener(new FacilityListAdapter.OnNaviClickListener() {
            @Override
            public void OnClick(Facility c) {
                ActivityUtils.startExploreActivity_forNavi(getActivity(), c.getName(), c.getLongitude(), c.getLatitude());
            }
        });

        listAdapter.setOnItemClickListener(new FacilityListAdapter.OnItemClickListener() {
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
        bq.addWhereEqualTo("type", "facility");
        bq.findObjects(new FindListener<Location>() {
            @Override
            public void done(List<Location> listA, BmobException e) {
                refreshLayout.setRefreshing(false);
                if (e == null) {
                    listRes.clear();
                    for (Location l : listA) {
                        Facility s = new Facility(l);
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
