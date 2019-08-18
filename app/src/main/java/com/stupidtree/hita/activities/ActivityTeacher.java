package com.stupidtree.hita.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.android.material.appbar.AppBarLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.text.TextUtils;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;
import com.stupidtree.hita.online.Teacher;
import com.stupidtree.hita.util.ActivityUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class ActivityTeacher extends BaseActivity {


    LoadTeacherPageTask pageTask;
    String teacherName;
    ImageView teacherAvatar;
    TextView name, school, gender, title, code, detail, phone, email;
    Teacher teacher;
    CardView avatar_card;
    ProgressBar refresh;
    AppBarLayout appBarLayout;


    @Override
    protected void stopTasks() {
        if(pageTask!=null&&!pageTask.isCancelled()) pageTask.cancel(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true, false, false);
        setContentView(R.layout.activity_teacher);
        teacherName = getIntent().getStringExtra("name");
        initToolbar();
        initViews();
        refreshPage();

//        loadingView = findViewById(R.id.teacher_loading);
//        webView = findViewById(R.id.webview);
//        notfoundView = findViewById(R.id.teacher_notfound);
//        webView.setVisibility(View.INVISIBLE);
//        notfoundView.setVisibility(View.INVISIBLE);
//      //  loadingView.setBackgroundColor(getColorPrimary());
//        new LoadTeacherPageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    void refreshPage() {
        refresh.setVisibility(View.VISIBLE);
        BmobQuery<Teacher> bq = new BmobQuery();
        bq.addWhereEqualTo("name", teacherName);
        bq.findObjects(new FindListener<Teacher>() {
            @Override
            public void done(List<Teacher> list, BmobException e) {
                refresh.setVisibility(View.INVISIBLE);
                if (e == null && list != null && list.size() > 0) {
                    teacher = list.get(0);
                    loadInfos();
                } else {
                    AlertDialog ad = new AlertDialog.Builder(ActivityTeacher.this).setMessage("是否前往教务系统，勾选导入教师信息后重新导入课表，帮助我们完善教师库？").setTitle("教师库未收录该教师")
                            .setNegativeButton("下次吧", null).setPositiveButton("好的！！！", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityUtils.startJWTSActivity(ActivityTeacher.this);
                                }
                            }).setNeutralButton("查找教师网站", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (pageTask != null && !pageTask.isCancelled())
                                        pageTask.cancel(true);
                                    pageTask = new LoadTeacherPageTask();
                                    pageTask.execute();
                                }
                            })

                            .create();
                    if (!ActivityTeacher.this.isDestroyed()) ad.show();
                }
            }
        });
    }


    void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        appBarLayout = findViewById(R.id.appbar);
        toolbar.setTitle("");
        toolbar.inflateMenu(R.menu.toolbar_teacher);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_website) {
                    if (pageTask != null && !pageTask.isCancelled()) pageTask.cancel(true);
                    pageTask = new LoadTeacherPageTask();
                    pageTask.execute();
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

    void initViews() {
        refresh = findViewById(R.id.progressBar);
        name = findViewById(R.id.teacher_name);
        gender = findViewById(R.id.teacher_gender);
        title = findViewById(R.id.teacher_title);
        school = findViewById(R.id.teacher_school);
        phone = findViewById(R.id.teacher_phone);
        email = findViewById(R.id.teacher_email);
        detail = findViewById(R.id.teacher_detail);
        code = findViewById(R.id.teacher_code);
        teacherAvatar = findViewById(R.id.teacher_avatar);
        avatar_card = findViewById(R.id.card_avatar);
    }

    void loadInfos() {
        name.setText(teacher.getName());
        gender.setText(teacher.getGender());
        title.setText(TextUtils.isEmpty(teacher.getTitle()) ? "无数据" : teacher.getTitle());
        school.setText(teacher.getSchool());
        phone.setText(TextUtils.isEmpty(teacher.getPhone()) ? "无数据" : teacher.getPhone());
        email.setText(TextUtils.isEmpty(teacher.getEmail()) ? "无数据" : teacher.getEmail());
        detail.setText(TextUtils.isEmpty(teacher.getDetail()) ? "无数据" : teacher.getDetail());
        code.setText(teacher.getTeacherCode());
        Glide.with(this).load(teacher.getPhotoLink())
                .placeholder(R.drawable.ic_account_activated).apply(RequestOptions.bitmapTransform(new CircleCrop())).into(teacherAvatar);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_teacher, menu);
        return super.onCreateOptionsMenu(menu);
    }

    class LoadTeacherPageTask extends AsyncTask {

        AlertDialog ad;

        LoadTeacherPageTask() {
            ad = new AlertDialog.Builder(ActivityTeacher.this).setTitle("正在尝试查找教师网站").setMessage("请稍候").
                    create();
            ad.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ad.show();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                Document teachersPage = Jsoup.connect("http://www.hitsz.edu.cn/teacher/id-14.html")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .header("Host", "www.hitsz.edu.cn")
                        .header("Upgrade-Insecure-Requests", "1")
                        .get();
                //System.out.println(teachersPage);
                //Log.e("page", String.valueOf(teachersPage));
                Elements names = teachersPage.getElementsByClass("name");
                List<SparseArray<String>> teachers = new ArrayList<>();
                //Elements e = teachersPage.select("a:contains("+teacherName+")");
                //Log.e("teacher:", String.valueOf(e));
                //System.out.println(e);
                for (Element e : names) {
                    for (Element t : e.getElementsByTag("a")) {
                        System.out.println(t.text() + "," + t.attr("href"));
                        if (t.text().contains(teacherName) || teacherName.contains(t.text())) {
                            return "http://www.hitsz.edu.cn" + t.attr("href");
                        }
                    }
                }
                System.out.println(teachers);
                return teachers;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            ad.dismiss();
            if (o instanceof String) {
                Uri uri = Uri.parse((String) o);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                ActivityTeacher.this.startActivity(intent);
            } else {
//                notfoundView.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        MaterialCircleAnimator.animShow(notfoundView, 600);
//                    }
//                });
                Toast.makeText(ActivityTeacher.this, "没有找到信息！", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
