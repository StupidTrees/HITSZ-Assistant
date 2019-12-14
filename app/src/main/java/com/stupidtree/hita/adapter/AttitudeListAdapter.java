package com.stupidtree.hita.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.stupidtree.hita.R;
import com.stupidtree.hita.online.Attitude;
import com.stupidtree.hita.online.HITAUser;

import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.TPE;

public class AttitudeListAdapter extends RecyclerView.Adapter<AttitudeListAdapter.ViewH> {

    List<Attitude> mBeans;
    LayoutInflater mInflater;
    Context mContext;

    public AttitudeListAdapter(Context mContext, List<Attitude> mBeans) {
        this.mBeans = mBeans;
        this.mContext = mContext;
        this.mInflater = LayoutInflater.from(mContext);
    }

    @NonNull
    @Override
    public ViewH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = mInflater.inflate(R.layout.dynamic_attitute_item,parent,false);
        return new ViewH(v);
    }

    @Override
    public int getItemCount() {
        return mBeans.size();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final ViewH holder, final int position) {
        final Attitude attitude = mBeans.get(position);
        holder.title.setText(attitude.getTitle());
        new refreshItemTask(holder,position,attitude,CurrentUser).executeOnExecutor(TPE);
    }

    class ViewH extends RecyclerView.ViewHolder {
        TextView title,upT,downT;
        FrameLayout voted;
        LinearLayout voting;
        ImageView up,down;
        ProgressBar result;


        public ViewH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            up = itemView.findViewById(R.id.up);
            down = itemView.findViewById(R.id.down);
            voted = itemView.findViewById(R.id.voted);
            voting = itemView.findViewById(R.id.voting);
            upT = itemView.findViewById(R.id.up_text);
            downT = itemView.findViewById(R.id.down_text);
            result = itemView.findViewById(R.id.vote_result);
        }
    }

    class refreshItemTask extends AsyncTask{

        ViewH holder;
        int position;
        Attitude attitude;
        HITAUser user;

        public refreshItemTask(ViewH holder, int position, Attitude attitude, HITAUser user) {
            this.holder = holder;
            this.position = position;
            this.attitude = attitude;
            this.user = user;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            holder.voted.setVisibility(View.GONE);
            holder.voting.setVisibility(View.INVISIBLE
            );
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            if(user==null) return true;
           return attitude.voted(CurrentUser);
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if((boolean)o){
                holder.voted.setVisibility(View.VISIBLE);
                holder.voting.setVisibility(View.GONE);
                holder.upT.setText(attitude.getUp()+"");
                holder.downT.setText(attitude.getDown()+"");
                holder.result.setProgress((int) ((float) attitude.getDown()/(attitude.getUp()+ attitude.getDown())*100));
            }else {
                holder.voted.setVisibility(View.GONE);
                holder.voting.setVisibility(View.VISIBLE);
                holder.up.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        attitude.thumUp(CurrentUser);
                        Attitude a = new Attitude(attitude);
                        a.update(new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if(e==null) Toast.makeText(mContext,"表态成功！",Toast.LENGTH_SHORT).show();
                                else Toast.makeText(mContext,"表态失败",Toast.LENGTH_SHORT).show();
                                notifyItemChanged(position);
                            }
                        });
                    }
                });
                holder.down.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        attitude.thumDown(CurrentUser);
                        Attitude a = new Attitude(attitude);
                        a.update(new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if(e==null) Toast.makeText(mContext,"表态成功！",Toast.LENGTH_SHORT).show();
                                else {
                                    e.printStackTrace();
                                    Toast.makeText(mContext,"表态失败"+e.toString(),Toast.LENGTH_SHORT).show();
                                }
                                notifyItemChanged(position);
                            }
                        });
                    }
                });
            }
        }
    }


}
