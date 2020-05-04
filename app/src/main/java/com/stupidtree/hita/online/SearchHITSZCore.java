package com.stupidtree.hita.online;

import android.util.Log;
import android.util.SparseArray;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SearchHITSZCore extends SearchCore<SparseArray<String>> {
    private static final int TITLE = 0;
    private static final int LINK = 1;
    private static final int TYPE = 2;
    private HashMap<String, String> cookies;
    private String token;


    public SearchHITSZCore() {
        this.cookies = new HashMap<>();
    }


    @Override
    public int getPageSize() {
        return 10;
    }

    @Override
    protected List<SparseArray<String>> reloadResult(String text) throws SearchException {
        List<SparseArray<String>> res = new ArrayList<>();
        Document d;
        try {
            d = Jsoup.connect("http://www.hitsz.edu.cn/search/index.html").
                    cookies(cookies)
                    .timeout(5000)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.87 Safari/537.36")
                    .data("keywords", text)
                    .data("maxPageItems", getPageSize() + "")
                    .data("pager.offset", "" + getOffset())
                    .get();
            token = d.select("input[name=token]").attr("value");
            Log.e("token", token);
        } catch (IOException e) {
            e.printStackTrace();
            throw SearchException.newConnectError();
        }
        try {
            Elements es = d.getElementsByTag("li");
            totalResult = Integer.parseInt(d.getElementsByClass("result_n").first().text());
            for (Element e : es) {
                if (e.getElementsByTag("div").size() > 0 && e.getElementsByTag("a").size() > 0) {
                    SparseArray<String> sa = new SparseArray<>();
                    sa.put(TITLE, e.getElementsByClass("title_o").text());
                    sa.put(LINK, e.getElementsByTag("a").first().attr("href"));
                    String[] info = e.getElementsByClass("info").text().split(">>");
                    String type = info[info.length - 1];
                    sa.put(TYPE, type);
                    res.add(sa);
                }
            }
        } catch (Exception e) {
            throw SearchException.newResolveError();
        }
        return res;
    }

    @Override
    protected List<SparseArray<String>> loadMoreResult(String text) throws SearchException {
        List<SparseArray<String>> res = new ArrayList<>();
        Document d;
        try {
            d = Jsoup.connect("http://www.hitsz.edu.cn/search/index.html").
                    cookies(cookies).
                    userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.87 Safari/537.36")
                    .data("keywords", text)
                    .data("token", token)
                    .data("maxPageItems", getPageSize() + "")
                    .data("pager.offset", "" + getOffset()).
                            get();
        } catch (IOException e) {
            e.printStackTrace();
            throw SearchException.newConnectError();
        }
        try {
            Elements es = d.getElementsByTag("li");
            totalResult = Integer.parseInt(d.getElementsByClass("result_n").first().text());
            for (Element e : es) {
                if (e.getElementsByTag("div").size() > 0 && e.getElementsByTag("a").size() > 0) {
                    SparseArray<String> sa = new SparseArray<>();
                    sa.put(TITLE, e.getElementsByClass("title_o").text());
                    sa.put(LINK, e.getElementsByTag("a").first().attr("href"));
                    String[] info = e.getElementsByClass("info").text().split(">>");
                    String type = info[info.length - 1];
                    sa.put(TYPE, type);
                    res.add(sa);
                }
            }
        } catch (Exception e) {
            throw SearchException.newResolveError();
        }
        return res;
    }


}
