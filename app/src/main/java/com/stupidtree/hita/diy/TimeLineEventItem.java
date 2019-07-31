package com.stupidtree.hita.diy;

import com.stupidtree.hita.core.timetable.EventItem;

public class TimeLineEventItem implements Comparable{
    public EventItem event = null;
    public int type;
    public boolean now = false;
    public int progress = 0;
    public TimeLineEventItem(int type, EventItem event){
        this.type = type;
        this.event = event;
    }
    public void setType(int type){
        this.type = type;
    }
    public void setNow(boolean now){
    this.now = now;
    }
    public void setProgress(int progress){
        this.progress = progress;
    }

    @Override
    public int compareTo(Object o) {
        return event.compareTo(((TimeLineEventItem)o).event);
    }
}
