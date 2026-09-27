package com.bigegg.tvrepairledger.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RepairRecordDao {
    @Query("SELECT * FROM repair_records ORDER BY dateEpochDay DESC, id DESC")
    fun observeAll(): Flow<List<RepairRecordEntity>>

    @Query("SELECT * FROM repair_records WHERE id = :id")
    suspend fun getById(id: Long): RepairRecordEntity?

    @Query("DELETE FROM repair_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query(
        """
        SELECT * FROM repair_records
        WHERE address LIKE '%' || :query || '%'
           OR phone LIKE '%' || :query || '%'
           OR repairDevice LIKE '%' || :query || '%'
           OR faultSymptom LIKE '%' || :query || '%'
           OR repairItem LIKE '%' || :query || '%'
           OR notes LIKE '%' || :query || '%'
        ORDER BY dateEpochDay DESC, id DESC
        """
    )
    fun search(query: String): Flow<List<RepairRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(record: RepairRecordEntity): Long

    @Update
    suspend fun update(record: RepairRecordEntity)

    @Delete
    suspend fun delete(record: RepairRecordEntity)
}
