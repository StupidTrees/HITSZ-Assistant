package com.stupidtree.hita.views;

import android.annotation.SuppressLint;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cncoderx.wheelview.OnWheelChangedListener;
import com.cncoderx.wheelview.WheelView;
import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.BaseActivity;

import java.util.ArrayList;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.themeCore;

public class PickNumberDialog extends RoundedCornerDialog {

    private int number;
    private int max,min;
    BaseActivity context;
    String title;
    private TextView dialogTitle;
    private ImageView done;
    private onDialogConformListener mOnDialogConformListener;
    private mWheel3DView picker;

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
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(context,themeCore.getCurrentThemeID());// your app theme here
        View view = getLayoutInflater().cloneInContext(contextThemeWrapper).inflate(R.layout.dialog_pick_number,null,false);
        setView(view);
        initViews(view);
    }
    public PickNumberDialog setInitialValue(int number){
        hasInit = true;
        this.number = number;
        return this;
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
                number = min + newIndex;
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
            picker.setCurrentIndex(number - min);
        }else {
            picker.setCurrentIndex(0);
        }
    }

}
