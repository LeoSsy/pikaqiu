package com.hhkv.rustitok.utils

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object MapUtils {
    //    public final static String COORDINATE_TYPE_GCJ02 = "gcj02";
    //    public final static String COORDINATE_TYPE_BD09LL = "bd09ll";
    //    public final static String COORDINATE_TYPE_BD09MC = "bd09";
    //    public static float[] EARTH_WEIGHT = {0.1f, 0.2f, 0.4f, 0.6f, 0.8f}; // 推算计算权重_地球
    //    public static float[] MOON_WEIGHT = {0.0167f,0.033f,0.067f,0.1f,0.133f};
    //    public static float[] MARS_WEIGHT = {0.034f,0.068f,0.152f,0.228f,0.304f};
    //坐标转换相关
    const val pi: Double = 3.14159265358979324
    const val a: Double = 6378245.0
    const val ee: Double = 0.00669342162296594323
    const val x_pi: Double = 3.14159265358979324 * 3000.0 / 180.0

    fun bd2wgs(lon: Double, lat: Double): DoubleArray {
        val bd2Gcj = bd09togcj02(lon, lat)
        return gcj02towgs84(bd2Gcj[0], bd2Gcj[1])
    }

    /**
     * WGS84 转换为 BD-09
     * @param lng   经度
     * @param lat   纬度
     * @return double[] 转换后的经度，纬度 数组
     */
    fun wgs2bd09(lng: Double, lat: Double): DoubleArray {
        //第一次转换
        var dlat = transformLat(lng - 105.0, lat - 35.0)
        var dlng = transformLon(lng - 105.0, lat - 35.0)
        val radlat = lat / 180.0 * pi
        var magic = sin(radlat)
        magic = 1 - ee * magic * magic
        val sqrtmagic = sqrt(magic)
        dlat = (dlat * 180.0) / ((a * (1 - ee)) / (magic * sqrtmagic) * pi)
        dlng = (dlng * 180.0) / (a / sqrtmagic * cos(radlat) * pi)
        val mglat = lat + dlat
        val mglng = lng + dlng

        //第二次转换
        val z = sqrt(mglng * mglng + mglat * mglat) + 0.00002 * sin(mglat * x_pi)
        val theta = atan2(mglat, mglng) + 0.000003 * cos(mglng * x_pi)
        val bd_lng = z * cos(theta) + 0.0065
        val bd_lat = z * sin(theta) + 0.006
        return doubleArrayOf(bd_lng, bd_lat)
    }

    fun bd09togcj02(bd_lon: Double, bd_lat: Double): DoubleArray {
        val x = bd_lon - 0.0065
        val y = bd_lat - 0.006
        val z = sqrt(x * x + y * y) - 0.00002 * sin(y * x_pi)
        val theta = atan2(y, x) - 0.000003 * cos(x * x_pi)
        val gg_lng = z * cos(theta)
        val gg_lat = z * sin(theta)
        return doubleArrayOf(gg_lng, gg_lat)
    }

    fun gcj02towgs84(lng: Double, lat: Double): DoubleArray {
        var dlat = transformLat(lng - 105.0, lat - 35.0)
        var dlng = transformLon(lng - 105.0, lat - 35.0)
        val radlat = lat / 180.0 * pi
        var magic = sin(radlat)
        magic = 1 - ee * magic * magic
        val sqrtmagic = sqrt(magic)
        dlat = (dlat * 180.0) / ((a * (1 - ee)) / (magic * sqrtmagic) * pi)
        dlng = (dlng * 180.0) / (a / sqrtmagic * cos(radlat) * pi)
        val mglat = lat + dlat
        val mglng = lng + dlng
        return doubleArrayOf(lng * 2 - mglng, lat * 2 - mglat)
    }

    private fun transformLat(lat: Double, lon: Double): Double {
        var ret = -100.0 + 2.0 * lat + 3.0 * lon + 0.2 * lon * lon + 0.1 * lat * lon + 0.2 * sqrt(
            abs(lat)
        )
        ret += (20.0 * sin(6.0 * lat * pi) + 20.0 * sin(2.0 * lat * pi)) * 2.0 / 3.0
        ret += (20.0 * sin(lon * pi) + 40.0 * sin(lon / 3.0 * pi)) * 2.0 / 3.0
        ret += (160.0 * sin(lon / 12.0 * pi) + 320 * sin(lon * pi / 30.0)) * 2.0 / 3.0
        return ret
    }

    private fun transformLon(lat: Double, lon: Double): Double {
        var ret = 300.0 + lat + 2.0 * lon + 0.1 * lat * lat + 0.1 * lat * lon + 0.1 * sqrt(abs(lat))
        ret += (20.0 * sin(6.0 * lat * pi) + 20.0 * sin(2.0 * lat * pi)) * 2.0 / 3.0
        ret += (20.0 * sin(lat * pi) + 40.0 * sin(lat / 3.0 * pi)) * 2.0 / 3.0
        ret += (150.0 * sin(lat / 12.0 * pi) + 300.0 * sin(lat / 30.0 * pi)) * 2.0 / 3.0
        return ret
    } //    private static boolean out_of_china(double lng, double lat) {
    //        return (lng < 72.004 || lng > 137.8347) || ((lat < 0.8293 || lat > 55.8271));
    //    }
}