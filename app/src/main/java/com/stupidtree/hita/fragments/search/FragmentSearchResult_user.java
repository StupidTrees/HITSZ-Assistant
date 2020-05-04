package com.stupidtree.hita.fragments.search;

import android.text.TextUtils;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.stupidtree.hita.R;
import com.stupidtree.hita.online.HITAUser;
import com.stupidtree.hita.online.SearchCore;
import com.stupidtree.hita.online.SearchException;
import com.stupidtree.hita.util.ActivityUtils;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;

public class FragmentSearchResult_user extends FragmentSearchResult<HITAUser> {

    public FragmentSearchResult_user() {

    }

    public static FragmentSearchResult_user newInstance() {
        return new FragmentSearchResult_user();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_search_result_1;
    }

    @Override
    void updateHintText(boolean reload, int addedSize) {
        result.setVisibility(View.GONE);
    }

    @Override
    int getHolderLayoutId() {
        return R.layout.dynamic_search_user_result_item;
    }

    @Override
    SearchCore<HITAUser> getSearchCore() {
        return new SearchCore<HITAUser>() {
            @Override
            public int getPageSize() {
                return 20;
            }

            @Override
            protected List<HITAUser> reloadResult(String keyword) throws SearchException {
                List<HITAUser> res2 = new ArrayList<>();
                try {
                    if (keyword.contains("test")) return res2;
                    BmobQuery<HITAUser> bq1 = new BmobQuery<>();
                    bq1.addWhereEqualTo("username", keyword);
                    BmobQuery<HITAUser> bq2 = new BmobQuery<>();
                    bq2.addWhereEqualTo("nick", keyword);
                    BmobQuery<HITAUser> bq4 = new BmobQuery<>();
                    String schoolName = getProcessedString(keyword);
                    bq4.addWhereEqualTo("school", schoolName);
                    List<BmobQuery<HITAUser>> cond = new ArrayList<>();
                    cond.add(bq1);
                    cond.add(bq2);
                    cond.add(bq4);
                    BmobQuery<HITAUser> bqq = new BmobQuery<>();
                    bqq.order("updatedAt");
                    bqq.or(cond);
                    bqq.setLimit(getPageSize());
                    bqq.setSkip(getOffset());
                    res2.addAll(bqq.findObjectsSync(HITAUser.class));
                    return res2;
                } catch (Exception e) {
                    e.printStackTrace();
                    throw SearchException.newConnectError();
                }
            }

            @Override
            protected List<HITAUser> loadMoreResult(String text) throws SearchException {
                return reloadResult(text);
            }


        };
    }

    @Override
    void bindHolder(SearchListAdapter.SimpleHolder holder, HITAUser l, int position) {
        holder.title.setText(l.getNick());
        holder.tag.setText(TextUtils.isEmpty(l.getSchool()) ? "未知学院" : l.getSchool());
        Glide.with(requireContext()).load(l.getAvatarUri())
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.ic_account_activated)
                .into(holder.picture);
    }


    @Override
    void onItemClicked(View card, int position) {
        try {
            ActivityUtils.startUserProfileActivity(getActivity(), listRes.get(position));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getProcessedString(String input) {
        if (input.contains("计算机") || input.contains("计院") || input.contains("计科"))
            return "计算机科学与技术学院";
        if (input.contains("自动化") || input.contains("机电")) return "机电工程与自动化学院";
        if (input.contains("电信") || input.contains("电子信息")) return "电子与信息工程学院";
        if (input.contains("经管") || input.contains("经济管理")) return "经济管理学院";
        if (input.contains("理学")) return "理学院";
        if (input.contains("土木") || input.contains("环境")) return "土木与环境工程学院";
        if (input.contains("材料")) return "材料科学与工程学院";
        return input;
    }

}
