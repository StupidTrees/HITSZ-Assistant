package com.stupidtree.hita.online;

import androidx.annotation.WorkerThread;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobRelation;

public class Post extends BmobObject {
    private HITAUser author;
    private String title;
    private String content;
    private List<String> images;
    private Topic topic;
    private int likeNum;
    private BmobRelation likes;
    private BmobRelation comments;

    public HITAUser getAuthor() {
        return author;
    }

    public void setAuthor(HITAUser author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getImages() {
        return images == null ? new ArrayList<String>() : images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    @WorkerThread
    public void likeSync(HITAUser user, boolean like) {
        BmobRelation br = new BmobRelation();
        if (like) br.add(user);
        else br.remove(user);
        Post temp = new Post();
        temp.setObjectId(getObjectId());
        temp.setLikeNum(getLikeNum() + (like ? 1 : -1));
        temp.setLikes(br);
        temp.updateSync();
    }

    public int getLikeNum() {
        return likeNum;
    }

    public void setLikeNum(int likeNum) {
        this.likeNum = likeNum;
    }

    public BmobRelation getLikes() {
        return likes;
    }

    public void setLikes(BmobRelation likes) {
        this.likes = likes;
    }

    public BmobRelation getComments() {
        return comments;
    }

    public void setComments(BmobRelation comments) {
        this.comments = comments;
    }
}
