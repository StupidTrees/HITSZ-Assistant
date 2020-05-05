package com.stupidtree.hita.fragments.popup;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.stupidtree.hita.R;

import java.util.ArrayList;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.TPE;

public class FragmentSheetList<T> extends FragmentRadiusPopup {

    private int title;
    private RecyclerView.Adapter<? extends RecyclerView.ViewHolder> listAdapter;
    private List<T> listRes;
    private ListLoader<T> listLoader;

    public FragmentSheetList(int title, ListLoader<T> listLoader) {
        this.listLoader = listLoader;
        this.title = title;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sheet_list, container, false);
        initList(v);
        return v;
        //return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void initList(View view) {
        listRes = new ArrayList<>();
        RecyclerView list = view.findViewById(R.id.list);
        TextView tt = view.findViewById(R.id.title);
        tt.setText(title);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        listAdapter = listLoader.initListAdapter(listRes);
        list.setAdapter(listAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        new refreshListTask().executeOnExecutor(TPE);
    }

    public interface ListLoader<T> {
        @WorkerThread
        List<T> loadData();

        RecyclerView.Adapter<? extends RecyclerView.ViewHolder> initListAdapter(List<T> list);
    }

    class refreshListTask extends AsyncTask {
        List<T> loadRes;

        @Override
        protected Object doInBackground(Object[] objects) {
            loadRes = listLoader.loadData();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            listRes.clear();
            listRes.addAll(loadRes);
            if (listAdapter != null) listAdapter.notifyDataSetChanged();
        }
    }

}
