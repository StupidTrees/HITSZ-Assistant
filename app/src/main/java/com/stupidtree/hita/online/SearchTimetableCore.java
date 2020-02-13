package com.stupidtree.hita.online;

import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.WorkerThread;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stupidtree.hita.activities.ActivityChatbot;
import com.stupidtree.hita.hita.ChatBotA;
import com.stupidtree.hita.hita.Chat_SearchEvent;
import com.stupidtree.hita.hita.Term;
import com.stupidtree.hita.hita.TextTools;
import com.stupidtree.hita.timetable.TimetableCore;
import com.stupidtree.hita.timetable.timetable.EventItem;
import com.stupidtree.hita.util.JsonUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.timeTableCore;
import static com.stupidtree.hita.hita.ChatBotA.propcessSerchEvents;

public class SearchTimetableCore {
    private HashMap<String, String> cookies;

    private String lastKeyword;

    public SearchTimetableCore() {
        this.cookies = new HashMap<>();
    }

    public void reset() {
        lastKeyword = "";
    }

    public String getLastKeyword() {
        return lastKeyword;
    }

    @WorkerThread
    public List<Object> searchForResult(String text, List<String> texts) throws SearchException {
        List<String> keywordConditions = new ArrayList<>();
        List<EventItem> UnderTimeCondition = new ArrayList<>();
        lastKeyword = text;
        List<Object> res = new ArrayList<>();
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
}
