package com.stupidtree.hita.timetable.packable;

import android.content.ContentValues;
import android.database.Cursor;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class EventItemHolder  {
    public int DOW;
    public HTime startTime;
    public HTime endTime;
    public int eventType;
    public String mainName;//名称
    public String tag2;     //地点
    public String tag3;     //教师（Exam为null）
    public String tag4;     //Exam为具体时间，Course为课程节数
    public ArrayList<Integer> weeks;
    public boolean isWholeDay;
    private String curriculumCode;
    private String uuid;
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventItemHolder that = (EventItemHolder) o;
        return DOW == that.DOW &&
                eventType == that.eventType &&
                startTime.equals(that.startTime) &&
                endTime.equals( that.endTime) &&
                mainName.equals(that.mainName);
    }
    public static String QUERY_SELECTION = "uuid=?";
    public String[] getQueryParams(){
        return new String[]{
                uuid
        };
    }
    @Override
    public int hashCode() {
        return Objects.hash(mainName,DOW, startTime, endTime, eventType,curriculumCode);
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
        //return mainName+","+startTime.toString()+"-"+endTime.toString();
    }
    public EventItemHolder(Cursor c) {
        String curriculumCode = c.getString(c.getColumnIndex("curriculum_code"));
        int type = c.getInt(c.getColumnIndex("type"));
        String eventName = c.getString(c.getColumnIndex("name"));
        String tag2 = c.getString(c.getColumnIndex("tag2"));
        String tag3 = c.getString(c.getColumnIndex("tag3"));
        String tag4 = c.getString(c.getColumnIndex("tag4"));
        uuid = c.getString(c.getColumnIndex("uuid"));
        HTime start = new HTime(c.getInt(c.getColumnIndex("from_hour")),c.getInt(c.getColumnIndex("from_minute")));
        HTime end = new HTime(c.getInt(c.getColumnIndex("to_hour")),c.getInt(c.getColumnIndex("to_minute")));
        int DOW = c.getInt(c.getColumnIndex("dow"));
        isWholeDay = c.getInt(c.getColumnIndex("is_whole_day"))!=0;
        ArrayList<Integer> weeks = new ArrayList<>();
        String[] wstr = c.getString(c.getColumnIndex("weeks")).split(",");
        for(int i=0;i<wstr.length;i++) {
            if(wstr[i].isEmpty()) continue;
            weeks.add(Integer.parseInt(wstr[i]));
        }
        eventType = type;
        this.startTime = start;
        endTime = null;
        this.endTime = end;
        mainName = eventName;
        this.tag2 = tag2;
        this.tag3 = tag3;
        this.tag4 = tag4;
        this.DOW = DOW;
        this.curriculumCode = curriculumCode;
        this.weeks = new ArrayList<>(weeks);
    }
    public EventItemHolder(String curriculumCode,int type, String eventName, String tag2, String tag3, String tag4, HTime start, HTime end, int DOW,boolean isWholeDay) {
        eventType = type;
        this.startTime = start;
        endTime = null;
        this.endTime = end;
        mainName = eventName;
        this.tag2 = tag2;
        this.tag3 = tag3;
        this.tag4 = tag4;
        this.DOW = DOW;
        this.curriculumCode = curriculumCode;
        this.isWholeDay = isWholeDay;
        weeks = new ArrayList<>();
        uuid = String.valueOf(UUID.randomUUID());
    }
    public EventItemHolder(String curriculumCode,int type, String eventName, String tag2, String tag3, String tag4, HTime start, HTime end, int DOW, ArrayList<Integer> weeks,boolean isWholeDay) {
        eventType = type;
        this.startTime = start;
        endTime = null;
        this.endTime = end;
        mainName = eventName;
        this.tag2 = tag2;
        this.tag3 = tag3;
        this.tag4 = tag4;
        this.DOW = DOW;
        this.curriculumCode = curriculumCode;
        this.weeks = new ArrayList<>(weeks);
        this.isWholeDay = isWholeDay;
        uuid = String.valueOf(UUID.randomUUID());
    }
    public EventItemHolder(EventItem ei) {
        eventType = ei.eventType;
        this.startTime = ei.startTime;
        endTime = null;
        this.endTime = ei.endTime;
        mainName = ei.mainName;
        this.tag2 = ei.tag2;
        this.tag3 = ei.tag3;
        this.tag4 = ei.tag4;
        this.DOW = ei.DOW;
        this.weeks = new ArrayList<>();
        weeks.add(ei.week);
        this.isWholeDay = ei.isWholeDay();
        this.curriculumCode = ei.curriculumCode;
        uuid = ei.getUuid();
    }

    public void addWeek(int week){
        if(!weeks.contains(week)) weeks.add(week);
    }
    public boolean hasCross(HTime t) {
        return startTime.compareTo(t) <= 0 && endTime.compareTo(t) >= 0;
    }
    public boolean withinWeeks(int fromW,int toW){
        List<Integer> temp = new ArrayList<>();
        for(int i = fromW;i<=toW;i++){
            temp.add(i);
        }
        return weeks.containsAll(temp);
    }
    public List<EventItem> getEventsWithinWeeks(int fromW,int toW){
        List<EventItem> result = new ArrayList<>();
        for (Integer i:weeks){
            if(!(i>=fromW&&i<=toW)) continue;
            EventItem eiT = new EventItem(uuid,curriculumCode,eventType,mainName,tag2,tag3,tag4,startTime,endTime,i, DOW,isWholeDay);
            result.add(eiT);
        }
        return result;
    }
    public List<EventItem> getAllEvents(){
        List<EventItem> result = new ArrayList<>();
        for (Integer i:weeks){
            EventItem eiT = new EventItem(uuid,curriculumCode,eventType,mainName,tag2,tag3,tag4,startTime,endTime,i, DOW,isWholeDay);
            result.add(eiT);
        }
        return result;
    }
    public EventItem getEventAtWeek(int week){
        EventItem eiT = new EventItem(uuid,curriculumCode,eventType,mainName,tag2,tag3,tag4,startTime,endTime,week, DOW,isWholeDay);
        return eiT;
    }
    public ContentValues getContentValues(){
        ContentValues cv = new ContentValues();
        cv.put("curriculum_code",curriculumCode);
        cv.put("name",mainName);
        cv.put("weeks",getWeeksText());
        cv.put("dow",DOW);
        cv.put("from_hour",startTime.hour);
        cv.put("to_hour",endTime.hour);
        cv.put("from_minute",startTime.minute);
        cv.put("to_minute",endTime.minute);
        cv.put("tag2",tag2);
        cv.put("tag3",tag3);
        cv.put("tag4",tag4);
        cv.put("type",eventType);
        cv.put("is_whole_day",isWholeDay);
        cv.put("uuid",uuid);
        return cv;
    }

    public int getEventType() {
        return eventType;
    }

    public String getMainName() {
        return mainName;
    }

    public String getWeeksText(){
        String res = "";
        for(Integer x: this.weeks){
            res = res+x+",";
        }
        if(res.endsWith(","))res = res.substring(0,res.length()-1);
        return res;
    }


}
