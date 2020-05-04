package com.stupidtree.hita.fragments.timetable_manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.stupidtree.hita.fragments.BaseFragment;
import com.stupidtree.hita.timetable.packable.Curriculum;


public abstract class FragmentCurriculumChild extends BaseFragment {
    CurriculumPageRoot root;
    boolean willRefreshOnResume = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        root = null;
    }

    @Override
    protected void stopTasks() {

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        willRefreshOnResume = true;
        if (context instanceof CurriculumPageRoot) {
            root = (CurriculumPageRoot) context;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        if(willRefreshOnResume) {
//            willRefreshOnResume = false;
//            Refresh();
//        }

    }

    public void setWillRefreshOnResume(boolean willRefreshOnResume) {
        this.willRefreshOnResume = willRefreshOnResume;
    }

    @Override
    public void Refresh() {

    }

    public interface CurriculumPageRoot {
        void onChangeColorSettingsRefresh();

        void onModifiedCurriculumRefresh();

        void onCurriculumDeleteRefresh();

        Curriculum getCurriculum();

        SharedPreferences getTimetableSP();
    }
}
