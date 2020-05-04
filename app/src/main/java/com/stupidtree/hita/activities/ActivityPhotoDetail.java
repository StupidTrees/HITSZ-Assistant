package com.stupidtree.hita.activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bm.library.PhotoView;
import com.bumptech.glide.Glide;
import com.stupidtree.hita.R;
import com.stupidtree.hita.util.ActivityUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ActivityPhotoDetail extends BaseActivity {

    List<String> urls;
    TextView label;
    ViewPager pager;
    int initIndex;

    @Override
    protected void stopTasks() {

    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true, false, true);
        setContentView(R.layout.activity_note_detail);
        label = findViewById(R.id.label);
        urls = new ArrayList<>();
        String[] data = getIntent().getStringArrayExtra("urls");
        initIndex = getIntent().getIntExtra("init_index", 0);
        if (data != null) {
            urls.addAll(Arrays.asList(data));
            label.setText((initIndex + 1) + "/" + urls.size());
            initPager();
        }
    }

    void initPager() {
        pager = findViewById(R.id.pager);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onPageSelected(int position) {
                label.setText((position + 1) + "/" + urls.size());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        pager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return urls.size();
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                container.removeView((View) object);
            }

            //设置viewpage内部东西的方法，如果viewpage内没有子空间滑动产生不了动画效果
            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                PhotoView v = new PhotoView(getThis());
                v.setScaleType(ImageView.ScaleType.FIT_CENTER);
                Glide.with(getThis()).load(urls.get(position)).timeout(10000).into(v);
                v.setAdjustViewBounds(false);
                v.enable();
                container.addView(v);
                v.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        new AlertDialog.Builder(getThis()).setItems(new String[]{getString(R.string.download_image)}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Toast.makeText(from,imageViewToUrl.get(view)+"",Toast.LENGTH_SHORT).show();
                                ActivityUtils.DownloadImage(getThis(), urls.get(which), new ActivityUtils.OnDownloadDoneListener() {
                                    @Override
                                    public void onDone() {
                                        try {
                                            Toast.makeText(getThis(), R.string.save_done, Toast.LENGTH_SHORT).show();

                                        } catch (Exception e) {

                                        }
                                    }
                                });
                            }
                        }).create().show();
                        return true;
                    }
                });
                return v;
            }
        });
        pager.setCurrentItem(initIndex);
    }


}
