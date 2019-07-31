package com.stupidtree.hita.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.stupidtree.hita.R;

import java.util.List;
import java.util.Map;

public class ZXJXJHListAdapter extends RecyclerView.Adapter<ZXJXJHListAdapter.zxjxjhItemHolder> {

List<Map<String,String>> mBeans;
LayoutInflater mInflater;

public ZXJXJHListAdapter(Context context, List<Map<String, String>> res){
    mBeans = res;
    mInflater = LayoutInflater.from(context);
}
    @NonNull
    @Override
    public zxjxjhItemHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
    View v = mInflater.inflate(R.layout.dynamic_jwts_zxjxjh_item,viewGroup,false);
        return new zxjxjhItemHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull zxjxjhItemHolder zxjxjhItemHolder, int i) {
        zxjxjhItemHolder.name.setText(mBeans.get(i).get("name"));
        zxjxjhItemHolder.attr.setText(mBeans.get(i).get("attr"));
        zxjxjhItemHolder.totalcourses.setText(mBeans.get(i).get("totalcourses"));
        zxjxjhItemHolder.exam.setText(mBeans.get(i).get("exam"));
        zxjxjhItemHolder.point.setText(mBeans.get(i).get("point"));
        zxjxjhItemHolder.xq.setText(mBeans.get(i).get("xq"));
        zxjxjhItemHolder.xn.setText(mBeans.get(i).get("xn"));
    }

    @Override
    public int getItemCount() {
        return mBeans.size();
    }

    class zxjxjhItemHolder extends RecyclerView.ViewHolder {
    TextView name,totalcourses,attr,point,exam,xn,xq;
        public zxjxjhItemHolder(@NonNull View itemView) {
            super(itemView);

            attr = itemView.findViewById(R.id.zxjxjh_attr);
            totalcourses = itemView.findViewById(R.id.zxjxjh_totalcourses);
            point = itemView.findViewById(R.id.zxjxjh_point);
            exam = itemView.findViewById(R.id.zxjxjh_exam);
            name = itemView.findViewById(R.id.zxjxjh_name);
            xn = itemView.findViewById(R.id.zxjxjh_xn);
            xq = itemView.findViewById(R.id.zxjxjh_xq);
        }
    }
}
