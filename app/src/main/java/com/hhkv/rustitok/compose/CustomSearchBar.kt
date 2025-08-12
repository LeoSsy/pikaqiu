package com.hhkv.rustitok.compose
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hhkv.rustitok.data.model.PoiData
import com.hhkv.rustitok.nav.NavigationRoutes
import com.hhkv.rustitok.utils.LocalNavController
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomSearchBar(
    isMockStart: Boolean,
    onSearch: (String) -> Unit,
    searchResults: List<PoiData>,
    onResultClick: (PoiData) -> Unit,
    clearData : () -> Unit,
    placeholder: @Composable () -> Unit = { Text("搜索目的地") },
    leadingIcon: @Composable (() -> Unit)? = { Icon(Icons.Default.Search, contentDescription = "搜索目的地") },
) {
    // 输入状态管理
    var expanded by rememberSaveable { mutableStateOf(false) }
    var textFieldState by remember { mutableStateOf<TextFieldState>(TextFieldState()) }
    var searchQuery by remember { mutableStateOf("") }
    var debouncedQuery by remember { mutableStateOf("") }
    var isDeleting by remember { mutableStateOf(false) }

    // 焦点 键盘管理者
    val focusMgr = LocalFocusManager.current
    val keyboardMgr = LocalSoftwareKeyboardController.current
    val navigationController = LocalNavController.current

    // 检测是否在删除
    LaunchedEffect(searchQuery) {
        isDeleting = searchQuery.length < debouncedQuery.length
    }

    // 防抖处理
    LaunchedEffect(searchQuery) {
        if (!isDeleting) { // 只有在非删除状态时才启动防抖
            delay(300)
            debouncedQuery = searchQuery
        }
    }

    // 执行搜索
    LaunchedEffect(debouncedQuery) {
        if (debouncedQuery.isNotEmpty() && !isDeleting) {
            expanded = true
            onSearch(debouncedQuery)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row {
                TextField(
                    modifier = Modifier
                        .weight(1.0f)
                        .background(
                            color = Color.White,
                            shape = CircleShape,
                        ),
                    value = textFieldState.text.toString(),
                    shape = CircleShape,
                    placeholder = placeholder,
                    leadingIcon = leadingIcon,
                    maxLines = 1,
                    colors = TextFieldDefaults.colors().copy(
                        cursorColor = Color.Black,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    trailingIcon = {
                        Button(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            onClick = {
                                focusMgr.clearFocus()
                                keyboardMgr?.hide()
                                onSearch(textFieldState.text.toString())
                            }) {
                            Text("搜索")
                        }
                    },
                    onValueChange = {
                        textFieldState.edit {
                            replace(0, length, it)
                        }
                        searchQuery = textFieldState.text.toString()
                        if (searchQuery.isEmpty()) {
                            clearData()
                        }
                        Log.d("CustomSearchBar", "search:${textFieldState.text.toString()}")
                    }
                )
                Spacer(modifier = Modifier.width(0.dp))
                IconButton(
                    modifier = Modifier.padding(top = 5.dp, start = 5.dp)
                        .background(MaterialTheme.colorScheme.primary,CircleShape),
                    onClick = {
                        navigationController.navigate(NavigationRoutes.SETTINGS)
                    }) {
                    Icon(Icons.Default.Settings,
                        modifier = Modifier
                            .width(18.dp)
                            .height(18.dp),
                        contentDescription = "设置",
                        tint = Color.White
                    )
                }
        }
            if (expanded)  LazyColumn(modifier = Modifier.padding(top = if(isMockStart)34.dp else 6.dp)) {
                items(count = searchResults.size) { index ->
                    val poi = searchResults[index]
                    Column(modifier = Modifier
                        .background(Color.White)
                        .padding(top = 12.dp, start = 12.dp, end = 12.dp)
                        .clickable {
                            focusMgr.clearFocus()
                            keyboardMgr?.hide()
                            textFieldState.clearText()
                            onResultClick(searchResults[index])
                            expanded = false
                        }) {
                        Text(
                            text =  poi.title.toString(),
                            modifier = Modifier.padding(0.dp),
                            color = Color.Black,
                            fontSize = 15.sp,
                            maxLines = 1,
                        )
                        Text(poi.address.toString(), color = Color.LightGray, maxLines = 2, fontSize = 13.sp)
                        Row {
                            Text("经度：${poi.lng}",color = Color.LightGray, maxLines = 1, fontSize = 13.sp)
                            Spacer(modifier = Modifier.size(12.dp))
                            Text("纬度：${poi.lat}",color = Color.LightGray, maxLines = 1, fontSize = 13.sp)
                        }
                        HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.05f))
                    }
                }
            }
        }
}