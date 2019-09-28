package com.stupidtree.hita.hita;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;

import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.library.AmbiguityLibrary;
import org.ansj.library.DicLibrary;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.util.MyStaticValue;
import org.nlpcn.commons.lang.tire.domain.Forest;
import org.nlpcn.commons.lang.tire.library.Library;

import java.io.File;
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
    private static final int FUN_SEARCH_PEOPLE = 768;
    public final static int FUN__STATE_SINGLE_SHOW_CLASSROOM = 35;
    public static final int FUN_INTENT_EXPLORE = 465;
    public static final int FUN_INTENT_CANTEEN = 843;
    public static final int FUN_INTENT_JWTS = 543;
    private static final int FUN_INTENT_INFOS = 855;
    private static final int FUN_INTENT_LAF = 845;
    private static final int FUN_QUERY_SUBJECTS = 752;
    Forest forest_default;
    Forest forest_ambiguity;
    Activity activityContext;

    public ChatBotA(Activity context) {
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
        mResult = TextTools.ReTag(result.getTerms());
        Log.e("切词+reTag", String.valueOf(mResult));
        return Judge_Function(text,mResult) != -1;
    }


    public JsonObject Interact(String text) {
        JsonObject result = new JsonObject();
        String tempRes = mResult.toString();
        result.addProperty("message_show", tempRes);
        Chat_SearchEvent CSE = new Chat_SearchEvent();

        switch (Judge_Function(text,mResult)) {
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


    private void addDict(Context context) throws Exception {
        InputStream is1 = activityContext.getAssets().open("mDict_default.dic");
        InputStream is2 = activityContext.getAssets().open("mDict_ambiguity.dic");
//        InputStream is1 = new FileInputStream(context.getFilesDir() + "/mDict_default.dic");
//        InputStream is2 = new FileInputStream(context.getFilesDir() + "/mDict_ambiguity.dic");
       // File f1 =new File(context.getFilesDir() + "/mDict_default.dic");
        //File f2 = new File(context.getFilesDir() + "/mDict_ambiguity.dic");

        //HITAApplication.copyAssetsSingleFile(f1.getParentFile(),"mDict_default.dic");
        //HITAApplication.copyAssetsSingleFile(f2.getParentFile(),"mDict_ambiguity.dic");
        forest_default = Library.makeForest(is1);//加载字典文件
        forest_ambiguity = Library.makeForest(is2);
       // MyStaticValue.putLibrary(DicLibrary.DEFAULT, String.valueOf(f1),forest_ambiguity);

       // MyStaticValue.putLibrary(AmbiguityLibrary.DEFAULT, String.valueOf(f2),forest_ambiguity);
    }
}
