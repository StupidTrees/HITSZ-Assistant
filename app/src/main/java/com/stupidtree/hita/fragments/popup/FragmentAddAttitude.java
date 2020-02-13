package com.stupidtree.hita.fragments.popup;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;
import com.stupidtree.hita.online.Attitude;
import com.yuyh.library.imgsel.ISNav;
import com.yuyh.library.imgsel.common.ImageLoader;
import com.yuyh.library.imgsel.config.ISListConfig;

import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

import static android.app.Activity.RESULT_OK;
import static com.stupidtree.hita.HITAApplication.HContext;

public class FragmentAddAttitude extends BottomSheetDialogFragment {
    ImageView image,clear_image;
    String URI;
    TextView add;
    EditText title;
    Button cancel,post;

    Attitude attitude;
    AttachedActivity mAttachedActivity;


    private static final int REQUEST_LIST_CODE = 0;


    public interface AttachedActivity{
        void onFragmentCalledRefresh();
    }

    public static FragmentAddAttitude newInstance(){
        FragmentAddAttitude r = new FragmentAddAttitude();
        return r;
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
      //  Log.e("attatch", String.valueOf(context));
        if(context instanceof AttachedActivity){
            mAttachedActivity = (AttachedActivity) context;
        }

    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setCancelable(false);
        View view = View.inflate(getContext(), R.layout.fragment_add_attitude, null);
        dialog.setContentView(view);
        ((View) view.getParent()).setBackgroundColor(Color.TRANSPARENT);
        initViews(view);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        attitude= new Attitude("");
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // 图片选择结果回调
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LIST_CODE && resultCode == RESULT_OK && data != null) {
            List<String> pathList = data.getStringArrayListExtra("result");
            if (pathList.size() > 0) {
                URI = pathList.get(0);
                image.setVisibility(View.VISIBLE);
               add.setVisibility(View.GONE);
               clear_image.setVisibility(View.VISIBLE);
                Glide.with(getContext()).load(URI).into(image);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    void initViews(View adv){
        post = adv.findViewById(R.id.post);
        cancel = adv.findViewById(R.id.cancel);
        title = adv.findViewById(R.id.edit_title);
        clear_image = adv.findViewById(R.id.image_clear);
        image = adv.findViewById(R.id.laf_image);
        add = adv.findViewById(R.id.laf_add);
        add.setVisibility(View.VISIBLE);
        image.setVisibility(View.GONE);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(title.getText())){
                    Toast.makeText(getContext(),"请输入标题！",Toast.LENGTH_SHORT).show();
                    return;
                }
                attitude.setTitle(title.getText().toString());
//                if(URI!=null){
//                    final BmobFile bf = new BmobFile(new File(URI));
//                    bf.upload(new UploadFileListener() {
//                        @Override
//                        public void done(BmobException e) {
//                            if(e==null){
//                                attitude.setImageUri(bf.getFileUrl());
//                                p.save(new SaveListener<String>() {
//                                    @Override
//                                    public void done(String s, BmobException e) {
//                                        if(e==null){
//                                            Toast.makeText(HContext,"发送成功！",Toast.LENGTH_SHORT).show();
//                                            if(mAttachedActivity!=null) mAttachedActivity.onFragmentCalledRefresh(type==LOST?0:1);
//                                        }
//                                        else Toast.makeText(HContext,"上传失败！",Toast.LENGTH_SHORT).show();
//                                       }
//                                });
//                            }else{
//                                Toast.makeText(HContext,"上传失败！",Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    });
//                }else{
                    attitude.save(new SaveListener<String>() {
                        @Override
                        public void done(String s, BmobException e) {
                            Toast.makeText(HContext,"成功！",Toast.LENGTH_SHORT).show();
                            if(mAttachedActivity!=null) mAttachedActivity.onFragmentCalledRefresh();
                        }
                    });
               // }
                dismiss();
            }
        });
//        clear_image.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                image.setVisibility(View.GONE);
//                add.setVisibility(View.VISIBLE);
//                p.setLocation(null);
//                clear_image.setVisibility(View.GONE);
//            }
//        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ISListConfig config = new ISListConfig.Builder()
                        // 是否多选, 默认true
                        .multiSelect(false)
                        // 是否记住上次选中记录, 仅当multiSelect为true的时候配置，默认为true
                        .rememberSelected(false)
                        // 使用沉浸式状态栏
                        .statusBarColor(((BaseActivity)getActivity()).getColorPrimary())
                        // 返回图标ResId
                        .backResId(R.drawable.bt_notes_toolbar_back)
                        // 标题
                        .title("图片")
                        // 标题文字颜色
                        .titleColor(Color.WHITE)
                        // TitleBar背景色
                        .titleBgColor(((BaseActivity)getActivity()).getColorPrimary())
                        // 裁剪大小。needCrop为true的时候配置
                        .cropSize(96, 54, 960, 540)
                        .needCrop(true)
                        // 第一个是否显示相机，默认true
                        .needCamera(false)
                        .build();
                ISNav x = ISNav.getInstance();
                x.init(new ImageLoader() {
                    @Override
                    public void displayImage(Context context, String path, ImageView imageView) {
                        //new mImageLoader().loadImage(path,imageView);
                        Glide.with(context).load(path).into(imageView);
                    }
                });
                // 跳转到图片选择器
                x.toListActivity(FragmentAddAttitude.this, config, REQUEST_LIST_CODE);
            }
        });

        title.setHint(getString(R.string.add_attitude_name));
        add.setVisibility(View.VISIBLE);
        image.setVisibility(View.GONE);
    }
}
