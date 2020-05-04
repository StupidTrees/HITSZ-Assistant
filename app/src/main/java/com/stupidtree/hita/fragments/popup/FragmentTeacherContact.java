package com.stupidtree.hita.fragments.popup;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.stupidtree.hita.R;

import java.util.Map;

public class FragmentTeacherContact extends FragmentRadiusPopup {
    private TextView phone, email, address;
    private String phoneS, emailS, addressS;

    public FragmentTeacherContact() {

    }

    public static FragmentTeacherContact newInstance(Map<String, String> contact) {
        Bundle b = new Bundle();
        b.putString("phone", contact.get("phone"));
        b.putString("email", contact.get("email"));
        b.putString("address", contact.get("address"));
        FragmentTeacherContact f = new FragmentTeacherContact();
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle contact = getArguments();
            phoneS = contact.getString("phone");
            emailS = contact.getString("email");
            addressS = contact.getString("address");
        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = View.inflate(getContext(), R.layout.fragment_teacher_contact, null);
        initViews(view);
        setInfos();
        return view;
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
