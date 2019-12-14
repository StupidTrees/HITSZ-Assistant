package com.stupidtree.hita.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;


import com.google.android.material.tabs.TabLayout;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.CurriculuManagerPagerAdapter;
import com.stupidtree.hita.core.Curriculum;
import com.stupidtree.hita.core.CurriculumHelper;
import com.stupidtree.hita.fragments.FragmentCurriculum;
import com.stupidtree.hita.util.ActivityUtils;
import com.stupidtree.hita.util.FileOperator;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


import static com.stupidtree.hita.activities.ActivityMain.saveData;
import static com.stupidtree.hita.HITAApplication.*;

public class ActivityCurriculumManager extends BaseActivity implements FragmentCurriculum.OnFragmentInteractionListener{

    private static final int CHOOSE_FILE_CODE = 0;
    FloatingActionButton fab;
    Toolbar mToolbar;
    LinearLayout noneLayout;
    ViewPager pager;
    CurriculuManagerPagerAdapter curriculuManagerPagerAdapter;
    List<BaseFragment> pagerRes;
    TabLayout tabs;

    @Override
    protected void stopTasks() {

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true, true, false);
        setContentView(R.layout.activity_curriculum_manager);
        initToolbar();
        initViews();
        initPager();
        //Refresh();
    }

    void initToolbar() {

        Toolbar toolbar = findViewById(R.id.main_tool_bar);
        toolbar.setTitle("选择当前课表");
        toolbar.inflateMenu(R.menu.toolbar_curriculum_manager);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_import_from_jwts:
                        ActivityUtils.startJWTSActivity(ActivityCurriculumManager.this);
                        break;
                    case R.id.action_import_from_excel:
                        chooseFile(ActivityCurriculumManager.this.getExternalFilesDir(null));
                        break;
                }
                return true;
            }
        });
    }

    void initPager(){
        tabs = findViewById(R.id.tabs);
        pager = findViewById(R.id.pager);
        pagerRes = new ArrayList<>();
        curriculuManagerPagerAdapter = new CurriculuManagerPagerAdapter(getSupportFragmentManager(),pagerRes);
        pager.setAdapter(curriculuManagerPagerAdapter);
        tabs.setupWithViewPager(pager);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position!=thisCurriculumIndex) fab.show();
                else fab.hide();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    void initViews() {
        noneLayout = findViewById(R.id.none_layout);
        mToolbar = findViewById(R.id.main_tool_bar);
        fab = findViewById(R.id.fab_done);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog ad = new AlertDialog.Builder(ActivityCurriculumManager.this).
                        setTitle("提示")
                        .setMessage("确定更换当前课表为\n" + allCurriculum.get(pager.getCurrentItem()).name + "\n吗？").
                                setNegativeButton("取消", null).
                                setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        thisCurriculumIndex = pager.getCurrentItem();
                                        mainTimeTable.core = allCurriculum.get(thisCurriculumIndex);
                                        thisWeekOfTerm = allCurriculum.get(thisCurriculumIndex).getWeekOfTerm(now);
                                        if (thisWeekOfTerm > allCurriculum.get(thisCurriculumIndex).totalWeeks) {
                                            allCurriculum.get(thisCurriculumIndex).totalWeeks = thisWeekOfTerm;
                                        }
                                        saveData();
                                        defaultSP.edit().putInt("thisCurriculum", thisCurriculumIndex).apply();
                                        timeWatcher.refreshProgress(false, true);
                                        finish();
                                    }
                                }).create();
                ad.show();


            }
        });


    }


    public void Refresh() {
        pagerRes.clear();
       // curriculuManagerPagerAdapter.notifyDataSetChanged();
        for(Curriculum c:allCurriculum){
            pagerRes.add(FragmentCurriculum.newInstance(c));
        }
        curriculuManagerPagerAdapter.notifyDataSetChanged();
        if (pagerRes.size()>0) {
            pager.setVisibility(View.VISIBLE);
            noneLayout.setVisibility(View.GONE);
        } else {
            pager.setVisibility(View.GONE);
            noneLayout.setVisibility(View.VISIBLE);
            fab.hide();
        }
        pager.setCurrentItem(thisCurriculumIndex);
        fab.hide();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_curriculum_manager, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //**********************************文件选择****************************************

    private static final String TAG1 = "FileChoose";

    // 调用系统文件管理器
    private void chooseFile(File path) {
        FileOperator.verifyStoragePermissions(this);
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*").addCategory(Intent.CATEGORY_OPENABLE);
        try {
            this.startActivityForResult(Intent.createChooser(intent, "Choose File"), CHOOSE_FILE_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "亲，木有文件管理器啊-_-!!", Toast.LENGTH_SHORT).show();
        }
    }

    public static String getPath(final Context context, final Uri uri) {

        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


    //当选择完Excel文件后调用此函数
    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CHOOSE_FILE_CODE) {
                Uri uri = data.getData();
                String sPath1;
                sPath1 = getPath(this, uri); // Paul Burke写的函数，根据Uri获得文件路径
                final File file = new File(sPath1);
                DatePickerDialog DPD = new DatePickerDialog(this, null, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
                DPD.setButton(DialogInterface.BUTTON_POSITIVE, "添加课程表", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int y = 0, m = 0, d = 0;
                        y = ((DatePickerDialog) dialog).getDatePicker().getYear();
                        m = ((DatePickerDialog) dialog).getDatePicker().getMonth() + 1;
                        d = ((DatePickerDialog) dialog).getDatePicker().getDayOfMonth();
                        new loadCurriculumTask(file, y, m, d).executeOnExecutor(HITAApplication.TPE);;
                    }
                });
                Toast.makeText(this, "请设置课表起始日期", Toast.LENGTH_LONG).show();
                DPD.setCancelable(false);
                DPD.setButton(DialogInterface.BUTTON_NEGATIVE, "算了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                DPD.create();
                DPD.show();

            }


        }


        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onFragmentInteraction() {
        Refresh();
    }

    class loadCurriculumTask extends AsyncTask {

        int y, m, d;
        File file;

        public loadCurriculumTask(File f, int y, int m, int d) {
            this.y = y;
            this.m = m;
            this.d = d;
            this.file = f;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            CurriculumHelper Curriculum = FileOperator.loadCurriculumHelperFromExcel(file, y, m, d);
            if (Curriculum != null) {
                return addCurriculumToTimeTable(Curriculum);
            } else {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if ((Boolean) o) {
                Toast.makeText(ActivityCurriculumManager.this, "已添加课表", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ActivityCurriculumManager.this, "添加失败！", Toast.LENGTH_SHORT).show();
            }
            Refresh();
            saveData();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    protected void onResume() {
        super.onResume();
        Refresh();
    }


}




