package com.stupidtree.hita.activities;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.stupidtree.hita.R;
import com.stupidtree.hita.fragments.BaseOperationTask;
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
import java.util.Objects;

import static com.stupidtree.hita.HITAApplication.TPE;

public class ActivityTeacherOfficial extends BaseActivity implements BaseOperationTask.OperationListener {


    boolean isFirst = true;
    String teacherName;
    String teacherId;
    String teacherUrl;
    ImageView teacherAvatar;
    TextView name, post, position, label;
    CardView avatar_card;
    AppBarLayout appBarLayout;
    Map<String, String> teacherInfo;
    Map<String, String> teacherProfile;
    List<String> tabTitles;
    ViewPager pager;
    PagerAdapter pagerAdapter;
    TabLayout tabs;
    ExtendedFloatingActionButton fab;
    NestedScrollView noneView;
    LoadTeacherPageTask pageTask1;
    LoadTeacherProfileTask pageTask2;
    SwipeRefreshLayout refresh;


    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        // Log.e("data", String.valueOf(getIntent().getData()));

        initPager();
        initToolbar();

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (isFirst) {
            Refresh();
            isFirst = false;
        }
    }

    void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        appBarLayout = findViewById(R.id.appbar);
        toolbar.setTitle("");
        toolbar.inflateMenu(R.menu.toolbar_teacher_official);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
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

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                float scale = 1.0f + (verticalOffset) / ((float) appBarLayout.getHeight());
                teacherAvatar.setScaleX(scale);
                teacherAvatar.setScaleY(scale);
                float mHeadImgScale = 0;
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
            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                @SuppressLint("InflateParams") View v = getLayoutInflater().inflate(R.layout.dynamic_teacher_official_info_page, null, false);
                TextView textView = v.findViewById(R.id.text);
                textView.setText(Html.fromHtml(teacherInfo.get(tabTitles.get(position))));
                container.addView(v);
                //最后要返回的是控件本身
                return v;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
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

    @SuppressLint("ResourceType")
    void initViews() {
        refresh = findViewById(R.id.refresh);
        fab = findViewById(R.id.fab);
        name = findViewById(R.id.teacher_name);
        post = findViewById(R.id.teacher_post);
        label = findViewById(R.id.teacher_label);
        position = findViewById(R.id.teacher_position);
        teacherAvatar = findViewById(R.id.teacher_avatar);
        avatar_card = findViewById(R.id.card_avatar);
        fab.setBackgroundColor(getColorAccent());
        //fab.hide();
        fab.setVisibility(View.INVISIBLE);
        fab.setHideMotionSpecResource(R.anim.fab_scale_hide);
        fab.setShowMotionSpecResource(R.anim.fab_scale_show);
        teacherAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityUtils.showOneImage(getThis(), "http://faculty.hitsz.edu.cn/file/showHP.do?d=" + teacherId);

            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTeacherContact.newInstance(teacherProfile).show(getSupportFragmentManager(), "ftc");
            }
        });
        refresh.setColorSchemeColors(getColorAccent());
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Refresh();
            }
        });
    }

    void Refresh() {
        name.setText(teacherName);
        Glide.with(this).load("http://faculty.hitsz.edu.cn/file/showHP.do?d=" +
                teacherId + "&&w=200&&h=200&&prevfix=200-")
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.ic_account_activated)
                .into(teacherAvatar);
        if (pageTask1 != null && pageTask1.getStatus() != AsyncTask.Status.FINISHED)
            pageTask1.cancel(true);
        if (pageTask2 != null && pageTask2.getStatus() != AsyncTask.Status.FINISHED)
            pageTask2.cancel(true);
        pageTask1 = new LoadTeacherPageTask(this,teacherId);
        pageTask1.executeOnExecutor(TPE);
        pageTask2 = new LoadTeacherProfileTask(this,teacherId,teacherUrl);
        pageTask2.executeOnExecutor(TPE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_teacher_official, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onOperationStart(String id, Boolean[] params) {
        if(id.equals("page")){
            fab.hide();
            refresh.setRefreshing(true);
        }
    }

    @Override
    public void onOperationDone(String id, BaseOperationTask task, Boolean[] params, Object result) {
        switch (id){
            case "page":
                LoadTeacherPageTask ltpt = (LoadTeacherPageTask) task;
                teacherInfo.clear();
                tabTitles.clear();
                teacherInfo.putAll(ltpt.infoToAdd);
                tabTitles.addAll(ltpt.titleToAdd);
                if (teacherInfo.size() > 0) {
                    noneView.setVisibility(View.GONE);
                    pager.setVisibility(View.VISIBLE);
                } else {
                    pager.setVisibility(View.GONE);
                    noneView.setVisibility(View.VISIBLE);
                }
                pagerAdapter.notifyDataSetChanged();
                pager.scheduleLayoutAnimation();
                refresh.setRefreshing(false);
                fab.show();
                break;
            case "profile":
                LoadTeacherProfileTask pt = (LoadTeacherProfileTask) task;
                teacherProfile.clear();
                teacherProfile.putAll(pt.teacherProfile);
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
                break;
        }

    }

    static class LoadTeacherProfileTask extends BaseOperationTask<Object> {

        Map<String,String> teacherProfile;
        String teacherId;
        String teacherUrl;
        LoadTeacherProfileTask(OperationListener listRefreshedListener, String teacherId,String teacherUrl) {
            super(listRefreshedListener);
            id = "profile";
            this.teacherUrl = teacherUrl;
            this.teacherId = teacherId;
            teacherProfile = new HashMap<>();
        }


        @Override
        protected Object doInBackground(OperationListener<Object> listRefreshedListener, Boolean... booleans) {
            try {
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



    }

    static class LoadTeacherPageTask extends BaseOperationTask<Object> {
        List<String> titleToAdd;
        HashMap<String, String> infoToAdd;

        String teacherId;

        LoadTeacherPageTask(OperationListener listRefreshedListener, String teacherId) {
            super(listRefreshedListener);
            this.teacherId = teacherId;
            titleToAdd = new ArrayList<>();
            infoToAdd = new HashMap<>();
            id = "page";
        }



        @Override
        protected Object doInBackground(OperationListener<Object> listRefreshedListener, Boolean... booleans) {
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
                            titleToAdd.add(e.text());
                            infoToAdd.put(e.text(), part.getElementsByTag("table").first().toString());
                        }
                    }
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

        }

    }


}
