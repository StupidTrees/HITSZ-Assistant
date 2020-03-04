//package com.stupidtree.hita.activities;
//
//import android.annotation.SuppressLint;
//import android.app.Activity;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.util.Base64;
//import android.util.Log;
//import android.view.View;
//import android.widget.EditText;
//import android.widget.LinearLayout;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//
//import androidx.appcompat.app.AlertDialog;
//import androidx.appcompat.widget.Toolbar;
//import androidx.core.app.ActivityCompat;
//
//import com.google.gson.Gson;
//import com.stupidtree.hita.BaseActivity;
//import com.stupidtree.hita.HITAApplication;
//import com.stupidtree.hita.R;
//import com.stupidtree.hita.diy.ButtonLoading;
//import com.stupidtree.hita.diy.MaterialCircleAnimator;
//
//import org.jsoup.Connection;
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//
//import java.io.IOException;
//import java.lang.ref.WeakReference;
//
//import static com.stupidtree.hita.HITAApplication.CurrentUser;
//import static com.stupidtree.hita.HITAApplication.TPE;
//import static com.stupidtree.hita.HITAApplication.cookies_ut;
//import static com.stupidtree.hita.HITAApplication.defaultSP;
//import static com.stupidtree.hita.HITAApplication.ut_username;
//
//public class ActivityLoginUT extends BaseActivity {
//    public static String UT_login_url = "https://idp.utsz.edu.cn/cas/login?null";
//    String lt;
//    EditText username, password;
//    //, safecode;
//    ButtonLoading login;
//    ProgressBar loadingView;
//    LinearLayout loginCard;
//    TextView loadingError;
//    //验证码
//    loadSafeCodeTask pageTask_safecode;
//    loginTask pageTask_login;
//
//    @Override
//    protected void stopTasks() {
//        if (pageTask_safecode != null && pageTask_safecode.getStatus()!=AsyncTask.Status.FINISHED)
//            pageTask_safecode.cancel(true);
//        if (pageTask_login != null && pageTask_login.getStatus()!=AsyncTask.Status.FINISHED) pageTask_login.cancel(true);
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setWindowParams(true, false, false);
//        setContentView(R.layout.activity_login_ut);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
//        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onBackPressed();
//            }
//        });
//        //safeCodeImage = findViewById(R.id.safecode_img);
//        username = findViewById(R.id.username);
//        password = findViewById(R.id.password);
//       // safecode = findViewById(R.id.safecode);
//        login = findViewById(R.id.login);
//        loginCard = findViewById(R.id.logincard);
//        loadingView = findViewById(R.id.loadingview);
//        loadingError = findViewById(R.id.loadingerror);
//        login.setOnButtonLoadingListener(new ButtonLoading.OnButtonLoadingListener() {
//            @Override
//            public void onClick() {
//                if (pageTask_login != null && pageTask_login.getStatus()!=AsyncTask.Status.FINISHED)
//                    pageTask_login.cancel(true);
//                pageTask_login = new loginTask(ActivityLoginUT.this, lt,username.getText().toString(), password.getText().toString(), true);
//                pageTask_login.executeOnExecutor(TPE);
//            }
//
//            @Override
//            public void onStart() {
//
//            }
//
//            @Override
//            public void onFinish() {
//
//            }
//        });
//
//        if (pageTask_safecode != null && pageTask_safecode.getStatus()!=AsyncTask.Status.FINISHED)
//            pageTask_safecode.cancel(true);
//        pageTask_safecode = new loadSafeCodeTask(this);
//        pageTask_safecode.executeOnExecutor(TPE);
//        //new directlyLoginTask().executeOnExecutor(HITAApplication.TPE);
//    }
//
//
//
//
//
//    //登录\爬取
//    public static String loginCheck(String username, String password,String lt) throws IOException {
//        Document after =  Jsoup.connect(UT_login_url)
//                .cookies(cookies_ut).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36")
//                .data("username",username)
//                .data("password", Base64.encodeToString(password.getBytes(),Base64.DEFAULT))
//                .data("lt",lt)
//                .data("Connection","keep-alive")
//                .data("_eventId","submit").post();
//        //System.out.println(after);
//        //Log.e("cookies", String.valueOf(cookies_ut));
//       if (after.toString().contains("姓 名：")) return "成功";
//       else if(after.toString().contains("您已在别的终端登录"))
//       {
//           Log.e("UT","多终端登录，改变链接");
//           Document after2 =  Jsoup.connect(UT_login_url)
//                   .cookies(cookies_ut).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36")
//                   .data("username",username)
//                   .data("password", Base64.encodeToString(password.getBytes(),Base64.DEFAULT))
//                   .data("Connection","keep-alive")
//                   .data("lt",lt)
//                   .data("continueLogin","1")
//                   .data("_eventId","submit").post();
//           if (after2.toString().contains("姓 名：")) return "成功";
//           else return "失败";
//       }
//        else return "失败";
//    }
//
//
//    class loadSafeCodeTask extends AsyncTask<String, Integer, Integer> {
//
//        WeakReference<ActivityLoginUT> weakReference;
//
//        loadSafeCodeTask(ActivityLoginUT at) {
//            weakReference = new WeakReference<>(at);
//        }
//
//        @Override
//        protected Integer doInBackground(String... strings) {
//            try {
//
//                Connection.Response response = Jsoup.connect(UT_login_url)
//                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
//                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
//                        .header("Content-Type", "application/x-www-form-urlencoded")
//                        .timeout(5000).execute();
//                //得到系统返回的Cookies
//                if(isCancelled()) return -1;
//                cookies_ut.clear();
//                cookies_ut.putAll(response.cookies());
//                Document d = Jsoup.connect(UT_login_url)
//                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
//                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
//                        .header("Content-Type", "application/x-www-form-urlencoded")
//                        .cookies(cookies_ut).get();
//                // System.out.println(d);
//                lt = d.getElementsByAttributeValue("name","lt").first().attr("value");
//                System.out.println(lt);
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                return -1;
//            }
//            return lt==null?-1:1;
//        }
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            final ActivityLoginUT at = weakReference.get();
//            at.loginCard.setVisibility(View.INVISIBLE);
//            at.loadingError.setVisibility(View.INVISIBLE);
//            at.loadingView.setVisibility(View.VISIBLE);
//            at.loadingView.post(new Runnable() {
//                @Override
//                public void run() {
//                    MaterialCircleAnimator.animShow(at.loadingView, 400);
//                }
//            });
//        }
//
//
//        @SuppressLint("WrongThread")
//        @Override
//        protected void onPostExecute(Integer i) {
//            super.onPostExecute(i);
//
//            final ActivityLoginUT at = weakReference.get();
//            if (at == null || at.isDestroyed() || at.isFinishing()) return;
//
//            if (i < 0) {
//                at.loadingError.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        at.loadingView.setVisibility(View.GONE);
//                        MaterialCircleAnimator.animShow(at.loadingError, 500);
//                    }
//                });
//                at.loginCard.setVisibility(View.INVISIBLE);
//
//            } else {
//                at.loginCard.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        at.loadingView.setVisibility(View.GONE);
//                        MaterialCircleAnimator.animShow(at.loginCard, 600);
//                    }
//                });
//                //safecode.setText(safecode_recognized);
//            }
//            String un = defaultSP.getString("ut_username",null);
//            if (un==null&&CurrentUser != null) {
//                at.username.setText("333"+CurrentUser.getStudentnumber());
//                at.password.setText(defaultSP.getString("333"+CurrentUser.getStudentnumber() + ".password", ""));
//            }else if(un!=null){
//                at.username.setText(un);
//                at.password.setText(defaultSP.getString(un + ".password", ""));
//
//            }
//
//
//        }
//    }
//
//
//    static class loginTask extends AsyncTask<String, Integer, String> {
//
//        String lt,username, password;
//        WeakReference<ActivityLoginUT> weakReference;
//
//        boolean toast;
//
//        loginTask(ActivityLoginUT at, String lt,String username, String password, boolean toast) {
//            weakReference = new WeakReference(at);
//            this.username = username;
//            this.lt = lt;
//            this.password = password;
//            this.toast = toast;
//        }
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            weakReference.get().login.setProgress(true);
//        }
//
//        @Override
//        protected String doInBackground(String... strings) {
//            try {
//                return weakReference.get().loginCheck(username, password,lt);
//            } catch (Exception e) {
//                e.printStackTrace();
//                return "失败";
//            }
//        }
//
//        @Override
//        protected void onPostExecute(String s) {
//            super.onPostExecute(s);
//            ActivityLoginUT at = weakReference.get();
//            if (at == null || at.isFinishing() || at.isDestroyed()) return;
//            at.login.setProgress(false);
//            if (s.contains("成功")) {
//                //cookies_ut.put("sso_username_cookie",username);
//                //cookies_ut.put("sso_password_cookie",password);
//                HITAApplication.login_ut = true;
//                ut_username = username;
//                Intent i = new Intent(weakReference.get(),ActivityUTService.class);
//                i.putExtra("username",username);
//                weakReference.get().startActivity(i);
//                defaultSP.edit().putString("ut_username",username).putString(username + ".password", password)
//                        .putString("ut_cookies",new Gson().toJson(cookies_ut))
//                        .apply();
//                weakReference.get().finish();
//            } else {
//                HITAApplication.login_ut = false;
//                ut_username = null;
//                //String x = Jsoup.parse(s).getElementsByTag("script").toString();
//                if (toast) {
//                    AlertDialog ad = new AlertDialog.Builder(at)
//                            .setTitle("提示")
//                            .setMessage("登录失败")
//                            .setPositiveButton("再来一次", null).create();
//                    ad.show();
//                }
//                //new loadSafeCodeTask(ActivityLoginUT.this).execute();
//
//            }
//        }
//    }
//
//
//
//
//
//
//}
//
//
//
//
//
