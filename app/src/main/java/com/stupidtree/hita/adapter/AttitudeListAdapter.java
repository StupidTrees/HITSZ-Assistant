package com.stupidtree.hita.adapter;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.stupidtree.hita.R;
import com.stupidtree.hita.fragments.BaseOperationTask;
import com.stupidtree.hita.fragments.popup.FragmentAddAttitude;
import com.stupidtree.hita.online.Attitude;
import com.stupidtree.hita.online.HITAUser;

import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.TPE;

public class AttitudeListAdapter extends BaseListAdapter<Attitude, AttitudeListAdapter.ViewH>
        implements BaseOperationTask.OperationListener<String> {

    private FragmentAddAttitude.AttachedActivity attachedActivity;

    public AttitudeListAdapter(Context mContext, List<Attitude> mBeans) {
        super(mContext, mBeans);
        if(mContext instanceof FragmentAddAttitude.AttachedActivity) attachedActivity = (FragmentAddAttitude.AttachedActivity) mContext;
        this.mBeans = mBeans;
        this.mContext = mContext;
        this.mInflater = LayoutInflater.from(mContext);
    }

    @Override
    protected int getLayoutId(int viewType) {
        return R.layout.dynamic_attitute_item;
    }

    @Override
    public ViewH createViewHolder(View v, int viewType) {
        return new ViewH(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final ViewH holder, final int position) {
        final Attitude attitude = mBeans.get(position);
        holder.title.setText(attitude.getTitle().replaceAll("\n"," "));
        holder.time.setText(attitude.getCreatedAt());
        holder.attitude = attitude;
        holder.result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext,"您已表过态啦",Toast.LENGTH_SHORT).show();
            }
        });
        new refreshItemTask(AttitudeListAdapter.this, holder, attitude, CurrentUser).executeOnExecutor(TPE);
    }

    @Override
    public void onOperationStart(String id, Boolean[] params) {

    }

    @Override
    public void onOperationDone(String id, BaseOperationTask task, Boolean[] params, final String str) {
        refreshItemTask t = (refreshItemTask) task;
        final ViewH holder = t.holder;
        final Attitude attitude = holder.attitude;
        ValueAnimator vo = ValueAnimator.ofFloat(1f, 0f);
        vo.setDuration(200);
        vo.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float f = (float) animation.getAnimatedValue();
                boolean voted = !str.equals("none");
                holder.voted.setAlpha(voted ? 1f - f : f);
                holder.voting.setAlpha(voted ? f : 1f - f);
                if (f == 1f) {
                    holder.voted.setVisibility(voted ? View.VISIBLE : View.GONE);
                    holder.voting.setVisibility(voted ? View.GONE : View.VISIBLE);
                }
            }
        });
        if (holder.voting.getVisibility() != View.VISIBLE && str.equals("none")
                || holder.voted.getVisibility() != View.VISIBLE && !str.equals("none")
        ) vo.start();
        if (!str.equals("none")) {
            if (str.equals("up")) {
                holder.chosen_up.setVisibility(View.VISIBLE);
                holder.chosen_down.setVisibility(View.GONE);
            } else if (str.equals("down")) {
                holder.chosen_down.setVisibility(View.VISIBLE);
                holder.chosen_up.setVisibility(View.GONE);
            }
            final ValueAnimator va = ValueAnimator.ofObject(new TypeEvaluator<Triple<Integer, Integer, Integer>>() {
                                                                @Override
                                                                public Triple<Integer, Integer, Integer> evaluate(float fraction, Triple<Integer, Integer, Integer> startValue, Triple<Integer, Integer, Integer> endValue) {
                                                                    final int curUp = (int) (endValue.getRight() * fraction + startValue.getRight() * (1 - fraction));
                                                                    final int curDown = (int) (endValue.getLeft() * fraction + startValue.getLeft() * (1 - fraction));
                                                                    final int curProgress = (int) ((float) endValue.getMiddle() * fraction + (float) startValue.getMiddle() * (1.0 - fraction));
                                                                    return Triple.of(curDown, curProgress, curUp);
                                                                }
                                                            }, Triple.of(0, 50, 0)
                    , Triple.of(attitude.getDown(), (int) ((float) attitude.getDown() / (attitude.getUp() + attitude.getDown()) * 100), attitude.getUp())
            );
            va.setInterpolator(new DecelerateInterpolator());
            va.setDuration(400);
            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Triple t = (Triple) animation.getAnimatedValue();
                    holder.upT.setText(t.getRight() + "");
                    holder.downT.setText(t.getLeft() + "");
                    holder.result.setProgress((Integer) t.getMiddle());
                }
            });
            va.start();


        } else {
            holder.up.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    attitude.thumUp(CurrentUser);
                    final Attitude a = new Attitude(attitude);
                    a.update(new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            if (e != null)
                                Toast.makeText(mContext, "表态失败", Toast.LENGTH_SHORT).show();
                            if (attachedActivity != null) attachedActivity.refreshOthers();
                            if (attachedActivity != null)
                                attachedActivity.notifyItem(a.getObjectId());
                        }
                    });
                }
            });
            holder.down.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    attitude.thumDown(CurrentUser);
                    final Attitude a = new Attitude(attitude);
                    a.update(new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            if (e != null) {
                                e.printStackTrace();
                                Toast.makeText(mContext, "表态失败" + e.toString(), Toast.LENGTH_SHORT).show();
                            }
                            if (attachedActivity != null) attachedActivity.refreshOthers();
                            if (attachedActivity != null)
                                attachedActivity.notifyItem(a.getObjectId());
                        }
                    });
                }
            });
        }
    }



    static class ViewH extends RecyclerView.ViewHolder {
        TextView title, upT, downT, time;
        FrameLayout voted;
        LinearLayout voting;
        ImageView up, down, chosen_up, chosen_down;
        ProgressBar result;
        Attitude attitude;


        ViewH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            time = itemView.findViewById(R.id.time);
            up = itemView.findViewById(R.id.up);
            down = itemView.findViewById(R.id.down);
            voted = itemView.findViewById(R.id.voted);
            voting = itemView.findViewById(R.id.voting);
            upT = itemView.findViewById(R.id.up_text);
            downT = itemView.findViewById(R.id.down_text);
            result = itemView.findViewById(R.id.vote_result);
            chosen_up = itemView.findViewById(R.id.chosen_up);
            chosen_down = itemView.findViewById(R.id.chosen_down);
        }
    }

    static class refreshItemTask extends BaseOperationTask<String> {

        ViewH holder;
        Attitude attitude;
        HITAUser user;

        refreshItemTask(OperationListener<String> listRefreshedListener, ViewH holder, Attitude attitude, HITAUser user) {
            super(listRefreshedListener);
            this.holder = holder;
            this.attitude = attitude;
            this.user = user;
        }

        @Override
        protected String doInBackground(OperationListener<String> listRefreshedListener, Boolean... booleans) {
            if (user == null) return null;
            return attitude.voted(CurrentUser);
        }

    }


}
