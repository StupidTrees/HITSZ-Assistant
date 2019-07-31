package com.stupidtree.hita.online;

import android.text.TextUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.stupidtree.hita.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Scenery extends Location {

    String detailImageUrls;
    String detailIntroduction;

    public Scenery(Location l) {
        super(l);
        try {
            JsonObject jo = new JsonParser().parse(l.getInfos()).getAsJsonObject();
            JsonElement diu = jo.get("detailImageUrls");
            JsonElement di = jo.get("detailIntroduction");
            if(diu!=null) detailImageUrls = diu.getAsString();
            if(di!=null) detailIntroduction = di.getAsString();
        } catch (Exception e) {
            detailIntroduction ="无介绍";
            detailImageUrls = "";
        }

    }


    List<String> getImageUris(){
        List<String> res = new ArrayList<>();
      for(String s:detailImageUrls.split("\\$")) res.add(s);
      return res;
    }
    void setImageUrls(List<String> imageList){
        StringBuilder sb = new StringBuilder();
        for(String s:imageList){
            sb.append(s).append("$");
        }
        detailImageUrls = sb.toString();
    }
    @Override
    public ArrayList<HashMap> getInfoListArray() {
        ArrayList<HashMap> result = new ArrayList();
        HashMap m1 = new HashMap();
        m1.put("key","名称");
        m1.put("value",name);
        m1.put("is_colored",false); //是否着色
        m1.put("icon", R.drawable.ic_label);
        HashMap m2 = new HashMap();
        m2.put("key","简介");
        m2.put("value",detailIntroduction);
        m2.put("is_colored",false);
        m2.put("icon", R.drawable.ic_description);
        HashMap m3 = new HashMap();
        m3.put("key","地址");
        m3.put("value",address);
        m3.put("is_colored",false);
        m3.put("icon", R.drawable.ic_location2);
        result.add(m1);
        result.add(m2);
        result.add(m3);
        return result;
    }

    @Override
    public JsonObject getInfoJsonObject() {
        JsonObject jo = new JsonObject();
        jo.addProperty("detailImageUrls",detailImageUrls);
        jo.addProperty("detailIntroduction",detailIntroduction);
        return jo;
    }
}
