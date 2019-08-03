package com.stupidtree.hita.activities;

import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;

import java.net.HttpCookie;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.defaultSP;

public class ActivityDynamicTable extends BaseActivity {

    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_table);
        setWindowParams(true,false,false);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("动态时间表生成");

        toolbar.inflateMenu(R.menu.toolbar_dynamic_timetable);
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_dynamic_timetable,menu);
                MenuItem item = (MenuItem) menu.findItem(R.id.action_dynamic_switch);
        item.setActionView(R.layout.util_dynamictimetable_toolbar_actionlayout);
        final Switch switchA = item
                .getActionView().findViewById(R.id.action_layout_switch);
        switchA.setChecked(defaultSP.getBoolean("dynamicTimeTable",false));
        switchA.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                  defaultSP.edit().putBoolean("dynamicTimeTable",isChecked).commit();
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
}
