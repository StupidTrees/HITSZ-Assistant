package com.stupidtree.hita.fragments.events;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.stupidtree.hita.R;
import com.stupidtree.hita.fragments.popup.FragmentAddEvent;
import com.stupidtree.hita.util.EventsUtils;

import java.util.Calendar;

import static com.stupidtree.hita.HITAApplication.timeTableCore;


public class FragmentArrangement extends FragmentEventItem {


    public FragmentArrangement() {
    }

    @SuppressLint("SetTextI18n")
    private void initViews(View dlgView) {
        TextView value1 = dlgView.findViewById(R.id.tt_dlg_value1);
        TextView value2 = dlgView.findViewById(R.id.tt_dlg_value2);
        TextView value3 = dlgView.findViewById(R.id.tt_dlg_value3);
        value1.setText(eventItem.tag2.isEmpty() ? getString(R.string.none) : eventItem.tag2);//标签1
        value2.setText(eventItem.tag3.isEmpty() ? getString(R.string.none) : eventItem.tag3);//标签2

        TextView date = dlgView.findViewById(R.id.tt_dlg_date);
        TextView name = dlgView.findViewById(R.id.tt_dlg_name);
        name.setText(eventItem.mainName);
        if (eventItem.isWholeDay()) {
            value3.setText(getString(R.string.wholeday));
        } else {
            value3.setText(eventItem.startTime.tellTime() + "-" + eventItem.endTime.tellTime());
        }
        final Calendar c = timeTableCore.getCurrentCurriculum().getDateAtWOT(eventItem.week, eventItem.DOW);
        date.setText(EventsUtils.getDateString(c, false, EventsUtils.TTY_FOLLOWING)
                + "\n" +
                EventsUtils.getWeekDowString(eventItem, false, EventsUtils.TTY_WK_FOLLOWING));
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
    protected int getLayoutId() {
        return R.layout.dialog_timetable_arrangement;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
    }


}
