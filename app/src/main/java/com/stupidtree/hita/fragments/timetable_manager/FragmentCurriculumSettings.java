package com.stupidtree.hita.fragments.timetable_manager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.BaseActivity;
import com.stupidtree.hita.fragments.BaseOperationTask;
import com.stupidtree.hita.timetable.packable.Curriculum;
import com.stupidtree.hita.timetable.packable.Subject;
import com.stupidtree.hita.util.ColorBox;
import com.stupidtree.hita.views.PickNumberDialog;
import com.stupidtree.hita.views.PickSimpleDateDialog;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.Calendar;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.TPE;
import static com.stupidtree.hita.HITAApplication.timeTableCore;
import static com.stupidtree.hita.timetable.TimeWatcherService.TIMETABLE_CHANGED;


public class FragmentCurriculumSettings extends FragmentCurriculumChild
        implements BaseOperationTask.OperationListener<Object> {
    private TextView totalWeeks, nameText;
    private ExpandableLayout expandableLayout;
    private TextView from;

    public FragmentCurriculumSettings() {
        // Required empty public constructor
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_curriculum_settings;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initList(view);
    }




    @Override
    public void onResume() {
        super.onResume();
        if (willRefreshOnResume) {
            willRefreshOnResume = false;
            Refresh();
        }
    }

    private void initList(View v) {
        nameText = v.findViewById(R.id.name_text);
        LinearLayout setName = v.findViewById(R.id.set_name);
        Button delete = v.findViewById(R.id.delete);
        LinearLayout setTotalWeek = v.findViewById(R.id.set_total_weeks);
        LinearLayout setStartDate = v.findViewById(R.id.set_start_date);
        from = v.findViewById(R.id.start_date_text);
        totalWeeks = v.findViewById(R.id.total_weeks_text);
        Switch enable_color = v.findViewById(R.id.enable_color);
        expandableLayout = v.findViewById(R.id.expandable);
        TextView resetColors = v.findViewById(R.id.reset_colors);
        resetColors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                AlertDialog ad = new AlertDialog.Builder(requireContext()).setTitle(getString(R.string.dialog_title_random_allocate))
                        .setNegativeButton(getString(R.string.button_cancel), null).setPositiveButton(getString(R.string.button_confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new resetColorTask(FragmentCurriculumSettings.this, root.getTimetableSP()).executeOnExecutor(TPE);
                            }
                        }).create();
                ad.show();
            }
        });
        setName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                @SuppressLint("InflateParams") View lv = getLayoutInflater().inflate(R.layout.dialog_editinfo, null);
                final EditText et = lv.findViewById(R.id.setinfo_text);
                et.setText(root.getCurriculum().getName());
                new AlertDialog.Builder(requireContext()).setTitle(R.string.notifi_curriculum_set_name)
                        .setView(lv)
                        .setPositiveButton(getString(R.string.button_confirm), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                root.getCurriculum().setName(et.getText().toString());
                                new saveCurriculumTask(FragmentCurriculumSettings.this, root.getCurriculum()).executeOnExecutor(TPE);
                            }
                        })
                        .setNegativeButton(getString(R.string.button_cancel), null)
                        .show();

            }
        });
        boolean colorfulOn = root.getTimetableSP().getBoolean("subjects_color_enable", false);
        enable_color.setChecked(colorfulOn);
        if (colorfulOn) expandableLayout.expand();
        else expandableLayout.collapse();

        enable_color.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                buttonView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                if (isChecked) expandableLayout.expand();
                else expandableLayout.collapse();
                new enableColorTask(FragmentCurriculumSettings.this, isChecked, root.getTimetableSP()).execute();
            }
        });
        setTotalWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new PickNumberDialog((BaseActivity) getActivity(), getString(R.string.dialog_title_set_total_weeks), 50, 1, new PickNumberDialog.onDialogConformListener() {
                    @SuppressLint("StaticFieldLeak")
                    @Override
                    public void onClick(int number) {
                        root.getCurriculum().setTotalWeeks(number);
                        new saveCurriculumTask(FragmentCurriculumSettings.this, root.getCurriculum()).executeOnExecutor(TPE);
                    }
                }).setInitialValue(root.getCurriculum().getTotalWeeks()).show();
            }
        });

        setStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new PickSimpleDateDialog((BaseActivity) getActivity(), new PickSimpleDateDialog.onDialogConformListener() {
                    @Override
                    public void onConfirm(Calendar date) {
                        root.getCurriculum().setStartDate(date);
                        new saveCurriculumTask(FragmentCurriculumSettings.this, root.getCurriculum()).executeOnExecutor(TPE);
                    }
                }).setInitialValue(root.getCurriculum().getStartDate())
                        .show();
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                AlertDialog ad = new AlertDialog.Builder(requireContext()).setTitle(getString(R.string.attention)).setMessage(getString(R.string.dialog_message_delete_curriculum)).
                        setPositiveButton(getString(R.string.button_confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new deleteTask(FragmentCurriculumSettings.this, root.getCurriculum().getCurriculumCode()).executeOnExecutor(TPE);
                            }
                        }).setNegativeButton(getString(R.string.button_cancel), null).
                        create();
                ad.show();
            }
        });
/*
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float fromD,toD;
                if(!headExpand.isExpanded()){
                    fromD = 0f;
                    toD = 180f;
                }else{
                    fromD = 180f;
                    toD = 0f;
                }
                RotateAnimation ra = new RotateAnimation(fromD,toD, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                ra.setInterpolator(new DecelerateInterpolator());
                ra.setDuration(300);//设置动画持续周期
                ra.setRepeatCount(0);//设置重复次数
                ra.setFillAfter(true);//动画执行完后是否停留在执行完的状态
                more.setAnimation(ra);
                more.startAnimation(ra);
                headExpand.toggle();
            }
        });
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog ad = new AlertDialog.Builder(requireContext()).setTitle(getString(R.string.dialog_title_curriculum_detail)).
                        setMessage(String.format(getString(R.string.dialog_message_curriculum_detail), curriculum.getName(), curriculum.getCurriculumCode())).create();
                ad.show();
            }
        });
*/
    }

    @Override
    protected void stopTasks() {
    }

    @Override
    public void Refresh() {
        if (root == null || root.getCurriculum() == null) return;
        from.setText(root.getCurriculum().readStartDate());
        totalWeeks.setText(String.format(getString(R.string.curriculum_manager_total), root.getCurriculum().getTotalWeeks()));
        nameText.setText(root.getCurriculum().getName());
    }

    @Override
    public void onOperationStart(String id, Boolean[] params) {

    }

    @Override
    public void onOperationDone(String id, BaseOperationTask task, Boolean[] params, Object result) {
        Intent i = new Intent(TIMETABLE_CHANGED);
        switch (id) {
            case "reset":
            case "enable":
                root.onChangeColorSettingsRefresh();
                break;
            case "save":
                root.onModifiedCurriculumRefresh();
                Refresh();
                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(i);
                Toast.makeText(requireContext(), R.string.curriculum_updated, Toast.LENGTH_SHORT).show();
                break;
            case "delete":
                if ((Boolean) result) {
                    Toast.makeText(HContext, R.string.delete_success, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(HContext, R.string.delete_failed, Toast.LENGTH_SHORT).show();
                }
                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(i);
                root.onCurriculumDeleteRefresh();
                break;
        }
    }


    static class enableColorTask extends BaseOperationTask<Object> {

        boolean enable;
        SharedPreferences sharedPreferences;

        enableColorTask(OperationListener<?> listRefreshedListener, Boolean enable, SharedPreferences sp) {
            super(listRefreshedListener);
            this.enable = enable;
            id = "enable";
            this.sharedPreferences = sp;
        }


        @SuppressLint("ApplySharedPref")
        @Override
        protected Object doInBackground(OperationListener<Object> listRefreshedListener, Boolean... booleans) {
            sharedPreferences.edit().putBoolean("subjects_color_enable", enable).commit();
            return super.doInBackground(listRefreshedListener, booleans);
        }
    }


    static class resetColorTask extends BaseOperationTask<Object> {

        SharedPreferences sharedPreferences;

        resetColorTask(OperationListener<?> listRefreshedListener, SharedPreferences sharedPreferences) {
            super(listRefreshedListener);
            this.sharedPreferences = sharedPreferences;
            id = "reset";
        }

        @Override
        protected Object doInBackground(OperationListener<Object> listRefreshedListener, Boolean... booleans) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            for (Subject s : timeTableCore.getCurrentCurriculum().getSubjects()) {
                editor.putInt("color:" + s.getName(), ColorBox.getRandomColor_Material());
            }
            editor.apply();
            return super.doInBackground(listRefreshedListener, booleans);
        }

    }

    static class saveCurriculumTask extends BaseOperationTask<Object> {

        Curriculum curriculum;

        saveCurriculumTask(OperationListener<?> listRefreshedListener, Curriculum curriculum) {
            super(listRefreshedListener);
            this.curriculum = curriculum;
            id = "save";
        }


        @Override
        protected Object doInBackground(OperationListener<Object> listRefreshedListener, Boolean... booleans) {
            if (curriculum != null) curriculum.saveToDB();
            if (timeTableCore.getCurrentCurriculum() != null &&
                    timeTableCore.getCurrentCurriculum().getCurriculumCode().equals(curriculum.getCurriculumCode())) {
                timeTableCore.updateCurrentCurriculumInfo(curriculum);
            }
            return super.doInBackground(listRefreshedListener, booleans);
        }
    }

    static class deleteTask extends BaseOperationTask<Boolean> {

        String curriculumCode;

        deleteTask(OperationListener<?> listRefreshedListener, String curriculumCode) {
            super(listRefreshedListener);
            this.curriculumCode = curriculumCode;
            id = "delete";
        }

        @Override
        protected Boolean doInBackground(OperationListener listRefreshedListener, Boolean... booleans) {
            return timeTableCore.deleteCurriculum(curriculumCode);
        }


    }
}
