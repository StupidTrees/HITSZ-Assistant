package com.stupidtree.hita.hita;

import android.text.TextUtils;
import android.util.Log;

import com.stupidtree.hita.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.HContext;

public class TextTools {
    public static final int THIS = -2;
    public static final int BEFORE= -3;
    public static final int NEXT= -4;
    public static final int T_BEFORE= -5;
    public static final int T_NEXT= -6;
    public static final int TT_BEFORE= -7;
    public static final int TT_NEXT= -8;
    public static final int LAST = 13;

    public static String[] words_time_DOW = {"星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期天", "周一", "周二", "周三", "周四", "周五", "周六", "周天", "周日", "星期日", "星期1", "星期2", "星期3", "星期4", "星期5", "星期6", "周1", "周2", "周3", "周4", "周5", "周6"};

    public static List<Term> NaiveSegmentation(String sentence){
        List<Term> result = new ArrayList();
        String leftSentence = sentence;
        while (true){
            Term next = getFirstTerm(leftSentence);
            if(next==null){
                if(leftSentence.length()>0)result.add(new Term(leftSentence,"unknown"));
                break;
            }
            Log.e("next_term:",next.toString());
            if(next.getIndexInSentence()!=0) result.add(new Term(leftSentence.substring(0,next.getIndexInSentence()),"unknown"));
            result.add(next);
            leftSentence = leftSentence.substring(next.getIndexInSentence()+next.getContent().length());
        }
        return result;
    }

    private static List<Term> getAllTermsFound(String sentence){
        List<Term> res = new ArrayList<>();
        String content;
        if((content = ContainsStrFromArray(sentence,R.array.t_w))!=null){
            res.add(new Term(content,"t_w").setIndexInSentence(sentence.indexOf(content)).setPriority(109));
        } 
        if((content = ContainsStrFromArray(sentence,R.array.t_dow))!=null){
            res.add(new Term(content,"t_dow").setIndexInSentence(sentence.indexOf(content)).setPriority(108));
        }
        if((content = ContainsStrFromArray(sentence,R.array.words_time_days_with_period))!=null){
            res.add(new Term(content,"t_dow_p").setIndexInSentence(sentence.indexOf(content)).setPriority(107));
        }
        if((content = ContainsStrFromArray(sentence,R.array.t_num))!=null){
            res.add(new Term(content,"t_num").setIndexInSentence(sentence.indexOf(content)).setPriority(106));
        }
        if((content = ContainsStrFromArray(sentence,R.array.t_h))!=null){
            res.add(new Term(content,"t_h").setIndexInSentence(sentence.indexOf(content)).setPriority(105));
        }if((content = ContainsStrFromArray(sentence,R.array.t_m))!=null){
            res.add(new Term(content,"t_m").setIndexInSentence(sentence.indexOf(content)).setPriority(104));
        }if((content = ContainsStrFromArray(sentence,R.array.t_pr))!=null){
            res.add(new Term(content,"t_pr").setIndexInSentence(sentence.indexOf(content)).setPriority(103));
        }if((content = ContainsStrFromArray(sentence,R.array.t_pr))!=null){
            res.add(new Term(content,"t_pr").setIndexInSentence(sentence.indexOf(content)).setPriority(102));
        } if((content = ContainsStrFromArray(sentence,R.array.words_to))!=null){
            res.add(new Term(content,"to").setIndexInSentence(sentence.indexOf(content)).setPriority(101));
        }if((content = ContainsStrFromArray(sentence,R.array.sub))!=null){
            res.add(new Term(content,"sub").setIndexInSentence(sentence.indexOf(content)).setPriority(100));
        } if((content = ContainsStrFromArray(sentence,R.array.t_next_one))!=null){
            res.add(new Term(content,"t_next_one").setIndexInSentence(sentence.indexOf(content)).setPriority(99));
        }if((content = ContainsStrFromArray(sentence,R.array.words_this))!=null){
            res.add(new Term(content,"this").setIndexInSentence(sentence.indexOf(content)).setPriority(98));
        }if((content = ContainsStrFromArray(sentence,R.array.words_next))!=null){
            res.add(new Term(content,"next").setIndexInSentence(sentence.indexOf(content)).setPriority(97));
        }if((content = ContainsStrFromArray(sentence,R.array.words_last))!=null){
            res.add(new Term(content,"last").setIndexInSentence(sentence.indexOf(content)).setPriority(96));
        }if((content = ContainsStrFromArray(sentence,R.array.words_hita))!=null){
            res.add(new Term(content,"hita").setIndexInSentence(sentence.indexOf(content)).setPriority(95));
        }if((content = ContainsStrFromArray(sentence,R.array.words_number))!=null){
            res.add(new Term(content,"number").setIndexInSentence(sentence.indexOf(content)).setPriority(94));
        }
        return res;
    }
    private static Term getFirstTerm(String sentence){
        List<Term> all = getAllTermsFound(sentence);
        if(all.size()==0) return null;
        List<Term> termsWithMinIndex = new ArrayList<>();
        Term minIndex = all.get(0);
        for(Term t:all){
            if(t.getIndexInSentence()<minIndex.getIndexInSentence()){
                termsWithMinIndex.clear();
                minIndex = t;
                termsWithMinIndex.add(minIndex);
            }else if(t.getIndexInSentence()==minIndex.getIndexInSentence()){
                minIndex = t;
                termsWithMinIndex.add(minIndex);
            }
        }
        if(termsWithMinIndex.size()==1) return termsWithMinIndex.get(0);
        else{
            Term maxPriority = all.get(0);
            for(Term t:all){
                if(t.getPriority()>maxPriority.getPriority()) maxPriority = t;
            }
            return maxPriority;
        }
    }



    public static List<Term> ReTag(List<Term> x) {
        for (Term t : x) {
            //if (mEquals(t.getContent(),R.array.words_hita)) t.setTag("hita");
            if (mEquals(t.getContent(),R.array.words_ee)) t.setTag("ee");
            //if (mEquals(t.getContent(), words_time_DOW)) t.setTag("t_dow");
            //if (mEquals(t.getContent(),R.array. words_time_week_withoutHead)) t.setTag("t_w");
//            if (isNumber(t.getContent()) || mEquals(t.getContent(), R.array.words_number))
//                t.setTag("number");
            //if (mEquals(t.getContent(), R.array.words_time_period)) t.setTag("t_pr");
            //if (mEquals(t.getContent(), R.array.words_time_hour)) t.setTag("t_h");
            //if (mEquals(t.getContent(), R.array.words_to)) t.setTag("to");
            //if (mEquals(t.getContent(), R.array.words_time_minute)) t.setTag("t_m");
            //if (mEquals(t.getContent(), R.array.words_this)) t.setTag("this");
           // if (mEquals(t.getContent(), R.array.words_next)) t.setTag("next");
            //if (mEquals(t.getContent(), R.array.words_last)) t.setTag("last");
            //if(mEquals(t.getContent(),R.array.words_time_days)) t.setTag("t_dow");
            //if(mEquals(t.getContent(),R.array.words_time_days_with_period)) t.setTag("t_dow_p");
            //if(mEquals(t.getContent(),R.array.words_next_one)) t.setTag("t_nextone");
            if(mEquals(t.getContent(), R.array.words_add_remind)) t.setTag("add_remind");
//            if(mEquals(t.getContent(),R.array.word_subject_wtv)
//            ||mEquals(t.getContent(),R.array.word_subject_alt)
//                    ||mEquals(t.getContent(),R.array.word_subject_comp)
//            ) t.setTag("sub");
        }
        return x;
    }

    public static boolean mContains(String x, String[] bases) {
        if (bases == null) return false;
        for (String i : bases) {
            if (x.contains(i)) return true;
        }
        return false;
    }
    public static boolean containsNumber(String s){
        return s.contains("1")||s.contains("2")||s.contains("3")||s.contains("4")||s.contains("5")||s.contains("6")||
                s.contains("7")||s.contains("8")||s.contains("9")||s.contains("0");
    }

    public static boolean mContains(String x, int id) {
        String[] bases = HContext.getResources().getStringArray(id);
        if (bases == null) return false;
        for (String i : bases) {
            if (x.contains(i)) return true;
        }
        return false;
    }
    public static String ContainsStrFromArray(String x, int id) {
        String[] bases = HContext.getResources().getStringArray(id);
        if (bases == null) return null;
        for (String i : bases) {
            if(x.contains(i))return i;
        }
        return null;
    }
    public static boolean mEquals(String x, String[] bases) {
        for (String i : bases) {
            if (x.equals(i)) return true;
        }
        return false;
    }
    public static boolean mEquals(String x, int id) {
        String[] bases = HContext.getResources().getStringArray(id);
        for (String i : bases) {
            if (x.equals(i)) return true;
        }
        return false;
    }
    public boolean mLike(String x, String[] bases) {
        for (String i : bases) {
            if (like(x,i)) return true;
        }
        return false;
    }
    public static boolean mLike(String x, int id) {
        String[] str = HContext.getResources().getStringArray(id);
        for (String i : str) {
            if (like(x,i)) return true;
        }
        return false;
    }
    public int getCount(String[] x, String key) {
        int result = 0;
        for (String i : x) {
            if (i.equals(key)) result++;
        }
        return result;
    }

    public int getCount(String[] x, String[] key) {
        int result = 0;
        for (String i : x) {
            if (getCount(key, i) >= 1) result++;
        }
        return result;
    }

    public static String getStringWithTag(List<Term> x, String type, int number) {
        List<String> resultList = new ArrayList<>();
        for (Term m : x) {
            if (m.getTag().equals(type)) {
                resultList.add(m.getContent());
            }
        }
        if (resultList.size() <= 0) return "";
        if (number > resultList.size()) return resultList.get(resultList.size() - 1);
        else return resultList.get(number - 1);
    }

    public String getStringWithTagFrom(List<Term> x, String type, String[] from, boolean isFromName, int number1, int number2, int number3) {
        //number1:从第几个FROM标记开始
        //number2:到第几个FROM标记结束，-1表示一直到结尾
        //number3:这段区间内的第几个目标字符串
        if (from == null || getCount(x, from, isFromName) <= 0) {

            List<String> resultList = new ArrayList<>();
            for (Term m : x) {
                if (m.getTag().equals(type)) {
                    resultList.add(m.getContent());
                }
            }
            if (resultList.size() <= 0) return "";
            if (number3 > resultList.size()) return resultList.get(resultList.size() - 1);
            else return resultList.get(number3 - 1);
        } else {
            int gotit = 0;
            List<String> resultList = new ArrayList<>();
            for (Term m : x) {
                if (isFromName) {
                    if (mContains(m.getContent(), from)) gotit++;
                } else {
                    if (mContains(m.getTag(), from)) gotit++;
                }
                if (gotit < number1) {

                } else if (number2 == -1) {
                    if (m.getTag().equals(type)) {
                        resultList.add(m.getContent());
                    }
                } else if (gotit < number2) {
                    if (m.getTag().equals(type)) {
                        resultList.add(m.getContent());
                    }
                }
            }
            if (resultList.size() <= 0) return "null";
            if (number3 > resultList.size()) return resultList.get(resultList.size() - 1);
            else return resultList.get(number3 - 1);
        }

    }
    public static String getStringAfterTag(List<Term> x, String[] from) {
        int last = 0;
        for(int i=0;i<x.size();i++){
            if(mContains(x.get(i).getTag(),from)) last = i;
        }
        StringBuilder sb = new StringBuilder();
        for(int i = last+1;i<x.size();i++){
            sb.append(x.get(i).getContent());
        }
        return sb.toString();
    }

    public static boolean JudgeQuestionting(String text) {
        // if (text.contains("有") && text.indexOf("有") < text.indexOf("呀")) return true;
        if (text.contains("有") && text.indexOf("有") < text.indexOf("吗")) return true;
        if (text.contains("有") && text.indexOf("有") < text.indexOf("不")) return true;
        if (text.contains("有") && text.indexOf("有") < text.indexOf("否")) return true;
        if (text.contains("存在") && text.indexOf("有") < text.indexOf("吗")) return true;
        if (text.contains("存在") && text.indexOf("有") < text.indexOf("不")) return true;
        return text.contains("存在") && text.indexOf("有") < text.indexOf("否");
    }
    public static int getCount_contains(List<Term> x, String key, boolean isName) {
        int result = 0;
        if (!isName) for (Term t : x)
            if (t.getTag().contains(key)) result++;
            else for (Term y : x) if (y.getContent().contains(key)) result++;
        return result;
    }
    public static int getCount_Equals(List<Term> x, String key, boolean isName) {
        int result = 0;
        if (!isName) for (Term t : x)
            if (t.getTag().equals(key)) result++;
            else for (Term y : x) if (y.getContent().equals(key)) result++;
        return result;
    }
    public int getCount(List<Term> x, String[] base, boolean isName) {
        int result = 0;
        if (!isName) {
            for (Term t : x) {
                for (int i = 0; i < base.length; i++) {
                    if (t.getTag().equals(base[i])) {
                        result++;
                        break;
                    }
                }
            }
        } else {
            for (Term t : x) {
                for (int i = 0; i < base.length; i++) {
                    if (t.getContent().equals(base[i])) {
                        result++;
                        break;
                    }
                }
            }
        }

        return result;
    }
    public static int getCount(List<Term> x, int arrId, boolean isName) {
        String[] base = HContext.getResources().getStringArray(arrId);
        int result = 0;
        if (!isName) {
            for (Term t : x) {
                for (int i = 0; i < base.length; i++) {
                    if (t.getTag().equals(base[i])) {
                        result++;
                        break;
                    }
                }
            }
        } else {
            for (Term t : x) {
                for (int i = 0; i < base.length; i++) {
                    if (t.getContent().equals(base[i])) {
                        result++;
                        break;
                    }
                }
            }
        }

        return result;
    }

    public int getCountBetweenTag(List<Term> x, String[] base, String[] tag, boolean isName, int number1, int number2) {
        /*在第number1个到number2个tag之间出现的次数*/
        /*number1=0表示从头开始，number2=0表示一直到尾巴*/
        int result = 0;
        int tagNum = 0;
        for (int i = 0; i < x.size(); i++) {

            if (mEquals(x.get(i).getTag(), tag)) tagNum++;
            if (tagNum >= number2 && number2 != 0) return result;
            if (tagNum < number1 && number1 != 0) continue;
            if (isName) {
                if (mEquals(x.get(i).getContent(), base)) result++;
            } else {
                if (mEquals(x.get(i).getTag(), base)) result++;
            }
        }
        if (tagNum == 0) {
            for (int i = 0; i < x.size(); i++) {

                if (isName) {
                    if (mEquals(x.get(i).getContent(), base)) result++;
                } else {
                    if (mEquals(x.get(i).getTag(), base)) result++;
                }
            }
        }

        return result;
    }

    public static String getStringBetweenTag(List<Term> x, String[] base, String[] tag, boolean isName, int number1, int number2, int number3) {
        /*在第number1个到number2个tag之间出现的第number3个type类型的*/
        /*number1=0表示从头开始，number2=0表示一直到尾巴*/
        /*isname表示这个base是和name匹配还是和标签匹配*/
        List<String> resultList = new ArrayList<>();
        int tagNum = 0;
        for (int i = 0; i < x.size(); i++) {

            if (mEquals(x.get(i).getTag(), tag)) tagNum++;
            if (tagNum >= number2 && number2 != 0) break;
            if (tagNum < number1 && number1 != 0) continue;
            if (isName) {
                if (mEquals(x.get(i).getContent(), base)) resultList.add(x.get(i).getContent());
            } else {
                if (mEquals(x.get(i).getTag(), base)) resultList.add(x.get(i).getContent());
            }
        }
        if (tagNum == 0) {
            for (int i = 0; i < x.size(); i++) {

                if (isName) {
                    if (mEquals(x.get(i).getContent(), base)) resultList.add(x.get(i).getContent());
                } else {
                    if (mEquals(x.get(i).getTag(), base)) resultList.add(x.get(i).getContent());
                }
            }
        }
        if (resultList.size() == 0) return null;
        if (number3 > resultList.size()) return resultList.get(resultList.size() - 1);
        else return resultList.get(number3 - 1);
    }
    public Term getTermBetweenTag(List<Term> x, String[] base, String[] tag, boolean isName, int number1, int number2, int number3) {
        /*在第number1个到number2个tag之间出现的第number3个type类型的*/
        /*number1=0表示从头开始，number2=0表示一直到尾巴*/
        /*isname表示这个base是和name匹配还是和标签匹配*/
        List<Term> resultList = new ArrayList<>();
        int tagNum = 0;
        for (int i = 0; i < x.size(); i++) {

            if (mEquals(x.get(i).getTag(), tag)) tagNum++;
            if (tagNum >= number2 && number2 != 0) break;
            if (tagNum < number1 && number1 != 0) continue;
            if (isName) {
                if (mEquals(x.get(i).getContent(), base)) resultList.add(x.get(i));
            } else {
                if (mEquals(x.get(i).getTag(), base)) resultList.add(x.get(i));
            }
        }
        if (tagNum == 0) {
            for (int i = 0; i < x.size(); i++) {

                if (isName) {
                    if (mEquals(x.get(i).getContent(), base)) resultList.add(x.get(i));
                } else {
                    if (mEquals(x.get(i).getTag(), base)) resultList.add(x.get(i));
                }
            }
        }
        if (resultList.size() == 0) return null;
        if (number3 > resultList.size()) return resultList.get(resultList.size() - 1);
        else return resultList.get(number3 - 1);
    }

    public static boolean isNumber(String x) {
        if (x == null || x.length() <= 0) return false;
        for (int i = 0; i < x.length(); i++) {
            if (!(x.charAt(i) >= '0' && x.charAt(i) <= '9')) {
                return false;
            }
        }
        return true;
    }

    public static boolean likeWithContain(String a,String b){
        if(TextUtils.isEmpty(a)&&!TextUtils.isEmpty(b)
        ||TextUtils.isEmpty(b)&&!TextUtils.isEmpty(a)
        ) return false;
        boolean b1 = a.contains(b) || b.contains(a);
        if(b1) return true;
        int delta = 0;
        if(a.length()<=5&&b.length()<=5) delta = 1;
        else if(a.length()<=8&&b.length()<=8) delta = 2;
        else if (a.length()<=10&&b.length()<=10)delta = 3;
        else if(a.length()>=11&&b.length()>=11) delta = 4;
        return EditDistance(a,b)<=delta;
    }

    public static boolean like(String a,String b){
        boolean b1 = a.contains(b) || b.contains(a);
        if(a.length()>3&&b.length()>3){
            if(b1) return true;
        }else if(Math.abs(a.length()-b.length())<=2){
            if(b1) return true;
        }
        int delta = 0;
        if(a.length()<=5&&b.length()<=5) delta = 1;
        else if(a.length()<=8&&b.length()<=8) delta = 2;
        else if (a.length()<=10&&b.length()<=10)delta = 3;
        else if(a.length()>=11&&b.length()>=11) delta = 4;
        return EditDistance(a,b)<=delta;
    }
    private static int EditDistance(String source, String target) {
        char[] sources = source.toCharArray();
        char[] targets = target.toCharArray();
        int sourceLen = sources.length;
        int targetLen = targets.length;
        int[][] d = new int[sourceLen + 1][targetLen + 1];
        for (int i = 0; i <= sourceLen; i++) {
            d[i][0] = i;
        }
        for (int i = 0; i <= targetLen; i++) {
            d[0][i] = i;
        }

        for (int i = 1; i <= sourceLen; i++) {
            for (int j = 1; j <= targetLen; j++) {
                if (sources[i - 1] == targets[j - 1]) {
                    d[i][j] = d[i - 1][j - 1];
                } else {
                    //插入
                    int insert = d[i][j - 1] + 1;
                    //删除
                    int delete = d[i - 1][j] + 1;
                    //替换
                    int replace = d[i - 1][j - 1] + 1;
                    d[i][j] = Math.min(insert, delete) > Math.min(delete, replace) ? Math.min(delete, replace) :
                            Math.min(insert, delete);
                }
            }
        }
        return d[sourceLen][targetLen];
    }

    public static boolean equals(String a,String b,String ignore){
        return a.replaceAll(ignore,"").equals(b.replaceAll(ignore,""));
    }


}
