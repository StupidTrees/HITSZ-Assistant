package com.stupidtree.hita.core;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stupidtree.hita.core.timetable.EventItem;
import com.stupidtree.hita.core.timetable.HTime;
import com.stupidtree.hita.online.HITAUser;

import java.io.Serializable;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cn.bmob.v3.BmobObject;

import static com.stupidtree.hita.HITAApplication.mDBHelper;

/*课表类*/
public class Curriculum extends BmobObject {
    public int start_year;
    public int start_month;
    public int start_day;
    public int totalWeeks; //最大周数
    public String name; //课表名称
    public String curriculumCode;
    public String curriculumText;
    public String subjectsText;
    HITAUser hitaUser;

    public Curriculum(int sY, int sM, int sD, String name) {
        this.name = name;
        int y, m, d;
        Calendar c = Calendar.getInstance();
        c.set(sY, sM - 1, sD);
        switch (c.get(Calendar.DAY_OF_WEEK)) {
            case 1:
                c.add(Calendar.DATE, -6);
                break;
            case 2:
                break;
            case 3:
                c.add(Calendar.DATE, -1);
                break;
            case 4:
                c.add(Calendar.DATE, -2);
                break;
            case 5:
                c.add(Calendar.DATE, -3);
                break;
            case 6:
                c.add(Calendar.DATE, -4);
                break;
            case 7:
                c.add(Calendar.DATE, -5);
                break;
        }
        y = c.get(Calendar.YEAR);
        m = c.get(Calendar.MONTH) + 1;
        d = c.get(Calendar.DAY_OF_MONTH);
        totalWeeks = 0;
        start_year = y;
        start_month = m;
        start_day = d;
    }
    public Curriculum(String CurriculumString) {
//        String[] txts = CurriculumString.split("@@",-1);//-1表示支持空串
//        start_year = Integer.parseInt(txts[0]);
//        start_month = Integer.parseInt(txts[1]);
//        start_day = Integer.parseInt(txts[2]);
//        name =txts[4];
//        curriculumCode = txts[5];
//        curriculumText = txts[6];
//        subjectsText = txts[7];
//        totalWeeks = Integer.parseInt(txts[3]);

        JsonObject jo= new JsonParser().parse(CurriculumString).getAsJsonObject();
        start_year = jo.get("start_year").getAsInt();
        start_month = jo.get("start_month").getAsInt();
        start_day = jo.get("start_day").getAsInt();
        totalWeeks = jo.get("total_week").getAsInt();
        name = jo.get("name").getAsString();
        curriculumCode = jo.get("curriculum_code").getAsString();
        curriculumText  = jo.get("curriculum_text").getAsString();
        subjectsText = jo.get("subjects").toString();
    }
    public Curriculum(Cursor cur) {
        int sY = cur.getInt(3);
        int sM = cur.getInt(4);
        int sD = cur.getInt(5);
        name = cur.getString(0);
        totalWeeks = cur.getInt(2);
        curriculumText = cur.getString(6);
        curriculumCode = cur.getString(1);
        int y, m, d;
        Calendar c = Calendar.getInstance();
        c.set(sY, sM - 1, sD);
        switch (c.get(Calendar.DAY_OF_WEEK)) {
            case 1:
                c.add(Calendar.DATE, -6);
                break;
            case 2:
                break;
            case 3:
                c.add(Calendar.DATE, -1);
                break;
            case 4:
                c.add(Calendar.DATE, -2);
                break;
            case 5:
                c.add(Calendar.DATE, -3);
                break;
            case 6:
                c.add(Calendar.DATE, -4);
                break;
            case 7:
                c.add(Calendar.DATE, -5);
                break;
        }
        y = c.get(Calendar.YEAR);
        m = c.get(Calendar.MONTH) + 1;
        d = c.get(Calendar.DAY_OF_MONTH);
        start_day = d;
        start_month = m;
        start_year = y;
    }
    public void setHitaUser(HITAUser hitaUser) {
        this.hitaUser = hitaUser;
    }
    public void setCurriculumCode(String code){
        curriculumCode = code;
    }

    public Subject getSubjectByCourse(EventItem ei){
        //Log.e("getSubjectByCourse",ei.mainName);
        //Log.e("subjects:", String.valueOf(Subjects));
        SQLiteDatabase sd = mDBHelper.getReadableDatabase();
        Cursor c = sd.query("subject",null,"name=? and curriculum_code=?",new String[]{ei.mainName,curriculumCode},null,null,null);
        if (c.moveToNext()){
            Subject s = new Subject(c);
            c.close();
            return s;
        }
        return null;
    }
    public Subject getSubjectByName(String name){
        SQLiteDatabase sd = mDBHelper.getReadableDatabase();
        Cursor c = sd.query("subject",null,"name=? and curriculum_code=?",new String[]{name,curriculumCode},null,null,null);
        while (c.moveToNext()){
            Subject s = new Subject(c);
            c.close();
            return s;
        }
        return null;
    }
    public Subject getSubjectByCourseCode(String code){
        SQLiteDatabase sd = mDBHelper.getReadableDatabase();
        Cursor c = sd.query("subject",null,"code=? and curriculum_code=?",new String[]{code,curriculumCode},null,null,null);
        while (c.moveToNext()){
            Subject s = new Subject(c);
            c.close();
            return s;
        }
        return null;
    }

    public List<Subject> getSubjectsByCourseCode(String code){
        List<Subject> result = new ArrayList<>();
        SQLiteDatabase sd = mDBHelper.getReadableDatabase();
        Cursor c = sd.query("subject",null,"code=? and curriculum_code=?",new String[]{code,curriculumCode},null,null,null);
        while (c.moveToNext()){
            Subject s = new Subject(c);
            result.add(s);
        }
        c.close();
        return result;
    }
    /*函数功能：传入日历对象，获取该日期在本课表中对应的周数*/
    public int getWeekOfTerm(Calendar c) {
        Calendar start = Calendar.getInstance();
        start.set(start_year, start_month - 1, start_day, 0, 0, 0);
        HDate temp = new HDate(c);
        if (temp.compareTo(new HDate(start_year,start_month,start_day)) < 0 ) {
            return -1;
        }else {
            double tempDay = (c.getTimeInMillis() - start.getTimeInMillis()) / (1000 * 3600 * 24);
            return (int) (tempDay / 7) + 1;

        }
    }
    public ArrayList<Subject> getSubjects(){
        ArrayList<Subject> res = new ArrayList<>();
        SQLiteDatabase sd = mDBHelper.getReadableDatabase();
        try {
            Cursor c = sd.query("subject",null,"curriculum_code=?",new String[]{curriculumCode},null,null,null);
            while (c.moveToNext()){
                Subject s = new Subject(c);
                res.add(s);
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
            sd.delete("subject",null,null);
        }
        return res;
    }

    public ArrayList<Subject> getSubjects_Exam(){
        ArrayList<Subject> res = new ArrayList<>();
        SQLiteDatabase sd = mDBHelper.getReadableDatabase();
        try {
            Cursor c = sd.query("subject",null,"curriculum_code=? and is_exam = ?",new String[]{curriculumCode, 1+""},null,null,null);
            while (c.moveToNext()){
                Subject s = new Subject(c);
                res.add(s);
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
            sd.delete("subject",null,null);
        }
        return res;
    }
    public ArrayList<Subject> getSubjects_No_Exam(){
        ArrayList<Subject> res = new ArrayList<>();
        SQLiteDatabase sd = mDBHelper.getReadableDatabase();
        try {
            Cursor c = sd.query("subject",null,"curriculum_code=? and is_exam = ?",new String[]{curriculumCode, 0+""},null,null,null);
            while (c.moveToNext()){
                Subject s = new Subject(c);
                res.add(s);
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
            sd.delete("subject",null,null);
        }
        return res;
    }
    public ArrayList<Subject> getSubjects_Mooc(){
        ArrayList<Subject> res = new ArrayList<>();
        SQLiteDatabase sd = mDBHelper.getReadableDatabase();
        try {
            Cursor c = sd.query("subject",null,"curriculum_code=? and is_mooc = ?",new String[]{curriculumCode, 1+""},null,null,null);
            while (c.moveToNext()){
                Subject s = new Subject(c);
                res.add(s);
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
            sd.delete("subject",null,null);
        }
        return res;
    }
    public ArrayList<Subject> getSubjects_Comp(){
        ArrayList<Subject> res = new ArrayList<>();
        SQLiteDatabase sd = mDBHelper.getReadableDatabase();
        try {
            Cursor c = sd.query("subject",null,"curriculum_code=? and compulsory = ?",new String[]{curriculumCode, "必修"},null,null,null);
            while (c.moveToNext()){
                Subject s = new Subject(c);
                res.add(s);
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
            sd.delete("subject",null,null);
        }
        return res;
    }
    public ArrayList<Subject> getSubjects_Alt(){
        ArrayList<Subject> res = new ArrayList<>();
        SQLiteDatabase sd = mDBHelper.getReadableDatabase();
        try {
            Cursor c = sd.query("subject",null,"curriculum_code=? and compulsory = ?",new String[]{curriculumCode, "选修"},null,null,null);
            while (c.moveToNext()){
                Subject s = new Subject(c);
                res.add(s);
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
            sd.delete("subject",null,null);
        }
        return res;
    }
    public ArrayList<Subject> getSubjects_WTV(){
        ArrayList<Subject> res = new ArrayList<>();
        SQLiteDatabase sd = mDBHelper.getReadableDatabase();
        try {
            Cursor c = sd.query("subject",null,"curriculum_code=? and compulsory = ?",new String[]{curriculumCode, "任选"},null,null,null);
            while (c.moveToNext()){
                Subject s = new Subject(c);
                res.add(s);
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
            sd.delete("subject",null,null);
        }
        return res;
    }
    public static ArrayList<Subject> getSubjects(String curriculumCode){
        ArrayList<Subject> res = new ArrayList<>();
        SQLiteDatabase sd = mDBHelper.getReadableDatabase();
        try {
            Cursor c = sd.query("subject",null,"curriculum_code=?",new String[]{curriculumCode},null,null,null);
            while (c.moveToNext()){
                Subject s = new Subject(c);
                res.add(s);
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
            sd.delete("subject",null,null);
        }
        return res;
    }

    /*函数功能：获取某一周第一天的日期*/
    public Calendar getFirstDateAtWOT(int WeekOfTerm) {
        if(WeekOfTerm>totalWeeks) WeekOfTerm = totalWeeks;
        Calendar temp = Calendar.getInstance();
        int daysToPlus = (WeekOfTerm - 1) * 7;
        temp.set(start_year, start_month - 1, start_day);
        temp.add(Calendar.DATE, daysToPlus);
        return temp;
    }

    public Calendar getDateAtWOT(int WeekOfTerm, int DOW) {
        Calendar temp = Calendar.getInstance();
        int daysToPlus = (WeekOfTerm - 1) * 7;
        temp.set(start_year, start_month - 1,start_day);
        temp.add(Calendar.DATE, daysToPlus);
        temp.add(Calendar.DAY_OF_MONTH, DOW - 1);
        return temp;
    }
    public Calendar getDateAt(int WeekOfTerm, int DOW, HTime time) {
        Calendar temp = Calendar.getInstance();
        int daysToPlus = (WeekOfTerm - 1) * 7;
        temp.set(start_year, start_month - 1,start_day);
        temp.add(Calendar.DATE, daysToPlus);
        temp.add(Calendar.DAY_OF_MONTH, DOW - 1);
        temp.set(Calendar.HOUR_OF_DAY,time.hour);
        temp.set(Calendar.MINUTE,time.minute);
        return temp;
    }

    /*函数功能：判断某年某月某日是否在这个课表的时间范围内*/
    public boolean Within(int year, int month, int day) {
        if (new HDate(year, month, day).compareTo(this.new HDate(start_year,start_month,start_day)) < 0) return false;
        return new HDate(year, month, day).weekOfTerm <= this.totalWeeks;
    }

    public String readStartDate(){
        return start_year+"年"+start_month+"月"+start_day+"日";
    }



    public class HDate implements Comparable {
        int year;
        int month;
        int dayOfMonth;
        int number;
        int dayOfWeek;
        int weekOfTerm;

        HDate(int year, int month, int dOM) {
            Calendar c = Calendar.getInstance();
            c.set(year, month - 1, dOM);
            Calendar start = Calendar.getInstance();
            start.set(start_year, start_month - 1, start_day);
            long tempDay = (c.getTimeInMillis() - start.getTimeInMillis()) / (1000 * 3600 * 24);
            weekOfTerm = (int) (tempDay / 7) + 1;
            this.year = year;
            this.month = month;
            dayOfMonth = dOM;
            dayOfWeek = c.get(Calendar.DAY_OF_WEEK) == 1 ? 7 : c.get(Calendar.DAY_OF_WEEK) - 1;


        }

        HDate(Calendar c) {
            Calendar start = Calendar.getInstance();
            start.set(start_year, start_month - 1, start_day);
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH) + 1;
            dayOfWeek = c.get(Calendar.DAY_OF_WEEK) == 1 ? 7 : c.get(Calendar.DAY_OF_WEEK) - 1;
            dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
            long tempDay = (c.getTimeInMillis() - start.getTimeInMillis()) / (1000 * 3600 * 24);
            weekOfTerm = (int) (tempDay / 7) + 1;
        }

        public String readDate() {
            return year + "年" + month + "月" + dayOfMonth + "日";
        }

        @Override
        public int compareTo(Object o) {
            if (this.year > ((HDate) o).year) return 1;
            else if (this.year < ((HDate) o).year) return -1;
            else if (this.month > ((HDate) o).month) return 1;
            else if (this.month < ((HDate) o).month) return -1;
            else if (this.dayOfMonth > ((HDate) o).dayOfMonth) return 1;
            else if (this.dayOfMonth < ((HDate) o).dayOfMonth) return -1;
            else return 0;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + dayOfMonth;
            result = prime * result + month;
            result = prime * result + year;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            HDate other = (HDate) obj;
            if (dayOfMonth != other.dayOfMonth)
                return false;
            if (month != other.month)
                return false;
            return year == other.year;
        }

    }
    public ContentValues getContentValues(){
        ContentValues cv = new ContentValues();
        cv.put("name",name);
        cv.put("curriculum_code",curriculumCode);
        cv.put("start_year",start_year);
        cv.put("start_month",start_month);
        cv.put("start_day",start_day);
        cv.put("total_weeks",totalWeeks);
        cv.put("curriculum_text",curriculumText);
        return cv;
    }

    public void setSubjectsText() {
       // StringBuilder sb = new StringBuilder();
        List<Subject> l = getSubjects();
//        for(int i=0;i<l.size();i++){
//            String rex = (i==l.size()-1)?"":"///";
//            sb.append(l.get(i).toString()).append(rex); //不可以用+=拼接！！！
//        }
        JsonObject jo = new JsonObject();
        JsonParser jp = new JsonParser();
        for(Subject s:l){
            jo.add(s.name,jp.parse(s.toString()));
            //jo.addProperty(s.name,s.toString());
        }
        subjectsText = jo.toString();
    }
    public ArrayList<Subject> getSubjectsFromString(){
        ArrayList<Subject> res = new ArrayList<>();
        JsonObject jo = new JsonParser().parse(subjectsText).getAsJsonObject();
        for(Map.Entry e :jo.entrySet()){
            res.add(new Subject(e.getValue().toString()));
        }
        return res;
    }


    @Override
    public String toString() {
        JsonObject jo = new JsonObject();
        JsonParser jp = new JsonParser();
        jo.addProperty("start_year",start_year);
        jo.addProperty("start_month",start_month);
        jo.addProperty("start_day",start_day);
        jo.addProperty("total_week",totalWeeks);
        jo.addProperty("name",name);
        jo.addProperty("curriculum_code",curriculumCode);
        jo.addProperty("curriculum_text",curriculumText);
        jo.add("subjects",jp.parse(subjectsText));
        return jo.toString();
       // return  start_year+"@@"+start_month+"@@"+start_day+"@@"+totalWeeks+"@@"+name+"@@"+curriculumCode+"@@"+curriculumText+"@@"+subjectsText;
    }
}






