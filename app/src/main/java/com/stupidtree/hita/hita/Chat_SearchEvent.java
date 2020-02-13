package com.stupidtree.hita.hita;

import com.google.gson.JsonObject;
import com.stupidtree.hita.R;


import java.util.List;

import static com.stupidtree.hita.hita.TextTools.*;

public class Chat_SearchEvent {

    private static final int SC_WW = 1;
    private static final int SC_NO = 2;

    public static JsonObject Process(List<Term> x, int TAG) {
        reUnion(x);
        System.out.println("已完成重组："+x);
        JsonObject result = new JsonObject();
        switch (Judge(x)) {
            case SC_WW:
                System.out.println("已识别为WW事件查询");
                JsonObject object = processSEWW(x);
                object.addProperty("tag",TAG);
                return object;
            case SC_NO:
                System.out.println("已识别为NO事件查询");
                JsonObject object1 = new JsonObject();
                object1.addProperty("function","search_event_nextone");
                object1.addProperty("tag",TAG);
                return object1;

        }
        result.addProperty("message_show","你这个查询信息不太对啊");
        return result;
    }

    private static void reUnion(List<Term> x) {
        for (int i = 0; i < x.size(); i++) {
            if (i+1<x.size()&&x.get(i).getTag().equals("t*w")) {
                x.get(i).setTag(("t_w"));
                x.get(i).setContent(x.get(i).getContent().substring(0, x.get(i).getContent().length() - 1));
                x.add(i + 1, new Term("到",  "to"));
            }
            if (i+1<x.size()&&x.get(i).getTag().equals("t*m")) {
                x.get(i).setTag(("t_m"));
                x.get(i).setContent(x.get(i).getContent().substring(0, x.get(i).getContent().length() - 1));
                x.add(i + 1, new Term("到",  "to"));
            }
            if (i+1<x.size()&&i+2<x.size()&&x.get(i).getTag().equals("number") && TextTools.mContains(x.get(i + 2).getTag(), new String[]{"t_w", "t*w"}) && TextTools.mContains(x.get(i + 1).getContent(), R.array.words_to)) {
                x.get(i).setContent(x.get(i).getContent() + "周");
                x.get(i).setTag(("t_w"));
            }
            if (i+1<x.size()&&i+2<x.size()&&x.get(i).getTag().equals("t_dow") && TextTools.mContains(x.get(i + 1).getContent(), R.array.words_to) && TextTools.mEquals(x.get(i + 2).getContent(),R.array.words_number)) {
                x.get(i + 2).setContent("周" + x.get(i + 2).getContent());
                x.get(i + 2).setTag(("t_dow"));
            }
            if (i+1<x.size()&&i+2<x.size()&&x.get(i).getTag().equals("number") && x.get(i + 1).getContent().equals(":") && x.get(i + 2).getTag().equals("number")) {
                x.get(i).setTag(("t_h"));
                x.get(i).setContent(x.get(i).getContent() + "点");
                x.get(i + 2).setTag(("t_m"));
                x.get(i + 2).setContent(x.get(i + 2).getContent() + "分");

            }
            if (i+1<x.size()&&x.get(i).getTag().equals("this") && x.get(i + 1).getTag().equals("t_dow")) {
                x.get(i).setTag(("t_w"));
                x.get(i).setContent("这周");
            }
            if (i+1<x.size()&&x.get(i).getTag().equals("next") && x.get(i + 1).getTag().equals("t_dow")) {
                x.get(i).setTag(("t_w"));
                x.get(i).setContent("下周");
            }
            if (i+1<x.size()&&x.get(i).getTag().equals("last") && x.get(i + 1).getTag().equals("t_dow")) {
                x.get(i).setTag(("t_w"));
                x.get(i).setContent("上周");
            }
            if(i+1<x.size()&&TextTools.mEquals(x.get(i).getContent(),R.array.words_backup_next)&&x.get(i+1).getTag().equals("number")){
                String old = x.get(i+1).getContent();
                x.remove(i+1);
                x.add(i+1,new Term("周"+old,"t_dow"));
            }
            if(TextTools.mEquals(x.get(i).getContent(),R.array.words_time_days_with_period)){
                String first = x.get(i).getContent().substring(0,1)+"天";
                String second  = x.get(i).getContent().substring(1)+"上";
                x.remove(i);
                x.add(i,new Term(first,"t_dow"));
                x.add(i+1,new Term(second,"t_pr"));
            }
        }
    }

    public static int Judge(List<Term> x) {
        if(TextTools.getCount_contains(x,"t_nextone",false)>=1){
            return SC_NO;
        }
        if ((TextTools.getCount_contains(x, "t_w", false) + TextTools.getCount_contains(x, "t_dow", false) >= 1)
                && TextTools.getCount(x, R.array.words_to, true) >= 1
                || TextTools.getCount_contains(x, "t_w", false) + TextTools.getCount_contains(x, "t_dow", false) + TextTools.getCount_contains(x, "t*w", false)+TextTools.getCount_contains(x,"t_pr",false) >= 1
                || (TextTools.getCount_contains(x, "t_h", false) + TextTools.getCount_contains(x, "t_m", false)  >= 1)&& TextTools.getCount(x, R.array.words_to, true) >= 1 )
         {
            return SC_WW;
        }

        return 0;
    }

    private static JsonObject processSEWW(List<Term> x) {
        String fromW_Txt;
        String toW_Txt;
        String fromDOW_Txt;
        String toDOW_Txt;
        String fromH_Txt;
        String toH_Txt;
        String fromM_Txt;
        String toM_Txt;
        String fromPr_Txt;
        String toPr_Txt;
        String num_Txt;
        int fromW_Num = 0;
        int toW_Num = 0;
        int fromDOW_Num = 0;
        int toDOW_Num = 0;
        int fromH_Num = 0;
        int toH_Num = 0;
        int fromM_Num = 0;
        int toM_Num = 0;
        int num_Num = 0;

        String[] temp1 = {"t_w"};
        String[] temp2 = {"t_dow"};
        String[] temp3 = {"to"};
        String[] temp4 = {"t_h"};
        String[] temp5 = {"t_m"};
        String[] temp6 = {"t_pr"};
        num_Txt = TextTools.getStringWithTag(x,"t_num",1);
        num_Num = parseNumText(num_Txt);
        fromW_Txt = TextTools.getStringBetweenTag(x, temp1, temp3, false, 0, 1, 1);
        toW_Txt = TextTools.getStringBetweenTag(x, temp1, temp3, false, 1, 0, 1);
        fromW_Num = parseWeekText(fromW_Txt);
        toW_Num = parseWeekText(toW_Txt);
        fromDOW_Txt = TextTools.getStringBetweenTag(x, temp2, temp3, false, 0, 1, 1);
        toDOW_Txt = TextTools.getStringBetweenTag(x, temp2, temp3, false, 1, 0, 1);
        fromDOW_Num = parseDOWText(fromDOW_Txt);
        toDOW_Num = parseDOWText(toDOW_Txt);
        fromH_Txt = TextTools.getStringBetweenTag(x, temp4, temp3, false, 0, 1, 1);
        toH_Txt = TextTools.getStringBetweenTag(x, temp4, temp3, false, 1, 0, 1);
        fromH_Num = parseHourText(fromH_Txt);
        toH_Num = parseHourText(toH_Txt);
        if (fromH_Num > 100) {
            fromH_Num -= 100;
            fromM_Num = 30;
        } else {
            fromM_Txt = TextTools.getStringBetweenTag(x, temp5, temp3, false, 0, 1, 1);
            fromM_Num = parseMinuteText(fromM_Txt);
        }
        if (toH_Num > 100) {
            toH_Num -= 100;
            toM_Num = 30;
        } else {
            toM_Txt = TextTools.getStringBetweenTag(x, temp5, temp3, false, 1, 0, 1);
            toM_Num = parseMinuteText(toM_Txt);
        }
        fromPr_Txt = TextTools.getStringBetweenTag(x, temp6, temp3, false, 0, 1, 1);
        toPr_Txt = TextTools.getStringBetweenTag(x, temp6, temp3, false, 1, 0, 1);
        if (fromPr_Txt != null&&toPr_Txt==null && fromH_Txt == null && toH_Txt == null) {
            switch (parsePeriodText(fromPr_Txt)) {
                case 1:
                    fromH_Num = 8;
                    fromM_Num = 0;
                    toH_Num = 11;
                    toM_Num = 59;
                    break;
                case 2:
                    fromH_Num = 12;
                    fromM_Num = 0;
                    toH_Num = 12;
                    toM_Num = 59;
                    break;
                case 3:
                    fromH_Num = 13;
                    fromM_Num = 0;
                    toH_Num = 17;
                    toM_Num = 59;
                    break;
                case 4:
                    fromH_Num = 18;
                    fromM_Num = 0;
                    toH_Num = 23;
                    toM_Num = 59;
                    break;
            }
        } else{
            if (fromPr_Txt != null && fromH_Txt == null) {
                switch (parsePeriodText(fromPr_Txt)) {
                    case 1:
                        fromH_Num = 8;
                        fromM_Num = 0;
                        break;
                    case 2:
                        fromH_Num = 12;
                        fromM_Num = 0;
                        break;
                    case 3:
                        fromH_Num = 13;
                        fromM_Num = 0;
                        break;
                    case 4:
                        fromH_Num = 18;
                        fromM_Num = 0;
                        break;
                }
            }
                if (toPr_Txt != null && toH_Txt == null) {
                    switch (parsePeriodText(toPr_Txt)) {
                        case 1:
                            toH_Num = 11;
                            toM_Num = 59;
                            break;
                        case 2:
                            toH_Num = 12;
                            toM_Num = 59;
                            break;
                        case 3:
                            toH_Num = 17;
                            toM_Num = 59;
                            break;
                        case 4:
                            toH_Num = 23;
                            toM_Num = 59;
                            break;
                    }
                }

        }
        if (fromPr_Txt != null && fromH_Num <= 12&&fromH_Txt!=null) {
            if (parsePeriodText(fromPr_Txt) >= 3) fromH_Num += 12;
        }
        if (toPr_Txt != null && toH_Num <= 12&&toH_Txt!=null) {
            if (parsePeriodText(toPr_Txt) >= 3) toH_Num += 12;
        }
       JsonObject OBJ = new JsonObject();
        OBJ.addProperty("fW",fromW_Num);
        OBJ.addProperty("tW",toW_Num);
        OBJ.addProperty("fDOW",fromDOW_Num);
        OBJ.addProperty("tDOW",toDOW_Num);
        OBJ.addProperty("fH",fromH_Num);
        OBJ.addProperty("fM",fromM_Num);
        OBJ.addProperty("tH",toH_Num);
        OBJ.addProperty("tM",toM_Num);
        OBJ.addProperty("num",num_Num);
        OBJ.addProperty("function","search_event_ww");
        return OBJ;
    }

    private static int parseWeekText(String text) {
        if (text == null || text.equals("")) return -1;
        String pureText = null;
        int result = -1;
        if (text.startsWith("第") && text.endsWith("周"))
            pureText = text.substring(1, text.length() - 1);
        else if (text.endsWith("周")) pureText = text.substring(0, text.length() - 1);
        else if (text.endsWith("星期")) pureText = text.substring(0, text.length() - 2);
        else return -1;

        if (isNumber(pureText)) {
            result = Integer.parseInt(pureText);
        } else {
            if (pureText.equals("一")) result = 1;
            else if (pureText.equals("二")) result = 2;
            else if (pureText.equals("三")) result = 3;
            else if (pureText.equals("四")) result = 4;
            else if (pureText.equals("五")) result = 5;
            else if (pureText.equals("六")) result = 6;
            else if (pureText.equals("七")) result = 7;
            else if (pureText.equals("八")) result = 8;
            else if (pureText.equals("九")) result = 9;
            else if (pureText.equals("十一")) result = 11;
            else if (pureText.equals("十二")) result = 12;
            else if (pureText.equals("十三")) result = 13;
            else if (pureText.equals("十四")) result = 14;
            else if (pureText.equals("十五")) result = 15;
            else if (pureText.equals("十六")) result = 16;
            else if (pureText.equals("十七")) result = 17;
            else if (pureText.equals("十八")) result = 18;
            else if (pureText.equals("十九")) result = 19;
            else if(TextTools.mEquals(pureText,R.array.words_this)) result = THIS;
            else if(TextTools.mEquals(pureText,R.array.words_next)) result = NEXT;
            else if(TextTools.mEquals(pureText,R.array.words_last)) result = BEFORE;
        }
        return result;
    }

    private static int parseDOWText(String text) {
        if (text == null || text.equals("")) return -1;
        String pureText = null;
        int result = -1;
        if (text.startsWith("周")) pureText = text.substring(1);
        else if (text.startsWith("星期")) pureText = text.substring(2);
        else if(TextTools.mEquals(text, R.array.words_time_days)) pureText = text;
        else return -1;
        if (isNumber(pureText)) {
            result = Integer.parseInt(pureText);
        } else {
            if (pureText.equals("一")) result = 1;
            else if (pureText.equals("二")) result = 2;
            else if (pureText.equals("三")) result = 3;
            else if (pureText.equals("四")) result = 4;
            else if (pureText.equals("五")) result = 5;
            else if (pureText.equals("六")) result = 6;
            else if (pureText.equals("日")) result = 7;
            else if (pureText.equals("天")) result = 7;
            else if(TextTools.mEquals(pureText,new String[]{"明天"})) result = NEXT;
            else if(TextTools.mEquals(pureText,new String[]{"昨天"})) result = BEFORE;
            else if(TextTools.mEquals(pureText,new String[]{"前天"})) result = T_BEFORE;
            else if(TextTools.mEquals(pureText,new String[]{"大前天"})) result = TT_BEFORE;
            else if(TextTools.mEquals(pureText,new String[]{"大前天"})) result = TT_BEFORE;
            else if(TextTools.mEquals(pureText,new String[]{"后天"})) result = T_NEXT;
            else if(TextTools.mEquals(pureText,new String[]{"大后天"})) result = TT_NEXT;
            else if(TextTools.mEquals(pureText,new String[]{"今天"})) result = THIS;

        }
        return result;
    }

    private static int parseHourText(String text) {
        if (text == null || text.equals("")) return -1;
        String pureText = null;
        int result = -1;
        if (text.endsWith("点半") || text.endsWith("点钟") || text.endsWith("点整"))
            pureText = text.substring(0, text.length() - 2);
        else if (text.endsWith("点")) pureText = text.substring(0, text.length() - 1);
        else return -1;
        if (isNumber(pureText)) {
            result = Integer.parseInt(pureText);
        } else {
            if (pureText.equals("一")) result = 1;
            else if (pureText.equals("二") || pureText.equals("两")) result = 2;
            else if (pureText.equals("三")) result = 3;
            else if (pureText.equals("四")) result = 4;
            else if (pureText.equals("五")) result = 5;
            else if (pureText.equals("六")) result = 6;
            else if (pureText.equals("七")) result = 7;
            else if (pureText.equals("八")) result = 8;
            else if (pureText.equals("九")) result = 9;
            else if (pureText.equals("十一")) result = 11;
            else if (pureText.equals("十二")) result = 12;
            else if (pureText.equals("十三")) result = 13;
            else if (pureText.equals("十四")) result = 14;
            else if (pureText.equals("十五")) result = 15;
            else if (pureText.equals("十六")) result = 16;
            else if (pureText.equals("十七")) result = 17;
            else if (pureText.equals("十八")) result = 18;
            else if (pureText.equals("十九")) result = 19;
            else if (pureText.equals("二十")) result = 20;
            else if (pureText.equals("二十一") || pureText.equals("二一")) result = 21;
            else if (pureText.equals("二十二") || pureText.equals("二二")) result = 22;
            else if (pureText.equals("二十三") || pureText.equals("二三")) result = 23;
            else if (pureText.equals("二十四") || pureText.equals("二四")) result = 24;
            else if (pureText.equals("零")) result = 0;
            else result = -1;
        }
        if (text.endsWith("点半")) result += 100;
        return result;
    }

    private static int parseMinuteText(String text) {
        if (text == null || text.equals("")) return -1;
        String pureText = null;
        int result = -1;
        if (text.endsWith("分钟")) pureText = text.substring(0, text.length() - 2);
        else if (text.endsWith("分")) pureText = text.substring(0, text.length() - 1);
        else return -1;
        if (isNumber(pureText)) {
            result = Integer.parseInt(pureText);
        } else {
            if (pureText.equals("一")) result = 1;
            else if (pureText.equals("二") || pureText.equals("两")) result = 2;
            else if (pureText.equals("三")) result = 3;
            else if (pureText.equals("四")) result = 4;
            else if (pureText.equals("五")) result = 5;
            else if (pureText.equals("六")) result = 6;
            else if (pureText.equals("七")) result = 7;
            else if (pureText.equals("八")) result = 8;
            else if (pureText.equals("九")) result = 9;
            else if (pureText.equals("十一")) result = 11;
            else if (pureText.equals("十二")) result = 12;
            else if (pureText.equals("十三")) result = 13;
            else if (pureText.equals("十四")) result = 14;
            else if (pureText.equals("十五")) result = 15;
            else if (pureText.equals("十六")) result = 16;
            else if (pureText.equals("十七")) result = 17;
            else if (pureText.equals("十八")) result = 18;
            else if (pureText.equals("十九")) result = 19;
            else if (pureText.equals("二十")) result = 20;
            else if (pureText.equals("二十一")) result = 21;
            else if (pureText.equals("二十二")) result = 22;
            else if (pureText.equals("二十三")) result = 23;
            else if (pureText.equals("二十四")) result = 24;
            else if (pureText.equals("二十五")) result = 25;
            else if (pureText.equals("二十六")) result = 26;
            else if (pureText.equals("二十七")) result = 27;
            else if (pureText.equals("二十八")) result = 28;
            else if (pureText.equals("二十九")) result = 29;
            else if (pureText.equals("三十")) result = 30;
            else if (pureText.equals("三十一")) result = 31;
            else if (pureText.equals("三十二")) result = 32;
            else if (pureText.equals("三十三")) result = 33;
            else if (pureText.equals("三十四")) result = 34;
            else if (pureText.equals("三十五")) result = 35;
            else if (pureText.equals("三十六")) result = 36;
            else if (pureText.equals("三十七")) result = 37;
            else if (pureText.equals("三十八")) result = 38;
            else if (pureText.equals("三十九")) result = 39;
            else if (pureText.equals("四十")) result = 40;
            else if (pureText.equals("四十一")) result = 41;
            else if (pureText.equals("四十二")) result = 42;
            else if (pureText.equals("四十三")) result = 43;
            else if (pureText.equals("四十四")) result = 44;
            else if (pureText.equals("四十五")) result = 45;
            else if (pureText.equals("四十六")) result = 46;
            else if (pureText.equals("四十七")) result = 47;
            else if (pureText.equals("四十八")) result = 48;
            else if (pureText.equals("四十九")) result = 49;
            else if (pureText.equals("五十")) result = 50;
            else if (pureText.equals("五十一")) result = 51;
            else if (pureText.equals("五十二")) result = 52;
            else if (pureText.equals("五十三")) result = 53;
            else if (pureText.equals("五十四")) result = 54;
            else if (pureText.equals("五十五")) result = 55;
            else if (pureText.equals("五十六")) result = 56;
            else if (pureText.equals("五十七")) result = 57;
            else if (pureText.equals("五十八")) result = 58;
            else if (pureText.equals("五十九")) result = 59;
            else if (pureText.equals("零")) result = 0;
            else result = -1;
        }
        return result;


    }

    private static int parsePeriodText(String text) {
        /*上午（0~11）=1，中午(11.01~13.00)=2，下午(13.01~17.00)=3，晚上(17.01~24.00)=4*/
        String[] morning = {"上午", "早上", "早晨", "前半天"};
        String[] noon = {"中午", "午间"};
        String[] afternoon = {"下午", "午后", "后半天"};
        String[] night = {"晚上", "夜晚", "夜间"};
        if (TextTools.mContains(text, morning)) return 1;
        if (TextTools.mContains(text, noon)) return 2;
        if (TextTools.mContains(text, afternoon)) return 3;
        if (TextTools.mContains(text, night)) return 4;
        return -1;
    }

    private static int parseNumText(String text){
        if(text==null||text.isEmpty()) return -1;
        String pureText;
        int result;
        if(text.contains("第")){
            pureText = text.substring(text.indexOf("第")+1,text.length()-1);
            if(isNumber(pureText)){
                result = Integer.parseInt(pureText);
            }else{
                if (pureText.equals("一")) result = 1;
                else if (pureText.equals("二")) result = 2;
                else if (pureText.equals("三")) result = 3;
                else if (pureText.equals("四")) result = 4;
                else if (pureText.equals("五")) result = 5;
                else if (pureText.equals("六")) result = 6;
                else if (pureText.equals("七")) result = 7;
                else if (pureText.equals("八")) result = 8;
                else if (pureText.equals("九")) result = 9;
                else if (pureText.equals("十一")) result = 11;
                else if (pureText.equals("十二")) result = 12;
                else result = 1;
            }
        }else{
            result = TextTools.LAST;
        }
        return result;
    }
}
