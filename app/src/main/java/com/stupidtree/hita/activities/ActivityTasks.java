package com.stupidtree.hita.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.BaseListAdapter;
import com.stupidtree.hita.adapter.DDLItemAdapter;
import com.stupidtree.hita.adapter.TaskListAdapter;
import com.stupidtree.hita.fragments.popup.FragmentAddTask;
import com.stupidtree.hita.timetable.packable.Task;
import com.stupidtree.hita.views.EditModeHelper;
import com.stupidtree.hita.views.WrapContentLinearLayoutManager;

import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.timeTableCore;

public class ActivityTasks extends BaseActivity implements
        FragmentAddTask.AddTaskDoneListener, EditModeHelper.EditableContainer {
    RecyclerView tasksList_now;
    TaskListAdapter listAdapter_now;
    FloatingActionButton fab;
    ArrayList<Task> listRes_now;
    ImageView none;
    refreshListTask pageTask;
    Toolbar toolbar;
    CollapsingToolbarLayout collapsingToolbarLayout;
    EditModeHelper editModeHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);
        setWindowParams(true, false, false);
        initToolbar();
        initList();
    }


    void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        collapsingToolbarLayout = findViewById(R.id.collapse);
        toolbar.setTitle(getString(R.string.label_activity_tasks));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        collapsingToolbarLayout.setExpandedTitleColor(Color.WHITE);
        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
        fab = findViewById(R.id.task_fab);
        none = findViewById(R.id.none_img1);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timeTableCore.isDataAvailable()) showAddTaskDialog();
                else
                    Snackbar.make(v, getString(R.string.notif_importdatafirst), Snackbar.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onBackPressed() {
        if (editModeHelper.isEditMode()) editModeHelper.closeEditMode();
        else super.onBackPressed();
    }

    void initList() {
        tasksList_now = findViewById(R.id.task_recycler);
        tasksList_now.setItemViewCacheSize(Integer.MAX_VALUE);
        //taskList_done = findViewById(R.id.task_recycler_done);
        listRes_now = new ArrayList<>();
        // listRes_done = new ArrayList<>();

        listAdapter_now = new TaskListAdapter(this, listRes_now);
        tasksList_now.setLayoutManager(new WrapContentLinearLayoutManager(this, RecyclerView.VERTICAL, false));
        //taskList_done.setLayoutManager(new WrapContentLinearLayoutManager(this, RecyclerView.VERTICAL, false));
        tasksList_now.setAdapter(listAdapter_now);
        //taskList_done.setAdapter(listAdapter_done);

        listAdapter_now.setOnItemClickListener(new BaseListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View card, int position) {
                //if(listRes_timeTableCore.getNow().get(position).getType()!=Task.TAG)new TaskDialog(getThis(),listRes_timeTableCore.getNow().get(position)).show();
            }
        });
        listAdapter_now.setOnItemSelectedListener(new DDLItemAdapter.OnItemSelectedListener() {
            @Override
            public void OnItemSelected(View v, boolean checked, int position, int ttNum) {
                collapsingToolbarLayout.setTitle(getString(R.string.number_of_items_selected, ttNum));
                // selectedNum.setText(getString(R.string.number_of_items_selected, ttNum));
                if (ttNum == 0) editModeHelper.closeEditMode();
            }
        });
        listAdapter_now.setOnItemLongClickListener(new BaseListAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(View view, int position) {
                if (listRes_now.get(position).getType() != Task.TAG && !editModeHelper.isEditMode()) {
                    editModeHelper.activateEditMode(position);
                    return true;
                } else return false;
            }
        });

        listAdapter_now.setOnFinishClickListener(new TaskListAdapter.OnFinishClickListener() {
            @Override
            public boolean OnClick(View v, Task t, int position) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                new finishTask(t, !t.isFinished(), position, listRes_now, listAdapter_now).execute();
                return true;
            }

        });

        editModeHelper = new EditModeHelper(this, listAdapter_now, this);
        editModeHelper.init(this);
    }

    void showAddTaskDialog() {
        new FragmentAddTask().show(Objects.requireNonNull(this).getSupportFragmentManager(), "fat");
    }

    void refreshText() {
        if (!timeTableCore.isDataAvailable()) {
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
        // Log.e("refresh", "tasks");
        if (pageTask != null && pageTask.getStatus() != AsyncTask.Status.FINISHED)
            pageTask.cancel(true);
        pageTask = new refreshListTask();
        pageTask.executeOnExecutor(HITAApplication.TPE);
    }


    @Override
    public void OnDone() {
        Refresh();
    }

    @Override
    public void onEditClosed() {
        if (fab != null) fab.show();
        collapsingToolbarLayout.setTitle(getString(R.string.label_activity_tasks));
        // collapsingToolbarLayout.setTitleEnabled(true);
        toolbar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onEditStarted() {
        fab.hide();
        toolbar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onItemCheckedChanged(int position, boolean checked, int currentSelected) {
        collapsingToolbarLayout.setTitle(getString(R.string.number_of_items_selected, currentSelected));
    }

    @Override
    public void onDelete(Collection toDelete) {
        new deleteTasks().execute();
    }


    @SuppressLint("StaticFieldLeak")
    class refreshListTask extends AsyncTask {
        List<Task> nowRes;
        List<Task> finishedRes;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            nowRes = timeTableCore.getUnfinishedTasks();
            finishedRes = timeTableCore.getFinishedTasks();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            List<Task> newL = new ArrayList<>();
            newL.addAll(nowRes);
            if (finishedRes.size() > 0)
                newL.add(Task.getTagInstance(getString(R.string.task_finished_name)));
            newL.addAll(finishedRes);
            listAdapter_now.notifyItemChangedSmooth(newL);
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
            timeTableCore.deleteTask(listRes.get(position));
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (isDestroyed()) return;
            ActivityMain.saveData();
            Refresh();
            // if(ftl!=null&&ftl.hasInit) ftl.Refresh(FragmentTimeLine.TL_REFRESH_FROM_UNHIDE);
        }
    }

    class finishTask extends AsyncTask {
        int position;
        List<Task> listRes;
        Task t;
        TaskListAdapter listAdapter;
        boolean finished;

        public finishTask(Task t, boolean finished, int position, List<Task> listRes, TaskListAdapter listAdapter) {
            this.t = t;
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
                return timeTableCore.setFinishTask(t, finished);
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
                ActivityMain.saveData();
                if ((Boolean) o) {
                    Refresh();
                } else
                    Toast.makeText(HContext, R.string.operation_failed, Toast.LENGTH_SHORT).show();

            }

        }
    }

    class deleteTasks extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            if (listAdapter_now.getCheckedItem() != null) {
                for (Task t : listAdapter_now.getCheckedItem()) {
                    timeTableCore.deleteTask(t);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            Refresh();
            editModeHelper.closeEditMode();
            Toast.makeText(getThis(), R.string.delete_success, Toast.LENGTH_SHORT).show();
        }
    }
}
