package com.stupidtree.hita.timetable;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.WorkerThread;

import com.stupidtree.hita.R;
import com.stupidtree.hita.hita.TextTools;
import com.stupidtree.hita.online.UserData;
import com.stupidtree.hita.timetable.packable.Curriculum;
import com.stupidtree.hita.timetable.packable.EventItem;
import com.stupidtree.hita.timetable.packable.EventItemHolder;
import com.stupidtree.hita.timetable.packable.HTime;
import com.stupidtree.hita.timetable.packable.Subject;
import com.stupidtree.hita.timetable.packable.Task;
import com.stupidtree.hita.timetable.packable.TimePeriod;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.TPE;
import static com.stupidtree.hita.HITAApplication.defaultSP;
import static com.stupidtree.hita.HITAApplication.timeServiceBinder;
import static com.stupidtree.hita.HITAApplication.timeTableCore;
import static com.stupidtree.hita.timetable.CurriculumCreator.CURRICULUM_TYPE_COURSE;

public class TimetableCore {
    public final static int COURSE = 1;
    public final static int EXAM = 2;
    public final static int ARRANGEMENT = 3;
    public final static int DDL = 5;
    public final static int DYNAMIC = 6;
    private Curriculum currentCurriculum = null;
    private String currentCurriculumId = null;
    private boolean isThisTerm = true;
    private int thisWeekOfTerm = -1;
    private Calendar now;
    private ContentResolver contentResolver;
    public static Uri uri_timetable = Uri.parse("content://com.stupidtree.hita.provider/timetable");
    public static Uri uri_task = Uri.parse("content://com.stupidtree.hita.provider/task");
    public static Uri uri_subject = Uri.parse("content://com.stupidtree.hita.provider/subject");
    private static Uri uri_curriculum = Uri.parse("content://com.stupidtree.hita.provider/curriculum");

    public TimetableCore(ContentResolver contentResolver) {
        now = Calendar.getInstance();
        this.contentResolver = contentResolver;
    }

    public static List<HTime> getTimeAtNumber(int begin, int last) {
        int[] startDots = {830, 930, 1030, 1130, 1345, 1440, 1545, 1640, 1830, 1925, 2030, 2125, 2230};
        int[] endDots = {920, 1015, 1120, 1215, 1435, 1530, 1635, 1730, 1920, 2015, 2120, 2215, 2320};
        List<HTime> temp = new ArrayList<>();
        HTime startTime, endTime;
        int sH, sM, eH, eM;
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

    public void onTerminate() {
        // mDBHelper.getWritableDatabase().close();
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

        contentResolver.delete(uri_curriculum, "curriculum_code=? and name=?", new String[]{toDel.getCurriculumCode(), toDel.getName()});
        contentResolver.delete(uri_timetable, "curriculum_code=?", new String[]{toDel.getCurriculumCode()});
        contentResolver.delete(uri_task, "curriculum_code=?", new String[]{toDel.getCurriculumCode()});
        contentResolver.delete(uri_task, "curriculum_code=?", new String[]{toDel.getCurriculumCode()});

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

    //    @WorkerThread
//    public ArrayList<Subject> getSubjects(String curriculumCode) {
//        ArrayList<Subject> res = new ArrayList<>();
//        try {
//            Cursor c = contentResolver.query(uri_subject, null, "curriculum_code=?", new String[]{curriculumCode}, null, null);
//            while (c != null && c.moveToNext()) {
//                Subject s = new Subject(c);
//                res.add(s);
//            }
//            if (c != null) {
//                c.close();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            contentResolver.delete(uri_subject, null, null);
//        }
//        return res;
//    }
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
            else if (to.before(dots[i].start)) return i;
        }
        return -1;
    }

    public static TimePeriod getClassSimplifiedTimeByTimeContainedIn(HTime time) {
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
        for (TimePeriod dot : dots) {
            if (time.during(dot)) return dot;
        }
        return null;
    }

    public Calendar getNow() {
        now.setTimeInMillis(System.currentTimeMillis());
        return now;
    }

    public boolean isDataAvailable() {
        return currentCurriculum != null;
    }

    public void loadDataFromCloud(UserData.UserDataCloud bud) {
        if (CurrentUser == null) return;
        new writeDataToLocalTask(contentResolver, bud).executeOnExecutor(TPE);
    }

    public void loadDataFromCloud(final Activity toFinish) {
        if (CurrentUser == null) return;
        BmobQuery<UserData.UserDataCloud> query = new BmobQuery<>();
        query.addWhereEqualTo("user", CurrentUser);
        query.findObjects(new FindListener<UserData.UserDataCloud>() {
            @Override
            public void done(List<UserData.UserDataCloud> list, BmobException e) { //如果done里面其他的函数出错，会再执行一次done抛出异常！！！
                Log.e("下载", "done");
                if (e == null && list != null && list.size() > 0) {
                    new writeDataToLocalTask(contentResolver, list.get(0), toFinish).executeOnExecutor(TPE);
                } else {
                    if (toFinish != null) toFinish.finish();
                    Log.e("下载失败", e == null ? "空结果" : e.toString());
                }
            }
        });
    }

    @WorkerThread
    public void clearData() {

        contentResolver.delete(uri_timetable, null, null);
        contentResolver.delete(uri_timetable, null, null);
        contentResolver.delete(uri_task, null, null);
        contentResolver.delete(uri_subject, null, null);
        currentCurriculum = null;
        currentCurriculumId = null;
    }

    @SuppressLint("ApplySharedPref")
    @WorkerThread
    public boolean addCurriculum(CurriculumCreator il, boolean coverSubject) {
        if (il == null) return false;
        Curriculum cur = il.getCurriculum();
        if (cur.getWeekOfTerm(timeTableCore.getNow()) > cur.getTotalWeeks())
            cur.setTotalWeeks(cur.getWeekOfTerm(timeTableCore.getNow()));
        contentResolver.delete(uri_curriculum, "curriculum_code=?", new String[]{il.getCurriculumCode()});
        contentResolver.insert(uri_curriculum, cur.getContentValues());
        //if(clearSubject)contentResolver.delete(uri_subject, "curriculum_code=?", new String[]{il.getCurriculumCode()});
        contentResolver.delete(uri_timetable, "curriculum_code=? and type=?", new String[]{il.getCurriculumCode(), TimetableCore.COURSE + ""});
        for (CurriculumCreator.CurriculumItem ci : il.getCurriculumList()) {
            if (ci.type == CURRICULUM_TYPE_COURSE) {
                StringBuilder tag4 = new StringBuilder(ci.begin + "");
                for (int i = 1; i < ci.last; i++) {
                    tag4.append(",").append(ci.begin + i);
                }
                EventItemHolder eih = new EventItemHolder(il.getCurriculumCode(), COURSE, ci.name, ci.place, ci.tag, tag4.toString(), getTimeAtNumber(ci.begin, ci.last).get(0), getTimeAtNumber(ci.begin, ci.last).get(1), ci.DOW, ci.weeks, false
                );
                contentResolver.insert(uri_timetable, eih.getContentValues());
            } else {
                addEvent(ci.weeks.get(0), ci.DOW, EXAM, ci.name, ci.place, ci.name, ci.tag, ci.begin, ci.last, false);
            }
        }

        for (Subject s : il.getSubjects()) {
            String ratesText, scoresText;
            Cursor c = contentResolver.query(uri_subject, null, "curriculum_code =? AND name=?",
                    new String[]{il.getCurriculumCode(), s.getName()}, null, null);
            if (c != null && c.moveToNext()) {
                ratesText = c.getString(c.getColumnIndex("rates"));
                scoresText = c.getString(c.getColumnIndex("scores"));
                ContentValues cv = s.getContentValues();
                cv.put("rates", ratesText);
                cv.put("scores", scoresText);
                String uuid = c.getString(c.getColumnIndex("uuid"));
                if (TextUtils.isEmpty(uuid)) {
                    if (coverSubject) {
                        cv.put("uuid", UUID.randomUUID().toString());
                        contentResolver.update(uri_subject, cv, "name=? AND curriculum_code=?", new String[]{il.getCurriculumCode(), s.getName()});
                    } else {
                        Subject temp = new Subject(c);
                        temp.setUUID(UUID.randomUUID().toString());
                        contentResolver.update(uri_subject, temp.getContentValues(), "name=? AND curriculum_code=?", new String[]{il.getCurriculumCode(), s.getName()});
                    }
                } else {
                    if (coverSubject) {
                        cv.put("uuid", uuid);
                        contentResolver.update(uri_subject, cv, "uuid=?", new String[]{uuid});
                    }
                }
            } else {
                if (TextUtils.isEmpty(s.getUUID())) {
                    s.setUUID(UUID.randomUUID().toString());
                }
                contentResolver.insert(uri_subject, s.getContentValues());
            }
            if (c != null) {
                c.close();
            }

        }

        currentCurriculumId = cur.getCurriculumCode();
        currentCurriculum = cur;
        defaultSP.edit().putString("current_curriculum", currentCurriculumId).commit();

        return true;
    }


    public boolean isThisTerm() {
        return isThisTerm;
    }


    public int getThisWeekOfTerm() {
        return thisWeekOfTerm;
    }


    public void syncTimeFlags() {

        if (isDataAvailable()) {
            thisWeekOfTerm = currentCurriculum.getWeekOfTerm(timeTableCore.getNow());
            isThisTerm = thisWeekOfTerm > 0;
            if (thisWeekOfTerm > currentCurriculum.getTotalWeeks()) {
                currentCurriculum.setTotalWeeks(thisWeekOfTerm);
            }
        } else {
            thisWeekOfTerm = -1;
            isThisTerm = false;
        }

    }


    public Curriculum getCurrentCurriculum() {
        return currentCurriculum;
    }

    private Curriculum getCurriculumAtId(String id) {
        if (id == null) return null;
        Cursor c = contentResolver.query(uri_curriculum, null, "curriculum_code=?", new String[]{id}, null, null);
        //ArrayList temp1 = FileOperator.loadCurriculumFromFile(this.getFilesDir());
        if (c != null && c.moveToNext()) {
            Curriculum res = new Curriculum(c);
            c.close();
            return res;
        }
        if (c != null) {
            c.close();
        }
        return null;
    }

    @WorkerThread
    public void insertSubject(Subject subject) {
        contentResolver.insert(uri_subject, subject.getContentValues());

    }

    Task getTaskByTag(String tag) {
        Cursor c = contentResolver.query(uri_task, null, "tag=?", new String[]{tag}, null, null);
        if (c != null && c.moveToNext()) {
            Task t = new Task(c);
            c.close();
            return t;
        } else {
            if (c != null) {
                c.close();
            }
            return null;
        }
    }

    @WorkerThread
    public void saveSubject(Subject subject) {
        contentResolver.update(uri_subject, subject.getContentValues(), "uuid=?", new String[]{subject.getUUID()});
    }

    @WorkerThread
    public void saveSubject(Subject subject, String where, String[] whereArg) {
        contentResolver.update(uri_subject, subject.getContentValues(), where, whereArg);
    }

    @WorkerThread
    public void saveCurriculum(Curriculum c) {
        if (contentResolver.update(uri_curriculum, c.getContentValues(), "curriculum_code=?", new String[]{c.getCurriculumCode()}) == 0) {
            contentResolver.insert(uri_curriculum, c.getContentValues());
        }
    }

    @WorkerThread
    public List<Subject> getSubjectsByCourseCode(String curriculumCode, String code) {
        if (curriculumCode == null) curriculumCode = currentCurriculumId;
        List<Subject> result = new ArrayList<>();
        Cursor c = contentResolver.query(uri_subject, null, "code=? and curriculum_code=?", new String[]{code, curriculumCode}, null, null);
        while (c != null && c.moveToNext()) {
            Subject s = new Subject(c);
            result.add(s);
        }
        if (c != null) {
            c.close();
        }
        return result;
    }


    @WorkerThread
    public Subject getSubjectByCourse(EventItem ei) {
        return getSubjectByName(ei.getCurriculumCode(), ei.getMainName());
    }

    public ArrayList<EventItem> getCourses(Subject subject) {
        ArrayList<EventItem> result = new ArrayList<>();
        Cursor c = contentResolver.query(uri_timetable, null, "name=? and type=? and curriculum_code=?",
                new String[]{subject.getName(), TimetableCore.COURSE + "", subject.getCurriculumId()}, null, null);
        while (c != null && c.moveToNext()) {
            EventItemHolder eih = new EventItemHolder(c);
            result.addAll(eih.getAllEvents());
        }
        if (c != null) {
            c.close();
        }
        return result;
    }

    @WorkerThread
    public EventItem getFirstCourse(Subject subject) {
        EventItem result = null;
        try {
            Cursor c = contentResolver.query(uri_timetable, null, "name=? and type=? and curriculum_code=?",
                    new String[]{subject.getName(), TimetableCore.COURSE + "", subject.getCurriculumId()}, null, null);
            if (c != null && c.moveToNext()) {
                EventItemHolder eih = new EventItemHolder(c);
                result = eih.getAllEvents().get(0);
            }
            if (c != null) {
                c.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @WorkerThread
    public Subject getSubjectByName(String curriculumCode, String name) {
        if (curriculumCode == null) curriculumCode = currentCurriculumId;
        Cursor c = contentResolver.query(uri_subject, null, "name=? and curriculum_code=?", new String[]{name, curriculumCode}, null, null);
        if (c != null && c.moveToNext()) {
            Subject s = new Subject(c);
            c.close();
            return s;
        }
        if (c != null) {
            c.close();
        }
        //找不到，则必须重新生成科目表了
        recreateSubjects(curriculumCode);
        Cursor c2 = contentResolver.query(uri_subject, null, "name=? and curriculum_code=?", new String[]{name, curriculumCode}, null, null);
        if (c2 != null && c2.moveToNext()) {
            Subject s = new Subject(c2);
            c2.close();
            return s;
        }
        if (c2 != null) {
            c2.close();
        }
        return null;
    }

    @WorkerThread
    public Subject getSubjectByCourseCode(String curriculumCode, String code) {
        if (curriculumCode == null) curriculumCode = currentCurriculumId;
        Cursor c = contentResolver.query(uri_subject, null, "code=? and curriculum_code=?", new String[]{code, curriculumCode}, null, null);
        if (c != null && c.moveToNext()) {
            Subject s = new Subject(c);
            c.close();
            return s;
        }
        return null;
    }


    @WorkerThread
    public ArrayList<Subject> getSubjects(String curriculumCode) {
        if (curriculumCode == null) curriculumCode = currentCurriculumId;
        ArrayList<Subject> res = new ArrayList<>();
        try {
            Cursor c = contentResolver.query(uri_subject, null, "curriculum_code=?", new String[]{curriculumCode}, null, null);
            while (c != null && c.moveToNext()) {
                Subject s = new Subject(c);
                res.add(s);
            }
            if (c != null) {
                c.close();
            }
            if (res.size() == 0) {
                Cursor c2 = contentResolver.query(uri_timetable, null, "curriculum_code=? AND type=?", new String[]{curriculumCode, String.valueOf(COURSE)}, null, null);
                if (c2 != null && c2.moveToNext()) { //如果科目表空，但是时间表里存在同名课程的话，说明科目需要重新创建
                    res.addAll(recreateSubjects(curriculumCode));
                }
                if (c2 != null) {
                    c2.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            contentResolver.delete(uri_subject, null, null);
        }
        return res;
    }

    @WorkerThread
    private List<Subject> recreateSubjects(String curriculumCode) {
        if (curriculumCode == null) curriculumCode = currentCurriculumId;
        contentResolver.delete(uri_subject, "curriculum_code=?", new String[]{curriculumCode});
        Cursor c = contentResolver.query(uri_timetable, null, "curriculum_code=? AND type=?", new String[]{curriculumCode, String.valueOf(COURSE)}, null, null);
        List<Subject> subjects = new ArrayList<>();
        while (c != null && c.moveToNext()) {
            EventItemHolder eih = new EventItemHolder(c);
            String name = eih.getMainName();
            boolean contains = false;
            for (Subject s : subjects) {
                if (s.getName().equals(name)) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                subjects.add(new Subject(curriculumCode, name, eih.tag3));
            }
        }
        if (c != null) c.close();
        for (Subject s : subjects) {
            contentResolver.insert(uri_subject, s.getContentValues());
        }
        return subjects;
    }

    @WorkerThread
    public ArrayList<Subject> getSubjects_Exam(String curriculumCode) {
        if (curriculumCode == null) curriculumCode = currentCurriculumId;
        ArrayList<Subject> res = new ArrayList<>();
        try {
            Cursor c = contentResolver.query(uri_subject, null, "curriculum_code=? and is_exam = ?", new String[]{curriculumCode, 1 + ""}, null, null);
            while (c != null && c.moveToNext()) {
                Subject s = new Subject(c);
                res.add(s);
            }
            if (c != null) {
                c.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            contentResolver.delete(uri_subject, null, null);
        }
        return res;
    }

    @WorkerThread
    public ArrayList<Subject> getSubjects_No_Exam(String curriculumCode) {
        if (curriculumCode == null) curriculumCode = currentCurriculumId;
        ArrayList<Subject> res = new ArrayList<>();
        try {
            Cursor c = contentResolver.query(uri_subject, null, "curriculum_code=? and is_exam = ?", new String[]{curriculumCode, 0 + ""}, null, null);
            while (c != null && c.moveToNext()) {
                Subject s = new Subject(c);
                res.add(s);
            }
            if (c != null) {
                c.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            contentResolver.delete(uri_subject, null, null);
        }
        return res;
    }

    @WorkerThread
    public ArrayList<Subject> getSubjects_Mooc(String curriculumCode) {
        if (curriculumCode == null) curriculumCode = currentCurriculumId;
        ArrayList<Subject> res = new ArrayList<>();
        try {
            Cursor c = contentResolver.query(uri_subject, null, "curriculum_code=? and is_mooc = ?", new String[]{curriculumCode, 1 + ""}, null, null);
            while (c != null && c.moveToNext()) {
                Subject s = new Subject(c);
                res.add(s);
            }
            if (c != null) {
                c.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            contentResolver.delete(uri_subject, null, null);
        }
        return res;
    }

    @WorkerThread
    public ArrayList<Subject> getSubjects_Comp(String curriculumCode) {
        if (curriculumCode == null) curriculumCode = currentCurriculumId;
        ArrayList<Subject> res = new ArrayList<>();
        try {
            Cursor c = contentResolver.query(uri_subject, null, "curriculum_code=? and compulsory = ?", new String[]{curriculumCode, "必修"}, null, null);
            while (c != null && c.moveToNext()) {
                Subject s = new Subject(c);
                res.add(s);
            }
            if (c != null) {
                c.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            contentResolver.delete(uri_subject, null, null);
        }
        return res;
    }

    @WorkerThread
    public ArrayList<Subject> getSubjects_Alt(String curriculumCode) {
        if (curriculumCode == null) curriculumCode = currentCurriculumId;
        ArrayList<Subject> res = new ArrayList<>();
        try {
            Cursor c = contentResolver.query(uri_subject, null, "curriculum_code=? and compulsory = ?", new String[]{curriculumCode, "选修"}, null, null);
            while (c != null && c.moveToNext()) {
                Subject s = new Subject(c);
                res.add(s);
            }
            if (c != null) {
                c.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            contentResolver.delete(uri_subject, null, null);
        }
        return res;
    }

    @WorkerThread
    public ArrayList<Subject> getSubjects_WTV(String curriculumCode) {
        if (curriculumCode == null) curriculumCode = currentCurriculumId;
        ArrayList<Subject> res = new ArrayList<>();
        try {
            Cursor c = contentResolver.query(uri_subject, null, "curriculum_code=? and compulsory = ?", new String[]{curriculumCode, "任选"}, null, null);
            while (c != null && c.moveToNext()) {
                Subject s = new Subject(c);
                res.add(s);
            }
            if (c != null) {
                c.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            contentResolver.delete(uri_subject, null, null);
        }
        return res;
    }

    @SuppressLint("ApplySharedPref")
    @WorkerThread
    public void initCoreData() {
        currentCurriculumId = defaultSP.getString("current_curriculum", null);
        if (currentCurriculumId == null) {
            List<Curriculum> all = getAllCurriculum();
            // Log.e("init_size", String.valueOf(all));
            if (all.size() > 0) {
                currentCurriculum = all.get(all.size() - 1);
                currentCurriculumId = currentCurriculum.getCurriculumCode();
                syncTimeFlags();
                if (isDataAvailable() && thisWeekOfTerm > getCurrentCurriculum().getTotalWeeks()) {
                    getCurrentCurriculum().setTotalWeeks(thisWeekOfTerm);
                }
                defaultSP.edit().putString("current_curriculum", currentCurriculumId).commit();
            }
        } else {
            List<Curriculum> all = getAllCurriculum();
            currentCurriculum = getCurriculumAtId(currentCurriculumId);
            // Log.e("init_id", String.valueOf(currentCurriculum));
            syncTimeFlags();
            if (currentCurriculum != null) {
                if (thisWeekOfTerm > getCurrentCurriculum().getTotalWeeks())
                    getCurrentCurriculum().setTotalWeeks(thisWeekOfTerm);
            } else if (all.size() > 0) {
                currentCurriculum = all.get(all.size() - 1);
                currentCurriculumId = currentCurriculum.getCurriculumCode();
                if (isDataAvailable() && thisWeekOfTerm > getCurrentCurriculum().getTotalWeeks())
                    getCurrentCurriculum().setTotalWeeks(thisWeekOfTerm);
                defaultSP.edit().putString("current_curriculum", currentCurriculumId).commit();
            }


        }
        syncTimeFlags();
        // Log.e("cur_text",currentCurriculum.getCurriculumText());
    }

    @WorkerThread
    public List<Curriculum> getAllCurriculum() {
        ArrayList<Curriculum> all = new ArrayList<>();
        Cursor cr = contentResolver.query(uri_curriculum, null, null, null, null, null);
        //ArrayList temp1 = FileOperator.loadCurriculumFromFile(this.getFilesDir());
        while (cr != null && cr.moveToNext()) {
            all.add(new Curriculum(cr));
        }
        if (cr != null) {
            cr.close();
        }
        List<Curriculum> result = new ArrayList<>();
        for (Curriculum c : all) {
            if (currentCurriculum != null && c.getCurriculumCode().equals(currentCurriculum.getCurriculumCode())) {
                result.add(currentCurriculum);
            } else result.add(c);
        }
        return result;
    }

    @WorkerThread
    public boolean saveDataToCloud() {
        if (CurrentUser == null) return false;
        Log.e("开始上传数据", "尝试");
        UserData data = UserData.create(contentResolver);
        data.loadTaskData().loadTimetableData().loadSubjectData().loadCurriculumData(getAllCurriculum());
        final UserData.UserDataCloud userDataCloud = data.getPreparedCloudData(CurrentUser);
        BmobQuery<UserData.UserDataCloud> bq = new BmobQuery<>();
        bq.addWhereEqualTo("user", CurrentUser);
        try {
            List<UserData.UserDataCloud> list = bq.findObjectsSync(UserData.UserDataCloud.class);
            if (list == null || list.size() == 0) userDataCloud.saveSync();
            else {
                list.size();
                Log.e("data", userDataCloud + "");
                // userDataCloud.setObjectId();
                userDataCloud.updateSync(list.get(0).getObjectId());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void saveDataToCloud(final OnDoneListener listener) {
        packUp(new OnPackDoneListener() {
            @Override
            public void onDone(final UserData.UserDataCloud data) {
                BmobQuery<UserData.UserDataCloud> bq = new BmobQuery<>();
                bq.addWhereEqualTo("user", CurrentUser);
                bq.findObjects(new FindListener<UserData.UserDataCloud>() {
                    @Override
                    public void done(final List<UserData.UserDataCloud> list, BmobException e) {
                        if (list == null || list.size() == 0) {
                            data.save(new SaveListener<String>() {
                                @Override
                                public void done(String s, BmobException e) {
                                    if (e == null) listener.onSuccess();
                                    else listener.onFailed(e);
                                }
                            });
                        } else {
                            data.setObjectId(list.get(0).getObjectId());
                            data.update(new UpdateListener() {
                                @Override
                                public void done(BmobException e) {
                                    if (e != null) listener.onFailed(e);
                                    else listener.onSuccess();
                                }
                            });

                        }
                    }
                });
            }

            @Override
            public void onFailed(Exception e) {
                listener.onFailed(e);
            }
        });

    }

    private void packUp(OnPackDoneListener listener) {
        new packUpTask(listener, contentResolver).executeOnExecutor(TPE);
    }

    public void loadDataFromCloud() {
        if (CurrentUser == null) return;
        BmobQuery<UserData.UserDataCloud> query = new BmobQuery<>();
        query.addWhereEqualTo("user", CurrentUser);
        query.findObjects(new FindListener<UserData.UserDataCloud>() {
            @Override
            public void done(List<UserData.UserDataCloud> list, BmobException e) { //如果done里面其他的函数出错，会再执行一次done抛出异常！！！
                Log.e("下载", "done");
                if (e == null && list != null && list.size() > 0) {
                    new writeDataToLocalTask(contentResolver, list.get(0)).execute();
                } else {
//                    Toast.makeText(HContext, R.string.no_data_on_cloud, Toast.LENGTH_SHORT).show();
                    Log.e("下载失败", e == null ? "空结果" : e.toString());
                }
            }
        });


    }

    public void updateCurrentCurriculumInfo(Curriculum other) {
        if (other == null || !currentCurriculum.getCurriculumCode().equals(other.getCurriculumCode()))
            return;
        currentCurriculum.setName(other.getName());
        currentCurriculum.setTotalWeeks(other.getTotalWeeks());
        currentCurriculum.setCurriculumText(other.getCurriculumText());
        currentCurriculum.setStartDate(other.getStartDate());
    }

    @SuppressLint("ApplySharedPref")
    @WorkerThread
    public boolean changeCurrentCurriculum(String newId) {
        Curriculum newC = getCurriculumAtId(newId);
        if (newC != null) {
            currentCurriculumId = newId;
            currentCurriculum = newC;
            syncTimeFlags();
            //setThisWeekOfTerm(newC.getWeekOfTerm(timeTableCore.getNow()));
            if (getThisWeekOfTerm() > newC.getTotalWeeks()) {
                newC.setTotalWeeks(timeTableCore.getThisWeekOfTerm());
            }
            defaultSP.edit().putString("current_curriculum", newId).commit();
            //saveData();
//            timeServiceBinder.refreshProgress();
            return true;
        }
        return false;
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
//
//    @WorkerThread
//    public Subject getSubjectByName(String name) {
//
//        Cursor c = contentResolver.query(uri_subject, null, "name=?", new String[]{name}, null, null);
//        if (c != null && c.moveToNext()) {
//            Subject s = new Subject(c);
//            c.close();
//            return s;
//        }
//        return null;
//    }

    @WorkerThread
    public void deleteSubject(String name, String curriculumId) {
        contentResolver.delete(uri_subject, "name=? AND curriculum_code=?", new String[]{name, curriculumId});
        contentResolver.delete(uri_timetable, "name=? AND curriculum_code=? AND type=?",
                new String[]{name, curriculumId, String.valueOf(COURSE)}
        );
    }

    public void addEvents(List<Integer> weeks, int DOW, int type, String eventName, String tag2, String tag3, String tag4, int begin, int last, boolean isWholeDay) {
        // Log.e("add", weeks + ",dow:" + DOW + ",event:" + eventName + ",from:" + begin + ",last:" + last);
        for (int i : weeks)
            if (i > getCurrentCurriculum().getTotalWeeks()) getCurrentCurriculum().setTotalWeeks(i);
        EventItemHolder temp = new EventItemHolder(getCurrentCurriculum().getCurriculumCode(), type, eventName, tag2, tag3, tag4, getTimeAtNumber(begin, last).get(0), getTimeAtNumber(begin, last).get(1), DOW, isWholeDay);
        temp.weeks.addAll(weeks);
        contentResolver.insert(uri_timetable, temp.getContentValues());
    }

    public String addEvent(EventItem ei) {
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
        EventItemHolder temp = new EventItemHolder(ei.getCurriculumCode(), type, eventName, tag2, tag3, tag4, start, end, DOW, ei.isWholeDay());
        temp.weeks.add(week);
        if (!TextUtils.isEmpty(ei.getUuid())) temp.setUuid(ei.getUuid());
        else ei.setUuid(temp.getUuid());
        Cursor c = contentResolver.query(uri_timetable, null, EventItemHolder.QUERY_SELECTION, temp.getQueryParams(), null, null);
        String uuid = temp.getUuid();
        if (c != null && c.moveToNext()) {
            List<Integer> weeks = new ArrayList<>();
            String[] strs = c.getString(2).split(",");
            for (String str : strs) {
                weeks.add(Integer.parseInt(str));
            }
            if (!weeks.contains(week)) {
                weeks.add(week);
            }
            String newWeeks = getWeeksText(weeks);
            ContentValues cv = temp.getContentValues();
            uuid = c.getString(c.getColumnIndex("uuid"));
            cv.put("weeks", newWeeks);
            cv.put("uuid", uuid);
            contentResolver.update(uri_timetable, cv, EventItemHolder.QUERY_SELECTION, temp.getQueryParams());
        } else {
            Log.e("add", temp.toString());
            contentResolver.insert(uri_timetable, temp.getContentValues());
        }
        if (c != null) {
            c.close();
        }

        return uuid;
    }

    @WorkerThread
    public boolean deleteEvent(EventItem ei, boolean deleteTask) {
        if (ei == null) return false;
        EventItemHolder eih = new EventItemHolder(ei);
        Cursor c = contentResolver.query(uri_timetable, null, "uuid=?",
                new String[]{ei.getUuid()}, null, null);
        if (c != null && c.moveToNext()) {
            List<Integer> weeks = new ArrayList<>();
            String[] strs = c.getString(2).split(",");
            for (String str : strs) {
                if (!str.isEmpty()) weeks.add(Integer.parseInt(str));
            }
            if (!weeks.contains(ei.week)) return false;
            else weeks.remove((Integer) ei.week);
            if (weeks.size() == 0) {
                contentResolver.delete(uri_timetable, "uuid=?", new String[]{ei.getUuid()});
            } else {
                String newWeeks = getWeeksText(weeks);
                ContentValues cv = new ContentValues();
                cv.put("weeks", newWeeks);
                contentResolver.update(uri_timetable, cv, "uuid=?", new String[]{ei.getUuid()});
            }
            if (deleteTask) {
                deleteTask(eih.tag4, false);
//                contentResolver.delete(uri_task,"uuid=?",
//                        new String[]{eih.tag4});
            }
        } else {
            if (c != null) {
                c.close();
            }
            return false;
        }
        c.close();
        return true;
    }

    /*函数功能：添加事件*/
    public String addEvent(int week, int DOW, int type, String eventName, String tag2, String tag3, String tag4, int begin, int last, boolean isWholeDay) {
        if (week > getCurrentCurriculum().getTotalWeeks())
            getCurrentCurriculum().setTotalWeeks(week);
        EventItemHolder temp = new EventItemHolder(getCurrentCurriculum().getCurriculumCode(), type, eventName, tag2, tag3, tag4, getTimeAtNumber(begin, last).get(0), getTimeAtNumber(begin, last).get(1), DOW, isWholeDay);
        temp.weeks.add(week);
        Cursor c = contentResolver.query(uri_timetable, null, EventItemHolder.QUERY_SELECTION, temp.getQueryParams(), null, null);
        String uuid = temp.getUuid();
        if (c != null && c.moveToNext()) {
            List<Integer> weeks = new ArrayList<>();
            String[] strs = c.getString(2).split(",");
            for (String str : strs) {
                if (!str.isEmpty()) weeks.add(Integer.parseInt(str));
            }
            if (!weeks.contains(week)) {
                weeks.add(week);
            }
            String newWeeks = getWeeksText(weeks);
            ContentValues cv = new ContentValues();
            uuid = c.getString(c.getColumnIndex("uuid"));
            cv.put("weeks", newWeeks);
            cv.put("uuid", uuid);
            contentResolver.update(uri_timetable, cv, EventItemHolder.QUERY_SELECTION, temp.getQueryParams());
        } else {
            contentResolver.insert(uri_timetable, temp.getContentValues());
        }
        if (c != null) {
            c.close();
        }
        return uuid;
    }

    @WorkerThread
    public boolean deleteEvent(String uuid, int week) {
        Cursor c = contentResolver.query(uri_timetable, null, "uuid=?",
                new String[]{uuid}, null, null);
        if (c != null && c.moveToNext()) {
            List<Integer> weeks = new ArrayList<>();
            String[] strs = c.getString(2).split(",");
            for (String str : strs) {
                if (!str.isEmpty()) weeks.add(Integer.parseInt(str));
            }
            if (!weeks.contains(week)) return false;
            else weeks.remove((Integer) week);
            if (weeks.size() == 0) {
                contentResolver.delete(uri_timetable, "uuid=?",
                        new String[]{uuid});
            } else {
                String newWeeks = getWeeksText(weeks);
                ContentValues cv = new ContentValues();
                cv.put("weeks", newWeeks);
                contentResolver.update(uri_timetable, cv, "uuid=?",
                        new String[]{uuid});
            }
        } else {
            if (c != null) {
                c.close();
            }
            return false;
        }
        c.close();
        return true;
    }

    public String addEvent(int week, int DOW, int type, String eventName, String tag2, String tag3, String tag4, HTime start, HTime end, boolean isWholeDay) {
        if (week > getCurrentCurriculum().getTotalWeeks())
            getCurrentCurriculum().setTotalWeeks(week);
        EventItemHolder temp = new EventItemHolder(getCurrentCurriculum().getCurriculumCode(), type, eventName, tag2, tag3, tag4, start, end, DOW, isWholeDay);
        temp.weeks.add(week);
        String uuid = temp.getUuid();
        Cursor c = contentResolver.query(uri_timetable, null, EventItemHolder.QUERY_SELECTION, temp.getQueryParams(), null, null);
        if (c != null && c.moveToNext()) {
            List<Integer> weeks = new ArrayList<>();
            String[] strs = c.getString(2).split(",");
            for (String str : strs) {
                if (!str.isEmpty()) weeks.add(Integer.parseInt(str));
            }
            if (!weeks.contains(week)) {
                weeks.add(week);
            }
            String newWeeks = getWeeksText(weeks);
            ContentValues cv = new ContentValues();
            uuid = c.getString(c.getColumnIndex("uuid"));
            cv.put("weeks", newWeeks);
            cv.put("uuid", uuid);
            contentResolver.update(uri_timetable, cv, EventItemHolder.QUERY_SELECTION, temp.getQueryParams());
        } else {
            contentResolver.insert(uri_timetable, temp.getContentValues());
        }
        if (c != null) {
            c.close();
        }
        return uuid;
    }

    @WorkerThread
    public void clearCurriculum(String curriculumCode) {
        contentResolver.delete(uri_timetable, "curriculum_code=? AND type=?", new String[]{curriculumCode + "", COURSE + ""});
    }

    @WorkerThread
    public List<EventItem> getEventWithInfoContainsAll(List<String> texts) {
        List<EventItem> res = new ArrayList<>();
        List<EventItem> UnderTimeCondition = getEventWithInfoContains(texts.get(0));
        for (EventItem ei : UnderTimeCondition) {
            boolean allMatched = true;
            Log.e("course", String.valueOf(ei));
            for (String cdt : texts) {
                Log.e("cdt", cdt);
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
        List<EventItem> result = new ArrayList<>();
        if (f_week > getCurrentCurriculum().getTotalWeeks() || t_week > getCurrentCurriculum().getTotalWeeks() || f_week <= 0)
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
    public boolean deleteEvent(Calendar from, Calendar to, int type) {
        List<EventItem> temp = getEventFrom(from, to, type);
        if (temp == null || temp.size() == 0) return false;
        for (EventItem e : temp) {
            deleteEvent(e, true);
        }
        return temp.size() > 0;
    }

    @WorkerThread
    public List<EventItem> getUnfinishedEvent(Calendar time, int type) {
        List<EventItem> result = new ArrayList<>();
        Cursor c = contentResolver.query(uri_timetable, null, "type=? and curriculum_code=?", new String[]{type + "", currentCurriculumId}, null, null);
        while (c != null && c.moveToNext()) {
            result.addAll(new EventItemHolder(c).getAllEvents());
        }
        if (c != null) {
            c.close();
        }
        List<EventItem> toRemove = new ArrayList<>();
        for (EventItem ei : result) {
            if (ei.hasPassed(time)) toRemove.add(ei);
        }
        result.removeAll(toRemove);
        return result;
    }

    @WorkerThread
    public boolean deleteTask(Task ta) {
        try {
            if (ta.has_deadline && !ta.ddlName.equals("null")) {
                String ddlUUID = ta.ddlName.split(":::")[0];
                int week = Integer.parseInt(ta.ddlName.split(":::")[1]);
                deleteEvent(ddlUUID, week);
            }
            for (String key : ta.getEvent_map().keySet()) {
                String EIuuid = key.split(":::")[0];
                contentResolver.delete(uri_timetable, "uuid=?", new String[]{EIuuid});
            }
            return contentResolver.delete(uri_task, Task.QUERY_SELECTION, ta.getQueryParams()) != 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @WorkerThread
    private void deleteTask(String uuid, boolean deleteDDL) {
        Task ta;
        if (TextUtils.isEmpty(uuid)) return;
        Cursor c = contentResolver.query(uri_task, null, "uuid=?", new String[]{uuid}, null, null);
        if (c != null && c.moveToNext()) {
            ta = new Task(c);
            c.close();
        } else {
            if (c != null) {
                c.close();
            }
            return;
        }
        try {
            if (deleteDDL && ta.has_deadline && !ta.ddlName.equals("null")) {
                String ddlUUID = ta.ddlName.split(":::")[0];
                int week = Integer.parseInt(ta.ddlName.split(":::")[1]);
                deleteEvent(ddlUUID, week);
            }
            for (String key : ta.getEvent_map().keySet()) {
                String EIuuid = key.split(":::")[0];
                contentResolver.delete(uri_timetable, "uuid=?", new String[]{EIuuid});
            }
            contentResolver.delete(uri_task, Task.QUERY_SELECTION, ta.getQueryParams());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @WorkerThread
    public boolean setFinishTask(Task ta, boolean finished) {
        try {
            if (ta.has_deadline && !ta.ddlName.equals("null")) {
                String ddlUUID = ta.ddlName.split(":::")[0];
                int week = Integer.parseInt(ta.ddlName.split(":::")[1]);
                deleteEvent(ddlUUID, week);
            }
            ta.setFinished(finished);
            return contentResolver.update(uri_task, ta.getContentValues(), "uuid=?", new String[]{ta.getUuid()}) != 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    @WorkerThread
    public void clearEvent(int type) {
        contentResolver.delete(uri_timetable, "type=?", new String[]{type + ""});
    }

    @WorkerThread
    void clearTask(String tagContains) {
        Cursor c = contentResolver.query(uri_task, null, "tag like?", new String[]{"%" + tagContains + "%"}, null, null);
        while (c != null && c.moveToNext()) {
            Task t = new Task(c);
            for (String x : t.getEvent_map().keySet()) {
                String uuid = x.split(":::")[0];
                contentResolver.delete(uri_timetable, "uuid=?", new String[]{uuid});
            }
            contentResolver.delete(uri_task, "uuid=?", new String[]{t.getUuid()});
        }
        if (c != null) {
            c.close();
        }

    }

    @WorkerThread
    public void clearEvent(int type, String name) {
        contentResolver.delete(uri_timetable, "type=? and name=?", new String[]{type + "", name});
    }

    @WorkerThread
    public List<EventItem> getOneDayEvents(int week, int DOW) {
        List<EventItem> result = new ArrayList<>();
        if (week <= 0 || !timeTableCore.isDataAvailable() || week > getCurrentCurriculum().getTotalWeeks())
            return result;
        Cursor c = contentResolver.query(uri_timetable, null, "curriculum_code=? and dow=? and weeks like?",
                new String[]{getCurrentCurriculum().getCurriculumCode() + ""
                        , DOW + "", "%" + week + "%"
                }, null, null);
        while (c != null && c.moveToNext()) {
            EventItemHolder eih = new EventItemHolder(c);
            result.addAll(eih.getEventsWithinWeeks(week, week));
        }
        if (c != null) {
            c.close();
        }
        return result;
    }

    public static boolean contains_integer(int[] array, int object) {
        for (int x : array) if (x == object) return true;
        return false;
    }

    @WorkerThread
    List<EventItem> getAllEvents() {
        List<EventItem> result = new ArrayList<>();
        Cursor c = contentResolver.query(uri_timetable, null, "curriculum_code=?", new String[]{getCurrentCurriculum().getCurriculumCode()}, null, null);
        while (c != null && c.moveToNext()) {
            result.addAll(new EventItemHolder(c).getAllEvents());
        }
        if (c != null) {
            c.close();
        }
        return result;
    }

    @WorkerThread
    public List<EventItem> getEventFrom(int f_week, int f_dayOfWeek, HTime start, int t_week, int t_dayOfWeek, HTime end, int type) {
        if (t_dayOfWeek == -1) t_dayOfWeek = getCurrentCurriculum().getTotalWeeks();
        if (f_week > t_week) return null;
        else if (f_week == t_week) {
            if (f_dayOfWeek > t_dayOfWeek) return null;
            else if (f_dayOfWeek == t_dayOfWeek) {
                if (start.compareTo(end) > 0) return null;
            }
        }
        List<EventItem> result = new ArrayList<>();
        if (f_week > getCurrentCurriculum().getTotalWeeks() || t_week > getCurrentCurriculum().getTotalWeeks() || f_week <= 0)
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
        List<EventItem> result = new ArrayList<>();
        if (f_week > getCurrentCurriculum().getTotalWeeks() || t_week > getCurrentCurriculum().getTotalWeeks() || f_week <= 0)
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
    public List<EventItem> getAllEvents(int type) {
        List<EventItem> result = new ArrayList<>();
        Cursor c = contentResolver.query(uri_timetable, null, "curriculum_code=? and type=?", new String[]{getCurrentCurriculum().getCurriculumCode(), type + ""}, null, null);
        while (c != null && c.moveToNext()) {
            result.addAll(new EventItemHolder(c).getAllEvents());
        }
        if (c != null) {
            c.close();
        }
        return result;
    }

    @WorkerThread
    private List<EventItem> getEventWithInfoContains(String text) {
        List<EventItem> result = new ArrayList<>();
        String q = "%" + text + "%";
        Cursor c = contentResolver.query(uri_timetable, null,
                "name LIKE ? OR tag2 LIKE ? OR tag3 LIKE ? OR tag4 LIKE ?", new String[]{q, q, q, q}, null, null);
        while (c != null && c.moveToNext()) {
            result.addAll(new EventItemHolder(c).getAllEvents());
        }
        if (c != null) {
            c.close();
        }
        return result;
    }

    @WorkerThread
    public EventItem getCourseAt(int week, int dow, int start, int last) {
        for (EventItem ei : getEventsWithinWeeks(week, week)) {
            if (ei.DOW == dow
                    && ei.startTime.equals(getTimeAtNumber(start, last).get(0))
                    && ei.endTime.equals(getTimeAtNumber(start, last).get(1))
                    && ei.eventType == COURSE) {
                return ei;
            }
        }
        return null;
    }

    @WorkerThread
    public List<EventItem> getEventFrom(Calendar from, Calendar to, int type) {
        List<EventItem> result = new ArrayList<>();
        int f_week = getCurrentCurriculum().getWeekOfTerm(from);
        int tempDOW1 = from.get(Calendar.DAY_OF_WEEK);
        int f_dayOfWeek = tempDOW1 == 1 ? 7 : tempDOW1 - 1;
        HTime start = new HTime(from);
        int t_week = getCurrentCurriculum().getWeekOfTerm(to);
        int tempDOW2 = to.get(Calendar.DAY_OF_WEEK);
        int t_dayOfWeek = tempDOW2 == 1 ? 7 : tempDOW2 - 1;
        HTime end = new HTime(to);
        if (t_dayOfWeek == -1) t_dayOfWeek = getCurrentCurriculum().getTotalWeeks();
        if (f_week > t_week) return null;
        else if (f_week == t_week) {
            if (f_dayOfWeek > t_dayOfWeek) return null;
            else if (f_dayOfWeek == t_dayOfWeek) {
                if (start.compareTo(end) > 0) return null;
            }
        }

        if (f_week > getCurrentCurriculum().getTotalWeeks() || t_week > getCurrentCurriculum().getTotalWeeks() || f_week <= 0)
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
    public ArrayList<Task> getFinishedTasks() {
        ArrayList<Task> res = new ArrayList<>();
        if (!isDataAvailable()) return res;
        Cursor c = contentResolver.query(uri_task, null, "curriculum_code=? and finished=?", new String[]{getCurrentCurriculum().getCurriculumCode(), 1 + ""}, null, null);
        while (c != null && c.moveToNext()) {
            res.add(new Task(c));
        }
        if (c != null) {
            c.close();
        }
        return res;
    }

    @WorkerThread
    public List<EventItem> getEventsWithinWeeks(int fromW, int toW) {
        List<EventItem> result = new ArrayList<>();
        Cursor c = contentResolver.query(uri_timetable, null, "curriculum_code=?", new String[]{getCurrentCurriculum().getCurriculumCode() + ""}, null, null);
        while (c != null && c.moveToNext()) {
            EventItemHolder eih = new EventItemHolder(c);
            result.addAll(eih.getEventsWithinWeeks(fromW, toW));
        }
        if (c != null) {
            c.close();
        }
        return result;
    }

    @WorkerThread
    Task getTaskWithUUID(String uuid) {
        Task result = null;
        if (uuid == null || TextUtils.isEmpty(uuid)) return null;
        try {
            Cursor c = contentResolver.query(uri_task, null, "uuid=?", new String[]{uuid}, null, null);
            if (c != null && c.moveToNext()) {
                result = new Task(c);
            }
            if (c != null) {
                c.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @WorkerThread
    public EventItemHolder getEventItemHolderWithUUID(String uuid) {
        EventItemHolder result = null;
        Cursor c = contentResolver.query(uri_timetable, null, "uuid=?", new String[]{uuid}, null, null);
        if (c != null && c.moveToNext()) {
            result = new EventItemHolder(c);
        }
        if (c != null) {
            c.close();
        }
        return result;
    }

    public void addTask(Task t) {
        contentResolver.insert(uri_task, t.getContentValues());
    }

    @WorkerThread
    public String addTask(String name, int fW, int fDOW, HTime sTime, int tW, int tDOW, HTime eTime, String ddlUUID) {
        Task t = new Task(getCurrentCurriculum().getCurriculumCode(), name, fW, fDOW, sTime, tW, tDOW, eTime, ddlUUID);
        contentResolver.insert(uri_task, t.getContentValues());
        return t.getUuid();
    }

    @WorkerThread
    public ArrayList<Task> getUnfinishedTasks() {
        ArrayList<Task> res = new ArrayList<>();
        if (!isDataAvailable()) return res;
        Cursor c = contentResolver.query(uri_task, null, "curriculum_code=? and finished=?", new String[]{getCurrentCurriculum().getCurriculumCode(), 0 + ""}, null, null);
        while (c != null && c.moveToNext()) {
            res.add(new Task(c));
        }
        if (c != null) {
            c.close();
        }
        return res;
    }

    List<TimePeriod> getSpaces(List<EventItem> breakT, Calendar from, Calendar to, int minDurationMinute, int type) {
        if (from.after(to) || from.get(Calendar.DAY_OF_MONTH) != to.get(Calendar.DAY_OF_MONTH))
            return null;
        List<TimePeriod> result = new ArrayList<>();
        List<EventItem> temp = getEventFrom(from, to, type);
        temp.addAll(breakT);
        Collections.sort(temp, new Comparator<EventItem>() {
            @Override
            public int compare(EventItem o1, EventItem o2) {
                return o1.compareTo(o2);
            }
        });
        // Log.e("temp event is:", temp.toString());
        if (temp.size() == 0) {
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
               // Log.e("event:", temp.get(i).toString());
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
        Collections.sort(result, new Comparator<TimePeriod>() {
            @Override
            public int compare(TimePeriod o1, TimePeriod o2) {
                return o1.compareTo(o2);
            }
        });
        return result;
    }

    @WorkerThread
    public ArrayList<Task> getUnfinishedTaskWithLength() {
        ArrayList<Task> res = new ArrayList<>();
        if (!isDataAvailable()) return res;
        Cursor c = contentResolver.query(uri_task, null, "curriculum_code=? and has_length=? and finished=?", new String[]{getCurrentCurriculum().getCurriculumCode(), 1 + "", 0 + ""}, null, null);
        while (c != null && c.moveToNext()) {
            res.add(new Task(c));
        }
        if (c != null) {
            c.close();
        }
        return res;
    }

    public static int getDOW(Calendar c) {
        int tempDOW1 = c.get(Calendar.DAY_OF_WEEK);
        return tempDOW1 == 1 ? 7 : tempDOW1 - 1;
    }

    public interface OnDoneListener {
        void onSuccess();

        void onFailed(Exception e);
    }

    private interface OnPackDoneListener {
        void onDone(UserData.UserDataCloud data);

        void onFailed(Exception e);
    }

    private static class packUpTask extends AsyncTask<Object, Object, Object> {

        OnPackDoneListener listener;
        ContentResolver contentResolver;

        packUpTask(OnPackDoneListener listener, ContentResolver contentResolver) {
            this.listener = listener;
            this.contentResolver = contentResolver;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                for (Curriculum c : timeTableCore.getAllCurriculum()) {
                    if (contentResolver.update(uri_curriculum, c.getContentValues(), "curriculum_code=?", new String[]{c.getCurriculumCode()}) == 0) {
                        contentResolver.insert(uri_curriculum, c.getContentValues());
                    }
                }
                UserData data = UserData.create(contentResolver);
                data.loadTaskData().loadTimetableData().loadSubjectData().loadCurriculumData(timeTableCore.getAllCurriculum());
                return data.getPreparedCloudData(CurrentUser);
            } catch (Exception e) {
                return e;
            }
        }

        @Override
        protected void onPostExecute(Object o) {
            if (o instanceof Exception) {
                listener.onFailed((Exception) o);
            } else {
                listener.onDone((UserData.UserDataCloud) o);
            }
        }
    }

    static class writeDataToLocalTask extends AsyncTask<Object, Object, Object> {

        UserData.UserDataCloud user_data;
        WeakReference<Activity> toFinish;
        ContentResolver contentResolver;

        writeDataToLocalTask(ContentResolver contentResolver, UserData.UserDataCloud data, Activity toFinish) {
            this.user_data = data;
            this.toFinish = new WeakReference<>(toFinish);
            this.contentResolver = contentResolver;
        }

        writeDataToLocalTask(ContentResolver contentResolver, UserData.UserDataCloud data) {
            this.user_data = data;
            this.toFinish = null;
            this.contentResolver = contentResolver;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                UserData data = UserData.create(contentResolver).loadData(user_data);
                // clearData();
                contentResolver.delete(uri_curriculum, null, null);
                contentResolver.delete(uri_timetable, null, null);
                for (Curriculum ci : data.getCurriculum()) {
                    if (TextUtils.isEmpty(ci.getCurriculumCode()) || TextUtils.isEmpty(ci.getName()))
                        continue;
                    contentResolver.insert(uri_curriculum, ci.getContentValues());
                    CurriculumCreator curriculumCreator = CurriculumCreator.create(ci.getCurriculumCode(), ci.getName(), ci.getStartDate());
                    curriculumCreator.loadCourse(ci.getCurriculumText());
                    timeTableCore.addCurriculum(curriculumCreator, true);
                }
                contentResolver.delete(uri_subject, null, null);
                for (Subject s : data.getSubjects()) {
                    if (TextUtils.isEmpty(s.getCurriculumId())) continue;
                    contentResolver.insert(uri_subject, s.getContentValues());
                }
                for (EventItemHolder eih : data.getEvents()) {
                    contentResolver.insert(uri_timetable, eih.getContentValues());
                }
                contentResolver.delete(uri_subject, null, null);
                for (Task t : data.getTasks()) {
                    contentResolver.insert(uri_subject, t.getContentValues());
                }
                timeTableCore.initCoreData();
                return true;

            } catch (Exception e1) {
                e1.printStackTrace();
                return false;
            }


        }

        @Override
        protected void onPostExecute(Object o) {
            if (toFinish != null && toFinish.get() != null && !toFinish.get().isFinishing() && !toFinish.get().isDestroyed()) {
                toFinish.get().finish();
            }
            if ((Boolean) o) {
                Toast.makeText(HContext, R.string.sync_success, Toast.LENGTH_SHORT).show();
                if (timeTableCore.isDataAvailable()) timeServiceBinder.refreshNowAndNextEvent();
            } else {
                Toast.makeText(HContext, R.string.sync_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getWeeksText(List<Integer> weeks) {
        StringBuilder res = new StringBuilder();
        for (Integer x : weeks) {
            res.append(x).append(",");
        }
        if (res.toString().endsWith(","))
            res = new StringBuilder(res.substring(0, res.length() - 1));
        return res.toString();
    }

}
