package com.stupidtree.hita.util;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.appcompat.app.AlertDialog;

import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.stupidtree.hita.activities.ActivityEmptyClassroomDetail;
import com.stupidtree.hita.activities.ActivityExplore;
import com.stupidtree.hita.activities.ActivityJWTS;
import com.stupidtree.hita.activities.ActivityLocation;
import com.stupidtree.hita.activities.ActivityLogin;
import com.stupidtree.hita.activities.ActivityLoginJWTS;
import com.stupidtree.hita.activities.ActivityNewsDetail;
import com.stupidtree.hita.activities.ActivityPhotoDetail;
import com.stupidtree.hita.activities.ActivityPostDetail;
import com.stupidtree.hita.activities.ActivitySearch;
import com.stupidtree.hita.activities.ActivitySetting;
import com.stupidtree.hita.activities.ActivitySubject;
import com.stupidtree.hita.activities.ActivitySubjectJW;
import com.stupidtree.hita.activities.ActivityTeacher;
import com.stupidtree.hita.activities.ActivityTeacherOfficial;
import com.stupidtree.hita.activities.ActivityUserCenter;
import com.stupidtree.hita.activities.ActivityUserProfile;
import com.stupidtree.hita.fragments.news.FragmentNewsBulletin;
import com.stupidtree.hita.online.Canteen;
import com.stupidtree.hita.online.Classroom;
import com.stupidtree.hita.online.Dormitory;
import com.stupidtree.hita.online.Facility;
import com.stupidtree.hita.online.HITAUser;
import com.stupidtree.hita.online.Location;
import com.stupidtree.hita.online.LostAndFound;
import com.stupidtree.hita.online.Scenery;
import com.stupidtree.hita.online.Teacher;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.jwCore;
import java.io.File;
public class ActivityUtils {

    public static void downloadFile(Activity context,String url,String name) {
        String fileName = new File(context.getFilesDir(),name).getAbsolutePath();
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


    public static void startSettingFor(Context from,String target){
        Intent i = new Intent(from, ActivitySetting.class);
        i.putExtra("target",target);
        from.startActivity(i);
    }
    public static void startJWSubjectActivity(Context from, String subjectId) {
        Intent i = new Intent(from, ActivitySubjectJW.class);
        i.putExtra("subject_id", subjectId);
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

    public static void startPhotoDetailActivity_transition(Activity from, String imageurl, View transition) {
        Intent i = new Intent(from, ActivityPhotoDetail.class);
        i.putExtra("imagePath", imageurl);
        transition.setTransitionName("image");
        ActivityOptionsCompat ip = ActivityOptionsCompat.makeSceneTransitionAnimation(from);
        from.startActivity(i, ip.toBundle());
    }

    public static void startPhotoDetailActivity(Activity from, String imageurl) {
        Intent i = new Intent(from, ActivityPhotoDetail.class);
        i.putExtra("imagePath", imageurl);
        from.startActivity(i);
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

    public static void startExploreActivity_forNavi(Activity from, String terminal, double longitude, double latitude) {

        Intent i = new Intent(from, ActivityExplore.class);
        i.putExtra("longitude", longitude);
        i.putExtra("latitude", latitude);
        i.putExtra("terminal", terminal);
        from.startActivity(i);
    }

    public static void startExploreActivity_forNavi(Activity from, String terminal) {
        Intent i = new Intent(from, ActivityExplore.class);
        i.putExtra("terminal", terminal);
        from.startActivity(i);
    }

    public static void startUserProfileActivity(Activity from, String objectId, View sharedAvatar) {
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

    public static void startJWTSActivity(final Context from) {
        Intent k;
        if (jwCore.hasLogin()) {
            k = new Intent(HContext, ActivityJWTS.class);
            from.startActivity(k);
        } else {
            if (CurrentUser == null) {
                AlertDialog ad = new AlertDialog.Builder(from).setTitle("提示").setMessage("请先登录HITSZ助手账号并绑定学号！").setPositiveButton("好的", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(from, ActivityLogin.class);
                        from.startActivity(i);
                    }
                }).create();
                ad.show();
            } else if (CurrentUser.getStudentnumber() == null || CurrentUser.getStudentnumber().isEmpty()) {

                AlertDialog ad = new AlertDialog.Builder(from).setTitle("提示").setMessage("请先绑定学号后再使用教务系统").setPositiveButton("好的", new DialogInterface.OnClickListener() {
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

    public static void startJWTSActivity_forPage(final Context from, int page) {
        Intent k;
        if (jwCore.hasLogin()) {
            k = new Intent(HContext, ActivityJWTS.class);
            k.putExtra("terminal", page + "");
            from.startActivity(k);
        } else {
            if (CurrentUser == null) {
                AlertDialog ad = new AlertDialog.Builder(from).setTitle("提示").setMessage("请先登录HITSZ助手账号并绑定学号！").setPositiveButton("好的", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(from, ActivityLogin.class);
                        from.startActivity(i);
                    }
                }).create();
                ad.show();
            } else if (CurrentUser.getStudentnumber() == null || CurrentUser.getStudentnumber().isEmpty()) {

                AlertDialog ad = new AlertDialog.Builder(from).setTitle("提示").setMessage("请先绑定学号后再使用教务系统").setPositiveButton("好的", new DialogInterface.OnClickListener() {
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

    public static void startPostDetailActivity(Activity from, LostAndFound laf, HITAUser author) {
        Intent i = new Intent(from, ActivityPostDetail.class);
        i.putExtra("laf", laf);
        i.putExtra("author", author);
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
