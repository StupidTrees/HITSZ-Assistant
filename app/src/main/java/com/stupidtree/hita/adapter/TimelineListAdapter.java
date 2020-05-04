package com.stupidtree.hita.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.lzyzsd.circleprogress.ArcProgress;
import com.google.android.material.snackbar.Snackbar;
import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivityCurriculumManager;
import com.stupidtree.hita.activities.ActivityLogin;
import com.stupidtree.hita.fragments.popup.FragmentAddEvent;
import com.stupidtree.hita.timetable.TimetableCore;
import com.stupidtree.hita.timetable.packable.EventItem;
import com.stupidtree.hita.timetable.packable.HTime;
import com.stupidtree.hita.views.MaterialCircleAnimator;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.timeTableCore;

public class TimelineListAdapter extends BaseListAdapter<EventItem, RecyclerView.ViewHolder> {

    private static final int PASSED = 13;
    private static final int HEADER = 690;
    private static final int WHOLE_DAY = 403;
    private static final int EMPTY = 393;
    private static final int FOOT = 75;

    private TimeLineSelf timeLineSelf;

    public TimelineListAdapter(Context mContext, TimeLineSelf timeLineSelf, List<EventItem> res) {
        super(mContext, res);
        this.timeLineSelf = timeLineSelf;
    }

    @Override
    protected int getLayoutId(int viewType) {
        int id = R.layout.dynamic_timeline_card_passed;
        switch (viewType) {
            case HEADER:
                return R.layout.dynamic_timeline_header;
            case EMPTY:
                return R.layout.dynamic_timeline_empty;
            case FOOT:
                return R.layout.dynamic_timeline_foot;
            case WHOLE_DAY:
                return R.layout.dynamic_timeline_card_wholeday;
            case TimetableCore.COURSE:
            case TimetableCore.EXAM:
                return R.layout.dynamic_timeline_card_important;
            case TimetableCore.ARRANGEMENT:
                return R.layout.dynamic_timeline_card_arrangement;
            case PASSED:
                return R.layout.dynamic_timeline_card_passed;
            case TimetableCore.DDL:
            case 4:
                return R.layout.dynamic_timeline_card_deadline;
        }
        return id;
    }

    @Override
    public RecyclerView.ViewHolder createViewHolder(View v, int viewType) {
        switch (viewType) {
            case HEADER:
                return new timelineHeaderHolder(v);
            case EMPTY:
            case FOOT:
                return new emptyHolder(v);
            default:
                return new timelineHolder(v, viewType);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof timelineHolder)
            bindTimelineHolder((timelineHolder) holder, position - 1);
        else if (holder instanceof timelineHeaderHolder)
            bindHeaderHolder((timelineHeaderHolder) holder);
    }

    private void bindTimelineHolder(final timelineHolder timelineHolder, final int position) {
        try {
            if (timelineHolder.timeline != null) {
                timelineHolder.timeline.determineTimelineType(position, mBeans.size());
                if (mBeans.size() == 1) {
                    timelineHolder.timeline.setLineWidth(0f);
                    // timelineHolder.timeline.setVisibility(View.GONE);
                } else {
                    timelineHolder.timeline.setLineWidth(mContext.getResources().getDimension(R.dimen.timeline_width));
                }
                // else timelineHolder.timeline.setVisibility(View.VISIBLE);
            }
            if (position >= mBeans.size() || position < 0) return;
            timelineHolder.tv_name.setText(mBeans.get(position).getMainName());
            if (timelineHolder.type == TimetableCore.DDL) {
                if (timelineHolder.tv_time != null)
                    timelineHolder.tv_time.setText(mBeans.get(position).startTime.tellTime());
            } else {
                if (timelineHolder.tv_time != null)
                    timelineHolder.tv_time.setText(mBeans.get(position).startTime.tellTime() + "-" + mBeans.get(position).endTime.tellTime());
            }
            if (timelineHolder.tv_duration != null) {
                int duration = mBeans.get(position).startTime.getDuration(mBeans.get(position).endTime);
                if (duration >= 60)
                    timelineHolder.tv_duration.setText(duration / 60 + "h " + (duration % 60 == 0 ? "" : duration % 60 + "min"));
                else timelineHolder.tv_duration.setText(duration + "min");
            }
            if (timelineHolder.progressBar != null) {
                if (mBeans.get(position) == timeLineSelf.getNowEvent()) {
                    timelineHolder.progressBar.setVisibility(View.VISIBLE);
                    timelineHolder.progressBar.setProgress((int) (timeLineSelf.getNowProgress() * 100));
                    timelineHolder.timeline.setImageDrawable(mContext.getDrawable(R.drawable.ic_timelapse));
                } else {
                    timelineHolder.progressBar.setVisibility(View.GONE);
                }
            }
            if (timelineHolder.tv_place != null) {
                String result = TextUtils.isEmpty(mBeans.get(position).tag2) ? mContext.getString(R.string.unknown_location) : mBeans.get(position).tag2;
                timelineHolder.tv_place.setText(result);
            }

            if (mOnItemClickListener != null) {
                timelineHolder.itemCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnItemClickListener.onItemClick(v, position);
                    }
                });
            }
            if (mOnItemLongClickListener != null) {
                timelineHolder.itemCard.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        mOnItemLongClickListener.onItemLongClick(v, position);
                        return true;
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void bindHeaderHolder(timelineHeaderHolder header) {
        header.UpdateHeadView();
    }

    @Override
    public int getItemViewType(int position) {

        if (position == 0) return HEADER;
        if (mBeans.size() == 0) {
            if (position == 1) return EMPTY;
            else return HEADER;
        } else {
            if (position == mBeans.size() + 1) return FOOT;
            int type;
            if (mBeans.get(position - 1).isWholeDay()) return WHOLE_DAY;
            else if (mBeans.get(position - 1).hasPassed(timeTableCore.getNow())) type = PASSED;
            else type = mBeans.get(position - 1).eventType;
            return type;
        }


    }

    @Override
    public int getItemCount() {
        return mBeans.size() == 0 ? 2 : mBeans.size() + 2;
    }

    @Override
    int getIndexBias() {
        return 1;
    }

    public void notifyItemChangedSmooth(List<EventItem> newL, boolean forgetIt) {
        if (forgetIt) {
            mBeans.clear();
            mBeans.addAll(newL);
            notifyDataSetChanged();
            return;
        }
        List<Integer> toInsert = new ArrayList<>();//记录变化的操作表，正表示加入，负表示删除
        Stack<Integer> toRemove = new Stack<>();
        List<EventItem> remains = new ArrayList<>(); //留下来的元素
        //找到要移除的
        for (int i = 0; i < mBeans.size(); i++) {
            if (!newL.contains(mBeans.get(i))) toRemove.push(i);
        }
        //先处理删除,从后往前删
        while (toRemove.size() > 0) {
            int index = toRemove.pop();
            mBeans.remove(index);
            notifyItemRemoved(index + getIndexBias());
            notifyItemRangeChanged(Math.max(1, index + getIndexBias() - 1), mBeans.size() + getIndexBias());
        }
        //找到要插入的
        for (int i = 0; i < newL.size(); i++) {
            EventItem ei = newL.get(i);
            if (!mBeans.contains(ei)) toInsert.add(i); //新加入的
            else remains.add(ei);
        }
        for (int i = 0; i < toInsert.size(); i++) {
            int index = toInsert.get(i);
            mBeans.add(index, newL.get(index));
            notifyItemInserted(index + getIndexBias());
            notifyItemRangeChanged(Math.max(1, index + getIndexBias() - 1), mBeans.size() + getIndexBias());
        }
        for (EventItem ei : remains) { //保留的
            int oldIndex = mBeans.indexOf(ei);
            int newIndex = newL.indexOf(ei);
            if (oldIndex == newIndex) notifyItemChanged(newIndex + getIndexBias());
            else {
                mBeans.add(newIndex, mBeans.remove(oldIndex));
                notifyItemMoved(oldIndex + getIndexBias(), newIndex + getIndexBias());
                notifyItemRangeChanged(Math.max(1, Math.min(oldIndex, newIndex) + getIndexBias() - 1), mBeans.size() + getIndexBias());
            }
        }
    }

    public interface TimeLineSelf {
        EventItem getNowEvent();

        EventItem getNextvent();

        float getNowProgress();

        FragmentManager getFragmentManager();

        List<EventItem> getTodayEvents();

        int getTodayCourseNum();


    }

    class timelineHolder extends RecyclerView.ViewHolder {
        int type;
        TextView tv_time;
        TextView tv_name;
        TextView tv_duration;
        TextView tv_place;
        ProgressBar progressBar;
        CardView itemCard;
        // TimelineView timelineView;
        com.alorma.timeline.TimelineView timeline;

        //LinearLayout naviButton;
        public timelineHolder(@NonNull View itemView, int type) {
            super(itemView);
            this.type = type;
            tv_time = itemView.findViewById(R.id.tl_tv_time);
            tv_name = itemView.findViewById(R.id.tl_tv_name);
            tv_duration = itemView.findViewById(R.id.tl_tv_duration);
            itemCard = itemView.findViewById(R.id.tl_card);
            progressBar = itemView.findViewById(R.id.event_progressbar);
            // timelineView = itemView.findViewById(R.id.timelineview);
            tv_place = itemView.findViewById(R.id.tl_tv_place);
            //naviButton = itemView.findViewById(R.id.tl_bt_navi);
            timeline = itemView.findViewById(R.id.timeline);
            //timelineView.initLine(TimelineView.getTimeLineViewType(type/1000,mBeans.size()));
        }
    }

    public class timelineHeaderHolder extends RecyclerView.ViewHolder {
        ExpandableLayout head_expand;
        ViewGroup head_counting_layout;
        ImageView head_image, head_counting_image;
        TextView head_title, head_subtitle;
        CardView head_card;
        TextView head_counting_time, head_counting_name,
                head_goQuickly_classroom;
        LinearLayout head_goNow;
        ArcProgress circleProgress;
        ImageView bt_bar_timetable, bt_bar_addEvent;
        View[] heads;
        headCardClickListener headCardClickListener;

        public timelineHeaderHolder(@NonNull View v) {
            super(v);
            head_counting_layout = v.findViewById(R.id.head_counting);
            head_expand = v.findViewById(R.id.head_expand);
            head_card = v.findViewById(R.id.timeline_head_card);
            circleProgress = v.findViewById(R.id.circle_progress);
            bt_bar_timetable = v.findViewById(R.id.bt_expand);
            bt_bar_addEvent = v.findViewById(R.id.bt_add);
            head_title = v.findViewById(R.id.timeline_titile);
            head_subtitle = v.findViewById(R.id.timeline_subtitle);
            head_image = v.findViewById(R.id.timeline_head_image);
            head_goNow = v.findViewById(R.id.timeline_head_gonow);
            headCardClickListener = new headCardClickListener(this);
            head_card.setOnClickListener(headCardClickListener);
            heads = new View[]{head_image, head_goNow, circleProgress};
            head_counting_name = v.findViewById(R.id.tl_head_counting_name);
            head_counting_image = v.findViewById(R.id.tl_head_counting_image);
            head_counting_time = v.findViewById(R.id.tl_head_counting_time);
            head_goQuickly_classroom = v.findViewById(R.id.tl_head_gonow_classroom);
            bt_bar_timetable.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    toggleHeadExpand();
                }
            });
            bt_bar_addEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (timeTableCore.isDataAvailable()) {
                        new FragmentAddEvent().show(timeLineSelf.getFragmentManager(), "add_event");
                    } else {
                        Snackbar.make(v, mContext.getString(R.string.notif_importdatafirst), Snackbar.LENGTH_SHORT).show();
                    }
                }
            });
        }

        private void setExpand(boolean expand) {
            if (expand && !head_expand.isExpanded()) {
                head_counting_layout.setVisibility(View.INVISIBLE);
                MaterialCircleAnimator.animShow(head_counting_layout, 500);
            }
            if (timeTableCore.isDataAvailable()) {
                float fromD, toD;
                if (expand) {
                    fromD = 0f;
                    toD = 180f;
                } else {
                    fromD = 180f;
                    toD = 0f;
                }
                RotateAnimation ra = new RotateAnimation(fromD, toD, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                ra.setInterpolator(new DecelerateInterpolator());
                ra.setDuration(200);//设置动画持续周期
                ra.setRepeatCount(0);//设置重复次数
                ra.setFillAfter(true);//动画执行完后是否停留在执行完的状态
                if (head_expand.isExpanded() && !expand || !head_expand.isExpanded() && expand) {
                    bt_bar_timetable.setAnimation(ra);
                    bt_bar_addEvent.setAnimation(ra);
                    bt_bar_timetable.startAnimation(ra);
                    bt_bar_addEvent.startAnimation(ra);
                }
                head_expand.setExpanded(expand);
            } else {
                head_expand.collapse();
            }
        }


        private void toggleHeadExpand() {
            if (!head_expand.isExpanded()) {
                head_counting_layout.setVisibility(View.INVISIBLE);
                MaterialCircleAnimator.animShow(head_counting_layout, 500);
            }
            if (timeTableCore.isDataAvailable()) {
                float fromD, toD;
                if (!head_expand.isExpanded()) {
                    fromD = 0f;
                    toD = 180f;
                } else {
                    fromD = 180f;
                    toD = 0f;
                }
                RotateAnimation ra = new RotateAnimation(fromD, toD, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                ra.setInterpolator(new DecelerateInterpolator());
                ra.setDuration(200);//设置动画持续周期
                ra.setRepeatCount(0);//设置重复次数
                ra.setFillAfter(true);//动画执行完后是否停留在执行完的状态
                bt_bar_timetable.setAnimation(ra);
                bt_bar_addEvent.setAnimation(ra);
                bt_bar_timetable.startAnimation(ra);
                bt_bar_addEvent.startAnimation(ra);
                head_expand.toggle();
            } else {
                head_expand.collapse();
            }
        }

        public void UpdateHeadView() {
            String titleToSet, subtitltToSet;
            if (CurrentUser == null) {
                titleToSet = mContext.getString(R.string.timeline_head_nulluser_title);
                subtitltToSet = mContext.getString(R.string.timeline_head_nulluser_subtitle);
                switchHeadView(head_image, R.drawable.ic_timeline_head_login);
                //switchToCountingAvailable = false;
                headCardClickListener.setMode(TimelineListAdapter.headCardClickListener.LOG_IN);

            } else if (!timeTableCore.isDataAvailable()) {
                titleToSet = mContext.getString(R.string.timeline_head_nulldata_title);
                subtitltToSet = mContext.getString(R.string.timeline_head_nulldata_subtitle);
                switchHeadView(head_image, R.drawable.ic_timeline_head_nulldata);
                headCardClickListener.setMode(TimelineListAdapter.headCardClickListener.JWTS);
            } else if (!timeTableCore.isThisTerm()) {
                titleToSet = mContext.getString(R.string.timeline_head_notthisterm_title);
                subtitltToSet = mContext.getString(R.string.timeline_head_notthisterm_subtitle);
                switchHeadView(head_image, R.drawable.ic_origami_paper_bird);
                headCardClickListener.setMode(TimelineListAdapter.headCardClickListener.SHOW_NEXT);
            } else if (timeLineSelf.getTodayEvents().size() == 0) {
                titleToSet = mContext.getString(R.string.timeline_head_free_title);
                subtitltToSet = mContext.getString(R.string.timeline_head_free_subtitle);
                switchHeadView(head_image, R.drawable.ic_timeline_head_free);
                headCardClickListener.setMode(TimelineListAdapter.headCardClickListener.SHOW_NEXT);
            } else if (timeLineSelf.getNowEvent() != null) {
                switchHeadView(circleProgress, -1);
                titleToSet = timeLineSelf.getNowEvent().mainName;
                subtitltToSet = mContext.getString(R.string.timeline_head_ongoing_subtitle);
                circleProgress.setProgress((int) (timeLineSelf.getNowProgress() * 100));
//
//            waveView.setWaterLevelRatio(timeServiceBinder.getNowProgress());
//            waveViewHelper.start();

                headCardClickListener.setMode(TimelineListAdapter.headCardClickListener.SHOW_NEXT);
            } else {
                if (new HTime(timeTableCore.getNow()).compareTo(new HTime(5, 0)) < 0 && new HTime(timeTableCore.getNow()).compareTo(new HTime(0, 0)) > 0) {
                    switchHeadView(head_image, R.drawable.ic_moon);
                    titleToSet = mContext.getString(R.string.timeline_head_goodnight_title);
                    subtitltToSet = mContext.getString(R.string.timeline_head_goodnight_subtitle);
                    headCardClickListener.setMode(TimelineListAdapter.headCardClickListener.SHOW_NEXT);
                } else if (new HTime(timeTableCore.getNow()).compareTo(new HTime(8, 15)) < 0 && new HTime(timeTableCore.getNow()).compareTo(new HTime(5, 00)) > 0) {
                    switchHeadView(head_image, R.drawable.ic_sunny);
                    titleToSet = mContext.getString(R.string.timeline_head_goodmorning_title);
                    subtitltToSet = String.format(mContext.getString(R.string.timelinr_goodmorning_subtitle), timeLineSelf.getTodayCourseNum());
                    headCardClickListener.setMode(TimelineListAdapter.headCardClickListener.SHOW_NEXT);
                } else if (new HTime(timeTableCore.getNow()).compareTo(new HTime(12, 15)) > 0 && new HTime(timeTableCore.getNow()).compareTo(new HTime(13, 00)) < 0) {
                    switchHeadView(head_image, R.drawable.ic_lunch);
                    titleToSet = mContext.getString(R.string.timeline_head_lunch_title);
                    subtitltToSet = mContext.getString(R.string.timeline_head_lunch_subtitle);
                    headCardClickListener.setMode(TimelineListAdapter.headCardClickListener.SHOW_NEXT);
                } else if (new HTime(timeTableCore.getNow()).compareTo(new HTime(17, 10)) > 0 && new HTime(timeTableCore.getNow()).compareTo(new HTime(18, 10)) < 0) {
                    switchHeadView(head_image, R.drawable.ic_lunch);
                    titleToSet = mContext.getString(R.string.timeline_head_dinner_title);
                    subtitltToSet = mContext.getString(R.string.timeline_head_dinner_subtitle);
                    headCardClickListener.setMode(TimelineListAdapter.headCardClickListener.SHOW_NEXT);
                } else if (timeLineSelf.getNextvent() != null) {
                    if (timeLineSelf.getNextvent().startTime.getDuration(new HTime(timeTableCore.getNow())) <= 15 && (timeLineSelf.getNextvent().eventType == TimetableCore.COURSE || timeLineSelf.getNextvent().eventType == TimetableCore.EXAM)) {
                        switchHeadView(head_goNow, -1);
                        titleToSet = timeLineSelf.getNextvent().getMainName();
                        // subtitltToSet = mContext.getString(R.string.timeline_head_gonow_subtitle);
                        subtitltToSet = String.format(mContext.getString(R.string.timeline_gonow_subtitle), timeLineSelf.getNextvent().startTime.getDuration(new HTime(timeTableCore.getNow())));
                        head_goQuickly_classroom.setText(timeLineSelf.getNextvent().tag2);
                        headCardClickListener.setMode(TimelineListAdapter.headCardClickListener.SHOW_NEXT);
                    } else {
                        titleToSet = mContext.getString(R.string.timeline_head_normal_title);
                        subtitltToSet = mContext.getString(R.string.timeline_head_normal_subtitle);
                        switchHeadView(head_image, R.drawable.ic_sunglasses);
                        headCardClickListener.setMode(TimelineListAdapter.headCardClickListener.SHOW_NEXT);
                    }
                } else {
                    if (new HTime(timeTableCore.getNow()).compareTo(new HTime(23, 00)) > 0 || new HTime(timeTableCore.getNow()).compareTo(new HTime(5, 0)) < 0) {
                        switchHeadView(head_image, R.drawable.ic_moon);
                        titleToSet = mContext.getString(R.string.timeline_head_goodnight_title);
                        subtitltToSet = mContext.getString(R.string.timeline_head_goodnight_subtitle);

                    } else {
                        switchHeadView(head_image, R.drawable.ic_finish);
                        titleToSet = mContext.getString(R.string.timeline_head_finish_title);
                        subtitltToSet = mContext.getString(R.string.timeline_head_finish_subtitle);
                    }
                    headCardClickListener.setMode(TimelineListAdapter.headCardClickListener.SHOW_NEXT);
                }
            }
            if (timeLineSelf.getNextvent() != null) {
                String text1 = String.format(
                        mContext.getString(R.string.time_format_1),
                        timeLineSelf.getNextvent().startTime.getDuration(new HTime(timeTableCore.getNow())) / 60,
                        timeLineSelf.getNextvent().startTime.getDuration(new HTime(timeTableCore.getNow())) % 60);
                String text2 = String.format(
                        mContext.getString(R.string.time_format_2),
                        timeLineSelf.getNextvent().startTime.getDuration(new HTime(timeTableCore.getNow()))
                );
                String timeText = timeLineSelf.getNextvent().startTime.getDuration(new HTime(timeTableCore.getNow())) >= 60 ? text1
                        : text2;
                head_counting_name.setText(timeLineSelf.getNextvent().mainName);
                head_counting_time.setText(timeText + mContext.getString(R.string.timeline_counting_middle));
                head_counting_image.setImageResource(R.drawable.ic_access_alarm_black_24dp);
                // head_counting_name.setVisibility(View.VISIBLE);
            } else {
                head_counting_name.setText("see you");
                head_counting_time.setText(R.string.timeline_counting_free);
                head_counting_image.setImageResource(R.drawable.ic_empty);
            }
            head_title.setText(titleToSet);
            head_subtitle.setText(subtitltToSet);
        }

        void switchHeadView(View view, int imageId) {
            for (int i = 0; i < heads.length; i++) {
                if (heads[i] == view) heads[i].setVisibility(View.VISIBLE);
                else heads[i].setVisibility(View.GONE);
            }
            if (view instanceof ImageView) ((ImageView) view).setImageResource(imageId);
            setExpand(false);
//        head_counting.post(new Runnable() {
//            @Override
//            public void run() {
//                MaterialCircleAnimator.animHide(head_counting);
//            }
//        });

        }
    }


    class emptyHolder extends RecyclerView.ViewHolder {

        public emptyHolder(@NonNull View itemView) {
            super(itemView);

        }
    }

    class headCardClickListener implements View.OnClickListener {
        static final int SHOW_NEXT = 94;
        static final int LOG_IN = 713;
        static final int JWTS = 577;
        int mode;
        timelineHeaderHolder head;

        headCardClickListener(timelineHeaderHolder head) {
            this.head = head;
        }

        public void setMode(int mode) {
            this.mode = mode;
        }

        @Override
        public void onClick(View v) {
            switch (mode) {
                case SHOW_NEXT:
                    v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    head.toggleHeadExpand();
                    break;
                case LOG_IN:
                    Intent i = new Intent(mContext, ActivityLogin.class);
                    mContext.startActivity(i);
                    break;
                case JWTS:
                    Intent k = new Intent(mContext, ActivityCurriculumManager.class);
                    mContext.startActivity(k);
                    break;

            }

        }
    }
}
