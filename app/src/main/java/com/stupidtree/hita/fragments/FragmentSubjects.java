package com.stupidtree.hita.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivitySubject;
import com.stupidtree.hita.adapter.SubjectsListAdapter;

import static com.stupidtree.hita.HITAApplication.DATA_STATE_HEALTHY;
import static com.stupidtree.hita.HITAApplication.allCurriculum;
import static com.stupidtree.hita.HITAApplication.getDataState;
import static com.stupidtree.hita.HITAApplication.thisCurriculumIndex;

public class FragmentSubjects extends Fragment {


    SubjectsListAdapter subjectsAdapter;
    RecyclerView subjectsList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View  v = inflater.inflate(R.layout.fragment_subjets, container, false);
        if(getDataState()==DATA_STATE_HEALTHY) initSubjects(v);
        return v;
    }



    void initSubjects(View v){
        subjectsList = v.findViewById(R.id.usercenter_subjects_list);
        subjectsAdapter = new SubjectsListAdapter(getContext(),allCurriculum.get(thisCurriculumIndex).getSubjects());
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

    public interface OnFragmentInteractionListener {

    }
}
