package com.stupidtree.hita;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.stupidtree.hita.activities.ActivityRankBoard;
import com.stupidtree.hita.activities.ActivityExplore;
import com.stupidtree.hita.activities.ActivityLogin;
import com.stupidtree.hita.activities.ActivityMain;
import com.stupidtree.hita.core.Curriculum;
import com.stupidtree.hita.core.HITADBHelper;
import com.stupidtree.hita.core.TimeTable;
import com.stupidtree.hita.core.timetable.EventItem;
import com.stupidtree.hita.core.timetable.HTime;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static com.stupidtree.hita.fragments.FragmentTimeLine.showEventDialog;


/**
 * Implementation of App Widget functionality.
 */
public class TimeLineWidget extends AppWidgetProvider {
    private static List<EventItem> todaysEvents;
    private static Calendar now;
    private static  EventItem nowEvent;
    private static EventItem nextEvent;
    private static float nowProgress;
    private static HITADBHelper mDBHelper;
    private static Curriculum curriculum;
    private static TimeTable timeTable;
    private static boolean dataAvailavle;
    private static int thisCurriculum;
    private static boolean hasInit = false;
    private static Set widgetsId = new HashSet(); //注意要是静态的
    private static DecimalFormat df = new DecimalFormat("#0.00");

    Context context;

    public int getTodayCourseNum() {
        int result = 0;
        for (EventItem ei : todaysEvents) {
            if (ei.eventType == TimeTable.TIMETABLE_EVENT_TYPE_COURSE) {
                result++;
            }
        }
        return result;
    }

    public void refreshTodaysEvents() {
        now.setTimeInMillis(System.currentTimeMillis());
        int DOW = now.get(Calendar.DAY_OF_WEEK) == 1 ? 7 : now.get(Calendar.DAY_OF_WEEK) - 1;
        todaysEvents.clear();
        for (EventItem ei : timeTable.getOneDayEvents(curriculum.getWeekOfTerm(now), DOW)) {
            todaysEvents.add(ei);
        }
        Collections.sort(todaysEvents);
    }

    public void refreshNowAndNextEvent() {
        boolean changed_now = false;
        boolean changed_next = false;
        try {
            HTime nowTime = new HTime(now);
            for (int i = todaysEvents.size() - 1; i >= 0; i--) {
                EventItem ei = todaysEvents.get(i);
                if (ei.hasCross(nowTime) && (!ei.isWholeDay)
                        && ei.eventType != TimeTable.TIMETABLE_EVENT_TYPE_DEADLINE
                        && ei.eventType != TimeTable.TIMETABLE_EVENT_TYPE_REMIND
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
                nowProgress = ((float) new HTime(now).getDuration(nowEvent.startTime)) / ((float) nowEvent.endTime.getDuration(nowEvent.startTime));
            }
        } catch (Exception e) {
            e.printStackTrace();
            nowEvent = null;
            nextEvent = null;
            return;
        }
    }

    void updateAllAppWidget(Context context, AppWidgetManager appWidgetManager) {
        Iterator it = widgetsId.iterator();
        //Log.e("set", widgetsId.toString());
        //Toast.makeText(context,widgetsId.toString(),Toast.LENGTH_SHORT).show();
        int appID;
        while (it.hasNext()) {
            appID = ((Integer) it.next()).intValue();
            Log.e("update", String.valueOf(appID));
            boolean hasProgress = false;
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_time_line);
            int ImageToSet;
            int IntentTerminal = 0;
            String classroom = null;
            if (dataAvailavle) {
                refreshTodaysEvents();
                refreshNowAndNextEvent();
                String titleToSet, subtitltToSet;
                if (todaysEvents.size() == 0) {
                    titleToSet = context.getString(R.string.timeline_head_free_title);
                    subtitltToSet = context.getString(R.string.timeline_head_free_subtitle);
                    ImageToSet = R.drawable.ic_timeline_head_free;
                } else if (nowEvent != null) {
                    ImageToSet = R.drawable.ic_now_circle;
                    titleToSet = nowEvent.mainName;
                    hasProgress = true;
                    subtitltToSet = "进度:" + df.format(nowProgress * 100) + "%";
                    IntentTerminal = 6;
                } else {
                    if (new HTime(now).compareTo(new HTime(5, 0)) < 0 && new HTime(now).compareTo(new HTime(0, 0)) > 0) {
                        ImageToSet = R.drawable.ic_moon;
                        titleToSet = context.getString(R.string.timeline_head_goodnight_title);
                        subtitltToSet = context.getString(R.string.timeline_head_goodnight_subtitle);
                    } else if (new HTime(now).compareTo(new HTime(8, 15)) < 0 && new HTime(now).compareTo(new HTime(5, 00)) > 0) {
                        ImageToSet = R.drawable.ic_sunny;
                        titleToSet = context.getString(R.string.timeline_head_goodmorning_title);
                        subtitltToSet = "今天共有" + getTodayCourseNum() + "节课";

                    } else if (new HTime(now).compareTo(new HTime(12, 15)) > 0 && new HTime(now).compareTo(new HTime(13, 00)) < 0) {
                        ImageToSet = R.drawable.ic_lunch;
                        titleToSet = context.getString(R.string.timeline_head_lunch_title);
                        subtitltToSet = context.getString(R.string.timeline_head_lunch_subtitle);
                        IntentTerminal = 4;
                    } else if (new HTime(now).compareTo(new HTime(17, 10)) > 0 && new HTime(now).compareTo(new HTime(18, 10)) < 0) {
                        ImageToSet = R.drawable.ic_lunch;
                        titleToSet = context.getString(R.string.timeline_head_dinner_title);
                        subtitltToSet = context.getString(R.string.timeline_head_dinner_subtitle);
                        IntentTerminal = 4;
                    } else if (nextEvent != null) {
                        if (nextEvent.startTime.getDuration(new HTime(now)) <= 15 && (nextEvent.eventType == TimeTable.TIMETABLE_EVENT_TYPE_COURSE || nextEvent.eventType == TimeTable.TIMETABLE_EVENT_TYPE_EXAM)) {
                            subtitltToSet = "前往" + nextEvent.tag2;
                            titleToSet = nextEvent.mainName + "马上开始";
                            ImageToSet = R.drawable.ic_now_circle;
                            classroom = nextEvent.tag2;
                            IntentTerminal = 3;
                        } else {
                            titleToSet = context.getString(R.string.timeline_head_normal_title);
                            subtitltToSet = context.getString(R.string.timeline_head_normal_subtitle);
                            ImageToSet = R.drawable.ic_sunglasses;
                        }
                    } else {
                        if (new HTime(now).compareTo(new HTime(23, 00)) > 0 || new HTime(now).compareTo(new HTime(5, 0)) < 0) {
                            ImageToSet = R.drawable.ic_moon;
                            titleToSet = context.getString(R.string.timeline_head_goodnight_title);
                            subtitltToSet = context.getString(R.string.timeline_head_goodnight_subtitle);
                        } else {
                            ImageToSet = R.drawable.ic_finish;
                            titleToSet = context.getString(R.string.timeline_head_finish_title);
                            subtitltToSet = context.getString(R.string.timeline_head_finish_subtitle);
                        }
                    }
                }
                views.setTextViewText(R.id.widget_title, titleToSet);
                views.setTextViewText(R.id.widget_subtitle, subtitltToSet);
                views.setImageViewResource(R.id.widget_image, ImageToSet);
            } else {
                views.setTextViewText(R.id.widget_title, "无数据");
                views.setTextViewText(R.id.widget_subtitle, "请先登录后导入课表");
                views.setImageViewResource(R.id.widget_image, R.drawable.ic_timeline_head_login);
                IntentTerminal = 2;
            }
            if(hasProgress){
                views.setProgressBar(R.id.widget_progress,100, (int) (nowProgress*100),false);
                views.setViewVisibility(R.id.widget_progress, View.VISIBLE);
            }else  views.setViewVisibility(R.id.widget_progress, View.GONE);
            views.setImageViewResource(R.id.widget_refresh,R.drawable.ic_refresh);
            Intent i = new Intent();
            PendingIntent pi;
            switch (IntentTerminal){
                case 6:
                case 0:i = new Intent(context, ActivityMain.class);break;
                case 2:i = new Intent(context, ActivityLogin.class);break;
                case 3:i = new Intent(context, ActivityExplore.class);break;
                case 4:i = new Intent(context, ActivityRankBoard.class);break;
              // :i = new Intent(context,TimeLineWidget.class);break;
            }
            if(IntentTerminal==3)i.putExtra("terminal",classroom);
//            if(IntentTerminal==6){
//                i.setAction("TIMELINE_WIDGET_ACTION_SHOW_EVENT_DIALOG");
//                Bundle b = new Bundle();
//                b.putSerializable("ei",nowEvent);
//                i.putExtras(b);
//                pi = PendingIntent.getBroadcast(context,0,i, FLAG_UPDATE_CURRENT);
//            }else
                pi = PendingIntent.getActivity(context,0,i, FLAG_UPDATE_CURRENT);
            Intent i2 = new Intent(context,TimeLineWidget.class);
            i2.setAction("TIMELINE_WIDGET_ACTION_REFRESH");
            PendingIntent pi2 = PendingIntent.getBroadcast(context,0,i2,FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.widge_root,pi);
            views.setOnClickPendingIntent(R.id.widget_refresh,pi2);
            appWidgetManager.updateAppWidget(appID, views);
        }
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        final String action = intent.getAction();
        Log.e("received", "!");
        // 更新 widget 的广播对应的action
        String ACTION_UPDATE_ALL = "com.stupidtree.hita.UPDATE_ALL";
        Log.e("recieved",intent.getAction());
        if (ACTION_UPDATE_ALL.equals(action)) {
            init(context);
            updateAllAppWidget(context, AppWidgetManager.getInstance(context));
        }else if(action.equals("TIMELINE_WIDGET_ACTION_REFRESH")){
            init(context);
            updateAllAppWidget(context, AppWidgetManager.getInstance(context));
            Toast.makeText(context,"刷新部件",Toast.LENGTH_SHORT).show();
        } else if(action.equals("TIMELINE_WIDGET_ACTION_SHOW_EVENT_DIALOG")){
            EventItem ei = (EventItem) intent.getExtras().getSerializable("ei");
           // Toast.makeText(context,ei.toString(),Toast.LENGTH_SHORT).show();

           // showEventDialog(context,ei,null,null);
        }
        else if (intent.hasCategory(Intent.CATEGORY_ALTERNATIVE)) {
        }
    }

    void init(Context context) {
        this.context = context;
        now = Calendar.getInstance();
        todaysEvents = new ArrayList<>();
        mDBHelper = new HITADBHelper(context);
        thisCurriculum = PreferenceManager.getDefaultSharedPreferences(context).getInt("thisCurriculum", 0);
        SQLiteDatabase sq = mDBHelper.getReadableDatabase();
        Cursor c = sq.query("curriculum", null, null, null, null, null, null);
        int i = -1;
        while (c.moveToNext()) {
            i++;
            if (i == thisCurriculum) break;
        }

        if (i == thisCurriculum) {
            dataAvailavle = true;
            curriculum = new Curriculum(c);
            timeTable = new TimeTable(curriculum);
        } else {
            dataAvailavle = false;
            curriculum = null;
            timeTable = null;
        }
        c.close();
        hasInit = true;
    }

    /**
     * 当小部件从备份恢复时调用该方法
     */
    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        super.onRestored(context, oldWidgetIds, newWidgetIds);
    }



    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context,appWidgetManager,appWidgetIds);
        if (!hasInit) init(context);
        for (int appWidgetId : appWidgetIds) {
            widgetsId.add(Integer.valueOf(appWidgetId));
        }
        updateAllAppWidget(context,appWidgetManager);
    }

    @Override
    public void onEnabled(Context context) { //不能在这里初始化！！！
        Intent intent = new Intent(context, WidgetService.class);
        context.startService(intent); //开始服务
        super.onEnabled(context);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // 当 widget 被删除时，对应的删除list中保存的widget的id
        Log.e("delete", String.valueOf(appWidgetIds.length));
        for (int appWidgetId : appWidgetIds) {
            widgetsId.remove(Integer.valueOf(appWidgetId));
        }
        super.onDeleted(context, appWidgetIds);
    }

    /**
     * 当最后一个该窗口小部件删除时调用该方法，注意是最后一个
     */
    @Override
    public void onDisabled(Context context) {
        // 在最后一个 widget 被删除时，终止服务
        Intent intent = new Intent(context, WidgetService.class);
        context.stopService(intent);
        super.onDisabled(context);
    }
}

