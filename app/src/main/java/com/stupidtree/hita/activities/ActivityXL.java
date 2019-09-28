package com.stupidtree.hita.activities;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.util.ArrayList;
import java.util.List;

public class ActivityXL extends BaseActivity {

    List<MonthViewRes> listRes;
    RecyclerView list;
    MonthAdapter listAdapter;
    AsyncTask pageTask;
    TextView calendar_name;
    SwipeRefreshLayout refresh;
    String psString;
    FloatingActionButton fab;

    @Override
    protected void stopTasks() {
        if(pageTask!=null&&!pageTask.isCancelled()) pageTask.cancel(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true,false,false);
        setContentView(R.layout.activity_xl);
        initToolbar();
        initList();

    }

    void initToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(psString)) {
                    Snackbar.make(v, "暂时没有获取到备注，请稍后再试", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                View dlg = getLayoutInflater().inflate(R.layout.dialog_textview,null,false);
                TextView tv = dlg.findViewById(R.id.text);
                AlertDialog ad = new AlertDialog.Builder(ActivityXL.this).setTitle("校历备注").setView(dlg).create();
                tv.setText(Html.fromHtml(psString));
                ad.show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Refresh();
    }

    void initList() {
        refresh = findViewById(R.id.refresh);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Refresh();
            }
        });
        calendar_name = findViewById(R.id.calendar_name);
        listRes = new ArrayList<>();
        list = findViewById(R.id.list);
        listAdapter = new MonthAdapter();
        list.setAdapter(listAdapter);
        list.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
    }


    protected void Refresh() {
        if (pageTask != null && !pageTask.isCancelled()) pageTask.cancel(true);
        pageTask = new refreshTask();
        pageTask.execute();
    }

    private class WeekViewRes {
        String name;
        String mon;
        String tue;
        String wed;
        String thu;
        String fri;
        String sat;
        String sun;

        public WeekViewRes(String name, String mon, String tue, String wed, String thu, String fri, String sat, String sun) {
            this.name = name;
            this.mon = mon;
            this.tue = tue;
            this.wed = wed;
            this.thu = thu;
            this.fri = fri;
            this.sat = sat;
            this.sun = sun;
        }
    }

    private class MonthViewRes {
        String name;
        List<WeekViewRes> weeks;

        public MonthViewRes(String name) {
            this.name = name;
            weeks = new ArrayList<>();
        }

        public void addWeek(WeekViewRes wr) {
            weeks.add(wr);
        }

    }

    private class WeekAdapter extends RecyclerView.Adapter<WeekAdapter.weekViewHolder> {
        List<WeekViewRes> mBeans;

        public WeekAdapter() {
            this.mBeans = new ArrayList<>();
        }

        public void setWeeks(List<WeekViewRes> weeks) {
            mBeans.clear();
            mBeans.addAll(weeks);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public weekViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.dynamic_xl_month_week, parent, false);
            return new weekViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull weekViewHolder holder, int position) {
            holder.name.setText(mBeans.get(position).name);
            holder.mon.setText(mBeans.get(position).mon);
            holder.tue.setText(mBeans.get(position).tue);
            holder.wed.setText(mBeans.get(position).wed);
            holder.thu.setText(mBeans.get(position).thu);
            holder.fri.setText(mBeans.get(position).fri);
            holder.sat.setText(mBeans.get(position).sat);
            holder.sun.setText(mBeans.get(position).sun);
        }

        @Override
        public int getItemCount() {
            return mBeans.size();
        }

        class weekViewHolder extends RecyclerView.ViewHolder {
            TextView name, mon, tue, wed, thu, fri, sat, sun;

            public weekViewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.week_name);
                mon = itemView.findViewById(R.id.mon);
                tue = itemView.findViewById(R.id.tue);
                wed = itemView.findViewById(R.id.wed);
                thu = itemView.findViewById(R.id.thu);
                fri = itemView.findViewById(R.id.fri);
                sat = itemView.findViewById(R.id.sat);
                sun = itemView.findViewById(R.id.sun);

            }
        }
    }

    private class MonthAdapter extends RecyclerView.Adapter<MonthAdapter.monthHolder> {

        @NonNull
        @Override
        public monthHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.dynamic_xl_month, parent, false);
            return new monthHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull monthHolder holder, int position) {
            holder.name.setText(listRes.get(position).name);
            holder.weekAdapter.setWeeks(listRes.get(position).weeks);
        }

        @Override
        public int getItemCount() {
            return listRes.size();
        }

        class monthHolder extends RecyclerView.ViewHolder {
            TextView name;
            RecyclerView weeks;
            WeekAdapter weekAdapter;

            public monthHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.month_name);
                weeks = itemView.findViewById(R.id.month_list);
                weekAdapter = new WeekAdapter();
                weeks.setAdapter(weekAdapter);
                weeks.setLayoutManager(new LinearLayoutManager(ActivityXL.this, RecyclerView.VERTICAL, false));
            }
        }
    }

    class refreshTask extends AsyncTask {
        String calendarName;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            refresh.setRefreshing(true);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                Document page = Jsoup.connect("http://www.hitsz.edu.cn/page/id-89.html")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .get();
                calendarName = page.getElementsByClass("calen_tit").first().text();
                Element table = page.getElementsByTag("table").first();
                Elements trs = table.select("tr");
                psString = table.getElementsByClass("ps_td").first().toString();
                listRes.clear();
                for (Element week : trs) {
                   // Log.e("week", String.valueOf(week));
                    if (week.getElementsByClass("week").size() == 0) {
                        //Log.e("has_class", "no");
                        continue;
                    }
                    if (week.getElementsByClass("month").size()>0) {
                        String monthName = week.getElementsByClass("month").text();
                        listRes.add(new MonthViewRes(monthName));
                    }
                    String name = week.getElementsByClass("week").text();
                    Elements days = week.select("td");
                    Elements toRemove = new Elements();
                    for (Element e : days) {
                        if (e.getElementsByClass("month").size() > 0) toRemove.add(e);
                        if (e.hasClass("week") || e.hasClass("ps_td")
                        ) toRemove.add(e);
                    }
                    days.removeAll(toRemove);
                    //Log.e("week:", String.valueOf(days));
                    WeekViewRes wr = new WeekViewRes(name,
                            days.get(0).text(), days.get(1).text(), days.get(2).text(), days.get(3).text(),
                            days.get(4).text(), days.get(5).text(), days.get(6).text()
                    );
                    if (listRes.size() > 0) listRes.get(listRes.size() - 1).addWeek(wr);
                     }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            refresh.setRefreshing(false);
            listAdapter.notifyDataSetChanged();
            list.scheduleLayoutAnimation();
            calendar_name.setText(calendarName);
        }
    }
}
