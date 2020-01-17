//package com.stupidtree.hita.jwts;
//
//import android.annotation.SuppressLint;
//import android.content.Intent;
//import android.os.AsyncTask;
//import android.os.Bundle;
//
//import androidx.recyclerview.widget.GridLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.bumptech.glide.Glide;
//import com.bumptech.glide.load.resource.bitmap.CircleCrop;
//import com.bumptech.glide.request.RequestOptions;
//import com.bumptech.glide.signature.ObjectKey;
//import com.stupidtree.hita.BaseActivity;
//import com.stupidtree.hita.BaseFragment;
//import com.stupidtree.hita.HITAApplication;
//import com.stupidtree.hita.R;
//import com.stupidtree.hita.activities.ActivityLoginJWTS;
//import com.stupidtree.hita.adapter.UserInfosAdapter;
//
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.jsoup.select.Elements;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import static com.stupidtree.hita.HITAApplication.CurrentUser;
//import static com.stupidtree.hita.HITAApplication.HContext;
//import static com.stupidtree.hita.HITAApplication.cookies_jwts;
//import static com.stupidtree.hita.HITAApplication.login_jwts;
//
//
//@SuppressLint("ValidFragment")
//public class FragmentJWTS_info extends BaseFragment {
//    HashMap<String, String> userInfos;
//    List<Map.Entry> listRes;
//    RecyclerView list;
//    UserInfosAdapter listAdapter;
//    ImageView avatarView;
//    TextView nameText,studentnumberText;
//    refreshInfoTask pageTask;
//    ProgressBar loading;
//    private byte[] avatar;
//    @SuppressLint("ValidFragment")
//    public FragmentJWTS_info(){
//        userInfos = new HashMap<>();
//    }
//
//    public static FragmentJWTS_info newInstance(){
//        return new FragmentJWTS_info();
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//    }
//
//
//    void initList(View v){
//        avatarView = v.findViewById(R.id.jwts_avatar);
//        loading = v.findViewById(R.id.loading);
//        nameText = v.findViewById(R.id.jwts_name);
//        studentnumberText = v.findViewById(R.id.jwts_studentnumber);
//        list = v.findViewById(R.id.rec_user_center_main);
//        listRes = new ArrayList<>();
//        for(Map.Entry x:userInfos.entrySet()){
//            if(x.getValue().equals("")||x.getValue()==null) continue;
//            listRes.add(x);
//        }
//        for(Map.Entry x:userInfos.entrySet()){
//            if(x.getValue().equals("")||x.getValue()==null){
//                listRes.add(x);
//            }
//        }
//        listAdapter = new UserInfosAdapter(listRes, (BaseActivity) this.getActivity());
//        list.setAdapter(listAdapter);
//        RecyclerView.LayoutManager lm = new GridLayoutManager(this.getContext(),2);
//        list.setLayoutManager(lm);
//
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_jwts_info, container, false);
//        initList(view);
//        return view;
//    }
//
//    @Override
//    protected void stopTasks() {
//        if(pageTask!=null&&pageTask.getStatus()!=AsyncTask.Status.FINISHED) pageTask.cancel(true);
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
//        if(pageTask!=null&&pageTask.getStatus()!=AsyncTask.Status.FINISHED) pageTask.cancel(true);
//        pageTask = new refreshInfoTask();
//        pageTask.executeOnExecutor(HITAApplication.TPE);
//    }
//    public void getUserInfo(Document doc){
//        Element table = doc.getElementsByTag("table").first();
//        try {
//            Elements ths = table.getElementsByTag("th");
//            Elements tds = table.getElementsByTag("td");
//            for(int i = 0;i<tds.size();i++){
//                if(tds.get(i).toString().contains("<img")){
//                    String image;
//                    image = "http://jwts.hitsz.edu.cn:8080/"+tds.get(i).select("img").attr("src");
//                    // userInfos.put("头像",image);
//                    avatar = Jsoup.connect(image).cookies(cookies_jwts).ignoreContentType(true).execute().bodyAsBytes();
//                    new saveAvatarTask(image).executeOnExecutor(HITAApplication.TPE);
////                    if(avatar==null) Toast.makeText(HContext,"加载教务头像失败！",Toast.LENGTH_SHORT).show();
////                    else Glide.with(HContext).load(avatar).signature(new ObjectKey(System.currentTimeMillis())).placeholder(R.drawable.ic_account).apply(RequestOptions.bitmapTransform(new CircleCrop())).into(avatarView);
//                    tds.remove(i);
//                }
//            }
//            userInfos.clear();
//            for(int i = 0;i<ths.size();i++){
//                userInfos.put(ths.get(i).text().replaceAll("：",""),tds.get(i).text());
//            }
////            if(CurrentUser!=null&&userInfos.get("学号").equals(CurrentUser.getStudentnumber())){
////                CurrentUser.setSchool(userInfos.get("系"));
////                CurrentUser.setRealname(userInfos.get("姓名"));
////                CurrentUser.update(new UpdateListener() {
////                    @Override
////                    public void done(BmobException e) {
////                        Toast.makeText(HContext,"已更新用户信息",Toast.LENGTH_SHORT).show();
////                    }
////                });
////            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        //System.out.println(userInfos);
//    }
//
//    class refreshInfoTask extends AsyncTask<String,Integer,Boolean> {
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            list.setVisibility(View.INVISIBLE);
//            loading.setVisibility(View.VISIBLE);
//        }
//
//        @Override
//        protected Boolean doInBackground(String... strings) {
//            try {
//                Document userinfo = Jsoup.connect("http://jwts.hitsz.edu.cn:8080/xswhxx/queryXswhxx").cookies(cookies_jwts).timeout(5000)
//                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
//                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
//                        .header("Content-Type", "application/x-www-form-urlencoded")
//                        .ignoreContentType(true)
//                        .get();
//                //userinfo.toString().contains("alert('")||
//                if(userinfo.getElementsByTag("table").size()<=0){
//                   // System.out.println(userinfo.toString());
//                    return false;
//                    //dd.toString().substring(dd.toString().indexOf("alert('")+7,dd.toString().indexOf("\')",dd.toString().indexOf("alert(\'"))).contains("过期")
//                }
//                getUserInfo(userinfo);
//            } catch (Exception e) {
//                return false;
//            }
//            return true;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean o) {
//            super.onPostExecute(o);
//            list.setVisibility(View.VISIBLE);
//            loading.setVisibility(View.GONE);
//            try {
//                if(!o){
//                    Toast.makeText(HContext,"页面过期，请返回重新登录！",Toast.LENGTH_SHORT).show();
//                    login_jwts = false;
//                    Intent i = new Intent(getActivity(), ActivityLoginJWTS.class);
//                    startActivity(i);
//                    getActivity().finish();
//                }
//                //Glide.with(HContext).load(avatar).signature(new ObjectKey(System.currentTimeMillis())).placeholder(R.drawable.ic_account).apply(RequestOptions.bitmapTransform(new CircleCrop())).into(avatarView);
//                if(CurrentUser!=null&&userInfos.get("学号").equals(CurrentUser.getStudentnumber()))  studentnumberText.setText(userInfos.get("学号")+"（已与本账号绑定）");
//                else  studentnumberText.setText(userInfos.get("学号"));
//                nameText.setText(userInfos.get("姓名"));
//                listRes.clear();
//                for(Map.Entry x:userInfos.entrySet()){
//                    if(x.getValue().equals("")||x.getValue()==null) continue;
//                    listRes.add(x);
//                }
//                for(Map.Entry x:userInfos.entrySet()){
//                    if(x.getValue().equals("")||x.getValue()==null){
//                        listRes.add(x);
//                    }
//                }
//                listAdapter.notifyDataSetChanged();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    class saveAvatarTask extends AsyncTask{
//        String link;
//        saveAvatarTask(String link){
//            this.link = link;
//        }
//        @Override
//        protected Object doInBackground(Object[] objects) {
//            try {
//                avatar = Jsoup.connect(link).cookies(cookies_jwts).ignoreContentType(true).execute().bodyAsBytes();
//            } catch (Exception e) {
//                return null;
//            }
//            //FileOperator.saveAvatarToFile(ActivityJWTS.this.getFilesDir(), "avatar_ugly", BitmapFactory.decodeByteArray(avatar,0,avatar.length));
//            //defaultSP.edit().putString("avatarGlideSignature", String.valueOf(System.currentTimeMillis())).commit();
//            return avatar;
//        }
//
//        @Override
//        protected void onPostExecute(Object o) {
//            super.onPostExecute(o);
//            if(o==null) Toast.makeText(HContext,"加载教务头像失败！",Toast.LENGTH_SHORT).show();
//            else Glide.with(HContext).load(avatar).signature(new ObjectKey(System.currentTimeMillis())).placeholder(R.drawable.ic_account_activated).apply(RequestOptions.bitmapTransform(new CircleCrop())).into(avatarView);
//            //Toast.makeText(ActivityJWTS.this,"已同步个人信息到APP",Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    public interface OnListFragmentInteractionListener {
//
//    }
//}
