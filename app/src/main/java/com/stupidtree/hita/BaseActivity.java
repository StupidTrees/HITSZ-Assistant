package com.stupidtree.hita;

import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;

import com.stupidtree.hita.activities.ActivityChatbot;

import java.util.HashSet;
import java.util.Set;

import static com.stupidtree.hita.HITAApplication.themeID;

public abstract class BaseActivity extends AppCompatActivity {

    abstract protected void stopTasks();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//禁止屏幕旋转
        super.onCreate(savedInstanceState);
        if(this instanceof ActivityChatbot){
            switch (themeID){
                case R.style.BlueTheme:setTheme(R.style.BlueTheme_chatbot);break;
                case R.style.RedTheme:setTheme(R.style.RedTheme_chatbot);break;
                case R.style.TealTheme:setTheme(R.style.TealTheme_chatbot);break;
                case R.style.GreenTheme:setTheme(R.style.GreenTheme_chatbot);break;
                case R.style.PinkTheme:setTheme(R.style.PinkTheme_chatbot);break;
                case R.style.DeepOrangeTheme:setTheme(R.style.DeepOrangeTheme_chatbot);break;
                case R.style.AmberTheme:setTheme(R.style.AmberTheme_chatbot);break;
                case R.style.BrownTheme:setTheme(R.style.BrownTheme_chatbot);break;
                case R.style.BlueGreyTheme:setTheme(R.style.BlueGreyTheme_chatbot);break;
                case R.style.DeepPurpleTheme:setTheme(R.style.DeepPurpleTheme_chatbot);break;
                case R.style.CyanTheme:setTheme(R.style.CyanTheme_chatbot);break;
                case R.style.IndigoTheme:setTheme(R.style.IndigoTheme_chatbot);break;
            }
        }else{
            setTheme(themeID);
        }
    }


    protected void setWindowParams(Boolean statusBar, Boolean darkColor, Boolean navi){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M&&darkColor)getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if(statusBar)getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if(navi)getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
    }


    public int getColorPrimary(){
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        return typedValue.data;
    }

    public int getColorPrimaryDark(){
        TypedValue typedValue = new  TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
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
