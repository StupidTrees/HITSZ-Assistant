package com.stupidtree.hita.fragments.popup;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivityCurriculumManager;
import com.stupidtree.hita.timetable.packable.HTime;
import com.stupidtree.hita.util.ActivityUtils;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.Objects;


@SuppressLint("ValidFragment")
public class FragmentTimetablePanel extends FragmentRadiusPopup {

    private ExpandableLayout expand;
    private Switch drawLine, enableColor, drawBGLine;
    private TextView fromTimeButton;
    private HTime fromTime;


    private PanelRoot root;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        WindowManager.LayoutParams lp = Objects.requireNonNull(dialog.getWindow()).getAttributes();
        lp.dimAmount = 0.2f;
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = View.inflate(getContext(), R.layout.fragment_timetable_panel, null);
        initViews(view);

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof PanelRoot) {
            root = (PanelRoot) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void initViews(View v) {
        CardView manager = v.findViewById(R.id.manager);
        CardView settings = v.findViewById(R.id.appearence);
        expand = v.findViewById(R.id.expand);
        enableColor = v.findViewById(R.id.colors);
        drawLine = v.findViewById(R.id.drawline);
        drawBGLine = v.findViewById(R.id.drawbglines);
        Button resetColor = v.findViewById(R.id.reset);
        fromTimeButton = v.findViewById(R.id.from);
        fromTime = new HTime(root.getSP().getString("timetable_start_time", "08:00"));
        enableColor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                if (!buttonView.isPressed()) return;
                buttonView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                expand.setExpanded(isChecked);
                if (root != null) root.changeEnableColor(isChecked);

            }
        });
        drawLine.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                buttonView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                if (!buttonView.isPressed()) return;
                if (root != null) root.drawLineChanged(isChecked);

            }
        });
        drawBGLine.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                if (!buttonView.isPressed()) return;
                if (root != null) root.drawBGLineChanged(isChecked);
            }
        });
        resetColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                if (root != null) root.callResetColor();
            }
        });
        fromTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                        fromTime.setTime(hourOfDay, minute);
                        fromTimeButton.setText(fromTime.tellTime());
                        if (root != null) root.changeFromTime(fromTime);


                    }
                }, fromTime.hour, fromTime.minute, true)
                        .show();
            }
        });
        manager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), ActivityCurriculumManager.class);
                startActivity(i);
            }
        });
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.startSettingFor(getActivity(), "appearance");
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        enableColor.setChecked(root.getSP().getBoolean("subjects_color_enable", false));
        drawLine.setChecked(root.getSP().getBoolean("timetable_draw_now_line", true));
        fromTimeButton.setText(fromTime.tellTime());
        expand.setExpanded(enableColor.isChecked());
        drawBGLine.setChecked(root.getSP().getBoolean("timetable_draw_bg_line", false));
    }


    public interface PanelRoot {
        void changeEnableColor(boolean changed);

        void drawLineChanged(boolean changed);

        void callResetColor();

        void changeFromTime(HTime from);

        void drawBGLineChanged(boolean isChecked);

        SharedPreferences getSP();
    }
}
