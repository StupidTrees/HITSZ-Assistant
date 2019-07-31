package com.stupidtree.hita.jwts;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.stupidtree.hita.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.text.DecimalFormat;

import cn.bmob.v3.http.I;

import static com.stupidtree.hita.HITAApplication.cookies;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentJWTS_cjgl_xfj.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentJWTS_cjgl_xfj#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentJWTS_cjgl_xfj extends Fragment {

    private OnFragmentInteractionListener mListener;

    TextView xfj,pm,percentage;
    String xfj_txt,pm_txt,percentage_txt;
    public FragmentJWTS_cjgl_xfj() {
        // Required empty public constructor
    }

    public static FragmentJWTS_cjgl_xfj newInstance(String param1, String param2) {
        FragmentJWTS_cjgl_xfj fragment = new FragmentJWTS_cjgl_xfj();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View v =  inflater.inflate(R.layout.fragment_jwts_cjgl_xfj, container, false);
       initViews(v);
       new refreshTask(getContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
       return v;
    }

    void initViews(View v){
        percentage = v.findViewById(R.id.txt_percentage);
        xfj = v.findViewById(R.id.txt_xfj);
        pm = v.findViewById(R.id.txt_pm);
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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    class refreshTask extends loadJWTSinfoTask{

        refreshTask(Context context) {
            super(context);
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                Document page = Jsoup.connect("http://jwts.hitsz.edu.cn/xfj/queryListXfj").cookies(cookies).timeout(60000)
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .ignoreContentType(true)
                        .get();
                String res = super.doInBackground(page.toString());
                if(res!=null) return res;
                xfj_txt = page.getElementById("pjxfj").text();
                pm_txt = page.getElementById("zrs").text();
                String[] x = pm_txt.split("/");
                int pm = Integer.parseInt(x[0]);
                int total = Integer.parseInt(x[1]);
                DecimalFormat df = new DecimalFormat("#0.00");
                percentage_txt = "前"+df.format(100*(float)pm/(float)total)+"%";
                return null;
            } catch (Exception e) {
                return "获取学分绩失败！";
            }
        }

        @Override
        protected void onPostExecute(String o) {
            super.onPostExecute(o);
            if(o==null){
                xfj.setText(xfj_txt);
                pm.setText(pm_txt);
                percentage.setText(percentage_txt);
            }
        }
    }
}
