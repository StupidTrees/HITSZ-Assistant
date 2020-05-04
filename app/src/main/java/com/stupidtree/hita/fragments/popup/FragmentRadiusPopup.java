package com.stupidtree.hita.fragments.popup;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.stupidtree.hita.R;

public class FragmentRadiusPopup extends BottomSheetDialogFragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.RadiusCornerBottomSheetDialogTheme);
    }
}
