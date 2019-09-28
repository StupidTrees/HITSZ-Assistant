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

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.clearData;

public class FragmentSignup extends BaseFragment {
    
    EditText username,password,confirm_password,nickname;
    ButtonLoading signup;
    TextInputLayout usernameLayout,passwordLayout,confirmPasswordLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_signup,container,false);
        initViews(v);
        return v;
    }
    
    void initViews(View v){
        username = v.findViewById(R.id.username);
        password = v.findViewById(R.id.password);
        confirm_password = v.findViewById(R.id.password_confirm);
        usernameLayout = v.findViewById(R.id.usernameLayout);
        passwordLayout = v.findViewById(R.id.passwordLayout);
        confirmPasswordLayout = v.findViewById(R.id.confirmPasswordLayout);
        signup = v.findViewById(R.id.signup);
        nickname = v.findViewById(R.id.nickname);
        username.addTextChangedListener(new mTextWatcher(usernameLayout));
        password.addTextChangedListener(new mTextWatcher(passwordLayout));
        confirm_password.addTextChangedListener(new mTextWatcher(confirmPasswordLayout));

        signup.setOnButtonLoadingListener(new ButtonLoading.OnButtonLoadingListener() {
            boolean toContinue;
            @Override
            public void onClick() {
                confirmPasswordLayout.setError(null);
                passwordLayout.setError(null);
                confirmPasswordLayout.setError(null);
                toContinue = false;
                if(username.getText().toString().isEmpty()) usernameLayout.setError("请输入用户名");
                else if(password.getText().toString().isEmpty()) passwordLayout.setError("请输入密码");
                else if (confirm_password.getText().toString().isEmpty()) confirmPasswordLayout.setError("请再次输入密码");
                else if(!password.getText().toString().equals(confirm_password.getText().toString())) confirmPasswordLayout.setError("两次输入的密码不一致！");
                else{
                    toContinue = true;
                    HITAUser hitau = new HITAUser();
                    hitau.setUsername(username.getText().toString());
                    hitau.setPassword(password.getText().toString());
                    hitau.setNick(nickname.getText().toString());
                    hitau.signUp(new SaveListener<HITAUser>() {
                        @Override
                        public void onStart() {
                            super.onStart();
                            signup.setProgress(true);
                        }

                        @Override
                        public void done(HITAUser hitaUser, BmobException e) {
                            signup.setProgress(false);
                            if (e == null) {
                                Toast.makeText(HContext, "注册成功！", Toast.LENGTH_SHORT).show();
                                hitaUser.login(new SaveListener<HITAUser>() {
                                    @Override
                                    public void done(HITAUser hitaUser, BmobException e) {
                                        CurrentUser = BmobUser.getCurrentUser(HITAUser.class);
                                        clearData();
                                        getActivity().finish();
                                    }
                                });
                            } else {
                                switch (e.getErrorCode()){
                                    case 202:
                                        usernameLayout.setError("该用户名已被占用");break;
                                }
                                Toast.makeText(HContext,"注册失败", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }
            }

            @Override
            public void onStart() {
              if(!toContinue) signup.setProgress(false);
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

        public mTextWatcher(TextInputLayout which) {
            this.which = which;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
          which.setError(null);
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }

}

