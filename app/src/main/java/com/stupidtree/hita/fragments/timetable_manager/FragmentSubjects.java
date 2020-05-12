package com.stupidtree.hita.fragments.timetable_manager;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.BaseListAdapter;
import com.stupidtree.hita.adapter.SubjectsListAdapter;
import com.stupidtree.hita.fragments.BasicRefreshTask;
import com.stupidtree.hita.fragments.popup.FragmentAddEvent;
import com.stupidtree.hita.timetable.packable.Subject;
import com.stupidtree.hita.util.ActivityUtils;
import com.stupidtree.hita.views.EditModeHelper;
import com.stupidtree.hita.views.WrapContentLinearLayoutManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.TPE;
import static com.stupidtree.hita.HITAApplication.timeTableCore;
import static com.stupidtree.hita.timetable.TimeWatcherService.TIMETABLE_CHANGED;


public class FragmentSubjects extends FragmentTimeTableChild
        implements EditModeHelper.EditableContainer,
        BasicRefreshTask.ListRefreshedListener<List<Subject>> {

    private SubjectsListAdapter subjectsAdapter;
    private RecyclerView subjectsList;
    private ArrayList<Subject> listRes;
    private refreshListTask pageTask;
    private boolean firstResume = true;
    private EditModeHelper editModeHelper;

    public FragmentSubjects() {
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_subjets;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (timeTableCore.isDataAvailable()) initSubjects(view);
    }


    @Override
    public void onResume() {
        super.onResume();
        if ((firstResume || willRefreshOnResume) && timeTableCore.isDataAvailable()) {
            Refresh(firstResume);
            willRefreshOnResume = false;
        }
        if (firstResume) firstResume = false;
    }

    private void initSubjects(View v) {
        listRes = new ArrayList<>();
        subjectsList = v.findViewById(R.id.usercenter_subjects_list);
        subjectsList.setItemViewCacheSize(20);
        subjectsAdapter = new SubjectsListAdapter(requireContext(), listRes, root.getTimetableSP());
        subjectsList.setLayoutManager(new WrapContentLinearLayoutManager(requireContext()));
        subjectsList.setAdapter(subjectsAdapter);
        subjectsAdapter.setOnItemClickListener(new BaseListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View card, int position) {
                if (position == listRes.size()) {
                    FragmentAddEvent.newInstance().setInitialType("course").show(getChildFragmentManager(), "ade");
                } else if (!listRes.get(position).getType().equals(Subject.TAG)) {
                    ActivityUtils.startSubjectActivity_name(getActivity(), listRes.get(position).getName());
                }
            }
        });

        subjectsAdapter.setOnItemLongClickListener(new BaseListAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(View view, int position) {
                if (position >= listRes.size() - 1 || listRes.get(position).getType().equals(Subject.TAG))
                    return false;
                editModeHelper.activateEditMode(position);
                return true;
            }
        });
        editModeHelper = new EditModeHelper(requireContext(), subjectsAdapter, this);
        editModeHelper.init(v, R.id.edit_layout, R.layout.edit_mode_bar_3);
        editModeHelper.setSmoothSwitch(true);
        //
    }


    @Override
    protected void stopTasks() {
        if (pageTask != null && pageTask.getStatus() != AsyncTask.Status.FINISHED)
            pageTask.cancel(true);
    }

    @Override
    public void Refresh() {
        Refresh(false);
    }

    public void Refresh(boolean anim) {
        Log.e("refreshingA", this + "," + root);
        if (!isResumed() || root == null || root.getCurriculum() == null) {
            willRefreshOnResume = true;
            return;
        }
        if (pageTask != null && pageTask.getStatus() != AsyncTask.Status.FINISHED)
            pageTask.cancel(true);
        pageTask = new refreshListTask(this);
        Log.e("refreshingB", String.valueOf(anim));
        pageTask.executeOnExecutor(TPE, anim);
    }

    @Override
    public void onEditClosed() {

    }

    @Override
    public void onEditStarted() {

    }

    @Override
    public void onItemCheckedChanged(int position, boolean checked, int currentSelected) {

    }

    @Override
    public void onDelete(Collection toDelete) {
        new deleteSubjectTask(toDelete).execute();
    }


    @Override
    public void onRefreshStart(String id, Boolean[] params) {

    }

    @Override
    public void onListRefreshed(String id, Boolean[] params, List<Subject> newList) {

        if (isDetached() || isRemoving()) return;
        boolean anim = false;
        if (params.length > 0) anim = params[0];
        subjectsList.setVisibility(View.VISIBLE);
        editModeHelper.closeEditMode();
        if (anim) {
            listRes.clear();
            listRes.addAll(newList);
            subjectsAdapter.notifyDataSetChanged();
            subjectsList.scheduleLayoutAnimation();
        } else subjectsAdapter.notifyItemChangedSmooth(newList);
    }


    public interface OnFragmentInteractionListener {

    }


    static class refreshListTask extends BasicRefreshTask<List<Subject>> {


        refreshListTask(ListRefreshedListener listRefreshedListener) {
            super(listRefreshedListener);
        }

        @Override
        protected List<Subject> doInBackground(ListRefreshedListener listRefreshedListener, Boolean... booleans) {
            super.doInBackground(listRefreshedListener, booleans);
            List<Subject> newList = new ArrayList<>();
            if (!timeTableCore.isDataAvailable()) return newList;
            List<Subject> all = timeTableCore.getSubjects(null);
            //timeTableCore.getAllEvents();
            List<Subject> exam = new ArrayList<>();
            List<Subject> other = new ArrayList<>();
            List<Subject> mooc = new ArrayList<>();
            for (Subject s : all) {
                if (s.isExam()) exam.add(s);
                else if (s.isMOOC()) mooc.add(s);
                else other.add(s);
            }
            if (exam.size() > 0)
                newList.add(Subject.getTagInstance(HContext.getString(R.string.counted_in_GPA)));
            newList.addAll(exam);
            if (other.size() > 0)
                newList.add(Subject.getTagInstance(HContext.getString(R.string.not_counted_in_GPA)));
            newList.addAll(other);
            if (mooc.size() > 0) newList.add(Subject.getTagInstance("MOOC"));
            newList.addAll(mooc);
            return newList;
        }


    }

    class deleteSubjectTask extends AsyncTask {

        Collection<Subject> toDelete;

        public deleteSubjectTask(Collection<Subject> toDelete) {
            this.toDelete = toDelete;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                for (Subject s : toDelete) {
                    timeTableCore.deleteSubject(s.getName(), timeTableCore.getCurrentCurriculum().getCurriculumCode());
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if ((boolean) o) {
                Toast.makeText(requireContext(), R.string.delete_success, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), R.string.delete_failed, Toast.LENGTH_SHORT).show();
            }
            Intent i = new Intent(TIMETABLE_CHANGED);
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(i);
        }
    }
}
