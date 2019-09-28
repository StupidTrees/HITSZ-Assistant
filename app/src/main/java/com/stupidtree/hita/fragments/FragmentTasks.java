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
import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivityMain;
import com.stupidtree.hita.core.TimeTable;
import com.stupidtree.hita.core.TimeTableGenerator;
import com.stupidtree.hita.core.timetable.EventItem;
import com.stupidtree.hita.core.timetable.HTime;
import com.stupidtree.hita.core.timetable.Task;
import com.stupidtree.hita.adapter.TaskListAdapter;
import com.stupidtree.hita.util.RefreshBroadcastReceiver;

import org.apache.http.util.TextUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

import tyrantgit.explosionfield.ExplosionField;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.isDataAvailable;
import static com.stupidtree.hita.HITAApplication.mainTimeTable;
import static com.stupidtree.hita.HITAApplication.now;

public class FragmentTasks extends BaseFragment implements RefreshBroadcastReceiver.ActionListener {
    RecyclerView tasksList_now, taskList_done, tasksList_notyet;
    TaskListAdapter listAdapter_now, listAdapter_done, listAdapter_notyet;
    FloatingActionButton fab;
    TextView head_title;
    ArrayList<Task> listRes_now, listRes_done, listRes_notyet;
    ImageView bt_add, none1, none2, none3;
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
        none1 = v.findViewById(R.id.none_img1);
        none2 = v.findViewById(R.id.none_img2);
        none3 = v.findViewById(R.id.none_img3);
        bt_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDataAvailable()) showAddTaskDialog();
                else Snackbar.make(v,"请先导入课表！",Snackbar.LENGTH_SHORT).show();
            }
        });
        hasInit = true;
        return v;
    }

    void initList(View v) {
        tasksList_now = v.findViewById(R.id.task_recycler);
        tasksList_notyet = v.findViewById(R.id.task_recycler_not_yet);
        taskList_done = v.findViewById(R.id.task_recycler_done);
        listRes_now = new ArrayList<>();
        listRes_done = new ArrayList<>();
        listRes_notyet = new ArrayList<>();
        listAdapter_done = new TaskListAdapter(getActivity(), listRes_done);
        listAdapter_notyet = new TaskListAdapter(getActivity(), listRes_notyet);
        listAdapter_now = new TaskListAdapter(getActivity(), listRes_now);
        tasksList_now.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        taskList_done.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        tasksList_notyet.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        tasksList_now.setAdapter(listAdapter_now);
        tasksList_notyet.setAdapter(listAdapter_notyet);
        taskList_done.setAdapter(listAdapter_done);
        listAdapter_now.setmOnItemLongClickListener(new TaskListAdapter.OnItemLongClickListener() {
            @Override
            public boolean OnClick(View v, int position) {
                if((!TextUtils.isEmpty(listRes_now.get(position).getTag()))&&listRes_now.get(position).getTag().contains(":::")) return false;
                ExplosionField ef = ExplosionField.attach2Window(getActivity());
                ef.explode(v);
                new deleteTask(position, listAdapter_now, listRes_now).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                return true;
            }
        });

        listAdapter_now.setmOnFinishClickListener(new TaskListAdapter.OnFinishClickListener() {
            @Override
            public boolean OnClick(View v, int position) {

                new finishTask(v,position, listRes_now, listAdapter_now).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                return true;
            }
        });
        listAdapter_notyet.setmOnItemLongClickListener(new TaskListAdapter.OnItemLongClickListener() {
            @Override
            public boolean OnClick(View v, int position) {
                if((!TextUtils.isEmpty(listRes_notyet.get(position).getTag()))&&listRes_notyet.get(position).getTag().contains(":::")) return false;
                ExplosionField ef = ExplosionField.attach2Window(getActivity());
                ef.explode(v);
                new deleteTask(position, listAdapter_notyet, listRes_notyet).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                return true;
            }
        });

        listAdapter_notyet.setmOnFinishClickListener(new TaskListAdapter.OnFinishClickListener() {
            @Override
            public boolean OnClick(View v, int position) {
                new finishTask(v,position, listRes_notyet, listAdapter_notyet).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                return true;
            }
        });
        listAdapter_done.setmOnItemLongClickListener(new TaskListAdapter.OnItemLongClickListener() {
            @Override
            public boolean OnClick(View v, int position) {
                if((!TextUtils.isEmpty(listRes_done.get(position).getTag()))&&listRes_done.get(position).getTag().contains(":::")) return false;
                ExplosionField ef = ExplosionField.attach2Window(getActivity());
                ef.explode(v);
                new deleteTask(position, listAdapter_done, listRes_done).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                return true;
            }
        });

        listAdapter_done.setmOnFinishClickListener(new TaskListAdapter.OnFinishClickListener() {
            @Override
            public boolean OnClick(View v, int position) {
                new finishTask(v,position, listRes_done, listAdapter_done).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                return true;
            }
        });

//        listAdapter_now.setOnClickListener(new TaskListAdapter.OnClickListener() {
//            @Override
//            public boolean OnClick(View v, int position) {
//                Task t = listRes_now.get(position);
//                int leftTime = t.getLength();
//                Calendar pointer = Calendar.getInstance();
//                while (leftTime>0){
//                    Calendar start;
//                    if(pointer.get(Calendar.YEAR)== now.get(Calendar.YEAR)&&pointer.get(Calendar.DAY_OF_YEAR)==now.get(Calendar.DAY_OF_YEAR)) start = now;
//                    else start = null;
//
//                    int week = mainTimeTable.core.getWeekOfTerm(pointer);
//                    if(week>mainTimeTable.core.totalWeeks) break;;
//                    int dow =  TimeTable.getDOW(pointer);
//                    SparseArray<HTime> tp = TimeTableGenerator.autoAdd_getTime(start,week,dow,50);
//                    if(tp==null) continue;
//                    String uuid = mainTimeTable.addEvent(week,dow,TimeTable.TIMETABLE_EVENT_TYPE_ARRANGEMENT,"处理任务"+t.name,"","",t.getUuid(),tp.get(0),tp.get(1),false);
//                    t.putEventMap(uuid+":::"+week,false);
//                    leftTime-=tp.get(0).getDuration(tp.get(1));
//                    pointer.add(Calendar.DATE,1);
//                }
//                Snackbar.make(v,"安排成功！",Snackbar.LENGTH_SHORT).show();
//                return true;
//            }
//        });
    }

    void showAddTaskDialog() {
        fat.show(Objects.requireNonNull(getActivity()).getSupportFragmentManager(), "fat");
    }

    void refreshText() {
        if (!isDataAvailable()) {
            none1.setVisibility(View.VISIBLE);
            none2.setVisibility(View.VISIBLE);
            none3.setVisibility(View.VISIBLE);
            head_title.setText("请先登录导入课表");
            return;
        }
        if (listRes_now.size() + listRes_notyet.size() == 0) {
            head_title.setText("没有任务");
        } else {
            head_title.setText("共有" + (listRes_now.size() + listRes_notyet.size()) + "个待完成任务");
        }
        if (listRes_notyet.size() == 0) none2.setVisibility(View.VISIBLE);
        else none2.setVisibility(View.GONE);

        if (listRes_now.size() == 0) none1.setVisibility(View.VISIBLE);
        else none1.setVisibility(View.GONE);
        if (listRes_done.size() == 0) none3.setVisibility(View.VISIBLE);
        else none3.setVisibility(View.GONE);

    }

    @Override
    public void receive(Context context, Intent intent) {
        Refresh();
    }

    @Override
    protected void stopTasks() {
        if (pageTask != null && !pageTask.isCancelled()) pageTask.cancel(true);
    }

    @Override
    public void Refresh() {
        if (pageTask != null && !pageTask.isCancelled()) pageTask.cancel(true);
        pageTask = new refreshListTask();
        pageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    @SuppressLint("StaticFieldLeak")
    class refreshListTask extends AsyncTask {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            listRes_done.clear();
            listRes_now.clear();
            listRes_notyet.clear();
            listRes_done.addAll(mainTimeTable.getfinishedTasks());
            List<Task> tasks = mainTimeTable.getUnfinishedTasks();
            for (Task t : tasks) {
                if (TaskListAdapter.getTaskState(t) == TaskListAdapter.TYPE_FREE || TaskListAdapter.getTaskState(t) == TaskListAdapter.TYPE_ARRANGED_ONGOING)
                    listRes_now.add(t);
                else listRes_notyet.add(t);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            listAdapter_done.notifyDataSetChanged();
            listAdapter_now.notifyDataSetChanged();
            listAdapter_notyet.notifyDataSetChanged();
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
        RecyclerView list;
        TaskListAdapter listAdapter;
        List<Task> listRes;

        deleteTask(int position, TaskListAdapter adapter, List<Task> listRes) {
            this.position = position;
            listAdapter = adapter;
            this.listRes = listRes;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            if (mainTimeTable.deleteTask(listRes.get(position))) {
                listRes.remove(position);
                return true;
            } else return false;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (getActivity().isDestroyed()) return;
            listAdapter.notifyItemRemoved(position);
            if (position != listRes.size()) { // 如果移除的是最后一个，忽略
                listAdapter.notifyItemRangeChanged(position, listRes.size() - position);
            }
            if ((Boolean) o) {
                refreshText();
            } else Toast.makeText(HContext, "删除失败!", Toast.LENGTH_SHORT).show();
            ActivityMain.saveData();
            Intent mes = new Intent("COM.STUPIDTREE.HITA.TIMELINE_REFRESH");
            mes.putExtra("from", "task");
            localBroadcastManager.sendBroadcast(mes);
            // if(ftl!=null&&ftl.hasInit) ftl.Refresh(FragmentTimeLine.TL_REFRESH_FROM_UNHIDE);
        }
    }

    class finishTask extends AsyncTask {
        int position;
        List<Task> listRes;
        TaskListAdapter listAdapter;
        View clickView;

        public finishTask(View click,int position, List<Task> listRes, TaskListAdapter listAdapter) {
            this.position = position;
            this.listRes = listRes;
            this.listAdapter = listAdapter;
            this.clickView = click;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            if(listRes.get(position).isHas_length()&&listRes.get(position).getProgress()<100){
                return "dialog";
            }else{
                if (mainTimeTable.finishTask(listRes.get(position))) {
                    listRes.remove(position);
                    listRes_done.clear();
                    listRes_done.addAll(mainTimeTable.getfinishedTasks());
                    return true;
                } else return false;
            }


        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (getActivity().isDestroyed()) return;
            if(o instanceof String && o.equals("dialog")){
                AlertDialog ad = new AlertDialog.Builder(getContext()).setMessage("任务未完成，请添加对应处理事件！").setTitle("任务进度尚未完成").create();
                ad.show();
            }else{
                ExplosionField ef = ExplosionField.attach2Window(getActivity());
                ef.explode(clickView);
                listAdapter.notifyItemRemoved(position);
                if (position != listRes.size()) { // 如果移除的是最后一个，忽略
                    listAdapter.notifyItemRangeChanged(position, listRes.size() - position);
                }
                listAdapter_done.notifyDataSetChanged();
                if ((Boolean) o) {
                    refreshText();
                } else Toast.makeText(HContext, "操作失败!", Toast.LENGTH_SHORT).show();
                ActivityMain.saveData();
                Intent mes = new Intent("COM.STUPIDTREE.HITA.TIMELINE_REFRESH");
                mes.putExtra("from", "task");
                localBroadcastManager.sendBroadcast(mes);
                // if(ftl!=null&&ftl.hasInit) ftl.Refresh(FragmentTimeLine.TL_REFRESH_FROM_UNHIDE);
            }

        }
    }
}
