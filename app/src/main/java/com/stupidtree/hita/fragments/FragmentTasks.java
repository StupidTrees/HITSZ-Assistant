package com.stupidtree.hita.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Bundle;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivityMain;
import com.stupidtree.hita.adapter.NaviPageAdapter;
import com.stupidtree.hita.adapter.TaskCardListAdapter;
import com.stupidtree.hita.core.TimeTable;
import com.stupidtree.hita.core.TimeTableGenerator;
import com.stupidtree.hita.core.timetable.EventItem;
import com.stupidtree.hita.core.timetable.HTime;
import com.stupidtree.hita.core.timetable.Task;
import com.stupidtree.hita.adapter.TaskListAdapter;
import com.stupidtree.hita.diy.WrapContentLinearLayoutManager;
import com.stupidtree.hita.util.RefreshBroadcastReceiver;

import org.apache.http.util.TextUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

import tyrantgit.explosionfield.ExplosionField;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.defaultSP;
import static com.stupidtree.hita.HITAApplication.isDataAvailable;
import static com.stupidtree.hita.HITAApplication.mainTimeTable;
import static com.stupidtree.hita.HITAApplication.now;
import static com.stupidtree.hita.adapter.NaviPageAdapter.TYPE_CARD;
import static com.stupidtree.hita.adapter.NaviPageAdapter.TYPE_HINT;
import static com.stupidtree.hita.adapter.NaviPageAdapter.TYPE_HITA;
import static com.stupidtree.hita.adapter.NaviPageAdapter.TYPE_JWTS_FUN;
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
