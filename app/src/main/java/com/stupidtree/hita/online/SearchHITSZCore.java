package com.stupidtree.hita.online;

import android.util.SparseArray;

import androidx.annotation.WorkerThread;

import com.stupidtree.hita.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.HContext;

public class SearchHITSZCore {
    private static final int TITLE = 0;
    private static final int LINK = 1;
    private static final int TYPE = 2;
    private HashMap<String, String> cookies;
    // private String token;
    private int totalResult = 0;

    private int pagerOffset = 0;
    private String lastKeyword;

    public SearchHITSZCore() {
        this.cookies = new HashMap<>();
    }

    public String getLastKeyword() {
        return lastKeyword;
    }


    //    @WorkerThread
//    public void init() {
//        try {
//            Document d = Jsoup.connect("http://www.hitsz.edu.cn/index.html").
//                    cookies(cookies).
//                    get();
//            token = d.select("input[name=token]").attr("value");
//            System.out.println(token);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public int getOffset() {
        return pagerOffset;
    }

    public void reset() {
        pagerOffset = 0;
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
        pagerOffset = 0;
        Document d = null;
        try {
            d = Jsoup.connect("http://www.hitsz.edu.cn/search/index.html").
                    cookies(cookies).
                    userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.87 Safari/537.36")
                    .data("keywords", text)
                    .data("pager.offset", "" + pagerOffset).
                            get();
        } catch (IOException e) {
            totalResult = 0;
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
                    String info[] = e.getElementsByClass("info").text().split(">>");
                    String type = info[info.length - 1];
                    sa.put(TYPE, type);
                    res.add(sa);
                }
            }
        } catch (Exception e) {
            totalResult = 0;
            throw SearchException.newResolveError();
        }
        return res;
    }

    @WorkerThread
    public List<SparseArray<String>> LoadMore() throws SearchException {
        List<SparseArray<String>> res = new ArrayList<>();
        pagerOffset += 10;
        Document d = null;
        try {
            d = Jsoup.connect("http://www.hitsz.edu.cn/search/index.html").
                    cookies(cookies).
                    userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.87 Safari/537.36")
                    .data("keywords", lastKeyword)
                    .data("pager.offset", "" + pagerOffset).
                            get();
        } catch (IOException e) {
            totalResult = 0;
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
                    String info[] = e.getElementsByClass("info").text().split(">>");
                    String type = info[info.length - 1];
                    sa.put(TYPE, type);
                    res.add(sa);
                }
            }
        } catch (Exception e) {
            totalResult = 0;
            throw SearchException.newResolveError();
        }
        return res;
    }

}
