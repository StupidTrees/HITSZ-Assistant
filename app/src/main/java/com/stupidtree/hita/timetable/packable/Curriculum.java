package com.stupidtree.hita.timetable.packable;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.google.gson.Gson;
import com.stupidtree.hita.timetable.TimetableCore;
import com.stupidtree.hita.util.DeflaterUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.timetable.TimetableCore.COURSE;
import static com.stupidtree.hita.timetable.TimetableCore.uri_timetable;

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



    public void setCurriculumCode(String code) {
        curriculumCode = code;
    }

    @WorkerThread
    public void generateCurriculumText() {
        Cursor c = HContext.getContentResolver().query(uri_timetable, null, "curriculum_code=? and type=?", new String[]{curriculumCode, String.valueOf(COURSE)}, null, null);
        List<EventItemHolder> eihs = new ArrayList<>();
        while (c != null && c.moveToNext()) {
            eihs.add(new EventItemHolder(c));
        }
        c.close();
        List<Map<String, String>> data = new ArrayList<>();
        for (EventItemHolder eih : eihs) {
            Map<String, String> m = new HashMap<>();
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








    public int getWeekOfTerm(Calendar c) {
        HDate temp = new HDate(c);
        if (temp.compareTo(new HDate(startDate)) < 0) {
            return -1;
        } else {
            double tempDay = (c.getTimeInMillis() - startDate.getTimeInMillis()) / (1000.0 * 3600 * 24);
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
            dayOfWeek = c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ? 7 : c.get(Calendar.DAY_OF_WEEK) - 1;


        }

        HDate(Calendar c) {
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH) + 1;
            dayOfWeek = c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ? 7 : c.get(Calendar.DAY_OF_WEEK) - 1;
            dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
            long tempDay = (c.getTimeInMillis() - startDate.getTimeInMillis()) / (1000 * 3600 * 24);
            weekOfTerm = (int) (tempDay / 7) + 1;
        }

        public String readDate() {
            return year + "年" + month + "月" + dayOfMonth + "日";
        }

        @Override
        public int compareTo(@NonNull Object o) {
            if (this.year > ((HDate) o).year) return 1;
            else if (this.year < ((HDate) o).year) return -1;
            else if (this.month > ((HDate) o).month) return 1;
            else if (this.month < ((HDate) o).month) return -1;
            else return Integer.compare(this.dayOfMonth, ((HDate) o).dayOfMonth);
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




    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Curriculum that = (Curriculum) o;
        return curriculumCode.equals(that.getCurriculumCode());
    }


    @NonNull
    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this); }
}






