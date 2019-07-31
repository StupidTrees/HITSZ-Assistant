package com.stupidtree.hita.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivityExplore;
import com.stupidtree.hita.activities.ActivityNewsDetail;
import com.stupidtree.hita.adapter.LectureListAdapter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragmentNewsLecture extends Fragment implements FragmentNews {
    RecyclerView list;
    int offset = 0;
    List<Map<String, String>> listRes;
    LectureListAdapter listAdapter;
    SwipeRefreshLayout pullRefreshLayout;
    boolean first = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_news_lecture, container, false);
        initList(v);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        new LoadTask(false ).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void backToTop() {
        list.smoothScrollToPosition(0);
    }

    void initList(View v) {
        pullRefreshLayout = v.findViewById(R.id.pullrefresh);
        list = v.findViewById(R.id.lecture_list);
        listRes = new ArrayList<>();
        listAdapter = new LectureListAdapter(this.getContext(), listRes);
        list.setAdapter(listAdapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false);
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
                    new LoadTask(true).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        });
        listAdapter.setOnItemClickListener(new LectureListAdapter.OnItemClickListener() {
            @Override
            public void OnClick(View v, int pos) {
                //ActivityOptionsCompat op = ActivityOptionsCompat.makeSceneTransitionAnimation(FragmentNewsLecture.this.getActivity(),v,"cardview");
                Intent i = new Intent(FragmentNewsLecture.this.getActivity(), ActivityNewsDetail.class);
                i.putExtra("link", listRes.get(pos).get("link"));
                i.putExtra("title", listRes.get(pos).get("title"));
                FragmentNewsLecture.this.getActivity().startActivity(i);
            }
        });
        listAdapter.setmOnNaviClickListener(new LectureListAdapter.OnNaviClickListener() {

            @Override
            public void OnClick(View v, String termial) {
                Intent i = new Intent(FragmentNewsLecture.this.getActivity(), ActivityExplore.class);
                i.putExtra("terminal", termial);
                startActivity(i);
            }
        });
        pullRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new LoadTask(true).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                first = true;
            }
        });
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
                    Map lecture = new HashMap();
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
            } catch (IOException e) {
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