package com.stupidtree.hita.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivityNewsDetail;
import com.stupidtree.hita.adapter.IpNewsListAdapter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragmentNewsIPNews extends BaseFragment implements FragmentNews {
    RecyclerView list;
    int offset = 0;
    List<Map<String,String>> listRes;
    IpNewsListAdapter listAdapter;
    SwipeRefreshLayout pullRefreshLayout;
    boolean first = true;
    LoadTask pageTask;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_news_ipnews,container,false);
        initList(v);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        Refresh();
    }

    public void backToTop(){
        list.smoothScrollToPosition(0);
    }

    void initList(View v){
        pullRefreshLayout = v.findViewById(R.id.pullrefresh);
        list = v.findViewById(R.id.news_list);
        listRes = new ArrayList<>();
        listAdapter = new IpNewsListAdapter(this.getContext(),listRes);
        list.setAdapter(listAdapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.getContext(),LinearLayoutManager.VERTICAL,false);
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
                    offset+=10;
                    if(pageTask!=null&&!pageTask.isCancelled())pageTask.cancel(true);
                    pageTask = new LoadTask(true);
                    pageTask.execute();
                }
            }
        });
        listAdapter.setOnItemClickListener(new IpNewsListAdapter.OnItemClickListener() {
            @Override
            public void OnClick(View v, int pos) {
                //ActivityOptionsCompat op = ActivityOptionsCompat.makeSceneTransitionAnimation(FragmentNewsIPNews.this.getActivity(),v,"cardview");
                Intent i = new Intent(FragmentNewsIPNews.this.getActivity(), ActivityNewsDetail.class);
                i.putExtra("link",listRes.get(pos).get("link"));
                i.putExtra("title",listRes.get(pos).get("title"));
                FragmentNewsIPNews.this.getActivity().startActivity(i);
            }
        });
        pullRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                first = true;
                if(pageTask!=null&&!pageTask.isCancelled())pageTask.cancel(true);
                pageTask = new LoadTask(true);
                pageTask.execute();
            }
        });
    }

    @Override
    protected void stopTasks() {
        if(pageTask!=null&&!pageTask.isCancelled())pageTask.cancel(true);
    }

    @Override
    protected void Refresh() {
        if(pageTask!=null&&!pageTask.isCancelled())pageTask.cancel(true);
        pageTask = new LoadTask(false);
        pageTask.execute();
    }

    class LoadTask extends AsyncTask{

        boolean swipe;
        LoadTask(boolean swipe){
            this.swipe = swipe;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
         if(swipe)pullRefreshLayout.setRefreshing(true);

        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                Document d = Jsoup.connect("http://www.hitsz.edu.cn/article/id-116.html?maxPageItems=20&keywords=&pager.offset="+offset)
                        .header("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                        .header("X-Requested-With","XMLHttpRequest")
                        .get();
                Elements e = d.getElementsByClass("newsletters");
                for(Element x:e.select("li")){
                    Map news = new HashMap();
                    String title = x.select("a").text().replace("[详细]","").replace("[转发]","");
                    String link = x.select("a").attr("href");
                    String time =x.select("[class=date]").text();
                    String view = x.select("[class=view]").text().substring(4);
                    String subtitle = x.select("[class=text]").text();
                    String image = x.select("img").attr("src");
                    news.put("time",time);
                    news.put("subtitle",subtitle);
                    news.put("view",view);
                    news.put("title",title);
                    news.put("image",image);
                    news.put("link",link);
                    listRes.add(news);
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
            listAdapter.notifyDataSetChanged();
            if(first) {
                //MaterialCircleAnimator.animShow(list, 700);
                list.scheduleLayoutAnimation();
                first = false;
            }

        }
    }


}
