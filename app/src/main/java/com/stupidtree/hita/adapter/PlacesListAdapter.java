package com.stupidtree.hita.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.stupidtree.hita.R;

import java.util.List;
import java.util.Map;

public class PlacesListAdapter extends RecyclerView.Adapter<PlacesListAdapter.PlacesViewHolder> {

    LayoutInflater mInflater;
List<Map> mBeans;



    PlacesListAdapter(Context context, List<Map>mBeans){
        mInflater = LayoutInflater.from(context);
        this.mBeans = mBeans;
    }
    @NonNull
    @Override
    public PlacesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = mInflater.inflate(R.layout.dynamic_places_item,viewGroup,false);
        return new PlacesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PlacesViewHolder placesViewHolder, int i) {
        placesViewHolder.image.setImageResource((Integer) mBeans.get(i).get("image"));
        placesViewHolder.text.setText((String) mBeans.get(i).get("text"));
    }

    @Override
    public int getItemCount() {
        return mBeans.size();
    }

    class PlacesViewHolder extends RecyclerView.ViewHolder{

        ImageView image;
        TextView text;
        public PlacesViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.place_img);
            text = itemView.findViewById(R.id.place_text);
        }
    }
}
