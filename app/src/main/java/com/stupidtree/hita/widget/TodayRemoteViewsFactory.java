//package com.stupidtree.hita.widget;
//
//import android.appwidget.AppWidgetManager;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.os.AsyncTask;
//import android.os.Process;
//import android.util.Log;
//import android.widget.RemoteViews;
//import android.widget.RemoteViewsService;
//
//import androidx.annotation.WorkerThread;
//import androidx.localbroadcastmanager.content.LocalBroadcastManager;
//
//import com.stupidtree.hita.R;
//import com.stupidtree.hita.timetable.packable.EventItem;
//
//
//public class TodayRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
//    private Context mContext;
//    private int mAppWidgetId;
//
//
//    /**
//     * 构造GridRemoteViewsFactory
//     */
//    TodayRemoteViewsFactory(Context context, Intent intent) {
//        mContext = context;
//
//        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
//                AppWidgetManager.INVALID_APPWIDGET_ID);
//    }
//
//    @Override
//    public RemoteViews getViewAt(int position) {
//        //  HashMap<String, Object> map;
//
//        // 获取 item_widget_device.xml 对应的RemoteViews
//        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.dynamic_widget_today_item);
//
//        // 设置 第position位的“视图”的数据
//        EventItem ei = mBeans.get(position);
//        //  rv.setImageViewResource(R.id.iv_lock, ((Integer) map.get(IMAGE_ITEM)).intValue());
//        rv.setTextViewText(R.id.time,ei.getStartTime().tellTime()+"-"+ei.getEndTime().tellTime());
//        rv.setTextViewText(R.id.name,ei.getMainName());
//        // 设置 第position位的“视图”对应的响应事件
////        Intent fillInIntent = new Intent();
////        fillInIntent.putExtra("Type", 0);
////        fillInIntent.putExtra(TodayWidget.COLLECTION_VIEW_EXTRA, position);
////        rv.setOnClickFillInIntent(R.id.rl_widget_device, fillInIntent);
////
//
////        Intent lockIntent = new Intent();
////        lockIntent.putExtra(ListWidgetProvider.COLLECTION_VIEW_EXTRA, position);
////        lockIntent.putExtra("Type", 1);
////        rv.setOnClickFillInIntent(R.id.iv_lock, lockIntent);
////
////        Intent unlockIntent = new Intent();
////        unlockIntent.putExtra("Type", 2);
////        unlockIntent.putExtra(ListWidgetProvider.COLLECTION_VIEW_EXTRA, position);
////        rv.setOnClickFillInIntent(R.id.iv_unlock, unlockIntent);
//
//        return rv;
//    }
//
//
//    public static void Refresh(){
//
//    }
//
//
//    @Override
//    public void onCreate() {
//
//
//    }
//
//    @Override
//    public int getCount() {
//        // 返回“集合视图”中的数据的总数
//        return mBeans.size();
//    }
//
//    @Override
//    public long getItemId(int position) {
//        // 返回当前项在“集合视图”中的位置
//        return position;
//    }
//
//    @Override
//    public RemoteViews getLoadingView() {
//        return null;
//    }
//
//    @Override
//    public int getViewTypeCount() {
//        // 只有一类 ListView
//        return 1;
//    }
//
//    @Override
//    public boolean hasStableIds() {
//        return true;
//    }
//
//    @Override
//    public void onDataSetChanged() {
//
//    }
//
//    @Override
//    public void onDestroy() {
//        mBeans.clear();
//    }
//
//}
