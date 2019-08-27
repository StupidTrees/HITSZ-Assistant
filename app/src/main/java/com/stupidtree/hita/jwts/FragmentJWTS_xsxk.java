package com.stupidtree.hita.jwts;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.XSXKListAdapter;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.cookies;

public class FragmentJWTS_xsxk extends BaseFragment {
    RecyclerView list;
    XSXKListAdapter listAdapter;
    List<Map<String, String>> lisRes;
    private OnFragmentInteractionListener mListener;
    List<Map<String, String>> xnxqOptions;
    List<List<Map<String, String>>> listButtonOptions;
    List<Map<String, String>> spinnerOptionsTYPE;
    List<String> spinnerOptionsXNXQ;
    ArrayAdapter spinnerAdapterXNXQ, spinnerAdapterBX, spinnerAdapterType;
    Spinner spinnerXNXQ, spinnerBX, spinnerType;
    TextView notification, totalCoursesOrResult,blank;
    ProgressBar loadingView;
    String token;
    public FragmentJWTS_xsxk() {
        // Required empty public constructor
    }


    public static FragmentJWTS_xsxk newInstance() {
        FragmentJWTS_xsxk fragment = new FragmentJWTS_xsxk();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_jwts_xsxk, container, false);
        initPage(v);
        initList(v);
        return v;
    }

    void initPage(View v) {
        spinnerOptionsXNXQ = new ArrayList<>();
        xnxqOptions = new ArrayList<>();
        spinnerOptionsTYPE = new ArrayList<>();
        Map m1 = new HashMap();
        Map m2 = new HashMap();
        Map m3 = new HashMap();
        Map m4 = new HashMap();
        Map m5 = new HashMap();
        Map m6 = new HashMap();
        Map m7 = new HashMap();
        m1.put("name", "必修");
        m1.put("value", "bx");
        m2.put("name", "限选");
        m2.put("value", "xx");
        m3.put("name", "文理通识");
        m3.put("value", "qxrx");
        m4.put("name", "创新研修");
        m4.put("value", "cxyx");
        m5.put("name", "创新实验");
        m5.put("value", "cxsy");
        m6.put("name", "体育");
        m6.put("value", "ty");
        m7.put("name","MOOC");
        m7.put("value","mooc");
        Map[] m = new Map[]{m1, m2, m3, m4, m5, m6,m7};
        spinnerOptionsTYPE.addAll(Arrays.<Map<String, String>>asList(m));
        ArrayList<String> typeStr = new ArrayList<>();
        for (Map<String, String> ma : spinnerOptionsTYPE) typeStr.add(ma.get("name"));
        loadingView = v.findViewById(R.id.xsxk_loading);
        spinnerXNXQ = v.findViewById(R.id.spinner_xsxk_xnxq);
        spinnerBX = v.findViewById(R.id.spinner_xsxk_bx);
        spinnerType = v.findViewById(R.id.spinner_xsxk_type);
        notification = v.findViewById(R.id.xsxk_notification);
        totalCoursesOrResult = v.findViewById(R.id.totalcoursesOrResult);
        blank = v.findViewById(R.id.xsxk_blank);
        spinnerAdapterXNXQ = new ArrayAdapter(v.getContext(), android.R.layout.simple_spinner_item, spinnerOptionsXNXQ);
        spinnerAdapterType = new ArrayAdapter(v.getContext(), android.R.layout.simple_spinner_item, typeStr);
        ArrayList<String> x = new ArrayList();
        x.add("备选课程");
        x.add("已选课程");
        spinnerAdapterBX = new ArrayAdapter(v.getContext(), android.R.layout.simple_spinner_item, x);
        spinnerAdapterBX.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAdapterXNXQ.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAdapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        AdapterView.OnItemSelectedListener spinnerSelect = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    new refreshListTask(spinnerOptionsTYPE.get(spinnerType.getSelectedItemPosition()).get("value"), xnxqOptions.get(spinnerXNXQ.getSelectedItemPosition()).get("value")).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } catch (Exception e) {

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
        spinnerXNXQ.setOnItemSelectedListener(spinnerSelect);
        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                new getWLTSPageTask(spinnerOptionsTYPE.get(position).get("value")).execute(AsyncTask.THREAD_POOL_EXECUTOR);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinnerXNXQ.setAdapter(spinnerAdapterXNXQ);
        spinnerType.setAdapter(spinnerAdapterType);
        spinnerBX.setOnItemSelectedListener(spinnerSelect);
        spinnerBX.setAdapter(spinnerAdapterBX);
        //spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


    }

    void initList(final View v) {
        list = v.findViewById(R.id.xsxk_list);
        lisRes = new ArrayList<>();
        listAdapter = new XSXKListAdapter(v.getContext(), lisRes);
        listButtonOptions = new ArrayList<>();
        list.setAdapter(listAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(v.getContext(), RecyclerView.VERTICAL, false);
        list.setLayoutManager(layoutManager);
        listAdapter.setmOnOperateClickListsner(new XSXKListAdapter.OnOperateClickListener() {
            @Override
            public void OnClick(final View view, final int index, boolean choose, final String rwh) {
                if (choose) {
                    String[] items = new String[listButtonOptions.get(index).size()];
                    for (int i = 0; i < listButtonOptions.get(index).size(); i++) {
                        items[i] = listButtonOptions.get(index).get(i).get("text");
                    }
                    AlertDialog ad = new AlertDialog.Builder(view.getContext()).setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new chooseCourseTask(rwh,listButtonOptions.get(index).get(which).get("zy"),xnxqOptions.get(spinnerXNXQ.getSelectedItemPosition()).get("value"),spinnerOptionsTYPE.get(spinnerType.getSelectedItemPosition()).get("value")).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            //Toast.makeText(view.getContext(), "不好意思哈，客户端不支持该操作", Toast.LENGTH_SHORT).show();

                        }
                    }).setTitle("选择志愿").create();

                    ad.show();
                }else{
                    new txTask(rwh,xnxqOptions.get(spinnerXNXQ.getSelectedItemPosition()).get("value"),spinnerOptionsTYPE.get(spinnerType.getSelectedItemPosition()).get("value")).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                }

            }
        });
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

    }

    @Override
    protected void Refresh() {

    }


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    class getWLTSPageTask extends AsyncTask {

        String pageXKLB;

        getWLTSPageTask(String pageXKLB) {
            this.pageXKLB = pageXKLB;
        }

        ;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            spinnerBX.setVisibility(View.INVISIBLE);
            spinnerXNXQ.setVisibility(View.INVISIBLE);
            loadingView.setVisibility(View.VISIBLE);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                xnxqOptions.clear();
                spinnerOptionsXNXQ.clear();
                Document wltsPage = Jsoup.connect("http://jwts.hitsz.edu.cn/xsxk/queryXsxk?pageXklb=" + pageXKLB).cookies(cookies).timeout(20000)
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .ignoreContentType(true)
                        .get();
                Element selections = wltsPage.getElementsByClass("XNXQ_CON").first();
                //System.out.println(selections);
                for (Element e : selections.getElementsByTag("option")) {
                    Map m = new HashMap();
                    m.put("value", e.attr("value"));
                    m.put("name", e.text());
                    xnxqOptions.add(m);
                    spinnerOptionsXNXQ.add(String.valueOf(m.get("name")));
                }
                return true;
            } catch (Exception e) {
                return false;
            }

        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if ((Boolean) o) {
                spinnerBX.setVisibility(View.VISIBLE);
                spinnerXNXQ.setVisibility(View.VISIBLE);
                loadingView.setVisibility(View.INVISIBLE);
                spinnerAdapterXNXQ.notifyDataSetChanged();
            } else
                Toast.makeText(HContext, "页面加载失败！", Toast.LENGTH_SHORT).show();
            try {
                new refreshListTask(spinnerOptionsTYPE.get(spinnerType.getSelectedItemPosition()).get("value"), xnxqOptions.get(spinnerXNXQ.getSelectedItemPosition()).get("value")).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } catch (Exception e) {

            }
        }
    }

    class refreshListTask extends AsyncTask {
        String xklb;
        String xnxq;
        Boolean hasButton;

        refreshListTask(String xklb, String xnxq) {
            this.xklb = xklb;
            this.xnxq = xnxq;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            lisRes.clear();
            listButtonOptions.clear();
            list.setVisibility(View.INVISIBLE);
            loadingView.setVisibility(View.VISIBLE);
            hasButton = false;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            String toReturn;
            try {
                String url = spinnerBX.getSelectedItemPosition() == 0 ? "http://jwts.hitsz.edu.cn/xsxk/queryXsxkList" : "http://jwts.hitsz.edu.cn/xsxk/queryYxkc";
                Document xkPage = Jsoup.connect(url).cookies(cookies).timeout(20000)
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .ignoreContentType(true)
                        .data("pageXklb", xklb)
                        .data("pageXnxq", xnxq)
                        .post();
                //System.out.println(xkPage.toString());
                if(xkPage.getElementById("token")!=null) token = xkPage.getElementById("token").attr("value");
                Elements rows = xkPage.getElementsByClass("bot_line").first().select("tr");
                //String head = rows.get(0).toString();
                //System.out.println(head);
                //Log.e("!!",rows.contains("各志愿已选人数")+",,,,"+rows.contains("选课结果"));
//                if(head.contains("各志愿已选人数")&&!head.contains("选课结果")){
//                    rows.remove(0);
//                    for(Element e:rows){
//                        // Log.e("!",e.toString());
//                        Map m = new HashMap();
//                        Elements es = e.select("td");
//                        m.put("name",es.get(3).text());
//                        m.put("info",es.get(7).text());
//                        m.put("totalcourses",es.get(12).text());
//                        m.put("content",es.get(14).text());
//                        m.put("point",es.get(9).text());
//                        m.put("type",es.get(6).text());
//                        m.put("bxOryx","bx");
//                        lisRes.add(m);
//                    }
//                }else if(head.contains("各志愿已选人数")&&head.contains("选课结果")){
//                    rows.remove(0);
//                    for(Element e:rows){
//                        // Log.e("!",e.toString());
//                        Map m = new HashMap();
//                        Elements es = e.select("td");
//                        m.put("name",es.get(3).text());
//                        m.put("info",es.get(11).text());
//                        m.put("chooseresult",es.get(14).text());
//                        m.put("content",es.get(10).text()+"已选");
//                        m.put("point",es.get(9).text()+"学分");
//                        m.put("type",es.get(6).text());
//                        m.put("bxOryx","yx");
//                        lisRes.add(m);
//                    }
//                }else if((!head.contains("志愿数"))&&head.contains("选课结果")){
//                    rows.remove(0);
//                    for(Element e:rows){
//                        // Log.e("!",e.toString());
//                        Map m = new HashMap();
//                        Elements es = e.select("td");
//                        m.put("name",es.get(3).text());
//                        m.put("info",es.get(5).text());
//                        m.put("chooseresult",es.get(12).text());
//                        m.put("content",es.get(10).text()+"已选");
//                        m.put("point",es.get(9).text()+"学分");
//                        m.put("type",es.get(6).text());
//                        m.put("bxOryx","yx");
//                        lisRes.add(m);
//                    }
//                }else{
//                    rows.remove(0);
//                    for(Element e:rows){
//                        // Log.e("!",e.toString());
//                        Map m = new HashMap();
//                        Elements es = e.select("td");
//                        m.put("name",es.get(3).text());
//                        m.put("info",es.get(7).text());
//                        m.put("totalcourses",es.get(12).text()+"学时");
//                        m.put("content",es.get(13).text()+"已选");
//                        m.put("point",es.get(11).text()+"学分");
//                        m.put("type",es.get(8).text());
//                        m.put("bxOryx","bx");
//                        lisRes.add(m);
//                    }
//                }

                Map<String, Integer> positions = new HashMap<>();
                int i = 0;
                for (Element th : rows.get(0).select("th")) {
                   // Log.e("th:", th.text());
                    if (th.text().contains("课程名称")) positions.put("kcmc", i);
                    if (th.text().contains("课程类别")) positions.put("kclb", i);
                    if (th.text().contains("课程性质")) positions.put("kcxz", i);
                    if (th.text().contains("学分")) positions.put("xf", i);
                    if (th.text().contains("已选/容量")) positions.put("yx/rl", i);
                    if (th.text().contains("选课结果")) positions.put("xkjg", i);
                    if (th.text().contains("学时")) positions.put("xs", i);
                    if (th.text().contains("志愿数")) positions.put("zys", i);
                    if (th.text().contains("各志愿已选人数")) positions.put("gzyyxrs", i);
                    if (th.text().contains("上课信息")) positions.put("skxx", i);
                    if (th.text().contains("选课时间")) positions.put("xksj", i);
                    i++;
                }

                rows.remove(0);
                for (Element row : rows) {
                    // Log.e("!",e.toString());
                    Map m = new HashMap();
                    Elements es = row.select("td");
                    if (positions.get("kcmc") != null)
                        m.put("kcmc", es.get(positions.get("kcmc")).text());
                    if (positions.get("kclb") != null)
                        m.put("kclb", es.get(positions.get("kclb")).text());
                    if (positions.get("kcxz") != null)
                        m.put("kcxz", es.get(positions.get("kcxz")).text());
                    if (positions.get("xf") != null)
                        m.put("xf", es.get(positions.get("xf")).text());
                    if (positions.get("yx/rl") != null)
                        m.put("yx/rl", es.get(positions.get("yx/rl")).text());
                    if (positions.get("xkjg") != null)
                        m.put("xkjg", es.get(positions.get("xkjg")).text());
                    if (positions.get("xs") != null)
                        m.put("xs", es.get(positions.get("xs")).text());
                    if (positions.get("zys") != null)
                        m.put("zys", es.get(positions.get("zys")).text());
                    if (positions.get("gzyyxrs") != null)
                        m.put("gzyyxrs", es.get(positions.get("gzyyxrs")).text());
                    if (positions.get("skxx") != null)
                        m.put("skxx", es.get(positions.get("skxx")).text());
                    if (positions.get("xksj") != null)
                        m.put("xksj", es.get(positions.get("xksj")).text());
                    m.put("bxOryx", spinnerBX.getSelectedItemPosition() == 0 ? "bx" : "yx");
                    if(row.getElementsByClass("addlist_button").size()>0){
                        m.put("hasbutton","true");
                        hasButton = true;
                        if(row.select("td").get(0).select("ul").size()>0){
                            List<Map<String, String>> ops = new ArrayList<>();
                            Elements  options = row.select("td").get(0).select("ul").first().select("li");
                            for (Element li : options) {
                                Map m2 = new HashMap();
                                m2.put("text", li.text());
                                String OnClick = li.select("a").first().attr("onclick");
                                String rwh = OnClick.substring(OnClick.indexOf("('") + 2, OnClick.indexOf("',"));
                                String zy = OnClick.substring(OnClick.indexOf(",'") + 2, OnClick.indexOf("')"));
                                m.put("rwh", rwh);
                                m2.put("zy", zy);
                                ops.add(m2);
                                listButtonOptions.add(ops);
                            }
                            if(options.size()==0){
                                m.put("hasbutton","false");
                                hasButton = false;
                            }
                        }else {
                            String rwh = row.getElementsByClass("addlist_button").select("a").first().attr("id");
                            m.put("rwh",rwh);
                            m.put("buttonType","tx");
                        }

                    }else{
                        hasButton = false;
                        m.put("hasbutton","false");
                    }



                    lisRes.add(m);
                }




                if (xkPage.getElementsByClass("bold red FONT14 zytsxx").size() > 0) {
                    toReturn = xkPage.getElementsByClass("bold red FONT14 zytsxx").first().text();
                } else toReturn = null;
                //System.out.println(xkPage);
            } catch (Exception e) {
                toReturn = null;
                e.printStackTrace();
            }
            return toReturn;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (o != null) {
                notification.setVisibility(View.VISIBLE);
                notification.setText(o.toString());
            } else notification.setVisibility(View.GONE);
            if(hasButton) blank.setVisibility(View.VISIBLE);
            else blank.setVisibility(View.GONE);
            if (spinnerBX.getSelectedItemPosition() == 0) totalCoursesOrResult.setText("学时");
            else totalCoursesOrResult.setText("结果");
            list.setVisibility(View.VISIBLE);
            loadingView.setVisibility(View.INVISIBLE);
            listAdapter.notifyDataSetChanged();
        }
    }

    class chooseCourseTask extends AsyncTask {


        String rwh, zy, xnxq,xklb;

        chooseCourseTask(String rwh, String zy, String xnxq,String xklb) {
            this.rwh = rwh;
            this.zy = zy;
            this.xnxq = xnxq;
            this.xklb = xklb;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                //trustEveryone();
               // String token = String.valueOf(new DecimalFormat("#.16").parse(String.valueOf(new Random().nextDouble())));
                Connection connect = Jsoup.connect("http://jwts.hitsz.edu.cn/xsxk/saveXsxk");
                connect.cookies(cookies);
                connect.timeout(10000);
                connect.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
                connect.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36");
                connect.header("Content-Type", "application/x-www-form-urlencoded");
                connect.header("Referer", "http://jwts.hitsz.edu.cn/xsxk/queryXsxkList?pageXklb=" + xklb + "&pageXnxq=" + xnxq);
                connect.header("Upgrade-Insecure-Requests", "1");
                connect.header("Connection", "keep-alive");
                connect.header("Origin", "http://jwts.hitsz.edu.cn");
                connect.header("Host", "jwts.hitsz.edu.cn");
                connect.data("rwh", rwh);
                connect.data("pageXnxq", xnxq);
                connect.data("pageXklb", xklb);
                connect.data("zy", zy);
                connect.data("token", token);
                Document xkPage = connect.post();
                Log.e("!!", "header:rwh=" + rwh + ",zy=" + zy + ",xnxq=" + xnxq+",xklb="+xklb+",token="+token);
                System.out.println(listButtonOptions.toString());
                String alert = null;
                if(xkPage.toString().contains("jQuery().ready(function")){
                    String x = xkPage.toString();
                    int from = x.indexOf("alert('",x.indexOf("jQuery().ready(function"))+7;
                    int to = x.indexOf("')",from);
                    String y = x.substring(from,to);
                    alert = y;
                    System.out.println(y);
                }
                return alert;
            } catch (IOException e) {
               // Log.e("!!error", "header:rwh=" + rwh + ",zy=" + zy + ",xnxq=" + xnxq);
                e.printStackTrace();
                return "出现错误！";
            }


        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if(o!=null){
                AlertDialog ad = new AlertDialog.Builder(getContext()).setTitle("提示").setMessage(o.toString()).create();
                ad.show();
            }
            try {
                new refreshListTask(spinnerOptionsTYPE.get(spinnerType.getSelectedItemPosition()).get("value"), xnxqOptions.get(spinnerXNXQ.getSelectedItemPosition()).get("value")).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } catch (Exception e) {

            }
        }
    }

    class txTask extends AsyncTask{

        String id,xklb,xnxq;
        txTask(String id,String xnxq,String xklb){
            this.id = id;
            this.xklb = xklb;
            this.xnxq = xnxq;
        }
        @Override
        protected Object doInBackground(Object[] objects) {
            Connection connect = Jsoup.connect("http://jwts.hitsz.edu.cn/xsxk/saveXstk");
            connect.cookies(cookies);
            connect.timeout(10000);
            connect.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
            connect.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36");
            connect.header("Content-Type", "application/x-www-form-urlencoded");
            connect.header("Referer", "http://jwts.hitsz.edu.cn/xsxk/queryXsxkList?pageXklb=" + xklb + "&pageXnxq=" + xnxq);
            connect.header("Upgrade-Insecure-Requests", "1");
            connect.header("Connection", "keep-alive");
            connect.header("Origin", "http://jwts.hitsz.edu.cn");
            connect.header("Host", "jwts.hitsz.edu.cn");
            connect.data("rwh", id);
            connect.data("pageXnxq", xnxq);
            connect.data("pageXklb", xklb);
            try {
                Document xkPage = connect.post();
                //System.out.println(xkPage);
                String alert = null;
                if(xkPage.toString().contains("jQuery().ready(function")){
                    String x = xkPage.toString();
                    int from = x.indexOf("alert('",x.indexOf("jQuery().ready(function"))+7;
                    int to = x.indexOf("')",from);
                    String y = x.substring(from,to);
                    alert = y;
                    System.out.println(y);
                }
                return alert;
            } catch (IOException e) {
                e.printStackTrace();
                return "出现错误！";
            }
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if(o!=null){
                AlertDialog ad = new AlertDialog.Builder(getContext()).setTitle("提示").setMessage(o.toString()).create();
                ad.show();
            }
            try {
                new refreshListTask(spinnerOptionsTYPE.get(spinnerType.getSelectedItemPosition()).get("value"), xnxqOptions.get(spinnerXNXQ.getSelectedItemPosition()).get("value")).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } catch (Exception e) {

            }
        }
    }
}
