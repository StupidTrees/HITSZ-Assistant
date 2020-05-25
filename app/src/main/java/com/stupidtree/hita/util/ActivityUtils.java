package com.stupidtree.hita.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityOptionsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivityAttitude;
import com.stupidtree.hita.activities.ActivityDDLManager;
import com.stupidtree.hita.activities.ActivityEmptyClassroomDetail;
import com.stupidtree.hita.activities.ActivityExamCountdown;
import com.stupidtree.hita.activities.ActivityJWTS;
import com.stupidtree.hita.activities.ActivityLocation;
import com.stupidtree.hita.activities.ActivityLogin;
import com.stupidtree.hita.activities.ActivityLoginJWTS;
import com.stupidtree.hita.activities.ActivityNewsDetail;
import com.stupidtree.hita.activities.ActivityPhotoDetail;
import com.stupidtree.hita.activities.ActivitySearch;
import com.stupidtree.hita.activities.ActivitySetting;
import com.stupidtree.hita.activities.ActivitySubject;
import com.stupidtree.hita.activities.ActivitySubjectJW;
import com.stupidtree.hita.activities.ActivityTasks;
import com.stupidtree.hita.activities.ActivityTeacher;
import com.stupidtree.hita.activities.ActivityTeacherOfficial;
import com.stupidtree.hita.activities.ActivityUserCenter;
import com.stupidtree.hita.activities.ActivityUserProfile;
import com.stupidtree.hita.activities.BaseActivity;
import com.stupidtree.hita.community.ActivityCommunity;
import com.stupidtree.hita.community.ActivityCreatePost;
import com.stupidtree.hita.community.ActivityOneTopic;
import com.stupidtree.hita.community.ActivityOneUserPostList;
import com.stupidtree.hita.online.Canteen;
import com.stupidtree.hita.online.Classroom;
import com.stupidtree.hita.online.Dormitory;
import com.stupidtree.hita.online.Facility;
import com.stupidtree.hita.online.HITAUser;
import com.stupidtree.hita.online.Location;
import com.stupidtree.hita.online.Scenery;
import com.stupidtree.hita.online.Teacher;
import com.stupidtree.hita.online.Topic;
import com.stupidtree.hita.timetable.TimetableCore;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.TPE;
import static com.stupidtree.hita.HITAApplication.jwCore;
import static com.stupidtree.hita.community.ActivityCommunity.REFRESH_RETURN;

public class ActivityUtils {

    static HashMap<View, String> imageViewToUrl = new HashMap<>();

    public static void downloadFile(Activity context, String url, String name) {
        String fileName = new File(context.getFilesDir(), name).getAbsolutePath();
        //文件下载链接
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        // 通知栏的下载通知
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setTitle(fileName);
        request.setMimeType("application/vnd.android.package-archive");
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName);
        if (file.exists()) {
            file.delete();
        }
        request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName);
        long downloadId = downloadManager.enqueue(request);
        Log.d("TAG", "downloadId:" + downloadId);
        //文件下载完成会发送完成广播，可注册广播进行监听
//        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
//        intentFilter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
//        intentFilter.addAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
//        mDownloadBroadcast = new DownloadBroadcast(file);
//        registerReceiver(mDownloadBroadcast, intentFilter);

    }

    public static void startTasksActivity(Context from) {
        if (TimetableCore.getInstance(HContext).isDataAvailable()) {
            Intent i = new Intent(from, ActivityTasks.class);
            from.startActivity(i);
        } else {
            Toast.makeText(from, from.getString(R.string.notif_importdatafirst), Toast.LENGTH_SHORT).show();
        }
    }

    public static void startDDLManagerActivity(Context from) {
        if (TimetableCore.getInstance(HContext).isDataAvailable()) {
            Intent i = new Intent(from, ActivityDDLManager.class);
            from.startActivity(i);
        } else {
            Toast.makeText(from, from.getString(R.string.notif_importdatafirst), Toast.LENGTH_SHORT).show();
        }
    }

    public static void startExamCDActivity(Context from) {
        if (TimetableCore.getInstance(HContext).isDataAvailable()) {
            Intent i = new Intent(from, ActivityExamCountdown.class);
            from.startActivity(i);
        } else {
            Toast.makeText(from, from.getString(R.string.notif_importdatafirst), Toast.LENGTH_SHORT).show();
        }
    }

    public static void startAttitudeActivity(final Context from) {
        Intent k;
        if (CurrentUser != null) {
            k = new Intent(from, ActivityAttitude.class);
            from.startActivity(k);
        } else if (CurrentUser == null) {
            AlertDialog ad = new AlertDialog.Builder(from).setTitle(R.string.attention).setMessage(R.string.log_in_first).setPositiveButton(R.string.button_confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent(from, ActivityLogin.class);
                    from.startActivity(i);
                }
            }).create();
            ad.show();
        }

    }

    public static void startCommunityActivity(final Context from) {
        Intent k;
        if (CurrentUser != null) {
            k = new Intent(from, ActivityCommunity.class);
            from.startActivity(k);
        } else if (CurrentUser == null) {
            AlertDialog ad = new AlertDialog.Builder(from).setTitle(R.string.attention).setMessage(R.string.log_in_first).setPositiveButton(R.string.button_confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent(from, ActivityLogin.class);
                    from.startActivity(i);
                }
            }).create();
            ad.show();
        }

    }

    public static void startOneUserPostsActivity(final Context from, HITAUser user) {
        if (CurrentUser == null) {
            AlertDialog ad = new AlertDialog.Builder(from).setTitle(R.string.attention).setMessage(R.string.log_in_first).setPositiveButton(R.string.button_confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent(from, ActivityLogin.class);
                    from.startActivity(i);
                }
            }).create();
            ad.show();
        } else {
            Intent i = new Intent(from, ActivityOneUserPostList.class);
            i.putExtra("user", user);
            if (from instanceof ActivityCommunity || from instanceof ActivityOneTopic) {
                ((BaseActivity) from).startActivityForResult(i, REFRESH_RETURN);
            } else {
                from.startActivity(i);
            }
        }

    }

    public static void startCreatePostActivity(Context from, Topic initTopic) {
        Intent i = new Intent(from, ActivityCreatePost.class);
        i.putExtra("topic", initTopic);
        if (from instanceof ActivityCommunity || from instanceof ActivityOneTopic) {
            ((BaseActivity) from).startActivityForResult(i, REFRESH_RETURN);
        } else {
            from.startActivity(i);
        }
    }

    public static void startPostDetail(Context from, String id) {
        Intent i = new Intent(from, com.stupidtree.hita.community.ActivityPostDetail.class);
        i.putExtra("id", id);
        if (from instanceof ActivityCommunity || from instanceof ActivityOneTopic || from instanceof ActivityOneUserPostList) {
            ((Activity) from).startActivityForResult(i, REFRESH_RETURN);
        } else from.startActivity(i);
    }

    public static void startSettingFor(Context from, String target) {
        Intent i = new Intent(from, ActivitySetting.class);
        i.putExtra("target", target);
        from.startActivity(i);
    }

    public static void startJWSubjectActivity(Context from, String subjectId) {
        Intent i = new Intent(from, ActivitySubjectJW.class);
        i.putExtra("subject_id", subjectId);
        from.startActivity(i);
    }

    public static void search(Context from, String keyword) {
        Intent i = new Intent(from, ActivitySearch.class);
        i.putExtra("keyword", keyword);
        from.startActivity(i);
    }

    public static void searchFor(Context from, String keyword, String type) {
        Intent i = new Intent(from, ActivitySearch.class);
        i.putExtra("keyword", keyword);
        i.putExtra("type", type);
        from.startActivity(i);
    }

    public static void openInBrowser(Context from, String link) {
        Uri uri = Uri.parse(link);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        from.startActivity(intent);
    }

    public static void startOfficialTeacherActivity_transition(Activity from, String id, String url, String name, View transition) {
        Intent i = new Intent(from, ActivityTeacherOfficial.class);
        i.putExtra("id", id);
        i.putExtra("url", url);
        i.putExtra("name", name);
        transition.setTransitionName("image");
        ActivityOptionsCompat ip = ActivityOptionsCompat.makeSceneTransitionAnimation(from, transition, "image");
        from.startActivity(i, ip.toBundle());
    }

    public static void startOfficialTeacherActivity(Context from, String id, String url, String name) {
        Intent i = new Intent(from, ActivityTeacherOfficial.class);
        i.putExtra("id", id);
        i.putExtra("url", url);
        i.putExtra("name", name);
        from.startActivity(i);
    }

    public static void startNewsActivity(Context from, String url, String title) {
        Intent i = new Intent(from, ActivityNewsDetail.class);
        i.putExtra("link", url);
        i.putExtra("title", title);
        i.putExtra("mode", "hitsz_news");
        from.startActivity(i);
    }

    public static void startZSWActivity(Context from, String title, String url) {
        Intent i = new Intent(from, ActivityNewsDetail.class);
        i.putExtra("link", url);
        i.putExtra("title", title);
        i.putExtra("mode", "zsw_news");
        from.startActivity(i);
    }


    public static void showMultipleImages(final BaseActivity from, final List<String> urls, final int index) {
        Intent it = new Intent(from, ActivityPhotoDetail.class);
        String[] urlsArr = new String[urls.size()];
        for (int i = 0; i < urlsArr.length; i++) urlsArr[i] = urls.get(i);
        it.putExtra("urls", urlsArr);
        it.putExtra("init_index", index);
        from.startActivity(it);
    }

    @SuppressLint("CheckResult")
    public static void DownloadImage(final Context context, final String url, final OnDownloadDoneListener listener) {
        File f = new File(url);
        String name = f.getName();

        final String path = Environment.getExternalStorageDirectory().getPath() + "/HITA/saved_image/" + name;
        Glide.with(context).asBitmap()
                .load(url).into(new SimpleTarget<Bitmap>() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] objects) {
                        try {
                            FileOperator.saveByteImageToFile((String) objects[1], (Bitmap) objects[0]);
                        } catch (Exception e) {

                        }

                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object o) {
                        super.onPostExecute(o);
                        listener.onDone();
                    }
                }.executeOnExecutor(TPE, resource, path);
                //FileOperator.sabeBitmapToFile(resource,context.getExternalFilesDir("saved_images")+"/test.jpg");
            }
        });
    }

    public static void showOneImage(final BaseActivity from, final String url) {
        Intent it = new Intent(from, ActivityPhotoDetail.class);
        String[] urlsArr = new String[1];
        urlsArr[0] = url;
        it.putExtra("urls", urlsArr);
        it.putExtra("init_index", 0);
        from.startActivity(it);
    }

    public static void startExploreActivity_forNavi(Activity from, String terminal, double longitude, double latitude) {

        Toast.makeText(from, "导航功能将在20迎新版中回归", Toast.LENGTH_SHORT).show();
//        Intent i = new Intent(from, ActivityExplore.class);
//        i.putExtra("longitude", longitude);
//        i.putExtra("latitude", latitude);
//        i.putExtra("terminal", terminal);
//        from.startActivity(i);
    }

    public static void startSubjectActivity_name(Context from, String name) {
        Intent i = new Intent(from, ActivitySubject.class);
        i.putExtra("useCode", false);
        i.putExtra("subject", name);
        from.startActivity(i);
    }

    public static void startSubjectActivity_code(Context from, String code) {
        Intent i = new Intent(from, ActivitySubject.class);
        i.putExtra("useCode", true);
        i.putExtra("subject", code);
        from.startActivity(i);
    }

    public static void startLocationActivity(Context from, Location c) {
        if (c.getType().equals("canteen") && !(c instanceof Canteen)) {
            c = new Canteen(c);
        } else if (c.getType().equals("scenery") && !(c instanceof Scenery)) {
            c = new Scenery(c);
        } else if (c.getType().equals("classroom") && !(c instanceof Classroom)) {
            c = new Classroom(c);
        } else if (c.getType().equals("dormitory") && !(c instanceof Dormitory)) {
            c = new Dormitory(c);
        } else if (c.getType().equals("facility") && !(c instanceof Facility)) {
            c = new Facility(c);
        }
        Intent i = new Intent(from, ActivityLocation.class);
        i.putExtra("location", c);
        from.startActivity(i);
    }

    public static void startLocationActivity_transition_image(Activity from, ImageView transition, Location c) {
        Intent i = new Intent(from, ActivityLocation.class);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(from, transition, "image");
        i.putExtra("location", c);
        i.putExtra("circle_reveal_image", false);
        from.startActivity(i, options.toBundle());
    }

    public static void startLocationActivity_name(Context from, String name) {
        Intent i = new Intent(from, ActivityLocation.class);
        i.putExtra("name", name);
        from.startActivity(i);
    }

    public static void startLocationActivity_objectId(Activity from, String id) {
        Intent i = new Intent(from, ActivityLocation.class);
        i.putExtra("objectId", id);
        from.startActivity(i);
    }

    public static void startExploreActivity_forNavi(Activity from, String terminal) {
        Toast.makeText(from, "导航功能将在20迎新版中回归", Toast.LENGTH_SHORT).show();
//        Intent i = new Intent(from, ActivityExplore.class);
//        i.putExtra("terminal", terminal);
//        from.startActivity(i);
    }

    public static void startUserProfileActivity(final Activity from, String objectId, View sharedAvatar) {
        if (CurrentUser == null) {
            AlertDialog ad = new AlertDialog.Builder(from).setTitle(R.string.attention).setMessage(R.string.log_in_first).setPositiveButton(R.string.button_confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent(from, ActivityLogin.class);
                    from.startActivity(i);
                }
            }).create();
            ad.show();
            return;
        }
        ActivityOptionsCompat op = ActivityOptionsCompat.makeSceneTransitionAnimation(from, sharedAvatar, "useravatar");
        Intent i;
        if (CurrentUser != null && objectId.equals(CurrentUser.getObjectId())) {
            i = new Intent(from, ActivityUserCenter.class);
        } else {
            i = new Intent(from, ActivityUserProfile.class);
            i.putExtra("objectId", objectId);
        }
        from.startActivity(i, op.toBundle());
    }

    public static void startUserProfileActivity(Activity from, HITAUser user, View sharedAvatar) {
        Intent i;
        if (CurrentUser != null && user.getObjectId().equals(CurrentUser.getObjectId())) {
            i = new Intent(from, ActivityUserCenter.class);
            from.startActivity(i);
        } else {
            if (user == null) return;
            String id = user.getObjectId();
            startUserProfileActivity(from, id, sharedAvatar);
//            i = new Intent(from, ActivityUserProfile.class);
//            Bundle b = new Bundle();
//            b.putSerializable("user", user);
//            i.putExtras(b);
        }
    }

    public static void startTopicPageActivity(final Context from, Topic topic) {
        if (CurrentUser == null) {
            AlertDialog ad = new AlertDialog.Builder(from).setTitle(R.string.attention).setMessage(R.string.log_in_first).setPositiveButton(R.string.button_confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent(from, ActivityLogin.class);
                    from.startActivity(i);
                }
            }).create();
            ad.show();
        } else {
            Intent i = new Intent(from, ActivityOneTopic.class);
            i.putExtra("topic", topic);
            if (from instanceof ActivityCommunity) {
                ((Activity) from).startActivityForResult(i, REFRESH_RETURN);
            } else from.startActivity(i);
        }

    }

    public static void startUserProfileActivity(final Activity from, HITAUser user) {

        if (CurrentUser == null) {
            AlertDialog ad = new AlertDialog.Builder(from).setTitle(R.string.attention).setMessage(R.string.log_in_first).setPositiveButton(R.string.button_confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent(from, ActivityLogin.class);
                    from.startActivity(i);
                }
            }).create();
            ad.show();
            return;
        }
        Intent i = null;
        if (user.getObjectId().equals(CurrentUser.getObjectId())) {
            i = new Intent(from, ActivityUserCenter.class);
        } else {
            i = new Intent(from, ActivityUserProfile.class);
//            Bundle b = new Bundle();
//            b.putSerializable("user", user);
//            i.putExtras(b);
            i.putExtra("objectId", user.getObjectId());
        }
        from.startActivity(i);

    }

    public static void startJWTSActivity(final Context from) {
        Intent k;
        if (jwCore.hasLogin()) {
            k = new Intent(HContext, ActivityJWTS.class);
            from.startActivity(k);
        } else {
            if (CurrentUser == null) {
                AlertDialog ad = new AlertDialog.Builder(from).setTitle(R.string.attention).setMessage(R.string.log_in_first).setPositiveButton(R.string.button_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(from, ActivityLogin.class);
                        from.startActivity(i);
                    }
                }).create();
                ad.show();
            } else if (CurrentUser.getStudentnumber() == null || CurrentUser.getStudentnumber().isEmpty()) {

                AlertDialog ad = new AlertDialog.Builder(from).setTitle(R.string.attention).setMessage(from.getString(R.string.verify_id_first)).setPositiveButton(R.string.button_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(from, ActivityUserCenter.class);
                        from.startActivity(i);
                    }
                }).create();
                ad.show();
            } else {
                k = new Intent(HContext, ActivityLoginJWTS.class);
                from.startActivity(k);
            }
        }
    }

    public static void startUTActivity(final Context from) {
//        Intent k;
//        if (login_ut && ut_username != null) {
//            k = new Intent(from, ActivityUTService.class);
//            k.putExtra("username", ut_username);
//            from.startActivity(k);
//        } else {
//            if (CurrentUser == null) {
//                AlertDialog ad = new AlertDialog.Builder(from).setTitle("提示").setMessage("请先登录HITSZ助手账号并绑定学号！").setPositiveButton("好的", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        Intent i = new Intent(from, ActivityLogin.class);
//                        from.startActivity(i);
//                    }
//                }).create();
//                ad.show();
//            } else if (CurrentUser.getStudentnumber() == null || CurrentUser.getStudentnumber().isEmpty()) {
//
//                AlertDialog ad = new AlertDialog.Builder(from).setTitle("提示").setMessage("请先绑定学号后再使用大学城服务").setPositiveButton("好的", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        Intent i = new Intent(from, ActivityUserCenter.class);
//                        from.startActivity(i);
//                    }
//                }).create();
//                ad.show();
//            } else {
//                k = new Intent(HContext, ActivityLoginUT.class);
//                from.startActivity(k);
//            }
//        }
    }

    public interface OnDownloadDoneListener {
        void onDone();
    }


    public static void startTeacherActivity(Activity from, String name) {
        Intent i = new Intent(from, ActivityTeacher.class);
        i.putExtra("name", name);
        from.startActivity(i);
    }

    public static void startTeacherActivity(Context from, Teacher t) {
        Intent i = new Intent(from, ActivityTeacher.class);
        Bundle b = new Bundle();
        b.putSerializable("teacher", t);
        i.putExtras(b);
        from.startActivity(i);
    }

    public static void startEmptyClassroomDetailActivity(Activity from, String name, String xnxq, String lh, String cd) {
        Intent i = new Intent(from, ActivityEmptyClassroomDetail.class);
        i.putExtra("name", name);
        i.putExtra("xnxq", xnxq);
        i.putExtra("lh", lh);
        i.putExtra("cd", cd);
        from.startActivity(i);
    }
}
