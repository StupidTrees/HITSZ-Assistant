package com.stupidtree.hita.jwts;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.UserInfosAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SuppressLint("ValidFragment")
public class FragmentJWTS_info extends Fragment {

    List<Map.Entry> listRes;
    RecyclerView list;
    UserInfosAdapter listAdapter;
    HashMap<String,String> userInfos;

    @SuppressLint("ValidFragment")
    public FragmentJWTS_info(HashMap infos){
        userInfos = infos;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    void initList(View v){
        list = v.findViewById(R.id.rec_user_center_main);
        listRes = new ArrayList<>();
        for(Map.Entry x:userInfos.entrySet()){
            if(x.getValue().equals("")||x.getValue()==null) continue;
            listRes.add(x);
        }
        for(Map.Entry x:userInfos.entrySet()){
            if(x.getValue().equals("")||x.getValue()==null){
                listRes.add(x);
            }
        }
        listAdapter = new UserInfosAdapter(listRes, (BaseActivity) this.getActivity());
        list.setAdapter(listAdapter);
        RecyclerView.LayoutManager lm = new GridLayoutManager(this.getContext(),2);
        list.setLayoutManager(lm);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_jwts_info, container, false);
        initList(view);
        return view;
    }

    public interface OnListFragmentInteractionListener {

    }
}
