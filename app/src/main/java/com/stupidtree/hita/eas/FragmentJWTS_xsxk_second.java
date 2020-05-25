package com.stupidtree.hita.eas;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.XSXKListAdapter;
import com.stupidtree.hita.fragments.BasicRefreshTask;
import com.stupidtree.hita.timetable.TimetableCore;
import com.stupidtree.hita.util.JsonUtils;
import com.stupidtree.hita.views.WrapContentLinearLayoutManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.stupidtree.hita.HITAApplication.TPE;
import static com.stupidtree.hita.HITAApplication.jwCore;



public class FragmentJWTS_xsxk_second extends JWFragment
        implements FragmentJW_xk_popup.XKPageSecond, BasicRefreshTask.ListRefreshedListener2<Pair<List<Map<String, String>>, List<Map<String, String>>>> {
    RecyclerView list;
    XSXKListAdapter listAdapter;
    List<Map<String, String>> listRes;
    List<Map<String,String>> listResFull;
    JsonObject pageInfo;
    TextView notification;
    FragmentJW_xk_popup.XKPageRoot xkPageRoot;
    protected String type;
    protected boolean willRefreshOnResume;


    public static FragmentJWTS_xsxk_second newInstance(String type, int title) {
        FragmentJWTS_xsxk_second fragment = new FragmentJWTS_xsxk_second();
        Bundle args = new Bundle();
        args.putString("type", type);
        args.putInt("title", title);
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentJWTS_xsxk_second() {
        // Required empty public constructor
    }

    void setXkPageRoot(FragmentJW_xk_popup.XKPageRoot root) {
        this.xkPageRoot = root;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getString("type");
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_jw_xk_second;
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        initList(v);
        initRefresh(v);
    }


    void initList(final View v) {
        notification = v.findViewById(R.id.xsxk_notification);
        list = v.findViewById(R.id.xsxk_list);
        listRes = new ArrayList<>();
        listResFull = new ArrayList<>();
        listAdapter = new XSXKListAdapter(requireContext(), listRes, false);
        list.setAdapter(listAdapter);
        list.setLayoutManager(new WrapContentLinearLayoutManager(getActivity()));
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
        if (xkPageRoot != null) new refreshListTask(this,
                type, xkPageRoot.getXn(), xkPageRoot.getXq(), xkPageRoot.getFilterNoVacancy(),
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
    public int getTitle() {
        assert getArguments() != null;
        return getArguments().getInt("title");
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
                Date beginD = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault()).parse(begin);
                Date endD = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault()).parse(end);
                if (beginD == null || endD == null) return false;
                return beginD.before(TimetableCore.getNow().getTime()) && endD.after(TimetableCore.getNow().getTime());
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

    @Override
    public void onRefreshStart(String id, Boolean[] params) {
        list.setVisibility(View.INVISIBLE);
        refresh.setRefreshing(true);
    }

    @Override
    public void onListRefreshed(String id, Boolean[] params, Object result) {

    }

    @Override
    public void onListRefreshed(String id, Boolean[] params, Pair<List<Map<String, String>>, List<Map<String, String>>> result, Object[] others) {
        String message = null;
        listRes.clear();
        listResFull.clear();
        listRes.addAll(result.second);
        listResFull.addAll(result.first);
        refresh.setRefreshing(false);
        list.setVisibility(View.VISIBLE);
        listAdapter.notifyDataSetChanged();
        list.scheduleLayoutAnimation();
        if (params.length > 1) message = (String) others[1];
        if (params.length > 0) pageInfo = (JsonObject) others[0];
        if (message != null) {
            notification.setVisibility(View.VISIBLE);
            notification.setText(message);
        } else notification.setVisibility(View.GONE);
    }


    static class refreshListTask extends BasicRefreshTask<Pair<List<Map<String, String>>, List<Map<String, String>>>> {
        String xklb;
        String xn;
        String xq;
        boolean filter_novacancy,filter_conflict;


        refreshListTask(ListRefreshedListener listRefreshedListener, String xklb, String xn, String xq, boolean filter_novacancy, boolean filter_conflict) {
            super(listRefreshedListener);
            this.xklb = xklb;
            this.xn = xn;
            this.xq = xq;
            this.filter_novacancy = filter_novacancy;
            this.filter_conflict = filter_conflict;
        }


        @Override
        protected Pair<List<Map<String, String>>, List<Map<String, String>>> doInBackground(ListRefreshedListener listRefreshedListener, Boolean... booleans) {
            JsonObject pageInfo = null;
            List<Map<String, String>> listResFull = new ArrayList<>();
            List<Map<String, String>> listRes = new ArrayList<>();
            try {
                List<Map<String,String>> res = jwCore.getXKList(xn,xq,xklb,filter_novacancy,filter_conflict);
                if (res.size() > 0 && (res.get(0).get("header") != null && Objects.equals(res.get(0).get("header"), "true"))) {
                    Map<String,String> header = res.get(0);
                    try {
                        pageInfo = new JsonParser().parse(Objects.requireNonNull(header.get("page"))).getAsJsonObject();
                    } catch (Exception ignored) {
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
                            headerTime.put("end", end);
                        }
                        if(headerTime.size()>0){
                            listRes.add(headerTime);
                        }
                    }
                    res.remove(header);

                }
                listResFull.addAll(res);
                for(Map<String,String> m:listResFull){
                    if(m.get("header")!=null) continue;
                    Map<String, String> mToShow = new HashMap<>();
                    mToShow.put("name",m.get("kcmc"));
                    mToShow.put("type",m.get("kcxzmc"));
                    mToShow.put("xs",m.get("zxs")+"学时");
                    mToShow.put("credit",m.get("xf")+"学分");
                    listRes.add(mToShow);
                }
                // Log.e("map-", String.valueOf(keyToTitle));
            } catch (Exception e) {
                e.printStackTrace();
            }
            others = new Object[]{pageInfo};
            return new Pair<>(listResFull, listRes);
        }


    }

}
