package com.mahmoudmohamaddarwish.personalattendancerecord

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Record::class], version = 1, exportSchema = false)
abstract class DB : RoomDatabase() {
    abstract fun getRecordDao(): RecordDao
}