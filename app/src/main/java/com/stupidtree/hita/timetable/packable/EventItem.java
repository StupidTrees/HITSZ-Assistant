package com.stupidtree.hita.timetable.packable;

import androidx.annotation.NonNull;

import com.stupidtree.hita.R;
import com.stupidtree.hita.timetable.TimetableCore;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Objects;
import java.util.UUID;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.timetable.TimetableCore.ARRANGEMENT;
import static com.stupidtree.hita.timetable.TimetableCore.COURSE;
import static com.stupidtree.hita.timetable.TimetableCore.DDL;
import static com.stupidtree.hita.timetable.TimetableCore.EXAM;

/*事件类*/
public class EventItem implements Serializable, Comparable {
    public static final int TAG = 488;
    public int week;
    public int DOW;
    public HTime startTime;
    public HTime endTime;
    public int eventType;
    public String mainName;//名称
    public String tag2;     //地点
    public String tag3;     //Exam为科目名，Course为教师
    public String tag4;     //Exam为具体时间，Course为课程节数
    private float rate; //用在course的评分
    public String curriculumCode;
    private boolean isWholeDay;
    private String uuid;


    public EventItem(String uuid, String curriculumCode, int type, String eventName, String tag2, String tag3, String tag4, HTime start, HTime end, int week, int DOW, boolean isWholeDay) {
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

    public EventItem(String uuid, String curriculumCode, int type, String eventName, String tag2, String tag3, String tag4, int begin, int last, int week, int DOW, boolean isWholeDay) {
        eventType = type;
        this.curriculumCode = curriculumCode;
        startTime = TimetableCore.getTimeAtNumber(begin, last).get(0);
        endTime = TimetableCore.getTimeAtNumber(begin, last).get(1);
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

    public static EventItem getTagInstance(String tag) {
        EventItem e = new EventItem(UUID.randomUUID().toString(), "", TAG, tag, "", "", "", new HTime(0, 0), new HTime(0, 0), -1, 1, true);
        e.setUuid(tag);
        return e;
    }

    public static int getDOW(Calendar c) {
        return c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ? 7 : c.get(Calendar.DAY_OF_WEEK) - 1;
    }

    public boolean equalsEvent(String text) {
        try {
            String[] xs = text.split(":::");
            String uuid = xs[0];
            String word = xs[1];
            return !uuid.equals(this.uuid) || !word.equals(String.valueOf(week));
        } catch (Exception e) {
            return true;
        }
    }

    public String getEventsIdStr() {
        return getUuid() + ":::" + week;
    }

    public int getDuration() {
        return startTime.getDuration(endTime);
    }

    public int getTypeName() {
        switch (eventType) {
            case DDL:
                return R.string.ade_ddl;
            case ARRANGEMENT:
                return R.string.ade_arrangement;
            case EXAM:
                return R.string.ade_exam;
            case COURSE:
                return R.string.ade_course;
        }
        return R.string.ade_arrangement;
    }

    public int getDuration(EventItem ei) {
        int weeks = ei.week - this.week;
        Calendar temp1 = Calendar.getInstance();
        temp1.set(Calendar.WEEK_OF_YEAR, 0);
        temp1.set(Calendar.HOUR_OF_DAY, startTime.hour);
        temp1.set(Calendar.MINUTE, startTime.minute);
        temp1.set(Calendar.DAY_OF_WEEK, DOW == 7 ? 1 : DOW + 1);
        Calendar temp2 = Calendar.getInstance();
        temp2.set(Calendar.WEEK_OF_YEAR, weeks);
        temp2.set(Calendar.HOUR_OF_DAY, ei.startTime.hour);
        temp2.set(Calendar.MINUTE, ei.startTime.minute);
        temp1.set(Calendar.DAY_OF_WEEK, ei.DOW == 7 ? 1 : ei.DOW + 1);
        return (int) ((temp2.getTimeInMillis() - temp1.getTimeInMillis()) / 3600);
    }

    public boolean isSameDay(Curriculum curriculum, Calendar c) {

        int week = curriculum.getWeekOfTerm(c);
        int dow = getDOW(c);
        // Log.e("is_same_day",c+",,"+mainName+":"+"w:"+week+"dow:"+dow);
        return week == this.week && dow == this.DOW;
    }


    public boolean hasOverLapping(EventItem other) {
        return this.startTime.compareTo(other.endTime) < 0 && this.endTime.compareTo(other.startTime) > 0;
    }

    public boolean hasCross(HTime t) {
        return startTime.compareTo(t) <= 0 && endTime.compareTo(t) >= 0;
    }

    public boolean hasCross_Strict(HTime t) {
        return startTime.compareTo(t) < 0 && endTime.compareTo(t) > 0;
    }

    public long getInWhatTimeWillItHappen(Curriculum c, Calendar x) {
        Calendar temp1 = c.getDateAt(week, DOW, startTime);
        Calendar temp2 = Calendar.getInstance();
        temp2.setTimeInMillis(x.getTimeInMillis());
        //Log.e("test_date","wk:"+wk+",week:"+week+"//"+temp1.getTime().toString()+","+temp2.getTime().toString());
        return (int) ((temp1.getTimeInMillis() - temp2.getTimeInMillis()) / 60000);
    }

    public boolean hasPassed(Calendar c) {
        TimetableCore tc = TimetableCore.getInstance(HContext);
        if (!tc.isDataAvailable()) return false;
        int week = tc.getCurrentCurriculum().getWeekOfTerm(c);
        int dow = getDOW(c);
        if (this.week == week) {
            if (this.DOW == dow) {
                if (this.isWholeDay()) return false;
                return this.endTime.compareTo(new HTime(c)) < 0;
            } else return this.DOW - dow < 0;
        } else {
            return this.week - week < 0;
        }
    }

    public boolean hasPassed(long timeInMills) {
        TimetableCore tc = TimetableCore.getInstance(HContext);
        if (!tc.isDataAvailable()) return false;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timeInMills);
        int week = tc.getCurrentCurriculum().getWeekOfTerm(c);
        int dow = getDOW(c);
        if (this.week == week) {
            if (this.DOW == dow) {
                if (this.isWholeDay()) return false;
                return this.endTime.compareTo(new HTime(c)) < 0;
            } else return this.DOW - dow < 0;
        } else {
            return this.week - week < 0;
        }
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
                Objects.equals(uuid, eventItem.getUuid())
                ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(curriculumCode, week, DOW, startTime, endTime, eventType, mainName);
    }

    @NonNull
    @Override
    public String toString() {
        return mainName + ",type:" + eventType + ",week:" + week + ",dow:" + DOW + "," + startTime.tellTime() + "~" + endTime.tellTime() + ",tag3:" + tag3;
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

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    public int getDOW() {
        return DOW;
    }

    public void setDOW(int DOW) {
        this.DOW = DOW;
    }

    public HTime getStartTime() {
        return startTime;
    }

    public void setStartTime(HTime startTime) {
        this.startTime = startTime;
    }

    public HTime getEndTime() {
        return endTime;
    }

    public void setEndTime(HTime endTime) {
        this.endTime = endTime;
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public String getMainName() {
        return mainName;
    }

    public void setMainName(String mainName) {
        this.mainName = mainName;
    }

    public String getTag2() {
        return tag2;
    }

    public void setTag2(String tag2) {
        this.tag2 = tag2;
    }

    public String getTag3() {
        return tag3;
    }

    public void setTag3(String tag3) {
        this.tag3 = tag3;
    }

    public String getTag4() {
        return tag4;
    }

    public void setTag4(String tag4) {
        this.tag4 = tag4;
    }

    public float getRate() {
        return rate;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }

    public String getCurriculumCode() {
        return curriculumCode;
    }

    public void setCurriculumCode(String curriculumCode) {
        this.curriculumCode = curriculumCode;
    }

    public boolean isWholeDay() {
        return isWholeDay;
    }

    public void setWholeDay(boolean wholeDay) {
        isWholeDay = wholeDay;
    }
}
