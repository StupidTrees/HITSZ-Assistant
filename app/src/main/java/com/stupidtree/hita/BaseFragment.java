package com.stupidtree.hita;

import androidx.fragment.app.Fragment;
import android.util.Log;
import android.util.TypedValue;

public abstract class BaseFragment extends Fragment {
    abstract protected void stopTasks();
    public abstract void Refresh();
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTasks();
        Log.e("onDestroy","停止任务");
    }

    public int getBGIconColor(){
        TypedValue typedValue = new  TypedValue();
        getActivity().getTheme().resolveAttribute(R.attr.background_icon_color, typedValue, true);
        return typedValue.data;
    }
    public int getColorPrimary(){
        TypedValue typedValue = new  TypedValue();
        getActivity().getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        return typedValue.data;
    }
}
