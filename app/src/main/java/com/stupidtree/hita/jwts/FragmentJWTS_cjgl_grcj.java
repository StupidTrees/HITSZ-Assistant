package com.stupidtree.hita.jwts;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.core.Curriculum;
import com.stupidtree.hita.core.Subject;
import com.stupidtree.hita.diy.WrapContentLinearLayoutManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.stupidtree.hita.HITAApplication.TPE;
import static com.stupidtree.hita.HITAApplication.allCurriculum;
import static com.stupidtree.hita.HITAApplication.cookies_jwts;

public class FragmentJWTS_cjgl_grcj extends BaseFragment {
    RecyclerView qzcj_list,qmcj_list;
    CJXXListAdapter qzcj_adapter,qmcj_adapter;
    List<Map<String,String>> qzcj_listRes,qmcj_listRes;
    private OnFragmentInteractionListener mListener;
    ProgressBar loadingView_qzcj,loadingView_qmcj;
    Spinner qmcj_xnxq_spinner;
    ArrayAdapter xnxqAdapter;
    List<String> xnxqAdapterRes;
    List<Map<String,String>> xnxqValueRes;
    Set<AsyncTask> taskSet;

    public FragmentJWTS_cjgl_grcj() {

        // Required empty public constructor
    }


    public static FragmentJWTS_cjgl_grcj newInstance() {
        FragmentJWTS_cjgl_grcj fragment = new FragmentJWTS_cjgl_grcj();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        taskSet = new HashSet<>();
        View v = inflater.inflate(R.layout.fragment_jwts_cjgl_grcj, container, false);
        initPage(v);
        initLists(v);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        Refresh();
    }

    void initPage(View v){

        loadingView_qzcj = v.findViewById(R.id.qzcj_loading);
        loadingView_qmcj = v.findViewById(R.id.qmcj_loading);
        qmcj_xnxq_spinner = v.findViewById(R.id.qmcj_xnxq_spinner);
        xnxqAdapterRes = new ArrayList<>();
        xnxqValueRes = new ArrayList<>();
        xnxqAdapter = new ArrayAdapter(v.getContext(),android.R.layout.simple_spinner_item,xnxqAdapterRes);
        qmcj_xnxq_spinner.setAdapter(xnxqAdapter);
    }

    void initLists(final View v){
        qzcj_list = v.findViewById(R.id.qzcj_list);
        qmcj_list = v.findViewById(R.id.qmcj_list);
        qzcj_listRes = new ArrayList<>();
        qmcj_listRes = new ArrayList<>();
        qzcj_adapter = new CJXXListAdapter(v.getContext(),qzcj_listRes);
        qmcj_adapter = new CJXXListAdapter(v.getContext(),qmcj_listRes);
        qzcj_list.setAdapter(qzcj_adapter);
        qmcj_list.setAdapter(qmcj_adapter);
        LinearLayoutManager layoutManager1 = new WrapContentLinearLayoutManager(v.getContext(),RecyclerView.VERTICAL,false);
        qzcj_list.setLayoutManager(layoutManager1);
        LinearLayoutManager layoutManager2 = new WrapContentLinearLayoutManager(v.getContext(),RecyclerView.VERTICAL,false);

        qmcj_list.setLayoutManager(layoutManager2);
        qmcj_xnxq_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                new refreshQMCJListTask(getContext()).executeOnExecutor(TPE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }





    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    protected void stopTasks() {
        for(AsyncTask at:taskSet) if(at!=null&&at.getStatus()!=AsyncTask.Status.FINISHED) at.cancel(true);
    }

    @Override
    public void Refresh() {
        new refreshQZCJListTask(getContext()).executeOnExecutor(HITAApplication.TPE);
        new refreshXNXQSpinnerTask(getContext()).executeOnExecutor(HITAApplication.TPE);
    }


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }




    class refreshQZCJListTask extends loadJWTSinfoTask{

        refreshQZCJListTask(Context context) {
            super(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            taskSet.add(this);
            qzcj_list.setVisibility(View.GONE);
            loadingView_qzcj.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                qzcj_listRes.clear();
                 Document xkPage = Jsoup.connect("http://jwts.hitsz.edu.cn/cjcx/queryQzcj").cookies(cookies_jwts).timeout(5000)
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .ignoreContentType(true)
                        .get();
                 String res = super.doInBackground(xkPage.toString());
                 if(res!=null) return res;
                //System.out.println(xkPage.toString());
                Elements rows = xkPage.getElementsByClass("bot_line").first().select("tr");
                rows.remove(0);
                for(Element tr:rows){
                    //Log.e("!",tr.toString());
                    Elements tds = tr.select("td");
                    Map m = new HashMap();
                    String name = tds.get(4).text();
                    String point = tds.get(7).text();
                    String score = tds.get(8).text();
                    String code = tds.get(3).text();
                    String info = tds.get(5).text();
                    m.put("name",name);
                    m.put("code",code);
                    m.put("info",info);
                    m.put("score",score);
                    m.put("point",point);
                    m.put("rank",null);
                   // System.out.println(m);
                    qzcj_listRes.add(m);
                }
                for(Map m:qzcj_listRes){
                    for(Curriculum cc:allCurriculum){
                       for(Subject s:cc.getSubjectsByCourseCode(m.get("code").toString())){
                           s.addScore("qz",m.get("score").toString());
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String o) {
            super.onPostExecute(o);
            qzcj_list.setVisibility(View.VISIBLE);
            loadingView_qzcj.setVisibility(View.GONE);
            qzcj_adapter.notifyDataSetChanged();
            taskSet.remove(this);
        }
    }

    class refreshXNXQSpinnerTask extends loadJWTSinfoTask{

        refreshXNXQSpinnerTask(Context context) {
            super(context);
        }

        @Override
        protected void onPreExecute() {
            taskSet.add(this);
            super.onPreExecute();
            qmcj_list.setVisibility(View.GONE);
            loadingView_qmcj.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                xnxqValueRes.clear();
                xnxqAdapterRes.clear();
                Document xkPage = Jsoup.connect("http://jwts.hitsz.edu.cn/cjcx/queryQmcj").cookies(cookies_jwts).timeout(5000)
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .ignoreContentType(true)
                        .get();
                String res = super.doInBackground(xkPage.toString());
                if(res!=null) return res;
                //System.out.println(xkPage.toString());
                Elements rows = xkPage.getElementsByClass("XNXQ_CON").first().select("option");
                for(Element xnxq:rows){
                    if(xnxq.text().contains("选择")) continue;
                    Map m = new HashMap();
                    m.put("value",xnxq.attr("value"));
                    m.put("text",xnxq.text());
                    xnxqValueRes.add(m);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String o) {
            super.onPostExecute(o);
            taskSet.remove(this);
            qmcj_list.setVisibility(View.VISIBLE);
            loadingView_qmcj.setVisibility(View.GONE);
            for(Map m:xnxqValueRes){
                xnxqAdapterRes.add((String) m.get("text"));
            }
            xnxqAdapter.notifyDataSetChanged();
        }
    }

    class refreshQMCJListTask extends  loadJWTSinfoTask{


        String pageXnxq;
        refreshQMCJListTask(Context context) {
            super(context);
        }

        @Override
        protected void onPreExecute() {
            taskSet.add(this);
            super.onPreExecute();
            pageXnxq = xnxqValueRes.get(qmcj_xnxq_spinner.getSelectedItemPosition()).get("value");
            qmcj_list.setVisibility(View.GONE);
            loadingView_qmcj.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
                try {
                    qmcj_listRes.clear();
                    Document xkPage = Jsoup.connect("http://jwts.hitsz.edu.cn/cjcx/queryQmcj").cookies(cookies_jwts).timeout(5000)
                            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                            .header("Content-Type", "application/x-www-form-urlencoded")
                            .ignoreContentType(true)
                            .data("pageXnxq",pageXnxq)
                            .data("pageSize","100")
                            .post();
                    String r = super.doInBackground(xkPage.toString());
                    if(r!=null) return r;
                    //System.out.println(xkPage.toString());
                    Elements rows = xkPage.getElementsByClass("bot_line").first().select("tr");
                    rows.remove(0);
                    for(Element tr:rows){
                        //Log.e("!",tr.toString());
                        Elements tds = tr.select("td");
                        Map m = new HashMap();
                        String name = tds.get(4).text();
                        String point = tds.get(7).text();
                        String score = tds.get(11).text();
                        String code = tds.get(3).text();
                        String info = tds.get(5).text();
                        String rank = tds.get(15).text();
                        m.put("name",name);
                        m.put("code",code);
                        m.put("info",info);
                        m.put("score",score);
                        m.put("point",point);
                        m.put("rank",rank);
                        // System.out.println(m);
                        qmcj_listRes.add(m);
                    }

                    for(Map m:qmcj_listRes){
                        for(Curriculum cc:allCurriculum){
                            for(Subject s:cc.getSubjectsByCourseCode(m.get("code").toString())){
                                s.addScore("qm",m.get("score").toString());
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;


        }

        @Override
        protected void onPostExecute(String o) {
            super.onPostExecute(o);
            taskSet.remove(this);
//            if(o!=null&&o instanceof String){
//                AlertDialog ad = new AlertDialog.Builder(getContext()).setMessage("提示").setMessage(o.toString()).setPositiveButton("好的",null).create();
//                ad.show();
//            }
            qmcj_list.setVisibility(View.VISIBLE);
            loadingView_qmcj.setVisibility(View.GONE);
            qmcj_adapter.notifyDataSetChanged();
        }
    }

   private static class CJXXListAdapter extends RecyclerView.Adapter<CJXXListAdapter.cjxxItemHolder> {

        List<Map<String,String>> mBeans;
        LayoutInflater mInflater;
        CJXXListAdapter.OnItemClickListener mOnItemClickListsner;
        interface OnItemClickListener{
            void OnClick(View view, int index, boolean choose);
        }

        public CJXXListAdapter(Context context, List<Map<String, String>> res){
            mBeans = res;
            mInflater = LayoutInflater.from(context);
        }

        public void setmOnItemClickListsner( OnItemClickListener mOnItemClickListsner) {
            this.mOnItemClickListsner = mOnItemClickListsner;
        }

        @NonNull
        @Override
        public CJXXListAdapter.cjxxItemHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View v = mInflater.inflate(R.layout.dynamic_jwts_cjxx_item,viewGroup,false);
            return new  CJXXListAdapter.cjxxItemHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull  CJXXListAdapter.cjxxItemHolder cjxxItemHolder, final int i) {
            cjxxItemHolder.name.setText(mBeans.get(i).get("name"));
            cjxxItemHolder.info.setText(mBeans.get(i).get("info"));
            cjxxItemHolder.point.setText(mBeans.get(i).get("point"));
            cjxxItemHolder.score.setText(mBeans.get(i).get("score"));
            if(mBeans.get(i).get("rank")==null) cjxxItemHolder.rank.setVisibility(View.GONE);
            else cjxxItemHolder.rank.setText(mBeans.get(i).get("rank"));
            // Log.e("!!",mBeans.get(i).toString());
            // System.out.println(mBeans.get(i));
        }

        @Override
        public int getItemCount() {
            return mBeans.size();
        }

        class cjxxItemHolder extends RecyclerView.ViewHolder {
            TextView name,info,point,score,rank;
            public cjxxItemHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.cjxx_name);
                info = itemView.findViewById(R.id.cjxx_info);
                point = itemView.findViewById(R.id.cjxx_point);
                score = itemView.findViewById(R.id.cjxx_score);
                rank = itemView.findViewById(R.id.cjxx_rank);
            }
        }
    }
}
