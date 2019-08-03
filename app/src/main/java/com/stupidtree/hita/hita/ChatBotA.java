package com.stupidtree.hita.hita;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.google.gson.JsonObject;

import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.nlpcn.commons.lang.tire.domain.Forest;
import org.nlpcn.commons.lang.tire.library.Library;

import java.io.InputStream;
import java.util.List;

import static com.stupidtree.hita.activities.ActivityChatbot.STATE_SEARCH_COURSE_SINGLE;
import static com.stupidtree.hita.activities.ActivityChatbot.State;


public class ChatBotA {

    boolean dictLoaded = false;
    List<Term> mResult;

    public static final int FUN_ADD_EVENT_REMIND = 651;
    public final static int FUN_SEARCH_TASK = 456;
    public final static int FUN_SEARCH_EVENT_COURSE = 1;
    public final static int FUN_SEARCH_EVENT_ARRANGE = 2;
    public final static int FUN_SEARCH_EVENT_DDL = 3;
    public final static int FUN_SEARCH_EVENT_EXAM = 4;
    public final static int FUN_SEARCH_EVENT_REMIND = 5;
    public final static int FUN_SEARCH_EVENT_ALL = 0;

    public final static int FUN__STATE_SINGLE_SHOW_CLASSROOM = 35;
    public static final int FUN_INTENT_EXPLORE = 465;
    public static final int FUN_INTENT_CANTEEN = 843;
    public static final int FUN_INTENT_JWTS = 543;
    Forest forest_default;
    Forest forest_ambiguity;
    TextTools textTools;
    Activity activityContext;

    public ChatBotA(Activity context) {
        textTools = new TextTools();
        activityContext = context;
    }


    public boolean simpleJudge(String text, Context context) {
        if (!dictLoaded) {
            try {
                addDict(context);
            } catch (Exception e) {
                Toast.makeText(context, "加载聊天机器人词典失败，请确认已授予读写权限！", Toast.LENGTH_SHORT).show();
            }
            dictLoaded = true;
        }
        Result result = ToAnalysis.parse(text, forest_default, forest_ambiguity); //分词结果的一个封装，主要是一个List<Term>的terms
        mResult = textTools.ReTag(result.getTerms());
        return Judge_Function(text) != -1;
    }


    public JsonObject Interact(String text) {
        JsonObject result = new JsonObject();
        String tempRes = mResult.toString();
        result.addProperty("message_show", tempRes);
        Chat_SearchEvent CSE = new Chat_SearchEvent(textTools);

        switch (Judge_Function(text)) {
            case FUN_SEARCH_EVENT_ALL:
                System.out.println("识别为查询所有事件");
                return CSE.Process(mResult, FUN_SEARCH_EVENT_ALL);
            case FUN_SEARCH_EVENT_COURSE:
                System.out.println("识别为查询课程");
                return CSE.Process(mResult, FUN_SEARCH_EVENT_COURSE);
            case FUN_SEARCH_EVENT_DDL:
                System.out.println("识别为查询DDL");
                return CSE.Process(mResult, FUN_SEARCH_EVENT_DDL);
            case FUN_SEARCH_EVENT_REMIND:
                System.out.println("识别为查询提醒");
                return CSE.Process(mResult, FUN_SEARCH_EVENT_REMIND);
            case FUN_SEARCH_EVENT_ARRANGE:
                System.out.println("识别为查询安排");
                return CSE.Process(mResult, FUN_SEARCH_EVENT_ARRANGE);
            case FUN_SEARCH_EVENT_EXAM:
                System.out.println("识别为查询考试");
                return CSE.Process(mResult, FUN_SEARCH_EVENT_EXAM);
            case FUN__STATE_SINGLE_SHOW_CLASSROOM:
                JsonObject classroom = new JsonObject();
                classroom.addProperty("function","search_event_context2_classroom");
                return classroom;
            case FUN_ADD_EVENT_REMIND:
                System.out.println("识别为添加提醒");
                Chat_AddEvent_Remind CAER = new Chat_AddEvent_Remind(textTools);
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
        }
        return result;
    }


    public int Judge_Function(String text) {

        if (textTools.mContains(text, textTools.words_add_remind)) return FUN_ADD_EVENT_REMIND;
        if (textTools.mContains(text, textTools.sentence_fun_explore)) return FUN_INTENT_EXPLORE;
        if (textTools.mContains(text, textTools.sentence_fun_canteen)) return FUN_INTENT_CANTEEN;
        if (textTools.mContains(text, textTools.sentence_fun_jwts)) return FUN_INTENT_JWTS;
        if (textTools.mContains(text, textTools.words_search) ||
                judgeQuestionting(text)
        ) {
            if (textTools.mContains(text, textTools.words_course)) return FUN_SEARCH_EVENT_COURSE;
            if (textTools.mContains(text, textTools.words_arrange)) return FUN_SEARCH_EVENT_ARRANGE;
            if (textTools.mContains(text, textTools.words_exam)) return FUN_SEARCH_EVENT_EXAM;
            if (textTools.mContains(text, textTools.words_ddl)) return FUN_SEARCH_EVENT_DDL;
            if (textTools.mContains(text, textTools.words_remind)) return FUN_SEARCH_EVENT_REMIND;
            if (textTools.mContains(text, textTools.words_events)) return FUN_SEARCH_EVENT_ALL;
            if (textTools.mContains(text, textTools.words_task)) return FUN_SEARCH_TASK;
        }
        if(textTools.mContains(text,textTools.words_course_special)) return FUN_SEARCH_EVENT_COURSE;

        System.out.println("===========STATE:" + State);
        if (State == STATE_SEARCH_COURSE_SINGLE) {
            if ((textTools.mContains(text, textTools.words_where) && textTools.mContains(text, textTools.words_classroom))
                    || textTools.mContains(text, textTools.words_target_findClassroom)
            ) {
                return FUN__STATE_SINGLE_SHOW_CLASSROOM;
            }
        }
        return -1;
    }

    boolean judgeQuestionting(String text) {
        if (text.contains("有") && text.indexOf("有") < text.indexOf("吗")) return true;
        if (text.contains("有") && text.indexOf("有") < text.indexOf("不")) return true;
        if (text.contains("有") && text.indexOf("有") < text.indexOf("否")) return true;
        if (text.contains("存在") && text.indexOf("有") < text.indexOf("吗")) return true;
        if (text.contains("存在") && text.indexOf("有") < text.indexOf("不")) return true;
        if (text.contains("存在") && text.indexOf("有") < text.indexOf("否")) return true;
        return false;
    }

    private void addDict(Context context) throws Exception {
        InputStream is1 = activityContext.getAssets().open("mDict_default.dic");
        InputStream is2 = activityContext.getAssets().open("mDict_ambiguity.dic");
//        InputStream is1 = new FileInputStream(context.getFilesDir() + "/mDict_default.dic");
//        InputStream is2 = new FileInputStream(context.getFilesDir() + "/mDict_ambiguity.dic");
        forest_default = Library.makeForest(is1);//加载字典文件
        forest_ambiguity = Library.makeForest(is2);
    }
}
