package com.stupidtree.hita.online;

import cn.bmob.v3.BmobObject;

public class RateUser extends BmobObject {
    HITAUser hitaUser;
    String rateObjectId;

    public HITAUser getHitaUser() {
        return hitaUser;
    }

    public void setHitaUser(HITAUser hitaUser) {
        this.hitaUser = hitaUser;
    }

    public String getRateObjectId() {
        return rateObjectId;
    }

    public void setRateObjectId(String rateObjectId) {
        this.rateObjectId = rateObjectId;
    }
}
