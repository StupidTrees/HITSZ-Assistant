//package com.stupidtree.hita.widget;
//
//import android.appwidget.AppWidgetManager;
//import android.appwidget.AppWidgetProvider;
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.Intent;
//import android.content.ServiceConnection;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.os.IBinder;
//import android.os.Process;
//import android.util.Log;
//import android.widget.RemoteViews;
//
//import androidx.localbroadcastmanager.content.LocalBroadcastManager;
//
//import com.stupidtree.hita.R;
//import com.stupidtree.hita.timetable.TimeWatcherService;
//import com.stupidtree.hita.timetable.TimetableCore;
//import com.stupidtree.hita.timetable.packable.EventItem;
//import com.stupidtree.hita.widget.TodayListWidgetService;
//import com.stupidtree.hita.widget.TodayRemoteViewsFactory;
//
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.List;
//
//
//
///**
// * Implementation of App Widget functionality.
// */
//public class TodayWidget extends AppWidgetProvider {
//
//    TimetableCore TimetableCore.getInstance(HContext);
//    public static TimeWatcherService.TimeServiceBinder binder;
//
//    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
//                                int appWidgetId) {
//
//        // Construct the RemoteViews object
//        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.today_widget);
//        // Instruct the widget manager to update the widget
//        appWidgetManager.updateAppWidget(appWidgetId, views);
//    }
//
//    @Override
//    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
//
//        for (int appWidgetId : appWidgetIds) {
//            // 获取AppWidget对应的视图
//            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.today_widget);
//            // 设置 “ListView” 的adapter。
//            // (01) intent: 对应启动 ListWidgetService(RemoteViewsService) 的intent
//            // (02) setRemoteAdapter: 设置 gridview的适配器
//            //    通过setRemoteAdapter将ListView和ListWidgetService关联起来，
//            //    以达到通过 ListWidgetService 更新 ListView的目的
//
//
//            Intent serviceIntent = new Intent(context, TodayListWidgetService.class);
//            remoteViews.setRemoteAdapter(R.id.list, serviceIntent);
//            // 设置响应 “ListView” 的intent模板
//            // 说明：“集合控件(如GridView、ListView、StackView等)”中包含很多子元素，如GridView包含很多格子。
//            //     它们不能像普通的按钮一样通过 setOnClickPendingIntent 设置点击事件，必须先通过两步。
//            //        (01) 通过 setPendingIntentTemplate 设置 “intent模板”，这是比不可少的！
//            //        (02) 然后在处理该“集合控件”的RemoteViewsFactory类的getViewAt()接口中 通过 setOnClickFillInIntent 设置“集合控件的某一项的数据”
////            Intent gridIntent = new Intent();
////
////            gridIntent.setAction(COLLECTION_VIEW_ACTION);
////            gridIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
////            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, gridIntent, PendingIntent.FLAG_UPDATE_CURRENT);
////            // 设置intent模板
////            remoteViews.setPendingIntentTemplate(R.id.lv_device, pendingIntent);
//            // 调用集合管理器对集合进行更新
//
//
//            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
//        }
//        super.onUpdate(context, appWidgetManager, appWidgetIds);
//    }
//
//    @Override
//    public void onEnabled(Context context) {
//        Intent i = new Intent(context,TimeWatcherService.class);
//        ServiceConnection connection = new ServiceConnection() {
//            @Override
//            public void onServiceConnected(ComponentName name, IBinder service) {
//                binder = (TimeWatcherService.TimeServiceBinder) service;
//            }
//
//            @Override
//            public void onServiceDisconnected(ComponentName name) {
//
//            }
//        };
//        context.bindService(i,connection,Context.BIND_AUTO_CREATE);
//        // Enter relevant functionality for when the first widget is created
//    }
//
//    @Override
//    public void onDisabled(Context context) {
//        // Enter relevant functionality for when the last widget is disabled
//    }
//
//
//}
//
