package com.stupidtree.hita.activities;

import  java.lang.String;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.widget.Toolbar;

import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;
import com.stupidtree.hita.util.WalkRouteOverlay;
import com.stupidtree.hita.util_navi.AMapUtil;
import com.stupidtree.hita.util_navi.ToastUtil;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.amap.api.maps.CameraUpdateFactory.newLatLng;
import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.defaultSP;


public class ActivityExplore extends BaseActivity implements AMap.OnMapClickListener, AMap.OnMarkerClickListener, AMap.OnInfoWindowClickListener, AMap.InfoWindowAdapter, RouteSearch.OnRouteSearchListener, AMap.OnMapLongClickListener, AMap.OnMyLocationChangeListener {

    private Button btn1;
    private Toolbar mToolbar;
    FloatingActionButton fab;
    MapView mapView=null;
    private AMap aMap=null;
    int click_darkmode=0; //昼夜模式开关
    private MarkerOptions mStartMarker,mEndMarker;

    //导航变量
    NaviLatLng mStartPointNavi
            ,mEndPointNavi;
    LatLonPoint mStartPoint=null,
            mEndPoint=null;
    private final int ROUTE_TYPE_WALK = 3;
    private RouteSearch mRouteSearch;
    private WalkRouteResult mWalkRouteResult;

    private LinearLayout mBottomLayout;
    private TextView mRotueTimeDes, mRouteDetailDes;
    private ProgressDialog progDialog = null;// 搜索时进度条

    String terminal;//目的地字符串
    boolean longClickAvailable;
    boolean first = true;
    List<Map<String,String>> lectureList;

    @Override
    protected void stopTasks() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true,true,false);
        setContentView(R.layout.activity_explore);
        terminal = getIntent().getStringExtra("terminal");
        lectureList = new ArrayList<>();
        //if(terminal==null||terminal.isEmpty()) mToolbar.setTitle("探索");
        //else mToolbar.setTitle("前往:"+terminal);
        longClickAvailable = terminal==null||terminal.isEmpty();//只有在探索模式才能长按导航
        //设置状态栏透明 添加返回图标
        mToolbar = findViewById(R.id.toolbar);
        if(!longClickAvailable)  mToolbar.setTitle("前往"+terminal);
        else mToolbar.setTitle("探索模式");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //点击按钮回到定位点
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mStartPoint==null) return;
                LatLng ll= new LatLng(mStartPoint.getLatitude(),mStartPoint.getLongitude());//定位点
                aMap.moveCamera(newLatLng(ll));
                aMap.moveCamera(CameraUpdateFactory.zoomTo((float)16.7));//设置地图的放缩级别
            }
        });
        //显示地图，定位
        mapView =  findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        if (aMap == null) {
            Init_map();
            //为amap添加事件监听器
            aMap.setOnMapLongClickListener(this);
        }
        new LoadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        changeDayAndNight();//昼夜模式
        if(defaultSP.getBoolean("firstOpen_explore",true)&&longClickAvailable){
            Guide();
            defaultSP.edit().putBoolean("firstOpen_explore",false).apply();
        }
    }


    void Guide(){
        TapTargetSequence tts = new TapTargetSequence(this).targets(
                TapTarget.forView(mapView,"欢迎使用探索模式","发现大学城每一天的惊喜") .drawShadow(true)
                        .cancelable(false)
                        .tintTarget(true)
                        .transparentTarget(false)
                        .outerCircleColor(R.color.blue_accent)
                        .titleTextSize(24)
                        .icon(getDrawable(R.drawable.bt_done)),
                TapTarget.forView(fab,"使用该按钮定位到当前位置","请允许HITSZ助手使用位置权限") .drawShadow(true)
                .cancelable(false)
                .tintTarget(true)
                .transparentTarget(false)
                .outerCircleColor(R.color.blue_accent)
                .titleTextSize(24),
                TapTarget.forView(mapView,"长按地图上的任意点进行寻路规划").drawShadow(true)
                        .cancelable(false)
                        .tintTarget(true)
                        .transparentTarget(false)
                        .outerCircleColor(R.color.blue_accent)
                        .titleTextSize(24)
                        .icon(getDrawable(R.drawable.ic_location)),
                TapTarget.forView(mapView,"每天的事件、讲座将会以图标形式显示在地图上！")
                        .drawShadow(true)
                        .cancelable(false)
                        .tintTarget(true)
                        .transparentTarget(false)
                        .outerCircleColor(R.color.blue_accent)
                        .titleTextSize(24)
                        .icon(getDrawable(R.drawable.ic_meeting)),
                TapTarget.forView(mapView,"开始使用吧！")            .drawShadow(true)
                        .cancelable(false)
                        .tintTarget(true)
                        .transparentTarget(false)
                        .outerCircleColor(R.color.blue_accent)
                        .titleTextSize(24)
                        .icon(getDrawable(R.drawable.bt_done))
        );
        tts.start();
    }




    private void Init_map(){
        aMap = mapView.getMap();
        LatLng ll= new LatLng(22.587458,113.971198);//学校
        aMap.moveCamera(newLatLng(ll));//程序启动移至学校
        aMap.moveCamera(CameraUpdateFactory.zoomTo((float)16.7));//设置地图的放缩级别
        //设置默认定位按钮是否显示，非必需设置。
        aMap.getUiSettings().setMyLocationButtonEnabled(true);
        // 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        aMap.setMyLocationEnabled(true);

        final MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
        //连续定位、蓝点不会移动到地图中心点，定位点依照设备方向旋转，并且蓝点会跟随设备移动。
        myLocationStyle.interval(2000);
        myLocationStyle.radiusFillColor(Color.argb(50,0,204,255));

        //
        myLocationStyle.strokeColor(Color.parseColor("#00FFFF"));
        aMap.setMyLocationStyle(myLocationStyle);
        //设置蓝点精度圈的颜色
        aMap.setOnMyLocationChangeListener(this);
        //将定位和缩放按钮隐藏
        UiSettings mUiSettings;//定义一个UiSettings对象
        mUiSettings = aMap.getUiSettings();//实例化UiSettings类对象
        mUiSettings.setZoomControlsEnabled(false);
        mUiSettings.setMyLocationButtonEnabled(false);
    }

    class LoadTask extends AsyncTask {//更新当日会议


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            lectureList.clear();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                Document d = Jsoup.connect("http://www.hitsz.edu.cn/article/id-78.html?maxPageItems=10&keywords=&pager.offset=" + 0)
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .get();
                Elements e = d.select("ul[class^=lecture_n]");
                Elements ee = e.select("li");
                for (Element x : ee) {
                    String place, date;
                    place = x.select("div[class^=lecture_bottom]").select("div:contains(讲座地点)").size() > 0 ? x.select("div[class^=lecture_bottom]").select("div:contains(讲座地点)").get(1).text() : "";
                    date = x.select("div[class^=lecture_top]").select("span[class=date]").text();
                    String link = x.select("a").attr("href");
                    String title = x.select("a").text();
                    Map m = new HashMap();
                    m.put("place",place);
                    m.put("date",date);
                    m.put("link",link);
                    m.put("title",title);
                    lectureList.add(m);
                }
                return true;
            } catch (IOException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            for(Map<String,String> m:lectureList){
                whetherAddToMap(m.get("place"), m.get("date"),m.get("title"));
            }

        }
    }

    void whetherAddToMap(String place,String date,String title){
        //判定当日日程
        Calendar calendar=Calendar.getInstance();
        String month,day;
        //判断月份
        month=date.substring(0,2);
        int real_mon=(month.charAt(0)-'0')*10 +(month.charAt(1)-'0');
        if(real_mon!=calendar.get(Calendar.MONTH)+1)   return;
        //判断日期
        day=date.substring(4,6);
        int real_day=(day.charAt(0)-'0')*10 +(day.charAt(1)-'0');
        if(real_day!=calendar.get(Calendar.DAY_OF_MONTH))    return;
        //对地点进行处理
        MarkerOptions markerOption;
        LatLonPoint latLonPoint=null;
        if(place.contains("A"))latLonPoint =new LatLonPoint(22.5880360000,113.9697940000);
        else if(place.contains("T5"))latLonPoint =new LatLonPoint(22.586149,113.974271);
        //标识当日会议
        if(latLonPoint!=null) {
            //设置图标
            markerOption=new MarkerOptions()
                    .position(AMapUtil.convertToLatLng(latLonPoint))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.meeting));
            markerOption.draggable(false);//设置Marker不可拖动
            // 将Marker设置为贴地显示，可以双指下拉地图查看效果
            markerOption.setFlat(true);//设置marker平贴地图效果
            markerOption.title(title);
            markerOption.snippet(place);
            aMap.addMarker(markerOption);
        }
    }
   private void changeDayAndNight(){
        btn1 = findViewById(R.id.button1);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(click_darkmode==0){ click_darkmode++;  btn1.setText("夜");
                    btn1.setBackgroundColor(Color.parseColor("#1A237E"));
                    btn1.setTextColor(Color.parseColor("#000000"));
                    aMap.setMapType(AMap.MAP_TYPE_NIGHT);}//夜景地图，aMap是地图控制器对象。
                else{ click_darkmode=0; aMap.setMapType(AMap.MAP_TYPE_NORMAL);   btn1.setText("昼");
                    btn1.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    btn1.setTextColor(Color.parseColor("#1A237E"));}
            }
        });
    }

    private void setfromandtoMarker() {
        mStartMarker=new MarkerOptions()
                .position(AMapUtil.convertToLatLng(mStartPoint))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.start));
        mEndMarker=new MarkerOptions()
                .position(AMapUtil.convertToLatLng(mEndPoint))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.end));
        aMap.addMarker(mStartMarker);
        aMap.addMarker(mEndMarker);
    }

    private void Init_walkpath(){

        aMap.setOnMapClickListener(ActivityExplore.this);
        aMap.setOnMarkerClickListener(ActivityExplore.this);
        aMap.setOnInfoWindowClickListener(ActivityExplore.this);
        aMap.setInfoWindowAdapter(ActivityExplore.this);
        mRouteSearch = new RouteSearch(this);
        mRouteSearch.setRouteSearchListener(this);
        //让主布局消失
        mBottomLayout =  findViewById(R.id.head_layout);
        //mHeadLayout= (FrameLayout)findViewById(R.id.mainFram);
        mBottomLayout.setVisibility(View.GONE);
        mRotueTimeDes = (TextView) findViewById(R.id.firstline);
        mRouteDetailDes = (TextView) findViewById(R.id.secondline);


    }
    /**
     * 对触摸地图事件回调
     */



    @Override
    public void onWalkRouteSearched(WalkRouteResult result, int errorCode) {
        //dissmissProgressDialog();
        aMap.clear();// 清理地图上的所有覆盖物
        if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getPaths() != null) {
                if (result.getPaths().size() > 0) {
                    mWalkRouteResult = result;
                    final WalkPath walkPath = mWalkRouteResult.getPaths()
                            .get(0);
                    if(walkPath == null) {
                        return;
                    }
                    WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(
                            this, aMap, walkPath,
                            mWalkRouteResult.getStartPos(),
                            mWalkRouteResult.getTargetPos());
                    walkRouteOverlay.removeFromMap();
                    walkRouteOverlay.addToMap();
                    walkRouteOverlay.zoomToSpan();
                    mBottomLayout.setVisibility(View.VISIBLE);
                    int dis = (int) walkPath.getDistance();
                    int dur = (int) walkPath.getDuration();
                    String des = AMapUtil.getFriendlyTime(dur)+"("+AMapUtil.getFriendlyLength(dis)+")";
                    mRotueTimeDes.setText(des);
                    mRouteDetailDes.setVisibility(View.GONE);
                    mBottomLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(HContext,
                                    ActivityWalkRouteDetail.class);
                            intent.putExtra("walk_path", walkPath);
                            intent.putExtra("walk_result",
                                    mWalkRouteResult);
                            intent.putExtra("start",mStartPointNavi);
                            intent.putExtra("end",mEndPointNavi);
                            startActivity(intent);
                        }
                    });
                } else if (result != null && result.getPaths() == null) {
                    ToastUtil.show(HContext,R.string.no_result);
                }
            } else {
                mBottomLayout.setVisibility(View.GONE);
                ToastUtil.show(HContext, R.string.no_result);
            }
        } else {
            ToastUtil.showerror(this.getApplicationContext(), errorCode);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mapView.onDestroy();
        //since 1.6.0 不再在naviview destroy的时候自动执行AMapNavi.stopNavi();请自行执行
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mapView.onPause();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    public void searchRouteResult(int routeType, int mode) {
        /**
         * 开始搜索路径规划方案
         */
        if (mStartPoint == null) {
            ToastUtil.show(HContext, "定位中，稍后再试...");
            return;
        }

        if (mEndPoint == null) {
            ToastUtil.show(HContext, "终点未设置");
            return;
        }
        setfromandtoMarker();
        // showProgressDialog();
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
                mStartPoint, mEndPoint);
        if (routeType == ROUTE_TYPE_WALK) {// 步行路径规划
            RouteSearch.WalkRouteQuery query = new RouteSearch.WalkRouteQuery(fromAndTo, mode);
            mRouteSearch.calculateWalkRouteAsyn(query);// 异步路径规划步行模式查询
        }
    }
    /**
     * 显示进度框
     */
    private void showProgressDialog() {
        if (progDialog == null) {
            progDialog = new ProgressDialog(this);
        }
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(true);
        progDialog.setMessage("正在搜索");
        progDialog.show();
    }
    /**
     * 隐藏进度框
     */
    private void dissmissProgressDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if(!longClickAvailable) return;
        //mEndPointNavi = new NaviLatLng(latLng.latitude,latLng.longitude);
        mEndPoint = new LatLonPoint(latLng.latitude,latLng.longitude);
        mEndPointNavi=new NaviLatLng(latLng.latitude,latLng.longitude);
        //获取AMapNavi实例
//添加监听回调，用于处理算路成功
        //初始化query对象，fromAndTo是包含起终点信息，walkMode是步行路径规划的模式
        RouteSearch mRouteSearch;
        Init_walkpath();
        searchRouteResult(ROUTE_TYPE_WALK, RouteSearch.WalkDefault);
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode)
        {
            case 1:
                if(resultCode==RESULT_OK) {
                    String returnData = data.getStringExtra("search_result");
                    if(returnData.equals("荔园一食堂")){
                         mEndPoint=new LatLonPoint(22.586012,113.968799);
                        RouteSearch mRouteSearch;
                        Init_walkpath();
                        searchRouteResult(ROUTE_TYPE_WALK, RouteSearch.WalkDefault);
                    }
                    break;
                }
            default:
        }
    }*/

    //    @Override
//    public void onBackPressed() {
//        if (mBottomLayout.isEnabled()) {
//            mBottomLayout.setVisibility(View.GONE);
//        } else {
//            super.onBackPressed();
//        }
//    }
    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {

    }


    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

    }


    @Override
    public void onMyLocationChange(Location location) {
        //位置改变的时候更新导航起点信息
        mStartPointNavi = new NaviLatLng(location.getLatitude(),location.getLongitude());
        mStartPoint=new LatLonPoint(location.getLatitude(),location.getLongitude());
        if(first){
            if(!longClickAvailable){
                SparseArray<Double> sa = getTerminalInfo(terminal);
                double latitude = sa.get(0);
                double longitude = sa.get(1);
                mEndPoint = new LatLonPoint(latitude,longitude);
                mEndPointNavi=new NaviLatLng(latitude,longitude);
                Init_walkpath();
                searchRouteResult(ROUTE_TYPE_WALK, RouteSearch.WalkDefault);
            }
            first = false;
        }
    }


    SparseArray<Double> getTerminalInfo(String classroom){
        double des_Latitute = 0;
        double des_Longtitude = 0;
        String title = "",snippet="";
        if (classroom.contains("T3")) {
            des_Latitute = 22.5858370000;
            des_Longtitude = 113.9723130000;
        }else if(classroom.contains("T4")){
            des_Latitute = 22.5852590000;
            des_Longtitude = 113.9723000000;

        }else if(classroom.contains("T5")){
            des_Latitute = 22.5852840000;
            des_Longtitude = 113.9732760000;

        }else if(classroom.contains("T6")){
            des_Latitute = 22.5851010000;
            des_Longtitude = 113.9738560000;

        }else if(classroom.contains("T2")){
            des_Latitute = 22.5861260000;
            des_Longtitude = 113.9743360000;

        }else if(classroom.contains("H")){
            des_Latitute =22.5860810000;
            des_Longtitude = 113.9733010000;

        }else if(classroom.contains("K")){
            des_Latitute =22.5869310000;
            des_Longtitude = 113.9728850000;

        }else if(classroom.contains("L")){
            des_Latitute =22.5879050000;
            des_Longtitude = 113.9728200000;

        }else if(classroom.contains("A")){
            des_Latitute =22.5880360000;
            des_Longtitude = 113.9697940000;
        }
        if(getIntent().getDoubleExtra("longitude",-1.0)>0) des_Longtitude = getIntent().getDoubleExtra("longitude",-1);
        if(getIntent().getDoubleExtra("latitude",-1.0)>0) des_Latitute = getIntent().getDoubleExtra("latitude",-1);
        SparseArray res = new SparseArray();
        res.put(0,des_Latitute);
        res.put(1,des_Longtitude);
        return res;

    }

}
