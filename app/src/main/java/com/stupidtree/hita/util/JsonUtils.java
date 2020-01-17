package com.stupidtree.hita.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

public class JsonUtils {

    public static String getStringInfo(JsonObject jo,String key){
        JsonElement je = jo.get(key);
        if(je==null||je.equals(JsonNull.INSTANCE)) return "";
        else return je.getAsString();
    }
}
