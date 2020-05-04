package com.stupidtree.hita.fragments.user_center;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.stupidtree.hita.R;
import com.stupidtree.hita.community.ActivityMessageBox;
import com.stupidtree.hita.fragments.BaseFragment;
import com.stupidtree.hita.online.Comment;
import com.stupidtree.hita.util.ActivityUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.CountListener;

import static com.stupidtree.hita.HITAApplication.CurrentUser;


public class FragmentUserCenter_ut extends BaseFragment {

    private TextView[] percentages;
    private ImageView[] icons;
    private ProgressBar[] progressBars;

    private boolean firstResume = true;

    private TextView punchLabel;
    private TextView messageNum;

    public FragmentUserCenter_ut() {
    }

    public static FragmentUserCenter_ut newInstance(){
        return new FragmentUserCenter_ut();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(false);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_usercenter_ut;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
    }

    private void initViews(View v) {
        punchLabel = v.findViewById(R.id.punch_label);
        ImageView firstI = v.findViewById(R.id.first_icon);
        ProgressBar firstPr = v.findViewById(R.id.first_progress);
        TextView firstP = v.findViewById(R.id.first_percentage);

        ImageView secondI = v.findViewById(R.id.second_icon);
        ProgressBar secondPr = v.findViewById(R.id.second_progress);
        TextView secondP = v.findViewById(R.id.second_percentage);

        ImageView thirdI = v.findViewById(R.id.third_icon);
        TextView thirdP = v.findViewById(R.id.third_percentage);
        ProgressBar thirdPr = v.findViewById(R.id.third_progress);

        progressBars = new ProgressBar[]{firstPr, secondPr, thirdPr};
        percentages = new TextView[]{firstP, secondP, thirdP};
        icons = new ImageView[]{firstI, secondI, thirdI};

        LinearLayout find_classmate = v.findViewById(R.id.find_school_mate);
        LinearLayout my_message = v.findViewById(R.id.my_message);
        LinearLayout my_post = v.findViewById(R.id.my_post);
        messageNum = v.findViewById(R.id.message_num);
        find_classmate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.searchFor(getActivity(), CurrentUser.getSchool(), "user");
            }
        });
        my_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), ActivityMessageBox.class);
                startActivity(i);
            }
        });
        my_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.startOneUserPostsActivity(getActivity(), CurrentUser);
            }
        });
    }


    @Override
    protected void stopTasks() {
    }

    @Override
    public void onResume() {
        super.onResume();
        Refresh();
        if (firstResume) {
            getUnreadMessage();
            firstResume = false;
        }
    }

    private void getUnreadMessage() {
        BmobQuery<Comment> bq1 = new BmobQuery<>();
        bq1.addWhereEqualTo("toUser", CurrentUser.getObjectId());
        BmobQuery<Comment> bq2 = new BmobQuery<>();
        bq2.addWhereNotEqualTo("from", CurrentUser.getObjectId());
        BmobQuery<Comment> bq3 = new BmobQuery<>();
        bq3.addWhereEqualTo("read", false);
        BmobQuery<Comment> q = new BmobQuery<>();
        q.and(Arrays.asList(bq1, bq2, bq3));
        q.count(Comment.class, new CountListener() {
            @Override
            public void done(Integer count, BmobException e) {
                if (e == null && count != null) {
                    if (count == 0) messageNum.setVisibility(View.GONE);
                    else {
                        messageNum.setVisibility(View.VISIBLE);
                        messageNum.setText(String.valueOf(count));
                    }
                } else messageNum.setVisibility(View.GONE);
            }
        });

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void Refresh() {
        try {
            int happy = CurrentUser.getHappyDays();
            int normal = CurrentUser.getNormalDays();
            final int sad = CurrentUser.getSadDays();
            List<Temp> l = new ArrayList<>();
            l.add(new Temp("happy", happy));
            l.add(new Temp("normal", normal));
            l.add(new Temp("sad", sad));
            Collections.sort(l, new Comparator<Temp>() {
                @Override
                public int compare(Temp o1, Temp o2) {
                    return o1.compareTo(o2);
                }
            });
            final int all = happy + normal + sad;
            for (int i = 0; i < 3; i++) {
                int iconID;
                final DecimalFormat df = new DecimalFormat("#0.00");
                int targetValue;
                if (l.get(i).type.equals("happy")) {
                    iconID = R.drawable.ic_mood_happy;
                    targetValue = happy;
                } else if (l.get(i).type.equals("normal")) {
                    iconID = R.drawable.ic_mood_normal;
                    targetValue = normal;
                } else {
                    iconID = R.drawable.ic_mood_sad;
                    targetValue = sad;
                }
                final ValueAnimator va = ValueAnimator.ofInt(progressBars[i].getProgress(), (int) (100 * (float) targetValue / all));
                va.setDuration(500);
                va.setStartDelay(i * 60);
                va.setInterpolator(new DecelerateInterpolator());
                final int finalI = i;
                va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int value = (int) animation.getAnimatedValue();
                        percentages[finalI].setText(df.format(value) + "%");
                        progressBars[finalI].setProgress(value);
                    }
                });
                va.start();

                icons[i].setImageResource(iconID);
            }
            punchLabel.setText(getString(R.string.user_center_you_have_punched, all));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static class Temp implements Comparable {
        String type;
        int number;

        Temp(String type, int number) {
            this.type = type;
            this.number = number;
        }

        @Override
        public int compareTo(@NonNull Object o) {
            return -(this.number - ((Temp) o).number);
        }
    }

}