package com.stupidtree.hita.fragments;


import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;

import com.stupidtree.hita.R;

public class FragmentSettings extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.pref_general);
    }
}