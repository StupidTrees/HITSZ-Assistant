package com.stupidtree.hita.online;

import android.os.AsyncTask;

public abstract class DownloadTask extends AsyncTask<String, Integer, Integer> {
    protected static final int TYPE_SUCXCSS = 0;
    protected static final int TYPE_FAILED = 1;
    protected static final int TYPE_PAUSED = 2;
    protected static final int TYPE_CANCELED = 3;

    //这个监听在服务中重写，因为在服务中启动任务，可以顺便初始化DownLoadTask（构造函数）
    protected DownloadService.DownLoadListener mListener;

    protected boolean isPaused = false;//下载是否暂停标记
    protected int lastProgress;//进度条上次更新时的大小
    protected boolean isCancelled = false;

    abstract public String getFileName();

    abstract public String getFolderPath();

    public void setListener(DownloadService.DownLoadListener mListener) {
        this.mListener = mListener;
    }

    public void pauseDownload() {
        //只需修改状态标记
        isPaused = true;
    }

    public void cancelDownload() {
        //只需修改状态标记
        isCancelled = true;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
//        final int progress = values[0];
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                if (lastProgress < progress + 1000) {
//                    mListener.onProgress(progress);
//                    lastProgress = progress;
//                }
//            }
//        }).start();
//


    }

    @Override
    protected Integer doInBackground(String... strings) {
        return null;
    }
}
