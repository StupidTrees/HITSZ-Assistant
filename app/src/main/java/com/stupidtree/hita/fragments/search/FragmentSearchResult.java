package com.stupidtree.hita.fragments.search;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.stupidtree.hita.R;
import com.stupidtree.hita.fragments.BaseFragment;
import com.stupidtree.hita.fragments.BasicRefreshTask;
import com.stupidtree.hita.online.SearchCore;
import com.stupidtree.hita.online.SearchException;
import com.stupidtree.hita.views.WrapContentLinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.TPE;

public abstract class FragmentSearchResult<T> extends BaseFragment
        implements BasicRefreshTask.ListRefreshedListener<Object> {
    protected List<T> listRes;
    protected RecyclerView list;
    SearchCore<T> searchCore;
    protected TextView result;
    private String searchText = "";
    private SwipeRefreshLayout swipeRefreshLayout;
    private SearchListAdapter listAdapter;
    private SearchTask pageTask;
    private SearchRoot searchRoot;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
    }

    private void initViews(View v) {
        listRes = new ArrayList<>();
        if (searchCore == null) searchCore = getSearchCore();
        listAdapter = new SearchListAdapter();
        initList(v, listAdapter);
        list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (!list.canScrollVertically(1)
                            && (listRes.size() >= searchCore.getPageSize())) {
                        Search(false, false);
                    }
                }
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof SearchRoot) {
            searchRoot = (SearchRoot) context;
            setSearchText(searchRoot.getSearchText());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        searchRoot = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (TextUtils.isEmpty(searchText) && searchRoot != null) {
            searchText = searchRoot.getSearchText();
            searchCore.setLastKeyword(null);
        }
        if (!TextUtils.isEmpty(searchText)) Refresh();
    }

    protected void initList(View v, RecyclerView.Adapter<SearchListAdapter.SimpleHolder> adapter) {
        list = v.findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setLayoutManager(new WrapContentLinearLayoutManager(requireContext()));
        result = v.findViewById(R.id.result);
        swipeRefreshLayout = v.findViewById(R.id.refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Search(false, true);
            }
        });
        swipeRefreshLayout.setColorSchemeColors(getColorAccent(), getColorPrimary());

    }

    public void Search(boolean hide, boolean reLoad) {

        if (TextUtils.isEmpty(searchText)) {
            swipeRefreshLayout.setRefreshing(false);
            return;
        }
        if (pageTask != null && !pageTask.isCancelled()) {
            pageTask.cancel(true);
        }
        pageTask = new SearchTask(this, searchCore, searchText, hide, reLoad);
        pageTask.executeOnExecutor(TPE, hide, reLoad);
    }

    @Override
    protected void stopTasks() {
        if (pageTask != null && !pageTask.isCancelled()) {
            pageTask.cancel(true);
            pageTask = null;
        }
    }



    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    @Override
    public void Refresh() {
        if (!searchText.equals(searchCore.getLastKeyword())) {
            Search(true, true);
        } else swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_search_result_1;
    }

    @Override
    public void onRefreshStart(String id, Boolean[] params) {
        if (params == null || params.length < 2) return;
        boolean hideContent = params[0];
        if (swipeRefreshLayout == null) return;
        swipeRefreshLayout.setRefreshing(true);
        if(hideContent){
            result.setVisibility(View.INVISIBLE);
            list.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onListRefreshed(String id, Boolean[] params, Object o) {
        boolean reload = params[1];
        swipeRefreshLayout.setRefreshing(false);
        list.setVisibility(View.VISIBLE);
        result.setVisibility(View.VISIBLE);
        if (o instanceof SearchException) {
            result.setText(((SearchException) o).getMessage());
        } else if (o instanceof List) {
            List l = (List) o;
            if (reload) {
                listRes.clear();
                listRes.addAll(l);
                listAdapter.notifyDataSetChanged();
                list.scheduleLayoutAnimation();
            } else {
                listRes.addAll(l);
                listAdapter.notifyItemRangeInserted(listRes.size() - ((List) o).size(), ((List) o).size());
                listAdapter.notifyItemRangeChanged(0, listRes.size());
            }
            if (listRes.size() == 0) result.setText(R.string.nothing_found);
            else updateHintText(reload, l.size());
            //if(!reload&&l.size()==0) Toast.makeText(requireContext(), getString(R.string.no_more_content), Toast.LENGTH_SHORT).show();
        }


    }

    abstract void updateHintText(boolean reload, int addedSize);

    abstract int getHolderLayoutId();

    abstract SearchCore<T> getSearchCore();

    abstract void bindHolder(SearchListAdapter.SimpleHolder simpleHolder, T data, int position);

    abstract void onItemClicked(View card, int position);

    public interface SearchRoot {
        String getSearchText();
    }

    public interface OnItemClickListener {
        void OnClick(View view, int position);

    }

    static class SearchTask extends BasicRefreshTask<Object> {
        String keyword;
        SearchCore searchCore;

        SearchTask(ListRefreshedListener<?> listRefreshedListener, SearchCore searchCore, String keyword, boolean hide, boolean reLoad) {
            super(listRefreshedListener, hide, reLoad);
            this.searchCore = searchCore;
            this.keyword = keyword;
        }

        @Override
        protected Object doInBackground(ListRefreshedListener listRefreshedListener, Boolean... booleans) {
            List<Object> result = new ArrayList<>();
            if (params.length < 2) return result;
            boolean reLoad = params[1];
            try {
                result.addAll(searchCore.search(keyword, reLoad));
            } catch (SearchException e) {
                e.printStackTrace();
                return e;
            }
            return result;
        }
    }

    public class SearchListAdapter extends RecyclerView.Adapter<SearchListAdapter.SimpleHolder> {
        OnItemClickListener onItemClickListener;

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }


        @NonNull
        @Override
        public SimpleHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int layoutId = getHolderLayoutId();
            View v = getLayoutInflater().inflate(layoutId, parent, false);
            return new SimpleHolder(v, viewType);
        }


        @SuppressLint("CheckResult")
        @Override
        public void onBindViewHolder(@NonNull final SimpleHolder holder, final int position) {
            bindHolder(holder, listRes.get(position), position);
            holder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClicked(v, position);
                }
            });
        }


        @Override
        public int getItemCount() {
            return listRes.size();
        }


        class SimpleHolder extends RecyclerView.ViewHolder {
            TextView title;
            TextView subtitle;
            ImageView picture;
            TextView tag;
            CardView card;
            int viewType;

            SimpleHolder(@NonNull View itemView, int viewType) {
                super(itemView);
                this.viewType = viewType;
                card = itemView.findViewById(R.id.card);
                title = itemView.findViewById(R.id.title);
                tag = itemView.findViewById(R.id.tag);
                subtitle = itemView.findViewById(R.id.subtitle);
                picture = itemView.findViewById(R.id.picture);
            }
        }

    }
}
