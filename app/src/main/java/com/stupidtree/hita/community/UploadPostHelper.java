package com.stupidtree.hita.community;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.stupidtree.hita.online.Post;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UploadBatchListener;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

public class UploadPostHelper {
    List<String> rawImages;
    List<String> compressedImages;
    Context context;
    StateListener listener;
    Post post;

    public UploadPostHelper(Context context, List<String> rawImages, Post post, StateListener listener) {
        this.context = context;
        compressedImages = new ArrayList<>();
        this.listener = listener;
        this.rawImages = rawImages;
        this.post = post;
    }

    public void run() {
        listener.onPostStart();
        compressBatch();
    }

    private void compressBatch() {
        if (rawImages.size() == 0) {
            listener.onCompressSuccess();
            upLoadFile();
            return;
        }
        final LinkedList<Runnable> taskList = new LinkedList<>();
        final Handler handler = new Handler();
        class Task implements Runnable {
            String path;

            Task(String path) {
                this.path = path;
            }

            @Override
            public void run() {
                Luban.with(context)
                        .setTargetDir(context.getExternalCacheDir().getAbsolutePath())
                        .load(new java.io.File(path))
                        .setCompressListener(new OnCompressListener() { //设置回调
                            @Override
                            public void onStart() {
                                listener.onCompressStart(taskList.size() + 1);
                            }

                            @Override
                            public void onSuccess(java.io.File file) {
                                compressedImages.add(file.getPath());
                                if (!taskList.isEmpty()) {
                                    Runnable runnable = taskList.pop();
                                    handler.post(runnable);
                                } else {
                                    listener.onCompressSuccess();
                                    upLoadFile();
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                listener.onCompressFails();
                            }
                        }).launch();    //启动压缩
            }
        }
        //循环遍历原始路径 添加至linklist中
        for (String path : rawImages) {
            taskList.add(new Task(path));
        }
        handler.post(taskList.pop());
    }

    private void upLoadFile() {
        listener.onUploadStart();
        if (rawImages.size() == 0) {
            post.setImages(new ArrayList<String>());
            listener.onUploadSuccess();
            post.save(new SaveListener<String>() {
                @Override
                public void done(String s, BmobException e) {
                    if (e == null) {
                        listener.onPostSuccess();
                    } else {
                        listener.onPostFails();
                    }
                }
            });
            return;
        }
        String[] paths = new String[compressedImages.size()];
        for (int i = 0; i < paths.length; i++) {
            paths[i] = compressedImages.get(i);
        }
        BmobFile.uploadBatch(paths, new UploadBatchListener() {
            @Override
            public void onSuccess(List<BmobFile> files, List<String> urls) {
                post.setImages(urls);
                if (urls.size() == compressedImages.size()) {
                    listener.onUploadSuccess();
                    post.save(new SaveListener<String>() {
                        @Override
                        public void done(String s, BmobException e) {
                            if (e == null) {
                                listener.onPostSuccess();
                            } else {
                                listener.onPostFails();
                            }
                        }
                    });
                }
            }

            @Override
            public void onProgress(int curIndex, int curPercent, int total, int totalPercent) {
                listener.onUploadProgress(curIndex, curPercent, total, totalPercent);
            }

            @Override
            public void onError(int statusCode, String errorMsg) {
                Log.e("error!", errorMsg);
                listener.onUploadFailed();
            }
        });

    }


    interface StateListener {
        void onPostStart();

        void onCompressStart(int number);

        void onCompressSuccess();

        void onCompressFails();

        void onUploadStart();

        void onUploadProgress(int curIndex, int curPercent, int total, int totalPercent);

        void onUploadSuccess();

        void onUploadFailed();

        void onPostSuccess();

        void onPostFails();
    }


}
