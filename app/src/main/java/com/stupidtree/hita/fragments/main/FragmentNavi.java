package com.stupidtree.hita.fragments.main;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivityLeaderBoard;

import com.stupidtree.hita.adapter.NaviPageAdapter;
import com.stupidtree.hita.diy.WrapContentLinearLayoutManager;
import com.stupidtree.hita.online.BannerItem;

import com.stupidtree.hita.online.HITAUser;
import com.stupidtree.hita.timetable.TimetableCore;
import com.stupidtree.hita.util.ActivityUtils;
import com.zhouwei.mzbanner.MZBannerView;
import com.zhouwei.mzbanner.holder.MZHolderCreator;
import com.zhouwei.mzbanner.holder.MZViewHolder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FetchUserInfoListener;
import cn.bmob.v3.listener.FindListener;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.TPE;
import static com.stupidtree.hita.HITAApplication.now;
import static com.stupidtree.hita.HITAApplication.timeTableCore;
import static com.stupidtree.hita.adapter.NaviPageAdapter.TYPE_BOARD_JW;
import static com.stupidtree.hita.adapter.NaviPageAdapter.TYPE_CARD;
import static com.stupidtree.hita.adapter.NaviPageAdapter.TYPE_HINT;
import static com.stupidtree.hita.adapter.NaviPageAdapter.TYPE_HITA;
import static com.stupidtree.hita.adapter.NaviPageAdapter.TYPE_JWTS_FUN;
import static com.stupidtree.hita.adapter.NaviPageAdapter.TYPE_MOOD;
import static com.stupidtree.hita.adapter.NaviPageAdapter.TYPE_NEWS;
import static com.stupidtree.hita.adapter.NaviPageAdapter.TYPE_NOTIFICATION;
import static com.stupidtree.hita.adapter.NaviPageAdapter.strToIntegerList;

public class FragmentNavi extends BaseFragment {

    //public static final String ORDER_NAME = "navi_page_order_9";
    private MZBannerView<BannerItem> banner;
    private List<BannerItem> bannerItemList;
    private TextView title, subtitle;
    private ImageView settingButton;
    private BroadcastReceiver broadcastReceiver;
    private List<Map<String, Object>> listRes;
    private RecyclerView list;
    private NaviPageAdapter listAdapter;
    private SwipeRefreshLayout refreshLayout;
    boolean firstEnter = true;
    Gson gson;
    // HashMap<Integer,String> type_name_map = new HashMap<>();
    SharedPreferences naviPagePreference;
    public static String[]  cardNames = new String[]{"news","life","mood","jw"};
    public static Integer[] cardTyps = new Integer[]{TYPE_NEWS, TYPE_HITA, TYPE_MOOD, TYPE_BOARD_JW};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_navi, container, false);
        naviPagePreference = getActivity().getSharedPreferences("navi_page", Context.MODE_PRIVATE);
        //InitSearch(v);
        initViews(v);
        initBanner(v);
        // initCanteen(v);
//        pagePreference(v);
        //refreshBanner();
        initBroadcastReceiver();
        initList(v);
        return v;
    }

    void initList(View v) {
        gson = new Gson();
        list = v.findViewById(R.id.navipage_list);
        listRes = new ArrayList<>();
        listAdapter = new NaviPageAdapter(listRes, getContext(), naviPagePreference);
        list.setAdapter(listAdapter);
        list.setLayoutManager(new WrapContentLinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        NaviPageAdapter.mCallBack mCallBack = new NaviPageAdapter.mCallBack(listAdapter);
        ItemTouchHelper helper = new ItemTouchHelper(mCallBack);
        helper.attachToRecyclerView(list);
    }

    void initBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
//                if (intent.getAction().equals("COM.STUPIDTREE.HITA.JWTS_AUTO_LOGIN_DONE")) {
//                    Log.e("接受到广播：", "登录教务完成");
//                    int addIndex_jw_fun = listRes.indexOf(defaultSP.getInt("navi_page_order_before_jwts", TYPE_BOARD_JW))+1;
//                    if(PreferenceManager.getDefaultSharedPreferences(HContext).getBoolean("navi_enabled_jw_sync",true))listAdapter.addItem(TYPE_JWTS_FUN,addIndex_jw_fun);
//                } else if (intent.getAction().equals("COM.STUPIDTREE.HITA.JWTS_LOGIN_FAIL")) {
//                    Log.e("接受到广播：", "登录教务失效");
//                    listAdapter.removeItems(new int[]{TYPE_JWTS_FUN});
//                }
//                if (intent.getAction().equals("COM.STUPIDTREE.HITA.UT_AUTO_LOGIN_DONE")) {
//                    Log.e("接受到广播：", "登录大学城完成");
//                    int addIndex_ut_card = listRes.indexOf(naviPagePreference.getInt("navi_page_order_before_ut_card", TYPE_BOARD_JW))+1;
//                    if(PreferenceManager.getDefaultSharedPreferences(HContext).getBoolean("navi_enabled_card",true)) listAdapter.addItem(TYPE_CARD,addIndex_ut_card);
//                } else if (intent.getAction().equals("COM.STUPIDTREE.HITA.UT_LOGIN_FAIL")) {
//                    Log.e("接受到广播：", "登录大学城失效");
//                    listAdapter.removeItems(new int[]{TYPE_CARD});
//                }
                if (intent.getAction().equals("COM.STUPIDTREE.HITA.USER_INFO_FETCHED")) {
                    Log.e("接受到广播：", "用户信息同步完成");
                    int i;
                    for (i = 0; i < listRes.size(); i++) {
                        if (((Number) listRes.get(i).get("type")).intValue() == TYPE_MOOD) break;
                    }
                    listAdapter.notifyItemChanged(i);
                    // list.scrollToPosition(0);
                }

            }
        };
        IntentFilter ifi = new IntentFilter();
        ifi.addAction("COM.STUPIDTREE.HITA.JWTS_AUTO_LOGIN_DONE");
        ifi.addAction("COM.STUPIDTREE.HITA.JWTS_LOGIN_FAIL");
        ifi.addAction("COM.STUPIDTREE.HITA.UT_AUTO_LOGIN_DONE");
        ifi.addAction("COM.STUPIDTREE.HITA.UT_LOGIN_FAIL");
        ifi.addAction("COM.STUPIDTREE.HITA.USER_INFO_FETCHED");
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiver, ifi);
    }

    private void initViews(View v) {
        settingButton = v.findViewById(R.id.navi_setting);
        title = v.findViewById(R.id.navi_title);
        subtitle = v.findViewById(R.id.navi_subtitle);
        refreshLayout = v.findViewById(R.id.refresh);
        refreshLayout.setColorSchemeColors(((BaseActivity) getActivity()).getColorPrimary(), ((BaseActivity) getActivity()).getColorPrimaryDark());
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Refresh(true, true);
            }
        });
        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] x = getResources().getStringArray(R.array.navi_setting_items);
               // final Integer[] preferenceTyps = new Integer[]{TYPE_NEWS, TYPE_HITA, TYPE_MOOD, TYPE_BOARD_JW};
                boolean[] checked = new boolean[4];
                for (int i = 0; i < checked.length; i++) {
                    checked[i] = naviPagePreference.getBoolean(cardNames[i]+"_enable",true);
                }
               final List<Map<String, Object>> toAdd = new ArrayList<>();
                final List<Map<String, Object>> toRemove = new ArrayList<>();
               final SharedPreferences.Editor edit = naviPagePreference.edit();
                AlertDialog ad = new AlertDialog.Builder(getContext()).setTitle(getString(R.string.navi_settings_title)).setMultiChoiceItems(x, checked, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        edit.putBoolean(cardNames[i]+"_enable",b);
//                        boolean notContains = true;
//                        for(Map<String,Object> m:order){
//                            if((int)m.get("type")==preferenceTyps[i]) notContains = false;
//                        }
//                        if(b&&notContains){
//                            Map<String,Object> m = new HashMap();
//                            m.put("type",preferenceTyps[i]);
//                            order.add(m);
//                        }
//                        listAdapter.saveOrders(order);
                    }
                })
                        .setPositiveButton(getString(R.string.button_confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                edit.commit();
                                Refresh(true,false);
                            }
                        })
//                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
//                    @Override
//                    public void onDismiss(DialogInterface dialogInterface) {
//                        Refresh(changed[0],false);
//                    }
//                })
                        .create();
                ad.show();
            }
        });
    }


//    void initPager(View v) {
//
//        fragments = new ArrayList<>();
//        fragments.add(new FragmentNewsLecture());
//        fragments.add(new FragmentNewsBulletin());
//        fragments.add(new FragmentNewsIPNews());
//        pagerAdapter = new NewsPagerAdapter(getFragmentManager(), fragments);
//        pager.setAdapter(pagerAdapter);
//        tab.setTabIndicatorFullWidth(false);
//        tab.setupWithViewPager(pager);
//    }

    private void initBanner(View v) {
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


    public void Refresh(boolean animate, boolean swipe) {
        //refreshBanner();
        new RefreshTask(animate, swipe).executeOnExecutor(TPE);
    }


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }


//    void refreshBanner() {
//        BmobQuery<BannerItem> bq = new BmobQuery<>();
//        bq.findObjects(new FindListener<BannerItem>() {
//            @Override
//            public void done(List<BannerItem> list, BmobException e) {
//                if (e == null && list != null && list.size() > 0) {
//                    bannerItemList.clear();
//                    bannerItemList.addAll(list);
//                    if (list.size() == 1) banner.setCanLoop(false);
//                    else banner.setCanLoop(true);
//                    banner.setPages(bannerItemList, new MZHolderCreator<BannerViewHolder>() {
//                        @Override
//                        public BannerViewHolder createViewHolder() {
//                            return new BannerViewHolder();
//                        }
//                    });
//                    banner.start();
//                } else if (bannerItemList.size() == 0) {
//                    BannerItem temp = new BannerItem();
//                    // temp.setImageUri("https://bmob-cdn-26359.bmobpay.com/2019/08/10/23ab6917400d551a805267303f0f840a.jpg");
//                    temp.setTitle("同学们好");
//                    temp.setAction(new JsonObject().toString());
//                    temp.setSubtitle("加载banner失败");
//                    bannerItemList.add(temp);
//                    banner.setCanLoop(false);
//                    banner.setPages(bannerItemList, new MZHolderCreator<BannerViewHolder>() {
//                        @Override
//                        public BannerViewHolder createViewHolder() {
//                            return new BannerViewHolder();
//                        }
//                    });
//                    banner.start();
//                    //Toast.makeText(HContext,"加载banner出错！"+e.toString(),Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onResume() {
        super.onResume();
        title.setText(getResources().getStringArray(R.array.months_full)[now.get(Calendar.MONTH)] + String.format(getString(R.string.date_day), now.get(Calendar.DAY_OF_MONTH)));
        if (timeTableCore.isDataAvailable()) {
            if (timeTableCore.isThisTerm())
                subtitle.setText(String.format(getString(R.string.week), timeTableCore.getCurrentCurriculum().getWeekOfTerm(now)) + " " +
                        getResources().getStringArray(R.array.dow1)[TimetableCore.getDOW(now) - 1]);
            else
                subtitle.setText(getString(R.string.navi_semister_not_begun) + " " + getResources().getStringArray(R.array.dow1)[TimetableCore.getDOW(now) - 1]);

        } else subtitle.setText(getString(R.string.navi_semister_no_data));
        Refresh(firstEnter, false);
        if (firstEnter) firstEnter = false;
        //banner.start();
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
                    Intent i = new Intent(getActivity(), ActivityLeaderBoard.class);
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


    class RefreshTask extends AsyncTask {
        List<BannerItem> bannerResult;
        boolean animate;
        boolean swipeRefresh;

        RefreshTask(boolean animate, boolean swipeRefresh) {
            this.animate = animate;
            this.swipeRefresh = swipeRefresh;
        }

        boolean first_enter_drag_hint = false;

        @Override
        protected void onPreExecute() {
            if (swipeRefresh) refreshLayout.setRefreshing(true);
            super.onPreExecute();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                BmobQuery<BannerItem> bq = new BmobQuery<>();
                bannerResult = bq.findObjectsSync(BannerItem.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            listRes.clear();
            for(int i=0;i<cardNames.length;i++){
                String card = cardNames[i];
                int type = cardTyps[i];
                boolean enable = naviPagePreference.getBoolean(card+"_enable",true);
                int power = naviPagePreference.getInt(card+"_power",i);
                if(enable){
                    Map<String,Object> m = new HashMap();
                    m.put("type",type);
                    m.put("type_name",card);
                    m.put("power",power);
                    listRes.add(m);
                }
            }
            Collections.sort(listRes, new Comparator<Map<String, Object>>() {
                @Override
                public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                    int power1 = (int) o1.get("power");
                    int power2 = (int)o2.get("power");
                    return power1-power2;
                }
            });
           // order = gson.fromJson(naviPagePreference.getString(ORDER_NAME, ""), List.class);
            first_enter_drag_hint = naviPagePreference.getBoolean("first_enter_navipage_hint_drag", true);
            BmobUser.fetchUserInfo(new FetchUserInfoListener<BmobUser>() {
                @Override
                public void done(BmobUser bmobUser, BmobException e) {
                    if (e == null) {
                        CurrentUser = BmobUser.getCurrentUser(HITAUser.class);
                        Intent i = new Intent();
                        i.setAction("COM.STUPIDTREE.HITA.USER_INFO_FETCHED");
                        LocalBroadcastManager.getInstance(HContext).sendBroadcast(i);
                    }
                }
            });
            return null;
        }


        private void addType(List<Map<String, Object>> list, int type, int position) {
            Map<String, Object> m = new HashMap<>();
            m.put("type", type);
            list.add(position, m);
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            listAdapter.notifyDataSetChanged();
            if (swipeRefresh) refreshLayout.setRefreshing(false);
            if (bannerResult != null && bannerResult.size() > 0) {
                bannerItemList.clear();
                for (BannerItem bi : bannerResult) {
                    if (bi.getType() .equals("advertisement")) bannerItemList.add(bi);
                    else if (bi.getType().equals("notification")) {
                        if (!naviPagePreference.getBoolean("notifi_clicked:" + bi.getObjectId(), false)) {
                            HashMap m = new HashMap();
                            m.put("item", bi);
                            listAdapter.addItem(TYPE_NOTIFICATION, 0, m);
                        }
                    }
                }
                if (bannerResult.size() == 1) banner.setCanLoop(false);
                else banner.setCanLoop(true);
                banner.setPages(bannerItemList, new MZHolderCreator<BannerViewHolder>() {
                    @Override
                    public BannerViewHolder createViewHolder() {
                        return new BannerViewHolder();
                    }
                });
                if (bannerItemList.size() == 0) {
                    banner.setVisibility(View.GONE);
                } else {
                    banner.setVisibility(View.VISIBLE);
                    banner.start();
                }
            } else {
                banner.setVisibility(View.GONE);    }

            if (first_enter_drag_hint)
                addType(listRes, TYPE_HINT, 0);//listRes.add(0,NaviPageAdapter.TYPE_HINT);
            if (animate) list.scheduleLayoutAnimation();
        }
    }

}


