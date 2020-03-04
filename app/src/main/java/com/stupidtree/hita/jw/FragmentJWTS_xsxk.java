package com.stupidtree.hita.jw;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.XSXKListAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.TPE;
import static com.stupidtree.hita.HITAApplication.jwCore;

public class FragmentJWTS_xsxk extends JWFragment implements FragmentJW_xk_popup.XKPageRoot {
    //List<Map<String, String>> xnxqOptions;
    List<String> spinnerOptionsXNXQ;
    private ArrayAdapter<? extends String> spinnerAdapterXNXQ;
   // Map<String,String> keyToTitle;
    Spinner spinnerXNXQ;
    ViewPager pager;
    TabLayout tabs;
    String xn,xq;
    boolean filter_no_vacancy,filter_conflict;
    List<FragmentJWTS_xsxk_second> fragments;
    CheckBox switch_no_vacancy,switch_conflict;
    public FragmentJWTS_xsxk() {
        // Required empty public constructor
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_jwts_xsxk, container, false);
        initPage(v);
        initPager(v);
        super.initRefresh(v);
      //  new refreshXNXQTask().executeOnExecutor(TPE);
        return v;
    }

    void initPager(View v){
        fragments = new ArrayList<>();
        fragments.add(new FragmentJWTS_xsxk_second_yx(this,getString(R.string.jw_xk_tabs_yx)));
        fragments.add(new FragmentJWTS_xsxk_second(this,"bx-b-b",getString(R.string.jw_xk_tabs_bx)));
        fragments.add(new FragmentJWTS_xsxk_second(this,"tsk-b-b",getString(R.string.jw_xk_tabs_wlts)));
        fragments.add(new FragmentJWTS_xsxk_second(this,"mooc-b-b",getString(R.string.jw_xk_tabs_mooc)));
        fragments.add(new FragmentJWTS_xsxk_second(this,"ty-b-b",getString(R.string.jw_xk_tabs_ty)));
        fragments.add(new FragmentJWTS_xsxk_second(this,"cx-b-b",getString(R.string.jw_xk_tabs_cx)));
        fragments.add(new FragmentJWTS_xsxk_second(this,"xx-b-b",getString(R.string.jw_xk_tabs_xx)));
        fragments.add(new FragmentJWTS_xsxk_second(this,"cxyx-b-b",getString(R.string.jw_xk_tabs_cxyx)));
        fragments.add(new FragmentJWTS_xsxk_second(this,"cxsy-b-b",getString(R.string.jw_xk_tabs_cxsy)));
        fragments.add(new FragmentJWTS_xsxk_second(this,"fankzy-b-b",getString(R.string.jw_xk_tabs_fankzy)));
        fragments.add(new FragmentJWTS_xsxk_second(this,"kzy-b-b",getString(R.string.jw_xk_tabs_kzy)));
        pager = v.findViewById(R.id.pager);
        tabs = v.findViewById(R.id.tabs);
        pager.setAdapter(new FragmentPagerAdapter(getFragmentManager(),FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
               // super.destroyItem(container, position, object);
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return fragments.get(position).getTitle();
            }

            @Override
            public int getCount() {
                return fragments.size();
            }
        });
        tabs.setupWithViewPager(pager);
    }
    void initPage(View v) {
        switch_conflict = v.findViewById(R.id.filter_conflict);
        switch_no_vacancy = v.findViewById(R.id.filter_novacancy);
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
                for(int i=0;i<fragments.size();i++){
                    if(i==pager.getCurrentItem()){
                        JWFragment current = fragments.get(i);
                        if(current.isResumed()) current.Refresh();
                        else current.setWillRefreshOnResume(true);
                    }else{
                        fragments.get(i).setWillRefreshOnResume(true);
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
    public String getTitle() {
        return HContext.getString(R.string.jw_tabs_xk);
    }

    @Override
    protected void stopTasks() {

    }


    @Override
    public void Refresh() {
        int i = 0;
        int now = 0;
        for (Map<String, String> item : jwRoot.getXNXQItems()) {
            if (item.get("sfdqxq").equals("1")) now = i;
            spinnerOptionsXNXQ.add(item.get("xnmc")+item.get("xqmc"));
            i++;
        }
        spinnerAdapterXNXQ.notifyDataSetChanged();
        spinnerXNXQ.setSelection(now);
        //new refreshXNXQTask().executeOnExecutor(TPE);
//        int i = 0;
//        int now = 0;
//        for (Map<String, String> item : jwRoot.getXNXQItems()) {
//            if (item.get("sfdqxq").equals("1")) now = i;
//            spinnerOptionsXNXQ.add(item.get("xnmc")+item.get("xqmc"));
//            i++;
//        }
//        spinnerXNXQ.setSelection(now);
//        spinnerAdapterXNXQ.notifyDataSetChanged();
    }

    @Override
    public void refreshAllPages() {
        for(int i=0;i<fragments.size();i++){
            if(i==pager.getCurrentItem()){
                JWFragment current = fragments.get(i);
                if(current.isResumed()) current.Refresh();
                else current.setWillRefreshOnResume(true);
            }else{
                fragments.get(i).setWillRefreshOnResume(true);
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


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }


}
