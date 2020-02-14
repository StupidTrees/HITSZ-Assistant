package com.stupidtree.hita.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;
import com.stupidtree.hita.diy.ButtonLoading;
import com.stupidtree.hita.jw.JWException;
import com.stupidtree.hita.util.ActivityUtils;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;


import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import cn.bmob.v3.BmobArticle;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

import static com.stupidtree.hita.HITAApplication.*;

public class ActivityLoginJWTS extends BaseActivity {
    //登录请求地址
    private final static String LOGIN = "http://jwts.hitsz.edu.cn:8080/login";
    //登录界面
    private final static String LOGIN_VIEW = "http://jwts.hitsz.edu.cn:8080/";
    //验证码请求地址
    private final static String CHECK_CODE = "http://jwts.hitsz.edu.cn:8080/captchaImage";
    //固定的参数值（URL编码）

    EditText username, password;
    ButtonLoading login;
    LinearLayout loginCard;
    TextView loadingError;
    String lt;
    String execution;
    ImageView vpnHint;
   // Button bt_vpn;
    //验证码
    private byte[] checkPic;

    loginTask pageTask_login;

    @Override
    protected void stopTasks() {
        if (pageTask_login != null && pageTask_login.getStatus()!=AsyncTask.Status.FINISHED) pageTask_login.cancel(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true, false, false);
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
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        vpnHint = findViewById(R.id.vpn);
        loginCard = findViewById(R.id.logincard);
        loadingError = findViewById(R.id.loadingerror);
        vpnHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BmobQuery<BmobArticle> bq = new BmobQuery<>();
                bq.addWhereEqualTo("objectId","\tVkLlSSSW");
                bq.findObjects(new FindListener<BmobArticle>() {
                    @Override
                    public void done(List<BmobArticle> list, BmobException e) {
                        String url;
                        if(e==null&&list!=null&&list.size()>0) url = list.get(0).getUrl();
                        else url = "http://files.hita.store/2020/02/14/ae00758f40817fab808e32dbf3aea7eb.html";
                        ActivityUtils.openInBrowser(ActivityLoginJWTS.this,url);
                    }
                });
            }
        });
        login.setOnButtonLoadingListener(new ButtonLoading.OnButtonLoadingListener() {
            @Override
            public void onClick() {
                if (pageTask_login != null && pageTask_login.getStatus()!=AsyncTask.Status.FINISHED)
                    pageTask_login.cancel(true);
                pageTask_login = new loginTask(username.getText().toString(), password.getText().toString(), true);
                pageTask_login.executeOnExecutor(TPE);
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onFinish() {

            }
        });
        if(CurrentUser!=null&& !TextUtils.isEmpty(CurrentUser.getStudentnumber())){
            username.setText(CurrentUser.getStudentnumber());
            password.setText(defaultSP.getString(CurrentUser.getStudentnumber()+".password",""));
        }
        //new directlyLoginTask().executeOnExecutor(HITAApplication.TPE);
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


    class loginTask extends AsyncTask<Object, Integer, Object> {

        String username, password;
        boolean toast;

        loginTask(String username, String password, boolean toast) {
            this.username = username;
            this.password = password;
            this.toast = toast;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            login.setProgress(true);
        }

        @Override
        protected Object doInBackground(Object... strings) {
            try {
                return jwCore.login(username,password);
            } catch (JWException e) {
              return e;
            }
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            login.setProgress(false);
            if(o instanceof JWException){
                JWException jwe = (JWException) o;
                AlertDialog ad = new AlertDialog.Builder(ActivityLoginJWTS.this).create();
                ad.setTitle("提示");
                String message = "登录失败！";
                if(jwe.getType()==JWException.CONNECT_ERROR) message = "网络连接错误";
                else if(jwe.getType()==JWException.LOGIN_FAILED||jwe.getType()==JWException.FORMAT_ERROR) message = "登录失败！";
                else if(jwe.getType()==JWException.DIALOG_MESSAGE) message = jwe.getDialogMessage();
                ad.setMessage(message);
                ad.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                ad.show();
            }
            else if(o instanceof Boolean){
                if ((boolean)o) {
                    presentActivity(ActivityLoginJWTS.this,login);
                    if (CurrentUser != null)
                        defaultSP.edit().putString(CurrentUser.getStudentnumber() + ".password", password).commit();
                    //defaultSP.edit().putString("username",username).commit();
                    //defaultSP.edit().putString("userpassword",password).commit();
                    //finish();
                } else {
                    if (toast) {
                        AlertDialog ad = new AlertDialog.Builder(ActivityLoginJWTS.this)
                                .setTitle("提示")
                                .setMessage("登录失败,请检查账号密码")
                                .setPositiveButton("好哒", null).create();
                        ad.show();
                    }

                }
            }

        }
    }


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
