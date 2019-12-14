package com.stupidtree.hita.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.TaskListAdapter;
import com.stupidtree.hita.core.timetable.Task;
import com.stupidtree.hita.diy.WrapContentLinearLayoutManager;
import com.stupidtree.hita.fragments.FragmentAddTask;
import com.stupidtree.hita.util.RefreshBroadcastReceiver;

import org.apache.http.util.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import tyrantgit.explosionfield.ExplosionField;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.isDataAvailable;
import static com.stupidtree.hita.HITAApplication.mainTimeTable;

public class ActivityTasks extends BaseActivity implements
FragmentAddTask.AddTaskDoneListener{


    RecyclerView tasksList_now;
    TaskListAdapter listAdapter_now;
    FloatingActionButton fab;
    ArrayList<Task> listRes_now, listRes_notyet;
    ImageView none;
    FragmentAddTask fat;
    public boolean hasInit = false;
    refreshListTask pageTask;
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);
        setWindowParams(true,true,false);
        fat = new FragmentAddTask();
        initToolbar();
       // initReceiver();
        initList();
        fab = findViewById(R.id.task_fab);
        none = findViewById(R.id.none_img1);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDataAvailable()) showAddTaskDialog();
                else Snackbar.make(v, "请先导入课表！", Snackbar.LENGTH_SHORT).show();
            }
        });
        hasInit = true;
    }


    void initToolbar(){
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("任务管理");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    void initList() {
        tasksList_now = findViewById(R.id.task_recycler);
        tasksList_now.setItemViewCacheSize(Integer.MAX_VALUE);
        //taskList_done = findViewById(R.id.task_recycler_done);
        listRes_now = new ArrayList<>();
       // listRes_done = new ArrayList<>();
        listRes_notyet = new ArrayList<>();
      listAdapter_now = new TaskListAdapter(this, listRes_now);
        tasksList_now.setLayoutManager(new WrapContentLinearLayoutManager(this, RecyclerView.VERTICAL, false));
        //taskList_done.setLayoutManager(new WrapContentLinearLayoutManager(this, RecyclerView.VERTICAL, false));
        tasksList_now.setAdapter(listAdapter_now);
         //taskList_done.setAdapter(listAdapter_done);
        listAdapter_now.setmOnItemLongClickListener(new TaskListAdapter.OnItemLongClickListener() {
            @Override
            public boolean OnClick(View v, int position) {
//                if ((!TextUtils.isEmpty(listRes_now.get(position).getTag())) && listRes_now.get(position).getTag().contains(":::"))
//                    return false;
                ExplosionField ef = ExplosionField.attach2Window(ActivityTasks.this);
                ef.explode(v);
                new deleteTask(position, listAdapter_now, listRes_now).execute();
                return true;
            }
        });

        listAdapter_now.setmOnFinishClickListener(new TaskListAdapter.OnFinishClickListener() {
            @Override
            public boolean OnClick(View v, Task t, int position) {
                new finishTask(t,!t.isFinished(), position, listRes_now, listAdapter_now).execute();
                return true;
            }

        });

//        listAdapter_done.setmOnItemLongClickListener(new TaskListAdapter.OnItemLongClickListener() {
//            @Override
//            public boolean OnClick(View v, int position) {
//                if ((!TextUtils.isEmpty(listRes_done.get(position).getTag())) && listRes_done.get(position).getTag().contains(":::"))
//                    return false;
//                ExplosionField ef = ExplosionField.attach2Window(ActivityTasks.this);
//                ef.explode(v);
//                new deleteTask(position, listAdapter_done, listRes_done).executeOnExecutor(HITAApplication.TPE);
//                return true;
//            }
//        });
//
//        listAdapter_done.setmOnFinishClickListener(new TaskListAdapter.OnFinishClickListener() {
//            @Override
//            public boolean OnClick(View v, int position) {
//                new undoFinishTask(v, position, listRes_done, listAdapter_done).executeOnExecutor(HITAApplication.TPE);
//                return true;
//            }
//        });


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
        fat.show(Objects.requireNonNull(this).getSupportFragmentManager(), "fat");
    }

    void refreshText() {
        if (!isDataAvailable()) {
            none.setVisibility(View.VISIBLE);
            return;
        }
        if (listRes_now.size() == 0) none.setVisibility(View.VISIBLE);
        else none.setVisibility(View.GONE);
        //if (listRes_done.size() == 0) none3.setVisibility(View.VISIBLE);
        //else none3.setVisibility(View.GONE);

    }


    @Override
    protected void stopTasks() {
        if (pageTask != null && pageTask.getStatus() != AsyncTask.Status.FINISHED)
            pageTask.cancel(true);
    }

    public void Refresh() {
        Log.e("refresh","tasks");
        if (pageTask != null && pageTask.getStatus() != AsyncTask.Status.FINISHED)
            pageTask.cancel(true);
        pageTask = new refreshListTask();
        pageTask.executeOnExecutor(HITAApplication.TPE);
    }


    @Override
    public void OnDone() {
        Refresh();
    }


    @SuppressLint("StaticFieldLeak")
    class refreshListTask extends AsyncTask {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
           // listRes_done.clear();
            listRes_now.clear();
            listRes_notyet.clear();
           // listRes_done.addAll(mainTimeTable.getfinishedTasks());
            List<Task> tasks = mainTimeTable.getUnfinishedTasks();

            for (Task t : tasks) {
                if (TaskListAdapter.getTaskState(t) == TaskListAdapter.TYPE_FREE || TaskListAdapter.getTaskState(t) == TaskListAdapter.TYPE_ARRANGED_ONGOING)
                    listRes_now.add(t);
                else listRes_notyet.add(t);
            }
            listRes_now.addAll(mainTimeTable.getfinishedTasks());
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            //listAdapter_done.notifyDataSetChanged();
            listAdapter_now.notifyDataSetChanged();
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
            if (isDestroyed()) return;
            listAdapter.notifyItemRemoved(position);
            if (position != listRes.size()) { // 如果移除的是最后一个，忽略
                listAdapter.notifyItemRangeChanged(position, listRes.size() - position);
            }
            if ((Boolean) o) {
                refreshText();
            } else Toast.makeText(HContext, "删除失败!", Toast.LENGTH_SHORT).show();
            ActivityMain.saveData();
            // if(ftl!=null&&ftl.hasInit) ftl.Refresh(FragmentTimeLine.TL_REFRESH_FROM_UNHIDE);
        }
    }

    class finishTask extends AsyncTask {
        int position;
        List<Task> listRes;
        Task t;
        TaskListAdapter listAdapter;
        boolean finished;

        public finishTask(Task t,boolean finished, int position, List<Task> listRes, TaskListAdapter listAdapter) {
            this.t = t;
            Log.e("finishTask:",listRes.get(position).name);
            this.position = position;
            this.listRes = listRes;
            this.listAdapter = listAdapter;
            this.finished = finished;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            if (t.isHas_length() && t.getProgress() < 100) {
                return "dialog";
            } else {
                return  mainTimeTable.setFinishTask(t,finished) ;
            }
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (isDestroyed()) return;
            if (o instanceof String && o.equals("dialog")) {
                AlertDialog ad = new AlertDialog.Builder(ActivityTasks.this).setMessage("任务未完成，请添加对应处理事件！").setTitle("任务进度尚未完成").create();
                ad.show();
            } else {
                if ((Boolean) o) {
                    int detPos;
                    if(finished) for(detPos  = listRes.size()-1;detPos>0&&listRes.get(detPos).isFinished()&&detPos!=position;detPos--);
                    else for(detPos  = 0;detPos<listRes.size()-1&&!listRes.get(detPos).isFinished()&&detPos!=position;detPos++);
                    listRes.add(detPos,listRes.remove(position));
                    //Collections.swap(listRes,position,detPos);
                    listAdapter.notifyItemMoved(position,detPos);
                    int rangeFrom = Math.min(position,detPos);
                    listAdapter.notifyItemRangeChanged(rangeFrom,listRes.size()-rangeFrom);
                    refreshText();
                } else Toast.makeText(HContext, "操作失败!", Toast.LENGTH_SHORT).show();
               // ActivityMain.saveData();
                // if(ftl!=null&&ftl.hasInit) ftl.Refresh(FragmentTimeLine.TL_REFRESH_FROM_UNHIDE);
            }

        }
    }

}
