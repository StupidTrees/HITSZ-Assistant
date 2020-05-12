package com.stupidtree.hita.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.fragments.BaseOperationTask;
import com.stupidtree.hita.timetable.packable.Subject;

import java.util.ArrayList;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.timeTableCore;

public class PickSubjectDialog extends RoundedCornerDialog implements BaseOperationTask.OperationListener<List<Subject>> {
    public static List<Subject> listRes;
    private SearchResultAdapter listAdapter;
    private getSuggestionsTask pageTask;
    private OnPickListener onPickListener;
    private String titleStr;

    public PickSubjectDialog(@NonNull Context context, String title, OnPickListener onPickListener) {
        super(context);
        @SuppressLint("InflateParams") View v = getLayoutInflater().inflate(R.layout.dialog_pick_subject, null, false);
        setView(v);
        this.titleStr = title;
        this.onPickListener = onPickListener;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initList();
    }

    @Override
    protected void onStop() {
        if (pageTask != null) pageTask.cancel(true);
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (pageTask != null && pageTask.getStatus() != AsyncTask.Status.FINISHED) {
            pageTask.cancel(true);
        }
        pageTask = new getSuggestionsTask(this);
        pageTask.executeOnExecutor(HITAApplication.TPE);


    }

    private void initList() {
        listRes = new ArrayList<>();
        RecyclerView list = findViewById(R.id.list);
        TextView title = findViewById(R.id.title);
        title.setText(titleStr);
        listAdapter = new SearchResultAdapter();
        list.setAdapter(listAdapter);
        list.setLayoutManager(new WrapContentLinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        listAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void Onlick(View v, int position) {
                onPickListener.OnPick(listRes.get(position));
                dismiss();
            }
        });

    }

    @Override
    public void onOperationStart(String id, Boolean[] params) {

    }

    @Override
    public void onOperationDone(String id, BaseOperationTask task, Boolean[] params, List<Subject> result) {
        listRes.clear();
        listRes.addAll(result);
        listAdapter.notifyDataSetChanged();
    }

    public interface OnPickListener {
        void OnPick(Subject subject);
    }

    interface OnItemClickListener {
        void Onlick(View v, int position);
    }

    static class getSuggestionsTask extends BaseOperationTask<List<Subject>> {


        getSuggestionsTask(OperationListener listRefreshedListener) {
            super(listRefreshedListener);
        }


        @Override
        protected List<Subject> doInBackground(OperationListener<List<Subject>> listRefreshedListener, Boolean... booleans) {
            return timeTableCore.getSubjects(null);
        }

    }

    public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.holder1> {

        OnItemClickListener onItemClickListener;


        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }


        @NonNull
        @Override
        public holder1 onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.dynamic_search_subject_item, parent, false);
            return new holder1(v);
        }

        @Override
        public void onBindViewHolder(@NonNull holder1 holder, final int position) {
            holder.name.setText(listRes.get(position).getName());
            if (onItemClickListener != null) {
                holder.item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onItemClickListener.Onlick(v, position);
                    }
                });
            }
        }


        @Override
        public int getItemCount() {
            return listRes.size();
        }

        class holder1 extends RecyclerView.ViewHolder {
            TextView name;
            ViewGroup item;

            holder1(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.name);
                item = itemView.findViewById(R.id.item);
            }
        }
    }
}
