package com.stupidtree.hita.fragments;

import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.BaseActivity;

public abstract class BaseFragment extends Fragment {
    private View view;
    abstract protected void stopTasks();
    public abstract void Refresh();

    protected abstract int getLayoutId();





    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTasks();
        Log.e("onDestroy","停止任务");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(getLayoutId(), container, false);
        }
        return view;
    }

    public BaseActivity getBaseActivity(){
        return (BaseActivity) getActivity();
    }

    public int getIconColorBottom() {
        TypedValue typedValue = new  TypedValue();
        getActivity().getTheme().resolveAttribute(R.attr.background_icon_color_bottom, typedValue, true);
        return typedValue.data;
    }

    public int getIconColorSecond() {
        TypedValue typedValue = new TypedValue();
        getActivity().getTheme().resolveAttribute(R.attr.background_icon_color_second, typedValue, true);
        return typedValue.data;
    }

    public int getColorControlNormal() {
        TypedValue typedValue = new TypedValue();
        getActivity().getTheme().resolveAttribute(R.attr.colorControlNormal, typedValue, true);
        return typedValue.data;
    }
    public int getColorPrimary(){
        TypedValue typedValue = new  TypedValue();
        getActivity().getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        return typedValue.data;
    }
    public int getColorAccent(){
        TypedValue typedValue = new  TypedValue();
        getActivity().getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true);
        return typedValue.data;
    }
    public int getTextColorPrimary(){
        TypedValue typedValue = new  TypedValue();
        getActivity().getTheme().resolveAttribute(R.attr.text_color_primary, typedValue, true);
        return typedValue.data;
    }


}
