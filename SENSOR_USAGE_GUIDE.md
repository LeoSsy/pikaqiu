# Kotlin Compose 中实现 SensorEventListener 指南

## 概述

在 Kotlin Compose 中实现 SensorEventListener 需要遵循 Compose 的生命周期管理原则，确保传感器监听器在组件销毁时正确注销。

## 基本实现方法

### 1. 使用 DisposableEffect 管理生命周期

```kotlin
@Composable
fun SensorExample() {
    val context = LocalContext.current
    val sensorManager = remember { 
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager 
    }
    
    var sensorData by remember { mutableStateOf(0f) }
    
    val sensorEventListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    sensorData = it.values[0]
                }
            }
            
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // 处理精度变化
            }
        }
    }
    
    DisposableEffect(sensorManager) {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensor?.let {
            sensorManager.registerListener(sensorEventListener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        
        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }
    
    Text("传感器数据: $sensorData")
}
```

### 2. 使用预定义的 Hook（推荐）

我们提供了简化的 Hook 函数：

```kotlin
@Composable
fun MyScreen() {
    // 获取加速度传感器数据
    val accelerometerData = useAccelerometer()
    
    // 获取陀螺仪数据
    val gyroscopeData = useGyroscope()
    
    // 获取光线传感器数据
    val lightData = useLightSensor()
    
    // 获取距离传感器数据
    val proximityData = useProximitySensor()
    
    Column {
        Text("加速度: ${accelerometerData.first}, ${accelerometerData.second}, ${accelerometerData.third}")
        Text("陀螺仪: ${gyroscopeData.first}, ${gyroscopeData.second}, ${gyroscopeData.third}")
        Text("光照: $lightData")
        Text("距离: $proximityData")
    }
}
```

### 3. 自定义传感器监听器

```kotlin
@Composable
fun CustomSensorExample() {
    val sensorManager = SensorUtils.getSensorManager()
    var shakeCount by remember { mutableStateOf(0) }
    
    val sensorEventListener = SensorUtils.createSensorListener(
        onAccelerometerChanged = { x, y, z ->
            val acceleration = kotlin.math.sqrt(x * x + y * y + z * z)
            if (acceleration > 15f) {
                shakeCount++
            }
        }
    )
    
    DisposableEffect(sensorManager) {
        SensorUtils.registerSensors(
            sensorManager,
            sensorEventListener,
            Sensor.TYPE_ACCELEROMETER
        )
        
        onDispose {
            SensorUtils.unregisterSensors(sensorManager, sensorEventListener)
        }
    }
    
    Text("摇晃次数: $shakeCount")
}
```

## 常用传感器类型

- `Sensor.TYPE_ACCELEROMETER` - 加速度传感器
- `Sensor.TYPE_GYROSCOPE` - 陀螺仪传感器
- `Sensor.TYPE_MAGNETIC_FIELD` - 磁力计传感器
- `Sensor.TYPE_LIGHT` - 光线传感器
- `Sensor.TYPE_PROXIMITY` - 距离传感器
- `Sensor.TYPE_GRAVITY` - 重力传感器
- `Sensor.TYPE_LINEAR_ACCELERATION` - 线性加速度传感器

## 传感器更新频率

- `SensorManager.SENSOR_DELAY_NORMAL` - 正常频率
- `SensorManager.SENSOR_DELAY_GAME` - 游戏频率（更快）
- `SensorManager.SENSOR_DELAY_UI` - UI 频率
- `SensorManager.SENSOR_DELAY_FASTEST` - 最快频率

## 性能优化建议

1. **使用 derivedStateOf 计算派生状态**：
```kotlin
val isDeviceMoving by remember {
    derivedStateOf {
        val (x, y, z) = accelerometerData
        kotlin.math.sqrt(x * x + y * y + z * z) > 10f
    }
}
```

2. **避免在 onSensorChanged 中进行复杂计算**：
```kotlin
// 好的做法
val sensorEventListener = remember {
    object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                // 只更新原始数据
                rawData = it.values.toList()
            }
        }
    }
}

// 在 Composable 中计算派生数据
val processedData by remember {
    derivedStateOf {
        // 在这里进行复杂计算
        rawData.map { it * 2 }
    }
}
```

3. **合理设置更新频率**：
```kotlin
// 对于实时性要求高的应用
SensorManager.SENSOR_DELAY_GAME

// 对于一般应用
SensorManager.SENSOR_DELAY_NORMAL
```

## 错误处理

```kotlin
@Composable
fun SafeSensorExample() {
    val sensorManager = SensorUtils.getSensorManager()
    var isSensorAvailable by remember { mutableStateOf(false) }
    
    DisposableEffect(sensorManager) {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        isSensorAvailable = sensor != null
        
        sensor?.let {
            sensorManager.registerListener(sensorEventListener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        
        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }
    
    if (isSensorAvailable) {
        Text("传感器可用")
    } else {
        Text("传感器不可用")
    }
}
```

## 在现有项目中使用

要在你的 `HistoryScreen` 中添加传感器功能，可以参考 `HistoryScreenWithSensor` 函数的实现。

## 注意事项

1. **权限**：某些传感器可能需要特定权限
2. **电池消耗**：传感器会消耗电池，使用完毕后要注销监听器
3. **设备兼容性**：不是所有设备都有相同的传感器
4. **数据精度**：传感器数据可能有噪声，需要进行滤波处理

## 完整示例

查看 `SensorScreen.kt` 和 `SensorExampleScreen.kt` 文件获取完整的实现示例。 