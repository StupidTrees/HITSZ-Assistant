package com.stupidtree.hita.hita;

import androidx.annotation.NonNull;

public class Term {
    String tag;
    String content;
    int indexInSentence;
    int priority;

    public int getPriority() {
        return priority;
    }

    public int getIndexInSentence() {
        return indexInSentence;
    }

    public Term setIndexInSentence(int indexInSentence) {
        this.indexInSentence = indexInSentence;
        return this;
    }
    public Term setPriority(int pr) {
        this.priority = pr;
        return this;
    }
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Term( String content,String tag) {
        this.tag = tag;
        this.content = content;
    }

    @NonNull
    @Override
    public String toString() {
        return "["+content+":"+tag+"]";
    }
}
