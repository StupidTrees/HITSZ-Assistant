package com.stupidtree.hita.fragments;


import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.stupidtree.hita.R;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.defaultSP;

public class FragmentSettings extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.pref_general);
        findPreference("dark_mode_mode").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String mode = (String)newValue;
                preference.setSummary(getDarkModeSummary(mode));
                if(mode.equals("dark_mode_follow")||mode.equals("dark_mode_auto")){
                    Toast.makeText(getContext(),"重启应用生效",Toast.LENGTH_SHORT).show();
                    defaultSP.edit().putBoolean("is_dark_mode",false).apply();
                }
                return true;
            }
        });
        findPreference("dark_mode_mode").setSummary(getDarkModeSummary(defaultSP.getString("dark_mode_mode","dark_mode_normal")));
        findPreference("app_task_enabled").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Toast.makeText(getContext(),"重启应用生效",Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        findPreference("app_events_enabled").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Toast.makeText(getContext(),"重启应用生效",Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        Preference jwts_autologin = findPreference("jwts_autologin");
        final Preference jwts_password = findPreference("jwts_password");
        if(CurrentUser==null){
            jwts_password.setEnabled(false);
            jwts_autologin.setEnabled(false);
            jwts_autologin.setSummary("请先登录绑定学号");
        }else if(TextUtils.isEmpty(CurrentUser.getStudentnumber())){
            jwts_password.setEnabled(false);
            jwts_autologin.setEnabled(false);
            jwts_autologin.setSummary("请先绑定学号");
        }else{
            jwts_password.setEnabled(true);
            jwts_autologin.setEnabled(true);
            jwts_autologin.setSummary("绑定学号:"+ CurrentUser.getStudentnumber());
            jwts_password.setKey(CurrentUser.getStudentnumber()+".password");
            if(TextUtils.isEmpty(defaultSP.getString(CurrentUser.getStudentnumber()+".password",null))){
                jwts_password.setSummary("未设置");
            }else{
                jwts_password.setSummary("已设置");
            }
            jwts_password.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    //defaultSP.edit().putString(CurrentUser.getStudentnumber()+".password", String.valueOf(newValue)).apply();
                    if(TextUtils.isEmpty((CharSequence) newValue)){
                        jwts_password.setSummary("未设置");
                    }else{
                        jwts_password.setSummary("已设置");
                    }
                    return true;
                }
            });
        }
    }

    String getDarkModeSummary(String mode){
        if(mode.equals("dark_mode_normal")) return "手动开关";
        else if(mode.equals("dark_mode_follow")) return "跟随系统";
        else return "按时间自动开关";
     }

}