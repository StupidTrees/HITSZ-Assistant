package com.stupidtree.hita.online;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.google.gson.Gson;
import com.stupidtree.hita.core.timetable.HTime;

import java.util.ArrayList;
import java.util.Objects;

import cn.bmob.v3.BmobObject;

public class TimeTable_upload_helper{
    private int dow;
    private int  from_hour;
    private int from_minute;
    private int to_hour;
    private int to_minute;
    public int  type = 0;
    public String name = null;
    private String tag2 = null;
    private String tag3 = null;
    private String tag4 = null;
    private String weeks = null;
    private boolean is_whole_day;
    public String curriculum_code;
    private String uuid;

    public TimeTable_upload_helper(Cursor c){
        curriculum_code = c.getString(c.getColumnIndex("curriculum_code"));
        type = c.getInt(c.getColumnIndex("type"));
        name = c.getString(c.getColumnIndex("name"));
        tag2 = c.getString(c.getColumnIndex("tag2"));
        tag3 = c.getString(c.getColumnIndex("tag3"));
        tag4 = c.getString(c.getColumnIndex("tag4"));
        from_hour = c.getInt(c.getColumnIndex("from_hour"));
        from_minute = c.getInt(c.getColumnIndex("from_minute"));
        to_hour = c.getInt(c.getColumnIndex("to_hour"));
        to_minute = c.getInt(c.getColumnIndex("to_minute"));
        dow = c.getInt(c.getColumnIndex("dow"));
        weeks = c.getString(c.getColumnIndex("weeks"));
        uuid = c.getString(c.getColumnIndex("uuid"));
        is_whole_day = c.getInt(c.getColumnIndex("is_whole_day"))!=0;

       // Log.e("nuh",toString());
    }
    
//    public TimeTable_upload_helper(String str){
//        String[] txts = str.split("##");
//        curriculum_code = txts[11];
//        type = Integer.parseInt(txts[5]);
//        name = txts[6];
//        tag2 = txts[7];
//        tag3 = txts[8];
//        tag4 = txts[9];
//        from_hour = Integer.parseInt(txts[1]);
//        from_minute = Integer.parseInt(txts[2]);
//        to_hour = Integer.parseInt(txts[3]);
//        to_minute = Integer.parseInt(txts[4]);
//        dow = Integer.parseInt(txts[0]);
//        weeks = txts[10];
//        is_whole_day = Boolean.parseBoolean(txts[12]);
//    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name,dow, new HTime(from_hour,from_minute), new HTime(to_hour,to_minute), type,curriculum_code);
    }
    
    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
       // return dow+"##"+from_hour+"##"+from_minute+"##"+to_hour+"##"+to_minute+"##"+type+"##"+name+"##"+tag2+"##"+tag3+"##"+tag4+"##"+weeks+"##"+curriculum_code+"##"+is_whole_day;
    }

    public ContentValues getContentValues(){
        ContentValues cv = new ContentValues();
        cv.put("curriculum_code",curriculum_code);
        cv.put("name",name);
        cv.put("weeks",weeks);
        cv.put("dow",dow);
        cv.put("from_hour",from_hour);
        cv.put("to_hour",to_hour);
        cv.put("from_minute",from_minute);
        cv.put("to_minute",to_minute);
        cv.put("tag2",tag2);
        cv.put("tag3",tag3);
        cv.put("tag4",tag4);
        cv.put("type",type);
        cv.put("is_whole_day",is_whole_day);
        cv.put("uuid",uuid);
        return cv;
    }
}
