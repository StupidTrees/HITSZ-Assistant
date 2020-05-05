package com.stupidtree.hita.fragments.news;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.NewsLectureListAdapter;
import com.stupidtree.hita.fragments.BaseFragment;
import com.stupidtree.hita.util.ActivityUtils;
import com.stupidtree.hita.views.WrapContentLinearLayoutManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragmentNewsLecture extends BaseFragment {
    RecyclerView list;
    int offset = 0;
    private List<Map<String, String>> listRes;
    private NewsLectureListAdapter listAdapter;
    private SwipeRefreshLayout pullRefreshLayout;
    boolean first = true;
    LoadTask pageTask;



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initList(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        Refresh();
    }

    public void backToTop() {
        list.smoothScrollToPosition(0);
    }

    void initList(View v) {
        pullRefreshLayout = v.findViewById(R.id.pullrefresh);
        pullRefreshLayout.setColorSchemeColors(getColorAccent(), getColorAccent());
        list = v.findViewById(R.id.list);
        listRes = new ArrayList<>();
        listAdapter = new NewsLectureListAdapter(this.requireContext(), listRes);
        list.setAdapter(listAdapter);
        RecyclerView.LayoutManager layoutManager = new WrapContentLinearLayoutManager(this.requireContext(), RecyclerView.VERTICAL, false);
        list.setLayoutManager(layoutManager);
        list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                int totalItemCount = recyclerView.getAdapter().getItemCount();
                int lastVisibleItemPosition = lm.findLastVisibleItemPosition();
                int visibleItemCount = recyclerView.getChildCount();

                if (newState == RecyclerView.SCROLL_STATE_IDLE
                        && lastVisibleItemPosition == totalItemCount - 1
                        && visibleItemCount > 0) {
                    offset += 10;
                    Refresh();
                }
            }
        });
        listAdapter.setOnItemClickListener(new NewsLectureListAdapter.OnItemClickListener() {
            @Override
            public void OnClick(View v, int pos) {
                //ActivityOptionsCompat op = ActivityOptionsCompat.makeSceneTransitionAnimation(FragmentNewsLecture.this.getActivity(),v,"cardview");
                ActivityUtils.startNewsActivity(getActivity(), listRes.get(pos).get("link"), listRes.get(pos).get("title"));
            }
        });
        listAdapter.setmOnNaviClickListener(new NewsLectureListAdapter.OnNaviClickListener() {

            @Override
            public void OnClick(View v, String termial) {
                ActivityUtils.startExploreActivity_forNavi(getActivity(), termial);
            }
        });
        pullRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Refresh();
                first = true;
            }
        });
    }


    @Override
    protected void stopTasks() {
        if (pageTask != null && pageTask.getStatus() != AsyncTask.Status.FINISHED)
            pageTask.cancel(true);
    }

    @Override
    public void Refresh() {
        if (pageTask != null && pageTask.getStatus() != AsyncTask.Status.FINISHED)
            pageTask.cancel(true);
        pageTask = new LoadTask(false);
        pageTask.executeOnExecutor(HITAApplication.TPE);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_news;
    }


    class LoadTask extends AsyncTask {
        boolean swipe;

        LoadTask(boolean swipe) {
            this.swipe = swipe;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (swipe) pullRefreshLayout.setRefreshing(true);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                Document d = Jsoup.connect("http://www.hitsz.edu.cn/article/id-78.html?maxPageItems=10&keywords=&pager.offset=" + offset)
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .get();
                Elements e = d.select("ul[class^=lecture_n]");
                Elements ee = e.select("li");
                for (Element x : ee) {
                    Map<String, String> lecture = new HashMap<>();
                    String title = x.select("a").text();
                    String link = x.select("a").attr("href");
                    String host, place, time;
                    host = x.select("div[class^=lecture_bottom]").select("div:contains(人)").size() > 0 ? x.select("div[class^=lecture_bottom]").select("div:contains(人)").get(1).text() : "";
                    place = x.select("div[class^=lecture_bottom]").select("div:contains(讲座地点)").size() > 0 ? x.select("div[class^=lecture_bottom]").select("div:contains(讲座地点)").get(1).text() : "";
                    time = x.select("div[class^=lecture_bottom]").select("div:contains(讲座时间)").size() > 0 ? x.select("div[class^=lecture_bottom]").select("div:contains(讲座时间)").get(1).text() : "";
                    String releaseTime = x.select("[class=date_t]").text();
                    String view = x.select("[class=view]").text();
                    String image = x.select("img").attr("src");

                    // Log.e("!!",date);
                    lecture.put("title", title);
                    lecture.put("host", host);
                    lecture.put("place", place);
                    lecture.put("time", time);
                    lecture.put("releasetime", releaseTime);
                    lecture.put("view", view);
                    lecture.put("picture", image);
                    lecture.put("link", link);
                    listRes.add(lecture);
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            pullRefreshLayout.setRefreshing(false);
            if (first) {
                //MaterialCircleAnimator.animShow(list, 700);
                list.scheduleLayoutAnimation();
                first = false;
            }
            listAdapter.notifyDataSetChanged();
        }
    }


}
