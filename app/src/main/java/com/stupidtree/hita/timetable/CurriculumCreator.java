package com.stupidtree.hita.timetable;

import com.stupidtree.hita.hita.TextTools;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/*课表类*/
public class CurriculumCreator implements Serializable {
    public static final int CURRICULUM_TYPE_COURSE = -123;
    public static final int CURRICULUM_TYPE_EXAM = -125;




    private HDate startDate;
    private int totalWeeks;
    private String name;
    private ArrayList<Subject> Subjects;
    private ArrayList<CurriculumItem> CurriculumList;
    private String curriculumCode;
    private String curriculumText;
    public CurriculumCreator(String code, int sY, int sM, int sD, String name) {
        CurriculumList = new ArrayList<>();
        Subjects = new ArrayList<>();
        curriculumCode = code;
        this.name = name;
        int y, m, d;
        Calendar c = Calendar.getInstance();
        c.set(sY, sM - 1, sD);
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
        y = c.get(Calendar.YEAR);
        m = c.get(Calendar.MONTH) + 1;
        d = c.get(Calendar.DAY_OF_MONTH);
        totalWeeks = 0;
        startDate = new HDate(y, m, d);
    }
    public String object_id;

    public Curriculum getCurriculum(){
        Curriculum res = new Curriculum(startDate.year,startDate.month,startDate.dayOfMonth,name);
        res.setCurriculumCode(curriculumCode);
        res.setTotalWeeks(totalWeeks);
        res.setCurriculumText(curriculumText);
        res.setObjectId(object_id);
        return res;
    }

    private void refreshTotalWeek() {
        int max = 0;
        for (CurriculumItem ci:CurriculumList) {
            for(int i:ci.weeks){
                if(i>max) max = i;
            }
        }
        totalWeeks = max;
    }

    public void CoursesGeneraor(int DOW, String name, String teacher, String classroom, int begin, int last, String[] Weeks) {
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
        CurriculumItem temp = new CurriculumItem(name,classroom,teacher,DOW,CURRICULUM_TYPE_COURSE,begin,last,weeksTemp);
        CurriculumList.add(temp);
        refreshTotalWeek();
        syncSubjects();
    }

    public void ExamGeneraor(int DOW, String name, String timeDetail, String classroom, int begin, int last, String[] Weeks) {
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
        CurriculumItem temp = new CurriculumItem(name,classroom,timeDetail,DOW,CURRICULUM_TYPE_EXAM,begin,last,weeksTemp);
        CurriculumList.add(temp);
        refreshTotalWeek();
    }

    public void syncSubjects(){
        Subjects.clear();
        for(CurriculumItem cit:CurriculumList){
            if(cit.type==CURRICULUM_TYPE_EXAM) continue;
            Subject tempS = new Subject(curriculumCode,cit.name,cit.tag);
            int subIndex = Subjects.indexOf(tempS);
            if(subIndex<0){
                Subjects.add(tempS);
            }
        }
    }

    public void updateSubjectInfo(List<Map<String,String>> data){
        for(Map<String,String> d:data){
            boolean found = false;
            for(Subject s:Subjects){
                if(TextTools.equals(s.getName(), Objects.requireNonNull(d.get("name")),"【实验】")){
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
            if(d.get("type").equals("MOOC")){
                if(!found){
                    Subject s = new Subject(curriculumCode);
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
    }



    //日期类，不仅有年月日，还可以根据当前Curriculum对象计算出周数
    public class HDate implements Comparable, Serializable {
        int year;
        int month;
        int dayOfMonth;
        int number;
        int dayOfWeek;
        int weekOfTerm;

        HDate(int year, int month, int dOM) {
            Calendar c = Calendar.getInstance();
            c.set(year, month - 1, dOM);
            Calendar start = Calendar.getInstance();
            if (startDate != null) {
                start.set(startDate.year, startDate.month - 1, startDate.dayOfMonth);
                long tempDay = (c.getTimeInMillis() - start.getTimeInMillis()) / (1000 * 3600 * 24);
                weekOfTerm = (int) (tempDay / 7) + 1;
            } else {
                weekOfTerm = 1;
            }
            this.year = year;
            this.month = month;
            dayOfMonth = dOM;
            dayOfWeek = c.get(Calendar.DAY_OF_WEEK) == 1 ? 7 : c.get(Calendar.DAY_OF_WEEK) - 1;


        }

        HDate(Calendar c) {
            Calendar start = Calendar.getInstance();
            start.set(startDate.year, startDate.month - 1, startDate.dayOfMonth);
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH) + 1;
            dayOfWeek = c.get(Calendar.DAY_OF_WEEK) == 1 ? 7 : c.get(Calendar.DAY_OF_WEEK) - 1;
            dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
            long tempDay = (c.getTimeInMillis() - start.getTimeInMillis()) / (1000 * 3600 * 24);
            weekOfTerm = (int) (tempDay / 7) + 1;
        }

        public String readDate() {
            return year + "年" + month + "月" + dayOfMonth + "日";
        }

        @Override
        public int compareTo(Object o) {
            if (this.year > ((HDate) o).year) return 1;
            else if (this.year < ((HDate) o).year) return -1;
            else if (this.month > ((HDate) o).month) return 1;
            else if (this.month < ((HDate) o).month) return -1;
            else if (this.dayOfMonth > ((HDate) o).dayOfMonth) return 1;
            else if (this.dayOfMonth < ((HDate) o).dayOfMonth) return -1;
            else return 0;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + dayOfMonth;
            result = prime * result + month;
            result = prime * result + year;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            HDate other = (HDate) obj;
            if (dayOfMonth != other.dayOfMonth)
                return false;
            if (month != other.month)
                return false;
            return year == other.year;
        }

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


        CurriculumItem(String name,String place,String tag,int DOW,int type,int begin,int last){
            this.name = name;
            this.type = type;
            this.begin = begin;
            this.last = last;
            this.place = place;
            this.tag = tag;
            this.DOW = DOW;
            weeks = new ArrayList<>();
        }
        CurriculumItem(String name,String place,String tag,int DOW,int type,int begin,int last,ArrayList weeks){
            this.name = name;
            this.type = type;
            this.begin = begin;
            this.last = last;
            this.place = place;
            this.tag = tag;
            this.DOW = DOW;
            this.weeks = weeks;
        }
        void addWeek(int x){
            if(!weeks.contains(x)) weeks.add(x);
            Collections.sort(weeks);
        }

    }

    public HDate getStartDate() {
        return startDate;
    }

    public void setStartDate(HDate startDate) {
        this.startDate = startDate;
    }

    public int getTotalWeeks() {
        return totalWeeks;
    }

    public void setTotalWeeks(int totalWeeks) {
        this.totalWeeks = totalWeeks;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Subject> getSubjects() {
        return Subjects;
    }

    public void setSubjects(ArrayList<Subject> subjects) {
        Subjects = subjects;
    }

    public ArrayList<CurriculumItem> getCurriculumList() {
        return CurriculumList;
    }

    public void setCurriculumList(ArrayList<CurriculumItem> curriculumList) {
        CurriculumList = curriculumList;
    }

    public String getCurriculumCode() {
        return curriculumCode;
    }

    public void setCurriculumCode(String curriculumCode) {
        this.curriculumCode = curriculumCode;
    }

    public String getCurriculumText() {
        return curriculumText;
    }

    public void setCurriculumText(String curriculumText) {
        this.curriculumText = curriculumText;
    }
}






