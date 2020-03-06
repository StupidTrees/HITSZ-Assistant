package com.stupidtree.hita.online;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.WorkerThread;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.stupidtree.hita.timetable.Curriculum;
import com.stupidtree.hita.timetable.CurriculumCreator;
import com.stupidtree.hita.timetable.Subject;
import com.stupidtree.hita.timetable.timetable.EventItem;
import com.stupidtree.hita.timetable.timetable.EventItemHolder;
import com.stupidtree.hita.timetable.timetable.Task;
import com.stupidtree.hita.util.DeflaterUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.bmob.v3.BmobObject;

import static com.stupidtree.hita.timetable.TimetableCore.TIMETABLE_EVENT_TYPE_COURSE;

public class UserData {
    private ArrayList<EventItemHolder> events;
    private ArrayList<Task> tasks;
    private ArrayList<Subject> subjects;
    private ArrayList<Curriculum> curriculum;
    private SQLiteDatabase database;
//    public UserData(List<Curriculum> ccs, ArrayList<TimeTable_upload_helper> tth, ArrayList<Task> tts){
////        StringBuilder sb = new StringBuilder();
////        for(int i=0;i<ccs.size();i++){
////            String rex=(i==ccs.size()-1)?"":":::";
////            sb.append(ccs.get(i).toString()).append(rex);
////        }
////        CurriculumsText = sb.toString();
//        JsonParser jp = new JsonParser();
//        JsonObject jo = new JsonObject();
//        for(Curriculum c:ccs){
//            jo.add(c.getCurriculumCode(),jp.parse(c.toString()));
//        }
//        CurriculumsText = jo.toString();
//
//        //StringBuilder sb2 = new StringBuilder();
////        for(int i=0;i<tth.size();i++){
////            String rex = i==tth.size()-1?"":"///";
////            sb2.append(tth.get(i)).append(rex);
////        }
//        JsonArray ja = new JsonArray();
//        for(TimeTable_upload_helper x:tth){
//            ja.add(x.toString());
//        }
//        timetableText = ja.toString();
////        StringBuilder sb3 = new StringBuilder();
////        for(int i=0;i<tts.size();i++){
////            String rex=(i==tts.size()-1)?"":"///";
////            sb3.append(tts.get(i).toString()).append(rex);
////        }
//        JsonArray ja2 = new JsonArray();
//        for(Task t:tts){
//            ja2.add(t.toString());
//        }
//        //TasksText = sb3.toString();
//        TasksText = ja2.toString();
//    }



    public UserData(SQLiteDatabase database){
        this.database = database;
        events = new ArrayList<>();
        tasks = new ArrayList<>();
        curriculum = new ArrayList<>();
        subjects = new ArrayList<>();
    }
    public static UserData create(SQLiteDatabase sqLiteDatabase){
        UserData data = new UserData(sqLiteDatabase);
        return data;
    }
    @WorkerThread
    public UserData loadTimetableData(){
        events.clear();
        final Cursor c = database.query("timetable", null, null, null, null, null, null);
        while (c.moveToNext()) {
            EventItemHolder eih = new EventItemHolder(c);
            if (eih.eventType == TIMETABLE_EVENT_TYPE_COURSE)
                continue;
            else events.add(eih);
        }
        c.close();
        return this;
    }
    @WorkerThread
    public UserData loadTaskData(){
        Cursor c2 = database.query("task", null, null, null, null, null, null);
        tasks.clear();
        while (c2.moveToNext()) {
            Task t = new Task(c2);
            if (!t.isFinished() && t.getType() != Task.TYPE_DYNAMIC) tasks.add(t);
        }
        c2.close();
        return this;
    }
    @WorkerThread
    public UserData loadSubjectData(){
        Cursor c2 = database.query("subject", null, null, null, null, null, null);
        subjects.clear();
        while (c2.moveToNext()) {
            Subject s = new Subject(c2);
            subjects.add(s);
        }
        c2.close();
        return this;
    }
    @WorkerThread
    public UserData loadCurriculumData(List<Curriculum> curriculms){
        this.curriculum.clear();
        curriculum.addAll(curriculms);
        return this;
    }

    @WorkerThread
    public UserData loadData(UserDataCloud cloudData){
        tasks.clear();
        events.clear();
        curriculum.clear();
        Gson gson = new Gson();
        List<String> template = new ArrayList<>();
        List<String> eventsList = gson.fromJson(cloudData.getTimetableData(),template.getClass());
        List<String> tasksList = gson.fromJson(cloudData.getTaskData(),template.getClass());
        List<String> curriculumList =gson.fromJson(cloudData.getCurriculumData(),template.getClass());
        List<String> subjectList =gson.fromJson(cloudData.getSubjectData(),template.getClass());

        for(String s:eventsList){
            events.add(gson.fromJson(s,EventItemHolder.class));
        }
        for(String s:tasksList){
            tasks.add(gson.fromJson(s,Task.class));
        }
        for(String s:subjectList){
            subjects.add(gson.fromJson(s,Subject.class));
           // Log.e("name",subjects.get(subjects.size()-1).getName());
        }
        for(String s:curriculumList){
            curriculum.add(gson.fromJson(s,Curriculum.class));
        }
      //  Log.e("subject_list",":::"+cloudData.getSubjectData());
//        tasks.addAll(gson.fromJson(cloudData.getTaskData(),tasks.getClass()));
//        events.addAll(gson.fromJson(cloudData.getTimetableData(),events.getClass()));
//        curriculum.addAll(gson.fromJson(cloudData.getTimetableData(),events.getClass()));
        return this;
    }
//    public ArrayList<Curriculum> getCurriculumsFromText(){
//        ArrayList list = new ArrayList();
//       JsonParser jp = new JsonParser();
//       for(Map.Entry e:jp.parse(CurriculumsText).getAsJsonObject().entrySet()){
//           list.add(new Curriculum(e.getValue().toString()));
//       }
//        return list;
//    }
//    public List<TimeTable_upload_helper> getTimeTableHelpersFromString(){
//        List<TimeTable_upload_helper> res = new ArrayList<>();
//        if(TextUtils.isEmpty(timetableText)) return res;
//          Gson gson = new Gson();
//        for(JsonElement je:new JsonParser().parse(timetableText).getAsJsonArray()){
//            res.add(gson.fromJson(je.getAsString(),TimeTable_upload_helper.class));
//        }
//        return res;
//    }
//    public ArrayList<Task> getTasksFromText(){
//        ArrayList list = new ArrayList();
//        if(TextUtils.isEmpty(TasksText)) return list;
//        //String[] tt =  TasksText.split("///");
//       //  Log.e("TaskText,tt",TasksText+"|||"+ String.valueOf(tt));
//        Gson gson =new Gson();
//        JsonArray ja = new JsonParser().parse(TasksText).getAsJsonArray();
//        for(JsonElement je:ja){
//            list.add(gson.fromJson(je.getAsString(),Task.class));
//        }
//        return list;
//


    @WorkerThread
    public UserDataCloud getPreparedCloudData(HITAUser user){
        UserDataCloud udc = new UserDataCloud(user);
        return udc.prepareData();
    }

    public ArrayList<EventItemHolder> getEvents() {
        return events;
    }

    public ArrayList<Task> getTasks() {
        return tasks;
    }

    public ArrayList<Curriculum> getCurriculum() {
        return curriculum;
    }

    public ArrayList<Subject> getSubjects() {
        return subjects;
    }

    public class UserDataCloud extends BmobObject{
        HITAUser user;
        String timetableData;
        String taskData;
        String subjectData;
        String curriculumData;
        UserDataCloud(HITAUser user){
            this.user = user;
        }

        public UserDataCloud cloneWithNewUser(HITAUser user){
            UserDataCloud newD = new UserDataCloud(user);
            newD.curriculumData = this.curriculumData;
            newD.subjectData = this.subjectData;
            newD.taskData = this.taskData;
            newD.timetableData = this.timetableData;
            return newD;
        }
//        UserDataCloud(UserDataCloud other,HITAUser user){
//            this.user = user;
//            timetableData = other.timetableData;
//            taskData = other.taskData;
//            subjectData = other.subjectData;
//            curriculumData = other.curriculumData;
//        }
        @WorkerThread
        public UserDataCloud prepareData(){
            Gson gson = new Gson();
            List<String> eventsList = new ArrayList<>();
            for(EventItemHolder eih:events){
                eventsList.add(eih.toString());
            }
            List<String> tasksList = new ArrayList<>();
            for(Task t:tasks){
                tasksList.add(t.toString());
            }
            List<String> subjectList = new ArrayList<>();
            for(Subject t:subjects){
                subjectList.add(t.toString());
            }
            List<String> curriculumList = new ArrayList<>();
            for(Curriculum c:curriculum){
                curriculumList.add(c.toString());
            }

            timetableData = DeflaterUtils.zipString(gson.toJson(eventsList));
            taskData = DeflaterUtils.zipString(gson.toJson(tasksList));
            curriculumData = DeflaterUtils.zipString(gson.toJson(curriculumList));
            subjectData = DeflaterUtils.zipString(gson.toJson(subjectList));
            return this;
        }

        @WorkerThread
        public String getTimetableData() {
            return DeflaterUtils.unzipString(timetableData);
        }
        @WorkerThread
        public String getTaskData() {
            return DeflaterUtils.unzipString(taskData);
        }

        @WorkerThread
        public String getSubjectData() {
            return DeflaterUtils.unzipString(subjectData);
        }

        @WorkerThread
        public String getCurriculumData() {
            return DeflaterUtils.unzipString(curriculumData);
        }



    }
}
