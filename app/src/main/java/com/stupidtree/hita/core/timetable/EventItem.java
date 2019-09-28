package com.stupidtree.hita.core.timetable;

import com.stupidtree.hita.core.Curriculum;
import com.stupidtree.hita.core.TimeTable;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Objects;

import static com.stupidtree.hita.HITAApplication.allCurriculum;

/*事件类*/
public class EventItem implements Serializable, Comparable {
    public int week;
    public int DOW;
    public HTime startTime = null;
    public HTime endTime = null;
    public int eventType = 0;
    public String mainName = null;//名称
    public String tag2 = null;     //地点
    public String tag3 = null;     //Exam为科目名，Course为教师
    public String tag4 = null;     //Exam为具体时间，Course为课程节数
    public float rate = 5f; //用在course的评分
    public String curriculumCode;
    public boolean isWholeDay;
    String uuid;



    public EventItem(String uuid,String curriculumCode,int type, String eventName, String tag2, String tag3, String tag4, HTime start, HTime end, int week, int DOW,boolean isWholeDay) {
        eventType = type;
        this.curriculumCode = curriculumCode;
        this.startTime = start;
        this.endTime = end;
        mainName = eventName;
        this.tag2 = tag2;
        this.tag3 = tag3;
        this.tag4 = tag4;
        this.week = week;
        this.DOW = DOW;
        this.isWholeDay = isWholeDay;
        rate = 2.5f;
        this.uuid = uuid;
     }

     public boolean equalsEvent(String text){
        try{
            String[] xs = text.split(":::");
            String uuid = xs[0];
            String word = xs[1];
            return uuid.equals(this.uuid)&&word.equals(String.valueOf(week));
        }catch (Exception e){
            return false;
        }
     }

     public String getEventsIdStr(){
        return getUuid()+":::"+week;
     }
     public int getDuration(){
        return startTime.getDuration(endTime);
     }

    public EventItem(String uuid,String curriculumCode,int type, String eventName, String tag2, String tag3, String tag4, int begin, int last, int week, int DOW,boolean isWholeDay) {
        eventType = type;
        this.curriculumCode = curriculumCode;
        startTime = TimeTable.getTimeAtNumber(begin, last).get(0);
        endTime = TimeTable.getTimeAtNumber(begin, last).get(1);
        mainName = eventName;
        this.tag2 = tag2;
        this.tag3 = tag3;
        this.tag4 = tag4;
        this.week = week;
        this.DOW = DOW;
        this.isWholeDay = isWholeDay;
        rate = 2.5f;
        this.uuid = uuid;
    }


    public boolean hasOverLapping(EventItem other) {
        return this.startTime.compareTo(other.endTime) < 0 && this.endTime.compareTo(other.startTime) > 0;
    }

    public boolean hasCross(HTime t) {
        return startTime.compareTo(t) <= 0 && endTime.compareTo(t) >= 0;
    }
    public boolean hasPassed(Calendar c){
        Curriculum x = null;
        for(Curriculum cr:allCurriculum){
            if(cr.curriculumCode.equals(curriculumCode)) x = cr;
        }
        if(x==null) return false;
        int week = x.getWeekOfTerm(c);
        int dow = getDOW(c);
        if(this.week==week){
        if(this.DOW==dow){
            return this.endTime.compareTo(new HTime(c))<0;
        }else return  this.DOW-dow<0;
        }else{
        return this.week-week<0;
    }
    }

    public static int getDOW(Calendar c){
        return c.get(Calendar.DAY_OF_WEEK)==1?7:c.get(Calendar.DAY_OF_WEEK)-1;
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
        EventItem eventItem = (EventItem) o;
        return week == eventItem.week &&
                DOW == eventItem.DOW &&
                eventType == eventItem.eventType &&
                Objects.equals(startTime, eventItem.startTime) &&
                Objects.equals(endTime, eventItem.endTime) &&
                Objects.equals(mainName, eventItem.mainName) &&
                Objects.equals(curriculumCode, eventItem.curriculumCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(curriculumCode,week,DOW,startTime, endTime, eventType, mainName);
    }

    @Override
    public String toString() {
        return mainName+",type:"+eventType+",week:"+week+",dow:"+DOW+","+startTime.tellTime()+"~"+endTime.tellTime();
    }

    @Override
    public int compareTo(Object o) {
        EventItem other = (EventItem) o;
        if (this.week == other.week) {
            if (this.DOW == other.DOW) {
                return startTime.compareTo(other.startTime);
            } else {
                return this.DOW - other.DOW;
            }
        } else {
            return this.week - other.week;
        }

    }
}
