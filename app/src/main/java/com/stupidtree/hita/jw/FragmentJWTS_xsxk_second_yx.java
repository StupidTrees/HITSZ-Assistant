package com.stupidtree.hita.jw;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.XSXKListAdapter;
import com.stupidtree.hita.util.ActivityUtils;
import com.stupidtree.hita.util.JsonUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.stupidtree.hita.HITAApplication.TPE;
import static com.stupidtree.hita.HITAApplication.jwCore;

public class FragmentJWTS_xsxk_second_yx extends FragmentJWTS_xsxk_second{


    public FragmentJWTS_xsxk_second_yx(FragmentJW_xk_popup.XKPageRoot xkPageRoot,String title) {
        super(xkPageRoot,"yxkc",title);
    }

    public FragmentJWTS_xsxk_second_yx() {
        // Required empty public constructor
    }


    public static BaseFragment newInstance() {
        FragmentJWTS_xsxk_second_yx fragment = new FragmentJWTS_xsxk_second_yx();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
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
                new FragmentJW_xk_popup(FragmentJWTS_xsxk_second_yx.this,"tk",listResFull.get(position)).show(getBaseActivity().getSupportFragmentManager(),"xk");
            }
        });
    }





    @Override
    protected void stopTasks() {

    }


    @Override
    public void Refresh() {
        new refreshListTask(xkPageRoot.getXn(),xkPageRoot.getXq()).executeOnExecutor(TPE);
    }


    @Override
    public String getTitle() {
        return title;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }


    class refreshListTask extends RefreshJWPageTask {
        String xn;
        String xq;
        Boolean hasButton;

        refreshListTask(String xn,String xq) {
            this.xn = xn;
            this.xq = xq;
        }


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
                List<Map<String,String>> res =jwCore.getYXList(xn,xq);

                if(res.size()>0&&(res.get(0).get("header")!=null&&res.get(0).get("header").equals("true"))){
                    Map<String,String> header = res.get(0);
                    try {
                        pageInfo = new JsonParser().parse(header.get("page")).getAsJsonObject();
                    } catch (Exception e) {
                        pageInfo = null;
                    }
                    res.remove(header);

//                    String begin = header.get("begin");
//                    String end = header.get("end");
//                    res.remove(header);
                }
                listResFull.addAll(res);
                for(Map<String,String> m:listResFull){
                    Map<String, String> mToShow = new HashMap<>();
                    mToShow.put("name",m.get("kcmc"));
                    mToShow.put("type",m.get("kcxzmc"));
                    mToShow.put("xs",m.get("xs")+"学时");
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
