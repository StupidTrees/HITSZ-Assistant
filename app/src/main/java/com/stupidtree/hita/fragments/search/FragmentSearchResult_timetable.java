package com.stupidtree.hita.fragments.search;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.stupidtree.hita.R;
import com.stupidtree.hita.online.SearchException;
import com.stupidtree.hita.online.SearchTeacherCore;
import com.stupidtree.hita.online.SearchTimetableCore;
import com.stupidtree.hita.timetable.TimetableCore;
import com.stupidtree.hita.timetable.timetable.EventItem;
import com.stupidtree.hita.timetable.timetable.Task;
import com.stupidtree.hita.util.ActivityUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.TPE;
import static com.stupidtree.hita.fragments.main.FragmentTimeLine.showEventDialog;

public class FragmentSearchResult_timetable extends FragmentSearchResult{
    private TimeTableSearchAdapter adapter;
    private List<Object> listRes;
    private SearchTimetableCore searchTimeTableCore;
    public FragmentSearchResult_timetable(String title) {
        super(title);
    }
    SearchTask pageTask;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search_result_1,container,false);
        initViews(v);
        searchTimeTableCore = new SearchTimetableCore();
        return v;
    }

    private void initViews(View v) {
        listRes = new ArrayList<>();
        adapter = new TimeTableSearchAdapter(listRes);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void OnClick(View view, int position) {

            }

            @Override
            public void OnClickTransition(View view, int position, View transition) {
                if(listRes.get(position) instanceof EventItem){
                    showEventDialog(getContext(),(EventItem)listRes.get(position),null,null);
                }
            }

            @Override
            public void OnLongClick(View view, int position) {

            }
        });
        initList(v,adapter);
    }

    @Override
    public void Search(boolean hide) {
        super.Search(hide);
        if(TextUtils.isEmpty(searchText)){
            swipeRefreshLayout.setRefreshing(false);
            return;
        }
        if(pageTask!=null&&!pageTask.isCancelled()){
            pageTask.cancel(true);
        }
        pageTask = new SearchTask(searchText,hide);
        pageTask.executeOnExecutor(TPE);

    }

//    @Override
//    public void swipeRefresh() {
//        Search(false);
//    }

    @Override
    protected void stopTasks() {
        if(pageTask!=null&&!pageTask.isCancelled()){
            pageTask.cancel(true);
            pageTask = null;
        }
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        Search();
//    }

    @Override
    public void Refresh() {
        if(!searchText.equals(searchTimeTableCore.getLastKeyword()))Search(true);
        else swipeRefreshLayout.setRefreshing(false);
    }

    class SearchTask extends SearchRefreshTask{


        public SearchTask(String keyword, boolean hideContent) {
            super(keyword, hideContent);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            listRes.clear();
            List<Object> res = null;
            try {
                res = searchTimeTableCore.searchForResult(keyword, Arrays.asList(keyword.replaceAll(" {2}"," ").split(" ")));
            } catch (SearchException e) {
               return e;
            }
            listRes.addAll(res);
            return res.size();
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            adapter.notifyDataSetChanged();
            list.scheduleLayoutAnimation();
            if(o instanceof SearchException){
                result.setText(((SearchException) o).getMessage());
            }else if(listRes.size()>0){
                result.setText(String.format(getString(R.string.timetable_total_searched),listRes.size()));
            }else{
                result.setText(R.string.nothing_found);
            }
        }
    }


    class TimeTableSearchAdapter extends RecyclerView.Adapter<TimeTableSearchAdapter.TeacherSearchViewHoler> {
        private static final int EVENT = 832;
        private static final int TASK = 34;
        List<Object> mBeans;
        OnItemClickListener onItemClickListener;

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        @Override
        public int getItemViewType(int position) {
            if(mBeans.get(position) instanceof EventItem) return EVENT;
            else if(mBeans.get(position) instanceof Task) return TASK;
            return EVENT;
        }

        public TimeTableSearchAdapter(List<Object> mBeans) {
            this.mBeans = mBeans;
        }

        @NonNull
        @Override
        public TeacherSearchViewHoler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int layoutId = viewType==EVENT?R.layout.dynamic_timetable_search_result_item_event:R.layout.dynamic_timetable_search_result_item_event;
            View v = getLayoutInflater().inflate(layoutId,parent,false);
            return new TeacherSearchViewHoler(v,viewType);
        }


        @SuppressLint({"CheckResult", "SetTextI18n"})
        @Override
        public void onBindViewHolder(@NonNull final TeacherSearchViewHoler holder, final int position) {
            if(holder.viewType==EVENT){
                EventItem ei = (EventItem) mBeans.get(position);
                holder.title.setText(ei.getMainName());
                holder.type.setText(String.format(HContext.getString(R.string.week),ei.getWeek())
                +" "+HContext.getResources().getStringArray(R.array.dow1)[ei.getDOW()-1]
                );
                int typeIconId = R.drawable.ic_chatbot_arrangement;
                switch(ei.eventType){
                    case TimetableCore
                            .TIMETABLE_EVENT_TYPE_COURSE:
                        typeIconId = R.drawable.ic_chatbot_course;break;
                    case TimetableCore
                            .TIMETABLE_EVENT_TYPE_EXAM:
                        typeIconId = R.drawable.ic_chatbot_exam;break;
                    case TimetableCore
                            .TIMETABLE_EVENT_TYPE_DEADLINE:
                        typeIconId = R.drawable.ic_chatbot_deadline;break;
                    case TimetableCore
                            .TIMETABLE_EVENT_TYPE_ARRANGEMENT:
                        typeIconId = R.drawable.ic_chatbot_arrangement;break;
                }
                holder.icon.setImageResource(typeIconId);

            }
            if(onItemClickListener!=null)holder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.OnClickTransition(view,position,holder.icon);
                }
            });
        }


        @Override
        public int getItemCount() {
            return mBeans.size();
        }



        class TeacherSearchViewHoler extends RecyclerView.ViewHolder{
            TextView title;
            ImageView icon;
            TextView type;
            CardView card;
            int viewType;
            public TeacherSearchViewHoler(@NonNull View itemView, int viewType) {
                super(itemView);
                this.viewType = viewType;
                card = itemView.findViewById(R.id.card);
                title = itemView.findViewById(R.id.title);
                type = itemView.findViewById(R.id.time);
                icon = itemView.findViewById(R.id.icon);
            }
        }

    }
}
