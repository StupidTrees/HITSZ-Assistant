//package com.stupidtree.hita.fragments;
//
//import android.os.AsyncTask;
//import android.os.Bundle;
//
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import com.stupidtree.hita.BaseFragment;
//import com.stupidtree.hita.HITAApplication;
//import com.stupidtree.hita.R;
//
//import org.jsoup.Connection;
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.jsoup.select.Elements;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//
//import static com.stupidtree.hita.HITAApplication.cookies_ut_card;
//
//public class FragmentUT_Card extends BaseFragment {
//
//
//    String username;
//    TextView name,money,number,kzt,djzt,gszt,jczt;
//    public FragmentUT_Card() {
//        // Required empty public constructor
//    }
//
//    public static FragmentUT_Card newInstance(String userName) {
//        FragmentUT_Card fragment = new FragmentUT_Card();
//        Bundle b = new Bundle();
//        b.putString("username",userName);
//        fragment.setArguments(b);
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        username = getArguments().getString("username");
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View v=  inflater.inflate(R.layout.fragment_ut_card, container, false);
//        initViews(v);
//        return v;
//    }
//
//
//    void initViews(View v){
//        name = v.findViewById(R.id.name);
//        money = v.findViewById(R.id.money);
//        number = v.findViewById(R.id.number);
//        jczt = v.findViewById(R.id.jczt);
//        kzt = v.findViewById(R.id.kzt);
//        gszt = v.findViewById(R.id.gszt);
//        djzt = v.findViewById(R.id.djzt);
//
//    }
//    @Override
//    protected void stopTasks() {
//
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        Refresh();
//    }
//
//    @Override
//    public void Refresh() {
//        new loadCardInfoTask().executeOnExecutor(HITAApplication.TPE);
//    }
//
//    class loadCardInfoTask extends AsyncTask{
//        String name_str,number_str,money_str;
//        String kzt_str,djzt_str,gszt_str,jczt_str;
//        @Override
//        protected Object doInBackground(Object[] objects) {
//            try {
//                Connection.Response r = Jsoup.connect("http://10.64.1.15/sfrzwhlgportalHome.action")
//                        .data("errorcode","1")
//                        .header("Connection","keep-alive")
//                        .data("continueurl","http://ecard.utsz.edu.cn/accountcardUser.action")
//                        .data("ssoticketid",username)
//                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36").header("Connection","keep-alive")
//                        .execute();
//                cookies_ut_card.clear();
//                cookies_ut_card.putAll(r.cookies());
//                Document d2 = Jsoup.connect("http://10.64.1.15/accountcardUser.action")
//                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
//                        .header("Connection","keep-alive")
//                        .header("Host","10.64.1.15")
//                        .cookies(cookies_ut_card)
//                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
//                        .header("Content-Type", "application/x-www-form-urlencoded")
//                        .get();
//               // System.out.println(d2);
//                Elements infosList = d2.select("td");
//                Elements toR = new Elements();
//                for(Element e:infosList){
//                    if(!e.hasClass("neiwen")||e.toString().contains("table")) toR.add(e);
//                }
//                infosList.removeAll(toR);
//                //System.out.println(infosList);
//                Map infosMap = new HashMap();
//                for(int i = 0;i+1<infosList.size();i+=2){
//                    //Log.e("td", String.valueOf(infosList.get(i)));
//                    String key = infosList.get(i).text().replaceAll(" ","").replaceAll("：","");
//                    String value = infosList.get(i+1).text();
//                    infosMap.put(key,value);
//                }
//                Log.e("infos", String.valueOf(infosMap));
//                name_str = infosMap.get("姓名").toString();
//                money_str = infosMap.get("余额").toString().replaceAll("）","）\n").replaceAll("\\)","\\)\n");
//                number_str = infosMap.get("学工号").toString();
//                kzt_str = infosMap.get("卡状态").toString();
//                djzt_str = infosMap.get("冻结状态").toString();
//                gszt_str = infosMap.get("挂失状态").toString();
//                jczt_str = infosMap.get("检查状态").toString();
//
//                return true;
//            } catch (Exception e) {
//                e.printStackTrace();
//                return false;
//            }
//        }
//
//        @Override
//        protected void onPostExecute(Object o) {
//            super.onPostExecute(o);
//            if((Boolean) o){
//                name.setText(name_str);
//                number.setText(number_str);
//                gszt.setText(gszt_str);
//                kzt.setText(kzt_str);
//                jczt.setText(jczt_str);
//                djzt.setText(djzt_str);
//                money.setText(money_str);
//            }else{
//                name.setText("");
//                number.setText("加载失败");
//            }
//        }
//    }
//}
