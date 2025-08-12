package com.hhkv.rustitok.utils
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

/**
 * Compose传感器监听工具类
 */
object ComposeSensorUtils {
    
    /**
     * 传感器数据状态
     */
    data class SensorData(
        val accelerometer: Triple<Float, Float, Float> = Triple(0f, 0f, 0f),
        val gyroscope: Triple<Float, Float, Float> = Triple(0f, 0f, 0f),
        val magnetometer: Triple<Float, Float, Float> = Triple(0f, 0f, 0f),
        val light: Float = 0f,
        val proximity: Float = 0f,
        val direction: Float = 0f
    )
    
    /**
     * 传感器监听器
     */
    class SensorListener(
        private val onDataChanged: (SensorData) -> Unit
    ) : SensorEventListener {
        
        private var sensorData = SensorData()
        
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                when (it.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        sensorData = sensorData.copy(
                            accelerometer = Triple(it.values[0], it.values[1], it.values[2])
                        )
                    }
                    Sensor.TYPE_GYROSCOPE -> {
                        sensorData = sensorData.copy(
                            gyroscope = Triple(it.values[0], it.values[1], it.values[2])
                        )
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        sensorData = sensorData.copy(
                            magnetometer = Triple(it.values[0], it.values[1], it.values[2])
                        )
                        // 计算方向
                        calculateDirection()
                    }
                    Sensor.TYPE_LIGHT -> {
                        sensorData = sensorData.copy(light = it.values[0])
                    }
                    Sensor.TYPE_PROXIMITY -> {
                        sensorData = sensorData.copy(proximity = it.values[0])
                    }
                }
                onDataChanged(sensorData)
            }
        }
        
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // 处理精度变化
        }
        
        private fun calculateDirection() {
            val accelerometer = sensorData.accelerometer
            val magnetometer = sensorData.magnetometer
            
            val R = FloatArray(9)
            val I = FloatArray(9)
            
            // 计算旋转矩阵
            SensorManager.getRotationMatrix(R, I, 
                floatArrayOf(accelerometer.first, accelerometer.second, accelerometer.third),
                floatArrayOf(magnetometer.first, magnetometer.second, magnetometer.third)
            )
            
            val orientation = FloatArray(3)
            SensorManager.getOrientation(R, orientation)
            
            // 转换为角度
            val direction = Math.toDegrees(orientation[0].toDouble()).toFloat()
            sensorData = sensorData.copy(direction = direction)
        }
    }
}
