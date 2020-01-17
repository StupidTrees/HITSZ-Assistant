package com.stupidtree.hita.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.timetable.Subject;
import com.stupidtree.hita.timetable.TimetableCore;
import com.stupidtree.hita.timetable.timetable.EventItem;
import com.stupidtree.hita.timetable.timetable.EventItemHolder;
import com.stupidtree.hita.util.ColorBox;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.ArrayList;

import static com.stupidtree.hita.HITAApplication.defaultSP;
import static com.stupidtree.hita.HITAApplication.mDBHelper;
import static com.stupidtree.hita.HITAApplication.now;
import static com.stupidtree.hita.HITAApplication.timeTableCore;


public class SubjectsListAdapter extends RecyclerView.Adapter {

    private static final int NORMAL = 967;
    private static final int MOOC = 731;
    private static final int TITLE = 971;
    LayoutInflater mInflater;
    ArrayList<Object> mBeans;
    OnItemClickListener mOnItemClickListener;
    private int columnNum;
    BaseFragment mFragment;
    private boolean colorfulMode = false;



    public SubjectsListAdapter(BaseFragment fragment, ArrayList<Object> subjets, int columnNum){
        mBeans = subjets;
        mInflater = LayoutInflater.from(fragment.getContext());
        this.mFragment = fragment;
        this.columnNum = columnNum;

    }


    public void setColorfulMode(boolean colorfulMode) {
        this.colorfulMode = colorfulMode;
    }

    public interface OnItemClickListener{
        void onClick(View view,int position);
    }
    public void setmOnItemClickListener(OnItemClickListener x){
        mOnItemClickListener = x;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v;
        if(i==TITLE) v = mInflater.inflate(R.layout.dynamic_subject_list_title,viewGroup,false);
        else if(i==MOOC)v = mInflater.inflate(R.layout.dynamic_subjects_mooc_item,viewGroup,false);
        else  v = mInflater.inflate(R.layout.dynamic_subjects_item,viewGroup,false);
        return new SubjectViewHolder(v);
    }

    @Override
    public int getItemViewType(int position) {
        Object o = mBeans.get(position);
        if(o instanceof String) return TITLE;
        else return ((Subject)o).isMOOC()?MOOC:NORMAL;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
        if(mBeans.get(i) instanceof Subject){
            Subject s = (Subject) mBeans.get(i);
            SubjectViewHolder subjectViewHolder = (SubjectViewHolder) viewHolder;
            subjectViewHolder.name.setText(s.getName());
            subjectViewHolder.code.setText(s.getCode());
            if(subjectViewHolder.progressBar!=null)new CalcProgressTask(subjectViewHolder.progress,subjectViewHolder.progressBar,s,subjectViewHolder.icon).executeOnExecutor(HITAApplication.TPE);
            if(mOnItemClickListener!=null){
                subjectViewHolder.card.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnItemClickListener.onClick(v,i);
                    }
                });
            }
        }else if(viewHolder instanceof SubjectViewHolder){
            SubjectViewHolder subjectViewHolder = (SubjectViewHolder) viewHolder;
            subjectViewHolder.name.setText(mBeans.get(i).toString());
//            String hint;
//            if(mBeans.get(i).equals("考试课")) hint = "计入学分绩";
//            else if(mBeans.get(i).equals("考查课")) hint = "要求通过";
//            else hint = "凑学分的";
//            subjectViewHolder.code.setText(hint);
        }

    }


    //判断是否是title，如果是，title占满一行的所有子项，则是ColumnNum个，如果是item，占满一个子项
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        //如果是title就占据2个单元格(重点)
        GridLayoutManager manager = (GridLayoutManager) recyclerView.getLayoutManager();
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if(mBeans.get(position) instanceof String||mBeans.get(position) instanceof Integer){
                    return columnNum;
                }else {
                    return 1;
                }
            }
        });
    }

    public ArrayList<Object> getList(){
        return mBeans;
    }
    @Override
    public int getItemCount() {
        return mBeans.size();
    }

    class SubjectViewHolder extends RecyclerView.ViewHolder{

        TextView name,progress,code;
        ImageView icon;
        ProgressBar progressBar;
        CardView card;//,label;
        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.usercenter_subjectitem_name);
            progress = itemView.findViewById(R.id.usercenter_subjectitem_progress);
            card = itemView.findViewById(R.id.usercenter_subjects_card);
            progressBar = itemView.findViewById(R.id.usercenter_subjectitem_progressBar);
            code = itemView.findViewById(R.id.usercenter_subjectitem_code);
           icon = itemView.findViewById(R.id.usercenter_subject_item_label);
        }
    }


    class CalcProgressTask extends  AsyncTask<String,Integer,Integer>{

        Subject subject;
        TextView tv;
        ProgressBar pb;
        ImageView icon;
        int color;
        CalcProgressTask(TextView tv,ProgressBar pb,Subject sb,ImageView icon){
            this.subject = sb;
            this.icon = icon;
            this.pb = pb;
            this.tv = tv;
        }

        @Override
        protected Integer doInBackground(String... strings) {
            int finished=0,unfinished=0;
            ArrayList<EventItem> result = new ArrayList<>();
            SQLiteDatabase sd = mDBHelper.getReadableDatabase();
            Cursor c = sd.query("timetable",null,"name=? and type=?",
                    new String[]{subject.getName(), TimetableCore.TIMETABLE_EVENT_TYPE_COURSE+""},null,null,null);
            if(colorfulMode){
                color = defaultSP.getInt("color:"+subject.getName(), Color.parseColor("#00000000"));
            }else color = -1;

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

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            pb.setProgress(integer);
            tv.setText(integer+"%");
            if(color!=-1) icon.setColorFilter(color);
            else icon.clearColorFilter();
        }
    }

}
