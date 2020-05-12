package com.stupidtree.hita.online;

import android.content.ContentResolver;
import android.database.Cursor;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.google.gson.Gson;
import com.stupidtree.hita.timetable.packable.Curriculum;
import com.stupidtree.hita.timetable.packable.EventItemHolder;
import com.stupidtree.hita.timetable.packable.Subject;
import com.stupidtree.hita.timetable.packable.Task;
import com.stupidtree.hita.util.DeflaterUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cn.bmob.v3.BmobObject;

import static com.stupidtree.hita.timetable.TimetableCore.COURSE;
import static com.stupidtree.hita.timetable.TimetableCore.uri_subject;
import static com.stupidtree.hita.timetable.TimetableCore.uri_task;
import static com.stupidtree.hita.timetable.TimetableCore.uri_timetable;

public class UserData {
    private ArrayList<EventItemHolder> events;
    private ArrayList<Task> tasks;
    private ArrayList<Subject> subjects;
    private ArrayList<Curriculum> curriculum;
    private ContentResolver resolver;

    private UserData(ContentResolver resolver){
        this.resolver = resolver;
        events = new ArrayList<>();
        tasks = new ArrayList<>();
        curriculum = new ArrayList<>();
        subjects = new ArrayList<>();
    }
    public static UserData create(ContentResolver contentResolver){
        return new UserData(contentResolver);
    }
    @WorkerThread
    public UserData loadTimetableData(){
        events.clear();
        final Cursor c = resolver.query(uri_timetable,  null, null, null, null, null);
        while (c != null && c.moveToNext()) {
            EventItemHolder eih = new EventItemHolder(c);
            if (eih.eventType == COURSE)
                continue;
            else events.add(eih);
        }
        if (c != null) {
            c.close();
        }
        return this;
    }
    @WorkerThread
    public UserData loadTaskData(){
        Cursor c2 = resolver.query(uri_task, null,  null, null, null, null);
        tasks.clear();
        while (c2 != null && c2.moveToNext()) {
            Task t = new Task(c2);
            if (!t.isFinished() && t.getType() != Task.TYPE_DYNAMIC) tasks.add(t);
        }
        if (c2 != null) {
            c2.close();
        }
        return this;
    }
    @WorkerThread
    public UserData loadSubjectData(){
        Cursor c2 = resolver.query(uri_subject, null, null, null, null, null);
        subjects.clear();
        while (c2 != null && c2.moveToNext()) {
            Subject s = new Subject(c2);
            subjects.add(s);
        }
        if (c2 != null) {
            c2.close();
        }
        return this;
    }
    @WorkerThread
    public UserData loadCurriculumData(List<Curriculum> curriculms){
        this.curriculum.clear();
        for (Curriculum c : curriculms) {
            c.generateCurriculumText();
            curriculum.add(c);
        }
        //  curriculum.addAll(curriculms);
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
            Subject k = gson.fromJson(s, Subject.class);
            if (TextUtils.isEmpty(k.getUUID())) {
                k.setUUID(UUID.randomUUID().toString());
            }
            subjects.add(k);
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

        @NonNull
        @Override
        public String toString() {
            return getSubjectData() + "\n" + getCurriculumData();
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
            String decoded = DeflaterUtils.unzipString(subjectData);
            //Log.e("科目：",decoded);
            return decoded;
        }

        @WorkerThread
        public String getCurriculumData() {
            return DeflaterUtils.unzipString(curriculumData);
        }



    }
}
