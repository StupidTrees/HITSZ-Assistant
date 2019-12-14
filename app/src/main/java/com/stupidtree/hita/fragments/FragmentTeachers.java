package com.stupidtree.hita.fragments;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.core.Curriculum;
import com.stupidtree.hita.core.Subject;
import com.stupidtree.hita.core.timetable.EventItem;
import com.stupidtree.hita.diy.WrapContentLinearLayoutManager;
import com.stupidtree.hita.util.ActivityUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.allCurriculum;
import static com.stupidtree.hita.HITAApplication.isDataAvailable;
import static com.stupidtree.hita.HITAApplication.thisCurriculumIndex;


public class FragmentTeachers extends BaseFragment {
    RecyclerView list;
    List<Map<String, String>> listRes;
    listAdapter listAdapter;
    refreshListTask pageTask;
    String curriculumCode;
    boolean firstResume = true;

    public FragmentTeachers() {
        // Required empty public constructor
    }


    public static FragmentTeachers newInstance(String curriculumCode) {
        FragmentTeachers fragment = new FragmentTeachers();
        Bundle args = new Bundle();
        args.putString("curriculum_code",curriculumCode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments()!=null){
            curriculumCode = getArguments().getString("curriculum_code");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_teachers, container, false);
        initList(v);

        return v;
    }


    @Override
    public void onResume() {
        super.onResume();
        Refresh(firstResume);
        if(firstResume) firstResume = false;

    }

    void initList(View v) {
        list = v.findViewById(R.id.teachers_list);
        listAdapter = new listAdapter();
        listRes = new ArrayList<>();
        list.setAdapter(listAdapter);
        list.setLayoutManager(new WrapContentLinearLayoutManager(getContext()));
    }

    @Override
    protected void stopTasks() {
        if(pageTask!=null&&pageTask.getStatus()!=AsyncTask.Status.FINISHED) pageTask.cancel(true);
    }

    @Override
    public void Refresh() {

    }
    public void Refresh(boolean anim) {
        if(pageTask!=null&&pageTask.getStatus()!=AsyncTask.Status.FINISHED) pageTask.cancel(true);
        pageTask =  new refreshListTask(anim);
        pageTask.executeOnExecutor(HITAApplication.TPE);;
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
            if (!isDataAvailable()) return false;
            List<Subject> sl = Curriculum.getSubjects(curriculumCode);
            for (Subject s : sl) {
                EventItem ei = s.getFirstCourse();
                if (ei == null) continue;
                for (String name : ei.tag3.split("，")) {
                    Map m = new HashMap();
                    m.put("name", name);
                    m.put("subject", s.name);
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
                    ActivityUtils.startTeacherActivity(getActivity(), listRes.get(i).get("name"));
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
