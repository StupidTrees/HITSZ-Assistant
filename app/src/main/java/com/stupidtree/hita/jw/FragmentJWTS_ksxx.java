//package com.stupidtree.hita.jwts;
//
//import android.content.Context;
//import android.net.Uri;
//import android.os.AsyncTask;
//import android.os.Bundle;
//
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.LinearLayout;
//import android.widget.ProgressBar;
//import android.widget.Toast;
//
//
//import com.stupidtree.hita.BaseFragment;
//import com.stupidtree.hita.HITAApplication;
//import com.stupidtree.hita.R;
//import com.stupidtree.hita.activities.ActivityMain;
//import com.stupidtree.hita.core.TimeTable;
//import com.stupidtree.hita.core.TimetableCore.EventItem;
//import com.stupidtree.hita.core.TimetableCore.HTime;
//import com.stupidtree.hita.diy.ButtonLoading;
//import com.stupidtree.hita.adapter.KSXXListAdapter;
//import com.stupidtree.hita.diy.WrapContentLinearLayoutManager;
//
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.jsoup.select.Elements;
//
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import static com.stupidtree.hita.HITAApplication.allCurriculum;
//import static com.stupidtree.hita.HITAApplication.cookies_jwts;
//import static com.stupidtree.hita.HITAApplication.isDataAvailable;
//import static com.stupidtree.hita.HITAApplication.mainTimeTable;
//import static com.stupidtree.hita.HITAApplication.thisCurriculumIndex;
//import static com.stupidtree.hita.fragments.main.FragmentTimeLine.showEventDialog;
//
//
//public class FragmentJWTS_ksxx extends BaseFragment {
//    RecyclerView list;
//    KSXXListAdapter listAdapter;
//    List<Map<String,String>> lisRes;
//    private OnFragmentInteractionListener mListener;
//    ProgressBar loadingView;
//    List<EventItem> exams;
//    ButtonLoading bt_import_exam;
//    LinearLayout listLayout;
//    LinearLayout invalidLayout;
//    refreshListTask pageTask;
//
//    public FragmentJWTS_ksxx() {
//        // Required empty public constructor
//    }
//
//
//    public static FragmentJWTS_ksxx newInstance() {
//        FragmentJWTS_ksxx fragment = new FragmentJWTS_ksxx();
//        Bundle args = new Bundle();
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View v = inflater.inflate(R.layout.fragment_jwts_ksxx, container, false);
//        initPage(v);
//        initList(v);
//        Refresh();
//        return v;
//    }
//
//    void initPage(View v){
//        listLayout = v.findViewById(R.id.listLayout);
//        invalidLayout = v.findViewById(R.id.ksxx_invalid);
//        loadingView = v.findViewById(R.id.xsxk_loading);
//        bt_import_exam = v.findViewById(R.id.button_import_ksxx);
//        bt_import_exam.setOnButtonLoadingListener(new ButtonLoading.OnButtonLoadingListener() {
//            @Override
//            public void onClick() {
//                if(!isDataAvailable()){
//                    Toast.makeText(FragmentJWTS_ksxx.this.getContext(),"请先导入一个课表！",Toast.LENGTH_SHORT).show();
//                }else{
//                    if(exams!=null&&exams.size()>0){
//                        new addExamsTask().executeOnExecutor(HITAApplication.TPE);
//                    }else{
//                        Toast.makeText(FragmentJWTS_ksxx.this.getContext(),"没有可导入的考试信息！",Toast.LENGTH_SHORT).show();
//                    }
//                }
//
//            }
//
//            @Override
//            public void onStart() {
//
//            }
//
//            @Override
//            public void onFinish() {
//
//            }
//        });
//    }
//
//    void initList(final View v){
//        exams = new ArrayList<>();
//        list = v.findViewById(R.id.ksxx_list);
//        lisRes = new ArrayList<>();
//        listAdapter = new KSXXListAdapter(v.getContext(),lisRes,false);
//        list.setAdapter(listAdapter);
//        LinearLayoutManager layoutManager = new WrapContentLinearLayoutManager(v.getContext(),RecyclerView.VERTICAL,false);
//        list.setLayoutManager(layoutManager);
//        listAdapter.setmOnOperateClickListsner(new KSXXListAdapter.OnOperateClickListener() {
//            @Override
//            public void OnClick(View view, int index, boolean choose) {
//                showEventDialog(getActivity(),exams.get(index),null,null);
//            }
//        });
//    }
//
//
//
//
//
//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }
//
//    @Override
//    protected void stopTasks() {
//        if(pageTask!=null&&pageTask.getStatus()!=AsyncTask.Status.FINISHED) pageTask.cancel(true);
//    }
//
//    @Override
//    public void Refresh() {
//        stopTasks();
//        pageTask =  new refreshListTask();
//        pageTask.executeOnExecutor(HITAApplication.TPE);
//    }
//
//
//    public interface OnFragmentInteractionListener {
//        void onFragmentInteraction(Uri uri);
//    }
//
//    void addExamEvent(String name,String code,String place,String time){
//        String dateS = null;
//        String hourS = null;
//        SimpleDateFormat sdf1;
//        try {
//            dateS = time.substring(0,time.indexOf("，"));
//            hourS = time.substring(time.lastIndexOf("，")+1);
//            sdf1 = new SimpleDateFormat("yyyy-MM-dd");
//        } catch (Exception e) {
//            dateS = time.substring(0,time.indexOf("("));
//            hourS = time.substring(time.lastIndexOf(")")+1);
//            sdf1 = new SimpleDateFormat("yyyy年MM月dd日");
//        }
//
//
//        String[] hourSS = hourS.split("-");
//
//        //Log.e("TIME",total);
//
//        SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");
//        Date date = new Date();
//        Date from = new Date();
//        Date to = new Date();
//        try {
//            date = sdf1.parse(dateS);
//            from = sdf2.parse(hourSS[0]);
//            to = sdf2.parse(hourSS[1]);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        Calendar tempDate = Calendar.getInstance();
//        tempDate.setTimeInMillis(date.getTime());
//        tempDate.set(Calendar.HOUR_OF_DAY,12);
//        //Log.e("date",date.toString());
//
//        Calendar tempFrom = Calendar.getInstance();
//        tempFrom.setTimeInMillis(from.getTime());
//        Calendar tempTo = Calendar.getInstance();
//        tempTo.setTimeInMillis(to.getTime());
//        int DOW = tempDate.get(Calendar.DAY_OF_WEEK)==1?7:tempDate.get(Calendar.DAY_OF_WEEK)-1;
//        //Log.e("date",tempDate.toString());
//        EventItem add = new EventItem(null,timeTableCore.getCurrentCurriculum().curriculumCode,TimetableCore.TIMETABLE_EVENT_TYPE_EXAM,name+"考试",place,"科目代码："+code,time,new HTime(tempFrom),new HTime(tempTo),timeTableCore.getCurrentCurriculum().getWeekOfTerm(tempDate),DOW,false);
//        Log.e("!!",add.toString());
//        exams.add(add);
//
//    }
//
//
//    class refreshListTask extends AsyncTask{
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            listLayout.setVisibility(View.INVISIBLE);
//            loadingView.setVisibility(View.VISIBLE);
//        }
//
//        @Override
//        protected Object doInBackground(Object[] objects) {
//            try {
//                exams.clear();
//                lisRes.clear();
//                 Document xkPage = Jsoup.connect("http://jwts.hitsz.edu.cn:8080/kscx/queryKcForXs").cookies(cookies_jwts).timeout(5000)
//                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
//                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
//                        .header("Content-Type", "application/x-www-form-urlencoded")
//                        .ignoreContentType(true)
//                        .get();
//                //System.out.println(xkPage.toString());
//                if(xkPage.toString().contains("alert('")) return false;
//                Elements rows = xkPage.getElementsByClass("bot_line").first().select("tr");
//                rows.remove(0);
//                for(Element tr:rows){
//                    Log.e("!",tr.toString());
//                    Elements tds = tr.select("td");
//                    Map m = new HashMap();
//                    String name = tds.get(1).text();
//                    String time = tds.get(5).text();
//                    String place = tds.get(3).text();
//                    String code = tds.get(2).text();
//                    m.put("name",name);
//                    m.put("code",code);
//                    m.put("place",place);
//                    m.put("time",time);
//                    addExamEvent(name,code,place,time);
//                    lisRes.add(m);
//                }
//                return lisRes.size()>0;
//            } catch (Exception e) {
//                e.printStackTrace();
//               return false;
//            }
//        }
//
//        @Override
//        protected void onPostExecute(Object o) {
//            super.onPostExecute(o);
//
//            if((Boolean)o){
//                invalidLayout.setVisibility(View.GONE);
//                listLayout.setVisibility(View.VISIBLE);
//                loadingView.setVisibility(View.INVISIBLE);
//                listAdapter.notifyDataSetChanged();
//                //ActivityMain.saveData(FragmentJWTS_ksxx.this.getActivity());
//            }else {
//                loadingView.setVisibility(View.INVISIBLE);
//                invalidLayout.setVisibility(View.VISIBLE);
//                listLayout.setVisibility(View.INVISIBLE);
//            }
//
//        }
//    }
//
//
//    class addExamsTask extends AsyncTask{
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            bt_import_exam.setProgress(true);
//            timeTableCore.clearEvent(TimetableCore.TIMETABLE_EVENT_TYPE_EXAM);
//        }
//
//        @Override
//        protected Object doInBackground(Object[] objects) {
//            try{
//                for(EventItem ei:exams){
//                    timeTableCore.addEvent(ei);
//                }
//                return true;
//            }catch (Exception e){
//                return false;
//            }
//
//
//        }
//
//        @Override
//        protected void onPostExecute(Object o) {
//            super.onPostExecute(o);
//            if(bt_import_exam!=null) bt_import_exam.setProgress(false);
//            if((Boolean) o){
//                Toast.makeText(FragmentJWTS_ksxx.this.getContext(),"导入成功！",Toast.LENGTH_SHORT).show();
//                ActivityMain.saveData();
//            }else{
//                Toast.makeText(FragmentJWTS_ksxx.this.getContext(),"导入失败！",Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//}
