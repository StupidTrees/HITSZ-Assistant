package com.stupidtree.hita.diy;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.widget.ImageView;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class mImageLoader {

    public  void loadImage(final String path, final ImageView image) {
        //如果缓存过就会从缓存中取出图像，ImageCallback接口中方法也不会被执行
        Bitmap cacheImage =loadBitMap(path, new ImageCallback() {
            //如果第一次加载图片
            @Override
            public void imageLoad(Bitmap bitmap) {
                image.setImageBitmap(bitmap);
            }
        });
        if (cacheImage != null) {
            image.setImageBitmap(cacheImage);
        }
    }


    private Map<String, SoftReference<Bitmap>> imageCache = new HashMap<String, SoftReference<Bitmap>>();    //缓存
    private ExecutorService executorService = Executors.newFixedThreadPool(5);    //固定五个线程来执行任务
    private final Handler handler = new Handler();
    private Bitmap loadBitMap(final String imgPath, final ImageCallback imageCallback) {
        //如果缓存过就从缓存中取出数据
        if (imageCache.containsKey(imgPath)) {
            SoftReference<Bitmap> softReference = imageCache.get(imgPath);
            if (softReference.get() != null) {
                return softReference.get();
            }
        }
        //缓存中没有图像，则从网络上取出数据，并将取出的数据缓存到内存中
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    final Bitmap bitmap = decodeBitmapFromPath(imgPath, 120, 160);
                    imageCache.put(imgPath, new SoftReference<Bitmap>(bitmap));
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            imageCallback.imageLoad(bitmap);
                        }
                    });
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        return null;
    }
    //回调函数，在调用者中用来跟新ui
    public interface ImageCallback {
        public void imageLoad(Bitmap bitmap);
    }
    //压缩图片
    public static Bitmap decodeBitmapFromPath(String path,
                                              int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // 原图的宽高
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

}
