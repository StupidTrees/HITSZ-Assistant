package com.stupidtree.hita.activities;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;
import com.stupidtree.hita.R;
import com.stupidtree.hita.fragments.FragmentUTMoodDay;
import com.stupidtree.hita.online.Infos;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class ActivityUTMood extends BaseActivity {
    List<Infos> pagerRes;
    List<FragmentUTMoodDay> fragments;
    ViewPager pager;
    pagerAdapter pagerAdapter;
    Toolbar toolbar;
    LineChart lineChart;
    SwipeRefreshLayout swipeRefreshLayout;
    LineDataSet lineDataSet;
    //Infos utMood;

    @Override
    protected void stopTasks() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        Refresh();
    }

    public float getMoodScore(JsonObject info) {
        int happy = info.get("happy").getAsInt();
        int normal = info.get("normal").getAsInt();
        int sad = info.get("sad").getAsInt();
        float haP = 100f * (float) happy / (happy + normal + sad);
        float nP = 100f * (float) normal / (happy + normal + sad);

        return (float) (haP * 0.5 + nP * 0.2 + 50);
    }

    void Refresh() {
        swipeRefreshLayout.setRefreshing(true);
        BmobQuery<Infos> bq = new BmobQuery<>();
        bq.addWhereEqualTo("type", "ut_mood");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -2);
        bq.addWhereGreaterThanOrEqualTo("createdAt", new BmobDate(c.getTime()));
        bq.order("createdAt");
        lineChart.setVisibility(View.INVISIBLE);
        bq.findObjects(new FindListener<Infos>() {
            @Override
            public void done(List<Infos> list, BmobException e) {
                swipeRefreshLayout.setRefreshing(false);
                if (e == null && list != null && list.size() > 0) {
                    lineChart.setVisibility(View.VISIBLE);
                    pagerRes.clear();
                    pagerRes.addAll(list);
                    fragments.clear();
                    List<Entry> entityList = new ArrayList<>();
                    int j = 0;
                    for (Infos i : list) {
                        FragmentUTMoodDay fut = FragmentUTMoodDay.newInstance(i.getJson(), i.getName().replaceAll("ut_mood_", ""));
                        if (!fragments.contains(fut)) {
                            fragments.add(fut);
                            entityList.add(new Entry(j, getMoodScore(i.getJson())));
                        }
                        j++;
                    }
                    lineChart.getXAxis().setAxisMaximum(list.size() + 1);
                    lineChart.getXAxis().setTextColor(Color.WHITE);
                    lineDataSet = new LineDataSet(entityList, "line");
                    lineDataSet.setLineWidth(3f);
                    lineDataSet.setHighlightEnabled(true);
                    lineDataSet.setHighLightColor(Color.parseColor("#88FFFFFF"));
                    lineDataSet.setCircleRadius(6f);
                    lineDataSet.setCircleHoleRadius(4f);
                    lineDataSet.setDrawCircleHole(true);
                    lineDataSet.setDrawFilled(true);
                    lineDataSet.setFillColor(Color.WHITE);
                    lineDataSet.setValueTextColor(Color.WHITE);
                    lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                    lineDataSet.setCircleColor(Color.WHITE);
                    LineData lineData = new LineData(lineDataSet);
                    lineChart.setData(lineData);
                    lineChart.zoom(0.2f * 1 * list.size(), 1f, list.size() - 1, 0);
                    pagerAdapter.notifyDataSetChanged();
                    pager.setCurrentItem(pagerRes.size() - 1);

                } else {
                    lineChart.setVisibility(View.INVISIBLE);
                    Snackbar.make(swipeRefreshLayout, "加载失败！", Snackbar.LENGTH_SHORT).show();
                }
            }
        });

    }

    void initChart() {
        lineChart.setScaleYEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getAxisLeft().setAxisMaximum(105f);
        lineChart.getAxisLeft().setAxisMinimum(48f);
        lineChart.getXAxis().setAxisLineColor(Color.WHITE);
        lineChart.getAxisLeft().setTextColor(Color.WHITE);
        lineChart.getAxisLeft().setAxisLineColor(Color.WHITE);
        lineChart.getAxisLeft().setDrawGridLines(false);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                pager.setCurrentItem((int) e.getX());
            }

            @Override
            public void onNothingSelected() {

            }
        });
        // lineChart.getXAxis().setDrawGridLines(false);
        //lineChart.setScaleX(si);

        Description description = new Description();
        description.setEnabled(false);
        lineChart.setDescription(description);
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setGranularityEnabled(false);
        xAxis.setLabelCount(pagerRes.size());

        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                if (Math.floor(value) != value || value >= pagerRes.size()) return "";
                try {
                    Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(pagerRes.get((int) value).getName().replaceAll("ut_mood_", ""));
                    return new SimpleDateFormat(getString(R.string.date_format_4), Locale.getDefault()).format(d);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return "";
            }
        });
        lineChart.setVisibleXRange(3, 10);
        lineChart.invalidate();
        setupGradient(lineChart);
    }

    void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.label_activity_utmood));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true, false, false);
        setContentView(R.layout.activity_utmood);
        initPager();
        initChart();
        initToolbar();
    }

    void initPager() {
        lineChart = findViewById(R.id.linechart);
        pagerRes = new ArrayList<>();
        fragments = new ArrayList<>();
        pager = findViewById(R.id.pager);
        pagerAdapter = new pagerAdapter(getSupportFragmentManager());
        swipeRefreshLayout = findViewById(R.id.refresh);
        pager.setAdapter(pagerAdapter);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Refresh();
            }
        });
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Highlight highlight = new Highlight(lineDataSet.getEntryForIndex(position).getX(), lineDataSet.getEntryForIndex(position).getY(), 0);
                lineChart.highlightValue(highlight);
                //lineChart.moveViewToX(highlight.getX());
                lineChart.moveViewToX(highlight.getX());
                lineChart.centerViewToAnimated(highlight.getX(), highlight.getY(), lineDataSet.getAxisDependency(), 200);

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    private void setupGradient(LineChart mChart) {
        Paint paint = mChart.getRenderer().getPaintRender();
        int height = mChart.getHeight();

        LinearGradient linGrad = new LinearGradient(0, 0, 0, height,
                Color.WHITE, Color.WHITE,
                Shader.TileMode.REPEAT);
        paint.setShader(linGrad);
    }

    class pagerAdapter extends FragmentStatePagerAdapter {

        public pagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            // return FragmentUTMoodDay.newInstance(pagerRes.get(position).getJson(),pagerRes.get(position).getName().replaceAll("ut_mood_",""));
            return fragments.get(position);
        }


        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return pagerRes.get(position).getName().replaceAll("ut_mood_", "");
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }
}
