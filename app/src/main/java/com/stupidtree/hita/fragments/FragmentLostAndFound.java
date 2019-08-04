package com.stupidtree.hita.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.LostAndFoundListAdapter;
import com.stupidtree.hita.online.HITAUser;
import com.stupidtree.hita.online.LostAndFound;
import com.stupidtree.hita.util.ActivityUtils;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentLostAndFound.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentLostAndFound#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentLostAndFound extends BaseFragment {
    String type;
    RecyclerView list;
    LostAndFoundListAdapter listAdapter;
    List<LostAndFound> listRes;
    SwipeRefreshLayout pullRefreshLayout;
    private OnFragmentInteractionListener mListener;

    public FragmentLostAndFound() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static FragmentLostAndFound newInstance(String type) {
        FragmentLostAndFound fragment = new FragmentLostAndFound();
        Bundle args = new Bundle();
        args.putString("type",type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getString("type");
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View v = inflater.inflate(R.layout.fragment_lost_and_found, container, false);
        pullRefreshLayout =v. findViewById(R.id.society_swiperefresh);
       initList(v);
       return v;
    }

    void initList(View v){
        list = v.findViewById(R.id.society_list);
        listRes = new ArrayList<>();
        listAdapter = new LostAndFoundListAdapter(getActivity(),listRes);
        list.setAdapter(listAdapter);
        RecyclerView.LayoutManager lm = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        list.setLayoutManager(lm);
        pullRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Refresh();
            }
        });
        listAdapter.setmOnPostClickListener(new LostAndFoundListAdapter.OnPostClickListener() {
            @Override
            public void OnClick(View v, LostAndFound laf, HITAUser author) {
                ActivityUtils.startPostDetailActivity(getActivity(),laf,author);
            }
        });

    }


    @Override
    public void onResume() {
        super.onResume();
        Refresh();
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
        pullRefreshLayout.setRefreshing(true);
        final BmobQuery<LostAndFound> query = new BmobQuery<>();
        query.addWhereEqualTo("type",type);
        query.order("-createdAt")
                .findObjects(new FindListener<LostAndFound>(){
                    @Override
                    public void done(List<LostAndFound> list2, BmobException e) {
                        if(e==null){
                            listRes.clear();
                            for(LostAndFound hp:list2){
                                listRes.add(hp);
                            }
                            listAdapter.notifyDataSetChanged();
                            pullRefreshLayout.setRefreshing(false);
                            list.scheduleLayoutAnimation();
                        }

                    }
                });
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
