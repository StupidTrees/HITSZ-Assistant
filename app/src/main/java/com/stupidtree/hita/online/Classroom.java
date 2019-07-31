package com.stupidtree.hita.online;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stupidtree.hita.R;

import java.util.ArrayList;
import java.util.HashMap;

public class Classroom extends Location {
    String company;
    String telephone;
    String businesshours;
    String domain;
    String function;
    Boolean studyhall;
    String code;

    public Classroom(Location l) {
        super(l);
        JsonObject jo = new JsonParser().parse(l.getInfos()).getAsJsonObject();
        company = jo.get("company").getAsString();
        telephone = jo.get("telephone").getAsString();
        businesshours = jo.get("businesshours").getAsString();
        domain = jo.get("domain").getAsString();
        function = jo.get("function").getAsString();
        studyhall = Boolean.valueOf(jo.get("studyhall").getAsString());
        if(jo.get("code")!=null) code = jo.get("code").getAsString();
    }
    public Classroom(){}

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

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public Boolean getStudyhall() {
        return studyhall;
    }

    public void setStudyhall(Boolean studyhall) {
        this.studyhall = studyhall;
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
        m6.put("key","用途");
        m6.put("value",function);
        m6.put("is_colored",true);
        m6.put("icon", R.drawable.ic_business);
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
        jo.addProperty("company",company);
        jo.addProperty("telephone",telephone);
        jo.addProperty("businesshours",businesshours);
        jo.addProperty("domain",domain);
        jo.addProperty("function",function);
        jo.addProperty("studyhall",studyhall);
        return null;
    }
}
