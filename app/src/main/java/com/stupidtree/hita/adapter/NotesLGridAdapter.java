package com.stupidtree.hita.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.stupidtree.hita.R;
import com.stupidtree.hita.core.Note;

import java.io.File;
import java.util.List;


public class NotesLGridAdapter extends RecyclerView.Adapter<NotesLGridAdapter.NotesGridViewHolder> {

    LayoutInflater mInflater;
    List<Note> mBeans;
    Context mContext;
    OnItemClickLitener mOnItemClickLitener;
    OnItemLongClickListener mOnItemLongClickListener;
    //mImageLoader mImageLoader;


    public interface OnItemClickLitener{
        void onItemClick(View view, int position,ImageView photoView);
    }
    public interface  OnItemLongClickListener{
        boolean onItemClick(View view,int position);
    }
    public NotesLGridAdapter(Context context,List<Note> res){
        mInflater = LayoutInflater.from(context);
        mBeans = res;
        mContext = context;
    }


    @NonNull
    @Override
    public NotesGridViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int type) {
        View v = mInflater.inflate(R.layout.dynamic_notes_griditem,viewGroup,false);
        return new NotesGridViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final NotesGridViewHolder notesGridViewHolder, final int position) {
        //notesGridViewHolder.image.setImageDrawable(BitmapDrawable.createFromPath(mBeans.get(position).imagePath));
        //mImageLoader.loadImage(mBeans.get(position).imagePath,notesGridViewHolder.image);
        if(mBeans.get(position).imagePath==null){
            notesGridViewHolder.image.setVisibility(View.GONE);
            notesGridViewHolder.text.setVisibility(View.VISIBLE);
            notesGridViewHolder.text.setText(mBeans.get(position).text);
        }else{
            notesGridViewHolder.image.setVisibility(View.VISIBLE);
            notesGridViewHolder.text.setVisibility(View.GONE);
            Glide.with(mContext).load(new File(mBeans.get(position).imagePath)).into(notesGridViewHolder.image);
            notesGridViewHolder.number.setText(position+"");
            if(mOnItemClickLitener!=null){
                notesGridViewHolder.card.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnItemClickLitener.onItemClick(v,position,notesGridViewHolder.image);
                    }
                });

            }
        }
        if(mOnItemLongClickListener!=null){
            notesGridViewHolder.card.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return mOnItemLongClickListener.onItemClick(v,position);
                }
            });
        }

    }

    public void insertNote(Note n){
        mBeans.add(n);
        notifyItemInserted(mBeans.size()-1);
    }
    public void removeNote(int position){
        mBeans.remove(position);
        notifyItemRemoved(position);
        if(position != mBeans.size()){ // 如果移除的是最后一个，忽略
            notifyItemRangeChanged(position, mBeans.size() - position);
        }
    }


    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener){
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    public void setOnItemLongClickLitener(OnItemLongClickListener mOnItemLongClickLitener){
        this.mOnItemLongClickListener = mOnItemLongClickLitener;
    }
    @Override
    public int getItemCount() {
        return mBeans.size();
    }


    class NotesGridViewHolder extends RecyclerView.ViewHolder{
        ImageView image;
        TextView text;
        TextView number;
        CardView card;
        public NotesGridViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.griditem_img);
            text = itemView.findViewById(R.id.griditem_text);
            card = itemView.findViewById(R.id.notes_cardview);
            number = itemView.findViewById(R.id.griditem_number);
        }
    }



}




