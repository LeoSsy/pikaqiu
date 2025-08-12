package com.hhkv.rustitok.data.model
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer

@Serializable
data class PoiData(val title: String?, val lng:Double, val lat:Double,
                   val city:String?, val address:String?, val district:String?,
                   val id: Long?, val createTime:String?,val updateTime:String?,
                    val mapType: MapType = MapType.baidu
)
