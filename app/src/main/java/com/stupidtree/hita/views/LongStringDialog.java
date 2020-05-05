package com.stupidtree.hita.views;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.stupidtree.hita.R;

public class LongStringDialog extends RoundedCornerDialog {

    private int stringId;

    public LongStringDialog(Context context, int title, int stringId, int confirmId) {
        super(context);
        this.stringId = stringId;
        setTitle(title);
        @SuppressLint("InflateParams") View v = getLayoutInflater().inflate(R.layout.dialog_long_string, null, false);
        setView(v);
        setButton(Dialog.BUTTON_POSITIVE, context.getText(confirmId), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView content = findViewById(R.id.content);
        content.setText(Html.fromHtml(getContext().getString(stringId)));
    }
}
