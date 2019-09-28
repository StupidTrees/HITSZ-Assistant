package com.stupidtree.hita.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.R;

import java.util.ArrayList;
import java.util.List;


public class FragmentFDY extends BaseFragment {
    JsonObject fdyInfo;
    TextView name,phone,office;
    RecyclerView qqList;
    List<JsonObject> listRes;
    qqListAdapter listAdapter;

    public FragmentFDY() {
        // Required empty public constructor
    }

    public static FragmentFDY newInstance(JsonObject info) {
        FragmentFDY fragment = new FragmentFDY();
        Bundle args = new Bundle();
        args.putString("info", String.valueOf(info));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
           fdyInfo = new JsonParser().parse(getArguments().get("info").toString()).getAsJsonObject();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_fdy, container, false);
        initViews(v);
        return v;
    }


    @Override
    public void onResume() {
        super.onResume();
        Refresh();
    }

    void initViews(View v){
        listRes = new ArrayList<>();
        phone = v.findViewById(R.id.phone);
        name = v.findViewById(R.id.name);
        office = v.findViewById(R.id.office);
        qqList = v.findViewById(R.id.qq_list);
        listAdapter = new qqListAdapter();
        qqList.setAdapter(listAdapter);
        qqList.setLayoutManager(new LinearLayoutManager(getContext(),RecyclerView.VERTICAL,false));
    }


    @Override
    protected void stopTasks() {

    }



    @Override
    public void Refresh() {
        if(fdyInfo!=null){
            name.setText(fdyInfo.get("name").getAsString());
            phone.setText(fdyInfo.get("phone").getAsString());
            office.setText(fdyInfo.get("office").getAsString());
        }
        listRes.clear();
        for(JsonElement je:fdyInfo.get("zy").getAsJsonArray()){
            listRes.add(je.getAsJsonObject());
        }
        listAdapter.notifyDataSetChanged();
    }

    class qqListAdapter extends RecyclerView.Adapter<qqListAdapter.holder>{

        @NonNull
        @Override
        public holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.dynamic_fdy_qq,parent,false);
            return new holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull holder holder, int position) {
            holder.qqGroup.setText(listRes.get(position).get("qq").getAsString());
            holder.name.setText(listRes.get(position).get("name").getAsString());
        }

        @Override
        public int getItemCount() {
            return listRes.size();
        }

        class holder extends RecyclerView.ViewHolder{
            TextView name;
            TextView qqGroup;
            public holder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.name);
                qqGroup = itemView.findViewById(R.id.qq);
            }
        }
    }
}
