package com.hhkv.rustitok.screen

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.hhkv.rustitok.data.model.HistoryViewModel
import com.hhkv.rustitok.database.DataBaseHistoryHelper
import com.hhkv.rustitok.database.dao.HistoryDao
import com.hhkv.rustitok.ui.theme.Mock_location_tiotikTheme
// 基础 Compose 导入
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hhkv.rustitok.screen.ui.theme.Purple40
import com.hhkv.rustitok.utils.LocalNavController
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.pointer.pointerInput
import androidx.savedstate.serialization.saved
import com.hhkv.rustitok.data.model.HomeViewModel
import com.hhkv.rustitok.data.model.MapType
import com.hhkv.rustitok.data.model.PoiData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.lang.Exception


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(){
    var viewModel: HistoryViewModel = viewModel()
    val databaseHelper = DataBaseHistoryHelper(LocalContext.current)
    val historyDao = HistoryDao(databaseHelper)
    val nav = LocalNavController.current
    val TAG = "HistoryScreen"
    val context = LocalContext.current
    // 核心：监听系统返回事件（包括侧滑返回）
    BackHandler(enabled = true) {
        Log.d(TAG,"✅ 当前页面被侧滑返回了")
        nav.previousBackStackEntry?.savedStateHandle?.set<Boolean>(
            "back",
            true
        )
        nav.popBackStack()
    }
    Mock_location_tiotikTheme {
        val historys by viewModel.historys.collectAsStateWithLifecycle()
        var isSelect by remember { mutableStateOf(false) }
        var selItems by remember { mutableStateOf(setOf<PoiData>()) }
        Scaffold(
            modifier = Modifier.Companion.fillMaxSize().padding(0.dp),
            topBar = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(48.dp).width(800.dp).background(MaterialTheme.colorScheme.primary),
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
                    Text("历史记录", color = Color.White)
                    Spacer(modifier = Modifier.weight(1.0f))
                    if(isSelect) IconButton(
                        onClick = {
                            // 删除
                            viewModel.deleteHistory(selItems = selItems.toList(), dao = historyDao)
                            for (item in selItems){
                                historys - item
                            }
                            selItems = setOf<PoiData>()
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                modifier = Modifier.size(15.dp),
                                tint = Color.White,
                                imageVector = Icons.Default.Delete,
                                contentDescription = ""
                            )
                            Text("共${selItems.size}项", color = Color.White, fontSize = 8.sp)
                        }
                    }
                }
            }
        ) { innerPadding ->
            val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
            val error by viewModel.error.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                viewModel.loadHistory(historyDao)
            }

            Column(modifier = Modifier.padding(innerPadding)) {
                Text("长按进入选择删除,点击退出选择模式", modifier = Modifier.padding(6.dp),
                    fontSize = 14.sp,
                    color = Color.LightGray.copy(alpha = 0.7f)
                    )
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.Companion.fillMaxSize(),
                            contentAlignment = Alignment.Companion.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    error != null -> {
                        Column {
                            Text(
                                "$error",
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.Companion.height(12.dp))
                            Button(
                                onClick = {
                                    viewModel.loadHistory(historyDao)
                                }
                            ) {
                                Text("重试")
                            }
                        }
                    }

                    historys.isEmpty() -> {
                        Box(
                            modifier = Modifier.Companion.fillMaxSize(),
                            contentAlignment = Alignment.Companion.Center
                        ) {
                            Text("暂无数据")
                        }
                    }

                    else -> {

                        LazyColumn(modifier = Modifier.Companion.padding(0.dp),
                            ) {
                            items(count = historys.size) { index ->
                                val poi = historys[index]
                                Box {
                                    Column(
                                        modifier = Modifier.Companion.background(
                                            if(selItems.contains(poi)) Color.Companion.Blue.copy(alpha = 0.2f) else Color.Companion.White,
                                        )
                                            .padding(top = 12.dp, start = 12.dp, end = 12.dp)
                                            .pointerInput(poi) {
                                                detectTapGestures(
                                                    onTap = {
                                                        if (isSelect){
                                                            selItems = if (selItems.contains(poi)){
                                                                selItems - poi
                                                            }else{
                                                                selItems + poi
                                                            }
                                                            if (selItems.isEmpty()){
                                                                isSelect = false
                                                            }
                                                        }else{
                                                            // 传递参数给上一页
                                                            try {
                                                                val jsonString = Json.encodeToString(poi)
                                                                nav.previousBackStackEntry?.savedStateHandle?.set<String>(
                                                                    "poi",
                                                                    jsonString
                                                                )
                                                                nav.popBackStack()
                                                            }catch (e: Exception){
                                                                Log.d(TAG,"e--${e.toString()}")
                                                                e.printStackTrace()
                                                            }

                                                        }
                                                    },
                                                    onLongPress = {
                                                        if (!isSelect){
                                                            isSelect = true
                                                            selItems = setOf(poi)
                                                            Toast.makeText(context,"请选择要删除的项",Toast.LENGTH_LONG).show()
                                                        }
                                                    }

                                                )
                                            }
                                    ) {
                                        Text(
                                            text = poi.title.toString(),
                                            modifier = Modifier.Companion.padding(0.dp),
                                            color = Color.Companion.Black,
                                            fontSize = 15.sp,
                                            maxLines = 1,
                                        )
                                        Text(
                                            poi.address.toString(),
                                            color = Color.Companion.LightGray,
                                            maxLines = 2,
                                            fontSize = 13.sp
                                        )
                                        Row {
                                            Text(
                                                "经度：${poi.lng}",
                                                color = Color.Companion.LightGray,
                                                maxLines = 1,
                                                fontSize = 13.sp
                                            )
                                            Spacer(modifier = Modifier.Companion.size(12.dp))
                                            Text(
                                                "纬度：${poi.lat}",
                                                color = Color.Companion.LightGray,
                                                maxLines = 1,
                                                fontSize = 13.sp
                                            )
                                            Spacer(modifier = Modifier.Companion.size(12.dp))
                                            Text(
                                                "来自：${if(poi.mapType == MapType.baidu) "百度地图" else "高德地图"}",
                                                color = Color.Companion.LightGray,
                                                maxLines = 1,
                                                fontSize = 13.sp
                                            )
                                        }
                                        HorizontalDivider(color = Color.Companion.LightGray.copy(alpha = 0.3f))
                                    }

                                    if(isSelect) Box (
                                        contentAlignment = Alignment.CenterEnd
                                    ){
                                        Row {
                                            Spacer(modifier = Modifier.weight(1.0f))
                                            Checkbox(
                                                checked = if(selItems.contains(poi)) true else false,
                                                onCheckedChange = {

                                                },
                                                enabled = false,)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}