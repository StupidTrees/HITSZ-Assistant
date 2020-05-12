package com.stupidtree.hita.fragments.user;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.stupidtree.hita.R;
import com.stupidtree.hita.fragments.BaseFragment;
import com.stupidtree.hita.timetable.TimetableCore;

import static com.stupidtree.hita.HITAApplication.timeTableCore;


public class FragmentUserCenter_sync extends BaseFragment {

    public FragmentUserCenter_sync() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        LinearLayout load_from_cloud = v.findViewById(R.id.load_from_cloud);
        LinearLayout save_to_cloud = v.findViewById(R.id.save_to_cloud);
        save_to_cloud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeTableCore.saveDataToCloud(new TimetableCore.OnDoneListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(requireContext(), R.string.upload_done, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Toast.makeText(requireContext(), R.string.upload_failed, Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                });
//                new upLoadTask().executeOnExecutor(TPE);
            }
        });
        load_from_cloud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog ad = new AlertDialog.Builder(getActivity()).setTitle(R.string.attention).setMessage("同步数据将清除所有本地数据").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        timeTableCore.loadDataFromCloud();
                    }
                }).setNegativeButton(R.string.button_cancel, null).create();
                ad.show();
            }
        });
    }

    @Override
    protected void stopTasks() {

    }

    @Override
    public void Refresh() {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_user_center_sync;
    }


}