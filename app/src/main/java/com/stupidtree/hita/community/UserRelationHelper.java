package com.stupidtree.hita.community;

import android.text.TextUtils;

import androidx.annotation.MainThread;

import com.stupidtree.hita.online.HITAUser;
import com.stupidtree.hita.online.UserRelation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

import static com.stupidtree.hita.HITAApplication.bmobCacheHelper;


public class UserRelationHelper {
    HITAUser user;

    public UserRelationHelper(HITAUser user) {
        this.user = user;
    }

    @MainThread
    private void QueryAvatar(final FetchAvatarListener listener) {
        BmobQuery<UserRelation> avatar = new BmobQuery<>();
        avatar.addWhereEqualTo("user", new BmobPointer(user));
        avatar.setCachePolicy(BmobQuery.CachePolicy.CACHE_ELSE_NETWORK);
        avatar.findObjects(new FindListener<UserRelation>() {
            @Override
            public void done(List<UserRelation> object, BmobException e) {
                if (e != null) listener.onFailed(e);
                else if (object != null && object.size() > 0) {
                    listener.onFetched(object.get(0));
                } else if (object == null || object.size() == 0) {
                    final UserRelation userRelation = new UserRelation();
                    userRelation.setUser(user);
                    userRelation.save(new SaveListener<String>() {
                        @Override
                        public void done(String s, BmobException e) {
                            if (e == null && !TextUtils.isEmpty(s)) {
                                userRelation.setObjectId(s);
                                listener.onFetched(userRelation);
                            } else {
                                listener.onFailed(e);
                            }
                        }
                    });
                } else {
                    listener.onFailed(e);
                }
            }
        });
    }

    @MainThread
    public void QueryFansBasicInfo(final boolean cache, final QueryFansInfoListener listener) {
        QueryAvatar(new FetchAvatarListener() {
            @Override
            public void onFetched(UserRelation avatar) {
                if (avatar == null) listener.onFailed(null);
                BmobQuery<HITAUser> followingQ = new BmobQuery<>();
                followingQ.addQueryKeys("nick,avatarUri");
                followingQ.addWhereRelatedTo("fans", new BmobPointer(avatar));
                if (cache) {
                    followingQ.setCachePolicy(BmobQuery.CachePolicy.CACHE_ELSE_NETWORK);
                } else {
                    followingQ.setCachePolicy(BmobQuery.CachePolicy.NETWORK_ONLY);
                }
                followingQ.findObjects(new FindListener<HITAUser>() {
                    @Override
                    public void done(List<HITAUser> object, BmobException e) {
                        if (e != null) listener.onFailed(e);
                        else {
                            if (object == null) listener.onResult(new ArrayList<HITAUser>());
                            else listener.onResult(object);
                        }
                    }
                });
            }

            @Override
            public void onFailed(Exception e) {
                listener.onFailed(e);
            }
        });
    }

    @MainThread
    public void QueryFansAndFollowingNum(final boolean cache, final QueryFansAndFollowingNumListener listener) {
        QueryAvatar(new FetchAvatarListener() {
            @Override
            public void onFetched(UserRelation avatar) {
                if (avatar == null) listener.onFailed(null);
                BmobQuery<HITAUser> fansq = new BmobQuery<>();
                fansq.addQueryKeys("nick,avatarUri");
                fansq.addWhereRelatedTo("fans", new BmobPointer(avatar));
                if (cache) {
                    fansq.setCachePolicy(BmobQuery.CachePolicy.CACHE_ELSE_NETWORK);
                } else {
                    fansq.setCachePolicy(BmobQuery.CachePolicy.NETWORK_ONLY);
                }
                fansq.count(HITAUser.class, new CountListener() {
                    @Override
                    public void done(Integer count, BmobException e) {
                        if (e == null && count != null) {
                            listener.onFansResult(count);
                        } else listener.onFailed(e);
                    }
                });

                BmobQuery<HITAUser> followingQ = new BmobQuery<>();
                followingQ.addQueryKeys("nick,avatarUri,objectId");
                followingQ.addWhereRelatedTo("following", new BmobPointer(avatar));
                if (cache) {
                    followingQ.setCachePolicy(BmobQuery.CachePolicy.CACHE_ELSE_NETWORK);
                } else {
                    followingQ.clearCachedResult(HITAUser.class);
                    followingQ.setCachePolicy(BmobQuery.CachePolicy.NETWORK_ONLY);
                }
                followingQ.count(HITAUser.class, new CountListener() {
                    @Override
                    public void done(Integer count, BmobException e) {
                        if (e != null || count == null) listener.onFailed(e);
                        else listener.onFollowingResult(count);
                    }
                });

            }

            @Override
            public void onFailed(Exception e) {
                listener.onFailed(e);
            }
        });
    }

    @MainThread
    public void QueryIsFollowing(final HITAUser other, final QueryIsFollowingListener listener) {
        QueryAvatar(new FetchAvatarListener() {
            @Override
            public void onFetched(UserRelation avatar) {
                BmobQuery<HITAUser> followingQ = new BmobQuery<>();
                followingQ.addQueryKeys("objectId");
                followingQ.addWhereRelatedTo("following", new BmobPointer(avatar));
                BmobQuery<HITAUser> bq2 = new BmobQuery();
                bq2.addWhereEqualTo("objectId", other.getObjectId());
                BmobQuery<HITAUser> fin = new BmobQuery<>();
                fin.and(Arrays.asList(followingQ, bq2));
                fin.addQueryKeys("objectId");
                fin.findObjects(new FindListener<HITAUser>() {
                    @Override
                    public void done(List<HITAUser> object, BmobException e) {
                        if (e != null || object == null || object.size() == 0)
                            listener.onResult(false);
                        else listener.onResult(true);
                    }
                });
            }

            @Override
            public void onFailed(Exception e) {
                listener.onResult(false);
            }
        });

    }

    @MainThread
    public void QueryFollowingBasicInfo(final boolean cache, final QueryFollowingInfoListener listener) {
        QueryAvatar(new FetchAvatarListener() {
            @Override
            public void onFetched(UserRelation avatar) {
                BmobQuery<HITAUser> followingQ = new BmobQuery<>();
                followingQ.addQueryKeys("nick,avatarUri,objectId");
                followingQ.addWhereRelatedTo("following", new BmobPointer(avatar));
                if (cache) {
                    followingQ.setCachePolicy(BmobQuery.CachePolicy.CACHE_ELSE_NETWORK);
                } else {
                    followingQ.setCachePolicy(BmobQuery.CachePolicy.NETWORK_ONLY);
                }
                followingQ.findObjects(new FindListener<HITAUser>() {
                    @Override
                    public void done(List<HITAUser> object, BmobException e) {
                        if (e != null || object == null) listener.onFailed(e);
                        else listener.onResult(object);
                    }
                });
            }

            @Override
            public void onFailed(Exception e) {
                listener.onFailed(e);
            }
        });
    }

    @MainThread
    public void QueryFollowingObjectId(final boolean cache, final QueryFollowingObjectIdListener listener) {
        QueryAvatar(new FetchAvatarListener() {
            @Override
            public void onFetched(UserRelation avatar) {
                BmobQuery<HITAUser> followingQ = new BmobQuery<>();
                followingQ.addQueryKeys("objectId");
                followingQ.addWhereRelatedTo("following", new BmobPointer(avatar));
                if (cache) {
                    followingQ.setCachePolicy(BmobQuery.CachePolicy.CACHE_ELSE_NETWORK);
                } else {
                    followingQ.setCachePolicy(BmobQuery.CachePolicy.NETWORK_ONLY);
                }
                followingQ.findObjects(new FindListener<HITAUser>() {
                    @Override
                    public void done(List<HITAUser> object, BmobException e) {
                        if (e != null || object == null) listener.onFailed(e);
                        else {
                            List<String> rr = new ArrayList<>();
                            for (HITAUser u : object) rr.add(u.getObjectId());
                            listener.onResult(rr);
                        }
                    }
                });
            }

            @Override
            public void onFailed(Exception e) {
                listener.onFailed(e);
            }
        });
    }

    public void Follow(final HITAUser other, final OnFollowListener listener) {
        QueryAvatar(new FetchAvatarListener() {
            @Override
            public void onFetched(final UserRelation avatar) {
                BmobRelation br = new BmobRelation();
                br.add(other);
                avatar.setFollowing(br);
                new UserRelationHelper(other).QueryAvatar(new FetchAvatarListener() {
                    @Override
                    public void onFetched(UserRelation avatar2) {
                        BmobRelation br = new BmobRelation();
                        br.add(user);
                        UserRelation ava2 = new UserRelation();
                        ava2.setObjectId(avatar2.getObjectId());
                        ava2.setFans(br);
                        ava2.update(new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if (e == null) {
                                    avatar.update(new UpdateListener() {
                                        @Override
                                        public void done(BmobException e) {
                                            if (e == null) {
                                                bmobCacheHelper.callMyFollowingsToRefresh();
                                                listener.onDone();
                                            } else listener.onFailed(e);
                                        }
                                    });
                                } else {
                                    e.printStackTrace();
                                    listener.onFailed(e);
                                }
                            }
                        });

                    }

                    @Override
                    public void onFailed(Exception e) {
                        listener.onFailed(e);
                    }
                });
            }

            @Override
            public void onFailed(Exception e) {
                listener.onFailed(e);
            }
        });
    }

    public void UnFollow(final HITAUser other, final OnFollowListener listener) {
        QueryAvatar(new FetchAvatarListener() {
            @Override
            public void onFetched(final UserRelation avatar) {
                BmobRelation br = new BmobRelation();
                br.remove(other);
                avatar.setFollowing(br);
                new UserRelationHelper(other).QueryAvatar(new FetchAvatarListener() {
                    @Override
                    public void onFetched(UserRelation avatar2) {
                        BmobRelation br = new BmobRelation();
                        br.remove(user);
                        avatar2.setFans(br);
                        avatar2.update(new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if (e == null) {
                                    avatar.update(new UpdateListener() {
                                        @Override
                                        public void done(BmobException e) {
                                            if (e == null) {
                                                bmobCacheHelper.callMyFollowingsToRefresh();
                                                listener.onDone();
                                            } else listener.onFailed(e);
                                        }
                                    });
                                } else {
                                    listener.onFailed(e);
                                }
                            }
                        });

                    }

                    @Override
                    public void onFailed(Exception e) {
                        listener.onFailed(e);
                    }
                });
            }

            @Override
            public void onFailed(Exception e) {
                listener.onFailed(e);
            }
        });
    }

    interface FetchAvatarListener {
        void onFetched(UserRelation avatar);

        void onFailed(Exception e);
    }

    public interface QueryFansInfoListener {
        void onResult(List<HITAUser> result);

        void onFailed(Exception e);
    }

    public interface QueryFansAndFollowingNumListener {
        void onFansResult(int fansNum);

        void onFollowingResult(int followingNum);

        void onFailed(Exception e);
    }

    public interface QueryIsFollowingListener {
        void onResult(boolean result);
    }


    public interface QueryFollowingInfoListener {
        void onResult(List<HITAUser> result);

        void onFailed(Exception e);
    }

    public interface QueryFollowingObjectIdListener {
        void onResult(List<String> result);

        void onFailed(Exception e);
    }

    public interface OnFollowListener {
        void onDone();

        void onFailed(Exception e);
    }
}
