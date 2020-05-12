package com.stupidtree.hita.fragments.campus;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.BaseListAdapter;
import com.stupidtree.hita.adapter.CanteenListAdapter;
import com.stupidtree.hita.fragments.BaseFragment;
import com.stupidtree.hita.online.Canteen;
import com.stupidtree.hita.online.Infos;
import com.stupidtree.hita.online.Location;
import com.stupidtree.hita.util.ActivityUtils;
import com.stupidtree.hita.views.WrapContentLinearLayoutManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class FragmentCanteenList extends BaseFragment {
    private CanteenListAdapter listAdapter;
    private RecyclerView list;
    private List<Canteen> listRes;
    private TextView rank_number, rank_board;
    private SwipeRefreshLayout refreshLayout;
    private ImageView head_bg;
    private boolean firstOpen = true;


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_canteen_list;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initList(view);
    }



    @Override
    protected void stopTasks() {

    }

    @Override
    public void Refresh() {

    }


    private void initList(View v) {
        refreshLayout = v.findViewById(R.id.refresh);
        list = v.findViewById(R.id.canteen_list);
        rank_number = v.findViewById(R.id.canteen_title);
        rank_board = v.findViewById(R.id.canteen_subtitle);
        head_bg = v.findViewById(R.id.head_bg);
        listRes = new ArrayList<>();
        listAdapter = new CanteenListAdapter(this.requireContext(), listRes);
        RecyclerView.LayoutManager layoutManager = new WrapContentLinearLayoutManager(this.requireContext(), RecyclerView.VERTICAL, false);
        list.setAdapter(listAdapter);
        list.setLayoutManager(layoutManager);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh(true,true);
            }
        });


        listAdapter.setOnNavigationClickListener(new CanteenListAdapter.OnNaviClickListener() {
            @Override
            public void OnClick(Canteen c) {
                ActivityUtils.startExploreActivity_forNavi(getActivity(), c.getName(), c.getLongitude(), c.getLatitude());
            }
        });

        listAdapter.setOnItemClickListener(new BaseListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View card, int position) {
                ActivityUtils.startLocationActivity(getActivity(),listRes.get(position));
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        refresh(firstOpen,firstOpen);
    }

    private void refresh(final boolean animate, boolean swipeRefresh) {
        firstOpen = false;
        if(swipeRefresh) refreshLayout.setRefreshing(true);
        BmobQuery<Location> bq = new BmobQuery<>();
        bq.addWhereEqualTo("type", "canteen");
        bq.findObjects(new FindListener<Location>() {
            @Override
            public void done(List<Location> listA, BmobException e) {
                refreshLayout.setRefreshing(false);
                if (e == null) {
                    List<Canteen> newL = new ArrayList<>();
                    for (Location l : listA) {
                        Canteen c = new Canteen(l);
                        newL.add(c);
                    }
                    Collections.sort(newL, new Comparator<Canteen>() {
                        @Override
                        public int compare(Canteen o1, Canteen o2) {
                            return o1.compareTo(o2);
                        }
                    });
                    if (animate) {
                        listRes.clear();
                        listRes.addAll(newL);
                        listAdapter.notifyDataSetChanged();
                        list.scheduleLayoutAnimation();
                    } else {
                        listAdapter.notifyItemChangedSmooth(newL, false);
                    }


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
                        String number = jo.get("title").getAsString();
                        String board = jo.get("subtitle").getAsString();
                        String imageUrl = jo.get("image_url").getAsString();
                        if (number != null) rank_number.setText(number);
                        if (board != null) rank_board.setText(board);
                        if(imageUrl!=null){
                            Glide.with(requireContext()).load(imageUrl).placeholder(R.drawable.gradient_bg)
                                    .into(head_bg);
                        }else head_bg.setImageResource(R.drawable.gradient_bg);

                    }
                } else {
                    rank_number.setText("-");
                    rank_board.setText("-");
                    head_bg.setImageResource(R.drawable.gradient_bg);
                }
            }
        });

    }
}
