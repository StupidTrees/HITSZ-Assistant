package com.stupidtree.hita.adapter;

import android.app.AlertDialog;
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

import android.widget.Button;


public class XSXKListAdapter extends RecyclerView.Adapter<XSXKListAdapter.pyjhItemHolder> {

    List<Map<String, String>> mBeans;
    LayoutInflater mInflater;
    Context mContext;
    OnOperateClickListener mOnOperateClickListsner;

    public interface OnOperateClickListener {
        void OnClick(View view, int index, boolean choose, String rwh);
    }

    public XSXKListAdapter(Context context, List<Map<String, String>> res) {
        mBeans = res;
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    public void setmOnOperateClickListsner(OnOperateClickListener mOnOperateClickListsner) {
        this.mOnOperateClickListsner = mOnOperateClickListsner;
    }

    @NonNull
    @Override
    public pyjhItemHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = mInflater.inflate(R.layout.dynamic_jwts_xsxk_item, viewGroup, false);
        return new pyjhItemHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull pyjhItemHolder pyjhItemHolder, final int i) {

        pyjhItemHolder.name.setText(mBeans.get(i).get("kcmc"));
        String totalORresult = mBeans.get(i).get("xs") == null ? mBeans.get(i).get("xkjg") : mBeans.get(i).get("xs");
        pyjhItemHolder.totalcourses.setText(totalORresult);
        pyjhItemHolder.content.setText(mBeans.get(i).get("yx/rl"));
        pyjhItemHolder.point.setText(mBeans.get(i).get("xf"));
        pyjhItemHolder.type.setText(mBeans.get(i).get("kclb"));
        pyjhItemHolder.operate.setText(mBeans.get(i).get("bxOryx").equals("bx") ? "选课" : "退选");
        if (mBeans.get(i).get("hasbutton").equals("true"))
            pyjhItemHolder.operate.setVisibility(View.VISIBLE);
        else pyjhItemHolder.operate.setVisibility(View.GONE);
        if (mOnOperateClickListsner != null) {
            pyjhItemHolder.operate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnOperateClickListsner.OnClick(v, i, mBeans.get(i).get("bxOryx").equals("bx"), mBeans.get(i).get("rwh"));
                }
            });
        }
        pyjhItemHolder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder sb = new StringBuilder();
                for (Map.Entry e : mBeans.get(i).entrySet()) {
                    String name = getName(e.getKey().toString());
                    if(name.equals("skip")) continue;
                   // Log.e("entry:",e.toString());
                    sb.append(name+"："+e.getValue()+"&&");
                }
                String[] items = sb.toString().split("&&");

                AlertDialog ad = new AlertDialog.Builder(mContext).setItems(items,null).create();
                ad.show();

            }
        });
    }


    private String getName(String pinyin) {
        if (pinyin.equals("kcmc")) return "课程名称";
        if (pinyin.equals("kclb")) return "课程类别";
        if (pinyin.equals("kcxz")) return "课程性质";
        if (pinyin.equals("xf")) return "学分";
        if (pinyin.equals("yx/rl")) return "已选/容量";
        if (pinyin.equals("xkjg")) return "选课结果";
        if (pinyin.equals("xs")) return "总学时";
        if (pinyin.equals("zys")) return "志愿数";
        if (pinyin.equals("gzyyxrs")) return "各志愿已选人数";
        if (pinyin.equals("skxx")) return "上课信息";
        if (pinyin.equals("xksj")) return "选课时间";
        return "skip";
    }

    @Override
    public int getItemCount() {
        return mBeans.size();
    }

    class pyjhItemHolder extends RecyclerView.ViewHolder {
        Button operate;
        TextView name, totalcourses, point, type, content;
        LinearLayout item;

        public pyjhItemHolder(@NonNull View itemView) {
            super(itemView);
            totalcourses = itemView.findViewById(R.id.xsxk_totalcourses);
            point = itemView.findViewById(R.id.xsxk_point);
            type = itemView.findViewById(R.id.xsxk_type);
            name = itemView.findViewById(R.id.xsxk_name);
            operate = itemView.findViewById(R.id.xsxk_operate);
            content = itemView.findViewById(R.id.xsxk_content);
            item = itemView.findViewById(R.id.xsxk_item);
        }
    }
}
