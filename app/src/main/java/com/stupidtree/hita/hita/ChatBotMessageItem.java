package com.stupidtree.hita.hita;


import com.stupidtree.hita.core.timetable.EventItem;
import com.stupidtree.hita.core.timetable.Task;

import java.util.List;

public class ChatBotMessageItem {
    public static final int MSG_TYPE_RGIHT = -11;
    public static final int MSG_TYPE_LEFT = -10;
    public int type;
    public String message = null;
    public String hint = null;
    String imageURI = null;
    public List<EventItem> courseList = null;
    public List<Task> taskList = null;
    public ChatBotMessageItem(int type, String message){
        this.message = message;
        this.type = type;
    }

    public ChatBotMessageItem(int type, String message,List<EventItem> courseList){
        this.message = message;
        this.type = type;
        this.courseList = courseList;
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
        this.courseList = courseList;
    }

    public void setTaskList(List<Task> taskList){
        this.taskList = taskList;
    }

    public String getImageURI() {
        return imageURI;
    }

    public void setImageURI(String imageURI) {
        this.imageURI = imageURI;
    }
}
