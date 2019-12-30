package com.stupidtree.hita.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;
import com.stupidtree.hita.diy.ButtonLoading;
import com.stupidtree.hita.diy.MaterialCircleAnimator;
import com.stupidtree.hita.util.SafecodeUtil;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;


import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import static com.stupidtree.hita.HITAApplication.*;
import static com.stupidtree.hita.util.SafecodeUtil.splitBitmapInto;

public class ActivityLoginJWTS extends BaseActivity {
    //登录请求地址
    private final static String LOGIN = "http://jwts.hitsz.edu.cn:8080/login";
    //登录界面
    private final static String LOGIN_VIEW = "http://jwts.hitsz.edu.cn:8080/";
    //验证码请求地址
    private final static String CHECK_CODE = "http://jwts.hitsz.edu.cn:8080/captchaImage";
    //固定的参数值（URL编码）

    ImageView safeCodeImage;
    EditText username, password, safecode;
    ButtonLoading login;
    ProgressBar loadingView;
    LinearLayout loginCard;
    TextView loadingError;
   // Button bt_vpn;
    //验证码
    private byte[] checkPic;
    loadSafeCodeTask pageTask_safecode;
    loginTask pageTask_login;

    @Override
    protected void stopTasks() {
        if (pageTask_safecode != null && pageTask_safecode.getStatus()!=AsyncTask.Status.FINISHED)
            pageTask_safecode.cancel(true);
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
        safeCodeImage = findViewById(R.id.safecode_img);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        safecode = findViewById(R.id.safecode);
        login = findViewById(R.id.login);
        loginCard = findViewById(R.id.logincard);
        loadingView = findViewById(R.id.loadingview);
      //  bt_vpn = findViewById(R.id.bt_vpn);
        loadingError = findViewById(R.id.loadingerror);
        login.setOnButtonLoadingListener(new ButtonLoading.OnButtonLoadingListener() {
            @Override
            public void onClick() {
                if (pageTask_login != null && pageTask_login.getStatus()!=AsyncTask.Status.FINISHED)
                    pageTask_login.cancel(true);
                pageTask_login = new loginTask(ActivityLoginJWTS.this, username.getText().toString(), password.getText().toString(), safecode.getText().toString(), true);
                pageTask_login.executeOnExecutor(TPE);
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
        pageTask_safecode = new loadSafeCodeTask(this);
        pageTask_safecode.executeOnExecutor(TPE);
        //new directlyLoginTask().executeOnExecutor(HITAApplication.TPE);
    }

    //模拟访问登录界面
    public int getCookiesFromJwc() {
        login_jwts = false;
        try {
            //第一次访问登录界面
            Connection.Response response = Jsoup.connect(LOGIN_VIEW).timeout(5000).execute();
            //得到系统返回的Cookies
            cookies_jwts.clear();
            cookies_jwts.putAll(response.cookies());
            //Log.e("cookie:",cookies_jwts.toString()+" ");
            //请求获得验证码的内容
            checkPic = Jsoup.connect(CHECK_CODE).cookies(cookies_jwts).ignoreContentType(true).execute().bodyAsBytes();
        } catch (Exception e) {
            return -1;
        }
        if (checkPic.length == 0 || cookies_jwts.size() == 0) {
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
    public static String loginCheck(String username, String password, String checkCode) throws IOException {
        Document login = Jsoup.connect(LOGIN).cookies(cookies_jwts).timeout(5000)
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

        WeakReference<ActivityLoginJWTS> weakReference;
        String safecode_recognized = null;

        loadSafeCodeTask(ActivityLoginJWTS at) {
            weakReference = new WeakReference<>(at);
        }

        @Override
        protected Integer doInBackground(String... strings) {
            int m = weakReference.get().getCookiesFromJwc();
            try {
                File dict = new File( HContext.getExternalFilesDir("")+"/tessdata/eng.traineddata");
                String path = HContext.getExternalFilesDir("") + "/tessdata/";
                File f = new File(path);
                f.mkdirs();
                if(!dict.exists()){
                    copyAssetsSingleFile(f,"eng.traineddata");
                }
                TessBaseAPI baseApi = new TessBaseAPI();
                //记得要在你的sd卡的tessdata文件夹下放对应的字典文件,例如我这里就放的是custom.traineddata
                baseApi.init(f.getParent(), "eng");
                baseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "0123456789ABCDEF");
                baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_CHAR);
                Bitmap bm = BitmapFactory.decodeByteArray(checkPic, 0, checkPic.length);
                Bitmap res = SafecodeUtil.getProcessedBitmap(bm);
                // Glide.with(ActivityLoginJWTS.this).load(res).into(safeCodeImage);
                StringBuilder result = new StringBuilder();
                for (Bitmap r : splitBitmapInto(res, 4,-6)) {
                    baseApi.setImage(r);
                    final String x = baseApi.getUTF8Text();
                    result.append(x);
                }
                //这里，你可以把result的值赋值给你的TextView
                Log.e("result", String.valueOf(result));
                safecode_recognized = result.toString();
                baseApi.end();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return m;
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

        @SuppressLint("WrongThread")
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
                safecode.setText(safecode_recognized);
            }
            if (CurrentUser != null) {
                at.username.setText(CurrentUser.getStudentnumber());
                at.password.setText(defaultSP.getString(CurrentUser.getStudentnumber() + ".password", ""));
            }


        }
    }

    public static Bitmap binarization(Bitmap img) {
        int width, height;
        width = img.getWidth();
        height = img.getHeight();
        int area = width * height;
        int gray[][] = new int[width][height];
        int average = 0;// 灰度平均值
        int graysum = 0;
        int graymean = 0;
        int grayfrontmean = 0;
        int graybackmean = 0;
        int pixelGray;
        int front = 0;
        int back = 0;
        int[] pix = new int[width * height];
        img.getPixels(pix, 0, width, 0, 0, width, height);
        for (int i = 1; i < width; i++) { // 不算边界行和列，为避免越界
            for (int j = 1; j < height; j++) {
                int x = j * width + i;
                int r = (pix[x] >> 16) & 0xff;
                int g = (pix[x] >> 8) & 0xff;
                int b = pix[x] & 0xff;
                pixelGray = (int) (0.3 * r + 0.59 * g + 0.11 * b);// 计算每个坐标点的灰度
                gray[i][j] = (pixelGray << 16) + (pixelGray << 8) + (pixelGray);
                graysum += pixelGray;
            }
        }
        graymean = (int) (graysum / area);// 整个图的灰度平均值
        average = graymean;
        for (int i = 0; i < width; i++) // 计算整个图的二值化阈值
        {
            for (int j = 0; j < height; j++) {
                if (((gray[i][j]) & (0x0000ff)) < graymean) {
                    graybackmean += ((gray[i][j]) & (0x0000ff));
                    back++;
                } else {
                    grayfrontmean += ((gray[i][j]) & (0x0000ff));
                    front++;
                }
            }
        }
        int frontvalue = (int) (grayfrontmean / front);// 前景中心
        int backvalue = (int) (graybackmean / back);// 背景中心
        float G[] = new float[frontvalue - backvalue + 1];// 方差数组
        int s = 0;
        //  Log.i(TAG, "Front:" + front + "**Frontvalue:" + frontvalue + "**Backvalue:" + backvalue);
        for (int i1 = backvalue; i1 < frontvalue + 1; i1++)// 以前景中心和背景中心为区间采用大津法算法（OTSU算法）
        {
            back = 0;
            front = 0;
            grayfrontmean = 0;
            graybackmean = 0;
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    if (((gray[i][j]) & (0x0000ff)) < (i1 + 1)) {
                        graybackmean += ((gray[i][j]) & (0x0000ff));
                        back++;
                    } else {
                        grayfrontmean += ((gray[i][j]) & (0x0000ff));
                        front++;
                    }
                }
            }
            grayfrontmean = (int) (grayfrontmean / front);
            graybackmean = (int) (graybackmean / back);
            G[s] = (((float) back / area) * (graybackmean - average)
                    * (graybackmean - average) + ((float) front / area)
                    * (grayfrontmean - average) * (grayfrontmean - average));
            s++;
        }
        float max = G[0];
        int index = 0;
        for (int i = 1; i < frontvalue - backvalue + 1; i++) {
            if (max < G[i]) {
                max = G[i];
                index = i;
            }
        }


        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int in = j * width + i;
                if (((gray[i][j]) & (0x0000ff)) < (index + backvalue)) {
                    pix[in] = Color.rgb(0, 0, 0);
                } else {
                    pix[in] = Color.rgb(255, 255, 255);
                }
            }
        }

        Bitmap temp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        temp.setPixels(pix, 0, width, 0, 0, width, height);
        return temp;
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
            } catch (Exception e) {
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
                login_jwts = false;
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
                login_jwts = true;
                at.presentActivity(at, at.login);
                if (CurrentUser != null)
                    defaultSP.edit().putString(CurrentUser.getStudentnumber() + ".password", password).commit();
                //defaultSP.edit().putString("username",username).commit();
                //defaultSP.edit().putString("userpassword",password).commit();
                //finish();
            } else {
                login_jwts = false;
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
