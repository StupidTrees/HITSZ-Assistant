package com.stupidtree.hita.util;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.autonavi.base.amap.mapcore.FileUtil;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.stupidtree.hita.BaseActivity;

import java.io.File;

public class GalleryPickerUtils {
    public static final int REQUEST_PICK_ONE_PHOTO = 2;
    public static final int REQUEST_CROP_PHOTO = 3;
    public static void  pickFromGallery(Activity pickResultReceiver){
        FileOperator.verifyStoragePermissions(pickResultReceiver);
        Intent intentToPickPic = new Intent(Intent.ACTION_PICK, null);
        intentToPickPic.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        pickResultReceiver.startActivityForResult(intentToPickPic, 2);
    }

    public static void cropPhoto(Activity activity,Uri uri, Uri cropUri, int outputX, int outputY) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");//可裁剪
        intent.putExtra("aspectX", 1); //裁剪的宽比例
        intent.putExtra("aspectY", 1);  //裁剪的高比例
        intent.putExtra("outputX", outputX); //裁剪的宽度
        intent.putExtra("outputY", outputY);  //裁剪的高度
        intent.putExtra("scale", true); //支持缩放
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cropUri);  //将裁剪的结果输出到指定的Uri
        intent.putExtra("return-data", false); //若为true则表示返回数据
        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());//裁剪成的图片的格式
        intent.putExtra("noFaceDetection", true);  //启用人脸识别
        activity.startActivityForResult(intent,REQUEST_CROP_PHOTO);
    }

}
