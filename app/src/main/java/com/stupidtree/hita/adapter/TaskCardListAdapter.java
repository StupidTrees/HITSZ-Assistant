package com.stupidtree.hita.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivityMain;
import com.stupidtree.hita.activities.ActivityTasks;
import com.stupidtree.hita.core.TimeTable;
import com.stupidtree.hita.core.timetable.EventItem;
import com.stupidtree.hita.core.timetable.Task;
import com.stupidtree.hita.diy.WrapContentLinearLayoutManager;
import com.stupidtree.hita.fragments.FragmentAddEvent;
import com.stupidtree.hita.fragments.FragmentAddTask;

import com.stupidtree.hita.hita.TextTools;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import tyrantgit.explosionfield.ExplosionField;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.TPE;
import static com.stupidtree.hita.HITAApplication.defaultSP;
import static com.stupidtree.hita.HITAApplication.isDataAvailable;
import static com.stupidtree.hita.HITAApplication.mainTimeTable;
import static com.stupidtree.hita.HITAApplication.now;
import static com.stupidtree.hita.adapter.NaviPageAdapter.integerToString;
import static com.stupidtree.hita.fragments.FragmentNavi.ORDER_NAME;
import static com.stupidtree.hita.fragments.FragmentTimeLine.showEventDialog;

public class TaskCardListAdapter extends RecyclerView.Adapter<TaskCardListAdapter.BaseHolder> {
    public static final int TYPE_TASK = 8;
    public static final int TYPE_DDL = 765;
    public static final int TYPE_EXAM = 902;
    List<Integer> mBeans;
    LayoutInflater mInflater;
    BaseActivity mContext;


    public TaskCardListAdapter(BaseActivity c, List<Integer> res) {
        mInflater = LayoutInflater.from(c);
        mBeans = res;
        mContext = c;
    }
    public void onMove(int fromPosition, int toPosition) {
        //对原数据进行移动
        Collections.swap(mBeans, fromPosition, toPosition);
        //通知数据移动
        notifyItemMoved(fromPosition, toPosition);
        saveOrders();
    }
    void saveOrders() {
        SharedPreferences.Editor editor = defaultSP.edit();
        editor.putString("task_page_order", integerToString(mBeans));
        editor.apply();
    }
    public static class mCallBack extends ItemTouchHelper.Callback {
        TaskCardListAdapter mAdapter;

        public mCallBack(TaskCardListAdapter mAdapter) {
            this.mAdapter = mAdapter;
        }

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            //首先回调的方法,返回int表示是否监听该方向
            int dragFlag = ItemTouchHelper.DOWN | ItemTouchHelper.UP;//拖拽
            int swipeFlag = 0;//侧滑删除
            return makeMovementFlags(dragFlag, swipeFlag);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            if (mAdapter != null) {
                mAdapter.onMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            }

            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }

        @Override
        public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            viewHolder.itemView.setAlpha(1.0f);
            try {
                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
            if (actionState != 0) {
                viewHolder.itemView.setAlpha(0.92f);
            }
            super.onSelectedChanged(viewHolder, actionState);
        }

    }


    @NonNull
    @Override
    public BaseHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        switch (viewType) {
            case TYPE_TASK:
                v = mInflater.inflate(R.layout.dynamic_task_card_task, parent, false);
                return new ViewHolder_Task(v);
            case TYPE_DDL:
                v = mInflater.inflate(R.layout.dynamic_task_card_ddl, parent, false);
                return new ViewHolder_DDL(v);
            case TYPE_EXAM:
                v = mInflater.inflate(R.layout.dynamic_task_card_exam, parent, false);
                return new ViewHolder_Exams(v);

        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        return mBeans.get(position);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseHolder holder, int position) {
        if (holder instanceof ViewHolder_Task) {
            final ViewHolder_Task vht = (ViewHolder_Task) holder;
            vht.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(mContext, ActivityTasks.class);
                    mContext.startActivity(i);
                }
            });
            vht.add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isDataAvailable())
                        new FragmentAddTask().show(mContext.getSupportFragmentManager(), "fat");
                    else Snackbar.make(view, "请先导入课表！", Snackbar.LENGTH_SHORT).show();
                }
            });
            vht.listAdapter.setmOnFinishClickListener(new TaskListAdapterMini.OnFinishClickListener() {
                @Override
                public boolean OnClick(View v, Task t, int position) {
                    new finishTask(vht,v,position).executeOnExecutor(HITAApplication.TPE);
                    return true;
                }

            });
            vht.listAdapter.setmOnItemLongClickListener(new TaskListAdapterMini.OnItemLongClickListener() {
                @Override
                public boolean OnClick(View v, int position) {
                    if((!org.apache.http.util.TextUtils.isEmpty(vht.listRes.get(position).getTag()))&&vht.listRes.get(position).getTag().contains(":::")) return false;
                    ExplosionField ef = ExplosionField.attach2Window(mContext);
                    ef.explode(v);
                    new deleteTask(vht,position).executeOnExecutor(HITAApplication.TPE);
                    return true;
                }
            });
            new RefreshTaskTask(vht).executeOnExecutor(TPE);
        } else if (holder instanceof ViewHolder_DDL) {
            ViewHolder_DDL vht = (ViewHolder_DDL) holder;
            new RefreshDDLTask(vht).executeOnExecutor(TPE);
            vht.add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isDataAvailable())
                        new FragmentAddEvent().showFor(mContext.getSupportFragmentManager(), 1);
                    else Snackbar.make(view, "请先导入课表！", Snackbar.LENGTH_SHORT).show();
                }
            });

        } else if (holder instanceof ViewHolder_Exams) {
            ViewHolder_Exams vht = (ViewHolder_Exams) holder;
            vht.add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isDataAvailable())
                        new FragmentAddEvent().showFor(mContext.getSupportFragmentManager(), 2);
                    else Snackbar.make(view, "请先导入课表！", Snackbar.LENGTH_SHORT).show();
                }
            });
            new RefreshExamTask(vht).executeOnExecutor(TPE);
        }
    }

    @Override
    public int getItemCount() {
        return mBeans.size();
    }

    class BaseHolder extends RecyclerView.ViewHolder {
        CardView card;
        ImageView empty;
        RecyclerView list;

        public BaseHolder(@NonNull View itemView) {
            super(itemView);
            empty = itemView.findViewById(R.id.empty);
            list = itemView.findViewById(R.id.list);
            card = itemView.findViewById(R.id.card);
        }
    }


    class ViewHolder_Task extends BaseHolder {
        RecyclerView list;
        ArrayList<Task> listRes;
        TaskListAdapterMini listAdapter;
        ImageView add;
        TextView title;

        public ViewHolder_Task(@NonNull View itemView) {
            super(itemView);
            list = itemView.findViewById(R.id.list);
            listRes = new ArrayList<>();
            listAdapter = new TaskListAdapterMini(mContext, listRes);
            list.setAdapter(listAdapter);
            list.setLayoutManager(new WrapContentLinearLayoutManager(mContext));
            add = itemView.findViewById(R.id.add);
            title = itemView.findViewById(R.id.title);
        }
    }

    class ViewHolder_Exams extends BaseHolder {
        RecyclerView list;
        ArrayList<EventItem> listRes;
        ExamItemAdapter listAdapter;
        ImageView add;
        TextView title;

        public ViewHolder_Exams(@NonNull View itemView) {
            super(itemView);
            list = itemView.findViewById(R.id.list);
            listRes = new ArrayList<>();
            listAdapter = new ExamItemAdapter(listRes);
            list.setAdapter(listAdapter);
            list.setLayoutManager(new WrapContentLinearLayoutManager(mContext));

            add = itemView.findViewById(R.id.add);
            title = itemView.findViewById(R.id.title);
        }
    }

    class ViewHolder_DDL extends BaseHolder {
        RecyclerView list;
        TextView title;
        ImageView add;
        ArrayList<EventItem> listRes;
        DDLItemAdapter listAdapter;

        public ViewHolder_DDL(@NonNull View itemView) {
            super(itemView);
            list = itemView.findViewById(R.id.list);
            title = itemView.findViewById(R.id.title);
            add = itemView.findViewById(R.id.add);
            listRes = new ArrayList<>();
            listAdapter = new DDLItemAdapter(listRes);
            list.setAdapter(listAdapter);
            list.setLayoutManager(new WrapContentLinearLayoutManager(mContext));
        }
    }


    class RefreshTaskTask extends AsyncTask {
        ViewHolder_Task vht;

        public RefreshTaskTask(ViewHolder_Task vht) {
            this.vht = vht;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            vht.listRes.clear();
            vht.listRes.addAll(mainTimeTable.getUnfinishedTasks());
            //Log.e("tasks", String.valueOf(vht.listRes));
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if(vht.listRes.size()>0){
                vht.empty.setVisibility(View.GONE);
                vht.list.setVisibility(View.VISIBLE);
                vht.title.setText("共有" + vht.listRes.size() + "个待办任务");
                vht.listAdapter.notifyDataSetChanged();
            }else{
                vht.empty.setVisibility(View.VISIBLE);
                vht.list.setVisibility(View.GONE);
                vht.title.setText("没有正在进行的任务");
            }

        }
    }

    class RefreshDDLTask extends AsyncTask {
        ViewHolder_DDL vht;

        public RefreshDDLTask(ViewHolder_DDL vht) {
            this.vht = vht;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            vht.listRes.clear();
            //Calendar c = Calendar.getInstance();
            //c.setTimeInMillis(now.getTimeInMillis());
            //c.add(Calendar.DATE,7);
            List<EventItem> result = mainTimeTable.getUnfinishedEvent(now, TimeTable.TIMETABLE_EVENT_TYPE_DEADLINE);


            if (result != null && result.size() > 0) vht.listRes.addAll(result);
            Collections.sort(vht.listRes);
            //Log.e("tasks", String.valueOf(vht.listRes));
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if(vht.listRes.size()>0){
                vht.empty.setVisibility(View.GONE);
                vht.list.setVisibility(View.VISIBLE);
                vht.title.setText("共有" + vht.listRes.size() + "个DDL");
                vht.listAdapter.notifyDataSetChanged();
            }else{
                vht.empty.setVisibility(View.VISIBLE);
                vht.list.setVisibility(View.GONE);
                vht.title.setText("现在没有DDL");
            }
        }
    }

    class RefreshExamTask extends AsyncTask {
        ViewHolder_Exams vht;

        public RefreshExamTask(ViewHolder_Exams vht) {
            this.vht = vht;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            vht.listRes.clear();
            //Calendar c = Calendar.getInstance();
            //c.setTimeInMillis(now.getTimeInMillis());
            //c.add(Calendar.DATE,7);
            List<EventItem> result = mainTimeTable.getUnfinishedEvent(now, TimeTable.TIMETABLE_EVENT_TYPE_EXAM);

            if (result != null && result.size() > 0) vht.listRes.addAll(result);
            Collections.sort(vht.listRes);
            //Log.e("tasks", String.valueOf(vht.listRes));
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            vht.listAdapter.notifyDataSetChanged();
            if(vht.listRes.size()>0){
                vht.empty.setVisibility(View.GONE);
                vht.list.setVisibility(View.VISIBLE);
                long minutes = vht.listRes.get(0).getInWhatTimeWillItHappen(mainTimeTable.core, now);
                Log.e("minutes", String.valueOf(minutes));
                int weeks = (int) (minutes / 10080);
                minutes %= 10080;
                int days = (int) (minutes / 1440);
                minutes %= 1440;
                int hours = (int) (minutes / 60);
                minutes %= 60;
                String weekS = weeks > 0 ? weeks + "周" : "";
                String dayS = days > 0 ? days + "天" : "";
                String hourS, minuteS;
                if (!TextUtils.isEmpty(weekS)) {
                    hourS = minuteS = "";
                } else if (!TextUtils.isEmpty(dayS)) {
                    minuteS = "";
                    hourS = hours > 0 ? hours + "时" : "";
                } else {
                    hourS = hours > 0 ? hours + "时" : "";
                    minuteS = minutes > 0 ? minutes + "分" : "";
                }
                vht.title.setText("考试倒计时"+weekS + dayS + hourS + minuteS);
                vht.listAdapter.notifyDataSetChanged();
            }else{
                vht.empty.setVisibility(View.VISIBLE);
                vht.list.setVisibility(View.GONE);
                vht.title.setText("没有考试");
            }



        }
    }

    class DDLItemAdapter extends RecyclerView.Adapter<DDLItemAdapter.xHolder> {

        List<EventItem> mBeans;

        public DDLItemAdapter(List<EventItem> mBeans) {
            this.mBeans = mBeans;
        }

        @NonNull
        @Override
        public xHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new xHolder(mInflater.inflate(R.layout.dynamic_task_card_ddl_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull xHolder holder, final int position) {
            EventItem ddl = mBeans.get(position);
            holder.time_date.setText(ddl.week + "周" + TextTools.words_time_DOW[ddl.DOW - 1]);
            if(ddl.isWholeDay) holder.time_time.setText("全天");
            else holder.time_time.setText(ddl.startTime.tellTime());
            holder.title.setText(ddl.mainName);
            now.setTimeInMillis(System.currentTimeMillis());

            long minutes = ddl.getInWhatTimeWillItHappen(mainTimeTable.core, now);
            Log.e("minutes", String.valueOf(minutes));
            int weeks = (int) (minutes / 10080);
            minutes %= 10080;
            int days = (int) (minutes / 1440);
            minutes %= 1440;
            int hours = (int) (minutes / 60);
            minutes %= 60;
            String weekS = weeks > 0 ? weeks + "周" : "";
            String dayS = days > 0 ? days + "天" : "";
            String hourS, minuteS;
            if (!TextUtils.isEmpty(weekS)) {
                hourS = minuteS = "";
            } else if (!TextUtils.isEmpty(dayS)) {
                minuteS = "";
                hourS = hours > 0 ? hours + "时" : "";
            } else {
                hourS = hours > 0 ? hours + "时" : "";
                minuteS = minutes > 0 ? minutes + "分" : "";
            }

            holder.remain.setText(weekS + dayS + hourS + minuteS);
            holder.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showEventDialog(mContext, mBeans.get(position), null, null);
                }
            });
        }


        @Override
        public int getItemCount() {
            return mBeans.size();
        }

        class xHolder extends RecyclerView.ViewHolder {
            TextView title, time_date, time_time, remain;
            ViewGroup item;


            public xHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.title);
                item = itemView.findViewById(R.id.item);
                time_date = itemView.findViewById(R.id.time_date);
                time_time = itemView.findViewById(R.id.time_time);
                remain = itemView.findViewById(R.id.time_remain);
            }
        }
    }

    class ExamItemAdapter extends RecyclerView.Adapter<ExamItemAdapter.yHolder> {

        List<EventItem> mBeans;

        public ExamItemAdapter(List<EventItem> mBeans) {
            this.mBeans = mBeans;
        }

        @NonNull
        @Override
        public yHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new yHolder(mInflater.inflate(R.layout.dynamic_task_card_exam_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull yHolder holder, final int position) {
            EventItem ddl = mBeans.get(position);
            holder.time.setText(ddl.week + "周" + TextTools.words_time_DOW[ddl.DOW - 1] + " " + ddl.startTime.tellTime());
            holder.title.setText(ddl.mainName);
            now.setTimeInMillis(System.currentTimeMillis());

            long minutes = ddl.getInWhatTimeWillItHappen(mainTimeTable.core, now);
            Log.e("minutes", String.valueOf(minutes));
            int weeks = (int) (minutes / 10080);
            minutes %= 10080;
            int days = (int) (minutes / 1440);
            minutes %= 1440;
            int hours = (int) (minutes / 60);
            minutes %= 60;
            String weekS = weeks > 0 ? weeks + "周" : "";
            String dayS = days > 0 ? days + "天" : "";
            String hourS, minuteS;
            if (!TextUtils.isEmpty(weekS)) {
                hourS = minuteS = "";
            } else if (!TextUtils.isEmpty(dayS)) {
                minuteS = "";
                hourS = hours > 0 ? hours + "时" : "";
            } else {
                hourS = hours > 0 ? hours + "时" : "";
                minuteS = minutes > 0 ? minutes + "分" : "";
            }

            holder.remain.setText(weekS + dayS + hourS + minuteS);
            holder.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showEventDialog(mContext, mBeans.get(position), null, null);
                }
            });
        }


        @Override
        public int getItemCount() {
            return mBeans.size();
        }

        class yHolder extends RecyclerView.ViewHolder {
            TextView title, time, remain;
            ViewGroup item;


            public yHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.title);
                item = itemView.findViewById(R.id.item);
                time = itemView.findViewById(R.id.time);
                remain = itemView.findViewById(R.id.time_remain);
            }
        }
    }
    class deleteTask extends AsyncTask {
        int position;
        RecyclerView list;
        TaskListAdapterMini listAdapter;
        List<Task> listRes;
        ViewHolder_Task vht;

        deleteTask(ViewHolder_Task vht,int position) {
            this.position = position;
            this.vht = vht;
            listAdapter = vht.listAdapter;
            this.listRes =vht.listRes;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            if (mainTimeTable.deleteTask(listRes.get(position))) {
                listRes.remove(position);
                return true;
            } else return false;
        }

        void refreshTitle(){
            if(vht.listRes.size()>0){
                vht.empty.setVisibility(View.GONE);
                vht.list.setVisibility(View.VISIBLE);
                vht.title.setText("共有" + vht.listRes.size() + "个待办任务");
            }else{
                vht.empty.setVisibility(View.VISIBLE);
                vht.list.setVisibility(View.GONE);
                vht.title.setText("没有正在进行的任务");
            }
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            listAdapter.notifyItemRemoved(position);
            if (position != listRes.size()) { // 如果移除的是最后一个，忽略
                listAdapter.notifyItemRangeChanged(position, listRes.size() - position);
            }
            if ((Boolean) o) {
               refreshTitle();
            } else Toast.makeText(HContext, "删除失败!", Toast.LENGTH_SHORT).show();
            ActivityMain.saveData();
            Intent mes = new Intent("COM.STUPIDTREE.HITA.TIMELINE_REFRESH");
            mes.putExtra("from", "task");
            mContext.sendBroadcast(mes);
            // if(ftl!=null&&ftl.hasInit) ftl.Refresh(FragmentTimeLine.TL_REFRESH_FROM_UNHIDE);
        }
    }

    class finishTask extends AsyncTask {
        int position;
        List<Task> listRes;
        TaskListAdapterMini listAdapter;
        View clickView;
        ViewHolder_Task vht;

        public finishTask(ViewHolder_Task vht,View click,int position) {
            this.position = position;
            this.vht = vht;
            this.listRes = vht.listRes;
            this.listAdapter = vht.listAdapter;
            this.clickView = click;
        }

        void refreshTitle(){
            if(vht.listRes.size()>0){
                vht.empty.setVisibility(View.GONE);
                vht.list.setVisibility(View.VISIBLE);
                vht.title.setText("共有" + vht.listRes.size() + "个待办任务");
            }else{
                vht.empty.setVisibility(View.VISIBLE);
                vht.list.setVisibility(View.GONE);
                vht.title.setText("没有正在进行的任务");
            }
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            if(listRes.get(position).isHas_length()&&listRes.get(position).getProgress()<100){
                return "dialog";
            }else{
                if (mainTimeTable.setFinishTask(listRes.get(position),true)) {
                    listRes.remove(position);
                    return true;
                } else return false;
            }


        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if(o instanceof String && o.equals("dialog")){
                AlertDialog ad = new AlertDialog.Builder(mContext).setMessage("任务未完成，请添加对应处理事件！").setTitle("任务进度尚未完成").create();
                ad.show();
            }else{
                //ExplosionField ef = ExplosionField.attach2Window(mContext);
                //ef.explode(clickView);
                listAdapter.notifyItemRemoved(position);
                if (position != listRes.size()) { // 如果移除的是最后一个，忽略
                    listAdapter.notifyItemRangeChanged(position, listRes.size() - position);
                }
                if ((Boolean) o) {
                    refreshTitle();
                } else Toast.makeText(HContext, "操作失败!", Toast.LENGTH_SHORT).show();
                ActivityMain.saveData();
                Intent mes = new Intent("COM.STUPIDTREE.HITA.TIMELINE_REFRESH");
                mes.putExtra("from", "task");
                mContext.sendBroadcast(mes);
                // if(ftl!=null&&ftl.hasInit) ftl.Refresh(FragmentTimeLine.TL_REFRESH_FROM_UNHIDE);
            }

        }
    }
}
