package com.stupidtree.hita.activities;

import androidx.core.content.ContextCompat;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.Switch;

import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;

import net.cachapa.expandablelayout.ExpandableLayout;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.defaultSP;

public class ActivityDynamicTable extends BaseActivity {

    Toolbar toolbar;
    Switch preViewPlan,preview_skip_no_exam,auto_mute,auto_mute_after;
    NumberPicker preview_length_picker,auto_mute_before_picker;
    ExpandableLayout preview_expand,mute_expand;
    @Override
    protected void stopTasks() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_table);
        setWindowParams(true,false,false);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("学习助手");

       // toolbar.inflateMenu(R.menu.toolbar_dynamic_timetable);
        toolbar.setBackgroundColor(getColorPrimary());
        toolbar.setTitleTextColor(ContextCompat.getColor(this,R.color.material_text_icon_white));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
//        switchButton.setChecked();
//        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//             }
//        });
        initAutoMute();
        initPreview();
    }

    void initAutoMute(){
        mute_expand = findViewById(R.id.exand_mute);
        auto_mute = findViewById(R.id.switch_auto_mute);
        auto_mute_before_picker = findViewById(R.id.numberpicker_mute_before);
        auto_mute_after = findViewById(R.id.switch_mute_after);
        auto_mute_before_picker.setMaxValue(15);
        auto_mute_after.setChecked(defaultSP.getBoolean("auto_mute_after",true));
        auto_mute_before_picker.setValue(defaultSP.getInt("auto_mute_before",15));
        auto_mute.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                if(isChecked){
                        NotificationManager notificationManager =
                                (NotificationManager) HContext.getSystemService(HContext.NOTIFICATION_SERVICE);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                                && !notificationManager.isNotificationPolicyAccessGranted()) {
                            Intent intent = new Intent(
                                    android.provider.Settings
                                            .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                            startActivity(intent);
                        }
                    mute_expand.expand();
                }
                else mute_expand.collapse();
                defaultSP.edit().putBoolean("auto_mute",isChecked).apply();
            }
        });
        auto_mute.setChecked(defaultSP.getBoolean("auto_mute",false));
        auto_mute_after.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                defaultSP.edit().putBoolean("auto_mute_after",isChecked).apply();
            }
        });
       auto_mute_before_picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                defaultSP.edit().putInt("auto_mute_before",newVal).apply();
            }
        });
    }
    void initPreview(){

        preview_expand = findViewById(R.id.expand_preview);
        preViewPlan = findViewById(R.id.previewPlan);
        preview_skip_no_exam = findViewById(R.id.switch_skip_no_exam);
        preview_length_picker = findViewById(R.id.numberpicker_prevew_length);
        preview_length_picker.setMaxValue(180);
        preview_skip_no_exam.setChecked(defaultSP.getBoolean("dtt_preview_skip_no_exam",true));
        preview_length_picker.setValue(defaultSP.getInt("dtt_preview_length",60));
        preViewPlan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                if(isChecked) preview_expand.expand();
                else preview_expand.collapse();
                defaultSP.edit().putBoolean("dtt_preview",isChecked).apply();
            }
        });
        preViewPlan.setChecked(defaultSP.getBoolean("dtt_preview",false));
        preview_skip_no_exam.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                defaultSP.edit().putBoolean("dtt_preview_skip_no_exam",isChecked).apply();
            }
        });
        preview_length_picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                defaultSP.edit().putInt("dtt_preview_length",newVal).apply();
            }
        });


    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.toolbar_dynamic_timetable,menu);
//                MenuItem item = (MenuItem) menu.findItem(R.id.action_dynamic_switch);
//       // item.setActionView(R.layout.util_dynamictimetable_toolbar_actionlayout);
////        final Switch switchA = item
////                .getActionView().findViewById(R.id.action_layout_switch);
//
//        return super.onCreateOptionsMenu(menu);
//    }
}
