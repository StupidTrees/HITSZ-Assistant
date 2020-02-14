package com.stupidtree.hita.fragments.search;

import android.annotation.SuppressLint;
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
import com.stupidtree.hita.util.ActivityUtils;

import java.util.ArrayList;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.TPE;

public class FragmentSearchResult_web extends FragmentSearchResult {
    private WebSearchAdapter adapter;
    private List<SparseArray<String>> listRes;
    private SearchHITSZCore searchHITSZCore;

    public FragmentSearchResult_web(String title) {
        super(title);
    }

    private AsyncTask pageTask;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search_result_1, container, false);
        initViews(v);
        //hasInit = false;
        searchHITSZCore = new SearchHITSZCore();
        return v;
    }

    private void initViews(View v) {
        listRes = new ArrayList<>();
        adapter = new WebSearchAdapter(listRes);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void OnClick(View view, int position) {
                if (position >= listRes.size()) {
                    LoadMore();
                } else {
                    ActivityUtils.startNewsActivity(getActivity(), listRes.get(position).get(1), listRes.get(position).get(0));
                }
            }

            @Override
            public void OnClickTransition(View view, int position, View transition) {

            }

            @Override
            public void OnLongClick(View view, int position) {

            }
        });
        initList(v, adapter);
    }

    @Override
    public void Search(boolean hide) {
        super.Search(hide);
        if(TextUtils.isEmpty(searchText)){
            swipeRefreshLayout.setRefreshing(false);
            return;
        }
        if (pageTask != null && !pageTask.isCancelled()) {
            pageTask.cancel(true);
        }
        pageTask = new SearchTask(searchText,hide);
        pageTask.executeOnExecutor(TPE);

    }
    public void LoadMore() {
        if (pageTask != null && !pageTask.isCancelled()) {
            pageTask.cancel(true);
        }
        pageTask = new LoadMoreTask();
        pageTask.executeOnExecutor(TPE);

    }
//    @Override
//    public void swipeRefresh() {
//        searchHITSZCore.reset();
//        Search(false);
//    }

    @Override
    protected void stopTasks() {
        if (pageTask != null && !pageTask.isCancelled()) {
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
        if (!searchText.equals(searchHITSZCore.getLastKeyword())) {
            Search(true);
        } else swipeRefreshLayout.setRefreshing(false);
    }

    @SuppressLint("StaticFieldLeak")
    class SearchTask extends SearchRefreshTask {

        public SearchTask(String keyword,boolean hideContent) {
            super(keyword,hideContent);
        }


        @Override
        protected Object doInBackground(Object[] objects) {
            List<SparseArray<String>> res = null;
            try {
                res = searchHITSZCore.searchForResult(keyword);
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
            if (o instanceof Integer) {
                list.getRecycledViewPool().clear();
                adapter.notifyDataSetChanged();
                list.scheduleLayoutAnimation();
            }
            if (o instanceof SearchException) {
                result.setText(((SearchException) o).getMessage());
            } else if (searchHITSZCore.getTotalResult() > 0) {
                result.setText(String.format(getString(R.string.web_total_searched), searchHITSZCore.getTotalResult(), listRes.size()));
            } else {
                result.setText(R.string.nothing_found);
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    class LoadMoreTask extends SearchLoadMoreTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            List<SparseArray<String>> res = null;
            try {
                res = searchHITSZCore.LoadMore();
            } catch (SearchException e) {
                return e;
            }
            listRes.addAll(res);
            return res.size();
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (o instanceof Integer) {
                int insertedNum = (int) o;

                adapter.notifyItemRangeInserted(listRes.size() - insertedNum, insertedNum);
                adapter.notifyItemRangeChanged(0, listRes.size());

            }
            if (o instanceof SearchException) {
                result.setText(((SearchException) o).getMessage());
            } else if (searchHITSZCore.getTotalResult() > 0) {
                result.setText(String.format(getString(R.string.web_total_searched), searchHITSZCore.getTotalResult(), listRes.size()));
            } else {
                result.setText(R.string.nothing_found);
            }
        }
    }

    class WebSearchAdapter extends RecyclerView.Adapter<WebSearchAdapter.WebSearchViewHoler> {
        List<SparseArray<String>> mBeans;
        OnItemClickListener onItemClickListener;
        private static final int ITEM = 182;
        private static final int FOOT = 827;

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        public WebSearchAdapter(List<SparseArray<String>> mBeans) {
            this.mBeans = mBeans;
        }

        @NonNull
        @Override
        public WebSearchViewHoler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int layoutId = viewType == FOOT ? R.layout.dynamic_web_search_foot : R.layout.dynamic_web_search_result_item;
            View v = getLayoutInflater().inflate(layoutId, parent, false);
            return new WebSearchViewHoler(v, viewType);
        }

        @Override
        public int getItemViewType(int position) {

            return position == mBeans.size() ? FOOT : ITEM;
        }

        @Override
        public void onBindViewHolder(@NonNull WebSearchViewHoler holder, final int position) {
            if (holder.viewType == ITEM) {
                holder.title.setText(mBeans.get(position).get(0));
                holder.type.setText(mBeans.get(position).get(2));
            }
            if (onItemClickListener != null)
                holder.card.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onItemClickListener.OnClick(view, position);
                    }
                });
        }


        @Override
        public int getItemCount() {
            return mBeans.size() == 0 ? 0 : mBeans.size() + 1;
        }


        class WebSearchViewHoler extends RecyclerView.ViewHolder {
            TextView title;
            TextView type;
            CardView card;
            int viewType;

            public WebSearchViewHoler(@NonNull View itemView, int viewType) {
                super(itemView);
                this.viewType = viewType;
                card = itemView.findViewById(R.id.card);
                title = itemView.findViewById(R.id.title);
                type = itemView.findViewById(R.id.type);
            }
        }

    }
}
