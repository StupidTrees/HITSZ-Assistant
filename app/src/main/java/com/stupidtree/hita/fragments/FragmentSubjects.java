package com.stupidtree.hita.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivitySubject;
import com.stupidtree.hita.adapter.SubjectsListAdapter;
import com.stupidtree.hita.core.Curriculum;
import com.stupidtree.hita.core.Subject;

import java.util.ArrayList;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.DATA_STATE_HEALTHY;
import static com.stupidtree.hita.HITAApplication.getDataState;

public class FragmentSubjects extends BaseFragment {


    private String curriculumCode;
    private SubjectsListAdapter subjectsAdapter;
    private RecyclerView subjectsList;
    ArrayList<Object> listRes;
    private refreshListTask pageTask;

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
        if (getDataState() == DATA_STATE_HEALTHY) initSubjects(v);
        return v;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (getDataState() == DATA_STATE_HEALTHY) Refresh(firstResume);
        if(firstResume) firstResume = false;
    }

    private void initSubjects(View v) {
        listRes = new ArrayList<Object>();
        subjectsList = v.findViewById(R.id.usercenter_subjects_list);
        subjectsAdapter = new SubjectsListAdapter(this, listRes, 2);
        GridLayoutManager glm = new GridLayoutManager(getContext(), 2);
        subjectsList.setLayoutManager(glm);
        subjectsList.setAdapter(subjectsAdapter);
        subjectsAdapter.setmOnItemClickListener(new SubjectsListAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (listRes.get(position) instanceof Subject) {
//                    ActivityOptionsCompat op = ActivityOptionsCompat.makeSceneTransitionAnimation(FragmentSubjects.this.getActivity()
//                    ,view,"card");
                    Intent i = new Intent(FragmentSubjects.this.getActivity(), ActivitySubject.class);
                    i.putExtra("subject", ((Subject) subjectsAdapter.getList().get(position)).name);
                    FragmentSubjects.this.getActivity().startActivity(i);
                }

            }
        });
    }

    @Override
    protected void stopTasks() {
        if (pageTask != null && !pageTask.isCancelled()) pageTask.cancel(true);
    }

    @Override
    public void Refresh() {

    }

    public void Refresh(boolean anim){
        if (pageTask != null && !pageTask.isCancelled()) pageTask.cancel(true);
        pageTask = new refreshListTask(anim);
        pageTask.execute();
    }

    public interface OnFragmentInteractionListener {

    }



    class refreshListTask extends AsyncTask {
        boolean anim;

        public refreshListTask(boolean anim) {
            this.anim = anim;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            listRes.clear();
            List<Subject> all = Curriculum.getSubjects(curriculumCode);
            //mainTimeTable.getAllEvents();
            List<Subject> exam = new ArrayList<>();
            List<Subject> other = new ArrayList<>();
            List<Subject> mooc = new ArrayList<>();
            for (Subject s : all) {
                if (s.exam) exam.add(s);
                else if (s.isMOOC) mooc.add(s);
                else other.add(s);
            }

            listRes.add("考试课");
            listRes.addAll(exam);
            listRes.add("考查课");
            listRes.addAll(other);
            listRes.add("MOOC");
            listRes.addAll(mooc);
            listRes.add((Integer)1);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            subjectsAdapter.notifyDataSetChanged();
            if(anim)subjectsList.scheduleLayoutAnimation();
        }
    }
}
