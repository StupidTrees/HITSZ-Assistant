package com.stupidtree.hita.views;

import android.annotation.SuppressLint;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cncoderx.wheelview.OnWheelChangedListener;
import com.cncoderx.wheelview.WheelView;
import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.BaseActivity;
import com.stupidtree.hita.timetable.TimetableCore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.themeCore;


public class PickCourseTimeDialog extends RoundedCornerDialog {

    private int dow,begin,end;
    BaseActivity context;
    private boolean timeSet = false;
    private ImageView done;
    private onDialogConformListener mOnDialogConformListener;
    private mWheel3DView pickDow, pickFromT, pickToT;
    private List<Boolean> weeks;
    private RecyclerView list;
    private pickWeekListAdapter listAdapter;
    private boolean hasInit = false;
    public interface onDialogConformListener{
        void onClick(List<Integer> weeks,int dow,int begin,int last);
    }
    public PickCourseTimeDialog(BaseActivity context, onDialogConformListener onDialogConformListener){
        super(context);
        mOnDialogConformListener = onDialogConformListener;
        this.context = context;
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(context,themeCore.getCurrentThemeID());// your app theme here
        View view = getLayoutInflater().cloneInContext(contextThemeWrapper).inflate(R.layout.dialog_pick_course_time,null,false);
        setView(view);
        initViews(view);
        initList(view);
    }

    public void setInitialValue(List<Integer> weeks,int dow,int begin,int last){
        if(weeks.size()>0){
            int max = weeks.get(0);
            for(int i:weeks) if(i>max) max = i;
            this.weeks.clear();
            int realMax = TimetableCore.getInstance(HContext).getCurrentCurriculum().getTotalWeeks() > max ? TimetableCore.getInstance(HContext).getCurrentCurriculum().getTotalWeeks() : max;
            for(int i = 1;i<=realMax;i++){
                this.weeks.add(weeks.contains(i));
            }
        }
        hasInit = true;
        this.begin = begin;
        this.end = begin+last-1;
        //Log.e("begun-end",this.begin+","+end);
        this.dow = dow;
    }

    public void setOnDialogConformListener(onDialogConformListener m) {
        this.mOnDialogConformListener = m;
    }




    void initViews(View view){
        done = view.findViewById(R.id.done);
        pickDow = view.findViewById(R.id.pickdow);
        pickFromT = view.findViewById(R.id.pickfromt);
        pickToT = view.findViewById(R.id.picktot);
        pickDow.setEntries(context.getResources().getStringArray(R.array.dow2));
        pickDow.setCyclic(false);
        String[] times = new String[12];
        String periodTemp = context.getString(R.string.period);
        for (int i = 0; i < 12; i++) {
            times[i] = String.format(periodTemp,i+1);
        }
        pickFromT.setEntries(times);
        pickToT.setEntries(times);
        pickFromT.setCyclic(false);
        pickToT.setCyclic(false);
        pickDow.setOnWheelChangedListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView view, int oldIndex, int newIndex) {
                dow = newIndex+1;
            }
        });
        pickFromT.setOnWheelChangedListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView view, int oldIndex, int newIndex) {
                begin = newIndex+1;
                if(pickToT.getCurrentIndex()<newIndex) pickToT.setCurrentIndex(newIndex);
            }
        });
        pickToT.setOnWheelChangedListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView view, int oldIndex, int newIndex) {
                end = newIndex+1;
                if(pickFromT.getCurrentIndex()>newIndex) pickFromT.setCurrentIndex(newIndex);
            }
        });
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeSet = true;
                List<Integer> finalWeek = new ArrayList<>();
                for(int i=0;i<weeks.size();i++){
                    if(weeks.get(i)) finalWeek.add(i+1);
                }
                if (finalWeek.size() == 0) {
                    Toast.makeText(getContext(), R.string.notifi_atleast_oneweek, Toast.LENGTH_SHORT).show();
                    return;
                }
                mOnDialogConformListener.onClick(finalWeek,dow,begin,end-begin+1);
                dismiss();
            }
        });
    }

    void initList(View v){
        list = v.findViewById(R.id.weekList);
        weeks = new ArrayList<>();
        for (int i = 0; i < TimetableCore.getInstance(HContext).getCurrentCurriculum().getTotalWeeks() + 1; i++)
            weeks.add(false);
        listAdapter = new pickWeekListAdapter();
        list.setAdapter(listAdapter);
        list.setLayoutManager(new GridLayoutManager(getContext(),5));
    }
    @Override
    protected void onStart() {
        super.onStart();
        if(!hasInit){
            int tempDOW = TimetableCore.getNow().get(Calendar.DAY_OF_WEEK);
            dow = tempDOW==1?7:tempDOW-1;
            begin = 1;
            end = 2;
        }
        pickDow.setCurrentIndex(dow-1);
        pickToT.setCurrentIndex(end-1);
        pickFromT.setCurrentIndex(begin-1);
        timeSet = true;
    }

    class pickWeekListAdapter extends RecyclerView.Adapter<pickWeekListAdapter.mViewHolder>{

        @NonNull
        @Override
        public mViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.dynamic_pick_week_item,parent,false);
            return new mViewHolder(v);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull final mViewHolder holder, final int position) {
            holder.text.setText((position+1)+"");
            if (position == weeks.size()) {
                holder.card.setCardBackgroundColor(ContextCompat.getColor(HContext,R.color.color_control_normal));
                holder.text.setTextColor(ContextCompat.getColor(HContext,R.color.text_color_secondary));
                holder.text.setText("＋");
                holder.card.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        for(int i=0;i<5;i++) {
                            weeks.add(false);
                            notifyItemChanged(weeks.size()-2);
                            notifyItemInserted(weeks.size()-1);
                        }

                    }
                });
            }else{
                if(!weeks.get(position)) {
                    holder.card.setCardBackgroundColor(ContextCompat.getColor(HContext,R.color.color_control_normal));
                    holder.text.setTextColor(ContextCompat.getColor(HContext,R.color.text_color_secondary));
                }else {
                    holder.card.setCardBackgroundColor(context.getColorAccent());
                    holder.text.setTextColor(context.getColorAccent());
                }
            holder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    weeks.set(position,!weeks.get(position));
                    notifyItemChanged(position);
                }
            });
            }
        }

        @Override
        public int getItemCount() {
            return weeks.size() + 1;
        }

        class mViewHolder extends RecyclerView.ViewHolder{
            CardView card;
            TextView text;
            public mViewHolder(@NonNull View itemView) {
                super(itemView);
                card = itemView.findViewById(R.id.card);
                text = itemView.findViewById(R.id.text);
            }
        }
    }


}
