package com.stupidtree.hita;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.text.TextUtils;

import com.stupidtree.hita.activities.ActivityLogin;
import com.stupidtree.hita.activities.ActivityLoginJWTS;
import com.stupidtree.hita.activities.ActivityMain;
import com.stupidtree.hita.activities.ActivityUserCenter;
import com.stupidtree.hita.timetable.TimeTableGenerator;
import com.stupidtree.hita.timetable.TimetableCore;
import com.stupidtree.hita.timetable.timetable.EventItem;
import com.stupidtree.hita.timetable.timetable.HTime;
import com.stupidtree.hita.timetable.timetable.Task;

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
    private NotificationCompat.Builder notificationBuilder;
    // private FragmentTimeLine fragmentTimeLine = null;
    private DecimalFormat df = new DecimalFormat("#.##%");
    public VolumeChangeReciever volumeChangeReciever;
    private AudioManager audioManager;

    public TimeWatcher(Application application) {
        audioManager = (AudioManager) HContext.getSystemService(Context.AUDIO_SERVICE);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);//每分钟变化
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        TimeChangeReceiver timeChangeReceiver = new TimeChangeReceiver();
        application.registerReceiver(timeChangeReceiver, intentFilter);
        volumeChangeReciever = new VolumeChangeReciever();

        if (defaultSP.getBoolean("auto_mute",false)&&defaultSP.getBoolean("forced_mute", false)){
            IntentFilter if2 = new IntentFilter();
            if2.addAction("android.media.VOLUME_CHANGED_ACTION");
            application.registerReceiver(volumeChangeReciever, if2);
        }

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
            new timeTickTask().executeOnExecutor(HITAApplication.TPE);
        }

    }

    class VolumeChangeReciever extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (nowEvent != null && nowEvent.eventType == TimetableCore.TIMETABLE_EVENT_TYPE_COURSE) {
                //Log.e("volume_change","cccc");
                audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 0, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 0, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, 0);
            }

        }
    }


    public void refreshProgress(boolean fromOther, boolean refreshTask) {
        try {
            timeTableCore.setThisWeekOfTerm(timeTableCore.getCurrentCurriculum().getWeekOfTerm(now));
        } catch (Exception e) {
            timeTableCore.setThisWeekOfTerm(-1);
        }
        if (defaultSP.getBoolean("dtt_preview", false) && timeTableCore.isDataAvailable()) {
            TimeTableGenerator.Dynamic_PreviewPlan(now);
        } else if (timeTableCore.isDataAvailable()) {
            timeTableCore.clearTask(":::");
            // timeTableCore.clearEvent(TimetableCore.TIMETABLE_EVENT_TYPE_DYNAMIC);
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
            if (ei.eventType == TimetableCore.TIMETABLE_EVENT_TYPE_COURSE) {
                result++;
            }
        }
        return result;
    }

    private void updateTaskProgress() {
        if (!timeTableCore.isDataAvailable()) return;
        List<EventItem> events = timeTableCore.getAllEvents();
        for (EventItem ei : events) {
            if (!ei.hasPassed(now) || TextUtils.isEmpty(ei.tag4) || ei.tag4 != null && ei.tag4.equals("null"))
                continue;
            Task t = timeTableCore.getTaskWithUUID(ei.tag4);
            if (t != null) {
                if (t.getEvent_map().get(ei.getUuid() + ":::" + ei.week) != null && !t.getEvent_map().get(ei.getUuid() + ":::" + ei.week)) {
                    t.putEventMap(ei.getUuid() + ":::" + ei.week, true);
                    float newProgress = (float) (100 * ((float) t.getProgress() / 100.0 * t.getLength() + ei.getDuration()) / t.getLength());
                    t.updateProgress((int) newProgress);
                    if (newProgress >= 100f) timeTableCore.setFinishTask(t,true);
                }
            }
        }
        List<Task> taks = timeTableCore.getUnfinishedTasks();
        for (Task t : taks) {
            if (!t.has_deadline) continue;
            Calendar end = timeTableCore.getCurrentCurriculum().getDateAt(t.tW, t.tDOW, t.eTime);
            if (end.before(now)) timeTableCore.setFinishTask(t,true);
        }

    }

    private void refreshTodaysEvents() {
        if (!timeTableCore.isDataAvailable()) return;
        int DOW = now.get(Calendar.DAY_OF_WEEK) == 1 ? 7 : now.get(Calendar.DAY_OF_WEEK) - 1;
        todaysEvents.clear();
        timeTableCore.setThisWeekOfTerm(timeTableCore.getCurrentCurriculum().getWeekOfTerm(now));
        timeTableCore.setThisTerm(timeTableCore.getThisWeekOfTerm()>=0);
        todaysEvents.addAll(timeTableCore.getOneDayEvents(timeTableCore.getThisWeekOfTerm(), DOW));
        Collections.sort(todaysEvents);
    }

    public void refreshNowAndNextEvent() {
        HTime nowTime = new HTime(now);
        try {
            boolean changed_now = false;
            boolean changed_next = false;
            for (int i = todaysEvents.size() - 1; i >= 0; i--) {
                EventItem ei = todaysEvents.get(i);
                if (ei.hasCross(nowTime) && (!ei.isWholeDay)
                        && ei.eventType != TimetableCore.TIMETABLE_EVENT_TYPE_DEADLINE
                        && ei.eventType != TimetableCore.TIMETABLE_EVENT_TYPE_REMIND
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

        } catch (Exception e) {
            e.printStackTrace();
//            nowEvent = null;
//            nextEvent = null;
            return;
        }
        try {
            boolean next_on = false;
            boolean autoMute = defaultSP.getBoolean("auto_mute", false);
            if (nowEvent != null) {
                nowProgress = ((float) new HTime(now).getDuration(nowEvent.startTime)) / ((float) nowEvent.endTime.getDuration(nowEvent.startTime));
                if (autoMute) {
                    String x = defaultSP.getString("mute_course", null);
                    if (nowEvent.eventType == TimetableCore.TIMETABLE_EVENT_TYPE_COURSE && (x == null || !nowEvent.equalsEvent(x))) {
                        startMute();
                        defaultSP.edit().putString("mute_course", nowEvent.getEventsIdStr()).apply();
                        sendNotification("已开启静音", "HITSZ学习助手");
                    }
                }

            }
            if (nextEvent != null && nextEvent.eventType == TimetableCore.TIMETABLE_EVENT_TYPE_COURSE) {
                if (autoMute) {
                    String x = defaultSP.getString("mute_course", null);
                    if (x == null || !nextEvent.equalsEvent(x)) {
                        if (nextEvent.startTime.getDuration(nowTime) <= defaultSP.getInt("auto_mute_before", 15)) {
                            startMute();
                            defaultSP.edit().putString("mute_course", nextEvent.getEventsIdStr()).apply();
                            sendNotification("已开启静音", "HITSZ学习助手");
                            next_on = true;
                        }
                    }
                }

            }

            if ((nowEvent == null && !next_on) || (nowEvent != null && nowEvent.eventType != TimetableCore.TIMETABLE_EVENT_TYPE_COURSE)) {
                if (autoMute && defaultSP.getBoolean("auto_mute_after", true)) {
                    String mute = defaultSP.getString("mute_course", null);
                    //  Log.e("mute",mute);
                    if (mute != null) {
                        boolean has = false;
                        for (EventItem ei : todaysEvents) {
                            if (ei.eventType != TimetableCore.TIMETABLE_EVENT_TYPE_COURSE) continue;
                            if (ei.getEventsIdStr().equals(mute)) {
                                has = true;
                                if (ei.endTime.before(nowTime)) {
                                    finishMute();
                                    sendNotification("已关闭静音", "HITSZ学习助手");
                                    defaultSP.edit().putString("mute_course", null).apply();
                                    break;
                                }

                            }
                        }
                        if (!has) {
                            finishMute();
                            sendNotification("已关闭静音", "HITSZ学习助手");
                            defaultSP.edit().putString("mute_course", null).apply();

                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //if(nowEvent==null&&)

    }

    private void startMute() {
        if (audioManager != null) {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            audioManager.getStreamVolume(AudioManager.STREAM_RING);
        }
    }

    private void finishMute() {

        AudioManager audioManager = (AudioManager) HContext.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            audioManager.getStreamVolume(AudioManager.STREAM_RING);
        }
    }

    private void sendNotification() {
        String title;
        String content;
        int IntentTerminal = 0;
        if (CurrentUser == null || !timeTableCore.isDataAvailable()) {
            title = "欢迎使用HITSZ助手";
            content = "请登录导入课表后使用";
            if (CurrentUser == null) IntentTerminal = 1;
            else if (TextUtils.isEmpty(CurrentUser.getStudentnumber())) IntentTerminal = 2;
            else if (!timeTableCore.isDataAvailable()) IntentTerminal = 3;
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

        Notification notification = notificationBuilder.build();

        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notificationManager.notify(R.string.app_notification_channel_id, notification);
    }

    private void sendNotification(String title, String content) {
        notificationBuilder.setContentTitle(title);
        notificationBuilder.setContentText(content);
        Notification n = notificationBuilder.build();
        notificationManager.notify(R.string.app_notification_channel_id_learning, n);
    }

    @SuppressLint("StaticFieldLeak")
    class timeTickTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                refreshProgress(false, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
