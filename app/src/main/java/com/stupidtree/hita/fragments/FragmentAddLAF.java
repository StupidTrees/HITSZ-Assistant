package com.stupidtree.hita.fragments;

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
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;
import com.stupidtree.hita.diy.PickInfoDialog;
import com.stupidtree.hita.online.HITAUser;
import com.stupidtree.hita.online.Location;
import com.stupidtree.hita.online.LostAndFound;
import com.yuyh.library.imgsel.ISNav;
import com.yuyh.library.imgsel.common.ImageLoader;
import com.yuyh.library.imgsel.config.ISListConfig;

import java.io.File;
import java.util.List;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UploadFileListener;

import static android.app.Activity.RESULT_OK;
import static com.stupidtree.hita.HITAApplication.HContext;

public class FragmentAddLAF extends BottomSheetDialogFragment {
    ImageView image,clear_location,clear_image;
    String URI;
    TextView pickLocation,dialog_title,add;
    EditText title;
    Button cancel,post;
    EditText content,contact;
    LostAndFound p;
    int type;
    AttachedActivity mAttachedActivity;
    Switch  anonymous;

    public static final int LOST = 144;
    public static final int FOUND = 239;
    private static final int REQUEST_LIST_CODE = 0;


    public interface AttachedActivity{
        void onFragmentCalledRefresh(int which);
    }

    public static FragmentAddLAF newInstance(int type){
        Bundle b = new Bundle();
        b.putInt("type",type);
        FragmentAddLAF r = new FragmentAddLAF();
        r.setArguments(b);
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
        View view = View.inflate(getContext(), R.layout.fragment_add_laf, null);
        dialog.setContentView(view);
        ((View) view.getParent()).setBackgroundColor(Color.TRANSPARENT);
        initViews(view);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        p = new LostAndFound();
        if(getArguments()!=null) type = getArguments().getInt("type");
        else type = LOST;
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
        anonymous = adv.findViewById(R.id.anonymous);
        dialog_title = adv.findViewById(R.id.title);
        post = adv.findViewById(R.id.post);
        cancel = adv.findViewById(R.id.cancel);
        contact = adv.findViewById(R.id.edit_contact);
        pickLocation = adv.findViewById(R.id.location_text);
        title = adv.findViewById(R.id.edit_title);
        clear_location = adv.findViewById(R.id.location_clear);
        clear_image = adv.findViewById(R.id.image_clear);
        content = adv.findViewById(R.id.edit_content);
        image = adv.findViewById(R.id.laf_image);
        add = adv.findViewById(R.id.laf_add);
        add.setVisibility(View.VISIBLE);
        image.setVisibility(View.GONE);
        pickLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PickInfoDialog(getContext(), "选择地点", PickInfoDialog.LOCATION_ALL, new PickInfoDialog.OnPickListener() {
                    @Override
                    public void OnPick(String title, Object obj) {
                        if(obj instanceof Location){
                            p.setLocation((Location) obj);
                            pickLocation.setText(title);
                            clear_location.setVisibility(View.VISIBLE);
                        }
                    }
                }).show();
            }
        });
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
                p.setAuthor(BmobUser.getCurrentUser(HITAUser.class));
                p.setTitle(title.getText().toString());
                p.setAnonymous(anonymous.isChecked());
                p.setFound(false);
                p.setContent(content.getText().toString());
                p.setContact(contact.getText().toString());
                p.setType(type==FOUND?"found":"lost");
                if(URI!=null){
                    final BmobFile bf = new BmobFile(new File(URI));
                    bf.upload(new UploadFileListener() {
                        @Override
                        public void done(BmobException e) {
                            if(e==null){
                                p.setImageUri(bf.getFileUrl());
                                p.save(new SaveListener<String>() {
                                    @Override
                                    public void done(String s, BmobException e) {
                                        if(e==null){
                                            Toast.makeText(HContext,"发送成功！",Toast.LENGTH_SHORT).show();
                                            if(mAttachedActivity!=null) mAttachedActivity.onFragmentCalledRefresh(type==LOST?0:1);
                                        }
                                        else Toast.makeText(HContext,"上传失败！",Toast.LENGTH_SHORT).show();
                                       }
                                });
                            }else{
                                Toast.makeText(HContext,"上传失败！",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else{
                    p.save(new SaveListener<String>() {
                        @Override
                        public void done(String s, BmobException e) {
                            Toast.makeText(HContext,"成功！",Toast.LENGTH_SHORT).show();
                            if(mAttachedActivity!=null) mAttachedActivity.onFragmentCalledRefresh(type==LOST?0:1);
                        }
                    });
                }
                dismiss();
            }
        });
        clear_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickLocation.setText("不设置地点");
                p.setLocation(null);
                clear_location.setVisibility(View.GONE);
            }
        });
        clear_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                image.setVisibility(View.GONE);
                add.setVisibility(View.VISIBLE);
                p.setLocation(null);
                clear_image.setVisibility(View.GONE);
            }
        });
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
                x.toListActivity(FragmentAddLAF.this, config, REQUEST_LIST_CODE);
            }
        });

        title.setHint("输入标题："+(type==FOUND?"找到…":"丢失…"));
        dialog_title.setText("发布"+(type==FOUND?"失物招领":"寻物启事"));
        add.setVisibility(View.VISIBLE);
        image.setVisibility(View.GONE);
    }
}
