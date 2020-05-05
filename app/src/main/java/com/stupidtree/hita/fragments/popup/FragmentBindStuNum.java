package com.stupidtree.hita.fragments.popup;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.fragments.BaseOperationTask;
import com.stupidtree.hita.jw.JWException;
import com.stupidtree.hita.online.HITAUser;
import com.stupidtree.hita.online.UserData;
import com.stupidtree.hita.views.ButtonLoading;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.jwCore;
import static com.stupidtree.hita.HITAApplication.timeTableCore;

public class FragmentBindStuNum extends FragmentRadiusPopup implements BaseOperationTask.OperationListener<Object> {

    private EditText username, password;
    private ButtonLoading login;
    private bindStuNumTask pageTask_login;
    private BindStuNumCallBackListener mBindStuNumCallBackListener;

    @Override
    public void onOperationStart(String id, Boolean[] params) {
        login.setProgress(true);
    }

    @Override
    public void onOperationDone(String id, BaseOperationTask task, Boolean[] params, Object s) {
        login.setProgress(false);
        bindStuNumTask t = (bindStuNumTask) task;
        final Map<String, String> userInfo = t.userInfo;
        final String username = t.username;
        final String password = t.password;
        boolean toast = params[0];
        if (s instanceof JWException) {
            if (toast) {
                AlertDialog ad = new AlertDialog.Builder(requireContext())
                        .setTitle(getString(R.string.attention))
                        .setMessage(getString(R.string.dialog_message_bind_failed) + ":" + ((JWException) s).getDialogMessage())
                        .setPositiveButton(getString(R.string.button_try_again), null).create();
                ad.show();
            }
        } else if (s instanceof Boolean) {
            if ((boolean) s) {
                BmobQuery<HITAUser> bq = new BmobQuery<>();
                bq.addWhereEqualTo("studentnumber", username);
                bq.findObjects(new FindListener<HITAUser>() {
                    @Override
                    public void done(final List<HITAUser> list, BmobException e) {
                        if (list != null && list.size() > 0) {
                            List<HITAUser> toR = new ArrayList<>();
                            for (HITAUser h : list)
                                if (h.getObjectId().equals(CurrentUser.getObjectId())) toR.add(h);
                            list.removeAll(toR);
                        }
                        if (e == null && list != null && list.size() > 0) {
                            final boolean[] options = {true, true};
                            AlertDialog ad = new AlertDialog.Builder(requireContext()).setTitle(
                                    String.format(getString(R.string.dialog_title_already_bound_to), list.get(0).getUsername(), list.get(0).getNick()))
                                    .setMultiChoiceItems(new String[]{getString(R.string.dialog_item_migrate_data)}, new boolean[]{true}, new DialogInterface.OnMultiChoiceClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                                            options[i] = b;
                                        }
                                    }).setPositiveButton(R.string.button_confirm, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (userInfo != null) getUserInfo(userInfo);
                                            if (options[0]) {
                                                BmobQuery<UserData.UserDataCloud> bq = new BmobQuery<>();
                                                bq.addWhereEqualTo("user", list.get(0).getObjectId());
                                                bq.findObjects(new FindListener<UserData.UserDataCloud>() {
                                                    @Override
                                                    public void done(List<UserData.UserDataCloud> list2, BmobException e) {
                                                        if (e == null && list2 != null && list2.size() > 0) {
                                                            // Log.e("found!", String.valueOf(list2));
                                                            // System.out.println(list2.get(0).getHitaUser());
                                                            UserData.UserDataCloud bud = list2.get(0);
                                                            timeTableCore.loadDataFromCloud(bud);
                                                            UserData.UserDataCloud newUD = bud.cloneWithNewUser(CurrentUser);
                                                            newUD.setObjectId(bud.getObjectId());
                                                            newUD.update(bud.getObjectId(), new UpdateListener() {
                                                                @Override
                                                                public void done(BmobException e) {
                                                                    if (e != null)
                                                                        e.printStackTrace();
                                                                    Log.e("迁移账号数据", "完成");
                                                                }
                                                            });

                                                        } else {
                                                            if (e != null)
                                                                Log.e("error", e.toString());
                                                            Toast.makeText(HContext, R.string.notif_migrate_data_failed, Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            }
                                            if (mBindStuNumCallBackListener != null)
                                                mBindStuNumCallBackListener.callBack(username, password);
                                        }
                                    }).setNegativeButton(R.string.button_cancel, null).create();
                            ad.show();
                        } else {
                            if (userInfo != null) getUserInfo(userInfo);
                            if (mBindStuNumCallBackListener != null)
                                mBindStuNumCallBackListener.callBack(username, password);
                        }
                    }
                });
            } else {
                jwCore.logOut();
                if (toast) {
                    AlertDialog ad = new AlertDialog.Builder(requireContext())
                            .setTitle(getString(R.string.attention))
                            .setMessage(getString(R.string.dialog_message_bind_failed))
                            .setPositiveButton(getString(R.string.button_try_again), null).create();
                    ad.show();
                }
            }
        }
    }

    protected void stopTasks() {
        if (pageTask_login != null && pageTask_login.getStatus() != AsyncTask.Status.FINISHED)
            pageTask_login.cancel(true);
    }

    public void setBindStuNumCallBackListener(BindStuNumCallBackListener bindStuNumCallBackListener) {
        this.mBindStuNumCallBackListener = bindStuNumCallBackListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = View.inflate(requireContext(), R.layout.fragment_bind_student_number, null);
        initViews(view);
        return view;
    }

    @SuppressLint("SetTextI18n")
    private void initViews(View v) {
        username = v.findViewById(R.id.username);
        password = v.findViewById(R.id.password);
        login = v.findViewById(R.id.login);
        TextView currentUserName = v.findViewById(R.id.current_user_name);
        if (CurrentUser != null)
            currentUserName.setText(CurrentUser.getNick() + "(" + CurrentUser.getUsername() + ")");
        login.setOnButtonLoadingListener(new ButtonLoading.OnButtonLoadingListener() {
            @Override
            public void onClick() {
                if (pageTask_login != null && pageTask_login.getStatus() != AsyncTask.Status.FINISHED)
                    pageTask_login.cancel(true);
                pageTask_login = new bindStuNumTask(FragmentBindStuNum.this, username.getText().toString(), password.getText().toString());
                pageTask_login.executeOnExecutor(HITAApplication.TPE, true);

            }

            @Override
            public void onStart() {

            }

            @Override
            public void onFinish() {

            }
        });
        //new directlyLoginTask().executeOnExecutor(HITAApplication.TPE);
    }

    private void getUserInfo(Map<String, String> info) {
        try {
            String stuNum = info.get("student_number");
            String realname = info.get("real_name");
            String school = info.get("school");
            if (CurrentUser != null) {
                CurrentUser.setSchool(school);
                CurrentUser.setStudentnumber(stuNum);
                CurrentUser.setRealname(realname);
                CurrentUser.update(new UpdateListener() {
                    @Override
                    public void done(BmobException e) {
                        Toast.makeText(HContext, R.string.user_info_updated,Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println(userInfos);
    }


    public interface BindStuNumCallBackListener {
        void callBack(String stuNum, String password);
    }

    static class bindStuNumTask extends BaseOperationTask<Object> {

        String username, password;
        Map<String, String> userInfo;

        bindStuNumTask(OperationListener listRefreshedListener, String username, String password) {
            super(listRefreshedListener);
            this.username = username;
            this.password = password;
            userInfo = new HashMap<>();
        }

        @Override
        protected Object doInBackground(OperationListener<Object> listRefreshedListener, Boolean... booleans) {
            try {
                boolean testResult = jwCore.login(username, password);
                if (testResult) {
                    userInfo.putAll(jwCore.getBasicUserInfo());
                }
                return testResult;
            } catch (JWException e) {
                return e;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTasks();
    }
}





