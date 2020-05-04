package com.stupidtree.hita.online;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobRelation;

public class BannerItem extends BmobObject {
    private String imageUri;
    private String title;
    private String subtitle;
    private String action;
    private String type;
    private String buttonText;
    private float height;
    private BmobRelation clickUser;
    private int showBeforeVersion;
    private int showAfterVersion;

    BannerItem(){
        clickUser = new BmobRelation();
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public JsonObject getAction() {
        JsonObject jo = new JsonParser().parse(action).getAsJsonObject();
        return jo;
    }

    public String getButtonText() {
        return buttonText;
    }

    public float getHeight() {
        return height;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public BmobRelation getClickUser() {
        return clickUser;
    }

    public void addClickUser(HITAUser user) {
        clickUser.add(user);
    }

    public String getType() {
        return type;
    }

    public int getShowBeforeVersion() {
        return showBeforeVersion;
    }

    public int getShowAfterVersion() {
        return showAfterVersion;
    }
}
