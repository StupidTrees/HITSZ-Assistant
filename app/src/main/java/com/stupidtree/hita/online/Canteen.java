package com.stupidtree.hita.online;

import android.text.TextUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stupidtree.hita.R;

import java.util.ArrayList;
import java.util.HashMap;

import cn.bmob.v3.BmobObject;

public class Canteen extends Location {
    String company;
    String telephone;
    String businesshours;

    public Canteen(Location l) {
        super(l);
        if(TextUtils.isEmpty(l.getInfos())) return;
        JsonObject jo = new JsonParser().parse(l.getInfos()).getAsJsonObject();
        JsonElement com = jo.get("company");
        JsonElement tele = jo.get("telephone");
        JsonElement bh = jo.get("businesshours");
        if(com!=null)company = com.getAsString();
        if(tele!=null)telephone = tele.getAsString();
        if(bh!=null)businesshours = bh.getAsString();
    }


    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }


    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getBusinesshours() {
        return businesshours;
    }

    public void setBusinesshours(String businesshours) {
        this.businesshours = businesshours;
    }




    public ArrayList<HashMap> getInfoListArray(){
        ArrayList<HashMap> result = new ArrayList();
        HashMap m1 = new HashMap();
        m1.put("key","名称");
        m1.put("value",name);
        m1.put("is_colored",false); //是否着色
        m1.put("icon", R.drawable.ic_label);
        HashMap m2 = new HashMap();
        m2.put("key","经营单位");
        m2.put("value",company);
        m2.put("is_colored",false);
        m2.put("icon", R.drawable.ic_business);
        HashMap m3 = new HashMap();
        m3.put("key","地址");
        m3.put("value",address);
        m3.put("is_colored",false);
        m3.put("icon", R.drawable.ic_location2);
        HashMap m4 = new HashMap();
        m4.put("key","负责人电话");
        m4.put("value",telephone);
        m4.put("is_colored",false);
        m4.put("is_phonenumber",true); //标注为电话
        m4.put("icon", R.drawable.ic_local_phone);
        HashMap m5 = new HashMap();
        m5.put("key","营业时间");
        m5.put("value",businesshours);
        m5.put("is_colored",true);
        m5.put("icon", R.drawable.ic_access_time);
        result.add(m1);
        result.add(m2);
        result.add(m3);
        result.add(m4);
        result.add(m5);
        return result;
    }

    @Override
    public JsonObject getInfoJsonObject() {
        JsonObject jo = new JsonObject();
        jo.addProperty("company",company);
        jo.addProperty("businesshours",businesshours);
        return jo;
    }


}
