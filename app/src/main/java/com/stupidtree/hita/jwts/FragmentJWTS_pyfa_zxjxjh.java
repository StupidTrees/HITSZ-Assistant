package com.stupidtree.hita.jwts;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivityMain;
import com.stupidtree.hita.adapter.ZXJXJHListAdapter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.stupidtree.hita.HITAApplication.cookies;
import static com.stupidtree.hita.HITAApplication.mDBHelper;

public class FragmentJWTS_pyfa_zxjxjh extends BaseFragment {
    List<Map<String, String>> subjectsItems;
    RecyclerView zxjxjhList;
    ZXJXJHListAdapter zxjxjhAdapter;
    private OnFragmentInteractionListener mListener;
    getZXJXJHTask pageTask;

    public FragmentJWTS_pyfa_zxjxjh() {
        // Required empty public constructor
    }

    public static FragmentJWTS_pyfa_zxjxjh newInstance() {
        return new FragmentJWTS_pyfa_zxjxjh();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_jwts_pyfa_zxjxjh, container, false);
        initViews(v);
        Refresh();
        return v;
    }



    void initViews(View v) {
        subjectsItems = new ArrayList<>();
        zxjxjhList = v.findViewById(R.id.jwts_zxjxjh_lish);
        zxjxjhAdapter = new ZXJXJHListAdapter(this.getContext(), subjectsItems);
        zxjxjhList.setAdapter(zxjxjhAdapter);
        zxjxjhList.setLayoutManager(new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false));

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    protected void stopTasks() {
        if (pageTask != null && !pageTask.isCancelled()) pageTask.cancel(true);
    }

    @Override
    protected void Refresh() {
        stopTasks();
        pageTask = new getZXJXJHTask();
        pageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    class getZXJXJHTask extends AsyncTask {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //bt_sync_info.setProgress(true);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            Document page = null;
            try {
                page = Jsoup.connect("http://jwts.hitsz.edu.cn/zxjh/queryZxkc").cookies(cookies).timeout(20000)
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .ignoreContentType(true)
                        .data("pageSize", "200")
                        .post();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Elements es = page.getElementsByClass("bot_line").select("tr");
            for (Element e : es) {
                if (e.toString().contains("option")) continue;
                if (e.toString().contains("是否考试课")) continue;
                HashMap<String, String> m = new HashMap();
                Elements tds = e.select("td");
                m.put("name", tds.get(1).text());
                m.put("attr", tds.get(6).text());
                m.put("type", tds.get(8).text());
                m.put("point", tds.get(10).text());
                m.put("totalcourses", tds.get(11).text());
                m.put("exam", tds.get(12).text());
                m.put("xn", tds.get(3).text());
                m.put("xq", tds.get(4).text());
                subjectsItems.add(m);
            }

            SQLiteDatabase sd = mDBHelper.getWritableDatabase();
            for (Map<String, String> m2 : subjectsItems) {
                ContentValues cv = new ContentValues();
                cv.put("is_exam", m2.get("exam").equals("是"));
                cv.put("is_default", false);
                sd.update("subject", cv, "code=?", new String[]{m2.get("code")});
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            //bt_sync_info.setProgress(false);
            //webview.loadData(o.toString(),"text/html; charset=UTF-8", null);
            //System.out.println(o);
            //Toast.makeText(ActivityJWTS.this,"已抓取到尽可能多的课程信息",Toast.LENGTH_SHORT).show();
            zxjxjhAdapter.notifyDataSetChanged();
            ActivityMain.saveData(FragmentJWTS_pyfa_zxjxjh.this.getActivity());

        }
    }

    Map<String, String> mContains(List<Map<String, String>> ir, String c) {

        for (Map<String, String> m2 : ir) {
            //System.out.println("compare:"+c+","+m2.get("name"));
            if (m2.get("name").equals(c)) return m2;
        }
        return null;
    }

    Map<String, String> mMatchCode(List<Map<String, String>> ir, String c) {
        for (Map<String, String> m2 : ir) {
            //System.out.println("compare:"+c+","+m2.get("name"));
            if (m2.get("code").equals(c)) return m2;
        }
        return null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
