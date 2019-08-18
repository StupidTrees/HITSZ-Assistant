package com.stupidtree.hita.online;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cn.bmob.v3.BmobObject;

public class BannerItem extends BmobObject {
    String imageUri;
    String title;
    String subtitle;
    String action;

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

    public void setAction(String action) {
        this.action = action;
    }
}
