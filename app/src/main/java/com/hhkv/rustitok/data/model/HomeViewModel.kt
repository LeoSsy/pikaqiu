package com.hhkv.rustitok.data.model
import OverlayDragListener
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.content.Context.WINDOW_SERVICE
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.PixelFormat
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Process
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.DPoint
import com.amap.api.maps.AMap
import com.amap.api.maps.model.LatLng
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.MapStatus
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MyLocationConfiguration
import com.baidu.mapapi.map.MyLocationData
import com.hhkv.rustitok.MainActivity
import com.hhkv.rustitok.R
import com.hhkv.rustitok.compose.CustomSearchBar
import com.hhkv.rustitok.config.AppConst
import com.hhkv.rustitok.config.AppConst.DEFAULT_LAT
import com.hhkv.rustitok.config.AppConst.DEFAULT_LNG
import com.hhkv.rustitok.data.model.LocationModel
import com.hhkv.rustitok.data.model.PoiData
import com.hhkv.rustitok.database.DataBaseHistoryHelper
import com.hhkv.rustitok.database.dao.HistoryDao
import com.hhkv.rustitok.nav.NavigationRoutes
import com.hhkv.rustitok.service.LocationService
import com.hhkv.rustitok.ui.theme.Mock_location_tiotikTheme
import com.hhkv.rustitok.utils.AMapUtils
import com.hhkv.rustitok.utils.AmapMapView
import com.hhkv.rustitok.utils.BaiduMapUtils
import com.hhkv.rustitok.utils.BaiduMapView
import com.hhkv.rustitok.utils.DialogUtils
import com.hhkv.rustitok.utils.MapUtils
import com.hhkv.rustitok.utils.MapUtils.gcj02towgs84
import com.hhkv.rustitok.utils.PermissionUtils
import com.hhkv.rustitok.utils.PermissionUtils.REQUEST_PERMISSIONS
import com.hhkv.rustitok.utils.PermissionUtils.hasPermissions
import com.hhkv.rustitok.utils.SharedPrefsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.amap.api.maps.MapView as AmpMapView
import com.baidu.location.LocationClient as BaiduLocationClient
import com.baidu.mapapi.map.MapView as BaiduMapview
import com.baidu.mapapi.model.LatLng as BaiduLatLng
import kotlinx.serialization.Serializable
import java.util.Timer
import java.util.TimerTask
import java.util.jar.Manifest
import com.amap.api.location.CoordinateConverter as AmapCoordinateConverter

@Serializable
enum class MapType{
    baidu,
    amap
}

class HomeViewModel : ViewModel() {

    companion object {
       const val TAG = "MainActivity"
        const val BIND_AUTO_CREATE = "BIND_AUTO_CREATE_ID"
    }
    // 当前位置信息数据类
     private val _currentLocationModel = MutableStateFlow<LocationModel?>(null)
    val currentLocationModel:StateFlow<LocationModel?> =  _currentLocationModel.asStateFlow()
    fun setLocation(location:LocationModel?) {
        _currentLocationModel.value = location
    }

    // 用户定位位置信息
    var originCurrentBaiduLng = DEFAULT_LNG
    var originCurrentBaiduLat = DEFAULT_LAT
    var originCurrentAmapLng = DEFAULT_LNG
    var originCurrentAmapLat = DEFAULT_LAT
    var originAddress = ""

    // 当前用户选择位置 108.971241,34.225699
     var currentLng = DEFAULT_LNG
     var currentLat = DEFAULT_LAT
     var currentCity = "西安"
    // 当前海拔
     var currentAltitude = 50.0
    // 是否有权限
     var hasPermission  = false
    // 服务绑定对象
    lateinit var serviceBinder: LocationService.LocationServiceBinder
    // 百度地图对象
    var baiduMap : BaiduMap? = null
    // 百度地图视图
    var baiduMapview : BaiduMapview? = null
    //百度定位对象
    var baiduLocationClient: BaiduLocationClient? = null
    // 高德对象
     var amapMap : AMap? = null
    // 高德地图视图
    var ampMapView : AmpMapView? = null
    //高德定位对象
    var amapLocationClient: AMapLocationClient? = null
     var mCurrentDirection = 0.0f
    // 缓存类
    lateinit var sharedPrefsManager: SharedPrefsManager

    // 记录当前使用的地图类型 1 百度 2 高德
    private val _currentUseMapType = MutableStateFlow<MapType>(MapType.baidu)
    val currentUseMapType:StateFlow<MapType> =  _currentUseMapType.asStateFlow()
    fun setMapType(type:MapType) {
        _currentUseMapType.value = type
    }

    // 是否开启服务
    private val _isMockStart = MutableStateFlow<Boolean>(false)
    val isMockStart: StateFlow<Boolean> = _isMockStart.asStateFlow()
    fun setMockStart(isMockStart: Boolean) {
        _isMockStart.value = isMockStart
    }

    // 前台服务
    var serviceIntent: Intent? = null


    // 数据库操作
    var databaseHelper:DataBaseHistoryHelper? = null
    var historyDao: HistoryDao? = null

    // 视图上下文
    var context: Context? = null
    fun setContextValue(context: Context) {
        this.context = context
        databaseHelper = DataBaseHistoryHelper(context)
        historyDao = HistoryDao(databaseHelper!!)
    }

    // 记录是否从其他页面返回当前页面
    var isFromOtherScreen  =  false

    private lateinit var windowParamCurrent: WindowManager.LayoutParams
    private lateinit var windowManager:WindowManager
    private var windowContentview: View? = null
    var timeLabel: TextView? = null

    // 记录是否移动了点击了地图
    var isMovedClickMap = false

    // 服务连接
    val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            p0: ComponentName?,
            p1: IBinder?
        ) {
            serviceBinder = p1 as LocationService.LocationServiceBinder
            Log.d(TAG,"服务连接成功！")
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            Log.d(TAG,"服务取消成功！")
        }
    }

    /**
     * 点击地图移动到指定点
     * */
    fun moveMapToPoint(isMyPosition: Boolean = false){
        currentLocationModel.value?.let {
            if (currentUseMapType.value == MapType.baidu){
                BaiduMapUtils.moveToLocationAndAddMarker(baiduMap!!,it,isMyPosition)
            }else{
                AMapUtils.moveToLocationAndAddMarker(amapMap!!,it,context!!,isMyPosition)
            }
        }
    }

    /**
     * 点击poi搜索结果移动到指定位置
     * */
    fun moveToPosition(data: PoiData,isSave: Boolean = true){
        try {
            var tempLng = data.lng
            var temLat = data.lat
            if (data.mapType == MapType.amap && currentUseMapType.value == MapType.baidu){
                // 高德坐标转换为百度
                val tempPoint = MapUtils.gcj02towgs84(tempLng, temLat)
                val resPoint = MapUtils.wgs2bd09(tempPoint[0],tempPoint[1])
                tempLng = resPoint[0]
                temLat = resPoint[1]
            }else if(data.mapType == MapType.baidu && currentUseMapType.value == MapType.amap){
                // 百度坐标转换为高德坐标
                val convert = AmapCoordinateConverter(context)
                convert.from(AmapCoordinateConverter.CoordType.BAIDU)
                convert.coord(DPoint(temLat,tempLng))
                val resPoint =  convert.convert()
                tempLng = resPoint.longitude
                temLat = resPoint.latitude
            }
            // 设置当前模拟位置为当前位置
            currentLng = tempLng
            currentLat = temLat
            _currentLocationModel.value = LocationModel(lat = currentLat,lng = currentLng, address = data.address, title =  data.title)
            // 设置地图移动到当前位置
            if (isMockStart.value){
                startLocation()
            }else{
                moveMapToPoint()
            }
            // 缓存用户点击的点
            if(isSave){
                historyDao?.let {
                    val rData = data.copy(mapType = currentUseMapType.value)
                    val resultOK = it.insert(rData)
                    if (resultOK > 0){
                        Log.d(TAG,"插入数据成功：$data")
                    }
                }
            }
        }catch (e: Exception){
            Log.d(TAG,"e：${e.toString()}")
            e.printStackTrace()
        }

    }

    /**
     * 开始模拟定位
     * */
    fun startLocation(){
        if (isMockStart.value) {
            if (currentLocationModel.value == null){
                stopLocation()
                Toast.makeText(context,"服务已停止",Toast.LENGTH_SHORT).show()
            }else{
                if (_currentUseMapType.value == MapType.baidu){
                    baiduMap?.clear()
                    this.currentLocationModel.value?.let {
                        val latLng = MapUtils.bd2wgs(it.lng,it.lat)
                        serviceBinder.setPosition(latLng[0],latLng[1],currentAltitude.toDouble())
                        moveMapToPoint()
                    }
                }else{
                    amapMap?.clear()
                    this.currentLocationModel.value?.let {
                        val latLng = MapUtils.gcj02towgs84(it.lng,it.lat)
                        serviceBinder.setPosition(latLng[0],latLng[1],currentAltitude.toDouble())
                        moveMapToPoint()
                    }
                }
                Toast.makeText(context,"已更新到最新位置",Toast.LENGTH_SHORT).show()

            }
        }else{
            val serviceIntent = Intent(context,LocationService::class.java)
            context?.bindService(serviceIntent,serviceConnection, Context.BIND_AUTO_CREATE)
            if (_currentUseMapType.value == MapType.baidu){
                // 百度坐标系统转换
                val latLng = MapUtils.bd2wgs(currentLng,currentLat)
                serviceIntent.putExtra(AppConst.LNG_MSG_ID,latLng[0])
                serviceIntent.putExtra(AppConst.LAT_MSG_ID,latLng[1])
                val altitude = sharedPrefsManager.getString(AppConst.CACHE_ALTITUDE_KEY)
                altitude?.let {
                    serviceIntent.putExtra(AppConst.ALT_MSG_ID, it.toDouble())
                }
            }else{
                // 高德坐标系统转换
                val latLng = MapUtils.gcj02towgs84(currentLng,currentLat)
                serviceIntent.putExtra(AppConst.LNG_MSG_ID,latLng[0])
                serviceIntent.putExtra(AppConst.LAT_MSG_ID,latLng[1])
                val altitude = sharedPrefsManager.getString(AppConst.CACHE_ALTITUDE_KEY)
                altitude?.let {
                    serviceIntent.putExtra(AppConst.ALT_MSG_ID, it.toDouble())
                }
            }
            this.serviceIntent = serviceIntent
            context?.startForegroundService(serviceIntent)
            _isMockStart.value = true
            Toast.makeText(context,"服务开启成功!",Toast.LENGTH_SHORT).show()
            showOverlay()
        }
    }

    /**
     * 结束模拟定位
     * */
    fun stopLocation(){
        try {
            hideOverlay()
            serviceIntent?.let {
                context?.unbindService(serviceConnection)
                context?.stopService(serviceIntent)
            }
            _isMockStart.value = false
            _currentLocationModel.value = null
            baiduLocationClient = null
            amapLocationClient = null
            Toast.makeText(context,"服务已停止!",Toast.LENGTH_LONG).show()
        }catch (e: Exception){
            Log.d(TAG,"停止服务异常： ${e.toString()}")
            e.printStackTrace()
        }

    }

    /**
     * 初始化图默认位置
     * */
    fun initDefaultLocation(){
        try {
                if (_currentUseMapType.value == MapType.baidu){
                    createBaiduLocationClient()
                }else{
                    createAmapLocationClient(this)
                }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    /***
     * 百度定位服务
     */
    fun createBaiduLocationClient(){
        if (baiduLocationClient != null) {
            setLocation(currentLocationModel.value?.copy(lat = originCurrentBaiduLat,lng = originCurrentBaiduLng))
            moveMapToPoint(isMyPosition = true)
            return
        }
        baiduLocationClient =  BaiduLocationClient(context)
        baiduLocationClient?.registerLocationListener(object : BDAbstractLocationListener(){
            override fun onReceiveLocation(location: BDLocation?) {
                Log.d(TAG,"百度定位成功，当前位置为: ${location?.city + location?.address} 经纬度：${"${location?.latitude}" + "${location?.longitude}"}")
                if (location == null || baiduMap == null) {
                    return
                }
                try {
                    originAddress = location.addrStr
                    originCurrentBaiduLng = location.longitude
                    originCurrentBaiduLat = location.latitude
                    currentLat = location.latitude
                    currentLng = location.longitude
                    currentAltitude = location.altitude
                    currentCity = location.city
                }catch (e: Exception){
                    currentCity = "西安"
                    Log.d(TAG,"设置定位数据报错: ${e.toString()}")
                }

                val locationData = MyLocationData.Builder()
                    // 暂时不设置方向 测试
                    .direction(mCurrentDirection)
                    .accuracy(location.radius)
                    .latitude(currentLat)
                    .longitude(currentLng).build()
                baiduMap?.setMyLocationData(locationData)
                baiduMap?.setMyLocationConfiguration(MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL,true,null))
                // 判断是否定位成功
                val err = location.locType
                if (err == BDLocation.TypeCriteriaException || err == BDLocation.TypeNetWorkException){
                    // 定位失败重新请求一次
                    baiduLocationClient?.requestLocation()
                }else{
                    _currentLocationModel.value = LocationModel(location.latitude,location.longitude,location.addrStr,location.addrStr)
                    val statusBuilder = MapStatus.Builder().target(BaiduLatLng(location.latitude,location.longitude)).zoom(15.0.toFloat()).build()
                    baiduMap?.animateMapStatus(MapStatusUpdateFactory.newMapStatus(statusBuilder))
                    moveMapToPoint(true)
                }
            }

            override fun onLocDiagnosticMessage(p0: Int, p1: Int, p2: String?) {
                Log.d(TAG,"定位出错: $p0 - $p1 - $p2")
                super.onLocDiagnosticMessage(p0, p1, p2)
            }
        })
        val locationOption = BaiduMapUtils.getLocationOption()
        baiduLocationClient?.locOption = locationOption
        baiduLocationClient?.start()
    }

    /***

     * 高德定位服务
     */
    fun createAmapLocationClient(vm: HomeViewModel){
        if(amapLocationClient != null) {
            setLocation(currentLocationModel.value?.copy(lat = originCurrentAmapLat,lng = originCurrentAmapLng))
            moveMapToPoint(isMyPosition = true)
            return
        }
        amapLocationClient =  AMapLocationClient(context)
        amapLocationClient?.setLocationOption(AMapUtils.getLocationOption())
        amapLocationClient?.setLocationListener { p0 ->
            p0?.let {
                if (it.errorCode == 0) {
                    amapLocationClient?.stopLocation()
                    val latLng = LatLng(it.latitude, it.longitude)
                    Log.d(
                        TAG,
                        "高德定位成功，当前位置为: ${it.address} - ${it.latitude} - ${it.longitude}"
                    )
                    try {
                        originAddress = it.address
                        originCurrentAmapLng = it.longitude
                        originCurrentAmapLat = it.latitude
                        currentLat = it.latitude
                        currentLng = it.longitude
                        currentAltitude = it.altitude
                        currentCity = it.city
                    }catch (e: Exception){
                        currentCity = "西安"
                        Log.d(TAG,"设置定位数据报错: ${e.toString()}")
                    }

                    _currentLocationModel.value = LocationModel(it.latitude, it.longitude, it.address,it.address)
                    moveMapToPoint(true)
                } else {
                    Log.d(TAG, "高德定位失败，err: ${it.errorInfo}")
                    amapLocationClient?.stopLocation()
                    amapLocationClient?.startLocation()
                }
            }
        }
        amapLocationClient?.startLocation()
    }


    /***
     * 新增悬浮窗内容
     */
    fun showOverlay(){
        context?.let {
            windowManager = it.getSystemService(WINDOW_SERVICE) as WindowManager
            windowParamCurrent =  WindowManager.LayoutParams()
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

            windowParamCurrent.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            windowParamCurrent.format = PixelFormat.RGBA_8888;
            windowParamCurrent.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            windowParamCurrent.gravity = Gravity.START + Gravity.TOP;
            windowParamCurrent.width = WindowManager.LayoutParams.WRAP_CONTENT;
            windowParamCurrent.height = WindowManager.LayoutParams.WRAP_CONTENT;

            windowContentview = View.inflate(it,R.layout.overlay_layout,null)
            val windowW = windowManager.defaultDisplay.width
            val windowH = windowManager.defaultDisplay.height
            windowParamCurrent.x = windowW
            windowParamCurrent.y = windowH
            windowContentview?.let { view ->
                timeLabel =  view.findViewById<TextView>(R.id.timeLabel)
                windowManager.addView(windowContentview,windowParamCurrent)
                // 添加脱拽事件
                val listener = OverlayDragListener(
                    it,
                    windowManager,
                    windowParamCurrent
                )
                view.setOnTouchListener(listener)
            }
        }

    }

    /***
     * 隐藏悬浮窗内容
     */
    fun hideOverlay(){
        if (windowContentview != null){
            windowManager.removeViewImmediate(windowContentview!!)
        }
    }
}