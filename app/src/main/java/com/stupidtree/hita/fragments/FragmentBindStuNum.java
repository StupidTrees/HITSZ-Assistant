package com.stupidtree.hita.fragments;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import androidx.appcompat.app.AlertDialog;

import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.core.Curriculum;
import com.stupidtree.hita.diy.ButtonLoading;
import com.stupidtree.hita.diy.MaterialCircleAnimator;
import com.stupidtree.hita.online.Bmob_User_Data;
import com.stupidtree.hita.online.HITAUser;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.loadDataFromCloud;
import static com.stupidtree.hita.HITAApplication.themeID;

public class FragmentBindStuNum extends BottomSheetDialogFragment {
    //登录请求地址
    private final static String LOGIN = "http://jwts.hitsz.edu.cn:8080/login";
    private final static String LOGIN_VIEW = "http://jwts.hitsz.edu.cn:8080/";
    private final static String CHECK_CODE = "http://jwts.hitsz.edu.cn:8080/captchaImage";

    ImageView safeCodeImage;
    EditText username, password, safecode;
    ButtonLoading login;
    ProgressBar loadingView;
    LinearLayout loginCard;
    TextView loadingError;
    //验证码
    private byte[] checkPic;
    TextView currentUserName;
    loadSafeCodeTask pageTask_safecode;
    loginTask pageTask_login;
    BindStuNumCallBackListener mBindStuNumCallBackListener;
    HashMap localCookies;


    interface BindStuNumCallBackListener {
        void callBack(String stuNum,String password);
    }

    protected void stopTasks() {
        if (pageTask_safecode != null &&pageTask_safecode.getStatus()!=AsyncTask.Status.FINISHED)
            pageTask_safecode.cancel(true);
        if (pageTask_login != null && pageTask_login.getStatus()!=AsyncTask.Status.FINISHED) pageTask_login.cancel(true);
    }

    public BindStuNumCallBackListener getBindStuNumCallBackListener() {
        return mBindStuNumCallBackListener;
    }

    public void setBindStuNumCallBackListener(BindStuNumCallBackListener bindStuNumCallBackListener) {
        this.mBindStuNumCallBackListener = bindStuNumCallBackListener;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(getContext(), themeID);// your app theme here
        View v = inflater.cloneInContext(contextThemeWrapper).inflate(R.layout.fragment_bind_student_number, container, false);
        initViews(v);
        localCookies = new HashMap();
        return v;
    }

    void initViews(View v) {
        safeCodeImage = v.findViewById(R.id.safecode_img);
        username = v.findViewById(R.id.username);
        password = v.findViewById(R.id.password);
        safecode = v.findViewById(R.id.safecode);
        login = v.findViewById(R.id.login);
        currentUserName = v.findViewById(R.id.current_user_name);
        loginCard = v.findViewById(R.id.logincard);
        loadingView = v.findViewById(R.id.loadingview);
        loadingError = v.findViewById(R.id.loadingerror);
        if (CurrentUser != null)
            currentUserName.setText(CurrentUser.getNick() + "（" + CurrentUser.getUsername() + "）");
        login.setOnButtonLoadingListener(new ButtonLoading.OnButtonLoadingListener() {
            @Override
            public void onClick() {
                if (pageTask_login != null && pageTask_login.getStatus()!=AsyncTask.Status.FINISHED)
                    pageTask_login.cancel(true);
                pageTask_login = new loginTask(username.getText().toString(), password.getText().toString(), safecode.getText().toString(), true);
                pageTask_login.executeOnExecutor(HITAApplication.TPE);

            }

            @Override
            public void onStart() {

            }

            @Override
            public void onFinish() {

            }
        });

        if (pageTask_safecode != null && pageTask_safecode.getStatus()!=AsyncTask.Status.FINISHED)
            pageTask_safecode.cancel(true);
        pageTask_safecode = new loadSafeCodeTask();
        pageTask_safecode.executeOnExecutor(HITAApplication.TPE);
        //new directlyLoginTask().executeOnExecutor(HITAApplication.TPE);
    }


    //模拟访问登录界面
    public int getCookiesFromJwc() {
        //HITAApplication.login_jwts = false;
        try {
            //第一次访问登录界面
            Connection.Response response = Jsoup.connect(LOGIN_VIEW).timeout(5000).execute();;
            //得到系统返回的Cookies
            localCookies.clear();
            localCookies.putAll(response.cookies());
            //Log.e("cookie:",localCookies.toString()+" ");
            //请求获得验证码的内容
            checkPic = Jsoup.connect(CHECK_CODE).cookies(localCookies).ignoreContentType(true).execute().bodyAsBytes();
        } catch (Exception e) {
            return -1;
        }
        if (checkPic.length == 0 || localCookies.size() == 0) {
            return -1;
        }
        return 0;
    }

    //登录\爬取
    public String loginCheck(String username, String password, String checkCode) throws IOException {
        Document login = Jsoup.connect(LOGIN).cookies(localCookies).timeout(5000)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .data("usercode", username).data("password", password).data("code", checkCode).
                        ignoreContentType(true).post();
        //System.out.println(login_jwts);
        if (login.toString().contains("alert('")) {
            return "ALT:" + login.toString().substring(login.toString().indexOf("alert('") + 7, login.toString().indexOf("\')", login.toString().indexOf("alert(\'")));
        } else if (login.toString().contains("欢迎使用")) return "成功";
        else return "失败";
    }


    class loadSafeCodeTask extends AsyncTask<String, Integer, Integer> {


        @Override
        protected Integer doInBackground(String... strings) {
            return getCookiesFromJwc();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loginCard.setVisibility(View.INVISIBLE);
            loadingError.setVisibility(View.INVISIBLE);
            loadingView.setVisibility(View.VISIBLE);
            loadingView.post(new Runnable() {
                @Override
                public void run() {
                    MaterialCircleAnimator.animShow(loadingView, 400);
                }
            });
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            loadingView.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Integer i) {
            super.onPostExecute(i);
            if (i < 0) {
                loadingError.post(new Runnable() {
                    @Override
                    public void run() {
                        loadingView.setVisibility(View.GONE);
                        MaterialCircleAnimator.animShow(loadingError, 500);
                    }
                });
                loginCard.setVisibility(View.INVISIBLE);

            } else {
                Glide.with(getContext()).load(checkPic).into(safeCodeImage);
                loginCard.post(new Runnable() {
                    @Override
                    public void run() {
                        loadingView.setVisibility(View.GONE);
                        MaterialCircleAnimator.animShow(loginCard, 600);
                    }
                });
            }
        }
    }

    class loginTask extends AsyncTask<String, Integer, String> {

        String username, password, safecode;

        Document userInfo;
        boolean toast;

        loginTask(String username, String password, String safecode, boolean toast) {
            this.username = username;
            this.password = password;
            this.safecode = safecode;
            this.toast = toast;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            login.setProgress(true);
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                String result = loginCheck(username, password, safecode);
                if (result.contains("成功")) {
                    try {
                        Document userinfo = Jsoup.connect("http://jwts.hitsz.edu.cn:8080/xswhxx/queryXswhxx").cookies(localCookies).timeout(5000)
                                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                                .header("Content-Type", "application/x-www-form-urlencoded")
                                .ignoreContentType(true)
                                .get();
                        //userinfo.toString().contains("alert('")||
                        if (userinfo.getElementsByTag("table").size() <= 0) {
                            // System.out.println(userinfo.toString());
                            return "失败";
                            //dd.toString().substring(dd.toString().indexOf("alert('")+7,dd.toString().indexOf("\')",dd.toString().indexOf("alert(\'"))).contains("过期")
                        }
                        userInfo = userinfo;

                    } catch (Exception e) {
                        return "失败";
                    }
                }
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return "失败";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            login.setProgress(false);
            if (s.startsWith("ALT:")) {
                HITAApplication.login_jwts = false;
                AlertDialog ad = new AlertDialog.Builder(getContext()).create();
                ad.setTitle("提示");
                ad.setMessage(s.substring(4));
                ad.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                ad.show();
            } else if (s.contains("成功")) {
                BmobQuery<HITAUser> bq = new BmobQuery<>();
                bq.addWhereEqualTo("studentnumber",username);

                bq.findObjects(new FindListener<HITAUser>() {
                    @Override
                    public void done(final List<HITAUser> list, BmobException e) {
                        if(list!=null&&list.size()>0){
                            List<HITAUser> toR = new ArrayList<>();
                            for(HITAUser h:list) if(h.getObjectId().equals(CurrentUser.getObjectId())) toR.add(h);
                            list.removeAll(toR);
                        }
                        if(e==null&&list!=null&&list.size()>0){
                            final boolean[] options = {true,true};
                            AlertDialog ad = new AlertDialog.Builder(getContext()).setTitle("该学号已和"+list.get(0).getUsername()+"("+list.get(0).getNick()+")绑定")
                                    .setMultiChoiceItems(new String[]{"迁移该账号数据至本账号"}, new boolean[]{true}, new DialogInterface.OnMultiChoiceClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                                            options[i] = b;
                                        }
                                    }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if(userInfo!=null) getUserInfo(userInfo);
                                            if(options[0]){
                                                BmobQuery<Bmob_User_Data> bq = new BmobQuery();
                                                bq.addWhereEqualTo("hitaUser",list.get(0).getObjectId());
                                                bq.findObjects(new FindListener<Bmob_User_Data>() {
                                                    @Override
                                                    public void done(List<Bmob_User_Data> list2, BmobException e) {
                                                        if(e==null&&list2!=null&&list2.size()>0){
                                                           // Log.e("found!", String.valueOf(list2));
                                                           // System.out.println(list2.get(0).getHitaUser());
                                                            Bmob_User_Data bud = list2.get(0);
                                                            loadDataFromCloud(bud);
                                                        }else{
                                                            Log.e("error",e.toString());
                                                            Toast.makeText(HContext,"迁移数据失败",Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                             }
                                            if (mBindStuNumCallBackListener != null)
                                                mBindStuNumCallBackListener.callBack(username,password);
                                        }
                                    }).setNegativeButton("取消",null).create();
                            ad.show();
                        }else{
                            if(userInfo!=null) getUserInfo(userInfo);
                            if (mBindStuNumCallBackListener != null)
                                mBindStuNumCallBackListener.callBack(username,password);
                        }
                    }
                });

            } else {
                HITAApplication.login_jwts = false;
                //String x = Jsoup.parse(s).getElementsByTag("script").toString();
                if (toast) {
                    AlertDialog ad = new AlertDialog.Builder(getContext())
                            .setTitle("提示")
                            .setMessage("绑定失败！")
                            .setPositiveButton("再来一次", null).create();
                    ad.show();
                }

            }
        }
    }

    public void getUserInfo(Document doc) {
        Element table = doc.getElementsByTag("table").first();
        try {
            Elements ths = table.getElementsByTag("th");
            Elements tds = table.getElementsByTag("td");

            String stuNum = new String();
            String school = new String();
            String realname = new String();
            for (int i = 0; i < tds.size(); i++) {
                if (tds.get(i).toString().contains("<img")) tds.remove(i);
            }
            for (int i = 0; i < ths.size(); i++) {
                String key = ths.get(i).text().replaceAll("：", "");
                if (key.equals("学号")) stuNum = tds.get(i).text();
                if (key.equals("系")) school = tds.get(i).text();
                if (key.equals("姓名")) realname = tds.get(i).text();
            }
            Log.e("获取用户数据：", stuNum + "," + school + "," + realname);
            if (CurrentUser != null) {
                CurrentUser.setSchool(school);
                CurrentUser.setRealname(realname);
                CurrentUser.update(new UpdateListener() {
                    @Override
                    public void done(BmobException e) {
                        Toast.makeText(HContext,"已更新用户信息",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println(userInfos);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTasks();
    }
}





