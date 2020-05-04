package com.stupidtree.hita.community;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.snackbar.Snackbar;
import com.stupidtree.hita.R;
import com.stupidtree.hita.online.Post;
import com.stupidtree.hita.online.Topic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

import static com.stupidtree.hita.HITAApplication.bmobCacheHelper;


public class FragmentTopicsPage extends FragmentTabsPostPage {

    private List<Topic> topics;


    @Override
    protected void stopTasks() {

    }

    @Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        super.onAttachFragment(childFragment);
        if (childFragment instanceof FragmentPostsList && fetcherMap != null) {
            ((FragmentPostsList) childFragment).setDataFetcher(fetcherMap.get(((FragmentPostsList) childFragment).getIdInParent()));
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initPager(view);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void Refresh() {
        GetBasicTopics();
    }

    @Override
    public boolean canShowFabNow() {
        return getCurrentTopic() != null && !getCurrentTopic().getType().contains("lock");
    }

    Topic getCurrentTopic() {
        if (pager == null) return null;
        int curPos = pager.getCurrentItem();
        if (curPos < topics.size()) return topics.get(curPos);
        else return null;
    }

    void initPager(View v) {
        fetcherMap = new HashMap<>();
        pager = v.findViewById(R.id.laf_pager);
        tabs = v.findViewById(R.id.laf_tabs);
        pagerAdapter = new lafPagerAdapter(getChildFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.setOffscreenPageLimit(8);
        tabs.setupWithViewPager(pager);
        topics = new ArrayList<>();
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                try {
                    if (position >= topics.size() && communityRoot != null) communityRoot.hideFab();
                    else if (topics.get(position).getType().contains("lock") && communityRoot != null)
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

    void callEveryoneToRefresh(Intent intent) {
        for (Fragment fp : getChildFragmentManager().getFragments()) {
            if (fp instanceof FragmentPostsList) {
                FragmentPostsList fpp = (FragmentPostsList) fp;
                fpp.respondRefreshRequest(intent);
            }
        }
    }

    private void GetBasicTopics() {
        BmobQuery<Topic> bq1 = new BmobQuery<>();
        bq1.addWhereEqualTo("type", "basic");
        BmobQuery<Topic> bq2 = new BmobQuery<>();
        bq2.addWhereEqualTo("type", "basic-lock");
        final BmobQuery<Topic> query = new BmobQuery<>();
        query.or(Arrays.asList(bq1, bq2));
        if (bmobCacheHelper.willBasicTopicsUseCache()) {
            query.setCachePolicy(BmobQuery.CachePolicy.CACHE_ELSE_NETWORK);
        } else {
            query.setCachePolicy(BmobQuery.CachePolicy.NETWORK_ONLY);
        }

        query.findObjects(new FindListener<Topic>() {
            @Override
            public void done(List<Topic> object, BmobException e) {
                topics.clear();
                if (object != null) topics.addAll(object);
                Collections.sort(topics, new Comparator<Topic>() {
                    @Override
                    public int compare(Topic o1, Topic o2) {
                        return o1.compareTo(o2);
                    }
                });
                bmobCacheHelper.basicTopicsRelease();
                List<Fragment> fragments = new ArrayList<>();
                for (final Topic t : topics) {
                    FragmentPostsList f = FragmentPostsList.newInstance(t.getName(), t.getObjectId() + "_posts", true);
                    fragments.add(f);
                    fetcherMap.put(f.getIdInParent(), new FragmentPostsList.DataFetcher() {
                        @Override
                        public void fetchData(boolean useCache, int skipSize, final FragmentPostsList.OnDataFetchedListener listener) {
                            BmobQuery<Post> bq = new BmobQuery<>();
                            bq.addWhereEqualTo("topic", t.getObjectId());
                            bq.setLimit(10);
                            bq.setSkip(skipSize);
                            bq.include("author.name,author,avatarUri");
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
                                        for (Post p : object) {
                                            p.setTopic(t);
                                        }
                                        listener.onDataFetched(object);
                                    }
                                }
                            });

                        }


                    });
                }
                if (fragments.size() > 0) fragments.add(new FragmentTopics());
                if (fragments.size() == 0)
                    Snackbar.make(pager, R.string.search_connect_error, Snackbar.LENGTH_SHORT).show();
                if (pagerAdapter instanceof lafPagerAdapter) {
                    ((lafPagerAdapter) pagerAdapter).notifyFragmentsChanged(fragments);
                }
            }
        });
    }

    class lafPagerAdapter extends FragmentStatePagerAdapter {


        Fragment[] fragments;

        lafPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            fragments = new Fragment[0];
        }

        void notifyFragmentsChanged(List<Fragment> f) {
            fragments = new Fragment[f.size()];
            for (int i = 0; i < f.size(); i++) fragments[i] = f.get(i);
            notifyDataSetChanged();
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            try {
                super.destroyItem(container, position, object);
                container.removeView(fragments[position].getView());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @NonNull
        @Override
        public Fragment getItem(int i) {
            return fragments[i];
        }

        @NonNull
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            fragments[position] = fragment;
            return fragment;
        }

        @Override
        public int getCount() {
            return fragments.length;
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            Fragment bf = fragments[position];
            if (bf instanceof FragmentPostsList) {
                FragmentPostsList fpl = (FragmentPostsList) bf;
                return fpl.getTitle();
            } else if (bf instanceof FragmentTopics) {
                return getString(R.string.more_topics);
            }
            return "";
        }
    }

}
