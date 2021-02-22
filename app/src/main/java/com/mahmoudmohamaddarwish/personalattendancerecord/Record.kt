package com.mahmoudmohamaddarwish.personalattendancerecord

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity
data class Record(
    var name: String,

    var startTime: Long,

    var attendanceTime: Long,

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
)

@Dao
interface RecordDao {
    @Insert
    suspend fun insertRecord(record: Record)

    @Update
    suspend fun updateRecord(record: Record)

    @Delete
    suspend fun deleteRecord(record: Record)

    @Query("select * from record")
    fun getAllRecords(): Flow<List<Record>>

    @Query("delete from record")
    suspend fun deleteAllRecords()
}