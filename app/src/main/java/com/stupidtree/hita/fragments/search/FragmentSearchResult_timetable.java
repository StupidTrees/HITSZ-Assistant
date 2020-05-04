package com.stupidtree.hita.fragments.search;

import android.view.View;

import com.stupidtree.hita.R;
import com.stupidtree.hita.online.SearchCore;
import com.stupidtree.hita.online.SearchTimetableCore;
import com.stupidtree.hita.timetable.TimetableCore;
import com.stupidtree.hita.timetable.packable.EventItem;
import com.stupidtree.hita.util.EventsUtils;

public class FragmentSearchResult_timetable extends FragmentSearchResult<Object> {

    public FragmentSearchResult_timetable() {

    }

    public static FragmentSearchResult_timetable newInstance() {
        return new FragmentSearchResult_timetable();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_search_result_1;
    }

    @Override
    void updateHintText(boolean reload, int addedSize) {
        result.setText(getString(R.string.timetable_total_searched, listRes.size()));
    }

    @Override
    int getHolderLayoutId() {
        return R.layout.dynamic_timetable_search_result_item_event;
    }

    @Override
    SearchCore<Object> getSearchCore() {
        return new SearchTimetableCore();
    }


    @Override
    void bindHolder(SearchListAdapter.SimpleHolder holder, Object data, int position) {
        if (data instanceof EventItem) {
            EventItem ei = (EventItem) data;
            holder.title.setText(ei.getMainName());
            holder.tag.setText(EventsUtils.getWeekDowString(ei, true, EventsUtils.TTY_REPLACE | EventsUtils.TTY_WK_REPLACE));
            int typeIconId = R.drawable.ic_chatbot_arrangement;
            switch (ei.eventType) {
                case TimetableCore
                        .COURSE:
                    typeIconId = R.drawable.ic_chatbot_course;
                    break;
                case TimetableCore
                        .EXAM:
                    typeIconId = R.drawable.ic_chatbot_exam;
                    break;
                case TimetableCore
                        .DDL:
                    typeIconId = R.drawable.ic_chatbot_deadline;
                    break;
                case TimetableCore
                        .ARRANGEMENT:
                    typeIconId = R.drawable.ic_chatbot_arrangement;
                    break;
            }
            holder.picture.setImageResource(typeIconId);

        }
    }


    @Override
    void onItemClicked(View card, int position) {
        if (listRes.get(position) instanceof EventItem) {
            EventsUtils.showEventItem(getBaseActivity(), (EventItem) listRes.get(position));
        }
    }

}
