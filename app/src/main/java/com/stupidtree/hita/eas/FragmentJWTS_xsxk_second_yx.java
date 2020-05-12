package com.stupidtree.hita.eas;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.gson.JsonParser;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.XSXKListAdapter;
import com.stupidtree.hita.hita.TextTools;
import com.stupidtree.hita.timetable.packable.Subject;
import com.stupidtree.hita.views.ButtonLoading;
import com.stupidtree.hita.views.WrapContentLinearLayoutManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.stupidtree.hita.HITAApplication.TPE;
import static com.stupidtree.hita.HITAApplication.jwCore;
import static com.stupidtree.hita.HITAApplication.timeTableCore;

public class FragmentJWTS_xsxk_second_yx extends FragmentJWTS_xsxk_second {


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
        lisRes = new ArrayList<>();
        listResFull = new ArrayList<>();
        listAdapter = new XSXKListAdapter(requireContext(), lisRes, true);
        list.setAdapter(listAdapter);
        list.setLayoutManager(new WrapContentLinearLayoutManager(getActivity()));
        listAdapter.setOnItemClickListener(new XSXKListAdapter.OnItemClickListener() {
            @Override
            public void OnClick(View view, int position) {
                if (position == lisRes.size()) {
                    if (view instanceof ButtonLoading) {
                        ButtonLoading b = (ButtonLoading) view;
                        new SyncSubjectTask(b).execute();
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
        new refreshListTask(xkPageRoot.getXn(), xkPageRoot.getXq()).executeOnExecutor(TPE);
    }


    class refreshListTask extends RefreshJWPageTask {
        String xn;
        String xq;
        Boolean hasButton;

        refreshListTask(String xn, String xq) {
            this.xn = xn;
            this.xq = xq;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            lisRes.clear();
            list.setVisibility(View.INVISIBLE);
            hasButton = false;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            String toReturn = null;
            try {
                lisRes.clear();
                listResFull.clear();
                List<Map<String, String>> res = jwCore.getYXList(xn, xq);
                if (res.size() > 0 && (res.get(0).get("header") != null && res.get(0).get("header").equals("true"))) {
                    Map<String, String> header = res.get(0);
                    try {
                        pageInfo = new JsonParser().parse(header.get("page")).getAsJsonObject();
                    } catch (Exception e) {
                        pageInfo = null;
                    }
                    res.remove(header);

//                    String begin = header.get("begin");
//                    String end = header.get("end");
//                    res.remove(header);
                }
                listResFull.addAll(res);
                for (Map<String, String> m : listResFull) {
                    Map<String, String> mToShow = new HashMap<>();
                    mToShow.put("name", m.get("kcmc"));
                    mToShow.put("type", m.get("kcxzmc"));
                    mToShow.put("xs", m.get("xs") + "学时");
                    mToShow.put("credit", m.get("xf") + "学分");
                    lisRes.add(mToShow);
                }
                // Log.e("map-", String.valueOf(keyToTitle));
            } catch (Exception e) {
                toReturn = null;
                e.printStackTrace();
            }
            return toReturn;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (o != null) {
                notification.setVisibility(View.VISIBLE);
                notification.setText(o.toString());
            } else notification.setVisibility(View.GONE);
            list.setVisibility(View.VISIBLE);
            listAdapter.notifyDataSetChanged();
            list.scheduleLayoutAnimation();
        }
    }

    class SyncSubjectTask extends AsyncTask {

        int number = 0;
        ButtonLoading bt;

        public SyncSubjectTask(ButtonLoading bt) {
            this.bt = bt;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            bt.setProgress(true);
        }

        @Override
        protected Object doInBackground(Object[] objects) {

            if (!timeTableCore.isDataAvailable()) return false;

            try {
                for (Map<String, String> d : jwCore.getChosenSubjectsInfo(xkPageRoot.getXn(), xkPageRoot.getXq())) {
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
                    if (d.get("type").equals("MOOC")) {
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

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            bt.setProgress(false);
            if ((boolean) o) {
                Toast.makeText(requireContext(), String.format(getString(R.string.subject_sync_success), number), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), R.string.subject_sync_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }

}
