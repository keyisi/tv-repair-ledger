package com.bigegg.tvrepairledger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.room.Room
import com.bigegg.tvrepairledger.data.AppDatabase
import com.bigegg.tvrepairledger.data.RoomRepairRepository
import com.bigegg.tvrepairledger.options.CommonOptionStore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "repair-ledger.db"
        )
            .addMigrations(*AppDatabase.ALL_MIGRATIONS)
            .build()
        val repository = RoomRepairRepository(database.repairRecordDao())
        val optionStore = CommonOptionStore(applicationContext)

        setContent {
            TvRepairLedgerApp(repository = repository, optionStore = optionStore)
        }
    }
}
