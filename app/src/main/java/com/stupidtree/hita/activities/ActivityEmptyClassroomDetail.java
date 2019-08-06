package com.stupidtree.hita.activities;

import android.os.AsyncTask;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.os.Bundle;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;


import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.hita.TextTools;
import com.stupidtree.hita.R;
import com.stupidtree.hita.core.TimeTable;
import com.stupidtree.hita.diy.HDatePickerDialog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.now;
import static com.stupidtree.hita.HITAApplication.thisWeekOfTerm;

public class ActivityEmptyClassroomDetail extends BaseActivity {
    Toolbar toolbar;
    String lh,xnxq,cd,name;
    WebView webView;
    ImageView setTime;
    TextView setTime_txt,xnxq_txt,toolbar_title;
    TextView[] txs;
    CardView[] lamps;
    SwipeRefreshLayout refresh;
    int pageWeek;
    int pageDow;
    getResultTask pageTask;

    @Override
    protected void stopTasks() {
        if(pageTask!=null&&!pageTask.isCancelled()) pageTask.cancel(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty_classroom_detail);
        webView = findViewById(R.id.webview);
        lh = getIntent().getStringExtra("lh");
        xnxq = getIntent().getStringExtra("xnxq");
        cd = getIntent().getStringExtra("cd");
        name = getIntent().getStringExtra("name");
        pageDow = TimeTable.getDOW(now);
        pageWeek = thisWeekOfTerm;

        Log.e("emptyclassroom_detail:","name="+name+"，code="+cd);
        initToolbar();
        initViews();
        // getResult();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Refresh();
    }

    void initToolbar(){
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar_title = findViewById(R.id.toolbar_title);
        toolbar_title.setText(name);
    }
    void initViews() {
        refresh = findViewById(R.id.refresh);

        xnxq_txt = findViewById(R.id.time_text);
        txs = new TextView[]{
                findViewById(R.id.tx_1),
                findViewById(R.id.tx_2),
                findViewById(R.id.tx_3),
                findViewById(R.id.tx_4),
                findViewById(R.id.tx_5),
                findViewById(R.id.tx_6)
        };
        lamps = new CardView[]{
                findViewById(R.id.lamp_1),
                findViewById(R.id.lamp_2),
                findViewById(R.id.lamp_3),
                findViewById(R.id.lamp_4),
                findViewById(R.id.lamp_5),
                findViewById(R.id.lamp_6)
        };
        setTime = findViewById(R.id.pick_date);
        setTime_txt = findViewById(R.id.date_show);
        setTime.setOnClickListener(new pickTimeListener());
        setTime_txt.setText("第" + pageWeek + "周 " + TextTools.words_time_DOW[pageDow - 1] + "(今天)");

        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
              Refresh();
            }
        });
    }

    void Refresh(){
        if(pageTask!=null&&!pageTask.isCancelled()) pageTask.cancel(true);
        pageTask =  new getResultTask();
        pageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    class pickTimeListener implements View.OnClickListener {
        HDatePickerDialog dialog;

        pickTimeListener() {
            dialog = new HDatePickerDialog(ActivityEmptyClassroomDetail.this, setTime_txt);
            dialog.setOnDialogConformListener(new HDatePickerDialog.onDialogConformListener() {
                @Override
                public void onClick(int week, int dow, boolean dateSet) {
                    pageWeek = week;
                    pageDow = dow;
                   Refresh();
                }
            });
        }

        @Override
        public void onClick(View v) {
            dialog.showDatePickerDialog();
        }
    }

    void getResult() {
        StringBuilder sb = new StringBuilder();
        sb.append("pageXnxq=").append(xnxq).append("&")
                .append("pageZc1=").append("1").append("&")
                .append("pageZc2=").append("1").append("&")
                .append("pageXiaoqu=").append("1").append("&")
                .append("pageLhdm=").append(lh).append("&")
                .append("pageCddm=").append(cd);
        webView.postUrl("http://jwts.hitsz.edu.cn/kjscx/queryKjs_wdl",
                sb.toString().getBytes()
        );

    }

    class getResultTask extends AsyncTask {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            refresh.setRefreshing(true);
            for (int i = 0; i < 6; i++) {
                txs[i].setText("-");
                txs[i].setTextColor(ContextCompat.getColor(HContext, R.color.material_secondary_text));
                lamps[i].setCardBackgroundColor(ContextCompat.getColor(HContext, R.color.material_backgroung_grey_300));
            }
            xnxq_txt.setText("查询学期："+xnxq);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            Boolean[] result = {false, false, false, false, false, false};
            try {
                Document page = Jsoup.connect("http://jwts.hitsz.edu.cn/kjscx/queryKjs_wdl")
                        .timeout(20000)
                        .data("pageXnxq", xnxq)
                        .data("pageZc1", pageWeek + "").data("pageZc2", pageWeek + "")
                        .data("pageXiaoqu", "1")
                        .data("pageLhdm", lh)
                        .data("pageCddm", cd)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36")
                        .post();
                Elements rows = page.getElementsByClass("dataTable").select("tr");
                rows.remove(0);
                rows.remove(0);
                int fromIndex = (pageDow - 1) * 6+1;
                Elements tds = rows.first().select("td");
                for (int i = fromIndex, j = 0; i < fromIndex + 6; i++, j++) {
                    //if (i >= tds.size()) continue;
                    result[j] = tds.get(i).getElementsByClass("kjs_icon kjs_icon01").size() > 0;
                }
                //System.out.println(page);
                return result;
            } catch (IOException e) {
                //Toast.makeText(ActivityEmptyClassroomDetail.this,"加载出错",Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return result;
            }
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            refresh.setRefreshing(false);
            Boolean[] res = (Boolean[]) o;
            for (int i = 0; i < 6; i++) {
                if (res[i]) {
                    txs[i].setText("占用");
                    txs[i].setTextColor(getColorPrimary());
                    lamps[i].setCardBackgroundColor(getColorPrimary());
                } else {
                    txs[i].setText("空闲");
                    txs[i].setTextColor(ContextCompat.getColor(HContext, R.color.material_secondary_text));
                    lamps[i].setCardBackgroundColor(ContextCompat.getColor(HContext, R.color.material_backgroung_grey_300));
                }
            }
            //webView.postUrl();
            //webView.loadData(Html.fromHtml(o.toString()).toString(),"text/html","UTF-8");
        }
    }

}
