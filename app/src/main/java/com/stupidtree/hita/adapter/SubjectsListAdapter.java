package com.stupidtree.hita.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
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

import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivityTimeTable;
import com.stupidtree.hita.core.Curriculum;
import com.stupidtree.hita.core.Subject;
import com.stupidtree.hita.core.TimeTable;
import com.stupidtree.hita.core.timetable.EventItem;
import com.stupidtree.hita.core.timetable.EventItemHolder;
import com.stupidtree.hita.fragments.FragmentSubjects;
import com.stupidtree.hita.fragments.FragmentTimeLine;
import com.stupidtree.hita.util.ColorBox;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.ArrayList;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.defaultSP;
import static com.stupidtree.hita.HITAApplication.mDBHelper;
import static com.stupidtree.hita.HITAApplication.mainTimeTable;
import static com.stupidtree.hita.HITAApplication.now;


public class SubjectsListAdapter extends RecyclerView.Adapter {

    private static final int NORMAL = 967;
    private static final int MOOC = 731;
    private static final int TITLE = 971;
    private static final int FOOT = 617;
    LayoutInflater mInflater;
    ArrayList<Object> mBeans;
    OnItemClickListener mOnItemClickListener;
    private int columnNum;
    BaseFragment mFragment;



    public SubjectsListAdapter(BaseFragment fragment, ArrayList<Object> subjets, int columnNum){
        mBeans = subjets;
        mInflater = LayoutInflater.from(fragment.getContext());
        this.mFragment = fragment;
        this.columnNum = columnNum;
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
        else if(i==FOOT) return new FootViewHolder(mInflater.inflate(R.layout.dynamic_subject_foot,viewGroup,false));
        else  v = mInflater.inflate(R.layout.dynamic_subjects_item,viewGroup,false);
        return new SubjectViewHolder(v);
    }

    @Override
    public int getItemViewType(int position) {
        Object o = mBeans.get(position);
        if(o instanceof String) return TITLE;
        else if(o instanceof Integer) return FOOT;
        else return ((Subject)o).isMOOC?MOOC:NORMAL;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
        if(mBeans.get(i) instanceof Subject){
            Subject s = (Subject) mBeans.get(i);
            SubjectViewHolder subjectViewHolder = (SubjectViewHolder) viewHolder;
            subjectViewHolder.name.setText(s.name);
            subjectViewHolder.code.setText(s.code);
            //int colorPos = i>=colors.length?colors.length-(i%colors.length)-1:i;
            //subjectViewHolder.image.setColorFilter(colors[colorPos]);
            //subjectViewHolder.label.setCardBackgroundColor(colors[colorPos]);
            if(subjectViewHolder.progressBar!=null)new CalcProgressTask(subjectViewHolder.progress,subjectViewHolder.progressBar,s,subjectViewHolder.icon).execute();
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
        }else{
            final FootViewHolder fvh = (FootViewHolder) viewHolder;
            fvh.resetColors.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog ad = new AlertDialog.Builder(mFragment.getContext()).setTitle("是否随机生成各科目颜色？")
                            .setNegativeButton("取消",null).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new  resetColorTask().execute();
                                }
                            }).create();
                    ad.show();
                }
            });
            fvh.resetColorsToTheme.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog ad = new AlertDialog.Builder(mFragment.getContext()).setTitle("是否将各科目颜色设置为主题色？")
                            .setNegativeButton("取消",null).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new resetColorToThemeTask().execute();
                                }
                            }).create();
                    ad.show();
                }
            });
            boolean color = defaultSP.getBoolean("timetable_colorful_mode",true);
            fvh.enable_color.setChecked(color);
            if(color)fvh. expandableLayout.expand();
            else  fvh.expandableLayout.collapse();

            fvh.enable_color.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                    if(isChecked)fvh.expandableLayout.expand();
                    else  fvh.expandableLayout.collapse();
                    defaultSP.edit().putBoolean("timetable_colorful_mode",isChecked).commit();
                    notifyDataSetChanged();

                }
            });
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
    class FootViewHolder extends RecyclerView.ViewHolder{
        ImageView resetColors,resetColorsToTheme;
        ExpandableLayout expandableLayout;
        Switch enable_color;

        public FootViewHolder(@NonNull View v) {
            super(v);
            enable_color = v.findViewById(R.id.enable_color);
            expandableLayout = v.findViewById(R.id.expandable);
            resetColorsToTheme = v.findViewById(R.id.reset_colors_to_theme);
            resetColors = v.findViewById(R.id.reset_colors);
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
                    new String[]{subject.name, TimeTable.TIMETABLE_EVENT_TYPE_COURSE+""},null,null,null);
            if(defaultSP.getBoolean("timetable_colorful_mode",true)){
                color = defaultSP.getInt("color:"+subject.name, Color.parseColor("#00000000"));
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

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            pb.setProgress(integer);
            tv.setText(integer+"%");
            if(color!=-1) icon.setColorFilter(color);
        }
    }
    class resetColorToThemeTask extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] objects) {
            for(Subject s:mainTimeTable.core.getSubjects()){
                defaultSP.edit().putInt("color:"+s.name,mFragment.getColorPrimary()).commit();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
             notifyDataSetChanged();
            super.onPostExecute(o);
        }
    }
    class resetColorTask extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] objects) {
            for(Subject s:mainTimeTable.core.getSubjects()){
                defaultSP.edit().putInt("color:"+s.name, ColorBox.getRandomColor_Material()).commit();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            notifyDataSetChanged();
            super.onPostExecute(o);
        }
    }
}
