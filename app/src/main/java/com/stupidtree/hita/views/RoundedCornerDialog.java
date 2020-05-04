package com.stupidtree.hita.views;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.stupidtree.hita.R;

import java.util.Objects;

import static com.stupidtree.hita.adapter.NewsIpNewsListAdapter.dip2px;

public class RoundedCornerDialog extends AlertDialog {
    protected RoundedCornerDialog(Context context) {
        super(context);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getWindow()).
                setLayout(dip2px(getContext(), 320), LinearLayout.LayoutParams.WRAP_CONTENT);
        getWindow().
                setBackgroundDrawableResource(R.drawable.dialog_background_radius);
    }
}
