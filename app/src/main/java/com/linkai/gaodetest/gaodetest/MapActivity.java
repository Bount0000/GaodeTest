package com.linkai.gaodetest.gaodetest;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.idst.nls.internal.utils.L;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapException;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Poi;
import com.amap.api.navi.AmapNaviPage;
import com.amap.api.navi.AmapNaviParams;
import com.amap.api.navi.AmapNaviType;
import com.amap.api.navi.INaviInfoCallback;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.linkai.gaodetest.gaodetest.utils.LocationService;
import com.linkai.gaodetest.gaodetest.utils.PoiOverlay;
import com.linkai.gaodetest.gaodetest.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import static android.os.Build.VERSION_CODES.M;
import static com.amap.api.services.core.AMapException.CODE_AMAP_SUCCESS;
import static com.autonavi.ae.pos.LocManager.init;

public class MapActivity extends Activity implements View.OnClickListener, AMap.OnMapClickListener, AMap.OnInfoWindowClickListener, AMap.InfoWindowAdapter, AMap.OnMarkerClickListener,
        PoiSearch.OnPoiSearchListener, INaviInfoCallback {
    MapView mMapView = null;
    AMap aMap = null;
    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationOption;
    private static final int STROKE_COLOR = Color.argb(180, 3, 145, 255);
    private static final int FILL_COLOR = Color.argb(10, 0, 0, 180);
    private LatLonPoint lp;
    private Marker locationMarker;
    private Button btn_navi;
    private Marker mlastMarker;
    private Marker detailMarker;
    private RelativeLayout mPoiDetail;
    private TextView mPoiName;
    private TextView mPoiAddress;
    private EditText mSearchText;
    private int currentPage = 0;// 当前页面，从0开始计数
    private PoiSearch.Query query;
    private PoiResult poiResult; // poi返回的结果
    private PoiOverlay poiOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        setup();
    }

    @Override
    protected void onStart() {
        super.onStart();

        LocationService locationService = new LocationService(this);
        locationService.registerListener(new AMapLocationListener() {


            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                lp = new LatLonPoint(aMapLocation.getLatitude(), aMapLocation.getLongitude());
                initDa();
            }

        });
        locationService.startLocation();
    }

    private void initDa() {
        if (aMap == null) {
            aMap = mMapView.getMap();
            aMap.setOnMapClickListener(this);
            aMap.setOnMarkerClickListener(this);
            aMap.setOnInfoWindowClickListener(this);
            aMap.setInfoWindowAdapter(this);
            locationMarker = aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f).icon(BitmapDescriptorFactory
                    .fromBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.gps_point))).position(new LatLng(lp.getLatitude(), lp.getLongitude())));
            locationMarker.showInfoWindow();
        }
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lp.getLatitude(), lp.getLongitude()), 14));

    }

    private void setup() {
        TextView btn_search = findViewById(R.id.btn_search);
        mPoiDetail = (RelativeLayout) findViewById(R.id.poi_detail);
        mPoiName = (TextView) findViewById(R.id.poi_name);
        mPoiAddress = (TextView) findViewById(R.id.poi_address);
        mSearchText = (EditText) findViewById(R.id.input_edittext);
        btn_navi = (Button) findViewById(R.id.btn_navi);
        btn_search.setOnClickListener(this);
        btn_navi.setOnClickListener(this);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onMapClick(LatLng latLng) {

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
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.getObject() != null) {
            whetherToShowDetailInfo(true);
            PoiItem mCurrentPoi = (PoiItem) marker.getObject();
            if (mlastMarker == null) {
                mlastMarker = marker;
            } else {
                // 将之前被点击的marker置为原来的状态
                resetlastmarker();
                mlastMarker = marker;
            }
            detailMarker = marker;
            detailMarker.setIcon(BitmapDescriptorFactory
                    .fromBitmap(BitmapFactory.decodeResource(
                            getResources(),
                            R.drawable.poi_marker_pressed)));
            setPoiItemDisplayContent(mCurrentPoi);

        } else {
            whetherToShowDetailInfo(false);
            resetlastmarker();
        }
        return true;
    }

    @Override
    public void onInitNaviFailure() {

    }

    @Override
    public void onGetNavigationText(String s) {

    }

    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {

    }

    @Override
    public void onArriveDestination(boolean b) {

    }

    @Override
    public void onStartNavi(int i) {

    }

    @Override
    public void onCalculateRouteSuccess(int[] ints) {

    }

    @Override
    public void onCalculateRouteFailure(int i) {

    }

    @Override
    public void onStopSpeaking() {

    }

    @Override
    public void onReCalculateRoute(int i) {

    }

    @Override
    public void onExitPage(int i) {

    }

    @Override
    public void onStrategyChanged(int i) {

    }

    @Override
    public View getCustomNaviBottomView() {
        return null;
    }

    @Override
    public View getCustomNaviView() {
        return null;
    }

    @Override
    public void onArrivedWayPoint(int i) {

    }

    /**
     * 搜索结果
     *
     * @param result
     * @param rcode
     */
    @Override
    public void onPoiSearched(PoiResult result, int rcode) {
        if (rcode == CODE_AMAP_SUCCESS) {
            if (result != null && result.getQuery() != null) {// 搜索poi的结果
                if (result.getQuery().equals(query)) {//是否为同一条
                    poiResult = result;
                    ArrayList<PoiItem> poiItems = poiResult.getPois();//取得第一页的poiitem数据，页数从数字0开始
                    List<SuggestionCity> searchSuggestionCitys = poiResult.getSearchSuggestionCitys();
                    if (poiItems != null && poiItems.size() > 0) {
                        //清除POI信息显示
                        whetherToShowDetailInfo(false);
                        //并还原点击marker样式
                        if (mlastMarker != null) {
                            resetlastmarker();
                        }

                        //清理之前搜索结果的marker
                        if (poiOverlay != null) {
                            poiOverlay.removeFromMap();
                        }
                        aMap.clear();
                        poiOverlay = new PoiOverlay(aMap, poiItems);
                        poiOverlay.addToMap();
                        poiOverlay.zoomToSpan();

                        aMap.addMarker(new MarkerOptions()
                                .anchor(0.5f, 0.5f)
                                .icon(BitmapDescriptorFactory
                                        .fromBitmap(BitmapFactory.decodeResource(
                                                getResources(), R.mipmap.gps_point)))
                                .position(new LatLng(lp.getLatitude(), lp.getLongitude())));

                        aMap.addCircle(new CircleOptions()
                                .center(new LatLng(lp.getLatitude(),
                                        lp.getLongitude())).radius(5000)
                                .strokeColor(Color.BLUE)
                                .fillColor(Color.argb(50, 1, 1, 1))
                                .strokeWidth(2));

                    } else if (searchSuggestionCitys != null && searchSuggestionCitys.size() > 0) {
                        showSuggestionCities(searchSuggestionCitys);
                    } else {
                        ToastUtil.show(this, R.string.no_result);
                    }

                } else {
                    ToastUtil.show(this, R.string.no_result);
                }
            } else {
                ToastUtil.show(this, R.string.no_result);
            }


        }
    }

    /**
     * poi没有搜索到数据，返回一些推荐城市的信息
     */
    private void showSuggestionCities(List<SuggestionCity> cities) {
        String infomation = "推荐城市\n";


        for (int i = 0; i < cities.size(); i++) {
            infomation += "城市名称：" + cities.get(i).getCityName()
                    + cities.get(i).getCityCode() + "城市编码：" + cities.get(i).getCityCode() + "\n";
        }

        ToastUtil.show(this, infomation);

    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    private void whetherToShowDetailInfo(boolean isToShow) {
        if (isToShow) {
            mPoiDetail.setVisibility(View.VISIBLE);

        } else {
            mPoiDetail.setVisibility(View.GONE);
        }
    }

    /**
     * 设置当前marker信息
     *
     * @param mCurrentPoi
     */
    private void setPoiItemDisplayContent(final PoiItem mCurrentPoi) {
        mPoiName.setText(mCurrentPoi.getTitle());
        mPoiAddress.setText(mCurrentPoi.getSnippet() + mCurrentPoi.getDistance());
    }

    /**
     * 将之前被点击的marker置为原来的状态
     */
    private void resetlastmarker() {
        int index = poiOverlay.getPoiIndex(mlastMarker);
        mlastMarker.setIcon(BitmapDescriptorFactory
                .fromBitmap(BitmapFactory.decodeResource(
                        getResources(),
                        R.mipmap.point4)));
        mlastMarker = null;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_search:
                doSearchQuery();
                break;
            case R.id.btn_navi:
                AmapNaviPage.getInstance().showRouteActivity(getApplicationContext(),
                        new AmapNaviParams(null, null,
                                new Poi(detailMarker.getTitle(), detailMarker.getPosition(), ""), AmapNaviType.DRIVER), MapActivity.this);
                break;
            default:
                break;
        }

    }

    /**
     * 开始进行poi搜索
     */
    private void doSearchQuery() {
        String keyWord = mSearchText.getText().toString();
        currentPage = 0;
        // 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        query = new PoiSearch.Query(keyWord, "", "");
        query.setPageSize(20);//设置每页最多返回的多少条poiitem
        query.setPageNum(currentPage);//设置查第一页
        if (lp != null) {
            PoiSearch poiSearch = new PoiSearch(this, query);
            poiSearch.setOnPoiSearchListener(this);
            poiSearch.setBound(new PoiSearch.SearchBound(lp, 50000));// 设置搜索区域为以lp点为圆心，其周围5000米范围
            poiSearch.searchPOIAsyn();//异步搜索
        }

    }
}
