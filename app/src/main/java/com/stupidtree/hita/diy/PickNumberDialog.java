package com.stupidtree.hita.diy;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cncoderx.wheelview.OnWheelChangedListener;
import com.cncoderx.wheelview.Wheel3DView;
import com.cncoderx.wheelview.WheelView;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;
import com.stupidtree.hita.timetable.timetable.HTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.now;
import static com.stupidtree.hita.HITAApplication.themeID;
import static com.stupidtree.hita.HITAApplication.timeTableCore;
import static com.stupidtree.hita.adapter.NewsIpNewsListAdapter.dip2px;

public class PickNumberDialog extends AlertDialog{

    private int number;
    private int max,min;
    BaseActivity context;
    String title;
    private TextView dialogTitle;
    private ImageView done;
    private onDialogConformListener mOnDialogConformListener;
    private Wheel3DView picker;

    private boolean hasInit = false;
    public interface onDialogConformListener{
        void onClick(int number);
    }
    public PickNumberDialog(BaseActivity context,String title, int max,int min,onDialogConformListener onDialogConformListener){
        super(context);
        this.max = max;
        this.title = title;
        this.min = min;
        mOnDialogConformListener = onDialogConformListener;
        this.context = context;
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(context,themeID);// your app theme here
        View view = getLayoutInflater().cloneInContext(contextThemeWrapper).inflate(R.layout.dialog_pick_number,null,false);
        setView(view);
        initViews(view);
    }
    public PickNumberDialog setInitialValue(int number){
        hasInit = true;
        this.number = number;
        return this;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().
                setLayout(dip2px(getContext(), 320), LinearLayout.LayoutParams.WRAP_CONTENT);
        getWindow().
                setBackgroundDrawableResource(R.drawable.dialog_background_radius);
    }

    void initViews(View view){
        done = view.findViewById(R.id.done);
        dialogTitle = view.findViewById(R.id.dialog_title);
        picker = view.findViewById(R.id.number);
        dialogTitle.setText(title);
        List<String> numberText = new ArrayList<>();
        for(int i=min;i<=max;i++){
            numberText.add(i+"");
        }
        picker.setEntries(numberText);

        picker.setOnWheelChangedListener(new OnWheelChangedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onChanged(WheelView view, int oldIndex, int newIndex) {
                number = newIndex+1;
            }
        });
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnDialogConformListener.onClick(number);
                dismiss();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(hasInit){
            picker.setCurrentIndex(number-1);
        }else {
            picker.setCurrentIndex(0);
        }
    }

}
