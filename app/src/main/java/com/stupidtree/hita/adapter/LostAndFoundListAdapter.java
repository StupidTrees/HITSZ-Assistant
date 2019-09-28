package com.stupidtree.hita.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.os.AsyncTask;
import android.text.TextUtils;
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
import com.stupidtree.hita.online.Location;
import com.stupidtree.hita.online.LostAndFound;
import com.stupidtree.hita.online.HITAUser;
import com.stupidtree.hita.util.ActivityUtils;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.TimeWatcher.todaysEvents;
import static com.stupidtree.hita.adapter.NewsIpNewsListAdapter.dip2px;

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
        if(TextUtils.isEmpty(mBeans.get(i).getContent())) societyViewholder.content.setVisibility(View.GONE);
        else societyViewholder.content.setVisibility(View.VISIBLE);
        societyViewholder.content.setText(mBeans.get(i).getContent());
        societyViewholder.title.setText(mBeans.get(i).getTitle());
        societyViewholder.time.setText(mBeans.get(i).getCreatedAt());
        if(mBeans.get(i).getImageUri()!=null&&(!mBeans.get(i).getImageUri().isEmpty())){
            societyViewholder.image.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(mBeans.get(i).getImageUri()).apply(RequestOptions.bitmapTransform(transformation)).into(societyViewholder.image);
        }else  societyViewholder.image.setVisibility(View.GONE);

        societyViewholder.avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.startUserProfileActivity((Activity) mContext,mBeans.get(i).getAuthor().getObjectId(),v);
            }
        });
        if(mBeans.get(i).getLocation()!=null&&mBeans.get(i).getLocation().getName()==null
        ||mBeans.get(i).getAuthor().getNick()==null
        ){
            new loadLAFTask(societyViewholder,mBeans.get(i)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
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
         TextView author;
        TextView title;
        TextView content;
        TextView time;
        TextView location_text;
        ImageView avatar;
        ImageView image;
        ImageView delete;
        CardView card,location_card;
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
            location_card = itemView.findViewById(R.id.location_card);
            location_text = itemView.findViewById(R.id.location_text);
        }
    }

    class loadLAFTask extends AsyncTask{

        societyViewholder viewholder;
        LostAndFound lostAndFound;
        HITAUser user;
        Location location;

        public loadLAFTask(societyViewholder viewholder, LostAndFound lostAndFound) {
            this.viewholder = viewholder;
            this.lostAndFound = lostAndFound;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //viewholder.location_card.setVisibility(View.INVISIBLE);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            BmobQuery<HITAUser> bq_usr= new BmobQuery();
            bq_usr.addWhereEqualTo("objectId",lostAndFound.getAuthor().getObjectId());
            List<HITAUser> res_usr = bq_usr.findObjectsSync(HITAUser.class);
                    if(res_usr!=null&&res_usr.size()>0) user = res_usr.get(0);
            if(lostAndFound.getLocation()!=null){
                BmobQuery<Location> bq = new BmobQuery();
                bq.addWhereEqualTo("objectId",lostAndFound.getLocation().getObjectId());
                List<Location> res_loc = bq.findObjectsSync(Location.class);
                if(res_loc!=null&&res_loc.size()>0) location = res_loc.get(0);
            }
            return null;

        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            try {
                if(user!=null){
                    viewholder.author.setText(user.getNick());
                    Glide.with(mContext).load(user.getAvatarUri()).
                            placeholder(R.drawable.ic_account_activated)
                            //.signature(new ObjectKey(String.valueOf(System.currentTimeMillis())))
                            .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                            .into(viewholder.avatar);
                    if(mOnPostClickListener!=null){
                        viewholder.card.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mOnPostClickListener.OnClick(v,lostAndFound,user);
                            }
                        });
                    }
                }else Toast.makeText(HContext,"获取用户信息失败！",Toast.LENGTH_SHORT).show();


                if(location!=null){
                    viewholder.location_card.setVisibility(View.VISIBLE);
                    lostAndFound.setLocation(location);
                    viewholder.location_text.setText(location.getName());
                    viewholder.location_card.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityUtils.startLocationActivity(mContext,location);
                        }
                    });
                }else viewholder.location_card.setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
