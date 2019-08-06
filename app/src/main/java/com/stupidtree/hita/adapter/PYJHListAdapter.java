package com.stupidtree.hita.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.stupidtree.hita.R;

import java.util.List;
import java.util.Map;

public class PYJHListAdapter extends RecyclerView.Adapter<PYJHListAdapter.pyjhItemHolder> {

List<Map<String,String>> mBeans;
LayoutInflater mInflater;

public PYJHListAdapter(Context context, List<Map<String, String>> res){
    mBeans = res;
    mInflater = LayoutInflater.from(context);
}
    @NonNull
    @Override
    public pyjhItemHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
    View v = mInflater.inflate(R.layout.dynamic_jwts_pyjh_item,viewGroup,false);
        return new pyjhItemHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull pyjhItemHolder pyjhItemHolder, int i) {

        pyjhItemHolder.name.setText(mBeans.get(i).get("name"));
        pyjhItemHolder.attr.setText(mBeans.get(i).get("attr"));
        pyjhItemHolder.totalcourses.setText(mBeans.get(i).get("totalcourses"));
        pyjhItemHolder.exam.setText(mBeans.get(i).get("exam"));
        pyjhItemHolder.point.setText(mBeans.get(i).get("point"));
        pyjhItemHolder.xq.setText(mBeans.get(i).get("xq"));
        pyjhItemHolder.xn.setText(mBeans.get(i).get("xn"));
    }

    @Override
    public int getItemCount() {
        return mBeans.size();
    }

    class pyjhItemHolder extends RecyclerView.ViewHolder {
    TextView name,totalcourses,attr,point,exam,xn,xq;
        public pyjhItemHolder(@NonNull View itemView) {
            super(itemView);

            attr = itemView.findViewById(R.id.pyjh_attr);
            totalcourses = itemView.findViewById(R.id.pyjh_totalcourses);
            point = itemView.findViewById(R.id.pyjh_point);
            exam = itemView.findViewById(R.id.pyjh_exam);
            name = itemView.findViewById(R.id.pyjh_name);
            xn= itemView.findViewById(R.id.pyjh_xn);
            xq = itemView.findViewById(R.id.pyjh_xq);

        }
    }
}
