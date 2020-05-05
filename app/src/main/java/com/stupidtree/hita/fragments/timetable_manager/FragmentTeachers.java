package com.stupidtree.hita.fragments.timetable_manager;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.timetable.packable.EventItem;
import com.stupidtree.hita.timetable.packable.Subject;
import com.stupidtree.hita.util.ActivityUtils;
import com.stupidtree.hita.views.WrapContentLinearLayoutManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.timeTableCore;


public class FragmentTeachers extends FragmentCurriculumChild {
    RecyclerView list;
    List<Map<String, String>> listRes;
    listAdapter listAdapter;
    refreshListTask pageTask;

    boolean firstResume = true;

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

    void initList(View v) {
        list = v.findViewById(R.id.teachers_list);
        listAdapter = new listAdapter();
        listRes = new ArrayList<>();
        list.setAdapter(listAdapter);
        list.setLayoutManager(new WrapContentLinearLayoutManager(requireContext()));
    }

    @Override
    protected void stopTasks() {
        if(pageTask!=null&&pageTask.getStatus()!=AsyncTask.Status.FINISHED) pageTask.cancel(true);
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
        if(pageTask!=null&&pageTask.getStatus()!=AsyncTask.Status.FINISHED) pageTask.cancel(true);
        pageTask =  new refreshListTask(anim);
        pageTask.executeOnExecutor(HITAApplication.TPE);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    class refreshListTask extends AsyncTask {

        boolean anim;

        public refreshListTask(boolean anim) {
            this.anim = anim;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            listRes.clear();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            if (!timeTableCore.isDataAvailable()) return false;
            List<Subject> sl = root.getCurriculum().getSubjects();
            for (Subject s : sl) {
                EventItem ei = s.getFirstCourse();
                if (ei == null) continue;
                if(TextUtils.isEmpty(ei.getTag3())) continue;
                for (String name : ei.getTag3().split("，")) {
                    Map m = new HashMap();
                    m.put("name", name);
                    m.put("subject", s.getName());
                    listRes.add(m);
                }
            }

            return true;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (!(Boolean) o) {
                Toast.makeText(HContext, "无教师信息", Toast.LENGTH_SHORT).show();
            }
            listAdapter.notifyDataSetChanged();
            if(anim) list.scheduleLayoutAnimation();
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

            public viewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.teacher_name);
                subject = itemView.findViewById(R.id.teacher_subject);
                card = itemView.findViewById(R.id.teacher_card);
            }
        }
    }
}
