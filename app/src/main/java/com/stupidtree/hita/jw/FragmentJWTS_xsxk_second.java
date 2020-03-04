package com.stupidtree.hita.jw;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.XSXKListAdapter;
import com.stupidtree.hita.util.JsonUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.stupidtree.hita.HITAApplication.TPE;
import static com.stupidtree.hita.HITAApplication.jwCore;
import static com.stupidtree.hita.HITAApplication.now;

public class FragmentJWTS_xsxk_second extends JWFragment implements FragmentJW_xk_popup.XKPageSecond {
    RecyclerView list;
    XSXKListAdapter listAdapter;
    List<Map<String, String>> lisRes;
    List<Map<String,String>> listResFull;
    JsonObject pageInfo;
    TextView notification;
    FragmentJW_xk_popup.XKPageRoot xkPageRoot;
    protected String type;
    protected String title;
    protected boolean willRefreshOnResume;


    public FragmentJWTS_xsxk_second(FragmentJW_xk_popup.XKPageRoot xkPageRoot,String type, String title) {
        this.type = type;
        this.xkPageRoot = xkPageRoot;
        this.title = title;
    }

    public FragmentJWTS_xsxk_second() {
        // Required empty public constructor
    }


    public static BaseFragment newInstance() {
        FragmentJWTS_xsxk_second fragment = new FragmentJWTS_xsxk_second();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_jw_xk_second, container, false);
        initPage(v);
        initList(v);
        super.initRefresh(v);
        return v;
    }

    void initPage(View v) {
       notification = v.findViewById(R.id.xsxk_notification);
    }

    void initList(final View v) {
        list = v.findViewById(R.id.xsxk_list);
        lisRes = new ArrayList<>();
        listResFull = new ArrayList<>();
        listAdapter = new XSXKListAdapter(v.getContext(), lisRes);
        list.setAdapter(listAdapter);
        list.setLayoutManager(new LinearLayoutManager(getActivity()));
        listAdapter.setOnItemClickListener(new XSXKListAdapter.OnItemClickListener() {
            @Override
            public void OnClick(View view,int position) {
//                String id = listResFull.get(position).get("kcid");
//                Log.e("id++++","id是："+id);
//                ActivityUtils.startJWSubjectActivity(getBaseActivity(),id);
                new FragmentJW_xk_popup(FragmentJWTS_xsxk_second.this,"xk",listResFull.get(position-1)).show(getBaseActivity().getSupportFragmentManager(),"xk");
            }
        });
    }





    @Override
    protected void stopTasks() {

    }


    public void setWillRefreshOnResume(boolean willRefreshOnResume) {
        this.willRefreshOnResume = willRefreshOnResume;
    }

    @Override
    public void Refresh() {
        new refreshListTask(type,xkPageRoot.getXn(),xkPageRoot.getXq(),xkPageRoot.getFilterNoVacancy(),
                xkPageRoot.getFilterConflict()).executeOnExecutor(TPE);
    }


    @Override
    public void onResume() {
        super.onResume();
        if(willRefreshOnResume) {
            Refresh();
            willRefreshOnResume = false;
        }
    }

    @Override
    public String getTitle() {
        return title;
    }


    @Override
    public String getSubjectType() {
        return type;
    }

    @Override
    public JsonObject getPageInfo() {
        return pageInfo;
    }

    @Override
    public boolean canXKNow() {
        try {
            if(pageInfo!=null&&pageInfo.has("xkgzszOne")){
                JsonObject pageDetail = pageInfo.get("xkgzszOne").getAsJsonObject();
                String begin = JsonUtils.getStringInfo(pageDetail,"ksrq");
                String end = JsonUtils.getStringInfo(pageDetail,"jsrq");
                Date beginD = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(begin);
                Date endD = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(end);
                return beginD.before(now.getTime())&&endD.after(now.getTime());
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public JWRoot getJWRoot() {
        return jwRoot;
    }

    @Override
    public FragmentJW_xk_popup.XKPageRoot getXKPageRoot() {
        return xkPageRoot;
    }


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }


    class refreshListTask extends RefreshJWPageTask {
        String xklb;
        String xn;
        String xq;
        Boolean hasButton;
        boolean filter_novacancy,filter_conflict;

        public refreshListTask(String xklb, String xn, String xq, boolean filter_novacancy, boolean filter_conflict) {
            this.xklb = xklb;
            this.xn = xn;
            this.xq = xq;
            this.filter_novacancy = filter_novacancy;
            this.filter_conflict = filter_conflict;
        }
//
//        refreshListTask(String xklb, String xn, String xq) {
//            this.xklb = xklb;
//            this.xn = xn;
//            this.xq = xq;
//        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            lisRes.clear();
            list.setVisibility(View.INVISIBLE);
            hasButton = false;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            String toReturn = null;
            try {
                lisRes.clear();
                listResFull.clear();
                List<Map<String,String>> res = jwCore.getXKList(xn,xq,xklb,filter_novacancy,filter_conflict);
                if(res.size()>0&&(res.get(0).get("header")!=null&&res.get(0).get("header").equals("true"))){
                    Map<String,String> header = res.get(0);
                    try {
                        pageInfo = new JsonParser().parse(header.get("page")).getAsJsonObject();

                    } catch (Exception e) {
                        pageInfo = null;
                    }
                    if(pageInfo!=null&&pageInfo.has("xkgzszOne")){
                        JsonObject pageDetail = pageInfo.get("xkgzszOne").getAsJsonObject();
                        String begin = JsonUtils.getStringInfo(pageDetail,"ksrq");
                        String end = JsonUtils.getStringInfo(pageDetail,"jsrq");
                        Map<String,String> headerTime = new HashMap<>();
                        headerTime.put("header","true");
                        if(!TextUtils.isEmpty(begin)){
                            headerTime.put("begin",begin);
                        }
                        if(!TextUtils.isEmpty(end)){
                           headerTime.put("end",end);
                        }
                        if(headerTime.size()>0){
                            lisRes.add(headerTime);
                        }
                        //Log.e("header", String.valueOf(headerTime));
                    }
                    res.remove(header);
//                    String begin = header.get("begin");
//                    String end = header.get("end");

                }
                listResFull.addAll(res);
                for(Map<String,String> m:listResFull){
                    if(m.get("header")!=null) continue;
                    Map<String, String> mToShow = new HashMap<>();
                    mToShow.put("name",m.get("kcmc"));
                    mToShow.put("type",m.get("kcxzmc"));
                    mToShow.put("xs",m.get("zxs")+"学时");
                    mToShow.put("credit",m.get("xf")+"学分");
                    lisRes.add(mToShow);
                }
               // Log.e("map-", String.valueOf(keyToTitle));
            } catch (Exception e) {
                toReturn = null;
                e.printStackTrace();
            }
            return toReturn;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (o != null) {
                notification.setVisibility(View.VISIBLE);
                notification.setText(o.toString());
            } else notification.setVisibility(View.GONE);
            list.setVisibility(View.VISIBLE);
            listAdapter.notifyDataSetChanged();
            list.scheduleLayoutAnimation();
        }
    }

}
