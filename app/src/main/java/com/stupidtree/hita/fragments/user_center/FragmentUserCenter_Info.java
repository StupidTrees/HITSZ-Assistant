package com.stupidtree.hita.fragments.user_center;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.stupidtree.hita.R;
import com.stupidtree.hita.fragments.BaseFragment;
import com.stupidtree.hita.fragments.popup.FragmentBindStuNum;
import com.stupidtree.hita.views.PickNumberDialog;

import java.util.Calendar;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.defaultSP;
import static com.stupidtree.hita.HITAApplication.timeTableCore;


public class FragmentUserCenter_Info extends BaseFragment {


    private TextView username, nick, studentID, realName, school, signature, profile, grade;

    public FragmentUserCenter_Info() {

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        Refresh();
    }

    private void initViews(View v) {
        username = v.findViewById(R.id.text_username);
        nick = v.findViewById(R.id.text_nick);
        studentID = v.findViewById(R.id.text_studentnumber);
        realName = v.findViewById(R.id.text_realname);
        school = v.findViewById(R.id.text_school);
        signature = v.findViewById(R.id.text_signature);
        LinearLayout nickItem = v.findViewById(R.id.item_nick);
        LinearLayout signatureItem = v.findViewById(R.id.item_signature);
        LinearLayout studentsnumberItem = v.findViewById(R.id.item_studensnumber);

        LinearLayout profileItem = v.findViewById(R.id.item_profile);
        profile = v.findViewById(R.id.text_profile);
        grade = v.findViewById(R.id.text_grade);
        LinearLayout gradeItem = v.findViewById(R.id.item_grade);

        signatureItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                @SuppressLint("InflateParams") View lv = getLayoutInflater().inflate(R.layout.dialog_editinfo, null);
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
                                            Toast.makeText(getContext(), R.string.notif_signature_updated, Toast.LENGTH_SHORT).show();
                                            Refresh();
                                        } else {
                                            Toast.makeText(getContext(), R.string.notif_signature_update_failed, Toast.LENGTH_SHORT).show();
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
                @SuppressLint("InflateParams") View lv = getLayoutInflater().inflate(R.layout.dialog_editinfo, null);
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
                                            Toast.makeText(getContext(), R.string.notif_nick_updated, Toast.LENGTH_SHORT).show();
                                            Refresh();
                                        } else {
                                            Toast.makeText(getContext(), R.string.notif_nick_update_failed, Toast.LENGTH_SHORT).show();
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
                        studentID.setText(stuNum);
                        CurrentUser.setStudentnumber(stuNum);
                        CurrentUser.update(new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                fbs.dismiss();
                                if (e == null) {
                                    Toast.makeText(getContext(), R.string.notif_bind_studentnumber_success, Toast.LENGTH_SHORT).show();
                                    studentID.setText(CurrentUser.getStudentnumber());
                                    school.setText(CurrentUser.getSchool());
                                    realName.setText(CurrentUser.getRealname());
                                    defaultSP.edit().putString(CurrentUser.getStudentnumber()+".password",password).apply();

                                } else {
                                    studentID.setText(showText(CurrentUser.getStudentnumber()));
                                    CurrentUser.setStudentnumber(oldStuNumBackUp);
                                    if (e.getErrorCode() == 401)
                                        Toast.makeText(getContext(), R.string.notif_bind_studentnumber_already, Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                    }

                });

                fbs.show(getChildFragmentManager(), "fbs");
//                View lv = getLayoutInflater().inflate(R.layout.dialog_editinfo,null);
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
//                                            Toast.makeText(getContext(),"更改绑定学号成功",Toast.LENGTH_SHORT).show();
//                                            studentnumber.setText(CurrentUser.getStudentnumber());
//                                        }else{
//                                            if(e.getErrorCode()==401)  Toast.makeText(getContext(),"该学号已与其他账号绑定！",Toast.LENGTH_SHORT).show();
//                                            else Toast.makeText(getContext(),e.toString(),Toast.LENGTH_SHORT).show();
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

        gradeItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PickNumberDialog(getBaseActivity(), getString(R.string.usercenter_info_grade),
                        timeTableCore.getNow().get(Calendar.YEAR), 2016, new PickNumberDialog.onDialogConformListener() {
                    @Override
                    public void onClick(int number) {
                        CurrentUser.setGrade(number);
                        CurrentUser.update(new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if (e == null) {
                                    Toast.makeText(getContext(), R.string.notif_grade_updated, Toast.LENGTH_SHORT).show();
                                    Refresh();
                                } else {
                                    Toast.makeText(getContext(), R.string.notif_grade_update_failed, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }).setInitialValue(CurrentUser.getGrade()).show();
            }
        });
        profileItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int[] selected = {0};
                new AlertDialog.Builder(getBaseActivity())
                        .setTitle(R.string.usercenter_info_profile)
                        .setSingleChoiceItems(R.array.profile_policy_items,
                                CurrentUser.getProfilePolicyIndex(),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        selected[0] = which;
                                    }
                                }).setPositiveButton(R.string.button_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CurrentUser.setProfilePolicyIndex(selected[0]);
                        CurrentUser.update(new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if (e == null) {
                                    Refresh();
                                    Toast.makeText(getContext(), R.string.notif_policy_updated, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), R.string.notif_policy_update_failed, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }).show();
            }
        });
    }

    private String showText(String raw) {
        if (raw == null || raw.isEmpty()) return getString(R.string.settings_not_set_yet);
        else return raw;
    }


    @Override
    protected void stopTasks() {

    }

    @Override
    public void Refresh() {
        if (CurrentUser == null) return;
        boolean changed = false;
        if (TextUtils.isEmpty(CurrentUser.getProfilePolicy())) {
            CurrentUser.setProfilePolicyIndex(1);
            changed = true;
        }
        if (CurrentUser.getGrade() < 2000 && !TextUtils.isEmpty(CurrentUser.getStudentnumber())) {
            try {
                String sn = CurrentUser.getStudentnumber();
                String numb = sn.replaceAll("SZ", "").replaceAll("sz", "");
                String two = numb.substring(0, 2);
                int gradeGuess = Integer.parseInt(two);
                CurrentUser.setGrade(2000 + gradeGuess);
                changed = true;
            } catch (Exception ignored) {

            }
        }
        if (changed) CurrentUser.update(new UpdateListener() {
            @Override
            public void done(BmobException e) {

            }
        });
        try {
            username.setText(showText(CurrentUser.getUsername()));
            nick.setText(showText(CurrentUser.getNick()));
            studentID.setText(showText(CurrentUser.getStudentnumber()));
            realName.setText(showText(CurrentUser.getRealname()));
            school.setText(showText(CurrentUser.getSchool()));
            signature.setText(showText(CurrentUser.getSignature()));
            String[] items = getResources().getStringArray(R.array.profile_policy_items);
            profile.setText(items[CurrentUser.getProfilePolicyIndex()]);
            if (CurrentUser.getGrade() < 2000) {
                grade.setText(R.string.settings_not_set_yet);
            } else grade.setText(String.valueOf(CurrentUser.getGrade()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_usercenter_info;
    }
}
