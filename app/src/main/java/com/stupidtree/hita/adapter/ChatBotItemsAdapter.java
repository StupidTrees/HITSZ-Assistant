package com.stupidtree.hita.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.stupidtree.hita.R;
import com.stupidtree.hita.online.Teacher;
import com.stupidtree.hita.timetable.packable.EventItem;
import com.stupidtree.hita.timetable.packable.Subject;
import com.stupidtree.hita.timetable.packable.Task;
import com.stupidtree.hita.util.ActivityUtils;
import com.stupidtree.hita.util.EventsUtils;
import com.stupidtree.hita.views.TaskDialog;

import java.util.HashMap;
import java.util.List;



public class ChatBotItemsAdapter extends RecyclerView.Adapter<ChatBotItemsAdapter.mHolder> {
    private List<HashMap> mList;
    private List mListRes;
    private LayoutInflater mInflater;
    private Context mContext;
    ChatBotItemsAdapter(List<HashMap> res,List listRes, Context context){
        mInflater = LayoutInflater.from(context);
        this.mListRes = listRes;
        mList = res;
        mContext = context;
    }

    @NonNull
    @Override
    public mHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = mInflater.inflate(R.layout.dynamic_chatbot_list_item,viewGroup,false);
        return new mHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull mHolder mHolder, final int i) {
        mHolder.text.setText(mList.get(i).get("title").toString());
        if(mList.get(i).containsKey("icon")){
            mHolder.icon.setVisibility(View.VISIBLE);
            mHolder.icon.setImageResource((Integer) mList.get(i).get("icon"));
        }else mHolder.icon.setVisibility(View.GONE);

        mHolder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String type = (String) mList.get(0).get("type");
                    if(type.equals("event")) {
                        EventsUtils.showEventItem(mContext, (EventItem) mListRes.get(i));
                    }else if(type.equals("task")){
                        new TaskDialog(mContext, (Task) mListRes.get(i)).show();
                    }else if(type.equals("teacher")){
                        ActivityUtils.startTeacherActivity(mContext, (Teacher) mListRes.get(i));
                    }else if(type.equals("subject")){
                        ActivityUtils.startSubjectActivity_name(mContext,((Subject)mListRes.get(i)).getName());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
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
            icon = itemView.findViewById(R.id.icon);
        }
    }

}
