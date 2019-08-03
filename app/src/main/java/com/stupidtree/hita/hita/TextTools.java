package com.stupidtree.hita.hita;

import org.ansj.domain.Nature;
import org.ansj.domain.Term;

import java.util.ArrayList;
import java.util.List;

public class TextTools {
    public static final int THIS = -2;
    public static final int BEFORE= -3;
    public static final int NEXT= -4;
    public static final int T_BEFORE= -5;
    public static final int T_NEXT= -6;
    public static final int TT_BEFORE= -7;
    public static final int TT_NEXT= -8;
    public static final int LAST = 13;

    public String[] words_search = {"查找", "搜索", "有哪些", "有什么", "查询","什么","有几","有多少","多少","有啥","有没有","还有多少"};
     public String[] words_events={"事","活动","行为"};
    public String[] words_course = {"课"};
    public String[] words_task = {"任务","task"};
    public String[] words_arrange ={"安排","规划"};
    public String[] words_ddl = {"deadline","ddl","DDL","DEADLINE","最后期限","期限"};
    public String[] words_exam = {"考试"};
    public String[] words_remind = {"提醒","嘱托","提示"};
    public String[] words_add_remind = {"提醒","记得","记住让","提醒我","记得让","记得让我","记住让我"};
    public String[] words_add_ddl = {"截止","停止","最后期限"};
    public String[] words_add_arrangement = {"我想要","我要做","我要去","我决定","让我","安排我","我安排"};

    public String[] words_course_special = {"上课不","上课吗","上课否","上不上课","课多吗","课多不多","课多不","上啥课","上什么课","几节课","啥课"};


    public String[] words_where = {"在哪", "是哪", "哪间", "哪边", "哪里","怎么走","什么","哪栋"};
    public String[] words_target_findClassroom = {"在哪上课","上课在哪","在哪上"};
    public String[] words_classroom= {"教室","班级","讲堂"};


    public String[] words_time_days = {"明天", "后天", "大后天", "昨天", "今天","前天","大前天"};
    public String[] words_time_days_with_period = { "今晚", "明晚","今早","明早"};
    public static String[] words_time_DOW = {"星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期天", "周一", "周二", "周三", "周四", "周五", "周六", "周天", "周日", "星期日", "星期1", "星期2", "星期3", "星期4", "星期5", "星期6", "周1", "周2", "周3", "周4", "周5", "周6"};
    public String[] words_time_week_withoutHead = {
            "1周", "2周", "3周", "4周", "5周", "6周", "7周", "8周", "9周", "11周", "12周", "13周", "14周", "15周", "16周", "17周", "18周", "19周", "10周"
            , "一周", "二周", "三周", "四周", "五周", "六周", "七周", "八周", "九周", "十周", "十一周", "十二周", "十三周", "十四周", "十五周", "十六周", "十七周", "十八周", "十九周"
            ,"前周","上周","这周","下周","本周","这星期","下星期","上星期"
    };
    public String[] words_time_hour = {
            "一点", "二点", "两点", "三点", "四点", "五点", "六点", "七点", "八点", "九点", "十点", "十一点", "十二点", "十三点", "十四点", "十五点", "十六点", "十七点", "十八点", "十九点", "二十点", "二十一点", "二十二点", "二十三点", "二十四点", "零点",
            "一点半", "二点半", "两点半", "三点半", "四点半", "五点半", "六点半", "七点半", "八点半", "九点半", "十点半", "十一点半", "十二点半", "十三点半", "十四点半", "十五点半", "十六点半", "十七点半", "十八点半", "十九点半", "二十点半", "二十一点半", "二十二点半", "二十三点半", "二十四点半", "零点半",
            "一点整", "二点整", "两点整", "三点整", "四点整", "五点整", "六点整", "七点整", "八点整", "九点整", "十点整", "十一点整", "十二点整", "十三点整", "十四点整", "十五点整", "十六点整", "十七点整", "十八点整", "十九点整", "二十点整", "二十一点整", "二十二点整", "二十三点整", "二十四点整", "零点整",
            "1点", "2点", "3点", "4点", "5点", "6点", "7点", "8点", "9点", "10点", "11点", "12点", "13点", "14点", "15点", "16点", "17点", "18点", "19点", "20点", "21点", "22点", "23点", "24点", "0点",
            "1点半", "2点半", "3点半", "4点半", "5点半", "6点半", "7点半", "8点半", "9点半", "10点半", "11点半", "12点半", "13点半", "14点半", "15点半", "16点半", "17点半", "18点半", "19点半", "20点半", "21点半", "22点半", "23点半", "24点半", "0点半",
            "1点整", "2点整", "3点整", "4点整", "5点整", "6点整", "7点整", "8点整", "9点整", "10点整", "11点整", "12点整", "13点整", "14点整", "15点整", "16点整", "17点整", "18点整", "19点整", "20点整", "21点整", "22点整", "23点整", "24点整", "0点整"

    };
    public String[] words_time_minute = {
            "零一分", "零二分", "零三分", "零四分", "零五分", "零六分", "零七分", "零八分", "零九分", "零十分",
            "零十一分", "零十二分", "零十三分", "零十四分", "零十五分", "零十六分", "零十七分", "零十八分", "零十九分", "零二十分",
            "零二十一分", "零二十二分", "零二十三分", "零二十四分", "零二十五分", "零二十六分", "零二十七分", "零二十八分", "零二十九分", "零三十分",
            "零三十一分", "零三十二分", "零三十三分", "零三十四分", "零三十五分", "零三十六分", "零三十七分", "零三十八分", "零三十九分", "零四十分",
            "零五十一分", "零五十二分", "零五十三分", "零五十四分", "零五十五分", "零五十六分", "零五十七分", "零五十八分", "零五十九分",
            "零1分", "零2分", "零3分", "零4分", "零5分", "零6分", "零7分", "零8分", "零9分", "零10分",
            "零21分", "零22分", "零23分", "零24分", "零25分", "零26分", "零27分", "零28分", "零29分", "零30分",
            "零31分", "零32分", "零33分", "零34分", "零35分", "零36分", "零37分", "零38分", "零39分", "零40分",
            "零41分", "零42分", "零43分", "零44分", "零45分", "零46分", "零47分", "零48分", "零49分", "零50分",
            "零51分", "零52分", "零53分", "零54分", "零55分", "零56分", "零57分", "零58分", "零59分",
            "一分", "二分", "两分", "三分", "四分", "五分", "六分", "七分", "八分", "九分", "十分",
            "十一分", "十二分", "十三分", "十四分", "十五分", "十六分", "十七分", "十八分", "十九分", "二十分",
            "二十一分", "二十二分", "二十三分", "二十四分", "二十五分", "二十六分", "二十七分", "二十八分", "二十九分", "三十分",
            "三十一分", "三十二分", "三十三分", "三十四分", "三十五分", "三十六分", "三十七分", "三十八分", "三十九分", "四十分",
            "五十一分", "五十二分", "五十三分", "五十四分", "五十五分", "五十六分", "五十七分", "五十八分", "五十九分",
            "1分", "2分", "3分", "4分", "5分", "6分", "7分", "8分", "9分", "10分",
            "21分", "22分", "23分", "24分", "25分", "26分", "27分", "28分", "29分", "30分",
            "31分", "32分", "33分", "34分", "35分", "36分", "37分", "38分", "39分", "40分",
            "41分", "42分", "43分", "44分", "45分", "46分", "47分", "48分", "49分", "50分",
            "51分", "52分", "53分", "54分", "55分", "56分", "57分", "58分", "59分"
    };
    public String[] words_time_period = {
            "上午", "中午", "早晨", "早上", "午间", "一大早", "下午", "午后", "晚上", "夜晚", "夜间"
            , "前半天", "后半天"
    };

    public String[] words_to = {"到", "至", "to","~","-"};
    public String[] words_number = {
            "一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九"
    };
    public String[] words_this ={
            "这","此","本"
    };
    public String[] words_last ={
            "上","前"
    };
    public String[] words_next = {
            "下","过一周","下个"
    };
    public String[] words_backup_next={
            "前周","上周","这周","下周","本周","下星期","上星期","下下星期","上上星期"
    };
    public String[] words_next_one={
            "一会儿","一会","接下来"
    };
    public String[] sentence_fun_explore={
            "探索模式","进入探索模式","开启探索模式","启动探索模式","激活探索模式"
            ,"大学城里有哪些活动","探索大学城","今天有哪些活动","大学城有什么活动"
            ,"大学城里有什么活动","探索周边","打开探索模式","打开地图"
    };
    public String[] sentence_fun_canteen={
            "有哪些食堂","有什么好吃","食堂在哪","哪有食堂","去哪吃","去何处吃","去哪用餐","去何处用餐",
            "去哪进食","有啥好吃","哪有好吃","哪有吃","哪里有食堂","哪里有吃的","有什么吃","打开食堂"
    };
    public String[] sentence_fun_jwts={
            "打开教务系统","启动教务系统","查成绩","进入教务系统","导入课表","同步课表","同步课程表","哈工深教务","哈工大深圳教务系统"
            ,"培养计划查询","开启教务系统"
    };
    public String[] sentence_fun_infos={
            "有什么新闻","有什么讲座","有什么通知","查询新闻","查询讲座","查询通知","有那些通知","有那些讲座","有哪些新闻","校区新闻"
    };
    public List<Term> ReTag(List<Term> x) {
        for (Term t : x) {
            if (mEquals(t.getName(), words_time_DOW)) t.setNature(new Nature("t_dow"));
            if (mEquals(t.getName(), words_time_week_withoutHead)) t.setNature(new Nature("t_w"));
            if (isNumber(t.getName()) || mEquals(t.getName(), words_number))
                t.setNature(new Nature("number"));
            if (mEquals(t.getName(), words_time_period)) t.setNature(new Nature("t_pr"));
            if (mEquals(t.getName(), words_time_hour)) t.setNature(new Nature("t_h"));
            if (mEquals(t.getName(), words_to)) t.setNature(new Nature("to"));
            if (mEquals(t.getName(), words_time_minute)) t.setNature(new Nature("t_m"));
            if (mEquals(t.getName(), words_this)) t.setNature(new Nature("this"));
            if (mEquals(t.getName(), words_next)) t.setNature(new Nature("next"));
            if (mEquals(t.getName(), words_last)) t.setNature(new Nature("last"));
            if(mEquals(t.getName(),words_time_days)) t.setNature(new Nature("t_dow"));
            if(mEquals(t.getName(),words_time_days_with_period)) t.setNature(new Nature("t_dow_p"));
            if(mEquals(t.getName(),words_next_one)) t.setNature(new Nature("t_nextone"));
            if(mEquals(t.getName(),words_add_remind)) t.setNature(new Nature("add_remind"));
        }
        return x;
    }

    public boolean mContains(String x, String[] bases) {
        if (bases == null) return false;
        for (String i : bases) {
            if (x.contains(i)) return true;
        }
        return false;
    }

    public boolean mEquals(String x, String[] bases) {
        for (String i : bases) {
            if (x.equals(i)) return true;
        }
        return false;
    }

    public int getCount(String x[], String key) {
        int result = 0;
        for (String i : x) {
            if (i.equals(key)) result++;
        }
        return result;
    }

    public int getCount(String x[], String[] key) {
        int result = 0;
        for (String i : x) {
            if (getCount(key, i) >= 1) result++;
        }
        return result;
    }

    public String getStringWithTag(List<Term> x, String type, int number) {
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
    public String getStringAfterTag(List<Term> x,  String[] from) {
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

    public int getCount(List<Term> x, String key, boolean isName) {
        int result = 0;
        if (!isName) for (Term t : x)
            if (t.getNatureStr().contains(key)) result++;
            else for (Term y : x) if (y.getName().contains(key)) result++;
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

    public String getStringBetweenTag(List<Term> x, String[] base, String[] tag, boolean isName, int number1, int number2, int number3) {
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

    public boolean isNumber(String x) {
        if (x == null || x.length() <= 0) return false;
        for (int i = 0; i < x.length(); i++) {
            if (!(x.charAt(i) >= '0' && x.charAt(i) <= '9')) {
                return false;
            }
        }
        return true;
    }

}
