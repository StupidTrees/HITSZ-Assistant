package com.stupidtree.hita.fragments.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.BaseListAdapter;
import com.stupidtree.hita.adapter.TaskCardListAdapter;
import com.stupidtree.hita.adapter.TimelineListAdapter;
import com.stupidtree.hita.fragments.BaseFragment;
import com.stupidtree.hita.fragments.BasicRefreshTask;
import com.stupidtree.hita.timetable.TimetableCore;
import com.stupidtree.hita.timetable.packable.EventItem;
import com.stupidtree.hita.timetable.packable.HTime;
import com.stupidtree.hita.util.EventsUtils;
import com.stupidtree.hita.util.RefreshBroadcastReceiver;
import com.stupidtree.hita.views.WrapContentLinearLayoutManager;
import com.stupidtree.hita.views.pullextend.ExtendListHeader;
import com.stupidtree.hita.views.pullextend.PullExtendLayout;
import com.stupidtree.hita.views.pullextend.mRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static com.stupidtree.hita.HITAApplication.defaultSP;
import static com.stupidtree.hita.HITAApplication.timeTableCore;
import static com.stupidtree.hita.adapter.NaviPageAdapter.strToIntegerList;
import static com.stupidtree.hita.timetable.TimeWatcherService.TIMETABLE_CHANGED;


@SuppressLint("ValidFragment")
public class FragmentTimeLine extends BaseFragment implements
        RefreshBroadcastReceiver.ActionListener, TimelineListAdapter.TimeLineSelf,
        BasicRefreshTask.ListRefreshedListener<List<EventItem>[]> {
    private TimelineListAdapter listAdapter;
    private EventItem nowEvent;
    private EventItem nextEvent;
    private float nowProgress;
    private List<EventItem> todaysEvents;
    private mRecyclerView list;
    private List<EventItem> timelineRes;
    private RefreshBroadcastReceiver refreshReceiver;
    private LocalBroadcastManager localBroadcastManager;
    private RefreshTask pageTask;
    private ExtendListHeader mPullNewHeader;
    private PullExtendLayout mPullExtendLayout;
    private TaskCardListAdapter headerListAdapter;
    private MainFABController mainFABController;
    public FragmentTimeLine() {

    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        initReceiver();
        if (context instanceof MainFABController) {
            mainFABController = (MainFABController) context;
        }
    }


    private void initReceiver() {
        refreshReceiver = new RefreshBroadcastReceiver();
        refreshReceiver.setListener(this);
        localBroadcastManager = LocalBroadcastManager.getInstance(requireActivity());
        IntentFilter iF = new IntentFilter();
        iF.addAction(TIMETABLE_CHANGED);
        localBroadcastManager.registerReceiver(refreshReceiver, iF);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        localBroadcastManager.unregisterReceiver(refreshReceiver);
        mainFABController = null;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initListAndAdapter(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        Refresh(true);
    }

    public boolean isHeaderExpanded() {
        if (mPullNewHeader == null) return false;
        return mPullNewHeader.isExpanded();
    }

    public void closeHeader() {
        if (mPullExtendLayout != null) mPullExtendLayout.closeExtendHeadAndFooter();
    }
    @Override
    public void receive(Context context, Intent intent) {
        Log.e("Timeline接收到广播", Objects.requireNonNull(intent.getAction()));
        if (isResumed()) {
            if (Objects.equals(intent.getAction(), TIMETABLE_CHANGED)) {
                Refresh(true);
            }
        }
    }


    @Override
    protected void stopTasks() {
        if (pageTask != null && pageTask.getStatus() != AsyncTask.Status.FINISHED)
            pageTask.cancel(true);
    }

    @Override
    public void Refresh() {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_timeline;
    }

    @Override
    public EventItem getNowEvent() {
        return nowEvent;
    }

    @Override
    public EventItem getNextvent() {
        return nextEvent;
    }

    @Override
    public float getNowProgress() {
        return nowProgress;
    }

    @Override
    public List<EventItem> getTodayEvents() {
        return todaysEvents;
    }

    @Override
    public void onRefreshStart(String id, Boolean[] params) {

    }

    @Override
    public void onListRefreshed(String id, Boolean[] params, List<EventItem>[] result) {
        if (isDetached() || isRemoving() || result == null || result.length != 2 || params.length < 1)
            return;
        List<EventItem> todayEventsToAdd = result[1];
        List<EventItem> toAdd = result[0];
        boolean notifyAll = params[0];
        todaysEvents.clear();
        todaysEvents.addAll(todayEventsToAdd);
        refreshNowAndNextEvent();
        try {
            RecyclerView.ViewHolder holder = list.findViewHolderForAdapterPosition(0);
            if (holder != null) {
                TimelineListAdapter.timelineHeaderHolder header = (TimelineListAdapter.timelineHeaderHolder) holder;
                header.UpdateHeadView();
            }
            listAdapter.notifyItemChangedSmooth(toAdd, notifyAll);
        } catch (Exception e) {
            e.printStackTrace();
            listAdapter.notifyItemChangedSmooth(toAdd, notifyAll);
        }
        headerListAdapter.notifyDataSetChanged();
    }

    private void initListAndAdapter(View v) {
        //头部加载更多
        todaysEvents = new ArrayList<>();

        mPullNewHeader = v.findViewById(R.id.extend_header);
        mPullExtendLayout = v.findViewById(R.id.pull_extend);
        RecyclerView mListHeader = mPullNewHeader.getRecyclerView();
        mListHeader.setLayoutManager(new GridLayoutManager(getContext(), 3));
        mPullNewHeader.setOnExpandListener(new ExtendListHeader.OnExpandListener() {
            @Override
            public void onExpand() {
                if (mainFABController != null) mainFABController.fabHide();
            }

            @Override
            public void onCollapse() {
                if (mainFABController != null) mainFABController.fabShow();
            }
        });
        mPullExtendLayout.setList_margin_top(requireContext().getResources().getDimensionPixelSize(R.dimen.timeline_margin_top));
        List<Integer> headRes = strToIntegerList(defaultSP.getString("task_page_order",
                "[]"));
        if (headRes.size() == 0) {
            headRes.add(TaskCardListAdapter.TYPE_TASK);
            headRes.add(TaskCardListAdapter.TYPE_DDL);
            headRes.add(TaskCardListAdapter.TYPE_EXAM);
        }
        headerListAdapter = new TaskCardListAdapter(getBaseActivity(), headRes);
        mListHeader.setAdapter(headerListAdapter);
        TaskCardListAdapter.mCallBack mCallBack = new TaskCardListAdapter.mCallBack(headerListAdapter);
        ItemTouchHelper helper = new ItemTouchHelper(mCallBack);
        helper.attachToRecyclerView(mListHeader);


        LinearLayoutManager layoutManager = new WrapContentLinearLayoutManager(this.getContext());
        list = v.findViewById(R.id.timelinelist);
        list.setItemViewCacheSize(Integer.MAX_VALUE);
        timelineRes = new ArrayList<>();
        listAdapter = new TimelineListAdapter(this.getContext(), this, timelineRes);
        list.setAdapter(listAdapter);
        list.setLayoutManager(layoutManager);
        listAdapter.setOnItemClickListener(new BaseListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View card, int position) {
                EventsUtils.showEventItem(getBaseActivity(), timelineRes.get(position));
            }
        });
//        listAdapter.setOnItemLongClickListener(new BaseListAdapter.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(View view, int position) {
//                if (timelineRes.get(position).eventType != TimetableCore.COURSE && timelineRes.get(position).eventType != DYNAMIC) {
//                    ExplosionField ef = ExplosionField.attach2Window(Objects.requireNonNull(FragmentTimeLine.this.getActivity()));
//                    ef.explode(view);
//                    new DeleteTask_timeline(position).executeOnExecutor(HITAApplication.TPE);
//                    return true;
//                }
//                return false;
//            }
//        });

    }

    public int getTodayCourseNum() {
        int result = 0;
        for (EventItem ei : todaysEvents) {
            if (ei.eventType == TimetableCore.COURSE) {
                result++;
            }
        }
        return result;
    }

    @SuppressLint("SetTextI18n")


    private void Refresh(boolean notifyAll) {
        if (!isResumed()) return;
        if (pageTask != null && pageTask.getStatus() != AsyncTask.Status.FINISHED)
            pageTask.cancel(true);
        pageTask = new RefreshTask(this);
        pageTask.executeOnExecutor(HITAApplication.TPE, notifyAll);
    }

    private void refreshNowAndNextEvent() {
        HTime nowTime = new HTime(timeTableCore.getNow());
        try {
            boolean changed_now = false;
            boolean changed_next = false;
            for (int i = todaysEvents.size() - 1; i >= 0; i--) {
                EventItem ei = todaysEvents.get(i);
                if (ei.hasCross(nowTime) && (!ei.isWholeDay())
                        && ei.eventType != TimetableCore.DDL
                ) {
                    nowEvent = ei;
                    changed_now = true;
                } else if (ei.startTime.compareTo(nowTime) > 0) {
                    nextEvent = ei;
                    changed_next = true;
                }
            }
            if (!changed_next) nextEvent = null;
            if (!changed_now) nowEvent = null;
            if (nowEvent != null) {
                nowProgress = ((float) new HTime(timeTableCore.getNow()).getDuration(nowEvent.startTime)) / ((float) nowEvent.endTime.getDuration(nowEvent.startTime));

            }
        } catch (Exception e) {
            e.printStackTrace();
//            nowEvent = null;
//            nextEvent = null;
        }

        //if(nowEvent==null&&)

    }


    public interface MainFABController {
        void fabRise();

        void fabHide();

        void fabShow();
    }

    private static class RefreshTask extends BasicRefreshTask<ArrayList[]> {


        RefreshTask(ListRefreshedListener listRefreshedListener) {
            super(listRefreshedListener);
        }

        @Override
        protected ArrayList[] doInBackground(ListRefreshedListener listRefreshedListener, Boolean... booleans) {
            ArrayList<EventItem> todayEventsToAdd = new ArrayList<>();
            ArrayList<EventItem> toAdd = new ArrayList<>();
            ArrayList[] result = new ArrayList[]{toAdd, todayEventsToAdd};
            if (!timeTableCore.isDataAvailable()) return result;
            if (!timeTableCore.isThisTerm() || !timeTableCore.isDataAvailable()) {
                return result;
            }
            todayEventsToAdd.addAll(timeTableCore.getOneDayEvents(timeTableCore.getThisWeekOfTerm(), TimetableCore.getDOW(timeTableCore.getNow())));
            List<EventItem> wholeDayRes = new ArrayList<>();
            for (EventItem ei : todayEventsToAdd) {
                if (ei.isWholeDay()) wholeDayRes.add(ei);
                else toAdd.add(ei);
            }
            Collections.sort(toAdd, new Comparator<EventItem>() {
                @Override
                public int compare(EventItem o1, EventItem o2) {
                    return o1.getStartTime().compareTo(o2.getStartTime());
                }
            });
            toAdd.addAll(0, wholeDayRes);
            return result;
        }





    }


}
