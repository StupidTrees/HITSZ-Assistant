package com.stupidtree.hita.timetable;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.WorkerThread;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stupidtree.hita.timetable.timetable.EventItem;
import com.stupidtree.hita.timetable.timetable.EventItemHolder;
import com.stupidtree.hita.online.HITAUser;
import com.stupidtree.hita.util.ColorBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.stupidtree.hita.HITAApplication.defaultSP;
import static com.stupidtree.hita.HITAApplication.mDBHelper;

public class Subject implements Comparable{
    private String code;
    private String name;
    private String teacher;
    private HashMap<Integer, Double> ratingMap;
    private HashMap<String, String> Scores;
    private boolean isMOOC = false;
    private boolean exam = false;
    private boolean Default = true;
    private String xnxq = "无数据";
    private String school = "无数据";
    private String credit = "无数据";
    private String compulsory = "无数据";
    private String totalCourses = "无数据";
    private String type = "无数据";
    private String curriculumCode;
    private HITAUser hitaUser;

    public void setHitaUser(HITAUser hitaUser) {
        this.hitaUser = hitaUser;
    }

   

    public Subject(String curriculumCode, String name, String teacher) {
        ratingMap = new HashMap<>();
        Scores = new HashMap<>();
        this.name = name;
        this.teacher = teacher;
        this.curriculumCode = curriculumCode;
        if(defaultSP.getInt("color:"+name,-1)==-1){
            defaultSP.edit().putInt("color:"+name, ColorBox.getRandomColor_Material()).apply();
        } }

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
        if(defaultSP.getInt("color:"+name,-1)==-1){
            defaultSP.edit().putInt("color:"+name, ColorBox.getRandomColor_Material()).apply();
        }
    }

    public Subject(String x){
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
        code = jo.get("code")==null?null:jo.get("code").getAsString();
        curriculumCode = jo.get("curriculum_code").getAsString();
        getScroesFromString(jo.get("scores").toString());
        getRatesFromString(jo.get("rates").toString());
        if(jo.get("color")!=null){
            if(jo.get("color").getAsInt()!=-1){
                defaultSP.edit().putInt("color:"+name,jo.get("color").getAsInt()).apply();
            }else{
                defaultSP.edit().putInt("color:"+name, ColorBox.getRandomColor_Material()).apply();
            }
        }

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
        jo.addProperty("code", code);
        jo.addProperty("curriculum_code",curriculumCode);
        Gson gson = new Gson();
        jo.add("scores",gson.toJsonTree(Scores));
        jo.add("rates",gson.toJsonTree(ratingMap));
        jo.addProperty("color",defaultSP.getInt("color:"+name,-1));
        //return name+"##"+type+"##"+isMOOC+"##"+exam+"##"+Default+"##"+xnxq+"##"+school+"##"+ credit +"##"+ compulsory +"##"+totalCourses+"##"+code+"##"+curriculumCode+"##"+scoresToString()+"##"+ratesToString();
        return jo.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, code, curriculumCode);
    }

    @WorkerThread
    public void setRate(Integer courseNumber, Double rate) {
        ratingMap.put(courseNumber, rate);
        SQLiteDatabase sd = mDBHelper.getWritableDatabase();
        sd.replace("subject", "scores",getContentValues());
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
        cv.put("code",code);
        cv.put("curriculum_code", curriculumCode);
        cv.put("scores", scoresToString());
        cv.put("rates", ratesToString());
        return cv;
    }

    public ArrayList<EventItem> getCourses() {
        ArrayList<EventItem> result = new ArrayList<>();
        SQLiteDatabase sd = mDBHelper.getReadableDatabase();
        Cursor c = sd.query("timetable", null, "name=? and type=?",
                new String[]{name, TimetableCore.TIMETABLE_EVENT_TYPE_COURSE + ""}, null, null, null);
        while (c.moveToNext()) {
            EventItemHolder eih = new EventItemHolder(c);
            result.addAll(eih.getAllEvents());
        }
        c.close();
        return result;
    }

    @WorkerThread
    public EventItem getFirstCourse() {
        EventItem result = null;
        try {
            SQLiteDatabase sd = mDBHelper.getReadableDatabase();
            Cursor c = sd.query("timetable", null, "name=? and type=?",
                    new String[]{name, TimetableCore.TIMETABLE_EVENT_TYPE_COURSE + ""}, null, null, null);
            if (c.moveToNext()) {
                EventItemHolder eih = new EventItemHolder(c);
                result = eih.getAllEvents().get(0);
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public boolean isMOOC() {
        return isMOOC;
    }

    public void setMOOC(boolean MOOC) {
        isMOOC = MOOC;
    }

    public boolean isExam() {
        return exam;
    }

    public void setExam(boolean exam) {
        this.exam = exam;
    }

    public boolean isDefault() {
        return Default;
    }

    public void setDefault(boolean aDefault) {
        Default = aDefault;
    }

    public String getXnxq() {
        return xnxq;
    }

    public void setXnxq(String xnxq) {
        this.xnxq = xnxq;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getCredit() {
        return credit;
    }

    public void setCredit(String credit) {
        this.credit = credit;
    }

    public String getCompulsory() {
        return compulsory;
    }

    public void setCompulsory(String compulsory) {
        this.compulsory = compulsory;
    }

    public String getTotalCourses() {
        return totalCourses;
    }

    public void setTotalCourses(String totalCourses) {
        this.totalCourses = totalCourses;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCurriculumId() {
        return curriculumCode;
    }

    public void setCurriculumCode(String curriculumCode) {
        this.curriculumCode = curriculumCode;
    }

    
    @WorkerThread
    public void addScore(final String name, String score) {
        Scores.put(name, score);
        SQLiteDatabase sd = mDBHelper.getWritableDatabase();
        sd.replace("subject", "rates",getContentValues());
        Log.e("addScore", name + "," + score);
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


    public HITAUser getHitaUser() {
        return hitaUser;
    }

    @Override
    public int compareTo(Object o) {
        return (int) (getPriority()-((Subject)o).getPriority());
    }
}
