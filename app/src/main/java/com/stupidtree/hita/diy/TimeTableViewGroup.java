package com.stupidtree.hita.diy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;
import com.stupidtree.hita.timetable.TimetableCore;
import com.stupidtree.hita.timetable.timetable.EventItem;
import com.stupidtree.hita.timetable.timetable.HTime;
import com.stupidtree.hita.timetable.timetable.TimePeriod;
import com.stupidtree.hita.util.TimeTableNowLine;


import java.util.List;

import tyrantgit.explosionfield.ExplosionField;

import static com.stupidtree.hita.HITAApplication.now;
import static com.stupidtree.hita.HITAApplication.timeTableCore;
import static com.stupidtree.hita.timetable.TimetableCore.TIMETABLE_EVENT_TYPE_COURSE;
import static com.stupidtree.hita.timetable.TimetableCore.TIMETABLE_EVENT_TYPE_DEADLINE;
import static com.stupidtree.hita.timetable.TimetableCore.TIMETABLE_EVENT_TYPE_REMIND;
import static com.stupidtree.hita.fragments.main.FragmentTimeLine.showEventDialog;

public class TimeTableViewGroup extends ViewGroup{
    int week;
    int width,height;
    int sectionWidth, sectionHeight = 180;

    HTime startTime = new HTime(0, 0);
    BaseActivity activityContext;
    TimeTableBlockAddView addButton = null;
    TimeTableBlockView.TimeTablePreferenceRoot root;

    public TimeTableViewGroup(Context context) {
        super(context);
    }

    public TimeTableViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TimeTableViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public void init(BaseActivity activity, int week, TimeTableBlockView.TimeTablePreferenceRoot root) {
        this.week = week;
        this.activityContext = activity;
        this.root = root;
        this.sectionHeight = root.getCardHeight();
        this.startTime = root.getStartTime();
        setClickable(true); //设置为可点击，否则onTouchEvent只返回DOWN
        if(timeTableCore.isThisTerm()&&week==timeTableCore.getThisWeekOfTerm()){
            View v = new View(getContext());
            v.setBackgroundColor(root.getTodayBGColor());
           // v.setAlpha(0.3f);
            addView(v);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //
       // Log.e("action", String.valueOf(event.getAction()));
       if(event.getAction()==MotionEvent.ACTION_UP){
           removeView(addButton);
           int dow = (int)event.getX()/sectionWidth+1;
           HTime time = startTime.getAdded((int)(event.getY()/sectionHeight*60f));
           TimePeriod period = timeTableCore.getClassSimplfiedTimeByTimeContainedIn(time);
           if(period!=null){
               if(period.start.before(startTime)) period.setStart(startTime);
               addButton = new TimeTableBlockAddView(activityContext,week,dow,
                       period
               );
               addView(addButton);

           }
       }else{
           removeView(addButton);
       }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        sectionWidth = width / 7;
        //setMeasuredDimension(width, height);
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if(child instanceof TimeTableBlockView){
                int cw = MeasureSpec.makeMeasureSpec(sectionWidth, MeasureSpec.EXACTLY);
                int cH;
                EventItem eventItem = ((TimeTableBlockView) child).getEvent();
                if(eventItem.eventType!= TIMETABLE_EVENT_TYPE_DEADLINE&&eventItem.eventType!=TIMETABLE_EVENT_TYPE_REMIND){
                    cH = MeasureSpec.makeMeasureSpec(getCardHeight((TimeTableBlockView) child), MeasureSpec.EXACTLY);
                }else{
                    cH = MeasureSpec.makeMeasureSpec((int) ((18/60f)*sectionHeight), MeasureSpec.EXACTLY);
                }
                child.measure(cw, cH);
            }else if(child instanceof TimeTableNowLine) {
                int cw = MeasureSpec.makeMeasureSpec(sectionWidth, MeasureSpec.EXACTLY);
                int cH = MeasureSpec.makeMeasureSpec(4, MeasureSpec.EXACTLY);
                child.measure(cw, cH);
            }else if(child instanceof TimeTableBlockAddView) {
                int cw = MeasureSpec.makeMeasureSpec(sectionWidth, MeasureSpec.EXACTLY);
                int cH = MeasureSpec.makeMeasureSpec((int) ((((TimeTableBlockAddView) child).getDuration() / 60f) * sectionHeight), MeasureSpec.EXACTLY);
                child.measure(cw, cH);
            }else {
                this.measureChild(child, widthMeasureSpec, heightMeasureSpec);
            }

            //
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();//获得子控件个数
        for (int i = 0; i < count; i++) {

            View child = getChildAt(i);
            if(child instanceof TimeTableBlockView ) {
                TimeTableBlockView block = (TimeTableBlockView) child;
                float lastTime = block.getDuration();
                float startTimeFromBeginning = startTime.getDuration(block.getEvent().startTime);
                int courseInWeek = block.getDow() - 1;//获得周几
                //计算左边的坐标
                int left = (sectionWidth * courseInWeek);
                //计算右边坐标
                int right = (left + sectionWidth);
                //计算顶部坐标
                int top = (int) ((startTimeFromBeginning / 60f) * sectionHeight);
                //计算底部坐标
                if (((TimeTableBlockView) child).getEvent().eventType == TIMETABLE_EVENT_TYPE_DEADLINE || ((TimeTableBlockView) child).getEvent().eventType == TIMETABLE_EVENT_TYPE_REMIND) {
                    lastTime = 18;
                }
                int bottom = top + (int) ((lastTime / 60f) * sectionHeight);
                // block.measure(sectionWidth,getCardHeight(block));
                block.layout(left, top, right, bottom);
            }else if(child instanceof TimeTableNowLine){
                View nowL = child;
                int left = sectionWidth* (TimetableCore.getDOW(now)-1);
                float startTimeFromBeginning = startTime.getDuration(new HTime(now));
                int top = (int) ((startTimeFromBeginning / 60f) * sectionHeight);
                nowL.layout(0, top,width,top+4);
            }else if(child instanceof TimeTableBlockAddView){
                TimeTableBlockAddView add = (TimeTableBlockAddView) child;
                int left = sectionWidth*(add.getDow()-1);
                int right = (left + sectionWidth);
                float startTimeFromBeginning = startTime.getDuration(add.getTimePeriod().start);
                int top = (int) ((startTimeFromBeginning / 60f) * sectionHeight);
                int bottom = top + (int) ((add.getDuration() / 60f) * sectionHeight);
                //Log.e("pos",top+","+bottom+","+left+","+right);
                add.layout(left, top, right,bottom);
            } else if(child != null){
                View today = child;
                int left = sectionWidth* (TimetableCore.getDOW(now)-1);
                int right = (left + sectionWidth);
                today.layout(left, 0, right,height);
            }


            // Log.e("mes:",top+","+bottom+","+left+","+right);
        }
    }

    private int getCardHeight(TimeTableBlockView timeTableBlockView) {
        return (int) ((timeTableBlockView.getDuration() / 60f) * sectionHeight);
    }




    public void addBlock(Object o){
        if(o instanceof EventItem){

            TimeTableBlockView timeTableBlockView = new TimeTableBlockView(getContext(), o,root);
            timeTableBlockView.setOnCardClickListener(new TimeTableBlockView.OnCardClickListener() {
                @Override
                public void OnClick(View v, EventItem ei) {
                    showEventDialog(getContext(),ei,null,null);
                    Log.e("!","click");
                }
            });
            timeTableBlockView.setOnCardLongClickListener(new TimeTableBlockView.OnCardLongClickListener() {
                @Override
                public boolean OnLongClick(final View v, final EventItem ei) {
                    PopupMenu pm  = new PopupMenu(getContext(),v);
                    pm.inflate(R.menu.menu_opr_timetable);
                    pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if(item.getItemId()==R.id.opr_delete){
                                AlertDialog ad = new AlertDialog.Builder(getContext()).
                                        setNegativeButton("取消", null)
                                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                timeTableCore.deleteEvent(ei, ei.eventType == TIMETABLE_EVENT_TYPE_DEADLINE);
                                                ExplosionField ef = ExplosionField.attach2Window(activityContext);
                                                ef.explode(v);
                                                v.setVisibility(View.GONE);

                                            }
                                        }).
                                                create();
                                ad.setTitle("确定删除吗？");
                                if(ei.eventType==TIMETABLE_EVENT_TYPE_COURSE){
                                    ad.setMessage("删除课程后,可以通过导入课表或同步云端数据恢复初始课表");
                                }

                                ad.show();
                            }
                            return true;
                        }
                    });
                    pm.show();
                    return true;
                }
            });
            addView(timeTableBlockView);
        }else if(o instanceof List){
            TimeTableBlockView timeTableBlockView = new TimeTableBlockView(getContext(), o,root);
            timeTableBlockView.setOnDuplicateCardClickListener(new TimeTableBlockView.OnDuplicateCardClickListener() {
                @Override
                public void OnDuplicateClick(View v, final List<EventItem> list) {
                    String[] items = new String[list.size()];
                    for(int i=0;i<items.length;i++){
                        items[i] = list.get(i).mainName;
                    }
                    AlertDialog ad = new AlertDialog.Builder(getContext()).setTitle("选择事件").setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            showEventDialog(getContext(),list.get(which),null,null);
                        }
                    }).create();
                    ad.show();
                }
            });
            addView(timeTableBlockView);
        }

    }


}
