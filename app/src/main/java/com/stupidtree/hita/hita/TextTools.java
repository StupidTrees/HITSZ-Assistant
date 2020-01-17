package com.stupidtree.hita.hita;

import com.stupidtree.hita.R;

import org.ansj.domain.Nature;
import org.ansj.domain.Term;

import java.util.ArrayList;
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

    public static List<Term> ReTag(List<Term> x) {
        for (Term t : x) {
            if (mEquals(t.getName(),R.array.words_hita)) t.setNature(new Nature("hita"));
            if (mEquals(t.getName(),R.array.words_ee)) t.setNature(new Nature("ee"));

            if (mEquals(t.getName(), words_time_DOW)) t.setNature(new Nature("t_dow"));
            if (mEquals(t.getName(),R.array. words_time_week_withoutHead)) t.setNature(new Nature("t_w"));
            if (isNumber(t.getName()) || mEquals(t.getName(), R.array.words_number))
                t.setNature(new Nature("number"));
            if (mEquals(t.getName(), R.array.words_time_period)) t.setNature(new Nature("t_pr"));
            if (mEquals(t.getName(), R.array.words_time_hour)) t.setNature(new Nature("t_h"));
            if (mEquals(t.getName(), R.array.words_to)) t.setNature(new Nature("to"));
            if (mEquals(t.getName(), R.array.words_time_minute)) t.setNature(new Nature("t_m"));
            if (mEquals(t.getName(), R.array.words_this)) t.setNature(new Nature("this"));
            if (mEquals(t.getName(), R.array.words_next)) t.setNature(new Nature("next"));
            if (mEquals(t.getName(), R.array.words_last)) t.setNature(new Nature("last"));
            if(mEquals(t.getName(),R.array.words_time_days)) t.setNature(new Nature("t_dow"));
            if(mEquals(t.getName(),R.array.words_time_days_with_period)) t.setNature(new Nature("t_dow_p"));
            if(mEquals(t.getName(),R.array.words_next_one)) t.setNature(new Nature("t_nextone"));
            if(mEquals(t.getName(), R.array.words_add_remind)) t.setNature(new Nature("add_remind"));
            if(mEquals(t.getName(),R.array.word_subject_wtv)
            ||mEquals(t.getName(),R.array.word_subject_alt)
                    ||mEquals(t.getName(),R.array.word_subject_comp)
            ) t.setNature(new Nature("sub"));
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
            if (m.getNatureStr().equals(type)) {
                resultList.add(m.getName());
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
                if (m.getNatureStr().equals(type)) {
                    resultList.add(m.getName());
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
                    if (mContains(m.getName(), from)) gotit++;
                } else {
                    if (mContains(m.getNatureStr(), from)) gotit++;
                }
                if (gotit < number1) {

                } else if (number2 == -1) {
                    if (m.getNatureStr().equals(type)) {
                        resultList.add(m.getName());
                    }
                } else if (gotit < number2) {
                    if (m.getNatureStr().equals(type)) {
                        resultList.add(m.getName());
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
            if(mContains(x.get(i).getNatureStr(),from)) last = i;
        }
        StringBuilder sb = new StringBuilder();
        for(int i = last+1;i<x.size();i++){
            sb.append(x.get(i).getName());
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
            if (t.getNatureStr().contains(key)) result++;
            else for (Term y : x) if (y.getName().contains(key)) result++;
        return result;
    }
    public static int getCount_Equals(List<Term> x, String key, boolean isName) {
        int result = 0;
        if (!isName) for (Term t : x)
            if (t.getNatureStr().equals(key)) result++;
            else for (Term y : x) if (y.getName().equals(key)) result++;
        return result;
    }
    public int getCount(List<Term> x, String[] base, boolean isName) {
        int result = 0;
        if (!isName) {
            for (Term t : x) {
                for (int i = 0; i < base.length; i++) {
                    if (t.getNatureStr().equals(base[i])) {
                        result++;
                        break;
                    }
                }
            }
        } else {
            for (Term t : x) {
                for (int i = 0; i < base.length; i++) {
                    if (t.getName().equals(base[i])) {
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
                    if (t.getNatureStr().equals(base[i])) {
                        result++;
                        break;
                    }
                }
            }
        } else {
            for (Term t : x) {
                for (int i = 0; i < base.length; i++) {
                    if (t.getName().equals(base[i])) {
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

            if (mEquals(x.get(i).getNatureStr(), tag)) tagNum++;
            if (tagNum >= number2 && number2 != 0) return result;
            if (tagNum < number1 && number1 != 0) continue;
            if (isName) {
                if (mEquals(x.get(i).getName(), base)) result++;
            } else {
                if (mEquals(x.get(i).getNatureStr(), base)) result++;
            }
        }
        if (tagNum == 0) {
            for (int i = 0; i < x.size(); i++) {

                if (isName) {
                    if (mEquals(x.get(i).getName(), base)) result++;
                } else {
                    if (mEquals(x.get(i).getNatureStr(), base)) result++;
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

            if (mEquals(x.get(i).getNatureStr(), tag)) tagNum++;
            if (tagNum >= number2 && number2 != 0) break;
            if (tagNum < number1 && number1 != 0) continue;
            if (isName) {
                if (mEquals(x.get(i).getName(), base)) resultList.add(x.get(i).getName());
            } else {
                if (mEquals(x.get(i).getNatureStr(), base)) resultList.add(x.get(i).getName());
            }
        }
        if (tagNum == 0) {
            for (int i = 0; i < x.size(); i++) {

                if (isName) {
                    if (mEquals(x.get(i).getName(), base)) resultList.add(x.get(i).getName());
                } else {
                    if (mEquals(x.get(i).getNatureStr(), base)) resultList.add(x.get(i).getName());
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

            if (mEquals(x.get(i).getNatureStr(), tag)) tagNum++;
            if (tagNum >= number2 && number2 != 0) break;
            if (tagNum < number1 && number1 != 0) continue;
            if (isName) {
                if (mEquals(x.get(i).getName(), base)) resultList.add(x.get(i));
            } else {
                if (mEquals(x.get(i).getNatureStr(), base)) resultList.add(x.get(i));
            }
        }
        if (tagNum == 0) {
            for (int i = 0; i < x.size(); i++) {

                if (isName) {
                    if (mEquals(x.get(i).getName(), base)) resultList.add(x.get(i));
                } else {
                    if (mEquals(x.get(i).getNatureStr(), base)) resultList.add(x.get(i));
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
