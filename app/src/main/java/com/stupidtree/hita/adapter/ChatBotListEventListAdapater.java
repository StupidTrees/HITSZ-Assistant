package com.stupidtree.hita.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stupidtree.hita.R;
import com.stupidtree.hita.core.TimeTable;
import com.stupidtree.hita.core.timetable.EventItem;

import java.util.List;

import static com.stupidtree.hita.hita.TextTools.words_time_DOW;


public class ChatBotListEventListAdapater extends RecyclerView.Adapter<ChatBotListEventListAdapater.mHolder> {
    List<EventItem> mList;
    LayoutInflater mInflater;
    private OnItemClickLitener mOnItemClickLitener;
    Context mContext;
    ChatBotListEventListAdapater(List<EventItem> res, Context context){
        mInflater = LayoutInflater.from(context);
        mList = res;
        mContext = context;
    }
    //设置回调接口
    public interface OnItemClickLitener{
        void onItemClick(View view, int position);
    }

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener){
        this.mOnItemClickLitener = mOnItemClickLitener;
    }
    @NonNull
    @Override
    public mHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = mInflater.inflate(R.layout.dynamic_chatbot_courselist_item,viewGroup,false);
        return new mHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull mHolder mHolder, final int i) {
        mHolder.text.setText(words_time_DOW[mList.get(i).DOW-1]+" "+mList.get(i).mainName);
        //通过为条目设置点击事件触发回调
        if (mOnItemClickLitener != null) {
            mHolder.itemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemClickLitener.onItemClick(view, i);
                }
            });
        }
        switch(mList.get(i).eventType){
            case TimeTable.TIMETABLE_EVENT_TYPE_COURSE:mHolder.icon.setImageDrawable(mContext.getDrawable(R.drawable.ic_chatbot_course));break;
            case TimeTable.TIMETABLE_EVENT_TYPE_ARRANGEMENT:mHolder.icon.setImageDrawable(mContext.getDrawable(R.drawable.ic_chatbot_arrangement));break;
            case TimeTable.TIMETABLE_EVENT_TYPE_DEADLINE:mHolder.icon.setImageDrawable(mContext.getDrawable(R.drawable.ic_chatbot_deadline));break;
            case TimeTable.TIMETABLE_EVENT_TYPE_REMIND:mHolder.icon.setImageDrawable(mContext.getDrawable(R.drawable.ic_chatbot_remind));break;
            case TimeTable.TIMETABLE_EVENT_TYPE_EXAM:mHolder.icon.setImageDrawable(mContext.getDrawable(R.drawable.ic_chatbot_exam));break;

        }
    }


    @Override
    public int getItemCount() {
        return mList.size();
    }

    class mHolder extends RecyclerView.ViewHolder{
        TextView text;
        LinearLayout itemLayout;
        ImageView icon;
        public mHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.style_chatbot_listitem);
            itemLayout = itemView.findViewById(R.id.recy_item_layout);
            icon = itemView.findViewById(R.id.chatbot_courselist_icon);
        }
    }

}
