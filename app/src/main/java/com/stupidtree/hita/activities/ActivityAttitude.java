package com.stupidtree.hita.activities;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.AttitudeListAdapter;
import com.stupidtree.hita.diy.WrapContentLinearLayoutManager;
import com.stupidtree.hita.fragments.FragmentAddAttitude;
import com.stupidtree.hita.online.Attitude;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

public class ActivityAttitude extends BaseActivity implements FragmentAddAttitude.AttachedActivity{

    SwipeRefreshLayout refreshLayout;
    RecyclerView list;
    AttitudeListAdapter listAdapter;
    List<Attitude> listRes;
    FloatingActionButton fab;
    Toolbar toolbar;
    @Override
    protected void stopTasks() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        Refresh();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attitude);
        setWindowParams(true,true,false);
        initToolbar();
        initViews();
        initList();
    }


    void initToolbar(){

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("HITSZ态度墙");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }
    void initViews(){
        refreshLayout = findViewById(R.id.refresh);
        refreshLayout.setColorSchemeColors(getColorPrimary());
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentAddAttitude.newInstance().show(getSupportFragmentManager(),"add_attitude");
            }
        });
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Refresh();
            }
        });
    }
    void initList(){

        list = findViewById(R.id.list);
        list.setItemViewCacheSize(30);
        listRes = new ArrayList<>();
        listAdapter = new AttitudeListAdapter(this,listRes);
        list.setAdapter(listAdapter);
        list.setLayoutManager(new WrapContentLinearLayoutManager(this));

    }


    void Refresh(){
        refreshLayout.setRefreshing(true);
        listRes.clear();
        BmobQuery<Attitude> bq = new BmobQuery<>();

        bq.order("-updatedAt").findObjects(new FindListener<Attitude>() {
            @Override
            public void done(List<Attitude> listx, BmobException e) {
                refreshLayout.setRefreshing(false);
                if(e==null&&listx!=null){
                    listRes.addAll(listx);
                    listAdapter.notifyDataSetChanged();
                    list.scheduleLayoutAnimation();
                }else{
                    Toast.makeText(ActivityAttitude.this,"刷新失败",Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    @Override
    public void onFragmentCalledRefresh() {
        Refresh();
    }
}
