package com.stupidtree.hita.diy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.stupidtree.hita.R;
import com.stupidtree.hita.timetable.TimetableCore;
import com.stupidtree.hita.timetable.timetable.EventItem;
import com.stupidtree.hita.timetable.timetable.HTime;
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
    TimeTablePreferenceRoot root;

    public interface OnCardClickListener {
        void OnClick(View v, EventItem ei);
    }

    public interface OnCardLongClickListener {
        boolean OnLongClick(View v, EventItem ei);
    }

    public interface OnDuplicateCardClickListener {
        void OnDuplicateClick(View v, List<EventItem> list);
    }

    public interface TimeTablePreferenceRoot {
        boolean isColorEnabled();

        String getTitleColor();

        String getSubTitleColor();

        String getIconColor();

        boolean willBoldText();

        boolean cardIconEnabled();

        int getCardOpacity();

        int getCardHeight();

        HTime getStartTime();

        int getTodayBGColor();

        int getTitleGravity();

        int getColorPrimary();

        int getColorAccent();

        int getTitleAlpha();

        int getSubtitleAlpha();

        String getCardBackground();
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


    @SuppressLint("ResourceAsColor")
    public TimeTableBlockView(@NonNull Context context, @NonNull Object obj, TimeTablePreferenceRoot root) {

        super(context);
        this.block = obj;
        this.root = root;
        if (block instanceof EventItem) {
            initEventCard(context);
        } else if (block instanceof List) {
            initDuplicateCard(context);
        }
    }

    private void initEventCard(Context context) {
        final EventItem ei = (EventItem) block;
        if (ei.eventType == TimetableCore.TIMETABLE_EVENT_TYPE_DEADLINE) {
            inflate(context, R.layout.dynamic_timetable_deadline_card, this);
        } else {
            inflate(context, R.layout.dynamic_timetable_course_card, this);
        }
        card = findViewById(R.id.card);
        title = findViewById(R.id.title);
        subtitle = findViewById(R.id.subtitle);
        icon = findViewById(R.id.icon);
        int subjectColor = 0;
        if (root.isColorEnabled() &&
                (ei.eventType == TimetableCore.TIMETABLE_EVENT_TYPE_COURSE ||
                        ei.eventType == TimetableCore.TIMETABLE_EVENT_TYPE_EXAM)
        ) {
            String query;
            if (ei.eventType == TimetableCore.TIMETABLE_EVENT_TYPE_EXAM && ei.mainName.endsWith("考试"))
                query = ei.mainName.substring(0, ei.mainName.length() - 2);
            else query = ei.mainName;
            int getC = defaultSP.getInt("color:" + query, -1);
            if (getC == -1) {
                int color = ColorBox.getRandomColor_Material();
                card.setBackgroundTintList(ColorStateList.valueOf(color));
                defaultSP.edit().putInt("color:" + ei.mainName, color).apply();
            } else {
                card.setBackgroundTintList(ColorStateList.valueOf(getC));
            }
            subjectColor = getC;
        } else {
            if (root.getCardBackground().equals("primary")) {
                card.setBackgroundTintList(ColorStateList.valueOf(root.getColorPrimary()));
            } else if (root.getCardBackground().equals("accent")) {
                card.setBackgroundTintList(ColorStateList.valueOf(root.getColorAccent()));
            }

        }
        switch (root.getTitleColor()) {
            case "subject":
                if (root.isColorEnabled() &&
                        (ei.eventType == TimetableCore.TIMETABLE_EVENT_TYPE_COURSE ||
                                ei.eventType == TimetableCore.TIMETABLE_EVENT_TYPE_EXAM)) {
                    if (title != null) title.setTextColor(subjectColor);
                } else if (title != null) title.setTextColor(root.getColorPrimary());
                break;
            case "white":
                if (title != null) title.setTextColor(Color.WHITE);
                break;
            case "black":
                if (title != null) title.setTextColor(Color.BLACK);
                break;
            case "primary":
                if (title != null) title.setTextColor(root.getColorPrimary());
                break;
            case "accent":
                if (title != null) title.setTextColor(root.getColorAccent());
                break;
        }
        switch (root.getSubTitleColor()) {
            case "subject":
                if (root.isColorEnabled() &&
                        (ei.eventType == TimetableCore.TIMETABLE_EVENT_TYPE_COURSE ||
                                ei.eventType == TimetableCore.TIMETABLE_EVENT_TYPE_EXAM)) {
                    if (subtitle != null) subtitle.setTextColor(subjectColor);
                } else if (subtitle != null) subtitle.setTextColor(root.getColorPrimary());
                break;
            case "white":
                if (subtitle != null) subtitle.setTextColor(Color.WHITE);
                break;
            case "black":
                if (subtitle != null) subtitle.setTextColor(Color.BLACK);
                break;
            case "primary":
                if (subtitle != null) subtitle.setTextColor(root.getColorPrimary());
                break;
            case "accent":
                if (subtitle != null) subtitle.setTextColor(root.getColorAccent());
                break;
        }
        if (icon != null) {
            if (root.cardIconEnabled()) {
                icon.setVisibility(VISIBLE);
                icon.setColorFilter(Color.WHITE);
                switch (root.getIconColor()) {
                    case "subject":
                        if (root.isColorEnabled() &&
                                (ei.eventType == TimetableCore.TIMETABLE_EVENT_TYPE_COURSE ||
                                        ei.eventType == TimetableCore.TIMETABLE_EVENT_TYPE_EXAM)) {
                            if (icon != null) icon.setColorFilter(subjectColor);
                        } else if (icon != null) icon.setColorFilter(root.getColorPrimary());
                        break;
                    case "white":
                        if (icon != null) icon.setColorFilter(Color.WHITE);
                        break;
                    case "black":
                        if (icon != null) icon.setColorFilter(Color.BLACK);
                        break;
                    case "primary":
                        if (icon != null) icon.setColorFilter(root.getColorPrimary());
                        break;
                    case "accent":
                        if (icon != null) icon.setColorFilter(root.getColorAccent());
                        break;
                }
            } else {
                icon.setVisibility(GONE);
            }
        }

        card.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onCardClickListener != null) onCardClickListener.OnClick(v, ei);
            }
        });
        card.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (onCardLongClickListener != null) {
                    return onCardLongClickListener.OnLongClick(v, ei);
                } else return false;
            }
        });
        if (title != null) title.setText(ei.mainName);
        if (subtitle != null) subtitle.setText(TextUtils.isEmpty(ei.tag2) ? "" : ei.tag2);
        card.getBackground().mutate().setAlpha((int) (255 * ((float) root.getCardOpacity() / 100)));
        if (root.willBoldText()) {
            title.setTypeface(Typeface.DEFAULT_BOLD);
            if (subtitle != null) subtitle.setTypeface(Typeface.DEFAULT_BOLD);
        }
        if (title != null) title.setAlpha((float) root.getTitleAlpha() / 100);
        if (subtitle != null) subtitle.setAlpha((float) root.getSubtitleAlpha() / 100);
        if (title != null) title.setGravity(root.getTitleGravity());
    }

    private void initDuplicateCard(Context context) {
        final List<EventItem> list = (List<EventItem>) block;
        inflate(context, R.layout.dynamic_timetable_duplicate_card, this);
        card = findViewById(R.id.card);
        title = findViewById(R.id.title);
        icon = findViewById(R.id.icon);
        StringBuilder sb = new StringBuilder();
        for (EventItem ei : list) sb.append(ei.mainName).append(";\n");
        title.setText(sb.toString());
        card.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onDuplicateCardClickListener != null)
                    onDuplicateCardClickListener.OnDuplicateClick(v, list);
            }
        });

        EventItem mainItem = null;
        for (EventItem ei : list) {
            if (ei.getEventType() == TimetableCore.TIMETABLE_EVENT_TYPE_COURSE || ei.getEventType() == TimetableCore.TIMETABLE_EVENT_TYPE_EXAM) {
                mainItem = ei;
                break;
            }
        }
        if (root.isColorEnabled() && mainItem != null) {
            String query;
            if (mainItem.eventType == TimetableCore.TIMETABLE_EVENT_TYPE_EXAM && mainItem.mainName.endsWith("考试"))
                query = mainItem.getMainName().substring(0, mainItem.getMainName().length() - 2);
            else query = mainItem.getMainName();
            int getC = defaultSP.getInt("color:" + query, -1);
            if (getC == -1) {
                int color = ColorBox.getRandomColor_Material();
                card.setBackgroundTintList(ColorStateList.valueOf(color));
                defaultSP.edit().putInt("color:" + mainItem.getMainName(), color).apply();
            } else {
                card.setBackgroundTintList(ColorStateList.valueOf(getC));
            }
            switch (root.getTitleColor()) {
                case "subject":
                    title.setTextColor(getC);
                    break;
                case "white":
                    title.setTextColor(Color.WHITE);
                    break;
                case "black":
                    title.setTextColor(Color.BLACK);
                    break;
                case "primary":
                    title.setTextColor(root.getColorPrimary());
                    break;
                case "accent":
                    title.setTextColor(root.getColorAccent());
                    break;
            }
            switch (root.getIconColor()) {
                case "subject":
                    if (icon != null) icon.setColorFilter(getC);
                    break;
                case "white":
                    if (icon != null) icon.setColorFilter(Color.WHITE);
                    break;
                case "black":
                    if (icon != null) icon.setColorFilter(Color.BLACK);
                    break;
                case "primary":
                    if (icon != null) icon.setColorFilter(root.getColorPrimary());
                    break;
                case "accent":
                    if (icon != null) icon.setColorFilter(root.getColorAccent());
                    break;
            }
        } else {
            if (root.getCardBackground().equals("primary")) {
                card.setBackgroundTintList(ColorStateList.valueOf(root.getColorPrimary()));
            } else if (root.getCardBackground().equals("accent")) {
                card.setBackgroundTintList(ColorStateList.valueOf(root.getColorAccent()));
            }
            switch (root.getTitleColor()) {
                case "white":
                    title.setTextColor(Color.WHITE);
                    break;
                case "black":
                    title.setTextColor(Color.BLACK);
                    break;
                case "primary":
                    title.setTextColor(root.getColorPrimary());
                    break;
                case "accent":
                    title.setTextColor(root.getColorAccent());
                    break;
            }
            icon.setColorFilter(Color.WHITE);
        }
        if (icon != null) {
            if (!root.cardIconEnabled()) {
                icon.setColorFilter(title.getCurrentTextColor());
            }
        }
        card.getBackground().mutate().setAlpha((int) (255 * ((float) root.getCardOpacity() / 100)));
        if (root.willBoldText()) {
            title.setTypeface(Typeface.DEFAULT_BOLD);
        }
        title.setAlpha((float) root.getTitleAlpha() / 100);
        title.setGravity(root.getTitleGravity());
    }

    public int getDow() {
        if (block instanceof EventItem) {
            return ((EventItem) block).DOW;
        } else if (block instanceof List) {
            return ((List<EventItem>) block).get(0).DOW;
        }
        return -1;
    }

    public int getDuration() {
        if (block instanceof EventItem) {
            return ((EventItem) block).getDuration();
        } else if (block instanceof List) {
            return ((List<EventItem>) block).get(0).getDuration();
        }
        return -1;
    }

    public EventItem getEvent() {
        if (block instanceof EventItem) {
            return ((EventItem) block);
        } else if (block instanceof List) {
            return ((List<EventItem>) block).get(0);
        }
        return null;
    }


}
