package com.hhkv.rustitok.compose
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hhkv.rustitok.data.model.HomeViewModel
import com.hhkv.rustitok.data.model.LocationModel
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import com.hhkv.rustitok.utils.ClipboardUtil


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointInfoWindow(modifier: Modifier, location: LocationModel){
        val context = LocalContext.current
    if (location.title != null)Column (
            modifier = modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ){
            Text(
                location.title,
                modifier = Modifier.padding(0.dp).align(Alignment.Start),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = Color.White,
                )
            Spacer(modifier = Modifier.height(5.dp))
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =  Arrangement.SpaceBetween
            ){
                Text("经度：${location.lng}",color = Color.White, maxLines = 1, fontSize = 13.sp)
                Button(
                    modifier = Modifier.height(24.dp),
                    contentPadding = PaddingValues(0.dp),
                    onClick = {
                        ClipboardUtil.copyText(context,"${location.lng}")
                        Toast.makeText(context,"内容已复制到剪切板", Toast.LENGTH_SHORT).show()
                }) {
                    Text("复制", fontSize = 10.sp)
                }
            }
            Spacer(modifier = Modifier.height(5.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement =  Arrangement.SpaceBetween
            ) {
                Text("纬度：${location.lat}",color = Color.White, maxLines = 1, fontSize = 13.sp)
                Button(
                    modifier = Modifier.height(24.dp),
                    contentPadding = PaddingValues(0.dp),
                    onClick = {
                        ClipboardUtil.copyText(context,"${location.lat}")
                        Toast.makeText(context,"内容已复制到剪切板", Toast.LENGTH_SHORT).show()
                }) {
                    Text("复制", fontSize = 10.sp, modifier = Modifier.padding(0.dp))
                }
            }
        }

}