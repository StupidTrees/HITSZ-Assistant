package com.stupidtree.hita.fragments.events;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.stupidtree.hita.R;
import com.stupidtree.hita.fragments.popup.FragmentAddEvent;
import com.stupidtree.hita.util.ActivityUtils;
import com.stupidtree.hita.util.EventsUtils;

import java.util.Calendar;

import static com.stupidtree.hita.HITAApplication.timeTableCore;


public class FragmentExam extends FragmentEventItem {
    private View subject;


    public FragmentExam() {
    }

    @SuppressLint("SetTextI18n")
    private void initViews(View dlgView) {
        ViewGroup nameLayout = dlgView.findViewById(R.id.name_layout);
        subject = dlgView.findViewById(R.id.subject);
        TextView value1 = dlgView.findViewById(R.id.tt_dlg_value1);
        TextView value2 = dlgView.findViewById(R.id.tt_dlg_value2);
        value1.setText(eventItem.tag2);//考场
        value2.setText(eventItem.tag4.isEmpty() ? getString(R.string.none) : eventItem.tag4);//具体考试时间
        TextView date = dlgView.findViewById(R.id.tt_dlg_date);
        TextView name = dlgView.findViewById(R.id.tt_dlg_name);
        name.setText(eventItem.mainName);
        final Calendar c = timeTableCore.getCurrentCurriculum().getDateAtWOT(eventItem.week, eventItem.DOW);
        date.setText(EventsUtils.getDateString(c, false, EventsUtils.TTY_FOLLOWING)
                + "\n" +
                EventsUtils.getWeekDowString(eventItem, false, EventsUtils.TTY_WK_FOLLOWING));

        subject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (eventItem.tag3.startsWith("科目代码：")) {
                    ActivityUtils.startSubjectActivity_code(requireContext(), eventItem.tag3.substring(5));
                } else if (eventItem.tag3.startsWith("科目名称：")) {
                    ActivityUtils.startSubjectActivity_name(requireContext(), eventItem.tag3.substring(5));
                } else {
                    ActivityUtils.startSubjectActivity_name(requireContext(), eventItem.tag3);
                }

            }
        });
        nameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subject.callOnClick();
            }
        });
        View delete = dlgView.findViewById(R.id.delete);
        View edit = dlgView.findViewById(R.id.edit);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                android.app.AlertDialog ad = new android.app.AlertDialog.Builder(requireContext()).
                        setNegativeButton(getString(R.string.button_cancel), null)
                        .setPositiveButton(getString(R.string.button_confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int which) {
                                deleteEvent();
                            }
                        }).create();
                ad.setTitle(getString(R.string.dialog_title_sure_delete));
                ad.show();
            }
        });
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = ((Fragment) popupRoot).getParentFragmentManager();
                FragmentAddEvent.newInstance().setEditEvent(eventItem).show(fragmentManager, "edit");
                popupRoot.callDismiss();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_timetable_exam;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
    }


}
