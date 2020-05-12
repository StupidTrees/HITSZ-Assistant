package com.stupidtree.hita.community;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.BaseActivity;
import com.stupidtree.hita.fragments.BaseFragment;
import com.stupidtree.hita.online.HITAUser;
import com.stupidtree.hita.online.Post;
import com.stupidtree.hita.online.Topic;
import com.stupidtree.hita.util.ActivityUtils;
import com.stupidtree.hita.views.CornerTransform;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

import static com.github.lzyzsd.circleprogress.Utils.dp2px;
import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.community.ActivityCommunity.REFRESH_RETURN;


public class ActivityOneTopic extends BaseActivity {


    Topic pageTopic;
    FloatingActionButton fab;
    Toolbar mToolbar;
    List<BaseFragment> fragments;
    ViewPager pager;
    // TabLayout tabs;

    TextView title, subtitle;
    ImageView cover, headBG;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true, false, false);
        setContentView(R.layout.activity_one_topic);
        initToolbar();
        initViews();
        initPager();
        pageTopic = (Topic) getIntent().getSerializableExtra("topic");
        fab = findViewById(R.id.fab_post);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (BmobUser.getCurrentUser(HITAUser.class) == null) {
                    Toast.makeText(HContext, R.string.log_in_first, Toast.LENGTH_SHORT).show();
                } else {
                    ActivityUtils.startCreatePostActivity(getThis(), pageTopic);
                }

            }
        });
        setInfoToView();
    }

    void initToolbar() {
        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle(getString(R.string.label_activity_one_topic));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REFRESH_RETURN) {
                if (data.hasExtra("mode")) {
                    ((FragmentPostsList) fragments.get(0)).respondRefreshRequest(data);
                }
                setResult(RESULT_OK, data);
            }
        }
    }

    void initViews() {
        headBG = findViewById(R.id.head_bg);
        title = findViewById(R.id.name);
        subtitle = findViewById(R.id.description);
        cover = findViewById(R.id.cover);
    }

    @Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        super.onAttachFragment(childFragment);
        if (childFragment instanceof FragmentPostsList) {
            ((FragmentPostsList) childFragment).setDataFetcher(new FragmentPostsList.DataFetcher() {
                @Override
                public void fetchData(boolean useCache, int skipSize, final FragmentPostsList.OnDataFetchedListener listener) {
                    BmobQuery<Post> bq = new BmobQuery();
                    bq.addWhereEqualTo("topic", pageTopic.getObjectId());
                    bq.setLimit(10);
                    bq.setSkip(skipSize);
                    bq.include("author.name,author.avatarUri");
                    bq.order("-createdAt");
                    if (useCache) {
                        bq.setCachePolicy(BmobQuery.CachePolicy.CACHE_ELSE_NETWORK);
                    } else {
                        bq.setCachePolicy(BmobQuery.CachePolicy.NETWORK_ONLY);
                    }
                    bq.findObjects(new FindListener<Post>() {
                        @Override
                        public void done(List<Post> object, BmobException e) {
                            if (e != null || object == null) listener.onFetchFailed(e);
                            else {
                                for (Post p : object) {
                                    p.setTopic(pageTopic);
                                }
                                listener.onDataFetched(object);
                            }
                        }
                    });
                }


            });

        }
    }

    void initPager() {
        fragments = new ArrayList<>();
        FragmentPostsList oneList = FragmentPostsList.newInstance("", "one", false);
        fragments.add(oneList);
        pager = findViewById(R.id.laf_pager);
        pager.setAdapter(new lafPagerAdapter(getSupportFragmentManager()));
    }

    void setInfoToView() {
        title.setText(pageTopic.getName());
        subtitle.setText(pageTopic.getDescription());
        CornerTransform cornerTransform = new CornerTransform(this, dp2px(getResources(), 8f));
        cornerTransform.setExceptCorner(false, false, false, false);
        Glide.with(this).load(pageTopic.getCover())
                .placeholder(R.drawable.ic_topic_gradient)
                .apply(RequestOptions.bitmapTransform(cornerTransform)).into(cover);
        // Glide.with(this).load()
        if (pageTopic == null || pageTopic.getType().contains("lock")) fab.hide();
        else fab.show();
    }


    class lafPagerAdapter extends FragmentPagerAdapter {

        public lafPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public Fragment getItem(int i) {
            return fragments.get(i);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }


    }
}
