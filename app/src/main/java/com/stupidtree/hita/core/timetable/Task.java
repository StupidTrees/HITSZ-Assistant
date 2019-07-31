package com.stupidtree.hita.core.timetable;

import android.content.ContentValues;
import android.database.Cursor;

import com.google.gson.Gson;

import java.util.Objects;

import static com.stupidtree.hita.HITAApplication.now;

public class Task {
    public String name;
    public  boolean has_deadline;
    private boolean every_day = false;
    private boolean has_length = false;
    private int length = -1;
    private int progress = -1;
    private int type = 0;
    private int priority = 0;
    public int fW,tW,fDOW,tDOW;
    public String curriculumCode;
    public String ddlName = "";
    public  HTime sTime = new HTime(0,0);
    public  HTime eTime = new HTime(0,0);

    public static String QUERY_SELECTION ="name=? AND has_ddl=? AND every_day=? AND has_length=?  AND type=?" +
            " AND curriculum_code=? ";

    public String[] getQueryParams(){
        return new String[]{
                name,(has_deadline?1:0)+"",( every_day?1:0)+"",(has_length?1:0)+"",
               type+"",curriculumCode
        };

    }
    public Task(String curriculumCode,String name){
        this.name = name;
        this.curriculumCode = curriculumCode;
        has_deadline = false;
        has_length = false;
        every_day = false;
    }
    public Task(String curriculumCode,String name,int fW,int fDOW,int tW,int tDOW,HTime sTime,HTime tTime,String ddlName){
        this.curriculumCode = curriculumCode;
        this.name = name;
        this.fW = fW;
        this.fDOW = fDOW;
        this.tW = tW;
        this.tDOW = tDOW;
        this.sTime = sTime;
        this.eTime = tTime;
        has_deadline = true;
        this.ddlName = ddlName;

    }
    public Task(String curriculumCode,String name,int fW,int fDOW,HTime sTime,EventItem DDL){
        this.curriculumCode = curriculumCode;
        this.name = name;
        this.fW = fW;
        this.fDOW = fDOW;
        this.tW = DDL.week;
        this.tDOW = DDL.DOW;
        this.sTime = sTime;
        this.eTime = DDL.startTime;
        this.ddlName = DDL.mainName;
        has_deadline = true;
    }
    public Task(Cursor c){
        this.curriculumCode = c.getString(c.getColumnIndex("curriculum_code"));
        this.name = c.getString(c.getColumnIndex("name"));
        this.fW = c.getInt(c.getColumnIndex("from_week"));
        this.fDOW = c.getInt(c.getColumnIndex("from_dow"));
        this.sTime = new HTime(c.getInt(c.getColumnIndex("from_hour")),c.getInt(c.getColumnIndex("from_minute")));
        this.tW = c.getInt(c.getColumnIndex("to_week"));
        this.tDOW = c.getInt(c.getColumnIndex("to_dow"));
        this.eTime = new HTime(c.getInt(c.getColumnIndex("to_hour")),c.getInt(c.getColumnIndex("to_minute")));
        this.ddlName = c.getString(c.getColumnIndex("ddl_name"));
        this.has_deadline = c.getInt(c.getColumnIndex("has_ddl"))!=0;
        has_length = c.getInt(c.getColumnIndex("has_length"))!=0;
        length = c.getInt(c.getColumnIndex("length"));
        progress = c.getInt(c.getColumnIndex("length"));
        type = c.getInt(c.getColumnIndex("type"));
        every_day = c.getInt(c.getColumnIndex("every_day"))!=0;
        priority = c.getInt(c.getColumnIndex("priority"));
    }
//

    public void arrangeTime(int fW,int fDOW,int tW,int tDOW,HTime sTime,HTime tTime,String ddlName){
        this.fW = fW;
        this.fDOW = fDOW;
        this.tW = tW;
        this.tDOW = tDOW;
        this.sTime = sTime;
        this.eTime = tTime;
        has_deadline = true;
        this.ddlName = ddlName;
    }
    public void setLength(int length){
        has_length = true;
        this.length = length;
        this.progress = 0;
    }

    public void setEvery_day(boolean every_day) {
        this.every_day = every_day;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return has_deadline == task.has_deadline &&
                fW == task.fW &&
                tW == task.tW &&
                fDOW == task.fDOW &&
                tDOW == task.tDOW &&
                every_day == task.every_day&&
                has_length == task.has_length&&
                Objects.equals(name, task.name) &&
                Objects.equals(sTime, task.sTime) &&
                Objects.equals(eTime, task.eTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, has_deadline, has_length,every_day,fW, tW, fDOW, tDOW, sTime, eTime);
    }
    
    public ContentValues getContentValues(){
        ContentValues cv = new ContentValues();
        cv.put("name",name);
        cv.put("has_ddl", has_deadline);
        cv.put("ddl_name",ddlName);
        cv.put("from_week",fW);
        cv.put("from_dow",fDOW);
        cv.put("from_hour",sTime.hour);
        cv.put("from_minute",sTime.minute);
        cv.put("to_week",tW);
        cv.put("to_dow",tDOW);
        cv.put("to_hour",eTime.hour);
        cv.put("to_minute",eTime.minute);
        cv.put("curriculum_code",curriculumCode);
        cv.put("every_day",every_day);
        cv.put("has_length",has_length);
        cv.put("length",length);
        cv.put("progress",progress);
        cv.put("type",type);
        cv.put("priority",priority);
        return cv;
    }


    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
       // return name+"##"+ has_deadline +"##"+ddlName+"##"+fW+"##"+fDOW+"##"+sTime.hour+"##"+sTime.minute+"##"+tW+"##"+tDOW+"##"+eTime.hour+"##"+eTime.minute+"##"+curriculumCode;
    }
}
