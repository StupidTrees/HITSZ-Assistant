package com.stupidtree.hita.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.ClassroomListAdapter;
import com.stupidtree.hita.online.Location;
import com.stupidtree.hita.online.RateUser;
import com.stupidtree.hita.online.Classroom;
import com.stupidtree.hita.util.ActivityUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.online.Location.showRateDialog;

public class FragmentClassroomList extends BaseFragment {
    ClassroomListAdapter listAdapter;
    RecyclerView list;
    List<Classroom> listRes;
    SwipeRefreshLayout refreshLayout;
    boolean firstOpen = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_classroom_list, container, false);
        initList(v);
       // refresh();
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
        list = v.findViewById(R.id.classroom_list);
        listRes = new ArrayList<>();
        listAdapter = new ClassroomListAdapter(this.getContext(), listRes);
        list.setAdapter(listAdapter);
        list.setLayoutManager(new GridLayoutManager(this.getContext(), 2));
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh(true,true);
            }
        });
//        listAdapter.setOnRateClickListener(new ClassroomListAdapter.OnRateClickListener() {
//            @Override
//            public void OnClick(final Classroom t) {
//                if (CurrentUser == null) {
//                    Toast.makeText(HContext, "请先登录!", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                BmobQuery<RateUser> bmobQuery = new BmobQuery();
//                bmobQuery.addWhereEqualTo("hitaUser", CurrentUser);
//                bmobQuery.addWhereEqualTo("rateObjectId", t.getObjectId());
//                bmobQuery.findObjects(new FindListener<RateUser>() {
//                    @Override
//                    public void done(List<RateUser> list, BmobException e) {
//                        if (e != null) {
//                            Log.e("!!", e.toString());
//                            return;
//                        }
//                        if (list != null && list.size() > 0) {
//                            Toast.makeText(HContext, "您已提交过评分，请下个评分周期再来进行评价", Toast.LENGTH_SHORT).show();
//                        } else {
//                            showRateDialog(getActivity(), t, new SaveListener() {
//                                @Override
//                                public void done(Object o, BmobException e) {
//                                    Toast.makeText(HContext, "评分成功！", Toast.LENGTH_SHORT).show();
//                                    Collections.sort(listRes);
//                                    listAdapter.notifyDataSetChanged();
//                                }
//
//                                @Override
//                                public void done(Object o, Object o2) {
//                                    Toast.makeText(HContext, "评分成功！", Toast.LENGTH_SHORT).show();
//                                    Collections.sort(listRes);
//                                    listAdapter.notifyDataSetChanged();
//                                }
//                            });
//                        }
//                    }
//                });
//            }
//        });

        listAdapter.setmOnNaviClickListener(new ClassroomListAdapter.OnNaviClickListener() {
            @Override
            public void OnClick(Classroom c) {
                ActivityUtils.startExploreActivity_forNavi(getActivity(), c.getName(), c.getLongitude(), c.getLatitude());
            }
        });

        listAdapter.setOnItemClickListener(new ClassroomListAdapter.OnItemClickListener() {
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
        if(swipeRefresh) refreshLayout.setRefreshing(true);
        BmobQuery<Location> bq = new BmobQuery();
        bq.addWhereEqualTo("type", "classroom");
        bq.findObjects(new FindListener<Location>() {
            @Override
            public void done(List<Location> listA, BmobException e) {
                refreshLayout.setRefreshing(false);

                if (e == null) {
                    listRes.clear();
                    for (Location l : listA) {
                        Classroom s = new Classroom(l);
                        listRes.add(s);
                    }
                    System.out.println(listA);
                    Collections.sort(listRes);
                    listAdapter.notifyDataSetChanged();
                    if(animate)list.scheduleLayoutAnimation();
                } else Log.e("!!", e.toString());
            }
        });

    }


}
