package com.stupidtree.hita.online;

import cn.bmob.v3.BmobObject;

public class SearchItem extends BmobObject {
    String content;
    String type;
    HITAUser user;

    public SearchItem(String content, String type, HITAUser user) {
        this.content = content;
        this.type = type;
        this.user = user;
    }
}
