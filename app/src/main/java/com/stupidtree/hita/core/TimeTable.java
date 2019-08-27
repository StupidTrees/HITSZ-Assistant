package com.stupidtree.hita.core;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.Html;
import android.util.Log;

import com.stupidtree.hita.core.timetable.EventItem;
import com.stupidtree.hita.core.timetable.EventItemHolder;
import com.stupidtree.hita.core.timetable.HTime;
import com.stupidtree.hita.core.timetable.Task;
import com.stupidtree.hita.core.timetable.TimePeriod;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.stupidtree.hita.HITAApplication.allCurriculum;
import static com.stupidtree.hita.HITAApplication.login;
import static com.stupidtree.hita.HITAApplication.mDBHelper;
import static com.stupidtree.hita.HITAApplication.mainTimeTable;
import static com.stupidtree.hita.HITAApplication.now;
import static com.stupidtree.hita.HITAApplication.thisCurriculumIndex;
import static com.stupidtree.hita.HITAApplication.timeWatcher;
import static com.stupidtree.hita.core.CurriculumHelper.CURRICULUM_TYPE_COURSE;

/*时间表类*/
public class TimeTable{
    public Curriculum core;
    //public ArrayList<Task> Tasks;
    public final static int TIMETABLE_EVENT_TYPE_COURSE = 1;
    public final static int TIMETABLE_EVENT_TYPE_EXAM = 2;
    public final static int TIMETABLE_EVENT_TYPE_ARRANGEMENT = 3;
    public final static int TIMETABLE_EVENT_TYPE_REMIND = 4;
    public final static int TIMETABLE_EVENT_TYPE_DEADLINE = 5;
    public final static int TIMETABLE_EVENT_TYPE_DYNAMIC = 6;



/*构造函数
    参数1：用来生成这个时间表的课程表
    参数2：这个时间表的名称
     */

    public TimeTable(Curriculum cl){
        core = cl;
    }

    public void upDateCore(Curriculum cl){
        core = cl;
    }
    public void addCurriculum(CurriculumHelper cl){
        SQLiteDatabase mDatabase = mDBHelper.getWritableDatabase();
        mDatabase.delete("timetable","curriculum_code=?",new String[]{cl.curriculumCode});
        for(CurriculumHelper.CurriculumItem ci:cl.CurriculumList){
            if(ci.type == CURRICULUM_TYPE_COURSE){
                String tag4 = ci.begin+"";
                for(int i=1;i<ci.last;i++){
                    tag4=tag4+","+(ci.begin+i);
                }
                EventItemHolder eih = new EventItemHolder(cl.curriculumCode,TIMETABLE_EVENT_TYPE_COURSE,ci.name,ci.place,ci.tag,tag4,getTimeAtNumber(ci.begin,ci.last).get(0),getTimeAtNumber(ci.begin,ci.last).get(1),ci.DOW,ci.weeks,false
                );
                mDatabase.insert("timetable",null,eih.getContentValues());
                //EventHolders.add();
            }else {
                addEvent(ci.weeks.get(0),ci.DOW,TIMETABLE_EVENT_TYPE_EXAM,ci.name,ci.place,ci.name,ci.tag,ci.begin,ci.last,false);
                //EventHolders.add(new EventItemHolder(1,ci.name,ci.place,null,ci.tag,getTimeAtNumber(ci.begin,ci.last).get(0),getTimeAtNumber(ci.begin,ci.last).get(1),ci.DOW,ci.weeks));
            }
        }
    }

    public static  List<HTime> getTimeAtNumber(int begin, int last) {
        int startDots[] = {830, 930, 1030, 1130, 1345, 1440, 1545, 1640, 1830, 1925, 2030, 2125};
        int endDots[] = {920, 1015, 1120, 1215, 1435, 1530, 1635, 1730, 1920, 2015, 2120, 2215};
        List<HTime> temp = new ArrayList<>();
        HTime startTime, endTime;
        int sH = 0, sM = 0, eH = 0, eM = 0;
        sH = startDots[begin - 1] / 100;
        sM = startDots[begin - 1] % 100;
        eH = endDots[(begin - 1) + last - 1] / 100;
        eM = endDots[(begin - 1) + last - 1] % 100;
        startTime = new HTime(sH, sM);
        endTime = new HTime(eH, eM);
        temp.add(startTime);
        temp.add(endTime);
        return temp;

    }

    public static int getNumberAtTime(Calendar time){
        HTime to =  new HTime(time);
        TimePeriod[] dots = new TimePeriod[]{
                new TimePeriod(new HTime(8,30),new HTime(9,20)),
                new TimePeriod(new HTime(9,30),new HTime(10,15)),
                new TimePeriod(new HTime(10,30),new HTime(11,20)),
                new TimePeriod(new HTime(11,30),new HTime(12,15)),
                new TimePeriod(new HTime(13,45),new HTime(14,35)),
                new TimePeriod(new HTime(14,40),new HTime(15,30)),
                new TimePeriod(new HTime(15,45),new HTime(16,35)),
                new TimePeriod(new HTime(16,30),new HTime(17,30)),
                new TimePeriod(new HTime(18,30),new HTime(19,20)),
                new TimePeriod(new HTime(19,25),new HTime(20,15)),
                new TimePeriod(new HTime(20,30),new HTime(21,20)),
                new TimePeriod(new HTime(21,25),new HTime(22,15)),
        };
        for(int i = 0;i<dots.length;i++){
          if(to.during(dots[i])) return i+1;
        }
        return -1;
    }

    /*函数功能：添加事件*/
    public String addEvent(int week, int DOW, int type, String eventName, String tag2, String tag3, String tag4, int begin, int last,boolean isWholeDay) {
        SQLiteDatabase mDatabase = mDBHelper.getWritableDatabase();
        if (week > core.totalWeeks) core.totalWeeks = week;
        EventItemHolder temp = new EventItemHolder(core.curriculumCode,type,eventName,tag2,tag3,tag4,getTimeAtNumber(begin,last).get(0),getTimeAtNumber(begin,last).get(1),DOW,isWholeDay);
        temp.weeks.add(week);
        Cursor c = mDatabase.query("timetable",null,EventItemHolder.QUERY_SELECTION,temp.getQueryParams(),null,null,null);
        String uuid = temp.getUuid();
        if(c.moveToNext()){
            List<Integer> weeks = new ArrayList<>();
            String [] strs = c.getString(2).split(",");
            for(int i=0;i<strs.length;i++){
                if(!strs[i].isEmpty())weeks.add(Integer.parseInt(strs[i]));
            }
            if(!weeks.contains(week)){
                weeks.add(week);
            }
            String newWeeks = getWeeksText(weeks);
            ContentValues cv = new ContentValues();
            uuid = c.getString(c.getColumnIndex("uuid"));
            cv.put("weeks",newWeeks);
            cv.put("uuid",uuid);
            mDatabase.update("timetable",cv,EventItemHolder.QUERY_SELECTION,temp.getQueryParams());
        }else{
            mDatabase.insert("timetable",null,temp.getContentValues());
        }
        c.close();
        return uuid;
    }
    public String addEvent(int week, int DOW, int type, String eventName, String tag2, String tag3, String tag4, HTime start,HTime end,boolean isWholeDay) {
        SQLiteDatabase mDatabase = mDBHelper.getWritableDatabase();
        if (week > core.totalWeeks) core.totalWeeks = week;
        EventItemHolder temp = new EventItemHolder(core.curriculumCode,type,eventName,tag2,tag3,tag4,start,end,DOW,isWholeDay);
        temp.weeks.add(week);
        String uuid = temp.getUuid();
        Cursor c = mDatabase.query("timetable",null,EventItemHolder.QUERY_SELECTION,temp.getQueryParams(),null,null,null);
        if(c.moveToNext()){
            List<Integer> weeks = new ArrayList<>();
            String [] strs = c.getString(2).split(",");
            for(int i=0;i<strs.length;i++){
                if(!strs[i].isEmpty())weeks.add(Integer.parseInt(strs[i]));
            }
            if(!weeks.contains(week)){
                weeks.add(week);
            }
            String newWeeks = getWeeksText(weeks);
            ContentValues cv = new ContentValues();
            uuid = c.getString(c.getColumnIndex("uuid"));
            cv.put("weeks",newWeeks);
            cv.put("uuid",uuid);
            mDatabase.update("timetable",cv,EventItemHolder.QUERY_SELECTION,temp.getQueryParams());
        }else{
           mDatabase.insert("timetable",null,temp.getContentValues());
        }
        c.close();
        return uuid;
    }
    public String addEvent(EventItem ei) {
        SQLiteDatabase mDatabase = mDBHelper.getWritableDatabase();
        int week,DOW, type;
        String eventName, tag2,  tag3, tag4;
        HTime start, end;
        week = ei.week;
        DOW = ei.DOW;
        type = ei.eventType;
        eventName = ei.mainName;
        tag2 = ei.tag2;
        tag3 = ei.tag3;
        tag4 = ei.tag4;
        start = ei.startTime;
        end = ei.endTime;
        if (week > core.totalWeeks) core.totalWeeks = week;
        EventItemHolder temp = new EventItemHolder(ei.curriculumCode,type,eventName,tag2,tag3,tag4,start,end,DOW,ei.isWholeDay);
        temp.weeks.add(week);
        ei.setUuid(temp.getUuid());
        Cursor c = mDatabase.query("timetable",null,EventItemHolder.QUERY_SELECTION,temp.getQueryParams(),null,null,null);
        String uuid = temp.getUuid();
        if(c.moveToNext()){
            List<Integer> weeks = new ArrayList<>();
            String [] strs = c.getString(2).split(",");
            for(int i=0;i<strs.length;i++){
                weeks.add(Integer.parseInt(strs[i]));
            }
            if(!weeks.contains(week)){
                weeks.add(week);
            }
            String newWeeks = getWeeksText(weeks);
            ContentValues cv = temp.getContentValues();
            uuid = c.getString(c.getColumnIndex("uuid"));
            cv.put("weeks",newWeeks);
            cv.put("uuid",uuid);
            mDatabase.update("timetable",cv,EventItemHolder.QUERY_SELECTION,temp.getQueryParams());
        }else{
            Log.e("add",temp.toString());
            mDatabase.insert("timetable",null,temp.getContentValues());
        }
        c.close();

        return uuid;
    }


    public boolean deleteEvent(EventItem ei,boolean deleteTask) {
        Log.e("deleteEvent","dt:"+deleteTask);
        SQLiteDatabase mSQLiteDatabase = mDBHelper.getWritableDatabase();
       if(ei==null) return false;
        EventItemHolder eih = new EventItemHolder(ei);
        Cursor c = mSQLiteDatabase.query("timetable",null,"uuid=?",
                new String[]{ei.getUuid()},null,null,null);
        if(c.moveToNext()){
            List<Integer> weeks = new ArrayList<>();
            String [] strs = c.getString(2).split(",");
            for(int i=0;i<strs.length;i++){
                if(!strs[i].isEmpty())weeks.add(Integer.parseInt(strs[i]));
            }
            if(!weeks.contains(ei.week))return false;
            else weeks.remove((Object)ei.week);
            if(weeks.size()==0){
                mSQLiteDatabase.delete("timetable","uuid=?",new String[]{ei.getUuid()});
            }else{
                String newWeeks = getWeeksText(weeks);
                ContentValues cv = new ContentValues();
                cv.put("weeks",newWeeks);
                mSQLiteDatabase.update("timetable",cv,"uuid=?",new String[]{ei.getUuid()});
            }
            if(deleteTask){
                deleteTask(eih.tag4,false);
//                mSQLiteDatabase.delete("task","uuid=?",
//                        new String[]{eih.tag4});
            }
        }else{
            c.close();
            return false;
        }
        c.close();
        return true;
    }

    public boolean deleteEvent(String uuid,int week) {
        SQLiteDatabase mSQLiteDatabase = mDBHelper.getWritableDatabase();
        Cursor c = mSQLiteDatabase.query("timetable",null,"uuid=?",
                new String[]{uuid},null,null,null);
        if(c.moveToNext()){
            List<Integer> weeks = new ArrayList<>();
            String [] strs = c.getString(2).split(",");
            for(int i=0;i<strs.length;i++){
                if(!strs[i].isEmpty())weeks.add(Integer.parseInt(strs[i]));
            }
            if(!weeks.contains(week))return false;
            else weeks.remove((Object)week);
            if(weeks.size()==0){
                mSQLiteDatabase.delete("timetable","uuid=?",
                        new String[]{uuid});
            }else{
                String newWeeks = getWeeksText(weeks);
                ContentValues cv = new ContentValues();
                cv.put("weeks",newWeeks);
                mSQLiteDatabase.update("timetable",cv,"uuid=?",
                        new String[]{uuid});
            }
        }else{
            c.close();
            return false;
        }
        c.close();
        return true;
    }
    public boolean deleteEvent(Calendar from,Calendar to,int type){
        List<EventItem> temp = getEventFrom(from,to,type);
        if(temp==null||temp.size()==0) return false;
        for(EventItem e:temp){
            deleteEvent(e,true);
        }
        return temp.size()>0;
    }
    public boolean deleteTask(Task ta){
        SQLiteDatabase sd = mDBHelper.getWritableDatabase();
        try {
            if (ta.has_deadline&&!ta.ddlName.equals("null")) {
                String ddlUUID = ta.ddlName.split(":::")[0];
                int week = Integer.parseInt(ta.ddlName.split(":::")[1]);
                mainTimeTable.deleteEvent(ddlUUID,week);
            }
            for(String key:ta.getEvent_map().keySet()){
                String EIuuid = key.split(":::")[0];
                sd.delete("timetable","uuid=?",new String[]{EIuuid});
            }
            return sd.delete("task",Task.QUERY_SELECTION,ta.getQueryParams())!=0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean deleteTask(String uuid,boolean deleteDDL){
        SQLiteDatabase sd = mDBHelper.getWritableDatabase();
        Task ta;
        Cursor c = sd.query("task",null,"uuid=?",new String[]{uuid},null,null,null);
        if(c.moveToNext()){
            ta = new Task(c);
            c.close();
        }else{
            c.close();
            return false;
        }
        try {
            if (deleteDDL&&ta.has_deadline&&!ta.ddlName.equals("null")) {
                String ddlUUID = ta.ddlName.split(":::")[0];
                int week = Integer.parseInt(ta.ddlName.split(":::")[1]);
                mainTimeTable.deleteEvent(ddlUUID,week);
            }
            for(String key:ta.getEvent_map().keySet()){
                String EIuuid = key.split(":::")[0];
                sd.delete("timetable","uuid=?",new String[]{EIuuid});
            }
            return sd.delete("task",Task.QUERY_SELECTION,ta.getQueryParams())!=0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean finishTask(Task ta){
        try {
            if (ta.has_deadline&&!ta.ddlName.equals("null")) {
                String ddlUUID = ta.ddlName.split(":::")[0];
                int week = Integer.parseInt(ta.ddlName.split(":::")[1]);
                mainTimeTable.deleteEvent(ddlUUID,week);
            }
            SQLiteDatabase sd = mDBHelper.getWritableDatabase();
            ta.setFinished(true);
            return sd.update("task",ta.getContentValues(),"uuid=?",new String[]{ta.getUuid()})!=0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
    public void clearEvent(int type){
        SQLiteDatabase mSQLiteDatabase = mDBHelper.getWritableDatabase();
        mSQLiteDatabase.delete("timetable","type=?",new String[]{type+""});
    }
    public void clearTask(String tagContains){
        SQLiteDatabase mSQLiteDatabase = mDBHelper.getWritableDatabase();
        Cursor c = mSQLiteDatabase.query("task",null,"tag like?",new String[]{"%"+tagContains+"%"},null,null,null);
        while (c.moveToNext()){
            Task t = new Task(c);
            for(String x:t.getEvent_map().keySet()){
                String uuid = x.split(":::")[0];
                mSQLiteDatabase.delete("timetable","uuid=?",new String[]{uuid});
            }
            mSQLiteDatabase.delete("task","uuid=?",new String[]{t.getUuid()});
        }
        c.close();

    }
    public void clearEvent(int type,String name){
        SQLiteDatabase mSQLiteDatabase = mDBHelper.getWritableDatabase();
        mSQLiteDatabase.delete("timetable","type=? and name=?",new String[]{type+"",name});
    }
    public void clearCurriculum(String curriculumCode){
        SQLiteDatabase mSQLiteDatabase = mDBHelper.getWritableDatabase();
        mSQLiteDatabase.delete("timetable","curriculum_code=? and type=?",new String[]{curriculumCode+"",TIMETABLE_EVENT_TYPE_COURSE+""});
    }

    public static boolean contains_integer(int[] array,int object){
        for(int x:array) if(x==object) return true;
        return false;
    }

    /*函数功能：获取从从某周某天某点到某周某天某点内的所有事件列表*/
    public List<EventItem> getEventFrom_typeLimit(int f_week, int f_dayOfWeek, HTime start, int t_week, int t_dayOfWeek, HTime end,int[] types) {
        // System.out.println("开始查询,共有事件"+getEventsWithinWeeks(f_week,t_week).size()+"个");
        if (t_dayOfWeek == -1) t_dayOfWeek = core.totalWeeks;
        if (f_week > t_week) return null;
        else if (f_week == t_week) {
            if (f_dayOfWeek > t_dayOfWeek) return null;
            else if (f_dayOfWeek == t_dayOfWeek) {
                if (start.compareTo(end) > 0) return null;
            }
        }
        List<EventItem> result = new ArrayList<EventItem>();
        if (f_week > core.totalWeeks || t_week > core.totalWeeks || f_week <= 0 || t_week <= 0) return null;
        for(EventItem ei:getEventsWithinWeeks(f_week,t_week)){
            if ((types!=null&&types.length!=0&&!contains_integer(types,ei.eventType))||(ei.week == f_week && ei.DOW < f_dayOfWeek) || (ei.week == t_week && ei.DOW > t_dayOfWeek)) continue;
            if ((ei.week == f_week && ei.DOW == f_dayOfWeek)) {
                if(f_dayOfWeek==t_dayOfWeek){
                    if (ei.hasCross(start) ||( ei.startTime.compareTo(start) >= 0&&ei.startTime.compareTo(end)<=0)) result.add(ei);
                }else{
                    if (ei.hasCross(start) ||( ei.startTime.compareTo(start) >= 0)) result.add(ei);
                }
            } else if (ei.week == t_week && ei.DOW== t_dayOfWeek) {
                if ((ei.endTime.compareTo(end) <= 0 )|| ei.hasCross(end)) result.add(ei);
            } else {
                result.add(ei);
            }
        }
         return result;
    }//type<0表示所有类型
    public List<EventItem> getEventFrom(int f_week, int f_dayOfWeek, HTime start, int t_week, int t_dayOfWeek, HTime end,int type) {
        // System.out.println("开始查询,共有事件"+getEventsWithinWeeks(f_week,t_week).size()+"个");
        if (t_dayOfWeek == -1) t_dayOfWeek = core.totalWeeks;
        if (f_week > t_week) return null;
        else if (f_week == t_week) {
            if (f_dayOfWeek > t_dayOfWeek) return null;
            else if (f_dayOfWeek == t_dayOfWeek) {
                if (start.compareTo(end) > 0) return null;
            }
        }
        List<EventItem> result = new ArrayList<EventItem>();
        if (f_week > core.totalWeeks || t_week > core.totalWeeks || f_week <= 0 || t_week <= 0) return null;
        for(EventItem ei:getEventsWithinWeeks(f_week,t_week)){
            if ((type>0&&ei.eventType!=type)||(ei.week == f_week && ei.DOW < f_dayOfWeek) || (ei.week == t_week && ei.DOW > t_dayOfWeek)) continue;
            if ((ei.week == f_week && ei.DOW == f_dayOfWeek)) {
                if(f_dayOfWeek==t_dayOfWeek){
                    if (ei.hasCross(start) ||( ei.startTime.compareTo(start) >= 0&&ei.startTime.compareTo(end)<=0)) result.add(ei);
                }else{
                    if (ei.hasCross(start) ||( ei.startTime.compareTo(start) >= 0)) result.add(ei);
                }
            } else if (ei.week == t_week && ei.DOW== t_dayOfWeek) {
                if ((ei.endTime.compareTo(end) <= 0 )|| ei.hasCross(end)) result.add(ei);
            } else {
                result.add(ei);
            }
        }
        return result;
    }//type<0表示所有类型
    public List<EventItem> getEventFrom(int f_week, int f_dayOfWeek, HTime start, int t_week, int t_dayOfWeek, HTime end) {
        //System.out.println("开始查询,共有事件"+getEventsWithinWeeks(f_week,t_week).size()+"个");
        if (t_dayOfWeek == -1) t_dayOfWeek = core.totalWeeks;
        if (f_week > t_week) return null;
        else if (f_week == t_week) {
            if (f_dayOfWeek > t_dayOfWeek) return null;
            else if (f_dayOfWeek == t_dayOfWeek) {
                if (start.compareTo(end) > 0) return null;
            }
        }
        List<EventItem> result = new ArrayList<EventItem>();
        if (f_week > core.totalWeeks || t_week > core.totalWeeks || f_week <= 0 || t_week <= 0) return null;
        for(EventItem ei:getEventsWithinWeeks(f_week,t_week)){
            if ((ei.week == f_week && ei.DOW < f_dayOfWeek) || (ei.week == t_week && ei.DOW > t_dayOfWeek)) continue;
            if ((ei.week == f_week && ei.DOW == f_dayOfWeek)) {
                if(f_dayOfWeek==t_dayOfWeek){
                    if (ei.hasCross(start) ||( ei.startTime.compareTo(start) >= 0&&ei.startTime.compareTo(end)<=0)) result.add(ei);
                }else{
                    if (ei.hasCross(start) ||( ei.startTime.compareTo(start) >= 0)) result.add(ei);
                }
            } else if (ei.week == t_week && ei.DOW== t_dayOfWeek) {
                if ((ei.endTime.compareTo(end) <= 0 )|| ei.hasCross(end)) result.add(ei);
            } else {
                result.add(ei);
            }
        }
        return result;
    }
    public List<EventItem> getEventFrom(Calendar from, Calendar to, int type) {
        List<EventItem> result = new ArrayList<EventItem>();
        int f_week = core.getWeekOfTerm(from);
        int tempDOW1 = from.get(Calendar.DAY_OF_WEEK);
        int f_dayOfWeek =  tempDOW1==1?7:tempDOW1-1;
        HTime start = new HTime(from);
        int t_week = core.getWeekOfTerm(to);
        int tempDOW2 = to.get(Calendar.DAY_OF_WEEK);
        int t_dayOfWeek =  tempDOW2==1?7:tempDOW2-1;
        HTime end = new HTime(to);
        //System.out.println("开始查询,共有事件"+getEventsWithinWeeks(f_week,t_week).size()+"个");

       // Log.e("getEventFrom",f_week+","+f_dayOfWeek+","+start.tellTime()+",,,"+t_week+","+t_dayOfWeek+","+end.tellTime());
        if (t_dayOfWeek == -1) t_dayOfWeek = core.totalWeeks;
        if (f_week > t_week) return null;
        else if (f_week == t_week) {
            if (f_dayOfWeek > t_dayOfWeek) return null;
            else if (f_dayOfWeek == t_dayOfWeek) {
                if (start.compareTo(end) > 0) return null;
            }
        }

        if (f_week > core.totalWeeks || t_week > core.totalWeeks || f_week <= 0 || t_week <= 0) return null;
        for(EventItem ei:getEventsWithinWeeks(f_week,t_week)){
            if ((type>0&&ei.eventType!=type)||(ei.week == f_week && ei.DOW < f_dayOfWeek) || (ei.week == t_week && ei.DOW > t_dayOfWeek)) continue;
            if ((ei.week == f_week && ei.DOW == f_dayOfWeek)) {
                if(f_dayOfWeek==t_dayOfWeek){
                    if (ei.hasCross(start) ||( ei.startTime.compareTo(start) >= 0&&ei.startTime.compareTo(end)<=0)) result.add(ei);
                }else{
                    if (ei.hasCross(start) ||( ei.startTime.compareTo(start) >= 0)) result.add(ei);
                }
            } else if (ei.week == t_week && ei.DOW== t_dayOfWeek) {
                if (ei.endTime.compareTo(end) <= 0|| ei.hasCross(end)) result.add(ei);
            } else {
                result.add(ei);
            }
        }
        return result;
    } //type<0表示所有类型

    public List<EventItem> getAllEvents(){
        List<EventItem> result = new ArrayList<>();
        SQLiteDatabase sd = mDBHelper.getReadableDatabase();
        Cursor c =  sd.query("timetable",null,"curriculum_code=?",new String[]{core.curriculumCode},null,null,null);
        while (c.moveToNext()){
            result.addAll(new EventItemHolder(c).getAllEvents()) ;
        }
        c.close();
        return result;
    }
    public List<EventItem> getOneDayEvents(int week,int DOW){
        List<EventItem> result = new ArrayList<>();
        if(week<=0||week>core.totalWeeks) return result;
        for(EventItem ei:getEventsWithinWeeks(week,week)){
            if(ei.DOW==DOW) result.add(ei);
        }
        return result;
    }

    public Task getTaskWithUUID(String uuid){
        Task result = null;
        SQLiteDatabase sd = mDBHelper.getReadableDatabase();
        Cursor c = sd.query("task",null,"uuid=?",new String[]{uuid},null,null,null);
        if(c.moveToNext()){
            result = new Task(c);
        }
        c.close();
        return result;
    }

    public EventItemHolder getEventItemHolderWithUUID(String uuid){
        EventItemHolder result = null;
        SQLiteDatabase sd = mDBHelper.getReadableDatabase();
        Cursor c = sd.query("timetable",null,"uuid=?",new String[]{uuid},null,null,null);
        if(c.moveToNext()){
            result = new EventItemHolder(c);
        }
        c.close();
        return result;
    }
//    public void addTask(String name){
//        Task t = new Task(core.curriculumCode,name);
//        SQLiteDatabase sd = mDBHelper.getWritableDatabase();
//        sd.insert("task",null,t.getContentValues());
//    }
//    public void addTask(String name,int fW,int fDOW,int tW,int tDOW,HTime sTime,HTime tTime,String ddlName){
//        Task t = new Task(core.curriculumCode,name,fW,fDOW,tW,tDOW,sTime,tTime,ddlName);
//        SQLiteDatabase sd = mDBHelper.getWritableDatabase();
//        sd.insert("task",null,t.getContentValues());
//    }

    public String addTask(Task t){
        SQLiteDatabase sd = mDBHelper.getWritableDatabase();
        sd.insert("task",null,t.getContentValues());
        return t.getUuid();
    }
    public String addTask(String name,int fW,int fDOW,HTime sTime,int tW,int tDOW,HTime eTime,String ddlUUID){
            SQLiteDatabase sd = mDBHelper.getWritableDatabase();
            Task t = new Task(core.curriculumCode,name,fW,fDOW,sTime,tW,tDOW,eTime,ddlUUID);
            sd.insert("task",null,t.getContentValues());
            return t.getUuid();
    }

    public ArrayList<Task> getUnfinishedTasks(){
        ArrayList<Task> res = new ArrayList<>();
        if(core==null) return res;
        SQLiteDatabase sld = mDBHelper.getReadableDatabase();
        Cursor c = sld.query("task",null,"curriculum_code=? and finished=?",new String[]{core.curriculumCode,0+""},null,null,null);
        while (c.moveToNext()){
             res.add(new Task(c));
        }
        c.close();
        return res;
    }

    public ArrayList<Task> getfinishedTasks(){
        ArrayList<Task> res = new ArrayList<>();
        if(core==null) return res;
        SQLiteDatabase sld = mDBHelper.getReadableDatabase();
        Cursor c = sld.query("task",null,"curriculum_code=? and finished=?",new String[]{core.curriculumCode,1+""},null,null,null);
        while (c.moveToNext()){
            res.add(new Task(c));
        }
        c.close();
        return res;
    }
    public ArrayList<Task> getUnfinishedTaskWithLength(){
        ArrayList<Task> res = new ArrayList<>();
        if(core==null) return res;
        SQLiteDatabase sld = mDBHelper.getReadableDatabase();
        Cursor c = sld.query("task",null,"curriculum_code=? and has_length=? and finished=?",new String[]{core.curriculumCode,1+"",0+""},null,null,null);
        while (c.moveToNext()){
             res.add(new Task(c));
        }
        c.close();
        return res;
    }
    public static int getDOW(Calendar c){
        int tempDOW1 = c.get(Calendar.DAY_OF_WEEK);
        return  tempDOW1==1?7:tempDOW1-1;
    }

    public EventItem getCourseAt(int week,int dow,int start,int last){
        for(EventItem ei:getEventsWithinWeeks(week,week)){
            if(ei.DOW==dow
                    &&ei.startTime.equals(getTimeAtNumber(start,last).get(0))
                    &&ei.endTime.equals(getTimeAtNumber(start,last).get(1))
                    &&ei.eventType==TIMETABLE_EVENT_TYPE_COURSE){
                return ei;
            }
        }
        return null;
    }

    public boolean hasOverLapping(int week, int dayOfWeek, EventItem ei) {
        for(EventItem e:getEventsWithinWeeks(week,week)){
            if(e.DOW!=dayOfWeek) continue;
            if(ei.hasOverLapping(e)) return true;
        }
        return false;
    }

    public List<EventItem> getEventsWithinWeeks(int fromW,int toW){
        SQLiteDatabase mSQLiteDatabase = mDBHelper.getReadableDatabase();
        List<EventItem> result = new ArrayList<>();
        Cursor c = mSQLiteDatabase.query("timetable",null,"curriculum_code=?",new String[]{core.curriculumCode+""},null,null,null);
        while (c.moveToNext()){
            EventItemHolder eih = new EventItemHolder(c);
            //Log.e("!!!",c.getString(0)+"|"+c.getString(2));
            result.addAll(eih.getEventsWithinWeeks(fromW,toW));
        }
        c.close();
        
        return result;
    }

    public List<TimePeriod> getSpaces(Calendar from, Calendar to, int minDurationMinute, int type){
        if(from.after(to)||from.get(Calendar.DAY_OF_MONTH)!=to.get(Calendar.DAY_OF_MONTH)) return null;
        List<TimePeriod> result = new ArrayList<>();
        List<EventItem> temp = getEventFrom(from,to,type);
        Collections.sort(temp);
        if(temp==null||temp.size()==0){
            TimePeriod m = new TimePeriod();
            m.start = new HTime(from);
            m.end = new HTime(to);
            result.add(m);
        }else if(temp.size()==1) {
            if(temp.get(0).startTime.after(new HTime(from))&&temp.get(0).startTime.getDuration(new HTime(from))>=minDurationMinute){
                TimePeriod m = new TimePeriod();
                m.start = new HTime(from);
                m.end = temp.get(0).startTime;
                result.add(m);
            }
            if(temp.get(0).endTime.before(new HTime(to))&&temp.get(0).endTime.getDuration(new HTime(to))>=minDurationMinute){
                TimePeriod m2 = new TimePeriod();
                m2.end = new HTime(to);
                m2.start = temp.get(0).endTime;
                result.add(m2);
            }
        }else{
                for(int i=0;i<temp.size();i++){
                    TimePeriod m = new TimePeriod();
                    Log.e("event:",temp.get(i).toString());
                    if(i==0){
                        if(temp.get(i).startTime.after(new HTime(from))&&temp.get(i).startTime.getDuration(new HTime(from))>=minDurationMinute){
                            m.start = new HTime(from);
                            m.end = temp.get(0).startTime;
                            Log.e("add:first",m.toString());
                            result.add(m);
                        }
                    }else if(i==temp.size()-1){
                        if(temp.get(i).endTime.before(new HTime(to))&&temp.get(i).endTime.getDuration(new HTime(to))>=minDurationMinute){
                            m.end = new HTime(to);
                            m.start = temp.get(i).endTime;
                            result.add(m);
                            Log.e("add:last",m.toString());
                        }
                    }

                    if(i+1<temp.size()&&temp.get(i).endTime.getDuration(temp.get(i+1).startTime)>=minDurationMinute){
                        TimePeriod m3 = new TimePeriod();
                        m3.start = temp.get(i).endTime;
                        m3.end = temp.get(i+1).startTime;
                        Log.e("add:normal",m3.toString());
                        result.add(m3);

                    }

                }

            }
        Collections.sort(result);
        return result;
    }

    public List<TimePeriod> getSpaces(List<EventItem> breakT,Calendar from, Calendar to, int minDurationMinute, int type){
        if(from.after(to)||from.get(Calendar.DAY_OF_MONTH)!=to.get(Calendar.DAY_OF_MONTH)) return null;
        List<TimePeriod> result = new ArrayList<>();
        List<EventItem> temp = getEventFrom(from,to,type);
        temp.addAll(breakT);
        Collections.sort(temp);
        if(temp==null||temp.size()==0){
            TimePeriod m = new TimePeriod();
            m.start = new HTime(from);
            m.end = new HTime(to);
            result.add(m);
        }else if(temp.size()==1) {
            if(temp.get(0).startTime.after(new HTime(from))&&temp.get(0).startTime.getDuration(new HTime(from))>=minDurationMinute){
                TimePeriod m = new TimePeriod();
                m.start = new HTime(from);
                m.end = temp.get(0).startTime;
                result.add(m);
            }
            if(temp.get(0).endTime.before(new HTime(to))&&temp.get(0).endTime.getDuration(new HTime(to))>=minDurationMinute){
                TimePeriod m2 = new TimePeriod();
                m2.end = new HTime(to);
                m2.start = temp.get(0).endTime;
                result.add(m2);
            }
        }else{
            for(int i=0;i<temp.size();i++){
                TimePeriod m = new TimePeriod();
                Log.e("event:",temp.get(i).toString());
                if(i==0){
                    if(temp.get(i).startTime.after(new HTime(from))&&temp.get(i).startTime.getDuration(new HTime(from))>=minDurationMinute){
                        m.start = new HTime(from);
                        m.end = temp.get(0).startTime;
                        Log.e("add:first",m.toString());
                        result.add(m);
                    }
                }else if(i==temp.size()-1){
                    if(temp.get(i).endTime.before(new HTime(to))&&temp.get(i).endTime.getDuration(new HTime(to))>=minDurationMinute){
                        m.end = new HTime(to);
                        m.start = temp.get(i).endTime;
                        result.add(m);
                        Log.e("add:last",m.toString());
                    }
                }

                if(i+1<temp.size()&&temp.get(i).endTime.getDuration(temp.get(i+1).startTime)>=minDurationMinute){
                    TimePeriod m3 = new TimePeriod();
                    m3.start = temp.get(i).endTime;
                    m3.end = temp.get(i+1).startTime;
                    Log.e("add:normal",m3.toString());
                    result.add(m3);

                }

            }

        }
        HTime fromH = new HTime(from);
        HTime toH = new HTime(to);
        //Log.e("spaces_beforeRemove", String.valueOf(result));
        List<TimePeriod> tpToRemove = new ArrayList<>();
        for(TimePeriod tp:result){
            if(tp.before(fromH)) tpToRemove.add(tp);
            if(tp.hasCross(fromH)){
                tp.start.setTime(fromH.hour,fromH.minute);
            }
            if(tp.hasCross(toH)) tp.end.setTime(toH.hour,toH.minute);
        }
        result.removeAll(tpToRemove);
        Collections.sort(result);
        return result;
    }

    private String getWeeksText(List<Integer> weeks){
        String res = "";
        for(Integer x: weeks){
            res = res+x+",";
        }
        if(res.endsWith(","))res = res.substring(0,res.length()-1);
        return res;
    }

}

