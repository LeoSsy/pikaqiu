package com.hhkv.rustitok.utils
import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.viewinterop.AndroidView
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.geocoder.GeocodeResult
import com.amap.api.services.geocoder.GeocodeSearch
import com.amap.api.services.geocoder.RegeocodeQuery
import com.amap.api.services.geocoder.RegeocodeResult
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.location.Poi
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.InfoWindow
import com.baidu.mapapi.map.MapPoi
import com.baidu.mapapi.map.MapStatus
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MapView
import com.baidu.mapapi.map.MarkerOptions
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.geocode.GeoCodeResult
import com.baidu.mapapi.search.sug.SuggestionSearch
import com.baidu.mapapi.search.sug.SuggestionSearchOption
import com.hhkv.rustitok.R
import com.hhkv.rustitok.data.model.LocationModel
import com.hhkv.rustitok.data.model.PoiData
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult
import com.hhkv.rustitok.utils.AMapUtils.geoSearch


@Composable
fun BaiduMapView(
    modifier: Modifier,
    onMapReady:(Pair<BaiduMap,MapView>) -> Unit = {},
    onMapClickCallBack: (LatLng?)  -> Unit = {},
    ){
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var baiduMap by remember { mutableStateOf<BaiduMap?>(null) }
    AndroidView(factory = { ctx ->
        MapView(ctx).apply {
            mapView = this
            baiduMap = this.map
            baiduMap?.let { map ->
                BaiduMapUtils.setupMap(map)
                onMapReady(Pair(map,mapView!!))
                map.setOnMapClickListener(object: BaiduMap.OnMapClickListener{
                    override fun onMapClick(p0: LatLng?) {
                        p0?.let {
                            onMapClickCallBack(p0)
                        }
                    }

                    override fun onMapPoiClick(p0: MapPoi?) {
                        p0?.let {
                            onMapClickCallBack(p0.position)
                        }
                    }
                })
            }
        }
    }, modifier = modifier,
        update = { view ->

        }

    )

    DisposableEffect(Unit) {
        onDispose {
            mapView?.onDestroy()
        }
    }
}

object BaiduMapUtils {
    const val TAG = "BaiduMapUtils"
    val suggestionSearch =  SuggestionSearch.newInstance()
    var geoCoder: GeoCoder? = null

    /***
     * 初始化地图参数
     */
    fun setupMap(baiduMap:BaiduMap){
        baiduMap.isMyLocationEnabled = false
        baiduMap.mapType = BaiduMap.MAP_TYPE_NORMAL
        val uiSettings = baiduMap.uiSettings
        uiSettings.isZoomGesturesEnabled = true
        uiSettings.isScrollGesturesEnabled = true
    }


    /**
     * 初始化定位参数配置
     */
    fun getLocationOption(): LocationClientOption {
        val locationOption = LocationClientOption()
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        locationOption.locationMode = LocationClientOption.LocationMode.Hight_Accuracy
        //可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
        locationOption.setCoorType("bd09ll")

        //可选，默认0，即仅定位一次，设置发起连续定位请求的间隔需要大于等于1000ms才是有效的
        locationOption.setScanSpan(1000)

        //可选，设置是否需要地址信息，默认不需要
        locationOption.setIsNeedAddress(true)

        //可选，设置是否需要设备方向结果
        locationOption.setNeedDeviceDirect(false)

        //可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        locationOption.isLocationNotify = true

        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        locationOption.setIgnoreKillProcess(true)

        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        locationOption.setIsNeedLocationDescribe(false)

        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        locationOption.setIsNeedLocationPoiList(false)

        //可选，默认false，设置是否收集CRASH信息，默认收集
        locationOption.SetIgnoreCacheException(true)


        //可选，默认false，设置是否开启Gps定位
        //locationOption.setOpenGps(true);
        locationOption.isOpenGnss = true

        //可选，默认false，设置定位时是否需要海拔信息，默认不需要，除基础定位版本都可用
        locationOption.setIsNeedAltitude(false)
       return locationOption
    }

    /**
     * poi搜索
     */
    fun poiSearch(text:String,city:String,
                  resultsCallback:(List<PoiData>)-> Unit =  {}){
        if (text.isEmpty() || city.isEmpty()){
            return
        }
        val option = SuggestionSearchOption()
        option.keyword(text).city(city)
        suggestionSearch.setOnGetSuggestionResultListener { res ->
            Log.d(TAG,"搜索建议:$res")
            val results = mutableListOf<PoiData>()
            for (item in res.allSuggestions){
                if (item.pt != null){
                    results.add(PoiData(item.key,item.pt.longitude,item.pt.latitude,item.city,item.address,item.district,null,null,null))
                }
            }
            resultsCallback(results)
        }
        suggestionSearch.requestSuggestion(option)
    }

    /***
     * 移动到指定位置 并添加标记
     */
    fun moveToLocationAndAddMarker(baiduMap:BaiduMap,locationModel:LocationModel,isMyPosition: Boolean = false) {
        clearMarkers(baiduMap)
        val position = LatLng(locationModel.lat,locationModel.lng)
        // 1. 移动到指定位置
        val mapStatus = MapStatus.Builder()
                .target(position)
        if (isMyPosition) {
            mapStatus.zoom(20f)
        }else{
            mapStatus.zoom(16f)
        }
        baiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(mapStatus.build()))
        // 2. 添加标记
        val bitmap =  BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding)
        val markerOptions = MarkerOptions()
                .position(position)
                .title(locationModel.title ?: "当前定位点")
                .icon(bitmap)
        baiduMap.addOverlay(markerOptions)
    }

    fun clearMarkers(baiduMap:BaiduMap) {
        baiduMap.clear()
    }

    /**
     * 逆地理编码
     */
    fun geoAddress(context: Context,
                   latLng: LatLng,
                   geoResultCallBack: (LocationModel) -> Unit
    ){
        if (geoCoder == null) {
            geoCoder = GeoCoder.newInstance()
            geoCoder?.setOnGetGeoCodeResultListener(object :
                com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener {
                override fun onGetGeoCodeResult(p0: GeoCodeResult?) {
                    Log.d(AMapUtils.TAG, "解析到地址: ${p0}")
                }

                override fun onGetReverseGeoCodeResult(p0: ReverseGeoCodeResult?) {
                    Log.d(AMapUtils.TAG, "逆解析到地址: ${p0}")
                    p0?.let {
                        geoResultCallBack(
                            LocationModel(
                                lat = it.location.latitude,
                                lng = it.location.longitude,
                                title = it.sematicDescription,
                                address = it.sematicDescription
                            )
                        )
                    }
                }

            })
        }
        val option = ReverseGeoCodeOption().location(latLng).newVersion(1).radius(500)
        geoCoder?.reverseGeoCode(option)

    }
}