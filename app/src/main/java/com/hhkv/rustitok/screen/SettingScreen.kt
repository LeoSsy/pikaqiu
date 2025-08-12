package com.hhkv.rustitok.screen
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baidu.mapapi.SDKInitializer
import com.hhkv.rustitok.application.LocationApplication
import com.hhkv.rustitok.config.AppConst.BAIDU_CACHE_KEY
import com.hhkv.rustitok.config.AppConst.BAIDU_DEFAULT_KEY
import com.hhkv.rustitok.data.model.PoiData
import com.hhkv.rustitok.utils.LocalNavController
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import com.hhkv.rustitok.config.AppConst.AMAP_CACHE_KEY
import com.hhkv.rustitok.config.AppConst.AMAP_DEFAULT_KEY
import com.hhkv.rustitok.utils.DialogUtils
import kotlin.system.exitProcess
import android.os.Process
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.platform.LocalFocusManager
import com.hhkv.rustitok.R
import com.hhkv.rustitok.utils.CrashManager.Companion.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen() {
    val nav = LocalNavController.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var baiduKeyVal by remember { mutableStateOf<String?>(null) }
    var amapKeyVal by remember { mutableStateOf<String?>(null) }
    val application =  context.applicationContext as LocationApplication
    if (application.sharedPrefsManager.getString(BAIDU_CACHE_KEY) != null) {
        baiduKeyVal = application.sharedPrefsManager.getString(BAIDU_CACHE_KEY)
    } else {
        baiduKeyVal = BAIDU_DEFAULT_KEY
    }

    if (application.sharedPrefsManager.getString(AMAP_CACHE_KEY) != null) {
        amapKeyVal = application.sharedPrefsManager.getString(AMAP_CACHE_KEY)
    } else {
        amapKeyVal = AMAP_DEFAULT_KEY
    }

    val textFieldStateBaidu by remember { mutableStateOf<TextFieldState>(TextFieldState()) }
    val textFieldStateAmap by remember { mutableStateOf<TextFieldState>(TextFieldState()) }
    var showDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Scaffold (
        modifier = Modifier.Companion
            .fillMaxSize()
            .padding(0.dp),
        topBar = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(48.dp)
                    .width(800.dp)
                    .background(MaterialTheme.colorScheme.primary),
            ) {
                IconButton(
                    onClick = {
                        nav.popBackStack()
                    }
                ) {
                    Icon(
                        modifier = Modifier.size(36.dp),
                        tint = Color.White,
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = ""

                    )
                }
                Spacer(modifier = Modifier.Companion.size(12.dp))
                Text("地图key设置", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1.0f))
                IconButton(
                    onClick = {

                        if (textFieldStateBaidu.text.toString().isEmpty()){
                            Toast.makeText(context,"请输入百度Key", Toast.LENGTH_SHORT).show()
                            return@IconButton
                        }

                        if (textFieldStateAmap.text.toString().isEmpty()){
                            Toast.makeText(context,"请输入高德Key", Toast.LENGTH_SHORT).show()
                            return@IconButton
                        }

                        if (textFieldStateBaidu.text.toString().trim().length != 32){
                            Toast.makeText(context,"请输入合法的百度Key", Toast.LENGTH_SHORT).show()
                            return@IconButton
                        }

                        if (textFieldStateAmap.text.toString().trim().length != 32){
                            Toast.makeText(context,"请输入合法的高德Key", Toast.LENGTH_SHORT).show()
                            return@IconButton
                        }

                        if (textFieldStateBaidu.text.toString().isNotEmpty() &&
                            textFieldStateBaidu.text.toString() == baiduKeyVal &&
                            textFieldStateAmap.text.toString().isNotEmpty() &&
                            textFieldStateAmap.text.toString() == amapKeyVal
                            ){
                            Toast.makeText(context,"你输入的Key与当前使用的相同", Toast.LENGTH_SHORT).show()
                            return@IconButton
                        }

                        application.sharedPrefsManager.setString(BAIDU_CACHE_KEY,textFieldStateBaidu.text.toString())
                        application.sharedPrefsManager.setString(AMAP_CACHE_KEY,textFieldStateAmap.text.toString())
                        showDialog = true
                    }
                ) {
                    Text("保存", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
        }
    }){ innerPadding ->
        Column(modifier = Modifier
            .padding(innerPadding)
            .padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("百度:")
                Spacer(modifier = Modifier.width(12.dp))
                TextField(
                    modifier = Modifier.weight(1.0f),
                    value = textFieldStateBaidu.text.toString(),
                    placeholder = {Text("$baiduKeyVal") },
                    maxLines = 2,
                    onValueChange = {
                        textFieldStateBaidu.edit {
                            replace(0, length, it)
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row (verticalAlignment = Alignment.CenterVertically){
                Text("高德:")
                Spacer(modifier = Modifier.width(12.dp))
                TextField(
                    modifier = Modifier.weight(1.0f),
                    value = textFieldStateAmap.text.toString(),
                    placeholder = {Text("$amapKeyVal") },
                    maxLines = 2,
                    onValueChange = {
                        textFieldStateAmap.edit {
                            replace(0, length, it)
                        }
                    }
                )
            }

            if(showDialog) DialogUtils.ShowDialog(
                title = "保存成功",
                hint =  "地图Key设置后需要重启App才能生效,是否重启?",
                confirmButtonClick =  {
                    showDialog = false
                    focusManager.clearFocus()
                    val windowManager =  context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                    val params = WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT
                    )
                    val loadingView =  View.inflate(context, R.layout.restart_loading,null)
                    windowManager.addView(loadingView,params)
                    scope.launch {
                        delay(2000L)
                        withContext(Dispatchers.Main) {
                            windowManager.removeView(loadingView)
                        }
                        throw  Exception().initCause(Throwable("重启"))
                    }
                }
            )
        }
    }
}