package com.stupidtree.hita.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;
import com.stupidtree.hita.diy.PickInfoDialog;
import com.stupidtree.hita.fragments.FragmentLostAndFound;
import com.stupidtree.hita.online.Location;
import com.stupidtree.hita.online.LostAndFound;
import com.stupidtree.hita.online.HITAUser;
import com.yuyh.library.imgsel.ISNav;
import com.yuyh.library.imgsel.common.ImageLoader;
import com.yuyh.library.imgsel.config.ISListConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UploadFileListener;

import static com.stupidtree.hita.HITAApplication.HContext;


public class ActivityLostAndFound extends BaseActivity implements FragmentLostAndFound.OnFragmentInteractionListener{


    FloatingActionButton fab;
    Toolbar mToolbar;
    postListener mPostListener;
    List<FragmentLostAndFound> fragments;
    ViewPager pager;
    TabLayout tabs;
    String[] titles;


    private static final int REQUEST_LIST_CODE = 0;
    private static final int REQUEST_CAMERA_CODE = 1;

    @Override
    protected void stopTasks() {

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true,true,false);
        setContentView(R.layout.activity_lostandfound);
        initPager();
        fab = findViewById(R.id.fab_post);
        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle("失物招领");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mPostListener = new postListener();
        fab.setOnClickListener(mPostListener);
    }

    void initPager(){
        fragments = new ArrayList<>();
        fragments.add(FragmentLostAndFound.newInstance("lost"));
        fragments.add(FragmentLostAndFound.newInstance("found"));
        titles = new String[]{"寻物启事","失物招领"};
        pager = findViewById(R.id.laf_pager);
        tabs = findViewById(R.id.laf_tabs);
        pager.setAdapter(new lafPagerAdapter(getSupportFragmentManager()));
        tabs.setupWithViewPager(pager);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // 图片选择结果回调
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LIST_CODE && resultCode == RESULT_OK && data != null) {
            List<String> pathList = data.getStringArrayListExtra("result");
            if (pathList.size() > 0) {
                mPostListener.URI = pathList.get(0);
                mPostListener.image.setVisibility(View.VISIBLE);
                mPostListener.add.setVisibility(View.GONE);
                Glide.with(ActivityLostAndFound.this).load(mPostListener.URI).into(mPostListener.image);
            }
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    class postListener implements View.OnClickListener {
        View adv;
        EditText title;
        EditText content,contact;
        AlertDialog ad;
        ImageView image,clear_location;
        LinearLayout add;
        String URI;
        TextView defaultText,pickLocation;
       LostAndFound p;

        postListener(){
            p = new LostAndFound();
            adv = getLayoutInflater().inflate(R.layout.dialog_add_laf,null);
            contact = adv.findViewById(R.id.edit_contact);
            pickLocation = adv.findViewById(R.id.location_text);
            title = adv.findViewById(R.id.edit_title);
            clear_location = adv.findViewById(R.id.location_clear);
            content = adv.findViewById(R.id.edit_content);
            image = adv.findViewById(R.id.laf_image);
            add = adv.findViewById(R.id.laf_add);
            defaultText = adv.findViewById(R.id.title_default);
            ad = new AlertDialog.Builder(ActivityLostAndFound.this).setTitle("发布失物招领").setView(adv).create();
            add.setVisibility(View.VISIBLE);
            image.setVisibility(View.GONE);
            pickLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new PickInfoDialog(ActivityLostAndFound.this, "选择地点", PickInfoDialog.LOCATION_ALL, new PickInfoDialog.OnPickListener() {
                        @Override
                        public void OnPick(String title, Object obj) {
                            if(obj instanceof Location){
                                p.setLocation((Location) obj);
                                pickLocation.setText(title);
                            }
                        }
                    }).show();
                }
            });
            clear_location.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pickLocation.setText("不设置地点");
                    p.setLocation(null);
                }
            });
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ISListConfig config = new ISListConfig.Builder()
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
                            .cropSize(96, 54, 960, 540)
                            .needCrop(true)
                            // 第一个是否显示相机，默认true
                            .needCamera(false)
                            .build();
                    ISNav x = ISNav.getInstance();
                    x.init(new ImageLoader() {
                        @Override
                        public void displayImage(Context context, String path, ImageView imageView) {
                            //new mImageLoader().loadImage(path,imageView);
                            Glide.with(context).load(path).into(imageView);
                        }
                    });
                    // 跳转到图片选择器
                    x.toListActivity(ActivityLostAndFound.this, config, REQUEST_LIST_CODE);
                }
            });
            ad.setButton(DialogInterface.BUTTON_POSITIVE, "发布", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    p.setAuthor(BmobUser.getCurrentUser(HITAUser.class));
                    p.setTitle((pager.getCurrentItem()==1?"找到":"丢失")+title.getText().toString());
                    p.setContent(content.getText().toString());
                    p.setContact(contact.getText().toString());
                    p.setType(pager.getCurrentItem()==1?"found":"lost");
                    if(URI!=null){
                        final BmobFile bf = new BmobFile(new File(URI));
                        bf.upload(new UploadFileListener() {
                            @Override
                            public void done(BmobException e) {
                                if(e==null){
                                    p.setImageUri(bf.getFileUrl());
                                    p.save(new SaveListener<String>() {
                                        @Override
                                        public void done(String s, BmobException e) {
                                            Toast.makeText(HContext,"成功！",Toast.LENGTH_SHORT).show();
                                            for(FragmentLostAndFound flaf:fragments){
                                                flaf.Refresh();
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }else{
                        p.save(new SaveListener<String>() {
                            @Override
                            public void done(String s, BmobException e) {
                                Toast.makeText(HContext,"成功！",Toast.LENGTH_SHORT).show();
                                for(FragmentLostAndFound flaf:fragments){
                                    flaf.Refresh();
                                }
                            }
                        });
                    }


                }
            });
            ad.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ad.dismiss();
                }
            });
            ad.setCancelable(false);


        }


        @Override
        public void onClick(View v) {
            if(BmobUser.getCurrentUser(HITAUser.class)==null){
                Toast.makeText(HContext,"请先登录！",Toast.LENGTH_SHORT).show();
                return; }
            defaultText.setText(pager.getCurrentItem()==1?"找到":"丢失");
            ad.setTitle("发布"+(pager.getCurrentItem()==1?"失物招领":"寻物启事"));
            add.setVisibility(View.VISIBLE);
            image.setVisibility(View.GONE);
            ad.show();
        }
    }

    class lafPagerAdapter extends FragmentPagerAdapter{

        public lafPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            return fragments.get(i);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }
}
