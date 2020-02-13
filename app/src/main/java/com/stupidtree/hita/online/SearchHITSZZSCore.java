package com.stupidtree.hita.online;

import android.util.SparseArray;

import androidx.annotation.WorkerThread;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.stupidtree.hita.R;
import com.stupidtree.hita.util.JsonUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.HContext;

public class SearchHITSZZSCore {
    public static final int TITLE = 0;
    public static final int SUBTITLE = 1;
    public static final int TIME = 2;
    public static final int ID = 3;
    // private String token;
    private int totalResult = 0;
    private String lastKeyword;

    public String getLastKeyword() {
        return lastKeyword;
    }


    public void reset() {

        lastKeyword = null;
    }

    public int getTotalResult() {
        return totalResult;
    }

    @WorkerThread
    public List<SparseArray<String>> searchForResult(String text) throws SearchException {
        if (text == null) text = "";
        List<SparseArray<String>> res = new ArrayList<>();
        lastKeyword = text;
        Document d = null;
        try {
            d = Jsoup.connect("http://zsb.hitsz.edu.cn/zs_common/xxbt/getBtxx").
                    //cookies(cookies).
                            header("X-Requested-With", "XMLHttpRequest")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.87 Safari/537.36")
                    .data("info", "{\"xxbt\":\"" + text + "\"}")
                    .post();
        } catch (IOException e) {
            totalResult = 0;
            throw SearchException.newConnectError();
        }
        try {
            String json = d.getElementsByTag("body").text();
            JsonObject jo = new JsonParser().parse(json).getAsJsonObject();
            if (jo.get("isSuccess").getAsBoolean()) {
                JsonArray ja = jo.get("module").getAsJsonArray();
                for (JsonElement je : ja) {
                    JsonObject item = je.getAsJsonObject();
                    String title = JsonUtils.getStringInfo(item, "xxbt");
                    String id = JsonUtils.getStringInfo(item, "id");
                    String time = JsonUtils.getStringInfo(item, "fbsj");
                    String subtitle = JsonUtils.getStringInfo(item, "zsxxjj");
                    SparseArray<String> sa = new SparseArray<String>();
                    sa.put(TITLE, title);
                    sa.put(ID, id);
                    sa.put(TIME, time);
                    sa.put(SUBTITLE, subtitle);
                    res.add(sa);
                }
            }else{
                throw SearchException.newConnectError();
            }
        } catch (Exception e) {
            throw SearchException.newResolveError();
        }
        return res;
    }
}
