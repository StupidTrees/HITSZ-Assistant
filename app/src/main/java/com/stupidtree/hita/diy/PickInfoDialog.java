package com.stupidtree.hita.diy;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivitySearch;
import com.stupidtree.hita.adapter.SearchResultAdapter;
import com.stupidtree.hita.core.timetable.EventItem;
import com.stupidtree.hita.core.timetable.Task;
import com.stupidtree.hita.fragments.FragmentTimeLine;
import com.stupidtree.hita.hita.TextTools;
import com.stupidtree.hita.online.Location;
import com.stupidtree.hita.online.Teacher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;

import static com.stupidtree.hita.HITAApplication.mainTimeTable;
import static com.stupidtree.hita.HITAApplication.searchText;
import static com.stupidtree.hita.adapter.NewsIpNewsListAdapter.dip2px;

public class PickInfoDialog extends AlertDialog {
    EditText searchview;
    RecyclerView list;
    SearchResultAdapter listAdapter;
    ProgressBar loading;
    getSuggestionsTask pageTask;
    int mode;
    OnPickListener onPickListener;
    ImageView done;
    TextView title;
    String titleStr;
    public static List<BmobObject> listRes;
    public static final int TEACHER = 378;
    public static final int LOCATION_ALL = 875;
    public static final int ALL = 327;

    public interface OnPickListener{
        void OnPick(String title,Object obj);
    }
    
    public PickInfoDialog(@NonNull Context context,String title,int mode,OnPickListener onPickListener) {
        super(context);
        View v = getLayoutInflater().inflate(R.layout.dialog_pick_info,null,false);
        setView(v);
        this.titleStr = title;
        this.mode = mode;
        this.onPickListener = onPickListener;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().
                setLayout(dip2px(getContext(), 320), LinearLayout.LayoutParams.WRAP_CONTENT);
        getWindow().
                setBackgroundDrawableResource(R.drawable.dialog_background_radius);
        initToolbar();
        initList();
    }

    @Override
    protected void onStop() {
        if(pageTask!=null) pageTask.cancel(true);
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(searchview!=null){
            if(pageTask!=null&&pageTask.getStatus()!=AsyncTask.Status.FINISHED){
                pageTask.cancel(true);
            }
            pageTask =  new getSuggestionsTask(  searchview.getText().toString());
            pageTask.executeOnExecutor(HITAApplication.TPE);

        }

    }

    void initToolbar(){
        loading = findViewById(R.id.loading);
        loading.setVisibility(View.GONE);
        searchview = findViewById(R.id.searchview);
        searchview.setText(searchText);
        searchview.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int old =  listRes.size();
                searchText = s.toString();
                listRes.clear();
                listAdapter.notifyItemRangeRemoved(0, old);
                if(pageTask!=null&&pageTask.getStatus()!=AsyncTask.Status.FINISHED){
                    pageTask.cancel(true);
                }
                pageTask =  new getSuggestionsTask(s.toString());
                pageTask.executeOnExecutor(HITAApplication.TPE);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    void initList(){
        listRes = new ArrayList<>();
        list = findViewById(R.id.list);
        done = findViewById(R.id.done);
        title = findViewById(R.id.title);
        title.setText(titleStr);
        listAdapter = new SearchResultAdapter(this.getContext(), listRes);
        list.setAdapter(listAdapter);
        list.setLayoutManager(new WrapContentLinearLayoutManager(getContext(),RecyclerView.VERTICAL,false));
        listAdapter.setOnItemClickListener(new SearchResultAdapter.OnItemClickListener() {
            @Override
            public void Onlick(View v, int position) {
                String name;
                if(listRes.get(position) instanceof Location){
                    name = ((Location) listRes.get(position)).getName();
                }else{
                    name = ((Teacher)listRes.get(position)).getName();
                }
                onPickListener.OnPick(name,listRes.get(position));
                dismiss();
            }
        });
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(searchview.getText().toString())) Toast.makeText(getContext(),"输入地点！",Toast.LENGTH_SHORT).show();
                else{
                    onPickListener.OnPick(searchview.getText().toString(),null);
                    dismiss();
                }
            }
        });
    }

    class getSuggestionsTask extends AsyncTask{

        String s;

        public getSuggestionsTask(String s) {
            this.s = s;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading.setVisibility(View.VISIBLE);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            List<BmobObject> result = new ArrayList<>();
            String toResearch = s.replaceAll("\n","").replaceAll(" ","");
            if(!TextUtils.isEmpty(toResearch)){
                if(mode==ALL||mode==TEACHER){
                    try {
                        BmobQuery<Teacher> bq2 = new BmobQuery<>();
                        bq2.addWhereContains("name",toResearch);
                        List<Teacher> foundTeachers = bq2.findObjectsSync(Teacher.class);
                        result.addAll(foundTeachers);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if(mode==ALL||mode==LOCATION_ALL){
                    try {
                        BmobQuery<Location> bq = new BmobQuery<>();
                        bq.addWhereContains("name",toResearch);
                        List<Location> foundLocations = bq.findObjectsSync(Location.class);
                        result.addAll(foundLocations);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            List<BmobObject> result = (List<BmobObject>) o;
            try {
                loading.setVisibility(View.GONE);
                listRes.addAll(result);
                listAdapter.notifyItemRangeInserted(0,result.size());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
