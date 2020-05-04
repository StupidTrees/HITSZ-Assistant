package com.stupidtree.hita.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;

import com.cncoderx.wheelview.OnWheelChangedListener;
import com.cncoderx.wheelview.Wheel3DView;
import com.cncoderx.wheelview.WheelView;

/*加上震动反馈*/
public class mWheel3DView extends Wheel3DView {
    public mWheel3DView(Context context) {
        super(context);
    }

    public mWheel3DView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setOnWheelChangedListener(final OnWheelChangedListener onWheelChangedListener) {
        OnWheelChangedListener lis = new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView view, int oldIndex, int newIndex) {
                onWheelChangedListener.onChanged(view, oldIndex, newIndex);
                if (oldIndex != newIndex)
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
            }
        };
        super.setOnWheelChangedListener(lis);
    }
}
