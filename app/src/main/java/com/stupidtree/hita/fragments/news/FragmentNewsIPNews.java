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
import com.stupidtree.hita.adapter.NewsIpNewsListAdapter;
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

public class FragmentNewsIPNews extends BaseFragment implements FragmentNews {
    private RecyclerView list;
    private int offset = 0;
    private List<Map<String, String>> listRes;
    private NewsIpNewsListAdapter listAdapter;
    private SwipeRefreshLayout pullRefreshLayout;
    private String pageCode;
    private boolean first = true;
    private LoadTask pageTask;

    public FragmentNewsIPNews() {
    }
    public static FragmentNewsIPNews getInstance(String pageCode){
        FragmentNewsIPNews r = new FragmentNewsIPNews();
        Bundle arg = new Bundle();
        arg.putString("code", pageCode);
        r.setArguments(arg);
        return r;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            pageCode = getArguments().getString("code");
        }
    }


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

    public void backToTop(){
        list.smoothScrollToPosition(0);
    }

    private void initList(View v) {
        pullRefreshLayout = v.findViewById(R.id.pullrefresh);
        pullRefreshLayout.setColorSchemeColors(getColorAccent(), getColorPrimary());
        list = v.findViewById(R.id.list);
        listRes = new ArrayList<>();
        listAdapter = new NewsIpNewsListAdapter(this.requireContext(), listRes);
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
                    offset+=10;
                    if(pageTask!=null&&pageTask.getStatus()!=AsyncTask.Status.FINISHED)pageTask.cancel(true);
                    pageTask = new LoadTask(true);
                    pageTask.executeOnExecutor(HITAApplication.TPE);
                }
            }
        });
        listAdapter.setOnItemClickListener(new NewsIpNewsListAdapter.OnItemClickListener() {
            @Override
            public void OnClick(View v, int pos) {
                //ActivityOptionsCompat op = ActivityOptionsCompat.makeSceneTransitionAnimation(FragmentNewsIPNews.this.getActivity(),v,"cardview");
                ActivityUtils.startNewsActivity(getActivity(),listRes.get(pos).get("link"),listRes.get(pos).get("title"));
            }
        });
        pullRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                first = true;
                if(pageTask!=null&&pageTask.getStatus()!=AsyncTask.Status.FINISHED)pageTask.cancel(true);
                pageTask = new LoadTask(true);
                pageTask.executeOnExecutor(HITAApplication.TPE);
            }
        });
    }

    @Override
    protected void stopTasks() {
        if(pageTask!=null&&pageTask.getStatus()!=AsyncTask.Status.FINISHED)pageTask.cancel(true);
    }

    @Override
    public void Refresh() {
        if(pageTask!=null&&pageTask.getStatus()!=AsyncTask.Status.FINISHED)pageTask.cancel(true);
        pageTask = new LoadTask(false);
        pageTask.executeOnExecutor(HITAApplication.TPE);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_news;
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
                Document d = Jsoup.connect("http://www.hitsz.edu.cn/article/id-"+pageCode+".html?maxPageItems=20&keywords=&pager.offset="+offset)
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
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            try {
                pullRefreshLayout.setRefreshing(false);
                listAdapter.notifyDataSetChanged();
                if(first) {
                    //MaterialCircleAnimator.animShow(list, 700);
                    list.scheduleLayoutAnimation();
                    first = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


}
