package com.stupidtree.hita.activities;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.BaseListAdapter;
import com.stupidtree.hita.adapter.DDLItemAdapter;
import com.stupidtree.hita.adapter.ExamCDItemAdapter;
import com.stupidtree.hita.fragments.BaseOperationTask;
import com.stupidtree.hita.fragments.popup.FragmentAddEvent;
import com.stupidtree.hita.fragments.popup.FragmentAddTask;
import com.stupidtree.hita.timetable.TimetableCore;
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
import java.util.Objects;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.TPE;
import static com.stupidtree.hita.timetable.TimeWatcherService.TIMETABLE_CHANGED;
import static com.stupidtree.hita.timetable.TimetableCore.EXAM;

public class ActivityExamCountdown extends BaseActivity implements
        FragmentAddTask.AddTaskDoneListener, EditModeHelper.EditableContainer,
BaseOperationTask.OperationListener<Object>{


    public boolean hasInit = false;
    RecyclerView list;
    ExamCDItemAdapter listAdapter;
    FloatingActionButton fab;
    ArrayList<EventItem> listRes;
    ImageView none;
    Toolbar toolbar;
    BroadcastReceiver receiver;
    ViewGroup countDownLayout;
    TextView time1, time2, time3, tag1, tag2, tag3, next_name;
    ViewGroup nextLayout;
    EditModeHelper<EventItem> editModeHelper;

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
        toolbar.setTitle(getString(R.string.label_activity_exam_countdown));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
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
                if (!TimetableCore.getInstance(HContext).isDataAvailable()) {
                    Snackbar.make(v, getString(R.string.notif_importdatafirst), Snackbar.LENGTH_SHORT).show();
                    return;
                }
                FragmentAddEvent.newInstance().setInitialType("exam").show(getSupportFragmentManager(), "fae");
            }
        });
    }


    void initList() {
        nextLayout = findViewById(R.id.next_layout);
        list = findViewById(R.id.task_recycler);
        list.setItemViewCacheSize(Integer.MAX_VALUE);
        listRes = new ArrayList<>();
        listAdapter = new ExamCDItemAdapter(this, listRes);
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
                if (listRes.get(position).getEventType() != EventItem.TAG) {
                    EventsUtils.showEventItem(getThis(), listRes.get(position));
                }

            }
        });
        editModeHelper = new EditModeHelper<>(this, listAdapter, this);
        editModeHelper.init(this, R.id.edit_bar);

    }


    @Override
    public void onBackPressed() {
        if (editModeHelper.isEditMode()) editModeHelper.closeEditMode();
        else super.onBackPressed();
    }

    @SuppressLint("SetTextI18n")
    void refreshText(List<EventItem> list) {

        if (!TimetableCore.getInstance(HContext).isDataAvailable()) {
            none.setVisibility(View.VISIBLE);
            return;
        }
        if (list.size() == 0) {
            none.setVisibility(View.VISIBLE);
            countDownLayout.setVisibility(View.GONE);
        } else {
            countDownLayout.setVisibility(View.VISIBLE);
            none.setVisibility(View.GONE);

            EventItem toShow = null;
            if (list.size() > 0) {
                toShow = list.get(0);
            }
            if (toShow != null) {
                time1.setVisibility(View.VISIBLE);
                tag1.setVisibility(View.VISIBLE);
                time2.setVisibility(View.VISIBLE);
                tag2.setVisibility(View.VISIBLE);
                time3.setVisibility(View.VISIBLE);
                tag3.setVisibility(View.VISIBLE);
                next_name.setText(toShow.getMainName());
                final EventItem finalToShow = toShow;
                nextLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EventsUtils.showEventItem(getThis(), finalToShow);
                    }
                });
                long minutes = toShow.getInWhatTimeWillItHappen(TimetableCore.getInstance(HContext).getCurrentCurriculum(), TimetableCore.getNow());
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
                    time3.setText(hourS);
                    tag3.setText(R.string.name_hour);
                } else if (days <= 0 && hours <= 0 && minutes <= 0) {
                    time1.setText(R.string.timeline_head_ongoing_subtitle);
                    tag1.setVisibility(View.GONE);
                    time2.setVisibility(View.GONE);
                    tag2.setVisibility(View.GONE);
                    time3.setVisibility(View.GONE);
                    tag3.setVisibility(View.GONE);
                } else { //*天*时*分
                    time1.setText(dayS);
                    tag1.setText(R.string.name_day);
                    time2.setText(hourS);
                    tag2.setText(R.string.name_hour);
                    time3.setText(minuteS);
                    tag3.setText(R.string.name_minute);
                }
            }


        }
    }




    public void Refresh() {
        new refreshListTask(this).executeOnExecutor(TPE);
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
        new deleteDDLTask(this,listAdapter.getCheckedItem()).executeOnExecutor(TPE);
    }

    @Override
    public void onResume() {
        super.onResume();
        Refresh();
    }

    @Override
    public void onOperationStart(String id, Boolean[] params) {

    }

    @Override
    public void onOperationDone(String id, BaseOperationTask task, Boolean[] params, Object result) {
        switch (id){
            case "refresh":
                refreshListTask rt = (refreshListTask) task;
                List<EventItem> newList = new ArrayList<>();
                if (rt.result_todo.size() > 0)
                    newList.add(EventItem.getTagInstance(getString(R.string.exam_todo)));
                newList.addAll(rt.result_todo);
                if (rt.result_passed.size() > 0)
                    newList.add(EventItem.getTagInstance(getString(R.string.exam_passed)));
                newList.addAll(rt.result_passed);
                if (newList.size() == 0)
                    newList.add(EventItem.getTagInstance(getString(R.string.no_exam_add_some)));
                listAdapter.notifyItemChangedSmooth(newList, true);
                refreshText(rt.result_todo);
                break;
            case "delete":
                Refresh();
                editModeHelper.closeEditMode();
                Toast.makeText(getThis(), R.string.delete_success, Toast.LENGTH_SHORT).show();

        }
    }

    static class refreshListTask extends BaseOperationTask<Object> {

        List<EventItem> result_todo;
        List<EventItem> result_passed;

        refreshListTask(OperationListener listRefreshedListener) {
            super(listRefreshedListener);
            result_todo = new ArrayList<>();
            result_passed = new ArrayList<>();
            id = "refresh";
        }

        @Override
        protected Object doInBackground(OperationListener<Object> listRefreshedListener, Boolean... booleans) {
            List<EventItem> res = TimetableCore.getInstance(HContext).getAllEvents(EXAM);
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



    }

    static class deleteDDLTask extends BaseOperationTask<Object> {

        Collection<EventItem> toDelete;

        deleteDDLTask(OperationListener listRefreshedListener, Collection<EventItem> toDelete) {
            super(listRefreshedListener);
            this.toDelete = toDelete;
            id = "delete";
        }

        @Override
        protected Object doInBackground(OperationListener<Object> listRefreshedListener, Boolean... booleans) {
            if (toDelete != null) {
                for (EventItem ei : toDelete) {
                    TimetableCore.getInstance(HContext).deleteEvent(ei, true);
                }
            }
            return null;
        }

    }


}
