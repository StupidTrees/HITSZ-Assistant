package com.stupidtree.hita.online;

import android.util.SparseArray;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stupidtree.hita.util.JsonUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SearchHITSZZSCore extends SearchCore<SparseArray<String>> {
    public static final int TITLE = 0;
    public static final int SUBTITLE = 1;
    public static final int TIME = 2;
    public static final int ID = 3;

    @Override
    public int getPageSize() {
        return 10;
    }

    @Override
    protected List<SparseArray<String>> reloadResult(String text) throws SearchException {
        List<SparseArray<String>> res = new ArrayList<>();
        Document d = null;
        try {
            d = Jsoup.connect("http://zsb.hitsz.edu.cn/zs_common/xxbt/getBtxx").
                    //cookies(cookies).
                            header("X-Requested-With", "XMLHttpRequest")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.87 Safari/537.36")
                    .data("info", "{\"xxbt\":\"" + text + "\"}")
                    .post();
        } catch (IOException e) {
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

    @Override
    protected List<SparseArray<String>> loadMoreResult(String text) throws SearchException {
        return new ArrayList<>();
    }


}
