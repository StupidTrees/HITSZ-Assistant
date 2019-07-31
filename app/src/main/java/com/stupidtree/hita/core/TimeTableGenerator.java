package com.stupidtree.hita.core;

import android.util.EventLog;
import android.util.Log;

import com.stupidtree.hita.core.timetable.EventItem;
import com.stupidtree.hita.core.timetable.EventItemHolder;
import com.stupidtree.hita.core.timetable.HTime;
import com.stupidtree.hita.core.timetable.TimePeriod;

import org.xml.sax.DTDHandler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import static com.stupidtree.hita.HITAApplication.allCurriculum;
import static com.stupidtree.hita.HITAApplication.mainTimeTable;
import static com.stupidtree.hita.HITAApplication.now;
import static com.stupidtree.hita.HITAApplication.themeID;
import static com.stupidtree.hita.HITAApplication.thisCurriculumIndex;
import static com.stupidtree.hita.HITAApplication.thisWeekOfTerm;

public class TimeTableGenerator {
    public static void Dynamic_PreviewPlan(Calendar present, TimeTable timeTable) {
        int minDURATION = 40;
        Calendar from = (Calendar) present.clone();
        Calendar to = (Calendar) present.clone();
        from.set(Calendar.HOUR_OF_DAY, 0);
        from.set(Calendar.MINUTE, 0);
        to.set(Calendar.HOUR_OF_DAY, 23);
        to.set(Calendar.MINUTE, 59);
        timeTable.deleteEvent(from, to, TimeTable.TIMETABLE_EVENT_TYPE_DYNAMIC);
        List<TimePeriod> breakTime = getBreakTime();
        for (TimePeriod tp : breakTime) {
            mainTimeTable.addEvent(thisWeekOfTerm, TimeTable.getDOW(now), TimeTable.TIMETABLE_EVENT_TYPE_DYNAMIC, "%%%break", "", "", "", tp.start, tp.end, false);
        }
        List<TimePeriod> spaces = timeTable.getSpaces(from, to, minDURATION, -1);
        timeTable.clearEvent(TimeTable.TIMETABLE_EVENT_TYPE_DYNAMIC, "%%%break");
        Collections.sort(spaces);
        for (TimePeriod tp : spaces) {
            mainTimeTable.addEvent(thisWeekOfTerm, TimeTable.getDOW(now), TimeTable.TIMETABLE_EVENT_TYPE_DYNAMIC, "动态", "", "", "", tp.start, tp.end, false);
        }

//        for(TimePeriod tp:spaces){
//            EventItem
//        }
        Log.e("after:", String.valueOf(spaces));
        //Log.e("before:", String.valueOf(spaces));
//        dealWithBreak(spaces,getBreakTime(),minDURATION);
//        Log.e("afterbreak:", String.valueOf(spaces));
//
//
//        from.add(Calendar.DATE,1);
//        to.add(Calendar.DATE,2);
//        from.set(Calendar.HOUR_OF_DAY,0);
//        from.set(Calendar.MINUTE,0);
//        List<EventItem> ddlS  = timeTable.getEventFrom(from,to,TimeTable.TIMETABLE_EVENT_TYPE_DEADLINE);
//        List<EventItem> exams  = timeTable.getEventFrom(from,to,TimeTable.TIMETABLE_EVENT_TYPE_EXAM);
//        //Log.e("ddlS", String.valueOf(ddlS));
//        if(exams!=null&&exams.size()>0){
//            spaces = splitPeriod(spaces,exams.size());
//            Log.e("afterSplit:", String.valueOf(spaces));
//            int i = 0;
//            for(TimePeriod xx:spaces){
//                if(xx.before(new HTime(now))) continue;
//                if(xx.getLeftTime(new HTime(now))>0&&xx.getLeftTime(new HTime(now))<minDURATION) continue;
//                HTime start = xx.start;
//                HTime end = xx.end;
//                if(i<exams.size()) {
//                    timeTable.addEvent(timeTable.core.getWeekOfTerm(present), timeTable.getDOW(present), TimeTable.TIMETABLE_EVENT_TYPE_DYNAMIC, "准备" + exams.get(i).mainName, "临近考试", "目标优先级高于一切", "！！！！", start, end,false);
//
//                }i++;
//            }
//        }else if(ddlS!=null&&ddlS.size()>0){
//            spaces = splitPeriod(spaces,ddlS.size());
//            Log.e("afterSplit:", String.valueOf(spaces));
//            int i = 0;
//            for(TimePeriod xx:spaces){
//                if(xx.before(new HTime(now))) continue;
//                if(xx.getLeftTime(new HTime(now))>0&&xx.getLeftTime(new HTime(now))<minDURATION) continue;
//                HTime start = xx.start;
//                HTime end = xx.end;
//                if(i<ddlS.size()) {
//                    timeTable.addEvent(timeTable.core.getWeekOfTerm(present), timeTable.getDOW(present), TimeTable.TIMETABLE_EVENT_TYPE_DYNAMIC, "处理DDL:" + ddlS.get(i).mainName, "明后天有DDL", "目标优先级高于一切", "完成后请删除DDL", start, end,false);
//
//                }i++;
//            }
//        }else{
//            to.add(Calendar.DATE,-1);
//            List<EventItem> courses = timeTable.getEventFrom(present,to,TimeTable.TIMETABLE_EVENT_TYPE_COURSE);
//            List<EventItem> examCourse = new ArrayList<>();
//            //Log.e("courses", String.valueOf(courses));
//            if(courses!=null){
//                for(EventItem course:courses){
//                    if(allCurriculum.get(thisCurriculumIndex).getSubjectByCourse(course)!=null&&allCurriculum.get(thisCurriculumIndex).getSubjectByCourse(course).exam){
//                        examCourse.add(course);
//                    }
//                }
//                Log.e("examCourses", String.valueOf(examCourse));
//                spaces = splitPeriod(spaces,examCourse.size());
//                Log.e("afterSplit:", String.valueOf(spaces));
//            }
//
//
//              //Log.e("courses", String.valueOf(courses));
//            if(examCourse.size()>0){
//                int i = 0;
//                for(TimePeriod xx:spaces){
//                    if(xx.before(new HTime(now))) continue;
//                    if(xx.getLeftTime(new HTime(now))>0&&xx.getLeftTime(new HTime(now))<minDURATION) continue;
//                    HTime start = xx.start;
//                    HTime end = xx.end;
//                    if(i<examCourse.size()){
//                        timeTable.addEvent(timeTable.core.getWeekOfTerm(present),timeTable.getDOW(present),TimeTable.TIMETABLE_EVENT_TYPE_DYNAMIC,"预习"+examCourse.get(i).mainName,"嗯！","无","无",start,end,false);
//                    }
//                    i++;
//                }
//            }
//
//        }

    }

    public static void autoAdd(int week, int dow, String name, String tag2, String tag3, String tag4, int type, int duration) {
        int minDURATION = 40;
        Calendar date = mainTimeTable.core.getDateAtWOT(week, dow);
        Calendar from = (Calendar) date.clone();
        Calendar to = (Calendar) date.clone();
        from.set(Calendar.HOUR_OF_DAY, 0);
        from.set(Calendar.MINUTE, 0);
        to.set(Calendar.HOUR_OF_DAY, 23);
        to.set(Calendar.MINUTE, 59);
        mainTimeTable.deleteEvent(from, to, TimeTable.TIMETABLE_EVENT_TYPE_DYNAMIC);
        List<TimePeriod> breakTime = getBreakTime();
        for (TimePeriod tp : breakTime) {
            mainTimeTable.addEvent(thisWeekOfTerm, TimeTable.getDOW(now), TimeTable.TIMETABLE_EVENT_TYPE_DYNAMIC, "%%%break", "", "", "", tp.start, tp.end, false);
        }
        List<TimePeriod> spaces = mainTimeTable.getSpaces(from, to, minDURATION, -1);
        mainTimeTable.clearEvent(TimeTable.TIMETABLE_EVENT_TYPE_DYNAMIC, "%%%break");
        Collections.sort(spaces);
        TimePeriod tp = spaces.get(spaces.size() - 1);
        for(int i = spaces.size()-1;i>=0;i--){
            if(spaces.get(i).getLength()>duration){
                HTime start = tp.start.getAdded(15);
                HTime end = tp.start.getAdded(duration);
                mainTimeTable.addEvent(week, dow, type, name, tag2, tag3, tag4, start, end, false);
                break;
            }else{
                //split
            }
        }

//        if(type==TimeTable.TIMETABLE_EVENT_TYPE_DEADLINE){
//            mainTimeTable.addTask("处理DDL:"+name,allCurriculum.get(thisCurriculumIndex).getWeekOfTerm(now), TimeTable.getDOW(now),new HTime(now),toAdd);
//        }

//        for(TimePeriod tp:spaces){
//            EventItem
//        }
        Log.e("after:", String.valueOf(spaces));
    }

    private static List<TimePeriod> dealWithBreak(List<TimePeriod> res, List<TimePeriod> breakT, int minDurationMinute) {
        List<TimePeriod> toDel = new ArrayList<>();
        List<TimePeriod> toAdd = new ArrayList<>();
        for (TimePeriod m : res) {

            List<TimePeriod> crosses = getCross(breakT, m);
            // Log.e("M：","m:"+m+",crosses:"+crosses+"\n");
            if (crosses.size() == 1) {
                if (crosses.get(0).start.compareTo(m.start) < 0)
                    crosses.get(0).start = m.start;
                if (crosses.get(0).end.compareTo(m.end) > 0)
                    crosses.get(0).end = m.end;
                TimePeriod newM = new TimePeriod();
                TimePeriod newM2 = new TimePeriod();
                // Log.e("fff", String.valueOf(crosses));
                if (crosses.get(0).start.getDuration(m.start) >= minDurationMinute) {
                    newM.start = m.start;
                    newM.end = crosses.get(0).start;
                    toAdd.add(newM);
                }
                if (crosses.get(0).end.getDuration(m.end) >= minDurationMinute) {
                    newM2.end = m.end;
                    newM2.start = crosses.get(0).end;
                    toAdd.add(newM2);
                }
            } else {
                for (int i = 0; i < crosses.size(); i++) {
                    //System.out.println("处理第"+i+1+"个cross");
                    TimePeriod newM = new TimePeriod();
                    if (crosses.get(i).start.compareTo(m.start) < 0) crosses.get(i).start = m.start;
                    if (crosses.get(i).end.compareTo(m.end) > 0) crosses.get(i).end = m.end;
                    if (i == 0) {
                        if (crosses.get(i).start.getDuration(m.start) >= minDurationMinute) {
                            newM.start = m.start;
                            newM.end = crosses.get(i).start;
                            //Log.e("add1","break:"+crosses.get(i)+ String.valueOf(newM));
                            toAdd.add(newM);
                        }
                    } else if (i == crosses.size() - 1) {
                        if (crosses.get(i).end.getDuration(m.end) >= minDurationMinute) {
                            newM.end = m.end;
                            newM.start = crosses.get(i).end;
                            //Log.e("add2","break:"+crosses.get(i)+  String.valueOf(newM));
                            toAdd.add(newM);
                        }
                    }
                    if (i + 1 < crosses.size() && crosses.get(i).end.getDuration(crosses.get(i + 1).start) >= minDurationMinute) {
                        TimePeriod newM3 = new TimePeriod();
                        newM3.start = crosses.get(i).end;
                        newM3.end = crosses.get(i + 1).start;
                        //Log.e("add3", "break:"+crosses.get(i)+ String.valueOf(newM3));
                        toAdd.add(newM3);
                    }
                }
            }
            if (crosses.size() > 0) toDel.add(m);
        }
        res.removeAll(toDel);
        res.addAll(toAdd);
        Collections.sort(res);
        return res;
    }

    private static List<TimePeriod> getCross(List<TimePeriod> breakT, TimePeriod res) {
        List<TimePeriod> result = new ArrayList<>();
        //Log.e("getCross","breakT:"+breakT+",,,res="+res);
        for (TimePeriod m : breakT) {
            // Log.e("Loop","n:"+m+",,,res="+res);
            if ((m.start.compareTo(res.end) <= 0 && m.start.compareTo(res.start) >= 0)
                    || (m.end.compareTo(res.start) >= 0 && m.end.compareTo(res.end) <= 0))
                result.add(m);
        }
        return result;
    }

    //    private static List<TimePeriod> getPeriodPieces(List<TimePeriod> res,int periodLength){
//        for(TimePeriod m:res){
//
//        }
//    }
    private static List<TimePeriod> splitPeriod(List<TimePeriod> res, int num) {
        if (num == 0 || num == 1) return res;
        List<TimePeriod> result = new ArrayList<>();
        int sumLength = 0;
        for (TimePeriod tp : res) {
            sumLength += tp.getLength();
        }
        int clip = sumLength / num;
        List<TimePeriod> tempQueue = new ArrayList<>();
        Log.e("sum=", String.valueOf(sumLength));
        for (TimePeriod tp : res) {
            if (!tempQueue.isEmpty()) {
                int tempSum = 0;
                for (TimePeriod k : tempQueue) tempSum += k.getLength();
                if (tempSum + tp.getLength() < clip) {
                    Log.e("add1", String.valueOf(tp));
                    tempQueue.add(tp);
                    continue;
                } else {
                    result.addAll(tempQueue);
                    tempQueue.clear();
                    result.add(tp.subPeriod(0, clip - tempSum));
                    if (tp.getLength() - (clip - tempSum) > clip) {
                        for (int j = 0; j < (tp.getLength() - (clip - tempSum)) / clip; j++) {
                            result.add(tp.subPeriod(tp.getLength() - (clip - tempSum) + j * clip, clip));
                            if (j == ((tp.getLength() - (clip - tempSum)) / clip) - 1) {
                                TimePeriod tpT = tp.subPeriod(tp.getLength() - (clip - tempSum) + (j + 1) * clip);
                                Log.e("add2", String.valueOf(tpT));
                                tempQueue.add(tpT);
                            }
                        }
                    }
                }
            } else if (tp.getLength() > clip) {
                for (int j = 0; j < tp.getLength() / clip; j++) {
                    TimePeriod tpT = tp.subPeriod(j * clip, clip);
                    Log.e("add3", String.valueOf(tpT));
                    result.add(tpT);
                    if (j == (tp.getLength() / clip) - 1)
                        tempQueue.add(tp.subPeriod((j + 1) * clip));
                }
            } else {
                tempQueue.add(tp);
            }
        }
        return result;
    }


    private static List<TimePeriod> getBreakTime() {
        List<TimePeriod> breakT = new ArrayList<>();
        TimePeriod m0 = new TimePeriod();
        m0.start = new HTime(0, 00);
        m0.end = new HTime(8, 10);
        TimePeriod m = new TimePeriod();
        m.start = new HTime(12, 30);
        m.end = new HTime(13, 20);
        TimePeriod m2 = new TimePeriod();
        m2.start = new HTime(17, 45);
        m2.end = new HTime(18, 10);

        TimePeriod m3 = new TimePeriod();
        m3.start = new HTime(22, 30);
        m3.end = new HTime(23, 59);
        breakT.add(m0);
        breakT.add(m);
        breakT.add(m2);
        breakT.add(m3);

        Collections.sort(breakT);
        return breakT;
    }


}
