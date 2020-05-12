package com.stupidtree.hita.fragments.timetable_manager;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.fragments.BaseOperationTask;
import com.stupidtree.hita.timetable.packable.EventItem;
import com.stupidtree.hita.timetable.packable.Subject;
import com.stupidtree.hita.util.ActivityUtils;
import com.stupidtree.hita.views.WrapContentLinearLayoutManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.stupidtree.hita.HITAApplication.timeTableCore;


public class FragmentTeachers extends FragmentTimeTableChild implements BaseOperationTask.OperationListener<List<Map<String,String>>> {
    private RecyclerView list;
    private List<Map<String, String>> listRes;
    private listAdapter listAdapter;

    private boolean firstResume = true;

    public FragmentTeachers() {
        // Required empty public constructor
    }


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_teachers;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initList(view);
    }




    @Override
    public void onResume() {
        super.onResume();
        if (willRefreshOnResume) {
            Refresh(firstResume);
            willRefreshOnResume = false;
        }
        if(firstResume) firstResume = false;
    }

    private void initList(View v) {
        list = v.findViewById(R.id.teachers_list);
        listAdapter = new listAdapter();
        listRes = new ArrayList<>();
        list.setAdapter(listAdapter);
        list.setLayoutManager(new WrapContentLinearLayoutManager(requireContext()));
    }

    @Override
    protected void stopTasks() {
          }

    @Override
    public void Refresh() {
        Refresh(true);
    }
    public void Refresh(boolean anim) {
        if (root == null || root.getCurriculum() == null) {
            willRefreshOnResume = true;
            return;
        }
        new refreshListTask(this,root.getCurriculum().getCurriculumCode(),anim).executeOnExecutor(HITAApplication.TPE);
    }

    @Override
    public void onOperationStart(String id, Boolean[] params) {

    }

    @Override
    public void onOperationDone(String id, BaseOperationTask task, Boolean[] params, List<Map<String, String>> result) {
        refreshListTask rt = (refreshListTask) task;
        listRes.clear();
        listRes.addAll(result);
        listAdapter.notifyDataSetChanged();
        if(rt.anim) list.scheduleLayoutAnimation();
    }


    static class refreshListTask extends BaseOperationTask<List< Map<String, String>>> {
        String curriculumCode;
        boolean anim;

        refreshListTask(OperationListener listRefreshedListener, String curriculumCode, boolean anim) {
            super(listRefreshedListener);
            this.curriculumCode = curriculumCode;
            this.anim = anim;
        }


        @Override
        protected List< Map<String, String>> doInBackground(OperationListener<List< Map<String, String>>> listRefreshedListener, Boolean... booleans) {
            List<Map<String, String>> result = new ArrayList<>();
            if (!timeTableCore.isDataAvailable()) return result;
            List<Subject> sl = timeTableCore.getSubjects(curriculumCode);
            for (Subject s : sl) {
                EventItem ei = timeTableCore.getFirstCourse(s);
                if (ei == null) continue;
                if(TextUtils.isEmpty(ei.getTag3())) continue;
                for (String name : ei.getTag3().split("，")) {
                    Map<String, String> m = new HashMap<>();
                    m.put("name", name);
                    m.put("subject", s.getName());
                    result.add(m);
                }
            }

            return  result;
        }
    }

    class listAdapter extends RecyclerView.Adapter<listAdapter.viewHolder> {


        @NonNull
        @Override
        public viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new viewHolder(getLayoutInflater().inflate(R.layout.dynamic_teacher, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull viewHolder viewHolder, final int i) {
            viewHolder.subject.setText(listRes.get(i).get("subject"));
            viewHolder.name.setText(listRes.get(i).get("name"));
            viewHolder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityUtils.searchFor(getActivity(), listRes.get(i).get("name"),"teacher");
                }
            });
        }

        @Override
        public int getItemCount() {
            return listRes.size();
        }

        class viewHolder extends RecyclerView.ViewHolder {
            TextView name, subject;
            View card;

            viewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.teacher_name);
                subject = itemView.findViewById(R.id.teacher_subject);
                card = itemView.findViewById(R.id.teacher_card);
            }
        }
    }
}
