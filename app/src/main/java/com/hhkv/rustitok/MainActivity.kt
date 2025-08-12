package com.hhkv.rustitok
import android.Manifest
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.AlertDialog
import android.app.ComponentCaller
import android.content.ComponentName
import android.content.DialogInterface
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import com.hhkv.rustitok.config.AppConst
import com.hhkv.rustitok.ui.theme.Mock_location_tiotikTheme
import com.hhkv.rustitok.nav.AppNavigation
import com.hhkv.rustitok.utils.PermissionUtils

class MainActivity : ComponentActivity() {

    companion object {
        const val TAG = "MainActivity"
    }

    fun checkLocationPermission(){
        val requestPermission = ActivityResultContracts.RequestMultiplePermissions()
        val requestPermissionLauncher = registerForActivityResult(requestPermission) {
            permissions ->
            permissions.entries.forEach { (permission,isGranted) ->
                Log.d(TAG,"$permission--$isGranted")
                when {
                    isGranted -> {

                    }
                    else -> {
                        val alert =   AlertDialog.Builder(this).setTitle(PermissionUtils.PERMISSIONS_INFOS[permission] + "被拒绝").setMessage("请到应用设置权限管理中开启此权限")
                        .setNegativeButton("取消",object : DialogInterface.OnClickListener {
                            override fun onClick(p0: DialogInterface?, p1: Int) {
                            }
                        })
                        .setPositiveButton("设置",object : DialogInterface.OnClickListener {
                            override fun onClick(p0: DialogInterface?, p1: Int) {
                                val intent = Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    ("package:$packageName").toUri()
                                )
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                            }
                        })
                       alert.show()
                    }
                }
            }
        }
        if(VERSION.SDK_INT >= VERSION_CODES.TIRAMISU){
            requestPermissionLauncher.launch(arrayOf(ACCESS_FINE_LOCATION,ACCESS_COARSE_LOCATION,POST_NOTIFICATIONS))
        }else{
            requestPermissionLauncher.launch(arrayOf(ACCESS_FINE_LOCATION,ACCESS_COARSE_LOCATION))
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window,false)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.statusBarColor = Color.Transparent.toArgb()
//        window.run {
//            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
//            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//            statusBarColor = Color.Transparent.toArgb()
//
//            WindowCompat.getInsetsController(this, decorView).apply {
//                isAppearanceLightStatusBars = true
//            }
//        }
//        checkLocationPermission()
        setContent(){
            Mock_location_tiotikTheme {
                Scaffold (modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        AppNavigation()
                    }
                }
            }
        }
    }

}

