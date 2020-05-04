package com.stupidtree.hita.util;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.core.content.ContextCompat;

import java.io.File;

import static androidx.core.app.ActivityCompat.requestPermissions;
import static com.stupidtree.hita.community.ActivityCreatePost.RC_CHOOSE_PHOTO;
import static com.stupidtree.hita.community.ActivityCreatePost.RC_CROP_PHOTO;
import static com.stupidtree.hita.community.ActivityCreatePost.RC_TAKE_PHOTO;

/**
 * @author D10NG
 * @date on 2019-05-15 09:15
 */
public class GalleryPicker {

    /**
     * 剪裁输出uri路径
     */
    public static final Uri cropImgUri = Uri.parse("file:///" + Environment.getExternalStorageDirectory() + "/photo_crop.jpg");
    /**
     * 拍照输出真实路径
     */
    public String tempPhotoPath;

    /**
     * 打开相机
     */
    public static void takePhoto(Activity mContext) {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
            // 未授权，申请授权
            requestPermissions(mContext,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA},
                    RC_TAKE_PHOTO);
            return;
        }
        // 已授权
        Intent intentToTakePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 设置照片输出位置
        File photoFile = new File(Environment.getExternalStorageDirectory(), "photo.jpg");
        //  tempPhotoPath = photoFile.getAbsolutePath();
        Uri tempImgUri = FileProviderUtils.getUriForFile(mContext, photoFile);
        intentToTakePhoto.putExtra(MediaStore.EXTRA_OUTPUT, tempImgUri);
        mContext.startActivityForResult(intentToTakePhoto, RC_TAKE_PHOTO);
    }

    /**
     * 选图
     */
    public static void choosePhoto(Activity mContext, boolean multiple) {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // 未授权，申请授权(从相册选择图片需要读取存储卡的权限)
            requestPermissions(mContext,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    RC_CHOOSE_PHOTO);
            return;
        }
        // 已授权，获取照片
        Intent intentToPickPic = new Intent(Intent.ACTION_PICK, null);
        if (multiple) intentToPickPic.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intentToPickPic.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        mContext.startActivityForResult(intentToPickPic, RC_CHOOSE_PHOTO);
    }

    /**
     * 剪裁图片
     */
    public static void cropPhoto(Activity mContext, String path, Uri toPath, int size) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        FileProviderUtils.setIntentDataAndType(mContext, intent, new File(path));
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", size);
        intent.putExtra("outputY", size);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, toPath);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        mContext.startActivityForResult(intent, RC_CROP_PHOTO);
    }

}
