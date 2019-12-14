package com.stupidtree.hita.online;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobObject;

public class ChatMessage extends BmobObject {
    String queryText;
    String answer;
    String tag;
    List<String> queryArray;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public List<String> getQueryArray() {
        return queryArray;
    }

    public void setQueryArray(List<String> queryArray) {
        this.queryArray = queryArray;
    }

    public String getQueryText() {
        return queryText;
    }

    public void setQueryText(String queryText) {
        this.queryText = queryText;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
