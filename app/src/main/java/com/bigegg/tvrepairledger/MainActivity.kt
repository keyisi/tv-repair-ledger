package com.bigegg.tvrepairledger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowInsetsControllerCompat
import androidx.room.Room
import com.bigegg.tvrepairledger.data.AppDatabase
import com.bigegg.tvrepairledger.data.RoomRepairRepository
import com.bigegg.tvrepairledger.options.CommonOptionStore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 顶栏是深蓝底，状态栏图标必须用浅色，否则时间/电量几乎看不见。
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
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
