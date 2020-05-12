package com.stupidtree.hita.fragments.main;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.stupidtree.hita.online.BannerItem;

import java.util.HashMap;
import java.util.Map;

import static com.stupidtree.hita.adapter.NavigationListAdapter.TYPE_NOTIFICATION;

public class NavigationCardItem implements Comparable {
    private String type_name;
    private int type;
    private int power;
    private Map<String, Object> extras;

    NavigationCardItem(int type, String type_name) {
        this.type_name = type_name;
        this.type = type;
        extras = new HashMap<>();
    }

    void putNotificationExtra(BannerItem bi) {
        extras.put("notification", bi);
    }

    public BannerItem getNotificationExtra() {
        Object o = extras.get("notification");
        if (!(o instanceof BannerItem)) return null;
        return (BannerItem) o;
    }


    private int getPower() {
        return power;
    }

    void setPower(int power) {
        this.power = power;
    }

    public String getType_name() {
        return type_name;
    }

    public int getType() {
        return ((Number) type).intValue();
    }


    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        if (getClass() != obj.getClass()) return false;
        if (type != ((NavigationCardItem) obj).getType()) return false;
        if (type == TYPE_NOTIFICATION) {
            return getNotificationExtra().getObjectId().equals(((NavigationCardItem) obj).getNotificationExtra().getObjectId());
        }
        return type == ((NavigationCardItem) obj).getType();
    }

    @Override
    public int compareTo(@NonNull Object o) {
        if (o instanceof NavigationCardItem) {
            return power - ((NavigationCardItem) o).getPower();
        }
        return 0;
    }
}
