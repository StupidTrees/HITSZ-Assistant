package com.stupidtree.hita.activities;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.text.Editable;
import android.text.Layout;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.SearchResultAdapter;
import com.stupidtree.hita.diy.RevealAnimation;
import com.stupidtree.hita.diy.WrapContentLinearLayoutManager;
import com.stupidtree.hita.fragments.search.FragmentSearchResult;
import com.stupidtree.hita.fragments.search.FragmentSearchResult_library;
import com.stupidtree.hita.fragments.search.FragmentSearchResult_location;
import com.stupidtree.hita.fragments.search.FragmentSearchResult_teacher;
import com.stupidtree.hita.fragments.search.FragmentSearchResult_timetable;
import com.stupidtree.hita.fragments.search.FragmentSearchResult_web;
import com.stupidtree.hita.fragments.search.FragmentSearchResult_zsw;
import com.stupidtree.hita.online.HITAUser;
import com.stupidtree.hita.online.Location;
import com.stupidtree.hita.online.SearchItem;
import com.stupidtree.hita.online.Teacher;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.SearchResultList;
import static com.stupidtree.hita.HITAApplication.searchText;
import static com.stupidtree.hita.diy.RevealAnimation.EXTRA_CIRCULAR_REVEAL_X;
import static com.stupidtree.hita.diy.RevealAnimation.EXTRA_CIRCULAR_REVEAL_Y;

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
    List<FragmentSearchResult> fragments;
    ViewPager pager;

    TabLayout tabs;
    //View blank;


    @Override
    protected void stopTasks() {
        if (pageTask != null && pageTask.getStatus() != AsyncTask.Status.FINISHED)
            pageTask.cancel(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true, true, false);
        setContentView(R.layout.activity_search);
        initToolbar();
        rootLayout = findViewById(R.id.rootlayout);

        mRevealAnimation = new RevealAnimation(rootLayout, getIntent(), this);

        initList();
        initPager();
        onAnimateLayout(savedInstanceState, getIntent());
        searchForPurpose();
    }

    void searchForPurpose(){
        String text = getIntent().getStringExtra("keyword");
        String purpose = getIntent().getStringExtra("type");
        if(TextUtils.isEmpty(text)||TextUtils.isEmpty(purpose)) return;
        searchview.setText(text);
        setSearchText(text);
        int index = 0;
        if(purpose.equals("timetable")) index = 0;
        else if(purpose.equals("teacher")) index = 1;
        else if(purpose.equals("location")) index = 2;
        else if(purpose.equals("library")) index = 3;
        else if(purpose.equals("hitsz")) index = 4;
        else if(purpose.equals("hitzs")) index = 5;
        pager.setCurrentItem(index);
    }

    void setSearchText(String text){
        for(FragmentSearchResult fs:fragments){
            fs.setSearchText(text);
        }
    }

    void initToolbar() {
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
//        searchview.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
////                int old =  SearchResultList.size();
////                searchText = s.toString();
////                SearchResultList.clear();
////                listAdapter.notifyItemRangeRemoved(0, old);
////                if(pageTask!=null&&pageTask.getStatus()!=AsyncTask.Status.FINISHED){
////                    pageTask.cancel(true);
////                }
////                pageTask =  new getSuggestionsTask(s.toString());
////                pageTask.executeOnExecutor(HITAApplication.TPE);
////                if (!TextUtils.isEmpty(s.toString())) {
////                    if(fragments.get(pager.getCurrentItem()) instanceof FragmentSearchResult_timetable){
////                        fragments.get(pager.getCurrentItem()).setSearchText(s.toString());
////                        fragments.get(pager.getCurrentItem()).Search(true);
////                    }
////
////                }
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//
//            }
//        });
        searchview.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(TextUtils.isEmpty(textView.getText().toString())) return false;
                if (i == EditorInfo.IME_ACTION_GO || i == EditorInfo.IME_ACTION_SEARCH) {
                    /*隐藏软键盘*/
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    // 隐藏软键盘
                    imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                    setSearchText(searchview.getText().toString());
                    fragments.get(pager.getCurrentItem()).Search(true);
                    return true;
                }
                return false;
            }
        });

    }

    void initPager() {
        fragments = new ArrayList<>();
        pager = findViewById(R.id.pager);
        tabs = findViewById(R.id.tabs);
        fragments.add(new FragmentSearchResult_timetable(getString(R.string.tab_search_timetable)));
        fragments.add(new FragmentSearchResult_teacher(getString(R.string.tab_search_teacher)));
        fragments.add(new FragmentSearchResult_location(getString(R.string.tab_search_location)));
        fragments.add(new FragmentSearchResult_library(getString(R.string.tab_search_library)));
        fragments.add(new FragmentSearchResult_web(getString(R.string.tab_hitsz_website_info)));
        fragments.add(new FragmentSearchResult_zsw(getString(R.string.tab_search_zsw)));
        pager.setAdapter(new pagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT));
        tabs.setupWithViewPager(pager);
    }

    void initList() {

        list = findViewById(R.id.list);
        listAdapter = new SearchResultAdapter(this, SearchResultList);
        list.setAdapter(listAdapter);
        list.setLayoutManager(new WrapContentLinearLayoutManager(this, RecyclerView.VERTICAL, false));
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
                        mRevealAnimation.revealActivity(revealX, revealY, new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animator) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animator) {
                                popUpKeyboard();
                            }

                            @Override
                            public void onAnimationCancel(Animator animator) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animator) {

                            }
                        }, 400);
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
        if (getIntent().hasExtra(EXTRA_CIRCULAR_REVEAL_X) &&
                getIntent().hasExtra(EXTRA_CIRCULAR_REVEAL_Y)) {
            mRevealAnimation.unRevealActivity(300);
        }else super.onBackPressed();

    }

    void popUpKeyboard() {
        searchview.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
//        searchview.requestFocus();
//        searchview.requestFocusFromTouch();
//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                InputMethodManager inputMethodManager = (
//                        InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                inputMethodManager.toggleSoftInput(
//                        0, InputMethodManager.HIDE_NOT_ALWAYS);
//            }
//        }, 10);
    }


    class pagerAdapter extends FragmentPagerAdapter {

        public pagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return fragments.get(position).getTitle();
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            // super.destroyItem(container, position, object);
        }
    }

    class getSuggestionsTask extends AsyncTask {

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
            String toResearch = s.replaceAll("\n", "").replaceAll(" ", "");
            if (!TextUtils.isEmpty(toResearch)) {
                try {
                    BmobQuery<Teacher> bq2 = new BmobQuery<>();
                    bq2.addWhereContains("name", toResearch);
                    List<Teacher> foundTeachers = bq2.findObjectsSync(Teacher.class);
                    result.addAll(foundTeachers);
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        BmobQuery<Teacher> bq = new BmobQuery<>();
                        bq.addWhereEqualTo("name", toResearch);
                        List<Teacher> foundLocations = bq.findObjectsSync(Teacher.class);
                        result.addAll(foundLocations);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                try {
                    BmobQuery<Location> bq = new BmobQuery<>();
                    bq.addWhereContains("name", toResearch);
                    List<Location> foundLocations = bq.findObjectsSync(Location.class);
                    result.addAll(foundLocations);
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        BmobQuery<Location> bq = new BmobQuery<>();
                        bq.addWhereEqualTo("name", toResearch);
                        List<Location> foundLocations = bq.findObjectsSync(Location.class);
                        result.addAll(foundLocations);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
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
                listAdapter.notifyItemRangeInserted(0, result.size());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

