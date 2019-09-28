package com.stupidtree.hita.hita;

import com.google.gson.JsonObject;
import com.stupidtree.hita.R;

import org.ansj.domain.Term;

import java.util.List;

public class Chat_QuerySubject {
    private static final int QS_NUMBER_OF_SUBJECT = 61;
    public static JsonObject Process(List<Term> x,String text){
        JsonObject jo = new JsonObject();
        switch(Judge(x,text)){
            case QS_NUMBER_OF_SUBJECT:
                System.out.println("-识别为科目数量查询");
                jo.addProperty("function","query_subject_number_of_subject");
                jo.addProperty("type",getType(TextTools.getStringWithTag(x,"sub",1)));
        }
        return jo;
    }

    private static String getType(String subject){
        if(TextTools.mContains(subject,R.array.word_subject_exam)) return "exam";
        if(TextTools.mContains(subject,R.array.word_subject_no_exam)) return "no_exam";
        if(TextTools.mContains(subject,R.array.word_subject_mooc)) return "mooc";
        if(TextTools.mContains(subject,R.array.word_subject_comp)) return "comp";
        if(TextTools.mContains(subject,R.array.word_subject_alt)) return "alt";
        if(TextTools.mContains(subject,R.array.word_subject_wtv)) return "wtv";
        return "all";
    }

    private static int Judge(List<Term> x,String text){
        if((TextTools.mContains(text, R.array.words_search) ||
               TextTools.JudgeQuestionting(text)
        )&&TextTools.getCount_Equals(x,"sub",false)>=1) return QS_NUMBER_OF_SUBJECT;
        return QS_NUMBER_OF_SUBJECT;
    }

}
