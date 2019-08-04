package com.stupidtree.hita.activities;

import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import com.stupidtree.hita.BaseActivity;
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
        if(pageTask!=null&&!pageTask.isCancelled())pageTask.cancel(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true,true,false);
         setContentView(R.layout.activity_news_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        link = "http://www.hitsz.edu.cn"+getIntent().getStringExtra("link");
        title = findViewById(R.id.detail_title);
        time = findViewById(R.id.detail_time);
        wv = findViewById(R.id.webview);
        title.setText(getIntent().getStringExtra("title"));
        //支持javascript
        wv.getSettings().setJavaScriptEnabled(true);
        //支持自适应
        wv.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        wv.getSettings().setLoadWithOverviewMode(true);


    }

    @Override
    protected void onResume() {
        super.onResume();
        if(pageTask!=null&&!pageTask.isCancelled())pageTask.cancel(true);
        pageTask = new LoadTask();
        pageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

            } catch (IOException e) {
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
                    d.removeClass("download_file");
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
