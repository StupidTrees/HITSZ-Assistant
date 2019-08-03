package com.stupidtree.hita.activities;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Button;


import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;


import java.util.List;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.defaultSP;
import static com.stupidtree.hita.HITAApplication.getThemeID;
import static com.stupidtree.hita.HITAApplication.themeID;

public class ActivityTheme extends BaseActivity {

    RadioGroup radioGroup;
    CardView demoCard;
    CardView demoFab;
    Toolbar toolbar;
    LinearLayout demoCardDark;
    int[] radioButtons;
    ImageView demoBG;
   Button changeTheme;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true,true,false);
        setContentView(R.layout.activity_theme);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("设置主题颜色");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
       radioGroup = findViewById(R.id.radioGroup);
        demoCard = findViewById(R.id.demo_card);
        demoCardDark = findViewById(R.id.demo_card_dark);
        demoFab = findViewById(R.id.demo_fab);
        changeTheme = findViewById(R.id.bt_changetheme);
        demoBG = findViewById(R.id.demo_bg);
        radioButtons = new int[]{R.id.radioButton1,R.id.radioButton2,R.id.radioButton3,
                R.id.radioButton4,R.id.radioButton5,R.id.radioButton6,
                R.id.radioButton7,R.id.radioButton8,R.id.radioButton9,
                R.id.radioButton10,R.id.radioButton11,R.id.radioButton12,
        };

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                changeDemo(checkedId);

            }
        });

        changeTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos;
                for(pos = 0;pos<radioButtons.length;pos++){
                    if(radioButtons[pos]==radioGroup.getCheckedRadioButtonId()) break;
                }
                System.out.println("pos="+pos);

                defaultSP.edit().putInt("theme_id",pos).commit();
                getThemeID();
                Intent mStartActivity = new Intent(HContext,ActivityMain.class);
                int mPendingIntentId = 2333333;
                PendingIntent mPendingIntent = PendingIntent.getActivity(ActivityTheme.this, mPendingIntentId,mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager mgr = (AlarmManager) ActivityTheme.this.getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis(), mPendingIntent);
                System.exit(0);
//                AlertDialog dialog = new AlertDialog.Builder(ActivityTheme.this).create();
//                dialog.setTitle("更换成功");
//                dialog.setMessage("已更换主题，需要手动重启应用生效");
//                dialog.setButton(DialogInterface.BUTTON_POSITIVE, "退出应用", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                        // 1\. 通过Context获取ActivityManager
//                        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
//
//                        // 2\. 通过ActivityManager获取任务栈
//                        List<ActivityManager.AppTask> appTaskList = activityManager.getAppTasks();
//
//                        // 3\. 逐个关闭Activity
//                        for (ActivityManager.AppTask appTask : appTaskList) {
//                            appTask.finishAndRemoveTask();
//                        }
//
//
////                        // 4\. 结束进程
////                        System.exit(0);
//
//                    }
//                });
//                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "先不退出", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                    }
//                });
//                dialog.show();
                //Toast.makeText(ActivityTheme.this,"重启后生效",Toast.LENGTH_SHORT).show();
                //
            }
        });
        switch (themeID){
            case R.style.RedTheme:radioGroup.check(R.id.radioButton1);break;
            case R.style.PinkTheme:radioGroup.check(R.id.radioButton2);break;
            case R.style.BrownTheme:radioGroup.check(R.id.radioButton3);break;
            case R.style.BlueTheme:radioGroup.check(R.id.radioButton4);break;
            case R.style.BlueGreyTheme:radioGroup.check(R.id.radioButton5);break;
            case R.style.TealTheme:radioGroup.check(R.id.radioButton6);break;
            case R.style.DeepPurpleTheme:radioGroup.check(R.id.radioButton7);break;
            case R.style.GreenTheme:radioGroup.check(R.id.radioButton8);break;
            case R.style.DeepOrangeTheme:radioGroup.check(R.id.radioButton9);break;
            case R.style.IndigoTheme:radioGroup.check(R.id.radioButton10);break;
            case R.style.CyanTheme:radioGroup.check(R.id.radioButton11);break;
            case R.style.AmberTheme:radioGroup.check(R.id.radioButton12);break;
        }
        changeDemo(radioGroup.getCheckedRadioButtonId());


    }

    void changeDemo(int checkedId){
        GradientDrawable aDrawable = null;
        switch (checkedId){
            case R.id.radioButton1:
                aDrawable =new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{ContextCompat.getColor(HContext,R.color.red_primary),ContextCompat.getColor(HContext,R.color.red_fade)});
                demoFab.setCardBackgroundColor(ContextCompat.getColor(HContext,R.color.red_accent));
                break;
            case R.id.radioButton2:
                aDrawable =new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{ContextCompat.getColor(HContext,R.color.pink_primary),ContextCompat.getColor(HContext,R.color.pink_fade)});
                demoFab.setCardBackgroundColor(ContextCompat.getColor(HContext,R.color.pink_accent));
                break;
            case R.id.radioButton3:
                aDrawable =new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{ContextCompat.getColor(HContext,R.color.brown_primary),ContextCompat.getColor(HContext,R.color.brown_fade)});
                demoFab.setCardBackgroundColor(ContextCompat.getColor(HContext,R.color.brown_accent));
                break;
            case R.id.radioButton4:
                aDrawable =new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{ContextCompat.getColor(HContext,R.color.blue_primary),ContextCompat.getColor(HContext,R.color.blue_fade)});
                demoFab.setCardBackgroundColor(ContextCompat.getColor(HContext,R.color.blue_accent));
                break;
            case R.id.radioButton5:
                aDrawable =new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{ContextCompat.getColor(HContext,R.color.blue_grey_primary),ContextCompat.getColor(HContext,R.color.blue_grey_fade)});
                demoFab.setCardBackgroundColor(ContextCompat.getColor(HContext,R.color.blue_grey_accent));
                break;
            case R.id.radioButton6:
                aDrawable =new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{ContextCompat.getColor(HContext,R.color.teal_primary),ContextCompat.getColor(HContext,R.color.teal_fade)});
                demoFab.setCardBackgroundColor(ContextCompat.getColor(HContext,R.color.teal_accent));
                break;
            case R.id.radioButton7:
                aDrawable =new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{ContextCompat.getColor(HContext,R.color.deep_purple_primary),ContextCompat.getColor(HContext,R.color.deep_purple_fade)});
                demoFab.setCardBackgroundColor(ContextCompat.getColor(HContext,R.color.deep_purple_accent));
                break;
            case R.id.radioButton8:
                aDrawable =new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{ContextCompat.getColor(HContext,R.color.green_primary),ContextCompat.getColor(HContext,R.color.green_fade)});
                demoFab.setCardBackgroundColor(ContextCompat.getColor(HContext,R.color.green_accent));
                break;
            case R.id.radioButton9:
                aDrawable =new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{ContextCompat.getColor(HContext,R.color.deep_orange_primary),ContextCompat.getColor(HContext,R.color.deep_orange_fade)});
                demoFab.setCardBackgroundColor(ContextCompat.getColor(HContext,R.color.deep_orange_accent));
                break;
            case R.id.radioButton10:
                aDrawable =new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{ContextCompat.getColor(HContext,R.color.indigo_primary),ContextCompat.getColor(HContext,R.color.indigo_fade)});
                demoFab.setCardBackgroundColor(ContextCompat.getColor(HContext,R.color.indigo_accent));
                break;
            case R.id.radioButton11:
                aDrawable =new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{ContextCompat.getColor(HContext,R.color.cyan_primary),ContextCompat.getColor(HContext,R.color.cyan_fade)});
                demoFab.setCardBackgroundColor(ContextCompat.getColor(HContext,R.color.cyan_accent));
                break;
            case R.id.radioButton12:
                aDrawable =new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{ContextCompat.getColor(HContext,R.color.amber_primary),ContextCompat.getColor(HContext,R.color.amber_fade)});
                demoFab.setCardBackgroundColor(ContextCompat.getColor(HContext,R.color.amber_accent));
                break;

        }
       demoBG.setImageDrawable(aDrawable);
    }
}
