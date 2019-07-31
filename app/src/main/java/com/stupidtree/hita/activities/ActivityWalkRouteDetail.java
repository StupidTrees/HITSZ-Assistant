package com.stupidtree.hita.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.navi.enums.NaviType;
import com.amap.api.navi.model.AMapCalcRouteResult;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapModelCross;
import com.amap.api.navi.model.AMapNaviCameraInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviRouteNotifyData;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AMapServiceAreaInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.services.route.WalkPath;
import com.autonavi.tbt.TrafficFacilityInfo;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.WalkSegmentListAdapter;
import com.stupidtree.hita.util_navi.AMapUtil;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.ArrayList;
import java.util.List;

public class ActivityWalkRouteDetail extends BaseActivity implements AMapNaviViewListener, AMapNaviListener {
    private WalkPath mWalkPath;
    private RecyclerView mWalkSegmentList;
    private WalkSegmentListAdapter mWalkSegmentListAdapter;
    //private WalkRouteResult mWalkRouteResult;
    private AMapNavi mAMapNavi;
    private AMapNaviView mAMapNaviView;
    //导航变量
    NaviLatLng mStartPointNavi=null
            ,mEndPointNavi=null;
    Toolbar mToolbar;
    ExpandableLayout expandableLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setWindowParams(true,false,false);
        setContentView(R.layout.activity_walk_route_detail);
        getIntentData();
        initToolbar();
        Navigation(savedInstanceState);

    }

    void initToolbar(){
        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle("步行路线详情");
        mToolbar.setBackgroundColor(getColorPrimary());
        String dur = AMapUtil.getFriendlyTime((int) mWalkPath.getDuration());
        String dis = AMapUtil
                .getFriendlyLength((int) mWalkPath.getDistance());
        mToolbar.setSubtitle(dur + "(" + dis + ")");

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expandableLayout.toggle();
            }
        });

        expandableLayout = findViewById(R.id.navi_expandable);
        mWalkSegmentList = findViewById(R.id.bus_segment_list);
        mWalkSegmentListAdapter = new WalkSegmentListAdapter(
                this.getApplicationContext(), mWalkPath.getSteps());
        mWalkSegmentList.setAdapter(mWalkSegmentListAdapter);
        LinearLayoutManager lm = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        mWalkSegmentList.setLayoutManager(lm);

    }

    private void Navigation(Bundle savedInstanceState){
        mAMapNaviView = findViewById(R.id.navi_view);
        //mAMapNaviView.setVisibility(View.VISIBLE);
        mAMapNaviView.onCreate(savedInstanceState);
        mAMapNaviView.setAMapNaviViewListener(this);
        //获取AMapNavi实例
        mAMapNavi = AMapNavi.getInstance(this);
        mAMapNavi.setEmulatorNaviSpeed(20);
        mAMapNaviView.setBackgroundColor(getColorPrimary());int strategyFlag = 0;
        List<NaviLatLng> startList = new ArrayList<NaviLatLng>();
        /**
         * 途径点坐标集合
         */
        List<NaviLatLng> wayList = new ArrayList<NaviLatLng>();
        /**
         * 终点坐标集合［建议就一个终点］
         */
        List<NaviLatLng> endList = new ArrayList<NaviLatLng>();
        try {
            strategyFlag = mAMapNavi.strategyConvert(true, false, false, true, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        startList.add(mStartPointNavi);
        endList.add(mEndPointNavi);
        mAMapNavi.calculateDriveRoute(startList, endList, wayList, strategyFlag);
        // mAMapNaviView.
        //mAMapNavi.startNavi(NaviType.GPS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mAMapNaviView.onDestroy();
        //since 1.6.0 不再在naviview destroy的时候自动执行AMapNavi.stopNavi();请自行执行
        mAMapNavi.stopNavi();
        mAMapNavi.destroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mAMapNaviView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mAMapNaviView.onPause();
    }
    private void getIntentData() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        mWalkPath = intent.getParcelableExtra("walk_path");
        mStartPointNavi=intent.getParcelableExtra("start");
        mEndPointNavi=intent.getParcelableExtra("end");
    }

    public void onBackClick(View view) {
        this.finish();
    }

    @Override
    public void onNaviSetting() {

    }

    @Override
    public void onNaviCancel() {

    }

    @Override
    public boolean onNaviBackClick() {
        return false;
    }

    @Override
    public void onNaviMapMode(int i) {

    }

    @Override
    public void onNaviTurnClick() {

    }

    @Override
    public void onNextRoadClick() {

    }

    @Override
    public void onScanViewButtonClick() {

    }

    @Override
    public void onLockMap(boolean b) {

    }

    @Override
    public void onNaviViewLoaded() {

    }

    @Override
    public void onMapTypeChanged(int i) {

    }

    @Override
    public void onNaviViewShowMode(int i) {

    }

    @Override
    public void onInitNaviFailure() {

    }

    @Override
    public void onInitNaviSuccess() {
        Log.e("!!","!!!");
        mAMapNavi.calculateWalkRoute(mStartPointNavi,mEndPointNavi);
    }

    @Override
    public void onStartNavi(int i) {

    }

    @Override
    public void onTrafficStatusUpdate() {

    }

    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {

    }

    @Override
    public void onGetNavigationText(int i, String s) {

    }

    @Override
    public void onGetNavigationText(String s) {

    }

    @Override
    public void onEndEmulatorNavi() {

    }

    @Override
    public void onArriveDestination() {

    }

    @Override
    public void onCalculateRouteFailure(int i) {

    }

    @Override
    public void onReCalculateRouteForYaw() {

    }

    @Override
    public void onReCalculateRouteForTrafficJam() {

    }

    @Override
    public void onArrivedWayPoint(int i) {

    }

    @Override
    public void onGpsOpenStatus(boolean b) {

    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {

    }

    @Override
    public void onNaviInfoUpdated(AMapNaviInfo aMapNaviInfo) {

    }

    @Override
    public void updateCameraInfo(AMapNaviCameraInfo[] aMapNaviCameraInfos) {

    }

    @Override
    public void updateIntervalCameraInfo(AMapNaviCameraInfo aMapNaviCameraInfo, AMapNaviCameraInfo aMapNaviCameraInfo1, int i) {

    }

    @Override
    public void onServiceAreaUpdate(AMapServiceAreaInfo[] aMapServiceAreaInfos) {

    }

    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {

    }

    @Override
    public void hideCross() {

    }

    @Override
    public void showModeCross(AMapModelCross aMapModelCross) {

    }

    @Override
    public void hideModeCross() {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo[] aMapLaneInfos, byte[] bytes, byte[] bytes1) {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo aMapLaneInfo) {

    }

    @Override
    public void hideLaneInfo() {

    }

    @Override
    public void onCalculateRouteSuccess(int[] ints) {
        //super.onCalculateRouteSuccess();
        mAMapNavi.startNavi(AMapNavi.EmulatorNaviMode);
    }

    @Override
    public void notifyParallelRoad(int i) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {

    }

    @Override
    public void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo) {

    }

    @Override
    public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {

    }

    @Override
    public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {

    }

    @Override
    public void onPlayRing(int i) {

    }

    @Override
    public void onCalculateRouteSuccess(AMapCalcRouteResult aMapCalcRouteResult) {

        mAMapNavi.startNavi(NaviType.EMULATOR);
    }

    @Override
    public void onCalculateRouteFailure(AMapCalcRouteResult aMapCalcRouteResult) {

    }

    @Override
    public void onNaviRouteNotify(AMapNaviRouteNotifyData aMapNaviRouteNotifyData) {

    }
}

