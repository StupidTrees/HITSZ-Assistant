package com.stupidtree.hita.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stupidtree.hita.R;

import java.util.List;
import java.util.Map;


public class KSXXListAdapter extends RecyclerView.Adapter<KSXXListAdapter.pyjhItemHolder> {

List<Map<String,String>> mBeans;
LayoutInflater mInflater;
OnOperateClickListener mOnOperateClickListsner;
boolean mini;
public interface OnOperateClickListener{
    void OnClick(View view, int index, boolean choose);
}

public KSXXListAdapter(Context context, List<Map<String, String>> res,boolean mini){
    mBeans = res;
    this.mini = mini;
    mInflater = LayoutInflater.from(context);
}

    public void setmOnOperateClickListsner(OnOperateClickListener mOnOperateClickListsner) {
        this.mOnOperateClickListsner = mOnOperateClickListsner;
    }

    @NonNull
    @Override
    public pyjhItemHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
    int ID = mini?R.layout.dynamic_navipage_ksxx_item:R.layout.dynamic_jwts_ksxx_item;
        View v = mInflater.inflate(ID,viewGroup,false);
        return new pyjhItemHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull pyjhItemHolder pyjhItemHolder, final int i) {

        pyjhItemHolder.name.setText(mBeans.get(i).get("name"));
        pyjhItemHolder.time.setText(mBeans.get(i).get("time"));
        pyjhItemHolder.place.setText(mBeans.get(i).get("place"));
        pyjhItemHolder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnOperateClickListsner.OnClick(view,i,false);
            }
        });
      }

    @Override
    public int getItemCount() {
        return mBeans.size();
    }

    class pyjhItemHolder extends RecyclerView.ViewHolder {
    TextView name,place,time;
    LinearLayout item;
        public pyjhItemHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.ksxx_name);
            place = itemView.findViewById(R.id.ksxx_place);
            time = itemView.findViewById(R.id.ksxx_time);
            item = itemView.findViewById(R.id.item);
        }
    }
}
