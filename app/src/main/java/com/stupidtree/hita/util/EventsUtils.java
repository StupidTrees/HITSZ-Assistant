package com.stupidtree.hita.util;

import android.content.Context;
import android.text.TextUtils;

import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.BaseActivity;
import com.stupidtree.hita.fragments.popup.FragmentEvent;
import com.stupidtree.hita.timetable.TimetableCore;
import com.stupidtree.hita.timetable.packable.Curriculum;
import com.stupidtree.hita.timetable.packable.EventItem;
import com.stupidtree.hita.timetable.packable.HTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static com.stupidtree.hita.HITAApplication.HContext;


public class EventsUtils {
    public static final int TTY_NONE = 0;
    public static final int TTY_REPLACE = 1 << 1;
    public static final int TTY_FOLLOWING = 1 << 2;
    public static final int TTY_WK_REPLACE = 1 << 3;
    public static final int TTY_WK_FOLLOWING = 1 << 4;

    //单位为分钟
    private static long getDuration(Calendar x, Calendar y) {
        return Math.abs(x.getTimeInMillis() - y.getTimeInMillis()) / 60000;
    }

    public static String itWillStartIn(Calendar from, EventItem target, boolean simplified) {
        try {
            long minutes = target.getInWhatTimeWillItHappen(TimetableCore.getInstance(HContext).getCurrentCurriculum(), from);
            int weeks = (int) (minutes / 10080);
            minutes %= 10080;
            int days = (int) (minutes / 1440);
            minutes %= 1440;
            int hours = (int) (minutes / 60);
            minutes %= 60;
            String weekS = HContext.getResources().getQuantityString(
                    simplified?R.plurals.count_week_simplified:R.plurals.count_week, weeks,weeks);
            weekS = weeks > 0 ? weekS : "";
//            if (weeks == 1) weekS = weekS.replace("ks", "k"); //英文去掉复数
            String dayS = days > 0 ? String.format(HContext.getString(R.string.days), days) : "";
            if (days == 1) dayS = dayS.replace("ys", "y");
            String hourS, minuteS;
            if (!TextUtils.isEmpty(weekS)) { //有星期时，只显示星期+天数
                hourS = minuteS = "";
            } else if (!TextUtils.isEmpty(dayS)) { //有天数时，只显示天数+小时
                minuteS = "";
                hourS = hours > 0 ?
                        HContext.getString(simplified ? R.string.time_format_3_simplified : R.string.time_format_3, hours)
                        : "";
            } else {
                hourS = hours > 0 ?
                        HContext.getString(simplified ? R.string.time_format_3_simplified : R.string.time_format_3, hours)
                        : "";
                minuteS = minutes > 0 ?
                        HContext.getString(simplified ? R.string.time_format_2_simplified : R.string.time_format_2, minutes)
                        : "";
            }
            if (hours == 1) hourS = hourS.replace("rs", "r");
            if (minutes == 1) minuteS = minuteS.replace("ns", "n").replace("minutes", "minute");

            if (target.isWholeDay()) {
                if (weeks == 0 && days == 0) return HContext.getString(R.string.right_today);
                else return weekS + dayS;
            }
            if (TextUtils.isEmpty(weekS + dayS + hourS + minuteS))
                return HContext.getString(R.string.timeline_head_ongoing_subtitle);
            else return weekS + dayS + hourS + minuteS;
        } catch (Exception e) {
            return "";
        }
    }


    /**
     * simplified:简化April为Apr
     * TTYMode:0显示原信息,1替换整个为今天，2为末尾加(今天/明天)
     **/
    public static String getDateString(Calendar c, boolean simplified, int TTYMode) {
        String tag = getTTTag(c);
        String following = "";
        switch (TTYMode) {
            case TTY_NONE:
                break;
            case TTY_REPLACE:
                if (!TextUtils.isEmpty(tag)) return tag;
                break;
            case TTY_FOLLOWING:
                if (!TextUtils.isEmpty(tag)) {
                    following = HContext.getString(R.string.brackets_content, tag);
                }
                break;
        }
        return new SimpleDateFormat(HContext.getString(simplified ? R.string.date_format_3 : R.string.date_format_1), Locale.getDefault())
                .format(c.getTime()) + following;
    }


    /**
     * simplified:简化Thursday为Thu
     * TTYMode:0显示原信息,1替换整个为今天，2为末尾加(今天/明天)
     * TTY=n+3可将周数替换为这周、下周
     * TTY=n+4
     **/
    public static String getWeekDowString(EventItem ei, boolean simplified, int TTYMode) {
        if (!TimetableCore.getInstance(HContext).isDataAvailable()) return "";
        return getWeekDowString(ei.getWeek(), ei.getDOW(), simplified, TTYMode);
    }

    public static String getWeekDowString(int week, int dow, boolean simplified, int TTYMode) {
        TimetableCore tc = TimetableCore.getInstance(HContext);
        if (!tc.isDataAvailable()) return "";
        Curriculum c = tc.getCurrentCurriculum();
        Calendar then = c.getDateAt(week, dow, new HTime(12, 0));
        String rawText = new SimpleDateFormat(HContext.getString(simplified ? R.string.date_format_2_simplified : R.string.date_format_2, week), Locale.getDefault()).format(then.getTime());
        String tag = getTTTag(then);
        String wkTag = getWKTag(tc.getThisWeekOfTerm(), week);
        if ((TTYMode & TTY_WK_FOLLOWING) > 0) {
            if (!TextUtils.isEmpty(wkTag)) {
                rawText = new SimpleDateFormat(HContext.getString(simplified ? R.string.date_format_2_simplified_wk_followed : R.string.date_format_2_wk_followed, week, wkTag), Locale.getDefault()).format(then.getTime());
            }
        } else if ((TTYMode & TTY_WK_REPLACE) > 0) {
            if (!TextUtils.isEmpty(wkTag)) {
                rawText = new SimpleDateFormat(HContext.getString(simplified ? R.string.date_format_2_simplified_wk_replaced : R.string.date_format_2_wk_replaced, wkTag), Locale.getDefault()).format(then.getTime());
            }
        }
        if ((TTYMode & TTY_REPLACE) > 0) {
            if (!TextUtils.isEmpty(tag)) rawText = tag;
        } else if ((TTYMode & TTY_FOLLOWING) > 0) {
            if (!TextUtils.isEmpty(tag)) {
                rawText += HContext.getString(R.string.brackets_content, tag);
            }
        }
        return rawText;
    }

    public static int getDOW(Calendar c) {
        int tempDOW1 = c.get(Calendar.DAY_OF_WEEK);
        return tempDOW1 == 1 ? 7 : tempDOW1 - 1;
    }

    private static String getTTTag(Calendar then) {

        Calendar now = Calendar.getInstance();
        Calendar tom = (Calendar) TimetableCore.getNow().clone();
        tom.add(Calendar.DATE, 1);
        Calendar yest = (Calendar) TimetableCore.getNow().clone();
        yest.add(Calendar.DATE, -1);
        Calendar tat = (Calendar) TimetableCore.getNow().clone();
        tat.add(Calendar.DATE, 2);
        Calendar tby = (Calendar) TimetableCore.getNow().clone();
        tby.add(Calendar.DATE, -2);
        if (isSameDay(now, then)) return HContext.getString(R.string.today);
        else if (isSameDay(tom, then)) return HContext.getString(R.string.tomorrow);
        else if (isSameDay(yest, then)) return HContext.getString(R.string.yesterday);
        else if (isSameDay(tat, then)) return HContext.getString(R.string.tda_tomorrow);
        else if (isSameDay(tby, then)) return HContext.getString(R.string.tdb_yesterday);
        else return "";
    }

    private static String getWKTag(int currentWk, int week) {
        if (currentWk == week) return HContext.getString(R.string.name_this_week);
        else if (currentWk == week - 1) return HContext.getString(R.string.name_next_week);
        else if (currentWk == week + 1) return HContext.getString(R.string.name_last_week);
        return "";
    }

    private static boolean isSameDay(Calendar calDateA, Calendar calDateB) {
        return calDateA.get(Calendar.YEAR) == calDateB.get(Calendar.YEAR)
                && calDateA.get(Calendar.MONTH) == calDateB.get(Calendar.MONTH)
                && calDateA.get(Calendar.DAY_OF_MONTH) == calDateB
                .get(Calendar.DAY_OF_MONTH);
    }

    public static String getWeekDowString(EventItem ei) {
        if (!TimetableCore.getInstance(HContext).isDataAvailable()) return "";
        Curriculum c = TimetableCore.getInstance(HContext).getCurrentCurriculum();
        Calendar then = c.getDateAt(ei.getWeek(), ei.getDOW(), ei.startTime);
        return new SimpleDateFormat(HContext.getString(R.string.date_format_2_simplified, ei.week), Locale.getDefault()).format(then.getTime());
    }

    public static void showEventItem(Context context, EventItem eventItem) {
        if (context instanceof BaseActivity) {
            BaseActivity ba = (BaseActivity) context;
            ArrayList<EventItem> list = new ArrayList<>();
            list.add(eventItem);
            FragmentEvent.newInstance(list).show(ba.getSupportFragmentManager(), "event");
        }
    }

    public static void showEventItem(Context context, List<EventItem> eventItems) {
        if (context instanceof BaseActivity && eventItems instanceof ArrayList) {
            BaseActivity ba = (BaseActivity) context;
            FragmentEvent.newInstance((ArrayList<EventItem>) eventItems).show(ba.getSupportFragmentManager(), "event");
        }
    }


}
