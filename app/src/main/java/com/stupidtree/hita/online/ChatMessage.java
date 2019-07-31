package com.stupidtree.hita.online;

import cn.bmob.v3.BmobObject;

public class ChatMessage extends BmobObject {
    String queryText;
    String answer;

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
