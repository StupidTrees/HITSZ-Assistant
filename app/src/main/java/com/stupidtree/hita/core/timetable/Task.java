package com.stupidtree.hita.core.timetable;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.WorkerThread;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import static com.stupidtree.hita.HITAApplication.mDBHelper;
import static com.stupidtree.hita.HITAApplication.mainTimeTable;
import static com.stupidtree.hita.HITAApplication.now;

public class Task {
    public static final int TYPE_DYNAMIC = 833;
    public String name;
    public  boolean has_deadline;
    private boolean every_day = false;
    private boolean has_length = false;
    private int length = -1;
    private int progress = -1;
    private int type = 0;
    private int priority = 0;
    public int fW,tW,fDOW,tDOW;
    private String uuid;
    private HashMap<String,Boolean> event_map;
    public String curriculumCode;
    public String ddlName = "";
    private String tag;
    private boolean finished;
    public  HTime sTime = new HTime(0,0);
    public  HTime eTime = new HTime(0,0);


    public static String QUERY_SELECTION ="uuid=?";

    public String[] getQueryParams(){
        return new String[]{
               uuid
        };

    }
    public Task(String curriculumCode,String name){
        this.name = name;
        this.curriculumCode = curriculumCode;
        has_deadline = false;
        has_length = false;
        every_day = false;
        finished = false;
        uuid = String.valueOf(UUID.randomUUID());
        event_map = new HashMap<>();
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
        event_map = new HashMap<>();
        finished = false;
        uuid = String.valueOf(UUID.randomUUID());
    }
    public Task(String curriculumCode,String name,int fW,int fDOW,HTime sTime,int tW,int tDOW,HTime eTime,String ddlUUID){
        this.curriculumCode = curriculumCode;
        this.name = name;
        this.fW = fW;
        this.fDOW = fDOW;
        this.tW =tW;
        this.tDOW = tDOW;
        this.sTime = sTime;
        this.eTime = eTime;
        this.ddlName = ddlUUID+":::"+tW;
        has_deadline = true;
        event_map = new HashMap<>();
        finished = false;
        uuid = String.valueOf(UUID.randomUUID());
    }

    @WorkerThread
    public int getDealtTime_All(){
        int result = 0;
        for(String x:event_map.keySet()){
            String uuid = x.split(":::")[0];
            EventItemHolder eih = mainTimeTable.getEventItemHolderWithUUID(uuid);
            if(eih!=null) {
                result += eih.startTime.getDuration(eih.endTime);
            }
        }
        return result;
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
        progress = c.getInt(c.getColumnIndex("progress"));
        type = c.getInt(c.getColumnIndex("type"));
        every_day = c.getInt(c.getColumnIndex("every_day"))!=0;
        priority = c.getInt(c.getColumnIndex("priority"));
        uuid = c.getString(c.getColumnIndex("uuid"));
        tag = c.getString(c.getColumnIndex("tag"));
        finished = c.getInt(c.getColumnIndex("finished"))!=0;
        event_map = new Gson().fromJson(c.getString(c.getColumnIndex("event_map")),HashMap.class);
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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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

    public void setDdlName(String ddlUUID,String week){
        ddlName = ddlUUID+":::"+week;
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
        cv.put("uuid",uuid);
        cv.put("tag",tag);
        cv.put("event_map",new Gson().toJson(event_map));
        cv.put("finished",finished);
        return cv;
    }

    public boolean isEvery_day() {
        return every_day;
    }

    public boolean isHas_length() {
        return has_length;
    }

    public void setHas_length(boolean has_length) {
        this.has_length = has_length;
    }

    public int getLength() {
        return length;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getType() {
        return type;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public void putEventMap(String key, Boolean value){ //耗时
        event_map.put(key,value);
        SQLiteDatabase sdb = mDBHelper.getWritableDatabase();
        sdb.update("task",getContentValues(),"uuid=?",new String[]{uuid});
    }

    public HashMap<String,Boolean> getEvent_map() {
        return event_map;
    }

    public void updateProgress(int progress){ //耗时
        this.progress = progress;
        SQLiteDatabase sdb = mDBHelper.getWritableDatabase();
        sdb.update("task",getContentValues(),"uuid=?",new String[]{uuid});
    }
    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
       // return name+"##"+ has_deadline +"##"+ddlName+"##"+fW+"##"+fDOW+"##"+sTime.hour+"##"+sTime.minute+"##"+tW+"##"+tDOW+"##"+eTime.hour+"##"+eTime.minute+"##"+curriculumCode;
    }
}
