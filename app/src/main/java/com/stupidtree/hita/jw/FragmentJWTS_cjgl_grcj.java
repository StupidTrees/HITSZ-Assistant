package com.stupidtree.hita.jw;

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

import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.timetable.Curriculum;
import com.stupidtree.hita.timetable.Subject;
import com.stupidtree.hita.diy.WrapContentLinearLayoutManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.stupidtree.hita.HITAApplication.TPE;
import static com.stupidtree.hita.HITAApplication.jwCore;
import static com.stupidtree.hita.HITAApplication.timeTableCore;

public class FragmentJWTS_cjgl_grcj extends JWFragment {
    RecyclerView qmcj_list;
    CJXXListAdapter qmcj_adapter;
    List<String> xnxqPickerName;
    List<Map<String,String>> xnxqPickerData;

    List<Map<String,String>> qzcj_listRes,qmcj_listRes;
    Spinner xnxqPicker;
    ArrayAdapter xnxqAdapter;
    ProgressBar loadingView_qmcj;
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
        Refresh(null,null);
    }

    void initPage(View v){
        xnxqPickerName = new ArrayList<>();
        xnxqPickerData = new ArrayList<>();
        xnxqPicker = v.findViewById(R.id.xnxq_picker);
        loadingView_qmcj = v.findViewById(R.id.qmcj_loading);
        xnxqAdapter = new ArrayAdapter(getContext(),R.layout.dynamic_xnxq_spinner_item,xnxqPickerName);
        xnxqPicker.setAdapter(xnxqAdapter);
        xnxqAdapter.setDropDownViewResource(R.layout.dynamic_xnxq_spinner_dropdown_item);
    }

    void initLists(final View v){
        qmcj_list = v.findViewById(R.id.qmcj_list);
        qzcj_listRes = new ArrayList<>();
        qmcj_listRes = new ArrayList<>();
        qmcj_adapter = new CJXXListAdapter(v.getContext(),qmcj_listRes);
        qmcj_list.setAdapter(qmcj_adapter);
        LinearLayoutManager layoutManager2 = new WrapContentLinearLayoutManager(v.getContext(),RecyclerView.VERTICAL,false);

        qmcj_list.setLayoutManager(layoutManager2);
        xnxqPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Map<String,String> dt = xnxqPickerData.get(i);
                new refreshQMCJListTask(null,null,dt.get("xn"),dt.get("xq")).executeOnExecutor(TPE);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }







    @Override
    protected void stopTasks() {
        for(AsyncTask at:taskSet) if(at!=null&&at.getStatus()!=AsyncTask.Status.FINISHED) at.cancel(true);
    }

    @Override
    public void Refresh(OnRefreshStartListener start, OnRefreshFinishListener finish) {
        new refreshXNXQSpinnerTask(start,finish).executeOnExecutor(HITAApplication.TPE);
    }




    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }




    class refreshXNXQSpinnerTask extends RefreshJWPageTask{


        public refreshXNXQSpinnerTask(OnRefreshStartListener refreshStartListener, OnRefreshFinishListener refreshFinishListener) {
            super(refreshStartListener, refreshFinishListener);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                List<Map<String, String>> xnxqList = jwCore.getXNXQ();
                xnxqPickerData.clear();
                xnxqPickerData.addAll(xnxqList);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            taskSet.add(this);
            super.onPreExecute();
            qmcj_list.setVisibility(View.GONE);
            loadingView_qmcj.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            taskSet.remove(this);
            xnxqPickerName.clear();
            qmcj_list.setVisibility(View.VISIBLE);
            loadingView_qmcj.setVisibility(View.GONE);
            for (Map<String, String> item : xnxqPickerData) {
                xnxqPickerName.add(item.get("xnmc") + item.get("xqmc"));
            }
            xnxqPickerName.add(0,"全部");
            Map<String,String> all = new HashMap<>();
            all.put("xn",null);
            all.put("xq",null);
            xnxqPickerData.add(0,all);
            xnxqAdapter.notifyDataSetChanged();
            xnxqPicker.setSelection(0);
        }


    }

    class refreshQMCJListTask extends  RefreshJWPageTask{

        String xn,xq;

        public refreshQMCJListTask(OnRefreshStartListener refreshStartListener, OnRefreshFinishListener refreshFinishListener
        ,String xn,String xq
        ) {
            super(refreshStartListener, refreshFinishListener);
                this.xn = xn;
                this.xq = xq;
        }


        @Override
        protected void onPreExecute() {
            taskSet.add(this);
            super.onPreExecute();
            qmcj_list.setVisibility(View.GONE);
            loadingView_qmcj.setVisibility(View.VISIBLE);
        }

        @Override
        protected Object doInBackground(Object... strings) {
                try {
                    qmcj_listRes.clear();
                   qmcj_listRes.addAll(jwCore.getGRCJ(xn,xq));
                    for(Map m:qmcj_listRes){
                        for(Curriculum cc:timeTableCore.getAllCurriculum()){
                            for(Subject s:cc.getSubjectsByCourseCode(m.get("code").toString())){
                                s.addScore("qm",m.get("final_score").toString());
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;

        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            taskSet.remove(this);
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
            cjxxItemHolder.info.setText(mBeans.get(i).get("exam"));
            cjxxItemHolder.point.setText(mBeans.get(i).get("credit"));
            cjxxItemHolder.type.setText(mBeans.get(i).get("type"));
            cjxxItemHolder.final_score.setText(mBeans.get(i).get("final_score"));
            cjxxItemHolder.total_score.setText(mBeans.get(i).get("total_score"));
            // Log.e("!!",mBeans.get(i).toString());
            // System.out.println(mBeans.get(i));
        }

        @Override
        public int getItemCount() {
            return mBeans.size();
        }

        class cjxxItemHolder extends RecyclerView.ViewHolder {
            TextView name,info,point,final_score,total_score,type;
            public cjxxItemHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.cjxx_name);
                info = itemView.findViewById(R.id.cjxx_info);
                point = itemView.findViewById(R.id.cjxx_point);
                type = itemView.findViewById(R.id.cjxx_type);
                final_score = itemView.findViewById(R.id.cjxx_final_score);
                total_score = itemView.findViewById(R.id.cjxx_total_score);
            }
        }
    }
}
