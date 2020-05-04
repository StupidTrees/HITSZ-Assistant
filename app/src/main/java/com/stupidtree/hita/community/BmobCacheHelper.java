package com.stupidtree.hita.community;

import java.util.HashMap;

public class BmobCacheHelper {
    HashMap<String, Boolean> BmobCacheMap;

    public BmobCacheHelper() {
        BmobCacheMap = new HashMap<>();
    }

    public void callMyFollowingsToRefresh() {
        BmobCacheMap.put("my_following_id", true);
        BmobCacheMap.put("my_following_basic", true);
    }

    public void callBasicTopicsToRefresh() {
        BmobCacheMap.put("basic_topics", true);
    }

    public boolean willBasicTopicsUseCache() {
        return !BmobCacheMap.containsKey("basic_topics");
    }

    public void basicTopicsRelease() {
        BmobCacheMap.remove("basic_topics");
    }

    public boolean willMyFollowingIdUseCache() {
        return !BmobCacheMap.containsKey("my_following_id");
    }

    public boolean willMyFollowingBasicUseCache() {
        return !BmobCacheMap.containsKey("my_following_basic");
    }

    public void MyFollowingBasicRelease() {
        BmobCacheMap.remove("my_following_basic");
    }

    public void MyFollowingIDRelease() {
        BmobCacheMap.remove("my_following_id");
    }
}
