package com.stupidtree.hita.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.CurriculumManagerAdapter;
import com.stupidtree.hita.adapter.SubjectsManagerPagerAdapter;
import com.stupidtree.hita.core.Curriculum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.allCurriculum;
import static com.stupidtree.hita.HITAApplication.deleteCurriculum;

public class FragmentCurriculum extends BaseFragment {
    Curriculum curriculum;
    private ViewPager viewpager;
    private SubjectsManagerPagerAdapter pagerAdapter;
    private List<BaseFragment> fragments;
    private TabLayout tabLayout;
    TextView name;
    TextView from;
    TextView totalWeeks;
    ImageView image, more;
    CardView card;

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
        setInfos();
        return v;
    }

    void initViews(View v) {
        name = v.findViewById(R.id.cm_name);
        from = v.findViewById(R.id.cm_from);
        totalWeeks = v.findViewById(R.id.cm_totalweek);
        image = v.findViewById(R.id.cm_image);
        more = v.findViewById(R.id.cm_more);
        card = v.findViewById(R.id.card);
    }

    void initPager(View v) {
        // Log.e("init","pager");
        fragments = new ArrayList<>();
        tabLayout = v.findViewById(R.id.subjects_tablayout);
        viewpager = v.findViewById(R.id.subjects_viewpager);
        String[] titles = {"科目", "教师"};
        fragments.add(FragmentSubjects.newInstance(curriculum.curriculumCode));
        fragments.add(FragmentTeachers.newInstance(curriculum.curriculumCode));
        pagerAdapter = new SubjectsManagerPagerAdapter(getChildFragmentManager(), fragments, Arrays.asList(titles));
        viewpager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewpager);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setTabIndicatorFullWidth(false);
        Log.e("size:" + curriculum.curriculumCode, fragments.size() + "");
        //tabLayout.setTabTextColors(ColorStateList.valueOf(getColorPrimary()));
    }

    void setInfos() {
        if (curriculum.name.indexOf("(") > 0)
            name.setText(curriculum.name.substring(0, curriculum.name.indexOf("(")));
        else name.setText(curriculum.name);
        from.setText("开始于" + curriculum.readStartDate());
        totalWeeks.setText("共" + curriculum.totalWeeks + "周");
        if (curriculum.name.contains("春")) image.setImageResource(R.drawable.ic_spring);
        else if (curriculum.name.contains("夏")) image.setImageResource(R.drawable.ic_summer);
        else if (curriculum.name.contains("秋")) image.setImageResource(R.drawable.ic_autumn);
        else if (curriculum.name.contains("冬")) image.setImageResource(R.drawable.ic_winter);
        else image.setImageResource(R.drawable.ic_menu_jwts);
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu pm = new PopupMenu(getContext(), v);
                pm.getMenuInflater().inflate(R.menu.menu_opr_curriculum, pm.getMenu());
                pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.curriculum_opr_detail) {
                            AlertDialog ad = new AlertDialog.Builder(getContext()).setTitle("课表详情").
                                    setMessage("课表名称：" + curriculum.name + "\n课表代码：" + curriculum.curriculumCode).create();
                            ad.show();
                            return true;
                        } else if (item.getItemId() == R.id.curriculum_opr_delete) {
                            AlertDialog ad = new AlertDialog.Builder(getContext()).setTitle("提示").setMessage("删除课表将删除一切与之关联的事件、科目、任务，确定吗？").
                                    setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            new deleteTask(curriculum.curriculumCode).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                        }
                                    }).setNegativeButton("取消", null).
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
                AlertDialog ad = new AlertDialog.Builder(getContext()).setTitle("课表详情").
                        setMessage("课表名称：" + curriculum.name + "\n课表代码：" + curriculum.curriculumCode).create();
                ad.show();
            }
        });
    }

    class deleteTask extends AsyncTask {

        String curriculumCode;

        deleteTask(String curriculumCode) {
            this.curriculumCode = curriculumCode;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            return deleteCurriculum(curriculumCode);
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

    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction();
    }


}
