package com.stupidtree.hita.community;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.BaseActivity;
import com.stupidtree.hita.online.Comment;
import com.stupidtree.hita.util.ActivityUtils;
import com.stupidtree.hita.views.CornerTransform;
import com.stupidtree.hita.views.WrapContentLinearLayoutManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.TPE;


public class ActivityMessageBox extends BaseActivity {


    Toolbar mToolbar;
    RecyclerView list;
    BAdapter listAdapter;
    List<Comment> listRes;
    SwipeRefreshLayout refresh;
    int pageNum;

    @Override
    protected void stopTasks() {

    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true, true, false);
        setContentView(R.layout.activity_community_message_box);
        initToolbar();
        initViews();
        new RefreshTask(true).execute();
    }

    void initViews() {
        refresh = findViewById(R.id.refresh);
        refresh.setColorSchemeColors(getColorAccent());
        list = findViewById(R.id.list);
        listRes = new ArrayList<>();
        listAdapter = new BAdapter();
        list.setAdapter(listAdapter);
        list.setLayoutManager(new WrapContentLinearLayoutManager(this));
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new RefreshTask(true).executeOnExecutor(TPE);
            }
        });
        list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (!recyclerView.canScrollVertically(1)) {
                        if (listRes.size() >= 10) {
                            new RefreshTask(true).executeOnExecutor(TPE);
                        }
                    }
                }
            }
        });
    }


    void initToolbar() {
        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.label_activity_message_box);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    class RefreshTask extends AsyncTask {

        boolean clear;
        int addedNum;

        RefreshTask(boolean clear) {
            this.clear = clear;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            if (CurrentUser == null) return null;
            BmobQuery<Comment> bq = new BmobQuery<>();
            bq.addWhereEqualTo("toUser", CurrentUser.getObjectId());
            if (clear) {
                listRes.clear();
                pageNum = 0;
            }
            bq.setSkip(pageNum);
            bq.setLimit(10);

            BmobQuery<Comment> bq2 = new BmobQuery<>();
            bq2.addWhereNotEqualTo("from", CurrentUser.getObjectId());

            List<BmobQuery<Comment>> cond = Arrays.asList(bq, bq2);
            BmobQuery<Comment> fin = new BmobQuery();
            fin.and(cond);
            fin.include("from.nick,from.avatarUri,post.images,post.content");
            fin.order("-createdAt");
            List<Comment> res = fin.findObjectsSync(Comment.class);
            if (res != null) {
                for (Comment c : res) {
                    if (!c.isRead()) {
                        c.setRead(true);
                        c.update(new UpdateListener() {
                            @Override
                            public void done(BmobException e) {

                            }
                        });
                    }
                }
                addedNum = res.size();
                listRes.addAll(res);
            }
            pageNum += addedNum;
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            refresh.setRefreshing(false);
            if (clear) {
                listAdapter.notifyDataSetChanged();
                list.scheduleLayoutAnimation();
            } else {
                listAdapter.notifyItemRangeInserted(listRes.size() - addedNum, addedNum);
            }
        }
    }

    class BAdapter extends RecyclerView.Adapter<BAdapter.CHolder> {


        @NonNull
        @Override
        public CHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.dynamic_community_message_reply, parent, false);

            return new CHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull final CHolder holder, int position) {
            final Comment c = listRes.get(position);
            holder.authorName.setText(c.getFrom().getNick());
            holder.content.setText("回复你：" + c.getContent());
            holder.time.setText(c.getCreatedAt());
            holder.authorLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityUtils.startUserProfileActivity(getThis(), c.getFrom());
                }
            });
            Glide.with(getThis()).load(c.getFrom().getAvatarUri())
                    .placeholder(R.drawable.ic_account_activated)
                    .apply(RequestOptions.circleCropTransform())
                    .into(holder.avatar);

            holder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityUtils.startPostDetail(getThis(), c.getPost().getObjectId());
                }
            });
            holder.postTitle.setText(c.getPost().getContent());
            if (c.getPost() != null && c.getPost().getImages() != null && c.getPost().getImages().size() > 0) {
                holder.postIMG.setVisibility(View.VISIBLE);
                Glide.with(getThis()).load(c.getPost().getImages().get(0))
                        .apply(RequestOptions.bitmapTransform(new CornerTransform(getThis(), 8f)))
                        .into(holder.postIMG);
            } else {
                holder.postIMG.setVisibility(View.GONE);
            }


        }

        @Override
        public int getItemCount() {
            return listRes.size();
        }

        class CHolder extends RecyclerView.ViewHolder {
            ImageView avatar, postIMG;
            TextView postTitle;
            TextView authorName;
            TextView time, content;
            View card;
            LinearLayout authorLayout;

            public CHolder(@NonNull View v) {
                super(v);
                card = v.findViewById(R.id.card);
                avatar = v.findViewById(R.id.avatar);
                authorName = v.findViewById(R.id.name);
                time = v.findViewById(R.id.time);
                content = v.findViewById(R.id.content);
                authorLayout = v.findViewById(R.id.author_layout);
                postTitle = v.findViewById(R.id.post);
                postIMG = v.findViewById(R.id.image);
            }
        }
    }

}
