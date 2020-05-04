package com.stupidtree.hita.online;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobRelation;

public class Comment extends BmobObject {
    private HITAUser from;
    private HITAUser toUser;
    private Comment to;
    private Post post;
    private String type;
    private String content;
    private BmobRelation reply;
    private boolean read;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public HITAUser getFrom() {
        return from;
    }

    public void setFrom(HITAUser from) {
        this.from = from;
    }


    public BmobRelation getReply() {
        return reply;
    }

    public void setReply(BmobRelation reply) {
        this.reply = reply;
    }

    public Comment getTo() {
        return to;
    }

    public void setTo(Comment to) {
        this.to = to;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public HITAUser getToUser() {
        return toUser;
    }

    public void setToUser(HITAUser toUser) {
        this.toUser = toUser;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
