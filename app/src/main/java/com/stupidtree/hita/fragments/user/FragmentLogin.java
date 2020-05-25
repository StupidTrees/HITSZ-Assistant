package com.stupidtree.hita.fragments.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.snackbar.Snackbar;
import com.stupidtree.hita.R;
import com.stupidtree.hita.fragments.BaseFragment;
import com.stupidtree.hita.online.HITAUser;
import com.stupidtree.hita.timetable.TimetableCore;
import com.stupidtree.hita.views.ButtonLoading;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.timetable.TimeWatcherService.USER_CHANGED;

public class FragmentLogin extends BaseFragment {
    
    private EditText username,password;
    private ButtonLoading login;
   // TextInputLayout usernameLayout,passwordLayout;


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_login;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
    }

    private void initViews(View v) {
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
                if (username.getText().toString().isEmpty())
                    Toast.makeText(requireContext(), R.string.enter_username, Toast.LENGTH_SHORT).show();
                else if (password.getText().toString().isEmpty())
                    Toast.makeText(requireContext(), R.string.enter_password, Toast.LENGTH_SHORT).show();
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
                                Intent i = new Intent(USER_CHANGED);
                                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(i);
                                CurrentUser = BmobUser.getCurrentUser(HITAUser.class);
                                TimetableCore.getInstance(HContext).loadDataFromCloud(getActivity());
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

