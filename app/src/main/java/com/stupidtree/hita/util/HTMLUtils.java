package com.stupidtree.hita.util;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

public class HTMLUtils {
    public static String getStringValueByClass(Element d, String className){
        String res = null;
        try {
            return d.getElementsByClass(className).first().text();
        } catch (Exception e) {
            return res;
        }
    }
    public static String getAttrValueByClass(Element d, String className,String attr){
        String res = null;
        try {
            return d.getElementsByClass(className).first().attr(attr);
        } catch (Exception e) {
            return res;
        }
    }
    public static  String getAttrValueInTag(Element d,String attr,String tag){
        try {
            return d.getElementsByTag(tag).first().attr(attr);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static  String getTextOfTag(Element d,String tag){
        try {
            return d.getElementsByTag(tag).first().text();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static  String getTextOfTagHavingAttr(Element d,String tag,String attr){
        try {
            for(Element e: d.getElementsByTag(tag)){
                if(e.hasAttr(attr)) return e.text();
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static Elements getElementsInClassByTag(Document d,String className,String tag){
        try {
            return d.getElementsByClass(className).first().getElementsByTag(tag);
        } catch (Exception e) {
            e.printStackTrace();
            return new Elements();
        }
    }
}
