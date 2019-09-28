package com.stupidtree.hita.util;

import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.stupidtree.hita.R;

import static com.stupidtree.hita.HITAApplication.HContext;

public class ImageUtils {

    public static void loadImage_Bmob(final ImageView view, final String url){
        view.post(new Runnable() {
            @Override
            public void run() {
                int y = view.getWidth();
                int x = view.getHeight();
                String toU = url+"!/fxfn/"+x+"x"+y;
                Log.e("x,y",x+","+y);
                Glide.with(HContext).load(toU).centerCrop().into(view);

            }
        });
       }
}
