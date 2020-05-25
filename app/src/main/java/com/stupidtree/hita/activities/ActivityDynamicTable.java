package com.stupidtree.hita.activities;

import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.Switch;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.stupidtree.hita.R;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.Objects;

import static com.stupidtree.hita.HITAApplication.defaultSP;
import static com.stupidtree.hita.HITAApplication.timeServiceBinder;
import static com.stupidtree.hita.timetable.TimeWatcherService.WATCHER_REFRESH;

public class ActivityDynamicTable extends BaseActivity {

    Toolbar toolbar;
    Switch preViewPlan,preview_skip_no_exam,auto_mute,auto_mute_after,forced_mute,event_notify;
    NumberPicker preview_length_picker,auto_mute_before_picker;
    ExpandableLayout preview_expand,mute_expand;
    CollapsingToolbarLayout collapsingToolbarLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_table);
        setWindowParams(true,false,false);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.label_activity_learning_assistant));
        collapsingToolbarLayout = findViewById(R.id.collapse);
        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
        collapsingToolbarLayout.setExpandedTitleColor(Color.WHITE);
       // toolbar.inflateMenu(R.menu.toolbar_dynamic_timetable);
        //toolbar.setBackgroundColor(getColorPrimary());
        toolbar.setTitleTextColor(ContextCompat.getColor(this,R.color.material_text_icon_white));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        initEventsNotify();
        initAutoMute();
        initPreview();
    }

    void initEventsNotify(){
        event_notify = findViewById(R.id.switch_class_notification);
        event_notify.setChecked(defaultSP.getBoolean("event_notify_enable",true));
        event_notify.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Intent i = new Intent(WATCHER_REFRESH);
                LocalBroadcastManager.getInstance(getThis()).sendBroadcast(i);
                defaultSP.edit().putBoolean("event_notify_enable",isChecked).apply();
            }
        });
    }
    void initAutoMute(){
        mute_expand = findViewById(R.id.exand_mute);
        auto_mute = findViewById(R.id.switch_auto_mute);
        auto_mute_before_picker = findViewById(R.id.numberpicker_mute_before);
        auto_mute_after = findViewById(R.id.switch_mute_after);
        forced_mute = findViewById(R.id.forced_mute);
        auto_mute_before_picker.setMaxValue(15);
        auto_mute_after.setChecked(defaultSP.getBoolean("auto_mute_after",true));
        auto_mute_before_picker.setValue(defaultSP.getInt("auto_mute_before",15));
        auto_mute.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                try {
                    if(isChecked){
                        if(forced_mute.isChecked()) {
                            timeServiceBinder.registerVolumeWatcher();
                            }
                            NotificationManager notificationManager =
                                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                            if(notificationManager!=null&&!notificationManager.isNotificationPolicyAccessGranted()) {
                                auto_mute.setChecked(false);
                                Intent intent = new Intent(
                                        android.provider.Settings
                                                .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                                startActivity(intent);
                            }else{
                                mute_expand.expand();
                                defaultSP.edit().putBoolean("auto_mute",true).apply();
                            }

                    }
                    else{
                        mute_expand.collapse();
                        if(forced_mute.isChecked()) timeServiceBinder.unRegisterVolumeWatcher();
                        defaultSP.edit().putBoolean("auto_mute",false).apply();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        forced_mute.setChecked(defaultSP.getBoolean("forced_mute",false));
        forced_mute.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                try {
                    defaultSP.edit().putBoolean("forced_mute",b).apply();
                    if(b){
                        timeServiceBinder.registerVolumeWatcher();
                    }else{
                        timeServiceBinder.unRegisterVolumeWatcher();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

                if(isChecked) {
                    defaultSP.edit().putBoolean("dtt_preview", true).apply();
                    preview_expand.expand();
                }
                else {
                    defaultSP.edit().putBoolean("dtt_preview", false).apply();
                    preview_expand.collapse();
                }

            }
        });
        preViewPlan.setChecked(defaultSP.getBoolean("dtt_preview", false));
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
