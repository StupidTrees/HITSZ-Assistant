package com.stupidtree.hita.activities;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Bundle;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;
import com.stupidtree.hita.diy.ButtonLoading;
import com.stupidtree.hita.util.UpdateManager;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.beta.UpgradeInfo;
import com.tencent.bugly.beta.download.DownloadTask;
import com.tencent.bugly.beta.upgrade.UpgradeListener;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import cn.bmob.v3.BmobArticle;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;


public class ActivityAboutHITA extends BaseActivity {
ButtonLoading checkUpDate;
Toolbar tb;
TextView version;
ImageView update_image;

TextView update_title,update_message,update_version,update_time,update_size;
Button update_start,update_delete;
ProgressBar update_progress;
LinearLayout updateArea;
WebView webView;

Handler handler;
Runnable runnable;
BroadcastReceiver receiver;

    @Override
    protected void stopTasks() {

    }

    //mReciever reciever;
    //IntentFilter intentFilter;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true,false,false);
        setContentView(R.layout.activity_about_hita);
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
        version.setText(getString(R.string.version)+versionName);
        initReceiver();
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


    void initReceiver(){
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                checkUpDate.setProgress(false);
                if(intent.getAction().equals("com.stupidtree.hita.upgrade_failed"))  Toast.makeText(ActivityAboutHITA.this, R.string.check_for_update_failed, Toast.LENGTH_SHORT).show();
                else if(intent.getAction().equals("com.stupidtree.hita.upgrade_success")) Toast.makeText(ActivityAboutHITA.this, R.string.update_available, Toast.LENGTH_SHORT).show();
                else if(intent.getAction().equals("com.stupidtree.hita.upgrade_no_version"))Toast.makeText(ActivityAboutHITA.this, R.string.already_up_to_date,Toast.LENGTH_SHORT).show();

            }
        };
        IntentFilter iF = new IntentFilter();
        iF.addAction("com.stupidtree.hita.upgrade_failed");
        iF.addAction("com.stupidtree.hita.upgrade_success");
        iF.addAction("com.stupidtree.hita.upgrade_no_version");
        registerReceiver(receiver,iF);
    }
    void initToolbar(){
        tb = findViewById(R.id.toolbar);
        tb.setTitle(getString(R.string.label_activity_about_hita));
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
        webView.setBackgroundColor(0);
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
                Beta.startDownload();
//                updateBtn(task);
            }
        });
        checkUpDate.setOnButtonLoadingListener(new ButtonLoading.OnButtonLoadingListener() {
            @Override
            public void onClick() {
                checkUpDate.setProgress(true);
               // Toast.makeText(ActivityAboutHITA.this, getString(R.string.checking_for_update),Toast.LENGTH_SHORT).show();
                UpdateManager.checkUpdate(ActivityAboutHITA.this);
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onFinish() {

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

    @SuppressLint("SetTextI18n")
    void refreshViews(){

        if(Beta.getUpgradeInfo()!=null){
            Glide.with(this).load(Beta.getUpgradeInfo().imageUrl).into(update_image);
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
            webView.setVisibility(View.GONE);
            checkUpDate.setVisibility(View.GONE);
            update_title.setText(Beta.getUpgradeInfo().title);

            update_version.setText(getString(R.string.update_version)+ Beta.getUpgradeInfo().versionName);
            update_size.setText(getString(R.string.update_size)+ new DecimalFormat(".##").format((float)Beta.getUpgradeInfo().fileSize/(1024*1024))+"MB");
            update_time.setText(getString(R.string.update_release_time)+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Beta.getUpgradeInfo().publishTime));
            update_message.setText(getString(R.string.update_log)+Beta.getUpgradeInfo().newFeature);

            switch (Beta.getStrategyTask().getStatus()) {
                case DownloadTask.INIT:
                case DownloadTask.DELETED:
                case DownloadTask.FAILED: {
                    btState = getString(R.string.start_download);
                }
                break;
                case DownloadTask.COMPLETE: {
                    btState = getString(R.string.install);
                }
                break;
                case DownloadTask.DOWNLOADING: {
                  btState = getString(R.string.pause);
                }
                break;
                case DownloadTask.PAUSED: {
                    btState = getString(R.string.continue_download);
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
            webView.setVisibility(View.VISIBLE);
            checkUpDate.setVisibility(View.VISIBLE);
        }


    }

}
