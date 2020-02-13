package com.stupidtree.hita.online;

import android.util.SparseArray;

import androidx.annotation.WorkerThread;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.stupidtree.hita.util.JsonUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SearchTeacherCore {
    public static final int NAME = 0;
    public static final int DEPARTMENT = 1;
    public static final int ID = 2;
    public static final int URL = 3;
    private HashMap<String, String> cookies;

    private String lastKeyword;

    public SearchTeacherCore() {
        this.cookies = new HashMap<>();
    }

    public void reset() {
        lastKeyword = "";
    }

    public String getLastKeyword() {
        return lastKeyword;
    }

    @WorkerThread
    public List<SparseArray<String>> searchForResult(String text) throws SearchException{
        if (text == null) text = "";
        lastKeyword = text;
        text = text.replaceAll("老师", "").replaceAll("教师", "").replaceAll("教室", "")
                .replaceAll("teacher", "").replaceAll("Teacher", "").replaceAll("先生", "")
                .replaceAll("女士", "");
        List<SparseArray<String>> res = new ArrayList<>();

        Document d = null;
        try {
            d = Jsoup.connect("http://faculty.hitsz.edu.cn/hompage/findTeachersByName.do").
                    cookies(cookies).
                    userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.87 Safari/537.36")
                    .data("userName", text)
                    .data("X-Requested-With", "XMLHttpRequest").
                            post();
        } catch (IOException e) {
            throw SearchException.newConnectError();
        }
        try {
            String json = d.getElementsByTag("body").text();
            JsonObject jo = new JsonParser().parse(json).getAsJsonObject();
            JsonArray ja = jo.get("rows").getAsJsonArray();
            for (JsonElement je : ja) {
                JsonObject teacher = je.getAsJsonObject();
                SparseArray<String> sa = new SparseArray<String>();
                sa.put(NAME, JsonUtils.getStringInfo(teacher, "userName"));
                sa.put(ID, JsonUtils.getStringInfo(teacher, "id"));
                sa.put(URL, JsonUtils.getStringInfo(teacher, "url"));
                sa.put(DEPARTMENT, JsonUtils.getStringInfo(teacher, "department"));
                res.add(sa);
            }
        } catch (Exception e) {
           throw SearchException.newResolveError();
        }

        return res;
    }
}
