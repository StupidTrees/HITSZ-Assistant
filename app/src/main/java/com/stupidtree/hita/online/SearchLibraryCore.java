package com.stupidtree.hita.online;

import android.text.TextUtils;
import android.util.SparseArray;

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

public class SearchLibraryCore extends SearchCore<SparseArray<String>> {
    public static final int TITLE = 0;
    private static final int SUBTITLE = 1;
    public static final int AUTHOR = 2;
    public static final int PUBLISHER = 3;
    private static final int DATE = 4;
    public static final int IMAGE = 5;
    public static final int URL = 6;
    private HashMap<String, String> cookies;
    private String range = "F44010";

    public void setRange(String range) {
        this.range = range;
    }

    public SearchLibraryCore() {
        this.cookies = new HashMap<>();
    }


    @Override
    public int getPageSize() {
        return 10;
    }

    @Override
    protected List<SparseArray<String>> reloadResult(String text) throws SearchException {
        List<SparseArray<String>> res = new ArrayList<>();
        Document d = null;
        try {
            Connection c = Jsoup.connect("https://opac.lib.utsz.edu.cn/Search/searchshow.jsp").
                    cookies(cookies)
                    .timeout(5000)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.87 Safari/537.36")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "nvigate")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("Sec-Fetch-User", "?1")
                    .data("v_tablearray", "bibliosm,serbibm,apabibibm,mmbibm,")
                    .data("v_index", "all")
                    .data("v_value", text)
                    .data("sortfield", "ptitle")
                    .data("sorttype", "desc")
                    .data("pageNum", "10")
                    .ignoreHttpErrors(true)
                    .ignoreContentType(true)
                    .data("v_page", "" + (getOffset() + 1));
            if (TextUtils.isEmpty(range)) d = c.get();
            else d = c.data("library", range).get();
        } catch (IOException e) {
            e.printStackTrace();
            //throw SearchException.newConnectError();
        }
        try {
            assert d != null;
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
            throw SearchException.newResolveError();
        }
        try {
            totalResult = Integer.parseInt(HTMLUtils.getElementsInClassByTag(d, "total", "span").get(1).text());
        } catch (NumberFormatException e) {
            totalResult = 0;
        }

        return res;
    }

    @Override
    protected List<SparseArray<String>> loadMoreResult(String text) throws SearchException {
        List<SparseArray<String>> res = new ArrayList<>();
        Document d = null;
        try {
            Connection c = Jsoup.connect("https://opac.lib.utsz.edu.cn/Search/searchshow.jsp").
                    cookies(cookies)
                    .timeout(5000)
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
                    .data("pageNum", "10")
                    .ignoreHttpErrors(true)
                    .ignoreContentType(true)
                    .data("v_page", "" + (getOffset() + 1));
            if (TextUtils.isEmpty(range)) d = c.get();
            else d = c.data("library", range).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (d == null) return res;
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
