package com.stupidtree.hita.fragments.search;

import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;

import androidx.annotation.Nullable;

import com.stupidtree.hita.R;
import com.stupidtree.hita.online.SearchCore;
import com.stupidtree.hita.online.SearchHITSZCore;
import com.stupidtree.hita.util.ActivityUtils;

public class FragmentSearchResult_web extends FragmentSearchResult<SparseArray<String>> {
    public FragmentSearchResult_web() {

    }

    public static FragmentSearchResult_web newInstance() {
        return new FragmentSearchResult_web();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_search_result_1;
    }

    @Override
    void updateHintText(boolean reload, int addedSize) {
        result.setText(getString(R.string.web_total_searched, searchCore.getTotalResult(), listRes.size()));
    }

    @Override
    int getHolderLayoutId() {
        return R.layout.dynamic_web_search_result_item;
    }

    @Override
    SearchCore<SparseArray<String>> getSearchCore() {
        return new SearchHITSZCore();
    }

    @Override
    void bindHolder(SearchListAdapter.SimpleHolder holder, SparseArray<String> data, int position) {
        holder.title.setText(data.get(0));
        holder.tag.setText(data.get(2));
    }


    @Override
    void onItemClicked(View card, int position) {
        ActivityUtils.startNewsActivity(getActivity(), listRes.get(position).get(1), listRes.get(position).get(0));
    }

}
