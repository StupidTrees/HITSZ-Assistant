package com.stupidtree.hita;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDelegate;

import com.stupidtree.hita.activities.ActivityChatbot;
import com.stupidtree.hita.activities.ActivitySearch;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.defaultSP;
import static com.stupidtree.hita.HITAApplication.themeCore;

public class AppThemeCore {
    private int themeID;
    private ThemeItem currentTheme;
    boolean darkModeOn = false;
    String darkModeMode;
    private List<ThemeItem> allThemeList;

    AppThemeCore() {
        allThemeList = Arrays.asList(
                new ThemeItem(R.string.theme_name_amber, R.style.AmberTheme, R.color.amber_primary, R.color.amber_accent, R.color.amber_fade, R.style.AmberTheme_translucent),
               new ThemeItem(R.string.theme_name_hit, R.style.HITTheme, R.color.hit_primary, R.color.hit_accent, R.color.hit_fade, R.style.HITTheme_translucent),
                new ThemeItem(R.string.theme_name_red, R.style.RedTheme, R.color.red_primary, R.color.red_accent, R.color.red_fade, R.style.RedTheme_translucent),
                new ThemeItem(R.string.theme_name_blue, R.style.BlueTheme, R.color.blue_primary, R.color.blue_accent, R.color.blue_fade, R.style.BlueTheme_translucent),
                new ThemeItem(R.string.theme_name_deep_orange, R.style.DeepOrangeTheme, R.color.deep_orange_primary, R.color.deep_orange_accent, R.color.deep_orange_fade, R.style.DeepOrangeTheme_translucent),
                new ThemeItem(R.string.theme_name_cyan, R.style.CyanTheme, R.color.cyan_primary, R.color.cyan_accent, R.color.cyan_fade, R.style.CyanTheme_translucent),
                new ThemeItem(R.string.theme_name_pink, R.style.PinkTheme, R.color.pink_primary, R.color.pink_accent, R.color.pink_fade, R.style.PinkTheme_translucent),
                new ThemeItem(R.string.theme_name_green, R.style.GreenTheme, R.color.green_primary, R.color.green_accent, R.color.green_fade, R.style.GreenTheme_translucent),
                new ThemeItem(R.string.theme_name_android, R.style.TealTheme, R.color.teal_primary, R.color.teal_accent, R.color.teal_fade, R.style.TealTheme_translucent),
                new ThemeItem(R.string.theme_name_purple, R.style.DeepPurpleTheme, R.color.deep_purple_primary, R.color.deep_purple_accent, R.color.deep_purple_fade, R.style.DeepPurpleTheme_translucent),
                new ThemeItem(R.string.theme_name_brown, R.style.BrownTheme, R.color.brown_primary, R.color.brown_accent, R.color.brown_fade, R.style.BrownTheme_translucent),
                new ThemeItem(R.string.theme_name_grey, R.style.BlueGreyTheme, R.color.blue_grey_primary, R.color.blue_grey_accent, R.color.blue_grey_fade, R.style.BlueGreyTheme_translucent),
                new ThemeItem(R.string.theme_name_cruelsummer, R.style.CruelSummerTheme, R.color.cruelsummer_primary, R.color.cruelsummer_accent, R.color.cruelsummer_fade, R.style.CruelSummerTheme_translucent),
                new ThemeItem(R.string.theme_name_indigo, R.style.IndigoTheme, R.color.indigo_primary, R.color.indigo_accent, R.color.indigo_fade, R.style.IndigoTheme_translucent),
                new ThemeItem(R.string.theme_name_mi, R.style.MITheme, R.color.mi_primary, R.color.mi_accent, R.color.mi_fade, R.style.MITheme_translucent),
                new ThemeItem(R.string.theme_name_p30, R.style.P30Theme, R.color.p30_primary, R.color.p30_accent, R.color.p30_fade, R.style.P30Theme_translucent),
                new ThemeItem(R.string.theme_name_rose, R.style.RoseTheme, R.color.rose_primary, R.color.rose_accent, R.color.rose_fade, R.style.RoseTheme_translucent),
                new ThemeItem(R.string.theme_name_vice, R.style.ViceTheme, R.color.vice_primary, R.color.vice_accent, R.color.vice_fade, R.style.ViceTheme_translucent),
                new ThemeItem(R.string.theme_name_mild, R.style.MildTheme, R.color.mild_primary, R.color.mild_accent, R.color.mild_fade, R.style.MildTheme_translucent),
                new ThemeItem(R.string.theme_name_morning, R.style.MorningTheme, R.color.morning_primary, R.color.morning_accent, R.color.morning_fade, R.style.MorningTheme_translucent),
                new ThemeItem(R.string.theme_name_ph, R.style.PHTheme, R.color.ph_primary, R.color.ph_accent, R.color.ph_fade, R.style.PHTheme_translucent),
                new ThemeItem(R.string.theme_name_cyberpunk, R.style.CyberPunkTheme, R.color.cyberpunk_primary, R.color.cyberpunk_accent, R.color.cyberpunk_fade, R.style.CyberPunkTheme_translucent),
                new ThemeItem(R.string.theme_name_diechilan, R.style.DiechilanTheme, R.color.magic_primary, R.color.magic_accent, R.color.magic_fade, R.style.DiechilanTheme_translucent),
                new ThemeItem(R.string.theme_name_tan, R.style.TanTheme, R.color.tan_primary, R.color.tan_accent, R.color.tan_fade, R.style.TanTheme_translucent),
                new ThemeItem(R.string.theme_name_yadinglv, R.style.yadinglvTheme, R.color.yadinglv_primary, R.color.yadinglv_accent, R.color.yadinglv_fade, R.style.yadinglvTheme_translucent),
                new ThemeItem(R.string.theme_name_manjianghong, R.style.ManjiangHongTheme, R.color.manjianghong_primary, R.color.manjianghong_accent, R.color.manjianghong_fade, R.style.ManjianghongTheme_translucent)

        );
        darkModeOn = defaultSP.getBoolean("is_dark_mode", false);
    }

    public ThemeItem getCurrentTheme() {
        return currentTheme;
    }

    public boolean isDarkModeOn() {
        return darkModeOn;
    }

    public void switchDarkMode(BaseActivity from, boolean isChecked) {
        darkModeOn = isChecked;
        defaultSP.edit().putBoolean("is_dark_mode", isChecked).apply();
        if (isChecked)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        if (from != null) from.recreate();
    }

    public List<ThemeItem> getAllThemeList() {
        return allThemeList;
    }

    public class ThemeItem {
        String name;
        int themeId;
        int themeTranslucentID;
        int colorPrimary;
        int colorAccent;
        int colorFade;

        public ThemeItem(int nameId, int themeId, int colorPrimary, int colorAccent, int colorFade, int themeTranslucentID) {
            this.name = HContext.getString(nameId);
            this.themeTranslucentID = themeTranslucentID;
            this.themeId = themeId;
            this.colorPrimary = colorPrimary;
            this.colorAccent = colorAccent;
            this.colorFade = colorFade;
        }

        public String getName() {
            return name;
        }

        public int getThemeId() {
            return themeId;
        }

        public int getColorPrimary() {
            return colorPrimary;
        }

        public int getColorAccent() {
            return colorAccent;
        }

        public int getColorFade() {
            return colorFade;
        }
    }

    public boolean isDarkModeFollowingSystem() {
        return !darkModeMode.equals("dark_mode_normal");
    }

    public int getCurrentThemeID() {
        return themeID;
    }


    public void initAppTheme() {
        darkModeOn = defaultSP.getBoolean("is_dark_mode", false);
        darkModeMode = defaultSP.getString("dark_mode_mode", "dark_mode_normal");
        currentTheme = allThemeList.get(defaultSP.getInt("theme_index", 12));
        themeID = currentTheme.getThemeId();
        if (CurrentUser != null) {
            CurrentUser.setUsingTheme(currentTheme.getName());
            CurrentUser.update(new UpdateListener() {
                @Override
                public void done(BmobException e) {

                }
            });
        }
        if (themeID == R.style.PHTheme || themeID == R.style.CyberPunkTheme) {
            defaultSP.edit().putBoolean("is_dark_mode", true).apply();
            darkModeOn = true;
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            return;
        }
        if (darkModeMode.equals("dark_mode_normal")) {
            if (darkModeOn) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }

    }

    public void changeTheme(Activity activity, int chosenIndex) {
        darkModeOn = defaultSP.getBoolean("is_dark_mode", false);
        darkModeMode = defaultSP.getString("dark_mode_mode", "dark_mode_normal");
        defaultSP.edit().putInt("theme_index", chosenIndex).apply();
        currentTheme = getAllThemeList().get(chosenIndex);
        int oldTheme = themeID;
        themeID = currentTheme.getThemeId();
        if (CurrentUser != null) {
            CurrentUser.setUsingTheme(currentTheme.getName());
            CurrentUser.update(new UpdateListener() {
                @Override
                public void done(BmobException e) {

                }
            });
        }
        if (themeID == R.style.PHTheme || themeID == R.style.CyberPunkTheme) {
            darkModeOn = true;
            defaultSP.edit().putBoolean("is_dark_mode", true).apply();
            //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else if (oldTheme == R.style.PHTheme || oldTheme == R.style.CyberPunkTheme) {
            darkModeOn = false;
            defaultSP.edit().putBoolean("is_dark_mode", false).apply();
            if (darkModeMode.equals("dark_mode_normal"))
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        } else {
            if (darkModeMode.equals("dark_mode_normal")) {
                if (darkModeOn)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            }
        }

        activity.recreate();

    }

    public void applyThemeWhenActivityCreates(BaseActivity activity) {
        if (themeID == R.style.PHTheme || themeID == R.style.CyberPunkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        if (activity instanceof ActivityChatbot || activity instanceof ActivitySearch) {
            activity.setTheme(getCurrentTheme().themeTranslucentID);
        } else {
            activity.setTheme(getCurrentThemeID());
        }
//        if(themeID==R.style.PHTheme){
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//        }

    }

}
