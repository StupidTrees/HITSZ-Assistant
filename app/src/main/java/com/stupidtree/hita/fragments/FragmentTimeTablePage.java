package com.stupidtree.hita.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.core.widget.NestedScrollView;
import androidx.cardview.widget.CardView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.hita.TextTools;
import com.stupidtree.hita.R;
import com.stupidtree.hita.core.TimeTable;
import com.stupidtree.hita.core.timetable.EventItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import tyrantgit.explosionfield.ExplosionField;

import static com.stupidtree.hita.HITAApplication.allCurriculum;
import static com.stupidtree.hita.HITAApplication.defaultSP;
import static com.stupidtree.hita.HITAApplication.isThisTerm;
import static com.stupidtree.hita.HITAApplication.mainTimeTable;
import static com.stupidtree.hita.HITAApplication.now;
import static com.stupidtree.hita.HITAApplication.thisCurriculumIndex;

import static com.stupidtree.hita.HITAApplication.thisWeekOfTerm;
import static com.stupidtree.hita.core.TimeTable.TIMETABLE_EVENT_TYPE_ARRANGEMENT;
import static com.stupidtree.hita.core.TimeTable.TIMETABLE_EVENT_TYPE_COURSE;
import static com.stupidtree.hita.core.TimeTable.TIMETABLE_EVENT_TYPE_DEADLINE;
import static com.stupidtree.hita.core.TimeTable.TIMETABLE_EVENT_TYPE_DYNAMIC;
import static com.stupidtree.hita.core.TimeTable.TIMETABLE_EVENT_TYPE_EXAM;
import static com.stupidtree.hita.core.TimeTable.TIMETABLE_EVENT_TYPE_REMIND;
import static com.stupidtree.hita.fragments.FragmentTimeLine.showEventDialog;


public class FragmentTimeTablePage extends BaseFragment {

    public boolean hasInit = false;
    View pageView;
    public int pageWeek;
    boolean curiculumOnly;
    boolean wholeday;
    NestedScrollView scrollView;
    int start;
    RelativeLayout rdays[] = new RelativeLayout[7]; //课程表格子布局对象
    /*辅助性变量区*/
    Calendar temp0 = Calendar.getInstance();
    Calendar temp1 = Calendar.getInstance();
    Calendar temp2 = Calendar.getInstance();
    /*UI类常量*/
    static int CARD_HEIGHT = 160;//课程表卡片高度

    /*控件对象区*/
    TextView tdays[] = new TextView[8]; //顶部日期文本
    TextView tWholeDays[] = new TextView[7];
    CardView tWholeDayCards[] = new CardView[7];
    LinearLayout classNumberLayout;
    LinearLayout wholedayLayout;
    refreshPageTask pageTask;

    public FragmentTimeTablePage() {
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
        curiculumOnly = defaultSP.getBoolean("timetable_curriculumonly", true);
        wholeday = defaultSP.getBoolean("timetable_wholeday", false);
        start = wholeday ? 0 : 8;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dynamic_timetable_page, null);
        pageView = v;
        initTableLayouts(v);
        initDateTextViews(v);
        initWholeDayCardViews(v);
        initWholeDayTextViews(v);
        classNumberLayout = v.findViewById(R.id.tt_class_number_layout);
        createLeftView();
        scrollView = v.findViewById(R.id.timetable_scroll);
        hasInit = true;
        RefreshPageView(pageWeek);

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
            text.setText(i + ":0");
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

    private void refreshDateViews() {

        /*显示上方日期*/
        tdays[0].setText((allCurriculum.get(thisCurriculumIndex).getFirstDateAtWOT(pageWeek).get(Calendar.MONTH) + 1) + "");
        Calendar firstDateTemp = allCurriculum.get(thisCurriculumIndex).getFirstDateAtWOT(pageWeek);
        Calendar temp = Calendar.getInstance();
        for (int k = 1; k <= 7; k++) {
            temp.setTime(firstDateTemp.getTime());
            temp.add(Calendar.DATE, k - 1);
            tdays[k].setText(temp.get(Calendar.DAY_OF_MONTH) + "");
        }
    }

    void initTableLayouts(View v) {
        rdays[0] = v.findViewById(R.id.tt_rl_monday);
        rdays[1] = v.findViewById(R.id.tt_rl_tuesday);
        rdays[2] = v.findViewById(R.id.tt_rl_wednesday);
        rdays[3] = v.findViewById(R.id.tt_rl_thursday);
        rdays[4] = v.findViewById(R.id.tt_rl_friday);
        rdays[5] = v.findViewById(R.id.tt_rl_saturday);
        rdays[6] = v.findViewById(R.id.tt_rl_weekday);
    }

    public void RefreshPageView(int week) {
        if(pageTask!=null&&!pageTask.isCancelled()) pageTask.cancel(true);
        pageTask =  new refreshPageTask(week);
        pageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void NotifyRefresh() {
        if (!hasInit) return;
        CARD_HEIGHT = defaultSP.getInt("TimeTable_cardheight", 160);//课程表卡片高度
        curiculumOnly = defaultSP.getBoolean("timetable_curriculumonly", true);
        wholeday = defaultSP.getBoolean("timetable_wholeday", false);
        start = wholeday ? 0 : 8;
        createLeftView();
        RefreshPageView(pageWeek);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {

        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    protected void stopTasks() {
        if(pageTask!=null&&!pageTask.isCancelled()) pageTask.cancel(true);
    }

    @Override
    protected void Refresh() {

    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    class refreshPageTask extends AsyncTask<String,Integer,List<List<EventItem>>>{

        int week;
        refreshPageTask(int week){
        this.week = week;}
        int[] colors = {
                Color.parseColor("#3F51B5"),
                Color.parseColor("#2196F3"),
                Color.parseColor("#009688"),
                Color.parseColor("#4CAF50"),
                Color.parseColor("#FFC107"),
                Color.parseColor("#FF5722"),
                Color.parseColor("#F44336"),
                Color.parseColor("#E91E63"),
                Color.parseColor("#9C27B0"),
                Color.parseColor("#795548"),
                Color.parseColor("#607D8B"),
        };
        @Override
        protected List<List<EventItem>> doInBackground(String... strings) {
            List<List<EventItem>> res = new ArrayList<>();
            for (int p = 1; p <= 7; p++){
                res.add(mainTimeTable.getOneDayEvents(week,p));
            }
            return res;
        }

        @Override
        protected void onPostExecute(List<List<EventItem>> lists) {
            super.onPostExecute(lists);
            //Log.e("week"+week,"refresh:"+hasInit);
            if (!hasInit) return;
            int dow = now.get(Calendar.DAY_OF_WEEK);
            refreshDateViews();
            if (wholeday) scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.scrollTo(0, 8 * CARD_HEIGHT);
                }
            });

            /*清空每一周的布局中显示的内容*/
            for (RelativeLayout x : rdays) {
                if (x.getChildCount() > 0) x.removeAllViews();
            }
            for (CardView cv : tWholeDayCards) {
                cv.setVisibility(View.GONE);
            }
            /*绘制时间线*/
            for (int x = 1; x < 8; x++) {
                for (int i = start+1; i < 24; i++) {
                    View line = new View(getContext());
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2);
                    //params.setMargins(0, 0, 20, 0);
                    line.setY(CARD_HEIGHT * (i-start)); //设置开始高度,即第几节课开始
                    line.setBackgroundColor(Color.parseColor("#10000000"));
                    line.setLayoutParams(params);
                    rdays[x-1].addView(line);
                }
            }
            /*实现当日背景色突出*/
            for (int x = 1; x < 8; x++) {
                //tdays[x].setBackground(null);
                rdays[x - 1].setBackground(null);
            }
            if (isThisTerm) {
                if (week == thisWeekOfTerm) {
                    int index = (dow == 1) ? 6 : dow - 2;
                    // tdays[index+1].setBackgroundColor(Color.parseColor("#08000000"));
                    rdays[index].setBackgroundColor(Color.parseColor("#08000000"));
                }
            }

            boolean hasWholedayWholeWeek = false;
            /*遍历含有事件的每一天，将事件显示在RelativeLayout中*/
            for (int p = 1; p <= 7; p++) {
                List<EventItem> thisDaysEvents = lists.get(p-1);
                if (thisDaysEvents == null || thisDaysEvents.size() == 0) continue;
                List<EventItem> wholeDayList = new ArrayList<>();
                for (final EventItem ei : thisDaysEvents) {
                    Log.e("e:", ei.mainName + "," + ei.isWholeDay);
                    if (ei.isWholeDay) {
                        wholeDayList.add(ei);
                        hasWholedayWholeWeek = true;
                        continue;
                    }
                    if (curiculumOnly && ei.eventType != TimeTable.TIMETABLE_EVENT_TYPE_COURSE && ei.eventType != TimeTable.TIMETABLE_EVENT_TYPE_EXAM)
                        continue;
                    RelativeLayout day = null;
                    day = rdays[p - 1];
                    /*绘制每个课程格子*/
                    View view = null; //加载单个课程布局
                    if (ei.eventType == TIMETABLE_EVENT_TYPE_EXAM || ei.eventType == TIMETABLE_EVENT_TYPE_COURSE) {//如果是课程，填充课程卡片的布局文件
                        view = getLayoutInflater().inflate(R.layout.dynamic_timetable_course_card, null);
                    } else if (ei.eventType == TIMETABLE_EVENT_TYPE_ARRANGEMENT) {
                        view = getLayoutInflater().inflate(R.layout.dynamic_timetable_arrangement_card, null);
                    } else if (ei.eventType == TIMETABLE_EVENT_TYPE_DEADLINE) {
                        view = getLayoutInflater().inflate(R.layout.dynamic_timetable_deadline_card, null);
                    } else if (ei.eventType == TIMETABLE_EVENT_TYPE_REMIND) {
                        view = getLayoutInflater().inflate(R.layout.dynamic_timetable_remind_card, null);
                    } else if (ei.eventType == TIMETABLE_EVENT_TYPE_DYNAMIC) {
                        view = getLayoutInflater().inflate(R.layout.dynamic_timetable_dynamic_card, null);
                    }
                    CardView blockCard = view.findViewById(R.id.timetable_card); //课程格子的卡片

                    temp0.set(2000, 1, 1, start, 00);
                    temp1.set(2000, 1, 1, ei.startTime.hour, ei.startTime.minute);
                    temp2.set(2000, 1, 1, ei.endTime.hour, ei.endTime.minute);
                    float lastTimeInMills = temp2.getTimeInMillis() - temp1.getTimeInMillis();
                    float startTimeFromBeginningInMills = temp1.getTimeInMillis() - temp0.getTimeInMillis();
                    view.setY(CARD_HEIGHT * ((startTimeFromBeginningInMills / 3600000))); //设置开始高度,即第几节课开始
                    LinearLayout.LayoutParams params;
                    if (ei.eventType != TIMETABLE_EVENT_TYPE_DEADLINE && ei.eventType != TIMETABLE_EVENT_TYPE_REMIND) {
                        params = new LinearLayout.LayoutParams
                                (ViewGroup.LayoutParams.MATCH_PARENT, (int) ((lastTimeInMills / 3600000) * CARD_HEIGHT)); //设置布局高度,即跨多少节课
                    } else {
                        params = new LinearLayout.LayoutParams
                                (ViewGroup.LayoutParams.MATCH_PARENT, 60); //ddl高度固定
                    }
                    view.setLayoutParams(params);
                    TextView text_block_name = view.findViewById(R.id.tt_card_coursename);//格子中显示课程名的TextView
                    TextView text_block_place = view.findViewById(R.id.tt_card_place);//格子中显示上课地点的TextView
                    TextView text_block_type = view.findViewById(R.id.tt_card_head_text);//格子中显示事件类型的TextView
                    CardView card_block_head = view.findViewById(R.id.tt_card_head_card);//格子头头的卡片
                    if (ei.eventType == TIMETABLE_EVENT_TYPE_COURSE) {
                        card_block_head.setCardBackgroundColor(((BaseActivity) getActivity()).getColorPrimary());
                        text_block_place.setText(ei.tag2);//显示地点
                    } else if (ei.eventType == TIMETABLE_EVENT_TYPE_EXAM) {
                        card_block_head.setCardBackgroundColor(((BaseActivity) getActivity()).getColorAccent());
                        text_block_place.setText(ei.tag2);//显示地点
                    }

                    text_block_name.setText(ei.mainName);
                    day.addView(view);
                    blockCard.setOnClickListener(new ClickBlockListener(ei));
                    blockCard.setOnLongClickListener(new LongClickBlockListener(ei));
                   // blockCard.setCardBackgroundColor(colors[new Random().nextInt(9)]);
                }
                if (wholeDayList.size() > 0) {
                    tWholeDayCards[p - 1].setVisibility(View.VISIBLE);
                    tWholeDays[p - 1].setText(wholeDayList.size() + "");
                    tWholeDayCards[p - 1].setOnClickListener(new OnWholeDayCardClickListener(wholeDayList, p));
                } else tWholeDayCards[p - 1].setVisibility(View.GONE);
            }
            if(hasWholedayWholeWeek) wholedayLayout.setVisibility(View.VISIBLE);
            else wholedayLayout.setVisibility(View.GONE);


            /*最后，显示当前时间指针*/
            if (isThisTerm) {
                if (week == thisWeekOfTerm) {
                    int index = (dow == 1) ? 6 : dow - 2;
                    View v = new View(getContext());
                    temp0.setTimeInMillis(now.getTimeInMillis());
                    temp0.set(Calendar.HOUR_OF_DAY,start);
                    temp0.set(Calendar.MINUTE,0);
                    float startTimeFromBeginningInMills = now.getTimeInMillis() - temp0.getTimeInMillis();
                    v.setBackgroundColor(((BaseActivity)getActivity()).getColorPrimary());
                    v.setAlpha(0.5f);
                    v.setY(CARD_HEIGHT*startTimeFromBeginningInMills / 3600000);
                    LinearLayout.LayoutParams params;
                    params = new LinearLayout.LayoutParams
                            (ViewGroup.LayoutParams.MATCH_PARENT, 7);
                    v.setLayoutParams(params);
                    rdays[index].addView(v);
                }
            }
        }
    }


    class ClickBlockListener implements View.OnClickListener {
        EventItem ei;

        ClickBlockListener(EventItem ei) {
            this.ei = ei;
        }

        @Override
        public void onClick(View v) {
//            mFloatingActionButton.hide();
            showEventDialog((Activity) v.getContext(), ei, v, null);
        }
    }

    class LongClickBlockListener implements View.OnLongClickListener {
        EventItem ei;

        LongClickBlockListener(EventItem ei) {
            this.ei = ei;
        }

        @Override
        public boolean onLongClick(final View v) {
            if (ei.eventType != TIMETABLE_EVENT_TYPE_COURSE) {
                AlertDialog ad = new AlertDialog.Builder(FragmentTimeTablePage.this.getContext()).
                        setNegativeButton("取消", null)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mainTimeTable.deleteEvent(ei, true);
                                ExplosionField ef = ExplosionField.attach2Window(FragmentTimeTablePage.this.getActivity());
                                ef.explode(v);
                            }
                        }).
                                create();
                ad.setMessage("确定删除事件吗？");
                ad.show();
                return true;
            }
            return false;
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
