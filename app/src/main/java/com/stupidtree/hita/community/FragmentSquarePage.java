package com.stupidtree.hita.community;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.BaseTabAdapter;
import com.stupidtree.hita.online.Post;
import com.stupidtree.hita.online.Topic;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;


public class FragmentSquarePage extends FragmentTabsPostPage {


    @Override
    protected void stopTasks() {

    }

    @Override
    public void Refresh() {
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initPager(view);
    }

    @Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        super.onAttachFragment(childFragment);
        if (fetcherMap == null) {
            fetcherMap = new HashMap<>();
            setFetcherForChildren();
        }
        // Log.e("attach", String.valueOf(childFragment));
        if (childFragment instanceof FragmentPostsList) {
            ((FragmentPostsList) childFragment).setDataFetcher(fetcherMap.get(((FragmentPostsList) childFragment).getIdInParent()));
        }
    }

    @Override
    public boolean canShowFabNow() {
        if (!isResumed()) return false;
        return pager.getCurrentItem() != 2;
    }

    private void setFetcherForChildren() {
        fetcherMap.put("latest_posts", new FragmentPostsList.DataFetcher() {
            @Override
            public void fetchData(boolean useCache, int skipSize, final FragmentPostsList.OnDataFetchedListener listener) {
                BmobQuery<Post> bq = new BmobQuery<>();
                bq.setLimit(10);
                bq.setSkip(skipSize);
                bq.include("topic,author.name,author,avatarUri");
                bq.order("-createdAt");
                if (useCache) {
                    bq.setCachePolicy(BmobQuery.CachePolicy.CACHE_ELSE_NETWORK);
                } else {
                    bq.setCachePolicy(BmobQuery.CachePolicy.NETWORK_ONLY);
                }
                bq.findObjects(new FindListener<Post>() {
                    @Override
                    public void done(List<Post> object, BmobException e) {
                        if (e != null || object == null) {
                            listener.onFetchFailed(e);
                        } else {
                            listener.onDataFetched(object);
                        }
                    }
                });

            }


        });

        fetcherMap.put("hottest_posts",
                new FragmentPostsList.DataFetcher() {
                    @Override
                    public void fetchData(boolean useCache, int skipSize, final FragmentPostsList.OnDataFetchedListener listener) {
                        BmobQuery<Post> bq = new BmobQuery<>();
                        Calendar c = Calendar.getInstance();
                        c.add(Calendar.DATE, -14);
                        c.set(Calendar.HOUR_OF_DAY, 0);
                        c.set(Calendar.MINUTE, 0);
                        bq.addWhereGreaterThanOrEqualTo("createdAt", new BmobDate(c.getTime()));
                        bq.order("-likeNum");
                        bq.setLimit(10);
                        bq.setSkip(skipSize);
                        bq.include("topic,author.name,author,avatarUri");
                        if (useCache) {
                            bq.setCachePolicy(BmobQuery.CachePolicy.CACHE_ELSE_NETWORK);
                        } else {
                            bq.setCachePolicy(BmobQuery.CachePolicy.NETWORK_ONLY);
                        }
                        bq.findObjects(new FindListener<Post>() {
                            @Override
                            public void done(List<Post> object, BmobException e) {
                                if (e != null || object == null) {
                                    listener.onFetchFailed(e);
                                } else {
                                    listener.onDataFetched(object);
                                }
                            }
                        });

                    }


                });

        fetcherMap.put("bulletin_post",
                new FragmentPostsList.DataFetcher() {
                    @Override
                    public void fetchData(boolean useCache, int skipSize, final FragmentPostsList.OnDataFetchedListener listener) {
                        BmobQuery<Topic> innerQuery = new BmobQuery<>();
                        String[] types = new String[]{"normal-lock", "high_lock", "basic-lock", "low-lock", "lock"};
                        innerQuery.addWhereContainedIn("type", Arrays.asList(types));
                        BmobQuery<Post> query = new BmobQuery<>();
                        query.include("author.name,author.avatarUri,topic.name");
                        query.order("-createdAt");
                        query.addWhereMatchesQuery("topic", "Topic", innerQuery);
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


                });
    }

    private void initPager(View v) {
        pager = v.findViewById(R.id.laf_pager);
        tabs = v.findViewById(R.id.laf_tabs);
        final int[] titles = new int[]{R.string.latest, R.string.hottest, R.string.tabs_theta_bulletin};
        pagerAdapter = new BaseTabAdapter(getChildFragmentManager(), 3) {

            @NonNull
            @Override
            public CharSequence getPageTitle(int position) {
                return getString(titles[position]);
            }

            @Override
            protected Fragment initItem(int position) {
                switch (position) {
                    case 0:
                        return FragmentPostsList.newInstance(getString(R.string.latest), "latest_posts", true);
                    case 1:
                        return FragmentPostsList.newInstance(getString(R.string.hottest), "hottest_posts", true);
                    case 2:
                        return FragmentPostsList.newInstance(getString(R.string.tabs_theta_bulletin), "bulletin_post", true);
                }
                return null;
            }
        };
        pager.setAdapter(pagerAdapter);
        pager.setOffscreenPageLimit(8);
        tabs.setupWithViewPager(pager);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                try {
                    if (position == 2 && communityRoot != null)
                        communityRoot.hideFab();
                    else if (communityRoot != null) communityRoot.showFab();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


}
