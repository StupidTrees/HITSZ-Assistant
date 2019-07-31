package com.stupidtree.hita.jwts;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivityMain;
import com.stupidtree.hita.core.CurriculumHelper;
import com.stupidtree.hita.core.Subject;
import com.stupidtree.hita.diy.ButtonLoading;
import com.stupidtree.hita.online.Teacher;
import com.stupidtree.hita.util.FileOperator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UploadFileListener;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.cookies;


public class FragmentJWTS_grkb extends Fragment {
    Spinner spinner_grkb;
    ButtonLoading bt_import_grkb;
    List<String> spinnerItems;
    List<Map<String, String>> curriculumItems;
    Switch uploadTeacher;
    ArrayAdapter aa;


    private OnFragmentInteractionListener mListener;

    public FragmentJWTS_grkb() {

    }

    public static FragmentJWTS_grkb newInstance() {
        return new FragmentJWTS_grkb();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_jwts_grkb, container, false);
        initViews(v);
        new refreshPageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return v;
    }

    void initViews(View v) {

        spinner_grkb = v.findViewById(R.id.spinner_grkb);
        spinnerItems = new ArrayList<>();
        curriculumItems = new ArrayList<>();
        aa = new ArrayAdapter(this.getContext(), android.R.layout.simple_spinner_item, spinnerItems);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_grkb.setAdapter(aa);
        bt_import_grkb = v.findViewById(R.id.button_import_grkb);
        uploadTeacher = v.findViewById(R.id.switch_teacher);
        bt_import_grkb.setOnButtonLoadingListener(new ButtonLoading.OnButtonLoadingListener() {
            @Override
            public void onClick() {
                if (curriculumItems.size() > 0)
                    new importGRKBTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            }

            @Override
            public void onStart() {

            }

            @Override
            public void onFinish() {

            }
        });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    protected void getSubjectsInfo(CurriculumHelper ci, String xnxq) throws IOException {
        Document page = Jsoup.connect("http://jwts.hitsz.edu.cn/kbcx/queryXsxkXq").cookies(cookies).timeout(60000)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .data("fhlj", "kbcx/queryXszkb")
                .data("xnxq", xnxq)
                .ignoreContentType(true)
                .post();
        Elements table = page.getElementsByClass("bot_line");
        Elements subjects = table.select("tr");
        subjects.remove(0);
        List<Element> moocs_del = new ArrayList<>();
        List<Subject> MOOCS_add = new ArrayList<>();
        for (Element s : subjects) {
            Elements rows = s.getElementsByTag("td");
            if (rows.get(7).text() == null || rows.get(7).text().isEmpty()) {
                moocs_del.add(s);
                Subject ss = new Subject(ci.curriculumCode, rows.get(2).text(), "无");
                ss.isMOOC = true;
                ss.type = rows.get(4).text();
                ss.compulsory = rows.get(5).text();
                ss.school = rows.get(6).text();
                ss.credit = rows.get(9).text();
                ss.totalCourses = rows.get(10).text();
                ss.xnxq = rows.get(0).text();
                ss.code = rows.get(1).text();
                MOOCS_add.add(ss);
            }
        }
        subjects.removeAll(moocs_del);
        List<Element> leftElement = new ArrayList<>(subjects);
        List<Subject> leftSubject = new ArrayList<>(ci.Subjects);
        for (Element s : subjects) {
            Elements rows = s.getElementsByTag("td");
            Subject ss;
            //Log.e("test",rows+",,实验="+(rows.get(10).text().isEmpty())+",get="+ ci.getSubjectByName(rows.get(2).text()+"(实验)"));
            if (rows.get(10).text().isEmpty())
                ss = ci.getSubjectByName(rows.get(2).text() + "(实验)");
            else ss = ci.getSubjectByName(rows.get(2).text());
            if (ss == null) continue;
            ss.type = rows.get(4).text();
            ss.compulsory = rows.get(5).text();
            ss.school = rows.get(6).text();
            ss.credit = rows.get(9).text();
            ss.totalCourses = rows.get(10).text(
            );
            ss.xnxq = rows.get(0).text();
            ss.code = rows.get(1).text();
            leftSubject.remove(ss);
            leftElement.remove(s);
        }
        while (leftElement.size() > 0 && leftSubject.size() > 0) {
            Elements rows = leftElement.get(0).getElementsByTag("td");
            Subject ss = null;
            for (Subject s : leftSubject) {
                if (s.name.contains(rows.get(2).text()) ||
                        rows.get(2).text().contains(s.name)
                ) {
                    ss = s;
                    break;
                }
            }
            leftElement.remove(0);
            if (ss == null) continue;
            ss.type = rows.get(4).text();
            ss.compulsory = rows.get(5).text();
            ss.school = rows.get(6).text();
            ss.credit = rows.get(9).text();
            ss.totalCourses = rows.get(10).text();
            ss.xnxq = rows.get(0).text();
            ss.code = rows.get(1).text();
            leftSubject.remove(ss);
        }
        ci.Subjects.addAll(MOOCS_add);


//        for(Subject x:ci.Subjects){
//            if(x.code.equals("无数据")) continue;
//
//            Document kcInfo = Jsoup.connect("http://jwts.hitsz.edu.cn/pub/queryKcxxView?kcdm="+x.code+"&flag=1").cookies(cookies).timeout(60000)
//                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
//                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
//                    .header("Content-Type", "application/x-www-form-urlencoded")
//                    .ignoreContentType(true)
//                    .get();
//            Element e = kcInfo.getElementsByTag("table").first();
//            // x.infoHTML = e.toString();
//            // Log.e("aa",e.toString());
//        }
    }

    protected void getSubjectsInfo2(CurriculumHelper ci, String xnxq) throws IOException {
        Document page = Jsoup.connect(" http://jwts.hitsz.edu.cn/zxjh/queryZxkc").cookies(cookies).timeout(30000)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .ignoreContentType(true)
                .get();
        System.out.println(page);
        Elements table = page.getElementsByClass("bot_line");
        Elements subjects = table.select("tr");
        subjects.remove(0);
        for (Subject s : ci.Subjects) {
            for (Element e : subjects) {
                Elements rows = e.getElementsByTag("td");
                //Log.e("compare:",s.code+"-->"+rows.get(0));
                if (rows.get(0).text().equals(s.code)) {
                    //Log.e("找到了！",s.name+"-->"+rows);
                    s.exam = rows.get(12).text().contains("是");
                    s.Default = false;
                }
            }
        }

    }


    protected void getTeacherInfos(String type,String xnxq) throws IOException {
        Document page = Jsoup.connect(" http://jwts.hitsz.edu.cn/xsxk/queryYxkc").cookies(cookies).timeout(30000)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .ignoreContentType(true)
                .data("pageXklb",type)
                .data("pageXnxq", xnxq)
                .post();
        final Elements links = page.select("a[onclick^=javascript:queryJsxxDiv]");
        System.out.println(links);
        for (Element e : links) {
            String onclick = e.attr("onclick");
            final String teacherCode = onclick.substring(onclick.indexOf("('") + 2, onclick.indexOf("')"));
            Document teacher = Jsoup.connect("http://jwts.hitsz.edu.cn/pub/queryJsxxView?zgh=" + teacherCode).cookies(cookies).timeout(30000)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .ignoreContentType(true)
                    .get();
            Element table = teacher.getElementsByTag("table").first();
            Elements rows = table.select("tr");
            final String name = rows.get(0).select("td").get(0).text();
            String gender = rows.get(1).select("td").get(0).text();
            String title = rows.get(2).select("td").first().text();
            String school = rows.get(4).select("td").first().text();
            String phone = rows.get(5).select("td").first().text();
            String email = rows.get(6).select("td").first().text();
            String detail = rows.get(7).select("td").first().text();
             final Teacher t = new Teacher(teacherCode,name, gender, title, school, phone, email, detail);
            BmobQuery<Teacher> bq = new BmobQuery<>();
            bq.addWhereEqualTo("name",name);
            bq.findObjects(new FindListener<Teacher>() {
                @Override
                public void done(List<Teacher> list, BmobException e) {
                    if(e==null&&list!=null&&list.size()>0);
                    else{
                        new uploadTeacherTask(t).execute();
                    }
                }
            });

        }

    }

    class refreshPageTask extends AsyncTask {

        @Override
        protected Map<String, Object> doInBackground(Object[] objects) {
            Map result = new HashMap();
            try {
                curriculumItems.clear();
                Document dd = Jsoup.connect("http://jwts.hitsz.edu.cn/kbcx/queryGrkb").cookies(cookies).timeout(20000)
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .ignoreContentType(true)
                        .get();

                if (dd.toString().contains("alert('") || dd.getElementsByTag
                        ("table").size() <= 0) {
                    return null;
                    //dd.toString().substring(dd.toString().indexOf("alert('")+7,dd.toString().indexOf("\')",dd.toString().indexOf("alert(\'"))).contains("过期")
                }
                Elements e = dd.getElementsByTag("select");
                for (Element x : e.select("option")) {
                    Map m = new HashMap();
                    m.put("name", x.text());
                    m.put("value", x.attr("value"));
                    if (x.toString().contains("selected")) {
                        m.put("name", x.text() + "(当前学期)");
                    }
                    curriculumItems.add(m);
                }
                result.put("spinneritem", curriculumItems);
            } catch (IOException e) {
                return null;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (o != null) {
                Map m = (Map<String, Object>) o;
                if (m.get("spinneritem") != null) {
                    List<Map> l = (List) m.get("spinneritem");
                    for (Map<String, String> x : l) {
                        spinnerItems.add(x.get("name"));
                    }
                    aa.notifyDataSetChanged();
                    for (int i = 0; i < spinnerItems.size(); i++) {
                        if (spinnerItems.get(i).contains("当前学期")) {
                            spinner_grkb.setSelection(i);
                            break;
                        }
                    }
                }
            }

        }
    }

    class importGRKBTask extends AsyncTask {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            bt_import_grkb.setProgress(true);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            Document page;
            try {
                page = Jsoup.connect("http://jwts.hitsz.edu.cn/kbcx/queryGrkb").cookies(cookies).timeout(60000)
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .data("fhlj", "kbcx/queryGrkb")
                        .data("xnxq", curriculumItems.get(spinner_grkb.getSelectedItemPosition()).get("value"))
                        .ignoreContentType(true)
                        .post();
                Elements table = page.getElementsByTag("table");
                Elements rows = table.select("tr");
                String name = page.getElementsByClass("xfyq_top").select("span").text();
                Map<String, Object> curriculum = new HashMap<>();
                curriculum.put("name", name);
                List<List<String>> rowsList = new ArrayList<>();
                for (int i = 2; i < rows.size(); i++) {
                    List<String> oneRow = new ArrayList<>();
                    Elements blocks = rows.get(i).select("td");
                    for (int j = 2; j < blocks.size(); j++) {
                        String blockText = blocks.get(j).toString();
                        String raw = blockText.substring(blockText.indexOf(">") + 1, blockText.lastIndexOf("</td>"));
                        oneRow.add(raw);
                    }
                    rowsList.add(oneRow);
                }
                curriculum.put("table", rowsList);
                Document ddd = Jsoup.connect("http://jwts.hitsz.edu.cn/kbcx/queryXszkb").cookies(cookies).timeout(60000)
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .data("fhlj", "kbcx/queryXszkb")
                        .data("zc", "1")
                        .data("xnxq", curriculumItems.get(spinner_grkb.getSelectedItemPosition()).get("value"))
                        .ignoreContentType(true)
                        .post();

                int year, month, day;
                try {
                    String rawTime = ddd.getElementsByClass("xfyq_top").select("div").text();
                    String time[] = rawTime.substring(rawTime.lastIndexOf("(") + 1, rawTime.lastIndexOf("-")).split("/");
                    month = Integer.parseInt(time[0]);
                    day = Integer.parseInt(time[1]);
                    year = Integer.parseInt(rawTime.substring(0, 4));
                } catch (Exception e) {
                    e.printStackTrace();
                    month = 1;
                    day = 1;
                    year = 2000;
                }

                if (page.toString().contains("alert('")) {
                    return "ALT:" + page.toString().substring(page.toString().indexOf("alert('") + 7, page.toString().indexOf("\')", page.toString().indexOf("alert(\'")));
                } else if (ddd.toString().contains("alert('")) {
                    return "ALT:" + ddd.toString().substring(ddd.toString().indexOf("alert('") + 7, ddd.toString().indexOf("\')", ddd.toString().indexOf("alert(\'")));
                } else {
                    if (rowsList.size() != 0) {
                        final CurriculumHelper s = FileOperator.loadCurriculumHelperFromCloud(curriculumItems.get(spinner_grkb.getSelectedItemPosition()).get("value"), curriculum, year, month, day);
                        //Log.e("code=",s.curriculumCode);
                        //Log.e("all:", String.valueOf(curriculumItems));
                        getSubjectsInfo(s, s.curriculumCode);
                        getSubjectsInfo2(s, s.curriculumCode);
                        if(uploadTeacher.isChecked()) {
                            getTeacherInfos("bx",s.curriculumCode);//必修
                            getTeacherInfos("qxrx",s.curriculumCode);//文理通识
                            getTeacherInfos("qxrx",s.curriculumCode);//文理通识
                            getTeacherInfos("ty",s.curriculumCode);//体育
                            getTeacherInfos("xx",s.curriculumCode);//限选
                            getTeacherInfos("cxyx",s.curriculumCode);//创新研修
                            getTeacherInfos("kzy",s.curriculumCode);//跨专业

                        }
                        if (HITAApplication.addCurriculumToTimeTable(s)) {
                            ActivityMain.saveData(FragmentJWTS_grkb.this.getActivity());
                            return "导入成功！";
                        } else {
                            return "导入失败！";
                        }
                    } else return "获取失败！";
                }

            } catch (IOException e) {
                return "导入失败！";
            }
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            bt_import_grkb.setProgress(false);
            if (o.toString().startsWith("ALT:")) {
                AlertDialog ad = new AlertDialog.Builder(FragmentJWTS_grkb.this.getActivity()).create();
                ad.setTitle("来自教务系统滴友好提示");
                ad.setMessage(o.toString().substring(4));
                ad.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                ad.show();
            } else {
                Toast.makeText(HContext, o.toString(), Toast.LENGTH_SHORT).show();
                //if(o.toString().contains("成功")) new ActivityJWTS.getPYJHTask().executeOnExecutor(THREAD_POOL_EXECUTOR);
            }
        }
    }


    class uploadTeacherTask extends AsyncTask{

        Teacher t;
        uploadTeacherTask(Teacher t){
            this.t = t;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
           // Log.e("add teacher:",t.getName());
            try {
                String photo = "http://jwts.hitsz.edu.cn/xxgl/showPhoto?zgh=" + t.getTeacherCode();
                byte[] teacherPhoto;
                teacherPhoto = Jsoup.connect(photo).cookies(cookies).ignoreContentType(true).execute().bodyAsBytes();
                String path = getActivity().getExternalCacheDir().toString() + "/tpt/teacher:" + t.getTeacherCode() + ".png";
                FileOperator.saveByteImageToFile(path, BitmapFactory.decodeByteArray(teacherPhoto, 0, teacherPhoto.length));
                final BmobFile bf = new BmobFile(new File(path));
                bf.upload(new UploadFileListener() {
                    @Override
                    public void done(BmobException e) {
                        t.setPhotoLink(bf.getFileUrl());
                        t.save(new SaveListener<String>() {
                            @Override
                            public void done(String s, BmobException e) {
                                Log.e("save teacher",t.getName());
                            }
                        });
                    }
                });
            } catch (IOException e1) {
                return false;
            }
            return true;
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
