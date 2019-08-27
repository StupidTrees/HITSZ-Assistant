package com.stupidtree.hita.fragments;


import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.stupidtree.hita.R;

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
    }

    String getDarkModeSummary(String mode){
        if(mode.equals("dark_mode_normal")) return "手动开关";
        else if(mode.equals("dark_mode_follow")) return "跟随系统";
        else return "按时间自动开关";
     }

}