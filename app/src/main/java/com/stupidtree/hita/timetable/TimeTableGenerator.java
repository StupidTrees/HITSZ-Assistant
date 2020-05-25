package com.stupidtree.hita.timetable;

import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.WorkerThread;

import com.stupidtree.hita.timetable.packable.EventItem;
import com.stupidtree.hita.timetable.packable.HTime;
import com.stupidtree.hita.timetable.packable.Subject;
import com.stupidtree.hita.timetable.packable.Task;
import com.stupidtree.hita.timetable.packable.TimePeriod;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.defaultSP;


public class TimeTableGenerator {

    @WorkerThread
    static void Dynamic_PreviewPlan(Calendar present) {
        TimetableCore tc = TimetableCore.getInstance(HContext);
        int totalLength =  defaultSP.getInt("dtt_preview_length",60); //每天花一小时预习
        Calendar from = (Calendar) present.clone();
        Calendar to = (Calendar) present.clone();
        from.set(Calendar.HOUR_OF_DAY, 0);
        from.set(Calendar.MINUTE, 0);
        to.set(Calendar.HOUR_OF_DAY, 23);
        to.set(Calendar.MINUTE, 59);
        from.add(Calendar.DATE,1);
        to.add(Calendar.DATE,1);
        List<EventItem> toRemove = new ArrayList<>();
        HashMap<EventItem,Float> courseMap = new HashMap<>();
        List<EventItem> courses = tc.getEventFrom(from, to, TimetableCore.COURSE);
        if(courses==null||courses.size()==0) return;
        boolean skipNoExam = defaultSP.getBoolean("dtt_preview_skip_no_exam",true);
        for(EventItem ei:courses){
            Subject subject = tc.getSubjectByCourse(ei);
            if(subject==null) continue;
            if((!subject.isExam())&&skipNoExam)  toRemove.add(ei);
            else courseMap.put(ei,subject.getPriority());
        }

        for(Map.Entry<EventItem,Float> entry:courseMap.entrySet()){
            Task t = new Task(tc.getCurrentCurriculum().getCurriculumCode(), "预习" + entry.getKey().mainName);
            t.setType(Task.TYPE_DYNAMIC);
            t.setPriority(entry.getValue().intValue());
            String tag = entry.getKey().getUuid()+":::"+entry.getKey().week;
            t.setTag(tag);
            if (tc.getTaskByTag(tag) == null) {
                tc.addTask(t);
            }
        }

    }

    public static SparseArray<HTime> autoAdd_getTime(Calendar now,int week, int dow, int duration) {
        int minDURATION = 40;
        TimetableCore tc = TimetableCore.getInstance(HContext);
        Calendar date = tc.getCurrentCurriculum().getDateAtWOT(week, dow);
        Calendar from,to;
        if (now != null && date.get(Calendar.YEAR) == TimetableCore.getNow().get(Calendar.YEAR) && date.get(Calendar.DAY_OF_YEAR) == TimetableCore.getNow().get(Calendar.DAY_OF_YEAR)) {
            from = (Calendar) TimetableCore.getNow().clone();
            to = (Calendar) date.clone();
        }else{
            from = (Calendar) date.clone();
            to = (Calendar) date.clone();
            from.set(Calendar.HOUR_OF_DAY, 0);
            from.set(Calendar.MINUTE, 0);
        }
        to.set(Calendar.HOUR_OF_DAY, 23);
        to.set(Calendar.MINUTE, 59);
        //tc.deleteEvent(from, to, TimetableCore.DYNAMIC);
        List<TimePeriod> breakTime = getBreakTime();
        List<EventItem> breakTemp = new ArrayList<>();
        for (TimePeriod tp : breakTime) {
            breakTemp.add(new EventItem("", tc.getCurrentCurriculum().getCurriculumCode(), TimetableCore.DYNAMIC, "%%%break", "", "", "", tp.start, tp.end, week, dow, false));
        }
        List<TimePeriod> spaces = tc.getSpaces(breakTemp, from, to, minDURATION, -1);
       // Log.e("spaces", String.valueOf(spaces));
        //tc.clearEvent(TimetableCore.DYNAMIC, "%%%break");
        Collections.sort(spaces);
       // Log.e( "autoAdd_getTime:the free time is: ",spaces.toString() );
        if(spaces.size() == 0) return null;
        TimePeriod tp = spaces.get(spaces.size()-1);
        String uuid = null;
        for(int i = spaces.size()-1;i>=0;i--){
            if(spaces.get(i).getLength()>duration){
                int x = spaces.get(i).getLength()/4;
                int padding = Math.min(x, 5);
                HTime start = tp.start.getAdded(padding);
                HTime end = tp.start.getAdded(duration);
                SparseArray<HTime> result = new SparseArray<>(2);
                result.put(0,start);
                result.put(1,end);
                return result;
            }
        }
        return null;

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
        m0.start = new HTime(0, 0);
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

        //Collections.sort(breakT);
        return breakT;
    }


}
