package com.stupidtree.hita.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.stupidtree.hita.R;
import com.stupidtree.hita.diy.CornerTransform;
import com.stupidtree.hita.online.LostAndFound;
import com.stupidtree.hita.online.HITAUser;
import com.stupidtree.hita.util.ActivityUtils;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.TimeWatcher.todaysEvents;
import static com.stupidtree.hita.adapter.IpNewsListAdapter.dip2px;

public class LostAndFoundListAdapter extends RecyclerView.Adapter <LostAndFoundListAdapter.societyViewholder>{


    LayoutInflater mInflaterl;
    List<LostAndFound> mBeans;
    Context mContext;
    CornerTransform transformation;
    OnPostClickListener mOnPostClickListener;


    public interface OnPostClickListener{
        void OnClick(View v,LostAndFound laf,HITAUser author);
    }

    public void setmOnPostClickListener(OnPostClickListener mOnPostClickListener) {
        this.mOnPostClickListener = mOnPostClickListener;
    }

    public LostAndFoundListAdapter(Context context, List<LostAndFound> res){
        this.mInflaterl = LayoutInflater.from(context);
        mContext = context;
        mBeans = res;
        transformation = new CornerTransform(mContext, dip2px(mContext, 10));
    }

    @NonNull
    @Override
    public societyViewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = mInflaterl.inflate(R.layout.dynamic_laf_post,viewGroup,false);
        return new societyViewholder(v);
    }

    void deleteItem(int position){
        mBeans.remove(position);
        notifyItemRemoved(position);
        if(position != mBeans.size()){ // 如果移除的是最后一个，忽略
            notifyItemRangeChanged(position, todaysEvents.size() - position);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final societyViewholder societyViewholder, final int i) {
        societyViewholder.content.setText(mBeans.get(i).getContent());
        societyViewholder.title.setText(mBeans.get(i).getTitle());
        societyViewholder.time.setText(mBeans.get(i).getUpdatedAt());
        if(mBeans.get(i).getImageUri()!=null&&(!mBeans.get(i).getImageUri().isEmpty())){
            societyViewholder.image.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(mBeans.get(i).getImageUri()).apply(RequestOptions.bitmapTransform(transformation)).into(societyViewholder.image);
        }else  societyViewholder.image.setVisibility(View.GONE);
        BmobQuery<HITAUser> bq = new BmobQuery();
        bq.addWhereEqualTo("objectId",mBeans.get(i).getAuthor().getObjectId());
        bq.findObjects(new FindListener<HITAUser>() {
            @Override
            public void done(final List<HITAUser> list, BmobException e) {
                if(e==null&&list!=null&list.size()>0){
                    societyViewholder.author.setText(list.get(0).getNick());
                    Glide.with(mContext).load(list.get(0).getAvatarUri()).
                            placeholder(R.drawable.ic_account_activated)
                            //.signature(new ObjectKey(String.valueOf(System.currentTimeMillis())))
                            .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                            .into(societyViewholder.avatar);
                    if(mOnPostClickListener!=null){
                        societyViewholder.card.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mOnPostClickListener.OnClick(v,mBeans.get(i),list.get(0));
                            }
                        });
                    }
                }else Log.e("!",e.toString());

            }
        });
        societyViewholder.avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.startUserProfileActivity((Activity) mContext,mBeans.get(i).getAuthor().getObjectId(),v);
            }
        });
        if(CurrentUser!=null&& CurrentUser.getObjectId() .equals(mBeans.get(i).getAuthor().getObjectId())){
            societyViewholder.delete.setVisibility(View.VISIBLE);
            societyViewholder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog ad = new AlertDialog.Builder(mContext).setTitle("提示").setMessage("确定删除该失物招领吗？").setNegativeButton("取消",null)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    final LostAndFound hp = mBeans.get(i);
                                    if(hp.getImageUri()!=null&&(!hp.getImageUri().isEmpty())){
                                        BmobFile bf = new BmobFile();
                                        bf.setUrl(hp.getImageUri());
                                        bf.delete(new UpdateListener() {
                                            @Override
                                            public void done(BmobException e) {
                                                hp.delete(new UpdateListener() {
                                                    @Override
                                                    public void done(BmobException e) {
                                                        Toast.makeText(HContext,"删除成功!",Toast.LENGTH_SHORT).show();
                                                        deleteItem(i);
                                                    }
                                                });
                                            }
                                        });
                                    }else{
                                        hp.delete(new UpdateListener() {
                                            @Override
                                            public void done(BmobException e) {
                                                Toast.makeText(HContext,"删除成功!",Toast.LENGTH_SHORT).show();
                                                deleteItem(i);
                                            }
                                        });
                                    }
                                }
                            }).create();
                    ad.show();


                }
            });
        }else{
            societyViewholder.delete.setVisibility(View.GONE);
        }


    }

    @Override
    public int getItemCount() {
        return mBeans.size();
    }

    class societyViewholder extends RecyclerView.ViewHolder{
        TextView author,title,content,time;
        ImageView avatar;
        ImageView image;
        ImageView delete;
        CardView card;
        public societyViewholder(@NonNull View itemView) {
            super(itemView);
            author = itemView.findViewById(R.id.post_author);
            title = itemView.findViewById(R.id.post_title);
            content = itemView.findViewById(R.id.post_content);
            time = itemView.findViewById(R.id.post_time);
            avatar = itemView.findViewById(R.id.post_avatar);
            image = itemView.findViewById(R.id.post_image);
            delete = itemView.findViewById(R.id.post_delete);
            card = itemView.findViewById(R.id.post_card);
        }
    }
}
