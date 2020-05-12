package com.stupidtree.hita.timetable.packable;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stupidtree.hita.timetable.TimetableCore;
import com.stupidtree.hita.util.ColorBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.defaultSP;
import static com.stupidtree.hita.timetable.TimetableCore.uri_subject;
//import static com.stupidtree.hita.HITAApplication.mDBHelper;

public class Subject implements Comparable {
    public static final String TAG = "SUBJECT_TYPE_TAG";
    private String code;
    private String id;
    private String name;
    private String teacher;
    private HashMap<Integer, Double> ratingMap;
    private HashMap<String, String> Scores;
    private boolean isMOOC = false;
    private boolean exam = false;
    private boolean Default = true;
    private String xnxq = "";
    private String school = "";
    private String credit = "";
    private String compulsory = "";
    private String totalCourses = "";
    private String type = "";
    private String curriculumCode;
    private String uuid;


    public Subject(String curriculumCode, String name, String teacher) {
        ratingMap = new HashMap<>();
        Scores = new HashMap<>();
        this.name = name;
        this.teacher = teacher;
        this.curriculumCode = curriculumCode;
        this.uuid = UUID.randomUUID().toString();
        if (defaultSP.getInt("color:" + name, -1) == -1) {
            defaultSP.edit().putInt("color:" + name, ColorBox.getRandomColor_Material()).apply();
        }
    }

    @WorkerThread
    public Subject(Cursor c) {
        ratingMap = new HashMap<>();
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
        id = c.getString(c.getColumnIndex("id"));
        curriculumCode = c.getString(c.getColumnIndex("curriculum_code"));
        uuid = c.getString(c.getColumnIndex("uuid"));
        if (defaultSP.getInt("color:" + name, -1) == -1) {
            defaultSP.edit().putInt("color:" + name, ColorBox.getRandomColor_Material()).apply();
        }
        if (TextUtils.isEmpty(uuid)) {
            uuid = String.valueOf(UUID.randomUUID());
        }

    }

    public static Subject getTagInstance(String name) {
        Subject s = new Subject("", name, "");
        s.setType(TAG);
        return s;
    }


    @NonNull
    @Override
    public String toString() {
        Gson gson = new Gson();
        JsonObject jo = gson.toJsonTree(this, Subject.class).getAsJsonObject();
        jo.add("scores", gson.toJsonTree(Scores));
        jo.add("rates", gson.toJsonTree(ratingMap));
        jo.addProperty("color", defaultSP.getInt("color:" + name, -1));
        return jo.toString();
    }
//
//    @WorkerThread
//    public void setRate(Integer courseNumber, Double rate) {
//        ratingMap.put(courseNumber, rate);
//        SQLiteDatabase sd = mDBHelper.getWritableDatabase();
//        sd.replace("subject", "scores", getContentValues());
//    }

    public Double getRate(int number) {
        if (ratingMap.get(number) == null) return 0.0;
        return ratingMap.get(number);
    }

    public String getUUID() {
        return uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

//    public double getRank() {
//        Double rateSum = 0.0;
//        for (Double f : ratingMap.values()) {
//            rateSum += f;
//        }
//        rateSum /= ratingMap.size();
//        float Point = 0;
//        try {
//        } catch (Exception e) {
//            Point = 2.0f;
//        }
//        return Point * rateSum;
//    }

    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        if (name == null) name = " ";
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
        cv.put("id", id);
        cv.put("uuid", uuid);
        return cv;
    }





    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subject subject = (Subject) o;
        return curriculumCode.equals(subject.getCurriculumId()) && uuid.equals(subject.getUUID());
    }


    private String scoresToString() {
//        StringBuilder sb = new StringBuilder();
//        for(Map.Entry e:Scores.entrySet()){
//            sb.append(e.getKey()).append("__").append(e.getValue()).append("&&&");
//        }
//        return sb.toString();
        Gson gson = new Gson();
        Log.e("scores", String.valueOf(Scores));
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
        for (Map.Entry<String, JsonElement> e : jo.entrySet()) {
            Scores.put(e.getKey(), e.getValue().getAsString());
        }
    }

    private String ratesToString() {
        Gson gson = new Gson();
        return gson.toJson(ratingMap);
    }

    private void getRatesFromString(String s) {
        JsonParser jp = new JsonParser();
        JsonObject jo = jp.parse(s).getAsJsonObject();
        for (Map.Entry e : jo.entrySet()) {
            ratingMap.put(Integer.parseInt(e.getKey().toString()), Double.valueOf(e.getValue().toString()));
        }
    }

    public String getId() {
        return id;
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
        HContext.getContentResolver().update(uri_subject,  getContentValues(),"uuid=?",new String[]{getUUID()});
    }

    public HashMap<String, String> getScores() {
        return Scores;
    }

    public HashMap<Integer, Double> getRatingMap() {
        return ratingMap;
    }

    public void setId(String id) {
        this.id = id;
    }

    public float getPriority() {
        float creditF, rate;
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
        if (size == 0) rate = 0f;
        else rate = (float) (sum / size);
        return 100 + creditF * 5 - rate * 20;

    }


    @Override
    public int compareTo(@NonNull Object o) {
        return (int) (getPriority() - ((Subject) o).getPriority());
    }


}
