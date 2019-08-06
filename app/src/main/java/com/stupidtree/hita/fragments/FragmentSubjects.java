package com.stupidtree.hita.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.stupidtree.hita.core.Subject;

import java.util.ArrayList;

import static com.stupidtree.hita.HITAApplication.DATA_STATE_HEALTHY;
import static com.stupidtree.hita.HITAApplication.allCurriculum;
import static com.stupidtree.hita.HITAApplication.getDataState;
import static com.stupidtree.hita.HITAApplication.thisCurriculumIndex;

public class FragmentSubjects extends BaseFragment {


    SubjectsListAdapter subjectsAdapter;
    RecyclerView subjectsList;
    ArrayList<Subject> listRes;
    refreshListTask pageTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View  v = inflater.inflate(R.layout.fragment_subjets, container, false);
        if(getDataState()==DATA_STATE_HEALTHY) initSubjects(v);
        return v;
    }


    @Override
    public void onResume() {
        super.onResume();
        if(getDataState()==DATA_STATE_HEALTHY) Refresh();
    }

    void initSubjects(View v){
        listRes = new ArrayList<>();
        subjectsList = v.findViewById(R.id.usercenter_subjects_list);
        subjectsAdapter = new SubjectsListAdapter(getContext(),listRes);
        subjectsList.setAdapter(subjectsAdapter);
        GridLayoutManager glm = new GridLayoutManager(getContext(),2);
        subjectsList.setLayoutManager(glm);
        subjectsAdapter.setmOnItemClickListener(new SubjectsListAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                ActivityOptionsCompat op = ActivityOptionsCompat.makeSceneTransitionAnimation(FragmentSubjects.this.getActivity());
                Intent i = new Intent(FragmentSubjects.this.getActivity(), ActivitySubject.class);
                i.putExtra("subject",subjectsAdapter.getList().get(position).name);
                FragmentSubjects.this.getActivity().startActivity(i,op.toBundle());
            }
        });
    }

    @Override
    protected void stopTasks() {
        if(pageTask!=null&&!pageTask.isCancelled()) pageTask.cancel(true);
    }

    @Override
    protected void Refresh() {
        if(pageTask!=null&&!pageTask.isCancelled()) pageTask.cancel(true);
        pageTask = new refreshListTask();
        pageTask.execute();
    }

    public interface OnFragmentInteractionListener {

    }


    class refreshListTask extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] objects) {
            listRes.clear();
            listRes.addAll(allCurriculum.get(thisCurriculumIndex).getSubjects());
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            subjectsAdapter.notifyDataSetChanged();
        }
    }
}
