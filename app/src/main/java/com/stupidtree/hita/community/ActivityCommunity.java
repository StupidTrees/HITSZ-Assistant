package com.stupidtree.hita.community;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.BaseActivity;
import com.stupidtree.hita.adapter.BaseTabAdapter;
import com.stupidtree.hita.online.Comment;
import com.stupidtree.hita.online.HITAUser;
import com.stupidtree.hita.online.Post;
import com.stupidtree.hita.online.Topic;
import com.stupidtree.hita.util.ActivityUtils;
import com.stupidtree.hita.views.BadgeActionProvider;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.FindListener;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.bmobCacheHelper;


public class ActivityCommunity extends BaseActivity implements FragmentTopicsPage.CommunityRoot {


    public static final int REFRESH_RETURN = 368;
    public static final int MODE_REFRESH_ALL = 731;
    public static final int MODE_REFRESH_ITEM = 247;
    public static final int MODE_REMOVE_ITEM = 248;
    FloatingActionButton fab;
    Toolbar mToolbar;
    ViewPager pager;
    BaseTabAdapter pagerAdapter;
    TabLayout tabs;
    String[] titles;
    BadgeActionProvider mBadgeActionProvider;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true, true, false);
        setContentView(R.layout.activity_community);
        initToolbar();
        initViews();
        initPager();
    }

    void initViews() {
        fab = findViewById(R.id.fab_post);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (BmobUser.getCurrentUser(HITAUser.class) == null) {
                    Toast.makeText(HContext, R.string.log_in_first, Toast.LENGTH_SHORT).show();
                } else {
                    Topic t = null;
                    if (pagerAdapter.getCurrentFragment() instanceof FragmentTopicsPage) {
                        t = ((FragmentTopicsPage) pagerAdapter.getCurrentFragment()).getCurrentTopic();
                    }
                    ActivityUtils.startCreatePostActivity(getThis(), t);
                    //FragmentAddLAF.newInstance(pager.getCurrentItem()==0?FragmentAddLAF.LOST:FOUND).show(getSupportFragmentManager(),"add_laf");
                }

            }
        });
        fab.setBackgroundTintList(ColorStateList.valueOf(getColorAccent()));
        fab.setImageTintList(ColorStateList.valueOf(Color.WHITE));
    }


    void initToolbar() {
        mToolbar = findViewById(R.id.toolbar);
        mToolbar.inflateMenu(R.menu.toolbar_community);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
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
                if (data != null && data.hasExtra("mode")) {
                    for (Fragment x : getSupportFragmentManager().getFragments()) {
                        if (x instanceof FragmentTabsPostPage) {
                            ((FragmentTabsPostPage) x).callEveryoneToRefresh(data);
                        } else if (x instanceof FragmentPostsList) {
                            ((FragmentPostsList) x).respondRefreshRequest(data);
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (CurrentUser != null) getUnreadMessage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_community, menu);
        MenuItem menuItem = menu.findItem(R.id.action_download);
        mBadgeActionProvider = (BadgeActionProvider) MenuItemCompat.getActionProvider(menuItem);
        mBadgeActionProvider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getThis(), ActivityMessageBox.class);
                startActivity(i);
                //  ToastUtils.showShort("下载管理");
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    void initPager() {

        titles = getResources().getStringArray(R.array.laf_tabs);
        pager = findViewById(R.id.laf_pager);
        pager.setOffscreenPageLimit(3);
        tabs = findViewById(R.id.laf_tabs);
        pagerAdapter = new BaseTabAdapter(getSupportFragmentManager(), 3) {
            @Override
            protected Fragment initItem(int position) {
                switch (position) {
                    case 0:
                        return new FragmentSquarePage();
                    case 1:
                        return new FragmentTopicsPage();
                    case 2:
                        return FragmentPostsList.newInstance("", "friends_posts", true);
                }
                return null;
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return titles[position];
            }
        };
        pager.setAdapter(pagerAdapter);
        tabs.setupWithViewPager(pager);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Fragment selected = pagerAdapter.getCurrentFragment();
                if (selected instanceof FragmentTabsPostPage) {
                    if (((FragmentTabsPostPage) selected).canShowFabNow())
                        showFab();
                    else hideFab();
                } else showFab();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void hideFab() {
        if (fab != null) fab.hide();
    }

    @Override
    public void showFab() {
        if (fab != null) fab.show();
    }

    @Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        super.onAttachFragment(childFragment);
        if (childFragment instanceof FragmentPostsList) {
            ((FragmentPostsList) childFragment).setDataFetcher(new FragmentPostsList.DataFetcher() {


                @Override
                public void fetchData(final boolean useCache, final int skipSize, final FragmentPostsList.OnDataFetchedListener listener) {
                    if (CurrentUser == null) {
                        listener.onFetchFailed(null);
                        return;
                    }
                    new UserRelationHelper(CurrentUser).QueryFollowingObjectId(bmobCacheHelper.willMyFollowingIdUseCache(), new UserRelationHelper.QueryFollowingObjectIdListener() {
                        @Override
                        public void onResult(List<String> result) {
                            result.add(CurrentUser.getObjectId());
                            BmobQuery<HITAUser> innerQuery = new BmobQuery<HITAUser>();
                            innerQuery.addWhereContainedIn("objectId", result);
                            BmobQuery<Post> query = new BmobQuery<Post>();
                            query.include("author.name,author.avatarUri,topic.name");
                            query.order("-createdAt");
                            query.addWhereMatchesQuery("author", "_User", innerQuery);
                            query.setLimit(10);
                            query.setSkip(skipSize);
                            if (useCache) {
                                query.setCachePolicy(BmobQuery.CachePolicy.CACHE_ELSE_NETWORK);
                            } else {
                                query.setCachePolicy(BmobQuery.CachePolicy.NETWORK_ONLY);
                            }
                            query.findObjects(new FindListener<Post>() {
                                @Override
                                public void done(List<Post> object, BmobException e) {
                                    if (e != null || object == null) listener.onFetchFailed(e);
                                    else listener.onDataFetched(object);
                                }
                            });
                        }

                        @Override
                        public void onFailed(Exception e) {
                            listener.onFetchFailed(e);
                        }
                    });

                }


            });
        }
    }

    void getUnreadMessage() {

        BmobQuery<Comment> bq1 = new BmobQuery<>();
        bq1.addWhereEqualTo("toUser", CurrentUser.getObjectId());
        BmobQuery<Comment> bq2 = new BmobQuery<>();
        bq2.addWhereNotEqualTo("from", CurrentUser.getObjectId());
        BmobQuery<Comment> bq3 = new BmobQuery<>();
        bq3.addWhereEqualTo("read", false);
        BmobQuery<Comment> q = new BmobQuery<>();
        q.and(Arrays.asList(bq1, bq2, bq3));
        q.count(Comment.class, new CountListener() {
            @Override
            public void done(Integer count, BmobException e) {
                if (e == null) mBadgeActionProvider.setBadge(count);
                else mBadgeActionProvider.setBadge(0);
            }
        });

    }




}
