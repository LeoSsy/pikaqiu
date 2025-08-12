package com.hhkv.rustitok.service
import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.location.LocationRequest
import android.location.provider.ProviderProperties
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Message
import android.os.Process
import android.os.SystemClock
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.hhkv.rustitok.MainActivity
import com.hhkv.rustitok.R
import com.hhkv.rustitok.config.AppConst
import com.hhkv.rustitok.config.AppConst.DEFAULT_LAT
import com.hhkv.rustitok.config.AppConst.DEFAULT_LNG
import com.hhkv.rustitok.utils.PermissionUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random


class LocationService : Service() {

    companion object {
        const val TAG = "LocationService"
        const val SERVICE_NOTE_CHANNEL_ID = "LocationService_ID_666"
        const val SERVICE_NOTE_CHANNEL_NAME = "LocationService"
        const val SERVICE_NOTE_ID:Long = 99231
    }
    // 是否停止服务
    private var isStop = false
    // 位置管理
    private lateinit var locationManager:LocationManager
    // 当前经纬度
    private var currentLng = 34.179083
    private var currentLat = 108.959329
    private var currentAlt = 55.0
    // 方向角度
    private var currentAngle = 0.0
    // 速度
    private var currentSpeed = 2.5
    // 是否创建过通知渠道
    private var isCreateNotification = false
    private var mockLocationJob: Job? = null

    override fun onBind(p0: Intent?): IBinder {
        return  LocationServiceBinder()
    }

    override fun onDestroy() {
        super.onDestroy()
        isStop = true
        removeTestProviderNetwork()
        removeTestProviderGPS()
        stopForeground(STOP_FOREGROUND_REMOVE)
        mockLocationJob?.cancel()
        mockLocationJob = null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            val lat = it.getDoubleExtra(AppConst.LAT_MSG_ID,DEFAULT_LAT)
            val lng = it.getDoubleExtra(AppConst.LNG_MSG_ID,DEFAULT_LNG)
            val alt = it.getDoubleExtra(AppConst.ALT_MSG_ID,55.0)
            currentLat = lat
            currentLng = lng
            currentAlt = alt
        }
        initNotification()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        removeTestProviderNetwork()
        addTestProviderNetwork()
        removeTestProviderGPS()
        addTestProviderGPS()
        initLocation()
        initNotification()
    }



    private fun initLocation(){

        mockLocationJob = CoroutineScope(Dispatchers.Main).launch {
            try {
                while (!isStop) {
                    setLocationNetwork()
                    setLocationGPS()
                    delay(100)
                }
            }catch (e: Exception){
                Log.e(TAG, "ERROR - handleMessage")
            }
        }
    }


    private fun removeTestProviderGPS() {
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, false)
                locationManager.removeTestProvider(LocationManager.GPS_PROVIDER)
            }
        } catch (e: Exception) {
            Log.d(TAG,"SERVICEGO: ERROR - removeTestProviderGPS")
        }
    }

    // 注意下面临时添加 @SuppressLint("wrongconstant") 以处理 addTestProvider 参数值的 lint 错误
    @SuppressLint("wrongconstant")
    private fun addTestProviderGPS() {
        try {
            // 注意，由于 android api 问题，下面的参数会提示错误(以下参数是通过相关API获取的真实GPS参数，不是随便写的)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                locationManager.addTestProvider(
                    LocationManager.GPS_PROVIDER,
                    false,
                    true,
                    false,
                    false,
                    true,
                    true,
                    true,
                    ProviderProperties.POWER_USAGE_HIGH,
                    ProviderProperties.ACCURACY_FINE
                )
            } else {
                locationManager.addTestProvider(
                    LocationManager.GPS_PROVIDER, false, true, false,
                    false, true, true, true, Criteria.POWER_HIGH, Criteria.ACCURACY_FINE
                )
            }
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true)
            }
        } catch (e: Exception) {
            Log.e(TAG,"SERVICEGO: ERROR - addTestProviderGPS")
        }
    }

    private fun setLocationGPS() {
        try {
            // 尽可能模拟真实的 GPS 数据
            val loc = Location(LocationManager.GPS_PROVIDER)
            loc.accuracy = 3f // 设定此位置的估计水平精度，以米为单位。
            loc.altitude = currentAlt // 设置高度，在 WGS 84 参考坐标系中的米
            loc.bearing = currentAngle.toFloat() // 方向（度）
            loc.latitude = currentLat // 纬度（度）
            loc.longitude = currentLng // 经度（度）
            loc.bearing = 1f
            loc.altitude = 50.0 // 模拟海拔
            loc.time = System.currentTimeMillis() // 本地时间
            loc.speed = currentSpeed.toFloat()
            loc.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
            loc.bearingAccuracyDegrees = 0.1f
            loc.verticalAccuracyMeters = 0.1f
            loc.speedAccuracyMetersPerSecond = 0.01f
            val bundle = Bundle()
            bundle.putInt("satellites", 7)
            loc.extras = bundle
            loc.latitude += (Random.nextDouble() - 0.5) * 0.0001
            loc.longitude += (Random.nextDouble() - 0.5) * 0.0001
            locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, loc)

        } catch (e: Exception) {
            Log.d(TAG,"SERVICEGO: ERROR - setLocationGPS")
        }
    }

    private fun removeTestProviderNetwork() {
        try {
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.setTestProviderEnabled(LocationManager.NETWORK_PROVIDER, false)
                locationManager.removeTestProvider(LocationManager.NETWORK_PROVIDER)
            }
        } catch (e: Exception) {
            Log.d(TAG,"SERVICEGO: ERROR - removeTestProviderNetwork")
        }
    }

    // 注意下面临时添加 @SuppressLint("wrongconstant") 以处理 addTestProvider 参数值的 lint 错误
    @SuppressLint("wrongconstant")
    private fun addTestProviderNetwork() {
        try {
            // 注意，由于 android api 问题，下面的参数会提示错误(以下参数是通过相关API获取的真实NETWORK参数，不是随便写的)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                locationManager.addTestProvider(
                    LocationManager.NETWORK_PROVIDER, true, false,
                    true, true, true, true,
                    true, ProviderProperties.POWER_USAGE_LOW, ProviderProperties.ACCURACY_COARSE
                )
            } else {
                locationManager.addTestProvider(
                    LocationManager.NETWORK_PROVIDER, true, false,
                    true, true, true, true,
                    true, Criteria.POWER_LOW, Criteria.ACCURACY_COARSE
                )
            }
            if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.setTestProviderEnabled(LocationManager.NETWORK_PROVIDER, true)
            }
        } catch (e: SecurityException) {
            Log.d(TAG,"ERROR - addTestProviderNetwork")
        }
    }

    private fun setLocationNetwork() {
        try {
            // 尽可能模拟真实的 NETWORK 数据
            val loc = Location(LocationManager.NETWORK_PROVIDER)
            loc.accuracy = 3f // 设定此位置的估计水平精度，以米为单位。
            loc.altitude = currentAlt // 设置高度，在 WGS 84 参考坐标系中的米
            loc.bearing = 1f // 方向（度）
            loc.latitude = currentLat // 纬度（度）
            loc.longitude = currentLng // 经度（度）
            loc.altitude = 50.0 // 模拟海拔
            loc.time = System.currentTimeMillis() // 本地时间
            loc.speed = currentSpeed.toFloat()
            loc.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
            loc.bearingAccuracyDegrees = 0.1f
            loc.verticalAccuracyMeters = 0.1f
            loc.speedAccuracyMetersPerSecond = 0.01f
            loc.latitude += (Random.nextDouble() - 0.5) * 0.0001
            loc.longitude += (Random.nextDouble() - 0.5) * 0.0001
            locationManager.setTestProviderLocation(LocationManager.NETWORK_PROVIDER, loc)
        } catch (e: Exception) {
            Log.d(TAG,"ERROR - setLocationNetwork")
        }
    }

    private fun initNotification() {

        if (!PermissionUtils.hasPermission(this, Manifest.permission.POST_NOTIFICATIONS)){
            return
        }

        if (isCreateNotification) return

        //准备intent
        val clickIntent = Intent(
            this,
            MainActivity::class.java
        )
        val clickPI = PendingIntent.getActivity(this, 0, clickIntent, PendingIntent.FLAG_MUTABLE)

        try {
            // channel
            val mChannel = NotificationChannel(SERVICE_NOTE_CHANNEL_ID,SERVICE_NOTE_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)

            val notification: Notification = NotificationCompat.Builder(
                this,
                SERVICE_NOTE_CHANNEL_ID
            )
                .setChannelId(SERVICE_NOTE_CHANNEL_ID)
                .setContentTitle(resources.getString(R.string.app_name))
                .setContentText(resources.getString(R.string.app_service_tips))
                .setContentIntent(clickPI)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
            startForeground(SERVICE_NOTE_ID.toInt(), notification)
            startForeground(SERVICE_NOTE_ID.toInt(), notification)
            isCreateNotification  = true
        }catch (e:Exception){
            Log.d(TAG,"创建前台服务失败！")
        }

    }


    inner class LocationServiceBinder : Binder() {
        fun setPosition(lng: Double, lat: Double, alt: Double) {
            currentLng = lng
            currentLat = lat
            currentAlt = alt
        }
    }
}