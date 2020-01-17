package com.stupidtree.hita.fragments;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.R;
import com.stupidtree.hita.online.HITAUser;
import com.stupidtree.hita.diy.ButtonLoading;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.timeTableCore;

public class FragmentLogin extends BaseFragment {
    
    EditText username,password;
    ButtonLoading login;
   // TextInputLayout usernameLayout,passwordLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login,container,false);
        initViews(v);
        return v;
    }
    
    void initViews(View v){
        username = v.findViewById(R.id.username);
        password = v.findViewById(R.id.password);
     //   usernameLayout = v.findViewById(R.id.usernameLayout);
      //  passwordLayout = v.findViewById(R.id.passwordLayout);
        login = v.findViewById(R.id.login);
        login.setEnabled(false);
        login.setAlpha(0.2f);
        username.addTextChangedListener(new mTextWatcher());
        password.addTextChangedListener(new mTextWatcher());
        login.setOnButtonLoadingListener(new ButtonLoading.OnButtonLoadingListener() {
            boolean toContinue;
            @Override
            public void onClick() {
                toContinue = false;
                if(username.getText().toString().isEmpty()) Toast.makeText(getContext(), R.string.enter_username,Toast.LENGTH_SHORT).show();
                else if(password.getText().toString().isEmpty()) Toast.makeText(getContext(), R.string.enter_password,Toast.LENGTH_SHORT).show();
                else{
                    toContinue = true;
                    HITAUser hitau = new HITAUser();
                    hitau.setUsername(username.getText().toString());
                    hitau.setPassword(password.getText().toString());
                    hitau.login(new SaveListener<HITAUser>() {
                        @Override
                        public void onStart() {
                            super.onStart();
                            login.setProgress(true);
                        }

                        @Override
                        public void done(HITAUser hitaUser, BmobException e) {
                            login.setProgress(false);
                            if (e == null) {
                                Toast.makeText(HContext, R.string.login_success_syncing, Toast.LENGTH_SHORT).show();
                                CurrentUser = BmobUser.getCurrentUser(HITAUser.class);
                                timeTableCore.loadDataFromCloud(getActivity());
                            } else {
                                switch (e.getErrorCode()){
                                    case 101:Snackbar.make(login,getString(R.string.username_or_password_wrong),Snackbar.LENGTH_SHORT).show();
                                }
                                Toast.makeText(HContext, R.string.login_failed, Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }
            }

            @Override
            public void onStart() {
              if(!toContinue) login.setProgress(false);
            }

            @Override
            public void onFinish() {

            }
        });
    }

    @Override
    protected void stopTasks() {

    }

    @Override
    public void Refresh() {

    }

    class mTextWatcher implements TextWatcher {


        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(TextUtils.isEmpty(username.getText().toString())||TextUtils.isEmpty(password.getText().toString())){
                login.setEnabled(false);
                login.setAlpha(0.2f);
            }
            else {
                login.setEnabled(true);
                login.setAlpha(1f);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }



}

