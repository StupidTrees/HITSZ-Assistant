package com.stupidtree.hita.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.core.Curriculum;

import java.util.List;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.deleteCurriculum;

public class CurriculumManagerAdapter extends RecyclerView.Adapter<CurriculumManagerAdapter.CMViewHolder> {

    List<Curriculum> mBeans;
    LayoutInflater mInflater;
    Context mContext;


    public CurriculumManagerAdapter(Context c, List<Curriculum> res){
        mInflater = LayoutInflater.from(c);
        mBeans = res;
        mContext = c;
    }

    @NonNull
    @Override
    public CMViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = mInflater.inflate(R.layout.dynamic_curriculum_manager,viewGroup,false);
        return new CMViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CMViewHolder cmViewHolder, final int i) {
        final String name = mBeans.get(i).name;
        if(name.indexOf("(")>0)cmViewHolder.name.setText(name.substring(0,name.indexOf("(")));
        else  cmViewHolder.name.setText(name);
        cmViewHolder.from.setText("开始于"+mBeans.get(i).readStartDate());
        cmViewHolder.totalWeeks.setText("共"+mBeans.get(i).totalWeeks+"周");
        if(name.contains("春")) cmViewHolder.image.setImageResource(R.drawable.ic_spring);
        else if(name.contains("夏")) cmViewHolder.image.setImageResource(R.drawable.ic_summer);
        else if(name.contains("秋")) cmViewHolder.image.setImageResource(R.drawable.ic_autumn);
        else if(name.contains("冬")) cmViewHolder.image.setImageResource(R.drawable.ic_winter);
        else  cmViewHolder.image.setImageResource(R.drawable.ic_menu_jwts);
        cmViewHolder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu pm = new PopupMenu(mContext,v);
                pm.getMenuInflater().inflate(R.menu.menu_opr_curriculum,pm.getMenu());
                pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId()==R.id.curriculum_opr_detail){
                            AlertDialog ad = new AlertDialog.Builder(mContext).setTitle("课表详情").
                                    setMessage("课表名称："+name+"\n课表代码："+mBeans.get(i).curriculumCode).create();
                            ad.show();
                            return true;
                        }else if(item.getItemId()==R.id.curriculum_opr_delete){
                            AlertDialog ad = new AlertDialog.Builder(mContext).setTitle("提示").setMessage("删除课表将删除一切与之关联的事件、科目、任务，确定吗？").
                                    setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                           new deleteTask(i).executeOnExecutor(HITAApplication.TPE);
                                        }
                                    }).setNegativeButton("取消",null).
                                    create();
                            ad.show();
                            return true;
                        }
                        return false;
                    }
                });
                pm.show();
            }
        });
        cmViewHolder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog ad = new AlertDialog.Builder(mContext).setTitle("课表详情").
                        setMessage("课表名称："+name+"\n课表代码："+mBeans.get(i).curriculumCode).create();
                ad.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mBeans.size();
    }

    class CMViewHolder extends RecyclerView.ViewHolder{

        TextView name;
        TextView from;
        TextView totalWeeks;
        ImageView image,more;
        CardView card;
        public CMViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.cm_name);
            from = itemView.findViewById(R.id.cm_from);
            totalWeeks = itemView.findViewById(R.id.cm_totalweek);
            image = itemView.findViewById(R.id.cm_image);
            more = itemView.findViewById(R.id.cm_more);
            card = itemView.findViewById(R.id.card);
        }
    }

    class deleteTask extends AsyncTask{

        int position;
        deleteTask(int pos){
            position = pos;
        }
        @Override
        protected Object doInBackground(Object[] objects) {
            return deleteCurriculum(position);
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            notifyDataSetChanged();
            if((Boolean)o){
                Toast.makeText(HContext,"删除成功！",Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(HContext,"删除失败！",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
