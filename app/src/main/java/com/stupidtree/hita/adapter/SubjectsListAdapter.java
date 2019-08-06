package com.stupidtree.hita.adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.stupidtree.hita.R;
import com.stupidtree.hita.core.Subject;
import com.stupidtree.hita.core.TimeTable;
import com.stupidtree.hita.core.timetable.EventItem;
import com.stupidtree.hita.core.timetable.EventItemHolder;

import java.util.ArrayList;

import static com.stupidtree.hita.HITAApplication.mDBHelper;
import static com.stupidtree.hita.HITAApplication.now;


public class SubjectsListAdapter extends RecyclerView.Adapter<SubjectsListAdapter.SubjectViewHolder> {

    private static final int NORMAL = 967;
    private static final int MOOC = 731;
    LayoutInflater mInflater;
    ArrayList<Subject> mBeans;
    OnItemClickListener mOnItemClickListener;



    public SubjectsListAdapter(Context context, ArrayList<Subject> subjets){
        mBeans = subjets;
        mInflater = LayoutInflater.from(context);
    }
    public interface OnItemClickListener{
        void onClick(View view,int position);
    }
    public void setmOnItemClickListener(OnItemClickListener x){
        mOnItemClickListener = x;
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v;
        if(i==MOOC)v = mInflater.inflate(R.layout.dynamic_usercenter_subjects_mooc_item,viewGroup,false);
        else  v = mInflater.inflate(R.layout.dynamic_usercenter_subjects_item,viewGroup,false);
        return new SubjectViewHolder(v);
    }

    @Override
    public int getItemViewType(int position) {
        return mBeans.get(position).isMOOC?MOOC:NORMAL;
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder subjectViewHolder, final int i) {
        subjectViewHolder.name.setText(mBeans.get(i).name);
        subjectViewHolder.code.setText(mBeans.get(i).code);
        //int colorPos = i>=colors.length?colors.length-(i%colors.length)-1:i;
       //subjectViewHolder.image.setColorFilter(colors[colorPos]);
        //subjectViewHolder.label.setCardBackgroundColor(colors[colorPos]);
        if(subjectViewHolder.progressBar!=null)new CalcProgressTask(subjectViewHolder.progress,subjectViewHolder.progressBar,mBeans.get(i)).execute();
        if(mOnItemClickListener!=null){
            subjectViewHolder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onClick(v,i);
                }
            });
        }
    }
    public ArrayList<Subject> getList(){
        return mBeans;
    }
    @Override
    public int getItemCount() {
        return mBeans.size();
    }

    class SubjectViewHolder extends RecyclerView.ViewHolder{

        TextView name,progress,code;
        ProgressBar progressBar;
        CardView card;//,label;
        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.usercenter_subjectitem_name);
            progress = itemView.findViewById(R.id.usercenter_subjectitem_progress);
            card = itemView.findViewById(R.id.usercenter_subjects_card);
            progressBar = itemView.findViewById(R.id.usercenter_subjectitem_progressBar);
            code = itemView.findViewById(R.id.usercenter_subjectitem_code);
            //label = itemView.findViewById(R.id.usercenter_subject_item_label);
        }
    }


    class CalcProgressTask extends  AsyncTask<String,Integer,Integer>{

        Subject subject;
        TextView tv;
        ProgressBar pb;
        CalcProgressTask(TextView tv,ProgressBar pb,Subject sb){
            this.subject = sb;
            this.pb = pb;
            this.tv = tv;
        }

        @Override
        protected Integer doInBackground(String... strings) {
            int finished=0,unfinished=0;
            ArrayList<EventItem> result = new ArrayList<>();
            SQLiteDatabase sd = mDBHelper.getReadableDatabase();
            Cursor c = sd.query("timetable",null,"name=? and type=?",
                    new String[]{subject.name, TimeTable.TIMETABLE_EVENT_TYPE_COURSE+""},null,null,null);
            while (c.moveToNext()){
                EventItemHolder eih = new EventItemHolder(c);
                result.addAll(eih.getAllEvents());
            }
            for(EventItem ei:result){
                if(ei.hasPassed(now)) finished++;
                else unfinished++;
            }
            float x = ((float) finished)*100.0f/(float)(finished+unfinished);
            return (int) x;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pb.setMax(100);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            pb.setProgress(integer);
            tv.setText(integer+"%");
        }
    }
}
