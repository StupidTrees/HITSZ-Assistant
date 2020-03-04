package com.stupidtree.hita.online;

import cn.bmob.v3.BmobObject;

public class errorTableText extends BmobObject {
    String tableText;
    String correction;
    Exception exception;

    public errorTableText(String tableText, Exception exception) {
        this.tableText = tableText;
        this.exception = exception;
    }

}
