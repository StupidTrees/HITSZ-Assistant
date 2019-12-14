package com.stupidtree.hita.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;



public class ActivityNewsDetail extends BaseActivity {
    String link;
    TextView title,time;
    WebView wv;
    LoadTask pageTask;
    @Override
    protected void stopTasks() {
        if(pageTask!=null&&pageTask.getStatus()!=AsyncTask.Status.FINISHED)pageTask.cancel(true);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true,true,false);
         setContentView(R.layout.activity_news_detail);

        link = "http://www.hitsz.edu.cn"+getIntent().getStringExtra("link");
        title = findViewById(R.id.detail_title);
        time = findViewById(R.id.detail_time);
        wv = findViewById(R.id.webview);
        //wv.setBackgroundColor(0);

        title.setText(getIntent().getStringExtra("title"));
        //支持javascript
        wv.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        wv.getSettings().setJavaScriptEnabled(true);
        //支持自适应
        wv.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        wv.getSettings().setLoadWithOverviewMode(true);

        initToolbar();

    }

    void initToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.inflateMenu(R.menu.toolbar_news_detail);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId()==R.id.action_website){
                    Uri uri = Uri.parse(link);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    ActivityNewsDetail.this.startActivity(intent);
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_news_detail,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(pageTask!=null&&pageTask.getStatus()!=AsyncTask.Status.FINISHED)pageTask.cancel(true);
        pageTask = new LoadTask();
        pageTask.executeOnExecutor(HITAApplication.TPE);
    }

    class LoadTask extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                Document d = Jsoup.connect(link).get();
                String time = d.select("[class=tip]").get(0).text()+"浏览量";
                String text = d.select("[class=detail]").select("[class=edittext]").toString();
                //System.out.println(text);
                Map<String,String> m = new HashMap<>();
                m.put("text",text);
                m.put("time",time);
                return m;

            } catch (Exception e) {
                return null;
            }

        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            try {
                Map<String,String> m =(Map)o;
                if(o!=null){
                    time.setText(Html.fromHtml(m.get("time")));
                    Document d = Jsoup.parse(m.get("text"));
                   // d.removeClass("download_file");
                    String js = "<script type=\"text/javascript\">"+
                            "var imgs = document.getElementsByTagName('img');" + // 找到img标签
                            "for(var i = 0; i<imgs.length; i++){" +  // 逐个改变
                            "imgs[i].style.width = '100%';" +  // 宽度改为100%
                            "imgs[i].style.height = 'auto';" +
                            "}" +
                            "</script>";
                    wv.loadData(d.toString()+js,"text/html; charset=UTF-8", null);

                   // wv.loadDataWithBaseURL(null,m.get("text"),"text/html", "UTF-8", null);
    //                title.post(new Runnable() {
    //                   @Override
    //                    public void run() {
    //                        MaterialCircleAnimator.animShow(title,500);
    //                    }
    //                });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
