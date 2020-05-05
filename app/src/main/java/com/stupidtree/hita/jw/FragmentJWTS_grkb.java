package com.stupidtree.hita.jw;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AlertDialog;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivityMain;
import com.stupidtree.hita.online.Teacher;
import com.stupidtree.hita.timetable.CurriculumCreator;
import com.stupidtree.hita.views.ButtonLoading;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import cn.bmob.v3.BmobQuery;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.jwCore;
import static com.stupidtree.hita.HITAApplication.timeTableCore;
import static com.stupidtree.hita.timetable.TimeWatcherService.TIMETABLE_CHANGED;


public class FragmentJWTS_grkb extends JWFragment {
    private Spinner xnxq_picker;
    private ButtonLoading bt_import_grkb;
    private Switch uploadTeacher, syncSubject;

    //数据区
    private List<String> spinnerItems;
    private ArrayAdapter xnxqAdapter;

    public FragmentJWTS_grkb() {

    }

    public static FragmentJWTS_grkb newInstance() {
        return new FragmentJWTS_grkb();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_jwts_grkb;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
    }


    private void initViews(View v) {
        super.initRefresh(v);
        xnxq_picker = v.findViewById(R.id.xnxq_picker);
        spinnerItems = new ArrayList<>();
        //xnxnData = new ArrayList<>();
        xnxqAdapter = new ArrayAdapter(requireContext(), R.layout.dynamic_xnxq_spinner_item, spinnerItems);
        xnxqAdapter.setDropDownViewResource(R.layout.dynamic_xnxq_spinner_dropdown_item);
        xnxq_picker.setAdapter(xnxqAdapter);
        bt_import_grkb = v.findViewById(R.id.button_import_grkb);
        uploadTeacher = v.findViewById(R.id.switch_teacher);
        syncSubject = v.findViewById(R.id.switch_subject);
        bt_import_grkb.setOnButtonLoadingListener(new ButtonLoading.OnButtonLoadingListener() {
            @Override
            public void onClick() {
                if (jwRoot.getXNXQItems().size() > 0) {
                    Map<String, String> xnxq = jwRoot.getXNXQItems().get(xnxq_picker.getSelectedItemPosition());
                    String xn = xnxq.get("xn");
                    String xq = xnxq.get("xq");
                    String name = xnxq.get("xnmc") + xnxq.get("xqmc") + "课表";
                    new importGRKBTask(xn, xq, name, uploadTeacher.isChecked(), syncSubject.isChecked()).execute();
                }
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onFinish() {

            }
        });
    }




    @WorkerThread
    private void getSubjectsInfo(CurriculumCreator ci, String xn, String xq) throws JWException {
        ci.updateSubjectInfo(jwCore.getChosenSubjectsInfo(xn, xq));
    }

    @WorkerThread
    private void uploadTeacherInfo(List<String> teacherId) throws JWException {
        List<Teacher> teacherList = new ArrayList<>();
        for (String id : teacherId) {
            Map<String, String> m = jwCore.getTeacherData(id);
            String teacherCode = m.get("id");
            String gender = m.get("gender");
            String title = m.get("title");
            String school = m.get("school");
            String phone = m.get("phone");
            String email = m.get("email");
            String detail = m.get("detail");
            String name = m.get("name");
            final Teacher t = new Teacher(teacherCode, name, gender, title, school, phone, email, detail);
            if (!teacherList.contains(t)) teacherList.add(t);
        }
        // Log.e("ts",teacherList.toString());
        for (final Teacher t : teacherList) {
            BmobQuery<Teacher> bq = new BmobQuery<>();
            bq.addWhereEqualTo("name", t.getName());
            List<Teacher> queryExisted = bq.findObjectsSync(Teacher.class);
            // Log.e("外部查找教师：",t.getName());
            if(queryExisted!=null&&queryExisted.size()>0){
                Teacher existed = queryExisted.get(0);
                t.setObjectId(existed.getObjectId());
                String result = t.updateSync();
                Log.e("TEACHER", "更新教师信息：" + t.getName()+"   result:"+result);
            }else{
                String result = t.saveSync();
                Log.e("TEACHER", "上传教师信息：" + t.getName()+"   result:"+result);
            }
        }

    }


    @Override
    public int getTitle() {
        return R.string.jw_tabs_frkb;
    }

    @Override
    protected void stopTasks() {
//        if (pageTask != null && pageTask.getStatus() != AsyncTask.Status.FINISHED)
//            pageTask.cancel(true);
    }


    @Override
    public void Refresh() {
        refresh.setRefreshing(false);
        stopTasks();
        spinnerItems.clear();
        int i = 0;
        int now = 0;
        for (Map<String, String> item : jwRoot.getXNXQItems()) {
            if (item.get("sfdqxq").equals("1")) now = i;
            spinnerItems.add(item.get("xnmc") + item.get("xqmc"));
            i++;
        }
        xnxqAdapter.notifyDataSetChanged();
        xnxq_picker.setSelection(now);
    }



    @SuppressLint("StaticFieldLeak")
    class importGRKBTask extends AsyncTask {

        String xn;
        String xq;
        String kbName;
        boolean syncSubject;
        boolean uploadTeacher;

        public importGRKBTask(String xn, String xq, String kbName, boolean uploadT, boolean syncSubject) {
            this.xn = xn;
            this.xq = xq;
            this.syncSubject = syncSubject;
            this.kbName = kbName;
            this.uploadTeacher = uploadT;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            bt_import_grkb.setProgress(true);

        }

        @Override
        protected Object doInBackground(Object[] objects) {
            String code = xn + xq;
            List<Map<String, String>> kbData = null;
            try {
                kbData = jwCore.getGRKBData(xn, xq);
                Calendar startDate = jwCore.getFirstDateOfCurriculum(xn, xq);
                CurriculumCreator s = CurriculumCreator.create(code, kbName,startDate).loadCourse(kbData);
                getSubjectsInfo(s, xn, xq);
                if (uploadTeacher) {
                    uploadTeacherInfo(jwCore.getTeacherOfChosenSubjects(xn,xq));
                }
                if (timeTableCore.addCurriculum(s, syncSubject)) {
                    ActivityMain.saveData();
                    return true;
                }
                return false;
            } catch (JWException e) {
                return e;
            }



        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            bt_import_grkb.setProgress(false);
            Intent i = new Intent(TIMETABLE_CHANGED);
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(i);
            if (o != null && o instanceof JWException) {
                if (((JWException) o).getType() == JWException.DIALOG_MESSAGE) {
                    AlertDialog ad = new AlertDialog.Builder(FragmentJWTS_grkb.this.getActivity()).create();
                    ad.setTitle("提示");
                    ad.setMessage(((JWException) o).getDialogMessage());
                    ad.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    ad.show();
                } else if (((JWException) o).getType() == JWException.CONNECT_ERROR) {
                    Toast.makeText(HContext, "网络错误", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(HContext, ((Boolean) o) ? "成功" : "失败", Toast.LENGTH_SHORT).show();
                //if(o.toString().contains("成功")) new ActivityJWTS.getPYJHTask().executeOnExecutor(HITAApplication.TPE);
            }
            ActivityMain.saveData();
        }
    }



}
