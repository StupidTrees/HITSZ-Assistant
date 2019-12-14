package com.stupidtree.hita.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.core.app.ActivityOptionsCompat;
import androidx.appcompat.app.AlertDialog;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.stupidtree.hita.activities.ActivityEmptyClassroomDetail;
import com.stupidtree.hita.activities.ActivityExplore;
import com.stupidtree.hita.activities.ActivityJWTS;
import com.stupidtree.hita.activities.ActivityLocation;
import com.stupidtree.hita.activities.ActivityLogin;
import com.stupidtree.hita.activities.ActivityLoginJWTS;
import com.stupidtree.hita.activities.ActivityLoginUT;
import com.stupidtree.hita.activities.ActivityPhotoDetail;
import com.stupidtree.hita.activities.ActivityPostDetail;
import com.stupidtree.hita.activities.ActivitySubject;
import com.stupidtree.hita.activities.ActivityTeacher;
import com.stupidtree.hita.activities.ActivityUTService;
import com.stupidtree.hita.activities.ActivityUserCenter;
import com.stupidtree.hita.activities.ActivityUserProfile;
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
import static com.stupidtree.hita.HITAApplication.login_jwts;
import static com.stupidtree.hita.HITAApplication.login_ut;
import static com.stupidtree.hita.HITAApplication.ut_username;

public class ActivityUtils {

    public static void startPhotoDetailActivity_transition(Activity from,String imageurl, View transition){
        Intent i = new Intent(from, ActivityPhotoDetail.class);
        i.putExtra("imagePath",imageurl);
        transition.setTransitionName("image");
        ActivityOptionsCompat ip = ActivityOptionsCompat.makeSceneTransitionAnimation(from);
        from.startActivity(i,ip.toBundle());
    }
    public static void startSubjectActivity_name(Context from,String name){
        Intent i = new Intent(from, ActivitySubject.class);
        i.putExtra("useCode",false);
        i.putExtra("subject",name);
        from.startActivity(i);
    }
    public static void startSubjectActivity_code(Context from,String code){
        Intent i = new Intent(from, ActivitySubject.class);
        i.putExtra("useCode",true);
        i.putExtra("subject",code);
        from.startActivity(i);
    }
    public static void   startLocationActivity(Context from, Location c) {
        if(c.getType().equals("canteen")&&!(c instanceof Canteen)){
            c = new Canteen(c);
        }else if(c.getType().equals("scenery")&&!(c instanceof Scenery)){
            c = new Scenery(c);
        }else if(c.getType().equals("classroom")&&!(c instanceof Classroom)){
            c = new Classroom(c);
        }else if(c.getType().equals("dormitory")&&!(c instanceof Dormitory)){
            c = new Dormitory(c);
        }else if(c.getType().equals("facility")&&!(c instanceof Facility)){
            c = new Facility(c);
        }
        Intent i = new Intent(from, ActivityLocation.class);
        i.putExtra("location",c);
        from.startActivity(i);
    }

    public static void startLocationActivity_transition_image(Activity from, ImageView transition, Location c) {
        Intent i = new Intent(from, ActivityLocation.class);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(from,transition,"image");
        i.putExtra("location",c);
        i.putExtra("circle_reveal_image",false);
        from.startActivity(i,options.toBundle());
    }
    public static void startLocationActivity_name(Context from, String name) {
        Intent i = new Intent(from, ActivityLocation.class);
        i.putExtra("name",name);
        from.startActivity(i);
    }
    public static void startLocationActivity_objectId(Activity from, String id) {
        Intent i = new Intent(from, ActivityLocation.class);
        i.putExtra("objectId",id);
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
        if (CurrentUser != null && objectId .equals( CurrentUser.getObjectId())) {
            i = new Intent(from, ActivityUserCenter.class);
        } else {
            i = new Intent(from, ActivityUserProfile.class);
            i.putExtra("objectId", objectId);
        }
        from.startActivity(i, op.toBundle());
    }

    public static void startUTActivity(final Context from){
        Intent k;
        if(login_ut&&ut_username!=null){
            k = new Intent(from, ActivityUTService.class);
            k.putExtra("username",ut_username);
            from.startActivity(k);
        } else{
            if(CurrentUser==null){
                AlertDialog ad = new AlertDialog.Builder(from).setTitle("提示").setMessage("请先登录HITSZ助手账号并绑定学号！").setPositiveButton("好的", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(from, ActivityLogin.class);
                        from.startActivity(i);
                    }
                }).create();
                ad.show();
            }else if(CurrentUser.getStudentnumber()==null||CurrentUser.getStudentnumber().isEmpty()){

                AlertDialog ad = new AlertDialog.Builder(from).setTitle("提示").setMessage("请先绑定学号后再使用大学城服务").setPositiveButton("好的", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(from,ActivityUserCenter.class);
                        from.startActivity(i);
                    }
                }).create();
                ad.show();
            }else{
                k = new Intent(HContext, ActivityLoginUT.class);
                from.startActivity(k);
            }
        }
    }
    public static void startJWTSActivity(final Context from){
        Intent k;
        if(login_jwts){
            k = new Intent(HContext, ActivityJWTS.class);
            from.startActivity(k);
        } else{
            if(CurrentUser==null){
                AlertDialog ad = new AlertDialog.Builder(from).setTitle("提示").setMessage("请先登录HITSZ助手账号并绑定学号！").setPositiveButton("好的", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(from, ActivityLogin.class);
                        from.startActivity(i);
                    }
                }).create();
                ad.show();
            }else if(CurrentUser.getStudentnumber()==null||CurrentUser.getStudentnumber().isEmpty()){

                AlertDialog ad = new AlertDialog.Builder(from).setTitle("提示").setMessage("请先绑定学号后再使用教务系统").setPositiveButton("好的", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(from,ActivityUserCenter.class);
                        from.startActivity(i);
                    }
                }).create();
                ad.show();
            }else{
                k = new Intent(HContext, ActivityLoginJWTS.class);
                from.startActivity(k);
            }
        }
    }
    public static void startJWTSActivity_forPage(final Context from,int page){
        Intent k;
        if(login_jwts){
            k = new Intent(HContext, ActivityJWTS.class);
            k.putExtra("terminal",page+"");
            from.startActivity(k);
        } else{
            if(CurrentUser==null){
                AlertDialog ad = new AlertDialog.Builder(from).setTitle("提示").setMessage("请先登录HITSZ助手账号并绑定学号！").setPositiveButton("好的", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(from, ActivityLogin.class);
                        from.startActivity(i);
                    }
                }).create();
                ad.show();
            }else if(CurrentUser.getStudentnumber()==null||CurrentUser.getStudentnumber().isEmpty()){

                AlertDialog ad = new AlertDialog.Builder(from).setTitle("提示").setMessage("请先绑定学号后再使用教务系统").setPositiveButton("好的", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(from,ActivityUserCenter.class);
                        from.startActivity(i);
                    }
                }).create();
                ad.show();
            }else{
                k = new Intent(HContext, ActivityLoginJWTS.class);

                from.startActivity(k);
            }
        }
    }

    public static void startTeacherActivity(Activity from,String name){
        Intent i = new Intent(from, ActivityTeacher.class);
        i.putExtra("name",name);
        from.startActivity(i);
    }
    public static void startTeacherActivity(Context from, Teacher t){
        Intent i = new Intent(from, ActivityTeacher.class);
        Bundle b = new Bundle();
        b.putSerializable("teacher",t);
        i.putExtras(b);
        from.startActivity(i);
    }
    public static void startPostDetailActivity(Activity from, LostAndFound laf, HITAUser author){
        Intent i = new Intent(from, ActivityPostDetail.class);
        i.putExtra("laf",laf);
        i.putExtra("author",author);
        from.startActivity(i);
    }

    public static void startEmptyClassroomDetailActivity(Activity from,String name,String xnxq,String lh,String cd){
        Intent i = new Intent(from, ActivityEmptyClassroomDetail.class);
        i.putExtra("name",name);
        i.putExtra("xnxq",xnxq);
        i.putExtra("lh",lh);
        i.putExtra("cd",cd);
        from.startActivity(i);
    }
}
