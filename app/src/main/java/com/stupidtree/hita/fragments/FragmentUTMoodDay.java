package com.stupidtree.hita.fragments;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stupidtree.hita.R;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class FragmentUTMoodDay extends BaseFragment {

    private JsonObject info;
    private String date;

    private TextView title, subtitle;
    private TextView score;
    private TextView score_comment;
    private TextView[] percentages;
    private ImageView[] icons;
    private ProgressBar[] progressBars;

    public FragmentUTMoodDay() {
        // Required empty public constructor
    }


    public static FragmentUTMoodDay newInstance(JsonObject info, String date) {
        FragmentUTMoodDay fragment = new FragmentUTMoodDay();
        Bundle args = new Bundle();
        args.putString("info", info.toString());
        args.putString("date", date);
        fragment.setArguments(args);
        return fragment;
    }

    private float getMoodScore() {
        int happy = info.get("happy").getAsInt();
        int normal = info.get("normal").getAsInt();
        int sad = info.get("sad").getAsInt();
        float haP = 100f * (float) happy / (happy + normal + sad);
        float nP = 100f * (float) normal / (happy + normal + sad);
        return (float) (haP * 0.5 + nP * 0.2 + 50);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            info = new JsonParser().parse(Objects.requireNonNull(requireArguments().getString("info"))).getAsJsonObject();
            date = getArguments().getString("date");
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_utmood_day;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        Refresh();
    }

    private void initViews(View v) {
        title = v.findViewById(R.id.title);
        subtitle = v.findViewById(R.id.subtitle);
        score_comment = v.findViewById(R.id.score_comment);
        score = v.findViewById(R.id.score);
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
    }


    @Override
    protected void stopTasks() {

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void Refresh() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(Objects.requireNonNull(sdf.parse(date)).getTime());
            title.setText(getResources().getStringArray(R.array.months_full)[c.get(Calendar.MONTH)] +
                    String.format(getString(R.string.date_day), c.get(Calendar.DAY_OF_MONTH)));
            subtitle.setText(c.get(Calendar.YEAR) + "");
            int happy = info.get("happy").getAsInt();
            int normal = info.get("normal").getAsInt();
            int sad = info.get("sad").getAsInt();
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
            int all = happy + normal + sad;
            final DecimalFormat sf = new DecimalFormat("#0.0");
            final float moodScore = getMoodScore();
            score_comment.setText(getScoreComment(moodScore));
            ValueAnimator vs = ValueAnimator.ofFloat(0, moodScore);
            vs.setDuration(800);
            vs.setInterpolator(new DecelerateInterpolator());
            vs.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float vslue = (float) animation.getAnimatedValue();
                    score.setText(sf.format(vslue));
                }
            });
            vs.start();
            //icons[i].setImageTintList(ColorStateList.valueOf(HContext.getColor(tintID)));


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
                //icons[i].setImageTintList(ColorStateList.valueOf(HContext.getColor(tintID)));

            }


        } catch (ParseException | JsonIOException e) {
            e.printStackTrace();
        }
    }

    private String getScoreComment(float score) {
        if (score < 60) return getString(R.string.mood_0);
        else if (score < 70) return getString(R.string.mood_1);
        else if (score < 73) return getString(R.string.mood_2);
        else if (score < 75) return getString(R.string.mood_3);
        else if (score < 78) return getString(R.string.mood_4);
        else if (score < 83) return getString(R.string.mood_5);
        else if (score < 85) return getString(R.string.mood_6);
        else if (score < 90) return getString(R.string.mood_7);
        else return getString(R.string.mood_8);

    }

    public static class Temp implements Comparable {
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
