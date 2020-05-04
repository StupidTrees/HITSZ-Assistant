package com.stupidtree.hita.community;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.stupidtree.hita.R;
import com.stupidtree.hita.fragments.popup.FragmentRadiusPopup;
import com.stupidtree.hita.online.Comment;
import com.stupidtree.hita.online.HITAUser;
import com.stupidtree.hita.online.Post;

import cn.bmob.v3.BmobUser;

import static com.stupidtree.hita.HITAApplication.TPE;

@SuppressLint("ValidFragment")
public class FragmentAddComment extends FragmentRadiusPopup {

    private static final int TO_POST = 54;
    private static final int TO_COMMENT = 34;
    Post post;
    private Comment toComment;
    private int mode;
    private onDoneListener onDoneListener;
    private TextView userName;
    private EditText content;

    public FragmentAddComment() {

    }

    FragmentAddComment(Post toPost, onDoneListener onDoneListener) {
        this.post = toPost;
        this.onDoneListener = onDoneListener;
        mode = TO_POST;
    }


    FragmentAddComment(Post post, Comment toComment, onDoneListener onDoneListener) {
        this.toComment = toComment;
        this.onDoneListener = onDoneListener;
        this.post = post;
        mode = TO_COMMENT;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = View.inflate(getContext(), R.layout.fragment_add_comment, null);
        initViews(view);

        setData();
        return view;
    }

    private void initViews(View v) {
        TextView title = v.findViewById(R.id.title);
        TextView send = v.findViewById(R.id.send);
        content = v.findViewById(R.id.content);
        userName = v.findViewById(R.id.username);
        if (mode == TO_POST) {
            title.setText(R.string.reply_post);
            userName.setVisibility(View.GONE);
        } else {
            title.setText(R.string.reply_to_comment);
            userName.setVisibility(View.VISIBLE);
        }
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(content.getText())) {
                    Toast.makeText(getContext(), getString(R.string.please_input), Toast.LENGTH_SHORT).show();
                } else {

                    new commentTask(content.getText().toString()).executeOnExecutor(TPE);
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
        }
        content.requestFocus();
    }

    private void setData() {
        if (mode == TO_COMMENT) {
            if (toComment != null && toComment.getFrom() != null) {
                userName.setText(toComment.getFrom().getNick());
            }
        }
    }

//    @Override
//    public void onDismiss(@NonNull DialogInterface dialog) {
//  super.onDismiss(dialog);
//    }

    interface onDoneListener {
        void onDone();
    }

    @SuppressLint("StaticFieldLeak")
    class commentTask extends AsyncTask {

        String content;

        commentTask(String content) {
            this.content = content;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            if (mode == TO_POST && post != null) {
                Comment c = new Comment();
                c.setFrom(BmobUser.getCurrentUser(HITAUser.class));
                c.setContent(content);
                c.setToUser(post.getAuthor());
                c.setType("to_post");
                c.setPost(post);
                c.setTo(null);
                c.saveSync();
            } else if (mode == TO_COMMENT && toComment != null) {
                Comment c = new Comment();
                c.setFrom(BmobUser.getCurrentUser(HITAUser.class));
                c.setContent(content);
                c.setToUser(toComment.getFrom());
                c.setType("to_comment");
                c.setPost(post);
                c.setTo(toComment);
                c.saveSync();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            onDoneListener.onDone();
            dismiss();
            Toast.makeText(getContext(), R.string.comment_added, Toast.LENGTH_SHORT).show();
        }
    }
}
