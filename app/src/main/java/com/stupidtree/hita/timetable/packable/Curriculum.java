package com.stupidtree.hita.timetable.packable;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.WorkerThread;

import com.google.gson.Gson;
import com.stupidtree.hita.timetable.TimetableCore;
import com.stupidtree.hita.util.DeflaterUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.stupidtree.hita.HITAApplication.mDBHelper;
import static com.stupidtree.hita.timetable.TimetableCore.COURSE;

/*课表类*/
public class Curriculum {
    private Calendar startDate;
    private int totalWeeks; //最大周数
    private String name; //课表名称
    private String curriculumCode;
    private String curriculumText;


    public Curriculum(Calendar c, String name) {
        this.name = name;
        startDate = Calendar.getInstance();
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
        totalWeeks = 0;
        startDate.setTimeInMillis(c.getTimeInMillis());
    }

    //    public Curriculum(String CurriculumString) {
//        JsonObject jo= new JsonParser().parse(CurriculumString).getAsJsonObject();
//        startDate = Calendar.getInstance();
//        startDate.set(jo.get("start_year").getAsInt(),jo.get("start_month").getAsInt()-1,jo.get("start_day").getAsInt());
//        totalWeeks = jo.get("total_week").getAsInt();
//        name = jo.get("name").getAsString();
//        curriculumCode = jo.get("curriculum_code").getAsString();
//        curriculumText  = jo.get("curriculum_text").getAsString();
//       // subjectsText = jo.get("subjects").toString();
//    }
    public Curriculum(Cursor cur) {
        int sY = cur.getInt(cur.getColumnIndex("start_year"));
        int sM = cur.getInt(cur.getColumnIndex("start_month"));
        int sD = cur.getInt(cur.getColumnIndex("start_day"));
        name = cur.getString(cur.getColumnIndex("name"));
        totalWeeks = cur.getInt(cur.getColumnIndex("total_weeks"));
        curriculumText = cur.getString(cur.getColumnIndex("curriculum_text"));
        curriculumCode = cur.getString(cur.getColumnIndex("curriculum_code"));
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
        startDate = c;
    }

    @WorkerThread
    public static ArrayList<Subject> getSubjects(String curriculumCode) {
        ArrayList<Subject> res = new ArrayList<>();
        SQLiteDatabase sd = mDBHelper.getReadableDatabase();
        try {
            Cursor c = sd.query("subject", null, "curriculum_code=?", new String[]{curriculumCode}, null, null, null);
            while (c.moveToNext()) {
                Subject s = new Subject(c);
                res.add(s);
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
            sd.delete("subject", null, null);
        }
        return res;
    }

    public void setCurriculumCode(String code) {
        curriculumCode = code;
    }

    @WorkerThread
    public void generateCurriculumText() {
        SQLiteDatabase sqd = mDBHelper.getReadableDatabase();
        Cursor c = sqd.query("timetable", null, "curriculum_code=? and type=?", new String[]{curriculumCode, String.valueOf(COURSE)}, null, null, null);
        List<EventItemHolder> eihs = new ArrayList<>();
        while (c.moveToNext()) {
            eihs.add(new EventItemHolder(c));
        }
        c.close();
        List<Map<String, String>> data = new ArrayList<>();
        for (EventItemHolder eih : eihs) {
            Map<String, String> m = new HashMap();
            m.put("name", eih.getMainName());
            m.put("teacher", eih.tag3);
            m.put("dow", String.valueOf(eih.DOW));
            m.put("classroom", eih.tag2);
            int begin = TimetableCore.getNumberAtTime(eih.startTime);
            int end = TimetableCore.getNumberAtTime(eih.endTime);
            int last = end - begin + 1;
            m.put("begin", String.valueOf(begin));
            m.put("last", String.valueOf(last));
            m.put("weeks", eih.getWeeksText());
            data.add(m);
        }
        String curriculumText = new Gson().toJson(data);
        Log.e(name + ".Text", curriculumText);
        this.curriculumText = DeflaterUtils.zipString(curriculumText);
    }

    @WorkerThread
    public Subject getSubjectByCourse(EventItem ei) {
        return getSubjectByName(ei.getMainName());
    }

    @WorkerThread
    public Subject getSubjectByName(String name) {
        SQLiteDatabase sd = mDBHelper.getReadableDatabase();
        Cursor c = sd.query("subject", null, "name=? and curriculum_code=?", new String[]{name, curriculumCode}, null, null, null);
        if (c.moveToNext()) {
            Subject s = new Subject(c);
            c.close();
            return s;
        }
        c.close();
        //找不到，则必须重新生成科目表了
        recreateSubjects();
        Cursor c2 = sd.query("subject", null, "name=? and curriculum_code=?", new String[]{name, curriculumCode}, null, null, null);
        if (c2.moveToNext()) {
            Subject s = new Subject(c2);
            c2.close();
            return s;
        }
        c2.close();
        return null;
    }

    @WorkerThread
    public Subject getSubjectByCourseCode(String code) {
        SQLiteDatabase sd = mDBHelper.getReadableDatabase();
        Cursor c = sd.query("subject", null, "code=? and curriculum_code=?", new String[]{code, curriculumCode}, null, null, null);
        while (c.moveToNext()) {
            Subject s = new Subject(c);
            c.close();
            return s;
        }
        return null;
    }

    @WorkerThread
    public List<Subject> getSubjectsByCourseCode(String code) {
        List<Subject> result = new ArrayList<>();
        SQLiteDatabase sd = mDBHelper.getReadableDatabase();
        Cursor c = sd.query("subject", null, "code=? and curriculum_code=?", new String[]{code, curriculumCode}, null, null, null);
        while (c.moveToNext()) {
            Subject s = new Subject(c);
            result.add(s);
        }
        c.close();
        return result;
    }

    @WorkerThread
    public ArrayList<Subject> getSubjects() {
        ArrayList<Subject> res = new ArrayList<>();
        SQLiteDatabase sd = mDBHelper.getReadableDatabase();
        try {
            Cursor c = sd.query("subject", null, "curriculum_code=?", new String[]{curriculumCode}, null, null, null);
            while (c.moveToNext()) {
                Subject s = new Subject(c);
                res.add(s);
            }
            c.close();
            if (res.size() == 0) {
                Cursor c2 = sd.query("timetable", null, "curriculum_code=? AND type=?", new String[]{curriculumCode, String.valueOf(COURSE)}, null, null, null);
                if (c2.moveToNext()) { //如果科目表空，但是时间表里存在同名课程的话，说明科目需要重新创建
                    res.addAll(recreateSubjects());
                }
                c2.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            sd.delete("subject", null, null);
        }
        return res;
    }

    @WorkerThread
    private List<Subject> recreateSubjects() {
        SQLiteDatabase sd = mDBHelper.getWritableDatabase();
        sd.delete("subject", "curriculum_code=?", new String[]{curriculumCode});
        Cursor c = sd.query("timetable", null, "curriculum_code=? AND type=?", new String[]{curriculumCode, String.valueOf(COURSE)}, null, null, null);
        List<Subject> subjects = new ArrayList<>();
        while (c.moveToNext()) {
            EventItemHolder eih = new EventItemHolder(c);
            String name = eih.getMainName();
            boolean contains = false;
            for (Subject s : subjects) {
                if (s.getName().equals(name)) contains = true;
            }
            if (!contains) {
                subjects.add(new Subject(curriculumCode, name, eih.tag3));
            }
        }
        for (Subject s : subjects) {
            sd.insert("subject", null, s.getContentValues());
        }
        return subjects;
    }

    @WorkerThread
    public ArrayList<Subject> getSubjects_Exam() {
        ArrayList<Subject> res = new ArrayList<>();
        SQLiteDatabase sd = mDBHelper.getReadableDatabase();
        try {
            Cursor c = sd.query("subject", null, "curriculum_code=? and is_exam = ?", new String[]{curriculumCode, 1 + ""}, null, null, null);
            while (c.moveToNext()) {
                Subject s = new Subject(c);
                res.add(s);
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
            sd.delete("subject", null, null);
        }
        return res;
    }

    @WorkerThread
    public ArrayList<Subject> getSubjects_No_Exam() {
        ArrayList<Subject> res = new ArrayList<>();
        SQLiteDatabase sd = mDBHelper.getReadableDatabase();
        try {
            Cursor c = sd.query("subject", null, "curriculum_code=? and is_exam = ?", new String[]{curriculumCode, 0 + ""}, null, null, null);
            while (c.moveToNext()) {
                Subject s = new Subject(c);
                res.add(s);
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
            sd.delete("subject", null, null);
        }
        return res;
    }

    @WorkerThread
    public ArrayList<Subject> getSubjects_Mooc() {
        ArrayList<Subject> res = new ArrayList<>();
        SQLiteDatabase sd = mDBHelper.getReadableDatabase();
        try {
            Cursor c = sd.query("subject", null, "curriculum_code=? and is_mooc = ?", new String[]{curriculumCode, 1 + ""}, null, null, null);
            while (c.moveToNext()) {
                Subject s = new Subject(c);
                res.add(s);
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
            sd.delete("subject", null, null);
        }
        return res;
    }

    @WorkerThread
    public ArrayList<Subject> getSubjects_Comp() {
        ArrayList<Subject> res = new ArrayList<>();
        SQLiteDatabase sd = mDBHelper.getReadableDatabase();
        try {
            Cursor c = sd.query("subject", null, "curriculum_code=? and compulsory = ?", new String[]{curriculumCode, "必修"}, null, null, null);
            while (c.moveToNext()) {
                Subject s = new Subject(c);
                res.add(s);
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
            sd.delete("subject", null, null);
        }
        return res;
    }

    @WorkerThread
    public ArrayList<Subject> getSubjects_Alt() {
        ArrayList<Subject> res = new ArrayList<>();
        SQLiteDatabase sd = mDBHelper.getReadableDatabase();
        try {
            Cursor c = sd.query("subject", null, "curriculum_code=? and compulsory = ?", new String[]{curriculumCode, "选修"}, null, null, null);
            while (c.moveToNext()) {
                Subject s = new Subject(c);
                res.add(s);
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
            sd.delete("subject", null, null);
        }
        return res;
    }

    @WorkerThread
    public ArrayList<Subject> getSubjects_WTV() {
        ArrayList<Subject> res = new ArrayList<>();
        SQLiteDatabase sd = mDBHelper.getReadableDatabase();
        try {
            Cursor c = sd.query("subject", null, "curriculum_code=? and compulsory = ?", new String[]{curriculumCode, "任选"}, null, null, null);
            while (c.moveToNext()) {
                Subject s = new Subject(c);
                res.add(s);
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
            sd.delete("subject", null, null);
        }
        return res;
    }

    public int getWeekOfTerm(Calendar c) {
        HDate temp = new HDate(c);
        if (temp.compareTo(new HDate(startDate)) < 0) {
            return -1;
        } else {
            double tempDay = (c.getTimeInMillis() - startDate.getTimeInMillis()) / (1000 * 3600 * 24);
            return (int) (tempDay / 7) + 1;
        }
    }


    /*函数功能：获取某一周第一天的日期*/
    public Calendar getFirstDateAtWOT(int WeekOfTerm) {
        if (WeekOfTerm > totalWeeks) WeekOfTerm = totalWeeks;
        Calendar temp = Calendar.getInstance();
        int daysToPlus = (WeekOfTerm - 1) * 7;
        temp.setTimeInMillis(startDate.getTimeInMillis());
        temp.add(Calendar.DATE, daysToPlus);
        return temp;
    }

    public Calendar getDateAtWOT(int WeekOfTerm, int DOW) {
        Calendar temp = Calendar.getInstance();
        int daysToPlus = (WeekOfTerm - 1) * 7;
        temp.setTimeInMillis(startDate.getTimeInMillis());
        temp.add(Calendar.DATE, daysToPlus);
        temp.add(Calendar.DAY_OF_MONTH, DOW - 1);
        return temp;
    }

    public Calendar getDateAt(int WeekOfTerm, int DOW, HTime time) {
        Calendar temp = Calendar.getInstance();
        int daysToPlus = (WeekOfTerm - 1) * 7;
        temp.setTimeInMillis(startDate.getTimeInMillis());
        temp.add(Calendar.DATE, daysToPlus);
        temp.add(Calendar.DAY_OF_MONTH, DOW - 1);
        temp.set(Calendar.HOUR_OF_DAY, time.hour);
        temp.set(Calendar.MINUTE, time.minute);
        return temp;
    }

    public Calendar getDateAt(EventItem ei) {
        Calendar temp = Calendar.getInstance();
        int daysToPlus = (ei.getWeek() - 1) * 7;
        temp.setTimeInMillis(startDate.getTimeInMillis());
        temp.add(Calendar.DATE, daysToPlus);
        temp.add(Calendar.DAY_OF_MONTH, ei.getDOW() - 1);
        temp.set(Calendar.HOUR_OF_DAY, ei.getStartTime().hour);
        temp.set(Calendar.MINUTE, ei.getStartTime().minute);
        return temp;
    }
    /*函数功能：判断某年某月某日是否在这个课表的时间范围内*/
    public boolean Within(int year, int month, int day) {
        if (new HDate(year, month, day).compareTo(this.new HDate(startDate)) < 0) return false;
        return new HDate(year, month, day).weekOfTerm <= this.totalWeeks;
    }

    public String readStartDate() {
        int y = startDate.get(Calendar.YEAR);
        int m = startDate.get(Calendar.MONTH) + 1;
        int d = startDate.get(Calendar.DAY_OF_MONTH);
        return y + "-" + m + "-" + d;
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

            long tempDay = (c.getTimeInMillis() - startDate.getTimeInMillis()) / (1000 * 3600 * 24);
            weekOfTerm = (int) (tempDay / 7) + 1;
            this.year = year;
            this.month = month;
            dayOfMonth = dOM;
            dayOfWeek = c.get(Calendar.DAY_OF_WEEK) == 1 ? 7 : c.get(Calendar.DAY_OF_WEEK) - 1;


        }

        HDate(Calendar c) {
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH) + 1;
            dayOfWeek = c.get(Calendar.DAY_OF_WEEK) == 1 ? 7 : c.get(Calendar.DAY_OF_WEEK) - 1;
            dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
            long tempDay = (c.getTimeInMillis() - startDate.getTimeInMillis()) / (1000 * 3600 * 24);
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

    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        if (startDate == null) startDate = Calendar.getInstance();
        int y = startDate.get(Calendar.YEAR);
        int m = startDate.get(Calendar.MONTH) + 1;
        int d = startDate.get(Calendar.DAY_OF_MONTH);
        cv.put("name", name);
        cv.put("curriculum_code", curriculumCode);
        cv.put("start_year", y);
        cv.put("start_month", m);
        cv.put("start_day", d);
        cv.put("total_weeks", totalWeeks);
        cv.put("curriculum_text", curriculumText == null ? "{}" : curriculumText);
        return cv;
    }

//    public void setSubjectsText() {
//        subjectList = new ArrayList<>();
//        subjectList.addAll(getSubjects());
//       // StringBuilder sb = new StringBuilder();
////        List<Subject> l = getSubjects();
//////        for(int i=0;i<l.size();i++){
//////            String rex = (i==l.size()-1)?"":"///";
//////            sb.append(l.get(i).toString()).append(rex); //不可以用+=拼接！！！
//////        }
////        JsonObject jo = new JsonObject();
////        JsonParser jp = new JsonParser();
////        for(Subject s:l){
////            jo.add(s.getName(),jp.parse(s.toString()));
////            //jo.addProperty(s.name,s.toString());
////        }
////        subjectsText = jo.toString();
//    }
//    public ArrayList<Subject> getSubjectsFromString(){
//        ArrayList<Subject> res = new ArrayList<>();
//        JsonObject jo = new JsonParser().parse(subjectsText).getAsJsonObject();
//        for(Map.Entry e :jo.entrySet()){
//            res.add(new Subject(e.getValue().toString()));
//        }
//        return res;
//    }

    public Calendar getStartDate() {
        return startDate;
    }

    public void setStartDate(Calendar date) {
        startDate.setTimeInMillis(date.getTimeInMillis());
    }


    public int getTotalWeeks() {
        return totalWeeks;
    }

    public void setTotalWeeks(int totalWeeks) {
        this.totalWeeks = totalWeeks;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCurriculumCode() {
        return curriculumCode;
    }

    public String getCurriculumText() {
        return curriculumText;
    }

    public void setCurriculumText(String curriculumText) {
        this.curriculumText = curriculumText;
    }


    @WorkerThread
    public void saveToDB() {
        SQLiteDatabase sd = mDBHelper.getWritableDatabase();
        if (sd.update("curriculum", getContentValues(), "curriculum_code=?", new String[]{getCurriculumCode()}) == 0) {
            sd.insert("curriculum", null, getContentValues());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Curriculum that = (Curriculum) o;
        return curriculumCode.equals(that.getCurriculumCode());
    }


    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
//        JsonObject jo = new JsonObject();
//        JsonParser jp = new JsonParser();
//        int y = startDate.get(Calendar.YEAR);
//        int m = startDate.get(Calendar.MONTH)+1;
//        int d = startDate.get(Calendar.DAY_OF_MONTH);
//        jo.addProperty("start_year",y);
//        jo.addProperty("start_month",m);
//        jo.addProperty("start_day",d);
//        jo.addProperty("total_week",totalWeeks);
//        jo.addProperty("name",name);
//        jo.addProperty("curriculum_code",curriculumCode);
//        jo.addProperty("curriculum_text",curriculumText);
//        jo.add("subjects",jp.parse(subjectsText));
//        return jo.toString();
        // return  start_year+"@@"+start_month+"@@"+start_day+"@@"+totalWeeks+"@@"+name+"@@"+curriculumCode+"@@"+curriculumText+"@@"+subjectsText;
    }
}






