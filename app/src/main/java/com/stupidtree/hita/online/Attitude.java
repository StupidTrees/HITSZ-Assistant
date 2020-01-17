package com.stupidtree.hita.online;

import android.util.Log;

import androidx.annotation.WorkerThread;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.datatype.BmobRelation;

import static com.stupidtree.hita.HITAApplication.CurrentUser;

public class Attitude extends BmobObject {

    String title;
    BmobRelation upUser;
    //List<String> upUser;
    BmobRelation downUser;
    int up;
    int down;

    public Attitude(String title)
    {
        this.title = title;
        upUser = new BmobRelation();
        downUser = new BmobRelation();
        up = down = 0;
    }

    public Attitude(Attitude a){
        this.title = a.getTitle();
        setUp(a.getUp());
        setDown(a.getDown());
        setDownUser(a.getDownUser());
        setUpUser(a.getUpUser());
        setObjectId(a.getObjectId());
    }

    public void thumUp(HITAUser user){
        if(upUser==null) upUser = new BmobRelation();
        if(downUser==null) downUser = new BmobRelation();
        upUser.add(user);
        up++;
    }


    public void thumDown(HITAUser user){
        if(upUser==null) upUser = new BmobRelation();
        if(downUser==null) downUser = new BmobRelation();
        downUser.add(user);
        down++;
    }

    public int getUp() {
        return up;
    }

    public void setUp(int up) {
        this.up = up;
    }

    public int getDown() {
        return down;
    }

    public void setDown(int down) {
        this.down = down;
    }

    @WorkerThread
    public boolean voted(HITAUser user){
        BmobQuery<HITAUser> bq = new BmobQuery<>();
        BmobQuery<HITAUser> bq2 = new BmobQuery<>();
        bq.addWhereRelatedTo("upUser",new BmobPointer(this));
        List<HITAUser> uR = bq.findObjectsSync(HITAUser.class);
        Log.e("result_u", String.valueOf(uR));

        if(uR!=null&&uR.size()>0) {
            for(HITAUser h:uR) if(h.getObjectId().equals(user.getObjectId())) return true;
        }

        bq2.addWhereRelatedTo("downUser",new BmobPointer(this));
       // bq2.addWhereEqualTo("objectId",user.getObjectId());
       //Log.e("query", String.valueOf(bq2.getWhere()));
        List<HITAUser> uD = bq2.findObjectsSync(HITAUser.class);
        Log.e("result_d", String.valueOf(uD));
        if(uD!=null&&uD.size()>0) {
            for(HITAUser h:uD) if(h.getObjectId().equals(user.getObjectId())) return true;
        }

        return false;
        //return upUser.getObjects().contains(new BmobPointer(user))||downUser.getObjects().contains(new BmobPointer(user));
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BmobRelation getUpUser() {
        return upUser;
    }

    public void setUpUser(BmobRelation upUser) {
        this.upUser = upUser;
    }

    public BmobRelation getDownUser() {
        return downUser;
    }

    public void setDownUser(BmobRelation downUser) {
        this.downUser = downUser;
    }
}
