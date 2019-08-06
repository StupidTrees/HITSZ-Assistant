package com.stupidtree.hita.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.stupidtree.hita.R;
import com.stupidtree.hita.core.TimeTable;
import com.stupidtree.hita.core.timetable.EventItem;

import java.util.List;

public class TimeLineWholedayAdapter extends RecyclerView.Adapter<TimeLineWholedayAdapter.tlwdViewHolder> {

    List<EventItem> mBeans;
    LayoutInflater mInflater;
    OnItemClickListener onItemClickListener;
    OnItemLongClickListener onItemLongClickListener;

    public interface OnItemClickListener{
        void OnClick(View v,int position);
    }

    public interface  OnItemLongClickListener{
        boolean OnLongClick(View v,int position);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public TimeLineWholedayAdapter(Context x, List<EventItem> res){
        mInflater = LayoutInflater.from(x);
        mBeans = res;
    }
    @NonNull
    @Override
    public tlwdViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = mInflater.inflate(R.layout.dynamic_timeline_wholeday,viewGroup,false);
        return new tlwdViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull tlwdViewHolder tlwdViewHolder, final int i) {
        tlwdViewHolder.name.setText(mBeans.get(i).mainName);
        int iconId = R.drawable.ic_timeline_deadline;
        switch (mBeans.get(i).eventType){
            case TimeTable.TIMETABLE_EVENT_TYPE_DEADLINE: iconId = R.drawable.ic_timeline_deadline; break;
            case TimeTable.TIMETABLE_EVENT_TYPE_REMIND: iconId = R.drawable.ic_timeline_remind; break;
            case TimeTable.TIMETABLE_EVENT_TYPE_ARRANGEMENT:iconId = R.drawable.ic_timeline_wholeday_arrange;break;
        }
        tlwdViewHolder.icon.setImageResource(iconId);
        if(onItemClickListener!=null){
            tlwdViewHolder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.OnClick(v,i);
                }
            });
        }
        if(onItemLongClickListener!=null){
            tlwdViewHolder.card.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return onItemLongClickListener.OnLongClick(v,i);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return mBeans.size();
    }

    class tlwdViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        ImageView icon;
        CardView card;

        public tlwdViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tl_wholeday_name);
            icon = itemView.findViewById(R.id.tl_wholeday_icon);
            card = itemView.findViewById(R.id.tl_wholeday_card);
        }
    }
}
