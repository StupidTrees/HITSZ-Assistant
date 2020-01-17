package com.stupidtree.hita.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.R;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.defaultSP;

public class FragmentUserCenter_Info extends BaseFragment {


    TextView username, nick, studentnumber, realname, school, signature;
    LinearLayout nickItem, signatureItem, studentsnumberItem;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View  v = inflater.inflate(R.layout.fragment_usercenter_info,container,false);
        initViews(v);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        Refresh();
    }

    void initViews(View v) {
        username = v.findViewById(R.id.text_username);
        nick = v.findViewById(R.id.text_nick);
        studentnumber = v.findViewById(R.id.text_studentnumber);
        realname = v.findViewById(R.id.text_realname);
        school = v.findViewById(R.id.text_school);
        signature = v.findViewById(R.id.text_signature);
        nickItem = v.findViewById(R.id.item_nick);
        signatureItem = v.findViewById(R.id.item_signature);
        studentsnumberItem = v.findViewById(R.id.item_studensnumber);



        signatureItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View lv = getLayoutInflater().inflate(R.layout.dialog_usercenter_setinfo, null);
                final EditText et = lv.findViewById(R.id.setinfo_text);
                et.setText(signature.getText());
                new AlertDialog.Builder(getContext()).setTitle(R.string.set_signature)
                        .setView(lv)
                        .setPositiveButton(getString(R.string.button_confirm), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                CurrentUser.setSignature(et.getText().toString());
                                CurrentUser.update(new UpdateListener() {
                                    @Override
                                    public void done(BmobException e) {
                                        if (e == null) {
                                            Toast.makeText(HContext, R.string.notif_signature_updated, Toast.LENGTH_SHORT).show();
                                            signature.setText(CurrentUser.getSignature());
                                        } else {
                                            Toast.makeText(HContext, R.string.notif_signature_update_failed, Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });
                            }
                        })
                        .setNegativeButton(getString(R.string.button_cancel), null)
                        .show();
            }
        });

        nickItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View lv = getLayoutInflater().inflate(R.layout.dialog_usercenter_setinfo, null);
                final EditText et = lv.findViewById(R.id.setinfo_text);
                et.setText(nick.getText());
                new AlertDialog.Builder(getContext()).setTitle(R.string.notif_set_nick)
                        .setView(lv)
                        .setPositiveButton(getString(R.string.button_confirm), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                CurrentUser.setNick(et.getText().toString());
                                CurrentUser.update(new UpdateListener() {
                                    @Override
                                    public void done(BmobException e) {
                                        if (e == null) {
                                            Toast.makeText(HContext, R.string.notif_nick_updated, Toast.LENGTH_SHORT).show();
                                            nick.setText(CurrentUser.getNick());
                                        } else {
                                            Toast.makeText(HContext, R.string.notif_nick_update_failed, Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });
                            }
                        })
                        .setNegativeButton(getString(R.string.button_cancel), null)
                        .show();
            }
        });
        studentsnumberItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FragmentBindStuNum fbs = new FragmentBindStuNum();
                final String oldStuNumBackUp = CurrentUser.getStudentnumber();
                fbs.setBindStuNumCallBackListener(new FragmentBindStuNum.BindStuNumCallBackListener() {
                    @Override
                    public void callBack(String stuNum, final String password) {
                        studentnumber.setText(stuNum);
                        CurrentUser.setStudentnumber(stuNum);
                        CurrentUser.update(new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                fbs.dismiss();
                                if (e == null) {
                                    Toast.makeText(HContext, R.string.notif_bind_studentnumber_success, Toast.LENGTH_SHORT).show();
                                    studentnumber.setText(CurrentUser.getStudentnumber());
                                    school.setText(CurrentUser.getSchool());
                                    realname.setText(CurrentUser.getRealname());
                                    defaultSP.edit().putString(CurrentUser.getStudentnumber()+".password",password).apply();

                                } else {
                                    studentnumber.setText(showText(CurrentUser.getStudentnumber()));
                                    CurrentUser.setStudentnumber(oldStuNumBackUp);
                                    if (e.getErrorCode() == 401)
                                        Toast.makeText(HContext, R.string.notif_bind_studentnumber_already, Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(HContext, e.toString(), Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                    }

                });

                fbs.show(getFragmentManager(),"fbs");
//                View lv = getLayoutInflater().inflate(R.layout.dialog_usercenter_setinfo,null);
//                final EditText et = lv.findViewById(R.id.setinfo_text);
//                et.setText(studentnumber.getText());
//                new AlertDialog.Builder(getContext()).setTitle("注意：绑定学号后其他账号将无法绑定该学号")
//                        .setView(lv)
//                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//                                CurrentUser.setStudentnumber(et.getText().toString());
//                                CurrentUser.update(new UpdateListener() {
//                                    @Override
//                                    public void done(BmobException e) {
//                                        if(e==null){
//                                            Toast.makeText(HContext,"更改绑定学号成功",Toast.LENGTH_SHORT).show();
//                                            studentnumber.setText(CurrentUser.getStudentnumber());
//                                        }else{
//                                            if(e.getErrorCode()==401)  Toast.makeText(HContext,"该学号已与其他账号绑定！",Toast.LENGTH_SHORT).show();
//                                            else Toast.makeText(HContext,e.toString(),Toast.LENGTH_SHORT).show();
//                                        }
//
//                                    }
//                                });
//                            }
//                        })
//                        .setNegativeButton("取消", null)
//                        .show();
            }
        });
    }

    String showText(String raw) {
        if (raw == null || raw.isEmpty()) return getString(R.string.settings_not_set_yet);
        else return raw;
    }


    @Override
    protected void stopTasks() {

    }

    @Override
    public void Refresh() {
        try {
            username.setText(showText(CurrentUser.getUsername()));
            nick.setText(showText(CurrentUser.getNick()));
            studentnumber.setText(showText(CurrentUser.getStudentnumber()));
            realname.setText(showText(CurrentUser.getRealname()));
            school.setText(showText(CurrentUser.getSchool()));
            signature.setText(showText(CurrentUser.getSignature()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
