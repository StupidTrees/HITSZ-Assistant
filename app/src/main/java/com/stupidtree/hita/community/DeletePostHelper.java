package com.stupidtree.hita.community;

import android.content.Context;

import com.stupidtree.hita.online.Comment;
import com.stupidtree.hita.online.Post;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DeleteBatchListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

public class DeletePostHelper {
    Context context;
    StateListener listener;
    Post post;

    public DeletePostHelper(Context context, Post post, StateListener listener) {
        this.context = context;
        this.listener = listener;
        this.post = post;
    }

    public void run() {
        if (post == null) {
            listener.onDeleteFails();
            return;
        }
        listener.onDeleteStart();
        deleteFiles();
    }

    private void deleteFiles() {
        listener.onDeleteFileStart();
        if (post.getImages().size() > 0) {
            String[] urls = new String[post.getImages().size()];
            for (int i = 0; i < urls.length; i++) urls[i] = post.getImages().get(i);
            BmobFile.deleteBatch(urls, new DeleteBatchListener() {
                @Override
                public void done(String[] failUrls, BmobException e) {
                    if (e != null) {
                        listener.onDeleteFails();
                    } else {
                        listener.onDeleteFileSuccess();
                        findComments();
                        deletePostSelf();
                    }
                }
            });
        } else {
            findComments();
            deletePostSelf();
        }
    }

    private void findComments() {
        BmobQuery<Comment> bq = new BmobQuery<>();
        bq.addWhereEqualTo("post", post.getObjectId());
        bq.findObjects(new FindListener<Comment>() {
            @Override
            public void done(List<Comment> object, BmobException e) {
                if (object != null) {
                    for (Comment c : object) {
                        c.delete(new UpdateListener() {
                            @Override
                            public void done(BmobException e) {

                            }
                        });
                    }
                }
            }
        });

    }

    void deletePostSelf() {

        post.delete(new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    listener.onDeleteSuccess();
                } else {
                    listener.onDeleteFails();
                }
            }
        });
    }

    interface StateListener {
        void onDeleteStart();

        void onDeleteFileStart();

        void onDeleteFileSuccess();

        void onDeleteFails();

        void onDeleteSuccess();
    }


}
