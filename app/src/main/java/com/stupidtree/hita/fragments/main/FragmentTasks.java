package com.stupidtree.hita.fragments.main;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Bundle;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.TaskCardListAdapter;
import com.stupidtree.hita.util.RefreshBroadcastReceiver;

import java.util.ArrayList;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.defaultSP;
import static com.stupidtree.hita.adapter.NaviPageAdapter.strToIntegerList;

public class FragmentTasks extends BaseFragment implements RefreshBroadcastReceiver.ActionListener {
    RecyclerView list;
    List<Integer> listRes;
    TaskCardListAdapter listAdapter;


    LocalBroadcastManager localBroadcastManager;
    RefreshBroadcastReceiver refreshBroadcastReceiver;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initReceiver();
    }

    void initReceiver() {
        localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
        refreshBroadcastReceiver = new RefreshBroadcastReceiver();
        refreshBroadcastReceiver.setListener(this);
        IntentFilter iF = new IntentFilter();
        iF.addAction("COM.STUPIDTREE.HITA.TASK_REFRESH");
        iF.addAction("COM.STUPIDTREE.HITA.TIMELINE_REFRESH");
        iF.addAction("COM.STUPIDTREE.HITA.TIMETABLE_PAGE_REFRESH");
        localBroadcastManager.registerReceiver(refreshBroadcastReceiver, iF);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tasks, container, false);
        initList(v);

        return v;
    }

    void initList(View v) {
        list = v.findViewById(R.id.list);
        listRes = new ArrayList<>();
        listAdapter = new TaskCardListAdapter((BaseActivity) getActivity(),listRes);
        list.setAdapter(listAdapter);
        list.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        TaskCardListAdapter.mCallBack mCallBack = new   TaskCardListAdapter.mCallBack(listAdapter);
        ItemTouchHelper helper = new ItemTouchHelper(mCallBack);
        helper.attachToRecyclerView(list);
        List<Integer> order  = strToIntegerList(defaultSP.getString("task_page_order","[]"));
        if(order.size()>0){
            for(int i=0;i<order.size();i++){
                if(order.get(i) == TaskCardListAdapter.TYPE_TASK){
                    if(defaultSP.getBoolean("app_task_enabled",true)){
                        listRes.add(order.get(i));
                    }
                }else  listRes.add(order.get(i));

            }
        }else{
            if(defaultSP.getBoolean("app_task_enabled",true)) listRes.add(TaskCardListAdapter.TYPE_TASK);
            listRes.add(TaskCardListAdapter.TYPE_DDL);
            listRes.add(TaskCardListAdapter.TYPE_EXAM);
        }

    }



    @Override
    public void receive(Context context, Intent intent) {
        Log.e("fragmentTask_recieve",intent.getAction());
        Refresh();
    }





    @Override
    public void onResume() {
        super.onResume();
        Refresh();
    }

    @Override
    protected void stopTasks() {

    }

    @Override
    public void Refresh() {
        listAdapter.notifyDataSetChanged();
    }
}
