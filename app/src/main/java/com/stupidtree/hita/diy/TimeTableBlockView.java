package com.stupidtree.hita.diy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.EventLog;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.stupidtree.hita.R;
import com.stupidtree.hita.core.TimeTable;
import com.stupidtree.hita.core.timetable.EventItem;
import com.stupidtree.hita.util.ColorBox;

import java.util.List;

import static com.stupidtree.hita.HITAApplication.defaultSP;

@SuppressLint("ViewConstructor")
public class TimeTableBlockView extends FrameLayout {
    Object block;

    View card;
    TextView title;
    TextView subtitle;
    ImageView icon;
    OnCardClickListener onCardClickListener;
    OnCardLongClickListener onCardLongClickListener;
    OnDuplicateCardClickListener onDuplicateCardClickListener;

    public interface OnCardClickListener {
        void OnClick(View v, EventItem ei);
    }
    public interface OnCardLongClickListener {
       boolean OnLongClick(View v, EventItem ei);
    }
    public interface OnDuplicateCardClickListener {
        void OnDuplicateClick(View v, List<EventItem> list);
    }

    public void setOnDuplicateCardClickListener(OnDuplicateCardClickListener onDuplicateCardClickListener) {
        this.onDuplicateCardClickListener = onDuplicateCardClickListener;
    }

    public void setOnCardLongClickListener(OnCardLongClickListener onCardLongClickListener) {
        this.onCardLongClickListener = onCardLongClickListener;
    }

    public void setOnCardClickListener(OnCardClickListener onCardClickListener) {
        this.onCardClickListener = onCardClickListener;

    }


    public TimeTableBlockView(@NonNull Context context, @NonNull Object obj,boolean colorful) {

        super(context);
        this.block = obj;
        if(block instanceof EventItem){
            final EventItem ei = (EventItem) block;
            if(ei.eventType==TimeTable.TIMETABLE_EVENT_TYPE_DEADLINE){
                inflate(context, R.layout.dynamic_timetable_deadline_card, this);
            }else if(ei.eventType==TimeTable.TIMETABLE_EVENT_TYPE_REMIND){
                inflate(context,R.layout.dynamic_timetable_remind_card,this);
            }else{
                inflate(context, R.layout.dynamic_timetable_course_card, this);
            }

            card = findViewById(R.id.card);
            title = findViewById(R.id.title);
            subtitle = findViewById(R.id.subtitle);
            icon = findViewById(R.id.icon);

            if(colorful&&(ei.eventType==TimeTable.TIMETABLE_EVENT_TYPE_COURSE||
                    ei.eventType==TimeTable.TIMETABLE_EVENT_TYPE_EXAM)
                    ){
                card.getBackground().mutate().setAlpha(180);
                String query;
                if(ei.eventType==TimeTable.TIMETABLE_EVENT_TYPE_EXAM&&ei.mainName.endsWith("考试")) query = ei.mainName.substring(0,ei.mainName.length()-2);
                else query = ei.mainName;
                int getC = defaultSP.getInt("color:"+query,-1);
                if(getC ==-1){
                    int color = ColorBox.getRandomColor_Material();

                    card.setBackgroundTintList(ColorStateList.valueOf(color));
                    // card.setCardBackgroundColor(color);
                    defaultSP.edit().putInt("color:"+ei.mainName,color).apply();
                }else{
                    card.setBackgroundTintList(ColorStateList.valueOf(getC));
                    //card.setCardBackgroundColor(getC);
                }
                title.setTextColor(Color.WHITE);
                subtitle.setTextColor(Color.WHITE);
                icon.setColorFilter(Color.WHITE);
            }
            card.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onCardClickListener!=null) onCardClickListener.OnClick(v, ei);
                }
            });
            card.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(onCardLongClickListener!=null){
                        return onCardLongClickListener.OnLongClick(v,ei);
                    }else return false;
                }
            });
            if(title!=null)title.setText(ei.mainName);
            if(subtitle!=null)subtitle.setText(TextUtils.isEmpty(ei.tag2) ? "" : ei.tag2);
        }else if(block instanceof List){
            final List<EventItem> list = (List<EventItem>) block;
            inflate(context, R.layout.dynamic_timetable_duplicate_card, this);
            card = findViewById(R.id.card);
            title = findViewById(R.id.title);
            card.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onDuplicateCardClickListener!=null) onDuplicateCardClickListener.OnDuplicateClick(v, list);
                }
            });
            StringBuilder sb = new StringBuilder();
            for(EventItem ei:list) sb.append(ei.mainName).append(";");
            title.setText(sb.toString());
        }

    }




    public int getDow() {
        if(block instanceof EventItem){
            return ((EventItem) block).DOW;
        }else if(block instanceof List){
            return ((List<EventItem>) block).get(0).DOW;
        }
        return -1;
    }

    public int getDuration() {
        if(block instanceof EventItem){
            return ((EventItem) block).getDuration();
        }else if(block instanceof List){
            return ((List<EventItem>) block).get(0).getDuration();
        }
        return -1;
    }

    public EventItem getEvent() {
        if(block instanceof EventItem){
            return ((EventItem) block);
        }else if(block instanceof List){
            return ((List<EventItem>) block).get(0);
        }
        return null;
    }


}
