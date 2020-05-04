package com.stupidtree.hita.community;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.stupidtree.hita.R;
import com.stupidtree.hita.fragments.BaseFragment;
import com.stupidtree.hita.online.Comment;
import com.stupidtree.hita.online.HITAUser;
import com.stupidtree.hita.online.Post;
import com.stupidtree.hita.online.Topic;
import com.stupidtree.hita.util.ActivityUtils;
import com.stupidtree.hita.views.CornerTransform;
import com.stupidtree.hita.views.WrapContentLinearLayoutManager;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.FindListener;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.TPE;
import static com.stupidtree.hita.adapter.NewsIpNewsListAdapter.dip2px;
import static com.stupidtree.hita.community.ActivityCommunity.MODE_REFRESH_ALL;
import static com.stupidtree.hita.community.ActivityCommunity.MODE_REFRESH_ITEM;
import static com.stupidtree.hita.community.ActivityCommunity.MODE_REMOVE_ITEM;

public class FragmentPostsList extends BaseFragment {
    private static final int RESUME_FIRST = 64;
    private static final int RESUME_NONE = 358;
    private static final int RESUME_REFRESH = 244;
    private static final int RESUME_NOTIFY_ITEM = 326;
    private RecyclerView list;
    private PostListAdapter listAdapter;
    private List<Post> listRes;
    private SwipeRefreshLayout pullRefreshLayout;
    private DataFetcher dataFetcher;

    private int resumeMode = RESUME_NONE;
    private boolean useCacheWhenResume = true;
    private boolean refreshFromStart = true;
    private Intent resumeArgument;

    private int pageNum = 0;
    private boolean showTopic = true;
    private View emptyView;
    private boolean is_refreshing = false; //保证同一时间只有一个刷新任务在进行


    public FragmentPostsList() {
        // Required empty public constructor
    }

    public static FragmentPostsList newInstance(String title, String id, boolean showTopic) {
        Bundle args = new Bundle();
        args.putString("id", id);
        args.putString("title", title);
        args.putBoolean("showTopic", showTopic);
        FragmentPostsList f = new FragmentPostsList();
        f.setArguments(args);
        return f;
    }

    String getIdInParent() {
        if (getArguments() != null) {
            Bundle arg = getArguments();
            return arg.getString("id");
        }
        return "";
    }

    void setDataFetcher(DataFetcher fetcher) {
        if (isDetached() || isRemoving()) return;
        this.dataFetcher = fetcher;
    }

//    @Override
//    public void onAttach(@NonNull Context context) {
//        super.onAttach(context);
//        if(context instanceof PostListParent){
//            dataFetcher = ((PostListParent) context).giveDataFetcher(getTag());
//        }
//    }

    @Override
    public void onDetach() {
        super.onDetach();
        dataFetcher = null;
    }

    void respondRefreshRequest(Intent bundle) {
        int mode = bundle.getIntExtra("mode", MODE_REFRESH_ALL);
        String objectId = bundle.getStringExtra("objectId");
        if (isResumed()) {
            switch (mode) {
                case MODE_REFRESH_ALL:
                    Refresh(false, true);
                    break;
                case MODE_REMOVE_ITEM:
                    int i;
                    boolean found = false;
                    for (i = 0; i < listRes.size(); i++) {
                        if (listRes.get(i).getObjectId().equals(objectId)) {
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        listRes.remove(i);
                        listAdapter.notifyItemRemoved(i);
                        listAdapter.notifyItemRangeChanged(i, listRes.size());
                    }

                    break;
                case MODE_REFRESH_ITEM:
                    boolean found2 = false;
                    Post object = null;
                    for (i = 0; i < listRes.size(); i++) {
                        if (listRes.get(i).getObjectId().equals(objectId)) {
                            found2 = true;
                            object = listRes.get(i);
                            break;
                        }
                    }
                    if (found2) {
                        if (bundle.hasExtra("likeNum")) {
                            object.setLikeNum(bundle.getIntExtra("likeNum", 0));
                        }
                        listAdapter.notifyItemChanged(i);
                    }
                    break;
            }

        } else {
            if (mode == MODE_REFRESH_ALL) {
                resumeMode = RESUME_REFRESH;
                useCacheWhenResume = false;
                refreshFromStart = true;
            } else if (resumeMode != RESUME_FIRST) {
                resumeMode = RESUME_NOTIFY_ITEM;
                resumeArgument = bundle;
//                resumeArgId = objectId;
//                resumeArgMode = mode;
            }

        }
    }

    public String getTitle() {
        if (getArguments() != null) {
            Bundle arg = getArguments();
            return arg.getString("title");
        }
        return "";
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_post_page;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pullRefreshLayout = view.findViewById(R.id.refresh);
        initList(view);
        refreshFromStart = true;
        useCacheWhenResume = true;
        resumeMode = RESUME_FIRST;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle arg = getArguments();
            showTopic = arg.getBoolean("showTopic");
        }
    }

    private void initList(View v) {
        emptyView = v.findViewById(R.id.empty);
        list = v.findViewById(R.id.list);
        listRes = new ArrayList<>();
        list.setItemViewCacheSize(20);
        listAdapter = new PostListAdapter();
        list.setAdapter(listAdapter);
        RecyclerView.LayoutManager lm = new WrapContentLinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        list.setLayoutManager(lm);
        pullRefreshLayout.setColorSchemeColors(getColorAccent());
        pullRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Refresh(false, true);
            }
        });
        list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                //当前状态为停止滑动状态SCROLL_STATE_IDLE时
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //不能滑动，说明到底了
                    if (!recyclerView.canScrollVertically(1)) {
                        if (listRes.size() >= 10) {
                            Refresh(true, false);
                        }
                    }
                }
            }

        });

    }

    @Override
    public void Refresh() {

    }

    @Override
    public void onResume() {
        super.onResume();
        if (resumeMode == RESUME_FIRST) resumeMode = RESUME_REFRESH;
        switch (resumeMode) {
            case RESUME_REFRESH:
                Refresh(useCacheWhenResume, refreshFromStart);
                useCacheWhenResume = true;
                refreshFromStart = false;
                resumeMode = RESUME_NONE;
                break;
            case RESUME_NOTIFY_ITEM:
                if (resumeArgument != null) {
                    int resumeArgMode = resumeArgument.getIntExtra("mode", MODE_REFRESH_ALL);
                    String resumeArgId = resumeArgument.getStringExtra("objectId");
                    switch (resumeArgMode) {
                        case MODE_REMOVE_ITEM:
                            int i;
                            boolean found = false;
                            for (i = 0; i < listRes.size(); i++) {
                                if (listRes.get(i).getObjectId().equals(resumeArgId)) {
                                    found = true;
                                    break;
                                }
                            }
                            if (found) {
                                listRes.remove(i);
                                listAdapter.notifyItemRemoved(i);
                                listAdapter.notifyItemRangeChanged(i, listRes.size());
                            }

                            break;
                        case MODE_REFRESH_ITEM:
                            boolean found2 = false;
                            Post object = null;
                            for (i = 0; i < listRes.size(); i++) {
                                if (listRes.get(i).getObjectId().equals(resumeArgId)) {
                                    object = listRes.get(i);
                                    found2 = true;
                                    break;
                                }
                            }
                            if (found2) {
                                if (object != null && resumeArgument.hasExtra("likeNum")) {
                                    object.setLikeNum(resumeArgument.getIntExtra("likeNum", 0));
                                }
                                listAdapter.notifyItemChanged(i);
                            }
                            break;
                    }
                }


        }
    }

    public void Refresh(boolean useCache, final boolean clear) {
        if (is_refreshing) return;
        pullRefreshLayout.setRefreshing(true);
        is_refreshing = true;
        emptyView.setVisibility(View.GONE);
        if (clear) {
            pageNum = 0;
        }
        if (dataFetcher != null)
            dataFetcher.fetchData(useCache, pageNum, new OnDataFetchedListener() {
                @Override
                public void onDataFetched(List<Post> result) {
                    is_refreshing = false;
                    pullRefreshLayout.setRefreshing(false);
                    if (clear) {
                        listRes.clear();
                        pageNum = 0;
                    }
                    if (result != null) {
                        listRes.addAll(result);
                        pageNum += result.size();
                    }
                    if (clear) {
                        listAdapter.notifyDataSetChanged();
                        list.scheduleLayoutAnimation();
                    } else if (result != null && result.size() > 0) {
                        listAdapter.notifyItemRangeInserted(listRes.size() - result.size(), result.size());
                    }
                    if (listRes.size() > 0) emptyView.setVisibility(View.GONE);
                    else emptyView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onFetchFailed(Exception e) {
                    listRes.clear();
                    is_refreshing = false;
                    pullRefreshLayout.setRefreshing(false);
                    emptyView.setVisibility(View.VISIBLE);
                    listAdapter.notifyDataSetChanged();
                    list.scheduleLayoutAnimation();
                }
            });
    }


    @Override
    protected void stopTasks() {
    }

    interface PostListParent {
        DataFetcher giveDataFetcher(String tag);
    }


    interface DataFetcher extends Serializable {
        void fetchData(boolean useCache, int skipSize, OnDataFetchedListener listener);
    }

    interface OnDataFetchedListener {
        void onDataFetched(List<Post> result);

        void onFetchFailed(Exception e);
    }

    class PostListAdapter extends RecyclerView.Adapter<PostListAdapter.CHolder> {
        CornerTransform transformation;

        PostListAdapter() {
            transformation = new CornerTransform(getContext(), dip2px(getContext(), 10f));
            transformation.setExceptCorner(false, false, false, false);
        }

        @NonNull
        @Override
        public CHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View v = getLayoutInflater().inflate(R.layout.dynamic_post, viewGroup, false);
            return new CHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull final CHolder viewholder, final int i) {
            final Post post = listRes.get(i);
            if (TextUtils.isEmpty(post.getContent()))
                viewholder.content.setVisibility(View.GONE);
            else viewholder.content.setVisibility(View.VISIBLE);
            viewholder.content.setText(post.getContent());
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date d = sdf.parse(post.getCreatedAt());
                SimpleDateFormat sdf2 = new SimpleDateFormat("yy-MM-dd HH:mm", Locale.getDefault());
                viewholder.time.setVisibility(View.VISIBLE);
                viewholder.time.setText(sdf2.format(d));
            } catch (ParseException e) {
                e.printStackTrace();
                viewholder.time.setVisibility(View.GONE);
            }
            if (post.getImages() != null && post.getImages().size() == 1) {
                viewholder.image.setVisibility(View.VISIBLE);
                viewholder.imageList.setVisibility(View.GONE);
                Glide.with(getContext()).load(post.getImages().get(0)).apply(RequestOptions.bitmapTransform(transformation)).into(viewholder.image);
            } else if (post.getImages() != null && post.getImages().size() > 1) {
                viewholder.imageList.setVisibility(View.VISIBLE);
                viewholder.image.setVisibility(View.GONE);
                viewholder.imageListRes.clear();
                viewholder.imageListRes.addAll(post.getImages());
                viewholder.imageListAdapter.notifyDataSetChanged();
            } else {
                viewholder.image.setVisibility(View.GONE);
                viewholder.imageList.setVisibility(View.GONE);
            }

            final HITAUser author = post.getAuthor();
            if (author != null) {
                viewholder.author.setText(author.getNick());
                Glide.with(getContext()).load(author.getAvatarUri())
                        .placeholder(R.drawable.ic_account_activated)
                        .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                        .into(viewholder.avatar);
            }


            viewholder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityUtils.startPostDetail(getActivity(), post.getObjectId());
                }
            });
            if (!showTopic) {

                viewholder.topicView.setVisibility(View.GONE);
            } else {
                final Topic topic = post.getTopic();
                if (topic != null) {
                    viewholder.topicView.setVisibility(View.VISIBLE);
                    viewholder.topic.setText(topic.getName());
                } else viewholder.topicView.setVisibility(View.GONE);

            }

            viewholder.like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new likeTask(viewholder, post, !viewholder.liked).executeOnExecutor(TPE);
                }
            });
            viewholder.likeNum.setText(post.getLikeNum() + "");
            viewholder.likes = post.getLikeNum();
            refreshCommentNum(post, new CountListener() {
                @Override
                public void done(Integer count, BmobException e) {
                    if (count == null || e != null) {
                        viewholder.comments = 0;
                        viewholder.commentNum.setText("");
                    } else {
                        viewholder.comments = count;
                        viewholder.commentNum.setText(viewholder.comments + "");
                    }

                }
            });
            refreshLiked(post, viewholder, new FindListener<HITAUser>() {
                @Override
                public void done(List<HITAUser> object, BmobException e) {
                    boolean result = object != null && object.size() > 0;
                    viewholder.liked = result;
                    viewholder.like.setClickable(post != null && CurrentUser != null);
                    if (result) {
                        viewholder.like.setImageResource(R.drawable.ic_like_filled);
                        viewholder.like.setColorFilter(getColorAccent());
                    } else {
                        viewholder.like.setImageResource(R.drawable.ic_like_outline);
                        viewholder.like.clearColorFilter();
                    }
                }
            });
//            BmobQuery<HITAUser> likeNumQ = new BmobQuery<>();
//            Post temp = new Post();
//            temp.setObjectId(post.getObjectId());
//            likeNumQ.addWhereRelatedTo("likes", new BmobPointer(temp));
//            likeNumQ.count(HITAUser.class, new CountListener() {
//                @Override
//                public void done(Integer count, BmobException e) {
//                    if (count == null || e != null) {
//                        viewholder.likes = 0;
//                        viewholder.likeNum.setText("");
//                    } else {
//                        viewholder.likes = count;
//                        viewholder.likeNum.setText(viewholder.likes + "");
//                    }
//
//                }
//            });


        }


        @Override
        public int getItemCount() {
            return listRes.size();
        }


        void refreshCommentNum(Post post, CountListener countListener) {
            BmobQuery<Comment> commentNumQ = new BmobQuery<>();
            commentNumQ.addWhereEqualTo("post", post.getObjectId());
            commentNumQ.count(Comment.class, countListener);
        }

        void refreshLiked(Post post, CHolder holder, FindListener<HITAUser> findListener) {
            holder.like.setClickable(false);
            if (CurrentUser == null) {
                findListener.done(null, null);
                return;
            }
            BmobQuery<HITAUser> con1 = new BmobQuery<>();
            con1.addWhereRelatedTo("likes", new BmobPointer(post));
            BmobQuery<HITAUser> con2 = new BmobQuery<>();
            con2.addWhereEqualTo("objectId", CurrentUser.getObjectId());
            BmobQuery<HITAUser> cons = new BmobQuery<>();
            List<BmobQuery<HITAUser>> consL = new ArrayList<>();
            consL.add(con1);
            consL.add(con2);
            cons.and(consL).findObjects(findListener);
        }

        class likeTask extends AsyncTask {
            CHolder holder;
            Post post;
            boolean like;

            public likeTask(CHolder holder, Post post, boolean like) {
                this.holder = holder;
                this.post = post;
                this.like = like;
            }

            @Override
            protected Object doInBackground(Object[] objects) {
                post.likeSync(CurrentUser, like);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                if (like) holder.likes++;
                else holder.likes--;
                holder.liked = like;
                holder.likeNum.setText(holder.likes + "");
                if (like) {
                    holder.like.setImageResource(R.drawable.ic_like_filled);
                    holder.like.setColorFilter(getColorAccent());
                } else {
                    holder.like.setImageResource(R.drawable.ic_like_outline);
                    holder.like.clearColorFilter();
                }

            }
        }

        class CHolder extends RecyclerView.ViewHolder {
            TextView author;
            TextView content;
            TextView time;
            ImageView avatar;
            ImageView image;
            RecyclerView imageList;
            List<String> imageListRes;
            IMGListAdapter imageListAdapter;
            ImageView like;
            TextView likeNum, commentNum;
            boolean liked;
            int likes, comments;
            CardView card;
            ViewGroup topicView;
            TextView topic;
            // Chip topic;

            public CHolder(@NonNull View itemView) {
                super(itemView);
                author = itemView.findViewById(R.id.post_author);
                content = itemView.findViewById(R.id.post_content);
                time = itemView.findViewById(R.id.post_time);
                avatar = itemView.findViewById(R.id.post_avatar);
                image = itemView.findViewById(R.id.post_image);
                card = itemView.findViewById(R.id.post_card);
                like = itemView.findViewById(R.id.like_icon);
                likeNum = itemView.findViewById(R.id.like_num);
                commentNum = itemView.findViewById(R.id.comment_num);
                topicView = itemView.findViewById(R.id.topic_view);
                topic = itemView.findViewById(R.id.topic);
                imageList = itemView.findViewById(R.id.image_list);
                imageListRes = new ArrayList<>();
                imageListAdapter = new IMGListAdapter(imageListRes);
                imageList.setAdapter(imageListAdapter);
                imageList.setLayoutManager(new GridLayoutManager(getContext(), 3));
                imageList.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return card.onTouchEvent(event);
                    }
                });
            }
        }

    }

    class IMGListAdapter extends RecyclerView.Adapter<IMGListAdapter.IHolder> {

        CornerTransform transformation;
        List<String> mBeans;

        public IMGListAdapter(List<String> mBeans) {
            this.mBeans = mBeans;
            transformation = new CornerTransform(getContext(), dip2px(getContext(), 8));
            transformation.setExceptCorner(false, false, false, false);

        }


        @NonNull
        @Override
        public IHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int layout = R.layout.dynamic_post_img_item_mini;
            View v = getLayoutInflater().inflate(layout, parent, false);
            return new IHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull IHolder holder, final int position) {
            Glide.with(getContext()).load(mBeans.get(position))
                    .apply(RequestOptions.bitmapTransform(transformation))
                    //.centerCrop().
                    .into(holder.image);
        }

        @Override
        public int getItemCount() {
            return Math.min(mBeans.size(), 6);
        }

        class IHolder extends RecyclerView.ViewHolder {
            ImageView image;

            public IHolder(@NonNull View itemView) {
                super(itemView);
                image = itemView.findViewById(R.id.image);
            }
        }

    }


}
