package com.stupidtree.hita.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivityLocation;
import com.stupidtree.hita.diy.WrapContentLinearLayoutManager;
import com.stupidtree.hita.online.LostAndFound;
import com.stupidtree.hita.util.ActivityUtils;

import org.w3c.dom.Text;

import java.sql.Ref;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.bmob.v3.BmobQuery;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.TPE;


public class FragmentUserCenter_ut extends BaseFragment {
    RecyclerView lafList;
    lafListAdapter lafListAdapter;
    ArrayList<LostAndFound> lafListRes;
    refreshLafTask pageTask;
    TextView punch_title,happy_days,normal_days,sad_days;

    public static FragmentUserCenter_ut newInstance(){
        FragmentUserCenter_ut r = new FragmentUserCenter_ut();
        return r;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_usercenter_ut,container,false);
        initLafList(v);
        initViews(v);
        return v;
    }

    void initViews(View v){
        punch_title = v.findViewById(R.id.punch_title);
        happy_days = v.findViewById(R.id.happy_text);
        normal_days = v.findViewById(R.id.normal_text);
        sad_days = v.findViewById(R.id.sad_text);
    }



    @Override
    protected void stopTasks() {
        if(pageTask!=null&&pageTask.getStatus()!= AsyncTask.Status.FINISHED) pageTask.cancel(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        Refresh();
    }

    @Override
    public void Refresh() {
        if(pageTask!=null&&pageTask.getStatus()!= AsyncTask.Status.FINISHED) pageTask.cancel(true);
        pageTask =  new refreshLafTask();
        pageTask.executeOnExecutor(TPE);
    }

    void initLafList(View v){
        lafListRes = new ArrayList<>();
        lafList = v.findViewById(R.id.laf_list);
        lafListAdapter = new lafListAdapter(lafListRes);
        lafList.setAdapter(lafListAdapter);
        lafList.setLayoutManager(new WrapContentLinearLayoutManager(getContext(),RecyclerView.HORIZONTAL,false));
    }


    class lafListAdapter extends RecyclerView.Adapter<lafListAdapter.lafViewHolder>{

        List<LostAndFound> mBeans;

        public lafListAdapter(List<LostAndFound> mBeans) {
            this.mBeans = mBeans;
        }

        @NonNull
        @Override
        public lafListAdapter.lafViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new lafListAdapter.lafViewHolder(getLayoutInflater().inflate(R.layout.dynamic_user_laf,parent,false));
        }

        @Override
        public void onBindViewHolder(@NonNull lafListAdapter.lafViewHolder holder, final int position) {
            holder.time.setText(mBeans.get(position).getCreatedAt());
            holder.title.setText(mBeans.get(position).getTitle());
            holder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityUtils.startPostDetailActivity(getActivity(),mBeans.get(position),mBeans.get(position).getAuthor());
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
            try {
                if(CurrentUser!=null){
                    BmobQuery<LostAndFound> bq = new BmobQuery();
                    bq.addWhereEqualTo("author",CurrentUser.getObjectId());
                    List<LostAndFound> r = bq.findObjectsSync(LostAndFound.class);
                    if(r!=null&&r.size()>0) {
                        lafListRes.clear();
                        lafListRes.addAll(r);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            try {
                lafListAdapter.notifyDataSetChanged();
                punch_title.setText(String.format(getString(R.string.user_center_you_have_punched),CurrentUser.getPunchDays()));
                happy_days.setText(CurrentUser.getHappyDays()+"");
                normal_days.setText(CurrentUser.getNormalDays()+"");
                sad_days.setText(CurrentUser.getSadDays()+"");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}