package com.stupidtree.hita.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.CanteenListAdapter;
import com.stupidtree.hita.online.Canteen;
import com.stupidtree.hita.online.Infos;
import com.stupidtree.hita.online.Location;
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

public class FragmentCanteenList extends Fragment {
    CanteenListAdapter listAdapter;
    RecyclerView list;
    List<Canteen> listRes;
    TextView rank_number, rank_board;
    SwipeRefreshLayout refreshLayout;
    boolean firstOpen = true;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_canteen_list, container, false);
        initList(v);
       // refresh();
        return v;
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    void initList(View v) {
        refreshLayout = v.findViewById(R.id.refresh);
        list = v.findViewById(R.id.canteen_list);
        rank_number = v.findViewById(R.id.canteen_number);
        rank_board = v.findViewById(R.id.canteen_board);
        listRes = new ArrayList<>();
        listAdapter = new CanteenListAdapter(this.getContext(), listRes);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false);
        list.setAdapter(listAdapter);
        list.setLayoutManager(layoutManager);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh(true,true);
            }
        });


        listAdapter.setmOnNaviClickListener(new CanteenListAdapter.OnNaviClickListener() {
            @Override
            public void OnClick(Canteen c) {
                ActivityUtils.startExploreActivity_forNavi(getActivity(), c.getName(), c.getLongitude(), c.getLatitude());
            }
        });

        listAdapter.setOnItemClickListener(new CanteenListAdapter.OnItemClickListener() {
            @Override
            public void OnClick(View v, ImageView transitionImage,int position) {

                ActivityUtils.startLocationActivity(getActivity(),listRes.get(position));
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        refresh(firstOpen,firstOpen);
    }

    void refresh(final boolean animate,boolean swipeRefresh) {
        firstOpen = false;
        if(swipeRefresh) refreshLayout.setRefreshing(true);
        BmobQuery<Location> bq = new BmobQuery();
        bq.addWhereEqualTo("type", "canteen");
        bq.findObjects(new FindListener<Location>() {
            @Override
            public void done(List<Location> listA, BmobException e) {
                refreshLayout.setRefreshing(false);
                if (e == null) {
                    listRes.clear();
                    for (Location l : listA) {
                        Canteen c = new Canteen(l);
                        listRes.add(c);
                    }
                    Collections.sort(listRes);
                    listAdapter.notifyDataSetChanged();
                    if(animate) list.scheduleLayoutAnimation();
                }
            }
        });
        BmobQuery<Infos> bq2 = new BmobQuery<>();
        bq2.addWhereEqualTo("name", "canteen_rank_info");
        bq2.findObjects(new FindListener<Infos>() {
            @Override
            public void done(List<Infos> list, BmobException e) {
                if (e == null && list != null && list.size() > 0) {
                    JsonObject jo = list.get(0).getJson();
                    if (jo != null) {
                        String number = jo.get("number_text").getAsString();
                        String board = jo.get("board_text").getAsString();
                        if (number != null) rank_number.setText(number);
                        if (board != null) rank_board.setText(board);
                    }
                } else {
                    rank_number.setText("-");
                    rank_board.setText("-");
                }
            }
        });

    }
}
