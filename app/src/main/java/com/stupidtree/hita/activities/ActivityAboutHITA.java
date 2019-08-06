package com.stupidtree.hita.activities;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Bundle;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;
import com.stupidtree.hita.util.UpdateManager;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.beta.download.DownloadTask;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import cn.bmob.v3.BmobArticle;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;


public class ActivityAboutHITA extends BaseActivity {
Button checkUpDate;
Toolbar tb;
TextView version;
ImageView update_image;

TextView update_title,update_message,update_version,update_time,update_size;
Button update_start,update_delete;
ProgressBar update_progress;
NestedScrollView updateArea;
WebView webView;

Handler handler;
Runnable runnable;

boolean imageLoaded = false;

    @Override
    protected void stopTasks() {

    }

    //mReciever reciever;
    //IntentFilter intentFilter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true,true,false);
        setContentView(R.layout.activity_about_hita);
        //reciever = new mReciever();
        initToolbar();
        initViews();
        PackageInfo packageInfo = null;

        try {
            packageInfo = getPackageManager()
                    .getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        //获取APP版本versionName
        String versionName = packageInfo.versionName;
        //获取APP版本versionCode

        version.setText("版本："+versionName);

       // intentFilter = new IntentFilter();
        //intentFilter.addAction("android.intent.updatebroadcast");




        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                refreshViews();
                handler.postDelayed(this,500);
            }
        };
    }
    void initToolbar(){
        tb = findViewById(R.id.toolbar);
        tb.setTitle("关于");
        setSupportActionBar(tb);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });



    }
    void initViews(){
        webView = findViewById(R.id.webview);
        checkUpDate = findViewById(R.id.update_check);
        update_image = findViewById(R.id.update_image);
        version = findViewById(R.id.version);
        update_message = findViewById(R.id.update_message);
        update_title = findViewById(R.id.update_title);
        update_start = findViewById(R.id.update_start);
        update_progress = findViewById(R.id.update_progress);
        updateArea = findViewById(R.id.update_area);
        update_size = findViewById(R.id.update_size);
        update_delete = findViewById(R.id.update_delete);
        update_time = findViewById(R.id.update_time);
        update_version = findViewById(R.id.update_version);
        update_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadTask task = Beta.startDownload();
//                updateBtn(task);
            }
        });
        checkUpDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ActivityAboutHITA.this,"检查更新...",Toast.LENGTH_SHORT).show();
                UpdateManager.checkUpdate(ActivityAboutHITA.this);
            }
        });
        update_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Beta.getStrategyTask().delete(true);
            }
        });
        BmobQuery<BmobArticle> bq = new BmobQuery<>();
        bq.addWhereEqualTo("objectId","jB7Q8881");
        bq.findObjects(new FindListener<BmobArticle>() {
            @Override
            public void done(List<BmobArticle> list, BmobException e) {
                if(e==null&&list!=null&&list.size()>0){
                    webView.setVisibility(View.VISIBLE);
                    webView.loadUrl(list.get(0).getUrl());
                }else webView.setVisibility(View.GONE);
            }
        });
         }
    public void updateBtn(DownloadTask task) {

        /*根据下载任务状态设置按钮*/
        switch (task.getStatus()) {
            case DownloadTask.INIT:
            case DownloadTask.DELETED:
            case DownloadTask.FAILED: {
                update_start.setText("开始下载");
            }
            break;
            case DownloadTask.COMPLETE: {
                update_start.setText("安装");
            }
            break;
            case DownloadTask.DOWNLOADING: {
                update_start.setText("暂停");
            }
            break;
            case DownloadTask.PAUSED: {
                update_start.setText("继续下载");
            }
            break;
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(runnable);
    }

    void refreshViews(){
        if(Beta.getUpgradeInfo()!=null){
            //Beta.getUpgradeInfo().
            Log.e("!!!",Beta.getUpgradeInfo().imageUrl+",,,");

            Glide.with(this).load(Beta.getUpgradeInfo().imageUrl).into(update_image);
//            if(!imageLoaded&&Beta.getUpgradeInfo().imageUrl!=null&&(!Beta.getUpgradeInfo().imageUrl.isEmpty())){
//
//                imageLoaded = true;
//            }

            String btState = "";
            String btProgress;
            if(Beta.getStrategyTask().getStatus()==DownloadTask.DOWNLOADING||Beta.getStrategyTask().getStatus()==DownloadTask.PAUSED){
                update_progress.setVisibility(View.VISIBLE);
                update_progress.setProgress((int)( (float)Beta.getStrategyTask().getSavedLength()/(float)Beta.getStrategyTask().getTotalLength()*100));
                btProgress="("+(int)( (float)Beta.getStrategyTask().getSavedLength()/(float)Beta.getStrategyTask().getTotalLength()*100)+"%)";
            }else{
                update_progress.setVisibility(View.GONE);
                btProgress = "";
            }
            updateArea.setVisibility(View.VISIBLE);
            checkUpDate.setVisibility(View.GONE);
            update_title.setText(Beta.getUpgradeInfo().title);

            update_version.setText("版本："+ Beta.getUpgradeInfo().versionName);
            update_size.setText("文件大小："+ new DecimalFormat(".##").format((float)Beta.getUpgradeInfo().fileSize/(1024*1024))+"MB");
            update_time.setText("发布时间："+new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss").format(Beta.getUpgradeInfo().publishTime));
            update_message.setText("更新日志："+Beta.getUpgradeInfo().newFeature);

            switch (Beta.getStrategyTask().getStatus()) {
                case DownloadTask.INIT:
                case DownloadTask.DELETED:
                case DownloadTask.FAILED: {
                    btState = "开始下载";
                }
                break;
                case DownloadTask.COMPLETE: {
                    btState = "安装";
                }
                break;
                case DownloadTask.DOWNLOADING: {
                  btState = "暂停";
                }
                break;
                case DownloadTask.PAUSED: {
                    btState = "继续下载";
                }
                break;
            }
            if(Beta.getStrategyTask().getStatus()==DownloadTask.COMPLETE){
                update_delete.setVisibility(View.VISIBLE);
            }else{
                update_delete.setVisibility(View.GONE);
            }
            update_start.setText(btState+btProgress);

        }else{
            updateArea.setVisibility(View.GONE);
            checkUpDate.setVisibility(View.VISIBLE);
        }


    }


//    @Override
//    protected void onResume() {
//        super.onResume();
//        localBroadcastManager.registerReceiver(reciever,intentFilter);
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        localBroadcastManager.unregisterReceiver(reciever);
//    }
//
//
//    class mReciever extends BroadcastReceiver{
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Log.e("!,","recieved");
//            refreshViews(intent.getFloatExtra("downloadprogress",0));
//        }
//    }

}
