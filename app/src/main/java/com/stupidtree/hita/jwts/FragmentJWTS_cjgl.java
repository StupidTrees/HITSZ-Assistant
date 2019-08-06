package com.stupidtree.hita.jwts;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentJWTS_cjgl.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentJWTS_cjgl#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentJWTS_cjgl extends BaseFragment {

    private OnFragmentInteractionListener mListener;

    ViewPager pager;
    TabLayout tabs;

    public FragmentJWTS_cjgl() {
        // Required empty public constructor
    }


    public static FragmentJWTS_cjgl newInstance() {
        FragmentJWTS_cjgl fragment = new FragmentJWTS_cjgl();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_jwts_cjgl, container, false);
        initViews(v);
        return v;
    }

    void initViews(View v){
        pager = v.findViewById(R.id.cjgl_pager);
        tabs = v.findViewById(R.id.cjgl_tabs);
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new FragmentJWTS_cjgl_xxjd());
        fragments.add(new FragmentJWTS_cjgl_grcj());
        fragments.add(new FragmentJWTS_cjgl_xfj());
        pager.setAdapter(new pagerAdapter(getFragmentManager(),fragments,new String[]{"学习进度","个人成绩","学分绩"}));
        tabs.setupWithViewPager(pager);
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
    protected void Refresh() {

    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private class pagerAdapter extends FragmentPagerAdapter{

        List<Fragment> mBeans;
        String[] title;
        public pagerAdapter(FragmentManager fm,List<Fragment> res,String[] title) {
            super(fm);
            this.title = title;
            mBeans = res;
        }

        @Override
        public Fragment getItem(int i) {
            return mBeans.get(i);
        }

        @Override
        public int getCount() {
            return mBeans.size();
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            //super.destroyItem(container, position, object);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return title[position];
        }
    }
}
