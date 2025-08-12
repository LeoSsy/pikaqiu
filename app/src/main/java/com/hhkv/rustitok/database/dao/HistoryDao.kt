package com.hhkv.rustitok.database.dao

import android.content.ContentValues
import com.hhkv.rustitok.data.model.MapType
import com.hhkv.rustitok.data.model.PoiData
import com.hhkv.rustitok.database.DataBaseHistoryHelper

class HistoryDao(val dbHelper: DataBaseHistoryHelper) {


    fun insert(poi: PoiData): Long {
        val db = dbHelper.writableDatabase
        // 判断是否存在
        val all = getAll()
        val findPoi = all.find { it.title == poi.title }
        if (findPoi != null) {
            return  update(poi = findPoi, id = findPoi.id).toLong()
        }else {
            val values = ContentValues().apply {
                put(DataBaseHistoryHelper.COLUMN_TITLE,poi.title)
                put(DataBaseHistoryHelper.COLUMN_LNG,poi.lng)
                put(DataBaseHistoryHelper.COLUMN_LAT,poi.lat)
                put(DataBaseHistoryHelper.COLUMN_CITY,poi.city)
                put(DataBaseHistoryHelper.COLUMN_MAPTYPE, if(poi.mapType == MapType.baidu) "baidu" else "amp")
                put(DataBaseHistoryHelper.COLUMN_ADDRESS,poi.address)
                put(DataBaseHistoryHelper.COLUMN_DISTRICT,poi.district)
            }
            return db.insert(DataBaseHistoryHelper.TABLE_NAME,null,values)
        }

    }

    fun update(poi: PoiData, id: Long?): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DataBaseHistoryHelper.COLUMN_TITLE,poi.title)
            put(DataBaseHistoryHelper.COLUMN_LNG,poi.lng)
            put(DataBaseHistoryHelper.COLUMN_LAT,poi.lat)
            put(DataBaseHistoryHelper.COLUMN_CITY,poi.city)
            put(DataBaseHistoryHelper.COLUMN_MAPTYPE, if(poi.mapType == MapType.baidu) "baidu" else "amap")
            put(DataBaseHistoryHelper.COLUMN_ADDRESS,poi.address)
            put(DataBaseHistoryHelper.COLUMN_DISTRICT,poi.district)
        }
        return db.update(DataBaseHistoryHelper.TABLE_NAME,values,
            "${DataBaseHistoryHelper.COLUMN_ID} = ?",
            arrayOf(id.toString())
        )
    }

    fun delete(id: Long): Int {
        val db = dbHelper.writableDatabase
        return db.delete(DataBaseHistoryHelper.TABLE_NAME,"${DataBaseHistoryHelper.COLUMN_ID} = ?", arrayOf(id.toString()))
    }


    fun getBy(id: Long) : PoiData? {
        val db = dbHelper.writableDatabase
        val cursor = db.query(DataBaseHistoryHelper.TABLE_NAME,
            null,
            "${DataBaseHistoryHelper.COLUMN_ID} = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
            )
        var poiData:PoiData? = null
        while (cursor.moveToNext()){
             poiData = PoiData(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(DataBaseHistoryHelper.COLUMN_ID)),
                title = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHistoryHelper.COLUMN_TITLE)),
                lng = cursor.getDouble(cursor.getColumnIndexOrThrow(DataBaseHistoryHelper.COLUMN_LNG)),
                lat = cursor.getDouble(cursor.getColumnIndexOrThrow(DataBaseHistoryHelper.COLUMN_LAT)),
                city = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHistoryHelper.COLUMN_CITY)),
                 mapType = if(cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHistoryHelper.COLUMN_MAPTYPE)) == "baidu") MapType.baidu else MapType.amap,
                 address = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHistoryHelper.COLUMN_ADDRESS)),
                district = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHistoryHelper.COLUMN_DISTRICT)),
                createTime = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHistoryHelper.COLUMN_CREATE_TIME)),
                updateTime = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHistoryHelper.COLUMN_UPDATE_TIME)),
            )
        }
        cursor.close()
        return poiData
    }

    fun getAll(): List<PoiData>{
        val db = dbHelper.writableDatabase
        val cursor = db.query(DataBaseHistoryHelper.TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            null
        )
        var poiDatas = mutableListOf<PoiData>()
        while (cursor.moveToNext()){
           val poiData = PoiData(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(DataBaseHistoryHelper.COLUMN_ID)),
                title = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHistoryHelper.COLUMN_TITLE)),
                lng = cursor.getDouble(cursor.getColumnIndexOrThrow(DataBaseHistoryHelper.COLUMN_LNG)),
                lat = cursor.getDouble(cursor.getColumnIndexOrThrow(DataBaseHistoryHelper.COLUMN_LAT)),
                city = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHistoryHelper.COLUMN_CITY)),
               mapType = if(cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHistoryHelper.COLUMN_MAPTYPE)) == "baidu") MapType.baidu else MapType.amap,
               address = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHistoryHelper.COLUMN_ADDRESS)),
                district = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHistoryHelper.COLUMN_DISTRICT)),
                createTime = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHistoryHelper.COLUMN_CREATE_TIME)),
                updateTime = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseHistoryHelper.COLUMN_UPDATE_TIME)),
            )
            poiDatas.add(poiData)
        }
        cursor.close()
        return poiDatas
    }
}