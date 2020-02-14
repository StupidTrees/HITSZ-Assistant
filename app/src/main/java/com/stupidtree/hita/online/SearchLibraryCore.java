package com.stupidtree.hita.online;

import android.text.TextUtils;
import android.util.SparseArray;

import androidx.annotation.WorkerThread;

import com.stupidtree.hita.util.HTMLUtils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchLibraryCore {
    public static final int TITLE = 0;
    public static final int SUBTITLE = 1;
    public static final int AUTHOR = 2;
    public static final int PUBLISHER = 3;
    public static final int DATE = 4;
    public static final int IMAGE = 5;
    public static final int URL = 6;
    private HashMap<String, String> cookies;
    // private String token;
    private int totalResult = 0;

    private int pagerOffset = 0;
    private String lastKeyword;
    private String range;

    public void setRange(String range) {
        this.range = range;
    }

    public SearchLibraryCore() {
        this.cookies = new HashMap<>();
    }

    public String getLastKeyword() {
        return lastKeyword;
    }


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
            Connection c = Jsoup.connect("https://opac.lib.utsz.edu.cn/Search/searchshow.jsp").
                    cookies(cookies)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.87 Safari/537.36")
                    .data("v_value", text)
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "nvigate")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("Sec-Fetch-User", "?1")
                    .data("v_tablearray", "bibliosm,serbibm,apabibibm,mmbibm,")
                    .data("v_index", "all")
                    .data("sortfield", "ptitle")
                    .data("sorttype", "desc")
                    .data("pageNum", "50")
                    .ignoreHttpErrors(true)
                    .ignoreContentType(true)
                    .data("v_page", "" + (pagerOffset + 1));
            if (TextUtils.isEmpty(range)) d = c.get();
            else d = c.data("library", range).get();
        } catch (IOException e) {
            totalResult = 0;
            e.printStackTrace();
            //throw SearchException.newConnectError();
        }
        try {
            Elements lis = d.getElementsByTag("li");
            for (Element li : lis) {
                if (li.getElementsByClass("author").size() > 0) {
                    SparseArray<String> m = new SparseArray<>();
                    String author = HTMLUtils.getStringValueByClass(li, "author");
                    String publisher = HTMLUtils.getStringValueByClass(li, "publisher");
                    String dates = HTMLUtils.getStringValueByClass(li, "dates");
                    String describe = HTMLUtils.getStringValueByClass(li, "text");
                    String image = HTMLUtils.getAttrValueByClass(li, "img", "src");
                    String uri = "https://opac.lib.utsz.edu.cn/Search/" + HTMLUtils.getAttrValueInTag(li, "href", "a");
                    String title = HTMLUtils.getTextOfTagHavingAttr(li, "a", "title");
                    m.put(TITLE, title);
                    m.put(SUBTITLE, describe);
                    m.put(AUTHOR, author);
                    m.put(PUBLISHER, publisher);
                    m.put(DATE, dates);
                    m.put(IMAGE, image);
                    m.put(URL, uri);
                    System.out.println(li);
                    res.add(m);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            totalResult = 0;
            throw SearchException.newResolveError();
        }
        try {
            totalResult = Integer.parseInt(HTMLUtils.getElementsInClassByTag(d, "total", "span").get(1).text());
        } catch (NumberFormatException e) {
            totalResult = 0;
        }

        return res;
    }

    @WorkerThread
    public List<SparseArray<String>> loadMore() throws SearchException {
        List<SparseArray<String>> res = new ArrayList<>();
        pagerOffset++;
        Document d = null;
        try {
            Connection c = Jsoup.connect("https://opac.lib.utsz.edu.cn/Search/searchshow.jsp").
                    cookies(cookies)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.87 Safari/537.36")
                    .data("v_value", lastKeyword)
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "nvigate")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("Sec-Fetch-User", "?1")
                    .data("v_tablearray", "bibliosm,serbibm,apabibibm,mmbibm,")
                    .data("v_index", "all")
                    .data("sortfield", "ptitle")
                    .data("sorttype", "desc")
                    .data("pageNum", "10")
                    .ignoreHttpErrors(true)
                    .ignoreContentType(true)
                    .data("v_page", "" + (pagerOffset + 1));
            if (TextUtils.isEmpty(range)) d = c.get();
            else d = c.data("library", range).get();
        } catch (IOException e) {
            totalResult = 0;
            e.printStackTrace();
            //throw SearchException.newConnectError();
        }
        try {
            Elements lis = d.getElementsByTag("li");
            for (Element li : lis) {
                if (li.getElementsByClass("author").size() > 0) {
                    SparseArray<String> m = new SparseArray<>();
                    String author = HTMLUtils.getStringValueByClass(li, "author");
                    String publisher = HTMLUtils.getStringValueByClass(li, "publisher");
                    String dates = HTMLUtils.getStringValueByClass(li, "dates");
                    String describe = HTMLUtils.getStringValueByClass(li, "text");
                    String image = HTMLUtils.getAttrValueByClass(li, "img", "src");
                    String uri = "https://opac.lib.utsz.edu.cn/Search/" + HTMLUtils.getAttrValueInTag(li, "href", "a");
                    String title = HTMLUtils.getTextOfTagHavingAttr(li, "a", "title");
                    m.put(TITLE, title);
                    m.put(SUBTITLE, describe);
                    m.put(AUTHOR, author);
                    m.put(PUBLISHER, publisher);
                    m.put(DATE, dates);
                    m.put(IMAGE, image);
                    m.put(URL, uri);
                    System.out.println(li);
                    res.add(m);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            totalResult = 0;
            throw SearchException.newResolveError();
        }
        try {
            totalResult = Integer.parseInt(HTMLUtils.getElementsInClassByTag(d, "total", "span").get(1).text());
        } catch (NumberFormatException e) {
            totalResult = 0;
        }

        return res;
    }


}
