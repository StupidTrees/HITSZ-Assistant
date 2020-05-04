package com.stupidtree.hita.community;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.stupidtree.hita.R;
import com.stupidtree.hita.fragments.popup.FragmentRadiusPopup;
import com.stupidtree.hita.online.Topic;
import com.stupidtree.hita.views.WrapContentLinearLayoutManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cn.bmob.v3.BmobQuery;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.TPE;

@SuppressLint("ValidFragment")
public class FragmentTopicsPopup extends FragmentRadiusPopup {

    private RecyclerView list;
    private boolean first = true;
    private ProgressBar loading;
    private OnPickListener listener;
    private TopicAdapter listAdapter;
    private List<Topic> listRes;

    public FragmentTopicsPopup() {

    }

    public FragmentTopicsPopup(OnPickListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = View.inflate(getContext(), R.layout.fragment_topics_popup, null);
        first = true;
        initList(view);
        return view;
    }

    void initList(View v) {
        loading = v.findViewById(R.id.loading);
        list = v.findViewById(R.id.list);
        listRes = new ArrayList<>();
        listAdapter = new TopicAdapter();
        list.setAdapter(listAdapter);
        list.setLayoutManager(new WrapContentLinearLayoutManager(getContext()));
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        new refreshTask().executeOnExecutor(TPE);
        if (first) first = false;
    }

    interface OnPickListener {
        void onPick(Topic topic);
    }

    class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.THolder> {


        @NonNull
        @Override
        public THolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.dynamic_topic_item_mini, parent, false);
            return new THolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull final THolder holder, int position) {
            final Topic t = listRes.get(position);
            holder.title.setText(t.getName());
            if (TextUtils.isEmpty(t.getDescription())) holder.subtitle.setVisibility(View.GONE);
            else {
                holder.subtitle.setVisibility(View.VISIBLE);
                holder.subtitle.setText(t.getDescription());
            }
            if (t.getType().equals("basic")) holder.title.setTypeface(Typeface.DEFAULT_BOLD);
            else holder.title.setTypeface(Typeface.DEFAULT);
            Glide.with(getContext()).load(t.getCover())
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.ic_topic_gradient)
                    .into(holder.image);
            holder.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onPick(t);
                    dismiss();
                }
            });


        }

        @Override
        public int getItemCount() {
            return listRes.size();
        }

        class THolder extends RecyclerView.ViewHolder {
            TextView title, subtitle;
            ImageView image;
            View item;

            THolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.title);
                subtitle = itemView.findViewById(R.id.subtitle);
                image = itemView.findViewById(R.id.image);
                item = itemView.findViewById(R.id.item);
            }
        }
    }


    class refreshTask extends AsyncTask {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading.setVisibility(View.VISIBLE);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            listRes.clear();
            BmobQuery<Topic> bq = new BmobQuery();
            String[] lockedType = new String[]{"basic-lock", "high-lock", "low-lock", "normal-lock", "lock"};
            if (CurrentUser == null || !CurrentUser.getUsername().equals("hita"))
                bq.addWhereNotContainedIn("type", Arrays.asList(lockedType));
            List<Topic> res = bq.findObjectsSync(Topic.class);
            listRes.addAll(res);
            Collections.sort(listRes);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            loading.setVisibility(View.GONE);
            listAdapter.notifyDataSetChanged();
        }
    }

}
