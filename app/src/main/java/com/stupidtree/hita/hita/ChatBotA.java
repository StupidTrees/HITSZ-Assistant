package com.stupidtree.hita.hita;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.timetable.timetable.EventItem;
import com.stupidtree.hita.timetable.timetable.HTime;


import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.now;
import static com.stupidtree.hita.HITAApplication.timeTableCore;
import static com.stupidtree.hita.activities.ActivityChatbot.STATE_SEARCH_COURSE_SINGLE;
import static com.stupidtree.hita.activities.ActivityChatbot.State;
import static com.stupidtree.hita.hita.TextTools.BEFORE;
import static com.stupidtree.hita.hita.TextTools.NEXT;
import static com.stupidtree.hita.hita.TextTools.THIS;
import static com.stupidtree.hita.hita.TextTools.TT_BEFORE;
import static com.stupidtree.hita.hita.TextTools.TT_NEXT;
import static com.stupidtree.hita.hita.TextTools.T_BEFORE;
import static com.stupidtree.hita.hita.TextTools.T_NEXT;
import static com.stupidtree.hita.timetable.TimetableCore.TIMETABLE_EVENT_TYPE_ARRANGEMENT;
import static com.stupidtree.hita.timetable.TimetableCore.TIMETABLE_EVENT_TYPE_COURSE;
import static com.stupidtree.hita.timetable.TimetableCore.TIMETABLE_EVENT_TYPE_DEADLINE;
import static com.stupidtree.hita.timetable.TimetableCore.TIMETABLE_EVENT_TYPE_EXAM;
import static com.stupidtree.hita.timetable.TimetableCore.TIMETABLE_EVENT_TYPE_REMIND;


public class ChatBotA {

    List<Term> mResult;

    public static final int FUN_ADD_EVENT_REMIND = 651;
    public final static int FUN_SEARCH_TASK = 456;
    public final static int FUN_SEARCH_EVENT_COURSE = 1;
    public final static int FUN_SEARCH_EVENT_ARRANGE = 2;
    public final static int FUN_SEARCH_EVENT_DDL = 3;
    public final static int FUN_SEARCH_EVENT_EXAM = 4;
    public final static int FUN_SEARCH_EVENT_REMIND = 5;
    public final static int FUN_SEARCH_EVENT_ALL = 0;
    private static final int FUN_SEARCH_PEOPLE = 768;
    public final static int FUN__STATE_SINGLE_SHOW_CLASSROOM = 35;
    public static final int FUN_INTENT_EXPLORE = 465;
    public static final int FUN_INTENT_CANTEEN = 843;
    public static final int FUN_INTENT_JWTS = 543;
    private static final int FUN_INTENT_INFOS = 855;
    private static final int FUN_INTENT_LAF = 845;
    private static final int FUN_QUERY_SUBJECTS = 752;

    Activity activityContext;

    public ChatBotA(Activity context) {
        activityContext = context;
    }


    public boolean simpleJudge(String text) {
        mResult = TextTools.NaiveSegmentation(text);
        Log.e("切词+reTag", String.valueOf(mResult));
        return Judge_Function(text,mResult) != -1;
    }

    public static boolean isTimeCondition(List<Term> terms){
        int test = Chat_SearchEvent.Judge(terms);
        return test!=0;
    }
    public static List<EventItem> propcessSerchEvents(JsonObject values) {
        int fromW = values.get("fW").getAsInt();
        int toW = values.get("tW").getAsInt();
        int fromDOW = values.get("fDOW").getAsInt();
        int toDOW = values.get("tDOW").getAsInt();
        HTime fromT = new HTime(values.get("fH").getAsInt(), values.get("fM").getAsInt());
        HTime toT = new HTime(values.get("tH").getAsInt(), values.get("tM").getAsInt());
        int tag = values.get("tag").getAsInt();
        int num = values.get("num").getAsInt();
        int thisDOW = now.get(Calendar.DAY_OF_WEEK) == 1 ? 7 : now.get(Calendar.DAY_OF_WEEK) - 1;
        if (fromW == BEFORE) fromW = timeTableCore.getThisWeekOfTerm() - 1 <= 0 ? 1 : timeTableCore.getThisWeekOfTerm() - 1;
        if (fromW == THIS) fromW = timeTableCore.isThisTerm() ? timeTableCore.getThisWeekOfTerm() : 1;
        if (fromW == NEXT)
            fromW = timeTableCore.isThisTerm() ? ((timeTableCore.getThisWeekOfTerm() + 1 > timeTableCore.getCurrentCurriculum().getTotalWeeks()) ? timeTableCore.getCurrentCurriculum().getTotalWeeks() : timeTableCore.getThisWeekOfTerm() + 1) : 2;
        if (toW == BEFORE) toW = timeTableCore.getThisWeekOfTerm() - 1 <= 0 ? 1 : timeTableCore.getThisWeekOfTerm() - 1;
        if (toW == THIS) toW = timeTableCore.isThisTerm() ? timeTableCore.getThisWeekOfTerm() : 1;
        if (toW == NEXT)
            toW = timeTableCore.isThisTerm() ? ((timeTableCore.getThisWeekOfTerm() + 1 > timeTableCore.getCurrentCurriculum().getTotalWeeks()) ? timeTableCore.getCurrentCurriculum().getTotalWeeks() : timeTableCore.getThisWeekOfTerm() + 1) : 2;

        if (fromW == -1) {
            if (fromDOW == BEFORE) {
                if (thisDOW < 2) {
                    fromW = timeTableCore.getThisWeekOfTerm() - 1;
                    fromDOW = 7;
                } else {
                    fromW = timeTableCore.getThisWeekOfTerm();
                    fromDOW = thisDOW - 1;
                }
            } else if (fromDOW == T_BEFORE) {
                if (thisDOW < 3) {
                    fromW = timeTableCore.getThisWeekOfTerm() - 1;
                    if (thisDOW == 2) fromDOW = 7;
                    else if (thisDOW == 1) fromDOW = 6;
                } else {
                    fromW = timeTableCore.getThisWeekOfTerm();
                    fromDOW = thisDOW - 2;
                }
            } else if (fromDOW == TT_BEFORE) {
                if (thisDOW < 4) {
                    fromW = timeTableCore.getThisWeekOfTerm() - 1;
                    if (thisDOW == 3) fromDOW = 7;
                    else if (thisDOW == 2) fromDOW = 6;
                    else if (thisDOW == 1) fromDOW = 5;
                } else {
                    fromW = timeTableCore.getThisWeekOfTerm();
                    fromDOW = thisDOW - 3;
                }
            } else if (fromDOW == THIS) {
                fromW = timeTableCore.getThisWeekOfTerm();
                fromDOW = thisDOW;
            } else if (fromDOW == NEXT) {
                if (thisDOW == 7) {
                    fromW = timeTableCore.getThisWeekOfTerm() + 1;
                    fromDOW = 1;
                } else {
                    fromW = timeTableCore.getThisWeekOfTerm();
                    fromDOW = thisDOW + 1;
                }
            } else if (fromDOW == T_NEXT) {
                if (thisDOW == 6) {
                    fromW = timeTableCore.getThisWeekOfTerm() + 1;
                    fromDOW = 1;
                } else if (thisDOW == 7) {
                    fromW = timeTableCore.getThisWeekOfTerm() + 1;
                    fromDOW = 2;
                } else {
                    fromW = timeTableCore.getThisWeekOfTerm();
                    fromDOW = thisDOW + 2;
                }
            } else if (fromDOW == TT_NEXT) {
                if (thisDOW == 5) {
                    fromW = timeTableCore.getThisWeekOfTerm() + 1;
                    fromDOW = 1;
                } else if (thisDOW == 6) {
                    fromW = timeTableCore.getThisWeekOfTerm() + 1;
                    fromDOW = 2;
                } else if (thisDOW == 7) {
                    fromW = timeTableCore.getThisWeekOfTerm() + 1;
                    fromDOW = 3;
                } else {
                    fromW = timeTableCore.getThisWeekOfTerm();
                    fromDOW = thisDOW + 3;
                }
            }
        }
        if (toW == -1) {
            if (toDOW == BEFORE) {
                if (thisDOW < 2) {
                    toW = timeTableCore.getThisWeekOfTerm() - 1;
                    toDOW = 7;
                } else {
                    toW = timeTableCore.getThisWeekOfTerm();
                    toDOW = thisDOW - 1;
                }
            } else if (toDOW == T_BEFORE) {
                if (thisDOW < 3) {
                    toW = timeTableCore.getThisWeekOfTerm() - 1;
                    if (thisDOW == 2) toDOW = 7;
                    else if (thisDOW == 1) toDOW = 6;
                } else {
                    toW = timeTableCore.getThisWeekOfTerm();
                    toDOW = thisDOW - 2;
                }
            } else if (toDOW == TT_BEFORE) {
                if (thisDOW < 4) {
                    toW = timeTableCore.getThisWeekOfTerm() - 1;
                    if (thisDOW == 3) toDOW = 7;
                    else if (thisDOW == 2) toDOW = 6;
                    else if (thisDOW == 1) toDOW = 5;
                } else {
                    toW = timeTableCore.getThisWeekOfTerm();
                    toDOW = thisDOW - 3;
                }
            } else if (toDOW == THIS) {
                toW = timeTableCore.getThisWeekOfTerm();
                toDOW = thisDOW;
            } else if (toDOW == NEXT) {
                if (thisDOW == 7) {
                    toW = timeTableCore.getThisWeekOfTerm() + 1;
                    toDOW = 1;
                } else {
                    toW = timeTableCore.getThisWeekOfTerm();
                    toDOW = thisDOW + 1;
                }
            } else if (toDOW == T_NEXT) {
                if (thisDOW == 6) {
                    toW = timeTableCore.getThisWeekOfTerm() + 1;
                    toDOW = 1;
                } else if (thisDOW == 7) {
                    toW = timeTableCore.getThisWeekOfTerm() + 1;
                    toDOW = 2;
                } else {
                    toW = timeTableCore.getThisWeekOfTerm();
                    toDOW = thisDOW + 2;
                }
            } else if (toDOW == TT_NEXT) {
                if (thisDOW == 5) {
                    toW = timeTableCore.getThisWeekOfTerm() + 1;
                    toDOW = 1;
                } else if (thisDOW == 6) {
                    toW = timeTableCore.getThisWeekOfTerm() + 1;
                    toDOW = 2;
                } else if (thisDOW == 7) {
                    toW = timeTableCore.getThisWeekOfTerm() + 1;
                    toDOW = 3;
                } else {
                    toW = timeTableCore.getThisWeekOfTerm();
                    toDOW = thisDOW + 3;
                }
            }
        }


        if (toDOW == -1 || fromDOW == -1) {
            if (fromDOW != -1) toDOW = fromDOW;
            else if (fromW == -1 && toW == -1) {
                fromDOW = thisDOW;
                toDOW = fromDOW;
            } else {
                fromDOW = 1;
                toDOW = 7;
            }
        }
        if (fromW == -1 || toW == -1) {
            if (fromW == toW) toW = fromW = timeTableCore.isThisTerm() ? timeTableCore.getThisWeekOfTerm() : 1;
            else if (fromW == -1) fromW = timeTableCore.isThisTerm() ? timeTableCore.getThisWeekOfTerm() : toW;
            else if (toW == -1) toW = fromW;
        }
        if (fromT.hour == -1) {
            fromT.hour = 0;
            fromT.minute = 0;
        }
        if (toT.hour == -1) {
            toT.hour = 23;
            toT.minute = 59;
        }
        if (toW > timeTableCore.getCurrentCurriculum().getTotalWeeks())
            toW = timeTableCore.getCurrentCurriculum().getTotalWeeks();
        toW = (toW > timeTableCore.getCurrentCurriculum().getTotalWeeks()) ? timeTableCore.getCurrentCurriculum().getTotalWeeks() : toW;
        System.out.println("放入查询函数的是：fW=" + fromW + ",fDOW=" + fromDOW + ",fT=" + fromT.tellTime() + ",tW=" + toW + ",tDOW=" + toDOW + ",tT=" + toT.tellTime());
        List<EventItem> result = null;
        switch (tag) {
            case ChatBotA.FUN_SEARCH_EVENT_ALL:
                result = timeTableCore.getEventFrom(fromW, fromDOW, fromT, toW, toDOW, toT);
                break;
            case ChatBotA.FUN_SEARCH_EVENT_COURSE:
                result = timeTableCore.getEventFrom(fromW, fromDOW, fromT, toW, toDOW, toT, TIMETABLE_EVENT_TYPE_COURSE);
                break;
            case ChatBotA.FUN_SEARCH_EVENT_ARRANGE:
                result = timeTableCore.getEventFrom(fromW, fromDOW, fromT, toW, toDOW, toT, TIMETABLE_EVENT_TYPE_ARRANGEMENT);
                break;
            case ChatBotA.FUN_SEARCH_EVENT_EXAM:
                result = timeTableCore.getEventFrom(fromW, fromDOW, fromT, toW, toDOW, toT, TIMETABLE_EVENT_TYPE_EXAM);
                break;
            case ChatBotA.FUN_SEARCH_EVENT_REMIND:
                result = timeTableCore.getEventFrom(fromW, fromDOW, fromT, toW, toDOW, toT, TIMETABLE_EVENT_TYPE_REMIND);
                break;
            case ChatBotA.FUN_SEARCH_EVENT_DDL:
                result = timeTableCore.getEventFrom(fromW, fromDOW, fromT, toW, toDOW, toT, TIMETABLE_EVENT_TYPE_DEADLINE);
                break;
        }
        if (num != -1 && result != null && result.size() > 0) {
            if (num != TextTools.LAST && num >= result.size()) return result;
            if (num == TextTools.LAST) num = result.size();
            ArrayList<EventItem> temp = new ArrayList<>();
            temp.add(result.get(num - 1));
            return temp;
        } else {
            return result;
        }
    }


    public JsonObject Interact(String text) {
        JsonObject result = new JsonObject();
        String tempRes = mResult.toString();
        result.addProperty("message_show", tempRes);
        switch (Judge_Function(text,mResult)) {
            case FUN_SEARCH_EVENT_ALL:
                System.out.println("识别为查询所有事件");
                return Chat_SearchEvent.Process(mResult, FUN_SEARCH_EVENT_ALL);
            case FUN_SEARCH_EVENT_COURSE:
                System.out.println("识别为查询课程");
                return Chat_SearchEvent.Process(mResult, FUN_SEARCH_EVENT_COURSE);
            case FUN_SEARCH_EVENT_DDL:
                System.out.println("识别为查询DDL");
                return Chat_SearchEvent.Process(mResult, FUN_SEARCH_EVENT_DDL);
            case FUN_SEARCH_EVENT_REMIND:
                System.out.println("识别为查询提醒");
                return Chat_SearchEvent.Process(mResult, FUN_SEARCH_EVENT_REMIND);
            case FUN_SEARCH_EVENT_ARRANGE:
                System.out.println("识别为查询安排");
                return Chat_SearchEvent.Process(mResult, FUN_SEARCH_EVENT_ARRANGE);
            case FUN_SEARCH_EVENT_EXAM:
                System.out.println("识别为查询考试");
                return Chat_SearchEvent.Process(mResult, FUN_SEARCH_EVENT_EXAM);
            case FUN__STATE_SINGLE_SHOW_CLASSROOM:
                JsonObject classroom = new JsonObject();
                classroom.addProperty("function","search_event_context2_classroom");
                return classroom;
            case FUN_QUERY_SUBJECTS:
                System.out.println("识别为课程查询");
                return Chat_QuerySubject.Process(mResult,text);
            case FUN_ADD_EVENT_REMIND:
                System.out.println("识别为添加提醒");
                Chat_AddEvent_Remind CAER = new Chat_AddEvent_Remind();
                return CAER.Process(mResult);
            case FUN_SEARCH_TASK:
                System.out.println("识别为任务查询");
                JsonObject joT = new JsonObject();
                joT.addProperty("function", "search_task");
                return joT;
            case FUN_INTENT_EXPLORE:
                System.out.println("识别为探索模式");
                JsonObject jo2 = new JsonObject();
                jo2.addProperty("function","intent_explore");
                return jo2;
            case FUN_INTENT_CANTEEN:
                System.out.println("识别为食堂搜索");
                JsonObject jo3 = new JsonObject();
                jo3.addProperty("function", "intent_canteen");
                return jo3;
            case FUN_INTENT_JWTS:
                System.out.println("识别为教务系统");
                JsonObject jo4 = new JsonObject();
                jo4.addProperty("function", "intent_jwts");
                return jo4;
            case FUN_INTENT_INFOS:
                System.out.println("识别为信息");
                JsonObject jok = new JsonObject();
                jok.addProperty("function", "intent_infos");
                return jok;
            case FUN_SEARCH_PEOPLE:
                String name = TextTools.getStringWithTag(mResult,"nr",1);
                JsonObject jo5 = new JsonObject();
                jo5.addProperty("function", "search_people");
                jo5.addProperty("name", name.replaceAll("老师","").replaceAll("教师","")
                .replaceAll("的",""));
                return jo5;
            case FUN_INTENT_LAF:
                System.out.println("识别为失物招领");
                JsonObject jo6 = new JsonObject();
                jo6.addProperty("function", "intent_laf");
                return jo6;
        }
        return result;
    }


    public int Judge_Function(String text,List<Term> terms) {
        //越往上的功能优先级越高
        if (TextTools.mLike(text, R.array.words_add_remind)) return FUN_ADD_EVENT_REMIND;
        if (TextTools.mLike(text, R.array.sentence_fun_explore)) return FUN_INTENT_EXPLORE;
        if (TextTools.mLike(text, R.array.sentence_fun_canteen)) return FUN_INTENT_CANTEEN;
        if (TextTools.mLike(text, R.array.sentence_fun_jwts)) return FUN_INTENT_JWTS;
        if (TextTools.mLike(text, R.array.sentence_fun_infos)) return FUN_INTENT_INFOS;
        if (TextTools.mLike(text, R.array.sentence_fun_laf)) return FUN_INTENT_LAF;

        if(TextTools.getCount_Equals(terms,"sub",false)>=1) return FUN_QUERY_SUBJECTS;

        if (TextTools.mContains(text, R.array.words_search) ||
                TextTools.JudgeQuestionting(text)
        ) {
            if (TextTools.mContains(text, R.array.words_course)) return FUN_SEARCH_EVENT_COURSE;
            if (TextTools.mContains(text,R.array.words_arrangement)) return FUN_SEARCH_EVENT_ARRANGE;
            if (TextTools.mContains(text,R.array.words_exam)) return FUN_SEARCH_EVENT_EXAM;
            if (TextTools.mContains(text, R.array.words_ddl)) return FUN_SEARCH_EVENT_DDL;
            if (TextTools.mContains(text, R.array.words_remind)) return FUN_SEARCH_EVENT_REMIND;
            if (TextTools.mContains(text, R.array.words_event)) return FUN_SEARCH_EVENT_ALL;
            if (TextTools.mContains(text, R.array.words_task)) return FUN_SEARCH_TASK;
        }
        if(TextTools.mContains(text,R.array.words_course_special)) return FUN_SEARCH_EVENT_COURSE;
        if(TextTools.getCount_Equals(terms,"nr",false)>=1)  return FUN_SEARCH_PEOPLE;
        System.out.println("===========STATE:" + State);
        if (State == STATE_SEARCH_COURSE_SINGLE) {
            if ((TextTools.mLike(text, R.array.words_where) && TextTools.mLike(text, R.array.words_classroom))
                    || TextTools.mLike(text, R.array.words_target_findClassroom)
            ) {
                return FUN__STATE_SINGLE_SHOW_CLASSROOM;
            }
        }
        return -1;
    }


}
