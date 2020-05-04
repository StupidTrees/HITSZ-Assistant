package com.stupidtree.hita.fragments.popup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.stupidtree.hita.R;
import com.stupidtree.hita.online.Attitude;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.HContext;

public class FragmentAddAttitude extends FragmentRadiusPopup {
    private EditText title;

    private Attitude attitude;
    private AttachedActivity mAttachedActivity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = View.inflate(getContext(), R.layout.fragment_add_attitude, container);
        initViews(view);
        return view;
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

    @SuppressLint("SetTextI18n")
    private void initViews(View adv) {
        Button post = adv.findViewById(R.id.post);
        Button cancel = adv.findViewById(R.id.cancel);
        title = adv.findViewById(R.id.edit_title);
        ImageView clear_image = adv.findViewById(R.id.image_clear);
        ImageView image = adv.findViewById(R.id.laf_image);
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
                attitude.setAuthor(CurrentUser);
                attitude.save(new SaveListener<String>() {
                        @Override
                        public void done(String s, BmobException e) {
                            Toast.makeText(HContext,"成功！",Toast.LENGTH_SHORT).show();
                            if(mAttachedActivity!=null) mAttachedActivity.refreshAll();
                        }
                    });
                dismiss();
            }
        });

        title.setHint(getString(R.string.add_attitude_name));
        image.setVisibility(View.GONE);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        attitude = new Attitude("");
    }

    public interface AttachedActivity {
        void refreshAll();

        void refreshOthers();

        void notifyItem(String objectId);
    }
}
