package com.stupidtree.hita.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stupidtree.hita.R;
import com.stupidtree.hita.core.timetable.Task;

import java.util.List;


public class ChatBotListTaskListAdapater extends RecyclerView.Adapter<ChatBotListTaskListAdapater.mHolder> {
    List<Task> mList;
    LayoutInflater mInflater;
    private OnItemClickLitener mOnItemClickLitener;
    Context mContext;
    ChatBotListTaskListAdapater(List<Task> res, Context context){
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
        View v = mInflater.inflate(R.layout.dynamic_chatbot_tasklist_item,viewGroup,false);
        return new mHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull mHolder mHolder, final int i) {
        mHolder.text.setText(mList.get(i).name);
        if (mOnItemClickLitener != null) {
            mHolder.itemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemClickLitener.onItemClick(view, i);
                }
            });
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
            text = itemView.findViewById(R.id.tasklist_name);
            itemLayout = itemView.findViewById(R.id.recy_item_layout);
            icon = itemView.findViewById(R.id.chatbot_courselist_icon);
        }
    }

}
