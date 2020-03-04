package com.stupidtree.hita.online;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import cn.bmob.v3.BmobUser;

public class HITAUser extends BmobUser {


    String school;
    String realname;
    String nick;
    String signature;
    String studentnumber;
    String avatarUri;
    String punchInfo;
    String usingTheme;


    public void setUsingTheme(String usingTheme) {
        this.usingTheme = usingTheme;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }


    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getStudentnumber() {
        return studentnumber;
    }

    public void setStudentnumber(String studentnumber) {
        this.studentnumber = studentnumber;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getAvatarUri() {
        return avatarUri;
    }

    public void setAvatarUri(String avatarUri) {
        this.avatarUri = avatarUri;
    }

    @Override
    public String toString() {
        return getUsername() + "," + getNick() + "," + getRealname();
    }


    public int getHappyDays() {
        JsonObject jo;
        try {
            jo = new JsonParser().parse(punchInfo).getAsJsonObject();
        } catch (Exception e) {
            JsonObject j = new JsonObject();
            j.addProperty("happy_days", 0);
            j.addProperty("sad_days", 0);
            j.addProperty("normal_days", 0);
            punchInfo = j.toString();
            jo = j;
        }
        if (jo.has("happy_days")) return jo.get("happy_days").getAsInt();
        else return 0;


    }

    public int getNormalDays() {
        JsonObject jo;
        try {
            jo = new JsonParser().parse(punchInfo).getAsJsonObject();
        } catch (Exception e) {
            JsonObject j = new JsonObject();
            j.addProperty("happy_days", 0);
            j.addProperty("sad_days", 0);
            j.addProperty("normal_days", 0);
            punchInfo = j.toString();
            jo = j;
        }
        if (jo.has("normal_days")) return jo.get("normal_days").getAsInt();
        else return 0;


    }

    public int getSadDays() {
        JsonObject jo;
        try {
            jo = new JsonParser().parse(punchInfo).getAsJsonObject();
        } catch (Exception e) {
            JsonObject j = new JsonObject();
            j.addProperty("happy_days", 0);
            j.addProperty("sad_days", 0);
            j.addProperty("normal_days", 0);
            punchInfo = j.toString();
            jo = j;
        }
        if (jo.has("sad_days")) return jo.get("sad_days").getAsInt();
        else return 0;
    }

    public int getPunchDays() {
        JsonObject jo;
        try {
            jo = new JsonParser().parse(punchInfo).getAsJsonObject();
        } catch (Exception e) {
            JsonObject j = new JsonObject();
            j.addProperty("happy_days", 0);
            j.addProperty("sad_days", 0);
            j.addProperty("normal_days", 0);
            punchInfo = j.toString();
            jo = j;
        }
        int happy = 0, normal = 0, sad = 0;
        if (jo.has("sad_days")) sad = jo.get("sad_days").getAsInt();
        if (jo.has("normal_days")) normal = jo.get("normal_days").getAsInt();
        if (jo.has("happy_days")) sad = jo.get("happy_days").getAsInt();
        return happy + sad + normal;
    }

    public boolean hasPunch(Calendar c) {
        JsonObject jo;
        try {
            jo = new JsonParser().parse(punchInfo).getAsJsonObject();
        } catch (Exception e) {
            JsonObject j = new JsonObject();
            j.addProperty("happy_days", 0);
            j.addProperty("sad_days", 0);
            j.addProperty("normal_days", 0);
            punchInfo = j.toString();
            jo = j;
        }
        if (jo.has("punch_date")) {
            String date = jo.get("punch_date").getAsString();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String exp = sdf.format(c.getTime());
            return exp.equals(date);
        } else {
            return false;
        }

    }

    public void Punch(Calendar c, int choice) {
        JsonObject jo;
        try {
            jo = new JsonParser().parse(punchInfo).getAsJsonObject();
        } catch (Exception e) {
            JsonObject j = new JsonObject();
            j.addProperty("happy_days", 0);
            j.addProperty("sad_days", 0);
            j.addProperty("normal_days", 0);
            punchInfo = j.toString();
            jo = j;
        }
        if (!jo.has("sad_days")) jo.addProperty("sad_days", 0);
        if (!jo.has("normal_days")) jo.addProperty("normal_days", 0);
        if (!jo.has("happy_days")) jo.addProperty("happy_days", 0);

        if (choice == 0) {
            int happy = jo.get("happy_days").getAsInt();
            jo.addProperty("happy_days", happy + 1);
        } else if (choice == 2) {
            int sad = jo.get("sad_days").getAsInt();
            jo.addProperty("sad_days", sad + 1);
        } else if (choice == 1) {
            int normal = jo.get("normal_days").getAsInt();
            jo.addProperty("normal_days", normal + 1);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd");
        jo.addProperty("punch_date", sdf.format(c.getTime()));
        punchInfo = jo.toString();
    }
}
