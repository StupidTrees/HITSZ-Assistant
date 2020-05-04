package com.stupidtree.hita.community;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.BaseActivity;
import com.stupidtree.hita.fragments.popup.FragmentLoading;
import com.stupidtree.hita.fragments.popup.FragmentRelatedUsers;
import com.stupidtree.hita.online.Comment;
import com.stupidtree.hita.online.HITAUser;
import com.stupidtree.hita.online.Post;
import com.stupidtree.hita.util.ActivityUtils;
import com.stupidtree.hita.views.WrapContentLinearLayoutManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.TPE;
import static com.stupidtree.hita.community.ActivityCommunity.MODE_REFRESH_ITEM;
import static com.stupidtree.hita.community.ActivityCommunity.MODE_REMOVE_ITEM;

public class ActivityPostDetail extends BaseActivity {

    private static final int ITEM_LIMIT = 20;
    TextView content;
    RecyclerView list, commentList;
    Toolbar toolbar;
    CardView topicCard;
    TextView topicText;
    TextView author;
    TextView time;
    ImageView avatar;
    LinearLayout authorLayout;

    IMGListAdapter listAdapter;
    CommentListAdapter commentListAdapter;
    ArrayList<String> listRes;
    String postId;
    Post pagePost;

    List<Comment> commentListRes;
    LinearLayout commentBT, likeBT;
    View commentBottom;
    ImageView likeIcon;
    TextView likeNumText;
    TextView commentLabel, likeLabel;

    ImageView delete;
    int commentPageNum = 0;
    int likeNum;
    boolean liked;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true, true, false);
        setContentView(R.layout.activity_post_detail);
        initList();
        initViews();
        initToolbar();
        postId = getIntent().getStringExtra("id");
        LoadPage();
    }

    @Override
    protected void stopTasks() {

    }

    //加载Post对象
    void LoadPage() {
        BmobQuery<Post> bq = new BmobQuery<>();
        bq.addWhereEqualTo("objectId", postId);
        bq.setCachePolicy(BmobQuery.CachePolicy.CACHE_ELSE_NETWORK);
        bq.include("author.nick,author.avatarUri,topic");
        bq.setCachePolicy(BmobQuery.CachePolicy.CACHE_ELSE_NETWORK);
        bq.findObjects(new FindListener<Post>() {
            @Override
            public void done(List<Post> object, BmobException e) {
                Log.e("done", "ex" + e);
                if (object != null && object.size() > 0) {
                    pagePost = object.get(0);

                    setPageInfo();
                }
            }
        });
    }

    //加载完Post后才调用
    void setPageInfo() {
        if (pagePost == null) return;
        content.setText(pagePost.getContent());
        listRes.clear();
        author.setText(pagePost.getAuthor().getNick());
        time.setText(pagePost.getCreatedAt());
        Glide.with(this)
                .load(pagePost.getAuthor().getAvatarUri())
                .placeholder(R.drawable.ic_account_activated)
                .apply(RequestOptions.circleCropTransform())
                .into(avatar);
        listRes.addAll(pagePost.getImages());
        listAdapter.notifyDataSetChanged();
        if (pagePost.getTopic() != null) {
            topicCard.setVisibility(View.VISIBLE);
            topicText.setText(pagePost.getTopic().getName());
            topicCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityUtils.startTopicPageActivity(getThis(), pagePost.getTopic());
                }
            });
        } else {
            topicCard.setVisibility(View.INVISIBLE);
        }
        if (pagePost.getAuthor() != null && CurrentUser != null &&
                pagePost.getAuthor().getObjectId().equals(CurrentUser.getObjectId())
                || CurrentUser != null && CurrentUser.getUsername().equals("hita")) {
            delete.setVisibility(View.VISIBLE);
        } else {
            delete.setVisibility(View.GONE);
        }

        final BmobQuery<HITAUser> likeNumQ = new BmobQuery<>();
        likeNumQ.addWhereRelatedTo("likes", new BmobPointer(pagePost));
        likeNumQ.count(HITAUser.class, new CountListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void done(Integer count, BmobException e) {
                if (count == null || e != null) {
                    likeNum = 0;
                    likeNumText.setText("");
                    likeLabel.setClickable(false);
                    likeLabel.setText(getString(R.string.users_liked) + " ");
                } else {
                    likeNum = count;
                    pagePost.setLikeNum(likeNum);
                    likeLabel.setClickable(true);
                    likeLabel.setText(getString(R.string.users_liked) + " " + likeNum);
                    likeNumText.setText(likeNum + "");
                    Intent i = new Intent();
                    i.putExtra("mode", MODE_REFRESH_ITEM);
                    i.putExtra("objectId", postId);
                    i.putExtra("likeNum", likeNum);
                    setResult(RESULT_OK, i);
                }


            }
        });
        new refreshLikedTask().executeOnExecutor(TPE);
        new refreshCommentsTask(true).executeOnExecutor(TPE);
    }

    void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.post_detail);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    void initViews() {
        delete = findViewById(R.id.delete);
        authorLayout = findViewById(R.id.author_layout);
        author = findViewById(R.id.post_author);
        content = findViewById(R.id.post_content);
        time = findViewById(R.id.post_time);
        avatar = findViewById(R.id.post_avatar);
        topicText = findViewById(R.id.topic);
        topicCard = findViewById(R.id.topic_view);
        //title = findViewById(R.id.title);
        content = findViewById(R.id.content);
        commentBT = findViewById(R.id.comment);
        commentBottom = findViewById(R.id.comment_bottom);
        likeBT = findViewById(R.id.like);
        likeIcon = findViewById(R.id.like_icon);
        likeNumText = findViewById(R.id.like_num);
        commentLabel = findViewById(R.id.comment_label);
        likeLabel = findViewById(R.id.like_label);
        likeLabel.setClickable(false);
        likeLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FragmentRelatedUsers(false, getString(R.string.users_liked), new FragmentRelatedUsers.DataFetcher() {
                    @Override
                    public void fetchData(boolean anim, final FragmentRelatedUsers.OnFetchListener listener) {
                        BmobQuery<HITAUser> users = new BmobQuery<>();
                        users.addWhereRelatedTo("likes", new BmobPointer(pagePost));
                        users.addQueryKeys("nick,avatarUri");
                        users.findObjects(new FindListener<HITAUser>() {
                            @Override
                            public void done(List<HITAUser> object, BmobException e) {
                                if (object != null && e == null) listener.OnFetchDone(object);
                                else listener.OnFailed();
                            }
                        });
                    }

                    @Override
                    public void fetchCurrentFollowingData(boolean cache, final FragmentRelatedUsers.OnFollowingFetchListener listener) {
                        new UserRelationHelper(CurrentUser).QueryFollowingObjectId(cache, new UserRelationHelper.QueryFollowingObjectIdListener() {
                            @Override
                            public void onResult(List<String> result) {
                                listener.OnFetchDone(result);
                            }

                            @Override
                            public void onFailed(Exception e) {
                                listener.OnFailed();
                            }
                        });
                    }

                }).show(getSupportFragmentManager(), UUID.randomUUID().toString());
            }
        });

        authorLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pagePost != null && pagePost.getAuthor() != null) {
                    ActivityUtils.startUserProfileActivity(getThis(), pagePost.getAuthor());
                }
            }
        });
        likeBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new likeTask(!liked).executeOnExecutor(TPE);
            }
        });
        commentBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CurrentUser == null) {
                    Toast.makeText(getThis(), getString(R.string.log_in_first), Toast.LENGTH_SHORT).show();
                    return;
                }
                new FragmentAddComment(pagePost, new FragmentAddComment.onDoneListener() {
                    @Override
                    public void onDone() {
                        new refreshCommentsTask(true).executeOnExecutor(TPE);
                        Intent i = new Intent();
                        i.putExtra("mode", MODE_REFRESH_ITEM);
                        i.putExtra("objectId", postId);
                        setResult(RESULT_OK, i);
                    }
                }).show(getSupportFragmentManager(), UUID.randomUUID().toString());
            }
        });
        commentBottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commentBT.callOnClick();
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog ad = new AlertDialog.Builder(getThis())
                        .setTitle(R.string.attention)
                        .setMessage(R.string.sure_to_delete_post).setNegativeButton(R.string.button_cancel, null)
                        .setPositiveButton(R.string.button_confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                deletePost();
                            }
                        }).create();
                ad.show();


            }
        });

    }

    void initList() {
        list = findViewById(R.id.list);
        listRes = new ArrayList<>();
        listAdapter = new IMGListAdapter();
        list.setAdapter(listAdapter);
        list.setLayoutManager(new WrapContentLinearLayoutManager(getThis()));

        commentList = findViewById(R.id.clist);
        commentListRes = new ArrayList<>();
        commentListAdapter = new CommentListAdapter();
        commentList.setLayoutManager(new WrapContentLinearLayoutManager(getThis()));
        commentList.setAdapter(commentListAdapter);

    }


    void deletePost() {

        if (CurrentUser == null || pagePost == null ||
                !CurrentUser.getUsername().equals("hita") && !CurrentUser.getObjectId().equals(pagePost.getAuthor().getObjectId()))
            return;
        final FragmentLoading fragmentLoading = FragmentLoading.newInstance(getString(R.string.delete_start));
        if (pagePost != null && CurrentUser != null) {
            new DeletePostHelper(getThis(), pagePost, new DeletePostHelper.StateListener() {
                @Override
                public void onDeleteStart() {
                    fragmentLoading.show(getSupportFragmentManager(), UUID.randomUUID().toString());
                    fragmentLoading.updateSubtitle(getString(R.string.delete_start));
                }

                @Override
                public void onDeleteFileStart() {
                    fragmentLoading.updateSubtitle(getString(R.string.delete_file_start));
                }

                @Override
                public void onDeleteFileSuccess() {

                }


                @Override
                public void onDeleteFails() {
                    Toast.makeText(getThis(), R.string.delete_failed, Toast.LENGTH_SHORT).show();
                    fragmentLoading.dismiss();
                }

                @Override
                public void onDeleteSuccess() {
                    Toast.makeText(getThis(), R.string.delete_success, Toast.LENGTH_SHORT).show();
                    fragmentLoading.dismiss();
                    Intent i = new Intent();
                    i.putExtra("mode", MODE_REMOVE_ITEM);
                    i.putExtra("objectId", postId);
                    setResult(RESULT_OK, i);
                    finish();

                }
            }).run();
        }
    }

    void showCommentActions(final Comment c) {
        AlertDialog ad = new AlertDialog.Builder(getThis())
                .setItems(R.array.community_comment_item, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                c.delete(new UpdateListener() {
                                    @Override
                                    public void done(BmobException e) {
                                        if (e == null) {
                                            new refreshCommentsTask(true).executeOnExecutor(TPE);
                                            Intent i = new Intent();
                                            i.putExtra("mode", MODE_REFRESH_ITEM);
                                            i.putExtra("objectId", postId);
                                            setResult(RESULT_OK, i);
                                            Toast.makeText(getThis(), R.string.delete_success, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                break;
                            case 1:
                                new FragmentAddComment(pagePost, c, new FragmentAddComment.onDoneListener() {
                                    @Override
                                    public void onDone() {
                                        new refreshCommentsTask(true).executeOnExecutor(TPE);
                                    }
                                }).show(getSupportFragmentManager(), UUID.randomUUID().toString());
                            case 2:
                                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData mClipData = ClipData.newPlainText("comment", c.getContent());
                                if (cm != null) {
                                    cm.setPrimaryClip(mClipData);
                                }
                                break;

                        }
                    }
                }).create();
        ad.show();
    }

    class likeTask extends AsyncTask {
        boolean like;

        public likeTask(boolean like) {
            this.like = like;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            if (pagePost == null) return null;
            pagePost.likeSync(CurrentUser, like);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (like) likeNum++;
            else likeNum--;
            liked = like;
            likeNumText.setText(likeNum + "");
            likeLabel.setText(getString(R.string.users_liked) + " " + likeNum);
            if (like) {
                likeIcon.setImageResource(R.drawable.ic_like_filled);
                likeIcon.setColorFilter(getColorAccent());
            } else {
                likeIcon.setImageResource(R.drawable.ic_like_outline);
                likeIcon.clearColorFilter();
            }
            Intent i = new Intent();
            i.putExtra("mode", MODE_REFRESH_ITEM);
            i.putExtra("objectId", postId);
            i.putExtra("likeNum", likeNum);
            setResult(RESULT_OK, i);
        }
    }

    class refreshLikedTask extends AsyncTask {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            likeBT.setClickable(false);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            if (CurrentUser == null) return false;
            BmobQuery<HITAUser> con1 = new BmobQuery<>();
            con1.addWhereRelatedTo("likes", new BmobPointer(pagePost));
            BmobQuery<HITAUser> con2 = new BmobQuery<>();
            con2.addWhereEqualTo("objectId", CurrentUser.getObjectId());
            BmobQuery<HITAUser> cons = new BmobQuery<>();
            List<BmobQuery<HITAUser>> consL = new ArrayList<>();
            consL.add(con1);
            consL.add(con2);
            List<HITAUser> res = cons.and(consL).findObjectsSync(HITAUser.class);
            return res != null && res.size() > 0;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            liked = (boolean) o;
            likeBT.setClickable(pagePost != null && CurrentUser != null);
            if ((boolean) o) {
                likeIcon.setImageResource(R.drawable.ic_like_filled);
                likeIcon.setColorFilter(getColorAccent());
            } else {
                likeIcon.setImageResource(R.drawable.ic_like_outline);
                likeIcon.clearColorFilter();
            }
        }
    }

    class refreshCommentsTask extends AsyncTask {

        boolean clear;
        List<Comment> toAdd;

        public refreshCommentsTask(boolean clear) {
            this.clear = clear;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            commentLabel.setText(getString(R.string.no_comment));
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            if (clear) {
                commentListRes.clear();
                commentPageNum = 0;
            }

            BmobQuery<Comment> bq = new BmobQuery();
            bq.addWhereEqualTo("post", pagePost);
            bq.setLimit(ITEM_LIMIT);
            bq.setSkip(commentPageNum);
            bq.include("from.nick,from.avatarUri,toUser.nick");
            bq.order("+createdAt");
            toAdd = bq.findObjectsSync(Comment.class);
            if (toAdd != null) commentPageNum += toAdd.size();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (toAdd != null) {
                commentListRes.addAll(toAdd);
                if (clear) {
                    commentListAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getThis(), R.string.no_more, Toast.LENGTH_SHORT).show();
                    commentListAdapter.notifyItemRangeInserted(commentListRes.size() - toAdd.size(), toAdd.size());
                    commentListAdapter.notifyItemRangeChanged(commentListRes.size() - 1, 2);
                }
                if (commentListRes.size() == 0) commentLabel.setText(R.string.no_comment);
                else commentLabel.setText(R.string.all_comment);
            }
        }
    }

    class IMGListAdapter extends RecyclerView.Adapter<IMGListAdapter.IHolder> {
        @NonNull
        @Override
        public IHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int layout = R.layout.dynamic_post_img_item;
            View v = getLayoutInflater().inflate(layout, parent, false);
            return new IHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull final IHolder holder, final int position) {
            Glide.with(getThis()).load(listRes.get(position))
                    .into(holder.image);
            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityUtils.showMultipleImages(getThis(), listRes, position);
                    // ActivityUtils.startPhotoDetailActivity(getThis(), listRes.get(position));
                }
            });
        }

        @Override
        public int getItemCount() {
            return listRes.size();
        }

        class IHolder extends RecyclerView.ViewHolder {
            ImageView image;

            public IHolder(@NonNull View itemView) {
                super(itemView);
                image = itemView.findViewById(R.id.image);
            }
        }

    }

    class CommentListAdapter extends RecyclerView.Adapter<CommentListAdapter.CHolder> {

        private static final int ITEM = 512;
        private static final int FOOT = 316;

        @NonNull
        @Override
        public CHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int id = viewType == ITEM ? R.layout.dynamic_comment_item : R.layout.dynamic_web_search_foot;
            View v = getLayoutInflater().inflate(id, parent, false);
            return new CHolder(v, viewType);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == commentListRes.size()) return FOOT;
            return ITEM;
        }

        @Override
        public void onBindViewHolder(@NonNull final CHolder holder, int position) {
            if (holder.type == ITEM) {

                final Comment c = commentListRes.get(position);
                holder.authorName.setText(c.getFrom().getNick());
                holder.content.setText(c.getContent());
                holder.time.setText(c.getCreatedAt());
                holder.authorLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityUtils.startUserProfileActivity(getThis(), c.getFrom());

                    }
                });
                Glide.with(getThis()).load(c.getFrom().getAvatarUri()).
                        placeholder(R.drawable.ic_account_activated)
                        .apply(RequestOptions.circleCropTransform()).into(holder.avatar);
                if (c.getType().equals("to_post")) {
                    holder.tocomment.setVisibility(View.GONE);
                } else {
                    holder.tocomment.setVisibility(View.VISIBLE);
                    if (c.getToUser() != null) {
                        holder.touser.setText(c.getToUser().getNick());
                        holder.touser.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ActivityUtils.startUserProfileActivity(getThis(), c.getToUser());
                            }
                        });
                    }
                }
                holder.card.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (CurrentUser == null) {
                            Toast.makeText(getThis(), R.string.log_in_first, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (CurrentUser.getUsername().equals("hita") || CurrentUser.getObjectId().equals(c.getFrom().getObjectId())) {
                            showCommentActions(c);
                        } else {
                            new FragmentAddComment(pagePost, c, new FragmentAddComment.onDoneListener() {
                                @Override
                                public void onDone() {
                                    new refreshCommentsTask(true).executeOnExecutor(TPE);
                                }
                            }).show(getSupportFragmentManager(), UUID.randomUUID().toString());
                        }

                    }
                });
            } else {
                holder.card.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new refreshCommentsTask(false).executeOnExecutor(TPE);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return commentListRes.size() < ITEM_LIMIT ? commentListRes.size() : commentListRes.size() + 1;
        }

        class CHolder extends RecyclerView.ViewHolder {
            ImageView avatar;
            TextView authorName;
            TextView time, content;
            View card;
            LinearLayout tocomment, authorLayout;
            TextView touser;
            int type;

            public CHolder(@NonNull View v, int type) {
                super(v);
                this.type = type;
                card = v.findViewById(R.id.card);
                avatar = v.findViewById(R.id.avatar);
                authorName = v.findViewById(R.id.name);
                time = v.findViewById(R.id.time);
                content = v.findViewById(R.id.content);
                tocomment = v.findViewById(R.id.tocomment);
                touser = v.findViewById(R.id.touser);
                authorLayout = v.findViewById(R.id.author_layout);

            }
        }


    }


}
