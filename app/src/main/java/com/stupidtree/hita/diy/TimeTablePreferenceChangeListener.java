package com.stupidtree.hita.diy;

import android.content.Context;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.Preference;

import cn.bmob.v3.http.I;

import static com.stupidtree.hita.activities.ActivityTimeTable.TIMETABLE_REFRESH;
import static com.stupidtree.hita.timetable.TimeWatcherService.TIMETABLE_CHANGED;

public class TimeTablePreferenceChangeListener implements Preference.OnPreferenceChangeListener{
    Context context;
    ChangeAction change;

    public TimeTablePreferenceChangeListener(Context context, ChangeAction change) {
        this.context = context;
        this.change = change;
    }

    public TimeTablePreferenceChangeListener(Context context) {
        this.context = context;
    }

    public interface ChangeAction{
        boolean OnChanged(Preference preference,Object newValue);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        callTimeTableToRefresh();
        if(change!=null) return change.OnChanged(preference,newValue);
        return true;
    }
    private void callTimeTableToRefresh(){
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(TIMETABLE_REFRESH));
    }
}
