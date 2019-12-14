package com.stupidtree.hita.online;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cn.bmob.v3.BmobObject;

public class Infos extends BmobObject {
    String name;
    String type;
    String json;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public JsonObject getJson(){
        try {
            JsonParser jp = new JsonParser();
            return jp.parse(json).getAsJsonObject();
        } catch (Exception e) {
            e.printStackTrace();
            return new JsonObject();
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


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setJson(JsonObject jo){
        json = jo.toString();
    }
}
