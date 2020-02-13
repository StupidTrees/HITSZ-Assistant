package com.stupidtree.hita.fragments.search;

import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.stupidtree.hita.R;
import com.stupidtree.hita.online.SearchException;
import com.stupidtree.hita.online.SearchHITSZCore;
import com.stupidtree.hita.online.SearchLibraryCore;
import com.stupidtree.hita.util.ActivityUtils;

import java.util.ArrayList;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.TPE;

public class FragmentSearchResult_library extends FragmentSearchResult {
    private WebSearchAdapter adapter;
    private List<SparseArray<String>> listRes;
    private SearchLibraryCore searchLibraryCore;

    public FragmentSearchResult_library(String title) {
        super(title);
    }

    AsyncTask pageTask;
    private Switch range;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search_result_library, container, false);
        searchLibraryCore = new SearchLibraryCore();
        initViews(v);
        //hasInit = false;

        return v;
    }

    private void initViews(View v) {
        listRes = new ArrayList<>();
        range = v.findViewById(R.id.range);
        adapter = new WebSearchAdapter(listRes);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void OnClick(View view, int position) {
                if (position >= listRes.size()) {
                    LoadMore();
                } else {
                    ActivityUtils.openInBrowser(getActivity(), listRes.get(position).get(SearchLibraryCore.URL));
                }
            }

            @Override
            public void OnClickTransition(View view, int position, View transition) {

            }

            @Override
            public void OnLongClick(View view, int position) {

            }
        });
        range.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (!b) {
                    range.setText(R.string.lib_ut);
                    searchLibraryCore.setRange("F44010");
                } else {
                    range.setText(R.string.lib_sz);
                    searchLibraryCore.setRange("");
                }
                Search(false);
            }
        });

        initList(v, adapter);
    }

    @Override
    public void Search(boolean hide) {
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
//        searchLibraryCore.reset();
//        Search();
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
        if (!searchText.equals(searchLibraryCore.getLastKeyword())) {
            Search(true);
        } else swipeRefreshLayout.setRefreshing(false);
    }

    class SearchTask extends SearchRefreshTask {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(hideContent)range.setVisibility(View.INVISIBLE);
        }

        public SearchTask(String keyword, boolean hideContent) {
            super(keyword, hideContent);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            List<SparseArray<String>> res = null;
            try {
                res = searchLibraryCore.searchForResult(keyword);
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
            range.setVisibility(View.VISIBLE);
            if (o instanceof Integer) {
                list.getRecycledViewPool().clear();
                adapter.notifyDataSetChanged();
                list.scheduleLayoutAnimation();
            }
            if (o instanceof SearchException) {
                result.setText(((SearchException) o).getMessage());
            } else if (searchLibraryCore.getTotalResult() > 0) {
                result.setText(String.format(getString(R.string.web_total_searched), searchLibraryCore.getTotalResult(), listRes.size()));
            } else {
                result.setText(R.string.nothing_found);
            }
        }
    }

    class LoadMoreTask extends SearchLoadMoreTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            List<SparseArray<String>> res = null;
            try {
                res = searchLibraryCore.loadMore();
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
            } else if (searchLibraryCore.getTotalResult() > 0) {
                result.setText(String.format(getString(R.string.web_total_searched), searchLibraryCore.getTotalResult(), listRes.size()));
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
            int layoutId = viewType == FOOT ? R.layout.dynamic_web_search_foot : R.layout.dynamic_library_search_result_item;
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
                holder.title.setText(mBeans.get(position).get(SearchLibraryCore.TITLE));
                holder.author.setText(mBeans.get(position).get(SearchLibraryCore.AUTHOR));
                holder.publisher.setText(mBeans.get(position).get(SearchLibraryCore.PUBLISHER));
                Glide.with(getContext()).load(mBeans.get(position).get(SearchLibraryCore.IMAGE))
                        .placeholder(R.drawable.ic_book).
                        into(holder.picture);
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
            TextView author, publisher;
            ImageView picture;
            CardView card;
            int viewType;

            public WebSearchViewHoler(@NonNull View itemView, int viewType) {
                super(itemView);
                this.viewType = viewType;
                card = itemView.findViewById(R.id.card);
                title = itemView.findViewById(R.id.title);
                author = itemView.findViewById(R.id.author);
                publisher = itemView.findViewById(R.id.publisher);
                picture = itemView.findViewById(R.id.picture);
            }
        }

    }
}
