package com.stupidtree.hita.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.LocationInfoListAdapter;
import com.stupidtree.hita.diy.MaterialCircleAnimator;
import com.stupidtree.hita.online.Canteen;
import com.stupidtree.hita.online.Classroom;
import com.stupidtree.hita.online.Facility;
import com.stupidtree.hita.online.Location;
import com.stupidtree.hita.online.LostAndFound;
import com.stupidtree.hita.online.Scenery;
import com.stupidtree.hita.util.ActivityUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

import static com.stupidtree.hita.HITAApplication.HContext;

public class ActivityLocation extends BaseActivity {

    Location location;
    TextView name,intro;
    RecyclerView infoList,lafList;
    Toolbar mToolbar;
    LocationInfoListAdapter infoListAdapter;
    lafListAdapter lafListAdapter;
    ArrayList<HashMap> infoListRes;
    ArrayList<LostAndFound> lafListRes;
    ImageView appbarBG;
    FloatingActionButton fab;
    //CollapsingToolbarLayout collapsingToolbarLayout;
    TextView rate;
    ImageView rateButton;

    @Override
    protected void stopTasks() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true,true,false);
        setContentView(R.layout.activity_location);
        initToolbar();
        initViews();
        initInfoList();
        initLafList();
        if(getIntent().getSerializableExtra("location")!=null){
            location = (Location) getIntent().getSerializableExtra("location");
            loadInfos();
            refresh();
        }else if(getIntent().getStringExtra("objectId")!=null){
            String id = getIntent().getStringExtra("objectId");
            BmobQuery<Location> bq = new BmobQuery();
            bq.addWhereEqualTo("objectId",id);
            bq.findObjects(new FindListener<Location>() {
                @Override
                public void done(List<Location> list, BmobException e) {
                    if(e!=null) Log.e("error",e.toString());
                    if(e==null&&list!=null&&list.size()>0){
                        if(list.get(0).getType().equals("canteen")){
                            Canteen c = new Canteen(list.get(0));
                            location = c;
                        }else if(list.get(0).getType().equals("scenery")){
                            Scenery c = new Scenery(list.get(0));
                            location = c;
                        }else if(list.get(0).getType().equals("classroom")){
                            Classroom c = new Classroom(list.get(0));
                            location = c;
                        }else if(list.get(0).getType().equals("facility")){
                            Facility c = new Facility(list.get(0));
                            location = c;
                        }else{
                            location = new Location();
                        }
                    }else {
                        Toast.makeText(HContext,"没有找到地点信息！",Toast.LENGTH_SHORT);
                        location = new Location();
                    }
                    loadInfos();
                    refresh();
                }
            });
        } else if(getIntent().getStringExtra("name")!=null){
            BmobQuery<Location> bq = new BmobQuery();
           // bq.find
            bq.addWhereEqualTo("name",getIntent().getStringExtra("name"));
            bq.findObjects(new FindListener<Location>() {
                @SuppressLint("ShowToast")
                @Override
                public void done(List<Location> list, BmobException e) {
                    if(e!=null) Log.e("error",e.toString());
                    if(e==null&&list!=null&&list.size()>0){
                        if(list.get(0).getType().equals("canteen")){
                            Canteen c = new Canteen(list.get(0));
                            location = c;
                        }else if(list.get(0).getType().equals("scenery")){
                            Scenery c = new Scenery(list.get(0));
                            location = c;
                        }else if(list.get(0).getType().equals("classroom")){
                            Classroom c = new Classroom(list.get(0));
                            location = c;
                        }else if(list.get(0).getType().equals("facility")){
                            Facility c = new Facility(list.get(0));
                            location = c;
                        }else{
                            location = new Location();
                        }
                    }else {
                        Toast.makeText(HContext,"没有找到地点信息！",Toast.LENGTH_SHORT);
                        location = new Location() ;
                    }

                    loadInfos();
                    refresh();
                }
            });
        }else{
            Toast.makeText(HContext,"没有找到地点信息！",Toast.LENGTH_SHORT).show();
            return;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(location!=null) refresh();
    }

    void initToolbar(){
        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
    void initViews(){
        rate = findViewById(R.id.location_rate);
        name = findViewById(R.id.name);
        intro = findViewById(R.id.intro);
        rateButton = findViewById(R.id.location_rate_button);
        appbarBG = findViewById(R.id.location_image);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(location==null) return;
                if(location.getLongitude()>0&&location.getLatitude()>0){
                    ActivityUtils.startExploreActivity_forNavi(ActivityLocation.this,location.getName(),location.getLongitude(),location.getLatitude());
                }else  ActivityUtils.startExploreActivity_forNavi(ActivityLocation.this,location.getName());
            }
        });
        rateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Location.showRateDialog(ActivityLocation.this, location, new SaveListener() {
                    @Override
                    public void done(Object o, BmobException e) {
                        Toast.makeText(HContext, "评分成功！", Toast.LENGTH_SHORT).show();
                        refresh();
                    }

                    @Override
                    public void done(Object o, Object o2) {
                        Toast.makeText(HContext, "评分成功！", Toast.LENGTH_SHORT).show();
                        refresh();
                    }
                });
            }
        });
        appbarBG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(location!=null&&location.getImageURL()!=null){
                    ActivityUtils.startPhotoDetailActivity_transition(ActivityLocation.this,location.getImageURL(),view);
                }
            }
        });
    }
    void loadInfos(){
        name.setText(location.getName());
        intro.setText(location.getPositionIntroduction());
       // collapsingToolbarLayout.setTitle(location.getName());
        mToolbar.setSubtitle(location.getPositionIntroduction());
        if(getIntent().getBooleanExtra("circle_reveal_image",true)){
            if(!this.isDestroyed()){
                Glide.with(this).load(location.getImageURL())
                        .centerCrop()
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                appbarBG.setImageDrawable(resource);
                                appbarBG.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        MaterialCircleAnimator.animShow(appbarBG,500);
                                    }
                                });
                                return true;
                            }
                        })
                        .into(appbarBG);
            }

        }else if(!this.isDestroyed()) Glide.with(this).load(location.getImageURL())
                .centerCrop().into(appbarBG);
//        Glide.with(this).load(location.getImageURL())
//                .centerCrop().into(appbarBG);
    }
    void initInfoList(){
        infoListRes = new ArrayList<>();
        infoList = findViewById(R.id.location_info_list);
//        if(getAction().getSerializableExtra("infos")!=null){
//            infoListRes.addAll((Collection<? extends Map>) getAction().getSerializableExtra("infos"));
//        }
        infoListAdapter = new LocationInfoListAdapter(this,infoListRes,getColorPrimary());
        LinearLayoutManager lm = new LinearLayoutManager(this,RecyclerView.VERTICAL,false);
        infoList.setAdapter(infoListAdapter);
        infoList.setLayoutManager(lm);
    }

    void initLafList(){
        lafListRes = new ArrayList<>();
        lafList = findViewById(R.id.location_laf_list);
        lafListAdapter = new lafListAdapter(lafListRes);
        lafList.setAdapter(lafListAdapter);
        lafList.setLayoutManager(new LinearLayoutManager(this,RecyclerView.HORIZONTAL,false));
    }

    void refresh(){
        DecimalFormat  df = new DecimalFormat("#0.00");
        infoListRes.clear();
        infoListRes.addAll(location.getInfoListArray());
        infoListAdapter.notifyDataSetChanged();
        rate.setText(df.format(location.getRate())+"/10");
        if(location!=null) new refreshLafTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    class lafListAdapter extends RecyclerView.Adapter<lafListAdapter.lafViewHolder>{

        List<LostAndFound> mBeans;

        public lafListAdapter(List<LostAndFound> mBeans) {
            this.mBeans = mBeans;
        }

        @NonNull
        @Override
        public lafViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new lafViewHolder(getLayoutInflater().inflate(R.layout.dynamic_location_laf,parent,false));
        }

        @Override
        public void onBindViewHolder(@NonNull lafViewHolder holder, final int position) {
            holder.time.setText(mBeans.get(position).getCreatedAt());
            holder.title.setText(mBeans.get(position).getTitle());
            holder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
               ActivityUtils.startPostDetailActivity(ActivityLocation.this,mBeans.get(position),mBeans.get(position).getAuthor());
                }
            });
        }

        @Override
        public int getItemCount() {
            return mBeans.size();
        }

        class lafViewHolder extends RecyclerView.ViewHolder{

            TextView title,time;
            CardView card;
            public lafViewHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.title);
                time = itemView.findViewById(R.id.time);
                card = itemView.findViewById(R.id.card);
            }
        }
    }

    class refreshLafTask extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] objects) {
            if(location!=null){
                BmobQuery<LostAndFound> bq = new BmobQuery();
                bq.addWhereEqualTo("location",location.getObjectId());
                List<LostAndFound> r = bq.findObjectsSync(LostAndFound.class);
                if(r!=null&&r.size()>0) {
                    lafListRes.clear();
                    lafListRes.addAll(r);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            lafListAdapter.notifyDataSetChanged();
        }
    }
}
