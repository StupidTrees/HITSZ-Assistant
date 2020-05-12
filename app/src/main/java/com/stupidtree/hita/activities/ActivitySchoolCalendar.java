package com.stupidtree.hita.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.fragments.BaseOperationTask;
import com.stupidtree.hita.util.ActivityUtils;
import com.stupidtree.hita.views.WrapContentLinearLayoutManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ActivitySchoolCalendar extends BaseActivity implements BaseOperationTask.OperationListener<List<ActivitySchoolCalendar.MonthViewRes>> {

    List<MonthViewRes> listRes;
    RecyclerView list;
    MonthAdapter listAdapter;
    TextView calendar_name;
    SwipeRefreshLayout refresh;
    String psString;
    FloatingActionButton fab;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true,false,false);
        setContentView(R.layout.activity_schoolcalendar);
        initToolbar();
        initList();
        Refresh();
    }

    void initToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.inflateMenu(R.menu.toolbar_teacher_official);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.action_open_in_browser){
                    ActivityUtils.openInBrowser(getThis(),"http://www.hitsz.edu.cn/page/id-89.html");
                    return true;
                }
                return false;
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
                @SuppressLint("InflateParams") View dlg = getLayoutInflater().inflate(R.layout.dialog_textview,null,false);
                TextView tv = dlg.findViewById(R.id.text);
                AlertDialog ad = new AlertDialog.Builder(ActivitySchoolCalendar.this).setTitle("校历备注").setView(dlg).create();
                tv.setText(Html.fromHtml(psString));
                ad.show();
            }
        });
    }



    void initList() {
        refresh = findViewById(R.id.refresh);
        refresh.setColorSchemeColors(getColorAccent());
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
        list.setLayoutManager(new WrapContentLinearLayoutManager(this, RecyclerView.VERTICAL, false));
    }


    protected void Refresh() {
         new refreshTask(this).executeOnExecutor(HITAApplication.TPE);
    }

    @Override
    public void onOperationStart(String id, Boolean[] params) {
        refresh.setRefreshing(true);
    }

    @Override
    public void onOperationDone(String id, BaseOperationTask task, Boolean[] params, List<MonthViewRes> result) {
        refreshTask rt = (refreshTask) task;
        psString = rt.psString;
        refresh.setRefreshing(false);
        listRes.clear();
        listRes.addAll(result);
        listAdapter.notifyDataSetChanged();
        list.scheduleLayoutAnimation();
        calendar_name.setText(rt.calendarName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_teacher_official,menu);
        return super.onCreateOptionsMenu(menu);
    }

    private static class WeekViewRes {
        String name;
        String mon;
        String tue;
        String wed;
        String thu;
        String fri;
        String sat;
        String sun;

        WeekViewRes(String name, String mon, String tue, String wed, String thu, String fri, String sat, String sun) {
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

    static class MonthViewRes {
        String name;
        List<WeekViewRes> weeks;

        MonthViewRes(String name) {
            this.name = name;
            weeks = new ArrayList<>();
        }

        void addWeek(WeekViewRes wr) {
            weeks.add(wr);
        }

    }

    class WeekAdapter extends RecyclerView.Adapter<WeekAdapter.weekViewHolder> {
        List<WeekViewRes> mBeans;

        WeekAdapter() {
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

            weekViewHolder(@NonNull View itemView) {
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

            monthHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.month_name);
                weeks = itemView.findViewById(R.id.month_list);
                weekAdapter = new WeekAdapter();
                weeks.setAdapter(weekAdapter);
                weeks.setLayoutManager(new WrapContentLinearLayoutManager(ActivitySchoolCalendar.this, RecyclerView.VERTICAL, false));
            }
        }
    }

    static class refreshTask extends BaseOperationTask<List<MonthViewRes>> {
        String calendarName;
        String psString;

        refreshTask(OperationListener<? extends List> listRefreshedListener) {
            super(listRefreshedListener);
        }


        @Override
        protected List<MonthViewRes> doInBackground(OperationListener<List<MonthViewRes>> listRefreshedListener, Boolean... booleans) {
            List<MonthViewRes> result = new ArrayList<>();
            try {
                Document page = Jsoup.connect("http://www.hitsz.edu.cn/page/id-89.html")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .get();
                calendarName = page.getElementsByClass("calen_tit").first().text();
                Element table = page.getElementsByTag("table").first();
                Elements trs = table.select("tr");
                psString = table.getElementsByClass("ps_td").first().toString();

                for (Element week : trs) {
                    // Log.e("week", String.valueOf(week));
                    if (week.getElementsByClass("week").size() == 0) {
                        //Log.e("has_class", "no");
                        continue;
                    }
                    if (week.getElementsByClass("month").size()>0) {
                        String monthName = week.getElementsByClass("month").text();
                        result.add(new MonthViewRes(monthName));
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
                    if (result.size() > 0) result.get(result.size() - 1).addWeek(wr);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

    }
}
