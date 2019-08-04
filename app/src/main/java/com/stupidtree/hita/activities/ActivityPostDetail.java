package com.stupidtree.hita.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;
import com.stupidtree.hita.diy.CornerTransform;
import com.stupidtree.hita.online.HITAUser;
import com.stupidtree.hita.online.LostAndFound;
import com.stupidtree.hita.util.ActivityUtils;

import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.adapter.IpNewsListAdapter.dip2px;

public class ActivityPostDetail extends BaseActivity {
    LostAndFound laf;
    HITAUser authorUser;
    TextView author,title,content,time;
    ImageView avatar;
    ImageView image;
    ImageView delete;
    Toolbar toolbar;

    @Override
    protected void stopTasks() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true,true,false);
        setContentView(R.layout.activity_post_detail);
        laf = (LostAndFound) getIntent().getSerializableExtra("laf" );
        authorUser = (HITAUser) getIntent().getSerializableExtra("author");
        initViews();
        initToolbar();
        loadInfos();
    }

    void initToolbar(){
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("帖子详情");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
    
    void initViews(){
        author = findViewById(R.id.post_author);
        title = findViewById(R.id.post_title);
        content = findViewById(R.id.post_content);
        time = findViewById(R.id.post_time);
        avatar = findViewById(R.id.post_avatar);
        image = findViewById(R.id.post_image);
        delete = findViewById(R.id.post_delete);
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.startUserProfileActivity(ActivityPostDetail.this,authorUser.getObjectId(),v);
            }
        });
        if(CurrentUser!=null&& CurrentUser.getObjectId() .equals(laf.getAuthor().getObjectId())){
            delete.setVisibility(View.VISIBLE);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog ad = new AlertDialog.Builder(ActivityPostDetail.this).setTitle("提示").setMessage("确定删除该失物招领吗？").setNegativeButton("取消",null)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    final LostAndFound hp = laf;
                                    if(hp.getImageUri()!=null&&(!hp.getImageUri().isEmpty())){
                                        BmobFile bf = new BmobFile();
                                        bf.setUrl(hp.getImageUri());
                                        bf.delete(new UpdateListener() {
                                            @Override
                                            public void done(BmobException e) {
                                                hp.delete(new UpdateListener() {
                                                    @Override
                                                    public void done(BmobException e) {
                                                        Toast.makeText(HContext,"删除成功!",Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    }
                                                });
                                            }
                                        });
                                    }else{
                                        hp.delete(new UpdateListener() {
                                            @Override
                                            public void done(BmobException e) {
                                                Toast.makeText(HContext,"删除成功!",Toast.LENGTH_SHORT).show();
                                                finish();
                                            }
                                        });
                                    }
                                }
                            }).create();
                    ad.show();


                }
            });
        }else{
            delete.setVisibility(View.GONE);
        }
    }

    void loadInfos(){
        author.setText(authorUser.getNick());
        title.setText(laf.getTitle());
        content.setText(laf.getContent());
        time.setText(laf.getUpdatedAt());
        Glide.with(this).load(authorUser.getAvatarUri()).
        placeholder(R.drawable.ic_account_activated)
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(avatar);
        Transformation<Bitmap> transformation;
        transformation = new CornerTransform(ActivityPostDetail.this, dip2px(HContext, 10));
        Glide.with(this).load(laf.getImageUri()).apply(RequestOptions.bitmapTransform(transformation))
                .into(image);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent i = new Intent(ActivityPostDetail.this, ActivityPhotoDetail.class);
                ActivityUtils.startPhotoDetailActivity_transition(ActivityPostDetail.this,laf.getImageUri(),view);
            }
        });
    }
}
