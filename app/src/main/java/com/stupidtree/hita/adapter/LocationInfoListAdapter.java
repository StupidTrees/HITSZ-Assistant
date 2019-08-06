package com.stupidtree.hita.adapter;


import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.stupidtree.hita.R;

import java.util.ArrayList;
import java.util.HashMap;

public class LocationInfoListAdapter extends RecyclerView.Adapter<LocationInfoListAdapter.mViewHolder>{

    LayoutInflater mInflater;
    ArrayList<HashMap> mBeans;
    int colorPrimary;

    public LocationInfoListAdapter(Context c, ArrayList<HashMap> x, int colorPrimary){
        mInflater = LayoutInflater.from(c);
        mBeans = x;
        this.colorPrimary = colorPrimary;
    }

    @NonNull
    @Override
    public mViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = mInflater.inflate(R.layout.dynamic_location_info,viewGroup,false);
        return new mViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull mViewHolder mViewHolder, int i) {
        if((Boolean) mBeans.get(i).get("is_colored")) mViewHolder.value.setTextColor(colorPrimary);
        if(mBeans.get(i).get("is_phonenumber")!=null&&(Boolean) mBeans.get(i).get("is_phonenumber")) mViewHolder.value.setAutoLinkMask(Linkify.PHONE_NUMBERS);
        mViewHolder.value.setText((String)mBeans.get(i).get("value"));
        mViewHolder.key.setText((CharSequence) mBeans.get(i).get("key"));
        mViewHolder.icon.setImageResource((Integer) mBeans.get(i).get("icon"));
    }

    @Override
    public int getItemCount() {
        return mBeans.size();
    }

    class mViewHolder extends RecyclerView.ViewHolder{

        ImageView icon;
        TextView key,value;
        public mViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.li_icon);
            key = itemView.findViewById(R.id.li_key);
            value = itemView.findViewById(R.id.li_value);
        }
    }
}
