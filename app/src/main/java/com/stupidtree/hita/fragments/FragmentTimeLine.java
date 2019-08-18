package com.stupidtree.hita.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.core.util.Pair;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gelitenight.waveview.library.WaveView;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.android.material.snackbar.Snackbar;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.activities.ActivityUserCenter;
import com.stupidtree.hita.hita.TextTools;
import com.stupidtree.hita.R;
import com.stupidtree.hita.TimeWatcher;
import com.stupidtree.hita.activities.ActivityCourse;
import com.stupidtree.hita.activities.ActivityExplore;
import com.stupidtree.hita.activities.ActivityRankBoard;
import com.stupidtree.hita.activities.ActivityLogin;
import com.stupidtree.hita.activities.ActivityLoginJWTS;
import com.stupidtree.hita.activities.ActivityMain;
import com.stupidtree.hita.activities.ActivitySubject;
import com.stupidtree.hita.activities.ActivityTeacher;
import com.stupidtree.hita.activities.ActivityTimeTable;
import com.stupidtree.hita.adapter.TimeLineWholedayAdapter;
import com.stupidtree.hita.core.TimeTable;
import com.stupidtree.hita.core.timetable.EventItem;
import com.stupidtree.hita.core.timetable.HTime;
import com.stupidtree.hita.diy.MaterialCircleAnimator;
import com.stupidtree.hita.adapter.TimelineListAdapter;
import com.stupidtree.hita.diy.WaveViewHelper;
import com.stupidtree.hita.util.ActivityUtils;
import com.stupidtree.hita.util.RefreshBroadcastReceiver;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import tyrantgit.explosionfield.ExplosionField;

import static com.stupidtree.hita.TimeWatcher.nextEvent;
import static com.stupidtree.hita.TimeWatcher.nowEvent;
import static com.stupidtree.hita.TimeWatcher.nowProgress;
import static com.stupidtree.hita.TimeWatcher.todaysEvents;

import static com.stupidtree.hita.core.TimeTable.TIMETABLE_EVENT_TYPE_COURSE;
import static com.stupidtree.hita.core.TimeTable.TIMETABLE_EVENT_TYPE_DEADLINE;
import static com.stupidtree.hita.core.TimeTable.TIMETABLE_EVENT_TYPE_DYNAMIC;
import static com.stupidtree.hita.core.TimeTable.TIMETABLE_EVENT_TYPE_EXAM;
import static com.stupidtree.hita.HITAApplication.*;

@SuppressLint("ValidFragment")
public class FragmentTimeLine extends BaseFragment implements
        RefreshBroadcastReceiver.ActionListener {
    public static final int TL_REFRESH_FROM_TIMETICK = 111;
    public static final int TL_REFRESH_FROM_TASK = 114;
    public static final int TL_REFRESH_FROM_UNHIDE = 112;
    public static final int TL_REFRESH_FROM_DELETE = 113;
    boolean hasInit = false;
    boolean isFirst = false;
    CollapsingToolbarLayout mCollapsingToolbarLayout;
    AppBarLayout mAppBarLayout;
    NestedScrollView noneLayout;
    View[] heads;
    WaveView waveView;
    WaveViewHelper waveViewHelper;
    DecimalFormat df = new DecimalFormat("#.#%");
    TimelineListAdapter TimeLineListAdapter;
    TimeLineWholedayAdapter timelineWholedayAdapter;
    RecyclerView TimeLineList,timelineWholedayList;
    List<EventItem> timelineRes,wholeDayRes;
    ImageView bt_bar_timetable,bt_bar_addEvent;
    LinearLayout head_counting,head_goNow;
    TextView head_counting_time,head_counting_name,head_counting_middle,
            head_goQuickly_classroom,head_goNow_course;
    CardView findClassroom,head_card;
    TextView head_title,head_subtitle;
    ImageView head_image,head_counting_image;

    //public FragmentTasks ftsk;
    boolean switchToCountingAvailable = false;
    headCardClickListener headCardClickListener;
    RefreshBroadcastReceiver refreshReciever;
    LocalBroadcastManager localBroadcastManager;
    SwipeRefreshLayout swipeRefreshLayout;
    RefreshTask pageTask;

    public FragmentTimeLine(){

    }

    public static FragmentTimeLine newInstance(Boolean isFirst){
        FragmentTimeLine fl = new  FragmentTimeLine();
        Bundle b = new Bundle();
        b.putBoolean("isFirst",isFirst);
        fl.setArguments(b);
        return fl;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments()!=null) isFirst = getArguments().getBoolean("isFirst");
        //layoutAnimationController = AnimationUtils.loadLayoutAnimation(FragmentTimeLine.this.getContext(), R.anim.recycler_layout_animation_falls_down);

        initReciever();
    }


    private void initReciever(){
        refreshReciever = new RefreshBroadcastReceiver();
        refreshReciever.setListener(this);
        localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter iF = new IntentFilter();
        iF.addAction("COM.STUPIDTREE.HITA.TIMELINE_REFRESH");
        localBroadcastManager.registerReceiver(refreshReciever,iF);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_timeline, container, false);
        timeWatcher.refreshProgress(true,true);
        initViews(v);
        initListAndAdapter(v);

        if(!isFirst){
            head_card.post(new Runnable() {
                @Override
                public void run() {
                    MaterialCircleAnimator.animShow(mAppBarLayout,500);
                }
            });
        }else{
            head_card.setVisibility(View.INVISIBLE);
        }

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        Refresh(TL_REFRESH_FROM_UNHIDE,false);
    }

    public void showHeadCard(){
        if(!hasInit) return;
        head_card.setVisibility(View.VISIBLE);
        head_card.post(new Runnable() {
            @Override
            public void run() {
                MaterialCircleAnimator.animShow(mAppBarLayout,500);
            }
        });
    }
    public void continueToGuide(){
        new TapTargetSequence(this.getActivity())
                .targets(
//                        TapTarget.forView(bt_bar_timetable,"点这里查看时间表","课程表plus")
//                                .drawShadow(true)
//                                .cancelable(false)
//                                .tintTarget(false)
//                                .id(15)
//                                .targetRadius(16)
//                                .transparentTarget(true)
//                                .targetCircleColor(R.color.white)
//                                .outerCircleColor(R.color.amber_primary),
                        TapTarget.forView(bt_bar_addEvent,"点这里添加事件","妈妈再也不用担心我的时间安排")
                                .drawShadow(true)
                                .cancelable(false)
                                .tintTarget(false)
                                .id(16)
                                .targetRadius(16)
                                .transparentTarget(true)
                                .targetCircleColor(R.color.white)
                                .outerCircleColor(R.color.amber_primary)
                ).listener(new TapTargetSequence.Listener() {
            @Override
            public void onSequenceFinish() {
                ((ActivityMain)getActivity()).continueToGuide();
            }

            @Override
            public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {;
            }

            @Override
            public void onSequenceCanceled(TapTarget lastTarget) {

            }
        }).start();


    }



    @Override
    public void receive(Context context, Intent intent) {
        if(intent.getAction().equals("COM.STUPIDTREE.HITA.TIMELINE_REFRESH")){
            if(intent.hasExtra("from")&&intent.getStringExtra("from").equals("time_tick")) Refresh(TL_REFRESH_FROM_TIMETICK,false);
            else if(intent.hasExtra("from")&&intent.getStringExtra("from").equals("task")) Refresh(TL_REFRESH_FROM_TASK,false);
            else Refresh(TL_REFRESH_FROM_UNHIDE,false);
        }
    }

    @Override
    protected void stopTasks() {
        if(pageTask!=null&&!pageTask.isCancelled()) pageTask.cancel(true);
    }

    @Override
    protected void Refresh() {

    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    public void initViews(View v) {
        mAppBarLayout = v.findViewById(R.id.app_bar);
        noneLayout = v.findViewById(R.id.tl_noneview);
        head_card = v.findViewById(R.id.timeline_head_card);
        bt_bar_timetable = v.findViewById(R.id.bt_timetable);
        bt_bar_addEvent = v.findViewById(R.id.bt_add);
        head_title = v.findViewById(R.id.timeline_titile);
        head_subtitle= v.findViewById(R.id.timeline_subtitle);
        head_image = v.findViewById(R.id.timeline_head_image);
        head_goNow = v.findViewById(R.id.timeline_head_gonow);
        head_goNow_course = v.findViewById(R.id.tl_head_gonow_course);
        mCollapsingToolbarLayout = v.findViewById(R.id.collapsingtoolbar);
        mCollapsingToolbarLayout.setCollapsedTitleTextColor(Color.parseColor("#202020"));
        mCollapsingToolbarLayout.setExpandedTitleColor(Color.parseColor("#00202020"));
        mCollapsingToolbarLayout.setScrimAnimationDuration(280);
        mCollapsingToolbarLayout.setScrimVisibleHeightTrigger(280);

        bt_bar_timetable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Transition explode = TransitionInflater.from(FragmentTimeLine.this.getActivity()).inflateTransition(android.R.transition.explode);
                Intent ii = new Intent(FragmentTimeLine.this.getActivity(), ActivityTimeTable.class);
                startActivity(ii);
            }
        });
        bt_bar_addEvent.setOnClickListener(new addEventClickListener());
        initHead(v);
        hasInit = true;

    }


    @SuppressLint("ClickableViewAccessibility")
    public void initHead(View v){
        waveView = v.findViewById(R.id.timeline_head_waveview);
        waveViewHelper = new WaveViewHelper(waveView, nowProgress);
        waveView.setShapeType(WaveView.ShapeType.CIRCLE);
        waveView.setBorder(8,Color.WHITE);
        waveView.setWaveColor(
                Color.WHITE,Color.WHITE
        );
        findClassroom = v.findViewById(R.id.find_classroom);
        headCardClickListener = new headCardClickListener();

        head_card.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN){
                   headCardClickListener.posX = event.getX();
                   headCardClickListener.posY = event.getY();
                }
                return false;
            }
        });

        head_card.setOnClickListener(headCardClickListener);
        head_counting = v.findViewById(R.id.head_counting);
        heads = new View[]{head_image,head_goNow,waveView};
        head_counting_name = v.findViewById(R.id.tl_head_counting_name);
        head_counting_image = v.findViewById(R.id.tl_head_counting_image);
        head_counting_middle = v.findViewById(R.id.tl_head_counting_middle);
        head_counting_time = v.findViewById(R.id.tl_head_counting_time);
        head_goQuickly_classroom = v.findViewById(R.id.tl_head_gonow_classroom);

        View.OnClickListener findFoodListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(FragmentTimeLine.this.getActivity(), ActivityRankBoard.class);
                FragmentTimeLine.this.getActivity().startActivity(i);
            }
        };
       // findFood_lunch.setOnClickListener(findFoodListener);
        //findFood_dinner.setOnClickListener(findFoodListener);
        findClassroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(FragmentTimeLine.this.getActivity(), ActivityExplore.class);
                i.putExtra("terminal",head_goQuickly_classroom.getText());
                FragmentTimeLine.this.getActivity().startActivity(i);
            }
        });
    }

    public void initListAndAdapter(View v) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
        TimeLineList = v.findViewById(R.id.timelinelist);
        swipeRefreshLayout = v.findViewById(R.id.timeline_refresh);
        timelineWholedayList = v.findViewById(R.id.timeline_wholeday_list);
        wholeDayRes = new ArrayList<>();
        timelineRes = new ArrayList<>();
        timelineWholedayAdapter = new TimeLineWholedayAdapter(this.getContext(),wholeDayRes);
        TimeLineListAdapter = new TimelineListAdapter(this.getContext(),timelineRes);
        TimeLineList.setAdapter(TimeLineListAdapter);
        TimeLineList.setLayoutManager(layoutManager);
        timelineWholedayList.setLayoutManager(layoutManager2);
        timelineWholedayList.setAdapter(timelineWholedayAdapter);
        TimeLineListAdapter.setOnItemClickLitener(new TimelineListAdapter.OnItemClickLitener() {
            @Override
            public void onItemClick(View card, View time, View name, int position) {
                showEventDialog(FragmentTimeLine.this.getActivity(), timelineRes.get(position),card,name);
            }


        });
        TimeLineListAdapter.setOnItemLongClickLitener(new TimelineListAdapter.OnItemLongClickLitener() {
            @Override
            public void onItemLongClick(View view, int position) {
                if (todaysEvents.get(position).eventType != TimeTable.TIMETABLE_EVENT_TYPE_COURSE&&todaysEvents.get(position).eventType != TIMETABLE_EVENT_TYPE_DYNAMIC) {
                    ExplosionField ef = ExplosionField.attach2Window(FragmentTimeLine.this.getActivity());
                    ef.explode(view);
                    new DeleteTask_timeline(position).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        });
        TimeLineListAdapter.setmOnNaviClickListener(new TimelineListAdapter.OnNaviClickListener() {
            @Override
            public void onNaviClick(View view, int position, int type,String terminal) {
                if(type== TIMETABLE_EVENT_TYPE_EXAM||type== TIMETABLE_EVENT_TYPE_COURSE){
                    Intent i = new Intent(getActivity(),ActivityExplore.class);
                    i.putExtra("terminal",terminal);
                    startActivity(i);
                }
            }
        });
        timelineWholedayAdapter.setOnItemClickListener(new TimeLineWholedayAdapter.OnItemClickListener() {
            @Override
            public void OnClick(View v, int position) {
                showEventDialog(FragmentTimeLine.this.getActivity(),wholeDayRes.get(position),null,null);
            }
        });

        timelineWholedayAdapter.setOnItemLongClickListener(new TimeLineWholedayAdapter.OnItemLongClickListener() {
            @Override
            public boolean OnLongClick(View v, int position) {
                ExplosionField ef = ExplosionField.attach2Window(FragmentTimeLine.this.getActivity());
                ef.explode(v);
                new DeleteTask_wholeday(position).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                return true;
            }
        });
        swipeRefreshLayout.setColorSchemeColors(((BaseActivity)getActivity()).getColorAccent());
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Refresh(TL_REFRESH_FROM_TIMETICK,true);
            }
        });
    }

    public void UpdateHeadView() {
        String titleToSet,subtitltToSet;
        if(CurrentUser==null){
            titleToSet = HContext.getString(R.string.timeline_head_nulluser_title);
            subtitltToSet = HContext.getString(R.string.timeline_head_nulluser_subtitle);
            switchHeadView(head_image,R.drawable.ic_timeline_head_login);
            //switchToCountingAvailable = false;
            headCardClickListener.setMode(headCardClickListener.LOG_IN);

        }else if(CurrentUser!=null&&!isDataAvailable()){
            titleToSet = HContext.getString(R.string.timeline_head_nulldata_title);
            subtitltToSet = HContext.getString(R.string.timeline_head_nulldata_subtitle);
            switchHeadView(head_image,R.drawable.ic_timeline_head_nulldata);
            headCardClickListener.setMode(headCardClickListener.JWTS);
            switchToCountingAvailable = false;
        }else if(!isThisTerm){
            titleToSet = HContext.getString(R.string.timeline_head_notthisterm_title);
            subtitltToSet = HContext.getString(R.string.timeline_head_notthisterm_subtitle);
            switchHeadView(head_image,R.drawable.ic_origami_paper_bird);
            headCardClickListener.setMode(headCardClickListener.SHOW_NEXT);
        } else if(todaysEvents.size()==0){
            titleToSet = HContext.getString(R.string.timeline_head_free_title);
            subtitltToSet = HContext.getString(R.string.timeline_head_free_subtitle);
            switchHeadView(head_image,R.drawable.ic_timeline_head_free);
            headCardClickListener.setMode(headCardClickListener.SHOW_NEXT);
        } else if(nowEvent!=null){
            switchHeadView(waveView,-1);
            titleToSet= nowEvent.mainName;
            subtitltToSet = "进度:"+df.format(nowProgress);
            waveView.setWaterLevelRatio(nowProgress);
            headCardClickListener.setMode(headCardClickListener.SHOW_NEXT);
        }else{
            if(new HTime(now).compareTo(new HTime(5,0))<0&&new HTime(now).compareTo(new HTime(0,0))>0){
                switchHeadView(head_image,R.drawable.ic_moon);
                titleToSet = HContext.getString(R.string.timeline_head_goodnight_title);
                subtitltToSet = HContext.getString(R.string.timeline_head_goodnight_subtitle);
                headCardClickListener.setMode(headCardClickListener.SHOW_NEXT);
            }else if(new HTime(now).compareTo(new HTime(8,15))<0&&new HTime(now).compareTo(new HTime(5,00))>0){
                switchHeadView(head_image,R.drawable.ic_sunny);
                titleToSet = HContext.getString(R.string.timeline_head_goodmorning_title);
                subtitltToSet= "今天共有"+ timeWatcher.getTodayCourseNum()+"节课";
                headCardClickListener.setMode(headCardClickListener.SHOW_NEXT);
            }else if(new HTime(now).compareTo(new HTime(12,15))>0&&new HTime(now).compareTo(new HTime(13,00))<0){
                switchHeadView(head_image,R.drawable.ic_lunch);
                titleToSet = HContext.getString(R.string.timeline_head_lunch_title);
                subtitltToSet = HContext.getString(R.string.timeline_head_lunch_subtitle);
                headCardClickListener.setMode(headCardClickListener.CANTEEN);
            }else if(new HTime(now).compareTo(new HTime(17,10))>0&&new HTime(now).compareTo(new HTime(18,10))<0){
                switchHeadView(head_image,R.drawable.ic_lunch);
                titleToSet = HContext.getString(R.string.timeline_head_dinner_title);
                subtitltToSet = HContext.getString(R.string.timeline_head_dinner_subtitle);
                headCardClickListener.setMode(headCardClickListener.CANTEEN);
            }else if(nextEvent!=null){
                if(nextEvent.startTime.getDuration(new HTime(now))<=15&&(nextEvent.eventType==TimeTable.TIMETABLE_EVENT_TYPE_COURSE||nextEvent.eventType==TimeTable.TIMETABLE_EVENT_TYPE_EXAM)){
                    switchHeadView(head_goNow,-1);
                    subtitltToSet = HContext.getString(R.string.timeline_head_gonow_subtitle);

                    titleToSet = (nextEvent.startTime.getDuration(new HTime(now)) >= 60 ?
                            (nextEvent.startTime.getDuration(new HTime(now))) / 60 + "小时" + (nextEvent.startTime.getDuration(new HTime(now))) % 60 + "分"
                            :nextEvent.startTime.getDuration(new HTime(now))+"分钟")+"后";
                    head_goQuickly_classroom.setText(nextEvent.tag2);
                    head_goNow_course.setText(nextEvent.mainName);
                    headCardClickListener.setMode(headCardClickListener.NONE);
                }else{
                    titleToSet = HContext.getString(R.string.timeline_head_normal_title);
                    subtitltToSet = HContext.getString(R.string.timeline_head_normal_subtitle);
                    switchHeadView(head_image,R.drawable.ic_sunglasses);
                    headCardClickListener.setMode(headCardClickListener.SHOW_NEXT);
                }
            }else{
                if(new HTime(now).compareTo(new HTime(23,00))>0||new HTime(now).compareTo(new HTime(5,0))<0){
                    switchHeadView(head_image,R.drawable.ic_moon);
                    titleToSet = HContext.getString(R.string.timeline_head_goodnight_title);
                    subtitltToSet = HContext.getString(R.string.timeline_head_goodnight_subtitle);
                }else{
                    switchHeadView(head_image,R.drawable.ic_finish);
                    titleToSet = HContext.getString(R.string.timeline_head_finish_title);
                    subtitltToSet = HContext.getString(R.string.timeline_head_finish_subtitle);
                }
                switchToCountingAvailable = false;
            }
        }
        if(nextEvent!=null){
            String timeText = nextEvent.startTime.getDuration(new HTime(now)) >= 60 ?
                    (nextEvent.startTime.getDuration(new HTime(now))) / 60 + "小时" + ((int) nextEvent.startTime.getDuration(new HTime(now))) % 60 + "分"
                    :nextEvent.startTime.getDuration(new HTime(now))+"分钟";
            head_counting_name.setText(nextEvent.mainName);
            head_counting_time.setText(timeText);
            head_counting_middle.setVisibility(View.VISIBLE);
            head_counting_time.setVisibility(View.VISIBLE);
            head_counting_image.setVisibility(View.GONE);
            head_counting_name.setVisibility(View.VISIBLE);
        }else{
            head_counting_name.setVisibility(View.GONE);
            head_counting_middle.setVisibility(View.GONE);
            head_counting_time.setVisibility(View.GONE);
            head_counting_image.setVisibility(View.VISIBLE);
        }
        head_title.setText(titleToSet);
        head_subtitle.setText(subtitltToSet);
    }

    void switchHeadView(View view,int imageId){
        for(int i = 0;i<heads.length;i++){
            if(heads[i]==view) heads[i].setVisibility(View.VISIBLE);
            else heads[i].setVisibility(View.GONE);
        }
        if(view instanceof ImageView)((ImageView)view).setImageResource(imageId);
        if(view instanceof WaveView){
            waveViewHelper.start();
        }else{
            waveViewHelper.cancel();
        }
        head_counting.post(new Runnable() {
            @Override
            public void run() {
                MaterialCircleAnimator.animHide(head_counting);
            }
        });

    }

    private void Refresh(int from,boolean swipe) {
        if(!hasInit) return;
        if(pageTask!=null&&!pageTask.isCancelled()) pageTask.cancel(true);
        pageTask = new RefreshTask(from,swipe);
        pageTask.execute();
    }

    private class RefreshTask extends AsyncTask<String, Integer, Integer> {
        int from;
        boolean swipe;
        RefreshTask(int f,boolean swipe) {
            this.swipe = swipe;
            from = f;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(swipe)swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected Integer doInBackground(String... strings) {
            correctData();
            if(!isThisTerm) return -2;
            boolean refreshTask = from!=TL_REFRESH_FROM_TASK;
            if (from != TL_REFRESH_FROM_TIMETICK) timeWatcher.refreshProgress(true,refreshTask);
            if (getDataState() == DATA_STATE_NONE_CURRICULUM) return DATA_STATE_NONE_CURRICULUM;
            timelineRes.clear();
            wholeDayRes.clear();
            for(EventItem ei:todaysEvents){
              //  Log.e("ei:",ei.mainName+","+ei.isWholeDay);
                if(ei.isWholeDay) wholeDayRes.add(ei);
                else timelineRes.add(ei);
            }
            if(timelineRes.size()==0) return -5;
            return 0;
        }


        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            UpdateHeadView();
            swipeRefreshLayout.setRefreshing(false);
            if (integer == DATA_STATE_NONE_CURRICULUM) {
                    noneLayout.setVisibility(View.VISIBLE);
                    TimeLineList.setVisibility(View.GONE);
                    return;
                } else if (integer == -2) {
                    noneLayout.setVisibility(View.VISIBLE);
                    TimeLineList.setVisibility(View.GONE);
                    return;
                }else if(integer==-5) {
                noneLayout.setVisibility(View.VISIBLE);
                TimeLineList.setVisibility(View.GONE);
            }else{
                TimeLineList.setVisibility(View.VISIBLE);
                noneLayout.setVisibility(View.INVISIBLE);
            }
            timelineWholedayAdapter.notifyDataSetChanged();
                if(from!=TL_REFRESH_FROM_DELETE) TimeLineListAdapter.notifyDataSetChanged();
                if (from != TL_REFRESH_FROM_UNHIDE&&from!=TL_REFRESH_FROM_DELETE&&swipe) {
                   // TimeLineList.setLayoutAnimation(layoutAnimationController);
                    TimeLineList.scheduleLayoutAnimation();
                }

        }
    }

    class DeleteTask_timeline extends AsyncTask{
        int position;
        DeleteTask_timeline(int pos){
            position = pos;
        }
        @Override
        protected Object doInBackground(Object[] objects) {
            boolean res = mainTimeTable.deleteEvent(timelineRes.get(position),timelineRes.get(position).eventType==TIMETABLE_EVENT_TYPE_DEADLINE);
            if(res){
                todaysEvents.remove(timelineRes.get(position));
                timelineRes.remove(position);
                timeWatcher.refreshNowAndNextEvent();
            }
            return res;

        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if((Boolean)o){
                TimeLineListAdapter.notifyItemRemoved(position);
                if(position != timelineRes.size()){ // 如果移除的是最后一个，忽略
                    TimeLineListAdapter.notifyItemRangeChanged(position, todaysEvents.size() - position);
                }else{
                    if(position-1>=0)TimeLineListAdapter.notifyItemChanged(position-1);
                }
                UpdateHeadView();
                if(todaysEvents.size()==0) {
                    noneLayout.setVisibility(View.VISIBLE);
                    TimeLineList.setVisibility(View.GONE);
                }
            }
            Intent mes = new Intent("COM.STUPIDTREE.HITA.TASK_REFRESH");
            localBroadcastManager.sendBroadcast(mes);
            //if(ftsk!=null&&ftsk.hasInit) ftsk.Refresh();
            ActivityMain.saveData(getActivity());

        }
    }
    class DeleteTask_wholeday extends AsyncTask{
        int position;
        DeleteTask_wholeday(int pos){
            position = pos;
        }
        @Override
        protected Object doInBackground(Object[] objects) {
            boolean res = mainTimeTable.deleteEvent(wholeDayRes.get(position),wholeDayRes.get(position).eventType==TIMETABLE_EVENT_TYPE_DEADLINE);
            if(res){
                timeWatcher.refreshNowAndNextEvent();
                todaysEvents.remove(wholeDayRes.get(position));
                wholeDayRes.remove(position);
                if(todaysEvents.size()==0) {
                    noneLayout.setVisibility(View.VISIBLE);
                    TimeLineList.setVisibility(View.GONE);
                }
            }
            return res;

        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if((Boolean)o){
                timelineWholedayAdapter.notifyItemRemoved(position);
                if(position != wholeDayRes.size()){ // 如果移除的是最后一个，忽略
                   timelineWholedayAdapter.notifyItemRangeChanged(position, todaysEvents.size() - position);
                }
                UpdateHeadView();
                ActivityMain.saveData(getActivity());
            }
            Intent mes = new Intent("COM.STUPIDTREE.HITA.TASK_REFRESH");
            localBroadcastManager.sendBroadcast(mes);
           // if(ftsk!=null&&ftsk.hasInit) ftsk.Refresh();

        }
    }

    class headCardClickListener implements View.OnClickListener{
        public static final int SHOW_NEXT = 94;
        public static final int LOG_IN = 713;
        public static final int JWTS = 577;
        public static final int CANTEEN = 379;
        public static final int NONE = 264;
        float posX;
        float posY;
        int mode;

        public void setMode(int mode) {
            this.mode = mode;
        }

        @Override
        public void onClick(View v) {
            switch(mode){
                case SHOW_NEXT:
                    if(head_counting.getVisibility()==View.VISIBLE){
                    head_counting.post(new Runnable() {
                        @Override
                        public void run() {
                            MaterialCircleAnimator.animHide(head_counting,400,posX,posY);
                        }
                    });

                }else{
                    head_counting.post(new Runnable() {
                        @Override
                        public void run() {
                            MaterialCircleAnimator.animShow(head_counting,400,posX,posY);
                        }
                    });

                }
                    break;
                case LOG_IN:
                    Intent i = new Intent(getActivity(), ActivityLogin.class);
                    startActivity(i);
                    break;
                case JWTS:
                    Intent k;
                    if(CurrentUser.getStudentnumber()==null||CurrentUser.getStudentnumber().isEmpty()){
                        AlertDialog ad = new AlertDialog.Builder(getActivity()).setTitle("提示").setMessage("请先绑定学号后再使用教务系统导入课表").setPositiveButton("好的", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = new Intent(HContext, ActivityUserCenter.class);
                                startActivity(i);
                            }
                        }).create();
                        ad.show();
                    }else{
                        k = new Intent(HContext, ActivityLoginJWTS.class);
                        startActivity(k);
                    }
                    break;
                case CANTEEN:
                    Intent c = new Intent(getActivity(), ActivityRankBoard.class);
                    startActivity(c);
                    break;

            }

        }
    }

    class addEventClickListener implements View.OnClickListener{
        FragmentAddEvent aef;
        addEventClickListener(){
            aef = FragmentAddEvent.newInstance();
        }
        @Override
        public void onClick(View v) {
            if (isDataAvailable()) {
                if(isThisTerm)aef.show(getFragmentManager(), "add_event");
                else Snackbar.make(v,"这学期还没开始呐，试着切换课表为已开始学期吧！",Snackbar.LENGTH_SHORT).show();
                   // Toast.makeText(FragmentTimeLine.this.getContext(), , Toast.LENGTH_SHORT).show();
            } else {
                Snackbar.make(v,"请先导入课表！",Snackbar.LENGTH_SHORT).show();
               // Toast.makeText(FragmentTimeLine.this.getContext(), "请先导入课表！", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public static void showEventDialog(final Activity a, final EventItem ei, final View transitionCard, View transitionName) {
        View dlgView = null;
        final AlertDialog dialog = new AlertDialog.Builder(a).create();
        if (ei.eventType == TIMETABLE_EVENT_TYPE_COURSE) {
            //Log.e("======!!!", String.valueOf(ei));
            dlgView = a.getLayoutInflater().inflate(R.layout.dialog_timetable_course, null);
            final TextView value2 = dlgView.findViewById(R.id.tt_dlg_value2);
            final TextView value3 = dlgView.findViewById(R.id.tt_dlg_value3);
            final TextView value4 = dlgView.findViewById(R.id.tt_dlg_value4);
            final TextView value5 = dlgView.findViewById(R.id.tt_dlg_value5);
            final TextView name = dlgView.findViewById(R.id.tt_dlg_name);
            final ImageView detail = dlgView.findViewById(R.id.dlg_bt_detail);
            final LinearLayout teacher_detail = dlgView.findViewById(R.id.tt_dlg_value3_detail);
            final LinearLayout classroom_detail = dlgView.findViewById(R.id.tt_dlg_value2_detail);
            final LinearLayout card = dlgView.findViewById(R.id.card);
            ImageView classroom_detail_icon = dlgView.findViewById(R.id.classroom_detail_icon);
            value2.setText(ei.tag2.isEmpty() ? "无" : ei.tag2);
            value3.setText(ei.tag3.isEmpty() ? "无" : ei.tag3);
            value4.setText(ei.startTime.tellTime() + "-" + ei.endTime.tellTime());
            value5.setText(ei.tag4.isEmpty() ? "无" : ei.tag4);
            //dialog.setTitle(ei.mainName);
            name.setText(ei.mainName);
            detail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityOptionsCompat ops = ActivityOptionsCompat.makeSceneTransitionAnimation(a,card,"card");
                    Intent i = new Intent(a, ActivityCourse.class);
                    Bundle b = new Bundle();
                    b.putSerializable("eventitem",ei);
                    i.putExtras(b);
                    a.startActivity(i);
                    //dialog.dismiss();
                }
            });
            teacher_detail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String[] names = ei.tag3.split("，");
                    if(names.length>1){
                        AlertDialog ad = new AlertDialog.Builder(a).setTitle("选择教师").setItems(names, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent il = new Intent(a, ActivityTeacher.class);
                                il.putExtra("name",names[i]);
                                a.startActivity(il);
                            }
                        }).create();
                        ad.show();
                    }else {
                        Intent i = new Intent(a, ActivityTeacher.class);
                        i.putExtra("name",ei.tag3);
                        a.startActivity(i);
                    }
                }
            });
            if(ei.tag2.isEmpty()){
                classroom_detail_icon.setVisibility(View.GONE);
            }else{
                classroom_detail_icon.setVisibility(View.VISIBLE);
                classroom_detail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String cr[] = ei.tag2.split("，");
                        final ArrayList<String> classRooms = new ArrayList<>(Arrays.asList(cr));
                        if(classRooms.size()>1){
                            ArrayList<String> toRemove = new ArrayList<>();
                            for(int i=0;i<classRooms.size();i++){
                                classRooms.set(i,classRooms.get(i).substring(classRooms.get(i).lastIndexOf("周")+1));
                            }
                            for(String x:classRooms){
                                if(TextUtils.isEmpty(x)) toRemove.add(x);
                            }
                            classRooms.removeAll(toRemove);
                            String classRoomItems[] = new String[classRooms.size()];
                            for(int i=0;i<classRoomItems.length;i++) classRoomItems[i] = classRooms.get(i);
                            AlertDialog ad = new AlertDialog.Builder(a).setTitle("选择教室").setItems(classRoomItems, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ActivityUtils.startLocationActivity_name(a,classRooms.get(i));
                                }
                            }).create();
                            ad.show();
                        }else ActivityUtils.startLocationActivity_name(a,ei.tag2);
//                    Intent i = new Intent(a,ActivityExplore.class);
//                    i.putExtra("terminal",ei.tag2);
//                    a.startActivity(i);
                    }
                });
            }


        }else if (ei.eventType == TIMETABLE_EVENT_TYPE_DYNAMIC) {
            //Log.e("======!!!", String.valueOf(ei));
            dlgView = a.getLayoutInflater().inflate(R.layout.dialog_timetable_dynamic, null);
            final TextView name = dlgView.findViewById(R.id.tt_dlg_name);
            //dialog.setTitle(ei.mainName);
            name.setText(ei.mainName);
            final TextView value1 = dlgView.findViewById(R.id.tt_dlg_value1);
            final TextView value2 = dlgView.findViewById(R.id.tt_dlg_value2);
            final TextView value3 = dlgView.findViewById(R.id.tt_dlg_value3);
            value1.setText(ei.tag2.isEmpty() ? "无" : ei.tag2);//标签1
            value2.setText(ei.tag3.isEmpty() ? "无" : ei.tag3);//标签2
            value2.setText(ei.tag3.isEmpty() ? "无" : ei.tag3);//标签2
            value3.setText(ei.startTime.tellTime() + "-" + ei.endTime.tellTime());
        } else if (ei.eventType == TIMETABLE_EVENT_TYPE_EXAM) {
            dlgView = a.getLayoutInflater().inflate(R.layout.dialog_timetable_exam, null);
            final TextView name = dlgView.findViewById(R.id.tt_dlg_name);
            final TextView value1 = dlgView.findViewById(R.id.tt_dlg_value1);
            final TextView value2 = dlgView.findViewById(R.id.tt_dlg_value2);
            final ImageView detail = dlgView.findViewById(R.id.dlg_bt_detail);
            name.setText(ei.mainName);
            value1.setText(ei.tag2);//考场
            value2.setText(ei.tag4.isEmpty() ? "无" : ei.tag4);//具体考试时间
            detail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // ActivityOptionsCompat ops = ActivityOptionsCompat.makeSceneTransitionAnimation(a,new Pair<View, String>(name,"course_name"));
                    Intent i = new Intent(a, ActivitySubject.class);
                    if(ei.tag3.startsWith("科目代码：")){
                        i.putExtra("useCode",true);
                        i.putExtra("subject", ei.tag3.substring(5));
                       // Log.e("code:",ei.tag3.substring(4));
                    }else{
                        i.putExtra("useCode",false);
                        i.putExtra("subject", ei.tag3);
                    }
                    a.startActivity(i);
                    dialog.dismiss();
                }
            });
        } else if (ei.eventType == TimeTable.TIMETABLE_EVENT_TYPE_ARRANGEMENT) {
            dlgView = a.getLayoutInflater().inflate(R.layout.dialog_timetable_arrangement, null);
            final TextView value1 = dlgView.findViewById(R.id.tt_dlg_value1);
            final TextView value2 = dlgView.findViewById(R.id.tt_dlg_value2);
            final TextView value3 = dlgView.findViewById(R.id.tt_dlg_value3);
            dialog.setTitle(ei.mainName);
            value1.setText(ei.tag2.isEmpty() ? "无" : ei.tag2);//标签1
            value2.setText(ei.tag3.isEmpty() ? "无" : ei.tag3);//标签2
            value3.setText(ei.startTime.tellTime() + "-" + ei.endTime.tellTime());
        } else if (ei.eventType == TimeTable.TIMETABLE_EVENT_TYPE_DEADLINE) {
            dlgView = a.getLayoutInflater().inflate(R.layout.dialog_timetable_deadline, null);
            final TextView value1 = dlgView.findViewById(R.id.tt_dlg_value1);
            final TextView value2 = dlgView.findViewById(R.id.tt_dlg_value2);
            final TextView value3 = dlgView.findViewById(R.id.tt_dlg_value3);
            dialog.setTitle(ei.mainName);
            value1.setText(ei.tag2.isEmpty() ? "无" : ei.tag2);//标签1
            value2.setText(ei.tag3.isEmpty() ? "无" : ei.tag3);//标签2
            value3.setText(ei.startTime.tellTime());
        } else if (ei.eventType == TimeTable.TIMETABLE_EVENT_TYPE_REMIND) {
            dlgView = a.getLayoutInflater().inflate(R.layout.dialog_timetable_remind, null);

            final TextView value1 = dlgView.findViewById(R.id.tt_dlg_value1);
            final TextView value2 = dlgView.findViewById(R.id.tt_dlg_value2);
            final TextView value3 = dlgView.findViewById(R.id.tt_dlg_value3);
            dialog.setTitle(ei.mainName);
            value1.setText(ei.tag2.isEmpty() ? "无" : ei.tag2);//标签1
            value2.setText(ei.tag3.isEmpty() ? "无" : ei.tag3);//标签2
            value3.setText(ei.startTime.tellTime());
        }

        TextView date = dlgView.findViewById(R.id.tt_dlg_date);
        Calendar c = allCurriculum.get(thisCurriculumIndex).getDateAtWOT(ei.week, ei.DOW);
        date.setText(c.get(Calendar.MONTH) + 1 + "月" + c.get(Calendar.DAY_OF_MONTH) + "日" + "(第" + ei.week + "周" + TextTools.words_time_DOW[ei.DOW - 1] + ")");
        if (ei.isWholeDay) {
            final TextView value3 = dlgView.findViewById(R.id.tt_dlg_value3);
            value3.setText("全天");
        }

        dialog.setView(dlgView);
        dialog.show();

    }




}
