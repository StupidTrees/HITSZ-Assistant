package com.stupidtree.hita;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.ColorUtils;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;

import com.stupidtree.hita.activities.ActivityChatbot;

import static com.stupidtree.hita.HITAApplication.themeID;

public abstract class BaseActivity extends AppCompatActivity {

    abstract protected void stopTasks();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(this instanceof ActivityChatbot){
            switch (themeID){
                case R.style.BlueTheme:setTheme(R.style.BlueTheme_chatbot);break;
                case R.style.RedTheme:setTheme(R.style.RedTheme_chatbot);break;
                case R.style.TealTheme:setTheme(R.style.TealTheme_chatbot);break;
                case R.style.GreenTheme:setTheme(R.style.GreenTheme_chatbot);break;
                case R.style.PinkTheme:setTheme(R.style.PinkTheme_chatbot);break;
                case R.style.DeepOrangeTheme:
                    setTheme(R.style.DeepOrangeTheme_chatbot);
                   // AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    break;
                case R.style.AmberTheme:setTheme(R.style.AmberTheme_chatbot);break;
                case R.style.BrownTheme:setTheme(R.style.BrownTheme_chatbot);break;
                case R.style.BlueGreyTheme:setTheme(R.style.BlueGreyTheme_chatbot);break;
                case R.style.DeepPurpleTheme:setTheme(R.style.DeepPurpleTheme_chatbot);break;
                case R.style.CyanTheme:setTheme(R.style.CyanTheme_chatbot);break;
                case R.style.IndigoTheme:setTheme(R.style.IndigoTheme_chatbot);break;
            }
        }else{
           // if(themeID==R.style.DeepOrangeTheme) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
           // else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            setTheme(themeID);
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//禁止屏幕旋转
    }


    protected void setWindowParams(Boolean statusBar, Boolean darkColor, Boolean navi){
        if(darkColor&&isLightColor(getBackgroundColorBottom())){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }else{
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }

       // if (AppCompatDelegate.getDefaultNightMode()!=AppCompatDelegate.MODE_NIGHT_YES&&Build.VERSION.SDK_INT >= Build.VERSION_CODES.M&&darkColor)getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if(statusBar)getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if(navi)getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
    }

    private boolean isLightColor(@ColorInt int color) {
        return ColorUtils.calculateLuminance(color) >= 0.5;
    }
    public int getColorPrimary(){
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        return typedValue.data;
    }
    public int getTextColorPrimary(){
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.text_color_primary, typedValue, true);
        return typedValue.data;
    }
    public int getTextColorIconic(){
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.text_color_iconic, typedValue, true);
        return typedValue.data;
    }
    public int getColorPrimaryDark(){
        TypedValue typedValue = new  TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
        return typedValue.data;
    }

    public int getBackgroundColorBottom(){
        TypedValue typedValue = new  TypedValue();
        getTheme().resolveAttribute(R.attr.background_color_bottom, typedValue, true);
        return typedValue.data;
    }

    public int getColorAccent(){
        TypedValue typedValue = new  TypedValue();
        getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true);
        return typedValue.data;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTasks();
        Log.e("onDestroy","停止任务");
    }

}
