package com.stupidtree.hita.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.Image;
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
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.SceneryListAdapter;
import com.stupidtree.hita.online.Location;
import com.stupidtree.hita.online.RateUser;
import com.stupidtree.hita.online.Scenery;
import com.stupidtree.hita.online.RateUser;
import com.stupidtree.hita.util.ActivityUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.online.Location.showRateDialog;

public class FragmentSceneryList extends Fragment {
    SceneryListAdapter listAdapter;
    RecyclerView list;
    List<Scenery> listRes;
    SwipeRefreshLayout refreshLayout;

    boolean firstOpen = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_scenery_list, container, false);
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
        list = v.findViewById(R.id.scenery_list);
        listRes = new ArrayList<>();
        listAdapter = new SceneryListAdapter(this.getContext(), listRes);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false);
        list.setAdapter(listAdapter);
        list.setLayoutManager(layoutManager);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh(true,true);
            }
        });

        listAdapter.setmOnNaviClickListener(new SceneryListAdapter.OnNaviClickListener() {
            @Override
            public void OnClick(Scenery c) {
                ActivityUtils.startExploreActivity_forNavi(getActivity(), c.getName(), c.getLongitude(), c.getLatitude());
            }
        });

        listAdapter.setOnItemClickListener(new SceneryListAdapter.OnItemClickListener() {
            @Override
            public void OnClick(View v, ImageView img, int position) {
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
        bq.addWhereEqualTo("type", "scenery");
        bq.findObjects(new FindListener<Location>() {
            @Override
            public void done(List<Location> listA, BmobException e) {
                refreshLayout.setRefreshing(false);
                if (e == null) {
                    listRes.clear();
                    for (Location l : listA) {
                        Scenery s = new Scenery(l);
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
