package com.stupidtree.hita.online;


import android.text.TextUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import cn.bmob.v3.BmobUser;

public class HITAUser extends BmobUser {


    public static final String SHOWN_TO_NOBODY = "shown_to_no8d";
    public static final String SHOWN_TO_FOLLOWING = "shown_to_following";
    public static final String SHOWN_TO_AV8D = "shown_to_av8d";
    private String school;
    private String realname;
    private String nick;
    private String signature;
    private String studentnumber;
    private String avatarUri;
    private String punchInfo;
    private String usingTheme;
    private String profilePolicy;
    private int grade;

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public void setUsingTheme(String usingTheme) {
        this.usingTheme = usingTheme;
    }

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
        return getUsername() + "," + getNick() + "," + getRealname();
    }


    public String getProfilePolicy() {
        if (TextUtils.isEmpty(profilePolicy)) {
            profilePolicy = SHOWN_TO_FOLLOWING;
        }
        return profilePolicy;
    }

    public void setProfilePolicy(String profilePolicy) {
        this.profilePolicy = profilePolicy;
    }

    public int getProfilePolicyIndex() {
        switch (profilePolicy) {
            case SHOWN_TO_NOBODY:
                return 0;
            case SHOWN_TO_FOLLOWING:
                return 1;
            case SHOWN_TO_AV8D:
                return 2;
        }
        return 1;
    }

    public void setProfilePolicyIndex(int policyIndex) {
        switch (policyIndex) {
            case 0:
                this.profilePolicy = SHOWN_TO_NOBODY;
                break;
            case 1:
                this.profilePolicy = SHOWN_TO_FOLLOWING;
                break;
            case 2:
                this.profilePolicy = SHOWN_TO_AV8D;
                break;
        }
    }
//    @WorkerThread
//    public List<HITAUser> getFollowingFull() {
//        UserRelation avatar = getUserRelationAvatar();
//        BmobQuery<HITAUser> followingQ = new BmobQuery<>();
//        followingQ.addWhereRelatedTo("following", new BmobPointer(avatar));
//        if (BmobCacheMap.containsKey("following")) {
//            BmobCacheMap.remove("following");
//        } else {
//            followingQ.setCachePolicy(BmobQuery.CachePolicy.CACHE_ELSE_NETWORK);
//        }
//        return followingQ.findObjectsSync(HITAUser.class);
//    }
//    @WorkerThread
//    public List<HITAUser> getFollowingInfo() {
//        UserRelation avatar = getUserRelationAvatar();
//        BmobQuery<HITAUser> followingQ = new BmobQuery<>();
//        followingQ.addQueryKeys("nick,avatarUri,objectId");
//        followingQ.addWhereRelatedTo("following", new BmobPointer(avatar));
//        if (BmobCacheMap.containsKey("following")) {
//            BmobCacheMap.remove("following");
//        } else {
//            followingQ.setCachePolicy(BmobQuery.CachePolicy.CACHE_ELSE_NETWORK);
//        }
//        return followingQ.findObjectsSync(HITAUser.class);
//    }
//    @WorkerThread
//    public List<String> getFollowingObjectIds() {
//        try {
//            UserRelation avatar = getUserRelationAvatar();
//            BmobQuery<HITAUser> followingQ = new BmobQuery<>();
//
//            followingQ.addQueryKeys("objectId");
//            followingQ.addWhereRelatedTo("following", new BmobPointer(avatar));
//            if (BmobCacheMap.containsKey("following")) {
//                BmobCacheMap.remove("following");
//            } else {
//                followingQ.setCachePolicy(BmobQuery.CachePolicy.CACHE_ELSE_NETWORK);
//            }
//            List<HITAUser> res = followingQ.findObjectsSync(HITAUser.class);
//            List<String> resS = new ArrayList<>();
//            if(res!=null) for(HITAUser h:res) resS.add(h.getObjectId());
//            return resS;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ArrayList<>();
//        }
//    }

//    @WorkerThread
//    public boolean Following(HITAUser other) {
//        try {
//            UserRelation avatar = getUserRelationAvatar();
//            BmobQuery<HITAUser> followingQ = new BmobQuery<>();
//            followingQ.addQueryKeys("objectId");
//            followingQ.addWhereRelatedTo("following", new BmobPointer(avatar));
//            BmobQuery<HITAUser> bq2 = new BmobQuery();
//            bq2.addWhereEqualTo("objectId",other.getObjectId());
//            BmobQuery<HITAUser> fin = new BmobQuery<>();
//            fin.and(Arrays.asList(followingQ,bq2));
//            fin.addQueryKeys("objectId");
//            List<HITAUser> res = fin.findObjectsSync(HITAUser.class);
//            return res!=null&&res.size()>0;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//    @WorkerThread
//    public List<HITAUser> getFansFull(){
//        UserRelation avatar = getUserRelationAvatar();
//        BmobQuery<HITAUser> followingQ = new BmobQuery<>();
//        if(BmobCacheMap.containsKey("fans")){
//            BmobCacheMap.remove("fans");
//        }else{
//            followingQ.setCachePolicy(BmobQuery.CachePolicy.CACHE_ELSE_NETWORK);
//        }
//        followingQ.addWhereRelatedTo("fans", new BmobPointer(avatar));
//        return followingQ.findObjectsSync(HITAUser.class);
//    }
//    @WorkerThread
//    public List<HITAUser> getFansInfo(){
//        UserRelation avatar = getUserRelationAvatar();
//        BmobQuery<HITAUser> followingQ = new BmobQuery<>();
//        followingQ.addQueryKeys("nick,avatarUri");
//        followingQ.addWhereRelatedTo("fans", new BmobPointer(avatar));
//        if(BmobCacheMap.containsKey("fans")){
//            BmobCacheMap.remove("fans");
//        }else{
//            followingQ.setCachePolicy(BmobQuery.CachePolicy.CACHE_ELSE_NETWORK);
//        }
//        return followingQ.findObjectsSync(HITAUser.class);
//    }
//    @WorkerThread
//    public UserRelation getUserRelationAvatar() {
//        try {
//            BmobQuery<UserRelation> avatar = new BmobQuery<>();
//            avatar.addWhereEqualTo("user", new BmobPointer(this));
//            Log.e("AVATAR_has_cache", String.valueOf(avatar.hasCachedResult(UserRelation.class)));
//            avatar.setCachePolicy(BmobQuery.CachePolicy.CACHE_ELSE_NETWORK);
//            List<UserRelation> res1 = avatar.findObjectsSync(UserRelation.class);
//            if (res1 == null || res1.size() == 0) { //没有的话就创建直接保存
//                UserRelation userRelation = new UserRelation();
//                userRelation.setUser(this);
//                UserRelation newR = new UserRelation();
//                String id = userRelation.saveSync();
//                if(TextUtils.isEmpty(id)) return null;
//                newR.setObjectId(id);
//                return newR;
//            }
//            return res1.get(0);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }


    public int getHappyDays() {
        JsonObject jo;
        try {
            jo = new JsonParser().parse(punchInfo).getAsJsonObject();
        } catch (Exception e) {
            JsonObject j = new JsonObject();
            j.addProperty("happy_days", 0);
            j.addProperty("sad_days", 0);
            j.addProperty("normal_days", 0);
            punchInfo = j.toString();
            jo = j;
        }
        if (jo.has("happy_days")) return jo.get("happy_days").getAsInt();
        else return 0;


    }

    public int getNormalDays() {
        JsonObject jo;
        try {
            jo = new JsonParser().parse(punchInfo).getAsJsonObject();
        } catch (Exception e) {
            JsonObject j = new JsonObject();
            j.addProperty("happy_days", 0);
            j.addProperty("sad_days", 0);
            j.addProperty("normal_days", 0);
            punchInfo = j.toString();
            jo = j;
        }
        if (jo.has("normal_days")) return jo.get("normal_days").getAsInt();
        else return 0;


    }

    public int getSadDays() {
        JsonObject jo;
        try {
            jo = new JsonParser().parse(punchInfo).getAsJsonObject();
        } catch (Exception e) {
            JsonObject j = new JsonObject();
            j.addProperty("happy_days", 0);
            j.addProperty("sad_days", 0);
            j.addProperty("normal_days", 0);
            punchInfo = j.toString();
            jo = j;
        }
        if (jo.has("sad_days")) return jo.get("sad_days").getAsInt();
        else return 0;
    }

    public int getPunchDays() {
        JsonObject jo;
        try {
            jo = new JsonParser().parse(punchInfo).getAsJsonObject();
        } catch (Exception e) {
            JsonObject j = new JsonObject();
            j.addProperty("happy_days", 0);
            j.addProperty("sad_days", 0);
            j.addProperty("normal_days", 0);
            punchInfo = j.toString();
            jo = j;
        }
        int happy = 0, normal = 0, sad = 0;
        if (jo.has("sad_days")) sad = jo.get("sad_days").getAsInt();
        if (jo.has("normal_days")) normal = jo.get("normal_days").getAsInt();
        if (jo.has("happy_days")) sad = jo.get("happy_days").getAsInt();
        return happy + sad + normal;
    }

    public boolean hasPunch(Calendar c) {
        JsonObject jo;
        try {
            jo = new JsonParser().parse(punchInfo).getAsJsonObject();
        } catch (Exception e) {
            JsonObject j = new JsonObject();
            j.addProperty("happy_days", 0);
            j.addProperty("sad_days", 0);
            j.addProperty("normal_days", 0);
            punchInfo = j.toString();
            jo = j;
        }
        if (jo.has("punch_date")) {
            String date = jo.get("punch_date").getAsString();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String exp = sdf.format(c.getTime());
            return exp.equals(date);
        } else {
            return false;
        }

    }

    public void Punch(Calendar c, int choice) {
        JsonObject jo;
        try {
            jo = new JsonParser().parse(punchInfo).getAsJsonObject();
        } catch (Exception e) {
            JsonObject j = new JsonObject();
            j.addProperty("happy_days", 0);
            j.addProperty("sad_days", 0);
            j.addProperty("normal_days", 0);
            punchInfo = j.toString();
            jo = j;
        }
        if (!jo.has("sad_days")) jo.addProperty("sad_days", 0);
        if (!jo.has("normal_days")) jo.addProperty("normal_days", 0);
        if (!jo.has("happy_days")) jo.addProperty("happy_days", 0);

        if (choice == 0) {
            int happy = jo.get("happy_days").getAsInt();
            jo.addProperty("happy_days", happy + 1);
        } else if (choice == 2) {
            int sad = jo.get("sad_days").getAsInt();
            jo.addProperty("sad_days", sad + 1);
        } else if (choice == 1) {
            int normal = jo.get("normal_days").getAsInt();
            jo.addProperty("normal_days", normal + 1);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd", Locale.getDefault());
        jo.addProperty("punch_date", sdf.format(c.getTime()));
        punchInfo = jo.toString();
    }


}
