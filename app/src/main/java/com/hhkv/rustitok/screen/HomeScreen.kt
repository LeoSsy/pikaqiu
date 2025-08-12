package com.hhkv.rustitok.screen
import android.Manifest
import android.app.Activity
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hhkv.rustitok.compose.CustomSearchBar
import com.hhkv.rustitok.config.AppConst
import com.hhkv.rustitok.data.model.HomeViewModel
import com.hhkv.rustitok.data.model.LocationModel
import com.hhkv.rustitok.data.model.PoiData
import com.hhkv.rustitok.nav.NavigationRoutes
import com.hhkv.rustitok.ui.theme.Mock_location_tiotikTheme
import com.hhkv.rustitok.utils.AMapUtils
import com.hhkv.rustitok.utils.AmapMapView
import com.hhkv.rustitok.utils.BaiduMapUtils
import com.hhkv.rustitok.utils.BaiduMapView
import com.hhkv.rustitok.utils.DialogUtils
import com.hhkv.rustitok.utils.PermissionUtils
import com.hhkv.rustitok.utils.SharedPrefsManager
import com.hhkv.rustitok.data.model.MapType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.hhkv.rustitok.compose.PointInfoWindow
import com.hhkv.rustitok.compose.TimeInfoBar
import com.hhkv.rustitok.utils.ComposeSensorUtils
import com.hhkv.rustitok.utils.LocalNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.Timer
import java.util.TimerTask

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen() {
        val context = LocalContext.current
        val vm: HomeViewModel = viewModel()
        // 无悬浮窗权限弹框
        var showFloatWindowPermission by remember { mutableStateOf(false) }
        // 无模拟定位权限弹框
        var showMockLocationPermission by remember { mutableStateOf(false) }
        // 通知权限弹框
        var showMockNotificationPermission by remember { mutableStateOf(false) }
        // 传感器数据监听
        var sensorData by remember { mutableStateOf(ComposeSensorUtils.SensorData()) }
        var sensorManager by remember { mutableStateOf<SensorManager?>(null) }
        var sensorListener by remember { mutableStateOf<ComposeSensorUtils.SensorListener?>(null) }
        val sensorTypes: List<Int> = listOf(Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_MAGNETIC_FIELD)
        val delay: Int = SensorManager.SENSOR_DELAY_UI
        val navigationController = LocalNavController.current
        val focusMgr = LocalFocusManager.current
        val permissionsState = rememberMultiplePermissionsState(
             if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                 listOf(
                     Manifest.permission.ACCESS_FINE_LOCATION,
                     Manifest.permission.ACCESS_COARSE_LOCATION,
                     Manifest.permission.POST_NOTIFICATIONS,
                     )
             }else{
                 listOf(
                     Manifest.permission.ACCESS_FINE_LOCATION,
                     Manifest.permission.ACCESS_COARSE_LOCATION
                 )
             }

        )

        // 核心：监听系统返回事件（包括侧滑返回）
        BackHandler(enabled = true) {
            // 这里可以添加返回确认逻辑
            focusMgr.clearFocus()
            Log.d(HomeViewModel.TAG,"✅ 当前页面被侧滑返回了")
        }
        // 监听权限改变
        LaunchedEffect(permissionsState.allPermissionsGranted) {
            if (permissionsState.allPermissionsGranted) {
                vm.initDefaultLocation()
            }
        }

    // 监听生命周期
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, e ->
                when (e) {
                    Lifecycle.Event.ON_CREATE -> {
                        val reStart = (context as Activity).intent.getBooleanExtra("FROM_CRASH",false)
                        if (reStart){
                            context.intent.putExtra("FROM_CRASH",false)
                            Toast.makeText(context,"重启成功", Toast.LENGTH_SHORT).show()
                        }
                        vm.setContextValue(context)
                        vm.sharedPrefsManager = SharedPrefsManager(context)
                        // 监听传感器数据
                        sensorListener = ComposeSensorUtils.SensorListener { data ->
                            sensorData = data
                            vm.mCurrentDirection = data.direction
                        }
                        val cacheMapType = vm.sharedPrefsManager.getString(AppConst.USE_MAPVIEW_TYPE_KEY)
                        if (cacheMapType != null){
                            vm.setMapType(if (cacheMapType.toInt() == 1) MapType.baidu else MapType.amap)
                        }
                    }
                    Lifecycle.Event.ON_RESUME -> {
                        sensorManager?.let { manager ->
                            sensorListener?.let { listener ->
                                sensorTypes.forEach { sensorType ->
                                    val sensor = manager.getDefaultSensor(sensorType)
                                    sensor?.let {
                                        manager.registerListener(listener, it, delay)
                                    }
                                }
                            }
                        }
                        if (vm.currentUseMapType.value == MapType.baidu){
                            vm.baiduMapview?.onResume()
                        }else{
                            vm.ampMapView?.onResume()
                        }
                    }

                    Lifecycle.Event.ON_PAUSE -> {
                        sensorListener?.let { listener ->
                            sensorManager?.unregisterListener(listener)
                        }
                        if (vm.currentUseMapType.value == MapType.baidu){
                            vm.baiduMapview?.onPause()
                        }else{
                            vm.ampMapView?.onPause()
                        }
                    }

                    Lifecycle.Event.ON_DESTROY -> {
                        vm.stopLocation()
                        sensorListener?.let { listener ->
                            sensorManager?.unregisterListener(listener)
                        }
                        if (vm.currentUseMapType.value == MapType.baidu){
                            vm.baiduMap?.clear()
                            vm. baiduMapview?.onDestroy()
                        }else{
                            vm.amapMap?.clear()
                            vm.ampMapView?.onDestroy()
                        }
                    }

                    else -> {}
                }
            }

            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        Mock_location_tiotikTheme {
            Scaffold(modifier = Modifier
                .fillMaxSize()
                .padding(0.dp),
                topBar = {
                    Text("", modifier = Modifier
                        .padding(0.dp)
                        .size(0.dp))
                }
            ) { innerPadding ->
                val currentUseMapType by vm.currentUseMapType.collectAsStateWithLifecycle()
                var mockState by rememberSaveable { mutableStateOf<Boolean>(false) }
                var expanded by rememberSaveable { mutableStateOf(false) }
                var selectedItem by rememberSaveable { mutableStateOf(if(currentUseMapType == MapType.baidu)"百度" else "高德") }
                val items = listOf("百度", "高德")
                var searchResults by remember { mutableStateOf<List<PoiData>>(emptyList()) }
                val currentLocationModel by vm.currentLocationModel.collectAsStateWithLifecycle()
                val isMockStart by vm.isMockStart.collectAsStateWithLifecycle()
                var time by rememberSaveable { mutableStateOf(0) }
                val updateTime by rememberUpdatedState(time)
                var timeStr by rememberSaveable { mutableStateOf("") }
                var timer:Timer? = null
                if(isMockStart) DisposableEffect(Unit) {
                    timer = Timer().apply {
                        schedule(object : TimerTask(){
                            override fun run() {
                                time = updateTime + 1
                                val hours = time / 3600
                                val minutes = (time % 3600) / 60
                                val secs = time % 60
                                timeStr = "%02d:%02d:%02d".format(hours, minutes, secs)
                                CoroutineScope(Dispatchers.Main).launch {
                                    vm.timeLabel?.let {
                                        vm.timeLabel?.text = timeStr
                                    }
                                }
                            }
                        },1000,1000)
                    }

                    onDispose {
                        timer.cancel()
                    }
                }
                LaunchedEffect(UInt) {
                    // 记录返回当前也后是否
                    navigationController.currentBackStackEntry?.savedStateHandle?.getStateFlow<String?>("poi",null)?.collect { poi ->
                        vm.isFromOtherScreen = true
                        if(poi != null){
                            poi.let {
                                try {
                                    val poiData = Json.decodeFromString<PoiData>(it.toString())
                                    // 设置当前模拟位置为当前位置
                                    vm.moveToPosition(data = poiData, isSave = false)
                                    navigationController.currentBackStackEntry?.savedStateHandle?.remove<String>("poi")
                                }catch (e: Exception){
                                    Log.d("TAG","e-->${e.toString()}")
                                }

                            }
                        }else{
                            vm.moveMapToPoint(isMyPosition = true)
                        }
                    }
                    navigationController.currentBackStackEntry?.savedStateHandle?.getStateFlow<Boolean>("back",false)?.collect { v->
                        v.let {
                            if(v){
                                vm.moveMapToPoint(isMyPosition = true)
                            }
                            navigationController.currentBackStackEntry?.savedStateHandle?.remove<Boolean>("back")
                        }
                    }
                }
                // 地图
                Box(modifier = Modifier.padding(innerPadding)){
                    if(currentUseMapType == MapType.baidu)
                        BaiduMapView(
                        modifier = Modifier.padding(innerPadding),
                        onMapReady = { (map,mapView) ->
                            vm.baiduMap = map
                            vm.baiduMapview = mapView
                            Log.d(HomeViewModel.TAG,"地图初始化完成")
                            permissionsState.launchMultiplePermissionRequest()
                            vm.initDefaultLocation()
                        },
                            onMapClickCallBack = { latLng ->
                                vm.isMovedClickMap = true
                                focusMgr.clearFocus()
                                latLng?.let {
                                    vm.currentLng = latLng.longitude
                                    vm.currentLat = latLng.latitude
                                    vm.setLocation(LocationModel(lat = vm.currentLat, lng = vm.currentLng, title = "", address = "" ))
                                    vm.moveMapToPoint()
                                    BaiduMapUtils.geoAddress(context,latLng,{ it ->
                                        Log.d(HomeViewModel.TAG,"$it")
                                        vm.setLocation(it)
                                    })
                                    if(vm.isMockStart.value) vm.startLocation()
                                }
                            }
                    ) else AmapMapView(
                        modifier = Modifier.padding(innerPadding),
                        onMapReady = { (map,mapView) ->
                            vm.amapMap = map
                            vm.ampMapView = mapView
                            val bundle = (context as? Activity)?.intent?.extras ?: Bundle()
                            mapView.onCreate(bundle)
                            Log.d(HomeViewModel.TAG,"地图初始化完成")
                            permissionsState.launchMultiplePermissionRequest()
                            vm.initDefaultLocation()
                        },
                        onMapClickCallBack = {latLng ->
                            focusMgr.clearFocus()
                            vm.isMovedClickMap = true
                            latLng?.let {
                                vm.currentLng = latLng.longitude
                                vm.currentLat = latLng.latitude
                                vm.setLocation(LocationModel(lat = vm.currentLat, lng = vm.currentLng, title = "", address = ""))
                                vm.moveMapToPoint()
                                AMapUtils.geoAddress(context,latLng,{ it ->
                                    Log.d(HomeViewModel.TAG,"$it")
                                    vm.setLocation(it)
                                })
                                if(vm.isMockStart.value) vm.startLocation()
                            }
                        }
                    )


                    // 底部按钮
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                bottom = 78.dp,
                                start = 10.dp,
                                end = 10.dp
                            )
                            .align(Alignment.BottomCenter),
                        contentAlignment = Alignment.BottomCenter
                    ){

                        Column{
                            if(currentLocationModel != null) PointInfoWindow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        bottom = 0.dp,
                                        start = 0.dp,
                                        end = 0.dp
                                    )
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                                currentLocationModel!!
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween) {
                                    val scale by animateFloatAsState(
                                        targetValue = if (mockState) 1.25f else 1f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        ),
                                        label = "scale"
                                    )
                                    Spacer(modifier = Modifier.padding(12.dp))
                                    Button(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .scale(scale)
                                            .align(Alignment.CenterVertically)
                                            .background(
                                                MaterialTheme.colorScheme.primary,
                                                CircleShape
                                            ),
                                        onClick = {
                                            if (isMockStart){
                                                mockState = false
                                                vm.setMockStart(false)
                                                vm.stopLocation()
                                                time = 0
                                                timer?.cancel()
                                            }else{
                                                // 检查权限
                                                if (PermissionUtils.isWifiEnabled(context)){
                                                    Toast.makeText(context,"WIFI开启可能导致闪退不稳定的情况，请关闭WIFI",Toast.LENGTH_LONG).show()
                                                    return@Button
                                                }

                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                                                    if (!PermissionUtils.hasPermission(context, Manifest.permission.POST_NOTIFICATIONS)){
                                                        showMockNotificationPermission = true
                                                        return@Button
                                                    }
                                                }

                                                if (!PermissionUtils.isAllowMockLocation(context)){
                                                    showMockLocationPermission = true
                                                    return@Button
                                                }
                                                if (!Settings.canDrawOverlays(context)){
                                                    showFloatWindowPermission = true
                                                    return@Button
                                                }
                                                vm.startLocation()
                                                vm.setMockStart(true)
                                                mockState = true
                                            }
                                        }) {
                                        Text(text = if (isMockStart) "停止" else "开启",
                                            modifier = Modifier.align(
                                            Alignment.CenterVertically))
                                    }
                                Column(
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Spacer(modifier = Modifier.weight(1.0f))
                                        Box(
                                            modifier = Modifier
                                                .width(120.dp)
                                                .height(35.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Button(
                                                onClick = { expanded = true },
                                            ) {
                                                Text(selectedItem)
                                                Icon(
                                                    imageVector = Icons.Default.ArrowDropDown,
                                                    contentDescription = "下拉箭头"
                                                )
                                            }

                                            DropdownMenu(
                                                expanded = expanded,
                                                onDismissRequest = { expanded = false },
                                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                            ) {
                                                items.forEach { item ->
                                                    DropdownMenuItem(
                                                        text = { Text(item) },
                                                        onClick = {
                                                            if (item == "百度" && vm.currentUseMapType.value == MapType.baidu) return@DropdownMenuItem
                                                            if(isMockStart) vm.stopLocation()
                                                            selectedItem = item
                                                            expanded = false
                                                            mockState = false
                                                            vm.setMapType(if (item == "百度") MapType.baidu else MapType.amap)
                                                            vm.sharedPrefsManager.setString(
                                                                AppConst.USE_MAPVIEW_TYPE_KEY,
                                                                if (vm.currentUseMapType.value == MapType.baidu) "1" else "2"
                                                            )
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                        Button(
                                            shape = CircleShape,
                                            modifier = Modifier.height(35.dp),
                                            onClick = {
                                                if (vm.currentUseMapType.value == MapType.baidu){
                                                    vm.setLocation(LocationModel(lng = vm.originCurrentBaiduLng, lat = vm.originCurrentBaiduLat, title = vm.originAddress, address = vm.originAddress ))
                                                }else{
                                                    vm.setLocation(LocationModel(lng = vm.originCurrentAmapLng, lat = vm.originCurrentAmapLat, title = vm.originAddress, address = vm.originAddress ))
                                                }
                                                vm.moveMapToPoint(true)
                                                if (isMockStart) vm.startLocation()
                                            },
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = "当前定位",
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Button(
                                        modifier = Modifier
                                            .height(35.dp)
                                            .width(178.dp),
                                        onClick = {
                                            // 跳转历史
                                            navigationController.navigate(NavigationRoutes.HISTORY) {
                                                Log.d(HomeViewModel.TAG, "to history")
                                            }
                                        }) {
                                        Text(text = "历史记录")
                                    }
                                }
                            }
                        }

                        // 搜索框
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 8.dp)) {
                            CustomSearchBar(
                                isMockStart = mockState,
                                searchResults = searchResults,
                                clearData = {
                                    searchResults = emptyList()
                                },
                                onResultClick = { data ->
                                    searchResults = listOf()
                                    // 设置当前模拟位置为当前位置
                                    vm.moveToPosition(data = data)
                                },
                                onSearch = { text ->
                                    Log.d(HomeViewModel.TAG, "搜索内容:$text")
                                    // 执行poi搜索
                                    if (currentUseMapType == MapType.baidu) {
                                        BaiduMapUtils.poiSearch(
                                            text,
                                            vm.currentCity,
                                            resultsCallback = { data ->
                                                searchResults = data
                                            }
                                        )
                                    } else {
                                        AMapUtils.poiSearch(
                                            context,
                                            text,
                                            vm.currentCity,
                                            resultsCallback = { data ->
                                                searchResults = data
                                            })
                                    }
                                },
                            )

                        }



                        /// 显示无通知权限弹框
                        if (showMockNotificationPermission) {
                            DialogUtils.ShowEnableNotificationDialog(context,confirmButtonClick = {
                                showMockNotificationPermission = false
                            },
                                dismissButtonClick = {
                                    showMockNotificationPermission = false
                                })
                        }
                        /// 显示无模拟位置权限弹框
                        if (showMockLocationPermission) {
                            DialogUtils.ShowEnableMockLocationDialog(context,confirmButtonClick = {
                                showMockLocationPermission = false
                            },
                                dismissButtonClick = {
                                    showMockLocationPermission = false
                                })
                        }
                        /// 显示无模拟位置权限弹框
                        if(showFloatWindowPermission){
                            DialogUtils.ShowEnableFloatWindowDialog(context,
                                confirmButtonClick = {
                                    showFloatWindowPermission = false
                                },
                                dismissButtonClick = {
                                    showFloatWindowPermission = false
                                })
                        }
                    }

                     Box(modifier = Modifier.padding(top = 70.dp, start = 12.dp , end = 12.dp)) {
                        TimeInfoBar(showTimeInfo = isMockStart, timeStr)
                    }
                }
            }
    }
}

