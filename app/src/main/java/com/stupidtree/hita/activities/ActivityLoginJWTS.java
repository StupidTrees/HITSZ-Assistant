package com.stupidtree.hita.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.HttpsConnection;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.core.Curriculum;
import com.stupidtree.hita.diy.ButtonLoading;
import com.stupidtree.hita.diy.MaterialCircleAnimator;

import org.apache.http.impl.client.HttpClients;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;


import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;
import static com.stupidtree.hita.HITAApplication.*;

public class ActivityLoginJWTS extends BaseActivity {
    //登录请求地址
    private final static String LOGIN = "http://jwts.hitsz.edu.cn/login";
    //登录界面
    private final static String LOGIN_VIEW = "http://jwts.hitsz.edu.cn/";
    //验证码请求地址
    private final static String CHECK_CODE = "http://jwts.hitsz.edu.cn/captchaImage";
    //固定的参数值（URL编码）

    ImageView safeCodeImage;
    EditText username, password, safecode;
    ButtonLoading login;
    ProgressBar loadingView;
    LinearLayout loginCard;
    TextView loadingError;
    Button bt_vpn;
    //验证码
    private byte[] checkPic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true, true, false);
        setContentView(R.layout.activity_login_jwts);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        safeCodeImage = findViewById(R.id.safecode_img);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        safecode = findViewById(R.id.safecode);
        login = findViewById(R.id.login);
        loginCard = findViewById(R.id.logincard);
        loadingView = findViewById(R.id.loadingview);
        bt_vpn = findViewById(R.id.bt_vpn);
        loadingError = findViewById(R.id.loadingerror);
        login.setOnButtonLoadingListener(new ButtonLoading.OnButtonLoadingListener() {
            @Override
            public void onClick() {
                new loginTask(ActivityLoginJWTS.this, username.getText().toString(), password.getText().toString(), safecode.getText().toString(), true).executeOnExecutor(THREAD_POOL_EXECUTOR);
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onFinish() {

            }
        });
        bt_vpn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://vpn.hitsz.edu.cn");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                ActivityLoginJWTS.this.startActivity(intent);
            }
        });

        new loadSafeCodeTask(this).executeOnExecutor(THREAD_POOL_EXECUTOR);
        //new directlyLoginTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    //模拟访问登录界面
    public int getCookiesFromJwc() {
        HITAApplication.login = false;
        try {
            //第一次访问登录界面
            Connection.Response response = Jsoup.connect(LOGIN_VIEW).timeout(30000).execute();
            //得到系统返回的Cookies
            cookies = (HashMap<String, String>) response.cookies();
            //Log.e("cookie:",cookies.toString()+" ");
            //请求获得验证码的内容
            checkPic = Jsoup.connect(CHECK_CODE).cookies(cookies).ignoreContentType(true).execute().bodyAsBytes();
        } catch (IOException e) {
            return -1;
        }
        if (checkPic.length == 0 || cookies.size() == 0) {
            return -1;
        }
        return 0;
    }

    public int getCookiesFromJwc_vpn() {
        trustEveryone();
        HITAApplication.login = false;
        try {
            //第一次访问登录界面
            Connection.Response response = Jsoup.connect("https://sso.hitsz.edu.cn:7002/cas/login?service=https%3A%2F%2Fvpn.hitsz.edu.cn%2Fpor%2Fcas_sso.csp%3Fmode%3Dbs")
                    .timeout(30000)
                    .ignoreHttpErrors(true)
                    .referrer("https://vpn.hitsz.edu.cn/")
                    .header("Host", "sso.hitsz.edu.cn:7002")
                    .header("Upgrade-Insecure-Requests", "1")
                    //.header("Connection","keep-alive")
                    .header("User-Agent", "Mozilla/5.0 (Linux; U; Android 9; zh-cn; MIX 2S Build/PKQ1.180729.001) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/71.0.3578.141 Mobile Safari/537.36 XiaoMi/MiuiBrowser/10.8.5")
                    .execute();
            cookies = (HashMap<String, String>) response.cookies();
            Log.e("!!",cookies.toString()+"?");
            Document vpnPage = Jsoup.connect("https://sso.hitsz.edu.cn:7002/cas/login?service=https%3A%2F%2Fvpn.hitsz.edu.cn%2Fpor%2Fcas_sso.csp%3Fmode%3Dbs")
                    .timeout(30000)
                    .cookies(cookies)
                    .ignoreHttpErrors(true)
                    .referrer("https://sso.hitsz.edu.cn:7002/cas/login?service=https%3A%2F%2Fvpn.hitsz.edu.cn%2Fpor%2Fcas_sso.csp%3Fmode%3Dbs")
                    .header("Host", "sso.hitsz.edu.cn:7002")
                    .header("Origin","https://sso.hitsz.edu.cn:700")
                    .header("Upgrade-Insecure-Requests", "1")
                    //.header("Connection","keep-alive")
                    .header("User-Agent", "Mozilla/5.0 (Linux; U; Android 9; zh-cn; MIX 2S Build/PKQ1.180729.001) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/71.0.3578.141 Mobile Safari/537.36 XiaoMi/MiuiBrowser/10.8.5")
                    .data("_eventId","submit")
                    .data("username","180110324")
                    .data("password", "www,2012.com")
                   // .data("lt","LT-39151-p7ySeTbx1VyD3KGdMryqvllsOObevA-cas")
                    //.data("execution","e3s1")
                    .post();
            System.out.println(vpnPage);
            Document vpnPage2 = Jsoup.connect("https://vpn.hitsz.edu.cn/por/cas_sso.csp?mode=bs&ticket=ST-23814-UkMjlJ3fMjDql72fcfcl-cas")
                    .timeout(30000)
                    .cookies(cookies)
                    .ignoreHttpErrors(true)
                    .header("Connection","keep-alive")
                    .referrer("https://sso.hitsz.edu.cn:7002/cas/login?service=https%3A%2F%2Fvpn.hitsz.edu.cn%2Fpor%2Fcas_sso.csp%3Fmode%3Dbs")
                    .header("Host", "vpn.hitsz.edu.cn")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", "Mozilla/5.0 (Linux; U; Android 9; zh-cn; MIX 2S Build/PKQ1.180729.001) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/71.0.3578.141 Mobile Safari/537.36 XiaoMi/MiuiBrowser/10.8.5")
                    .get();
            Log.e("!!3",cookies.toString()+"?");
            System.out.println(vpnPage2);
            Document loginPage = Jsoup.connect("https://vpn.hitsz.edu.cn/web/1/http/0/jwts.hitsz.edu.cn:80/")
                    .timeout(30000)
                    .cookies(cookies)
                    .ignoreHttpErrors(true)
                    .referrer("https://vpn.hitsz.edu.cn/por/service.csp")
                    .header("Host", "vpn.hitsz.edu.cn")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", "Mozilla/5.0 (Linux; U; Android 9; zh-cn; MIX 2S Build/PKQ1.180729.001) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/71.0.3578.141 Mobile Safari/537.36 XiaoMi/MiuiBrowser/10.8.5")
                    .get();
            System.out.println(loginPage);
            Log.e("!!4",cookies.toString()+"?");
            //请求获得验证码的内容
           checkPic =  Jsoup.connect("https://vpn.hitsz.edu.cn/web/0/http/1/jwts.hitsz.edu.cn/captchaImage").cookies(cookies).
                    header("Host", "vpn.hitsz.edu.cn")
                    .referrer("https://vpn.hitsz.edu.cn/web/1/http/0/jwts.hitsz.edu.cn:80/")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36")
                    .ignoreHttpErrors(true).ignoreContentType(true).execute().bodyAsBytes();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        if (checkPic.length == 0 || cookies.size() == 0) {
            return -1;
        }
        return 0;
    }

    public void presentActivity(Activity activity, View view) {
//        int revealX,revealY;
//        if(view==null){
//             revealX= 0;
//            revealY = 0;
//        }else{
//            revealX = (int) (view.getX() + view.getWidth() / 2);
//            revealY = (int) (view.getY() + view.getHeight() / 2);
//        }
        Intent intent = new Intent(this, ActivityJWTS.class);
//        intent.putExtra(ActivityJWTS.EXTRA_CIRCULAR_REVEAL_X, revealX);
//        intent.putExtra(ActivityJWTS.EXTRA_CIRCULAR_REVEAL_Y, revealY);

        if (view != null) view.setVisibility(View.VISIBLE);
        ActivityCompat.startActivity(activity, intent, null);
//        overridePendingTransition(0, 0);
        finish();
    }

    //登录\爬取
    public String loginCheck(String username, String password, String checkCode) throws IOException {
        Document login = Jsoup.connect(LOGIN).cookies(cookies).timeout(30000)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .data("usercode", username).data("password", password).data("code", checkCode).
                        ignoreContentType(true).post();
        //System.out.println(login);
        if (login.toString().contains("alert('")) {
            return "ALT:" + login.toString().substring(login.toString().indexOf("alert('") + 7, login.toString().indexOf("\')", login.toString().indexOf("alert(\'")));
        } else if (login.toString().contains("欢迎使用")) return "成功";
        else return "失败";
    }


    static class loadSafeCodeTask extends AsyncTask<String, Integer, Integer> {

        WeakReference<ActivityLoginJWTS> weakReference;

        loadSafeCodeTask(ActivityLoginJWTS at) {
            weakReference = new WeakReference<>(at);
        }

        @Override
        protected Integer doInBackground(String... strings) {
            return weakReference.get().getCookiesFromJwc();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            final ActivityLoginJWTS at = weakReference.get();
            at.loginCard.setVisibility(View.INVISIBLE);
            at.loadingError.setVisibility(View.INVISIBLE);
            at.loadingView.setVisibility(View.VISIBLE);
            at.loadingView.post(new Runnable() {
                @Override
                public void run() {
                    MaterialCircleAnimator.animShow(at.loadingView, 400);
                }
            });
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            final ActivityLoginJWTS at = weakReference.get();
            at.loadingView.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Integer i) {
            super.onPostExecute(i);
            final ActivityLoginJWTS at = weakReference.get();
            if (at == null || at.isDestroyed() || at.isFinishing()) return;

            if (i < 0) {
                at.loadingError.post(new Runnable() {
                    @Override
                    public void run() {
                        at.loadingView.setVisibility(View.GONE);
                        MaterialCircleAnimator.animShow(at.loadingError, 500);
                    }
                });
                at.loginCard.setVisibility(View.INVISIBLE);

            } else {
                Glide.with(at).load(at.checkPic).into(at.safeCodeImage);
                at.loginCard.post(new Runnable() {
                    @Override
                    public void run() {
                        at.loadingView.setVisibility(View.GONE);
                        MaterialCircleAnimator.animShow(at.loginCard, 600);
                    }
                });
            }
            if (CurrentUser != null) {
                at.username.setText(CurrentUser.getStudentnumber());
                at.password.setText(PreferenceManager.getDefaultSharedPreferences(HContext).getString(CurrentUser.getStudentnumber() + ".password", ""));
            }
        }
    }

    static class loginTask extends AsyncTask<String, Integer, String> {

        String username, password, safecode;
        WeakReference<ActivityLoginJWTS> weakReference;

        boolean toast;

        loginTask(ActivityLoginJWTS at, String username, String password, String safecode, boolean toast) {
            weakReference = new WeakReference(at);
            this.username = username;
            this.password = password;
            this.safecode = safecode;
            this.toast = toast;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            weakReference.get().login.setProgress(true);
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                return weakReference.get().loginCheck(username, password, safecode);
            } catch (IOException e) {
                e.printStackTrace();
                return "失败";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ActivityLoginJWTS at = weakReference.get();
            if (at == null || at.isFinishing() || at.isDestroyed()) return;
            at.login.setProgress(false);
            if (s.startsWith("ALT:")) {
                HITAApplication.login = false;
                AlertDialog ad = new AlertDialog.Builder(at).create();
                ad.setTitle("来自教务系统滴友好提示");
                ad.setMessage(s.substring(4));
                ad.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                ad.show();
            } else if (s.contains("成功")) {
                HITAApplication.login = true;
                at.presentActivity(at, at.login);
                if (CurrentUser != null)
                    PreferenceManager.getDefaultSharedPreferences(HContext).edit().putString(CurrentUser.getStudentnumber() + ".password", password).commit();
                //PreferenceManager.getDefaultSharedPreferences(HContext).edit().putString("username",username).commit();
                //PreferenceManager.getDefaultSharedPreferences(HContext).edit().putString("userpassword",password).commit();
                //finish();
            } else {
                HITAApplication.login = false;
                //String x = Jsoup.parse(s).getElementsByTag("script").toString();
                if (toast) {
                    AlertDialog ad = new AlertDialog.Builder(at)
                            .setTitle("教务系统提示")
                            .setMessage("登录失败惹")
                            .setPositiveButton("再来一次", null).create();
                    ad.show();
                }

            }
        }
    }
//
//    class directlyLoginTask extends AsyncTask{
//
//        @Override
//        protected Object doInBackground(Object[] objects) {
//            if(cookies!=null&&cookies.size()>0){
//                Document login = null;
//                try {
//                    login = Jsoup.connect(LOGIN).cookies(cookies).timeout(60000)
//                            .header("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
//                            .header("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
//                            .header("Content-Type","application/x-www-form-urlencoded").
//                                    ignoreContentType(true).post();
//                } catch (IOException e) {
//                    return false;
//                }
//                 if(login.toString().contains("欢迎使用")) return true;
//                else return false;
//            }else{
//                return false;
//            }
//
//        }
//
//        @Override
//        protected void onPostExecute(Object o) {
//            super.onPostExecute(o);
//                Boolean b = (Boolean) o;
//                if (b) {
//                    presentActivity(ActivityLoginJWTS.this,login);
//                   // finish();
//                } else {
//                    setContentView(R.layout.activity_login_jwts);
//                    Toolbar toolbar = findViewById(R.id.toolbar);
//                    setSupportActionBar(toolbar);
//                    setSupportActionBar(toolbar);
//                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
//                    getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
//                    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            onBackPressed();
//                        }
//                    });
//                    safeCodeImage = findViewById(R.id.safecode_img);
//                    username = findViewById(R.id.username);
//                    password = findViewById(R.id.password);
//                    safecode = findViewById(R.id.safecode);
//                    login = findViewById(R.id.login);
//                    loginCard = findViewById(R.id.logincard);
//                    loadingView = findViewById(R.id.loadingview);
//                    loadingError = findViewById(R.id.loadingerror);
//                    login.setOnButtonLoadingListener(new ButtonLoading.OnButtonLoadingListener() {
//                        @Override
//                        public void onClick() {
//                            new loginTask(username.getText().toString(), password.getText().toString(), safecode.getText().toString(), true).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//                        }
//
//                        @Override
//                        public void onStart() {
//
//                        }
//
//                        @Override
//                        public void onFinish() {
//
//                        }
//                    });
//
//                    new loadSafeCodeTask().executeOnExecutor(THREAD_POOL_EXECUTOR);
//                }
//            }
//
//    }

    public static void trustEveryone() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }
}





