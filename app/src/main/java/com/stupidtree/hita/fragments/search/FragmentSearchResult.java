package com.stupidtree.hita.fragments.search;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.R;
import com.stupidtree.hita.online.SearchItem;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

import static com.stupidtree.hita.HITAApplication.CurrentUser;

public abstract class FragmentSearchResult extends BaseFragment {

    private String title;
    protected String searchText="";
    protected RecyclerView list;
    protected SwipeRefreshLayout swipeRefreshLayout;
    protected TextView result;
    //protected ProgressBar loading;

    public String getTitle() {
        return title;
    }

    public FragmentSearchResult(String title) {
        this.title = title;
    }


    protected void initList(View v, RecyclerView.Adapter adapter){
        list = v.findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
      //  loading = v.findViewById(R.id.loading);
        result = v.findViewById(R.id.result);
        swipeRefreshLayout = v.findViewById(R.id.refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Search(false);
            }
        });
        swipeRefreshLayout.setColorSchemeColors(getColorPrimary());

    }
    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }
    interface OnItemClickListener{
        void OnClick(View view,int position);
        void OnClickTransition(View view,int position,View transition);
        void OnLongClick(View view, int position);
    }
    public void Search(boolean hideContent){
        new SearchItem(searchText,getTitle(), CurrentUser)
                .save(new SaveListener<String>() {
                    @Override
                    public void done(String s, BmobException e) {

                    }
                });
    };

    protected void refreshStart(boolean hideContent){
        swipeRefreshLayout.setRefreshing(true);
        if(hideContent){
            result.setVisibility(View.INVISIBLE);
            list.setVisibility(View.INVISIBLE);
        }
    }

    protected void refreshEnds(){
        swipeRefreshLayout.setRefreshing(false);
        list.setVisibility(View.VISIBLE);
        result.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!TextUtils.isEmpty(searchText))Refresh();
    }

    class SearchRefreshTask extends AsyncTask{
        boolean hideContent;
        String keyword;

        public SearchRefreshTask(String keyword,boolean hideContent) {
            this.keyword = keyword;
            this.hideContent = hideContent;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            refreshStart(hideContent);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            refreshEnds();
        }
    }
    class SearchLoadMoreTask extends AsyncTask{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
