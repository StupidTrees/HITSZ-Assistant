package com.stupidtree.hita.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.SubjectsListAdapter;
import com.stupidtree.hita.adapter.SubjectsManagerPagerAdapter;
import com.stupidtree.hita.diy.PickNumberDialog;
import com.stupidtree.hita.diy.PickSimpleDateDialog;
import com.stupidtree.hita.timetable.Curriculum;
import com.stupidtree.hita.timetable.Subject;
import com.stupidtree.hita.util.ColorBox;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.TPE;
import static com.stupidtree.hita.HITAApplication.defaultSP;
import static com.stupidtree.hita.HITAApplication.timeTableCore;

public class FragmentCurriculum extends BaseFragment {
    private Curriculum curriculum;
    private ViewPager viewpager;
    private SubjectsManagerPagerAdapter pagerAdapter;
    private List<BaseFragment> fragments;
    private TabLayout tabLayout;
    private TextView name;
    private TextView from;
    private TextView totalWeeks;
    private ImageView image, more;
    private LinearLayout setTotalWeek,setStartDate;
    private CardView card;
    private TextView resetColors,resetColorsToTheme;
    private ExpandableLayout expandableLayout;
    private Switch enable_color;

    private OnFragmentInteractionListener mListener;

    public FragmentCurriculum() {
        // Required empty public constructor
    }


    public static FragmentCurriculum newInstance(Curriculum curriculum) {
        FragmentCurriculum fragment = new FragmentCurriculum();
        Bundle args = new Bundle();
        args.putSerializable("curriculum", curriculum);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            curriculum = (Curriculum) getArguments().getSerializable("curriculum");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_curriculum, container, false);
        initPager(v);
        initViews(v);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        Refresh();
    }

    void initViews(View v) {
        setTotalWeek = v.findViewById(R.id.set_total_weeks);
        setStartDate = v.findViewById(R.id.set_start_date);
        name = v.findViewById(R.id.cm_name);
        from = v.findViewById(R.id.start_date_text);
        totalWeeks = v.findViewById(R.id.total_weeks_text);
        image = v.findViewById(R.id.cm_image);
        more = v.findViewById(R.id.cm_more);
        card = v.findViewById(R.id.card);
        enable_color = v.findViewById(R.id.enable_color);
        expandableLayout = v.findViewById(R.id.expandable);
        resetColorsToTheme = v.findViewById(R.id.reset_colors_to_theme);
        resetColors = v.findViewById(R.id.reset_colors);
        resetColors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog ad = new AlertDialog.Builder(getContext()).setTitle(getString(R.string.dialog_title_random_allocate))
                        .setNegativeButton(getString(R.string.button_cancel),null).setPositiveButton(getString(R.string.button_confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new resetColorTask().executeOnExecutor(TPE);
                            }
                        }).create();
                ad.show();
            }
        });
        resetColorsToTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog ad = new AlertDialog.Builder(getContext()).setTitle(getString(R.string.dialog_title_set_to_theme))
                        .setNegativeButton(getString(R.string.button_cancel),null).setPositiveButton(getString(R.string.button_confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new resetColorToThemeTask().executeOnExecutor(TPE);
                            }
                        }).create();
                ad.show();
            }
        });

        boolean colorfulOn = defaultSP.getBoolean("timetable_colorful_mode",true);
        enable_color.setChecked(colorfulOn);
        if(colorfulOn) expandableLayout.expand();
        else  expandableLayout.collapse();

        enable_color.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                if(isChecked)expandableLayout.expand();
                else  expandableLayout.collapse();
                new enableColorTask(isChecked).execute();
            }
        });
        setTotalWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new PickNumberDialog((BaseActivity) getActivity(), getString(R.string.dialog_title_set_total_weeks), 50, 1, new PickNumberDialog.onDialogConformListener() {
                    @SuppressLint("StaticFieldLeak")
                    @Override
                    public void onClick(int number) {
                        curriculum.setTotalWeeks(number);
                        new saveCurriculumTask().executeOnExecutor(TPE);
                    }
                }).setInitialValue(curriculum.getTotalWeeks()).show();
            }
        });

        setStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new PickSimpleDateDialog((BaseActivity) getActivity(), new PickSimpleDateDialog.onDialogConformListener() {
                    @Override
                    public void onConfirm(Calendar date) {
                        curriculum.setStartDate(date);
                        new saveCurriculumTask().executeOnExecutor(TPE);
                    }
                }).setInitialValue(curriculum.getStart_year(),curriculum.getStart_month(),curriculum.getStart_day())
                .show();
            }
        });
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu pm = new PopupMenu(getContext(), v);
                pm.getMenuInflater().inflate(R.menu.menu_opr_curriculum, pm.getMenu());
                pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.curriculum_opr_detail) {
                            AlertDialog ad = new AlertDialog.Builder(getContext()).setTitle(getString(R.string.dialog_title_curriculum_detail)).
                                    setMessage(String.format(getString(R.string.dialog_message_curriculum_detail),curriculum.getName(),curriculum.getCurriculumCode())).create();
                            ad.show();
                            return true;
                        } else if (item.getItemId() == R.id.curriculum_opr_delete) {
                            AlertDialog ad = new AlertDialog.Builder(getContext()).setTitle(getString(R.string.attention)).setMessage(getString(R.string.dialog_message_delete_curriculum)).
                                    setPositiveButton(getString(R.string.button_confirm), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            new deleteTask(curriculum.getCurriculumCode()).executeOnExecutor(TPE);
                                        }
                                    }).setNegativeButton(getString(R.string.button_cancel), null).
                                    create();
                            ad.show();
                            return true;
                        }
                        return false;
                    }
                });
                pm.show();
            }
        });
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog ad = new AlertDialog.Builder(getContext()).setTitle(getString(R.string.dialog_title_curriculum_detail)).
                        setMessage(String.format(getString(R.string.dialog_message_curriculum_detail),curriculum.getName(),curriculum.getCurriculumCode())).create();
                ad.show();
            }
        });
    }

    void initPager(View v) {
        // Log.e("init","pager");
        fragments = new ArrayList<>();
        tabLayout = v.findViewById(R.id.subjects_tablayout);
        viewpager = v.findViewById(R.id.subjects_viewpager);
        String[] titles = getResources().getStringArray(R.array.curriculum_tabs);
        fragments.add(FragmentSubjects.newInstance(curriculum.getCurriculumCode()));
        fragments.add(FragmentTeachers.newInstance(curriculum.getCurriculumCode()));
        pagerAdapter = new SubjectsManagerPagerAdapter(getChildFragmentManager(), fragments, Arrays.asList(titles));
        viewpager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewpager);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setTabIndicatorFullWidth(false);
    }



    class deleteTask extends AsyncTask {

        String curriculumCode;

        deleteTask(String curriculumCode) {
            this.curriculumCode = curriculumCode;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            return timeTableCore.deleteCurriculum(curriculumCode);
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if ((Boolean) o) {
                Toast.makeText(HContext, "删除成功！", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(HContext, "删除失败！", Toast.LENGTH_SHORT).show();
            }
            mListener.onFragmentInteraction();
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    protected void stopTasks() {

    }

    @Override
    public void Refresh() {
        if (curriculum.getName().indexOf("(") > 0)
            name.setText(curriculum.getName().substring(0, curriculum.getName().indexOf("(")));
        else name.setText(curriculum.getName());
        from.setText(getString(R.string.curriculum_manager_satrtat) + curriculum.readStartDate());
        totalWeeks.setText(String.format(getString(R.string.curriculum_manager_total),curriculum.getTotalWeeks()));
        if (curriculum.getName().contains("春")) image.setImageResource(R.drawable.ic_spring);
        else if (curriculum.getName().contains("夏")) image.setImageResource(R.drawable.ic_summer);
        else if (curriculum.getName().contains("秋")) image.setImageResource(R.drawable.ic_autumn);
        else if (curriculum.getName().contains("冬")) image.setImageResource(R.drawable.ic_winter);
        else image.setImageResource(R.drawable.ic_menu_jwts);

    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction();
    }


    class enableColorTask extends AsyncTask{

        boolean enable;

        public enableColorTask(boolean enable) {
            this.enable = enable;
        }

        @SuppressLint("ApplySharedPref")
        @Override
        protected Object doInBackground(Object[] objects) {
            defaultSP.edit().putBoolean("timetable_colorful_mode",enable).commit();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            ((FragmentSubjects)fragments.get(0)).Refresh(true);
        }
    }
    @SuppressLint("StaticFieldLeak")
    class resetColorToThemeTask extends AsyncTask{

        @SuppressLint("ApplySharedPref")
        @Override
        protected Object doInBackground(Object[] objects) {
            for(Subject s:timeTableCore.getCurrentCurriculum().getSubjects()){
                defaultSP.edit().putInt("color:"+s.getName(),getColorPrimary()).commit();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            ((FragmentSubjects)fragments.get(0)).Refresh(true);
            super.onPostExecute(o);
        }
    }
    class resetColorTask extends AsyncTask{

        @SuppressLint("ApplySharedPref")
        @Override
        protected Object doInBackground(Object[] objects) {
            for(Subject s:timeTableCore.getCurrentCurriculum().getSubjects()){
                defaultSP.edit().putInt("color:"+s.getName(), ColorBox.getRandomColor_Material()).commit();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            ((FragmentSubjects)fragments.get(0)).Refresh(true);
            super.onPostExecute(o);
        }
    }

    class saveCurriculumTask extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] objects) {
            curriculum.saveToDB();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            Refresh();
            Toast.makeText(getContext(), R.string.curriculum_updated,Toast.LENGTH_SHORT).show();
        }
    }
}
