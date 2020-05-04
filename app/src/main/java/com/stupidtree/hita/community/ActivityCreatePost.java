package com.stupidtree.hita.community;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.BaseActivity;
import com.stupidtree.hita.fragments.popup.FragmentLoading;
import com.stupidtree.hita.online.HITAUser;
import com.stupidtree.hita.online.Post;
import com.stupidtree.hita.online.Topic;
import com.stupidtree.hita.util.FileProviderUtils;
import com.stupidtree.hita.util.GalleryPicker;
import com.stupidtree.hita.views.CornerTransform;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cn.bmob.v3.BmobUser;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.adapter.NewsIpNewsListAdapter.dip2px;
import static com.stupidtree.hita.community.ActivityCommunity.MODE_REFRESH_ALL;

public class ActivityCreatePost extends BaseActivity {

    public static final int RC_CHOOSE_PHOTO = 10;
    public static final int RC_TAKE_PHOTO = 11;
    public static final int RC_CROP_PHOTO = 12;
    EditText content;
    RecyclerView list;
    Toolbar toolbar;
    Chip topicChip;
    IMGListAdapter listAdapter;
    List<String> listRes;
    Topic specificTopic = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true, true, false);
        setContentView(R.layout.activity_create_post);
        specificTopic = (Topic) getIntent().getSerializableExtra("topic");
        initList();
        initViews();
        initToolbar();
    }

    @Override
    protected void stopTasks() {

    }

    void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.label_activity_create_post);
        toolbar.inflateMenu(R.menu.toolbar_create_post);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_done) {
                    if (TextUtils.isEmpty(content.getText().toString())) {
                        Snackbar.make(list, "还没有输入内容呐", Snackbar.LENGTH_SHORT).show();
                    } else {
                        Post(listRes, content.getText().toString());
                    }

                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            //Toast.makeText(this, "操作取消", Toast.LENGTH_SHORT).show();
            return;
        }
        switch (requestCode) {
            case RC_CHOOSE_PHOTO:
                if (null == data) {
                    Toast.makeText(this, R.string.no_image_fetched, Toast.LENGTH_SHORT).show();
                    return;
                }
                Uri uri = data.getData();
                if (null == uri) { //如果单个Uri为空，则可能是1:多个数据 2:没有数据
                    ClipData clipData = data.getClipData();
                    if (clipData == null || clipData.getItemCount() == 0) { //没有数据，弹出提示
                        Toast.makeText(this, R.string.no_image_selected, Toast.LENGTH_SHORT).show();
                    } else { //否则，加入多个图片
                        List<String> toAdd = new ArrayList<>();
                        int vacancy = 9 - listRes.size();
                        for (int i = 0; i < clipData.getItemCount() && i < vacancy; i++) {
                            ClipData.Item item = clipData.getItemAt(i);
                            toAdd.add(FileProviderUtils.getFilePathByUri(this, item.getUri()));
                        }
                        listRes.addAll(toAdd);
                        listAdapter.notifyItemRangeInserted(listRes.size() - toAdd.size(), toAdd.size());
                        listAdapter.notifyItemRangeChanged(listRes.size() - 1, 2);
                    }
                    return;
                } else {
                    listRes.add(FileProviderUtils.getFilePathByUri(this, uri));
                    listAdapter.notifyItemInserted(listRes.size() - 1);
                }
//                // 剪裁图片
//               galleryUtils.cropPhoto(FileProviderUtils.getFilePathByUri(this, uri), 200);
//                break;
//            case RC_TAKE_PHOTO:
//                // 剪裁图片
//               galleryUtils.cropPhoto(galleryUtils.tempPhotoPath, 200);
//                break;
//            case RC_CROP_PHOTO:
//                // 显示图片

                break;
        }
    }


    void initViews() {
        topicChip = findViewById(R.id.topic);
        content = findViewById(R.id.content);
        topicChip.setCloseIconVisible(false);
        topicChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FragmentTopicsPopup(new FragmentTopicsPopup.OnPickListener() {
                    @Override
                    public void onPick(Topic topic) {
                        if (!CurrentUser.getUsername().equals("hita") && topic != null && topic.getType().contains("lock")) {
                            Toast.makeText(getThis(), R.string.no_topic_permission, Toast.LENGTH_SHORT).show();
                        } else {
                            specificTopic = topic;
                            topicChip.setCloseIconVisible(true);
                            topicChip.setText(topic.getName());
                        }

                    }
                }).show(getSupportFragmentManager(), UUID.randomUUID().toString());
            }
        });

        topicChip.setOnCloseIconClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                specificTopic = null;
                topicChip.setCloseIconVisible(false);
                topicChip.setText(R.string.set_topic);
            }
        });

        if (specificTopic != null) {
            topicChip.setText(specificTopic.getName());
            topicChip.setCloseIconVisible(true);
        }

    }

    void initList() {
        list = findViewById(R.id.list);
        listRes = new ArrayList<>();
        listAdapter = new IMGListAdapter();
        list.setAdapter(listAdapter);
        list.setLayoutManager(new GridLayoutManager(this, 3));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_create_post, menu);
        return super.onCreateOptionsMenu(menu);
    }

    void Post(List<String> images, String content) {
        if (CurrentUser == null) return;
        Post p = new Post();
        p.setContent(content);
        if (specificTopic != null && (CurrentUser.getUsername().equals("hita") || !specificTopic.getType().contains("lock"))) {
            p.setTopic(specificTopic);
        }
        p.setAuthor(BmobUser.getCurrentUser(HITAUser.class));
        final FragmentLoading loading = FragmentLoading.newInstance("正在发布");
        UploadPostHelper uph = new UploadPostHelper(this, images, p, new UploadPostHelper.StateListener() {

            @Override
            public void onPostStart() {
                loading.show(getSupportFragmentManager(), UUID.randomUUID().toString());

            }

            @Override
            public void onCompressStart(int number) {
                loading.updateSubtitle(String.format(getString(R.string.compressing_pic), number));
            }

            @Override
            public void onCompressSuccess() {
                loading.updateSubtitle(getString(R.string.compress_done));
            }

            @Override
            public void onCompressFails() {
                loading.updateSubtitle(getString(R.string.compress_failed));
                loading.dismiss();
                Toast.makeText(getThis(), R.string.post_fail, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUploadStart() {
                loading.updateSubtitle(getString(R.string.uploading_pic));
            }

            @Override
            public void onUploadProgress(int curIndex, int curPercent, int total, int totalPercent) {
                loading.updateSubtitle(String.format(getString(R.string.uploading_progress)
                        , curIndex, total, curPercent));

            }

            @Override
            public void onUploadSuccess() {
                loading.updateSubtitle(getString(R.string.uploade_done));
            }

            @Override
            public void onUploadFailed() {
                loading.dismiss();
                Toast.makeText(getThis(), R.string.post_fail, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPostSuccess() {
                loading.dismiss();
                Toast.makeText(getThis(), R.string.post_success, Toast.LENGTH_SHORT).show();
                Intent i = new Intent();
                i.putExtra("mode", MODE_REFRESH_ALL);
                setResult(RESULT_OK, i);
                finish();
            }

            @Override
            public void onPostFails() {
                loading.dismiss();
                Toast.makeText(getThis(), R.string.post_fail, Toast.LENGTH_SHORT).show();
            }
        });
        uph.run();
    }

    class IMGListAdapter extends RecyclerView.Adapter<IMGListAdapter.IHolder> {
        private static final int ADD = 67;
        private static final int ITEM = 498;

        CornerTransform transformation;

        public IMGListAdapter() {
            transformation = new CornerTransform(getThis(), dip2px(getThis(), 8));
            transformation.setExceptCorner(false, false, false, false);

        }

        @NonNull
        @Override
        public IHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int layout = viewType == ADD ? R.layout.dynamic_add_post_img_add : R.layout.dynamic_add_post_img_item;
            View v = getLayoutInflater().inflate(layout, parent, false);
            return new IHolder(v, viewType);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == listRes.size()) return ADD;
            else return ITEM;
        }

        @Override
        public void onBindViewHolder(@NonNull IHolder holder, final int position) {
            if (holder.type == ITEM) {
                Glide.with(getThis()).load(listRes.get(position))
                        .apply(RequestOptions.bitmapTransform(transformation))
                        //.centerCrop().
                        .into(holder.image);
                holder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listRes.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, listRes.size());
                    }
                });
            } else {
                holder.add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GalleryPicker.choosePhoto(getThis(), true);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return listRes.size() == 9 ? listRes.size() : listRes.size() + 1;
        }

        class IHolder extends RecyclerView.ViewHolder {
            ImageView image;
            ImageView delete;
            View add;
            int type;

            public IHolder(@NonNull View itemView, int type) {
                super(itemView);
                this.type = type;
                image = itemView.findViewById(R.id.image);
                delete = itemView.findViewById(R.id.delete);
                add = itemView.findViewById(R.id.add);
            }
        }

    }


}
