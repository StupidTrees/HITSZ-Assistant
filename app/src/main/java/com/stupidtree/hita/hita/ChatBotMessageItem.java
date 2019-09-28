package com.stupidtree.hita.hita;


import com.stupidtree.hita.R;
import com.stupidtree.hita.core.Subject;
import com.stupidtree.hita.core.TimeTable;
import com.stupidtree.hita.core.timetable.EventItem;
import com.stupidtree.hita.core.timetable.Task;
import com.stupidtree.hita.online.Teacher;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatBotMessageItem {
    public static final int MSG_TYPE_RGIHT = -11;
    public static final int MSG_TYPE_LEFT = -10;
    public int type;
    public String message = null;
    public String hint = null;
    private String imageURI = null;
    public List<HashMap> list = null;
    public List listRes = null;

    public ChatBotMessageItem(int type, String message){
        this.message = message;
        this.type = type;
    }


    public void setMessage(String text){
        message = text;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public void setCourseList(List<EventItem> courseList){
        list = new ArrayList<>();
        listRes = courseList;
        for(EventItem ei:courseList){
            HashMap m = new HashMap();
            m.put("title", TextTools.words_time_DOW[ei.DOW-1]+" "+ei.mainName);
            m.put("type","event");
            int icon = R.drawable.ic_chatbot_course;
            switch(ei.eventType){
                case TimeTable.TIMETABLE_EVENT_TYPE_COURSE:icon = R.drawable.ic_chatbot_course ;break;
                case TimeTable.TIMETABLE_EVENT_TYPE_ARRANGEMENT:icon = R.drawable.ic_chatbot_arrangement;break;
                case TimeTable.TIMETABLE_EVENT_TYPE_DEADLINE:icon = R.drawable.ic_chatbot_deadline;break;
                case TimeTable.TIMETABLE_EVENT_TYPE_REMIND:icon = R.drawable.ic_chatbot_remind;break;
                case TimeTable.TIMETABLE_EVENT_TYPE_EXAM:icon = R.drawable.ic_chatbot_exam;break;
            }
            m.put("icon",icon);
            list.add(m);
        }
    }

    public void setTaskList(List<Task> taskList){
        list = new ArrayList<>();
        listRes = taskList;
        for(Task t :taskList){
            HashMap m = new HashMap();
            m.put("title", t.name);
            m.put("type","task");
            int icon = R.drawable.ic_round;
            m.put("icon",icon);
            list.add(m);
        }
    }

    public void setTeacherList(List<Teacher> teacherList){
        list = new ArrayList<>();
        listRes = teacherList;
        for(Teacher t :teacherList){
            HashMap m = new HashMap();
            m.put("title", t.getName());
            m.put("type","teacher");
            int icon = R.drawable.ic_dlg_tt_teacher;
            m.put("icon",icon);
            list.add(m);
        }
    }

    public void setSubjectList(List<Subject> subjectList){
        list = new ArrayList<>();
        listRes = subjectList;
        for(Subject t :subjectList){
            HashMap m = new HashMap();
            m.put("title", t.name);
            m.put("type","subject");
            int icon = R.drawable.ic_menu_subject;
            m.put("icon",icon);
            list.add(m);
        }
    }
    public String getImageURI() {
        return imageURI;
    }

    public void setImageURI(String imageURI) {
        this.imageURI = imageURI;
    }


}
