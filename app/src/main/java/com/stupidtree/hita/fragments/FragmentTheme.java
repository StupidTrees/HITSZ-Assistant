package com.stupidtree.hita.fragments;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.Toolbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Button;


import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.stupidtree.hita.R;


import java.util.Objects;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.defaultSP;
import static com.stupidtree.hita.HITAApplication.getThemeID;
import static com.stupidtree.hita.HITAApplication.themeID;

public class FragmentTheme extends BottomSheetDialogFragment {

    private RadioGroup radioGroup;
    private CardView demoCard;
    private CardView demoFab;
    private FloatingActionButton change;
    private LinearLayout demoCardDark;
    private int[] radioButtons;
    private ImageView demoBG;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = View.inflate(getContext(), R.layout.fragment_theme, null);
        dialog.setContentView(view);
        ((View) view.getParent()).setBackgroundColor(Color.TRANSPARENT);
        initViews(view);
        return dialog;
    }


    void initViews(View v) {
        radioGroup = v.findViewById(R.id.radioGroup);
        demoCard = v.findViewById(R.id.demo_card);
        demoCardDark = v.findViewById(R.id.demo_card_dark);
        demoFab = v.findViewById(R.id.demo_fab);
        change = v.findViewById(R.id.change_theme);
        demoBG = v.findViewById(R.id.demo_bg);
        radioButtons = new int[]{R.id.radioButton1, R.id.radioButton2, R.id.radioButton3,
                R.id.radioButton4, R.id.radioButton5, R.id.radioButton6,
                R.id.radioButton7, R.id.radioButton8, R.id.radioButton9,
                R.id.radioButton10, R.id.radioButton11, R.id.radioButton12,
        };

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                changeDemo(checkedId);

            }
        });

        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos;
                for (pos = 0; pos < radioButtons.length; pos++) {
                    if (radioButtons[pos] == radioGroup.getCheckedRadioButtonId()) break;
                }
               // System.out.println("pos=" + pos);
                defaultSP.edit().putInt("theme_id", pos).apply();
                getThemeID();
                Objects.requireNonNull(getActivity()).recreate();
//                Intent mStartActivity = new Intent(HContext, ActivityMain.class);
//                int mPendingIntentId = 2333333;
//                PendingIntent mPendingIntent = PendingIntent.getActivity(getActivity(), mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
//                AlarmManager mgr = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
//                mgr.set(AlarmManager.RTC, System.currentTimeMillis(), mPendingIntent);
//                System.exit(0);
            }
        });
        switch (themeID) {
            case R.style.RedTheme:
                radioGroup.check(R.id.radioButton1);
                break;
            case R.style.PinkTheme:
                radioGroup.check(R.id.radioButton2);
                break;
            case R.style.BrownTheme:
                radioGroup.check(R.id.radioButton3);
                break;
            case R.style.BlueTheme:
                radioGroup.check(R.id.radioButton4);
                break;
            case R.style.BlueGreyTheme:
                radioGroup.check(R.id.radioButton5);
                break;
            case R.style.TealTheme:
                radioGroup.check(R.id.radioButton6);
                break;
            case R.style.DeepPurpleTheme:
                radioGroup.check(R.id.radioButton7);
                break;
            case R.style.GreenTheme:
                radioGroup.check(R.id.radioButton8);
                break;
            case R.style.DeepOrangeTheme:
                radioGroup.check(R.id.radioButton9);
                break;
            case R.style.IndigoTheme:
                radioGroup.check(R.id.radioButton10);
                break;
            case R.style.CyanTheme:
                radioGroup.check(R.id.radioButton11);
                break;
            case R.style.AmberTheme:
                radioGroup.check(R.id.radioButton12);
                break;
        }
        changeDemo(radioGroup.getCheckedRadioButtonId());
    }



    void changeDemo(int checkedId) {
        GradientDrawable aDrawable = null;
        switch (checkedId) {
            case R.id.radioButton1:
                aDrawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{ContextCompat.getColor(HContext, R.color.red_primary), ContextCompat.getColor(HContext, R.color.red_fade)});
                demoFab.setCardBackgroundColor(ContextCompat.getColor(HContext, R.color.red_accent));
               // change.setBackgroundTintList(new ColorStateList(null,new int[]{ContextCompat.getColor(HContext, R.color.red_accent)}));
                demoFab.setCardBackgroundColor(ContextCompat.getColor(HContext, R.color.red_accent));

                break;
            case R.id.radioButton2:
                aDrawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{ContextCompat.getColor(HContext, R.color.pink_primary), ContextCompat.getColor(HContext, R.color.pink_fade)});

                demoFab.setCardBackgroundColor(ContextCompat.getColor(HContext, R.color.pink_accent));
                break;
            case R.id.radioButton3:
                aDrawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{ContextCompat.getColor(HContext, R.color.brown_primary), ContextCompat.getColor(HContext, R.color.brown_fade)});
                demoFab.setCardBackgroundColor(ContextCompat.getColor(HContext, R.color.brown_accent));
                break;
            case R.id.radioButton4:
                aDrawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{ContextCompat.getColor(HContext, R.color.blue_primary), ContextCompat.getColor(HContext, R.color.blue_fade)});
                demoFab.setCardBackgroundColor(ContextCompat.getColor(HContext, R.color.blue_accent));
                break;
            case R.id.radioButton5:
                aDrawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{ContextCompat.getColor(HContext, R.color.blue_grey_primary), ContextCompat.getColor(HContext, R.color.blue_grey_fade)});
                demoFab.setCardBackgroundColor(ContextCompat.getColor(HContext, R.color.blue_grey_accent));
                break;
            case R.id.radioButton6:
                aDrawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{ContextCompat.getColor(HContext, R.color.teal_primary), ContextCompat.getColor(HContext, R.color.teal_fade)});
                demoFab.setCardBackgroundColor(ContextCompat.getColor(HContext, R.color.teal_accent));
                break;
            case R.id.radioButton7:
                aDrawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{ContextCompat.getColor(HContext, R.color.deep_purple_primary), ContextCompat.getColor(HContext, R.color.deep_purple_fade)});
                demoFab.setCardBackgroundColor(ContextCompat.getColor(HContext, R.color.deep_purple_accent));
                break;
            case R.id.radioButton8:
                aDrawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{ContextCompat.getColor(HContext, R.color.green_primary), ContextCompat.getColor(HContext, R.color.green_fade)});
                demoFab.setCardBackgroundColor(ContextCompat.getColor(HContext, R.color.green_accent));
                break;
            case R.id.radioButton9:
                aDrawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{ContextCompat.getColor(HContext, R.color.deep_orange_primary), ContextCompat.getColor(HContext, R.color.deep_orange_fade)});
                demoFab.setCardBackgroundColor(ContextCompat.getColor(HContext, R.color.deep_orange_accent));
                break;
            case R.id.radioButton10:
                aDrawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{ContextCompat.getColor(HContext, R.color.indigo_primary), ContextCompat.getColor(HContext, R.color.indigo_fade)});
                demoFab.setCardBackgroundColor(ContextCompat.getColor(HContext, R.color.indigo_accent));
                break;
            case R.id.radioButton11:
                aDrawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{ContextCompat.getColor(HContext, R.color.cyan_primary), ContextCompat.getColor(HContext, R.color.cyan_fade)});
                demoFab.setCardBackgroundColor(ContextCompat.getColor(HContext, R.color.cyan_accent));
                break;
            case R.id.radioButton12:
                aDrawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{ContextCompat.getColor(HContext, R.color.amber_primary), ContextCompat.getColor(HContext, R.color.amber_fade)});
                demoFab.setCardBackgroundColor(ContextCompat.getColor(HContext, R.color.amber_accent));
                break;

        }
        demoBG.setImageDrawable(aDrawable);
    }
}
