package com.stupidtree.hita.eas;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.XSXKListAdapter;
import com.stupidtree.hita.fragments.BaseOperationTask;
import com.stupidtree.hita.hita.TextTools;
import com.stupidtree.hita.timetable.packable.Subject;
import com.stupidtree.hita.views.ButtonLoading;
import com.stupidtree.hita.views.WrapContentLinearLayoutManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.stupidtree.hita.HITAApplication.TPE;
import static com.stupidtree.hita.HITAApplication.jwCore;
import static com.stupidtree.hita.HITAApplication.timeTableCore;

public class FragmentJWTS_xsxk_second_yx extends FragmentJWTS_xsxk_second implements BaseOperationTask.OperationListener<Object> {


    public FragmentJWTS_xsxk_second_yx() {
        // Required empty public constructor
    }

    public static FragmentJWTS_xsxk_second newInstance(int title) {
        FragmentJWTS_xsxk_second_yx fragment = new FragmentJWTS_xsxk_second_yx();
        Bundle args = new Bundle();
        args.putInt("title", title);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    void initList(final View v) {
        notification = v.findViewById(R.id.xsxk_notification);
        list = v.findViewById(R.id.xsxk_list);
        listRes = new ArrayList<>();
        listResFull = new ArrayList<>();
        listAdapter = new XSXKListAdapter(requireContext(), listRes, true);
        list.setAdapter(listAdapter);
        list.setLayoutManager(new WrapContentLinearLayoutManager(getActivity()));
        listAdapter.setOnItemClickListener(new XSXKListAdapter.OnItemClickListener() {
            @Override
            public void OnClick(View view, int position) {
                if (position == listRes.size()) {
                    if (view instanceof ButtonLoading) {
                        ButtonLoading b = (ButtonLoading) view;
                        new SyncSubjectTask(FragmentJWTS_xsxk_second_yx.this, xkPageRoot.getXn(), xkPageRoot.getXq(), b).execute();
                    }
                } else
                    new FragmentJW_xk_popup(FragmentJWTS_xsxk_second_yx.this, "tk", listResFull.get(position)).show(getBaseActivity().getSupportFragmentManager(), "xk");
            }
        });
    }


    @Override
    protected void stopTasks() {

    }


    @Override
    public void Refresh() {
        new refreshListTask(this, xkPageRoot.getXn(), xkPageRoot.getXq()).executeOnExecutor(TPE);
    }

    @Override
    public void onOperationStart(String id, Boolean[] params) {
        list.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onOperationDone(String id, BaseOperationTask task, Boolean[] params, Object o) {
        switch (id) {
            case "refresh":
                refreshListTask rt = (refreshListTask) task;
                listRes.clear();
                listRes.addAll(rt.listRes);
                listResFull.clear();
                listResFull.addAll(rt.listResFull);
                pageInfo = rt.pageInfo;
                if (o != null) {
                    notification.setVisibility(View.VISIBLE);
                    notification.setText(o.toString());
                } else notification.setVisibility(View.GONE);
                list.setVisibility(View.VISIBLE);
                listAdapter.notifyDataSetChanged();
                list.scheduleLayoutAnimation();
                refresh.setRefreshing(false);
                break;
            case "sync":
                SyncSubjectTask st = (SyncSubjectTask) task;
                if (st.bt.get() != null) {
                    st.bt.get().setProgress(false);
                }
                int number = st.number;
                if ((boolean) o) {
                    Toast.makeText(requireContext(), String.format(getString(R.string.subject_sync_success), number), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), R.string.subject_sync_failed, Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }


    static class refreshListTask extends BaseOperationTask<String> {
        String xn;
        String xq;
        Boolean hasButton;
        List<Map<String, String>> listResFull;
        List<Map<String, String>> listRes;
        JsonObject pageInfo;

        refreshListTask(OperationListener listRefreshedListener, String xn, String xq) {
            super(listRefreshedListener);
            this.xn = xn;
            this.xq = xq;
            hasButton = false;
            listRes = new ArrayList<>();
            listResFull = new ArrayList<>();
            id = "refresh";
        }


        @Override
        protected String doInBackground(OperationListener<String> listRefreshedListener, Boolean... booleans) {
            try {
                List<Map<String, String>> res = jwCore.getYXList(xn, xq);
                if (res.size() > 0 && (res.get(0).get("header") != null && Objects.equals(res.get(0).get("header"), "true"))) {
                    Map<String, String> header = res.get(0);
                    try {
                        pageInfo = new JsonParser().parse(Objects.requireNonNull(header.get("page"))).getAsJsonObject();
                    } catch (Exception e) {
                        pageInfo = null;
                    }
                    res.remove(header);
                }
                listResFull.addAll(res);
                for (Map<String, String> m : listResFull) {
                    Map<String, String> mToShow = new HashMap<>();
                    mToShow.put("name", m.get("kcmc"));
                    mToShow.put("type", m.get("kcxzmc"));
                    mToShow.put("xs", m.get("xs") + "学时");
                    mToShow.put("credit", m.get("xf") + "学分");
                    listRes.add(mToShow);
                }
                // Log.e("map-", String.valueOf(keyToTitle));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    static class SyncSubjectTask extends BaseOperationTask<Object> {

        int number = 0;
        WeakReference<ButtonLoading> bt;
        String xn, xq;

        SyncSubjectTask(OperationListener listRefreshedListener, String xn, String xq, ButtonLoading bt) {
            super(listRefreshedListener);
            this.bt = new WeakReference<>(bt);
            this.xn = xn;
            this.xq = xq;
            id = "sync";
        }

        @Override
        protected void onPreExecute(OperationListener<Object> listRefreshedListener) {
            if (bt.get() != null) {
                bt.get().setProgress(true);
            }
        }

        @Override
        protected Object doInBackground(OperationListener<Object> listRefreshedListener, Boolean... booleans) {
            if (!timeTableCore.isDataAvailable()) return false;

            try {
                for (Map<String, String> d : jwCore.getChosenSubjectsInfo(xn, xq)) {
                    boolean found = false;
                    for (Subject s : timeTableCore.getSubjects(null)) {
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
                            s.setMOOC(Objects.equals(d.get("type"), "MOOC"));
                            if (TextUtils.isEmpty(s.getUUID())) {
                                s.setUUID(UUID.randomUUID().toString());
                                timeTableCore.saveSubject(s, "name=? AND curriculum_code=?", new String[]{s.getName(), s.getCurriculumId()});
                            } else {
                                timeTableCore.saveSubject(s);
                            }
                            number++;
                        }
                    }
                    if (Objects.equals(d.get("type"), "MOOC")) {
                        if (!found) {
                            Subject s = new Subject(timeTableCore.getCurrentCurriculum().getCurriculumCode(), d.get("name"), d.get("teacher"));
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
                            timeTableCore.insertSubject(s);
                            number++;
                        }
                    }
                }
                return true;
            } catch (JWException e) {
                e.printStackTrace();
                return false;
            }

        }

    }

}
