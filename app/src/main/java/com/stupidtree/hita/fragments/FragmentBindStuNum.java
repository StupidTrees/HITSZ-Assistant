package com.stupidtree.hita.fragments;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.diy.ButtonLoading;
import com.stupidtree.hita.diy.MaterialCircleAnimator;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;
import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.cookies;
import static com.stupidtree.hita.HITAApplication.themeID;

public class FragmentBindStuNum extends BottomSheetDialogFragment {
    //登录请求地址
    private final   static String LOGIN = "http://jwts.hitsz.edu.cn/login";
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
    //验证码
    private byte[] checkPic;
    TextView currentUserName;
    loadSafeCodeTask pageTask_safecode;
    loginTask pageTask_login;
    BindStuNumCallBackListener mBindStuNumCallBackListener;
    
    
    interface BindStuNumCallBackListener{
        void callBack(String stuNum);
    }

    protected void stopTasks() {
        if(pageTask_safecode!=null&&!pageTask_safecode.isCancelled())pageTask_safecode.cancel(true);
        if(pageTask_login!=null&&!pageTask_login.isCancelled()) pageTask_login.cancel(true);
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
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(getContext(),themeID);// your app theme here
        View v = inflater.cloneInContext(contextThemeWrapper).inflate(R.layout.fragment_bind_student_number, container, false);
        initViews(v);
       return v;
    }

    void initViews(View v){
        safeCodeImage = v.findViewById(R.id.safecode_img);
        username = v.findViewById(R.id.username);
        password = v.findViewById(R.id.password);
        safecode = v.findViewById(R.id.safecode);
        login = v.findViewById(R.id.login);
        currentUserName = v.findViewById(R.id.current_user_name);
        loginCard = v.findViewById(R.id.logincard);
        loadingView = v.findViewById(R.id.loadingview);
        loadingError = v.findViewById(R.id.loadingerror);
        if(CurrentUser!=null) currentUserName.setText(CurrentUser.getUsername()+"（"+CurrentUser.getUsername()+"）");
        login.setOnButtonLoadingListener(new ButtonLoading.OnButtonLoadingListener() {
            @Override
            public void onClick() {
                if(pageTask_login!=null&&!pageTask_login.isCancelled()) pageTask_login.cancel(true);
                pageTask_login = new loginTask( username.getText().toString(), password.getText().toString(), safecode.getText().toString(), true);
                pageTask_login.executeOnExecutor(THREAD_POOL_EXECUTOR);

            }

            @Override
            public void onStart() {

            }

            @Override
            public void onFinish() {

            }
        });

        if(pageTask_safecode!=null&&!pageTask_safecode.isCancelled()) pageTask_safecode.cancel(true);
        pageTask_safecode = new loadSafeCodeTask();
        pageTask_safecode.executeOnExecutor(THREAD_POOL_EXECUTOR);
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
                return loginCheck(username, password, safecode);
            } catch (IOException e) {
                e.printStackTrace();
                return "失败";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            login.setProgress(false);
            if (s.startsWith("ALT:")) {
                HITAApplication.login = false;
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
               if(mBindStuNumCallBackListener!=null) mBindStuNumCallBackListener.callBack(username);

            } else {
                HITAApplication.login = false;
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTasks();
    }
}





