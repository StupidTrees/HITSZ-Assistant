package com.stupidtree.hita.fragments.search;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.stupidtree.hita.R;
import com.stupidtree.hita.online.SearchException;
import com.stupidtree.hita.online.SearchHITSZCore;
import com.stupidtree.hita.online.SearchHITSZZSCore;
import com.stupidtree.hita.util.ActivityUtils;

import java.util.ArrayList;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.TPE;

public class FragmentSearchResult_zsw extends FragmentSearchResult{
    private WebSearchAdapter adapter;
    private List<SparseArray<String>> listRes;
    private SearchHITSZZSCore searchHITSZZSCore;
    public FragmentSearchResult_zsw(String title) {
        super(title);
    }
    SearchTask pageTask;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search_result_1,container,false);
        initViews(v);
        //hasInit = false;
        searchHITSZZSCore = new SearchHITSZZSCore();
        return v;
    }

    private void initViews(View v) {
        listRes = new ArrayList<>();
        adapter = new WebSearchAdapter(listRes);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void OnClick(View view, int position) {
                ActivityUtils.startZSWActivity(getActivity(),listRes.get(position).get(SearchHITSZZSCore.TITLE),listRes.get(position).get(SearchHITSZZSCore.ID));

                }

            @Override
            public void OnClickTransition(View view, int position, View transition) {

            }

            @Override
            public void OnLongClick(View view, int position) {

            }
        });
        initList(v,adapter);
    }

    @Override
    public void Search(boolean hide) {
        super.Search(hide);
        if(TextUtils.isEmpty(searchText)){
            swipeRefreshLayout.setRefreshing(false);
            return;
        }
        if(pageTask!=null&&!pageTask.isCancelled()){
            pageTask.cancel(true);
        }
        pageTask = new SearchTask(searchText,hide);
        pageTask.executeOnExecutor(TPE);

    }

//    @Override
//    public void swipeRefresh() {
//        searchHITSZZSCore.reset();
//        Search(false);
//    }

    @Override
    protected void stopTasks() {
        if(pageTask!=null&&!pageTask.isCancelled()){
            pageTask.cancel(true);
            pageTask = null;
        }
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        Search();
//    }

    @Override
    public void Refresh() {
        if(!searchText.equals(searchHITSZZSCore.getLastKeyword())){
            Search(true);
        }else swipeRefreshLayout.setRefreshing(false);
    }

    class SearchTask extends SearchRefreshTask{


        public SearchTask(String keyword,boolean hide) {
            super(keyword,hide);
        }


        @Override
        protected Object doInBackground(Object[] objects) {
            List<SparseArray<String>> res = null;
            try {
                res = searchHITSZZSCore.searchForResult(keyword);
            } catch (SearchException e) {
                return e;
            }
            listRes.clear();
            listRes.addAll(res);
            return res.size();
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            list.getRecycledViewPool().clear();
            adapter.notifyDataSetChanged();
            list.scheduleLayoutAnimation();
            if(o instanceof SearchException){
                result.setText(((SearchException) o).getMessage());
            }else if(listRes.size()>0){
                result.setText(String.format(getString(R.string.zsw_total_searched),listRes.size()));
            }else{
                result.setText(R.string.nothing_found);
            }
        }
    }


    class WebSearchAdapter extends RecyclerView.Adapter<WebSearchAdapter.WebSearchViewHoler> {
        List<SparseArray<String>> mBeans;
        OnItemClickListener onItemClickListener;

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        public WebSearchAdapter(List<SparseArray<String>> mBeans) {
            this.mBeans = mBeans;
        }

        @NonNull
        @Override
        public WebSearchViewHoler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.dynamic_zsw_search_result_item,parent,false);
            return new WebSearchViewHoler(v,viewType);
        }


        @Override
        public void onBindViewHolder(@NonNull WebSearchViewHoler holder, final int position) {
            holder.title.setText(mBeans.get(position).get(SearchHITSZZSCore.TITLE));
            String subtitle = mBeans.get(position).get(SearchHITSZZSCore.SUBTITLE);
            if(TextUtils.isEmpty(subtitle)){
                holder.subtitle.setVisibility(View.GONE);
            }else{
                holder.subtitle.setVisibility(View.VISIBLE);
                holder.subtitle.setText(subtitle);
            }
            holder.time.setText(mBeans.get(position).get(SearchHITSZZSCore.TIME).replaceAll("/","-"));
            if(onItemClickListener!=null)holder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.OnClick(view,position);
                }
            });
        }


        @Override
        public int getItemCount() {
            return mBeans.size();
        }



        class WebSearchViewHoler extends RecyclerView.ViewHolder{
            TextView title,subtitle;
            TextView time;
            CardView card;
            int viewType;
            public WebSearchViewHoler(@NonNull View itemView,int viewType) {
                super(itemView);
                this.viewType = viewType;
                card = itemView.findViewById(R.id.card);
                title = itemView.findViewById(R.id.title);
                subtitle = itemView.findViewById(R.id.subtitle);
                time = itemView.findViewById(R.id.time);
            }
        }

    }
}
