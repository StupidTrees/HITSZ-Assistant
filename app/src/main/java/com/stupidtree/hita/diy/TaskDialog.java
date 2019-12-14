package com.stupidtree.hita.diy;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.core.timetable.EventItem;
import com.stupidtree.hita.core.timetable.Task;
import com.stupidtree.hita.fragments.FragmentTimeLine;
import com.stupidtree.hita.hita.TextTools;

import java.util.ArrayList;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.mainTimeTable;
import static com.stupidtree.hita.adapter.NewsIpNewsListAdapter.dip2px;

public class TaskDialog extends AlertDialog {
    TextView name,progress,length,ddl,start;
    LinearLayout progressLayout,lengthLayout,ddlLayout,startLayout;
    RecyclerView eventList;
    eventListAdapter listAdapter;
    List<EventItem> listRes;
    Task task;
    public
    TaskDialog(@NonNull Context context, Task y) {
        super(context);
        View v = getLayoutInflater().inflate(R.layout.dialog_task,null,false);
        name = v.findViewById(R.id.name);
        progress = v.findViewById(R.id.progress);
        length = v.findViewById(R.id.length);
        ddl = v.findViewById(R.id.ddl);
        progressLayout = v.findViewById(R.id.progress_layout);
        lengthLayout = v.findViewById(R.id.length_layout);
        ddlLayout = v.findViewById(R.id.ddl_layout);
        startLayout = v.findViewById(R.id.start_layout);
        start = v.findViewById(R.id.start);
        eventList = v.findViewById(R.id.event_list);
        listRes = new ArrayList<>();
        listAdapter = new eventListAdapter(listRes,context);
        eventList.setAdapter(listAdapter);
        eventList.setLayoutManager(new WrapContentLinearLayoutManager(context));
        setView(v);
        task = y;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().
                setLayout(dip2px(getContext(), 320), LinearLayout.LayoutParams.WRAP_CONTENT);
        getWindow().
                setBackgroundDrawableResource(R.drawable.dialog_background_radius);
        name.setText(task.name);
        if(task.isHas_length()){
            lengthLayout.setVisibility(View.VISIBLE);
            progressLayout.setVisibility(View.VISIBLE);
            length.setText(task.getLength()+"min");
            progress.setText(task.getProgress()+"%");
        }else {
            lengthLayout.setVisibility(View.GONE);
            progressLayout.setVisibility(View.GONE);
        }
        if(task.has_deadline){
            ddlLayout.setVisibility(View.VISIBLE);
            startLayout.setVisibility(View.VISIBLE);
            start.setText(task.fW+"周"+ TextTools.words_time_DOW[task.fDOW-1]+" "+task.sTime.tellTime());
            ddl.setText(task.tW+"周"+ TextTools.words_time_DOW[task.tDOW-1]+" "+task.eTime.tellTime());
        }else{
            ddlLayout.setVisibility(View.GONE);
            startLayout.setVisibility(View.GONE);
        }
        new refreshListTask().executeOnExecutor(HITAApplication.TPE);;



    }

    public class eventListAdapter extends RecyclerView.Adapter<eventListAdapter.mHolder> {
        List<EventItem> mList;
        Context context;
        eventListAdapter(List<EventItem> res,Context context){
            mList = res;
            this.context = context;
        }


        @NonNull
        @Override
        public mHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.dynamic_dialog_task_eventlist_item,parent,false);
            return new mHolder(v);

        }

        @Override
        public void onBindViewHolder(@NonNull mHolder holder, final int position) {
            final EventItem ei = mList.get(position);
            holder.text.setText(ei.mainName);
            holder.week.setText(ei.week+"周");
            holder.dow.setText(TextTools.words_time_DOW[ei.DOW-1]);
            holder.length.setText(ei.startTime.getDuration(ei.endTime)+"min");
            holder.itemLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                       FragmentTimeLine.showEventDialog((Activity) context,ei,null,null);
                    }
                });


        }


        @Override
        public int getItemCount() {
            return mList.size();
        }

        class mHolder extends RecyclerView.ViewHolder{
            TextView text,week,dow,length;
            LinearLayout itemLayout;
            public mHolder(@NonNull View itemView) {
                super(itemView);
                text = itemView.findViewById(R.id.tasklist_name);
                week = itemView.findViewById(R.id.week);
                dow = itemView.findViewById(R.id.dow);
                length = itemView.findViewById(R.id.length);
                itemLayout = itemView.findViewById(R.id.ei_layout);
            }
        }

    }

    class refreshListTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                listRes.clear();
                for(String x:task.getEvent_map().keySet()){
                    String uuid = x.split(":::")[0];
                    int week = Integer.parseInt(x.split(":::")[1]);
                    listRes.add(mainTimeTable.getEventItemHolderWithUUID(uuid).getEventAtWeek(week));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            listAdapter.notifyDataSetChanged();
        }
    }
}
