package com.stupidtree.hita.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.azoft.carousellayoutmanager.CarouselLayoutManager;
import com.azoft.carousellayoutmanager.CarouselZoomPostLayoutListener;
import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivityMain;
import com.stupidtree.hita.core.TimeTable;
import com.stupidtree.hita.core.timetable.EventItem;
import com.stupidtree.hita.core.timetable.Task;
import com.stupidtree.hita.adapter.TaskListAdapter;
import com.stupidtree.hita.util.RefreshBroadcastReceiver;

import java.util.ArrayList;
import java.util.Objects;

import tyrantgit.explosionfield.ExplosionField;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.isDataAvailable;
import static com.stupidtree.hita.HITAApplication.mainTimeTable;

public class FragmentTasks extends BaseFragment implements RefreshBroadcastReceiver.ActionListener {
    RecyclerView tasksList;
    TaskListAdapter listAdapter;
    FloatingActionButton fab;
    TextView head_title;
    FrameLayout noneLayout;
    ArrayList<Task> listRes;
    ImageView bt_add;
    FragmentAddTask fat;
    public boolean hasInit = false;
    LocalBroadcastManager localBroadcastManager;
    RefreshBroadcastReceiver refreshBroadcastReceiver;
    refreshListTask pageTask;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fat = new FragmentAddTask();
        //fat.attachToFragment(this);
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
        //fab = v.findViewById(R.id.task_fab);
        bt_add = v.findViewById(R.id.bt_add);
        head_title = v.findViewById(R.id.task_head_title);
        noneLayout = v.findViewById(R.id.none_layout);
        bt_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDataAvailable()) showAddTaskDialog();
                else Toast.makeText(HContext, "请先导入课表！", Toast.LENGTH_SHORT).show();
            }
        });
        hasInit = true;
        return v;
    }

    void initList(View v) {
        tasksList = v.findViewById(R.id.task_recycler);
        listRes = new ArrayList<>();
        listAdapter = new TaskListAdapter(getContext(), listRes);
        // LinearLayoutManager manager = new LinearLayoutManager(this.getContext(),LinearLayoutManager.VERTICAL,false);
        //SkidRightLayoutManager manager = new SkidRightLayoutManager(1.5f, 0.85f);
        final CarouselLayoutManager layoutManager = new CarouselLayoutManager(CarouselLayoutManager.VERTICAL);
        layoutManager.setMaxVisibleItems(12);
        layoutManager.setPostLayoutListener(new CarouselZoomPostLayoutListener());
        LinearLayoutManager lm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        tasksList.setLayoutManager(lm);
        tasksList.setAdapter(listAdapter);
        listAdapter.setmOnItemLongClickListener(new TaskListAdapter.OnItemLongClickListener() {
            @Override
            public boolean OnClick(View v, int position) {
                ExplosionField ef = ExplosionField.attach2Window(getActivity());
                ef.explode(v);
                new deleteTask(position).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                return true;
            }
        });
    }

    void showAddTaskDialog() {
        fat.show(Objects.requireNonNull(getActivity()).getSupportFragmentManager(), "fat");
    }

    void refreshText() {
        if (!isDataAvailable()) {
            noneLayout.setVisibility(View.VISIBLE);
            head_title.setText("请先登录导入课表");
            return;
        }
        if (listRes.size() == 0) {
            noneLayout.setVisibility(View.VISIBLE);
            head_title.setText("没有任务");
        } else {
            noneLayout.setVisibility(View.GONE);
            head_title.setText("共有" + listRes.size() + "个待完成任务");
        }
    }

    @Override
    public void receive(Context context, Intent intent) {
        Refresh();
    }

    @Override
    protected void stopTasks() {
        if(pageTask!=null&&!pageTask.isCancelled()) pageTask.cancel(true);
    }

    @Override
    protected void Refresh() {
        if(pageTask!=null&&!pageTask.isCancelled()) pageTask.cancel(true);
        pageTask = new refreshListTask();
        pageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    @SuppressLint("StaticFieldLeak")
    class refreshListTask extends AsyncTask {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            listRes.clear();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            listRes.addAll(mainTimeTable.getTasks());
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            listAdapter.notifyDataSetChanged();
            refreshText();
        }
    }



    @Override
    public void onResume() {
        super.onResume();
        Refresh();
    }


    class deleteTask extends AsyncTask {
        int position;

        deleteTask(int position) {
            this.position = position;
        }

        @Override
        protected Object doInBackground(Object[] objects) {

            if (listRes.get(position).has_deadline) {
                EventItem ei = new EventItem(mainTimeTable.core.curriculumCode, TimeTable.TIMETABLE_EVENT_TYPE_DEADLINE,
                        listRes.get(position).ddlName, null, null, null,
                        listRes.get(position).eTime, listRes.get(position).eTime, listRes.get(position).tW, listRes.get(position).tDOW
                        , false);
                mainTimeTable.deleteEvent(ei, false);
            }
            if (mainTimeTable.deleteTask(listRes.get(position))) {
                listRes.remove(position);
                return true;
            } else return false;


        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if(getActivity().isDestroyed()) return;
            listAdapter.notifyItemRemoved(position);
            if (position != listRes.size()) { // 如果移除的是最后一个，忽略
                listAdapter.notifyItemRangeChanged(position, listRes.size() - position);
            }
            if ((Boolean) o) {
                refreshText();
            } else Toast.makeText(HContext, "删除失败!", Toast.LENGTH_SHORT).show();
            ActivityMain.saveData(getActivity());
            Intent mes = new Intent("COM.STUPIDTREE.HITA.TIMELINE_REFRESH_FROM_OTHER");
            localBroadcastManager.sendBroadcast(mes);
            // if(ftl!=null&&ftl.hasInit) ftl.Refresh(FragmentTimeLine.TL_REFRESH_FROM_UNHIDE);
        }
    }
}
