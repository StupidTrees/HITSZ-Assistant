package com.stupidtree.hita.online;


import cn.bmob.v3.BmobUser;

public class HITAUser extends BmobUser {


    String school;
    String realname;
    String nick;
    String signature;
    String studentnumber;
    String avatarUri;

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }


    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getStudentnumber() {
        return studentnumber;
    }

    public void setStudentnumber(String studentnumber) {
        this.studentnumber = studentnumber;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getAvatarUri() {
        return avatarUri;
    }

    public void setAvatarUri(String avatarUri) {
        this.avatarUri = avatarUri;
    }

    @Override
    public String toString() {
        return getUsername()+","+getNick()+","+getRealname();
    }
}
