package com.stupidtree.hita.activities;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;

import com.stupidtree.hita.diy.MaterialCircleAnimator;
import com.stupidtree.hita.diy.mBlurTransformation;
import com.stupidtree.hita.online.HITAUser;

import java.util.List;


import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

import static com.stupidtree.hita.HITAApplication.HContext;

public class ActivityUserProfile extends BaseActivity
{
    ImageView appbarBg;
    CollapsingToolbarLayout mToolbarLayout;
    ImageView avatar;
    TextView name,signature,nick,school,grade;
    AppBarLayout appBarLayout;
    HITAUser user = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setWindowParams(true,true,false);
        initToolbar();
        initViews();
        loadUserInfos();
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
        mToolbarLayout = findViewById(R.id.usercenter_toolbarlayout);
        mToolbarLayout.setScrimAnimationDuration(300);
        mToolbarLayout.setScrimVisibleHeightTrigger(500);
        mToolbarLayout.setCollapsedTitleTextColor(ContextCompat.getColor(this,R.color.material_primary_text));
        appbarBg = findViewById(R.id.usercenter_appbar_image);
    }
    void initViews(){
        name = findViewById(R.id.profile_name);
        signature = findViewById(R.id.profile_signature);
        avatar = findViewById(R.id.profile_avatar);
        nick = findViewById(R.id.profile_nick);
        school = findViewById(R.id.profile_school);
        grade = findViewById(R.id.profile_grade);
    }
    void loadUserInfos(){
        BmobQuery<HITAUser> bq = new BmobQuery<>();
        bq.addWhereEqualTo("objectId",getIntent().getStringExtra("objectId"));
        bq.findObjects(new FindListener<HITAUser>() {
            @Override
            public void done(List<HITAUser> list, BmobException e) {
                if(e==null&&list!=null&&list.size()>0){
                    user = list.get(0);
                    nick.setText(user.getNick());
                    name.setText(user.getRealname());
                    school.setText(user.getSchool());
                    signature.setText(user.getSignature());
                    grade.setText(getGradeText(user.getStudentnumber()));
                    Glide.with(ActivityUserProfile.this).load(user.getAvatarUri()).apply(RequestOptions.circleCropTransform())
                            .placeholder(R.drawable.ic_account_activated).into(avatar);
                    Glide.with(ActivityUserProfile.this).load(user.getAvatarUri())
                            //.signature(new ObjectKey(Objects.requireNonNull(defaultSP.getString("avatarGlideSignature", String.valueOf(System.currentTimeMillis())))))
                            //.placeholder(R.drawable.ic_account_activated)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    appbarBg.setImageDrawable(resource);
                                    appbarBg.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            MaterialCircleAnimator.animShow(appbarBg,700);
                                        }
                                    });
                                    return true;
                                }
                            })
                            .apply(RequestOptions.bitmapTransform(new mBlurTransformation(ActivityUserProfile.this, 22, 6)))
                            .into(appbarBg);

                }else{
                    Toast.makeText(HContext,"获取用户信息失败！",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private  String getGradeText(String studentNumber){
        if(studentNumber.startsWith("SZ16")) return "16级";
        else if(studentNumber.startsWith("SZ17")) return "17级";
        else if(studentNumber.startsWith("18")) return "18级";
        else return "未知年级";
     }


}
