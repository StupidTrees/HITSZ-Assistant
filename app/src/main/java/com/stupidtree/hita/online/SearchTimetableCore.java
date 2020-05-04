package com.stupidtree.hita.online;

import com.stupidtree.hita.hita.ChatBotA;
import com.stupidtree.hita.hita.Chat_SearchEvent;
import com.stupidtree.hita.hita.Term;
import com.stupidtree.hita.hita.TextTools;
import com.stupidtree.hita.timetable.packable.EventItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.timeTableCore;
import static com.stupidtree.hita.hita.ChatBotA.propcessSerchEvents;

public class SearchTimetableCore extends SearchCore<Object> {

    public SearchTimetableCore() {

    }

    @Override
    public int getPageSize() {
        return 100;
    }

    @Override
    protected List<Object> reloadResult(String text) throws SearchException {

        List<String> texts = Arrays.asList(text.replaceAll(" {2}", " ").split(" "));
        List<String> keywordConditions = new ArrayList<>();
        List<EventItem> UnderTimeCondition = new ArrayList<>();
        List res = new ArrayList<>();
        try {
            for (String condition : texts) {
                List<Term> segment = TextTools.NaiveSegmentation(condition);
                //Log.e("segment", String.valueOf(segment));
                if (ChatBotA.isTimeCondition(segment)) {
                    List<EventItem> se = propcessSerchEvents(Chat_SearchEvent.Process(segment, 0));
                    UnderTimeCondition.addAll(se);
                } else {
                    keywordConditions.add(condition);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (UnderTimeCondition.size() > 0) {
            for (EventItem ei : UnderTimeCondition) {
                boolean allMatched = true;
                for (String cdt : keywordConditions) {
                    boolean match = TextTools.likeWithContain(ei.getMainName(), cdt)
                            || TextTools.likeWithContain(ei.getTag2(), cdt)
                            || TextTools.likeWithContain(ei.getTag3(), cdt)
                            || TextTools.likeWithContain(ei.getTag4(), cdt);
                    if (!match) {
                        allMatched = false;
                        break;
                    }
                }
                if (allMatched) res.add(ei);
            }
        } else {
            res.addAll(timeTableCore.getEventWithInfoContainsAll(texts));
        }


        return res;
    }

    @Override
    protected List<Object> loadMoreResult(String text) throws SearchException {
        return new ArrayList<>();
    }





}
