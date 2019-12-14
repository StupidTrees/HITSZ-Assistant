package com.stupidtree.hita.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.stupidtree.hita.online.Location;
import com.stupidtree.hita.online.LostAndFound;
import com.stupidtree.hita.util.ActivityUtils;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.adapter.NewsIpNewsListAdapter.dip2px;

public class ActivityPostDetail extends BaseActivity {
    LostAndFound laf;
    HITAUser authorUser;
    TextView author,title,content,time;
    ImageView avatar;
    ImageView image;
    ImageView delete;
    Toolbar toolbar;
    TextView phone_text,location_text;
    LinearLayout location_layout;
    LinearLayout userLayout;

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
        userLayout = findViewById(R.id.user_layout);
        location_layout = findViewById(R.id.location_layout);
        location_text = findViewById(R.id.location_text);
        phone_text = findViewById(R.id.phone_text);
        author = findViewById(R.id.post_author);
        title = findViewById(R.id.post_title);
        content = findViewById(R.id.post_content);
        time = findViewById(R.id.post_time);
        avatar = findViewById(R.id.post_avatar);
        image = findViewById(R.id.post_image);
        delete = findViewById(R.id.post_delete);
        userLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(authorUser!=null&&!laf.isAnonymous())  ActivityUtils.startUserProfileActivity(ActivityPostDetail.this,authorUser.getObjectId(),avatar);
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

        title.setText(laf.getTitle());
        content.setText(laf.getContent());
        time.setText(laf.getCreatedAt());
        if(authorUser!=null){
            if(laf.isAnonymous()) {
                author.setText("匿名");
                image.setImageResource(R.drawable.ic_account);
            }
            else{
                author.setText(authorUser.getNick());
                Glide.with(this).load(authorUser.getAvatarUri()).placeholder(R.drawable.ic_account_activated)
                        .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                        .into(avatar);
            }
        }else if(laf.isAnonymous()){
            author.setText("匿名");
            image.setImageResource(R.drawable.ic_account);

        }

        Transformation<Bitmap> transformation;
        transformation = new CornerTransform(ActivityPostDetail.this, dip2px(HContext, 10));
        Glide.with(this).load(laf.getImageUri()).apply(RequestOptions.bitmapTransform(transformation))
                .into(image);
        if(TextUtils.isEmpty(laf.getContent())){
            content.setVisibility(View.GONE);
        }else content.setVisibility(View.VISIBLE);
        if(TextUtils.isEmpty(laf.getImageUri())){
            image.setVisibility(View.GONE);
        }else image.setVisibility(View.VISIBLE);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent i = new Intent(ActivityPostDetail.this, ActivityPhotoDetail.class);
                ActivityUtils.startPhotoDetailActivity_transition(ActivityPostDetail.this,laf.getImageUri(),view);
            }
        });
        if(TextUtils.isEmpty(laf.getContact())){
            phone_text.setText("未设置");
        }else{
            phone_text.setText(laf.getContact());
        }
        if(laf.getAuthor().getNick()==null&&!laf.isAnonymous()){
            BmobQuery<HITAUser> bq = new BmobQuery<>();
            bq.addWhereEqualTo("objectId",laf.getAuthor().getObjectId());
            bq.findObjects(new FindListener<HITAUser>() {
                @Override
                public void done(List<HITAUser> list, BmobException e) {
                    if(list!=null&&list.size()>0){
                        laf.setAuthor(list.get(0));
                        authorUser = list.get(0);
                        author.setText(authorUser.getNick());
                        Glide.with(HContext).load(authorUser.getAvatarUri()).
                                placeholder(R.drawable.ic_account_activated)
                                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                                .into(avatar);
                    }else{
                        author.setText("...");
                    }

                }
            });
        }
        if(laf.getLocation()!=null){
            if(laf.getLocation().getName()!=null){
                location_layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityUtils.startLocationActivity(ActivityPostDetail.this,laf.getLocation());
                    }
                });
                location_text.setText(laf.getLocation().getName());
            }else{
                location_layout.setOnClickListener(null);
                location_text.setText("...");
                BmobQuery<Location> bq = new BmobQuery<>();
                bq.addWhereEqualTo("objectId",laf.getLocation().getObjectId());
                bq.findObjects(new FindListener<Location>() {
                    @Override
                    public void done(List<Location> list, BmobException e) {
                        if(list!=null&&list.size()>0){
                            laf.setLocation(list.get(0));
                            location_text.setText(list.get(0).getName());
                            location_layout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ActivityUtils.startLocationActivity(ActivityPostDetail.this,laf.getLocation());
                                }
                            });
                        }else{
                            location_text.setText("加载失败");
                            location_layout.setOnClickListener(null);
                        }
                    }
                });
            }
        }else {
            location_layout.setOnClickListener(null);
            location_text.setText("未指定");
        }
    }



}
