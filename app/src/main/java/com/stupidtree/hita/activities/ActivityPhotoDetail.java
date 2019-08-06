package com.stupidtree.hita.activities;

import android.os.Bundle;

import com.bm.library.PhotoView;
import com.bumptech.glide.Glide;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;



public class ActivityPhotoDetail extends BaseActivity {

    String imagePath;
    PhotoView photoView;

    @Override
    protected void stopTasks() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true,false,true);
        setContentView(R.layout.activity_note_detail);
        imagePath = getIntent().getStringExtra("imagePath");
        photoView = findViewById(R.id.photoview);
        Glide.with(this).load(imagePath).into(photoView);
        //photoView.setImageDrawable(GlideDrawable.createFromPath(imagePath));
        photoView.enable();
       // noteText.setText(text);
       //Glide.with(this).load(imagePath).asBitmap();
    }
}
