package com.stupidtree.hita.fragments.search;

import android.util.SparseArray;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.stupidtree.hita.R;
import com.stupidtree.hita.online.SearchCore;
import com.stupidtree.hita.online.SearchTeacherCore;
import com.stupidtree.hita.online.Teacher;
import com.stupidtree.hita.util.ActivityUtils;

public class FragmentSearchResult_teacher extends FragmentSearchResult<Object> {
    public FragmentSearchResult_teacher() {

    }

    public static FragmentSearchResult_teacher newInstance() {
        return new FragmentSearchResult_teacher();
    }



    @Override
    protected int getLayoutId() {
        return R.layout.fragment_search_result_1;
    }

    @Override
    void updateHintText(boolean reload, int addedSize) {
        result.setText(getString(R.string.teacher_total_searched, listRes.size()));
    }

    @Override
    int getHolderLayoutId() {
        return R.layout.dynamic_teacher_search_result_item;
    }

    @Override
    SearchCore<Object> getSearchCore() {
        return new SearchTeacherCore();
    }

    @Override
    void bindHolder(SearchListAdapter.SimpleHolder holder, Object data, int position) {
        if (data instanceof SparseArray) {
            SparseArray tsa = (SparseArray) data;
            holder.title.setText(tsa.get(0).toString());
            holder.tag.setVisibility(View.GONE);
            holder.subtitle.setText(tsa.get(SearchTeacherCore.DEPARTMENT).toString());
            Glide.with(requireContext()).load("http://faculty.hitsz.edu.cn/file/showHP.do?d=" +
                    tsa.get(SearchTeacherCore.ID) + "&&w=200&&h=200&&prevfix=200-")
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.ic_account_activated)
                    .into(holder.picture);
        } else if (data instanceof Teacher) {
            Teacher t = (Teacher) data;
            holder.title.setText(t.getName());
            holder.subtitle.setText(t.getSchool());
            holder.tag.setVisibility(View.VISIBLE);
            holder.tag.setText(R.string.teacher_temp_label);
            Glide.with(requireContext()).load(t.getPhotoLink())
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.ic_account_activated)
                    .into(holder.picture);
        }
    }

    @Override
    void onItemClicked(View card, int position) {
        if (listRes.get(position) instanceof SparseArray) {
            SparseArray sa = (SparseArray) listRes.get(position);
            ActivityUtils.startOfficialTeacherActivity(getActivity(),
                    sa.get(SearchTeacherCore.ID).toString(),
                    sa.get(SearchTeacherCore.URL).toString(),
                    sa.get(SearchTeacherCore.NAME).toString()
            );
        } else if (listRes.get(position) instanceof Teacher) {
            ActivityUtils.startTeacherActivity(getActivity(), (Teacher) listRes.get(position));
        }

    }


}
