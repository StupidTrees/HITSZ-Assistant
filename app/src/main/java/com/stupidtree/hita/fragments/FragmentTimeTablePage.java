package com.stupidtree.hita.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.core.widget.NestedScrollView;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.timetable.timetable.HTime;
import com.stupidtree.hita.diy.TimeTableViewGroup;
import com.stupidtree.hita.hita.TextTools;
import com.stupidtree.hita.R;
import com.stupidtree.hita.timetable.timetable.EventItem;
import com.stupidtree.hita.util.TimeTableNowLine;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.defaultSP;
import static com.stupidtree.hita.HITAApplication.now;
import static com.stupidtree.hita.HITAApplication.timeTableCore;
import static com.stupidtree.hita.fragments.main.FragmentTimeLine.showEventDialog;


public class FragmentTimeTablePage extends BaseFragment {

    public boolean hasInit = false;
    View pageView;
    public int pageWeek;
   // boolean curiculumOnly;
    boolean wholeday;
    boolean colorfulMode,drawNowLine;
    NestedScrollView scrollView;
    TimeTableViewGroup timeTableView;
    int start;
    /*辅助性变量区*/
    Calendar temp0 = Calendar.getInstance();
    /*UI类常量*/
    static int CARD_HEIGHT = 160;//课程表卡片高度

    /*控件对象区*/
    TextView[] tdays = new TextView[8]; //顶部日期文本
    TextView[] tWholeDays = new TextView[7];
    CardView[] tWholeDayCards = new CardView[7];
    LinearLayout classNumberLayout;
    LinearLayout wholedayLayout;
    refreshPageTask pageTask;
    BroadcastReceiver receiver;
    IntentFilter ift;
    LocalBroadcastManager localBroadcastManager;

    public FragmentTimeTablePage() {
        localBroadcastManager = LocalBroadcastManager.getInstance(HContext);
        ift = new IntentFilter();
        ift.addAction("COM.STUPIDTREE.HITA.TIMETABLE_PAGE_REFRESH");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.e("timetable_page" + pageWeek, "收到刷新广播");
                if (FragmentTimeTablePage.this.isDetached() || FragmentTimeTablePage.this.isRemoving() || FragmentTimeTablePage.this.isStateSaved())
                    return;
                if (intent.getIntExtra("week", 1) == pageWeek) RefreshPageView(pageWeek);
            }
        };
        // Required empty public constructor
    }


    public static FragmentTimeTablePage newInstance(int pageWeek) {
        FragmentTimeTablePage fragment = new FragmentTimeTablePage();
        Bundle args = new Bundle();
        args.putInt("week", pageWeek);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CARD_HEIGHT = defaultSP.getInt("TimeTable_cardheight", 160);//课程表卡片高度

        if (getArguments() != null) {
            pageWeek = getArguments().getInt("week");
        }
      //  curiculumOnly = defaultSP.getBoolean("timetable_curriculumonly", false);
        wholeday = defaultSP.getBoolean("timetable_wholeday", false);
        colorfulMode = defaultSP.getBoolean("timetable_colorful_mode", true);
        drawNowLine= defaultSP.getBoolean("timetable_draw_now_line", true);

        start = wholeday ? 0 : 8;


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dynamic_timetable_page, null);
        pageView = v;
        initDateTextViews(v);
        initWholeDayCardViews(v);
        initWholeDayTextViews(v);
        classNumberLayout = v.findViewById(R.id.tt_class_number_layout);
        createLeftView();
        scrollView = v.findViewById(R.id.timetable_scroll);
        hasInit = true;
        RefreshPageView(pageWeek);
        timeTableView = v.findViewById(R.id.timetableview);
        return v;
    }

    /*初始化_创建左侧时间标签*/
    private void createLeftView() {
        if (classNumberLayout.getChildCount() > 0) classNumberLayout.removeAllViews();
        View view;
        TextView text;
        for (int i = start; i < 24; i++) {
            view = LayoutInflater.from(this.getContext()).inflate(R.layout.dynamic_timetable_left_time, null);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, CARD_HEIGHT);
            //params.setMargins(0, 0, 20, 0);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                view.setForegroundGravity(Gravity.LEFT);
            }
            view.setLayoutParams(params);
            text = view.findViewById(R.id.tt_left_time_text);
            text.setText(i + ":00");
            classNumberLayout.addView(view);
        }

    }

    void initDateTextViews(View v) {
        tdays[0] = v.findViewById(R.id.tt_tv_month);
        tdays[1] = v.findViewById(R.id.tt_tv_day1);
        tdays[2] = v.findViewById(R.id.tt_tv_day2);
        tdays[3] = v.findViewById(R.id.tt_tv_day3);
        tdays[4] = v.findViewById(R.id.tt_tv_day4);
        tdays[5] = v.findViewById(R.id.tt_tv_day5);
        tdays[6] = v.findViewById(R.id.tt_tv_day6);
        tdays[7] = v.findViewById(R.id.tt_tv_day7);
    }

    void initWholeDayTextViews(View v) {
        tWholeDays[0] = v.findViewById(R.id.tt_wholeday_text_1);
        tWholeDays[1] = v.findViewById(R.id.tt_wholeday_text_2);
        tWholeDays[2] = v.findViewById(R.id.tt_wholeday_text_3);
        tWholeDays[3] = v.findViewById(R.id.tt_wholeday_text_4);
        tWholeDays[4] = v.findViewById(R.id.tt_wholeday_text_5);
        tWholeDays[5] = v.findViewById(R.id.tt_wholeday_text_6);
        tWholeDays[6] = v.findViewById(R.id.tt_wholeday_text_7);
        wholedayLayout = v.findViewById(R.id.wholeday_layout);
    }

    void initWholeDayCardViews(View v) {
        tWholeDayCards[0] = v.findViewById(R.id.tt_wholeday_card_1);
        tWholeDayCards[1] = v.findViewById(R.id.tt_wholeday_card_2);
        tWholeDayCards[2] = v.findViewById(R.id.tt_wholeday_card_3);
        tWholeDayCards[3] = v.findViewById(R.id.tt_wholeday_card_4);
        tWholeDayCards[4] = v.findViewById(R.id.tt_wholeday_card_5);
        tWholeDayCards[5] = v.findViewById(R.id.tt_wholeday_card_6);
        tWholeDayCards[6] = v.findViewById(R.id.tt_wholeday_card_7);
    }

    @SuppressLint("SetTextI18n")
    private void refreshDateViews() {

        try {
            /*显示上方日期*/
            tdays[0].setText(HContext.getResources().getStringArray(R.array.months)[timeTableCore.getCurrentCurriculum().getFirstDateAtWOT(pageWeek).get(Calendar.MONTH)]);
            Calendar firstDateTemp = timeTableCore.getCurrentCurriculum().getFirstDateAtWOT(pageWeek);
            Calendar temp = Calendar.getInstance();
            for (int k = 1; k <= 7; k++) {
                temp.setTime(firstDateTemp.getTime());
                temp.add(Calendar.DATE, k - 1);
                tdays[k].setText(temp.get(Calendar.DAY_OF_MONTH) + "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void RefreshPageView(int week) {
        if (pageTask != null && pageTask.getStatus()!=AsyncTask.Status.FINISHED) pageTask.cancel(true);
        pageTask = new refreshPageTask(week);
        pageTask.executeOnExecutor(HITAApplication.TPE);
    }

    public void NotifyRefresh() {
        if (!hasInit) return;
        CARD_HEIGHT = defaultSP.getInt("TimeTable_cardheight", 160);//课程表卡片高度
       // curiculumOnly = defaultSP.getBoolean("timetable_curriculumonly", false);
        wholeday = defaultSP.getBoolean("timetable_wholeday", false);
        colorfulMode = defaultSP.getBoolean("timetable_colorful_mode", true);
        drawNowLine = defaultSP.getBoolean("timetable_draw_now_line", true);

        start = wholeday ? 0 : 8;
        createLeftView();
        RefreshPageView(pageWeek);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        localBroadcastManager.registerReceiver(receiver, ift);

        if (context instanceof OnFragmentInteractionListener) {

        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        Log.e("detach", ",");
        super.onDetach();
        localBroadcastManager.unregisterReceiver(receiver);

    }

    @Override
    protected void stopTasks() {
        if (pageTask != null && pageTask.getStatus()!=AsyncTask.Status.FINISHED) pageTask.cancel(true);
    }

    @Override
    public void Refresh() {

    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    class refreshPageTask extends AsyncTask<String, Integer, List<Object>> {

        int week;

        refreshPageTask(int week) {
            this.week = week;
        }

        @Override
        protected List<Object> doInBackground(String... strings) {
            List<Object> res = new ArrayList<>();
            if (timeTableCore.getCurrentCurriculum() == null) return res;
            for (int p = 1; p <= 7; p++) {
                List<EventItem> oneDayEvent = timeTableCore.getOneDayEvents(week, p);
                List<Object> oneDay = new ArrayList<Object>();
                List<Integer> usedIndex = new ArrayList<>();
                for (int i = 0; i < oneDayEvent.size(); i++) {
                    if (usedIndex.contains(i)) continue;
                    EventItem ei = oneDayEvent.get(i);
                    if(ei.isWholeDay){
                        oneDay.add(ei);
                        continue;
                    }
                    List<EventItem> result = new ArrayList<>();
                    result.add(ei);
                    for (int j = i + 1; j < oneDayEvent.size(); j++) {
                        EventItem x = oneDayEvent.get(j);
                        if(x.isWholeDay) continue;
                        if (ei.startTime.equals(x.startTime) && ei.endTime.equals(x.endTime)) {
                            result.add(x);
                            usedIndex.add(j);
                        }
                    }
                    if (result.size() > 1&&!ei.isWholeDay) oneDay.add(result);
                    else oneDay.add(ei);
                }
                res.addAll(oneDay);
                // }
            }
            return res;
        }

        @Override
        protected void onPostExecute(List<Object> lists) {
            super.onPostExecute(lists);
            try {
                if (!hasInit) return;
                if (!timeTableCore.isDataAvailable()) return;
                refreshDateViews();
                if (wholeday) scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.scrollTo(0, 8 * CARD_HEIGHT);
                    }
                });
                timeTableView.removeAllViews();
                timeTableView.init(getActivity(), pageWeek, CARD_HEIGHT, new HTime(start, 0), getBGIconColor(), colorfulMode);
                for (CardView cv : tWholeDayCards) {
                    cv.setVisibility(View.GONE);
                }
                boolean hasWholedayWholeWeek = false;
                ArrayList <List<EventItem>> wholeDayMap = new ArrayList<>();
                for (int i = 0; i < 7; i++) {
                    wholeDayMap.add(new ArrayList<EventItem>());
                }
                for (Object o : lists) {
                    if (o instanceof EventItem) {
                        if (!((EventItem) o).isWholeDay) timeTableView.addBlock(o);
                        else {
                            wholeDayMap.get(((EventItem) o).DOW-1).add((EventItem) o);
                            Log.e("add",o.toString());
                        }
                    }else if(o instanceof List){
                        timeTableView.addBlock(o);
                    }
                }
                for (int p=0;p<7;p++) {
                    if (wholeDayMap.get(p).size() > 0) {
                        hasWholedayWholeWeek = true;
                        tWholeDayCards[p].setVisibility(View.VISIBLE);
                        tWholeDays[p].setText(wholeDayMap.get(p).size() + "");
                        tWholeDayCards[p].setOnClickListener(new OnWholeDayCardClickListener(wholeDayMap.get(p), p+1));
                    } else tWholeDayCards[p].setVisibility(View.GONE);
                }
                if (hasWholedayWholeWeek) wholedayLayout.setVisibility(View.VISIBLE);
                else wholedayLayout.setVisibility(View.GONE);

                if(pageWeek==timeTableCore.getThisWeekOfTerm()&&drawNowLine&&new HTime(now).after(new HTime(start,0))){
                    timeTableView.addView(new TimeTableNowLine(getContext(),getColorPrimary()));
                }


            } catch (
                    Exception e) {
                e.printStackTrace();
            }
        }
    }


    class OnWholeDayCardClickListener implements View.OnClickListener {

        List<EventItem> res;
        String[] dialogItem;
        AlertDialog ad;

        OnWholeDayCardClickListener(final List<EventItem> res, int dow) {
            this.res = res;
            dialogItem = new String[res.size()];
            for (int i = 0; i < res.size(); i++) {
                dialogItem[i] = res.get(i).mainName;
            }
            ad = new AlertDialog.Builder(getContext()).setItems(dialogItem, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showEventDialog(getActivity(), res.get(which), null, null);
                }
            }).create();
            ad.setTitle("第" + pageWeek + "周," + TextTools.words_time_DOW[dow - 1] + " 的全天事件");
        }

        @Override
        public void onClick(View v) {
            ad.show();
        }
    }

}
