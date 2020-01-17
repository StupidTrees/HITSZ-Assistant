//package com.stupidtree.hita.jwts;
//
//import android.content.Context;
//import android.net.Uri;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.stupidtree.hita.BaseFragment;
//import com.stupidtree.hita.HITAApplication;
//import com.stupidtree.hita.R;
//import com.stupidtree.hita.diy.WrapContentLinearLayoutManager;
//
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.jsoup.select.Elements;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import static com.stupidtree.hita.HITAApplication.HContext;
//import static com.stupidtree.hita.HITAApplication.cookies_jwts;
//
//public class FragmentJWTS_cjgl_xxjd extends BaseFragment {
//
//
//    private OnFragmentInteractionListener mListener;
//
//    TextView ms_required,ms_done,xf_required,xf_done;
//    String  ms_required_txt,ms_done_txt,xf_required_txt,xf_done_txt;
//    List<Map<String,String>> listRes;
//    xflbListAdapter listAdapter;
//    RecyclerView list;
//    ProgressBar loading;
//    refreshPageTask pageTask;
//
//
//    public FragmentJWTS_cjgl_xxjd() {
//        // Required empty public constructor
//    }
//
//
//    public static FragmentJWTS_cjgl_xxjd newInstance(String param1, String param2) {
//        FragmentJWTS_cjgl_xxjd fragment = new FragmentJWTS_cjgl_xxjd();
//
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View v = inflater.inflate(R.layout.fragment_jwts_cjgl_xxjd, container, false);
//        initViews(v);
//        initList(v);
//        Refresh();
//        return v;
//    }
//
//
//
//    void initViews(View v){
//        xf_done = v.findViewById(R.id.xxjd_xf_done);
//        xf_required = v.findViewById(R.id.xxjd_xf_required);
//        ms_done = v.findViewById(R.id.xxjd_ms_done);
//        ms_required = v.findViewById(R.id.xxjd_ms_required);
//        loading = v.findViewById(R.id.loading);
//    }
//    void initList(View v){
//        listRes = new ArrayList<>();
//        list = v.findViewById(R.id.xxjd_list);
//        listAdapter = new xflbListAdapter();
//        list.setAdapter(listAdapter);
//        list.setLayoutManager(new WrapContentLinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
//    }
//
//    @Override
//    protected void stopTasks() {
//        if(pageTask!=null&&pageTask.getStatus()!=AsyncTask.Status.FINISHED) pageTask.cancel(true);
//    }
//
//    @Override
//    public void Refresh() {
//        if(pageTask!=null&&pageTask.getStatus()!=AsyncTask.Status.FINISHED) pageTask.cancel(true);
//        pageTask = new refreshPageTask(getContext());
//        pageTask.executeOnExecutor(HITAApplication.TPE);
//    }
//
//
//    class refreshPageTask extends loadJWTSinfoTask{
//
//
//        refreshPageTask(Context context) {
//            super(context);
//        }
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            listRes.clear();
//            loading.setVisibility(View.VISIBLE);
//            list.setVisibility(View.GONE);
//        }
//
//        @Override
//        protected String doInBackground(String... strings) {
//
//            try {
//                Document page = Jsoup.connect("http://jwts.hitsz.edu.cn:8080/jdcx/queryXsjdcx").cookies(cookies_jwts).timeout(60000)
//                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
//                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
//                        .header("Content-Type", "application/x-www-form-urlencoded")
//                        .ignoreContentType(true)
//                        .get();
//                String res = super.doInBackground(page.toString());
//                if(res!=null) return res;
//                Element bx = page.getElementsByClass("addlist_01").first();
//                ms_required_txt = bx.select("tr").get(1).select("td").get(0).text();
//                 ms_done_txt  = bx.select("tr").get(1).select("td").get(1).text();
//                xf_required_txt = bx.select("tr").get(3).select("td").get(0).text();
//                xf_done_txt  = bx.select("tr").get(3).select("td").get(1).text();
//                Element xflbyq = page.getElementsByClass("addlist_01").get(1);
//                Elements xfyqs = xflbyq.select("tr");
//                for(Element e:xfyqs){
//                    if(e.toString().contains("已完成学分")) continue;
//                    Elements tds = e.select("td");
//                    Map m = new HashMap();
//                    m.put("xflb",tds.get(0).text());
//                    m.put("yqxf",tds.get(1).text());
//                    m.put("done",tds.get(2).text());
//                    m.put("tobedone",tds.get(3).text());
//                    listRes.add(m);
//                }
//                return null;
//            } catch (Exception e) {
//                return "加载学习进度失败！";
//            }
//
//        }
//
//        @Override
//        protected void onPostExecute(String o) {
//            super.onPostExecute(o);
//            loading.setVisibility(View.GONE);
//            if(o==null){
//                ms_done.setText(ms_done_txt);
//                ms_required.setText(ms_required_txt);
//                xf_done.setText(xf_done_txt);
//                xf_required.setText(xf_required_txt);
//                listAdapter.notifyDataSetChanged();
//                list.setVisibility(View.VISIBLE);
//            }else Toast.makeText(HContext,"载入成绩信息失败！",Toast.LENGTH_SHORT).show();
//
//        }
//    }
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
//
//    public interface OnFragmentInteractionListener {
//        void onFragmentInteraction(Uri uri);
//    }
//
//    private class xflbListAdapter extends RecyclerView.Adapter<xflbListAdapter.xflbViewHolder>{
//
//
//        @NonNull
//        @Override
//        public xflbViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
//            return new xflbViewHolder(getLayoutInflater().inflate(R.layout.dynamic_jwts_cjgl_xxjd_item,viewGroup,false));
//        }
//
//        @Override
//        public void onBindViewHolder(@NonNull xflbViewHolder xflbViewHolder, int i) {
//            xflbViewHolder.done.setText(listRes.get(i).get("done"));
//            xflbViewHolder.tobedone.setText(listRes.get(i).get("tobedone"));
//            xflbViewHolder.yqxf.setText(listRes.get(i).get("yqxf"));
//            xflbViewHolder.xflb.setText(listRes.get(i).get("xflb"));
//        }
//
//        @Override
//        public int getItemCount() {
//            return listRes.size();
//        }
//
//        class xflbViewHolder extends RecyclerView.ViewHolder{
//            TextView xflb,yqxf,done,tobedone;
//            public xflbViewHolder(@NonNull View itemView) {
//                super(itemView);
//                xflb = itemView.findViewById(R.id.xflb);
//                yqxf = itemView.findViewById(R.id.yqxf);
//                done = itemView.findViewById(R.id.done);
//                tobedone = itemView.findViewById(R.id.tobedone);
//            }
//        }
//    }
//}
