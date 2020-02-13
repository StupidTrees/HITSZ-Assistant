package com.stupidtree.hita.diy;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.stupidtree.hita.R;

import static com.stupidtree.hita.adapter.NewsIpNewsListAdapter.dip2px;

public class ColorPickerDialog extends Dialog {
    public interface OnColorSelectedListener {
        void OnSelected(int color);
    }

    private int color = Color.CYAN;
    private ImageView confrim;
    private View colorDemo;
    private SeekBar sbR, sbB, sbG;
    private OnColorSelectedListener onColorSelectedListener;

    public ColorPickerDialog(@NonNull Context context) {
        super(context);
    }

    public void show(OnColorSelectedListener os) {
        this.onColorSelectedListener = os;
        show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setLayout(dip2px(getContext(), 320), LinearLayout.LayoutParams.WRAP_CONTENT);
        getWindow().setBackgroundDrawableResource(R.drawable.dialog_background_radius);
        View v = getLayoutInflater().inflate(R.layout.dialog_color_picker, null);
        setContentView(v);
        initViews(v);
    }

    @Override
    protected void onStart() {
        super.onStart();
        syncSeekbarWithColor();
    }

    public ColorPickerDialog initColor(int color) {
        this.color = color;
        return this;
    }

    private void syncSeekbarWithColor() {
        int red = (color & 0xff0000) >> 16;
        int green = (color & 0x00ff00) >> 8;
        int blue = (color & 0x0000ff);
        sbR.setProgress(red);
        sbG.setProgress(green);
        sbB.setProgress(blue);
    }

    private void syncDemoWithColor() {
        colorDemo.setBackgroundColor(color);
    }

    private void initViews(View v) {
        confrim = v.findViewById(R.id.done);
        sbB = v.findViewById(R.id.sb_b);
        sbG = v.findViewById(R.id.sb_g);
        sbR = v.findViewById(R.id.sb_r);
        colorDemo = v.findViewById(R.id.color_demo);
        sbR.setMax(255);
        sbG.setMax(255);
        sbB.setMax(255);
        SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                color = Color.rgb(sbR.getProgress(), sbG.getProgress(), sbB.getProgress());
                syncDemoWithColor();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };
        sbG.setOnSeekBarChangeListener(seekBarChangeListener);
        sbR.setOnSeekBarChangeListener(seekBarChangeListener);
        sbB.setOnSeekBarChangeListener(seekBarChangeListener);

        confrim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onColorSelectedListener.OnSelected(color);
                dismiss();
            }
        });
    }
}
