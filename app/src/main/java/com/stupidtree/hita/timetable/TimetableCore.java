package com.stupidtree.hita.timetable;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import androidx.annotation.WorkerThread;

import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.hita.TextTools;
import com.stupidtree.hita.online.Bmob_User_Data;
import com.stupidtree.hita.online.TimeTable_upload_helper;
import com.stupidtree.hita.timetable.timetable.EventItem;
import com.stupidtree.hita.timetable.timetable.EventItemHolder;
import com.stupidtree.hita.timetable.timetable.HTime;
import com.stupidtree.hita.timetable.timetable.Task;
import com.stupidtree.hita.timetable.timetable.TimePeriod;
import com.stupidtree.hita.util.FileOperator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.defaultSP;
import static com.stupidtree.hita.HITAApplication.mDBHelper;
import static com.stupidtree.hita.HITAApplication.now;
import static com.stupidtree.hita.HITAApplication.timeServiceBinder;
import static com.stupidtree.hita.HITAApplication.timeTableCore;
import static com.stupidtree.hita.activities.ActivityMain.saveData;
import static com.stupidtree.hita.timetable.CurriculumCreator.CURRICULUM_TYPE_COURSE;
import static com.stupidtree.hita.timetable.TimeWatcherService.WATCHER_REFRESH;

public class TimetableCore {
    public final static int TIMETABLE_EVENT_TYPE_COURSE = 1;
    public final static int TIMETABLE_EVENT_TYPE_EXAM = 2;
    public final static int TIMETABLE_EVENT_TYPE_ARRANGEMENT = 3;
    public final static int TIMETABLE_EVENT_TYPE_REMIND = 4;
    public final static int TIMETABLE_EVENT_TYPE_DEADLINE = 5;
    public final static int TIMETABLE_EVENT_TYPE_DYNAMIC = 6;
    private Curriculum currentCurriculum = null;
    private String currentCurriculumId = null;
    private boolean isThisTerm = true;
    private int thisWeekOfTerm = -1;

    public TimetableCore() {

    }

    public void onTerminate() {
        mDBHelper.getWritableDatabase().close();
    }

    @WorkerThread
    public boolean deleteCurriculum(String curriculumCode) {
        List<Curriculum> allCurriculum = getAllCurriculum();
        int index = -1;
        for (int i = 0; i < allCurriculum.size(); i++) {
            if (allCurriculum.get(i).getCurriculumCode().equals(curriculumCode)) index = i;
        }
        if (index < 0) return false;
        Curriculum toDel = allCurriculum.get(index);
        SQLiteDatabase sd = mDBHelper.getWritableDatabase();
        sd.delete("curriculum", "curriculum_code=? and name=?", new String[]{toDel.getCurriculumCode(), toDel.getName()});
        sd.delete("timetable", "curriculum_code=?", new String[]{toDel.getCurriculumCode()});
        sd.delete("task", "curriculum_code=?", new String[]{toDel.getCurriculumCode()});
        sd.delete("subject", "curriculum_code=?", new String[]{toDel.getCurriculumCode()});

        allCurriculum.remove(index);
        if (allCurriculum.size() > 0) {
            currentCurriculum = allCurriculum.get(allCurriculum.size() - 1);
            currentCurriculumId = allCurriculum.get(allCurriculum.size() - 1).getCurriculumCode();
        } else {
            currentCurriculumId = null;
            currentCurriculum = null;
            thisWeekOfTerm = -1;
        }
        return true;
    }

    @SuppressLint("ApplySharedPref")
    @WorkerThread
    public boolean addCurriculumToTimeTable(CurriculumCreator il) {
        if (il == null) return false;
        Curriculum cur = il.getCurriculum();
        SQLiteDatabase sdb = mDBHelper.getWritableDatabase();
        if (cur.getWeekOfTerm(now) > cur.getTotalWeeks()) cur.setTotalWeeks(cur.getWeekOfTerm(now));
        sdb.delete("curriculum","curriculum_code=?",new String[]{il.getCurriculumCode()});
        sdb.insert("curriculum",null,cur.getContentValues());
        currentCurriculumId = cur.getCurriculumCode();
        currentCurriculum = cur;
        // timeTablegetCurrentCurriculum().getMainTimeTable().clearCurriculum(il.getCurriculumId());
        addCurriculum(il);
        addSubjects(il);
        defaultSP.edit().putString("current_curriculum", currentCurriculumId).commit();
        return true;
    }


    public void addSubjects(CurriculumCreator ch) {
        // Log.e("subjects:", String.valueOf(ch.Subjects));
        SQLiteDatabase sd = mDBHelper.getWritableDatabase();

        //sd.delete("subject","curriculum_code=?",new String[]{ch.getCurriculumId()});
        for (Subject s : ch.getSubjects()) {
            String ratesText = null;
            String scoresText = null;
            Cursor c = sd.query("subject", new String[]{"rates,scores"}, "curriculum_code =? AND name=?",
                    new String[]{ch.getCurriculumCode(), s.getName()}, null, null, null);
            if (c.moveToNext()) {
                ratesText = c.getString(0);
                scoresText = c.getString(1);
            }
            c.close();
            ContentValues cv = s.getContentValues();
            if (ratesText != null) cv.put("rates", ratesText);
            if (scoresText != null) cv.put("scores", scoresText);
//            sd.delete("subject","name=? and curriculum_code=? and code=?",
//                    new String[]{s.getName(),s.getCurriculumId(),s.code});
            sd.replace("subject", null, cv);
//            if (sd.update("subject", s.getContentValues(), "name=? and curriculum_code=? and code=?",
//                    new String[]{s.getName(),s.getCurriculumId(),s.code}) == 0) {
//                sd.insert("subject", null, s.getContentValues());
//            }
        }
    }

    @SuppressLint("ApplySharedPref")
    @WorkerThread
    public void initCoreData() {
        currentCurriculumId = defaultSP.getString("current_curriculum", null);
        if (currentCurriculumId == null) {
            List<Curriculum> all = getAllCurriculum();
            if (all.size() > 0) {
                currentCurriculum = all.get(all.size() - 1);
                currentCurriculumId = currentCurriculum.getCurriculumCode();
                thisWeekOfTerm = currentCurriculum.getWeekOfTerm(now);
                if (isDataAvailable() && thisWeekOfTerm > getCurrentCurriculum().getTotalWeeks())
                    getCurrentCurriculum().setTotalWeeks(thisWeekOfTerm);
                defaultSP.edit().putString("current_curriculum", currentCurriculumId).commit();
            }
        } else {
            currentCurriculum = mDBHelper.getCurriculumAtId(currentCurriculumId);
            if (currentCurriculum != null)
                thisWeekOfTerm = getCurrentCurriculum().getWeekOfTerm(now);
            if (isDataAvailable() && thisWeekOfTerm > getCurrentCurriculum().getTotalWeeks())
                getCurrentCurriculum().setTotalWeeks(thisWeekOfTerm);
        }
       // Log.e("cur_text",currentCurriculum.getCurriculumText());
    }

    public boolean isDataAvailable() {
        return currentCurriculum != null;
    }

    @WorkerThread
    public boolean saveDataToCloud(final boolean showToast) {
        if (CurrentUser == null) return false;
        List<Curriculum> allCurriculums = getAllCurriculum();
        Log.e("开始上传数据", "尝试");
        for (final Curriculum ci : allCurriculums) {
            ci.setSubjectsText();
        }
        SQLiteDatabase sd = mDBHelper.getReadableDatabase();
        ArrayList<TimeTable_upload_helper> TUHs = new ArrayList<>();
        final Cursor c = sd.query("timetable", null, null, null, null, null, null);
        while (c.moveToNext()) {
            TimeTable_upload_helper bc = new TimeTable_upload_helper(c);
            if (bc.type == TIMETABLE_EVENT_TYPE_COURSE || bc.type == TIMETABLE_EVENT_TYPE_DYNAMIC)
                continue;
            else TUHs.add(bc);
        }
        c.close();
        ArrayList<Task> tasks = new ArrayList<>();
        Cursor c2 = sd.query("task", null, null, null, null, null, null);
        while (c2.moveToNext()) {
            Task t = new Task(c2);
            if (!t.isFinished() && t.getType() != Task.TYPE_DYNAMIC) tasks.add(t);
        }
        c2.close();
        final Bmob_User_Data BUD = new Bmob_User_Data(allCurriculums, TUHs, tasks);
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
                            if (showToast)
                                Toast.makeText(HContext, R.string.uploaded_success, Toast.LENGTH_SHORT).show();
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
                                if (showToast)
                                    Toast.makeText(HContext, R.string.uploaded_success, Toast.LENGTH_SHORT).show();
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


    public boolean loadDataFromCloud() {
        if (CurrentUser == null) return false;
        clearData();
        BmobQuery<Bmob_User_Data> query = new BmobQuery<>();
        query.addWhereEqualTo("hitaUser", CurrentUser);
        query.findObjects(new FindListener<Bmob_User_Data>() {
            @Override
            public void done(List<Bmob_User_Data> list, BmobException e) { //如果done里面其他的函数出错，会再执行一次done抛出异常！！！
                Log.e("下载", "done");
                if (e == null && list != null && list.size() > 0) {
                    new writeDataToLocalTask(list.get(0)).executeOnExecutor(HITAApplication.TPE);
                } else {
                    Toast.makeText(HContext, R.string.no_data_on_cloud, Toast.LENGTH_SHORT).show();
                    Log.e("下载失败", e == null ? "空结果" : e.toString());
                }
            }
        });


        return true;
    }

    public boolean loadDataFromCloud(Bmob_User_Data bud) {
        if (CurrentUser == null) return false;
        clearData();
        new writeDataToLocalTask(bud).executeOnExecutor(HITAApplication.TPE);
        return true;
    }

    public boolean loadDataFromCloud(final Activity toFinish) {
        if (CurrentUser == null) return false;
        clearData();
        BmobQuery<Bmob_User_Data> query = new BmobQuery<>();
        query.addWhereEqualTo("hitaUser", CurrentUser);
        query.findObjects(new FindListener<Bmob_User_Data>() {
            @Override
            public void done(List<Bmob_User_Data> list, BmobException e) { //如果done里面其他的函数出错，会再执行一次done抛出异常！！！
                Log.e("下载", "done");
                if (e == null && list != null && list.size() > 0) {
                    new writeDataToLocalTask(list.get(0), toFinish).executeOnExecutor(HITAApplication.TPE);
                } else {
                    if (toFinish != null) toFinish.finish();
                    Log.e("下载失败", e == null ? "空结果" : e.toString());
                }
            }
        });


        return true;
    }

    @WorkerThread
    public void clearData() {
        mDBHelper.clearTables();
        currentCurriculum = null;
        currentCurriculumId = null;
    }


    class writeDataToLocalTask extends AsyncTask {

        Bmob_User_Data user_data;
        Activity tofinish;

        writeDataToLocalTask(Bmob_User_Data bmob_user_data, Activity tofinish) {
            this.user_data = bmob_user_data;
            this.tofinish = tofinish;
        }

        writeDataToLocalTask(Bmob_User_Data bmob_user_data) {
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
                    sqd.delete("subject", "curriculum_code=?", new String[]{ci.getCurriculumCode()});
                    for (Subject s : ci.getSubjectsFromString()) {
                        sqd.insert("subject", null, s.getContentValues());
                    }
                }
                for (TimeTable_upload_helper tuh : user_data.getTimeTableHelpersFromString()) {
                    sqd.insert("timetable", null, tuh.getContentValues());
                }
                for (Task t : user_data.getTasksFromText()) {
                    sqd.insert("task", null, t.getContentValues());
                }

                initCoreData();
                return true;

            } catch (Exception e1) {
                e1.printStackTrace();
                return false;
            }


        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (tofinish != null) {
                tofinish.finish();
            }
            if ((Boolean) o) {
                Toast.makeText(HContext, R.string.sync_success, Toast.LENGTH_SHORT).show();
                if (isDataAvailable()) timeServiceBinder.refreshNowAndNextEvent();
            } else {
                Toast.makeText(HContext, R.string.sync_error, Toast.LENGTH_SHORT).show();
            }
        }
    }


    public boolean isThisTerm() {
        return isThisTerm;
    }

    public void setThisTerm(boolean thisTerm) {
        isThisTerm = thisTerm;
    }

    public int getThisWeekOfTerm() {
        return thisWeekOfTerm;
    }

    public void setThisWeekOfTerm(int thisWeekOfTerm) {
        this.thisWeekOfTerm = thisWeekOfTerm;
    }

    public Curriculum getCurrentCurriculum() {
        return currentCurriculum;
    }

    @WorkerThread
    public List<Curriculum> getAllCurriculum() {
        List<Curriculum> all = mDBHelper.getAllCurriculum();
        List<Curriculum> result = new ArrayList<>();
        for (Curriculum c : all) {
            if (currentCurriculum != null && c.getCurriculumCode().equals(currentCurriculum.getCurriculumCode())) {
                result.add(currentCurriculum);
            } else result.add(c);
        }
        return result;
    }

    @SuppressLint("ApplySharedPref")
    @WorkerThread
    public void changeCurrentCurriculum(String newId) {
        Curriculum newC = mDBHelper.getCurriculumAtId(newId);
        if (newC != null) {
            currentCurriculumId = newId;
            currentCurriculum = newC;
            setThisWeekOfTerm(newC.getWeekOfTerm(now));
            if (getThisWeekOfTerm() > newC.getTotalWeeks()) {
                newC.setTotalWeeks(timeTableCore.getThisWeekOfTerm());
            }
            defaultSP.edit().putString("current_curriculum", newId).commit();
            saveData();
//            Intent i = new Intent(WATCHER_REFRESH);
//            HContext.sendBroadcast(i);
            timeServiceBinder.refreshProgress();
        }
    }


    @WorkerThread
    public void addCurriculum(CurriculumCreator cl) {
        SQLiteDatabase mDatabase = mDBHelper.getWritableDatabase();
        mDatabase.delete("timetable", "curriculum_code=? and type=?", new String[]{cl.getCurriculumCode(), TimetableCore.TIMETABLE_EVENT_TYPE_COURSE + ""});
        for (CurriculumCreator.CurriculumItem ci : cl.getCurriculumList()) {
            if (ci.type == CURRICULUM_TYPE_COURSE) {
                String tag4 = ci.begin + "";
                for (int i = 1; i < ci.last; i++) {
                    tag4 = tag4 + "," + (ci.begin + i);
                }
                EventItemHolder eih = new EventItemHolder(cl.getCurriculumCode(), TIMETABLE_EVENT_TYPE_COURSE, ci.name, ci.place, ci.tag, tag4, getTimeAtNumber(ci.begin, ci.last).get(0), getTimeAtNumber(ci.begin, ci.last).get(1), ci.DOW, ci.weeks, false
                );
                mDatabase.insert("timetable", null, eih.getContentValues());
                //EventHolders.add();
            } else {
                addEvent(ci.weeks.get(0), ci.DOW, TIMETABLE_EVENT_TYPE_EXAM, ci.name, ci.place, ci.name, ci.tag, ci.begin, ci.last, false);
                //EventHolders.add(new EventItemHolder(1,ci.name,ci.place,null,ci.tag,getTimeAtNumber(ci.begin,ci.last).get(0),getTimeAtNumber(ci.begin,ci.last).get(1),ci.DOW,ci.weeks));
            }
        }
         }

    public static List<HTime> getTimeAtNumber(int begin, int last) {
        int[] startDots = {830, 930, 1030, 1130, 1345, 1440, 1545, 1640, 1830, 1925, 2030, 2125,2230};
        int[] endDots = {920, 1015, 1120, 1215, 1435, 1530, 1635, 1730, 1920, 2015, 2120, 2215,2320};
        List<HTime> temp = new ArrayList<>();
        HTime startTime, endTime;
        int sH = 0, sM = 0, eH = 0, eM = 0;
        sH = startDots[begin - 1] / 100;
        sM = startDots[begin - 1] % 100;
        eH = endDots[(begin - 1) + last - 1] / 100;
        eM = endDots[(begin - 1) + last - 1] % 100;
        startTime = new HTime(sH, sM);
        endTime = new HTime(eH, eM);
        temp.add(startTime);
        temp.add(endTime);
        return temp;

    }

    public static int getNumberAtTime(Calendar time) {
        HTime to = new HTime(time);
        TimePeriod[] dots = new TimePeriod[]{
                new TimePeriod(new HTime(8, 30), new HTime(9, 20)),
                new TimePeriod(new HTime(9, 30), new HTime(10, 15)),
                new TimePeriod(new HTime(10, 30), new HTime(11, 20)),
                new TimePeriod(new HTime(11, 30), new HTime(12, 15)),
                new TimePeriod(new HTime(13, 45), new HTime(14, 35)),
                new TimePeriod(new HTime(14, 40), new HTime(15, 30)),
                new TimePeriod(new HTime(15, 45), new HTime(16, 35)),
                new TimePeriod(new HTime(16, 30), new HTime(17, 30)),
                new TimePeriod(new HTime(18, 30), new HTime(19, 20)),
                new TimePeriod(new HTime(19, 25), new HTime(20, 15)),
                new TimePeriod(new HTime(20, 30), new HTime(21, 20)),
                new TimePeriod(new HTime(21, 25), new HTime(22, 15)),
                new TimePeriod(new HTime(22, 30), new HTime(23, 20)),

        };
        for (int i = 0; i < dots.length; i++) {
            if (to.during(dots[i])) return i + 1;
        }
        return -1;
    }
    public static int getNumberAtTime(HTime to) {

        TimePeriod[] dots = new TimePeriod[]{
                new TimePeriod(new HTime(8, 30), new HTime(9, 20)),
                new TimePeriod(new HTime(9, 30), new HTime(10, 15)),
                new TimePeriod(new HTime(10, 30), new HTime(11, 20)),
                new TimePeriod(new HTime(11, 30), new HTime(12, 15)),
                new TimePeriod(new HTime(13, 45), new HTime(14, 35)),
                new TimePeriod(new HTime(14, 40), new HTime(15, 30)),
                new TimePeriod(new HTime(15, 45), new HTime(16, 35)),
                new TimePeriod(new HTime(16, 30), new HTime(17, 30)),
                new TimePeriod(new HTime(18, 30), new HTime(19, 20)),
                new TimePeriod(new HTime(19, 25), new HTime(20, 15)),
                new TimePeriod(new HTime(20, 30), new HTime(21, 20)),
                new TimePeriod(new HTime(21, 25), new HTime(22, 15)),
        };
        for (int i = 0; i < dots.length; i++) {
            if (to.during(dots[i])) return i + 1;
            else if(to.before(dots[i].start)) return i;
        }
        return -1;
    }
    public static TimePeriod getClassTimeByTimeContainedIn(HTime time) {
        TimePeriod[] dots = new TimePeriod[]{
                new TimePeriod(new HTime(8, 30), new HTime(9, 20)),
                new TimePeriod(new HTime(9, 30), new HTime(10, 15)),
                new TimePeriod(new HTime(10, 30), new HTime(11, 20)),
                new TimePeriod(new HTime(11, 30), new HTime(12, 15)),
                new TimePeriod(new HTime(13, 45), new HTime(14, 35)),
                new TimePeriod(new HTime(14, 40), new HTime(15, 30)),
                new TimePeriod(new HTime(15, 45), new HTime(16, 35)),
                new TimePeriod(new HTime(16, 30), new HTime(17, 30)),
                new TimePeriod(new HTime(18, 30), new HTime(19, 20)),
                new TimePeriod(new HTime(19, 25), new HTime(20, 15)),
                new TimePeriod(new HTime(20, 30), new HTime(21, 20)),
                new TimePeriod(new HTime(21, 25), new HTime(22, 15)),
        };
        for (int i = 0; i < dots.length; i++) {
            if (time.during(dots[i]))  return dots[i];
        }
        return null;
    }

    public static TimePeriod getClassSimplfiedTimeByTimeContainedIn(HTime time) {
        TimePeriod[] dots = new TimePeriod[]{
                new TimePeriod(new HTime(0, 0), new HTime(8, 30)),
                new TimePeriod(new HTime(8, 30), new HTime(10, 15)),
                new TimePeriod(new HTime(10, 15), new HTime(10, 30)),
                new TimePeriod(new HTime(10, 30), new HTime(12, 15)),
                new TimePeriod(new HTime(12, 15), new HTime(13, 45)),
                new TimePeriod(new HTime(13, 45), new HTime(15, 30)),
                new TimePeriod(new HTime(15, 30), new HTime(15, 45)),
                new TimePeriod(new HTime(15, 45), new HTime(17, 30)),
                new TimePeriod(new HTime(17, 30), new HTime(18, 30)),
                new TimePeriod(new HTime(18, 30), new HTime(20, 15)),
                new TimePeriod(new HTime(20, 15), new HTime(20, 30)),
                new TimePeriod(new HTime(20, 30), new HTime(22, 15)),
                new TimePeriod(new HTime(22, 15), new HTime(23, 59))
        };
        for (int i = 0; i < dots.length; i++) {
            if (time.during(dots[i]))  return dots[i];
        }
        return null;
    }

    /*函数功能：添加事件*/
    public String addEvent(int week, int DOW, int type, String eventName, String tag2, String tag3, String tag4, int begin, int last, boolean isWholeDay) {
        SQLiteDatabase mDatabase = mDBHelper.getWritableDatabase();
        if (week > getCurrentCurriculum().getTotalWeeks())
            getCurrentCurriculum().setTotalWeeks(week);
        EventItemHolder temp = new EventItemHolder(getCurrentCurriculum().getCurriculumCode(), type, eventName, tag2, tag3, tag4, getTimeAtNumber(begin, last).get(0), getTimeAtNumber(begin, last).get(1), DOW, isWholeDay);
        temp.weeks.add(week);
        Cursor c = mDatabase.query("timetable", null, EventItemHolder.QUERY_SELECTION, temp.getQueryParams(), null, null, null);
        String uuid = temp.getUuid();
        if (c.moveToNext()) {
            List<Integer> weeks = new ArrayList<>();
            String[] strs = c.getString(2).split(",");
            for (int i = 0; i < strs.length; i++) {
                if (!strs[i].isEmpty()) weeks.add(Integer.parseInt(strs[i]));
            }
            if (!weeks.contains(week)) {
                weeks.add(week);
            }
            String newWeeks = getWeeksText(weeks);
            ContentValues cv = new ContentValues();
            uuid = c.getString(c.getColumnIndex("uuid"));
            cv.put("weeks", newWeeks);
            cv.put("uuid", uuid);
            mDatabase.update("timetable", cv, EventItemHolder.QUERY_SELECTION, temp.getQueryParams());
        } else {
            mDatabase.insert("timetable", null, temp.getContentValues());
        }
        c.close();
        return uuid;
    }

    public String addEvents(List<Integer> weeks, int DOW, int type, String eventName, String tag2, String tag3, String tag4, int begin, int last, boolean isWholeDay) {
        Log.e("add", weeks + ",dow:" + DOW + ",event:" + eventName + ",from:" + begin + ",last:" + last);
        for (int i : weeks)
            if (i > getCurrentCurriculum().getTotalWeeks()) getCurrentCurriculum().setTotalWeeks(i);
        SQLiteDatabase mDatabase = mDBHelper.getWritableDatabase();
        EventItemHolder temp = new EventItemHolder(getCurrentCurriculum().getCurriculumCode(), type, eventName, tag2, tag3, tag4, getTimeAtNumber(begin, last).get(0), getTimeAtNumber(begin, last).get(1), DOW, isWholeDay);
        temp.weeks.addAll(weeks);
        mDatabase.insert("timetable", null, temp.getContentValues());
        return temp.getUuid();
    }

    public String addEvent(int week, int DOW, int type, String eventName, String tag2, String tag3, String tag4, HTime start, HTime end, boolean isWholeDay) {
        SQLiteDatabase mDatabase = mDBHelper.getWritableDatabase();
        if (week > getCurrentCurriculum().getTotalWeeks())
            getCurrentCurriculum().setTotalWeeks(week);
        EventItemHolder temp = new EventItemHolder(getCurrentCurriculum().getCurriculumCode(), type, eventName, tag2, tag3, tag4, start, end, DOW, isWholeDay);
        temp.weeks.add(week);
        String uuid = temp.getUuid();
        Cursor c = mDatabase.query("timetable", null, EventItemHolder.QUERY_SELECTION, temp.getQueryParams(), null, null, null);
        if (c.moveToNext()) {
            List<Integer> weeks = new ArrayList<>();
            String[] strs = c.getString(2).split(",");
            for (int i = 0; i < strs.length; i++) {
                if (!strs[i].isEmpty()) weeks.add(Integer.parseInt(strs[i]));
            }
            if (!weeks.contains(week)) {
                weeks.add(week);
            }
            String newWeeks = getWeeksText(weeks);
            ContentValues cv = new ContentValues();
            uuid = c.getString(c.getColumnIndex("uuid"));
            cv.put("weeks", newWeeks);
            cv.put("uuid", uuid);
            mDatabase.update("timetable", cv, EventItemHolder.QUERY_SELECTION, temp.getQueryParams());
        } else {
            mDatabase.insert("timetable", null, temp.getContentValues());
        }
        c.close();
        return uuid;
    }

    public String addEvent(EventItem ei) {
        SQLiteDatabase mDatabase = mDBHelper.getWritableDatabase();
        int week, DOW, type;
        String eventName, tag2, tag3, tag4;
        HTime start, end;
        week = ei.week;
        DOW = ei.DOW;
        type = ei.eventType;
        eventName = ei.mainName;
        tag2 = ei.tag2;
        tag3 = ei.tag3;
        tag4 = ei.tag4;
        start = ei.startTime;
        end = ei.endTime;
        if (week > getCurrentCurriculum().getTotalWeeks())
            getCurrentCurriculum().setTotalWeeks(week);
        EventItemHolder temp = new EventItemHolder(ei.getCurriculumCode(), type, eventName, tag2, tag3, tag4, start, end, DOW, ei.isWholeDay);
        temp.weeks.add(week);
        ei.setUuid(temp.getUuid());
        Cursor c = mDatabase.query("timetable", null, EventItemHolder.QUERY_SELECTION, temp.getQueryParams(), null, null, null);
        String uuid = temp.getUuid();
        if (c.moveToNext()) {
            List<Integer> weeks = new ArrayList<>();
            String[] strs = c.getString(2).split(",");
            for (int i = 0; i < strs.length; i++) {
                weeks.add(Integer.parseInt(strs[i]));
            }
            if (!weeks.contains(week)) {
                weeks.add(week);
            }
            String newWeeks = getWeeksText(weeks);
            ContentValues cv = temp.getContentValues();
            uuid = c.getString(c.getColumnIndex("uuid"));
            cv.put("weeks", newWeeks);
            cv.put("uuid", uuid);
            mDatabase.update("timetable", cv, EventItemHolder.QUERY_SELECTION, temp.getQueryParams());
        } else {
            Log.e("add", temp.toString());
            mDatabase.insert("timetable", null, temp.getContentValues());
        }
        c.close();

        return uuid;
    }

    @WorkerThread
    public boolean deleteEvent(EventItem ei, boolean deleteTask) {
        Log.e("deleteEvent", "dt:" + deleteTask);
        SQLiteDatabase mSQLiteDatabase = mDBHelper.getWritableDatabase();
        if (ei == null) return false;
        EventItemHolder eih = new EventItemHolder(ei);
        Cursor c = mSQLiteDatabase.query("timetable", null, "uuid=?",
                new String[]{ei.getUuid()}, null, null, null);
        if (c.moveToNext()) {
            List<Integer> weeks = new ArrayList<>();
            String[] strs = c.getString(2).split(",");
            for (int i = 0; i < strs.length; i++) {
                if (!strs[i].isEmpty()) weeks.add(Integer.parseInt(strs[i]));
            }
            if (!weeks.contains(ei.week)) return false;
            else weeks.remove((Object) ei.week);
            if (weeks.size() == 0) {
                mSQLiteDatabase.delete("timetable", "uuid=?", new String[]{ei.getUuid()});
            } else {
                String newWeeks = getWeeksText(weeks);
                ContentValues cv = new ContentValues();
                cv.put("weeks", newWeeks);
                mSQLiteDatabase.update("timetable", cv, "uuid=?", new String[]{ei.getUuid()});
            }
            if (deleteTask) {
                deleteTask(eih.tag4, false);
//                mSQLiteDatabase.delete("task","uuid=?",
//                        new String[]{eih.tag4});
            }
        } else {
            c.close();
            return false;
        }
        c.close();
        return true;
    }

    @WorkerThread
    public boolean deleteEvent(String uuid, int week) {
        SQLiteDatabase mSQLiteDatabase = mDBHelper.getWritableDatabase();
        Cursor c = mSQLiteDatabase.query("timetable", null, "uuid=?",
                new String[]{uuid}, null, null, null);
        if (c.moveToNext()) {
            List<Integer> weeks = new ArrayList<>();
            String[] strs = c.getString(2).split(",");
            for (int i = 0; i < strs.length; i++) {
                if (!strs[i].isEmpty()) weeks.add(Integer.parseInt(strs[i]));
            }
            if (!weeks.contains(week)) return false;
            else weeks.remove((Object) week);
            if (weeks.size() == 0) {
                mSQLiteDatabase.delete("timetable", "uuid=?",
                        new String[]{uuid});
            } else {
                String newWeeks = getWeeksText(weeks);
                ContentValues cv = new ContentValues();
                cv.put("weeks", newWeeks);
                mSQLiteDatabase.update("timetable", cv, "uuid=?",
                        new String[]{uuid});
            }
        } else {
            c.close();
            return false;
        }
        c.close();
        return true;
    }

    @WorkerThread
    public boolean deleteEvent(Calendar from, Calendar to, int type) {
        List<EventItem> temp = getEventFrom(from, to, type);
        if (temp == null || temp.size() == 0) return false;
        for (EventItem e : temp) {
            deleteEvent(e, true);
        }
        return temp.size() > 0;
    }

    @WorkerThread
    public boolean deleteTask(Task ta) {
        SQLiteDatabase sd = mDBHelper.getWritableDatabase();
        try {
            if (ta.has_deadline && !ta.ddlName.equals("null")) {
                String ddlUUID = ta.ddlName.split(":::")[0];
                int week = Integer.parseInt(ta.ddlName.split(":::")[1]);
                deleteEvent(ddlUUID, week);
            }
            for (String key : ta.getEvent_map().keySet()) {
                String EIuuid = key.split(":::")[0];
                sd.delete("timetable", "uuid=?", new String[]{EIuuid});
            }
            return sd.delete("task", Task.QUERY_SELECTION, ta.getQueryParams()) != 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @WorkerThread
    public boolean deleteTask(String uuid, boolean deleteDDL) {
        SQLiteDatabase sd = mDBHelper.getWritableDatabase();
        Task ta;
        if (TextUtils.isEmpty(uuid)) return false;
        Cursor c = sd.query("task", null, "uuid=?", new String[]{uuid}, null, null, null);
        if (c.moveToNext()) {
            ta = new Task(c);
            c.close();
        } else {
            c.close();
            return false;
        }
        try {
            if (deleteDDL && ta.has_deadline && !ta.ddlName.equals("null")) {
                String ddlUUID = ta.ddlName.split(":::")[0];
                int week = Integer.parseInt(ta.ddlName.split(":::")[1]);
                deleteEvent(ddlUUID, week);
            }
            for (String key : ta.getEvent_map().keySet()) {
                String EIuuid = key.split(":::")[0];
                sd.delete("timetable", "uuid=?", new String[]{EIuuid});
            }
            return sd.delete("task", Task.QUERY_SELECTION, ta.getQueryParams()) != 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @WorkerThread
    public boolean setFinishTask(Task ta, boolean finished) {
        Log.e("finishe:", ta.name);
        try {
            if (ta.has_deadline && !ta.ddlName.equals("null")) {
                String ddlUUID = ta.ddlName.split(":::")[0];
                int week = Integer.parseInt(ta.ddlName.split(":::")[1]);
                deleteEvent(ddlUUID, week);
            }
            ta.setFinished(finished);
            SQLiteDatabase sd = mDBHelper.getWritableDatabase();
            return sd.update("task", ta.getContentValues(), "uuid=?", new String[]{ta.getUuid()}) != 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    @WorkerThread
    public void clearEvent(int type) {
        SQLiteDatabase mSQLiteDatabase = mDBHelper.getWritableDatabase();
        mSQLiteDatabase.delete("timetable", "type=?", new String[]{type + ""});
    }

    @WorkerThread
    public void clearTask(String tagContains) {
        SQLiteDatabase mSQLiteDatabase = mDBHelper.getWritableDatabase();
        Cursor c = mSQLiteDatabase.query("task", null, "tag like?", new String[]{"%" + tagContains + "%"}, null, null, null);
        while (c.moveToNext()) {
            Task t = new Task(c);
            for (String x : t.getEvent_map().keySet()) {
                String uuid = x.split(":::")[0];
                mSQLiteDatabase.delete("timetable", "uuid=?", new String[]{uuid});
            }
            mSQLiteDatabase.delete("task", "uuid=?", new String[]{t.getUuid()});
        }
        c.close();

    }

    @WorkerThread
    public void clearEvent(int type, String name) {
        SQLiteDatabase mSQLiteDatabase = mDBHelper.getWritableDatabase();
        mSQLiteDatabase.delete("timetable", "type=? and name=?", new String[]{type + "", name});
    }

    @WorkerThread
    public void clearCurriculum(String curriculumCode) {
        SQLiteDatabase mSQLiteDatabase = mDBHelper.getWritableDatabase();
        mSQLiteDatabase.delete("timetable", "curriculum_code=? AND type=?", new String[]{curriculumCode + "", TIMETABLE_EVENT_TYPE_COURSE + ""});
    }

    public static boolean contains_integer(int[] array, int object) {
        for (int x : array) if (x == object) return true;
        return false;
    }


    @WorkerThread
    /*函数功能：获取从从某周某天某点到某周某天某点内的所有事件列表*/
    public List<EventItem> getEventFrom_typeLimit(int f_week, int f_dayOfWeek, HTime start, int t_week, int t_dayOfWeek, HTime end, int[] types) {
        // System.out.println("开始查询,共有事件"+getEventsWithinWeeks(f_week,t_week).size()+"个");
        if (t_dayOfWeek == -1) t_dayOfWeek = getCurrentCurriculum().getTotalWeeks();
        if (f_week > t_week) return null;
        else if (f_week == t_week) {
            if (f_dayOfWeek > t_dayOfWeek) return null;
            else if (f_dayOfWeek == t_dayOfWeek) {
                if (start.compareTo(end) > 0) return null;
            }
        }
        List<EventItem> result = new ArrayList<EventItem>();
        if (f_week > getCurrentCurriculum().getTotalWeeks() || t_week > getCurrentCurriculum().getTotalWeeks() || f_week <= 0 || t_week <= 0)
            return null;
        for (EventItem ei : getEventsWithinWeeks(f_week, t_week)) {
            if ((types != null && types.length != 0 && !contains_integer(types, ei.eventType)) || (ei.week == f_week && ei.DOW < f_dayOfWeek) || (ei.week == t_week && ei.DOW > t_dayOfWeek))
                continue;
            if ((ei.week == f_week && ei.DOW == f_dayOfWeek)) {
                if (f_dayOfWeek == t_dayOfWeek) {
                    if (ei.hasCross(start) || (ei.startTime.compareTo(start) >= 0 && ei.startTime.compareTo(end) <= 0))
                        result.add(ei);
                } else {
                    if (ei.hasCross(start) || (ei.startTime.compareTo(start) >= 0)) result.add(ei);
                }
            } else if (ei.week == t_week && ei.DOW == t_dayOfWeek) {
                if ((ei.endTime.compareTo(end) <= 0) || ei.hasCross(end)) result.add(ei);
            } else {
                result.add(ei);
            }
        }
        return result;
    }//type<0表示所有类型

    @WorkerThread
    public List<EventItem> getEventFrom(int f_week, int f_dayOfWeek, HTime start, int t_week, int t_dayOfWeek, HTime end, int type) {
        // System.out.println("开始查询,共有事件"+getEventsWithinWeeks(f_week,t_week).size()+"个");
        if (t_dayOfWeek == -1) t_dayOfWeek = getCurrentCurriculum().getTotalWeeks();
        if (f_week > t_week) return null;
        else if (f_week == t_week) {
            if (f_dayOfWeek > t_dayOfWeek) return null;
            else if (f_dayOfWeek == t_dayOfWeek) {
                if (start.compareTo(end) > 0) return null;
            }
        }
        List<EventItem> result = new ArrayList<EventItem>();
        if (f_week > getCurrentCurriculum().getTotalWeeks() || t_week > getCurrentCurriculum().getTotalWeeks() || f_week <= 0 || t_week <= 0)
            return null;
        for (EventItem ei : getEventsWithinWeeks(f_week, t_week)) {
            if ((type > 0 && ei.eventType != type) || (ei.week == f_week && ei.DOW < f_dayOfWeek) || (ei.week == t_week && ei.DOW > t_dayOfWeek))
                continue;
            if ((ei.week == f_week && ei.DOW == f_dayOfWeek)) {
                if (f_dayOfWeek == t_dayOfWeek) {
                    if (ei.hasCross(start) || (ei.startTime.compareTo(start) >= 0 && ei.startTime.compareTo(end) <= 0))
                        result.add(ei);
                } else {
                    if (ei.hasCross(start) || (ei.startTime.compareTo(start) >= 0)) result.add(ei);
                }
            } else if (ei.week == t_week && ei.DOW == t_dayOfWeek) {
                if ((ei.endTime.compareTo(end) <= 0) || ei.hasCross(end)) result.add(ei);
            } else {
                result.add(ei);
            }
        }
        return result;
    }//type<0表示所有类型

    @WorkerThread
    public List<EventItem> getEventFrom(int f_week, int f_dayOfWeek, HTime start, int t_week, int t_dayOfWeek, HTime end) {
        //System.out.println("开始查询,共有事件"+getEventsWithinWeeks(f_week,t_week).size()+"个");
        if (t_dayOfWeek == -1) t_dayOfWeek = getCurrentCurriculum().getTotalWeeks();
        if (f_week > t_week) return null;
        else if (f_week == t_week) {
            if (f_dayOfWeek > t_dayOfWeek) return null;
            else if (f_dayOfWeek == t_dayOfWeek) {
                if (start.compareTo(end) > 0) return null;
            }
        }
        List<EventItem> result = new ArrayList<EventItem>();
        if (f_week > getCurrentCurriculum().getTotalWeeks() || t_week > getCurrentCurriculum().getTotalWeeks() || f_week <= 0 || t_week <= 0)
            return null;
        for (EventItem ei : getEventsWithinWeeks(f_week, t_week)) {
            if ((ei.week == f_week && ei.DOW < f_dayOfWeek) || (ei.week == t_week && ei.DOW > t_dayOfWeek))
                continue;
            if ((ei.week == f_week && ei.DOW == f_dayOfWeek)) {
                if (f_dayOfWeek == t_dayOfWeek) {
                    if (ei.hasCross(start) || (ei.startTime.compareTo(start) >= 0 && ei.startTime.compareTo(end) <= 0))
                        result.add(ei);
                } else {
                    if (ei.hasCross(start) || (ei.startTime.compareTo(start) >= 0)) result.add(ei);
                }
            } else if (ei.week == t_week && ei.DOW == t_dayOfWeek) {
                if ((ei.endTime.compareTo(end) <= 0) || ei.hasCross(end)) result.add(ei);
            } else {
                result.add(ei);
            }
        }
        return result;
    }

    @WorkerThread
    public List<EventItem> getUnfinishedEvent(Calendar time, int type) {
        List<EventItem> result = new ArrayList<>();
        SQLiteDatabase sd = mDBHelper.getReadableDatabase();
        Cursor c = sd.query("timetable", null, "type=?", new String[]{type + ""}, null, null, null);
        while (c.moveToNext()) {
            result.addAll(new EventItemHolder(c).getAllEvents());
        }
        c.close();
        Log.e("getUnfinishedDDL", String.valueOf(result));
        List<EventItem> toRemove = new ArrayList<>();
        for (EventItem ei : result) {
            if (ei.hasPassed(time)) toRemove.add(ei);
        }
        result.removeAll(toRemove);
        return result;
    }

    @WorkerThread
    public List<EventItem> getEventWithInfoContains(String text) {
        List<EventItem> result = new ArrayList<>();
        SQLiteDatabase sd = mDBHelper.getReadableDatabase();
        String q = "%" + text + "%";
        Cursor c = sd.query("timetable", null,
                "name LIKE ? OR tag2 LIKE ? OR tag3 LIKE ? OR tag4 LIKE ?", new String[]{q, q, q, q}, null, null, null);
        while (c.moveToNext()) {
            result.addAll(new EventItemHolder(c).getAllEvents());
        }
        c.close();
        return result;
    }

    @WorkerThread
    public List<EventItem> getEventWithInfoContainsAll(List<String> texts) {
        List<EventItem> res = new ArrayList<>();
        List<EventItem> UnderTimeCondition = getEventWithInfoContains(texts.get(0));
        for (EventItem ei : UnderTimeCondition) {
            boolean allMatched = true;
            Log.e("course", String.valueOf(ei));
            for (String cdt : texts) {
                Log.e("cdt",cdt);
                boolean match = TextTools.likeWithContain(ei.getMainName(), cdt)
                        || TextTools.likeWithContain(ei.getTag2(), cdt)
                        || TextTools.likeWithContain(ei.getTag3(), cdt)
                        || TextTools.likeWithContain(ei.getTag4(), cdt);
                if (!match) {
                    allMatched = false;
                    break;
                }
            }
            Log.e("all_mat", String.valueOf(allMatched));
            if (allMatched) res.add(ei);
        }
        return res;
    }


    @WorkerThread
    public List<EventItem> getEventFrom(Calendar from, Calendar to, int type) {
        List<EventItem> result = new ArrayList<EventItem>();
        int f_week = getCurrentCurriculum().getWeekOfTerm(from);
        int tempDOW1 = from.get(Calendar.DAY_OF_WEEK);
        int f_dayOfWeek = tempDOW1 == 1 ? 7 : tempDOW1 - 1;
        HTime start = new HTime(from);
        int t_week = getCurrentCurriculum().getWeekOfTerm(to);
        int tempDOW2 = to.get(Calendar.DAY_OF_WEEK);
        int t_dayOfWeek = tempDOW2 == 1 ? 7 : tempDOW2 - 1;
        HTime end = new HTime(to);
        //System.out.println("开始查询,共有事件"+getEventsWithinWeeks(f_week,t_week).size()+"个");

        // Log.e("getEventFrom",f_week+","+f_dayOfWeek+","+start.tellTime()+",,,"+t_week+","+t_dayOfWeek+","+end.tellTime());
        if (t_dayOfWeek == -1) t_dayOfWeek = getCurrentCurriculum().getTotalWeeks();
        if (f_week > t_week) return null;
        else if (f_week == t_week) {
            if (f_dayOfWeek > t_dayOfWeek) return null;
            else if (f_dayOfWeek == t_dayOfWeek) {
                if (start.compareTo(end) > 0) return null;
            }
        }

        if (f_week > getCurrentCurriculum().getTotalWeeks() || t_week > getCurrentCurriculum().getTotalWeeks() || f_week <= 0 || t_week <= 0)
            return null;
        for (EventItem ei : getEventsWithinWeeks(f_week, t_week)) {
            if ((type > 0 && ei.eventType != type) || (ei.week == f_week && ei.DOW < f_dayOfWeek) || (ei.week == t_week && ei.DOW > t_dayOfWeek))
                continue;
            if ((ei.week == f_week && ei.DOW == f_dayOfWeek)) {
                if (f_dayOfWeek == t_dayOfWeek) {
                    if (ei.hasCross(start) || (ei.startTime.compareTo(start) >= 0 && ei.startTime.compareTo(end) <= 0))
                        result.add(ei);
                } else {
                    if (ei.hasCross(start) || (ei.startTime.compareTo(start) >= 0)) result.add(ei);
                }
            } else if (ei.week == t_week && ei.DOW == t_dayOfWeek) {
                if (ei.endTime.compareTo(end) <= 0 || ei.hasCross(end)) result.add(ei);
            } else {
                result.add(ei);
            }
        }
        return result;
    } //type<0表示所有类型


    @WorkerThread
    public List<EventItem> getAllEvents() {
        List<EventItem> result = new ArrayList<>();
        SQLiteDatabase sd = mDBHelper.getReadableDatabase();
        Cursor c = sd.query("timetable", null, "curriculum_code=?", new String[]{getCurrentCurriculum().getCurriculumCode()}, null, null, null);
        while (c.moveToNext()) {
            result.addAll(new EventItemHolder(c).getAllEvents());
        }
        c.close();
        return result;
    }

    public List<EventItem> getOneDayEvents(int week, int DOW) {
        List<EventItem> result = new ArrayList<>();
        if (week <= 0 || week > getCurrentCurriculum().getTotalWeeks()) return result;
        for (EventItem ei : getEventsWithinWeeks(week, week)) {
            if (ei.DOW == DOW) result.add(ei);
        }
        return result;
    }

    @WorkerThread
    public Task getTaskWithUUID(String uuid) {
        Task result = null;
        if (uuid == null || TextUtils.isEmpty(uuid)) return null;
        try {
            SQLiteDatabase sd = mDBHelper.getReadableDatabase();
            Cursor c = sd.query("task", null, "uuid=?", new String[]{uuid}, null, null, null);
            if (c.moveToNext()) {
                result = new Task(c);
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @WorkerThread
    public EventItemHolder getEventItemHolderWithUUID(String uuid) {
        EventItemHolder result = null;
        SQLiteDatabase sd = mDBHelper.getReadableDatabase();
        Cursor c = sd.query("timetable", null, "uuid=?", new String[]{uuid}, null, null, null);
        if (c.moveToNext()) {
            result = new EventItemHolder(c);
        }
        c.close();
        return result;
    }
//    public void addTask(String name){
//        Task t = new Task(getCurrentCurriculum().getCurriculumId(),name);
//        SQLiteDatabase sd = mDBHelper.getWritableDatabase();
//        sd.insert("task",null,t.getContentValues());
//    }
//    public void addTask(String name,int fW,int fDOW,int tW,int tDOW,HTime sTime,HTime tTime,String ddlName){
//        Task t = new Task(getCurrentCurriculum().getCurriculumId(),name,fW,fDOW,tW,tDOW,sTime,tTime,ddlName);
//        SQLiteDatabase sd = mDBHelper.getWritableDatabase();
//        sd.insert("task",null,t.getContentValues());
//    }

    public String addTask(Task t) {
        SQLiteDatabase sd = mDBHelper.getWritableDatabase();
        sd.insert("task", null, t.getContentValues());
        return t.getUuid();
    }

    @WorkerThread
    public String addTask(String name, int fW, int fDOW, HTime sTime, int tW, int tDOW, HTime eTime, String ddlUUID) {
        SQLiteDatabase sd = mDBHelper.getWritableDatabase();
        Task t = new Task(getCurrentCurriculum().getCurriculumCode(), name, fW, fDOW, sTime, tW, tDOW, eTime, ddlUUID);
        sd.insert("task", null, t.getContentValues());
        return t.getUuid();
    }

    @WorkerThread
    public ArrayList<Task> getUnfinishedTasks() {
        ArrayList<Task> res = new ArrayList<>();
        if (!isDataAvailable()) return res;
        SQLiteDatabase sld = mDBHelper.getReadableDatabase();
        Cursor c = sld.query("task", null, "curriculum_code=? and finished=?", new String[]{getCurrentCurriculum().getCurriculumCode(), 0 + ""}, null, null, null);
        while (c.moveToNext()) {
            res.add(new Task(c));
        }
        c.close();
        return res;
    }

    @WorkerThread
    public ArrayList<Task> getfinishedTasks() {
        ArrayList<Task> res = new ArrayList<>();
        if (!isDataAvailable()) return res;
        SQLiteDatabase sld = mDBHelper.getReadableDatabase();
        Cursor c = sld.query("task", null, "curriculum_code=? and finished=?", new String[]{getCurrentCurriculum().getCurriculumCode(), 1 + ""}, null, null, null);
        while (c.moveToNext()) {
            res.add(new Task(c));
        }
        c.close();
        return res;
    }

    @WorkerThread
    public ArrayList<Task> getUnfinishedTaskWithLength() {
        ArrayList<Task> res = new ArrayList<>();
        if (!isDataAvailable()) return res;
        SQLiteDatabase sld = mDBHelper.getReadableDatabase();
        Cursor c = sld.query("task", null, "curriculum_code=? and has_length=? and finished=?", new String[]{getCurrentCurriculum().getCurriculumCode(), 1 + "", 0 + ""}, null, null, null);
        while (c.moveToNext()) {
            res.add(new Task(c));
        }
        c.close();
        return res;
    }

    public static int getDOW(Calendar c) {
        int tempDOW1 = c.get(Calendar.DAY_OF_WEEK);
        return tempDOW1 == 1 ? 7 : tempDOW1 - 1;
    }

    @WorkerThread
    public EventItem getCourseAt(int week, int dow, int start, int last) {
        for (EventItem ei : getEventsWithinWeeks(week, week)) {
            if (ei.DOW == dow
                    && ei.startTime.equals(getTimeAtNumber(start, last).get(0))
                    && ei.endTime.equals(getTimeAtNumber(start, last).get(1))
                    && ei.eventType == TIMETABLE_EVENT_TYPE_COURSE) {
                return ei;
            }
        }
        return null;
    }

    public boolean hasOverLapping(int week, int dayOfWeek, EventItem ei) {
        for (EventItem e : getEventsWithinWeeks(week, week)) {
            if (e.DOW != dayOfWeek) continue;
            if (ei.hasOverLapping(e)) return true;
        }
        return false;
    }

    @WorkerThread
    public List<EventItem> getEventsWithinWeeks(int fromW, int toW) {
        SQLiteDatabase mSQLiteDatabase = mDBHelper.getReadableDatabase();
        List<EventItem> result = new ArrayList<>();
        Cursor c = mSQLiteDatabase.query("timetable", null, "curriculum_code=?", new String[]{getCurrentCurriculum().getCurriculumCode() + ""}, null, null, null);
        while (c.moveToNext()) {
            EventItemHolder eih = new EventItemHolder(c);
            //Log.e("!!!",c.getString(0)+"|"+c.getString(2));
            result.addAll(eih.getEventsWithinWeeks(fromW, toW));
        }
        c.close();

        return result;
    }

    public List<TimePeriod> getSpaces(Calendar from, Calendar to, int minDurationMinute, int type) {
        if (from.after(to) || from.get(Calendar.DAY_OF_MONTH) != to.get(Calendar.DAY_OF_MONTH))
            return null;
        List<TimePeriod> result = new ArrayList<>();
        List<EventItem> temp = getEventFrom(from, to, type);
        Collections.sort(temp);
        Log.e("!!!the temp is:", String.valueOf(temp));
        if (temp == null || temp.size() == 0) {
            TimePeriod m = new TimePeriod();
            m.start = new HTime(from);
            m.end = new HTime(to);
            result.add(m);
        } else if (temp.size() == 1) {
            if (temp.get(0).startTime.after(new HTime(from)) && temp.get(0).startTime.getDuration(new HTime(from)) >= minDurationMinute) {
                TimePeriod m = new TimePeriod();
                m.start = new HTime(from);
                m.end = temp.get(0).startTime;
                result.add(m);
            }
            if (temp.get(0).endTime.before(new HTime(to)) && temp.get(0).endTime.getDuration(new HTime(to)) >= minDurationMinute) {
                TimePeriod m2 = new TimePeriod();
                m2.end = new HTime(to);
                m2.start = temp.get(0).endTime;
                result.add(m2);
            }
        } else {
            for (int i = 0; i < temp.size(); i++) {
                TimePeriod m = new TimePeriod();
                Log.e("event:", temp.get(i).toString());
                if (i == 0) {
                    if (temp.get(i).startTime.after(new HTime(from)) && temp.get(i).startTime.getDuration(new HTime(from)) >= minDurationMinute) {
                        m.start = new HTime(from);
                        m.end = temp.get(0).startTime;
                        Log.e("add:first", m.toString());
                        result.add(m);
                    }
                } else if (i == temp.size() - 1) {
                    if (temp.get(i).endTime.before(new HTime(to)) && temp.get(i).endTime.getDuration(new HTime(to)) >= minDurationMinute) {
                        m.end = new HTime(to);
                        m.start = temp.get(i).endTime;
                        result.add(m);
                        Log.e("add:last", m.toString());
                    }
                }

                if (i + 1 < temp.size() && temp.get(i).endTime.getDuration(temp.get(i + 1).startTime) >= minDurationMinute) {
                    TimePeriod m3 = new TimePeriod();
                    m3.start = temp.get(i).endTime;
                    m3.end = temp.get(i + 1).startTime;
                    Log.e("add:normal", m3.toString());
                    result.add(m3);

                }

            }

        }
        Collections.sort(result);
        return result;
    }

    public List<TimePeriod> getSpaces(List<EventItem> breakT, Calendar from, Calendar to, int minDurationMinute, int type) {
        if (from.after(to) || from.get(Calendar.DAY_OF_MONTH) != to.get(Calendar.DAY_OF_MONTH))
            return null;
        List<TimePeriod> result = new ArrayList<>();
        List<EventItem> temp = getEventFrom(from, to, type);
        temp.addAll(breakT);
        Collections.sort(temp);
        Log.e("temp event is:", temp.toString());
        if (temp == null || temp.size() == 0) {
            TimePeriod m = new TimePeriod();
            m.start = new HTime(from);
            m.end = new HTime(to);
            result.add(m);
        } else if (temp.size() == 1) {
            if (temp.get(0).startTime.after(new HTime(from)) && temp.get(0).startTime.getDuration(new HTime(from)) >= minDurationMinute) {
                TimePeriod m = new TimePeriod();
                m.start = new HTime(from);
                m.end = temp.get(0).startTime;
                result.add(m);
            }
            if (temp.get(0).endTime.before(new HTime(to)) && temp.get(0).endTime.getDuration(new HTime(to)) >= minDurationMinute) {
                TimePeriod m2 = new TimePeriod();
                m2.end = new HTime(to);
                m2.start = temp.get(0).endTime;
                result.add(m2);
            }
        } else {
            for (int i = 0; i < temp.size(); i++) {
                TimePeriod m = new TimePeriod();
                Log.e("event:", temp.get(i).toString());
                if (i == 0) {
                    if (temp.get(i).startTime.after(new HTime(from)) && temp.get(i).startTime.getDuration(new HTime(from)) >= minDurationMinute) {
                        m.start = new HTime(from);
                        m.end = temp.get(0).startTime;
                        Log.e("add:first", m.toString());
                        result.add(m);
                    }
                } else if (i == temp.size() - 1) {
                    if (temp.get(i).endTime.before(new HTime(to)) && temp.get(i).endTime.getDuration(new HTime(to)) >= minDurationMinute) {
                        m.end = new HTime(to);
                        m.start = temp.get(i).endTime;
                        result.add(m);
                        Log.e("add:last", m.toString());
                    }
                }

                if (i + 1 < temp.size() && temp.get(i).endTime.getDuration(temp.get(i + 1).startTime) >= minDurationMinute) {
                    TimePeriod m3 = new TimePeriod();
                    m3.start = temp.get(i).endTime;
                    m3.end = temp.get(i + 1).startTime;
                    Log.e("add:normal", m3.toString());
                    result.add(m3);

                }

            }

        }
        HTime fromH = new HTime(from);
        HTime toH = new HTime(to);
        //Log.e("spaces_beforeRemove", String.valueOf(result));
        List<TimePeriod> tpToRemove = new ArrayList<>();
        for (TimePeriod tp : result) {
            if (tp.before(fromH)) tpToRemove.add(tp);
            if (tp.hasCross(fromH)) {
                tp.start.setTime(fromH.hour, fromH.minute);
            }
            if (tp.hasCross(toH)) tp.end.setTime(toH.hour, toH.minute);
        }
        result.removeAll(tpToRemove);
        Collections.sort(result);
        return result;
    }

    private String getWeeksText(List<Integer> weeks) {
        String res = "";
        for (Integer x : weeks) {
            res = res + x + ",";
        }
        if (res.endsWith(",")) res = res.substring(0, res.length() - 1);
        return res;
    }
}
