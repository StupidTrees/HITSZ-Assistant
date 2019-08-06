package com.stupidtree.hita.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.stupidtree.hita.R;
import com.stupidtree.hita.diy.AutoLocateHorizontalView;

import java.util.List;

/**
 * Created by jianglei on 2/4/17.
 */

public class HorizonalViewAdapter extends RecyclerView.Adapter<HorizonalViewAdapter.AgeViewHolder> implements AutoLocateHorizontalView.IAutoLocateHorizontalView {
    private Context context;
    private View view;
    private List<String> ages;
    public HorizonalViewAdapter(Context context, List<String>ages){
        this.context = context;
        this.ages = ages;
    }

    @Override
    public AgeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = LayoutInflater.from(context).inflate(R.layout.style_timetable_horizonalitem,parent,false);
        return new AgeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AgeViewHolder holder, int position) {
        holder.tvAge.setText(ages.get(position));
    }

    @Override
    public int getItemCount() {
        return  ages.size();
    }

    @Override
    public View getItemView() {
        return view;
    }

    @Override
    public void onViewSelected(boolean isSelected,int pos, RecyclerView.ViewHolder holder,int itemWidth) {
        if(isSelected) {
            ((AgeViewHolder) holder).tvAge.setTextSize(22);
        }else{
            ((AgeViewHolder) holder).tvAge.setTextSize(14);
        }
    }

    static class AgeViewHolder extends RecyclerView.ViewHolder{
        TextView tvAge;
        AgeViewHolder(View itemView) {
            super(itemView);
            tvAge = itemView.findViewById(R.id.tv_age);
        }
    }
}
