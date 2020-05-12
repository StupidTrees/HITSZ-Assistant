package com.stupidtree.hita.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.fragments.BaseOperationTask;
import com.stupidtree.hita.util.ActivityUtils;
import com.stupidtree.hita.util.JsonUtils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.stupidtree.hita.HITAApplication.HContext;


public class ActivityNewsDetail extends BaseActivity implements BaseOperationTask.OperationListener {
    private static final int MODE_HITSZ_NEWS = 929;
    private static final int MODE_ZSW_NEWS = 482;
    String link;
    TextView title, time;
    WebView wv;
    int mode;
    List<String> imagesOnPage;



    public static String[] returnImageUrlsFromHtml(String htmlCode) {
        List<String> imageSrcList = new ArrayList<String>();
        Pattern p = Pattern.compile("<img\\b[^>]*\\bsrc\\b\\s*=\\s*('|\")?([^'\"\n\r\f>]+(\\.jpg|\\.bmp|\\.eps|\\.gif|\\.mif|\\.miff|\\.png|\\.tif|\\.tiff|\\.svg|\\.wmf|\\.jpe|\\.jpeg|\\.dib|\\.ico|\\.tga|\\.cut|\\.pic|\\b)\\b)[^>]*>", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(htmlCode);
        String quote = null;
        String src = null;
        while (m.find()) {
            quote = m.group(1);
            src = (quote == null || quote.trim().length() == 0) ? m.group(2).split("//s+")[0] : m.group(2);
            imageSrcList.add(src);
        }
        if (imageSrcList.size() == 0) {
            return null;
        }
        return imageSrcList.toArray(new String[imageSrcList.size()]);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true, true, false);
        setContentView(R.layout.activity_news_detail);
        initToolbar();
        String mS = getIntent().getStringExtra("mode");
        if (mS.equals("hitsz_news")) mode = MODE_HITSZ_NEWS;
        else if (mS.equals("zsw_news")) mode = MODE_ZSW_NEWS;


        initLink();
        initViews();


    }

    @SuppressLint("SetJavaScriptEnabled")
    void initViews() {
        imagesOnPage = new ArrayList<>();
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
                ActivityUtils.showOneImage(getThis(), url);
                //   ActivityUtils.startPhotoDetailActivity(ActivityNewsDetail.this,url);
            }
        }, "jsCallJavaObj");
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                setWebImageClick(view);
            }
        });

    }



    @Override
    public void onOperationStart(String id, Boolean[] params) {

    }

    @Override
    public void onOperationDone(String id, BaseOperationTask task, Boolean[] params, Object o) {
        if (o != null) {
            Map m = (Map) o;
            Document d = new Document("");
            if (m.get("time") != null) time.setText(Html.fromHtml(String.valueOf(m.get("time"))));
            if (m.get("text") != null) d = Jsoup.parse(String.valueOf(m.get("text")));
            // d.removeClass("download_file");
            String js = "<script type=\"text/javascript\">" +
                    "var imgs = document.getElementsByTagName('img');" + // 找到img标签
                    "for(var i = 0; i<imgs.length; i++){" +  // 逐个改变
                    "imgs[i].style.width = '100%';" +  // 宽度改为100%
                    "imgs[i].style.height = 'auto';" +
                    "}" +
                    "</script>";
            if (mode == MODE_HITSZ_NEWS) {
                wv.loadUrl("http://www.hitsz.edu.cn" + getIntent().getStringExtra("link"));

            } else {
                wv.loadData(d.toString() + js, "text/html; charset=UTF-8", null);
            }
            //
        }
    }

    /**
     * Js調用Java接口
     */
    private interface JsCallJavaObj {
        void showBigImg(String url);
    }

    void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.inflateMenu(R.menu.toolbar_news_detail);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
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

    void initLink() {
        if (mode == MODE_HITSZ_NEWS)
            link = "http://www.hitsz.edu.cn" + getIntent().getStringExtra("link");
        else if (mode == MODE_ZSW_NEWS)
            link = "http://zsb.hitsz.edu.cn/zs_common/bkzn/zswz/zsjzxq?id=" + getIntent().getStringExtra("link");
    }

    /**
     * 設置網頁中圖片的點擊事件
     *
     * @param view
     */
    private void setWebImageClick(WebView view) {
        String jsCode = "javascript:(function(){" +
                "var imgs=document.getElementsByTagName(\"img\");" +
                "for(var i=0;i<imgs.length;i++){" +
                "imgs[i].onclick=function(){" +
                "window.jsCallJavaObj.showBigImg(this.src);" +
                "}}})()";
        view.loadUrl(jsCode);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_news_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new LoadTask(this, mode, getIntent().getStringExtra("link")).executeOnExecutor(HITAApplication.TPE);
    }

    static class LoadTask extends BaseOperationTask<Object> {

        int mode;
        String link;

        LoadTask(OperationListener listRefreshedListener, int mode, String link) {
            super(listRefreshedListener);
            this.mode = mode;
            this.link = link;
        }

        @Override
        protected Object doInBackground(OperationListener<Object> listRefreshedListener, Boolean... booleans) {
            try {
                Map<String, String> m = new HashMap<>();
                HashMap<String, String> cookies = new HashMap<>();
                if (mode == MODE_HITSZ_NEWS) {
                    Document d = Jsoup.connect("http://www.hitsz.edu.cn" + link).get();
                    try {
                        String text = d.select("[class=detail]").select("[class=edittext]").toString();
                        m.put("text", text);
                        String time = d.select("[class=tip]").get(0).text() + "浏览量";
                        m.put("time", time);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return m;
                } else if (mode == MODE_ZSW_NEWS) {
                    Connection.Response r = Jsoup.connect("http://zsb.hitsz.edu.cn/zs_common/bkzn/getJbxx")
                            .cookies(cookies)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.87 Safari/537.36")
                            .ignoreContentType(true)
                            .ignoreHttpErrors(true)
                            .cookies(cookies)
                            .method(Connection.Method.POST)
                            .data("info", "{\"id\":\"" + link + "\",\"xxlm\":\"\"}")
                            .execute();
                    String json = r.body();
                    JsonObject jo = new JsonParser().parse(json).getAsJsonObject();
                    JsonObject data = jo.get("module").getAsJsonArray().get(0).getAsJsonObject().get("data").getAsJsonObject();
                    String date = JsonUtils.getStringInfo(data, "fbsj");
                    String views = JsonUtils.getStringInfo(data, "llcs");
                    // Document d2 = Jsoup.parse(StringEscapeUtils.unescapeJava(d.toString()));
                    String text = JsonUtils.getStringInfo(data, "pcdxxnr");
                    m.put("text", text);
                    m.put("time", HContext.getString(R.string.posted_in) + date + " " + HContext.getString(R.string.total_views) + views);
                    return m;
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }


    }
}
