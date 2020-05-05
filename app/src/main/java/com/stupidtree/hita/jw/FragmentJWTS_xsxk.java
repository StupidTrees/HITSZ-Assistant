package com.stupidtree.hita.jw;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.BaseTabAdapter;
import com.stupidtree.hita.views.MaterialCircleAnimator;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FragmentJWTS_xsxk extends JWFragment implements FragmentJW_xk_popup.XKPageRoot {
    private List<String> spinnerOptionsXNXQ;
    private ArrayAdapter<? extends String> spinnerAdapterXNXQ;
    private Spinner spinnerXNXQ;
    private String xn, xq;
    private boolean filter_no_vacancy, filter_conflict;

    public FragmentJWTS_xsxk() {
        /* Required empty public constructor */
    }


    public static JWFragment newInstance() {
        FragmentJWTS_xsxk fragment = new FragmentJWTS_xsxk();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        super.onAttachFragment(childFragment);
        if (childFragment instanceof FragmentJWTS_xsxk_second) {
            ((FragmentJWTS_xsxk_second) childFragment).setXkPageRoot(this);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_jwts_xsxk;
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        initPage(v);
        initPager(v);
        initRefresh(v);
    }


    private void initPager(View v) {
        ViewPager pager = v.findViewById(R.id.pager);
        TabLayout tabs = v.findViewById(R.id.tabs);
        final int[] titles = new int[]{R.string.jw_xk_tabs_yx, R.string.jw_xk_tabs_bx, R.string.jw_xk_tabs_wlts, R.string.jw_xk_tabs_mooc
                , R.string.jw_xk_tabs_ty, R.string.jw_xk_tabs_cx, R.string.jw_xk_tabs_xx, R.string.jw_xk_tabs_cxyx, R.string.jw_xk_tabs_cxsy
                , R.string.jw_xk_tabs_fankzy, R.string.jw_xk_tabs_kzy};
        pager.setAdapter(new BaseTabAdapter(getChildFragmentManager(), 11) {
            @Override
            protected Fragment initItem(int position) {
                switch (position) {
                    case 0:
                        return FragmentJWTS_xsxk_second_yx.newInstance(R.string.jw_xk_tabs_yx);
                    case 1:
                        return FragmentJWTS_xsxk_second.newInstance("bx-b-b", R.string.jw_xk_tabs_bx);
                    case 2:
                        return FragmentJWTS_xsxk_second.newInstance("tsk-b-b", R.string.jw_xk_tabs_wlts);
                    case 3:
                        return FragmentJWTS_xsxk_second.newInstance("mooc-b-b", R.string.jw_xk_tabs_mooc);
                    case 4:
                        return FragmentJWTS_xsxk_second.newInstance("ty-b-b", R.string.jw_xk_tabs_ty);
                    case 5:
                        return FragmentJWTS_xsxk_second.newInstance("cx-b-b", R.string.jw_xk_tabs_cx);
                    case 6:
                        return FragmentJWTS_xsxk_second.newInstance("xx-b-b", R.string.jw_xk_tabs_xx);
                    case 7:
                        return FragmentJWTS_xsxk_second.newInstance("cxyx-b-b", R.string.jw_xk_tabs_cxyx);
                    case 8:
                        return FragmentJWTS_xsxk_second.newInstance("cxsy-b-b", R.string.jw_xk_tabs_cxsy);
                    case 9:
                        return FragmentJWTS_xsxk_second.newInstance("fankzy-b-b", R.string.jw_xk_tabs_fankzy);
                    case 10:
                        return FragmentJWTS_xsxk_second.newInstance("kzy-b-b", R.string.jw_xk_tabs_kzy);
                }
                return null;
            }

            @NonNull
            @Override
            public CharSequence getPageTitle(int position) {
                return getString(titles[position]);
            }
        }.setDestroyFragment(false));
        tabs.setupWithViewPager(pager);
    }

    private void initPage(View v) {
        ViewGroup optionsButton = v.findViewById(R.id.more);
        final ImageView optionsArrow = v.findViewById(R.id.more_arrow);
        final ExpandableLayout expandableLayout = v.findViewById(R.id.expand);
        optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expandableLayout.toggle();
                MaterialCircleAnimator.rotateTo(expandableLayout.isExpanded(), optionsArrow);
            }
        });
        CheckBox switch_conflict = v.findViewById(R.id.filter_conflict);
        CheckBox switch_no_vacancy = v.findViewById(R.id.filter_novacancy);
        switch_conflict.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filter_conflict = isChecked;
                refreshAllPages();
            }
        });
        switch_no_vacancy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filter_no_vacancy = isChecked;
                refreshAllPages();
            }
        });
        spinnerOptionsXNXQ = new ArrayList<>();
       // xnxqOptions = new ArrayList<>();

        spinnerXNXQ = v.findViewById(R.id.spinner_xsxk_xnxq);
        spinnerAdapterXNXQ = new ArrayAdapter<>(v.getContext(), R.layout.dynamic_xnxq_spinner_item, spinnerOptionsXNXQ);
        spinnerAdapterXNXQ.setDropDownViewResource(R.layout.dynamic_xnxq_spinner_dropdown_item);
         AdapterView.OnItemSelectedListener spinnerSelect = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                xn = jwRoot.getXNXQItems().get(position).get("xn");
                xq = jwRoot.getXNXQItems().get(position).get("xq");
//                Refresh();
                for (Fragment f : getChildFragmentManager().getFragments()) {
                    if (f instanceof JWFragment) {
                        if (f.isResumed()) {
                            ((JWFragment) f).Refresh();
                        } else {
                            ((JWFragment) f).setWillRefreshOnResume(true);
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
        spinnerXNXQ.setOnItemSelectedListener(spinnerSelect);
        spinnerXNXQ.setAdapter(spinnerAdapterXNXQ);
    }


    @Override
    public int getTitle() {
        return R.string.jw_tabs_xk;
    }

    @Override
    protected void stopTasks() {

    }


    @Override
    public void Refresh() {
        int i = 0;
        int now = 0;
        for (Map<String, String> item : jwRoot.getXNXQItems()) {
            if (Objects.equals(item.get("sfdqxq"), "1")) now = i;
            spinnerOptionsXNXQ.add(item.get("xnmc")+item.get("xqmc"));
            i++;
        }
        spinnerAdapterXNXQ.notifyDataSetChanged();
        spinnerXNXQ.setSelection(now);
    }

    @Override
    public void refreshAllPages() {
        for (Fragment f : getChildFragmentManager().getFragments()) {
            if (f instanceof JWFragment) {
                if (f.isResumed()) {
                    ((JWFragment) f).Refresh();
                } else {
                    ((JWFragment) f).setWillRefreshOnResume(true);
                }
            }
        }
    }

    @Override
    public String getXn() {
        return xn;
    }

    @Override
    public String getXq() {
        return xq;
    }

    @Override
    public boolean getFilterNoVacancy() {
        return filter_no_vacancy;
    }

    @Override
    public boolean getFilterConflict() {
        return filter_conflict;
    }




}
