package com.stupidtree.hita.online;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stupidtree.hita.timetable.Curriculum;
import com.stupidtree.hita.timetable.timetable.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.bmob.v3.BmobObject;

public class Bmob_User_Data extends BmobObject {
    String CurriculumsText;
    String TasksText;
    HITAUser hitaUser;
    String timetableText;
    public Bmob_User_Data(List<Curriculum> ccs,ArrayList<TimeTable_upload_helper> tth,ArrayList<Task> tts){
//        StringBuilder sb = new StringBuilder();
//        for(int i=0;i<ccs.size();i++){
//            String rex=(i==ccs.size()-1)?"":":::";
//            sb.append(ccs.get(i).toString()).append(rex);
//        }
//        CurriculumsText = sb.toString();
        JsonParser jp = new JsonParser();
        JsonObject jo = new JsonObject();
        for(Curriculum c:ccs){
            jo.add(c.getCurriculumCode(),jp.parse(c.toString()));
        }
        CurriculumsText = jo.toString();

        //StringBuilder sb2 = new StringBuilder();
//        for(int i=0;i<tth.size();i++){
//            String rex = i==tth.size()-1?"":"///";
//            sb2.append(tth.get(i)).append(rex);
//        }
        JsonArray ja = new JsonArray();
        for(TimeTable_upload_helper x:tth){
            ja.add(x.toString());
        }
        timetableText = ja.toString();
//        StringBuilder sb3 = new StringBuilder();
//        for(int i=0;i<tts.size();i++){
//            String rex=(i==tts.size()-1)?"":"///";
//            sb3.append(tts.get(i).toString()).append(rex);
//        }
        JsonArray ja2 = new JsonArray();
        for(Task t:tts){
            ja2.add(t.toString());
        }
        //TasksText = sb3.toString();
        TasksText = ja2.toString();
    }

   public ArrayList<Curriculum> getCurriculumsFromText(){
        ArrayList list = new ArrayList();
//        String[] tt = CurriculumsText.split(":::");
//        for(String s:tt){
//            list.add(new Curriculum(s));
//        }
       JsonParser jp = new JsonParser();
       for(Map.Entry e:jp.parse(CurriculumsText).getAsJsonObject().entrySet()){
           list.add(new Curriculum(e.getValue().toString()));
       }
        return list;
    }

    public List<TimeTable_upload_helper> getTimeTableHelpersFromString(){
        List<TimeTable_upload_helper> res = new ArrayList<>();
        if(TextUtils.isEmpty(timetableText)) return res; //一定注意空字符串不能split！！！
       // for(String x:timetableText.split("///")) res.add(new TimeTable_upload_helper(x));
        Gson gson = new Gson();
        for(JsonElement je:new JsonParser().parse(timetableText).getAsJsonArray()){

            res.add(gson.fromJson(je.getAsString(),TimeTable_upload_helper.class));
        }
        return res;
    }
    public ArrayList<Task> getTasksFromText(){
        ArrayList list = new ArrayList();
        if(TextUtils.isEmpty(TasksText)) return list;
        //String[] tt =  TasksText.split("///");
       //  Log.e("TaskText,tt",TasksText+"|||"+ String.valueOf(tt));
        Gson gson =new Gson();
        JsonArray ja = new JsonParser().parse(TasksText).getAsJsonArray();
        for(JsonElement je:ja){
            list.add(gson.fromJson(je.getAsString(),Task.class));
        }
        return list;
    }
    public HITAUser getHitaUser() {
        return hitaUser;
    }
    public void setHitaUser(HITAUser hitaUser) {
        this.hitaUser = hitaUser;
    }

    @Override
    public String toString() {
        return timetableText+",,,"+TasksText;
    }
}
