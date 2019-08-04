package com.stupidtree.hita.activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.hita.TextTools;
import com.stupidtree.hita.R;
import com.stupidtree.hita.core.TimeTable;
import com.stupidtree.hita.util.ActivityUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.Ref;
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
    Set<refreshDetailTask> detailTaskSet;



    @Override
    protected void stopTasks() {
        if(pageTask!=null&&!pageTask.isCancelled()) pageTask.cancel(true);
        for(refreshDetailTask rdt:detailTaskSet) if(rdt!=null&&!rdt.isCancelled()) rdt.cancel(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true,true,false);
        setContentView(R.layout.activity_empty_classroom);
        detailTaskSet = new HashSet<>();
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
    }

    void initList(){
        refresh = findViewById(R.id.refresh);
        list = findViewById(R.id.empty_classroom_list);
        listAdapter = new placesListAdapter();
        listRes = new ArrayList<>();
        list.setAdapter(listAdapter);
        list.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
             Refresh();
            }
        });
    }

    void Refresh(){
        if(pageTask!=null&&!pageTask.isCancelled()) pageTask.cancel(true);
        pageTask =  new refreshListTask();
        pageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    class refreshListTask extends AsyncTask{

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
                Document page = Jsoup.connect("http://jwts.hitsz.edu.cn/kjscx/queryKjs_wdl")
                        .timeout(20000)
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
                System.out.println(page);
                return null;
            } catch (IOException e) {
                //e.printStackTrace();
                return "error!";
            }
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            refresh.setRefreshing(false);
            listAdapter.notifyDataSetChanged();
            list.scheduleLayoutAnimation();
        }
    }

    class refreshDetailTask extends AsyncTask{
        String lhValue;
        List<Map> targetListRes;
        detailListAdapter targetAdapter;
        ProgressBar loading;
        RecyclerView targetList;
        String lhName;
        refreshDetailTask(String lhName,String lhValue,List<Map> res,detailListAdapter targetAdapter,ProgressBar load,RecyclerView targ){
            this.lhValue = lhValue;
            targetListRes = res;
            loading = load;
            targetList = targ ;
            this.targetAdapter = targetAdapter;
            this.lhName = lhName;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            targetList.setVisibility(View.GONE);
            loading.setVisibility(View.VISIBLE);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                Document page = Jsoup.connect("http://jwts.hitsz.edu.cn/kjscx/queryKjs_wdl")
                        .timeout(20000)
                        .data("pageXnxq",allCurriculum.get(thisCurriculumIndex).curriculumCode)
                        .data("pageZc1", String.valueOf(thisWeekOfTerm)).data("pageZc2", String.valueOf(thisWeekOfTerm))
                        .data("pageXiaoqu","1")
                        .data("pageLhdm",lhValue)
                        .data("pageCddm","")
                        .data("pageSize","100")
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36")
                        .post();
                Element lhs = page.getElementById("pageCddm");
                targetListRes.clear();
                List<Map> tempList = new ArrayList<>();
                for(Element lh:lhs.select("option")){
                    //Log.e(lhValue, String.valueOf(lh));
                    if(TextUtils.isEmpty(lh.attr("value"))) continue;
                    Map m = new HashMap();
                    m.put("name",lh.text());
                    m.put("value",lh.attr("value"));
                    tempList.add(m);
//                    JsonObject jo = new JsonObject();
//                    jo.addProperty("domain",lhValue);
//                    jo.addProperty("telephone","26905040");
//                    jo.addProperty("businesshours","7:00-22:30");
//                    jo.addProperty("function","教室");
//                    jo.addProperty("studyhall",false);
//                    jo.addProperty("company","航天物业");
//                    jo.addProperty("code",lh.attr("value"));
//                    Location l = new Location();
//                    l.setInfos(jo.toString());
//                    l.setAddress(lhName);
//                    l.setImageURL("https://bmob-cdn-26359.bmobpay.com/2019/07/27/fe6aa9854079b0288091fd12c8859cb3.jpg");
//                    l.setPositionIntroduction("教室");
//                    l.setRate(0);
//                    l.setStudentnum(0);
//                    l.setType("classroom");
//                    l.setName(lh.text());
//                    l.save(new SaveListener<String>() {
//                        @Override
//                        public void done(String s, BmobException e) {
//
//                        }
//                    });
                }
                Elements rows = page.getElementsByClass("dataTable").select("tr");
                rows.remove(0);
                rows.remove(0);
                for(Element lh:rows){
                   // Log.e("lh",lh.toString());
                    Elements tds = lh.select("td");
                    Map m = new HashMap();
                    m.put("name",tds.get(0).text());
                    boolean hasMatch = false;
                    for(Map tm:tempList){
                        if(tm.get("name").equals(tds.get(0).text())) {
                            m.put("value",tm.get("value"));
                            hasMatch = true;
                            break;
                        }
                    }
                    if(!hasMatch) m.put("value",tds.get(0).text());
                    int dow = TimeTable.getDOW(now);
                    int number = pageCourseNumber;
                    int index = (dow-1)*6+(number%2==0?number/2:number/2+1);
                    Log.e("Number", String.valueOf(number));
                    m.put("available",number<0||tds.get(index).getElementsByClass("kjs_icon kjs_icon01").size()==0);
                    targetListRes.add(m);
                }
                //System.out.println(page);
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return "error!";
            }
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            targetList.setVisibility(View.VISIBLE);
            loading.setVisibility(View.GONE);
            targetAdapter.notifyDataSetChanged();
            targetList.scheduleLayoutAnimation();
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
            placesHolder.detailDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    if(placesHolder.dialogPageTask!=null&&!placesHolder.dialogPageTask.isCancelled()) placesHolder.dialogPageTask.cancel(true);
                    detailTaskSet.remove(placesHolder.dialogPageTask);
                }
            });
            placesHolder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(placesHolder.dialogPageTask!=null&&!placesHolder.dialogPageTask.isCancelled()) placesHolder.dialogPageTask.cancel(true);
                    placesHolder.dialogPageTask = new refreshDetailTask((String) listRes.get(i).get("name"),(String) listRes.get(i).get("value"),placesHolder.detailRes,placesHolder.detailAdapter
                            ,placesHolder.loading,placesHolder.detailPlaces
                    );
                    detailTaskSet.add(placesHolder.dialogPageTask);
                    placesHolder.dialogPageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    placesHolder.detailDialog.show();


                }
            });

        }

        @Override
        public int getItemCount() {
            return listRes.size();
        }

        class placesHolder extends RecyclerView.ViewHolder{
            TextView domainName;
            RecyclerView detailPlaces;
            CardView card;
            List<Map> detailRes;
            detailListAdapter detailAdapter;
            ProgressBar loading;
            AlertDialog detailDialog;
            refreshDetailTask dialogPageTask;
            public placesHolder(@NonNull View itemView,String lh) {
                super(itemView);
                detailRes = new ArrayList<>();
                detailAdapter = new detailListAdapter(lh,detailRes);
                domainName = itemView.findViewById(R.id.domain_name);
                card = itemView.findViewById(R.id.card);

                View dialog = getLayoutInflater().inflate(R.layout.dialog_emptyclassroom_detail,null);
                detailDialog = new AlertDialog.Builder(ActivityEmptyClassroom.this).setView(dialog).setTitle("当前占用情况").create();
                detailPlaces = dialog.findViewById(R.id.detail_list);
                detailPlaces.setAdapter(detailAdapter);
                loading = dialog.findViewById(R.id.detail_loading);
                    detailPlaces.setLayoutManager(new GridLayoutManager(ActivityEmptyClassroom.this,2));

            }
        }
    }

    class detailListAdapter extends RecyclerView.Adapter<detailListAdapter.detailViewHolder>{

        List<Map> mBeans;
        String lh;
        detailListAdapter(String lh,List<Map> res){
            mBeans = res;
            this.lh = lh;
        }
        @NonNull
        @Override
        public detailViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View v = getLayoutInflater().inflate(R.layout.dynamic_emptyclassroom_detail,viewGroup,false);
            return new detailViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull detailViewHolder detailViewHolder, final int i) {
            detailViewHolder.name.setText((CharSequence) mBeans.get(i).get("name"));
            detailViewHolder.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityUtils.startEmptyClassroomDetailActivity(ActivityEmptyClassroom.this, String.valueOf(mBeans.get(i).get("name")),mainTimeTable.core.curriculumCode,lh,String.valueOf(mBeans.get(i).get("value")));
                }
            });
            if((mBeans.get(i).get("available")!=null&&(Boolean)mBeans.get(i).get("available"))){
//                detailViewHolder.lamp.setCardBackgroundColor(ContextCompat.getColor(HContext,R.color.green_primary));
//            }else{
                detailViewHolder.lamp.setCardBackgroundColor(ContextCompat.getColor(HContext,R.color.material_backgroung_grey_300));
            }
        }

        @Override
        public int getItemCount() {
            return mBeans.size();
        }

        class detailViewHolder extends RecyclerView.ViewHolder{
            TextView name;
            CardView item;
            CardView lamp;
            public detailViewHolder(@NonNull View itemView) {
                super(itemView);
                item = itemView.findViewById(R.id.detail_item);
                name = itemView.findViewById(R.id.detail_name);
                lamp = itemView.findViewById(R.id.lamp);
            }
        }
    }
}
