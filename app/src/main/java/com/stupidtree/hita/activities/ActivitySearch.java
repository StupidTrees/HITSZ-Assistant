package com.stupidtree.hita.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.Layout;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.SearchResultAdapter;
import com.stupidtree.hita.diy.RevealAnimation;
import com.stupidtree.hita.diy.WrapContentLinearLayoutManager;
import com.stupidtree.hita.online.Location;
import com.stupidtree.hita.online.Teacher;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

import static com.stupidtree.hita.HITAApplication.SearchResultList;
import static com.stupidtree.hita.HITAApplication.searchText;
import static com.stupidtree.hita.activities.ActivityChatbot.EXTRA_CIRCULAR_REVEAL_X;
import static com.stupidtree.hita.activities.ActivityChatbot.EXTRA_CIRCULAR_REVEAL_Y;

public class ActivitySearch extends BaseActivity {

    ViewGroup rootLayout;
    private RevealAnimation mRevealAnimation;
    private int revealX;
    private int revealY;
    EditText searchview;
    Toolbar toolbar;
    RecyclerView list;
    SearchResultAdapter listAdapter;
    ProgressBar loading;
    getSuggestionsTask pageTask;
    View blank;


    @Override
    protected void stopTasks() {
        if(pageTask!=null&&pageTask.getStatus()!=AsyncTask.Status.FINISHED) pageTask.cancel(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true, true, false);
        setContentView(R.layout.activity_search);
        initToolbar();
        rootLayout = findViewById(R.id.rootlayout);
        blank = findViewById(R.id.blank);
        blank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mRevealAnimation = new RevealAnimation(rootLayout, getIntent(), this);

        initList();

        onAnimateLayout(savedInstanceState, getIntent());
    }

    void initToolbar(){
        loading = findViewById(R.id.loading);
        loading.setVisibility(View.GONE);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        searchview = findViewById(R.id.searchview);
        searchview.setText(searchText);
        searchview.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int old =  SearchResultList.size();
                searchText = s.toString();
                SearchResultList.clear();
                listAdapter.notifyItemRangeRemoved(0, old);
                if(pageTask!=null&&pageTask.getStatus()!=AsyncTask.Status.FINISHED){
                    pageTask.cancel(true);
                }
                pageTask =  new getSuggestionsTask(s.toString());
                pageTask.executeOnExecutor(HITAApplication.TPE);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    void initList(){

        list = findViewById(R.id.list);
        listAdapter = new SearchResultAdapter(this, SearchResultList);
        list.setAdapter(listAdapter);
        list.setLayoutManager(new WrapContentLinearLayoutManager(this,RecyclerView.VERTICAL,false));
    }

    private void onAnimateLayout(Bundle savedInstanceState, Intent intent) {
        if (savedInstanceState == null && intent.hasExtra(EXTRA_CIRCULAR_REVEAL_X) && intent.hasExtra(EXTRA_CIRCULAR_REVEAL_Y)) {
            rootLayout.setVisibility(View.INVISIBLE);

            revealX = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_X, 0);
            revealY = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_Y, 0);

            ViewTreeObserver viewTreeObserver = rootLayout.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mRevealAnimation.revealActivity(revealX, revealY, null,300);
                        rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
            }
        } else {
            rootLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        mRevealAnimation.unRevealActivity(300);
    }

    @Override
    protected void onResume() {
        super.onResume();
        searchview.requestFocus();
        searchview.requestFocusFromTouch();
        //searchView.requestFocus();
//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//
//                InputMethodManager inputMethodManager = (
//                        InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                inputMethodManager.toggleSoftInput(
//                        0, InputMethodManager.HIDE_NOT_ALWAYS);
//            }
//        }, 295);

    }

    class getSuggestionsTask extends AsyncTask{

        String s;

        public getSuggestionsTask(String s) {
            this.s = s;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading.setVisibility(View.VISIBLE);
        }

        @Override
        protected Object doInBackground(Object[] objects) {

            List<BmobObject> result = new ArrayList<>();
            String toResearch = s.replaceAll("\n","").replaceAll(" ","");
            if(!TextUtils.isEmpty(toResearch)){
                try {
                    BmobQuery<Teacher> bq2 = new BmobQuery<>();
                    bq2.addWhereContains("name",toResearch);
                    List<Teacher> foundTeachers = bq2.findObjectsSync(Teacher.class);
                    result.addAll(foundTeachers);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    BmobQuery<Location> bq = new BmobQuery<>();
                    bq.addWhereContains("name",toResearch);
                    List<Location> foundLocations = bq.findObjectsSync(Location.class);
                    result.addAll(foundLocations);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            return result;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            List<BmobObject> result = (List<BmobObject>) o;
            try {
                loading.setVisibility(View.GONE);
                SearchResultList.addAll(result);
                listAdapter.notifyItemRangeInserted(0,result.size());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

