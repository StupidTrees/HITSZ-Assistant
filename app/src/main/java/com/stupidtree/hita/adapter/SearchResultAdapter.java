package com.stupidtree.hita.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.stupidtree.hita.R;
import com.stupidtree.hita.online.Canteen;
import com.stupidtree.hita.online.Classroom;
import com.stupidtree.hita.online.Dormitory;
import com.stupidtree.hita.online.Facility;
import com.stupidtree.hita.online.Location;
import com.stupidtree.hita.online.Scenery;
import com.stupidtree.hita.online.Teacher;
import com.stupidtree.hita.util.ActivityUtils;

import java.util.List;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.holder1> {

    List<BmobObject> mBeans;
    LayoutInflater mInflater;
    Context mContext;
    OnItemClickListener onItemClickListener;
    public interface OnItemClickListener{
        void Onlick(View v,int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public SearchResultAdapter(Context c, List<BmobObject> list) {
        this.mInflater = LayoutInflater.from(c);
        mContext = c;
        mBeans = list;
    }

    @NonNull
    @Override
    public holder1 onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = mInflater.inflate(R.layout.dynamic_search_item,parent,false);
        return new holder1(v);
    }

    @Override
    public void onBindViewHolder(@NonNull holder1 holder, final int position) {
        if(mBeans.get(position) instanceof Location) {
            final Location l = (Location) mBeans.get(position);
            holder.name.setText(l.getName());
            holder.type.setText(l.getType_Name());
            int iconId = R.drawable.ic_location2;
            LocationClickListener listener = new LocationClickListener();
            if(l.getType().equals("canteen")){
                final Canteen c = new Canteen(l);
                iconId = R.drawable.ic_lunch;
                listener.setL(c);
            }else if(l.getType().equals("classroom")){
                final Classroom cr =  new Classroom(l);
                iconId = R.drawable.ic_dlg_tt_classroom;
                listener.setL(cr);
            }else if(l.getType().equals("facility")){
                final Facility f = new Facility(l);
                iconId = R.drawable.ic_facility;
                listener.setL(f);
            }else if(l.getType().equals("dormitory")){
                final Dormitory d = new Dormitory(l);
                iconId = R.drawable.ic_business;
                listener.setL(d);
            }else if(l.getType().equals("scenery")){
                final Scenery s = new Scenery(l);
                iconId = R.drawable.ic_landscape;
                listener.setL(s);
            }
            holder.icon.setImageResource(iconId);

            holder.item.setOnClickListener(listener);
        }
        if(mBeans.get(position) instanceof Teacher) {
            final Teacher t = (Teacher) mBeans.get(position);
            holder.name.setText(t.getName());
            holder.type.setText("教师");
            holder.icon.setImageResource(R.drawable.ic_dlg_tt_teacher);
            holder.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityUtils.startTeacherActivity(mContext,t);
                }
            });
        }
        if(onItemClickListener!=null){
            holder.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.Onlick(v,position);
                }
            });
        }
    }

    public void addItem(BmobObject bo,int position){
        mBeans.add(position,bo);
        notifyItemInserted(position);
        notifyItemRangeChanged(
                position,mBeans.size()
        );
    }

    @Override
    public int getItemCount() {
        return mBeans.size();
    }


    class LocationClickListener implements View.OnClickListener{

        Location l;

        public Location getL() {
            return l;
        }

        public void setL(Location l) {
            this.l = l;
        }

        @Override
        public void onClick(View v) {
            if(l==null) return;
            Location up = new Location(l);
            up.setSearch(l.getSearch()+1);
            up.update(new UpdateListener() {
                @Override
                public void done(BmobException e) {
                    if(e!=null) Log.e("!",e.toString());
                }
            });
            ActivityUtils.startLocationActivity(mContext,l);
        }
    }
    class holder1 extends RecyclerView.ViewHolder{

        TextView name,type;
        ImageView icon;
        LinearLayout item;
        public holder1(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            type = itemView.findViewById(R.id.type);
            icon = itemView.findViewById(R.id.icon);
            item = itemView.findViewById(R.id.item);
        }
    }
}
