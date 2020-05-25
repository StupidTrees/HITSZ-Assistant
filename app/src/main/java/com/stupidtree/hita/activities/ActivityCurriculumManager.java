package com.stupidtree.hita.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Pair;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.BaseListAdapter;
import com.stupidtree.hita.adapter.SubjectsManagerPagerAdapter;
import com.stupidtree.hita.fragments.BaseOperationTask;
import com.stupidtree.hita.fragments.BasicRefreshTask;
import com.stupidtree.hita.fragments.popup.FragmentImportCurriculum;
import com.stupidtree.hita.fragments.timetable_manager.FragmentTimeTableChild;
import com.stupidtree.hita.fragments.timetable_manager.FragmentTimeTableSettings;
import com.stupidtree.hita.timetable.CurriculumCreator;
import com.stupidtree.hita.timetable.TimetableCore;
import com.stupidtree.hita.timetable.packable.Curriculum;
import com.stupidtree.hita.util.FileOperator;
import com.stupidtree.hita.views.WrapContentLinearLayoutManager;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.TPE;
import static com.stupidtree.hita.activities.ActivityMain.saveData;
import static com.stupidtree.hita.timetable.TimeWatcherService.TIMETABLE_CHANGED;

public class ActivityCurriculumManager extends BaseActivity implements FragmentTimeTableSettings.CurriculumPageRoot
        , BaseOperationTask.OperationListener<Object>
        , BasicRefreshTask.ListRefreshedListener<List<Curriculum>> {

    private static final int CHOOSE_FILE_CODE = 0;
    Toolbar mToolbar;
    LinearLayout noneLayout;
    List<Curriculum> pagerData;
    BroadcastReceiver receiver;
    boolean willRefreshOnResume;
    private ViewPager viewpager;
    private TabLayout tabLayout;
    private TextView name;

    // private ExpandableLayout headExpand;
    private ImageView image;//, more;
    //private TextView isCurrent;//,switchToCurrent;
    private LinearLayout swapButton;
    private ImageView swapIcon;
    private ExpandableLayout swapExpand;
    private CListAdapter listAdapter;
    private RecyclerView list;
    private Curriculum curriculumShow;
    private SharedPreferences timetableSP;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true, false, false);
        setContentView(R.layout.activity_curriculum_manager);
        timetableSP = getSharedPreferences("timetable_pref", MODE_PRIVATE);
        initReceiver();
        initToolbar();
        initViews();
        initCurriculumList();
        initPager();
        Refresh();
    }

    void initReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getStringExtra("type") != null && Objects.equals(intent.getStringExtra("type"), "time_tick")) {
                    return;
                }
                try {
                    Refresh();
                } catch (Exception e) {
                    willRefreshOnResume = true;
                }
            }
        };
        IntentFilter IF = new IntentFilter();
        IF.addAction(TIMETABLE_CHANGED);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, IF);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    void initCurriculumList() {
        list = findViewById(R.id.list);
        pagerData = new ArrayList<>();
        listAdapter = new CListAdapter(this, pagerData);
        list.setAdapter(listAdapter);

        list.setLayoutManager(new WrapContentLinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
    }

    void initToolbar() {
        Toolbar toolbar = findViewById(R.id.main_tool_bar);
        // toolbar.setTitle("课表管理");
        toolbar.inflateMenu(R.menu.toolbar_curriculum_manager);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.add) {
                    new FragmentImportCurriculum().show(getSupportFragmentManager(), "import");
                }
                return true;
            }
        });
    }

    void initPager() {
        tabLayout = findViewById(R.id.tabs);
        viewpager = findViewById(R.id.subjects_viewpager);
        String[] titles = getResources().getStringArray(R.array.curriculum_tabs);
        SubjectsManagerPagerAdapter pagerAdapter = new SubjectsManagerPagerAdapter(getSupportFragmentManager(), Arrays.asList(titles));
        viewpager.setOffscreenPageLimit(5);
        viewpager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewpager);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setTabIndicatorFullWidth(false);
    }

    void initViews() {
        noneLayout = findViewById(R.id.none_layout);
        mToolbar = findViewById(R.id.main_tool_bar);
        swapButton = findViewById(R.id.swap);
        swapExpand = findViewById(R.id.expand);
        swapIcon = findViewById(R.id.more);
        image = findViewById(R.id.cm_image);
        name = findViewById(R.id.cm_name);
        swapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float fromD, toD;
                if (!swapExpand.isExpanded()) {
                    fromD = 0f;
                    toD = 180f;
                } else {
                    fromD = 180f;
                    toD = 0f;
                }
                RotateAnimation ra = new RotateAnimation(fromD, toD, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                ra.setInterpolator(new DecelerateInterpolator());
                ra.setDuration(300);//设置动画持续周期
                ra.setRepeatCount(0);//设置重复次数
                ra.setFillAfter(true);//动画执行完后是否停留在执行完的状态
                swapIcon.setAnimation(ra);
                swapIcon.startAnimation(ra);
                swapExpand.toggle();
            }
        });
    }


    public void Refresh() {
        curriculumShow = TimetableCore.getInstance(HContext).getCurrentCurriculum();
        if (curriculumShow == null) {
            name.setText(R.string.curriculum_none);
            pagerData.clear();
            listAdapter.notifyDataSetChanged();
            tabLayout.setVisibility(View.INVISIBLE);
            viewpager.setVisibility(View.GONE);
            noneLayout.setVisibility(View.VISIBLE);
            swapButton.setVisibility(View.GONE);
            return;
        } else {
            tabLayout.setVisibility(View.VISIBLE);
            viewpager.setVisibility(View.VISIBLE);
            noneLayout.setVisibility(View.GONE);
            swapButton.setVisibility(View.VISIBLE);
        }
        name.setText(curriculumShow.getName());
//        if (curriculumShow.getName().indexOf("(") > 0)
//            name.setText(curriculumShow.getName().substring(0, curriculumShow.getName().indexOf("(")));
//        else name.setText(curriculumShow.getName());
        if (curriculumShow.getName().contains("春")) image.setImageResource(R.drawable.ic_spring);
        else if (curriculumShow.getName().contains("夏"))
            image.setImageResource(R.drawable.ic_summer);
        else if (curriculumShow.getName().contains("秋"))
            image.setImageResource(R.drawable.ic_autumn);
        else if (curriculumShow.getName().contains("冬"))
            image.setImageResource(R.drawable.ic_winter);
        else image.setImageResource(R.drawable.ic_menu_jwts);
        for (Fragment fx : getSupportFragmentManager().getFragments()) {
            if (!(fx instanceof FragmentTimeTableChild)) continue;
            FragmentTimeTableChild f = (FragmentTimeTableChild) fx;
            try {
                f.Refresh();
            } catch (Exception e) {
                e.printStackTrace();
                f.setWillRefreshOnResume(true);
            }
        }

        viewpager.setCurrentItem(0);
        new RefreshHeadListTask(this).execute();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_curriculum_manager, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (willRefreshOnResume) {
            willRefreshOnResume = false;
            Refresh();
        }
    }

    @Override
    public void onChangeColorSettingsRefresh() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments.size() < 1 || !(fragments.get(0) instanceof FragmentTimeTableChild)) return;
        FragmentTimeTableChild f = (FragmentTimeTableChild) fragments.get(0);
        try {
            f.Refresh();
        } catch (Exception e) {
            f.setWillRefreshOnResume(true);
            e.printStackTrace();
        }
    }

    @Override
    public void onModifiedCurriculumRefresh() {
        //Refresh();
    }

    @Override
    public void onCurriculumDeleteRefresh() {
        //Refresh();
    }

    @Override
    public Curriculum getCurriculum() {
        return curriculumShow;
    }

    @Override
    public SharedPreferences getTimetableSP() {
        return timetableSP;
    }

    //当选择完Excel文件后调用此函数
    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CHOOSE_FILE_CODE) {
                Uri uri = data.getData();
                String sPath1;
                sPath1 = FileOperator.getPath(this, uri); // Paul Burke写的函数，根据Uri获得文件路径
                if (sPath1 == null) return;
                final File file = new File(sPath1);
                new loadCurriculumTask(this, file, Calendar.getInstance()).executeOnExecutor(HITAApplication.TPE);


            }


        }


        super.onActivityResult(requestCode, resultCode, data);

    }


    @Override
    public void onOperationStart(String id, Boolean[] params) {

    }

    @SuppressLint("StringFormatMatches")
    @Override
    public void onOperationDone(String id, BaseOperationTask task, Boolean[] params, Object resObject) {
        if (resObject instanceof Boolean) {
            boolean result = (boolean) resObject;
            switch (id) {
                case "delete":
                    if (result) {
                        Toast.makeText(HContext, R.string.delete_success, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(HContext, R.string.delete_failed, Toast.LENGTH_SHORT).show();
                    }
                    Intent i = new Intent(TIMETABLE_CHANGED);
                    LocalBroadcastManager.getInstance(getThis()).sendBroadcast(i);
                    ActivityMain.saveData();
                    swapIcon.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    break;
                case "change":
                    Intent i2 = new Intent(TIMETABLE_CHANGED);
                    LocalBroadcastManager.getInstance(getThis()).sendBroadcast(i2);
                    Toast.makeText(getThis(), R.string.curriculum_changed, Toast.LENGTH_SHORT).show();
                    break;
            }
        } else if (resObject instanceof Pair) {
            Pair pair = (Pair) resObject;
            if ((boolean) pair.first) {
                Toast.makeText(ActivityCurriculumManager.this, getString(R.string.import_excel_success, pair.second), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ActivityCurriculumManager.this, R.string.import_failed, Toast.LENGTH_SHORT).show();
            }
            Intent i3 = new Intent(TIMETABLE_CHANGED);
            LocalBroadcastManager.getInstance(getThis()).sendBroadcast(i3);
            saveData();
        }
    }


    @Override
    public void onRefreshStart(String id, Boolean[] params) {

    }

    @Override
    public void onListRefreshed(String id, Boolean[] params, List<Curriculum> result) {
        listAdapter.notifyItemChangedSmooth(result);
        int index = -1;
        for (int i = 0; i < pagerData.size(); i++) {
            Curriculum c = pagerData.get(i);
            if (c.getCurriculumCode().equals(TimetableCore.getInstance(HContext).getCurrentCurriculum().getCurriculumCode())) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            list.scrollToPosition(index);
        }
    }


    static class RefreshHeadListTask extends BasicRefreshTask<List<Curriculum>> {
        List<Curriculum> result;

        RefreshHeadListTask(ListRefreshedListener<? extends List<Curriculum>> listRefreshedListener) {
            super(listRefreshedListener);
        }

        @Override
        protected List<Curriculum> doInBackground(ListRefreshedListener listRefreshedListener, Boolean... booleans) {
            result = new ArrayList<>();
            for (Curriculum c : TimetableCore.getInstance(HContext).getAllCurriculum()) {
                if (c != null) result.add(c);
            }
            return result;
        }
    }

    static class loadCurriculumTask extends BaseOperationTask<Pair<Boolean, Integer>> {

        Calendar startDate;
        File file;

        loadCurriculumTask(OperationListener<Object> listRefreshedListener, File f, Calendar start) {
            super(listRefreshedListener);
            startDate = start;
            this.file = f;
            id = "import_excel";
        }


        @Override
        protected Pair<Boolean, Integer> doInBackground(OperationListener listRefreshedListener, Boolean... booleans) {
            try {
                boolean result = true;
                List<Curriculum> all = TimetableCore.getInstance(HContext).getAllCurriculum();
                List<CurriculumCreator> res = FileOperator.loadCurriculumFromExcel(file, startDate);
                for (CurriculumCreator cc : res) {
                    for (Curriculum existed : all) {
                        String name = cc.getName();
                        String eN = existed.getName();
                        if (eN.equals(name)) {
                            int from = eN.lastIndexOf("(");
                            int to = eN.lastIndexOf(")");
                            if (from > 0 && to > 0 && from < to && eN.endsWith(")")) {
                                String number = eN.substring(from + 1, to);
                                try {
                                    int num = Integer.parseInt(number);
                                    cc.setName(name.substring(0, name.lastIndexOf("(")) + "(" + (num + 1) + ")");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                cc.setName(eN + "(0)");
                            }
                        }
                    }
                    result = result && TimetableCore.getInstance(HContext).addCurriculum(cc, true);
                }

                return new Pair<>(result, res.size());
            } catch (Exception e) {
                e.printStackTrace();
                return new Pair<>(false, 0);
            }
        }

    }

    static class deleteTask extends BaseOperationTask<Boolean> {

        String curriculumCode;

        deleteTask(OperationListener<?> listRefreshedListener, String curriculumCode) {
            super(listRefreshedListener);
            this.curriculumCode = curriculumCode;
            id = "delete";
        }


        @Override
        protected Boolean doInBackground(OperationListener listRefreshedListener, Boolean... booleans) {
            return TimetableCore.getInstance(HContext).deleteCurriculum(curriculumCode);
        }

    }

    static class changeTask extends BaseOperationTask<Boolean> {

        String newID;

        changeTask(OperationListener<?> listRefreshedListener, String newID) {
            super(listRefreshedListener);
            this.newID = newID;
            id = "change";
        }


        @Override
        protected Boolean doInBackground(OperationListener listRefreshedListener, Boolean... booleans) {
            return TimetableCore.getInstance(HContext).changeCurrentCurriculum(newID);
        }
    }

    class CListAdapter extends BaseListAdapter<Curriculum, CListAdapter.CHolder> {


        CListAdapter(Context context, List<Curriculum> mBeans) {
            super(context, mBeans);
        }

        @Override
        protected int getLayoutId(int viewType) {
            return R.layout.dynamic_curriculum_item;
        }

        @Override
        public CHolder createViewHolder(View v, int viewType) {
            return new CHolder(v);
        }


        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull final CHolder holder, int position) {
            final Curriculum c = pagerData.get(position);
            holder.title.setText(c.getName());
            final boolean isCurrent = c.getCurriculumCode().equals(TimetableCore.getInstance(HContext).getCurrentCurriculum().getCurriculumCode());
            holder.switchTo.setChecked(isCurrent);
            holder.subtitle.setText(getString(R.string.curriculum_manager_satrtat) + c.readStartDate());
            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    AlertDialog ad = new AlertDialog.Builder(getThis()).setTitle(getString(R.string.attention)).setMessage(getString(R.string.dialog_message_delete_curriculum)).
                            setPositiveButton(getString(R.string.button_confirm), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new deleteTask(ActivityCurriculumManager.this, c.getCurriculumCode()).executeOnExecutor(TPE);
                                }
                            }).setNegativeButton(getString(R.string.button_cancel), null).
                            create();
                    ad.show();
                }
            });
            holder.switchTo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isCurrent) return;
                    v.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
                    new changeTask(ActivityCurriculumManager.this, c.getCurriculumCode()).execute();
                }
            });
            holder.switchTo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (buttonView.isPressed()) buttonView.setChecked(!isChecked);
                }
            });
        }

        class CHolder extends RecyclerView.ViewHolder {
            TextView title, subtitle;
            ImageView delete;
            CheckBox switchTo;

            CHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.title);
                subtitle = itemView.findViewById(R.id.subtitle);
                delete = itemView.findViewById(R.id.delete);
                switchTo = itemView.findViewById(R.id.check);
            }
        }

    }


}




