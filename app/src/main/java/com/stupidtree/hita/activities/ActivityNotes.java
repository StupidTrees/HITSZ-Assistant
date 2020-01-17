package com.stupidtree.hita.activities;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.azoft.carousellayoutmanager.CarouselLayoutManager;
import com.azoft.carousellayoutmanager.CarouselZoomPostLayoutListener;
import com.azoft.carousellayoutmanager.CenterScrollListener;
import com.bumptech.glide.Glide;
import com.github.clans.fab.FloatingActionMenu;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.timetable.timetable.EventItem;
import com.stupidtree.hita.timetable.Note;
import com.stupidtree.hita.adapter.NotesLGridAdapter;
import com.stupidtree.hita.util.FileOperator;
import com.yuyh.library.imgsel.ISNav;
import com.yuyh.library.imgsel.common.ImageLoader;
import com.yuyh.library.imgsel.config.ISCameraConfig;
import com.yuyh.library.imgsel.config.ISListConfig;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


import tyrantgit.explosionfield.ExplosionField;

import static com.stupidtree.hita.HITAApplication.HContext;

public class ActivityNotes extends BaseActivity {
    private static final int REQUEST_LIST_CODE = 0;
    private static final int REQUEST_CAMERA_CODE = 1;
    RecyclerView notesGrid;
    NotesLGridAdapter gridAdapter;
    ArrayList<Note> gridItems;
    String curriculumName;
    EventItem ei;
    FloatingActionButton fabAdd;
    com.github.clans.fab.FloatingActionButton fab_camera,fab_write,fab_gallery;
    Toolbar mToolbar;
    String courseName;
    int week,dow;
    String number;
    EditText note_edit;
    TextView note_text;
    ImageView bt_edit,bt_done;
    int nowPosition = 0;
    LinearLayout invalidLayout,validLayout;
    FloatingActionMenu fam;
    CardView textCard;
    RefreshTask pageTask;

    @Override
    protected void stopTasks() {
        if(pageTask!=null&&pageTask.getStatus()!=AsyncTask.Status.FINISHED) pageTask.cancel(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);
        setWindowParams(true,true,false);
        ei = (EventItem) getIntent().getExtras().getSerializable("event");
        week = ei.week;
        dow = ei.DOW;
        curriculumName = getIntent().getStringExtra("curriculum");

        number = ei.tag4;
        courseName = ei.mainName;
        initGrid();
        initViews();
        Refresh(true);
    }
    private void initGrid(){
        final CarouselLayoutManager layoutManager = new CarouselLayoutManager(CarouselLayoutManager.VERTICAL);
        layoutManager.setMaxVisibleItems(5);
        layoutManager.addOnItemSelectionListener(new CarouselLayoutManager.OnCenterItemSelectionListener() {
            @Override
            public void onCenterItemChanged(int adapterPosition) {
                if(adapterPosition>=0) {
                    note_text.setText(gridItems.get(adapterPosition).text);
                    nowPosition = adapterPosition;
                }
                 }
        });
        layoutManager.setPostLayoutListener(new CarouselZoomPostLayoutListener());
        gridItems = new ArrayList<>();
        notesGrid = findViewById(R.id.notes_recyclerview);
        notesGrid.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        gridAdapter = new NotesLGridAdapter(this,gridItems);
        notesGrid.setAdapter(gridAdapter);
        notesGrid.setLayoutManager(layoutManager);
        notesGrid.setHasFixedSize(true);
        notesGrid.addOnScrollListener(new CenterScrollListener());

        //notesGrid.setLayoutManager (new GridLayoutManager(this,3,GridLayoutManager.VERTICAL,false));
        gridAdapter.setOnItemClickLitener(new NotesLGridAdapter.OnItemClickLitener() {

            @Override
            public void onItemClick(View view, int position, ImageView photoView) {
                if(gridItems.get(position).imagePath.isEmpty()) return;
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(ActivityNotes.this,
                        Pair.create(view, "image"));
                Intent i = new Intent(ActivityNotes.this, ActivityPhotoDetail.class);
                i.putExtra("imagePath",gridItems.get(position).imagePath);
                ActivityNotes.this.startActivity(i,options.toBundle());
            }
        });
        gridAdapter.setOnItemLongClickLitener(new NotesLGridAdapter.OnItemLongClickListener(){
            @Override
            public boolean onItemClick(final View view, final int position) {
                AlertDialog ad = new AlertDialog.Builder(ActivityNotes.this)
                .setTitle("提示")
                .setMessage("确认删除该笔记？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(position>=gridItems.size()) return;
                        if(gridItems.get(position).imagePath!=null){
                            File f = new File(gridItems.get(position).imagePath);
                            if(f.exists()) f.delete();
                        }
                        gridAdapter.removeNote(position);
                        ExplosionField ef = ExplosionField.attach2Window(ActivityNotes.this);
                        ef.explode(view);
                        Toast.makeText(ActivityNotes.this,"已删除",Toast.LENGTH_SHORT).show();
                        saveNoteToFile();
                        Refresh(false);
                    }
                })
                .setNegativeButton( "取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
                ad.show();
                return true;
            }
        });
    }

    private void initViews(){
        mToolbar = findViewById(R.id.toolbar);
        fabAdd = findViewById(R.id.notes_fab);
        mToolbar.setTitleTextColor(Color.parseColor("#202020"));
        note_text = findViewById(R.id.note_text);
        note_edit = findViewById(R.id.note_edit);
        bt_edit = findViewById(R.id.note_bt_edit);
        bt_done = findViewById(R.id.note_bt_editdone);
        invalidLayout = findViewById(R.id.notesinvalidlayout);
        validLayout = findViewById(R.id.notesvalidlayout);
        textCard = findViewById(R.id.note_textcard);
        fab_gallery = findViewById(R.id.note_fab1);
        fab_camera = findViewById(R.id.note_fab2);
        fab_write= findViewById(R.id.note_fab3);
        fam = findViewById(R.id.note_fabmenu);
        mToolbar.setTitle(ei.mainName);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        note_text.setVisibility(View.VISIBLE);
        note_edit.setVisibility(View.GONE);
        bt_edit.setVisibility(View.VISIBLE);
        bt_done.setVisibility(View.GONE);
        bt_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bt_edit.setVisibility(View.GONE);
                bt_done.setVisibility(View.VISIBLE);
                note_text.setVisibility(View.GONE);
                note_edit.setVisibility(View.VISIBLE);
                note_edit.setText(note_text.getText());
            }
        });
        bt_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                note_text.setVisibility(View.VISIBLE);
                note_edit.setVisibility(View.GONE);
                bt_edit.setVisibility(View.VISIBLE);
                bt_done.setVisibility(View.GONE);
                gridItems.get(nowPosition).setText(note_edit.getText().toString());
                saveNoteToFile();
                Refresh(false);
            }
        });
        fab_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fam.close(true);
                // 自由配置选项
                ISListConfig config = new ISListConfig.Builder()
                        // 是否多选, 默认true
                        .multiSelect(true)
                        // 是否记住上次选中记录, 仅当multiSelect为true的时候配置，默认为true
                        .rememberSelected(false)
                        // 使用沉浸式状态栏
                        .statusBarColor(getColorPrimaryDark())
                        // 返回图标ResId
                        .backResId(R.drawable.bt_notes_toolbar_back)
                        // 标题
                        .title("图片")
                        // 标题文字颜色
                        .titleColor(Color.WHITE)
                        // TitleBar背景色
                        .titleBgColor(getColorPrimary())
                        // 裁剪大小。needCrop为true的时候配置
                        //.cropSize(1, 1, 200, 200)
                        .needCrop(false)
                        // 第一个是否显示相机，默认true
                        .needCamera(false)
                        // 最大选择图片数量，默认9
                        .maxNum(9)
                        .build();
                ISNav x = ISNav.getInstance();
                x.init(new ImageLoader() {
                    @Override
                    public void displayImage(Context context, String path, ImageView imageView) {
                        //new mImageLoader().loadImage(path,imageView);
                        Glide.with(context).load(path).into(imageView);
                    }
                });
                // 跳转到图片选择器
                x.toListActivity(ActivityNotes.this, config, REQUEST_LIST_CODE);
            }
        });

        fab_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fam.close(true);
                ISCameraConfig config = new ISCameraConfig.Builder()
                        .needCrop(false) // 裁剪
                        //.cropSize(1, 1, 200, 200)
                        .build();

                ISNav.getInstance().toCameraActivity(ActivityNotes.this, config, REQUEST_CAMERA_CODE);
            }
        });
        fab_write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fam.close(true);
                View view = getLayoutInflater().inflate(R.layout.dialog_note_addtext,null);
                final EditText et = view.findViewById(R.id.note_addtext_edittext);
                AlertDialog dl = new AlertDialog.Builder(ActivityNotes.this).
                        setView(view)
                        .setTitle("输入笔记内容")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(et.getText().toString().isEmpty()){
                                    Toast.makeText(ActivityNotes.this,"请输入文本！",Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                gridAdapter.insertNote(new Note(null,et.getText().toString()));
                                saveNoteToFile();
                                Refresh(false);
                            }
                        })
                        .setNegativeButton("取消",null)
                        .create();
                dl.show();


            }
        });


    }



    public void Refresh(boolean refreshList){
        if(pageTask!=null&&pageTask.getStatus()!=AsyncTask.Status.FINISHED) pageTask.cancel(true);
        pageTask = new RefreshTask(refreshList,this);
        pageTask.executeOnExecutor(HITAApplication.TPE);
    }


    public void loadNoteFromFile(){
        ArrayList<Note> temp = FileOperator.loadNoteFromFile(Objects.requireNonNull(getExternalFilesDir(null)),curriculumName,week+"-"+dow,number);
        gridItems.clear();
        if(temp!=null){
            for(Note n:temp){
                gridItems.add(n);
            }
        }
    }

    public void saveNoteToFile(){
        FileOperator.saveNoteToFile(gridItems,getExternalFilesDir(null),curriculumName,week+"-"+dow,number);
    }

//    public void syncNoteWithFile(){
//        ArrayList<Note> temp = FileOperator.loadNoteFromFile(Objects.requireNonNull(getExternalFilesDir(null)),curriculumName,week+"-"+dow,number);
//        List<String> imgRes = FileOperator.loadNotePhotosFromFile(Objects.requireNonNull(getExternalFilesDir(null)),curriculumName,week+"-"+dow,number);
//        if(gridItems.size()==0){
//            if(temp!=null&&temp.size()!=0){
//                for(Note x:temp){
//                    gridItems.add(x);
//                }
//            }else if(imgRes!=null&&imgRes.size()>0){
//                gridItems.clear();
//                for(int i = 0;i<imgRes.size();i++){
//                    gridItems.add(new Note(imgRes.get(i),"无描述"));
//                }
//            }
//        }
//        for(Note x:gridItems){
//            if(x.imagePath==null) continue;
//            File  f = new File(x.imagePath);
//            if(!f.exists()){
//                x.imagePath = null;
//            }
//        }
//        FileOperator.saveNoteToFile(gridItems,getExternalFilesDir(null),curriculumName,week+"-"+dow,number);
//    }






    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 图片选择结果回调
        if (requestCode == REQUEST_LIST_CODE && resultCode == RESULT_OK && data != null) {
            List<String> pathList = data.getStringArrayListExtra("result");
            if(FileOperator.copyNotePhotosToFile(pathList,getExternalFilesDir(null),curriculumName,week+"-"+dow,number)){
                Toast.makeText(this,"添加成功！",Toast.LENGTH_SHORT).show();
                for(String x:pathList){
                    File file = new File(getExternalFilesDir(null).toString() + "/notes/"+curriculumName+"/"+week+"-"+dow+"/"+number+"/");
                    gridAdapter.insertNote(new Note(file.getPath()+x.substring(x.lastIndexOf("/")),"无描述"));
                }
                saveNoteToFile();
                Refresh(false);
            }else{
                Toast.makeText(this,"添加照片失败",Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == REQUEST_CAMERA_CODE && resultCode == RESULT_OK && data != null) {
            String path = data.getStringExtra("result"); // 图片地址
            ArrayList<String> pathList = new ArrayList<>();
            pathList.add(path);
            if(FileOperator.copyNotePhotosToFile(pathList,getExternalFilesDir(null),curriculumName,week+"-"+dow,number)){
                Toast.makeText(this,"添加成功！",Toast.LENGTH_SHORT).show();
                for(String x:pathList){
                    File file = new File(getExternalFilesDir(null).toString() + "/notes/"+curriculumName+"/"+week+"-"+dow+"/"+number+"/");
                    gridAdapter.insertNote(new Note(file.getPath()+x.substring(x.lastIndexOf("/")),"无描述"));
                }
                saveNoteToFile();
                Refresh(false);
            }else{
                Toast.makeText(this,"添加照片失败",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    static class RefreshTask extends AsyncTask{

        boolean refreshList;
        private WeakReference<ActivityNotes> mActivity;

        RefreshTask(boolean x,ActivityNotes activity){
            refreshList = x;
            mActivity = new WeakReference(activity);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            ActivityNotes activity = mActivity.get();
            if(activity==null||activity.isDestroyed()||activity.isFinishing()) return null;
            if (!FileOperator.verifyStoragePermissions(mActivity.get())) {
                Toast.makeText(HContext, "请给本应用授权后后再使用本功能！", Toast.LENGTH_SHORT).show();
                return null;
            }
            if(activity.gridItems.size()!=0&&(activity.nowPosition>=activity.gridItems.size())) activity.nowPosition = activity.gridItems.size()-1;
            activity.loadNoteFromFile();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            ActivityNotes activity = mActivity.get();
            if(activity==null||activity.isFinishing()||activity.isDestroyed()) return;
                if(activity.gridItems.size()>0){
                    activity.invalidLayout.setVisibility(View.GONE);
                    activity.validLayout.setVisibility(View.VISIBLE);
                   activity. note_text.setText(activity.gridItems.get(activity.nowPosition).text);
                }else{
                    activity.invalidLayout.setVisibility(View.VISIBLE);
                    activity.validLayout.setVisibility(View.GONE);
                }
                if(refreshList) activity.gridAdapter.notifyDataSetChanged();
        }
    }



}
