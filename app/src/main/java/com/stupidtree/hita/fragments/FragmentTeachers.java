package com.stupidtree.hita.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.stupidtree.hita.R;
import com.stupidtree.hita.core.Subject;
import com.stupidtree.hita.core.timetable.EventItem;
import com.stupidtree.hita.online.Teacher;
import com.stupidtree.hita.util.ActivityUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.allCurriculum;
import static com.stupidtree.hita.HITAApplication.isDataAvailable;
import static com.stupidtree.hita.HITAApplication.thisCurriculumIndex;


public class FragmentTeachers extends Fragment {
    RecyclerView list;
    List<Map<String, String>> listRes;
    listAdapter listAdapter;


    public FragmentTeachers() {
        // Required empty public constructor
    }


    public static FragmentTeachers newInstance() {
        FragmentTeachers fragment = new FragmentTeachers();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        new refreshListTask().execute();
    }

    void initList(View v) {
        list = v.findViewById(R.id.teachers_list);
        listAdapter = new listAdapter();
        listRes = new ArrayList<>();
        list.setAdapter(listAdapter);
        list.setLayoutManager(new GridLayoutManager(getContext(), 2));
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    class refreshListTask extends AsyncTask {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            listRes.clear();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            if (!isDataAvailable()) return false;
            List<Subject> sl = allCurriculum.get(thisCurriculumIndex).getSubjects();
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
            CardView card;

            public viewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.teacher_name);
                subject = itemView.findViewById(R.id.teacher_subject);
                card = itemView.findViewById(R.id.teacher_card);
            }
        }
    }
}
