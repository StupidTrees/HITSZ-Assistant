package com.stupidtree.hita.fragments.pref;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.DropDownPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreference;

import com.stupidtree.hita.R;
import com.stupidtree.hita.diy.TimeTablePreferenceChangeListener;

import java.util.Objects;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.defaultSP;
import static com.stupidtree.hita.activities.ActivityMain.MAIN_RECREATE;
import static com.stupidtree.hita.timetable.TimeWatcherService.NOTIFICATION_OFF;
import static com.stupidtree.hita.timetable.TimeWatcherService.NOTIFICATION_ON;
import static com.stupidtree.hita.timetable.TimeWatcherService.TIMETABLE_CHANGED;
import static com.stupidtree.hita.timetable.TimeWatcherService.WATCHER_REFRESH;

public class FragmentSettings extends PreferenceFragmentCompat {

    int prefId;
    String title;

    public FragmentSettings() {

    }

    public FragmentSettings(int prefId, String title) {
        this.prefId = prefId;
        this.title = title;
    }

    public String getTitle() {
        return title;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        getListView().setOverScrollMode(View.OVER_SCROLL_NEVER);
        return v;
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(prefId);
        switch (prefId) {
            case R.xml.pref_basic:
                setAutoLoginPreference();
                setBasicPreference();
                break;
            case R.xml.pref_appearence:
                setDarkModePreference();
                setTimetablePreference();
                break;
            case R.xml.pref_others:
                setAppPreference();
                break;


        }

    }

    private void setBasicPreference() {
        findPreference("app_task_enabled").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Intent i = new Intent(MAIN_RECREATE);
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(i);
                //Toast.makeText(getContext(),getString(R.string.notif_effect_after_restart),Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        findPreference("app_events_enabled").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Intent i = new Intent(MAIN_RECREATE);
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(i);
                // Toast.makeText(getContext(),getString(R.string.notif_effect_after_restart),Toast.LENGTH_SHORT).show();
                return true;
            }
        });

    }


    private void setAppPreference() {

        findPreference("notification").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Intent i = new Intent(WATCHER_REFRESH);
                int flag = (boolean) newValue ? NOTIFICATION_ON : NOTIFICATION_OFF;
                i.putExtra("switch_notification", flag);
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(i);
                return true;
            }
        });
    }

    private void setAutoLoginPreference() {
        Preference jwts_autologin = findPreference("jwts_autologin");
        final Preference jwts_password = findPreference("jwts_password");
        if (CurrentUser == null) {
            jwts_password.setEnabled(false);
            jwts_autologin.setEnabled(false);
            jwts_autologin.setSummary(R.string.settings_noti_loginfirst);
        } else if (TextUtils.isEmpty(CurrentUser.getStudentnumber())) {
            jwts_password.setEnabled(false);
            jwts_autologin.setEnabled(false);
            jwts_autologin.setSummary(R.string.settings_noti_bindfirst);
        } else {
            jwts_password.setEnabled(true);
            jwts_autologin.setEnabled(true);
            jwts_autologin.setSummary(String.format(getString(R.string.settings_noti_bound_to), CurrentUser.getStudentnumber() + ""));
            jwts_password.setKey(CurrentUser.getStudentnumber() + ".password");
            String pswd = defaultSP.getString(CurrentUser.getStudentnumber() + ".password", null);
            ((EditTextPreference) jwts_password).setText(pswd);
            if (TextUtils.isEmpty(pswd)) {
                jwts_password.setSummary(R.string.settings_not_set_yet);
            } else {
                jwts_password.setSummary(R.string.settings_already_set);
            }
            jwts_password.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    //defaultSP.edit().putString(CurrentUser.getStudentnumber()+".password", String.valueOf(newValue)).apply();
                    if (TextUtils.isEmpty((CharSequence) newValue)) {
                        jwts_password.setSummary(R.string.settings_not_set_yet);
                    } else jwts_password.setSummary(R.string.settings_already_set);
                    return true;
                }
            });
        }
    }

    private void setDarkModePreference() {
        final ListPreference lp = findPreference("dark_mode_mode");
        lp.setValue(defaultSP.getString("dark_mode_mode", "dark_mode_normal"));
        lp.setSummaryProvider(new Preference.SummaryProvider() {
            @Override
            public CharSequence provideSummary(Preference preference) {
                return lp.getEntry();
            }
        });
        lp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @SuppressLint("ApplySharedPref")
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String mode = (String) newValue;
                if (mode.equals("dark_mode_follow") || mode.equals("dark_mode_auto")) {
                    // Toast.makeText(getContext(),getString(R.string.notif_effect_after_restart),Toast.LENGTH_SHORT).show();
                    //defaultSP.edit().putBoolean("is_dark_mode",false).commit();
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                } else {
                    boolean isD = defaultSP.getBoolean("is_dark_mode", false);
                    if (isD)
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
//                else{
//                   // defaultSP.edit().putBoolean("is_dark_mode",false).commit();
//
//                }
                Intent i = new Intent(MAIN_RECREATE);
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(i);
                getActivity().recreate();
                return true;
            }
        });

    }

    private void setTimetablePreference() {
        findPreference("timetable_card_opacity")
                .setOnPreferenceChangeListener(new TimeTablePreferenceChangeListener(getContext()));
        findPreference("timetable_card_text_bold")
                .setOnPreferenceChangeListener(new TimeTablePreferenceChangeListener(getContext()));
        findPreference("timetable_card_title_alpha")
                .setOnPreferenceChangeListener(new TimeTablePreferenceChangeListener(getContext()));
        findPreference("timetable_card_subtitle_alpha")
                .setOnPreferenceChangeListener(new TimeTablePreferenceChangeListener(getContext()));

        final SeekBarPreference sbp = findPreference("timetable_card_height");
        sbp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                sbp.setValue(160);
                sbp.callChangeListener(160);
                return true;
            }
        });
        sbp.setOnPreferenceChangeListener(new TimeTablePreferenceChangeListener(getContext()));

        final ListPreference dropDownPreference = findPreference("timetable_card_title_gravity");
        String vl = defaultSP.getString("timetable_card_title_gravity", "top");
        dropDownPreference.setValue(vl);
        dropDownPreference.setSummaryProvider(new Preference.SummaryProvider() {
            @Override
            public CharSequence provideSummary(Preference preference) {
                return dropDownPreference.getEntry();
            }
        });
        dropDownPreference.setOnPreferenceChangeListener(new TimeTablePreferenceChangeListener(getContext()));


        final ListPreference cdBg = findPreference("timetable_card_background");
        String bgV = defaultSP.getString("timetable_card_background", "gradient");
        cdBg.setValue(bgV);
        cdBg.setOnPreferenceChangeListener(new TimeTablePreferenceChangeListener(getContext()));
        cdBg.setSummaryProvider(new Preference.SummaryProvider() {
            @Override
            public CharSequence provideSummary(Preference preference) {
                return cdBg.getEntry();
            }
        });

        final ListPreference ttColor = findPreference("timetable_card_title_color");
        String clV = defaultSP.getString("timetable_card_title_color", "white");
        ttColor.setValue(clV);
        ttColor.setOnPreferenceChangeListener(new TimeTablePreferenceChangeListener(getContext()));
        ttColor.setSummaryProvider(new Preference.SummaryProvider() {
            @Override
            public CharSequence provideSummary(Preference preference) {
                return ttColor.getEntry();
            }
        });

        final ListPreference sbttColor = findPreference("timetable_card_subtitle_color");
        String sbclV = defaultSP.getString("timetable_card_title_color", "white");
        sbttColor.setValue(sbclV);
        sbttColor.setOnPreferenceChangeListener(new TimeTablePreferenceChangeListener(getContext()));
        sbttColor.setSummaryProvider(new Preference.SummaryProvider() {
            @Override
            public CharSequence provideSummary(Preference preference) {
                return sbttColor.getEntry();
            }
        });

        final ListPreference iconColor = findPreference("timetable_card_icon_color");
        String iconV = defaultSP.getString("timetable_card_icon_color", "white");
        iconColor.setValue(iconV);
        iconColor.setOnPreferenceChangeListener(new TimeTablePreferenceChangeListener(getContext()));
        iconColor.setSummaryProvider(new Preference.SummaryProvider() {
            @Override
            public CharSequence provideSummary(Preference preference) {
                return iconColor.getEntry();
            }
        });

        final SwitchPreference enableColor = findPreference("subjects_color_enable");
        enableColor.setOnPreferenceChangeListener(new TimeTablePreferenceChangeListener(getContext(), new TimeTablePreferenceChangeListener.ChangeAction() {
            @Override
            public boolean OnChanged(Preference preference, Object newValue) {
                boolean val = (boolean) newValue;
                cdBg.setEnabled(!val);
                if (!val) {
                    boolean ttChange = ttColor.getValue().equals("subject");
                    boolean sbttChange = sbttColor.getValue().equals("subject");
                    boolean iconChange = iconColor.getValue().equals("subject");

                    ttColor.setEntries(R.array.preference_card_title_color_subject_disabled);
                    ttColor.setEntryValues(R.array.preference_card_title_color_values_subject_disabled);
                    sbttColor.setEntries(R.array.preference_card_title_color_subject_disabled);
                    sbttColor.setEntryValues(R.array.preference_card_title_color_values_subject_disabled);
                    iconColor.setEntries(R.array.preference_card_title_color_subject_disabled);
                    iconColor.setEntryValues(R.array.preference_card_title_color_values_subject_disabled);

                    if (ttChange) {
                        ttColor.setValue("white");
                    }
                    if (sbttChange) {
                        sbttColor.setValue("white");
                    }
                    if (iconChange) {
                        iconColor.setValue("white");
                    }
                } else {
                    ttColor.setEntries(R.array.preference_card_title_color);
                    ttColor.setEntryValues(R.array.preference_card_title_color_values);
                    sbttColor.setEntries(R.array.preference_card_title_color);
                    sbttColor.setEntryValues(R.array.preference_card_title_color_values);
                    iconColor.setEntries(R.array.preference_card_title_color);
                    iconColor.setEntryValues(R.array.preference_card_title_color_values);
                }
                return true;
            }
        }));
        enableColor.callChangeListener(enableColor.isChecked());


        final SwitchPreference iconEnable = findPreference("timetable_card_icon_enable");
        iconEnable.setOnPreferenceChangeListener(new TimeTablePreferenceChangeListener(getContext(), new TimeTablePreferenceChangeListener.ChangeAction() {
            @Override
            public boolean OnChanged(Preference preference, Object newValue) {
                boolean val = (boolean) newValue;
                iconColor.setEnabled(val);
                return true;
            }
        }));
        iconEnable.callChangeListener(iconEnable.isChecked());

        final ListPreference preset = findPreference("timetable_preset");
        preset.setOnPreferenceChangeListener(new TimeTablePreferenceChangeListener(getContext(), new TimeTablePreferenceChangeListener.ChangeAction() {
            @Override
            public boolean OnChanged(Preference preference, Object newValue) {
                String value = (String) newValue;
                if (value.equals("default")) {
                    enableColor.setChecked(false);
                    enableColor.callChangeListener(false);
                    sbp.setValue(160);
                    dropDownPreference.setValue("top");
                    cdBg.setValue("gradient");
                    ttColor.setValue("white");
                    sbttColor.setValue("white");
                    iconColor.setValue("white");
                    iconEnable.setChecked(true);
                    ((SeekBarPreference) findPreference("timetable_card_opacity"))
                            .setValue(100);
                    ((SwitchPreference) findPreference("timetable_card_text_bold"))
                            .setChecked(false);
                    ((SeekBarPreference) findPreference("timetable_card_title_alpha"))
                            .setValue(100);
                    ((SeekBarPreference) findPreference("timetable_card_subtitle_alpha"))
                            .setValue(100);
                } else if (value.equals("theme")) {
                    enableColor.setChecked(false);
                    enableColor.callChangeListener(false);
                    sbp.setValue(160);
                    dropDownPreference.setValue("center");
                    cdBg.setValue("accent");
                    ttColor.setValue("accent");
                    sbttColor.setValue("accent");
                    iconColor.setValue("accent");
                    iconEnable.setChecked(false);
                    ((SeekBarPreference) findPreference("timetable_card_opacity"))
                            .setValue(20);
                    ((SwitchPreference) findPreference("timetable_card_text_bold"))
                            .setChecked(true);
                    ((SeekBarPreference) findPreference("timetable_card_title_alpha"))
                            .setValue(100);
                    ((SeekBarPreference) findPreference("timetable_card_subtitle_alpha"))
                            .setValue(100);
                } else if (value.equals("subject-normal")) {
                    enableColor.setChecked(true);
                    enableColor.callChangeListener(true);
                    sbp.setValue(160);
                    dropDownPreference.setValue("top");
                    cdBg.setValue("gradient");
                    ttColor.setValue("white");
                    sbttColor.setValue("white");
                    iconColor.setValue("white");
                    iconEnable.setChecked(true);
                    ((SeekBarPreference) findPreference("timetable_card_opacity"))
                            .setValue(80);
                    ((SwitchPreference) findPreference("timetable_card_text_bold"))
                            .setChecked(false);
                    ((SeekBarPreference) findPreference("timetable_card_title_alpha"))
                            .setValue(100);
                    ((SeekBarPreference) findPreference("timetable_card_subtitle_alpha"))
                            .setValue(90);
                }else if(value.equals("subject")){
                    enableColor.setChecked(true);
                    enableColor.callChangeListener(true);
                    sbp.setValue(160);
                    dropDownPreference.setValue("center");
                    cdBg.setValue("subject");
                    ttColor.setValue("subject");
                    sbttColor.setValue("subject");
                    iconColor.setValue("subject");
                    iconEnable.setChecked(true);
                    ((SeekBarPreference) findPreference("timetable_card_opacity"))
                            .setValue(20);
                    ((SwitchPreference) findPreference("timetable_card_text_bold"))
                            .setChecked(true);
                    ((SeekBarPreference) findPreference("timetable_card_title_alpha"))
                            .setValue(100);
                    ((SeekBarPreference) findPreference("timetable_card_subtitle_alpha"))
                            .setValue(100);
                }
                return true;
            }
        }));
    }


}