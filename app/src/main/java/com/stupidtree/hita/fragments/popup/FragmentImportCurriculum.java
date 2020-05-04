package com.stupidtree.hita.fragments.popup;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivityMain;
import com.stupidtree.hita.timetable.CurriculumCreator;
import com.stupidtree.hita.timetable.packable.Curriculum;
import com.stupidtree.hita.util.ActivityUtils;
import com.stupidtree.hita.util.FileOperator;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import cn.bmob.v3.BmobArticle;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.timeTableCore;
import static com.stupidtree.hita.timetable.TimeWatcherService.TIMETABLE_CHANGED;

@SuppressLint("ValidFragment")
public class FragmentImportCurriculum extends FragmentRadiusPopup {

    private CardView newC, eas, excel;
    private ExpandableLayout expand;
    private Button instruction;
    private Button select;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = View.inflate(getContext(), R.layout.fragment_import_curriculum, null);
        initViews(view);
        return view;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    void initViews(View v) {
        newC = v.findViewById(R.id.newC);
        eas = v.findViewById(R.id.EAS);
        expand = v.findViewById(R.id.expand);
        instruction = v.findViewById(R.id.instruction);
        select = v.findViewById(R.id.pick);
        excel = v.findViewById(R.id.excel);
        newC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                new newTask().execute();
                dismiss();
            }
        });

        eas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                ActivityUtils.startJWTSActivity(getActivity());
            }
        });
        excel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expand.toggle();
            }
        });
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                FileOperator.chooseFile(getActivity(), getActivity().getExternalFilesDir(null));
            }
        });

        instruction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BmobQuery<BmobArticle> bq = new BmobQuery<>();
                bq.addWhereEqualTo("objectId", "Fwz4777A");
                bq.findObjects(new FindListener<BmobArticle>() {
                    @Override
                    public void done(List<BmobArticle> list, BmobException e) {
                        if (list != null && list.size() > 0 && e == null) {
                            ActivityUtils.openInBrowser(getActivity(), list.get(0).getUrl());
                        } else {
                            Toast.makeText(getContext(), R.string.check_your_network, Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }
        });

    }

    class newTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            List<Curriculum> all = timeTableCore.getAllCurriculum();
            int ex = 0;
            String name = "新建课表";
            for (Curriculum c : all) {
                if (c.getName().equals(name + "(" + ex + ")")) {
                    ex++;
                }
            }
            CurriculumCreator cc = CurriculumCreator.create(UUID.randomUUID().toString(), name + "(" + ex + ")", Calendar.getInstance());
            timeTableCore.addCurriculum(cc, true);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            Toast.makeText(HContext, HContext.getString(R.string.curriculum_created), Toast.LENGTH_SHORT).show();
            ActivityMain.saveData();
            Intent i = new Intent(TIMETABLE_CHANGED);
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(i);
        }
    }

}
