//package com.stupidtree.hita.diy;
//
//import android.content.Context;
//import android.widget.ImageView;
//
//import com.bumptech.glide.Glide;
//import com.bumptech.glide.load.resource.bitmap.CenterCrop;
//import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
//import com.bumptech.glide.request.RequestOptions;
//import com.youth.banner.loader.ImageLoader;
//
//import static com.stupidtree.hita.HITAApplication.HContext;
//
//public class GlideImageLoader extends ImageLoader { //用在banner上的自定义图片加载
//
//
//    CornerTransform transformation;
//    public GlideImageLoader(){
//        transformation = new CornerTransform(HContext, dip2px(HContext, 10));
//        transformation.setExceptCorner(false, false, false, false);
//
//    }
//    public static int dip2px(Context context, float dpValue) {
//        final float scale = context.getResources().getDisplayMetrics().density;
//        return (int) (dpValue * scale + 0.5f);
//    }
//    @Override
//    public void displayImage(Context context, Object path, ImageView imageView) {
//        /**
//         注意：
//         1.图片加载器由自己选择，这里不限制，只是提供几种使用方法
//         2.返回的图片路径为Object类型，由于不能确定你到底使用的那种图片加载器，
//         传输的到的是什么格式，那么这种就使用Object接收和返回，你只需要强转成你传输的类型就行，
//         切记不要胡乱强转！
//         */
//
//        //Glide 加载图片简单用法
//        Glide.with(context).load(path)
//                .apply(RequestOptions.bitmapTransform(transformation))
//                .into(imageView);
//
//    }
//
//}