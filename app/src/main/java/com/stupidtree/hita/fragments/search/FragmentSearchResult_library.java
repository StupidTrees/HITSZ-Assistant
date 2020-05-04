package com.stupidtree.hita.fragments.search;

import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.stupidtree.hita.R;
import com.stupidtree.hita.online.SearchCore;
import com.stupidtree.hita.online.SearchLibraryCore;
import com.stupidtree.hita.util.ActivityUtils;

public class FragmentSearchResult_library extends FragmentSearchResult<SparseArray<String>> {

    private SearchLibraryCore searchLibraryCore;

    public FragmentSearchResult_library() {

    }

    public static FragmentSearchResult_library newInstance() {
        return new FragmentSearchResult_library();
    }

    private Switch range;



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        searchLibraryCore = new SearchLibraryCore();
        range = view.findViewById(R.id.range);
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
                Search(false, true);
            }
        });
        super.onViewCreated(view, savedInstanceState);

    }


    @Override
    public void onRefreshStart(String id, Boolean[] params) {
        super.onRefreshStart(id, params);
        range.setVisibility(View.GONE);
    }

    @Override
    public void onListRefreshed(String id, Boolean[] params, Object o) {
        super.onListRefreshed(id, params, o);
        range.setVisibility(View.VISIBLE);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_search_result_library;
    }

    @Override
    void updateHintText(boolean reload, int addedSize) {
        result.setText(getString(R.string.web_total_searched, getSearchCore().getTotalResult(), getSearchCore().getLoadedResult()));
    }

    @Override
    int getHolderLayoutId() {
        return R.layout.dynamic_library_search_result_item;
    }

    @Override
    SearchCore<SparseArray<String>> getSearchCore() {
        if (searchLibraryCore == null) searchLibraryCore = new SearchLibraryCore();
        return searchLibraryCore;
    }

    @Override
    void bindHolder(SearchListAdapter.SimpleHolder holder, SparseArray<String> data, int position) {
        holder.title.setText(data.get(SearchLibraryCore.TITLE));
        holder.subtitle.setText(data.get(SearchLibraryCore.AUTHOR));
        holder.tag.setText(data.get(SearchLibraryCore.PUBLISHER));
        Glide.with(requireContext()).load(data.get(SearchLibraryCore.IMAGE))
                .placeholder(R.drawable.ic_book).
                into(holder.picture);
    }

    @Override
    void onItemClicked(View card, int position) {
        ActivityUtils.openInBrowser(requireContext(), listRes.get(position).get(SearchLibraryCore.URL));
    }
}
