package com.stupidtree.hita.activities;

import android.app.SharedElementCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;
import com.stupidtree.hita.fragments.popup.FragmentTeacherContact;
import com.stupidtree.hita.util.ActivityUtils;
import com.stupidtree.hita.util.HTMLUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.stupidtree.hita.HITAApplication.TPE;

public class ActivityTeacherOfficial extends BaseActivity {


    String teacherName;
    String teacherId;
    String teacherUrl;
    ImageView teacherAvatar;
    TextView name, description, post, position, label;
    CardView avatar_card;
    SwipeRefreshLayout refresh;
    AppBarLayout appBarLayout;
    Map<String, String> teacherInfo;
    Map<String, String> teacherProfile;
    List<String> tabTitles;
    ViewPager pager;
    PagerAdapter pagerAdapter;
    TabLayout tabs;
    FloatingActionButton fab;
    NestedScrollView noneView;
    LoadTeacherPageTask pageTask1;
    LoadTeacherProfileTask pageTask2;


    @Override
    protected void stopTasks() {
        if (pageTask1 != null && pageTask1.getStatus() != AsyncTask.Status.FINISHED)
        pageTask1.cancel(true);
        if (pageTask2 != null && pageTask2.getStatus() != AsyncTask.Status.FINISHED)
            pageTask2.cancel(true);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true, true, false);
        setContentView(R.layout.activity_teacher_official);
        teacherInfo = new HashMap<>();
        teacherProfile = new HashMap<>();
        teacherId = getIntent().getStringExtra("id");
        teacherUrl = getIntent().getStringExtra("url");
        teacherName = getIntent().getStringExtra("name");
        initViews();
        Log.e("data", String.valueOf(getIntent().getData()));
        Glide.with(this).load("http://faculty.hitsz.edu.cn/file/showHP.do?d=" +
                teacherId + "&&w=200&&h=200&&prevfix=200-")
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.ic_account_activated)
                .into(teacherAvatar);
        initPager();
        initToolbar();

    }



    @Override
    protected void onResume() {
        super.onResume();
        Refresh(false, false);
    }

    void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        appBarLayout = findViewById(R.id.appbar);
        toolbar.setTitle("");
        toolbar.inflateMenu(R.menu.toolbar_teacher_official);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_open_in_browser) {
                    ActivityUtils.openInBrowser(ActivityTeacherOfficial.this, "http://faculty.hitsz.edu.cn/" + teacherUrl);
                    return true;
                }
                return false;
            }
        });
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            private float mHeadImgScale = 0;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                float scale = 1.0f + (verticalOffset) / ((float) appBarLayout.getHeight());
                teacherAvatar.setScaleX(scale);
                teacherAvatar.setScaleY(scale);
                teacherAvatar.setTranslationY(mHeadImgScale * verticalOffset);
                avatar_card.setScaleX(scale);
                avatar_card.setScaleY(scale);
                avatar_card.setTranslationY(mHeadImgScale * verticalOffset);
            }
        });
    }

    void initPager() {
        noneView = findViewById(R.id.empty_view);
        pager = findViewById(R.id.pager);
        tabs = findViewById(R.id.tabs);
        tabTitles = new ArrayList<>();
        pagerAdapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return tabTitles.size();
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }

            //设置viewpage内部东西的方法，如果viewpage内没有子空间滑动产生不了动画效果
            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                View v = getLayoutInflater().inflate(R.layout.dynamic_teacher_official_info_page, null, false);
                TextView textView = v.findViewById(R.id.text);
                textView.setText(Html.fromHtml(teacherInfo.get(tabTitles.get(position))));
                container.addView(v);
                //最后要返回的是控件本身
                return v;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }

            //目的是展示title上的文字，
            @Override
            public CharSequence getPageTitle(int position) {
                return tabTitles.get(position);
            }

        };
        pager.setAdapter(pagerAdapter);
        tabs.setupWithViewPager(pager);
    }

    void initViews() {
        fab = findViewById(R.id.fab);
        refresh = findViewById(R.id.refresh);
        name = findViewById(R.id.teacher_name);
        description = findViewById(R.id.teacher_describe);
        post = findViewById(R.id.teacher_post);
        label = findViewById(R.id.teacher_label);
        position = findViewById(R.id.teacher_position);
        teacherAvatar = findViewById(R.id.teacher_avatar);
        avatar_card = findViewById(R.id.card_avatar);
        teacherAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityUtils.startPhotoDetailActivity_transition(ActivityTeacherOfficial.this,
                        "http://faculty.hitsz.edu.cn/file/showHP.do?d=" + teacherId,
                        view
                );
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new FragmentTeacherContact(teacherProfile).show(getSupportFragmentManager(), "ftc");
            }
        });
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Refresh(true, true);
            }
        });
        refresh.setColorSchemeColors(getColorPrimary(), getColorFade());
    }

    void Refresh(boolean swipe, boolean glide) {
        name.setText(teacherName);
        if (glide) Glide.with(this).load("http://faculty.hitsz.edu.cn/file/showHP.do?d=" +
                teacherId + "&&w=200&&h=200&&prevfix=200-")
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.ic_account_activated)
                .into(teacherAvatar);
        if(pageTask1!=null&&pageTask1.getStatus()!=AsyncTask.Status.FINISHED) pageTask1.cancel(true);
        if(pageTask2!=null&&pageTask2.getStatus() != AsyncTask.Status.FINISHED) pageTask2.cancel(true);
        pageTask1 = new LoadTeacherPageTask(swipe);
        pageTask1.executeOnExecutor(TPE);
        pageTask2 = new LoadTeacherProfileTask();
        pageTask2.executeOnExecutor(TPE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_teacher_official, menu);
        return super.onCreateOptionsMenu(menu);
    }

    class LoadTeacherProfileTask extends AsyncTask {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            fab.hide();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                teacherProfile.clear();
                Document d = Jsoup.connect("http://faculty.hitsz.edu.cn/" + teacherUrl)
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                        .data("id", teacherId)
                        .get();
                String description = HTMLUtils.getStringValueByClass(d, "user-describe");
                String post = HTMLUtils.getStringValueByClass(d, "user-post");
                String label = HTMLUtils.getStringValueByClass(d, "user-label");
                String position = HTMLUtils.getStringValueByClass(d, "user-position");
                teacherProfile.put("description", description);
                teacherProfile.put("post", post);
                teacherProfile.put("label", label);
                teacherProfile.put("position", position);
                for (Element e : HTMLUtils.getElementsInClassByTag(d, "cont", "li")) {
                    String text = e.text();
                    if (text.contains("电话")) teacherProfile.put("phone", text.replaceAll("电话", ""));
                    else if (text.contains("地址"))
                        teacherProfile.put("address", text.replaceAll("地址", ""));
                    else if (text.contains("邮箱"))
                        teacherProfile.put("email", text.replaceAll("邮箱", ""));
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            fab.show();
            String pos = teacherProfile.get("post");
            if (TextUtils.isEmpty(pos)) post.setVisibility(View.GONE);
            else {
                post.setVisibility(View.VISIBLE);
                post.setText(pos);
            }
            String posi = teacherProfile.get("position");
            if (TextUtils.isEmpty(posi)) position.setVisibility(View.GONE);
            else {
                position.setVisibility(View.VISIBLE);
                position.setText(posi);
            }
            String lab = teacherProfile.get("label");
            if (TextUtils.isEmpty(lab)) label.setVisibility(View.GONE);
            else {
                label.setVisibility(View.VISIBLE);
                label.setText(lab);
            }
        }
    }

    class LoadTeacherPageTask extends AsyncTask {
        boolean swipe;

        public LoadTeacherPageTask(boolean swipe) {
            this.swipe = swipe;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (swipe) refresh.setRefreshing(true);
            teacherInfo.clear();
            tabTitles.clear();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                Document teachersPage = Jsoup.connect("http://faculty.hitsz.edu.cn/TeacherHome/teacherBody.do")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .data("id", teacherId)
                        .post();
                //System.out.println(teachersPage);
                Elements tabs = teachersPage.getElementsByAttributeValueContaining("data-class", "tab").select("li");
                for (Element e : tabs) {
                    if (!e.toString().contains("ptaben") && !e.toString().contains("pTabEn")) {
                        String id = e.attr("data-class");
                        Element part = teachersPage.getElementById(id);
                        if (part != null && part.getElementsByTag("table").size() > 0) {
                            tabTitles.add(e.text());
                            teacherInfo.put(e.text(), part.getElementsByTag("table").first().toString());
                        }
                    }
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (swipe) refresh.setRefreshing(false);
            if (teacherInfo.size() > 0) {
                noneView.setVisibility(View.GONE);
                pager.setVisibility(View.VISIBLE);
            } else {
                pager.setVisibility(View.GONE);
                noneView.setVisibility(View.VISIBLE);
            }
            pagerAdapter.notifyDataSetChanged();
        }
    }


}
