package com.stupidtree.hita.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivityNewsDetail;
import com.stupidtree.hita.adapter.BulletinRecyclerAdapter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragmentNewsBulletin extends Fragment implements FragmentNews {
    RecyclerView list;
    BulletinRecyclerAdapter listAdapter;
    List<Map<String,String>> listRes;
    Toolbar toolbar;
    int offset = 0;
    SwipeRefreshLayout pullRefreshLayout;
    boolean first = true;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_news_bulletin,container,false);
        initList(v);
        return v;
    }

    @Override
    public void onResume() {
        new loadTask(false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        super.onResume();
    }

    public void backToTop(){
        list.smoothScrollToPosition(0);
    }
    void initList(View v){
        pullRefreshLayout = v.findViewById(R.id.pullrefresh);
        list = v.findViewById(R.id.bulletin_recyc);
        listRes = new ArrayList<>();
        listAdapter = new BulletinRecyclerAdapter(this.getContext(),listRes);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this.getContext(),LinearLayoutManager.VERTICAL,false);
        list.setLayoutManager(manager);
        listAdapter.setmOnItemClickListener(new BulletinRecyclerAdapter.OnItemClickListener() {
            @Override
            public void OnClick(View v, int position) {
                //ActivityOptionsCompat op = ActivityOptionsCompat.makeSceneTransitionAnimation(FragmentNewsBulletin.this.getActivity(),v,"cardview");
                Intent i = new Intent(FragmentNewsBulletin.this.getActivity(), ActivityNewsDetail.class);
                i.putExtra("link",listRes.get(position).get("link"));
                i.putExtra("title",listRes.get(position).get("title"));
                FragmentNewsBulletin.this.startActivity(i);
            }
        });
        list.setAdapter(listAdapter);
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
                    offset+=20;
                    new loadTask(true).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        });
        pullRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                first = true;
                new loadTask(true).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

    }

       class loadTask extends AsyncTask{

        boolean swipe;
        loadTask(boolean swipe){
            this.swipe = swipe;
        }
           @Override
           protected void onPreExecute() {
             super.onPreExecute();
            if(swipe) pullRefreshLayout.setRefreshing(true);
           }

           @Override
        protected Map<String,String> doInBackground(Object[] objects) {
            try {
                Document d = Jsoup.connect("http://www.hitsz.edu.cn/article/id-74.html?maxPageItems=20&keywords=&pager.offset="+offset)
                        .header("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                        .header("X-Requested-With","XMLHttpRequest")
                        .get();
                Elements annoucements = d.getElementsByClass("announcement");
                //System.out.println(annoucements);
                for(Element e:annoucements.select("li")){
                    String title = e.select("a").text();
                    String link = e.select("a").attr("href");
                    String views = e.select("[class=view]").text().substring(4);
                    String time = e.select("[class=date]").text();
                    Map m = new HashMap();
                    m.put("title",title);
                    m.put("link",link);
                    m.put("views",views);
                    m.put("time",time);
                    listRes.add(m);
                    //System.out.println("href="+link+",title="+title+",views="+views+",time="+time);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            listAdapter.notifyDataSetChanged();
            pullRefreshLayout.setRefreshing(false);
            if(first) {
                //MaterialCircleAnimator.animShow(list, 700);
                list.scheduleLayoutAnimation();
                first = false;
            }
        }
    }

}
