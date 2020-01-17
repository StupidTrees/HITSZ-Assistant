package com.stupidtree.hita.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.HContext;


public class FragmentUTMoodDay extends BaseFragment {

    JsonObject info;
    String date;

    TextView title;
    ProgressBar firstPr,secondPr,thirdPr;
    TextView firstP,secondP,thirdP,score,score_comment;
    ImageView firstI,secondI,thirdI;
    TextView[] percentages;
    ImageView[] icons;
    ProgressBar[] progressBars;
    public FragmentUTMoodDay() {
        // Required empty public constructor
    }



    public static FragmentUTMoodDay newInstance(JsonObject info, String date) {
        FragmentUTMoodDay fragment = new FragmentUTMoodDay();
        Bundle args = new Bundle();
        args.putString("info",info.toString());
        args.putString("date",date);
        fragment.setArguments(args);
        return fragment;
    }

    public float getMoodScore(){
        int happy = info.get("happy").getAsInt();
        int normal = info.get("normal").getAsInt();
        int sad = info.get("sad").getAsInt();
        float haP = 100f*(float)happy/(happy+normal+sad);
        float nP = 100f*(float)normal/(happy+normal+sad);
        return (float) (haP*0.5+nP*0.2+50);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            info = new JsonParser().parse(getArguments().getString("info")).getAsJsonObject();
            date = getArguments().getString("date");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
      View v = inflater.inflate(R.layout.fragment_utmood_day, container, false);

      initViews(v);
      return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        Refresh();
    }

    void initViews(View v){
        title = v.findViewById(R.id.title);
        score_comment = v.findViewById(R.id.score_comment);
        score = v.findViewById(R.id.score);
        firstI = v.findViewById(R.id.first_icon);
        firstPr = v.findViewById(R.id.first_progress);
        firstP = v.findViewById(R.id.first_percentage);

        secondI = v.findViewById(R.id.second_icon);
        secondPr = v.findViewById(R.id.second_progress);
        secondP = v.findViewById(R.id.second_percentage);

        thirdI = v.findViewById(R.id.third_icon);
        thirdP = v.findViewById(R.id.third_percentage);
        thirdPr = v.findViewById(R.id.third_progress);

        progressBars = new ProgressBar[]{firstPr, secondPr, thirdPr};
        percentages = new TextView[]{firstP,secondP,thirdP};
        icons = new ImageView[]{firstI,secondI,thirdI};
    }


    @Override
    protected void stopTasks() {

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void Refresh() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(sdf.parse(date).getTime());
            title.setText(c.get(Calendar.YEAR)+" "+ getResources().getStringArray(R.array.months_full)[c.get(Calendar.MONTH)]+
                    String.format(getString(R.string.date_day),c.get(Calendar.DAY_OF_MONTH)));
            int happy = info.get("happy").getAsInt();
            int normal = info.get("normal").getAsInt();
            int sad = info.get("sad").getAsInt();
            List<Temp> l = new ArrayList<>();
            l.add(new Temp("happy",happy));
            l.add(new Temp("normal",normal));
            l.add(new Temp("sad",sad));
            Collections.sort(l);
            int all = happy+normal+sad;
            DecimalFormat sf = new DecimalFormat("#0.00");
            float moodScore = getMoodScore();
            score_comment.setText(getScoreComment(moodScore));
            score.setText(sf.format(moodScore));
            for(int i=0;i<3;i++){
                int iconID;
                int colorID;
                int tintID;
                DecimalFormat df = new DecimalFormat("#.00");
                if(l.get(i).type.equals("happy")){
                    iconID = R.drawable.ic_mood_happy;
                    colorID = R.drawable.style_progressbar_mood_happy;
                    tintID = R.color.green_primary;
                    percentages[i].setText(df.format(100*(float)happy/all)+"%");
                    progressBars[i].setProgress((int) (100*(float)happy/all));
                }else if(l.get(i).type.equals("normal")){
                    iconID = R.drawable.ic_mood_normal;
                    colorID = R.drawable.style_progressbar_mood_normal;
                    tintID = R.color.blue_primary;
                    percentages[i].setText(df.format(100*(float)normal/all)+"%");
                    progressBars[i].setProgress((int) (100*(float)normal/all));
                }else{
                    iconID = R.drawable.ic_mood_sad;
                    colorID = R.drawable.style_progressbar_mood_sad;
                    tintID = R.color.red_primary;
                    percentages[i].setText(df.format(100*(float)sad/all)+"%");
                    progressBars[i].setProgress((int) (100*(float)sad/all));
                }
                progressBars[i].setProgressDrawable(HContext.getDrawable(colorID));
                icons[i].setImageResource(iconID);
                icons[i].setImageTintList(ColorStateList.valueOf(HContext.getColor(tintID)));

            }




        } catch (ParseException e) {
            e.printStackTrace();
        } catch (JsonIOException e) {
            e.printStackTrace();
        }
    }

    private String getScoreComment(float score){
        if(score<60) return getString(R.string.mood_0);
        else if(score<70) return getString(R.string.mood_1);
        else if(score<73) return getString(R.string.mood_2);
        else if(score<75) return getString(R.string.mood_3);
        else if(score<78) return getString(R.string.mood_4);
        else if(score<83) return getString(R.string.mood_5);
        else if(score<85) return getString(R.string.mood_6);
        else if(score<90) return getString(R.string.mood_7);
        else return getString(R.string.mood_8);

    }

    private class Temp implements Comparable{
        String type;
        int number;
        Temp(String type,int number){
            this.type = type;
            this.number = number;
        }

        @Override
        public int compareTo(Object o) {
            return -(this.number-((Temp)o).number);
        }
    }
}
