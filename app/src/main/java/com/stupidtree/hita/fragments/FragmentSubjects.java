package com.stupidtree.hita.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivitySubject;
import com.stupidtree.hita.adapter.SubjectsListAdapter;
import com.stupidtree.hita.timetable.Curriculum;
import com.stupidtree.hita.timetable.Subject;

import java.util.ArrayList;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.defaultSP;
import static com.stupidtree.hita.HITAApplication.timeTableCore;


public class FragmentSubjects extends BaseFragment {


    private String curriculumCode;
    private SubjectsListAdapter subjectsAdapter;
    private RecyclerView subjectsList;
    ArrayList<Object> listRes;
    private refreshListTask pageTask;
    View emptyView;
    private boolean firstResume = true;

    public FragmentSubjects() {
    }

    public static FragmentSubjects newInstance(String curriculumCode){
       //  Log.e("newInstance",curriculumCode);
        FragmentSubjects fs = new FragmentSubjects();
        Bundle b = new Bundle();
        b.putString("curriculum_code",curriculumCode);
        fs.setArguments(b);
        return fs;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.e("create:",getArguments().getString("curriculum_code"));
        if(getArguments()!=null){
            curriculumCode = getArguments().getString("curriculum_code");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_subjets, container, false);
        if (timeTableCore.isDataAvailable()) initSubjects(v);
        return v;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (timeTableCore.isDataAvailable()) Refresh(firstResume);
        if(firstResume) firstResume = false;
    }

    private void initSubjects(View v) {
        listRes = new ArrayList<>();
        emptyView = v.findViewById(R.id.empty_view);
        subjectsList = v.findViewById(R.id.usercenter_subjects_list);
        subjectsAdapter = new SubjectsListAdapter(this, listRes, 1);
        subjectsList.setLayoutManager(new LinearLayoutManager(getContext()));
        subjectsList.setAdapter(subjectsAdapter);
        subjectsAdapter.setColorfulMode(defaultSP.getBoolean("subjects_color_enable",false));
        subjectsAdapter.setmOnItemClickListener(new SubjectsListAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (listRes.get(position) instanceof Subject) {
//                    ActivityOptionsCompat op = ActivityOptionsCompat.makeSceneTransitionAnimation(FragmentSubjects.this.getActivity()
//                    ,view,"card");
                    Intent i = new Intent(FragmentSubjects.this.getActivity(), ActivitySubject.class);
                    i.putExtra("subject", ((Subject) subjectsAdapter.getList().get(position)).getName());
                    FragmentSubjects.this.getActivity().startActivity(i);
                }

            }
        });
    }


    @Override
    protected void stopTasks() {
        if (pageTask != null && pageTask.getStatus()!=AsyncTask.Status.FINISHED) pageTask.cancel(true);
    }

    @Override
    public void Refresh() {

    }

    public void Refresh(boolean anim){
        if (pageTask != null && pageTask.getStatus()!=AsyncTask.Status.FINISHED) pageTask.cancel(true);
        pageTask = new refreshListTask(anim);
        pageTask.executeOnExecutor(HITAApplication.TPE);
    }

    public interface OnFragmentInteractionListener {

    }



    class refreshListTask extends AsyncTask {
        boolean anim;
        boolean color;
        public refreshListTask(boolean anim) {
            this.anim = anim;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            listRes.clear();
            color = defaultSP.getBoolean("subjects_color_enable",false);
            List<Subject> all = Curriculum.getSubjects(curriculumCode);
            //timeTableCore.getAllEvents();
            List<Subject> exam = new ArrayList<>();
            List<Subject> other = new ArrayList<>();
            List<Subject> mooc = new ArrayList<>();
            for (Subject s : all) {
                if (s.isExam()) exam.add(s);
                else if (s.isMOOC()) mooc.add(s);
                else other.add(s);
            }

            listRes.add("考试课");
            listRes.addAll(exam);
            listRes.add("考查课");
            listRes.addAll(other);
            listRes.add("MOOC");
            listRes.addAll(mooc);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if(listRes.size()>0){
                emptyView.setVisibility(View.GONE);
                subjectsList.setVisibility(View.VISIBLE);
            }else{
                emptyView.setVisibility(View.VISIBLE);
                subjectsList.setVisibility(View.GONE);
            }
            subjectsAdapter.setColorfulMode(color);
            subjectsAdapter.notifyDataSetChanged();
            if(anim)subjectsList.scheduleLayoutAnimation();
        }
    }
}
