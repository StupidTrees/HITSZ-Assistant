package com.stupidtree.hita.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivitySubject;
import com.stupidtree.hita.adapter.SubjectsListAdapter;
import com.stupidtree.hita.core.Curriculum;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.DATA_STATE_HEALTHY;
import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.allCurriculum;
import static com.stupidtree.hita.HITAApplication.getDataState;
import static com.stupidtree.hita.HITAApplication.thisCurriculumIndex;

public class FragmentUserCenter_Info extends Fragment {


    TextView username,nick,studentnumber,realname,school,signature;
    LinearLayout nickItem,signatureItem,studentsnumberItem;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View  v = inflater.inflate(R.layout.fragment_usercenter_info, container, false);
        initViews(v);
        return v;
    }
    
    void initViews(View v){
        username = v.findViewById(R.id.text_username);
        nick = v.findViewById(R.id.text_nick);
        studentnumber = v.findViewById(R.id.text_studentnumber);
        realname = v.findViewById(R.id.text_realname);
        school = v.findViewById(R.id.text_school);
        signature = v.findViewById(R.id.text_signature);
        nickItem = v.findViewById(R.id.item_nick);
        signatureItem = v.findViewById(R.id.item_signature);
        studentsnumberItem = v.findViewById(R.id.item_studensnumber);
        username.setText(showText(CurrentUser.getUsername()));
        nick.setText(showText(CurrentUser.getNick()));
        studentnumber.setText(showText(CurrentUser.getStudentnumber()));
        realname.setText(showText(CurrentUser.getRealname()));
        school.setText(showText(CurrentUser.getSchool()));
        signature.setText(showText(CurrentUser.getSignature()));


        signatureItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View lv = getLayoutInflater().inflate(R.layout.dialog_usercenter_setinfo,null);
                final EditText et = lv.findViewById(R.id.setinfo_text);
                et.setText(signature.getText());
                new AlertDialog.Builder(getContext()).setTitle("设置个性签名")
                        .setView(lv)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                CurrentUser.setSignature(et.getText().toString());
                                CurrentUser.update(new UpdateListener() {
                                    @Override
                                    public void done(BmobException e) {
                                        if(e==null){
                                            Toast.makeText(HContext,"更改签名成功",Toast.LENGTH_SHORT).show();
                                            signature.setText(CurrentUser.getSignature());
                                        }else{
                                            Toast.makeText(HContext,"更改签名失败",Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        });

        nickItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View lv = getLayoutInflater().inflate(R.layout.dialog_usercenter_setinfo,null);
                final EditText et = lv.findViewById(R.id.setinfo_text);
                et.setText(nick.getText());
                new AlertDialog.Builder(getContext()).setTitle("设置昵称")
                        .setView(lv)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                CurrentUser.setNick(et.getText().toString());
                                CurrentUser.update(new UpdateListener() {
                                    @Override
                                    public void done(BmobException e) {
                                        if(e==null){
                                            Toast.makeText(HContext,"更改昵称成功",Toast.LENGTH_SHORT).show();
                                            nick.setText(CurrentUser.getNick());
                                        }else{
                                            Toast.makeText(HContext,"更改昵称失败",Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        });
        studentsnumberItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View lv = getLayoutInflater().inflate(R.layout.dialog_usercenter_setinfo,null);
                final EditText et = lv.findViewById(R.id.setinfo_text);
                et.setText(studentnumber.getText());
                new AlertDialog.Builder(getContext()).setTitle("注意：绑定学号后其他账号将无法绑定该学号")
                        .setView(lv)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                CurrentUser.setStudentnumber(et.getText().toString());
                                CurrentUser.update(new UpdateListener() {
                                    @Override
                                    public void done(BmobException e) {
                                        if(e==null){
                                            Toast.makeText(HContext,"更改绑定学号成功",Toast.LENGTH_SHORT).show();
                                            studentnumber.setText(CurrentUser.getStudentnumber());
                                        }else{
                                            if(e.getErrorCode()==401)  Toast.makeText(HContext,"该学号已与其他账号绑定！",Toast.LENGTH_SHORT).show();
                                            else Toast.makeText(HContext,e.toString(),Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        });
    }
    
    String showText(String raw){
        if(raw==null||raw.isEmpty()) return "未设置";
        else return raw;
    }




}
