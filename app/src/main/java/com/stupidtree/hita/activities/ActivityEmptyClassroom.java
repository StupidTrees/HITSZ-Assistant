package com.stupidtree.hita.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.fragments.BasicRefreshTask;
import com.stupidtree.hita.fragments.popup.FragmentEmptyClassroomDialog;
import com.stupidtree.hita.timetable.TimetableCore;
import com.stupidtree.hita.util.EventsUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.stupidtree.hita.HITAApplication.HContext;


public class ActivityEmptyClassroom extends BaseActivity implements BasicRefreshTask.ListRefreshedListener<List<Map<String,String>>> {
    Toolbar toolbar;
    ArrayList<Map<String,String>> listRes;
    placesListAdapter listAdapter;
    RecyclerView list;
    SwipeRefreshLayout refresh;
    TextView pageXnxq_Text,pageTime_Text;
    int pageCourseNumber;
    LinearLayout invalid;



//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true,true,false);
        setContentView(R.layout.activity_empty_classroom);
        initToolbar();
//        if(CurrentUser==null){
//            AlertDialog ad = new AlertDialog.Builder(this).setMessage("登录HITSZ助手账号后同步课表").setTitle("请登录").setPositiveButton("前往登录", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    Intent i = new Intent(ActivityEmptyClassroom.this,ActivityLogin.class);
//                    startActivity(i);
//                    finish();
//                }
//            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    finish();
//                }
//            }).create();
//            ad.show();
//        }else if(!isDataAvailable()){
//            AlertDialog ad = new AlertDialog.Builder(this).setMessage("需要当前学期的课表代码进行空教室查询，请导入课表后使用！").setTitle("没有课表数据").setPositiveButton("前往教务系统", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    ActivityUtils.startJWTSActivity(ActivityEmptyClassroom.this);
//                    finish();
//                }
//            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    finish();
//                }
//            }).create();
//            ad.show();
//        }else if(thisWeekOfTerm<0) {
//            AlertDialog ad = new AlertDialog.Builder(this).setMessage("当前选择的学期尚未开始，请切换为已开始学期进行查询！").setTitle("学期未开始").setPositiveButton("前往课表管理", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    Intent i = new Intent(ActivityEmptyClassroom.this,ActivityCurriculumManager.class);
//                    startActivity(i);
//                    finish();
//                }
//            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    finish();
//                }
//            }).create();
//            ad.show();
//
//        }else{
//
//        }
        initList();
        Refresh();
    }

    void initToolbar(){
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
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
        new refreshListTask(this).executeOnExecutor(HITAApplication.TPE);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onRefreshStart(String id, Boolean[] params) {
        TimetableCore tc = TimetableCore.getInstance(HContext);
        invalid.setVisibility(View.GONE);
        pageXnxq_Text.setText(tc.getCurrentCurriculum().getCurriculumCode());
        pageCourseNumber = TimetableCore.getNumberAtTime(TimetableCore.getNow());
        String nowNumber;
        if(pageCourseNumber<0)nowNumber = "课间/课后";
        else nowNumber = "第"+pageCourseNumber+"节课";
        pageTime_Text.setText(
                EventsUtils.getWeekDowString(tc.getThisWeekOfTerm(), TimetableCore.getDOW(TimetableCore.getNow()), false, EventsUtils.TTY_NONE) + " " + nowNumber);
        refresh.setRefreshing(true);
    }

    @Override
    public void onListRefreshed(String id, Boolean[] params, List<Map<String,String>> result) {
        refresh.setRefreshing(false);
        if(result!=null){
            listRes.clear();
            listRes.addAll(result);
            invalid.setVisibility(View.GONE);
            listAdapter.notifyDataSetChanged();
            list.scheduleLayoutAnimation();
        }else{
            invalid.setVisibility(View.VISIBLE);
        }
    }


    static class refreshListTask extends BasicRefreshTask<List<Map>> {

        refreshListTask(ListRefreshedListener listRefreshedListener) {
            super(listRefreshedListener);
        }

        @Override
        protected List<Map> doInBackground(ListRefreshedListener listRefreshedListener, Boolean... booleans) {
            List<Map> result = new ArrayList<>();
            try {
                Document page = Jsoup.connect("http://jwts.hitsz.edu.cn:8080/kjscx/queryKjs_wdl")
                        //.data("pageXnxq",TimetableCore.getInstance(HContext).getCurrentCurriculum().curriculumCode)
                        .data("pageZc1","1").data("pageZc2","1")
                        .data("pageXiaoqu","1")
                        .data("pageLhdm","")
                        .data("pageCddm","")
                        .ignoreContentType(true)
                        .ignoreHttpErrors(true)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36")
                        .post();
                Element lhs = page.getElementById("pageLhdm");
                for(Element lh:lhs.select("option")){
                    if(TextUtils.isEmpty(lh.attr("value"))) continue;
                    Map<String, String> m = new HashMap<>();
                    m.put("name",lh.text());
                    m.put("value",lh.attr("value"));
                    result.add(m);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return result;
        }


    }



    class placesListAdapter extends RecyclerView.Adapter<placesListAdapter.placesHolder>{


        @NonNull
        @Override
        public placesHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View v = getLayoutInflater().inflate(R.layout.dynamic_emptyclassroom_places,viewGroup,false);
            return new placesHolder(v, listRes.get(i).get("value"));
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public void onBindViewHolder(@NonNull final placesHolder placesHolder, final int i) {
            placesHolder.domainName.setText(listRes.get(i).get("name"));
            placesHolder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentEmptyClassroomDialog.getInstance(
                            listRes.get(i).get("name"), listRes.get(i).get("value"),pageCourseNumber).show(getSupportFragmentManager(),"aecd");
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
            placesHolder(@NonNull View itemView, String lh) {
                super(itemView);
                domainName = itemView.findViewById(R.id.domain_name);
                card = itemView.findViewById(R.id.card);
            }
        }
    }

   }
