package com.stupidtree.hita.timetable;

import androidx.annotation.WorkerThread;

import com.google.gson.Gson;
import com.stupidtree.hita.hita.TextTools;
import com.stupidtree.hita.util.DeflaterUtils;

import org.apache.http.util.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CurriculumCreator implements Serializable {
    static final int CURRICULUM_TYPE_COURSE = -123;
    private static final int CURRICULUM_TYPE_EXAM = -125;

    private Curriculum curriculum;
    private ArrayList<Subject> Subjects;
    private ArrayList<CurriculumItem> CurriculumList;

    public static CurriculumCreator create(String code, String name, Calendar startDate) {
        return new CurriculumCreator(code, name, startDate);
    }

    @WorkerThread
    public CurriculumCreator loadCourse(List<Map<String, String>> data) {
        for (Map<String, String> map : data) {
//            int dow = TextTools.isNumber(map.get("dow"));
            String name = map.get("name");
            String teacher = map.get("teacher");
            int dow = Integer.parseInt(map.get("dow"));
            String classroom = map.get("classroom");
            int begin = 1, last = 2;
            String[] weeks = new String[]{};
            try {
                begin = Integer.parseInt(map.get("begin"));
                last = Integer.parseInt(map.get("last"));
                weeks = map.get("weeks").split(",");
            } catch (Exception e) {

            }
            generateCourse(dow, name, teacher, classroom, begin, last, weeks);
        }
        String curriculumText = new Gson().toJson(data);
        curriculum.setCurriculumText(DeflaterUtils.zipString(curriculumText));
        return this;
    }

    @WorkerThread
    public CurriculumCreator loadCourse(String dataString){
        if(TextUtils.isEmpty(dataString)) return null;
        String decoded = DeflaterUtils.unzipString(dataString);
        List<Map<String,String>> data = new Gson().fromJson(decoded,List.class);
        return loadCourse(data);
    }

    @WorkerThread
    public CurriculumCreator updateSubjectInfo(List<Map<String, String>> data) {
        for (Map<String, String> d : data) {
            boolean found = false;
            for (Subject s : Subjects) {
                if (TextTools.equals(s.getName(), Objects.requireNonNull(d.get("name")), "【实验】")) {
                    found = true;
                    s.setCode(d.get("code"));
                    s.setSchool(d.get("school"));
                    s.setCompulsory(d.get("compulsory"));
                    s.setCredit(d.get("credit"));
                    s.setTotalCourses(d.get("period"));
                    s.setType(d.get("type"));
                    s.setTeacher(d.get("teacher"));
                    s.setXnxq(d.get("xnxq"));
                    s.setId(d.get("id"));
                }
            }
            if (d.get("type").equals("MOOC")) {
                if (!found) {
                    Subject s = new Subject(curriculum.getCurriculumCode(),d.get("name"),d.get("teacher"));
                    s.setMOOC(true);
                    s.setCode(d.get("code"));
                    s.setSchool(d.get("school"));
                    s.setCompulsory(d.get("compulsory"));
                    s.setCredit(d.get("credit"));
                    s.setTotalCourses(d.get("period"));
                    s.setType(d.get("type"));
                    s.setTeacher(d.get("teacher"));
                    s.setXnxq(d.get("xnxq"));
                    s.setId(d.get("id"));
                }
            }
        }
        return this;
    }


    public CurriculumCreator(String code, String name, Calendar c) {
        CurriculumList = new ArrayList<>();
        Subjects = new ArrayList<>();
        switch (c.get(Calendar.DAY_OF_WEEK)) {
            case 1:
                c.add(Calendar.DATE, -6);
                break;
            case 2:
                break;
            case 3:
                c.add(Calendar.DATE, -1);
                break;
            case 4:
                c.add(Calendar.DATE, -2);
                break;
            case 5:
                c.add(Calendar.DATE, -3);
                break;
            case 6:
                c.add(Calendar.DATE, -4);
                break;
            case 7:
                c.add(Calendar.DATE, -5);
                break;
        }
        curriculum = new Curriculum(c, name);
        curriculum.setCurriculumCode(code);

    }

    public Curriculum getCurriculum() {
        return curriculum;
    }

    private void refreshTotalWeek() {
        int max = 0;
        for (CurriculumItem ci : CurriculumList) {
            for (int i : ci.weeks) {
                if (i > max) max = i;
            }
        }
        curriculum.setTotalWeeks(max);
    }

    private void generateCourse(int DOW, String name, String teacher, String classroom, int begin, int last, String[] Weeks) {
        ArrayList<Integer> weeksTemp = new ArrayList<>();
        for (String x : Weeks) {
            int i;
            try {
                i = Integer.parseInt(x);
            } catch (Exception e) {
                continue;
            }
            weeksTemp.add(i);
        }
        Collections.sort(weeksTemp);
        CurriculumItem temp = new CurriculumItem(name, classroom, teacher, DOW, CURRICULUM_TYPE_COURSE, begin, last, weeksTemp);
        CurriculumList.add(temp);
        refreshTotalWeek();
        syncSubjects();
    }


    private void syncSubjects() {
        Subjects.clear();
        for (CurriculumItem cit : CurriculumList) {
            if (cit.type == CURRICULUM_TYPE_EXAM) continue;
            Subject tempS = new Subject(curriculum.getCurriculumCode(), cit.name, cit.tag);
            int subIndex = Subjects.indexOf(tempS);
            if (subIndex < 0) {
                Subjects.add(tempS);
            }
        }
    }


    public String getCurriculumCode() {
        return curriculum.getCurriculumCode();
    }


    public class CurriculumItem implements Serializable {
        public String name;
        String place;
        public String tag;//course->teacher;exam->timeDetail

        public int begin;
        public int last;
        public int type;
        public int DOW;
        public ArrayList<Integer> weeks = null;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CurriculumItem that = (CurriculumItem) o;
            return begin == that.begin &&
                    last == that.last &&
                    type == that.type &&
                    Objects.equals(name, that.name) &&
                    Objects.equals(place, that.place) &&
                    Objects.equals(tag, that.tag);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, place, tag, begin, last, type);
        }


        CurriculumItem(String name, String place, String tag, int DOW, int type, int begin, int last) {
            this.name = name;
            this.type = type;
            this.begin = begin;
            this.last = last;
            this.place = place;
            this.tag = tag;
            this.DOW = DOW;
            weeks = new ArrayList<>();
        }

        CurriculumItem(String name, String place, String tag, int DOW, int type, int begin, int last, ArrayList weeks) {
            this.name = name;
            this.type = type;
            this.begin = begin;
            this.last = last;
            this.place = place;
            this.tag = tag;
            this.DOW = DOW;
            this.weeks = weeks;
        }

        void addWeek(int x) {
            if (!weeks.contains(x)) weeks.add(x);
            Collections.sort(weeks);
        }

    }


    public ArrayList<Subject> getSubjects() {
        return Subjects;
    }

    public ArrayList<CurriculumItem> getCurriculumList() {
        return CurriculumList;
    }
}






