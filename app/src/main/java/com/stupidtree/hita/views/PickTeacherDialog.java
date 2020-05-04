package com.stupidtree.hita.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.fragments.search.FragmentSearchResult;
import com.stupidtree.hita.online.SearchException;
import com.stupidtree.hita.online.SearchTeacherCore;
import com.stupidtree.hita.online.Teacher;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;

public class PickTeacherDialog extends RoundedCornerDialog {
    public static List listRes;
    EditText searchview;
    RecyclerView list;
    TeacherSearchAdapter listAdapter;
    ProgressBar loading;
    getSuggestionsTask pageTask;
    OnPickListener onPickListener;
    ImageView done;
    TextView title;
    String titleStr;
    SearchTeacherCore searchTeacherCore;
    String initSearchValue = null;

    public PickTeacherDialog(@NonNull Context context, String title, OnPickListener onPickListener) {
        super(context);
        View v = getLayoutInflater().inflate(R.layout.dialog_pick_info, null, false);
        setView(v);
        this.titleStr = title;
        searchTeacherCore = new SearchTeacherCore();
        this.onPickListener = onPickListener;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initToolbar();
        initList();
    }

    public PickTeacherDialog setInitial(String text) {
        initSearchValue = text;
        return this;
    }

    @Override
    protected void onStop() {
        if (pageTask != null) pageTask.cancel(true);
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (searchview != null) {
            searchview.setText(initSearchValue);
//            if(pageTask!=null&&pageTask.getStatus()!=AsyncTask.Status.FINISHED){
//                pageTask.cancel(true);
//            }
//            pageTask =  new getSuggestionsTask(  searchview.getText().toString());
//            pageTask.executeOnExecutor(HITAApplication.TPE);

        }

    }

    void initToolbar() {
        loading = findViewById(R.id.loading);
        loading.setVisibility(View.GONE);
        searchview = findViewById(R.id.searchview);
        searchview.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int old = listRes.size();
                listRes.clear();
                listAdapter.notifyItemRangeRemoved(0, old);
                if (pageTask != null && pageTask.getStatus() != AsyncTask.Status.FINISHED) {
                    pageTask.cancel(true);
                }
                pageTask = new getSuggestionsTask(s.toString());
                pageTask.executeOnExecutor(HITAApplication.TPE);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    void initList() {
        listRes = new ArrayList<>();
        list = findViewById(R.id.list);
        done = findViewById(R.id.done);
        title = findViewById(R.id.title);
        title.setText(titleStr);
        listAdapter = new TeacherSearchAdapter(listRes);
        list.setAdapter(listAdapter);
        list.setLayoutManager(new WrapContentLinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        listAdapter.setOnItemClickListener(new FragmentSearchResult.OnItemClickListener() {
            @Override
            public void OnClick(View view, int position) {
                Object o = listRes.get(position);
                String name = "";
                if (o instanceof Teacher) {
                    name = ((Teacher) o).getName();
                } else if (o instanceof SparseArray) {
                    name = ((SparseArray<String>) o).get(0);
                }
                onPickListener.OnPick(name);
                dismiss();
            }


        });
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(searchview.getText().toString()))
                    Toast.makeText(getContext(), R.string.notifi_input_teacher, Toast.LENGTH_SHORT).show();
                else {
                    onPickListener.OnPick(searchview.getText().toString());
                    dismiss();
                }
            }
        });
    }

    public interface OnPickListener {
        void OnPick(String name);
    }

    class getSuggestionsTask extends AsyncTask {

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
            listRes.clear();
            if (TextUtils.isEmpty(s)) return null;
            try {
                listRes.addAll(searchTeacherCore.search(s, true));
                BmobQuery<Teacher> bq = new BmobQuery<>();
                bq.addWhereEqualTo("name", s);
                List<Teacher> res = bq.findObjectsSync(Teacher.class);
                listRes.addAll(res);
            } catch (SearchException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            try {
                loading.setVisibility(View.GONE);
                listAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class TeacherSearchAdapter extends RecyclerView.Adapter<TeacherSearchAdapter.TeacherSearchViewHoler> {
        List<Object> mBeans;
        FragmentSearchResult.OnItemClickListener onItemClickListener;

        public TeacherSearchAdapter(List<Object> mBeans) {
            this.mBeans = mBeans;
        }

        public void setOnItemClickListener(FragmentSearchResult.OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        @NonNull
        @Override
        public TeacherSearchViewHoler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int layoutId = R.layout.dynamic_teacher_search_result_item_mini;
            View v = getLayoutInflater().inflate(layoutId, parent, false);
            return new TeacherSearchViewHoler(v, viewType);
        }


        @SuppressLint("CheckResult")
        @Override
        public void onBindViewHolder(@NonNull final TeacherSearchAdapter.TeacherSearchViewHoler holder, final int position) {
            if (mBeans.get(position) instanceof SparseArray) {
                SparseArray<String> tsa = (SparseArray<String>) mBeans.get(position);
                holder.title.setText(tsa.get(0));
                holder.type.setVisibility(View.GONE);
                holder.department.setText(tsa.get(SearchTeacherCore.DEPARTMENT));
                Glide.with(getContext()).load("http://faculty.hitsz.edu.cn/file/showHP.do?d=" +
                        tsa.get(SearchTeacherCore.ID) + "&&w=200&&h=200&&prevfix=200-")
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.ic_account_activated)
                        .into(holder.picture);
                if (onItemClickListener != null)
                    holder.card.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onItemClickListener.OnClick(view, position);
                        }
                    });
            } else if (mBeans.get(position) instanceof Teacher) {
                Teacher t = (Teacher) mBeans.get(position);
                holder.title.setText(t.getName());
                holder.department.setText(t.getSchool());
                holder.type.setVisibility(View.VISIBLE);
                holder.type.setText(R.string.teacher_temp_label);
                Glide.with(getContext()).load(t.getPhotoLink())
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.ic_account_activated)
                        .into(holder.picture);
                if (onItemClickListener != null)
                    holder.card.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onItemClickListener.OnClick(view, position);
                        }
                    });
            }

        }


        @Override
        public int getItemCount() {
            return mBeans.size();
        }


        class TeacherSearchViewHoler extends RecyclerView.ViewHolder {
            TextView title;
            TextView department;
            ImageView picture;
            TextView type;
            CardView card;
            int viewType;

            public TeacherSearchViewHoler(@NonNull View itemView, int viewType) {
                super(itemView);
                this.viewType = viewType;
                card = itemView.findViewById(R.id.card);
                title = itemView.findViewById(R.id.name);
                type = itemView.findViewById(R.id.type);
                department = itemView.findViewById(R.id.department);
                picture = itemView.findViewById(R.id.picture);
            }
        }

    }

}
