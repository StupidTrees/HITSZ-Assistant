package com.stupidtree.hita.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivityMain;
import com.stupidtree.hita.activities.ActivityRankBoard;
import com.stupidtree.hita.adapter.HITSZInfoPagerAdapter;

import com.stupidtree.hita.adapter.NaviPageAdapter;
import com.stupidtree.hita.online.BannerItem;

import com.stupidtree.hita.util.ActivityUtils;
import com.zhouwei.mzbanner.MZBannerView;
import com.zhouwei.mzbanner.holder.MZHolderCreator;
import com.zhouwei.mzbanner.holder.MZViewHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

import static com.stupidtree.hita.HITAApplication.defaultSP;
import static com.stupidtree.hita.adapter.NaviPageAdapter.TYPE_BOARD_JW;
import static com.stupidtree.hita.adapter.NaviPageAdapter.TYPE_HINT;
import static com.stupidtree.hita.adapter.NaviPageAdapter.TYPE_HITA;
import static com.stupidtree.hita.adapter.NaviPageAdapter.TYPE_JWTS_FUN;
import static com.stupidtree.hita.adapter.NaviPageAdapter.strToIntegerList;

public class FragmentNavi extends BaseFragment {


    // SearchView searchview;
   MZBannerView banner;
    List<BannerItem> bannerItemList;

    HITSZInfoPagerAdapter pagerAdapter;
    //List<Fragment> fragments;
    //TabLayout tab;
    //ViewPager pager;
    LinearLayout card_info_layout;
    BroadcastReceiver broadcastReceiver;
    List<Integer> listRes;
    RecyclerView list;
    NaviPageAdapter listAdapter;
    SwipeRefreshLayout refreshLayout;
    Gson gson;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_navi, container, false);
        //InitSearch(v);
        initViews(v);
        initBanner(v);
        // initCanteen(v);
//        pagePreference(v);
        refreshBanner();
        initBroadcastReceiver();
        initList(v);
        return v;
    }

    void initList(View v) {
        gson = new Gson();
        list = v.findViewById(R.id.navipage_list);
        listRes = new ArrayList<>();
        List<Integer> order  = strToIntegerList(defaultSP.getString("navi_page_order_2","[]"));
        if(order.size()>0){
            for(int i=0;i<order.size();i++){
                if(order.get(i)!=TYPE_JWTS_FUN&&order.get(i)!=TYPE_HINT) listRes.add(order.get(i));
            }
            if(!order.contains(TYPE_HITA)) listRes.add(NaviPageAdapter.TYPE_HITA);
        }else{
            listRes.add(NaviPageAdapter.TYPE_HITA);
            listRes.add(NaviPageAdapter.TYPE_BULLETIN);
            listRes.add(NaviPageAdapter.TYPE_BOARD_JW);
           // listRes.add(NaviPageAdapter.TYPE_BOARD_SERVICE);
            listRes.add(NaviPageAdapter.TYPE_LECTURE);
            listRes.add(NaviPageAdapter.TYPE_IPNEWS);

        }
        if(defaultSP.getBoolean("first_enter_navipage_hint_drag",true)){
            listRes.add(0,NaviPageAdapter.TYPE_HINT);
        }
    
        listAdapter = new NaviPageAdapter(listRes, getContext());
        list.setAdapter(listAdapter);
        list.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        NaviPageAdapter.mCallBack mCallBack = new NaviPageAdapter.mCallBack(listAdapter);
        ItemTouchHelper helper = new ItemTouchHelper(mCallBack);
        helper.attachToRecyclerView(list);
    }

    void initBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("COM.STUPIDTREE.HITA.JWTS_AUTO_LOGIN_DONE")) {
                    Log.e("接受到广播：", "登录教务完成");
                    int addIndex_jw_fun = listRes.indexOf(defaultSP.getInt("navi_page_order_before_jwts", TYPE_BOARD_JW))+1;
//                    int addIndex_exam = listRes.indexOf(defaultSP.getInt("navi_page_order_before_jwts_exam", TYPE_BOARD_JW))+1;
//                    int addIndex_xfj = listRes.indexOf(defaultSP.getInt("navi_page_order_before_jwts_xfj", TYPE_BOARD_JW))+1;
//                    int addIndex_xk = listRes.indexOf(defaultSP.getInt("navi_page_order_before_jwts_xk", TYPE_BOARD_JW))+1;
//                    SparseArray<Integer> m_exam,m_xfj,m_xk;
//                    m_exam = new SparseArray<>();
//                    m_xfj = new SparseArray<>();
//                    m_xk = new SparseArray<>();
//                    m_exam.put(0,TYPE_EXAM);
//                    m_exam.put(1,addIndex_exam);
//                    m_xfj.put(0,TYPE_JWTS_XFJ);
//                    m_xfj.put(1,addIndex_xfj);
//                    m_xk.put(0,TYPE_JWTS_XK);
//                    m_xk.put(1,addIndex_xk);
//                    SparseArray<Integer>[] list = new SparseArray[]{m_exam,m_xfj,m_xk};
//                    for(int i= 1;i>=0;i--){
//                        for(int j=0;j<=i;j++){
//                            if(list[j].get(1)<list[j+1].get(1)){
//                                SparseArray temp = list[j];
//                                list[j] = list[j+1];
//                                list[j+1] = temp;
//                            }
//                        }
//                    }
//                    for(int i=0;i<3;i++){
//                        listAdapter.addItem(list[i].get(0),list[i].get(1));
//                    }
                    listAdapter.addItem(TYPE_JWTS_FUN,addIndex_jw_fun);
//                    listAdapter.addItem(TYPE_JWTS_XFJ,addIndex_xfj);
//                    listAdapter.addItem(TYPE_EXAM, addIndex_exam);
                    //list.scrollToPosition(0);
                } else if (intent.getAction().equals("COM.STUPIDTREE.HITA.JWTS_LOGIN_FAIL")) {
                    Log.e("接受到广播：", "登录教务失效");
                    listAdapter.removeItems(new int[]{TYPE_JWTS_FUN});
                }

            }
        };
        IntentFilter ifi = new IntentFilter();
        ifi.addAction("COM.STUPIDTREE.HITA.JWTS_AUTO_LOGIN_DONE");
        ifi.addAction("COM.STUPIDTREE.HITA.JWTS_LOGIN_FAIL");
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiver, ifi);
    }

    void initViews(View v) {
        refreshLayout = v.findViewById(R.id.refresh);
        refreshLayout.setColorSchemeColors(new int[]{((BaseActivity)getActivity()).getColorPrimary(),((BaseActivity)getActivity()).getColorPrimaryDark()});
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ActivityMain.autoLogin(refreshLayout);
            }
        });
    }


//    void initPager(View v) {
//
//        fragments = new ArrayList<>();
//        fragments.add(new FragmentNewsLecture());
//        fragments.add(new FragmentNewsBulletin());
//        fragments.add(new FragmentNewsIPNews());
//        pagerAdapter = new HITSZInfoPagerAdapter(getFragmentManager(), fragments);
//        pager.setAdapter(pagerAdapter);
//        tab.setTabIndicatorFullWidth(false);
//        tab.setupWithViewPager(pager);
//    }

    void initBanner(View v) {
        bannerItemList = new ArrayList<>();
        banner = v.findViewById(R.id.navi_banner);
        banner.setDelayedTime(4000);
        banner.start();
        //refreshBanner();
    }


    @SuppressLint("WrongConstant")



    @Override
    protected void stopTasks() {

    }

    @Override
    public void Refresh() {
    }


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }


    void refreshBanner() {
        BmobQuery<BannerItem> bq = new BmobQuery<>();
        bq.findObjects(new FindListener<BannerItem>() {
            @Override
            public void done(List<BannerItem> list, BmobException e) {
                if (e == null && list != null && list.size() > 0) {
                    bannerItemList.clear();
                    bannerItemList.addAll(list);
                    if (list.size() == 1) banner.setCanLoop(false);
                    else banner.setCanLoop(true);
                    banner.setPages(bannerItemList, new MZHolderCreator<BannerViewHolder>() {
                        @Override
                        public BannerViewHolder createViewHolder() {
                            return new BannerViewHolder();
                        }
                    });
                    banner.start();
                } else if (bannerItemList.size() == 0) {
                    BannerItem temp = new BannerItem();
                    temp.setImageUri("https://bmob-cdn-26359.bmobpay.com/2019/08/10/23ab6917400d551a805267303f0f840a.jpg");
                    temp.setTitle("同学们好");
                    temp.setAction(new JsonObject().toString());
                    temp.setSubtitle("加载banner失败");
                    bannerItemList.add(temp);
                    banner.setCanLoop(false);
                    banner.setPages(bannerItemList, new MZHolderCreator<BannerViewHolder>() {
                        @Override
                        public BannerViewHolder createViewHolder() {
                            return new BannerViewHolder();
                        }
                    });
                    banner.start();
                    //Toast.makeText(HContext,"加载banner出错！"+e.toString(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        Refresh();
        banner.start();


        //refreshBanner();
        //refreshCanteen();
        //banner.start();
    }




    @Override
    public void onPause() {
        super.onPause();
        banner.pause();
        //banner.getViewPager().setCurrentItem(0);
    }

    public class BannerViewHolder implements MZViewHolder<BannerItem> {
        private ImageView image;
        private TextView title;
        private TextView subtitle;
        private CardView card;

        @Override
        public View createView(Context context) {
            // 返回页面布局
            View view = LayoutInflater.from(context).inflate(R.layout.dynamic_navi_banner, null);
            image = view.findViewById(R.id.banner_image);
            title = view.findViewById(R.id.banner_title);
            subtitle = view.findViewById(R.id.banner_subtitle);
            card = view.findViewById(R.id.banner_card);
            return view;
        }

        @Override
        public void onBind(Context context, int i, final BannerItem bannerItem) {
            // Log.e("bind",bannerItem.getTitle());
            Glide.with(context).load(bannerItem.getImageUri()).centerCrop()
                    .placeholder(R.drawable.gradient_bg)
                    .into(image);
            title.setText(bannerItem.getTitle());
            subtitle.setText(bannerItem.getSubtitle());
            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bannerAction(bannerItem.getAction());
                }
            });
        }

    }


    private void bannerAction(JsonObject action) {
        try {
            if (action == null) return;
            if (action.has("intent")) {
                if (action.get("intent").getAsString().equals("jwts")) {
                    ActivityUtils.startJWTSActivity(getActivity());
                } else if (action.get("intent").getAsString().equals("rankboard")) {
                    Intent i = new Intent(getActivity(), ActivityRankBoard.class);
                    startActivity(i);
                }
            } else if (action.has("url")) {
                Uri uri = Uri.parse(action.get("url").getAsString());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            } else if (action.has("dialog_title") && action.has("dialog_message")) {
                AlertDialog ad = new AlertDialog.Builder(getContext()).setTitle(action.get("dialog_title").getAsString())
                        .setMessage(action.get("dialog_message").getAsString()).setPositiveButton("好的", null).create();
                ad.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}


