package com.stupidtree.hita.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import androidx.appcompat.app.AlertDialog;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.diy.ButtonLoading;
import com.stupidtree.hita.jw.JWException;
import com.stupidtree.hita.online.Bmob_User_Data;
import com.stupidtree.hita.online.HITAUser;

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

public class FragmentBindStuNum extends BottomSheetDialogFragment {

    EditText username, password;
    ButtonLoading login;
    LinearLayout loginCard;
    TextView currentUserName;
    bindStuNumTask pageTask_login;
    BindStuNumCallBackListener mBindStuNumCallBackListener;

    interface BindStuNumCallBackListener {
        void callBack(String stuNum,String password);
    }

    protected void stopTasks() {
        if (pageTask_login != null && pageTask_login.getStatus()!=AsyncTask.Status.FINISHED) pageTask_login.cancel(true);
    }

    public BindStuNumCallBackListener getBindStuNumCallBackListener() {
        return mBindStuNumCallBackListener;
    }

    public void setBindStuNumCallBackListener(BindStuNumCallBackListener bindStuNumCallBackListener) {
        this.mBindStuNumCallBackListener = bindStuNumCallBackListener;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = View.inflate(getContext(), R.layout.fragment_bind_student_number, null);
        dialog.setContentView(view);
        ((View) view.getParent()).setBackgroundColor(Color.TRANSPARENT);
        initViews(view);
        return dialog;
    }

//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(getContext(), themeID);// your app theme here
//        View v = inflater.cloneInContext(contextThemeWrapper).inflate(R.layout.fragment_bind_student_number, container, false);
//
//        return v;
//    }

    void initViews(View v) {
        username = v.findViewById(R.id.username);
        password = v.findViewById(R.id.password);
        login = v.findViewById(R.id.login);
        currentUserName = v.findViewById(R.id.current_user_name);
        loginCard = v.findViewById(R.id.logincard);
        if (CurrentUser != null)
            currentUserName.setText(CurrentUser.getNick() + "（" + CurrentUser.getUsername() + "）");
        login.setOnButtonLoadingListener(new ButtonLoading.OnButtonLoadingListener() {
            @Override
            public void onClick() {
                if (pageTask_login != null && pageTask_login.getStatus()!=AsyncTask.Status.FINISHED)
                    pageTask_login.cancel(true);
                pageTask_login = new bindStuNumTask(username.getText().toString(), password.getText().toString(), true);
                pageTask_login.executeOnExecutor(HITAApplication.TPE);

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




    class bindStuNumTask extends AsyncTask<String, Integer, Object> {

        String username, password;
        Map<String,String> userInfo;
        boolean toast;

        bindStuNumTask(String username, String password, boolean toast) {
            this.username = username;
            this.password = password;
            this.toast = toast;
            userInfo = new HashMap<>();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            login.setProgress(true);
        }

        @Override
        protected Object doInBackground(String... strings) {
            try {
                boolean testResult = jwCore.login(username,password);
                if (testResult) {
                    userInfo.putAll(jwCore.getBasicUserInfo());
                }
                return testResult;
            } catch (JWException e){
                return e;
            }
        }

        @Override
        protected void onPostExecute (Object s) {
            super.onPostExecute(s);
            login.setProgress(false);
            if(s instanceof JWException){
                if (toast) {
                    AlertDialog ad = new AlertDialog.Builder(getContext())
                            .setTitle(getString(R.string.attention))
                            .setMessage(getString(R.string.dialog_message_bind_failed)+":"+((JWException) s).getDialogMessage())
                            .setPositiveButton(getString(R.string.button_try_again), null).create();
                    ad.show();
                }
            }else if(s instanceof Boolean){
                if((boolean)s){
                    BmobQuery<HITAUser> bq = new BmobQuery<>();
                    bq.addWhereEqualTo("studentnumber",username);
                    bq.findObjects(new FindListener<HITAUser>() {
                        @Override
                        public void done(final List<HITAUser> list, BmobException e) {
                            if(list!=null&&list.size()>0){
                                List<HITAUser> toR = new ArrayList<>();
                                for(HITAUser h:list) if(h.getObjectId().equals(CurrentUser.getObjectId())) toR.add(h);
                                list.removeAll(toR);
                            }
                            if(e==null&&list!=null&&list.size()>0){
                                final boolean[] options = {true,true};
                                AlertDialog ad = new AlertDialog.Builder(getContext()).setTitle(
                                        String.format(getString(R.string.dialog_title_already_bound_to),list.get(0).getUsername(),list.get(0).getNick()))
                                        .setMultiChoiceItems(new String[]{getString(R.string.dialog_item_migrate_data)}, new boolean[]{true}, new DialogInterface.OnMultiChoiceClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                                                options[i] = b;
                                            }
                                        }).setPositiveButton(R.string.button_confirm, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                if(userInfo!=null) getUserInfo(userInfo);
                                                if(options[0]){
                                                    BmobQuery<Bmob_User_Data> bq = new BmobQuery();
                                                    bq.addWhereEqualTo("hitaUser",list.get(0).getObjectId());
                                                    bq.findObjects(new FindListener<Bmob_User_Data>() {
                                                        @Override
                                                        public void done(List<Bmob_User_Data> list2, BmobException e) {
                                                            if(e==null&&list2!=null&&list2.size()>0){
                                                                // Log.e("found!", String.valueOf(list2));
                                                                // System.out.println(list2.get(0).getHitaUser());
                                                                Bmob_User_Data bud = list2.get(0);
                                                                timeTableCore.loadDataFromCloud(bud);
                                                            }else{
                                                                Log.e("error",e.toString());
                                                                Toast.makeText(HContext, R.string.notif_migrate_data_failed,Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                                }
                                                if (mBindStuNumCallBackListener != null)
                                                    mBindStuNumCallBackListener.callBack(username,password);
                                            }
                                        }).setNegativeButton(R.string.button_cancel,null).create();
                                ad.show();
                            }else{
                                if(userInfo!=null) getUserInfo(userInfo);
                                if (mBindStuNumCallBackListener != null)
                                    mBindStuNumCallBackListener.callBack(username,password);
                            }
                        }
                    });
                }else{
                    jwCore.logOut();
                    if (toast) {
                        AlertDialog ad = new AlertDialog.Builder(getContext())
                                .setTitle(getString(R.string.attention))
                                .setMessage(getString(R.string.dialog_message_bind_failed))
                                .setPositiveButton(getString(R.string.button_try_again), null).create();
                        ad.show();
                    }
                }
            }
        }
    }

    public void getUserInfo(Map<String,String> info) {
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTasks();
    }
}





