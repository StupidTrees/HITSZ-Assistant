package com.stupidtree.hita.activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.tabs.TabLayout;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.BaseTabAdapter;
import com.stupidtree.hita.community.UserRelationHelper;
import com.stupidtree.hita.fragments.BaseFragment;
import com.stupidtree.hita.fragments.popup.FragmentLoading;
import com.stupidtree.hita.fragments.popup.FragmentRelatedUsers;
import com.stupidtree.hita.fragments.timetable_manager.FragmentSubjects;
import com.stupidtree.hita.fragments.user.FragmentUserCenter_Info;
import com.stupidtree.hita.fragments.user.FragmentUserCenter_sync;
import com.stupidtree.hita.fragments.user.FragmentUserCenter_ut;
import com.stupidtree.hita.online.HITAUser;
import com.stupidtree.hita.util.FileProviderUtils;
import com.stupidtree.hita.util.GalleryPicker;
import com.stupidtree.hita.views.MaterialCircleAnimator;
import com.stupidtree.hita.views.mBlurTransformation;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FetchUserInfoListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.bmobCacheHelper;
import static com.stupidtree.hita.HITAApplication.defaultSP;
import static com.stupidtree.hita.HITAApplication.jwCore;
import static com.stupidtree.hita.HITAApplication.timeTableCore;
import static com.stupidtree.hita.community.ActivityCreatePost.RC_CHOOSE_PHOTO;
import static com.stupidtree.hita.community.ActivityCreatePost.RC_CROP_PHOTO;
import static com.stupidtree.hita.timetable.TimeWatcherService.USER_CHANGED;

public class ActivityUserCenter extends BaseActivity implements FragmentSubjects.OnFragmentInteractionListener
        //, FragmentJWTS_info.OnListFragmentInteractionListener
{

    ViewPager viewpager;
    TabLayout tabLayout;
    ImageView appbarBg;
    CollapsingToolbarLayout mToolbarLayout;
    ImageView avatar;
    TextView name, signature;
    AppBarLayout appBarLayout;
    CardView change_avatar;
    int fansNum = 0, followingNum = 0;
    TextView fans, following, newFansNum;
    Uri cropImgUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_center);
        setWindowParams(true, true, false);
        initToolbar();
        initPager();
        initUserView();
        BmobUser.fetchUserInfo(new FetchUserInfoListener<BmobUser>() {
            @Override
            public void done(BmobUser user, BmobException e) {
                if (e == null) {
                    CurrentUser = BmobUser.getCurrentUser(HITAUser.class);
                }

                for (Fragment bf : getSupportFragmentManager().getFragments()) {
                    if (bf instanceof BaseFragment) {
                        ((BaseFragment) bf).Refresh();
                    }
                }
            }
        });
        if (CurrentUser != null) {
            cropImgUri = Uri.parse("file:///" + Environment.getExternalStorageDirectory() + "/avatar_" + CurrentUser.getUsername() + ".jpg");
        } else {
            cropImgUri = null;
        }
        RefreshFansAndFollowingNum();
        bmobCacheHelper.callMyFollowingsToRefresh();
    }

    void initToolbar() {
        appBarLayout = findViewById(R.id.appbar);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.label_activity_user_center));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.inflateMenu(R.menu.menu_user_center);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_logout) {
                    AlertDialog ad = new AlertDialog.Builder(ActivityUserCenter.this).create();
                    ad.setMessage(getString(R.string.logout_message));
                    ad.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.button_confirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            BmobUser.logOut();
                            Intent i = new Intent(USER_CHANGED);
                            LocalBroadcastManager.getInstance(getThis()).sendBroadcast(i);
                            timeTableCore.clearData();
                            jwCore.logOut();
                            CurrentUser = null;
                            finish();
                        }
                    });
                    ad.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    ad.show();
                }
                return true;
            }
        });
        mToolbarLayout = findViewById(R.id.usercenter_toolbarlayout);
        mToolbarLayout.setScrimAnimationDuration(300);
        mToolbarLayout.setScrimVisibleHeightTrigger(500);
        mToolbarLayout.setCollapsedTitleTextColor(ContextCompat.getColor(this, R.color.material_primary_text));
        appbarBg = findViewById(R.id.usercenter_appbar_image);
        appbarBg.post(new Runnable() {
            @Override
            public void run() {
                MaterialCircleAnimator.animShow(appbarBg, 700);
            }
        });
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                float scale = 1.0f + (verticalOffset) / ((float) appBarLayout.getHeight());
                avatar.setScaleX(scale);
                avatar.setScaleY(scale);
                float mHeadImgScale = 0;
                avatar.setTranslationY(mHeadImgScale * verticalOffset);

                change_avatar.setScaleX(scale);
                change_avatar.setScaleY(scale);
                change_avatar.setTranslationY(mHeadImgScale * verticalOffset);
            }
        });
    }

    void initUserView() {
        fans = findViewById(R.id.fans);
        newFansNum = findViewById(R.id.new_fans_num);
        following = findViewById(R.id.following);
        name = findViewById(R.id.usercenter_name);
        signature = findViewById(R.id.usercenter_signature);
        avatar = findViewById(R.id.usercenter_avatar);
        change_avatar = findViewById(R.id.change_avatar);
        //Glide.with(this).load(userInfos.get("头像")).into(avatar);
        name.setText(CurrentUser.getNick());
        signature.setText(CurrentUser.getSignature());
        loadAvatar();
        following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (followingNum == 0) return;

                new FragmentRelatedUsers(true, getString(R.string.my_following), new FragmentRelatedUsers.DataFetcher() {
                    @Override
                    public void fetchData(boolean anim, final FragmentRelatedUsers.OnFetchListener listener) {
                        new UserRelationHelper(CurrentUser).QueryFollowingBasicInfo(
                                bmobCacheHelper.willMyFollowingBasicUseCache(), new UserRelationHelper.QueryFollowingInfoListener() {
                                    @Override
                                    public void onResult(List<HITAUser> result) {
                                        listener.OnFetchDone(result);
                                        bmobCacheHelper.MyFollowingBasicRelease();
                                    }

                                    @Override
                                    public void onFailed(Exception e) {
                                        listener.OnFailed();
                                    }
                                });
                    }

                    @Override
                    public void fetchCurrentFollowingData(boolean cache, final FragmentRelatedUsers.OnFollowingFetchListener listener) {
                        listener.OnFetchDone(null);
                    }

                }).setOnChangeDataListener(new FragmentRelatedUsers.OnChangeDataListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onMyFollowingNumberFetched(int count) {
                        followingNum = count;
                        following.setText(getString(R.string.following) + " " + count);
                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onNumberFetched(int count) {
                        followingNum = count;
                        following.setText(getString(R.string.following) + " " + count);
                    }
                }).show(getSupportFragmentManager(), "related_user");
            }
        });
        fans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fansNum == 0) return;
                new FragmentRelatedUsers(false, getString(R.string.my_fans), new FragmentRelatedUsers.DataFetcher() {
                    @Override
                    public void fetchData(boolean anim, final FragmentRelatedUsers.OnFetchListener listener) {
                        new UserRelationHelper(CurrentUser).QueryFansBasicInfo(false, new UserRelationHelper.QueryFansInfoListener() {
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


                }).setOnChangeDataListener(new FragmentRelatedUsers.OnChangeDataListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onMyFollowingNumberFetched(int count) {
                        followingNum = count;

                        following.setText(getString(R.string.following) + " " + count);
                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onNumberFetched(int number) {
                        fansNum = number;
                        int last = defaultSP.getInt("last_fans_num", 0);
                        if (fansNum > last) {
                            newFansNum.setVisibility(View.VISIBLE);
                            newFansNum.setText("+" + (fansNum - last));
                        } else {
                            newFansNum.setVisibility(View.GONE);
                        }
                        defaultSP.edit().putInt("last_fans_num", fansNum).apply();
                        fans.setText(getString(R.string.fans) + " " + number);
                    }
                })
                        .show(getSupportFragmentManager(), "related_user");
            }
        });
        change_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GalleryPicker.choosePhoto(getThis(), false);
            }
        });
    }

    void initPager() {
        tabLayout = findViewById(R.id.usercenter_tablayout);
        viewpager = findViewById(R.id.usercenter_viewpager);
        final String[] titles = getResources().getStringArray(R.array.user_center_tabs);
        viewpager.setAdapter(new BaseTabAdapter(getSupportFragmentManager(), 3) {

            @Override
            protected Fragment initItem(int position) {
                switch (position) {
                    case 0:
                        return FragmentUserCenter_ut.newInstance();
                    case 1:
                        return new FragmentUserCenter_Info();
                    case 2:
                        return new FragmentUserCenter_sync();
                }
                return null;
            }


            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return titles[position];
            }
        }.setDestroyFragment(false));
        tabLayout.setupWithViewPager(viewpager);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setTabIndicatorFullWidth(false);
        //tabLayout.setTabTextColors(ColorStateList.valueOf(getColorPrimary()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case RC_CHOOSE_PHOTO:
                if (null == data) {
                    Toast.makeText(this, R.string.no_image_fetched, Toast.LENGTH_SHORT).show();
                    return;
                }
                Uri uri = data.getData();
                if (null == uri) { //如果单个Uri为空，则可能是1:多个数据 2:没有数据
                    Toast.makeText(this, R.string.no_image_selected, Toast.LENGTH_SHORT).show();
                    return;
                }
                // 剪裁图片
                GalleryPicker.cropPhoto(getThis(), FileProviderUtils.getFilePathByUri(this, uri), cropImgUri, 200);
                break;
            case RC_CROP_PHOTO:
                if (cropImgUri != null) {
                    changeAvatar(FileProviderUtils.getFilePathByUri(this, cropImgUri));
                }

                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user_center, menu);
        return super.onCreateOptionsMenu(menu);
    }

    void loadAvatar() {
        if (this.isDestroyed()) return;
        Glide.with(ActivityUserCenter.this).load(CurrentUser.getAvatarUri())
                //.signature(new ObjectKey(Objects.requireNonNull(defaultSP.getString("avatarGlideSignature", String.valueOf(System.currentTimeMillis())))))
                .placeholder(R.drawable.ic_account_activated)
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(avatar);
        Glide.with(ActivityUserCenter.this).load(CurrentUser.getAvatarUri())
                .placeholder(R.drawable.ic_account_activated)
                .apply(RequestOptions.bitmapTransform(new mBlurTransformation(this, 15, 4)))
                .into(appbarBg);


    }


    void RefreshFansAndFollowingNum() {
        fans.setText(R.string.fans);
        following.setText(R.string.following);
        new UserRelationHelper(CurrentUser)
                .QueryFansAndFollowingNum(false, new UserRelationHelper.QueryFansAndFollowingNumListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onFansResult(int count) {
                        fansNum = count;
                        int last = defaultSP.getInt("last_fans_num", 0);
                        if (fansNum > last) {
                            newFansNum.setVisibility(View.VISIBLE);
                            newFansNum.setText("+" + (fansNum - last));
                        } else {
                            newFansNum.setVisibility(View.GONE);
                        }
                        defaultSP.edit().putInt("last_fans_num", fansNum).apply();
                        fans.setText(getString(R.string.fans) + " " + count);
                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onFollowingResult(int count) {
                        followingNum = count;
                        following.setText(getString(R.string.following) + " " + count);
                    }

                    @Override
                    public void onFailed(Exception e) {
                        e.printStackTrace();
                        fans.setText(R.string.fans);
                        following.setText(R.string.following);
                    }
                });
    }

    void changeAvatar(String path) {
        final FragmentLoading fragmentLoading = FragmentLoading.newInstance(getString(R.string.changing));
        fragmentLoading.show(getSupportFragmentManager(), UUID.randomUUID().toString());
        final BmobFile file = new BmobFile(new File(path));

        file.upload(new UploadFileListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    if (!TextUtils.isEmpty(CurrentUser.getAvatarUri())) {
                        BmobFile bf = new BmobFile();
                        bf.setUrl(CurrentUser.getAvatarUri());
                        bf.delete(new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
//                                if (e == null) Log.e("删除原头像成功", "!");
//                                else e.printStackTrace();
                            }
                        });
                    }
                    CurrentUser.setAvatarUri(file.getFileUrl());
                    CurrentUser.update(new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            try {
                                fragmentLoading.dismiss();
                            } catch (Exception ignored) {

                            }
                            if (e == null) {
                                Toast.makeText(HContext, R.string.avatar_changed_success, Toast.LENGTH_SHORT).show();
                                defaultSP.edit().putString("avatarGlideSignature", String.valueOf(System.currentTimeMillis())).apply();
                                loadAvatar();
                            } else {
                                Toast.makeText(HContext, R.string.avatar_changed_failed, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


                } else {
                    try {
                        fragmentLoading.dismiss();
                    } catch (Exception ignored) {

                    }
                    Toast.makeText(HContext, R.string.avatar_changed_failed, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
