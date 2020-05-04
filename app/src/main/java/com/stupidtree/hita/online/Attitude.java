package com.stupidtree.hita.online;

import android.util.Log;

import androidx.annotation.WorkerThread;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.datatype.BmobRelation;

public class Attitude extends BmobObject {


    private String title;
    private BmobRelation upUser;
    //List<String> upUser;
    private BmobRelation downUser;
    private HITAUser author;
    private int up;
    private int down;

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


    public HITAUser getAuthor() {
        return author;
    }

    public void setAuthor(HITAUser author) {
        this.author = author;
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
    public String voted(HITAUser user){

        BmobQuery<HITAUser> up1 = new BmobQuery<>();
        up1.addWhereRelatedTo("upUser", new BmobPointer(this));
        BmobQuery<HITAUser> up2 = new BmobQuery<>();
        up2.addWhereEqualTo("objectId", user.getObjectId());
        List<BmobQuery<HITAUser>> upCond = new ArrayList<>();
        upCond.add(up1);
        upCond.add(up2);
        BmobQuery<HITAUser> upFinal = new BmobQuery<HITAUser>();
        upFinal.and(upCond);
        List<HITAUser> uR = upFinal.findObjectsSync(HITAUser.class);
        Log.e("result_u", String.valueOf(uR));
        if (uR != null && uR.size() > 0) return "up";


        BmobQuery<HITAUser> down1 = new BmobQuery<>();
        down1.addWhereRelatedTo("downUser", new BmobPointer(this));
        BmobQuery<HITAUser> down2 = new BmobQuery<>();
        down2.addWhereEqualTo("objectId", user.getObjectId());
        List<BmobQuery<HITAUser>> downCond = new ArrayList<>();
        downCond.add(down1);
        downCond.add(down2);
        BmobQuery<HITAUser> downFinal = new BmobQuery<HITAUser>();
        downFinal.and(downCond);
        List<HITAUser> uDown = downFinal.findObjectsSync(HITAUser.class);
        Log.e("result_u", String.valueOf(uDown));
        if (uDown != null && uDown.size() > 0) return "down";


//        if(uR!=null&&uR.size()>0) {
//            for(HITAUser h:uR) if(h.getObjectId().equals(user.getObjectId())) return "up";
//        }
//
//        bq2.addWhereRelatedTo("downUser",new BmobPointer(this));
//       // bq2.addWhereEqualTo("objectId",user.getObjectId());
//       //Log.e("query", String.valueOf(bq2.getWhere()));
//        List<HITAUser> uD = bq2.findObjectsSync(HITAUser.class);
//      //  Log.e("result_d", String.valueOf(uD));
//        if(uD!=null&&uD.size()>0) {
//            for(HITAUser h:uD) if(h.getObjectId().equals(user.getObjectId())) return "down";
//        }

        return "none";
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    private BmobRelation getUpUser() {
        return upUser;
    }

    private void setUpUser(BmobRelation upUser) {
        this.upUser = upUser;
    }

    private BmobRelation getDownUser() {
        return downUser;
    }

    private void setDownUser(BmobRelation downUser) {
        this.downUser = downUser;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attitude attitude = (Attitude) o;
        return up == attitude.up &&
                down == attitude.down &&
                Objects.equals(getObjectId(), attitude.getObjectId());
    }

}
