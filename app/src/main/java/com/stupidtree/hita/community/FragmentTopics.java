package com.stupidtree.hita.community;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.stupidtree.hita.R;
import com.stupidtree.hita.fragments.BaseFragment;
import com.stupidtree.hita.online.Topic;
import com.stupidtree.hita.util.ActivityUtils;
import com.stupidtree.hita.views.CornerTransform;
import com.stupidtree.hita.views.WrapContentLinearLayoutManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

import static com.stupidtree.hita.HITAApplication.bmobCacheHelper;

@SuppressLint("ValidFragment")
public class FragmentTopics extends BaseFragment {
    private RecyclerView list;
    private boolean is_refreshing = false;
    private boolean refreshOnResume = true;
    private boolean useCacheWhenResume = true;
    private TopicAdapter listAdapter;
    private List<Topic> listRes;
    private SwipeRefreshLayout refreshLayout;

    public FragmentTopics() {

    }

    @Override
    protected void stopTasks() {

    }

    @Override
    public void Refresh() {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_topics;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initList(view);
        refreshOnResume = true;
        useCacheWhenResume = true;
    }


    public void readyToRefresh() {
        refreshOnResume = true;
        useCacheWhenResume = false;
    }

    private void initList(View v) {
        refreshLayout = v.findViewById(R.id.refresh);
        list = v.findViewById(R.id.list);
        listRes = new ArrayList<>();
        listAdapter = new TopicAdapter();
        list.setAdapter(listAdapter);
        list.setLayoutManager(new WrapContentLinearLayoutManager(getContext()));
        refreshLayout.setColorSchemeColors(getColorAccent());
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Refresh(false);
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        if (refreshOnResume) {
            refreshOnResume = false;
            Refresh(useCacheWhenResume);
            useCacheWhenResume = true;
        }
    }

    private void Refresh(final boolean cache) {
        if (is_refreshing) return;
        is_refreshing = true;
        refreshLayout.setRefreshing(true);
        BmobQuery<Topic> bq = new BmobQuery<>();
        // bq.setLimit(200);
        if (!cache) {
            bmobCacheHelper.callBasicTopicsToRefresh();
            bq.setCachePolicy(BmobQuery.CachePolicy.NETWORK_ONLY);
        } else {
            bq.setCachePolicy(BmobQuery.CachePolicy.CACHE_ELSE_NETWORK);
        }
        bq.findObjects(new FindListener<Topic>() {
            @Override
            public void done(List<Topic> object, BmobException e) {
                refreshLayout.setRefreshing(false);
                is_refreshing = false;
                if (e == null && object != null) {
                    listRes.clear();
                    listRes.addAll(object);
                    Collections.sort(listRes, new Comparator<Topic>() {
                        @Override
                        public int compare(Topic o1, Topic o2) {
                            return o1.compareTo(o2);
                        }
                    });
                    listAdapter.notifyDataSetChanged();
                    list.scheduleLayoutAnimation();

                }
            }
        });

    }

    class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.THolder> {


        CornerTransform transform;

        TopicAdapter() {
            transform = new CornerTransform(getContext(), 8f);
            transform.setExceptCorner(false, false, false, false);
        }

        @NonNull
        @Override
        public THolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.dynamic_topic_item, parent, false);
            return new THolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull final THolder holder, int position) {
            final Topic t = listRes.get(position);
            holder.title.setText(t.getName());
            if (TextUtils.isEmpty(t.getDescription())) holder.subtitle.setVisibility(View.GONE);
            else {
                holder.subtitle.setVisibility(View.VISIBLE);
                holder.subtitle.setText(t.getDescription());
            }
            if (t.getType().equals("basic")) holder.title.setTypeface(Typeface.DEFAULT_BOLD);
            else holder.title.setTypeface(Typeface.DEFAULT);
            Glide.with(getContext()).load(t.getCover())
                    .apply(RequestOptions.bitmapTransform(transform))
                    .placeholder(R.drawable.ic_topic_gradient)
                    .into(holder.image);
            holder.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityUtils.startTopicPageActivity(getActivity(), t);
                }
            });


        }

        @Override
        public int getItemCount() {
            return listRes.size();
        }

        class THolder extends RecyclerView.ViewHolder {
            TextView title, subtitle;
            ImageView image;
            View item;

            THolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.title);
                subtitle = itemView.findViewById(R.id.subtitle);
                image = itemView.findViewById(R.id.image);
                item = itemView.findViewById(R.id.item);
            }
        }
    }

}
