package com.stupidtree.hita.activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import android.os.Bundle;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.fragments.FragmentEmptyClassroomDialog;
import com.stupidtree.hita.hita.TextTools;
import com.stupidtree.hita.R;
import com.stupidtree.hita.core.TimeTable;
import com.stupidtree.hita.util.ActivityUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.allCurriculum;
import static com.stupidtree.hita.HITAApplication.isDataAvailable;
import static com.stupidtree.hita.HITAApplication.mainTimeTable;
import static com.stupidtree.hita.HITAApplication.now;
import static com.stupidtree.hita.HITAApplication.thisCurriculumIndex;
import static com.stupidtree.hita.HITAApplication.thisWeekOfTerm;

public class ActivityEmptyClassroom extends BaseActivity {
    Toolbar toolbar;
    ArrayList<Map> listRes;
    placesListAdapter listAdapter;
    RecyclerView list;
    SwipeRefreshLayout refresh;
    TextView pageXnxq_Text,pageTime_Text;
    int pageCourseNumber;
    refreshListTask pageTask;
    LinearLayout invalid;



    @Override
    protected void stopTasks() {
        if(pageTask!=null&&pageTask.getStatus()!=AsyncTask.Status.FINISHED) pageTask.cancel(true);
          }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true,true,false);
        setContentView(R.layout.activity_empty_classroom);
        initToolbar();
        if(CurrentUser==null){
            AlertDialog ad = new AlertDialog.Builder(this).setMessage("登录HITSZ助手账号后同步课表").setTitle("请登录").setPositiveButton("前往登录", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent(ActivityEmptyClassroom.this,ActivityLogin.class);
                    startActivity(i);
                    finish();
                }
            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).create();
            ad.show();
        }else if(!isDataAvailable()){
            AlertDialog ad = new AlertDialog.Builder(this).setMessage("需要当前学期的课表代码进行空教室查询，请导入课表后使用！").setTitle("没有课表数据").setPositiveButton("前往教务系统", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ActivityUtils.startJWTSActivity(ActivityEmptyClassroom.this);
                    finish();
                }
            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).create();
            ad.show();
        }else if(thisWeekOfTerm<0) {
            AlertDialog ad = new AlertDialog.Builder(this).setMessage("当前选择的学期尚未开始，请切换为已开始学期进行查询！").setTitle("学期未开始").setPositiveButton("前往课表管理", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent(ActivityEmptyClassroom.this,ActivityCurriculumManager.class);
                    startActivity(i);
                    finish();
                }
            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).create();
            ad.show();

        }else{
            initList();
            Refresh();
        }

    }

    void initToolbar(){
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        pageTime_Text = findViewById(R.id.page_time_text);
        pageXnxq_Text = findViewById(R.id.page_xnxq_text);
        invalid = findViewById(R.id.invalid);
    }

    void initList(){
        refresh = findViewById(R.id.refresh);
        list = findViewById(R.id.empty_classroom_list);
        listAdapter = new placesListAdapter();
        listRes = new ArrayList<>();
        list.setAdapter(listAdapter);
        list.setLayoutManager(new GridLayoutManager(this,2));
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
             Refresh();
            }
        });
    }

    void Refresh(){
        if(pageTask!=null&&pageTask.getStatus()!=AsyncTask.Status.FINISHED) pageTask.cancel(true);
        pageTask =  new refreshListTask();
        pageTask.executeOnExecutor(HITAApplication.TPE);
    }
    class refreshListTask extends AsyncTask{

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            invalid.setVisibility(View.GONE);
            pageXnxq_Text.setText(mainTimeTable.core.curriculumCode);
            pageCourseNumber = TimeTable.getNumberAtTime(now);
            String nowNumber;
            if(pageCourseNumber<0)nowNumber = "课间/课后";
            else nowNumber = "第"+pageCourseNumber+"节课";
            pageTime_Text.setText("第"+thisWeekOfTerm+"周 "+ TextTools.words_time_DOW[TimeTable.getDOW(now)-1]+" "+nowNumber);
            now.setTimeInMillis(System.currentTimeMillis());
            refresh.setRefreshing(true);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                Document page = Jsoup.connect("http://jwts.hitsz.edu.cn:8080/kjscx/queryKjs_wdl")
                        .timeout(5000)
                        .data("pageXnxq",allCurriculum.get(thisCurriculumIndex).curriculumCode)
                        .data("pageZc1","1").data("pageZc2","1")
                        .data("pageXiaoqu","1")
                        .data("pageLhdm","")
                        .data("pageCddm","")
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36")
                        .post();
                Element lhs = page.getElementById("pageLhdm");
                listRes.clear();
                for(Element lh:lhs.select("option")){
                    if(TextUtils.isEmpty(lh.attr("value"))) continue;
                    Map m = new HashMap();
                    m.put("name",lh.text());
                    m.put("value",lh.attr("value"));
                    listRes.add(m);
                }
               // System.out.println(page);
                return listRes.size()>0;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            refresh.setRefreshing(false);
            if((boolean)o){
                invalid.setVisibility(View.GONE);
                listAdapter.notifyDataSetChanged();
                list.scheduleLayoutAnimation();
            }else{
                invalid.setVisibility(View.VISIBLE);
            }

        }
    }



    class placesListAdapter extends RecyclerView.Adapter<placesListAdapter.placesHolder>{


        @NonNull
        @Override
        public placesHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View v = getLayoutInflater().inflate(R.layout.dynamic_emptyclassroom_places,viewGroup,false);
            return new placesHolder(v, (String) listRes.get(i).get("value"));
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public void onBindViewHolder(@NonNull final placesHolder placesHolder, final int i) {
            placesHolder.domainName.setText((CharSequence) listRes.get(i).get("name"));
            placesHolder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentEmptyClassroomDialog.getInstance(
                            listRes.get(i).get("name").toString(),listRes.get(i).get("value").toString(),pageCourseNumber).show(getSupportFragmentManager(),"aecd");
                }
            });

        }

        @Override
        public int getItemCount() {
            return listRes.size();
        }

        class placesHolder extends RecyclerView.ViewHolder{
            TextView domainName;
            CardView card;
            public placesHolder(@NonNull View itemView,String lh) {
                super(itemView);
                domainName = itemView.findViewById(R.id.domain_name);
                card = itemView.findViewById(R.id.card);
            }
        }
    }

   }
