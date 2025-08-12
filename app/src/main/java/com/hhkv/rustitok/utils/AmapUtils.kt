package com.hhkv.rustitok.utils
import android.content.Context
import android.graphics.Point
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode
import com.amap.api.location.AMapLocationClientOption.AMapLocationProtocol
import com.amap.api.maps.AMap
import com.amap.api.maps.MapView
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.MyLocationStyle
import com.amap.api.services.core.AMapException
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.geocoder.GeocodeResult
import com.hhkv.rustitok.data.model.PoiData
import com.amap.api.services.poisearch.PoiResult
import com.amap.api.services.poisearch.PoiSearch
import com.hhkv.rustitok.R
import com.hhkv.rustitok.data.model.LocationModel
import com.amap.api.services.geocoder.GeocodeSearch
import com.amap.api.services.geocoder.RegeocodeQuery
import com.amap.api.services.geocoder.RegeocodeResult
import com.amap.api.maps.CameraUpdateFactory
import java.util.Date
import java.util.Timer

@Composable
fun AmapMapView(
    modifier: Modifier,
    onMapReady: (Pair<AMap,MapView>) -> Unit = { },
    onMapClickCallBack: (LatLng?)  -> Unit = {},
    ){
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var amapMap by remember { mutableStateOf<AMap?>(null) }
    AndroidView(factory = { ctx ->
        MapView(ctx).apply {
            mapView = this
            amapMap = this.map
            amapMap?.let { map ->
                AMapUtils.setupMap(map)
                onMapReady(Pair(map,mapView!!))
                map.addOnMapClickListener { latLng ->
                    onMapClickCallBack(latLng)
                }
                map.addOnPOIClickListener { poi ->
                    onMapClickCallBack(poi.coordinate)
                }
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

object AMapUtils {

    const val TAG = "AMapUtils"

    var poiSearch:PoiSearch? = null
    var geoSearch: GeocodeSearch? = null

    /***
     * 初始化地图参数
     */
    fun setupMap(amap: AMap){
        amap.isMyLocationEnabled = false
        amap.mapType = AMap.MAP_TYPE_NORMAL
        amap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE)
        val uiSettings = amap.uiSettings
        uiSettings.isZoomGesturesEnabled = true
        uiSettings.isScrollGesturesEnabled = true
        uiSettings.isZoomControlsEnabled = false
    }


    /**
     * 初始化定位参数配置
     */
    fun getLocationOption(): AMapLocationClientOption {
        val mOption = AMapLocationClientOption()
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        mOption.locationMode = AMapLocationMode.Hight_Accuracy;//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.isGpsFirst = false;//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.httpTimeOut = 30000;//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.interval = 2000000;//可选，设置定位间隔。默认为2秒
        mOption.isNeedAddress = true;//可选，设置是否返回逆地理地址信息。默认是true
        mOption.isOnceLocation = false;//可选，设置是否单次定位。默认是false
        mOption.isOnceLocationLatest = false;//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.isSensorEnable = false;//可选，设置是否使用传感器。默认是false
        mOption.isWifiScan = true; //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.isLocationCacheEnable = false; //可选，设置是否使用缓存定位，默认为true
        mOption.geoLanguage = AMapLocationClientOption.GeoLanguage.DEFAULT;//可选，设置逆地理信息的语言，默认值为默认语言（根据所在地区选择语言）
        return mOption
    }

    /**
     * poi搜索
     */
    fun poiSearch(
        context: Context,
        text:String,
                  city:String,
                  resultsCallback:(List<PoiData>)-> Unit =  {}){
        try {
            // 创建POI搜索查询
            val query = PoiSearch.Query(text, "", city) // 可以修改城市
            query.pageSize = 20
            query.pageNum = 1

            // 创建POI搜索对象
            poiSearch = PoiSearch(context, query)
            poiSearch?.setOnPoiSearchListener(object : PoiSearch.OnPoiSearchListener {
                override fun onPoiSearched(result: PoiResult?, errorCode: Int) {
                    when (errorCode) {
                        AMapException.CODE_AMAP_SUCCESS -> {
                            result?.let { poiResult ->
                                val poiItems = poiResult.pois.map { poi ->
                                        PoiData(
                                            title = poi.title,
                                            lng = poi.latLonPoint.longitude,
                                            lat = poi.latLonPoint.latitude,
                                            city = poi.cityName,
                                            address = poi.adName,
                                            district = poi.adName,
                                            id = null,
                                            createTime = null,
                                            updateTime = null
                                        )
                                }

                                // 更新UI
                                resultsCallback(poiItems)
                            }
                        }
                        else -> {
                            Log.e(TAG, "搜索失败，错误码: $errorCode")
                        }
                    }
                }

                override fun onPoiItemSearched(poiItem: com.amap.api.services.core.PoiItem?, errorCode: Int) {
                    // 单个POI项搜索回调，这里不需要处理
                }
            })
            // 开始搜索
            poiSearch?.searchPOIAsyn()
        } catch (e: Exception) {
            Log.e(TAG, "搜索异常", e)
        }
    }

    /**
     * 移动到当前位置 并添加标记
     */
    fun moveToLocationAndAddMarker(aMap: AMap,locationModel:LocationModel,context: Context,isMyPosition: Boolean = false) {
        clearMarkers(aMap)
        val position = LatLng(locationModel.lat,locationModel.lng)
        // 1. 移动到指定位置
        aMap.animateCamera(
            if(isMyPosition) CameraUpdateFactory.newLatLngZoom(position,18f) else CameraUpdateFactory.newLatLngZoom(position,16f)
        )

        // 2. 添加标记
        val layout = LayoutInflater.from(context)
        val view = layout.inflate(R.layout.marker_point,null)
        val bitmapView = BitmapDescriptorFactory.fromView(view)
        val markerOptions = MarkerOptions()
            .position(position)
            .title(locationModel.title)
            .icon(bitmapView)
            .snippet(locationModel.address).infoWindowEnable(true)
        aMap.addMarker(markerOptions)
    }

    fun clearMarkers(aMap: AMap) {
        aMap.clear()
    }

    /**
     * 逆地理编码
     */
    fun geoAddress(context: Context,
                   latLng: LatLng,
                   geoResultCallBack: (LocationModel) -> Unit
                   ){
        if (geoSearch == null){
            geoSearch = GeocodeSearch(context)
            geoSearch?.setOnGeocodeSearchListener(object : GeocodeSearch.OnGeocodeSearchListener {
                override fun onRegeocodeSearched(
                    p0: RegeocodeResult?,
                    p1: Int
                ) {
                    p0?.let {
                        geoResultCallBack(LocationModel(lat = latLng.latitude, lng = latLng.longitude, title = it.regeocodeAddress.formatAddress, address = it.regeocodeAddress.formatAddress))
                    }
                    Log.d(TAG,"解析到地址: ${p0}")
                }

                override fun onGeocodeSearched(
                    p0: GeocodeResult?,
                    p1: Int
                ) {
                    Log.d(TAG,"逆解析到地址: ${p0}")
                }

            })
        }
        val regeocodeQuery = RegeocodeQuery(LatLonPoint(latLng.latitude,latLng.longitude),200.0f,GeocodeSearch.AMAP)
        geoSearch?.getFromLocationAsyn(regeocodeQuery)
    }
}