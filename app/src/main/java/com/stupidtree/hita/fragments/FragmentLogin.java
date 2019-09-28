package com.stupidtree.hita.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputLayout;

import android.text.Editable;
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
import static com.stupidtree.hita.HITAApplication.loadDataFromCloud;

public class FragmentLogin extends BaseFragment {
    
    EditText username,password;
    ButtonLoading login;
    TextInputLayout usernameLayout,passwordLayout;

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
        usernameLayout = v.findViewById(R.id.usernameLayout);
        passwordLayout = v.findViewById(R.id.passwordLayout);
        login = v.findViewById(R.id.login);
        username.addTextChangedListener(new mTextWatcher(usernameLayout,passwordLayout));
        password.addTextChangedListener(new mTextWatcher(passwordLayout));
        login.setOnButtonLoadingListener(new ButtonLoading.OnButtonLoadingListener() {
            boolean toContinue;
            @Override
            public void onClick() {
                passwordLayout.setError(null);
                usernameLayout.setError(null);
                toContinue = false;
                if(username.getText().toString().isEmpty()) usernameLayout.setError("请输入用户名");
                else if(password.getText().toString().isEmpty()) passwordLayout.setError("请输入密码");
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
                                Toast.makeText(HContext, "登录成功,正在同步用户数据……", Toast.LENGTH_SHORT).show();
                                CurrentUser = BmobUser.getCurrentUser(HITAUser.class);
                                loadDataFromCloud(getActivity());
                            } else {
                                switch (e.getErrorCode()){
                                    case 101: usernameLayout.setError("用户名或密码错误");break;
                                }
                                Toast.makeText(HContext,"登录失败", Toast.LENGTH_SHORT).show();
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


        TextInputLayout which;
        TextInputLayout which2;

        public mTextWatcher(TextInputLayout which) {
            this.which = which;
            which2 = null;
        }
        public mTextWatcher(TextInputLayout which,TextInputLayout which2) {
            this.which = which;
            this.which2 = which2;
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            which.setError(null);
            if(which2!=null) which2.setError(null);
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }



}

