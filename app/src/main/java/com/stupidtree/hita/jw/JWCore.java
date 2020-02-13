package com.stupidtree.hita.jw;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.WorkerThread;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.stupidtree.hita.hita.TextTools;
import com.stupidtree.hita.util.FileOperator;
import com.stupidtree.hita.util.JsonUtils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

import static com.stupidtree.hita.HITAApplication.defaultSP;

public class JWCore {
    private boolean login;
    private HashMap<String, String> cookies;
    private Map<String, String> defaultRequestHeader;
    private int timeout;


    public JWCore() {
        login = false;
        timeout = 3000;
        cookies = new HashMap<>();
        defaultRequestHeader = new HashMap<String, String>();
        defaultRequestHeader.put("Accept", "*/*");
        defaultRequestHeader.put("Connection", "keep-alive");
        defaultRequestHeader.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36");

    }

    public boolean hasLogin() {
        return login;
    }

    @WorkerThread
    public boolean login(String username, String password) throws JWException {
        try {
            Connection hc = Jsoup.connect("http://jw.hitsz.edu.cn/cas").headers(defaultRequestHeader);
            cookies.clear();
            cookies.putAll(hc.execute().cookies());
            String lt = null, execution = null, eventId = null;
            Document d = hc.cookies(cookies).get();
            lt = d.select("input[name=lt]").first().attr("value");
            execution = d.select("input[name=execution]").first().attr("value");
            eventId = d.select("input[name=_eventId]").first().attr("value");
            Connection c2 = Jsoup.connect("https://sso.hitsz.edu.cn:7002/cas/login?service=http%3A%2F%2Fjw.hitsz.edu.cn%2FcasLogin")
                    .cookies(cookies)
                    .headers(defaultRequestHeader)
                    .ignoreContentType(true);
            Document page = c2.cookies(cookies)
                    .data("username", username)
                    .data("password", password)
                    .data("lt", lt)
                    .data("rememberMe", "on")
                    .data("execution", execution)
                    .data("_eventId", eventId).post();
            cookies.putAll(c2.response().cookies());
            login = page.toString().contains("qxdm");
            if (login) {
                if(defaultSP == null) return login;
                SharedPreferences.Editor edt = defaultSP.edit();
                edt.putString("jw_cookie", new Gson().toJson(cookies));
                edt.putString(username + ".password", password);
                edt.apply();
            } else logOut();
            return login;
        } catch (IOException e) {
            throw JWException.getConnectErrorExpection();
        }
    }

    @WorkerThread
    public boolean loginCheck() throws JWException {
        try {
            Document s = Jsoup.connect("http://jw.hitsz.edu.cn/UserManager/queryxsxx")
                    .timeout(timeout)
                    .cookies(cookies)
                    .headers(defaultRequestHeader)
                    .header("X-Requested-With", "XMLHttpRequest")
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .post();
            try {
                String json = s.getElementsByTag("body").text();
                if(json.contains("session已失效")) return false;
                JsonObject jo = new JsonParser().parse(json).getAsJsonObject();
                login = jo.has("XH");
                if (!login) logOut();
                return login;
            } catch (JsonSyntaxException e) {
                logOut();
                return false;
            }
        } catch (IOException e) {
            throw JWException.getConnectErrorExpection();
        }
    }

    @WorkerThread
    public Map<String,String> getBasicUserInfo() throws JWException {
        Map<String,String> result = new HashMap<>();
        try {
            Document s = Jsoup.connect("http://jw.hitsz.edu.cn/UserManager/queryxsxx")
                    .timeout(timeout)
                    .cookies(cookies)
                    .headers(defaultRequestHeader)
                    .header("X-Requested-With", "XMLHttpRequest")
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .post();
            try {
                String json = s.getElementsByTag("body").text();
                JsonObject jo = new JsonParser().parse(json).getAsJsonObject();
                result.put("real_name",JsonUtils.getStringInfo(jo,"XM"));
                result.put("school",JsonUtils.getStringInfo(jo,"YXMC"));
                result.put("student_number",JsonUtils.getStringInfo(jo,"XH"));
                result.put("grade",JsonUtils.getStringInfo(jo,"NJMC"));
                return result;
            } catch (JsonSyntaxException e) {
                logOut();
                throw JWException.getLoginFailedExpection();
            }
        } catch (IOException e) {
            throw JWException.getConnectErrorExpection();
        }
    }

    @WorkerThread
    public List<Map<String,String>> getGRCJ(String xn,String xq) throws JWException {
        List<Map<String,String>> result = new ArrayList<>();
        try {
            JsonObject requestPayLoad = new JsonObject();
            requestPayLoad.addProperty("xn",xn);
            requestPayLoad.addProperty("xq",xq);
            requestPayLoad.addProperty("kcmc",(String)null);
            requestPayLoad.addProperty("cxbj","-1");
            requestPayLoad.addProperty("pylx","1");
            requestPayLoad.addProperty("current", 1);
            requestPayLoad.addProperty("pageSize",100);
            Document s = Jsoup.connect("http://jw.hitsz.edu.cn/cjgl/grcjcx/grcjcx")
                    .timeout(timeout)
                    .cookies(cookies)
                    .headers(defaultRequestHeader)
                    .header("X-Requested-With", "XMLHttpRequest")
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .header("Content-Type","application/json;charset=UTF-8")
                    .requestBody(requestPayLoad.toString())
                    .post();
            try {
                String json = s.getElementsByTag("body").text();
                JsonArray list = new JsonParser().parse(json).getAsJsonObject().get("content").getAsJsonObject().get("list").getAsJsonArray();
                for(JsonElement je:list){
                    JsonObject jo = je.getAsJsonObject();
                    Map<String,String> m = new HashMap<>();
                    m.put("name",jo.get("kcmc").getAsString());
                    m.put("code",jo.get("kcdm").getAsString());
                    m.put("type",jo.get("kclb").getAsString());
                    m.put("compulsory",jo.get("kcxz").getAsString());
                    m.put("credit",jo.get("xf").getAsString());
                    m.put("total_score",jo.get("zzcj").getAsString());
                    m.put("final_score",jo.get("zzzscj").getAsString());
                    m.put("school",jo.get("yxmc").getAsString());
                    m.put("exam",jo.get("khfs").getAsString());
                    result.add(m);
                }
                return result;
            } catch (JsonSyntaxException e) {
                logOut();
                throw JWException.getLoginFailedExpection();
            }
        } catch (IOException e) {
            throw JWException.getConnectErrorExpection();
        }
    }


    public void logOut() {
        login = false;
        cookies.clear();
        if(defaultSP!=null)defaultSP.edit().putString("jw_cookie", null).apply();
    }

    @WorkerThread
    public List<Map<String, String>> getXNXQ() throws JWException {
        try {
            List<Map<String, String>> result = new ArrayList<>();
            Document s = Jsoup.connect("http://jw.hitsz.edu.cn/component/queryXnxq")
                    .cookies(cookies)
                    .headers(defaultRequestHeader)
                    .header("X-Requested-With", "XMLHttpRequest")
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .post();

            try {
                String json = s.getElementsByTag("body").first().text();
                JsonArray jsonList = new JsonParser().parse(json).getAsJsonObject().get("content").getAsJsonArray();
                for (JsonElement je : jsonList) {
                    Map<String, String> m = new HashMap<>();
                    for (Map.Entry<? extends String, ? extends JsonElement> e : je.getAsJsonObject().entrySet()) {
                        m.put(e.getKey().replaceAll("\"", ""), e.getValue().toString().replaceAll("\"", ""));
                    }
                    result.add(m);
                }
                return result;
            } catch (Exception e) {
                logOut();
                throw JWException.getLoginFailedExpection();
            }
        } catch (IOException e) {
            throw JWException.getConnectErrorExpection();
        }

    }

    @WorkerThread
    public List<Map<String, String>> getChosenSubjectsInfo(String xn, String xq) throws JWException {
        List<Map<String, String>> result = new ArrayList<>();
        try {
            Document d = Jsoup.connect("http://jw.hitsz.edu.cn/Xsxk/queryYxkc")
                    .timeout(timeout)
                    .cookies(cookies)
                    .headers(defaultRequestHeader)
                    .header("X-Requested-With", "XMLHttpRequest")
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .data("p_pylx", "1")
                    .data("p_sfgldjr", "0")
                    .data("p_sfredis", "0")
                    .data("p_sfsyxkgwc", "0")
                    .data("p_xn", xn) //学年
                    .data("p_xq", xq) //学期
                    .data("p_xnxq", xn + xq) //学年学期
                    .data("p_dqxn", xn) //当前学年
                    .data("p_dqxq", xq) //当前学期
                    .data("p_dqxnxq", xn + xq) //当前学年学期
                    .data("p_xkfsdm", "yixuan") //已选
                    .data("p_sfhlctkc", "0")
                    .data("p_sfhllrlkc", "0")
                    .data("p_sfxsgwckb", "1")
                    .post();
            String json = d.getElementsByTag("body").text();
            JsonArray yxkc = new JsonParser().parse(json).getAsJsonObject().get("yxkcList").getAsJsonArray();
            for (JsonElement je : yxkc) {
                JsonObject subject = je.getAsJsonObject();
                Map<String, String> m = new HashMap<>();
                m.put("code", JsonUtils.getStringInfo(subject, "kcdm"));
                m.put("name", JsonUtils.getStringInfo(subject, "kcmc"));
                m.put("compulsory", JsonUtils.getStringInfo(subject, "kcxzmc"));
                m.put("school", JsonUtils.getStringInfo(subject, "kkyxmc"));
                m.put("credit", JsonUtils.getStringInfo(subject, "xf"));
                m.put("period", JsonUtils.getStringInfo(subject, "xs"));
                m.put("type", JsonUtils.getStringInfo(subject, "kclbmc"));
                m.put("teacher", JsonUtils.getStringInfo(subject, "dgjsmc"));
                m.put("xnxq", xn + xq);
                //System.out.println(m);
                result.add(m);
            }

            return result;
        } catch (IOException e) {
            throw JWException.getConnectErrorExpection();
        }
    }

    @WorkerThread
    public List<Map<String, String>> getGRKBData(String xn, String xq) throws JWException {
        List<Map<String, String>> result = new ArrayList<>();
        try {
            Document s = Jsoup.connect("http://jw.hitsz.edu.cn/Xskbcx/queryXskbcxList")
                    .timeout(timeout)
                    .cookies(cookies)
                    .headers(defaultRequestHeader)
                    .header("X-Requested-With", "XMLHttpRequest")
                    .data("bs", "2")
                    .data("xn", xn)
                    .data("xq", xq)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .post();

            try {
                String json = s.getElementsByTag("body").text();
                JsonArray jsonList = new JsonParser().parse(json).getAsJsonArray();
                List<String> processedItems = new ArrayList<>();
                for (JsonElement je : jsonList) {
                    JsonObject jo = je.getAsJsonObject();
                    String mainSTR = jo.get("kbxx").getAsString();
                    mainSTR = mainSTR.replaceAll("待生效","");
                    String[] main = mainSTR.split("\n");
                    String tm = jo.get("key").getAsString();

                    int dow = tm.charAt(2) - '0';
                    int begin = Integer.parseInt(tm.split("_jc")[1]) * 2 - 1;
                    List<String> lineClips = Arrays.asList(main);
                    List<String> infoClips = new ArrayList<>();
                    String name = lineClips.get(0); //名字一定是第一个

                    for (int i=1;i<lineClips.size();i++) {
                        String line = lineClips.get(i);
                        for (String in : line.split("]\\[")) {
                            infoClips.add(in.replaceAll("]", "").replaceAll("\\[", ""));
                        }
                    }
                    String teacher = "", weekText = "", classroom = "";
                    String specificTime = null;
                    for (int i = 0; i < infoClips.size(); i++) {
                        String info = infoClips.get(i);
                        if (TextTools.containsNumber(info)&&info.contains("周")||
                                TextTools.isNumber(info.replaceAll(",","").replaceAll("-","").replaceAll("单","").replaceAll("双","")))
                            weekText = info;
                        else if (TextTools.containsNumber(info)&&info.contains("节")) specificTime = info.replaceAll("节", "");
                        else if (i == infoClips.size() - 1) classroom = info;
                        else teacher = info;
                    }

                    StringBuilder weeks = new StringBuilder();
                    List<Integer> weekI = new ArrayList<>();
                    for (String wk : weekText.split(",")) {
                        boolean pairW = false;
                        boolean singW = false;
                        if (wk.contains("单")) {
                            singW = true;
                            wk = wk.replaceAll("单", "").replaceAll("周", "");
                        } else if (wk.contains("双")) {
                            pairW = true;
                            wk = wk.replaceAll("双", "").replaceAll("周", "");
                        } else wk = wk.replaceAll("周", "");
                        if (wk.contains("-")) {
                            String[] ft = wk.split("-");
                            int from = Integer.parseInt(ft[0]);
                            int to = Integer.parseInt(ft[1]);
                            for (int i = from; i <= to; i++) {
                                if (pairW && i % 2 != 0 || singW && i % 2 == 0) continue;
                                if (!weekI.contains(i)) weekI.add(i);
                                //weeks.append(i+"").append(",");
                            }
                        } else if (!weekI.contains(Integer.parseInt(wk)))
                            weekI.add(Integer.parseInt(wk));
                    }
                    for (int i = 0; i < weekI.size(); i++) {
                        weeks.append(weekI.get(i) + "").append(i == weekI.size() - 1 ? "" : ",");
                    }

                    Map<String, String> m = new HashMap<>();
                    int beginFinal = begin, lastFinal = 2;
                    if (specificTime != null&&specificTime.contains("-")) {
                        String[] spcf = specificTime.split("-");
                        beginFinal = Integer.parseInt(spcf[0]);
                        lastFinal = Integer.parseInt(spcf[1]) - beginFinal + 1;
                    }
                    m.put("name", name);
                    m.put("teacher", teacher);
                    m.put("dow", String.valueOf(dow));
                    m.put("begin", String.valueOf(beginFinal));
                    m.put("classroom", classroom);
                    // m.put("weeks_raw",weekText);
                    m.put("weeks", weeks.toString());
                    m.put("last", String.valueOf(lastFinal));
                    if (!processedItems.contains(m.toString())) {
                        processedItems.add(m.toString());
                        result.add(m);
                    }

                    // System.out.println(m);
                }
            } catch (Exception e) {
                FileOperator.errorTableText et = new FileOperator.errorTableText(s.toString(),e);
                et.save(new SaveListener<String>() {
                    @Override
                    public void done(String s, BmobException e) {

                    }
                });
                e.printStackTrace();
                throw JWException.newDialogMessageExpection("导入错误！已上传错误报告" + e.toString());
            }
            return result;
        } catch (IOException e) {
            throw JWException.getConnectErrorExpection();
        }
    }

    @WorkerThread
    public Calendar getFirstDateOfCurriculum(String xn, String xq) throws JWException {
        try {
            Document s = Jsoup.connect("http://jw.hitsz.edu.cn/Xiaoli/queryMonthList")
                    .timeout(timeout)
                    .cookies(cookies)
                    .headers(defaultRequestHeader)
                    .header("X-Requested-With", "XMLHttpRequest")
                    .data("zyw", "zh")
                    .data("pxn", xn)
                    .data("pxq", xq)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .post();
            try {
                String json = s.getElementsByTag("body").text();
                JsonArray monthList = new JsonParser().parse(json).getAsJsonObject().get("monlist").getAsJsonArray();
                if (monthList.size() == 0) throw JWException.newDialogMessageExpection("该学期尚未开放！");
                JsonObject firstMon = monthList.get(0).getAsJsonObject();
                int year = firstMon.get("yy").getAsInt();
                int month = firstMon.get("mm").getAsInt();
                JsonArray firstMonDays = firstMon.get("dszlist").getAsJsonArray();
                int i;
                for (i = 0; i < firstMonDays.size(); i++) {
                    JsonObject aWeek = firstMonDays.get(i).getAsJsonObject();
                    JsonElement attr = aWeek.get("xldjz");
                    if (!attr.equals(JsonNull.INSTANCE)) break;
                }
                Calendar result = Calendar.getInstance();
                result.set(Calendar.YEAR, year);
                result.set(Calendar.MONTH, month - 1);
                result.set(Calendar.WEEK_OF_MONTH, i + 1);
                result.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
               // System.out.println("YY:" + year + " MM:" + month + "  DD:" + result.get(Calendar.DAY_OF_MONTH));
                return result;
            } catch (JsonSyntaxException e) {
                throw JWException.getLoginFailedExpection();
            }
        } catch (IOException e) {
            throw JWException.getConnectErrorExpection();
        }
    }
    @WorkerThread
    public List<String> getTeacherOfChosenSubjects(String xn, String xq) throws JWException {
        Log.e("gTO","xn:"+xn);
        List<String> result = new ArrayList<>();
        try {
            Document d = Jsoup.connect("http://jw.hitsz.edu.cn/Xsxk/queryYxkc")
                    .timeout(timeout)
                    .cookies(cookies)
                    .headers(defaultRequestHeader)
                    .header("X-Requested-With", "XMLHttpRequest")
                    .header("RoleCode","01")
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .data("p_pylx", "1")
                    .data("p_sfgldjr", "0")
                    .data("p_sfredis", "0")
                    .data("p_sfsyxkgwc", "0")
                    .data("p_xn", xn) //学年
                    .data("p_xq", xq) //学期
                    .data("p_xnxq", xn + xq) //学年学期
                    .data("p_dqxn", xn) //当前学年
                    .data("p_dqxq", xq) //当前学期
                    .data("p_dqxnxq", xn + xq) //当前学年学期
                    .data("p_xkfsdm", "yixuan") //已选
                    .data("p_sfhlctkc", "1")
                    .data("p_sfhllrlkc", "1")
                    .data("p_sfxsgwckb", "1")
                    .post();
              for (Element e:d.getElementsByTag("a")) {
                  String onclick = e.attr("onclick");
                  int from = onclick.indexOf("queryJsxx");
                  if(from>=0){
                      String id = onclick.substring(onclick.indexOf("(\'",from)+2,onclick.indexOf("\')",from));
                      result.add(id);

                  }
            }
            return result;
        } catch (IOException e) {
            throw JWException.getConnectErrorExpection();
        }
    }

    public Map<String, String> getTeacherData(String teacherId) throws JWException {
        Map<String, String> result = new HashMap<>();
        try {
            Document s = Jsoup.connect("http://jw.hitsz.edu.cn/Szgl/querySzglOneByjsid")
                    .timeout(timeout)
                    .cookies(cookies)
                    .headers(defaultRequestHeader)
                    .data("zghid", teacherId)
                    .header("X-Requested-With", "XMLHttpRequest")
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .post();
            try {
                String json = s.getElementsByTag("body").text();
                JsonObject t = new JsonParser().parse(json).getAsJsonObject();
                result.put("name", JsonUtils.getStringInfo(t, "jsxm"));
                result.put("phone", JsonUtils.getStringInfo(t, "lxdh"));
                result.put("email", JsonUtils.getStringInfo(t, "dzyx"));
                result.put("detail", JsonUtils.getStringInfo(t, "jsjj"));
                result.put("school", JsonUtils.getStringInfo(t, "glyxmc"));
                result.put("gender", JsonUtils.getStringInfo(t, "xbm"));
                result.put("id",teacherId);
                return result;
            } catch (JsonSyntaxException e) {
                throw JWException.getFormatErrorException();
            }
        } catch (IOException e) {
            throw JWException.getConnectErrorExpection();
        }
    }


    public HashMap<String, String> getCookies() {
        return cookies;
    }

    public void loadCookies(HashMap<? extends String, ? extends String> hm) {
        cookies.clear();
        cookies.putAll(hm);
    }

}
