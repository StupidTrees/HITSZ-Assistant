package com.stupidtree.hita.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.res.ColorStateList;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.azoft.carousellayoutmanager.CarouselLayoutManager;
import com.azoft.carousellayoutmanager.CarouselZoomPostLayoutListener;
import com.bumptech.glide.Glide;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;
import com.stupidtree.hita.online.Infos;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.thisCurriculumIndex;

public class ActivityYX_ToSchool extends BaseActivity {

    RecyclerView list;
    RecyclerView routeList;
    JsonArray listRes;
    List<JsonObject> routeListRes;
    Toolbar toolbar;
    mainPageAdapter listAdapter;
    routeAdapter routeListAdapter;

    @Override
    protected void stopTasks() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true,true,false);
        setContentView(R.layout.activity_yx_to_school);
        initList();
        initToolbar();
        Refresh();
    }

    void initToolbar(){
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("选择到达点");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }
    void initList(){
        listRes = new JsonArray();
        routeListRes = new ArrayList<>();
        list = findViewById(R.id.gts_list);
        routeList = findViewById(R.id.route_list);
        listAdapter = new mainPageAdapter();
        routeListAdapter = new routeAdapter();
        routeList.setAdapter(routeListAdapter);
        routeList.setLayoutManager(new LinearLayoutManager(ActivityYX_ToSchool.this,RecyclerView.VERTICAL,false));
        list.setAdapter(listAdapter);
        final CarouselLayoutManager layoutManager = new CarouselLayoutManager(CarouselLayoutManager.HORIZONTAL);
        layoutManager.setMaxVisibleItems(8);
        layoutManager.setPostLayoutListener(new CarouselZoomPostLayoutListener());
        list.setLayoutManager(layoutManager);
        layoutManager.addOnItemSelectionListener(new CarouselLayoutManager.OnCenterItemSelectionListener() {
            @Override
            public void onCenterItemChanged(int adapterPosition) {
                if(adapterPosition<0||adapterPosition>=listRes.size()) return;
               routeListRes.clear();
               for(JsonElement je:listRes.get(adapterPosition).getAsJsonObject().get("route").getAsJsonArray()){
                   routeListRes.add(je.getAsJsonObject());
               }
               routeListAdapter.notifyDataSetChanged();
               routeList.scheduleLayoutAnimation();
            }
        });
    }

    void Refresh() {
        BmobQuery<Infos> bq = new BmobQuery<>();
        bq.addWhereEqualTo("objectid", "vihp7779");
        bq.getObject("vihp7779", new QueryListener<Infos>() {
            @Override
            public void done(Infos infos, BmobException e) {
                if (e == null && infos != null) {
                    listRes.addAll(infos.getJsonArray());
                    listAdapter.notifyDataSetChanged();
                } else {
                    e.printStackTrace();
                    Log.e("error!", e.toString());
                }

            }
        });
    }


    class mainPageAdapter extends RecyclerView.Adapter<mainPageAdapter.holder>{


        @NonNull
        @Override
        public holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.dynamic_yx_gts_item,parent,false);
            return new holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull final holder holder, final int position) {
            JsonObject jo = listRes.get(position).getAsJsonObject();
            Glide.with(ActivityYX_ToSchool.this).load(jo.get("image").getAsString()).into(holder.image);
            holder.name.setText(jo.get("name").getAsString());
            if(jo.get("icon").getAsString().equals("airline")) holder.icon.setImageResource(R.drawable.ic_airplane);
            else if(jo.get("icon").getAsString().equals("train")) holder.icon.setImageResource(R.drawable.ic_train);
        }

        @Override
        public int getItemCount() {
            return listRes.size();
        }

        class holder extends RecyclerView.ViewHolder {

            TextView name;
            ImageView image,icon;
            public holder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.name);
                image = itemView.findViewById(R.id.image);
                icon = itemView.findViewById(R.id.icon);
            }
        }
    }

    class routeAdapter extends RecyclerView.Adapter<routeAdapter.holder>{


        @NonNull
        @Override
        public holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.dynamic_yx_gts_route_item,parent,false);
            return new holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull holder holder, int position) {
            holder.text.setText(routeListRes.get(position).get("text").getAsString());
            if(routeListRes.get(position).get("icon").getAsString().equals("get_in")){
                holder.icon.setImageResource(R.drawable.ic_train);
                holder.icon.setImageTintList(ColorStateList.valueOf(getColorPrimary()));
            } else if(routeListRes.get(position).get("icon").getAsString().equals("get_off")){
                holder.icon.setImageResource(R.drawable.ic_train);
                holder.icon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(HContext,R.color.material_secondary_text)));
            }else if(routeListRes.get(position).get("icon").getAsString().equals("transfer")){
                holder.icon.setImageResource(R.drawable.ic_transfer);
                holder.icon.setImageTintList(ColorStateList.valueOf(getColorPrimary()));

            }else if(routeListRes.get(position).get("icon").getAsString().equals("walk")){
                holder.icon.setImageResource(R.drawable.ic_walk);
                holder.icon.setImageTintList(ColorStateList.valueOf(getColorPrimary()));

            }
        }

        @Override
        public int getItemCount() {
            return routeListRes.size();
        }

        class holder extends RecyclerView.ViewHolder{
            ImageView icon;
            TextView text;
            public holder(@NonNull View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.icon);
                text = itemView.findViewById(R.id.text);
            }
        }
    }
}
