package com.stupidtree.hita.activities;

import android.annotation.SuppressLint;
import android.app.SharedElementCallback;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.util.ActivityUtils;
import com.stupidtree.hita.util.JsonUtils;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ActivityNewsDetail extends BaseActivity {
    private static final int MODE_HITSZ_NEWS = 929;
    private static final int MODE_ZSW_NEWS = 482;
    String link;
    TextView title, time;
    WebView wv;
    LoadTask pageTask;
    int mode;


    @Override
    protected void stopTasks() {
        if (pageTask != null && pageTask.getStatus() != AsyncTask.Status.FINISHED)
            pageTask.cancel(true);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true, true, false);
        setContentView(R.layout.activity_news_detail);
        String mS = getIntent().getStringExtra("mode");
        if (mS.equals("hitsz_news")) mode = MODE_HITSZ_NEWS;
        else if (mS.equals("zsw_news")) mode = MODE_ZSW_NEWS;


        initLink();
        initViews();
        initToolbar();

    }

    @SuppressLint("SetJavaScriptEnabled")
    void initViews(){
        title = findViewById(R.id.detail_title);
        time = findViewById(R.id.detail_time);
        wv = findViewById(R.id.webview);
        //wv.setBackgroundColor(0);
        title.setText(getIntent().getStringExtra("title"));
        //支持javascript
        wv.setWebChromeClient(new WebChromeClient());
        wv.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        wv.getSettings().setJavaScriptEnabled(true);
        //支持自适应
        wv.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        wv.getSettings().setLoadWithOverviewMode(true);
//java回调js代码，不要忘了@JavascriptInterface这个注解，不然点击事件不起作用
        wv.addJavascriptInterface(new JsCallJavaObj() {
            @JavascriptInterface
            @Override
            public void showBigImg(String url) {
                //String url =
                ActivityUtils.startPhotoDetailActivity(ActivityNewsDetail.this,url);
            }
        },"jsCallJavaObj");
        wv.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                setWebImageClick(view);
            }
        });

    }

    /**
     * 設置網頁中圖片的點擊事件
     * @param view
     */
    private  void setWebImageClick(WebView view) {
        String jsCode="javascript:(function(){" +
                "var imgs=document.getElementsByTagName(\"img\");" +
                "for(var i=0;i<imgs.length;i++){" +
                "imgs[i].onclick=function(){" +
                "window.jsCallJavaObj.showBigImg(this.src);" +
                "}}})()";
        wv.loadUrl(jsCode);
    }

    /**
     * Js調用Java接口
     */
    private interface JsCallJavaObj{
        void showBigImg(String url);
    }
    void initToolbar() {
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
                if (item.getItemId() == R.id.action_website) {
                    Uri uri = Uri.parse(link);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    ActivityNewsDetail.this.startActivity(intent);
                }
                return false;
            }
        });
    }
    void initLink(){
        if(mode==MODE_HITSZ_NEWS) link = "http://www.hitsz.edu.cn"+ getIntent().getStringExtra("link");
        else if(mode == MODE_ZSW_NEWS) link="http://zsb.hitsz.edu.cn/zs_common/bkzn/zswz/zsjzxq?id="+ getIntent().getStringExtra("link");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_news_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pageTask != null && pageTask.getStatus() != AsyncTask.Status.FINISHED)
            pageTask.cancel(true);
        pageTask = new LoadTask();
        pageTask.executeOnExecutor(HITAApplication.TPE);
    }

    class LoadTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                Map<String, String> m = new HashMap<>();
                HashMap<String,String> cookies = new HashMap<>();
                if(mode==MODE_HITSZ_NEWS){
                    Document d = Jsoup.connect("http://www.hitsz.edu.cn"+ getIntent().getStringExtra("link")).get();
                    try {
                        String text = d.select("[class=detail]").select("[class=edittext]").toString();
                        m.put("text", text);
                        String time = d.select("[class=tip]").get(0).text() + "浏览量";
                        m.put("time", time);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return m;
                }else if(mode==MODE_ZSW_NEWS){
                    Connection.Response  r = Jsoup.connect("http://zsb.hitsz.edu.cn/zs_common/bkzn/getJbxx")
                            .cookies(cookies)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.87 Safari/537.36")
                            .ignoreContentType(true)
                            .ignoreHttpErrors(true)
                            .cookies(cookies)
                            .method(Connection.Method.POST)
                            .data("info","{\"id\":\""+getIntent().getStringExtra("link")+"\",\"xxlm\":\"\"}")
                            .execute();
                    String json =r.body();
                    Log.e("lenght_after",String.valueOf(json.length()));
                   // System.out.print(json.length());
                    //System.out.println(json);
                    JsonObject jo = new JsonParser().parse(json).getAsJsonObject();
                    JsonObject data = jo.get("module").getAsJsonArray().get(0).getAsJsonObject().get("data").getAsJsonObject();
                    String date = JsonUtils.getStringInfo(data,"fbsj");
                    String views = JsonUtils.getStringInfo(data,"llcs");
                   // Document d2 = Jsoup.parse(StringEscapeUtils.unescapeJava(d.toString()));
                    String text = JsonUtils.getStringInfo(data,"pcdxxnr");
                    m.put("text",text);
                    m.put("time", getString(R.string.posted_in)+date+" "+getString(R.string.total_views)+views);
                    return m;
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            try {
                Map<String, String> m = (Map) o;
                if (o != null) {
                    Document d = new Document("");
                    if(m.get("time")!=null) time.setText(Html.fromHtml(m.get("time")));
                    if(m.get("text")!=null) d = Jsoup.parse(m.get("text"));
                    // d.removeClass("download_file");
                    String js = "<script type=\"text/javascript\">" +
                            "var imgs = document.getElementsByTagName('img');" + // 找到img标签
                            "for(var i = 0; i<imgs.length; i++){" +  // 逐个改变
                            "imgs[i].style.width = '100%';" +  // 宽度改为100%
                            "imgs[i].style.height = 'auto';" +
                            "}" +
                            "</script>";
                    wv.loadData(d.toString()+js , "text/html; charset=UTF-8", null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
