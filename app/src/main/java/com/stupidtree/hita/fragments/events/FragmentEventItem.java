package com.stupidtree.hita.fragments.events;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.stupidtree.hita.R;
import com.stupidtree.hita.fragments.BaseFragment;
import com.stupidtree.hita.timetable.packable.EventItem;

import static com.stupidtree.hita.HITAApplication.TPE;
import static com.stupidtree.hita.HITAApplication.timeTableCore;
import static com.stupidtree.hita.timetable.TimeWatcherService.TIMETABLE_CHANGED;
import static com.stupidtree.hita.timetable.TimetableCore.COURSE;
import static com.stupidtree.hita.timetable.TimetableCore.DDL;
import static com.stupidtree.hita.timetable.TimetableCore.EXAM;

abstract public class FragmentEventItem extends BaseFragment {
    EventItem eventItem;
    PopupFragment popupRoot;

    public FragmentEventItem() {
    }

    public static FragmentEventItem newInstance(EventItem ei) {
        FragmentEventItem fe;
        switch (ei.getEventType()) {
            case COURSE:
                fe = new FragmentCourse();
                break;
            case EXAM:
                fe = new FragmentExam();
                break;
            case DDL:
                fe = new FragmentDDL();
                break;
            default:
                fe = new FragmentArrangement();
        }
        Bundle b = new Bundle();
        b.putSerializable("event", ei);
        fe.setArguments(b);
        return fe;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventItem = (EventItem) getArguments().getSerializable("event");
        }
    }


    public void setRoot(PopupFragment root) {
        this.popupRoot = root;
    }

    public EventItem getEventItem() {
        return eventItem;
    }

    @Override
    protected void stopTasks() {

    }

    @Override
    public void Refresh() {

    }


    void deleteEvent() {
        new deleteTask().executeOnExecutor(TPE);
    }

    public interface PopupFragment {
        void callDismiss();
    }

    class deleteTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            return timeTableCore.deleteEvent(eventItem, eventItem.eventType == DDL);
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            Toast.makeText(getContext(), getString(R.string.notif_delete_success), Toast.LENGTH_SHORT).show();
            Intent i = new Intent();
            i.putExtra("week", eventItem.week);
            i.setAction(TIMETABLE_CHANGED);
            //Intent i2 = new Intent();
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(i);
            // dialog.dismiss();
            if (popupRoot != null) popupRoot.callDismiss();
        }
    }
}
