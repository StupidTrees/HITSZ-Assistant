package com.stupidtree.hita.online;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobGeoPoint;

public class Topic extends BmobObject implements Comparable {
    String name;
    String description;
    HITAUser host;
    String cover;
    String type;
    BmobGeoPoint location;
    int power;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public HITAUser getHost() {
        return host;
    }

    public void setHost(HITAUser host) {
        this.host = host;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    @Override
    public int compareTo(Object o) {
        Topic o1 = this;
        Topic o2 = (Topic) o;
        int power1 = 1, power2 = 1;
        switch (o1.getType()) {
            case "basic":
                power1 = 0;
                break;
            case "high":
                power1 = 100;
                break;
            case "normal":
                power1 = 200;
                break;
            case "low":
                power1 = 300;
                break;
        }
        switch (o2.getType()) {
            case "basic":
                power2 = 0;
                break;
            case "high":
                power2 = 100;
                break;
            case "normal":
                power2 = 200;
                break;
            case "low":
                power2 = 300;
                break;
        }
        return (power1 + o1.getPower()) - (power2 + o2.getPower());
    }
}
