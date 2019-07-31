package com.stupidtree.hita.online;

import cn.bmob.v3.BmobObject;

public class BannerItem extends BmobObject {
    String imageUri;
    String title;
    String intent;

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

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }
}
