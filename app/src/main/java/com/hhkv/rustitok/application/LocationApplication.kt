package com.hhkv.rustitok.application
import android.app.Application
import android.util.Log
import com.amap.api.location.AMapLocationClient
import com.baidu.location.LocationClient
import com.baidu.mapapi.SDKInitializer
import com.baidu.mapapi.common.BaiduMapSDKException
import com.hhkv.rustitok.config.AppConst.AMAP_CACHE_KEY
import com.hhkv.rustitok.config.AppConst.AMAP_DEFAULT_KEY
import com.hhkv.rustitok.config.AppConst.BAIDU_CACHE_KEY
import com.hhkv.rustitok.config.AppConst.BAIDU_DEFAULT_KEY
import com.hhkv.rustitok.utils.CrashManager
import com.hhkv.rustitok.utils.SharedPrefsManager

class LocationApplication : Application() {
    companion object {
        const val TAG = "LocationApplication"
    }

    lateinit var sharedPrefsManager: SharedPrefsManager
    override fun onCreate() {
        super.onCreate()
        sharedPrefsManager = SharedPrefsManager(this)
        try {
            SDKInitializer.setAgreePrivacy(this, true)
            SDKInitializer.initialize(this)
            LocationClient.setAgreePrivacy(true)
            if (sharedPrefsManager.getString(BAIDU_CACHE_KEY) != null) {
                SDKInitializer.setApiKey(sharedPrefsManager.getString(BAIDU_CACHE_KEY))
            } else {
                SDKInitializer.setApiKey(BAIDU_DEFAULT_KEY)
            }
        } catch (e: BaiduMapSDKException) {
            Log.e(TAG, "百度SDK初始化异常$e")
            e.printStackTrace()
        }
        try {
            AMapLocationClient.updatePrivacyShow(this, true, true)
            AMapLocationClient.updatePrivacyAgree(this, true)
            if (sharedPrefsManager.getString(AMAP_CACHE_KEY) != null) {
                AMapLocationClient.setApiKey(sharedPrefsManager.getString(AMAP_CACHE_KEY))
            } else {
                AMapLocationClient.setApiKey(AMAP_DEFAULT_KEY)
            }
        } catch (e: Exception) {
            Log.e(TAG, "高德SDK初始化异常$e")
            e.printStackTrace()
        }

        try {
            // 崩溃拦截处理
            CrashManager.instance.init(this)
            Log.d(TAG, "崩溃拦截器初始化成功")
        } catch (e: Exception) {
            Log.d(TAG, "崩溃拦截器初始化失败")
        }
    }

}