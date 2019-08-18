package com.stupidtree.hita.core;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stupidtree.hita.core.timetable.EventItem;
import com.stupidtree.hita.core.timetable.EventItemHolder;
import com.stupidtree.hita.online.HITAUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.stupidtree.hita.HITAApplication.mDBHelper;

public class Subject implements Comparable{
    public String code;
    public String name;
    public String teacher;
    //public String infoHTML = null;
    private HashMap<Integer, Double> ratingMap;
    private HashMap<String, String> Scores;

    public boolean isMOOC = false;
    public boolean exam = false;
    public boolean Default = true;
    public String xnxq = "无数据";
    public String school = "无数据";
    public String credit = "无数据";
    public String compulsory = "无数据";
    public String totalCourses = "无数据";
    public String type = "无数据";
    public String curriculumCode;

    public void setHitaUser(HITAUser hitaUser) {
        this.hitaUser = hitaUser;
    }

    HITAUser hitaUser;

    public Subject(String curriculumCode, String name, String teacher) {
        ratingMap = new HashMap<>();
        Scores = new HashMap<>();
        this.name = name;
        this.teacher = teacher;
        this.curriculumCode = curriculumCode;
    }

    public Subject(Cursor c) {
         ratingMap = new HashMap<Integer, Double>();
         Scores = new HashMap<>();
        getScroesFromString(c.getString(c.getColumnIndex("scores")));
        getRatesFromString(c.getString(c.getColumnIndex("rates")));
        name = c.getString(c.getColumnIndex("name"));
        type = c.getString(c.getColumnIndex("type"));
        isMOOC = c.getInt(c.getColumnIndex("is_mooc")) != 0;
        exam = c.getInt(c.getColumnIndex("is_exam")) != 0;
        Default = c.getInt(c.getColumnIndex("is_default")) != 0;
        xnxq = c.getString(c.getColumnIndex("xnxq"));
        school = c.getString(c.getColumnIndex("school"));
        credit = c.getString(c.getColumnIndex("point"));
        compulsory = c.getString(c.getColumnIndex("compulsory"));
        totalCourses = c.getString(c.getColumnIndex("total_courses"));
        code = c.getString(c.getColumnIndex("code"));
        curriculumCode = c.getString(c.getColumnIndex("curriculum_code"));
    }

    public Subject(String x){
         Log.e("新建Subject",x);
//        String[] c = x.split("##",-1); //-1支持末尾空串
//        ratingMap = new HashMap<>();
//        //Scores = new HashMap<>();
//        this.name = c[0];
//        this.type = c[1];
//        isMOOC = Boolean.parseBoolean(c[2]);
//        exam = Boolean.parseBoolean(c[3]);
//        Default = Boolean.parseBoolean(c[4]);
//        xnxq = c[5];
//        school = c[6];
//        credit = c[7];
//        compulsory = c[8];
//        totalCourses = c[9];
//        code = c[10];
//        curriculumCode = c[11];
//        getScroesFromString(c[12]);
//        getRatesFromString(c[13]);
        
        JsonObject jo = new JsonParser().parse(x).getAsJsonObject();
        ratingMap = new HashMap<>();
        Scores = new HashMap<>();
        name = jo.get("name").getAsString();
        type = jo.get("type").getAsString();
        isMOOC = jo.get("is_mooc").getAsBoolean();
        exam = jo.get("is_exam").getAsBoolean();
        Default = jo.get("default").getAsBoolean();
        xnxq = jo.get("xnxq").getAsString();
        school = jo.get("school").getAsString();
        credit = jo.get("credit").getAsString();
        compulsory = jo.get("compulsory").getAsString();
        totalCourses = jo.get("total_courses").getAsString();
        code = jo.get("code").getAsString();
        curriculumCode = jo.get("curriculum_code").getAsString();
        getScroesFromString(jo.get("scores").toString());
        getRatesFromString(jo.get("rates").toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subject subject = (Subject) o;
        return Objects.equals(name, subject.name);
    }

    @Override
    public String toString() {
        JsonObject jo = new JsonObject();
        jo.addProperty("name", name);
        jo.addProperty("type",type);
        jo.addProperty("is_mooc",isMOOC);
        jo.addProperty("is_exam", exam);
        jo.addProperty("default" ,Default);
        jo.addProperty("xnxq",xnxq);
        jo.addProperty("school",school);
        jo.addProperty("credit", credit);
        jo.addProperty("compulsory",compulsory);
        jo.addProperty("total_courses",totalCourses);
        jo.addProperty("code", code );
        jo.addProperty("curriculum_code",curriculumCode);
        Gson gson = new Gson();
        jo.add("scores",gson.toJsonTree(Scores));
        jo.add("rates",gson.toJsonTree(ratingMap));
        //return name+"##"+type+"##"+isMOOC+"##"+exam+"##"+Default+"##"+xnxq+"##"+school+"##"+ credit +"##"+ compulsory +"##"+totalCourses+"##"+code+"##"+curriculumCode+"##"+scoresToString()+"##"+ratesToString();
        return jo.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, code, curriculumCode);
    }

    public void setRate(Integer courseNumber, Double rate) {
        ratingMap.put(courseNumber, rate);
        SQLiteDatabase sd = mDBHelper.getWritableDatabase();
        sd.replace("subject", "scores",getContentValues());
        Log.e("ratingMap:addRate", String.valueOf(ratingMap));
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        }).run();
    }

    public Double getRate(int number) {
        if (ratingMap.get(number) == null) return 0.0;
        return ratingMap.get(number);
    }

    public double getRank() {
        Double rateSum = 0.0;
        for (Double f : ratingMap.values()) {
            rateSum += f;
        }
        rateSum /= ratingMap.size();
        float Point = 0;
        try {
            Point = Float.valueOf(Point);
        } catch (Exception e) {
            Point = 2.0f;
        }
        return Point * rateSum;
    }

    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("type", type);
        cv.put("is_mooc", isMOOC);
        cv.put("is_exam", exam);
        cv.put("is_default", Default);
        cv.put("xnxq", xnxq);
        cv.put("school", school);
        cv.put("point", credit);
        cv.put("compulsory", compulsory);
        cv.put("total_courses", totalCourses);
        cv.put("code", code);
        cv.put("curriculum_code", curriculumCode);
        cv.put("scores", scoresToString());
        cv.put("rates", ratesToString());
        return cv;
    }

    public ArrayList<EventItem> getCourses() {
        ArrayList<EventItem> result = new ArrayList<>();
        SQLiteDatabase sd = mDBHelper.getReadableDatabase();
        Cursor c = sd.query("timetable", null, "name=? and type=?",
                new String[]{name, TimeTable.TIMETABLE_EVENT_TYPE_COURSE + ""}, null, null, null);
        while (c.moveToNext()) {
            EventItemHolder eih = new EventItemHolder(c);
            result.addAll(eih.getAllEvents());
        }
        c.close();
        return result;
    }

    public EventItem getFirstCourse() {
        EventItem result = null;
        SQLiteDatabase sd = mDBHelper.getReadableDatabase();
        Cursor c = sd.query("timetable", null, "name=? and type=?",
                new String[]{name, TimeTable.TIMETABLE_EVENT_TYPE_COURSE + ""}, null, null, null);
        if (c.moveToNext()) {
            EventItemHolder eih = new EventItemHolder(c);
            result = eih.getAllEvents().get(0);
        }
        c.close();
        return result;
    }

    private String scoresToString() {
//        StringBuilder sb = new StringBuilder();
//        for(Map.Entry e:Scores.entrySet()){
//            sb.append(e.getKey()).append("__").append(e.getValue()).append("&&&");
//        }
//        return sb.toString();
        Gson gson = new Gson();
        Log.e("scores",String.valueOf(Scores));
        return gson.toJson(this.Scores);
    }

    private void getScroesFromString(String s) {
        //Log.e("scores",s);
//        Scores.clear();
//        String[] x = s.split("&&&");
//        for(String k:x){
//            String []p = k.split("__");
//            if(p.length==2)Scores.put(p[0],p[1]);
//        }
        //        }
        JsonParser jp = new JsonParser();
        JsonObject jo = jp.parse(s).getAsJsonObject();
        for(Map.Entry<String, JsonElement> e:jo.entrySet()) {
            Scores.put(e.getKey(), e.getValue().getAsString());
        }
    }

    private String ratesToString() {
//        StringBuilder sb = new StringBuilder();
//        for(Map.Entry e:ratingMap.entrySet()){
//            sb.append(e.getKey()).append("__").append(e.getValue()).append("&&&");
//        }
//        return sb.toString();
        //Log.e("ratingMap",String.valueOf(ratingMap));
        Gson gson = new Gson();
        String rates = gson.toJson(ratingMap);
       // Log.e("rateToStr", rates);
        return rates;
    }

    private void getRatesFromString(String s) {
        //Log.e("scores",s);
//        ratingMap.clear();
//        String[] x = s.split("&&&");
//        for(String k:x){
//            String []p = k.split("__");
//            if(p.length==2)ratingMap.put(Integer.parseInt(p[0]),Float.parseFloat(p[1]));
//        }
        JsonParser jp = new JsonParser();
        JsonObject jo = jp.parse(s).getAsJsonObject();
        for(Map.Entry e:jo.entrySet()){
            ratingMap.put(Integer.parseInt(e.getKey().toString()), Double.valueOf(e.getValue().toString()));
        }
//        Log.e("rate", String.valueOf(ratingMap));
//        Gson gson = new Gson();
//        ratingMap = gson.fromJson(s,HashMap.class);
    }

    public void addScore(final String name, String score) {
        Scores.put(name, score);
        SQLiteDatabase sd = mDBHelper.getWritableDatabase();
        sd.replace("subject", "rates",getContentValues());

//        Scores.put(name, score);
        Log.e("addScore", name + "," + score);
//        SQLiteDatabase sd = mDBHelper.getWritableDatabase();
//        sd.execSQL("update subject set scores =? where name= ? and code = ? and curriculum_code=?",new String[]{scoresToString(),name,code,curriculumCode});
        //sd.update("subject",getContentValues(),"name=?",new String[]{name});
//        String.valueOf(sd.replace("subject","rates", Subject.this.getContentValues()));
//        sd.delete("subject", "name=?",
//                new String[]{name});
//        sd.insert("subject", null, Subject.this.getContentValues());

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//

//            }
//        }).run();
    }

    public HashMap<String, String> getScores() {
        return Scores;
    }

    public HashMap<Integer, Double> getRatingMap() {
        return ratingMap;
    }

    public float getPriority(){
        float creditF,rate;
        try {
            creditF = Float.parseFloat(credit);
        } catch (NumberFormatException e) {
            creditF = 3.0f;
        }
        Double sum = 0.0;
        int size = 0;
        for (Double f : ratingMap.values()) {
            if (f < 0) continue;
            sum += f;
            size++;
        }
        if(size==0) rate = 0f;
        else rate = (float) (sum/size);
        return 100+creditF*5-rate*20;

    }

    @Override
    public int compareTo(Object o) {
        return (int) (getPriority()-((Subject)o).getPriority());
    }
}
