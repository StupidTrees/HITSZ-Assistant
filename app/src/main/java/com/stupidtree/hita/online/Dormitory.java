package com.stupidtree.hita.online;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stupidtree.hita.R;

import java.util.ArrayList;
import java.util.HashMap;

public class Dormitory extends Location {
    String businesshours;
    String telephone;
    String company;
    String occupation;

    public Dormitory(Location l) {
        super(l);
        JsonObject jo = new JsonParser().parse(l.getInfos()).getAsJsonObject();
        JsonElement je_com = jo.get("company");
        JsonElement je_tel = jo.get("telephone");
        JsonElement je_bus = jo.get("businesshours");
        JsonElement je_oc = jo.get("occupation");
        if(je_com!=null)  company = je_com.getAsString();
        if(je_tel!=null)  telephone = je_tel.getAsString();
        if(je_bus!=null) businesshours = je_bus.getAsString();
        if(je_oc!=null)  occupation = je_oc.getAsString();
    }

    public String getBusinesshours() {
        return businesshours;
    }

    public void setBusinesshours(String businesshours) {
        this.businesshours = businesshours;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
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
        m2.put("key","物业");
        m2.put("value",company);
        m2.put("is_colored",false);
        m2.put("icon", R.drawable.ic_business);
        HashMap m3 = new HashMap();
        m3.put("key","地址");
        m3.put("value",address);
        m3.put("is_colored",false);
        m3.put("icon", R.drawable.ic_location2);
        HashMap m4 = new HashMap();
        m4.put("key","物业电话");
        m4.put("value",telephone);
        m4.put("is_colored",true);
        m4.put("is_phonenumber",true); //标注为电话
        m4.put("icon", R.drawable.ic_local_phone);
        HashMap m5 = new HashMap();
        m5.put("key","开放时间");
        m5.put("value",businesshours);
        m5.put("is_colored",true);
        m5.put("icon", R.drawable.ic_access_time);
        HashMap m6 = new HashMap();
        m6.put("key","入住情况");
        m6.put("value",occupation);
        m6.put("is_colored",false);
        m6.put("icon", R.drawable.ic_crowd);
        result.add(m1);
        result.add(m2);
        result.add(m3);
        result.add(m4);
        result.add(m5);
        result.add(m6);
        return result;
    }

    @Override
    public JsonObject getInfoJsonObject() {
        JsonObject jo = new JsonObject();
        jo.addProperty("businesshours",businesshours);
        jo.addProperty("telephone",telephone);
        jo.addProperty("company",company);
        jo.addProperty("occupation",occupation);
        return jo;
    }
}
