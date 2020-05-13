package com.stupidtree.hita.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.stupidtree.hita.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class XSXKListAdapter extends RecyclerView.Adapter<XSXKListAdapter.pyjhItemHolder> {

    private static final int HEADER = 998;
    private static final int ITEM = 394;
    private static final int FOOT = 200;
    private List<Map<String, String>> mBeans;
    private LayoutInflater mInflater;
    private boolean footEnable;
    private Context mContext;
    private OnItemClickListener onItemClickListener;


    public interface OnOperateClickListener {
        void OnClick(View view, int index, boolean choose, String rwh);
    }

    public XSXKListAdapter(Context mContext, List<Map<String, String>> mBeans, boolean footEnable) {
        this.mBeans = mBeans;
        this.mInflater = LayoutInflater.from(mContext);
        this.mContext = mContext;
        this.footEnable = footEnable;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;

    }

    @NonNull
    @Override
    public pyjhItemHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        int layout;
        if (i == ITEM) layout = R.layout.dynamic_jwt_xsxk_item;
        else if (i == FOOT) layout = R.layout.dynamic_jw_xsxk_yx_foot;
        else layout = R.layout.dynamic_jwt_xsxk_header;
        View v = mInflater.inflate(layout, viewGroup, false);
        return new pyjhItemHolder(v, i);
    }

    public void setmOnOperateClickListsner(OnOperateClickListener mOnOperateClickListsner) {
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mBeans.size()) return FOOT;
        else if (mBeans.get(position).get("header") != null) return HEADER;
        else return ITEM;
    }


    @Override
    public void onBindViewHolder(@NonNull pyjhItemHolder pyjhItemHolder, final int i) {

        if (pyjhItemHolder.viewType == ITEM) {
            Map<String, String> itemM = mBeans.get(i);
            pyjhItemHolder.name.setText(itemM.get("name"));
            pyjhItemHolder.type.setText(itemM.get("type"));
            pyjhItemHolder.credit.setText(itemM.get("credit"));
            pyjhItemHolder.xs.setText(itemM.get("xs"));
            if (onItemClickListener != null) {
                pyjhItemHolder.item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onItemClickListener.OnClick(v, Math.abs(i));
                    }
                });
            }
        } else if (pyjhItemHolder.viewType == FOOT) {

            if (onItemClickListener != null) {
                pyjhItemHolder.item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onItemClickListener.OnClick(v, i);
                    }
                });
            }
        } else {
            Map<String, String> itemM = mBeans.get(i);
            if (pyjhItemHolder.begin != null) {
                if (itemM.get("begin") != null) pyjhItemHolder.begin.setText(itemM.get("begin"));
            }
            if (pyjhItemHolder.end != null) {
                if (itemM.get("end") != null) pyjhItemHolder.end.setText(itemM.get("end"));
            }

            try {
                Date from = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault()).parse(itemM.get("begin"));
                Date to = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault()).parse(itemM.get("end"));
                Calendar tempNow = Calendar.getInstance();
                if (from.before(tempNow.getTime()) && to.after(tempNow.getTime())) {
                    pyjhItemHolder.clock_activated.setVisibility(View.VISIBLE);
                    pyjhItemHolder.clock_disabled.setVisibility(View.GONE);
                } else {
                    pyjhItemHolder.clock_activated.setVisibility(View.GONE);
                    pyjhItemHolder.clock_disabled.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {

            }
        }

//        if(pyjhItemHolder.type==LABEL&&i<labels.size()){
//            pyjhItemHolder.text.setText(labels.get(i));
//        }else {
//            int index =i/labels.size()-1;
//            int bias = i%labels.size();
//            pyjhItemHolder.text.setText(mBeans.get(index).get(labels.get(bias)));
//        }
    }

    @Override
    public int getItemCount() {
        return footEnable ? mBeans.size() + 1 : mBeans.size();
    }


    public interface OnItemClickListener {
        void OnClick(View view, int position);
    }

    static class pyjhItemHolder extends RecyclerView.ViewHolder {
        View item;
        TextView name, credit, xs, type;
        TextView begin, end;// for header
        ImageView clock_activated, clock_disabled;
        int viewType;

        pyjhItemHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            item = itemView.findViewById(R.id.item);
            name = itemView.findViewById(R.id.name);
            credit = itemView.findViewById(R.id.credit);
            xs = itemView.findViewById(R.id.xs);
            type = itemView.findViewById(R.id.type);
            begin = itemView.findViewById(R.id.begin);
            end = itemView.findViewById(R.id.end);
            clock_activated = itemView.findViewById(R.id.activated);
            clock_disabled = itemView.findViewById(R.id.disabled);
            this.viewType = viewType;

        }
    }
}
