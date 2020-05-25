package com.stupidtree.hita.adapter;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.BaseActivity;
import com.stupidtree.hita.fragments.popup.FragmentAddEvent;
import com.stupidtree.hita.fragments.popup.FragmentAddTask;
import com.stupidtree.hita.timetable.TimetableCore;
import com.stupidtree.hita.timetable.packable.EventItem;
import com.stupidtree.hita.util.ActivityUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.TPE;
import static com.stupidtree.hita.HITAApplication.defaultSP;
import static com.stupidtree.hita.adapter.NavigationListAdapter.integerToString;

public class TaskCardListAdapter extends RecyclerView.Adapter<TaskCardListAdapter.BaseHolder> {
    public static final int TYPE_TASK = 8;
    public static final int TYPE_DDL = 765;
    public static final int TYPE_EXAM = 902;
    private List<Integer> mBeans;
    private LayoutInflater mInflater;
    private BaseActivity mContext;


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

    @NonNull
    @Override
    public BaseHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = null;
        switch (viewType) {
            case TYPE_TASK:
                v = mInflater.inflate(R.layout.dynamic_timeline_card_task, parent, false);
                break;
            case TYPE_DDL:
                v = mInflater.inflate(R.layout.dynamic_timeline_card_ddl, parent, false);
                break;
            case TYPE_EXAM:
                v = mInflater.inflate(R.layout.dynamic_timeline_card_ddl, parent, false);
                break;
        }
        return new BaseHolder(v, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseHolder holder, int position) {
        if (holder.type == TYPE_TASK) {
            holder.add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (TimetableCore.getInstance(HContext).isDataAvailable())
                        new FragmentAddTask().show(mContext.getSupportFragmentManager(), "fat");
                    else
                        Snackbar.make(view, mContext.getString(R.string.notif_importdatafirst), Snackbar.LENGTH_SHORT).show();
                }
            });
            new RefreshTaskTask(holder).executeOnExecutor(TPE);

        } else if (holder.type == TYPE_DDL) {
            new RefreshDDLTask(holder).executeOnExecutor(TPE);
            holder.add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (TimetableCore.getInstance(HContext).isDataAvailable())
                        new FragmentAddEvent().setInitialType("ddl").show(mContext.getSupportFragmentManager(), "fae");
                    else
                        Snackbar.make(view, mContext.getString(R.string.notif_importdatafirst), Snackbar.LENGTH_SHORT).show();
                }
            });

        } else if (holder.type == TYPE_EXAM) {
            holder.add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (TimetableCore.getInstance(HContext).isDataAvailable())
                        new FragmentAddEvent().setInitialType("exam").show(mContext.getSupportFragmentManager(), "fae");
                    else
                        Snackbar.make(view, mContext.getString(R.string.notif_importdatafirst), Snackbar.LENGTH_SHORT).show();
                }
            });
            new RefreshExamTask(holder).executeOnExecutor(TPE);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mBeans.get(position);
    }

    public static class mCallBack extends ItemTouchHelper.Callback {
        TaskCardListAdapter mAdapter;

        public mCallBack(TaskCardListAdapter mAdapter) {
            this.mAdapter = mAdapter;
        }

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            //首先回调的方法,返回int表示是否监听该方向
            int dragFlag = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;//拖拽
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

    @Override
    public int getItemCount() {
        return mBeans.size();
    }

    class BaseHolder extends RecyclerView.ViewHolder {
        CardView card;
        int num;
        int type;
        ImageView empty;
        View add;
        ViewGroup busy;
        TextView number, title;
        TextView next_primary, next_sign, next_secondary;

        public BaseHolder(@NonNull View itemView, final int type) {
            super(itemView);
            this.type = type;
            empty = itemView.findViewById(R.id.empty);
            number = itemView.findViewById(R.id.num);
            title = itemView.findViewById(R.id.title);
            add = itemView.findViewById(R.id.add);
            card = itemView.findViewById(R.id.card);
            busy = itemView.findViewById(R.id.busy);
            next_primary = itemView.findViewById(R.id.next_remain_primary);
            next_secondary = itemView.findViewById(R.id.next_remain_secondary);
            next_sign = itemView.findViewById(R.id.next_remain_sign);
            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (type == TYPE_TASK) {
                        ActivityUtils.startTasksActivity(mContext);
                    } else if (type == TYPE_EXAM) {
                        ActivityUtils.startExamCDActivity(mContext);
                    } else if (type == TYPE_DDL) {
                        ActivityUtils.startDDLManagerActivity(mContext);
                    }
                }
            });
        }
    }


    class RefreshTaskTask extends AsyncTask {
        BaseHolder vht;

        public RefreshTaskTask(BaseHolder vht) {
            this.vht = vht;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            TimetableCore tc = TimetableCore.getInstance(HContext);
            if (!tc.isDataAvailable()) {
                vht.num = 0;
                return null;
            }
            try {
                vht.num = tc.getUnfinishedTasks().size();
            } catch (Exception e) {
                e.printStackTrace();
                vht.num = 0;
            }
            //Log.e("tasks", String.valueOf(vht.listRes));
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (vht.num > 0) {
                vht.empty.setVisibility(View.GONE);
                vht.number.setVisibility(View.VISIBLE);
                vht.number.setText("" + vht.num);
                vht.title.setText(R.string.task_card_title_tasknum);
            } else {
                vht.number.setVisibility(View.GONE);
                vht.empty.setVisibility(View.VISIBLE);
                vht.title.setText(mContext.getString(R.string.task_card_title_notask));
            }

        }
    }

    class RefreshDDLTask extends AsyncTask {
        BaseHolder vht;
        EventItem nextToDo = null;

        RefreshDDLTask(BaseHolder vht) {
            this.vht = vht;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            TimetableCore tc = TimetableCore.getInstance(HContext);
            if (!tc.isDataAvailable()) {
                vht.num = 0;
                nextToDo = null;
                return null;
            }
            List<EventItem> result = tc.getUnfinishedEvent(TimetableCore.getNow(), TimetableCore.DDL);
            Collections.sort(result, new Comparator<EventItem>() {
                @Override
                public int compare(EventItem o1, EventItem o2) {
                    return o1.compareTo(o2);
                }
            });
            if (result.size() > 0) {
                nextToDo = result.get(0);
                vht.num = result.size();
            } else {
                vht.num = 0;
                nextToDo = null;
            }
            //Log.e("tasks", String.valueOf(vht.listRes));
            return null;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (vht.num > 0 && nextToDo != null) {
                vht.empty.setVisibility(View.GONE);
                vht.busy.setVisibility(View.VISIBLE);
                vht.number.setVisibility(View.VISIBLE);
                vht.number.setText("" + vht.num);
                vht.title.setText(R.string.task_card_title_ddlnum);
                long minutes = nextToDo.getInWhatTimeWillItHappen(TimetableCore.getInstance(HContext).getCurrentCurriculum(), TimetableCore.getNow());
                int weeks = (int) (minutes / 10080);
                minutes %= 10080;
                int days = (int) (minutes / 1440);
                minutes %= 1440;
                int hours = (int) (minutes / 60);
                minutes %= 60;
                String primary, second, sign;
                if (weeks > 9) {
                    primary = "9";
                    sign = ">";
                    second = mContext.getString(R.string.name_week_short);
                } else if (weeks > 0) {
                    primary = (weeks + 1) + "";
                    sign = "<";
                    second = mContext.getString(R.string.name_week_short);
                } else if (days > 0) {
                    primary = (days + 1) + "";
                    sign = "<";
                    second = mContext.getString(R.string.name_day_short);
                } else if (nextToDo.isWholeDay()) {
                    primary = 1 + "";
                    sign = "<";
                    second = mContext.getString(R.string.name_day_short);
                } else if (hours >= 10) {
                    primary = 1 + "";
                    sign = "<";
                    second = mContext.getString(R.string.name_day_short);
                } else if (hours > 0) {
                    primary = (hours + 1) + "";
                    sign = "<";
                    second = mContext.getString(R.string.name_hour_short);
                } else if (minutes >= 10) {
                    primary = 1 + "";
                    sign = "<";
                    second = mContext.getString(R.string.name_hour_short);
                } else if (minutes > 0) {
                    primary = (minutes + 1) + "";
                    sign = "<";
                    second = mContext.getString(R.string.name_minute_short);
                } else {
                    primary = "!";
                    sign = "";
                    second = "";

                }
                vht.next_sign.setText(sign);
                vht.next_primary.setText(primary);
                vht.next_secondary.setText(second);

            } else {
                vht.empty.setVisibility(View.VISIBLE);
                vht.number.setVisibility(View.GONE);
                vht.title.setText(mContext.getString(R.string.task_card_title_noddl));
                vht.busy.setVisibility(View.GONE);
            }
        }
    }

    class RefreshExamTask extends AsyncTask {
        BaseHolder vht;
        EventItem nextToDo = null;

        RefreshExamTask(BaseHolder vht) {
            this.vht = vht;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            TimetableCore tc = TimetableCore.getInstance(HContext);
            if (!tc.isDataAvailable()) {
                vht.num = 0;
                nextToDo = null;
                return null;
            }
            List<EventItem> result = tc.getUnfinishedEvent(TimetableCore.getNow(), TimetableCore.EXAM);
            Collections.sort(result, new Comparator<EventItem>() {
                @Override
                public int compare(EventItem o1, EventItem o2) {
                    return o1.compareTo(o2);
                }
            });
            if (result != null && result.size() > 0) {
                vht.num = result.size();
                nextToDo = result.get(0);
            } else vht.num = 0;
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (vht.num > 0 && nextToDo != null) {
                vht.empty.setVisibility(View.GONE);
                vht.number.setVisibility(View.VISIBLE);
                vht.number.setText("" + vht.num);
                vht.busy.setVisibility(View.VISIBLE);
                vht.title.setText(R.string.ade_exam);
                long minutes = nextToDo.getInWhatTimeWillItHappen(TimetableCore.getInstance(HContext).getCurrentCurriculum(), TimetableCore.getNow());
                int weeks = (int) (minutes / 10080);
                minutes %= 10080;
                int days = (int) (minutes / 1440);
                minutes %= 1440;
                int hours = (int) (minutes / 60);
                minutes %= 60;
                String primary, second, sign;
                if (weeks > 9) {
                    primary = "9";
                    sign = ">";
                    second = mContext.getString(R.string.name_week_short);
                } else if (weeks > 0) {
                    primary = (weeks + 1) + "";
                    sign = "<";
                    second = mContext.getString(R.string.name_week_short);
                } else if (days > 0) {
                    primary = (days + 1) + "";
                    sign = "<";
                    second = mContext.getString(R.string.name_day_short);
                } else if (nextToDo.isWholeDay()) {
                    primary = 1 + "";
                    sign = "<";
                    second = mContext.getString(R.string.name_day_short);
                } else if (hours >= 10) {
                    primary = 1 + "";
                    sign = "<";
                    second = mContext.getString(R.string.name_day_short);
                } else if (hours > 0) {
                    primary = (hours + 1) + "";
                    sign = "<";
                    second = mContext.getString(R.string.name_hour_short);
                } else if (minutes >= 10) {
                    primary = 1 + "";
                    sign = "<";
                    second = mContext.getString(R.string.name_hour_short);
                } else if (minutes > 0) {
                    primary = (minutes + 1) + "";
                    sign = "<";
                    second = mContext.getString(R.string.name_minute_short);
                } else {
                    primary = "!";
                    sign = "";
                    second = "";
                }
                vht.next_sign.setText(sign);
                vht.next_primary.setText(primary);
                vht.next_secondary.setText(second);
            } else {
                vht.number.setVisibility(View.GONE);
                vht.busy.setVisibility(View.GONE);
                vht.empty.setVisibility(View.VISIBLE);
                vht.title.setText(mContext.getString(R.string.exam_card_noexam));
            }


        }
    }




}
