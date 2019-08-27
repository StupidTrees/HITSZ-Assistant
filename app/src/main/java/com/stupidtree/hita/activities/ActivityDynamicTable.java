package com.stupidtree.hita.activities;

import androidx.core.content.ContextCompat;

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

import static com.stupidtree.hita.HITAApplication.defaultSP;

public class ActivityDynamicTable extends BaseActivity {

    Toolbar toolbar;
    Switch preViewPlan,preview_skip_no_exam;
    NumberPicker preview_length_picker;
    ExpandableLayout preview_expand;
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
        initPreview();
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
