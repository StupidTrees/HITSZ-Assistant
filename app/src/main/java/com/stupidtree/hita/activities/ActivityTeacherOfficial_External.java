package com.stupidtree.hita.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stupidtree.hita.R;
import com.stupidtree.hita.fragments.BaseOperationTask;
import com.stupidtree.hita.util.HTMLUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Objects;

import static com.stupidtree.hita.HITAApplication.TPE;

public class ActivityTeacherOfficial_External extends ActivityTeacherOfficial {

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            teacherId = Objects.requireNonNull(getIntent().getData()).toString().split("userid=")[1];
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    void initViews() {
        super.initViews();
    }


    @Override
    public void onOperationStart(String id, Boolean[] params) {
       // super.onOperationStart(id, params);
        switch (id){
            case "page":
                fab.hide();
                refresh.setRefreshing(true);
                break;
            case "profile":
        }
    }

    @Override
    public void onOperationDone(String id, BaseOperationTask task, Boolean[] params, Object result) {
       // super.onOperationDone(id, task, params, result);
        switch (id){
            case "page":
                LoadTeacherPageTask pt = (LoadTeacherPageTask) task;
                teacherName = pt.teacherName;
                teacherUrl = pt.teacherUrl;
                teacherInfo.clear();
                tabTitles.clear();
                tabTitles.addAll(pt.titleToAdd);
                teacherInfo.putAll(pt.infoToAdd);
                if (teacherInfo.size() > 0) {
                    noneView.setVisibility(View.GONE);
                    pager.setVisibility(View.VISIBLE);
                } else {
                    pager.setVisibility(View.GONE);
                    noneView.setVisibility(View.VISIBLE);
                }
                pagerAdapter.notifyDataSetChanged();
                pager.scheduleLayoutAnimation();
                fab.show();
                refresh.setRefreshing(false);
                name.setText(teacherName);
                if(pageTask2!=null&&pageTask2.getStatus() != AsyncTask.Status.FINISHED) pageTask2.cancel(true);
                pageTask2 = new LoadTeacherProfileTask(this,teacherId,teacherUrl);
                pageTask2.executeOnExecutor(TPE);
                break;
            case "profile":
                LoadTeacherProfileTask lt = (LoadTeacherProfileTask) task;
                teacherProfile.clear();
                teacherProfile.putAll(lt.teacherProfile);
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
                Glide.with(getThis()).load("http://faculty.hitsz.edu.cn/file/showHP.do?d=" +
                        teacherId + "&&w=200&&h=200&&prevfix=200-")
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.ic_account_activated)
                        .into(teacherAvatar);
                break;
        }
    }

    @Override
    void Refresh() {
        if(pageTask1!=null&&pageTask1.getStatus()!=AsyncTask.Status.FINISHED) pageTask1.cancel(true);
        pageTask1 = new LoadTeacherPageTask(this,teacherId);
        pageTask1.executeOnExecutor(TPE);
    }


    static class LoadTeacherProfileTask extends ActivityTeacherOfficial.LoadTeacherProfileTask {


        LoadTeacherProfileTask(OperationListener listRefreshedListener, String teacherId, String teacherUrl) {
            super(listRefreshedListener, teacherId, teacherUrl);
            id = "profile";
        }

        @Override
        protected Object doInBackground(OperationListener<Object> listRefreshedListener, Boolean... booleans) {
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


    }

    static class LoadTeacherPageTask extends ActivityTeacherOfficial.LoadTeacherPageTask {

        String teacherName;
        String teacherUrl;
        LoadTeacherPageTask(OperationListener listRefreshedListener, String teacherId) {
            super(listRefreshedListener, teacherId);
            id = "page";
        }


        @Override
        protected Object doInBackground(OperationListener<Object> listRefreshedListener, Boolean... booleans) {
            try {
                Document teachersInfo = Jsoup.connect("http://faculty.hitsz.edu.cn/TeacherHome/queryTeacherOne.do")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .data("userid", teacherId)
                        .data("language","ch")
                        .ignoreContentType(true)
                        .post();

                String json = teachersInfo.getElementsByTag("body").first().text();
                System.out.println(json);
                JsonObject jo = new JsonParser().parse(json).getAsJsonObject();
                JsonObject teacher = jo.get("teacher").getAsJsonObject();
                teacherName = teacher.get("userName").getAsString();
                teacherUrl = teacher.get("url").getAsString();
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
