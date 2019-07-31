package com.stupidtree.hita.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.stupidtree.hita.R;
import com.stupidtree.hita.core.Note;

import java.util.ArrayList;

public class CourseNoteGridAdapter extends RecyclerView.Adapter<CourseNoteGridAdapter.GridHolder> {
LayoutInflater mInflater;
ArrayList<Note> mBeans;
Context mContext;
    private int mScreenWidth;
public CourseNoteGridAdapter(Context c, ArrayList<Note> s){
    mBeans = s;
    mContext = c;
    mInflater =LayoutInflater.from(c);
    //this.mScreenWidth = mScreenWidth;
}

    @NonNull
    @Override
    public GridHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View  v = mInflater.inflate(R.layout.dynamic_course_notes_griditem,viewGroup,false);
        return new GridHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull GridHolder gridHolder, int i) {
//        RecyclerView.LayoutParams tvLp = (RecyclerView.LayoutParams) gridHolder.image.getLayoutParams();
//        tvLp.height = heights.get(position);
//        tvLp.width = mScreenWidth / 3;
//        vHolder.tv.setLayoutParams(tvLp);
//        vHolder.tv.setText(list.get(position));
//        vHolder.iv.setLayoutParams(tvLp);
        Glide.with(mContext).load(mBeans.get(i).imagePath).into(gridHolder.image);
            gridHolder.text.setText(mBeans.get(i).text);

    }

    @Override
    public int getItemCount() {
        return mBeans.size();
    }

    class GridHolder extends RecyclerView.ViewHolder{

        ImageView image;
        TextView text;
        public GridHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.griditem_img);
            text = itemView.findViewById(R.id.griditem_text);
        }
    }
}
