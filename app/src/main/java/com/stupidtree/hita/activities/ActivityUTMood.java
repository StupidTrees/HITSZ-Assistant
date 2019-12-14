package com.stupidtree.hita.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.JsonObject;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.R;
import com.stupidtree.hita.fragments.FragmentUTMoodDay;
import com.stupidtree.hita.online.Infos;

import java.sql.Ref;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

import static com.stupidtree.hita.HITAApplication.HContext;

public class ActivityUTMood extends BaseActivity

{
    List<Infos> pagerRes;
    List<FragmentUTMoodDay> fragments;
    ViewPager pager;
    pagerAdapter pagerAdapter;
    TabLayout tabs;
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
    public float getMoodScore(JsonObject info){
        int happy = info.get("happy").getAsInt();
        int normal = info.get("normal").getAsInt();
        int sad = info.get("sad").getAsInt();
        float haP = 100f*(float)happy/(happy+normal+sad);
        float nP = 100f*(float)normal/(happy+normal+sad);

        return (float) (haP*0.5+nP*0.2+50);
    }
    void Refresh(){
        swipeRefreshLayout.setRefreshing(true);
        BmobQuery<Infos> bq = new BmobQuery<>();
        bq.addWhereEqualTo("type","ut_mood");
        bq.order("createdAt");
        lineChart.setVisibility(View.INVISIBLE);
        bq.findObjects(new FindListener<Infos>() {
            @Override
            public void done(List<Infos> list, BmobException e) {
                swipeRefreshLayout.setRefreshing(false);
                if(e==null&&list!=null&&list.size()>0){
                    lineChart.setVisibility(View.VISIBLE);
                        pagerRes.clear();
                        pagerRes.addAll(list);
                        fragments.clear();
                        List<Entry> entityList=new ArrayList<>();
                        int j=0;
                        for(Infos i:list){
                            FragmentUTMoodDay fut = FragmentUTMoodDay.newInstance(i.getJson(),i.getName().replaceAll("ut_mood_",""));
                            if(!fragments.contains(fut)){
                                fragments.add(fut);
                                entityList.add(new Entry(j, getMoodScore(i.getJson())));
                        }
                        j++;
                        }
                        lineChart.getXAxis().setAxisMaximum(list.size()+1);
                        lineDataSet = new LineDataSet(entityList,"line");
                        lineDataSet.setLineWidth(3f);
                        lineDataSet.setHighlightEnabled(true);
                        lineDataSet.setHighLightColor(getColorPrimaryDark());
                        lineDataSet.setCircleRadius(5f);
                        lineDataSet.setCircleHoleRadius(2f);
                        lineDataSet.setDrawCircleHole(true);
                        lineDataSet.setColor(getColorPrimary());
                        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                        lineDataSet.setCircleColor(getColorAccent());
                        lineDataSet.setDrawFilled(true);
                        lineDataSet.setFillDrawable(HContext.getDrawable(R.drawable.gradient_bg_fade_vertical));
                        LineData lineData=new LineData(lineDataSet);
                        lineChart.setData(lineData);
                        lineChart.zoom(0.2f*1*list.size(),1f,list.size()-1,0);
                        pagerAdapter.notifyDataSetChanged();
                        pager.setCurrentItem(pagerRes.size()-1);

                }else{
                    lineChart.setVisibility(View.INVISIBLE);
                    Snackbar.make(swipeRefreshLayout,"加载失败！",Snackbar.LENGTH_SHORT).show();
                }
            }
        });

    }

    void initChart(){
        lineChart.setScaleYEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getAxisLeft().setAxisMaximum(100f);
        lineChart.getAxisLeft().setAxisMinimum(50f);
        lineChart.getXAxis().setDrawAxisLine(false);
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
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                if(Math.floor(value)!=value||value>=pagerRes.size()) return "";
                return pagerRes.get((int) value).getName().replaceAll("ut_mood_","").replaceAll("2019","19").replaceAll("2020","20").replaceAll("2021","21");
            }
        });
        lineChart.setVisibleXRange(3,10);
        lineChart.invalidate();
        setupGradient(lineChart);
    }
    void initToolbar(){
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("HITSZ校园心情");
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
        setWindowParams(true,true,false);
        setContentView(R.layout.activity_utmood);
        initPager();
        initChart();
        initToolbar();
    }

    void initPager(){
        lineChart = findViewById(R.id.linechart);
        pagerRes = new ArrayList<>();
        fragments = new ArrayList<>();
        pager = findViewById(R.id.pager);
        tabs = findViewById(R.id.tabs);
        pagerAdapter = new pagerAdapter(getSupportFragmentManager());
        swipeRefreshLayout = findViewById(R.id.refresh);
        pager.setAdapter(pagerAdapter);
        tabs.setupWithViewPager(pager);
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
                Highlight highlight = new Highlight(lineDataSet.getEntryForIndex(position).getX(),lineDataSet.getEntryForIndex(position).getY(),0);
                lineChart.highlightValue(highlight);
                //lineChart.moveViewToX(highlight.getX());
                lineChart.moveViewToX(highlight.getX());
                lineChart.centerViewToAnimated(highlight.getX(),highlight.getY(),lineDataSet.getAxisDependency(),200);

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
                getColorPrimary(),
                getColorFade(),
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
            return pagerRes.get(position).getName().replaceAll("ut_mood_","");
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }
}
