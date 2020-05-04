package com.stupidtree.hita.activities;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.BaseListAdapter;
import com.stupidtree.hita.adapter.DDLItemAdapter;
import com.stupidtree.hita.fragments.popup.FragmentAddEvent;
import com.stupidtree.hita.fragments.popup.FragmentAddTask;
import com.stupidtree.hita.timetable.packable.EventItem;
import com.stupidtree.hita.util.EventsUtils;
import com.stupidtree.hita.views.EditModeHelper;
import com.stupidtree.hita.views.WrapContentLinearLayoutManager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.timeTableCore;
import static com.stupidtree.hita.timetable.TimeWatcherService.TIMETABLE_CHANGED;
import static com.stupidtree.hita.timetable.TimetableCore.DDL;

public class ActivityDDLManager extends BaseActivity implements
        FragmentAddTask.AddTaskDoneListener, EditModeHelper.EditableContainer {


    public boolean hasInit = false;
    RecyclerView list;
    DDLItemAdapter listAdapter;
    FloatingActionButton fab;
    ArrayList<EventItem> listRes;
    ImageView none;
    refreshListTask pageTask;
    Toolbar toolbar;
    BroadcastReceiver receiver;
    ViewGroup countDownLayout;
    TextView time1, time2, time3, tag1, tag2, tag3, next_name;
    EditModeHelper editModeHelper;
    ViewGroup nextLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ddl_manager);
        setWindowParams(true, false, false);
        initToolbar();
        initReceiver();
        initCD();
        initList();
        hasInit = true;
    }

    void initReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!editModeHelper.isEditMode()) Refresh();
            }
        };
        IntentFilter iF = new IntentFilter();
        iF.addAction(TIMETABLE_CHANGED);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, iF);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    void initCD() {
        countDownLayout = findViewById(R.id.count_down);
        time1 = findViewById(R.id.time1);
        time2 = findViewById(R.id.time2);
        time3 = findViewById(R.id.time3);
        tag1 = findViewById(R.id.tag1);
        tag2 = findViewById(R.id.tag2);
        tag3 = findViewById(R.id.tag3);
        next_name = findViewById(R.id.next_name);
    }


    void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.label_activity_ddl_manager));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        fab = findViewById(R.id.task_fab);
        none = findViewById(R.id.none_img1);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!timeTableCore.isDataAvailable()) {
                    Snackbar.make(v, getString(R.string.notif_importdatafirst), Snackbar.LENGTH_SHORT).show();
                    return;
                }
                FragmentAddEvent.newInstance().setInitialType("ddl").show(getSupportFragmentManager(), "fae");
            }
        });

    }


    void initList() {
        nextLayout = findViewById(R.id.next_layout);
        list = findViewById(R.id.task_recycler);
        list.setItemViewCacheSize(Integer.MAX_VALUE);
        listRes = new ArrayList<>();
        listAdapter = new DDLItemAdapter(this, listRes);
        list.setLayoutManager(new WrapContentLinearLayoutManager(this, RecyclerView.VERTICAL, false));
        list.setAdapter(listAdapter);
        listAdapter.setOnItemLongClickListener(new DDLItemAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(View view, int position) {
                if (listRes.get(position).getEventType() != EventItem.TAG) {
                    editModeHelper.activateEditMode(position);
                    return true;
                } else return false;
            }

        });
        listAdapter.setOnItemClickListener(new BaseListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View card, int position) {
                if (listRes.get(position).getEventType() == EventItem.TAG) return;
                EventsUtils.showEventItem(getThis(), listRes.get(position));
            }
        });
        editModeHelper = new EditModeHelper(this, listAdapter, this);
        editModeHelper.init(this, R.id.edit_bar);
    }


    @Override
    public void onBackPressed() {
        if (editModeHelper.isEditMode()) editModeHelper.closeEditMode();
        else super.onBackPressed();
    }

    @SuppressLint("SetTextI18n")
    void refreshText(List<EventItem> list) {

        if (!timeTableCore.isDataAvailable()) {
            none.setVisibility(View.VISIBLE);
            return;
        }
        if (list.size() == 0) {
            none.setVisibility(View.VISIBLE);
            countDownLayout.setVisibility(View.GONE);
        } else {
            countDownLayout.setVisibility(View.VISIBLE);
            none.setVisibility(View.GONE);
            List<EventItem> today = new ArrayList<>();
            List<EventItem> notToday = new ArrayList<>();
            for (EventItem ex : list) {
                if (ex.isSameDay(timeTableCore.getCurrentCurriculum(), timeTableCore.getNow())) { //今天的
                    today.add(ex);
                } else {
                    notToday.add(ex);
                }
            }

            boolean showTodayWholeday = false;
            EventItem toShow = null;
            if (today.size() > 0) { //今天有ddl，优先显示今天的
                for (EventItem t : today) {
                    if (!t.isWholeDay()) { //优先显示非全天的
                        toShow = t;
                        showTodayWholeday = false;
                        break;
                    }
                }
                if (toShow == null) showTodayWholeday = true;//否则，显示全天
            } else if (notToday.size() > 0) {
                showTodayWholeday = false;
                toShow = notToday.get(0); //否则，选择非今天的第一个事件
            }

            if (toShow != null) {
                final EventItem finalToShow = toShow;
                nextLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EventsUtils.showEventItem(getThis(), finalToShow);
                    }
                });
            }
            if (showTodayWholeday) { //如果只显示今日的全天事件
                time2.setVisibility(View.GONE);
                tag2.setVisibility(View.GONE);
                time3.setVisibility(View.GONE);
                tag3.setVisibility(View.GONE);
                time1.setVisibility(View.VISIBLE);
                tag1.setVisibility(View.VISIBLE);
                time1.setText(R.string.today);
                tag1.setText(getString(R.string.total_wholeday_event, today.size()));
                next_name.setText(today.get(0).getMainName() + (today.size() > 1 ? "..." : ""));
            } else if (toShow != null) {
                time1.setVisibility(View.VISIBLE);
                tag1.setVisibility(View.VISIBLE);
                time2.setVisibility(View.VISIBLE);
                tag2.setVisibility(View.VISIBLE);
                time3.setVisibility(View.VISIBLE);
                tag3.setVisibility(View.VISIBLE);
                next_name.setText(toShow.getMainName());
                long minutes = toShow.getInWhatTimeWillItHappen(timeTableCore.getCurrentCurriculum(), timeTableCore.getNow());
                int weeks = (int) (minutes / 10080);
                minutes %= 10080;
                int days = (int) (minutes / 1440);
                minutes %= 1440;
                int hours = (int) (minutes / 60);
                minutes %= 60;
                DecimalFormat df = new DecimalFormat("00");
                String weekS = df.format(weeks);
                String dayS = df.format(days);
                String hourS = df.format(hours);
                String minuteS = df.format(minutes);
                if (weeks > 0) { //*周 *天 *时
                    time1.setText(weekS);
                    tag1.setText(R.string.name_week);
                    time2.setText(dayS);
                    tag2.setText(R.string.name_day);
                    if (toShow.isWholeDay()) {
                        time3.setVisibility(View.GONE);
                        tag3.setVisibility(View.GONE);
                    } else {
                        time3.setText(hourS);
                        tag3.setText(R.string.name_hour);
                        time3.setVisibility(View.VISIBLE);
                        tag3.setVisibility(View.VISIBLE);
                    }
                } else { //*天*时*分
                    time1.setText(dayS);
                    tag1.setText(R.string.name_day);
                    time2.setText(hourS);
                    tag2.setText(R.string.name_hour);
                    time3.setText(minuteS);
                    tag3.setText(R.string.name_minute);
                    if (toShow.isWholeDay()) {
                        time2.setVisibility(View.GONE);
                        tag2.setVisibility(View.GONE);
                        time3.setVisibility(View.GONE);
                        tag3.setVisibility(View.GONE);
                    } else {
                        time2.setVisibility(View.VISIBLE);
                        tag2.setVisibility(View.VISIBLE);
                        time3.setVisibility(View.VISIBLE);
                        tag3.setVisibility(View.VISIBLE);
                    }
                }
            }


        }
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
    }

    @Override
    public void onEditStarted() {
        if (fab != null) fab.hide();
    }

    @Override
    public void onItemCheckedChanged(int position, boolean checked, int currentSelected) {

    }

    @Override
    public void onDelete(Collection toDelete) {
        new deleteDDLTask().execute();
    }

    @Override
    public void onResume() {
        super.onResume();
        Refresh();
    }

    @SuppressLint("StaticFieldLeak")
    class refreshListTask extends AsyncTask {

        List<EventItem> result_todo;
        List<EventItem> result_passed;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            result_todo = new ArrayList<>();
            result_passed = new ArrayList<>();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            List<EventItem> res = timeTableCore.getAllEvents(DDL);
            for (EventItem ei : res) {
                if (ei.hasPassed(System.currentTimeMillis())) result_passed.add(ei);
                else result_todo.add(ei);
            }
            Collections.sort(result_passed, new Comparator<EventItem>() {
                @Override
                public int compare(EventItem o1, EventItem o2) {
                    return o2.compareTo(o1);
                }
            });
            Collections.sort(result_todo, new Comparator<EventItem>() {
                @Override
                public int compare(EventItem o1, EventItem o2) {
                    return o1.compareTo(o2);
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            List<EventItem> newList = new ArrayList<>();
            if (result_todo.size() > 0)
                newList.add(EventItem.getTagInstance(getString(R.string.ddl_todo)));
            newList.addAll(result_todo);
            if (result_passed.size() > 0)
                newList.add(EventItem.getTagInstance(getString(R.string.ddl_passed_tag)));
            newList.addAll(result_passed);
            if (newList.size() == 0)
                newList.add(EventItem.getTagInstance(getString(R.string.no_ddl_add_some)));

            listAdapter.notifyItemChangedSmooth(newList);
            refreshText(result_todo);

        }
    }

    class deleteDDLTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            if (listAdapter.getCheckedItem() != null) {
                for (EventItem ei : listAdapter.getCheckedItem()) {
                    timeTableCore.deleteEvent(ei, true);
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
