package com.stupidtree.hita.core.timetable;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Objects;

/*时间类，只有小时和分钟*/
public class HTime implements Comparable, Serializable {
    public int hour;
    public int minute;

    public HTime(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }
    public HTime(Calendar c){
        this.hour = c.get(Calendar.HOUR_OF_DAY);
        this.minute = c.get(Calendar.MINUTE);
    }

    public void setTime(int hour,int minute){
        this.hour = hour;
        this.minute = minute;
    }

    public HTime getAdded(int minute){
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY,hour);
        c.set(Calendar.MINUTE,this.minute);
        c.add(Calendar.MINUTE,minute);
        int Nhour = c.get(Calendar.HOUR_OF_DAY);
        int Nminute = c.get(Calendar.MINUTE);
        return new HTime(Nhour,Nminute);
    }

    public HTime getSub(int minute){
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY,hour);
        c.set(Calendar.MINUTE,this.minute);
        c.add(Calendar.MINUTE,-minute);
        int Nhour = c.get(Calendar.HOUR_OF_DAY);
        int Nminute = c.get(Calendar.MINUTE);
        return new HTime(Nhour,Nminute);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HTime hTime = (HTime) o;
        return hour == hTime.hour &&
                minute == hTime.minute;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hour, minute);
    }


    public boolean during(TimePeriod tp){
      return tp.start.compareTo(this)<=0&&tp.end.compareTo(this)>=0;
    }
    @Override
    public int compareTo(Object o) {
        if(this.hour==((HTime) o).hour) return this.minute - ((HTime) o).minute;
        else return this.hour-((HTime) o).hour;
    }

    public String tellTime() {
        String minuteText;
        if(minute<10){
            minuteText = "0"+minute;
        }else{
            minuteText = minute+"";
        }
        return hour + ":" + minuteText;
    }

    @Override
    public String toString() {
        return tellTime();
    }

    public int getDuration(HTime other) {
        HTime start = this.compareTo(other) > 0 ? other : this;
        HTime end = this.compareTo(other) > 0 ? this : other;
        return (60 - start.minute) + (end.hour - (start.hour + 1)) * 60 + end.minute;
    }

    public boolean after(HTime other){
        return this.compareTo(other)>0;
    }

    public boolean before(HTime other){
        return this.compareTo(other)<0;
    }
}
