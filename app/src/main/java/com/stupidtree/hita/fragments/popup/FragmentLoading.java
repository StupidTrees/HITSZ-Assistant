package com.stupidtree.hita.fragments.popup;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.stupidtree.hita.R;

@SuppressLint("ValidFragment")
public class FragmentLoading extends FragmentRadiusPopup {


    private TextView title;
    private TextView subtitle;
    private String titleS;

    FragmentLoading() {

    }

    public static FragmentLoading newInstance(String title) {
        Bundle b = new Bundle();
        b.putString("title", title);
        FragmentLoading f = new FragmentLoading();
        f.setArguments(b);
        return f;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = View.inflate(getContext(), R.layout.fragment_loading, null);
        initViews(view);
        return view;
    }


    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            titleS = getArguments().getString("title", "Loading");
        }
        setCancelable(false);
    }

    public void updateSubtitle(String text) {
        if (subtitle != null) subtitle.setText(text);
    }

    void initViews(View v) {
        title = v.findViewById(R.id.title);
        subtitle = v.findViewById(R.id.subtitle);
        title.setText(titleS);
        subtitle.setText("");
    }


}
