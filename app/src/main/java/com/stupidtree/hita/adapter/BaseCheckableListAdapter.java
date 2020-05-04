package com.stupidtree.hita.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.stupidtree.hita.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public abstract class BaseCheckableListAdapter<T, H extends BaseCheckableListAdapter.CheckableViewHolder> extends BaseListAdapter<T, H> {
    boolean EditMode = false;
    private OnItemSelectedListener onItemSelectedListener;
    private HashSet<T> checkedItem;

    BaseCheckableListAdapter(Context mContext, List<T> mBeans) {
        super(mContext, mBeans);
    }

    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
        this.onItemSelectedListener = onItemSelectedListener;
    }

    public boolean selectAll() {
        boolean someThingAdded = false;
        for (T ei : mBeans) {
            if (checkedItem.add(ei)) someThingAdded = true;
        }
        if (!someThingAdded) return false;
        notifyDataSetChanged();
        return true;
    }

    public boolean selectAllSmooth() {
        boolean someThingAdded = false;
        for (T ei : mBeans) {
            if (checkedItem.add(ei)) someThingAdded = true;
        }
        if (!someThingAdded) return false;
        notifyItemChangedSmooth(new ArrayList<T>(mBeans));
        //  notifyDataSetChanged();
        return true;
    }

    public void activateEditSmooth(int initPos) {
        EditMode = true;
        checkedItem = new HashSet<>();
        checkedItem.add(mBeans.get(initPos));
        notifyItemChangedSmooth(new ArrayList<T>(mBeans));
        // notifyDataSetChanged();
    }

    public void activateEdit(int initPos) {
        EditMode = true;
        checkedItem = new HashSet<>();
        checkedItem.add(mBeans.get(initPos));
        notifyDataSetChanged();
    }

    public void closeEditSmooth() {
        EditMode = false;
        notifyItemChangedSmooth(new ArrayList<T>(mBeans));
    }

    public void closeEdit() {
        EditMode = false;
        notifyDataSetChanged();
    }

    public HashSet<T> getCheckedItem() {
        return checkedItem;
    }

    abstract void bindHolderData(H holder, int position, T data);

    @Override
    public void onBindViewHolder(@NonNull final H holder, final int position) {
        super.onBindViewHolder(holder, position);
        if (mOnItemLongClickListener != null && holder.item != null)
            holder.item.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (EditMode) return false;
                    return mOnItemLongClickListener.onItemLongClick(v, position);
                }
            });
        if (mOnItemClickListener != null && holder.item != null)
            holder.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (EditMode && holder.checkBox != null) {
                        holder.checkBox.toggle();
                    } else mOnItemClickListener.onItemClick(view, position);
                }
            });
        if (EditMode && holder.checkBox != null) {
            holder.checkBox.setVisibility(View.VISIBLE);
        } else if (holder.checkBox != null) {
            holder.checkBox.setVisibility(View.GONE);
        }
        if (position + getIndexBias() <= mBeans.size() - 1) {
            final T data = mBeans.get(position + getIndexBias());
            if (EditMode && holder.checkBox != null) {
                holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) checkedItem.add(data);
                        else checkedItem.remove(data);
                        if (onItemSelectedListener != null)
                            onItemSelectedListener.OnItemSelected(buttonView, isChecked, position, checkedItem.size());
                    }
                });
                holder.checkBox.setChecked(checkedItem.contains(data));
            }
            bindHolderData(holder, position, data);
        } else {
            bindHolderData(holder, position, null);
        }


    }

    public interface OnItemSelectedListener {
        void OnItemSelected(View v, boolean checked, int position, int selectedNum);
    }

    public class CheckableViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        ViewGroup item;

        CheckableViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.check);
            item = itemView.findViewById(R.id.item);
        }
    }

}
