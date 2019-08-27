package com.stupidtree.hita;

import android.app.Activity;
import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;

import com.stupidtree.hita.core.Curriculum;
import com.stupidtree.hita.core.CurriculumHelper;
import com.stupidtree.hita.core.HITADBHelper;
import com.stupidtree.hita.core.timetable.Task;
import com.stupidtree.hita.online.Bmob_User_Data;
import com.stupidtree.hita.online.TimeTable_upload_helper;
import com.stupidtree.hita.online.HITAUser;
import com.stupidtree.hita.core.Subject;
import com.stupidtree.hita.core.TimeTable;
import com.stupidtree.hita.hita.ChatBotMessageItem;
import com.stupidtree.hita.activities.ActivityMain;
import com.stupidtree.hita.util.FileOperator;
import com.stupidtree.hita.util.mUpgradeListener;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.beta.upgrade.UpgradeStateListener;

import org.ansj.splitWord.analysis.ToAnalysis;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * 全局Application类，生命周期和整个应用相同
 */
public class HITAApplication extends Application {

    //一个全局的Context变量
    public static Context HContext;
    public static TimeWatcher timeWatcher;
    public static int themeID;

    /*重要变量*/
    public static Calendar now;
    public static boolean isThisTerm = true;
    public static int thisWeekOfTerm = -1;
    /*核心的变量*/
    public static ArrayList<Curriculum> allCurriculum;
    public static HashMap<String, String> cookies = null;
    public static boolean login = false;
    public static TimeTable mainTimeTable;
    public static SharedPreferences defaultSP;
    public static int thisCurriculumIndex;
    /*刻画数据状态的标志常量*/
    public static int DATA_STATE_NULL = 13;
    public static int DATA_STATE_NONE_CURRICULUM = 14;
    public static int DATA_STATE_GET_ERROR = 16;
    public static int DATA_STATE_HEALTHY = 17;

    public static List<ChatBotMessageItem> ChatBotListRes;//聊天机器人的聊天记录
    public static HITAUser CurrentUser = null;
    public static HITADBHelper mDBHelper;


    @Override
    public void onCreate() {
        super.onCreate();
        now = Calendar.getInstance();
        HContext = getApplicationContext();
        defaultSP = PreferenceManager.getDefaultSharedPreferences(this);
        mDBHelper = new HITADBHelper(HContext);
        allCurriculum = new ArrayList<>();
        ChatBotListRes = new ArrayList<>();
        timeWatcher = new TimeWatcher(this);
        mainTimeTable = new TimeTable(null);
        initUpgradeDialog();
        Bugly.init(this, "7c0e87536a", false);//务必最后再init
        Bmob.initialize(this, "9c9c53cd53b3c7f02c37b7a3e6fd9145");
        CurrentUser = BmobUser.getCurrentUser(HITAUser.class);
        getThemeID();
    }

    public boolean copyAssetsSingleFile(File file, String fileName) {
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.e("--Method--", "copyAssetsSingleFile: cannot create directory.");
                return false;
            }
        }
        try {
            InputStream inputStream = getAssets().open(fileName);
            File outFile = new File(file, fileName);
            FileOutputStream fileOutputStream = new FileOutputStream(outFile);
            // Transfer bytes from inputStream to fileOutputStream
            byte[] buffer = new byte[1024];
            int byteRead;
            while (-1 != (byteRead = inputStream.read(buffer))) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            inputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }



    private void initUpgradeDialog() {
        Beta.autoInit = true;
        /**
         * 自定义初始化开关
         */
        /**
         * true表示初始化时自动检查升级; false表示不会自动检查升级,需要手动调用Beta.checkUpgrade()方法;
         */
        Beta.autoCheckUpgrade = false;

        /**
         * 设置升级检查周期为60s(默认检查周期为0s)，60s内SDK不重复向后台请求策略);
         */
//        Beta.upgradeCheckPeriod = 60 * 1000;
        /**
         * 设置启动延时为1s（默认延时3s），APP启动1s后初始化SDK，避免影响APP启动速度;
         */
        Beta.initDelay = 3 * 1000;
        /**
         * 设置通知栏大图标，largeIconId为项目中的图片资源;
         */
        Beta.largeIconId = R.mipmap.ic_launcher_round;
        /**
         * 设置状态栏小图标，smallIconId为项目中的图片资源Id;
         */
        Beta.smallIconId = R.mipmap.ic_launcher_round;
        /**
         * 设置更新弹窗默认展示的banner，defaultBannerId为项目中的图片资源Id;
         * 当后台配置的banner拉取失败时显示此banner，默认不设置则展示“loading“;
         */
        //Beta.defaultBannerId = R.mipmap.ic_launcher_round;
        /**
         * 设置sd卡的Download为更新资源保存目录;
         * 后续更新资源会保存在此目录，需要在manifest中添加WRITE_EXTERNAL_STORAGE权限;
         */
        Beta.storageDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        /**
         * 已经确认过的弹窗在APP下次启动自动检查更新时会再次显示;
         */
        Beta.showInterruptedStrategy = true;
        /**
         * 只允许在MainActivity上显示更新弹窗，其他activity上不显示弹窗; 不设置会默认所有activity都可以显示弹窗;
         */
        Beta.canShowUpgradeActs.add(ActivityMain.class);

        /**
         * 设置Wifi下自动下载
         */
        Beta.autoDownloadOnWifi = defaultSP.getBoolean("autoDownloadInWifi", true);


        /*在application中初始化时设置监听，监听策略的收取*/
        Beta.upgradeListener = new mUpgradeListener();

        /* 设置更新状态回调接口 */
        Beta.upgradeStateListener = new UpgradeStateListener() {
            @Override
            public void onUpgradeFailed(boolean isManual) {
//                Intent intent = new Intent();
//                intent.setAction("android.intent.updatebroadcast");
//                localBroadcastManager.sendBroadcast(intent);
                Toast.makeText(getApplicationContext(), "检查更新失败！", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUpgradeSuccess(boolean b) {
//                Intent intent = new Intent();
//                intent.setAction("android.intent.updatebroadcast");
//                localBroadcastManager.sendBroadcast(intent);
                Toast.makeText(getApplicationContext(), "检查到更新", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUpgrading(boolean isManual) {
                //Log.e("!!!","!");
//                Intent intent = new Intent();
//                intent.setAction("android.intent.updatebroadcast");
//                localBroadcastManager.sendBroadcast(intent);
                // Toast.makeText(getApplicationContext(),"更新成功!",Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onDownloadCompleted(boolean b) {
//                Intent intent = new Intent();
//                intent.setAction("android.intent.updatebroadcast");
//                localBroadcastManager.sendBroadcast(intent);
                Toast.makeText(getApplicationContext(), "下载完成", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onUpgradeNoVersion(boolean isManual) {
            }
        };


    }

    public static void getThemeID() {
        String mode = defaultSP.getString("dark_mode_mode","dark_mode_normal");
        if(mode.equals("dark_mode_normal")){
            if(defaultSP.getBoolean("is_dark_mode",false)) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            else  AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }else if(mode.equals("dark_mode_follow")){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }

        switch (defaultSP.getInt("theme_id", 3)) {
            case 0:
                themeID = R.style.RedTheme;
                break;
            case 1:
                themeID = R.style.PinkTheme;
                break;
            case 2:
                themeID = R.style.BrownTheme;
                break;
            case 3:
                themeID = R.style.BlueTheme;
                break;
            case 4:
                themeID = R.style.BlueGreyTheme;
                break;
            case 5:
                themeID = R.style.TealTheme;
                break;
            case 6:
                themeID = R.style.DeepPurpleTheme;
                break;
            case 7:
                themeID = R.style.GreenTheme;
                break;
            case 8:
                themeID = R.style.DeepOrangeTheme;
                break;
            case 9:
                themeID = R.style.IndigoTheme;
                break;
            case 10:
                themeID = R.style.CyanTheme;
                break;
            case 11:
                themeID = R.style.AmberTheme;
                break;
        }
    }


    public static boolean deleteCurriculum(int index){
        if(index>allCurriculum.size()-1) return false;
        Curriculum toDel = allCurriculum.get(index);
        SQLiteDatabase sd = mDBHelper.getWritableDatabase();
        sd.delete("curriculum","curriculum_code=? and name=?",new String[]{toDel.curriculumCode,toDel.name});
        sd.delete("timetable","curriculum_code=?",new String[]{toDel.curriculumCode});
        sd.delete("task","curriculum_code=?",new String[]{toDel.curriculumCode});
        sd.delete("subject","curriculum_code=?",new String[]{toDel.curriculumCode});
        if(index==thisCurriculumIndex){
            if(allCurriculum.size()==1) mainTimeTable.upDateCore(null);
            else  mainTimeTable.upDateCore(allCurriculum.get(allCurriculum.size()-2));
            thisCurriculumIndex = allCurriculum.size()-1;
        }
        allCurriculum.remove(index);
        return true;
    }

    public static boolean addCurriculumToTimeTable(CurriculumHelper il) {
        if (il == null) return false;
        List<Curriculum> toDEl = new ArrayList<>();
        for (Curriculum temp : allCurriculum) {
            if (temp.curriculumCode.equals(il.curriculumCode)) {
                toDEl.add(temp);
            }
        }
        allCurriculum.removeAll(toDEl);
        Curriculum cur = il.getCurriculum();
        if (toDEl.size() > 0) cur.setObjectId(toDEl.get(0).getObjectId());
        if (cur.getWeekOfTerm(now) > cur.totalWeeks) cur.totalWeeks = cur.getWeekOfTerm(now);
        allCurriculum.add(cur);
        thisCurriculumIndex = allCurriculum.size() - 1;
        mainTimeTable.clearCurriculum(il.curriculumCode);
        mainTimeTable.core = cur;//顺序不能乱
        mainTimeTable.addCurriculum(il);
        addSubjects(il);
        defaultSP.edit().putInt("thisCurriculum", thisCurriculumIndex).apply();
        return true;
    }

    static void addSubjects(CurriculumHelper ch) {
        // Log.e("subjects:", String.valueOf(ch.Subjects));
        SQLiteDatabase sd = mDBHelper.getWritableDatabase();

        //sd.delete("subject","curriculum_code=?",new String[]{ch.curriculumCode});
        for (Subject s : ch.Subjects) {
            String ratesText = null;
            String scoresText = null;
            Cursor c = sd.query("subject",new String[]{"rates,scores"},"curriculum_code =? AND name=?",
                    new String[]{ch.curriculumCode,s.name},null,null,null);
            if(c.moveToNext()){
                ratesText = c.getString(0);
                scoresText = c.getString(1);
            }
            c.close();
            ContentValues cv = s.getContentValues();
            if(ratesText!=null) cv.put("rates",ratesText);
            if(scoresText!=null) cv.put("scores",scoresText);
//            sd.delete("subject","name=? and curriculum_code=? and code=?",
//                    new String[]{s.name,s.curriculumCode,s.code});
            sd.replace("subject",null,cv);
//            if (sd.update("subject", s.getContentValues(), "name=? and curriculum_code=? and code=?",
//                    new String[]{s.name,s.curriculumCode,s.code}) == 0) {
//                sd.insert("subject", null, s.getContentValues());
//            }
        }
    }

    public static void initCoreData(){
        allCurriculum.clear();
        ArrayList<Curriculum> temp1 = new ArrayList<>();
        SQLiteDatabase sd = mDBHelper.getReadableDatabase();
        Cursor c = sd.query("curriculum",null,null,null,null,null,null);
        //ArrayList temp1 = FileOperator.loadCurriculumFromFile(this.getFilesDir());
        while (c.moveToNext()){
            temp1.add(new Curriculum(c));
        }
        c.close();
        allCurriculum.addAll(temp1);
        correctData();
        thisCurriculumIndex = defaultSP.getInt("thisCurriculum",0);
        try {
            allCurriculum.get(thisCurriculumIndex);
            thisWeekOfTerm = allCurriculum.get(thisCurriculumIndex).getWeekOfTerm(now);
        } catch (Exception e) {
            thisCurriculumIndex = 0;
        }
        if(isDataAvailable()&&thisWeekOfTerm>allCurriculum.get(thisCurriculumIndex).totalWeeks) allCurriculum.get(thisCurriculumIndex).totalWeeks = thisWeekOfTerm;
    }
    public static void correctData() {
        if (allCurriculum == null || mainTimeTable == null) return;
        int allCurriculumSize = allCurriculum.size();
        if (thisCurriculumIndex >= allCurriculumSize) {
            if (allCurriculumSize > 0) thisCurriculumIndex = allCurriculumSize - 1;
            else thisCurriculumIndex = 0;
        }
        if (allCurriculum.size() > 0) {
            mainTimeTable.core = allCurriculum.get(thisCurriculumIndex);
        }

    }

    public static boolean isDataAvailable() {
        if (mainTimeTable == null) return false;
        if(mainTimeTable.core == null) return false;
        try {
            allCurriculum.get(thisCurriculumIndex);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static int getDataState() {
        if (allCurriculum == null || mainTimeTable == null) return DATA_STATE_NULL;
        if (allCurriculum.size() == 0) return DATA_STATE_NONE_CURRICULUM;
        if (!isDataAvailable()) return DATA_STATE_GET_ERROR;
        return DATA_STATE_HEALTHY;
    }

    public static boolean saveDataToCloud(final boolean showToast) {
        if (CurrentUser == null) return false;
        Log.e("开始上传数据", "尝试");
        for (final Curriculum ci : allCurriculum) {
            ci.setHitaUser(CurrentUser);
            ci.setSubjectsText();
        }
        SQLiteDatabase sd = mDBHelper.getReadableDatabase();
        ArrayList<TimeTable_upload_helper> TUHs = new ArrayList<>();
        final Cursor c = sd.query("timetable", null, null, null, null, null, null);
        while (c.moveToNext()) {
            TimeTable_upload_helper bc = new TimeTable_upload_helper(c);
            if (bc.type == TimeTable.TIMETABLE_EVENT_TYPE_COURSE || bc.type == TimeTable.TIMETABLE_EVENT_TYPE_DYNAMIC)
                continue;
            else TUHs.add(bc);
        }
        c.close();
        ArrayList<Task> tasks = new ArrayList<>();
        Cursor c2 = sd.query("task", null, null, null, null, null, null);
        while (c2.moveToNext()) {
            Task t = new Task(c2);
            if(!t.isFinished()&&t.getType()!=Task.TYPE_DYNAMIC)tasks.add(t);
        }
        c2.close();
        final Bmob_User_Data BUD = new Bmob_User_Data(allCurriculum, TUHs, tasks);
        BUD.setHitaUser(CurrentUser);
        BmobQuery<Bmob_User_Data> bq = new BmobQuery<>();
        bq.addWhereEqualTo("hitaUser", CurrentUser);
        bq.findObjects(new FindListener<Bmob_User_Data>() {
            @Override
            public void done(List<Bmob_User_Data> list, BmobException e) {
                Log.e("found:", e == null ? "null" : e.toString());
                if (e != null || list == null || list.size() == 0) {
                    BUD.save(new SaveListener<String>() {
                        @Override
                        public void done(String s, BmobException e) {
                            if(showToast) Toast.makeText(HContext,"上传成功！",Toast.LENGTH_SHORT).show();
                            if (e == null) Log.e("新增用户数据", "成功");
                            else Log.e("新增用户数据", e.toString());
                        }
                    });
                } else {
                    if (list != null && list.size() > 0) {
                        BUD.setObjectId(list.get(0).getObjectId());
                        BUD.update(new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if(showToast)Toast.makeText(HContext,"上传成功！",Toast.LENGTH_SHORT).show();
                                if (e == null) Log.e("更新用户数据", "成功");
                                else Log.e("更新用户数据", e.toString());
                            }
                        });
                    }
                }
            }
        });
        return true;
    }


    public static boolean loadDataFromCloud() {
        if (CurrentUser == null) return false;
        clearData();
        BmobQuery<Bmob_User_Data> query = new BmobQuery<>();
        query.addWhereEqualTo("hitaUser", CurrentUser);
        query.findObjects(new FindListener<Bmob_User_Data>() {
            @Override
            public void done(List<Bmob_User_Data> list, BmobException e) { //如果done里面其他的函数出错，会再执行一次done抛出异常！！！
                Log.e("下载","done");
                if (e == null && list != null && list.size() > 0) {
                    new writeDataToLocalTask(list.get(0)).execute();
                }else {
                    Toast.makeText(HContext,"云端没有数据！",Toast.LENGTH_SHORT).show();
                    Log.e("下载失败",e==null?"空结果":e.toString());
                }
            }
        });


        return true;
    }
    public static boolean loadDataFromCloud(final Activity toFinish) {
        if (CurrentUser == null) return false;
        clearData();
        BmobQuery<Bmob_User_Data> query = new BmobQuery<>();
        query.addWhereEqualTo("hitaUser", CurrentUser);
        query.findObjects(new FindListener<Bmob_User_Data>() {
            @Override
            public void done(List<Bmob_User_Data> list, BmobException e) { //如果done里面其他的函数出错，会再执行一次done抛出异常！！！
                Log.e("下载","done");
                if (e == null && list != null && list.size() > 0) {
                    new writeDataToLocalTask(list.get(0),toFinish).execute();
                }else {
                   if(toFinish!=null) toFinish.finish();
                    Log.e("下载失败",e==null?"空结果":e.toString());
                }
            }
        });


        return true;
    }

    public static void clearData() {
        mDBHelper.clearTables();
        allCurriculum.clear();
        thisCurriculumIndex = 0;
        mainTimeTable.core = null;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        mDBHelper.getWritableDatabase().close();
    }

//    class InitDataTask extends AsyncTask{
//        @Override
//        protected Object doInBackground(Object[] objects) {
//            initCoreData();
////            ((HITAApplication)ActivityMain.this.getApplication()).copyAssetsSingleFile(HContext.getFilesDir(), "mDict_default.dic");
////            ((HITAApplication)ActivityMain.this.getApplication()).copyAssetsSingleFile(HContext.getFilesDir(), "mDict_ambiguity.dic");
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Object o) {
//            super.onPostExecute(o);
//
//            // tlf.Refresh(FragmentTimeLine.TL_REFRESH_FROM_UNHIDE);
//        }
//    }

    static class writeDataToLocalTask extends AsyncTask{

        Bmob_User_Data user_data;
        Activity tofinish;
        writeDataToLocalTask(Bmob_User_Data bmob_user_data,Activity tofinish){
            this.user_data = bmob_user_data;
            this.tofinish = tofinish;
        }
        writeDataToLocalTask(Bmob_User_Data bmob_user_data){
            this.user_data = bmob_user_data;
            this.tofinish = null;
        }
        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                SQLiteDatabase sqd = mDBHelper.getWritableDatabase();
                for (Curriculum ci : user_data.getCurriculumsFromText()) {
                    sqd.insert("curriculum", null, ci.getContentValues());
                    addCurriculumToTimeTable(FileOperator.loadCurriculumHelperFromCurriculumText(ci));
                    sqd.delete("subject", "curriculum_code=?", new String[]{ci.curriculumCode});
                    for (Subject s : ci.getSubjectsFromString()) {
                        sqd.insert("subject", null, s.getContentValues());
                    }
                }
                for (TimeTable_upload_helper tuh :user_data.getTimeTableHelpersFromString()) {
                    sqd.insert("timetable", null, tuh.getContentValues());
                }
                for (Task t : user_data.getTasksFromText()) {
                    sqd.insert("task", null, t.getContentValues());
                }

                return true;

            } catch (Exception e1) {
                e1.printStackTrace();
               return false;
            }


        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if((Boolean)o){
                Toast.makeText(HContext,"成功同步该账号的课表！",Toast.LENGTH_SHORT).show();
                if(isDataAvailable()) timeWatcher.refreshNowAndNextEvent();
                if(tofinish!=null){
                    tofinish.finish();
                }
            }else{
                Toast.makeText(HContext,"同步用户数据出错！",Toast.LENGTH_SHORT).show();
            }
        }
    }


}
