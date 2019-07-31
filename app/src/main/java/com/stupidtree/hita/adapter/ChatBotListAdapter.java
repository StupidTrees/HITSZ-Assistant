package com.stupidtree.hita.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.stupidtree.hita.R;
import com.stupidtree.hita.ChatSec.ChatBotMessageItem;
import com.stupidtree.hita.diy.CornerTransform;

import java.util.List;

import static com.stupidtree.hita.adapter.IpNewsListAdapter.dip2px;
import static com.stupidtree.hita.fragments.FragmentTimeLine.showEventDialog;

public class ChatBotListAdapter extends RecyclerView.Adapter<ChatBotListAdapter.RecyclerHolder> {
    public static final int TYPE_RIGHT = -11;
    public static final int TYPE_LEFT = -10;
    private List<ChatBotMessageItem> mMsgList;
    private LayoutInflater mInflater;//布局装载器对象
    private Context mContext;
    OnUserAvatarClickListener mOnUserAvatarClickListener;

    public ChatBotListAdapter(Context context, List<ChatBotMessageItem> items) {
        super();
        mContext = context;
        mMsgList = items;
        mInflater = LayoutInflater.from(context);
    }

    public interface OnUserAvatarClickListener{
        void onClick(View v,int position);
    }

    public void setOnUserAvatarClickListener(OnUserAvatarClickListener mOnUserAvatarClickListener) {
        this.mOnUserAvatarClickListener = mOnUserAvatarClickListener;
    }

    @NonNull
    @Override
    public RecyclerHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int type) {
        View v;
        if(type == TYPE_LEFT) v = mInflater.inflate(R.layout.dynamic_chatbot_left,viewGroup,false);
        else v = mInflater.inflate(R.layout.dynamic_chatbot_right,viewGroup,false);
        return new RecyclerHolder(v,type);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerHolder recyclerHolder, final int i) {
        ChatBotMessageItem messageItem = mMsgList.get(i);
        recyclerHolder.textView.setText(messageItem.message);
        if(messageItem.getHint()!=null&&recyclerHolder.hint!=null){
            recyclerHolder.hint.setVisibility(View.VISIBLE);
            recyclerHolder.hint.setText(messageItem.getHint());
        }else if(recyclerHolder.hint!=null)recyclerHolder.hint.setVisibility(View.GONE);
        if(messageItem.courseList!=null) {
            recyclerHolder.resultRecy.setVisibility(View.VISIBLE);
            recyclerHolder.resultEventAdapter = new ChatBotListEventListAdapater(mMsgList.get(i).courseList, mContext);
            recyclerHolder.resultRecy.setNestedScrollingEnabled(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
            recyclerHolder.resultRecy.setLayoutManager(layoutManager);
            recyclerHolder.resultRecy.setAdapter(recyclerHolder.resultEventAdapter);
            recyclerHolder.resultEventAdapter.setOnItemClickLitener(new ChatBotListEventListAdapater.OnItemClickLitener() {
                @Override
                public void onItemClick(View view, int position) {
                    showEventDialog((Activity) mContext, mMsgList.get(i).courseList.get(position),view,null);
                }
            });
            recyclerHolder.resultEventAdapter.notifyDataSetChanged();

        } else if(messageItem.taskList!=null){
            recyclerHolder.resultRecy.setVisibility(View.VISIBLE);
            recyclerHolder.resultTaskAdapter = new ChatBotListTaskListAdapater(mMsgList.get(i).taskList, mContext);
            recyclerHolder.resultRecy.setNestedScrollingEnabled(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
            recyclerHolder.resultRecy.setLayoutManager(layoutManager);
            recyclerHolder.resultRecy.setAdapter(recyclerHolder.resultTaskAdapter);
            recyclerHolder.resultTaskAdapter.notifyDataSetChanged();
        }
        else if(recyclerHolder.type == TYPE_LEFT)recyclerHolder.resultRecy.setVisibility(View.GONE);
        if(recyclerHolder.asr_image!=null&&recyclerHolder.type==TYPE_LEFT&&mMsgList.get(i).getImageURI()!= null) {
            CornerTransform transformation;
            transformation = new CornerTransform(mContext, dip2px(mContext, 16));
            transformation.setExceptCorner(false, false, false, false);
            recyclerHolder.asr_image.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(mMsgList.get(i).getImageURI()).apply(RequestOptions.bitmapTransform(transformation)).into(recyclerHolder.asr_image);
        }else if(recyclerHolder.asr_image!=null)recyclerHolder.asr_image.setVisibility(View.GONE);
//        if(recyclerHolder.type==TYPE_RIGHT&&recyclerHolder.userAvatarImg!=null){
//            Glide.with(mContext)
//                    .load(CurrentUser.getAvatarUri())
//                    .signature(new ObjectKey(PreferenceManager.getDefaultSharedPreferences(HContext).getString("avatarGlideSignature", String.valueOf(System.currentTimeMillis()))))
//                    .centerCrop().
//                    apply(RequestOptions.bitmapTransform(new CircleCrop()))
//                    .into(recyclerHolder.userAvatarImg);
//        }
//        if(recyclerHolder.type==TYPE_RIGHT&&mOnUserAvatarClickListener!=null){
//            recyclerHolder.userAvatar.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mOnUserAvatarClickListener.onClick(v,i);
//                }
//            });
//        }
    }

    @Override
    public int getItemViewType(int position) {
        if(mMsgList.get(position).type == ChatBotMessageItem.MSG_TYPE_RGIHT) return TYPE_RIGHT;
        else return TYPE_LEFT;
    }


    @Override
    public int getItemCount() {
        return mMsgList.size();
    }


    public void addMessage(ChatBotMessageItem messageItem){
        mMsgList.add(messageItem);
        notifyItemInserted(mMsgList.size()-1);
    }

    public void deleteBefore(int position){
        for(int i=position-1;i>=0;i--) { //批量删除：倒着循环，防止索引错乱！！！！！！！！！
            mMsgList.remove(i);
            notifyItemRemoved(i);
            notifyItemRangeChanged(i,mMsgList.size());
        }

    }

    class RecyclerHolder extends RecyclerView.ViewHolder {
        TextView textView,hint;
        RecyclerView resultRecy;
        ChatBotListEventListAdapater resultEventAdapter;
        ChatBotListTaskListAdapater resultTaskAdapter;
        CardView userAvatar;
        ImageView userAvatarImg,asr_image;
        int type;
        private RecyclerHolder(View itemView,int type) {
            super(itemView);
            this.type = type;
            if(type == TYPE_LEFT) {
                textView = itemView.findViewById(R.id.left_text);
                hint = itemView.findViewById(R.id.asr_hint);
                resultRecy = itemView.findViewById(R.id.asr_recy);
                asr_image = itemView.findViewById(R.id.asr_img);
            } else{
                textView = itemView.findViewById(R.id.right_text);
                userAvatar = itemView.findViewById(R.id.chatbot_right_card);
                userAvatarImg = itemView.findViewById(R.id.usr_icon);
            }
        }
    }


}
