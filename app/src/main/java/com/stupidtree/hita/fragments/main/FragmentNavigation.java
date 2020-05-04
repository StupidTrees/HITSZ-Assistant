package com.stupidtree.hita.fragments.main;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.BaseListAdapter;
import com.stupidtree.hita.adapter.NaviPageAdapter;
import com.stupidtree.hita.fragments.BaseFragment;
import com.stupidtree.hita.fragments.BasicRefreshTask;
import com.stupidtree.hita.online.BannerItem;
import com.stupidtree.hita.views.WrapContentLinearLayoutManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

import static com.stupidtree.hita.HITAApplication.TPE;
import static com.stupidtree.hita.HITAApplication.timeTableCore;
import static com.stupidtree.hita.adapter.NaviPageAdapter.TYPE_BOARD_JW;
import static com.stupidtree.hita.adapter.NaviPageAdapter.TYPE_HINT;
import static com.stupidtree.hita.adapter.NaviPageAdapter.TYPE_HITA;
import static com.stupidtree.hita.adapter.NaviPageAdapter.TYPE_MOOD;
import static com.stupidtree.hita.adapter.NaviPageAdapter.TYPE_NEWS;
import static com.stupidtree.hita.adapter.NaviPageAdapter.TYPE_NOTIFICATION;
import static com.stupidtree.hita.timetable.TimeWatcherService.TIMETABLE_CHANGED;
import static com.stupidtree.hita.timetable.TimeWatcherService.USER_CHANGED;

public class FragmentNavigation extends BaseFragment implements NaviPageAdapter.NaviRoot, BasicRefreshTask.ListRefreshedListener2<List<NavigationCardItem>> {

    public static String[] cardNames = new String[]{"mood", "jw", "news", "life"};
    public static Integer[] cardType = new Integer[]{TYPE_MOOD, TYPE_BOARD_JW, TYPE_NEWS, TYPE_HITA};
    private boolean timeTableChangedOnResume = false;
    private boolean userChangedOnResume = false;
    private List<NavigationCardItem> listRes;
    private RecyclerView list;
    private NaviPageAdapter listAdapter;
    private SwipeRefreshLayout refreshLayout;
    private List<BannerItem> bannerItemList;
    private Calendar firstEnterTime;
    private SharedPreferences naviPagePreference;
    private BroadcastReceiver receiver;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        naviPagePreference = requireActivity().getSharedPreferences("navi_page", Context.MODE_PRIVATE);
        initBroadcastReceiver();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        initViews(v);
        initList(v);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(receiver);
    }

    private void initList(View v) {
        list = v.findViewById(R.id.navipage_list);
        list.setVisibility(View.INVISIBLE);
        listRes = new ArrayList<>();
        listAdapter = new NaviPageAdapter(listRes, getBaseActivity(), this);
        list.setAdapter(listAdapter);
        list.setLayoutManager(new WrapContentLinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        NaviPageAdapter.mCallBack mCallBack = new NaviPageAdapter.mCallBack(listAdapter);
        ItemTouchHelper helper = new ItemTouchHelper(mCallBack);
        helper.attachToRecyclerView(list);
    }

    private void initBroadcastReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Objects.equals(intent.getAction(), TIMETABLE_CHANGED)) {
                    if (isResumed()) {
                        //刷新头部、更新MOOD信息
                        listAdapter.notifyItemChanged(0);
                    } else {
                        timeTableChangedOnResume = true;
                    }
                } else if (Objects.equals(intent.getAction(), USER_CHANGED)) {
                    //  Log.e("receive",intent.getAction());
                    if (isResumed()) {
                        updateMoodItem();
                    } else {
                        userChangedOnResume = true;
                    }
                }

            }
        };
        IntentFilter ifi = new IntentFilter();
        ifi.addAction(TIMETABLE_CHANGED);
        ifi.addAction(USER_CHANGED);
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(receiver, ifi);
    }

    private void updateMoodItem() {
        for (int i = 0; i < listRes.size(); i++) {
            NavigationCardItem npi = listRes.get(i);
            if (npi.getType() == TYPE_MOOD) {
                listAdapter.notifyItemChanged(i + 1);
                break;
            }
        }
    }

    private void initViews(View v) {
        bannerItemList = new ArrayList<>();
        refreshLayout = v.findViewById(R.id.refresh);
        refreshLayout.setColorSchemeColors(getColorAccent(), getColorAccent());
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Refresh(true, true, false);
            }
        });

    }

    @SuppressLint("WrongConstant")


    @Override
    protected void stopTasks() {

    }

    @Override
    public void Refresh() {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_navi;
    }


    @Override
    public SharedPreferences getPreferences() {
        return naviPagePreference;
    }

    @Override
    public List<BannerItem> getADBanners() {
        return bannerItemList;
    }

    //    refreshUnchanged;对于位置不动的元素进行刷新
//     refreshBanner; 刷新Banner和通知
//    total; 全部刷新模式
    public void Refresh(boolean refreshUnChanged, boolean refreshBanner, boolean swipe) {
        //refreshBanner();
        HashMap<Integer, NavigationCardItem> toSave = new HashMap<>();
        //将非固定的保存起来
        for (NavigationCardItem npi : listRes) {
            boolean isBasic = false;
            for (int x : cardType) {
                if (npi.getType() == x) {
                    isBasic = true;
                    break;
                }
            }
            if (!isBasic) {
                toSave.put(listRes.indexOf(npi), npi);
            }
        }
        new RefreshTask(this, naviPagePreference, toSave).executeOnExecutor(TPE,
                refreshUnChanged, refreshBanner, swipe
        );


    }


    private void fetchBannerItems() {
        BmobQuery<BannerItem> bq = new BmobQuery<>();
        bq.findObjects(new FindListener<BannerItem>() {
            @Override
            public void done(List<BannerItem> queryResult, BmobException e) {
                if (queryResult != null && e == null) {
                    List<BannerItem> bannerResult = new ArrayList<>();
                    PackageInfo packageInfo = null;
                    try {
                        packageInfo = requireActivity().getPackageManager()
                                .getPackageInfo(requireActivity().getPackageName(), 0);
                    } catch (PackageManager.NameNotFoundException x) {
                        x.printStackTrace();
                    }
                    if (packageInfo != null) {
                        int code = packageInfo.versionCode;
                        for (BannerItem bi : queryResult) {
                            boolean down = bi.getShowAfterVersion() < 0 || code >= bi.getShowAfterVersion();
                            boolean up = bi.getShowBeforeVersion() < 0 || code < bi.getShowBeforeVersion();
                            if (up && down) {
                                bannerResult.add(bi);
                            }
                        }
                    } else {
                        bannerResult.addAll(queryResult);
                    }
                    if (bannerResult.size() > 0) {
                        bannerItemList.clear();
                        boolean notificationFetched = false;
                        List<NavigationCardItem> newList = new ArrayList<>();
                        for (NavigationCardItem npi : listRes) {
                            if (npi.getType() != TYPE_NOTIFICATION) newList.add(npi);
                        }

                        for (BannerItem bi : bannerResult) {
                            if (bi.getType().equals("advertisement")) {
                                bannerItemList.add(bi);
                            } else if (bi.getType().equals("notification")) {

                                if (!naviPagePreference.getBoolean("notifi_clicked:" + bi.getObjectId(), false)) {
                                    NavigationCardItem naviPageItem = new NavigationCardItem(TYPE_NOTIFICATION, "notification");
                                    naviPageItem.putNotificationExtra(bi);
                                    if (!newList.contains(naviPageItem)) {
                                        newList.add(0, naviPageItem);
                                        notificationFetched = true;
                                    }

                                }
                            }
                        }
                        listAdapter.notifyItemChanged(0); //刷新头部，显示Banner
                        if (notificationFetched) {
                            listAdapter.notifyItemChangedSmooth(newList, new BaseListAdapter.RefreshJudge<NavigationCardItem>() {
                                @Override
                                public boolean judge(NavigationCardItem data) {
                                    return data.getType() == TYPE_NOTIFICATION;
                                }
                            });

                        }
                    }
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onResume() {
        super.onResume();
        if (firstEnterTime != null) { //不是第一次进
            Calendar target = (Calendar) firstEnterTime.clone();
            target.add(Calendar.MINUTE, 10); //每10分钟刷新一次
            if (target.before(timeTableCore.getNow())) { //间隔一小时后，可以刷新banner
                firstEnterTime.setTimeInMillis(System.currentTimeMillis());
                Refresh(false, true, false);
            } else {
                Refresh(false, false, false); //否则刷新别的
            }
        } else {
            firstEnterTime = Calendar.getInstance();
            Refresh(false, true, true);
        }
        if (timeTableChangedOnResume) {
            listAdapter.notifyItemChanged(0);
            timeTableChangedOnResume = false;
        }
        if (userChangedOnResume) {
            updateMoodItem();
            userChangedOnResume = false;
        }

    }

    @Override
    public void onRefreshStart(String id, Boolean[] params) {
        if (params != null && params.length > 2 && params[2]) refreshLayout.setRefreshing(true);
    }

    @Override
    public void onListRefreshed(String id, Boolean[] params, Object result) {

    }

    @Override
    public void onListRefreshed(String id, Boolean[] params, List<NavigationCardItem> newList, Object[] others) {
        if (isRemoving() || isDetached() || others.length < 1 || params.length < 3) return;
        list.setVisibility(View.VISIBLE);
        refreshLayout.setRefreshing(false);
        boolean refreshUnchanged = params[0];
        boolean refreshBanner = params[1];
        boolean total = params[2];

        HashMap<Integer, NavigationCardItem> toSave = (HashMap<Integer, NavigationCardItem>) others[0];
        List<HashMap.Entry<Integer, NavigationCardItem>> savedList = new ArrayList<>(toSave.entrySet());
        Collections.sort(savedList, new Comparator<Map.Entry<Integer, NavigationCardItem>>() {
            @Override
            public int compare(Map.Entry<Integer, NavigationCardItem> o1, Map.Entry<Integer, NavigationCardItem> o2) {
                return o1.getKey() - o2.getKey();
            }
        });
        boolean hint_included = false;
        for (Map.Entry<Integer, NavigationCardItem> item : savedList) {
            int index = item.getKey();
            NavigationCardItem npi = item.getValue();
            if (npi.getType() == TYPE_HINT) hint_included = true;
            if (index < 0) index = 0;
            if (index > newList.size() - 1) newList.add(npi);
            else newList.add(index, npi);
        }
        boolean first_enter_drag_hint = naviPagePreference.getBoolean("first_enter_navipage_hint_drag", true);
        if (first_enter_drag_hint && !hint_included) {
            newList.add(0, new NavigationCardItem(TYPE_HINT, "hint"));
        }
        if (total) {
            listRes.clear();
            listRes.addAll(newList);
            listAdapter.notifyDataSetChanged();
            list.scheduleLayoutAnimation();
        } else {
            listAdapter.notifyItemChangedSmooth(newList, refreshUnchanged);
        }

        if (refreshBanner) fetchBannerItems();
    }


    static class RefreshTask extends BasicRefreshTask<List<NavigationCardItem>> {

        SharedPreferences naviPagePreference;
        HashMap<Integer, NavigationCardItem> toSave;


        RefreshTask(ListRefreshedListener listRefreshedListener, SharedPreferences naviPagePreference, HashMap<Integer, NavigationCardItem> toSave) {
            super(listRefreshedListener);
            this.naviPagePreference = naviPagePreference;
            this.toSave = toSave;
            others = new Object[]{toSave};
        }


        @Override
        protected List<NavigationCardItem> doInBackground(ListRefreshedListener listRefreshedListener, Boolean... booleans) {
            super.doInBackground(listRefreshedListener, booleans);
            List<NavigationCardItem> newList = new ArrayList<>();
            for (int i = 0; i < cardNames.length; i++) {
                String card = cardNames[i];
                int type = cardType[i];
                boolean enable = naviPagePreference.getBoolean(card + "_enable", true);
                int power = naviPagePreference.getInt(card + "_power", i);
                if (enable) {
                    NavigationCardItem npi = new NavigationCardItem(type, card);
                    npi.setPower(power);
                    newList.add(npi);
                }
            }
            Collections.sort(newList, new Comparator<NavigationCardItem>() {
                @Override
                public int compare(NavigationCardItem o1, NavigationCardItem o2) {
                    return o1.compareTo(o2);
                }
            });
            // order = gson.fromJson(naviPagePreference.getString(ORDER_NAME, ""), List.class);
            return newList;
        }

    }

}


