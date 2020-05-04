package com.stupidtree.hita.online;

import cn.bmob.v3.BmobObject;

public class Like extends BmobObject {
    private HITAUser from;
    private Post to;
    private boolean read;

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public HITAUser getFrom() {
        return from;
    }

    public void setFrom(HITAUser from) {
        this.from = from;
    }

    public Post getTo() {
        return to;
    }

    public void setTo(Post to) {
        this.to = to;
    }
}
