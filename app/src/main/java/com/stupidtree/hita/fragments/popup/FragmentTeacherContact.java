package com.stupidtree.hita.fragments.popup;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.stupidtree.hita.R;

import java.util.HashMap;
import java.util.Map;

public class FragmentTeacherContact extends BottomSheetDialogFragment {
    TextView phone,email,address;
    String phoneS,emailS,addressS;
    public FragmentTeacherContact(Map<String,String> contact){
        phoneS = contact.get("phone");
        emailS = contact.get("email");
        addressS = contact.get("address");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = View.inflate(getContext(), R.layout.fragment_teacher_contact, null);
        dialog.setContentView(view);
        initViews(view);
        setInfos();
        ((View) view.getParent()).setBackgroundColor(Color.TRANSPARENT);
        return dialog;
    }


    private void initViews(View v){
        phone = v.findViewById(R.id.phone);
        email = v.findViewById(R.id.email);
        address = v.findViewById(R.id.address);
    }

    private void setInfos(){
        if(TextUtils.isEmpty(phoneS))phone.setText(R.string.no_teacher_contact_data);
        else phone.setText(phoneS);
        if(TextUtils.isEmpty(emailS))email.setText(R.string.no_teacher_contact_data);
        else email.setText(emailS);
        if(TextUtils.isEmpty(addressS))address.setText(R.string.no_teacher_contact_data);
        else address.setText(addressS);

    }
}
