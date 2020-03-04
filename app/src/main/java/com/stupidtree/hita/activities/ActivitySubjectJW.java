package com.stupidtree.hita.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;
import com.stupidtree.hita.jw.JWException;
import com.stupidtree.hita.online.DownloadService;
import com.stupidtree.hita.online.DownloadTask;
import com.stupidtree.hita.util.ActivityUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.stupidtree.hita.HITAApplication.TPE;
import static com.stupidtree.hita.HITAApplication.jwCore;

public class ActivitySubjectJW extends BaseActivity {

    String subjectId;

    TextView name, name_en, code, type, credit, department, language, tag;
    TextView description, description_en, guideline, guideline_en;
    RecyclerView list_xs, list_team;
    List<Map<String, String>> listRes_xs, listRes_team;
    XSListAdapter listAdapter_xs;
    TeamAdapter listAdapter_team;

    String nameS, name_enS, codeS, typeS, creditS, departmentS, languageS, tagS;
    String descriptionS, description_enS, guidelineS, guideline_enS;
    String guidelineUrl, guidelineUrl_en;

    SwipeRefreshLayout refresh;
    refreshTask pageTask;
    DownloadService.DownloadBinder mBinder;
    ServiceConnection mConnection;

    @Override
    protected void stopTasks() {
        if (pageTask != null && !pageTask.isCancelled()) pageTask.cancel(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true, true, false);
        subjectId = getIntent().getStringExtra("subject_id");
        setContentView(R.layout.activity_subject_jw);
        initToolbar();
        initViews();
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mBinder = (DownloadService.DownloadBinder) service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };

        Intent intent = new Intent(getThis(), DownloadService.class);
        startService(intent);
        bindService(intent, mConnection, BIND_AUTO_CREATE);

    }

    void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.subject_jw_title));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }


    @Override
    protected void onResume() {
        super.onResume();
        Refresh();
    }

    void initViews() {
        refresh = findViewById(R.id.refresh);
        refresh.setColorSchemeColors(getColorAccent(), getColorFade());
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Refresh();
            }
        });
        name = findViewById(R.id.name);
        name_en = findViewById(R.id.name_en);
        code = findViewById(R.id.code);
        type = findViewById(R.id.type);
        credit = findViewById(R.id.credit);
        department = findViewById(R.id.department);
        language = findViewById(R.id.lang);
        tag = findViewById(R.id.tag);
        description = findViewById(R.id.description);
        description_en = findViewById(R.id.description_en);
        guideline = findViewById(R.id.guideline);
        guideline_en = findViewById(R.id.guideline_en);
        list_xs = findViewById(R.id.list_xfxs);
        list_team = findViewById(R.id.list_team);
        listRes_team = new ArrayList<>();
        listRes_xs = new ArrayList<>();
        listAdapter_team = new TeamAdapter();
        listAdapter_xs = new XSListAdapter();
        list_xs.setAdapter(listAdapter_xs);
        list_team.setAdapter(listAdapter_team);
        list_xs.setLayoutManager(new LinearLayoutManager(this));
        list_team.setLayoutManager(new LinearLayoutManager(this));
        guideline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBinder != null) {
                    Toast.makeText(getThis(), R.string.subject_jw_download_begin, Toast.LENGTH_SHORT).show();
                    mBinder.startDownLoad(new downloadGuidelineFileTask(
                            getExternalFilesDir(null).getAbsolutePath() + "/jw/",
                            guidelineS, subjectId, "zwfj"));
                }
            }
        });
        guideline_en.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBinder != null) {
                    Toast.makeText(getThis(), R.string.subject_jw_download_begin, Toast.LENGTH_SHORT).show();
                    mBinder.startDownLoad(new downloadGuidelineFileTask(
                            getExternalFilesDir(null).getAbsolutePath() + "/jw/",
                            guidelineS, subjectId, "ywfj"));
                }
//                new downloadGuidelineFileTask(guidelineS,subjectId,"ywfj").executeOnExecutor(TPE);

//                ActivityUtils.downloadFile(getThis(),jwCore.getHostName()+guidelineUrl_en,
//                        guideline_enS);
            }
        });
    }

    void Refresh() {
        if (pageTask != null && !pageTask.isCancelled()) pageTask.cancel(true);
        pageTask = new refreshTask();
        pageTask.executeOnExecutor(TPE);
    }

    class refreshTask extends AsyncTask {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            refresh.setRefreshing(true);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                Map map = jwCore.getSubjectDetail(subjectId);
                Log.e("map", String.valueOf(map));
                listRes_team.clear();
                listRes_team.addAll((Collection<? extends Map<String, String>>) map.get("team"));
                listRes_xs.clear();
                listRes_xs.addAll((Collection<? extends Map<String, String>>) map.get("xs"));
                Map<String, String> basicInfo = (Map<String, String>) map.get("basicInfo");
                if (basicInfo != null) {
                    nameS = basicInfo.get("name");
                    name_enS = basicInfo.get("name_en");
                    departmentS = basicInfo.get("department");
                    codeS = basicInfo.get("code");
                    typeS = basicInfo.get("type");
                    languageS = basicInfo.get("lang");
                    creditS = basicInfo.get("credit");
                    tagS = basicInfo.get("tag");
                }

                Map<String, String> description = (Map<String, String>) map.get("description");

                if (description != null) {
                    descriptionS = description.get("ch");
                    description_enS = description.get("en");
                    guidelineS = description.get("file_ch_name");
                    guideline_enS = description.get("file_en_name");
                    guidelineUrl = description.get("file_ch_url");
                    guidelineUrl_en = description.get("file_en_url");
                }
                listRes_team.clear();
                listRes_team.addAll((Collection<? extends Map<String, String>>) map.get("team"));
                return true;
            } catch (JWException e) {
                return e;
            }

        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            refresh.setRefreshing(false);
            if (o instanceof JWException) {
                Toast.makeText(getThis(), "加载出错！", Toast.LENGTH_SHORT);
            } else if ((boolean) o) {
                listAdapter_team.notifyDataSetChanged();
                listAdapter_xs.notifyDataSetChanged();
                setOrNone(name, nameS);
                setOrNone(name_en, name_enS);
                setOrNone(department, departmentS);
                setOrNone(code, codeS);
                setOrNone(type, typeS);
                setOrNone(language, languageS);
                setOrNone(credit, creditS);
                setOrNone(tag, tagS);
                setOrNone(description, descriptionS);
                setOrNone(description_en, description_enS);
                setOrHide(guideline, guidelineS);
                setOrHide(guideline_en, guideline_enS);
            }
        }
    }

    private void setOrNone(TextView textView, String s) {
        if (TextUtils.isEmpty(s)) textView.setText(getString(R.string.none));
        else {
            Pattern p = Pattern.compile("<!-[\\s\\S]*?-->");
            Matcher matcher = p.matcher(s);
            textView.setText(Html.fromHtml(matcher.replaceAll("")));
        }
    }

    private void setOrHide(TextView textView, String s) {
        if (TextUtils.isEmpty(s)) textView.setVisibility(View.INVISIBLE);
        else {
            Pattern p = Pattern.compile("<!-[\\s\\S]*?-->");
            Matcher matcher = p.matcher(s);
            textView.setText(Html.fromHtml(matcher.replaceAll("")));
        }
    }

    class XSListAdapter extends RecyclerView.Adapter<XSListAdapter.XSHolder> {


        @NonNull
        @Override
        public XSHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.dynamic_subject_jw_xfxs_item, parent, false);
            return new XSHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull XSHolder holder, int position) {
            holder.key.setText(listRes_xs.get(position).get("key"));
            holder.value.setText(listRes_xs.get(position).get("value"));
        }

        @Override
        public int getItemCount() {
            return listRes_xs.size();
        }

        class XSHolder extends RecyclerView.ViewHolder {
            TextView key, value;

            public XSHolder(@NonNull View itemView) {
                super(itemView);
                key = itemView.findViewById(R.id.key);
                value = itemView.findViewById(R.id.value);
            }
        }
    }

    class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.THolder> {

        @NonNull
        @Override
        public THolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.dynamic_subject_jw_team_item, parent, false);
            return new THolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull THolder holder, final int position) {
            holder.name.setText(listRes_team.get(position).get("name"));
            holder.resp.setText(listRes_team.get(position).get("responsible"));
            setOrHide(holder.file, listRes_team.get(position).get("file"));
            holder.name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityUtils.searchFor(getThis(),listRes_team.get(position).get("name"),"teacher");
                }
            });
            holder.file.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mBinder != null) {
                        Toast.makeText(getThis(), R.string.subject_jw_download_begin, Toast.LENGTH_SHORT).show();
                        mBinder.startDownLoad(new downloadTeamFileTask(
                                getExternalFilesDir(null).getAbsolutePath() + "/jw/",
                                listRes_team.get(position).get("file"),
                                listRes_team.get(position).get("id"),
                                listRes_team.get(position).get("downloadFlag")));
                    }
//                    String url = jwCore.getHostName()+"/kck/kctdwh/downKctdFj?id="
//                    +listRes_team.get(position).get("id")+"&downFlag="+listRes_team.get(position).get("downloadFlag");
//                    ActivityUtils.downloadFile(getThis(),url,listRes_team.get(position).get("file"));
                }
            });
            if (listRes_team.get(position).get("file") != null)
                holder.file.setText(listRes_team.get(position).get("file"));
        }

        @Override
        public int getItemCount() {
            return listRes_team.size();
        }

        class THolder extends RecyclerView.ViewHolder {

            TextView name, resp, file;

            public THolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.name);
                resp = itemView.findViewById(R.id.responsible);
                file = itemView.findViewById(R.id.file);
            }
        }


    }

    class downloadGuidelineFileTask extends DownloadTask {
        String fileName;
        String subjectId;
        String fjflag;
        String folder;

        public downloadGuidelineFileTask(String folder, String fileName, String subjectId, String fjflag) {
            this.fileName = fileName;
            this.folder = folder;
            this.subjectId = subjectId;
            this.fjflag = fjflag;
        }


        @Override
        protected void onPostExecute(Integer status) {
            super.onPostExecute(status);
            switch (status) {
                case TYPE_SUCXCSS:
                    mListener.onSuccess();
                    break;
                case TYPE_FAILED:
                    mListener.onFailed();
                    break;
                case TYPE_PAUSED:
                    mListener.onPaused();
                    break;
                case TYPE_CANCELED:
                    mListener.onCanceled();
                    break;
            }

//            if(o){
//                Toast.makeText(getThis(),"下载完成",Toast.LENGTH_SHORT).show();
//            }else{
//                Toast.makeText(getThis(),"下载失败",Toast.LENGTH_SHORT).show();
//            }

        }

        @Override
        public String getFileName() {
            return fileName;
        }

        @Override
        public String getFolderPath() {
            return folder;
        }

        @Override
        protected Integer doInBackground(String... strings) {
            try {
                String path = "http://jw.hitsz.edu.cn/kck/kcxxwh/downFj?kcid="
                        + subjectId + "&fjflag=" + fjflag + "&downFlag=";
                URL url = new URL(path);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setReadTimeout(5000);
                con.setConnectTimeout(5000);
                con.addRequestProperty("Accept-Encoding", "identity");
                StringBuilder sb = new StringBuilder();
                int i = 0;
                for (Map.Entry<String, String> e : jwCore.getCookies().entrySet()) {
                    sb.append(e.getKey()).append("=").append(e.getValue());
                    if (i != jwCore.getCookies().size() - 1) sb.append("; ");
                    i++;
                }
                con.addRequestProperty("Cookie", sb.toString());
                //con.setRequestProperty("Charset", "UTF-8");
                con.setRequestMethod("GET");
                if (con.getResponseCode() == 200) {
                    InputStream is = con.getInputStream();//获取输入流
                    FileOutputStream fileOutputStream = null;//文件输出流
                    if (is != null) {
                        java.io.File folderF = new java.io.File(folder);
                        if (!folderF.exists()) {
                            folderF.mkdirs();
                        }
                        fileOutputStream = new FileOutputStream(new java.io.File(folderF.toString() + "/" + fileName));//指定文件保存路径，代码看下一步
                        byte[] buf = new byte[1024*1024*10];
                        int ch;
                        int downloaded = 0;
                        while ((ch = is.read(buf)) != -1) {
                            downloaded += ch;
                           Log.e("dowloaded:", String.valueOf(downloaded));
                            fileOutputStream.write(buf, 0, ch);//将获取到的流写入文件中
                            publishProgress(downloaded);
                        }
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.flush();
                        fileOutputStream.close();
                    }
                }
            } catch (IOException e) {
                return TYPE_FAILED;
            }
            return TYPE_SUCXCSS;
        }
    }

    class downloadTeamFileTask extends DownloadTask {
        String fileName;
        String id;
        String downloadFlag;
        String folder;

        public downloadTeamFileTask(String folder, String fileName, String Id, String downloadflag) {
            this.fileName = fileName;
            this.folder = folder;
            this.id = Id;
            this.downloadFlag = downloadflag;
        }


        @Override
        protected void onPostExecute(Integer status) {
            super.onPostExecute(status);
            switch (status) {
                case TYPE_SUCXCSS:
                    mListener.onSuccess();
                    break;
                case TYPE_FAILED:
                    mListener.onFailed();
                    break;
                case TYPE_PAUSED:
                    mListener.onPaused();
                    break;
                case TYPE_CANCELED:
                    mListener.onCanceled();
                    break;
            }

//            if(o){
//                Toast.makeText(getThis(),"下载完成",Toast.LENGTH_SHORT).show();
//            }else{
//                Toast.makeText(getThis(),"下载失败",Toast.LENGTH_SHORT).show();
//            }

        }

        @Override
        public String getFileName() {
            return fileName;
        }

        @Override
        public String getFolderPath() {
            return folder;
        }

        @Override
        protected Integer doInBackground(String... strings) {
            try {
                String path = "http://jw.hitsz.edu.cn/kck/kctdwh/downKctdFj?id="
                        + id + "&downFlag=" + downloadFlag;
                URL url = new URL(path);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setReadTimeout(5000);
                con.setConnectTimeout(5000);
                StringBuilder sb = new StringBuilder();
                int i = 0;
                for (Map.Entry<String, String> e : jwCore.getCookies().entrySet()) {
                    sb.append(e.getKey()).append("=").append(e.getValue());
                    if (i != jwCore.getCookies().size() - 1) sb.append("; ");
                    i++;
                }
                con.addRequestProperty("Cookie", sb.toString());
                //con.setRequestProperty("Charset", "UTF-8");
                con.addRequestProperty("Accept-Encoding", "identity");
                con.setRequestMethod("GET");
                if (con.getResponseCode() == 200) {
                    InputStream is = con.getInputStream();//获取输入流
                    FileOutputStream fileOutputStream = null;//文件输出流
                    if (is != null) {
                        java.io.File folderF = new java.io.File(folder);
                        if (!folderF.exists()) {
                            folderF.mkdirs();
                        }
                        fileOutputStream = new FileOutputStream(new java.io.File(folderF.toString() + "/" + fileName));//指定文件保存路径，代码看下一步
                        byte[] buf = new byte[1024*1024*10];
                        int ch;
                        int downloaded = 0;
                        while ((ch = is.read(buf)) != -1) {
                            downloaded += ch;
                            //Log.e("dowloaded:", String.valueOf(downloaded));
                            fileOutputStream.write(buf, 0, ch);//将获取到的流写入文件中
                            publishProgress(downloaded);
                        }
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.flush();
                        fileOutputStream.close();
                    }
                }
            } catch (IOException e) {
                return TYPE_FAILED;
            }
            return TYPE_SUCXCSS;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBinder.getTaskNumber() == 0) {
            Intent intent = new Intent(getThis(), DownloadService.class);
            stopService(intent);
            unbindService(mConnection);
        }
    }
}
