package com.stupidtree.hita;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;

import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import android.text.TextUtils;

import com.stupidtree.hita.activities.ActivityLogin;
import com.stupidtree.hita.activities.ActivityLoginJWTS;
import com.stupidtree.hita.activities.ActivityMain;
import com.stupidtree.hita.activities.ActivityUserCenter;
import com.stupidtree.hita.core.TimeTable;
import com.stupidtree.hita.core.TimeTableGenerator;
import com.stupidtree.hita.core.timetable.EventItem;
import com.stupidtree.hita.core.timetable.HTime;
import com.stupidtree.hita.core.timetable.Task;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static com.stupidtree.hita.HITAApplication.*;

public class TimeWatcher {
    public static EventItem nowEvent;
    public static EventItem nextEvent;
    public static float nowProgress;
    public static List<EventItem> todaysEvents;
    private NotificationManager notificationManager;
    NotificationCompat.Builder notificationBuilder;
    Notification notification;
    // private FragmentTimeLine fragmentTimeLine = null;
    DecimalFormat df = new DecimalFormat("#.##%");

    public TimeWatcher(Application application) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);//每分钟变化
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        TimeChangeReceiver timeChangeReceiver = new TimeChangeReceiver();
        application.registerReceiver(timeChangeReceiver, intentFilter);
        now.setTimeInMillis(System.currentTimeMillis());
        initNotification(application);
        todaysEvents = new ArrayList<>();
        refreshProgress(false, true);
    }

    void initNotification(Application application) {


        notificationManager = (NotificationManager) application.getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(application.getString(R.string.app_notification_channel_id), "hita channel", NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(false); //是否在桌面icon右上角展示小红点    channel.setLightColor(Color.RED); //小红点颜色    channel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知    notificationManager.createNotificationChannel(channel);}
            channel.setImportance(NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableVibration(false);
            channel.setVibrationPattern(new long[]{0});
            channel.setSound(null, null);
            notificationManager.createNotificationChannel(channel);
        }
        notificationBuilder = new NotificationCompat.Builder(application, application.getString(R.string.app_notification_channel_id));
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setLargeIcon(BitmapFactory.decodeResource(HContext.getResources(), R.mipmap.ic_launcher_foreground)) //设置通知的大图标
                .setAutoCancel(false);//设置通知被点击一次是否自动取消

    }
//
//    public void bindTimeLine(FragmentTimeLine ftl) {
//        fragmentTimeLine = ftl;
//    }


    class TimeChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            now.setTimeInMillis(System.currentTimeMillis());
            switch (Objects.requireNonNull(intent.getAction())) {
                case Intent.ACTION_TIME_CHANGED:
                case Intent.ACTION_TIMEZONE_CHANGED:
                    refreshTodaysEvents();
            }
            new timeTickTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

    }


    public void refreshProgress(boolean fromOther, boolean refreshTask) {
        try {
            thisWeekOfTerm = allCurriculum.get(thisCurriculumIndex).getWeekOfTerm(now);
        } catch (Exception e) {
            thisWeekOfTerm = -1;
        }
        if (defaultSP.getBoolean("dtt_preview", false) && isDataAvailable()) {
            TimeTableGenerator.Dynamic_PreviewPlan(now);
        } else if (isDataAvailable()) {
            mainTimeTable.clearTask(":::");
            // mainTimeTable.clearEvent(TimeTable.TIMETABLE_EVENT_TYPE_DYNAMIC);
        }
        refreshTodaysEvents();
        refreshNowAndNextEvent();
        updateTaskProgress();
        if (defaultSP.getBoolean("notification", true))
            sendNotification();
        if (!fromOther) {
            Intent mes = new Intent("COM.STUPIDTREE.HITA.TIMELINE_REFRESH");
            mes.putExtra("from", "time_tick");
            LocalBroadcastManager.getInstance(HContext).sendBroadcast(mes);
            //fragmentTimeLine.Refresh(FragmentTimeLine.TL_REFRESH_FROM_TIMETICK);
        }
        if (refreshTask) {
            Intent mes2 = new Intent("COM.STUPIDTREE.HITA.TASK_REFRESH");
            LocalBroadcastManager.getInstance(HContext).sendBroadcast(mes2);
        }
    }

    public int getTodayCourseNum() {
        int result = 0;
        for (EventItem ei : todaysEvents) {
            if (ei.eventType == TimeTable.TIMETABLE_EVENT_TYPE_COURSE) {
                result++;
            }
        }
        return result;
    }

    public void updateTaskProgress() {
        if (!isDataAvailable()) return;
        List<EventItem> events = mainTimeTable.getAllEvents();
        for (EventItem ei : events) {
            if (!ei.hasPassed(now) || TextUtils.isEmpty(ei.tag4) || ei.tag4 != null && ei.tag4.equals("null"))
                continue;
            Task t = mainTimeTable.getTaskWithUUID(ei.tag4);
            if (t != null) {
                if (t.getEvent_map().get(ei.getUuid() + ":::" + ei.week) != null && !t.getEvent_map().get(ei.getUuid() + ":::" + ei.week)) {
                    t.putEventMap(ei.getUuid() + ":::" + ei.week, true);
                    float newProgress = (float) (100 * ((float) t.getProgress() / 100.0 * t.getLength() + ei.getDuration()) / t.getLength());
                    t.updateProgress((int) newProgress);
                    if (newProgress >= 100f) mainTimeTable.finishTask(t);
                }
            }
        }
        List<Task> taks = mainTimeTable.getUnfinishedTasks();
        for (Task t : taks) {
            if (!t.has_deadline) continue;
            Calendar end = mainTimeTable.core.getDateAt(t.tW, t.tDOW, t.eTime);
            if (end.before(now)) mainTimeTable.finishTask(t);
        }

    }

    public void refreshTodaysEvents() {
        if (!isDataAvailable()) return;
        int DOW = now.get(Calendar.DAY_OF_WEEK) == 1 ? 7 : now.get(Calendar.DAY_OF_WEEK) - 1;
        todaysEvents.clear();
        thisWeekOfTerm = allCurriculum.get(thisCurriculumIndex).getWeekOfTerm(now);
        if (thisWeekOfTerm < 0) isThisTerm = false;
        else isThisTerm = true;
        for (EventItem ei : mainTimeTable.getOneDayEvents(thisWeekOfTerm, DOW)) {
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
                String x = defaultSP.getString("mute_course", null);
                if (x == null || !nowEvent.equalsEvent(x)) {
                    startMute();
                    defaultSP.edit().putString("mute_course",nowEvent.getEventsIdStr()).apply();
                    sendNotification("已开启静音", "HITSZ学习助手");
                }
            }
            if (nextEvent != null && nextEvent.eventType == TimeTable.TIMETABLE_EVENT_TYPE_COURSE) {
                String x = defaultSP.getString("mute_course", null);
                if (x == null || !nextEvent.equalsEvent(x)) {
                    if (nextEvent.startTime.getDuration(nowTime) <= defaultSP.getInt("auto_mute_before", 15)) {
                        startMute();
                        defaultSP.edit().putString("mute_course", nextEvent.getEventsIdStr()).apply();
                        sendNotification("已开启静音", "HITSZ学习助手");
                    }
                }
            }
            //if(nowEvent==null&&)
        } catch (Exception e) {
            e.printStackTrace();
            nowEvent = null;
            nextEvent = null;
            return;
        }
    }

    public void startMute() {

        AudioManager audioManager = (AudioManager) HContext.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            audioManager.getStreamVolume(AudioManager.STREAM_RING);
        }
    }

    public void sendNotification() {
        String title;
        String content;
        int IntentTerminal = 0;
        if (CurrentUser == null || !isDataAvailable()) {
            title = "欢迎使用HITSZ助手";
            content = "请登录导入课表后使用";
            if (CurrentUser == null) IntentTerminal = 1;
            else if (TextUtils.isEmpty(CurrentUser.getStudentnumber())) IntentTerminal = 2;
            else if (!isDataAvailable()) IntentTerminal = 3;
        } else if (todaysEvents.size() == 0) {
            title = "今日空闲";
            content = "自由发挥";
            IntentTerminal = 0;
        } else if (nowEvent == null) {
            if (new HTime(now).compareTo(new HTime(23, 0)) > 0 || new HTime(now).compareTo(new HTime(5, 0)) < 0) {
                title = "夜深了";
                content = "晚安！";
                IntentTerminal = 0;
            } else if (nextEvent == null) {
                title = "接下来没有事件";
                content = "享受生活吧";
                IntentTerminal = 0;
            } else {
                String timeText;
                int duration = nextEvent.startTime.getDuration(new HTime(now));
                if (duration >= 60)
                    timeText = duration / 60 + "小时" + (duration % 60 == 0 ? "" : duration % 60 + "分钟");
                else timeText = duration + "分钟";
                title = "还有" + timeText;
                content = "进行 " + nextEvent.mainName;
                IntentTerminal = 0;
                //message = "距离下一个事件："+  +
            }
        } else {
            title = "正在进行(" + df.format(nowProgress) + ")";
            content = nowEvent.mainName;
            IntentTerminal = 0;
        }

        notificationBuilder.setContentTitle(title);
        notificationBuilder.setContentText(content);
        Intent intent;
        switch (IntentTerminal) {
            case 0:
                intent = new Intent(HContext, ActivityMain.class);
                break;
            case 1:
                intent = new Intent(HContext, ActivityLogin.class);
                break;
            case 2:
                intent = new Intent(HContext, ActivityUserCenter.class);
                break;
            case 3:
                intent = new Intent(HContext, ActivityLoginJWTS.class);
                break;
            default:
                intent = new Intent(HContext, ActivityMain.class);
                break;
        }
        //intent.putExtra("extra","exxxx");
        // pendingIntent = PendingIntent.getBroadcast(HContext, 0,intent, FLAG_UPDATE_CURRENT);
        PendingIntent pendingIntent = PendingIntent.getActivity(HContext, 0, intent, FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(pendingIntent);
        notificationBuilder.setVibrate(null);
        notificationBuilder.setSound(null);
        notificationBuilder.setLights(0, 0, 0);

        notification = notificationBuilder.build();

        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notificationManager.notify(R.string.app_notification_channel_id, notification);
    }

    public void sendNotification(String title, String content) {
        notificationBuilder.setContentTitle(title);
        notificationBuilder.setContentText(content);
        Notification n = notificationBuilder.build();
        notificationManager.notify(R.string.app_notification_channel_id, n);
    }

    class timeTickTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            refreshProgress(false, true);
            return null;
        }
    }
}
