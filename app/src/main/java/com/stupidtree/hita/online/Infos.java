package com.stupidtree.hita.online;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cn.bmob.v3.BmobObject;

public class Infos extends BmobObject {
    String name;
    String json;

    public JsonObject getJson(){
        try {
            JsonParser jp = new JsonParser();
            return jp.parse(json).getAsJsonObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public JsonArray getJsonArray(){
        try {
            JsonParser jp = new JsonParser();
            return jp.parse(json).getAsJsonArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
