package com.stupidtree.hita.online;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobRelation;

public class UserRelation extends BmobObject {
    private HITAUser user;
    private BmobRelation fans;
    private BmobRelation following;
//    @WorkerThread
//    public void followSync(HITAUser other){
//        if(other==null) return;
//        BmobRelation br = new BmobRelation();
//        br.add(other);
//        setFollowing(br);
//        UserRelation otherAv = other.getUserRelationAvatar();
//        otherAv.followedBySync(user);
//        updateSync();
//    }
//    @WorkerThread
//    public void followHalfSync(HITAUser other){
//        if(other==null) return;
//        BmobRelation br = new BmobRelation();
//        br.add(other);
//        setFollowing(br);
//        update(new UpdateListener() {
//            @Override
//            public void done(BmobException e) {
//
//            }
//        });
//        UserRelation otherAv = other.getUserRelationAvatar();
//        otherAv.followedBy(user);
//    }
//
//    @WorkerThread
//    public void followedBySync(HITAUser other){
//        if(other==null) return;
//        BmobRelation br = new BmobRelation();
//        br.add(other);
//        setFans(br);
//        updateSync();
//    }
//    public void followedBy(HITAUser other){
//        if(other==null) return;
//        BmobRelation br = new BmobRelation();
//        br.add(other);
//        setFans(br);
//        update(new UpdateListener() {
//            @Override
//            public void done(BmobException e) {
//
//            }
//        });
//    }
//    @WorkerThread
//    public void unFollowSync(HITAUser other){
//        if(other==null) return;
//        BmobRelation br = new BmobRelation();
//        br.remove(other);
//        setFollowing(br);
//        UserRelation otherAv = other.getUserRelationAvatar();
//        otherAv.unFollowedBySync(user);
//        updateSync();
//    }
//
//    public void unFollowedBySync(HITAUser other){
//        if(other==null) return;
//        BmobRelation br = new BmobRelation();
//        br.remove(other);
//        setFans(br);
//        updateSync();
//    }


    public HITAUser getUser() {
        return user;
    }

    public void setUser(HITAUser user) {
        this.user = user;
    }

    public BmobRelation getFans() {
        return fans;
    }

    public void setFans(BmobRelation fans) {
        this.fans = fans;
    }

    public BmobRelation getFollowing() {
        return following;
    }

    public void setFollowing(BmobRelation following) {
        this.following = following;
    }
}
