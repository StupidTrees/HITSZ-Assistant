package com.stupidtree.hita.activities;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.BaseTabAdapter;
import com.stupidtree.hita.fragments.search.FragmentSearchResult;
import com.stupidtree.hita.fragments.search.FragmentSearchResult_library;
import com.stupidtree.hita.fragments.search.FragmentSearchResult_location;
import com.stupidtree.hita.fragments.search.FragmentSearchResult_teacher;
import com.stupidtree.hita.fragments.search.FragmentSearchResult_timetable;
import com.stupidtree.hita.fragments.search.FragmentSearchResult_user;
import com.stupidtree.hita.fragments.search.FragmentSearchResult_web;
import com.stupidtree.hita.fragments.search.FragmentSearchResult_zsw;
import com.stupidtree.hita.views.RevealAnimation;

import java.util.Objects;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.views.RevealAnimation.EXTRA_CIRCULAR_REVEAL_X;
import static com.stupidtree.hita.views.RevealAnimation.EXTRA_CIRCULAR_REVEAL_Y;

public class ActivitySearch extends BaseActivity implements TransparentActivity, FragmentSearchResult.SearchRoot {

    ViewGroup rootLayout;
    private RevealAnimation mRevealAnimation;
    private int revealX;
    private int revealY;
    EditText searchView;
    Toolbar toolbar;
    boolean animateIn = false;
    boolean firstIn = true;
    ViewPager pager;
    SearchPagerAdapter pagerAdapter;

    TabLayout tabs;
    //View blank;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true, true, false);
        setContentView(R.layout.activity_search);
        //getWindow().getDecorView().setBackgroundColor(Color.TRANSPARENT);
        initToolbar();
        initPager();
        firstIn = true;
        rootLayout = findViewById(R.id.rootlayout);
        mRevealAnimation = new RevealAnimation(rootLayout, getIntent(), this);
        // initList();

        if (animateIn = getIntent().getBooleanExtra("anim", false)) {
            onAnimateLayout(savedInstanceState, getIntent());
        }
        searchForPurpose();

    }


    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        if (!animateIn && firstIn && !getIntent().hasExtra("type")) {
            firstIn = false;
            popUpKeyboard();
        }
    }

    void searchForPurpose() {
        String text = getIntent().getStringExtra("keyword");
        searchView.setText(text);
        setSearchText(text);
        String purpose = getIntent().getStringExtra("type");
        if (TextUtils.isEmpty(text) || TextUtils.isEmpty(purpose)) return;
        int index = 0;
        switch (purpose) {
            case "timetable":
                index = 0;
                break;
            case "teacher":
                index = 1;
                break;
            case "user":
                index = 2;
                break;
            case "location":
                index = 3;
                break;
            case "library":
                index = 4;
                break;
            case "hitsz":
                index = 5;
                break;
            case "hitzs":
                index = 6;
                break;
        }
        pager.setCurrentItem(index);
    }

    void initToolbar() {

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        searchView = findViewById(R.id.searchview);
        searchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (TextUtils.isEmpty(textView.getText().toString())) return false;
                if (i == EditorInfo.IME_ACTION_GO || i == EditorInfo.IME_ACTION_SEARCH) {
                    /*隐藏软键盘*/
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    // 隐藏软键盘
                    if (imm != null)
                        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                    setSearchText(searchView.getText().toString());

                    if (pagerAdapter.getCurrentFragment() instanceof FragmentSearchResult) {
                        ((FragmentSearchResult) pagerAdapter.getCurrentFragment()).Search(true, true);
                    }

                    return true;
                }
                return false;
            }
        });

    }

    void initPager() {
        pager = findViewById(R.id.pager);
        tabs = findViewById(R.id.tabs);
        pagerAdapter = new SearchPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        tabs.setupWithViewPager(pager);
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
                        }, 300);
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
        if (animateIn) {
            mRevealAnimation.unRevealActivity(300);
        } else super.onBackPressed();
    }

    void popUpKeyboard() {
        searchView.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public String getSearchText() {
        if (searchView == null) return "";
        return searchView.getText().toString();
    }

    void setSearchText(String text) {
        for (Fragment f : getSupportFragmentManager().getFragments()) {
            if (f instanceof FragmentSearchResult) {
                ((FragmentSearchResult) f).setSearchText(text);
            }
        }

    }

    static class SearchPagerAdapter extends BaseTabAdapter {

        int[] titles;

        SearchPagerAdapter(@NonNull FragmentManager fm) {
            super(fm, 7);
            titles = new int[]{
                    R.string.tab_search_timetable,
                    R.string.tab_search_teacher,
                    R.string.tab_search_user,
                    R.string.tab_search_location,
                    R.string.tab_search_library,
                    R.string.tab_hitsz_website_info,
                    R.string.tab_search_zsw
            };
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return HContext.getString(titles[position]);
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            //super.destroyItem(container, position, object);
            mFragments[position] = null;
        }

        @Override
        protected Fragment initItem(int position) {
            switch (position) {
                case 0:
                    return FragmentSearchResult_timetable.newInstance();
                case 1:
                    return FragmentSearchResult_teacher.newInstance();
                case 2:
                    return FragmentSearchResult_user.newInstance();
                case 3:
                    return FragmentSearchResult_location.newInstance();
                case 4:
                    return FragmentSearchResult_library.newInstance();
                case 5:
                    return FragmentSearchResult_web.newInstance();
                default:
                    return FragmentSearchResult_zsw.newInstance();
            }
        }

    }


}

