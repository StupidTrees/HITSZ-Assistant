package com.stupidtree.hita.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;

import com.stupidtree.hita.diy.MaterialCircleAnimator;
import com.stupidtree.hita.adapter.UserCenterPagerAdapter;
import com.stupidtree.hita.diy.mBlurTransformation;
import com.stupidtree.hita.jwts.FragmentJWTS_info;
import com.stupidtree.hita.fragments.FragmentUserCenter_Info;
import com.stupidtree.hita.fragments.FragmentSubjects;
import com.stupidtree.hita.fragments.FragmentUserCenter_sync;
import com.stupidtree.hita.util.FileOperator;
import com.yuyh.library.imgsel.ISNav;
import com.yuyh.library.imgsel.common.ImageLoader;
import com.yuyh.library.imgsel.config.ISListConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.clearData;

public class ActivityUserCenter extends BaseActivity implements FragmentSubjects.OnFragmentInteractionListener, FragmentJWTS_info.OnListFragmentInteractionListener
{
    private static final int REQUEST_LIST_CODE = 0;
    private static final int REQUEST_CAMERA_CODE = 1;
    ViewPager viewpager;
    UserCenterPagerAdapter pagerAdapter;
    List<Fragment> fragments;
    TabLayout tabLayout;
    ImageView appbarBg;
    CollapsingToolbarLayout mToolbarLayout;
    ImageView avatar;
    TextView name,signature;
    AppBarLayout appBarLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_center);
        setWindowParams(true,true,false);
        initToolbar();
        initPager();
        initUserView();
    }
    void initToolbar(){
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
        toolbar.inflateMenu(R.menu.menu_user_center);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(menuItem.getItemId()==R.id.action_logout){
                    AlertDialog ad = new AlertDialog.Builder(ActivityUserCenter.this).create();
                    ad.setMessage("注销后，本地所有数据将会被清空");
                    ad.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            BmobUser.logOut();
                            clearData();
                            CurrentUser = null;
                            finish();
                        }
                    });
                    ad.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
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
        mToolbarLayout.setCollapsedTitleTextColor(ContextCompat.getColor(this,R.color.material_primary_text));
        appbarBg = findViewById(R.id.usercenter_appbar_image);
        appbarBg.post(new Runnable() {
            @Override
            public void run() {
                MaterialCircleAnimator.animShow(appbarBg,700);
            }
        });
    }
    void initUserView(){
        name = findViewById(R.id.usercenter_name);
        signature = findViewById(R.id.usercenter_signature);
        avatar = findViewById(R.id.usercenter_avatar);
        //Glide.with(this).load(userInfos.get("头像")).into(avatar);
        name.setText(CurrentUser.getNick());
        signature.setText(CurrentUser.getSignature());
        loadAvatar();
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 自由配置选项
                @SuppressLint("ResourceType") ISListConfig config = new ISListConfig.Builder()
                        // 是否多选, 默认true
                        .multiSelect(false)
                        // 是否记住上次选中记录, 仅当multiSelect为true的时候配置，默认为true
                        .rememberSelected(false)
                        // 使用沉浸式状态栏
                        .statusBarColor(getColorPrimaryDark())
                        // 返回图标ResId
                        .backResId(R.drawable.bt_notes_toolbar_back)
                        // 标题
                        .title("图片")
                        // 标题文字颜色
                        .titleColor(Color.WHITE)
                        // TitleBar背景色
                        .titleBgColor(getColorPrimary())
                        // 裁剪大小。needCrop为true的时候配置
                        .cropSize(1, 1, 200, 200)
                        .needCrop(true)
                        // 第一个是否显示相机，默认true
                        .needCamera(false)
                        .build();
                ISNav x = ISNav.getInstance();
                x.init(new ImageLoader() {
                    @Override
                    public void displayImage(Context context, String path, ImageView imageView) {
                        Glide.with(context).load(path).into(imageView);
                    }
                });
                // 跳转到图片选择器
                x.toListActivity(ActivityUserCenter.this, config, REQUEST_LIST_CODE);
            }
        });
    }
    void initPager(){
        tabLayout = findViewById(R.id.usercenter_tablayout);
        viewpager = findViewById(R.id.usercenter_viewpager);
        String[] titles= {"个人资料","同步"};
        fragments = new ArrayList<>();
        fragments.add(new FragmentUserCenter_Info());
        fragments.add(new FragmentUserCenter_sync());
        pagerAdapter = new UserCenterPagerAdapter(getSupportFragmentManager(),fragments,Arrays.asList(titles));
        viewpager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewpager);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setTabIndicatorFullWidth(false);
        //tabLayout.setTabTextColors(ColorStateList.valueOf(getColorPrimary()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 图片选择结果回调
        if (requestCode == REQUEST_LIST_CODE && resultCode == RESULT_OK && data != null) {
            List<String> pathList = data.getStringArrayListExtra("result");
            new saveAvatarTask(pathList.get(0)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user_center,menu);
        return super.onCreateOptionsMenu(menu);
    }

    void loadAvatar(){
        if(TextUtils.isEmpty(CurrentUser.getAvatarUri())){
            avatar.setImageResource(R.drawable.ic_account_activated);
            //appbarBg.setImageResource(R.drawable.timeline_head_bg);
        }else {
            Glide.with(ActivityUserCenter.this).load(CurrentUser.getAvatarUri())
                    //.signature(new ObjectKey(Objects.requireNonNull(PreferenceManager.getDefaultSharedPreferences(HContext).getString("avatarGlideSignature", String.valueOf(System.currentTimeMillis())))))
                    //.placeholder(R.drawable.ic_account_activated)
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(avatar);
            Glide.with(ActivityUserCenter.this).load(CurrentUser.getAvatarUri())
                    //.signature(new ObjectKey(Objects.requireNonNull(PreferenceManager.getDefaultSharedPreferences(HContext).getString("avatarGlideSignature", String.valueOf(System.currentTimeMillis())))))
                    //.placeholder(R.drawable.ic_account_activated)
                    .apply(RequestOptions.bitmapTransform(new mBlurTransformation(this, 15, 4)))
                    .into(appbarBg);
        }

    }
    class saveAvatarTask extends AsyncTask{

        String path;
        saveAvatarTask(String s){
            path = s;
        }
        @Override
        protected Boolean doInBackground(Object[] objects) {
            Bitmap img = BitmapFactory.decodeFile(path);
            FileOperator.saveByteImageToFile(ActivityUserCenter.this.getFilesDir() + "/avatar_" + CurrentUser.getNick()+".png",img);
            final BmobFile file = new BmobFile(new File(ActivityUserCenter.this.getFilesDir()+"/"+"avatar_"+CurrentUser.getNick()+".png"));
            file.upload(new UploadFileListener() {
               @Override
               public void done(BmobException e) {
                   if (e == null) {
                       BmobFile bf = new BmobFile();
                       bf.setUrl(CurrentUser.getAvatarUri());
                       CurrentUser.setAvatarUri(file.getFileUrl());
                       CurrentUser.update(new UpdateListener() {
                           @Override
                           public void done(BmobException e) {
                               if(e==null){
                                   Toast.makeText(HContext, "更换头像成功", Toast.LENGTH_SHORT).show();
                                   PreferenceManager.getDefaultSharedPreferences(HContext).edit().putString("avatarGlideSignature", String.valueOf(System.currentTimeMillis())).commit();
                                   loadAvatar();
                               }else{
                                   Toast.makeText(HContext, "更换头像失败", Toast.LENGTH_SHORT).show();
                               }
                           }
                       });
                       bf.delete(new UpdateListener() {
                           @Override
                           public void done(BmobException e) {

                           }
                       });

                    }
                    else Toast.makeText(HContext,e.toString(),Toast.LENGTH_SHORT).show();
                }
            });
           return true;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

        }
    }
}
