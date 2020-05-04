package com.stupidtree.hita.fragments.news;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.NewsBulletinRecyclerAdapter;
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

public class FragmentNewsBulletin extends BaseFragment implements FragmentNews {
    RecyclerView list;
    NewsBulletinRecyclerAdapter listAdapter;
    List<Map<String, String>> listRes;
    Toolbar toolbar;
    int offset = 0;
    SwipeRefreshLayout pullRefreshLayout;
    boolean first = true;
    loadTask pageTask;


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
        pullRefreshLayout.setColorSchemeColors(getColorAccent(), getColorPrimary());
        list = v.findViewById(R.id.list);
        listRes = new ArrayList<>();
        listAdapter = new NewsBulletinRecyclerAdapter(this.getContext(), listRes);
        RecyclerView.LayoutManager manager = new WrapContentLinearLayoutManager(this.getContext(), RecyclerView.VERTICAL, false);
        list.setLayoutManager(manager);
        listAdapter.setmOnItemClickListener(new NewsBulletinRecyclerAdapter.OnItemClickListener() {
            @Override
            public void OnClick(View v, int position) {
                //ActivityOptionsCompat op = ActivityOptionsCompat.makeSceneTransitionAnimation(FragmentNewsBulletin.this.getActivity(),v,"cardview");
                ActivityUtils.startNewsActivity(getActivity(),listRes.get(position).get("link"),listRes.get(position).get("title"));

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
                    offset += 20;
                    if(pageTask!=null&&pageTask.getStatus()!=AsyncTask.Status.FINISHED) pageTask.cancel(true);
                    pageTask = new loadTask(true);
                    pageTask.executeOnExecutor(HITAApplication.TPE);
                }
            }
        });
        pullRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                first = true;
                if(pageTask!=null&&pageTask.getStatus()!=AsyncTask.Status.FINISHED) pageTask.cancel(true);
                pageTask = new loadTask(true);
                pageTask.executeOnExecutor(HITAApplication.TPE);
            }
        });

    }

    @Override
    protected void stopTasks() {

    }

    @Override
    public void Refresh() {
        if(pageTask!=null&&pageTask.getStatus()!=AsyncTask.Status.FINISHED) pageTask.cancel(true);
        pageTask = new loadTask(false);
        pageTask.executeOnExecutor(HITAApplication.TPE);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_news;
    }

    class loadTask extends AsyncTask {

        boolean swipe;

        loadTask(boolean swipe) {
            this.swipe = swipe;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (swipe) pullRefreshLayout.setRefreshing(true);
        }

        @Override
        protected Map<String, String> doInBackground(Object[] objects) {
            try {
                Document d = Jsoup.connect("http://www.hitsz.edu.cn/article/id-74.html?maxPageItems=20&keywords=&pager.offset=" + offset)
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .get();
                Elements annoucements = d.getElementsByClass("announcement");
                //System.out.println(annoucements);
                for (Element e : annoucements.select("li")) {
                    String title = e.select("a").text();
                    String link = e.select("a").attr("href");
                    String views = e.select("[class=view]").text().substring(4);
                    String time = e.select("[class=date]").text();
                    Map m = new HashMap();
                    m.put("title", title);
                    m.put("link", link);
                    m.put("views", views);
                    m.put("time", time);
                    listRes.add(m);
                    //System.out.println("href="+link+",title="+title+",views="+views+",time="+time);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            listAdapter.notifyDataSetChanged();
            pullRefreshLayout.setRefreshing(false);
            if (first) {
                //MaterialCircleAnimator.animShow(list, 700);
                list.scheduleLayoutAnimation();
                first = false;
            }
        }
    }

}
