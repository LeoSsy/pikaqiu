package com.hhkv.rustitok.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DataBaseHistoryHelper(context: Context) : SQLiteOpenHelper(context,NAME,null,VERSION) {

    companion object {
        const val NAME = "HISTORY_DB"
        const val VERSION = 1

        const val TABLE_NAME = "history"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_LNG = "lng"
        const val COLUMN_LAT = "lat"
        const val COLUMN_CITY = "city"
        const val COLUMN_MAPTYPE = "map_type"
        const val COLUMN_ADDRESS = "address"
        const val COLUMN_DISTRICT = "district"
        const val COLUMN_CREATE_TIME = "createTime"
        const val COLUMN_UPDATE_TIME = "updateTime"


    }

    override fun onCreate(db: SQLiteDatabase?) {

        val sql = """
            CREATE TABLE $TABLE_NAME (
                 $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                                $COLUMN_TITLE TEXT NOT NULL,
                                $COLUMN_LNG REAL NOT NULL,
                                $COLUMN_LAT REAL NOT NULL,
                                $COLUMN_CITY TEXT,
                                $COLUMN_MAPTYPE TEXT,
                                $COLUMN_ADDRESS TEXT,
                                $COLUMN_DISTRICT TEXT,
                                $COLUMN_CREATE_TIME DATETIME DEFAULT CURRENT_TIMESTAMP,
                                $COLUMN_UPDATE_TIME DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """.trimIndent()
        db?.execSQL(sql)
    }


    override fun onUpgrade(
        p0: SQLiteDatabase?,
        p1: Int,
        p2: Int
    ) {
        TODO("Not yet implemented")
    }
}