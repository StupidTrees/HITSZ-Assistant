package com.stupidtree.hita.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.services.route.WalkStep;
import com.stupidtree.hita.R;
import com.stupidtree.hita.util_navi.AMapUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 步行路线详情页adapter
 *
 */
public class WalkSegmentListAdapter extends RecyclerView.Adapter<WalkSegmentListAdapter.SegmentViewHolder> {
    private Context mContext;
    private List<WalkStep> mItemList = new ArrayList<WalkStep>();
    LayoutInflater mInflater;

    public WalkSegmentListAdapter(Context applicationContext,
                                  List<WalkStep> steps) {
        mContext = applicationContext;
        mItemList.add(new WalkStep());
        for (WalkStep walkStep : steps) {
            mItemList.add(walkStep);
        }
        mItemList.add(new WalkStep());
        mInflater = LayoutInflater.from(applicationContext);
    }


    @NonNull
    @Override
    public SegmentViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = mInflater.inflate(R.layout.dynamic_walk_segment_item_bus,viewGroup,false);
        return new SegmentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SegmentViewHolder segmentViewHolder, int position) {
        if (position == 0) {
            segmentViewHolder.dirIcon.setImageResource(R.drawable.dir_start);
            segmentViewHolder.lineName.setText("出发");
            segmentViewHolder.dirUp.setVisibility(View.INVISIBLE);
            segmentViewHolder.dirDown.setVisibility(View.VISIBLE);
        } else if (position == mItemList.size() - 1) {
            segmentViewHolder.dirIcon.setImageResource(R.drawable.dir_end);
            segmentViewHolder.lineName.setText("到达终点");
            segmentViewHolder.dirUp.setVisibility(View.VISIBLE);
            segmentViewHolder.dirDown.setVisibility(View.INVISIBLE);
        } else {
            segmentViewHolder.dirUp.setVisibility(View.VISIBLE);
            segmentViewHolder.dirDown.setVisibility(View.VISIBLE);
            String actionName = mItemList.get(position).getAction();
            int resID = AMapUtil.getWalkActionID(actionName);
            segmentViewHolder.dirIcon.setImageResource(resID);
            segmentViewHolder.lineName.setText(mItemList.get(position).getInstruction());
        }

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }



   class SegmentViewHolder extends RecyclerView.ViewHolder{
        TextView lineName;
        ImageView dirIcon;
        ImageView dirUp;
        ImageView dirDown;
        

        public SegmentViewHolder(@NonNull View itemView) {
            super(itemView);
            lineName =  itemView
                    .findViewById(R.id.bus_line_name);
            dirIcon =  itemView
                    .findViewById(R.id.bus_dir_icon);
            dirUp =  itemView
                    .findViewById(R.id.bus_dir_icon_up);
            dirDown =  itemView
                    .findViewById(R.id.bus_dir_icon_down);
        }
    }

}
