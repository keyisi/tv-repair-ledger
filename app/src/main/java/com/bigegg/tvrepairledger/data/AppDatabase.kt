package com.bigegg.tvrepairledger.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [RepairRecordEntity::class], version = 3, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun repairRecordDao(): RepairRecordDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE repair_records ADD COLUMN customerName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE repair_records ADD COLUMN warrantyDays INTEGER NOT NULL DEFAULT 90")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE repair_records ADD COLUMN repairDevice TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}
