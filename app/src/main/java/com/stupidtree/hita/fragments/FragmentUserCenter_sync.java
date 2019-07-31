package com.stupidtree.hita.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.stupidtree.hita.R;
import com.stupidtree.hita.core.Curriculum;
import com.stupidtree.hita.core.Subject;

import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobBatch;
import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.loadDataFromCloud;
import static com.stupidtree.hita.HITAApplication.saveDataToCloud;


public class FragmentUserCenter_sync extends Fragment {
    CardView load_from_cloud;
    CardView save_to_cloud;
    CardView clear_cloud;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user_center_sync,container,false);
        load_from_cloud = v.findViewById(R.id.load_from_cloud);
        save_to_cloud = v.findViewById(R.id.save_to_cloud);
        clear_cloud = v.findViewById(R.id.clear_cloud);
        save_to_cloud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new upLoadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
        load_from_cloud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog ad = new AlertDialog.Builder(getActivity()).setTitle("警告").setMessage("同步数据将清除所有本地数据").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       loadDataFromCloud();
                    }
                }).setNegativeButton("取消",null).create();
               ad.show();
            }
        });
        clear_cloud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BmobQuery<Curriculum> bq = new BmobQuery();
                bq.addWhereEqualTo("hitaUser",CurrentUser);
                bq.findObjects(new FindListener<Curriculum>() {
                    @Override
                    public void done(List list, BmobException e) {
                        new BmobBatch().deleteBatch(list);
                    }
                });
                BmobQuery<Subject> bs = new BmobQuery<>();
                bs.addWhereEqualTo("hitaUser",CurrentUser);
                bs.findObjects(new FindListener<Subject>() {
                    @Override
                    public void done(List list, BmobException e) {
                        if(e==null){
                            new BmobBatch().deleteBatch(list);
                        }else{
                            Log.e("!!",e.toString());
                        }

                    }
                });
            }
        });
        return v;
    }

    class upLoadTask extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] objects) {
            return saveDataToCloud(true);
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            Toast.makeText(HContext,"开始上传",Toast.LENGTH_SHORT).show();
        }
    }




}