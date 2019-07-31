package com.stupidtree.hita.core.timetable;

import java.util.Calendar;

public class TimePeriod implements Comparable {
    public HTime start;
    public HTime end;
    public TimePeriod(HTime s, HTime e){
        start = s;
        end = e;
    }

    public TimePeriod(){}

    public boolean hasCross(HTime time){
        return time.compareTo(start)>=0&&time.compareTo(end)<=0;
    }

    public boolean after(HTime time){
        return time.compareTo(start)<=0;
    }

    public boolean before(HTime time){
        return time.compareTo(end)>=0;
    }

    public int getLeftTime(HTime time){
       if(hasCross(time)){
           return end.getDuration(time);
       }else return -1;
    }
    @Override
    public int compareTo(Object o) {
//        if(start.compareTo(((TimePeriod)o).start)==0) return end.compareTo(((TimePeriod)o).end);
//        else return start.compareTo(((TimePeriod)o).start);
        return ((TimePeriod)o).getLength()-this.getLength();
    }

    @Override
    public String toString() {
        return start.tellTime()+"~"+end.tellTime();
    }

    public int getLength(){
        return start.getDuration(end);
    }

    public TimePeriod subPeriod(int from,int length){
        HTime s = start.getAdded(from);
        HTime e = s.getAdded(length);
        return new TimePeriod(s,e);
    }
    public TimePeriod subPeriod(int from){
        HTime s = start.getAdded(from);
        return new TimePeriod(s,end);
    }
}
