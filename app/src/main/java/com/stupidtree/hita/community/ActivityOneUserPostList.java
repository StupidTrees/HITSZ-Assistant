package com.stupidtree.hita.community;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.BaseActivity;
import com.stupidtree.hita.fragments.BaseFragment;
import com.stupidtree.hita.online.HITAUser;
import com.stupidtree.hita.online.Post;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

import static com.stupidtree.hita.community.ActivityCommunity.REFRESH_RETURN;


public class ActivityOneUserPostList extends BaseActivity {


    Toolbar mToolbar;
    List<BaseFragment> fragments;
    ViewPager pager;

    TextView title, subtitle;
    ImageView cover, headBG;
    HITAUser user;

    @Override
    protected void stopTasks() {

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true, true, false);
        setContentView(R.layout.activity_one_post_list);
        user = (HITAUser) getIntent().getSerializableExtra("user");
        if (user == null) finish();
        initToolbar();
        initViews();
        initPager();
    }

    void initToolbar() {
        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle(getString(R.string.users_all_post_label, user.getNick()));
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
                    bq.addWhereEqualTo("author", user.getObjectId());
                    bq.setLimit(10);
                    //Log.e("skip", String.valueOf(skipSize));
                    bq.setSkip(skipSize);
                    bq.include("author.name,author.avatarUri");
                    bq.order("-createdAt");
                    if (useCache) {
                        bq.setCachePolicy(BmobQuery.CachePolicy.CACHE_ELSE_NETWORK);
                    } else {
                        bq.clearCachedResult(Post.class);
                        bq.setCachePolicy(BmobQuery.CachePolicy.NETWORK_ONLY);
                    }
                    bq.findObjects(new FindListener<Post>() {
                        @Override
                        public void done(List<Post> object, BmobException e) {
                            //Log.e("found", String.valueOf(object.size()));
                            if (e != null || object == null) listener.onFetchFailed(e);
                            else {
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
        FragmentPostsList f = FragmentPostsList.newInstance("", "one", true);
        fragments.add(f);
        pager = findViewById(R.id.laf_pager);
        //   tabs = findViewById(R.id.laf_tabs);
        pager.setAdapter(new lafPagerAdapter(getSupportFragmentManager()));
        // tabs.setupWithViewPager(pager);
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
