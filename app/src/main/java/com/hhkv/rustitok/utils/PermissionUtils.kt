package com.hhkv.rustitok.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.util.Log
import androidx.core.content.ContextCompat

object PermissionUtils {

    const val TAG = "PermissionUtils"

    val BASIC_PERMISSIONS = listOf(
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.ACCESS_WIFI_STATE,
    )

//    val STORAGE_PERMISSIONS = if(VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
//        listOf(
//            Manifest.permission.READ_MEDIA_IMAGES,
//            Manifest.permission.READ_MEDIA_VIDEO,
//            Manifest.permission.READ_MEDIA_AUDIO)
//    }else{
//        listOf(
//            Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE)
//    }

    val LOCATION_PERMISSIONS = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION)

//    val CAMERA_PERMISSIONS = listOf(
//        Manifest.permission.CAMERA
//    )

    val NOTIFICATION = if(VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
        listOf( Manifest.permission.POST_NOTIFICATIONS)
    }else{
        listOf()
    }

    val REQUEST_PERMISSIONS = BASIC_PERMISSIONS  + LOCATION_PERMISSIONS + NOTIFICATION

    val PERMISSIONS_INFOS = mapOf<String,String>(
        Manifest.permission.INTERNET to "网络权限",
        Manifest.permission.ACCESS_NETWORK_STATE to "网络权限",
        Manifest.permission.ACCESS_WIFI_STATE to "网络权限",
        if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU){
            Manifest.permission.READ_MEDIA_IMAGES to "存储权限"
            Manifest.permission.READ_MEDIA_VIDEO to  "存储权限"
            Manifest.permission.READ_MEDIA_AUDIO to "存储权限"
        }else{
            Manifest.permission.READ_EXTERNAL_STORAGE to "存储权限"
            Manifest.permission.WRITE_EXTERNAL_STORAGE to  "存储权限"
        },
        Manifest.permission.ACCESS_FINE_LOCATION to  "位置权限",
        Manifest.permission.ACCESS_COARSE_LOCATION to  "位置权限",
        Manifest.permission.CAMERA to  "相机权限",
        if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
            Manifest.permission.POST_NOTIFICATIONS to  "通知权限"
        } else {
            Manifest.permission.CAMERA to  "相机权限"
        }
    )

    fun hasPermission(context:Context, permission:String) : Boolean {
      return  ContextCompat.checkSelfPermission(context,permission) == PackageManager.PERMISSION_GRANTED
    }

    fun hasPermissions(context:Context,permissions:List<String>) : Boolean {
        return permissions.all { hasPermission(context,it) }
    }

    fun getDeniedPermissions(context: Context,permissions:List<String>) : List<String> {
        val deniedPermissions = listOf<String>()
        for (it in permissions){
            if (deniedPermission(context,it) ){
                deniedPermissions.plus(it)
            }
        }
        return deniedPermissions
    }

    fun deniedPermission(context:Context, permission:String) : Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_DENIED
    }

    /***
     * wifi 是否可用
     */
    fun isWifiAvailable(context: Context):Boolean{
        val mgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = mgr.activeNetwork ?: return false
        val activeNetwork = mgr.getNetworkCapabilities(network)
        return  activeNetwork != null && activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    fun isWifiEnabled(context: Context) : Boolean{
        val mgr = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return mgr.isWifiEnabled
    }

    /***
     * 手机自带网络 是否可用
     */
    fun isMobileAvailable(context: Context):Boolean{
        val mgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = mgr.activeNetwork ?: return false
        val activeNetwork = mgr.getNetworkCapabilities(network)
        return  activeNetwork != null && activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    /***
     * 网络是否连接
     */
    fun isNetworkConnected(context: Context):Boolean{
        val mgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = mgr.activeNetwork ?: return false
        val activeNetwork = mgr.getNetworkCapabilities(network)
        return  activeNetwork != null && (activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
    }

    fun checkNetworkAvailable(context: Context):Boolean{
        return (isWifiAvailable(context) || isMobileAvailable(context)) && isNetworkConnected(context)
    }

    fun isGpsOpened(context: Context):Boolean{
        val mgr = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return mgr.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    /***
     * 模拟定位权限是否可用
     */
    fun isAllowMockLocation(context: Context) :Boolean {
        var allowMockLocation = false
        try {
            val mgr = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val providers = mgr.allProviders
            var index = 0
            for ((i,j) in providers.withIndex()){
                if (providers[i].equals(LocationManager.GPS_PROVIDER)){
                    break
                }
                index ++
            }
            if (index < providers.size){
                // 注意，由于 android api 问题，下面的参数会提示错误(以下参数是通过相关API获取的真实GPS参数，不是随便写的)
                if (VERSION.SDK_INT >= VERSION_CODES.S) {
                    mgr.addTestProvider(
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
                    mgr.addTestProvider(
                        LocationManager.GPS_PROVIDER, false, true, false,
                        false, true, true, true, Criteria.POWER_HIGH, Criteria.ACCURACY_FINE
                    )
                }
                allowMockLocation = true
            }
            if (allowMockLocation){
                mgr.setTestProviderEnabled(LocationManager.GPS_PROVIDER,false)
                mgr.removeTestProvider(LocationManager.GPS_PROVIDER)
            }
        }catch (e:Exception){
            Log.d(TAG,"isAllowMockLocation error:")
            e.printStackTrace()
        }
        return allowMockLocation
    }

}