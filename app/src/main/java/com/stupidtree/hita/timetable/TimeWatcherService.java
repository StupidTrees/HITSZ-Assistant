package com.stupidtree.hita.timetable;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.WorkerThread;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivityCurriculumManager;
import com.stupidtree.hita.activities.ActivityLogin;
import com.stupidtree.hita.activities.ActivityMain;
import com.stupidtree.hita.activities.ActivityUserCenter;
import com.stupidtree.hita.timetable.packable.EventItem;
import com.stupidtree.hita.timetable.packable.HTime;
import com.stupidtree.hita.timetable.packable.Task;
import com.stupidtree.hita.util.EventsUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.TPE;
import static com.stupidtree.hita.HITAApplication.defaultSP;
import static com.stupidtree.hita.HITAApplication.timeTableCore;

public class TimeWatcherService extends Service {
    public static final String WATCHER_REFRESH = "COM.STUPIDTREE.HITA.WATCHER_REFRESH";
    public static final String TIMETABLE_CHANGED = "COM.STUPIDTREE.HITA.TIMETABLE_CHANGED";
    public static final String USER_CHANGED = "COM.STUPIDTREE.HITA.USER_CHANGED";
    public static final int NOTIFICATION_ON = 893;
    public static final int NOTIFICATION_OFF = 759;
    public static final int NOTIFICATION_NOT_SPECIFIC = 776;
    private EventItem nowEvent;
    private EventItem nextEvent;
    private float nowProgress;
    private List<EventItem> todaysEvents;
    private NotificationManager notificationManager;
    // private NotificationCompat.Builder notificationBuilder;
    private DecimalFormat df = new DecimalFormat("#.##%");
    private VolumeChangeReciever volumeChangeReciever;
    private SelfRefreshReceiver selfRefreshReceiver;
    private TimeChangeReceiver timeChangeReceiver;
    private AudioManager audioManager;
    LocalBroadcastManager localBroadcastManager;
    private TimeServiceBinder mBinder = new TimeServiceBinder();

    public TimeWatcherService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        initBroadcast();
        initNotification();
        todaysEvents = new ArrayList<>();
    }

    void initBroadcast() {
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);//每分钟变化
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        timeChangeReceiver = new TimeChangeReceiver();
        selfRefreshReceiver = new SelfRefreshReceiver();
        registerReceiver(timeChangeReceiver, intentFilter);
        IntentFilter selfIf = new IntentFilter();
        selfIf.addAction(TIMETABLE_CHANGED);
        selfIf.addAction(WATCHER_REFRESH);
        localBroadcastManager.registerReceiver(selfRefreshReceiver, selfIf);
        volumeChangeReciever = new VolumeChangeReciever();
        if (defaultSP.getBoolean("auto_mute", false) && defaultSP.getBoolean("forced_mute", false)) {
            IntentFilter if2 = new IntentFilter();
            if2.addAction("android.media.VOLUME_CHANGED_ACTION");
            registerReceiver(volumeChangeReciever, if2);
        }
    }

    void initNotification() {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(getString(R.string.app_notification_channel_id), "HITSZ助手", NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(false); //是否在桌面icon右上角展示小红点    channel.setLightColor(Color.RED); //小红点颜色    channel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知    notificationManager.createNotificationChannel(channel);}
            channel.setImportance(NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableVibration(false);
            channel.setVibrationPattern(new long[]{0});
            channel.setSound(null, null);
            //  notificationManager.createNotificationChannel(channel);

            NotificationChannel channel2 = new NotificationChannel(getString(R.string.app_notification_channel2_id), "HITSZ学习助手", NotificationManager.IMPORTANCE_HIGH);
            channel2.enableLights(true); //是否在桌面icon右上角展示小红点    channel.setLightColor(Color.RED); //小红点颜色    channel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知    notificationManager.createNotificationChannel(channel);}
            channel2.setImportance(NotificationManager.IMPORTANCE_HIGH);
            channel2.enableVibration(true);
            channel2.enableLights(true);
            channel2.setShowBadge(true);
            channel2.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel2.setVibrationPattern(new long[]{100, 100, 100});

            NotificationChannel channel3 = new NotificationChannel(getString(R.string.app_notification_channel3_id), "HITSZ时间服务", NotificationManager.IMPORTANCE_NONE);
            channel3.setImportance(NotificationManager.IMPORTANCE_NONE);
            channel3.setShowBadge(false);
            channel3.enableLights(false);
            channel3.enableVibration(false);

            notificationManager.createNotificationChannels(Arrays.asList(channel, channel2, channel3));
            // notificationManager.createNotificationChannels();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        localBroadcastManager.unregisterReceiver(timeChangeReceiver);
        localBroadcastManager.unregisterReceiver(selfRefreshReceiver);
    }

    @WorkerThread
    private void refreshProgress(int switchNotification) {
        timeTableCore.syncTimeFlags();
        if (defaultSP.getBoolean("dtt_preview", false) && timeTableCore.isDataAvailable()) {
            TimeTableGenerator.Dynamic_PreviewPlan(timeTableCore.getNow());
        } else if (timeTableCore.isDataAvailable()) {
            timeTableCore.clearTask(":::");
            // timeTableCore.clearEvent(TimetableCore.DYNAMIC);
        }
        refreshTodaysEvents();
        refreshNowAndNextEvent();
        updateTaskProgress();
        if (switchNotification == NOTIFICATION_NOT_SPECIFIC) {
            if (defaultSP.getBoolean("notification", true)) sendNotification();
            else notificationManager.cancel(R.string.app_notification_channel_id);
        }
        if (switchNotification == NOTIFICATION_ON) sendNotification();
        else if (switchNotification == NOTIFICATION_OFF)
            notificationManager.cancel(R.string.app_notification_channel_id);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, getString(R.string.app_notification_channel3_id));
        notificationBuilder.setSmallIcon(R.drawable.notification_logo_small)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.logo)); //设置通知的大图标
//                .setAutoCancel(false);//设置通知被点击一次是否自动取消

        notificationBuilder.setContentTitle("HITSZ助手");
        notificationBuilder.setContentText("时间服务已启动");
        Notification n = notificationBuilder.build();
        startForeground(0, n);
        return super.onStartCommand(intent, flags, startId);

    }

    private int getTodayCourseNum() {
        int result = 0;
        for (EventItem ei : todaysEvents) {
            if (ei.eventType == TimetableCore.COURSE) {
                result++;
            }
        }
        return result;
    }

    @WorkerThread
    private void updateTaskProgress() {
        if (!timeTableCore.isDataAvailable()) return;
        List<EventItem> events = timeTableCore.getAllEvents();
        for (EventItem ei : events) {
            if (!ei.hasPassed(timeTableCore.getNow()) || TextUtils.isEmpty(ei.tag4) || ei.tag4 != null && ei.tag4.equals("null"))
                continue;
            Task t = timeTableCore.getTaskWithUUID(ei.tag4);
            if (t != null) {
                if (t.getEvent_map().get(ei.getUuid() + ":::" + ei.week) != null && !t.getEvent_map().get(ei.getUuid() + ":::" + ei.week)) {
                    t.putEventMap(ei.getUuid() + ":::" + ei.week, true);
                    float newProgress = (float) (100 * ((float) t.getProgress() / 100.0 * t.getLength() + ei.getDuration()) / t.getLength());
                    t.updateProgress((int) newProgress);
                    if (newProgress >= 100f) timeTableCore.setFinishTask(t, true);
                }
            }
        }
        List<Task> taks = timeTableCore.getUnfinishedTasks();
        for (Task t : taks) {
            if (!t.has_deadline) continue;
            Calendar end = timeTableCore.getCurrentCurriculum().getDateAt(t.tW, t.tDOW, t.eTime);
            if (end.before(timeTableCore.getNow())) timeTableCore.setFinishTask(t, true);
        }

    }

    @WorkerThread
    private void refreshTodaysEvents() {
        if (!timeTableCore.isDataAvailable()) return;
        int DOW = timeTableCore.getNow().get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ? 7 : timeTableCore.getNow().get(Calendar.DAY_OF_WEEK) - 1;
        todaysEvents.clear();
        timeTableCore.syncTimeFlags();
        todaysEvents.addAll(timeTableCore.getOneDayEvents(timeTableCore.getThisWeekOfTerm(), DOW));
        try {
            Collections.sort(todaysEvents, new Comparator<EventItem>() {
                @Override
                public int compare(EventItem o1, EventItem o2) {
                    return o1.getStartTime().compareTo(o2.getStartTime());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        } catch (Exception e) {
            e.printStackTrace();
//            nowEvent = null;
//            nextEvent = null;
            return;
        }
        try {
            boolean next_on = false;
            boolean autoMute = defaultSP.getBoolean("auto_mute", false);
            boolean eventNotify = defaultSP.getBoolean("event_notify_enable", true);
            if (nowEvent != null) {
                nowProgress = ((float) new HTime(timeTableCore.getNow()).getDuration(nowEvent.startTime)) / ((float) nowEvent.endTime.getDuration(nowEvent.startTime));
                if (autoMute) {
                    String x = defaultSP.getString("mute_course", null);
                    if (nowEvent.eventType == TimetableCore.COURSE && (x == null || nowEvent.equalsEvent(x))) {
                        startMute();
                        defaultSP.edit().putString("mute_course", nowEvent.getEventsIdStr()).apply();
                        sendNotification(getString(R.string.mute_on), getString(R.string.HITSZ_study_assistant));
                    }
                }

            }
            if (nextEvent != null && eventNotify && nextEvent.startTime.getDuration(nowTime) <= 15) {
                String x = defaultSP.getString("event_notify_course", null);
                if (x == null || nextEvent.equalsEvent(x)) {
                    defaultSP.edit().putString("event_notify_course", nextEvent.getEventsIdStr()).apply();
                    sendNotification_Alarm(nextEvent.mainName, getString(R.string.event_notify_soon));
                }
            }
            if (nextEvent != null && nextEvent.eventType == TimetableCore.COURSE) {
                if (autoMute) {
                    String x = defaultSP.getString("mute_course", null);
                    if (x == null || nextEvent.equalsEvent(x)) {
                        if (nextEvent.startTime.getDuration(nowTime) <= defaultSP.getInt("auto_mute_before", 15)) {
                            startMute();
                            defaultSP.edit().putString("mute_course", nextEvent.getEventsIdStr()).apply();
                            sendNotification(getString(R.string.mute_on), getString(R.string.HITSZ_study_assistant));
                            next_on = true;
                        }
                    }
                }

            }

            if ((nowEvent == null && !next_on) || (nowEvent != null && nowEvent.eventType != TimetableCore.COURSE)) {
                if (autoMute && defaultSP.getBoolean("auto_mute_after", true)) {
                    String mute = defaultSP.getString("mute_course", null);
                    //  Log.e("mute",mute);
                    if (mute != null) {
                        boolean has = false;
                        for (EventItem ei : todaysEvents) {
                            if (ei.eventType != TimetableCore.COURSE) continue;
                            if (ei.getEventsIdStr().equals(mute)) {
                                has = true;
                                if (ei.endTime.before(nowTime)) {
                                    finishMute();
                                    sendNotification(getString(R.string.mute_off), getString(R.string.HITSZ_study_assistant));
                                    defaultSP.edit().putString("mute_course", null).apply();
                                    break;
                                }

                            }
                        }
                        if (!has) {
                            finishMute();
                            sendNotification(getString(R.string.mute_off), getString(R.string.HITSZ_study_assistant));
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

    private void sendNotification() {

        String title = "";
        String content = "";
        int IntentTerminal = 0;
        boolean current = false;
        if (CurrentUser == null || !timeTableCore.isDataAvailable()) {
            title = getString(R.string.guide_1p);
            content = getString(R.string.notifi_import_first);
            if (CurrentUser == null) IntentTerminal = 1;
            else if (TextUtils.isEmpty(CurrentUser.getStudentnumber())) IntentTerminal = 2;
            else if (!timeTableCore.isDataAvailable()) IntentTerminal = 3;
        } else if (todaysEvents.size() == 0) {
            title = getString(R.string.timeline_head_free_title);
            content = getString(R.string.timeline_head_free_subtitle);
            IntentTerminal = 0;
        } else if (nowEvent == null) {
            if (new HTime(timeTableCore.getNow()).compareTo(new HTime(23, 0)) > 0 || new HTime(timeTableCore.getNow()).compareTo(new HTime(5, 0)) < 0) {
                title = getString(R.string.timeline_head_goodnight_title);
                content = getString(R.string.timeline_head_goodnight_subtitle);
                IntentTerminal = 0;
            } else if (nextEvent == null) {
                title = getString(R.string.timeline_head_finish_title);
                content = getString(R.string.timeline_head_finish_subtitle);
                IntentTerminal = 0;
            } else {
//                String text1 = String.format(
//                        getString(R.string.time_format_1),
//                        nextEvent.startTime.getDuration(new HTime(timeTableCore.getNow())) / 60,
//                        nextEvent.startTime.getDuration(new HTime(timeTableCore.getNow())) % 60);
//                String text2 = String.format(
//                        getString(R.string.time_format_2),
//                        nextEvent.startTime.getDuration(new HTime(timeTableCore.getNow()))
//                );
                String timeText = EventsUtils.itWillStartIn(timeTableCore.getNow(), nextEvent, false);
//                nextEvent.startTime.getDuration(new HTime(timeTableCore.getNow())) >= 60 ? text1
//                        : text2;
                title = timeText + getString(R.string.timeline_counting_middle);
                content = nextEvent.mainName;
                IntentTerminal = 0;
                //message = "距离下一个事件："+  +
            }
        } else if (nowEvent.getEventType() != TimetableCore.DDL) {
            current = true;
            content = getString(R.string.timeline_head_ongoing_subtitle) + " " + df.format(nowProgress);
            title = nowEvent.mainName;
            IntentTerminal = 0;
        }

        Intent intent;
        switch (IntentTerminal) {
            case 1:
                intent = new Intent(this, ActivityLogin.class);
                break;
            case 2:
                intent = new Intent(this, ActivityUserCenter.class);
                break;
            case 3:
                intent = new Intent(this, ActivityCurriculumManager.class);
                break;
            default:
                intent = new Intent(this, ActivityMain.class);
                break;
        }
        //intent.putExtra("extra","exxxx");
        // pendingIntent = PendingIntent.getBroadcast(HContext, 0,intent, FLAG_UPDATE_CURRENT);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_UPDATE_CURRENT);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Notification.Builder notificationBuilder = new Notification.Builder(this, getString(R.string.app_notification_channel_id));
            notificationBuilder.setSmallIcon(R.drawable.notification_logo_small)
                    .setLargeIcon(Icon.createWithResource(getApplicationContext(), R.drawable.logo))
                    .setOngoing(true);
            notificationBuilder.setContentTitle(title);
            notificationBuilder.setContentText(content);
            notificationBuilder.setContentIntent(pendingIntent);
            if (current) {
                notificationBuilder.setProgress(100, (int) (nowProgress * 100), false);
                // notificationBuilder.setSubText(df.format(nowProgress));
            }
            Notification notification = notificationBuilder.build();
            notification.flags = Notification.FLAG_ONGOING_EVENT;
            notificationManager.notify(R.string.app_notification_channel_id, notification);
        } else {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, getString(R.string.app_notification_channel_id));
            notificationBuilder.setSmallIcon(R.drawable.notification_logo_small)
                    .setOngoing(true)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.logo)); //设置通知的大图标
//                .setAutoCancel(false);//设置通知被点击一次是否自动取消
            if (current) {
                notificationBuilder.setProgress(100, (int) (nowProgress * 100), false);
                // content += "(" + df.format(nowProgress) + ")";
                // notificationBuilder.setSubText("(" + df.format(nowProgress) + ")");
            }
            notificationBuilder.setContentTitle(title);
            notificationBuilder.setContentText(content);
            notificationBuilder.setContentIntent(pendingIntent);
            notificationBuilder.setVibrate(null);
            notificationBuilder.setSound(null);
            notificationBuilder.setLights(0, 0, 0);
            Notification notification = notificationBuilder.build();
            notification.flags = Notification.FLAG_ONGOING_EVENT;
            notificationManager.notify(R.string.app_notification_channel_id, notification);
        }


    }

    private void sendNotification(String title, String content) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Notification.Builder notificationBuilder = new Notification.Builder(this, getString(R.string.app_notification_channel_id));
            notificationBuilder.setSmallIcon(R.drawable.notification_logo_small)
                    .setLargeIcon(Icon.createWithResource(getApplicationContext(), R.drawable.logo))
                    .setAutoCancel(true);

            notificationBuilder.setContentTitle(title);
            notificationBuilder.setContentText(content);

            Notification n = notificationBuilder.build();
            notificationManager.notify(2, n);
        } else {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, getString(R.string.app_notification_channel_id));
            notificationBuilder.setSmallIcon(R.drawable.notification_logo_small)
                    .setAutoCancel(true)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.logo)); //设置通知的大图标
//                .setAutoCancel(false);//设置通知被点击一次是否自动取消

            notificationBuilder.setContentTitle(title);
            notificationBuilder.setContentText(content);

            Notification n = notificationBuilder.build();
            notificationManager.notify(2, n);
        }

    }

    private void sendNotification_Alarm(String title, String content) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Notification.Builder notificationBuilder = new Notification.Builder(this, getString(R.string.app_notification_channel2_id));
            notificationBuilder.setSmallIcon(R.drawable.notification_logo_small)
                    .setLargeIcon(Icon.createWithResource(getApplicationContext(), R.drawable.logo));

            notificationBuilder.setContentTitle(title);
            notificationBuilder.setContentText(content);
            notificationBuilder.setSubText("HITSZ学习助手");
            Intent i = new Intent(this, ActivityMain.class);
            notificationBuilder.setContentIntent(PendingIntent.getActivity(this, 0, i, FLAG_UPDATE_CURRENT));
            notificationBuilder.setFullScreenIntent(PendingIntent.getActivity(this, 0, i, FLAG_UPDATE_CURRENT), true);
            Notification n = notificationBuilder.build();
            notificationManager.notify((int) System.currentTimeMillis(), n);
        } else {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, getString(R.string.app_notification_channel2_id));
            notificationBuilder.setSmallIcon(R.drawable.notification_logo_small)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.logo)); //设置通知的大图标
//                .setAutoCancel(false);//设置通知被点击一次是否自动取消
            notificationBuilder.setContentTitle(title);
            notificationBuilder.setContentText(content);
            notificationBuilder.setSubText("HITSZ学习助手");
            notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
            Intent i = new Intent(this, ActivityMain.class);
            notificationBuilder.setContentIntent(PendingIntent.getActivity(this, 0, i, FLAG_UPDATE_CURRENT));
            notificationBuilder.setFullScreenIntent(PendingIntent.getActivity(this, 0, i, FLAG_UPDATE_CURRENT), true);
            Notification n = notificationBuilder.build();
            notificationManager.notify((int) System.currentTimeMillis(), n);
        }

    }

    private void startMute() {
        if (audioManager != null) {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            audioManager.getStreamVolume(AudioManager.STREAM_RING);
        }
    }

    private void finishMute() {

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            audioManager.getStreamVolume(AudioManager.STREAM_RING);
        }
    }

    class TimeChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.e("监听到时钟变化", "进行刷新");
            Intent i = new Intent(TIMETABLE_CHANGED);
            i.putExtra("type", "time_tick");
            localBroadcastManager.sendBroadcast(i);
        }

    }

    class VolumeChangeReciever extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (nowEvent != null && nowEvent.eventType == TimetableCore.COURSE) {
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

    class SelfRefreshReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("WatcherService收到广播", intent.getAction());
            int switchNotification = intent.getIntExtra("switch_notification", NOTIFICATION_NOT_SPECIFIC);
            //boolean call = intent.getBooleanExtra("call_everyone",false);
            new refreshProgressTask(switchNotification).executeOnExecutor(TPE);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    class refreshProgressTask extends AsyncTask {

        int switchNotification;

        public refreshProgressTask(int s) {
            this.switchNotification = s;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            refreshProgress(switchNotification);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            // if(callEverybody) mBinder.callEveryoneToRefresh();
        }
    }

    public class TimeServiceBinder extends Binder {

        //        public void callEveryoneToRefresh(){
//            Intent i = new Intent();
//            i.setAction(TIMETABLE_CHANGED);
//            sendBroadcast(i);
//        }
        public List<EventItem> getTodaysEvent() {
            return todaysEvents;
        }

        public EventItem getNextEvent() {
            return nextEvent;
        }

        public EventItem getCurrentEvent() {
            return nowEvent;
        }

        public float getNowProgress() {
            return nowProgress;
        }

        public void unRegisterVolumeWatcher() {
            unregisterReceiver(volumeChangeReciever);
        }

        public void registerVolumeWatcher() {
            IntentFilter if2 = new IntentFilter();
            if2.addAction("android.media.VOLUME_CHANGED_ACTION");
            registerReceiver(volumeChangeReciever, if2);

        }

        @WorkerThread
        public void refreshProgress() {

            TimeWatcherService.this.refreshProgress(NOTIFICATION_NOT_SPECIFIC);
        }

        public void refreshNowAndNextEvent() {
            TimeWatcherService.this.refreshNowAndNextEvent();
        }

        public int getTodayCourseNum() {
            try {
                return TimeWatcherService.this.getTodayCourseNum();
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }

        }
    }
}
