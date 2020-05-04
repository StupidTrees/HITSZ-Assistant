package com.stupidtree.hita.activities;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.stupidtree.hita.R;
import com.stupidtree.hita.community.UserRelationHelper;
import com.stupidtree.hita.fragments.popup.FragmentRelatedUsers;
import com.stupidtree.hita.online.HITAUser;
import com.stupidtree.hita.util.ActivityUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

import static com.stupidtree.hita.HITAApplication.CurrentUser;

public class ActivityUserProfile extends BaseActivity {
    private static final int NULL = 464;
    private static final int FOLLOW = 915;
    private static final int UNFOLLOW = 42;
    ImageView appbarBg;
    CollapsingToolbarLayout mToolbarLayout;
    ImageView avatar;
    TextView name, signature, nick, school, grade;
    AppBarLayout appBarLayout;
    ExtendedFloatingActionButton fab;
    HITAUser user = null;
    TextView fans, following;
    int fansNum = 0, followingNum = 0;
    int fabAction;
    ViewGroup privacyLayout, gradeLayout;
    boolean hasPermission = false;
    TextView punchLabel;
    View moodLock;//,postLock;
    LinearLayout my_post;
    private ProgressBar firstPr, secondPr, thirdPr;
    private TextView firstP, secondP, thirdP;
    private ImageView firstI, secondI, thirdI;
    private TextView[] percentages;
    private ImageView[] icons;
    private ProgressBar[] progressBars;

    @Override
    protected void stopTasks() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setWindowParams(true, true, false);
        initToolbar();
        initViews();
        Bundle b = getIntent().getExtras();
        if (b != null && (user = (HITAUser) b.getSerializable("user")) != null) {
            verifyPermission();
            RefreshRelation();
        } else {
            loadUserInfoOnline();
        }

    }

    void initToolbar() {
        appBarLayout = findViewById(R.id.appbar);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mToolbarLayout = findViewById(R.id.usercenter_toolbarlayout);
        mToolbarLayout.setScrimAnimationDuration(300);
        mToolbarLayout.setScrimVisibleHeightTrigger(500);
        mToolbarLayout.setCollapsedTitleTextColor(ContextCompat.getColor(this, R.color.material_primary_text));
        appbarBg = findViewById(R.id.usercenter_appbar_image);
    }

    @SuppressLint("ResourceType")
    void initViews() {
        privacyLayout = findViewById(R.id.privacy_layout);
        gradeLayout = findViewById(R.id.grade_layout);
        fans = findViewById(R.id.fans);
        following = findViewById(R.id.following);
        name = findViewById(R.id.profile_name);
        signature = findViewById(R.id.profile_signature);
        avatar = findViewById(R.id.profile_avatar);
        nick = findViewById(R.id.profile_nick);
        school = findViewById(R.id.profile_school);
        grade = findViewById(R.id.profile_grade);
        fab = findViewById(R.id.fab_follow);
        fab.setBackgroundTintList(ColorStateList.valueOf(getColorAccent()));
        fab.setVisibility(View.INVISIBLE);


        punchLabel = findViewById(R.id.punch_label);
        firstI = findViewById(R.id.first_icon);
        firstPr = findViewById(R.id.first_progress);
        firstP = findViewById(R.id.first_percentage);

        secondI = findViewById(R.id.second_icon);
        secondPr = findViewById(R.id.second_progress);
        secondP = findViewById(R.id.second_percentage);

        thirdI = findViewById(R.id.third_icon);
        thirdP = findViewById(R.id.third_percentage);
        thirdPr = findViewById(R.id.third_progress);
        fab.setHideMotionSpecResource(R.anim.fab_scale_hide);
        fab.setShowMotionSpecResource(R.anim.fab_scale_show);
        progressBars = new ProgressBar[]{firstPr, secondPr, thirdPr};
        percentages = new TextView[]{firstP, secondP, thirdP};
        icons = new ImageView[]{firstI, secondI, thirdI};
        moodLock = findViewById(R.id.mood_lock);
        // postLock = findViewById(R.id.post_lock);

        my_post = findViewById(R.id.its_post);
        my_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user != null) ActivityUtils.startOneUserPostsActivity(getThis(), user);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CurrentUser != null) {
                    if (fabAction == FOLLOW) Follow();
                    else if (fabAction == UNFOLLOW) UnFollow();
                }
            }
        });
        following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (followingNum == 0) return;

                new FragmentRelatedUsers(false, getString(R.string.people_following), new FragmentRelatedUsers.DataFetcher() {
                    @Override
                    public void fetchData(boolean anim, final FragmentRelatedUsers.OnFetchListener listener) {
                        new UserRelationHelper(user).QueryFollowingBasicInfo(false, new UserRelationHelper.QueryFollowingInfoListener() {
                            @Override
                            public void onResult(List<HITAUser> result) {
                                listener.OnFetchDone(result);
                            }

                            @Override
                            public void onFailed(Exception e) {
                                listener.OnFailed();
                            }
                        });
                    }

                    @Override
                    public void fetchCurrentFollowingData(boolean cache, final FragmentRelatedUsers.OnFollowingFetchListener listener) {
                        new UserRelationHelper(CurrentUser).QueryFollowingObjectId(cache, new UserRelationHelper.QueryFollowingObjectIdListener() {
                            @Override
                            public void onResult(List<String> result) {
                                listener.OnFetchDone(result);
                            }

                            @Override
                            public void onFailed(Exception e) {
                                listener.OnFailed();
                            }
                        });
                    }
                }).show(getSupportFragmentManager(), "related_user");
            }
        });
        fans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fansNum == 0) return;
                new FragmentRelatedUsers(false, getString(R.string.people_fans),

                        new FragmentRelatedUsers.DataFetcher() {
                            @Override
                            public void fetchData(boolean anim, final FragmentRelatedUsers.OnFetchListener listener) {
                                new UserRelationHelper(user).QueryFansBasicInfo(false, new UserRelationHelper.QueryFansInfoListener() {
                                    @Override
                                    public void onResult(List<HITAUser> result) {
                                        listener.OnFetchDone(result);
                                    }

                                    @Override
                                    public void onFailed(Exception e) {
                                        listener.OnFailed();
                                    }
                                });
                            }

                            @Override
                            public void fetchCurrentFollowingData(boolean cache, final FragmentRelatedUsers.OnFollowingFetchListener listener) {
                                new UserRelationHelper(CurrentUser).QueryFollowingObjectId(cache, new UserRelationHelper.QueryFollowingObjectIdListener() {
                                    @Override
                                    public void onResult(List<String> result) {
                                        listener.OnFetchDone(result);
                                    }

                                    @Override
                                    public void onFailed(Exception e) {
                                        listener.OnFailed();
                                    }
                                });
                            }


                        }).show(getSupportFragmentManager(), "related_user");
            }
        });
    }

    void loadUserInfoOnline() {
        BmobQuery<HITAUser> bq = new BmobQuery<>();
        bq.addWhereEqualTo("objectId", getIntent().getStringExtra("objectId"));
        bq.findObjects(new FindListener<HITAUser>() {
            @Override
            public void done(List<HITAUser> list, BmobException e) {
                if (e == null && list != null && list.size() > 0) {
                    user = list.get(0);
                    verifyPermission();
                    RefreshRelation();
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    String showText(String raw) {
        if (raw == null || raw.isEmpty()) return "无数据";
        else return raw;
    }

    void RefreshProfile() {
        if (user == null) return;
        nick.setText(showText(user.getNick()));
        if (hasPermission) {
            privacyLayout.setVisibility(View.VISIBLE);
            name.setText(showText(user.getRealname()));
        } else {
            privacyLayout.setVisibility(View.GONE);
        }
        if (user.getGrade() < 2000 && !TextUtils.isEmpty(user.getStudentnumber())) {
            try {
                int gradeGuess = 0;
                String sn = user.getStudentnumber();
                String numb = sn.replaceAll("SZ", "").replaceAll("sz", "");
                String two = numb.substring(0, 2);
                gradeGuess = Integer.parseInt(two);
                user.setGrade(2000 + gradeGuess);
                gradeLayout.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                gradeLayout.setVisibility(View.GONE);
            }
        } else if (user.getGrade() > 2000) {
            gradeLayout.setVisibility(View.VISIBLE);
        } else {
            gradeLayout.setVisibility(View.GONE);
        }


        school.setText(showText(user.getSchool()));
        signature.setText(showText(user.getSignature()));
        grade.setText(user.getGrade() + "级");
        if (hasPermission) {
            refreshMood();
            //postLock.setVisibility(View.GONE);
            // my_post.setClickable(true);
            moodLock.setVisibility(View.GONE);
        } else {
            // my_post.setClickable(false);
            moodLock.setVisibility(View.VISIBLE);
            //  postLock.setVisibility(View.VISIBLE);
        }

        try {
            Glide.with(ActivityUserProfile.this).load(user.getAvatarUri()).apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.ic_account_activated).into(avatar);
            avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (user == null) return;
                    ActivityUtils.showOneImage(getThis(), user.getAvatarUri());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void refreshMood() {
        try {
            int happy = user.getHappyDays();
            int normal = user.getNormalDays();
            int sad = user.getSadDays();
            List<Temp> l = new ArrayList<>();
            l.add(new Temp("happy", happy));
            l.add(new Temp("normal", normal));
            l.add(new Temp("sad", sad));
            Collections.sort(l);
            int all = happy + normal + sad;
            for (int i = 0; i < 3; i++) {
                int iconID;
                DecimalFormat df = new DecimalFormat("#0.00");
                if (l.get(i).type.equals("happy")) {
                    iconID = R.drawable.ic_mood_happy;
                    percentages[i].setText(df.format(100 * (float) happy / all) + "%");
                    progressBars[i].setProgress((int) (100 * (float) happy / all));
                } else if (l.get(i).type.equals("normal")) {
                    iconID = R.drawable.ic_mood_normal;
                    percentages[i].setText(df.format(100 * (float) normal / all) + "%");
                    progressBars[i].setProgress((int) (100 * (float) normal / all));
                } else {
                    iconID = R.drawable.ic_mood_sad;
                    percentages[i].setText(df.format(100 * (float) sad / all) + "%");
                    progressBars[i].setProgress((int) (100 * (float) sad / all));
                }
                //  progressBars[i].setProgressDrawable(HContext.getDrawable(colorID));
                icons[i].setImageResource(iconID);
                //icons[i].setImageTintList(ColorStateList.valueOf(HContext.getColor(tintID)));

            }
            punchLabel.setText(getString(R.string.user_center_you_have_punched, all));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void RefreshRelation() {

        if (user == null) return;
        refreshFansFollowingNum();
        if (CurrentUser == null) {
            fab.hide();
        } else {
            fab.hide();
            fab.shrink();
            //fab.setIconResource(R.drawable.ic_loading);
            new UserRelationHelper(CurrentUser).QueryIsFollowing(user, new UserRelationHelper.QueryIsFollowingListener() {
                @Override
                public void onResult(boolean result) {
                    fab.show();
                    if (result) {
                        fabAction = UNFOLLOW;
                        fab.setIconResource(R.drawable.ic_unfollow);
                        fab.setText(R.string.unfollow);
                    } else {
                        fabAction = FOLLOW;
                        fab.setIconResource(R.drawable.ic_follow);
                        fab.setText(R.string.follow);
                    }
                }
            });
            // new refreshRelationTask().executeOnExecutor(TPE);
        }
    }

//    class followTask extends AsyncTask {
//        @Override
//        protected Object doInBackground(Object[] objects) {
//            UserRelation avatar = CurrentUser.getUserRelationAvatar();
//            avatar.followSync(user);
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Object o) {
//            super.onPostExecute(o);
//
//        }
//    }
//
//    class unFollowTask extends AsyncTask {
//        @Override
//        protected Object doInBackground(Object[] objects) {
//            UserRelation avatar = CurrentUser.getUserRelationAvatar();
//            avatar.unFollowSync(user);
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Object o) {
//            super.onPostExecute(o);
//            BmobCacheMap.put("following", true);
//            BmobCacheMap.put("fans", true);
//            Toast.makeText(getThis(), R.string.unfollowed, Toast.LENGTH_SHORT).show();
//            RefreshRelation();
//        }
//    }


//    class refreshRelationTask extends AsyncTask {
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            fab.show();
//            fab.shrink();
//            fab.setIconResource(R.drawable.ic_loading);
//        }
//
//        @Override
//        protected Object doInBackground(Object[] objects) {
//            UserRelation myAvatar = CurrentUser.getUserRelationAvatar();
//            BmobQuery<HITAUser> bq = new BmobQuery<>();
//            bq.addWhereRelatedTo("following", new BmobPointer(myAvatar));
//            BmobQuery<HITAUser> bq2 = new BmobQuery<>();
//            bq2.addWhereEqualTo("objectId", user.getObjectId());
//            List<BmobQuery<HITAUser>> cond = new ArrayList<>();
//            cond.add(bq);
//            cond.add(bq2);
//            BmobQuery<HITAUser> bqq = new BmobQuery<>();
//            bqq.and(cond);
//            List<HITAUser> res = bqq.findObjectsSync(HITAUser.class);
//            return res != null && res.size() > 0;
//        }
//
//        @Override
//        protected void onPostExecute(Object o) {
//            super.onPostExecute(o);
//            fab.extend();
//
//        }
//    }

    void Follow() {
        new UserRelationHelper(CurrentUser).Follow(user, new UserRelationHelper.OnFollowListener() {
            @Override
            public void onDone() {
                Toast.makeText(getThis(), R.string.followed, Toast.LENGTH_SHORT).show();
                RefreshRelation();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(getThis(), R.string.follow_failed, Toast.LENGTH_SHORT).show();
            }
        });

    }

    void UnFollow() {
        new UserRelationHelper(CurrentUser).UnFollow(user, new UserRelationHelper.OnFollowListener() {
            @Override
            public void onDone() {
                Toast.makeText(getThis(), R.string.unfollowed, Toast.LENGTH_SHORT).show();
                RefreshRelation();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(getThis(), R.string.unfollow_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    void refreshFansFollowingNum() {
        fans.setText(R.string.fans);
        following.setText(R.string.following);
        new UserRelationHelper(user).QueryFansAndFollowingNum(false,
                new UserRelationHelper.QueryFansAndFollowingNumListener() {
                    @Override
                    public void onFansResult(int n) {
                        fansNum = n;
                        fans.setText(getString(R.string.fans) + " " + n);
                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onFollowingResult(int n) {
                        followingNum = n;
                        following.setText(getString(R.string.following) + " " + n);
                    }

                    @Override
                    public void onFailed(Exception e) {
                        fans.setText(R.string.fans);
                        following.setText(R.string.following);
                    }
                });
    }
//    class refreshFansTask extends AsyncTask {
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//
//        }
//
//        @Override
//        protected Object doInBackground(Object[] objects) {
//            if (user == null) return null;
//            UserRelation avatar = user.getUserRelationAvatar();
//            final BmobQuery<HITAUser> fansQ = new BmobQuery<>();
//            fansQ.addWhereRelatedTo("fans", new BmobPointer(avatar));
//            fansQ.count(HITAUser.class, new CountListener() {
//                @Override
//                public void done(Integer count, BmobException e) {
//
//                }
//            });
//            BmobQuery<HITAUser> followingQ = new BmobQuery<>();
//            followingQ.addWhereRelatedTo("following", new BmobPointer(avatar));
//            followingQ.count(HITAUser.class, new CountListener() {
//                @Override
//                public void done(Integer count, BmobException e) {
//
//                }
//            });
//            return null;
//        }
//    }

    void verifyPermission() {
        String policy = user.getProfilePolicy();
        if (CurrentUser == null) {
            hasPermission = false;
            RefreshProfile();
        } else if (policy.equals(HITAUser.SHOWN_TO_AV8D)) {
            hasPermission = true;
            RefreshProfile();
        } else if (policy.equals(HITAUser.SHOWN_TO_NOBODY)) {
            hasPermission = false;
            RefreshProfile();
        } else if (policy.equals(HITAUser.SHOWN_TO_FOLLOWING)) {
            new UserRelationHelper(user).QueryIsFollowing(CurrentUser, new UserRelationHelper.QueryIsFollowingListener() {
                @Override
                public void onResult(boolean result) {
                    hasPermission = result;
                    RefreshProfile();
                }
            });
        }
    }

    //    class verifyPermissionTask extends AsyncTask {
//
//        @Override
//        protected Object doInBackground(Object[] objects) {
//
//            return false;
//        }
//
//        @Override
//        protected void onPostExecute(Object o) {
//            super.onPostExecute(o);
//            hasPermission = (boolean) o;
//
//        }
//    }
    private class Temp implements Comparable {
        String type;
        int number;

        Temp(String type, int number) {
            this.type = type;
            this.number = number;
        }

        @Override
        public int compareTo(Object o) {
            return -(this.number - ((Temp) o).number);
        }
    }
}
