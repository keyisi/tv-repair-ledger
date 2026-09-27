package com.bigegg.tvrepairledger.data

import com.bigegg.tvrepairledger.domain.RepairRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface RepairRepository {
    fun observeAll(): Flow<List<RepairRecord>>
    fun search(query: String): Flow<List<RepairRecord>>
    suspend fun getById(id: Long): RepairRecord?
    suspend fun save(record: RepairRecord): Long
    suspend fun delete(record: RepairRecord)
    suspend fun deleteById(id: Long)
}

class RoomRepairRepository(private val dao: RepairRecordDao) : RepairRepository {
    override fun observeAll(): Flow<List<RepairRecord>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun search(query: String): Flow<List<RepairRecord>> =
        dao.search(query).map { rows -> rows.map { it.toDomain() } }

    override suspend fun getById(id: Long): RepairRecord? =
        dao.getById(id)?.toDomain()

    override suspend fun save(record: RepairRecord): Long {
        return if (record.id == 0L) {
            dao.insert(record.toEntity())
        } else {
            dao.update(record.toEntity())
            record.id
        }
    }

    override suspend fun delete(record: RepairRecord) {
        dao.delete(record.toEntity())
    }

    override suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }
}
