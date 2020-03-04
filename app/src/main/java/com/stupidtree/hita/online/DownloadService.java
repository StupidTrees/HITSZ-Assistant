package com.stupidtree.hita.online;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivityMain;

import static com.stupidtree.hita.HITAApplication.TPE;

public class DownloadService extends Service {
    private DownloadBinder mBinder = new DownloadBinder();
    NotificationManager notificationManager;
    ArrayList<DownloadTask> taskQueue;
    int totalTaskNum = 11;

    @Override
    public void onCreate() {
        super.onCreate();
        taskQueue = new ArrayList<>();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(getString(R.string.app_notification_channel_id), getString(R.string.app_notification_channel_name), NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public interface DownLoadListener {

        void onProgress(int progress);

        void onSuccess();

        void onFailed();

        void onPaused();

        void onCanceled();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * 获取系统状态栏信息服务
     */
    private NotificationManager getNotificationManager() {
        return notificationManager;
    }


    private Notification getStartNotification(String name) {
        Notification.Builder builder = new Notification.Builder(this);
        //设置notification信息
        builder.setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentTitle(name + getString(R.string.downloading))
                .setContentText(String.format(getString(R.string.download_file_progress_pattern), taskQueue.size()));
        builder.setProgress(100, 100, true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(getString(R.string.app_notification_channel_id));
        }
        return builder.build();
    }
    private Notification getFailedNotification(String name) {
        Notification.Builder builder = new Notification.Builder(this);
        //设置notification信息
        builder.setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentTitle(getString(R.string.download_fail))
                .setContentText(name);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(getString(R.string.app_notification_channel_id));
        }
        return builder.build();
    }
    private Notification getFinishNotification(String name,String path) {
        Uri selectedUri = Uri.parse(path);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(selectedUri, "resource/folder");
        //  Intent intent = new Intent(this, ActivityMain.class);//上下文
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        Notification.Builder builder = new Notification.Builder(this);
        //设置notification信息
        builder.setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentIntent(pi)
                .setSubText(getString(R.string.download_open_folder))
                .setContentTitle(getString(R.string.download_success))
        .setContentText(name)
        ;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(getString(R.string.app_notification_channel_id));
        }
        return builder.build();
    }

    public class DownloadBinder extends Binder {
        private DownLoadListener listener = new DownLoadListener() {
            @Override
            public void onProgress(int progress) {
//                //设置进度条
//                getNotificationManager().notify(1,
//                        getProgressNotification(taskQueue.get(0).getFileName() + getString(R.string.downloading)
////                                , progress));
            }

            @Override
            public void onSuccess() {
                getNotificationManager().notify(
                        totalTaskNum, getFinishNotification(
                        taskQueue.get(0).getFileName(),
                        taskQueue.get(0).getFolderPath()));
                if (taskQueue.size() > 1) {
                    taskQueue.remove(0);
                    startForeground(1, getStartNotification(taskQueue.get(0).getFileName()));
                    taskQueue.get(0).setListener(listener);
                    taskQueue.get(0).executeOnExecutor(TPE);
                } else if (taskQueue.size() == 1) {
                    //关闭前台服务，并且创建下载成功通知
                    stopForeground(true);
                    taskQueue.remove(0);
                }

            }

            @Override
            public void onFailed() {
                stopForeground(true);
                getNotificationManager().notify(1,getFailedNotification(taskQueue.get(0).getFileName()));
                taskQueue.remove(0);
                if (taskQueue.size() > 0) {
                    taskQueue.get(0).setListener(listener);
                    taskQueue.get(0).executeOnExecutor(TPE);
                }
            }

            @Override
            public void onPaused() {
                //getNotificationManager().notify(1, getProgressNotification(getString(R.string.download_paused), -1));
            }

            @Override
            public void onCanceled() {
                taskQueue.clear();
//                stopForeground(true);
//                getNotificationManager().notify(1, getProgressNotification(getString(R.string.download_canceld), -1));
            }
        };

        public int getTaskNumber() {
            return taskQueue.size();
        }

        /**
         * 开始下载
         */
        public void startDownLoad(DownloadTask downloadTask) {
            for(DownloadTask dt:taskQueue){
                if((dt.getFolderPath()+dt.getFileName()).equals(downloadTask.getFolderPath()+downloadTask.getFileName())){
                    return;
                }
            }
            taskQueue.add(downloadTask);
            totalTaskNum++;
            if (taskQueue.size() == 1) {
                downloadTask.setListener(listener);
                downloadTask.execute();
                //Log.d(TAG, "开始下载的服务");
            }
            startForeground(1, getStartNotification(taskQueue.get(0).getFileName()));
        }

        public void pauseDownLoad() {
            if (taskQueue.size() > 0) {
                taskQueue.get(0).pauseDownload();
            }
        }

        public void cancelDownLoad() {
            if (taskQueue.size() > 0) {
                taskQueue.get(0).cancelDownload();
                String filename = taskQueue.get(0).getFileName();
                String derectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                File file = new File(derectory + filename);
                if (file.exists()) {
                    file.delete();
                }
                getNotificationManager().cancel(1);//关闭1号通知
                stopForeground(true);
            }
            taskQueue.clear();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        stopSelf();
    }
}


