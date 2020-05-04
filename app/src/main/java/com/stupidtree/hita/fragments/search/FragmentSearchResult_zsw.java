package com.stupidtree.hita.fragments.search;

import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;

import com.stupidtree.hita.R;
import com.stupidtree.hita.online.SearchCore;
import com.stupidtree.hita.online.SearchHITSZZSCore;
import com.stupidtree.hita.util.ActivityUtils;

public class FragmentSearchResult_zsw extends FragmentSearchResult<SparseArray<String>> {
    public FragmentSearchResult_zsw() {

    }

    public static FragmentSearchResult_zsw newInstance() {
        return new FragmentSearchResult_zsw();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_search_result_1;
    }

    @Override
    void updateHintText(boolean reload, int addedSize) {
        result.setText(getString(R.string.zsw_total_searched, listRes.size()));
    }

    @Override
    int getHolderLayoutId() {
        return R.layout.dynamic_zsw_search_result_item;
    }

    @Override
    SearchCore<SparseArray<String>> getSearchCore() {
        return new SearchHITSZZSCore();
    }

    @Override
    void bindHolder(SearchListAdapter.SimpleHolder holder, SparseArray<String> data, int position) {
        holder.title.setText(data.get(SearchHITSZZSCore.TITLE));
        String subtitle = data.get(SearchHITSZZSCore.SUBTITLE);
        if (TextUtils.isEmpty(subtitle)) {
            holder.subtitle.setVisibility(View.GONE);
        } else {
            holder.subtitle.setVisibility(View.VISIBLE);
            holder.subtitle.setText(subtitle);
        }
        holder.tag.setText(data.get(SearchHITSZZSCore.TIME).replaceAll("/", "-"));

    }

    @Override
    void onItemClicked(View card, int position) {
        ActivityUtils.startZSWActivity(getActivity(), listRes.get(position).get(SearchHITSZZSCore.TITLE), listRes.get(position).get(SearchHITSZZSCore.ID));
    }
}
