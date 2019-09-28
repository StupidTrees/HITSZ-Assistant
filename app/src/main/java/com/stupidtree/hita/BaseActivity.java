package com.stupidtree.hita;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
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
import com.stupidtree.hita.activities.ActivitySearch;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static com.stupidtree.hita.HITAApplication.themeID;

public abstract class BaseActivity extends AppCompatActivity {

    abstract protected void stopTasks();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O && isTranslucentOrFloating()) {
            fixOrientation();
        }
        super.onCreate(savedInstanceState);
        if(this instanceof ActivityChatbot||this instanceof ActivitySearch){
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
        try {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//禁止屏幕旋转
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean fixOrientation(){
        try {
            Field field = Activity.class.getDeclaredField("mActivityInfo");
            field.setAccessible(true);
            ActivityInfo o = (ActivityInfo)field.get(this);
            o.screenOrientation = -1;
            field.setAccessible(false);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    private boolean isTranslucentOrFloating(){
        boolean isTranslucentOrFloating = false;
        try {
            int [] styleableRes = (int[]) Class.forName("com.android.internal.R$styleable").getField("Window").get(null);
            final TypedArray ta = obtainStyledAttributes(styleableRes);
            Method m = ActivityInfo.class.getMethod("isTranslucentOrFloating", TypedArray.class);
            m.setAccessible(true);
            isTranslucentOrFloating = (boolean)m.invoke(null, ta);
            m.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isTranslucentOrFloating;
    }

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O && isTranslucentOrFloating()) {
            return;
        }
        super.setRequestedOrientation(requestedOrientation);
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
